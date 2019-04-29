package com.leyou.order.service;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.auth.entity.UserInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Stock;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.*;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.pojo.SeckillOrder;
import com.leyou.order.service.OrderService;
import com.leyou.order.service.OrderStatusService;
import com.leyou.order.vo.OrderStatusMessage;
import com.leyou.utils.IdWorker;
import com.leyou.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.sound.midi.SoundbankResource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
@Service
public class OrderService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Order order) {
        long orderId = idWorker.nextId();

        UserInfo userInfo = LoginInterceptor.getLoginUser();

        order.setBuyerNick(userInfo.getUsername());
        order.setBuyerRate(false);
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setUserId(userInfo.getId());

        this.orderMapper.insertSelective(order);


        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());

        orderStatus.setStatus(1);

        this.orderStatusMapper.insertSelective(orderStatus);


        order.getOrderDetails().forEach(orderDetail -> {
            orderDetail.setOrderId(orderId);
        });

        this.orderDetailMapper.insertList(order.getOrderDetails());

        order.getOrderDetails().forEach(orderDetail -> this.stockMapper.reduceStock(orderDetail.getSkuId(), orderDetail.getNum()));

        return orderId;

    }

    public Order queryOrderById(Long id) {
        Order order = this.orderMapper.selectByPrimaryKey(id);

        Example example = new Example(OrderDetail.class);

        example.createCriteria().andEqualTo("orderId", id);

        List<OrderDetail> orderDetail = this.orderDetailMapper.selectByExample(example);

        orderDetail.forEach(System.out::println);

        OrderStatus orderStatus = this.orderStatusMapper.selectByPrimaryKey(order.getOrderId());

        order.setOrderDetails(orderDetail);

        order.setStatus(orderStatus.getStatus());

        return order;
    }

    public PageResult<Order> queryUserOrderList(Integer page, Integer rows, Integer status) {
        try {
            PageHelper.startPage(page, rows);
            UserInfo userInfo = LoginInterceptor.getLoginUser();


            Page<Order> pageInfo = (Page<Order>) this.orderMapper.queryOrderList(userInfo.getId(), status);

            List<Order> orderList = pageInfo.getResult();

            orderList.forEach(order -> {
                Example example = new Example(OrderDetail.class);
                example.createCriteria().andEqualTo("orderId", order.getOrderId());
                List<OrderDetail> orderDetailList = this.orderDetailMapper.selectByExample(example);
                order.setOrderDetails(orderDetailList);

            });
            return new PageResult<>(pageInfo.getTotal(), (long) pageInfo.getPages(), orderList);
        } catch (Exception e) {
            logger.error("查询订单出错", e);
            return null;
        }

    }

    public Boolean updateOrderStatus(Long id, Integer status) {
        UserInfo userInfo = LoginInterceptor.getLoginUser();

        Long spuId = this.goodsClient.querySkuById(findSkuIdByOrderId(id)).getSpuId();

        OrderStatus orderStatus = new OrderStatus();

        orderStatus.setOrderId(id);
        orderStatus.setStatus(status);

        OrderStatusMessage orderStatusMessage = new OrderStatusMessage(id, userInfo.getId(), userInfo.getUsername(), spuId, 1);
        OrderStatusMessage orderStatusMessage2 = new OrderStatusMessage(id, userInfo.getId(), userInfo.getUsername(), spuId, 2);

        switch (status) {
            case 2:
                orderStatus.setPaymentTime(new Date());
                break;
            case 3:
                orderStatus.setConsignTime(new Date());
                orderStatusService.sendMessage(orderStatusMessage);
                orderStatusService.sendMessage(orderStatusMessage2);
                break;
            case 4:
                orderStatus.setEndTime(new Date());
                orderStatusService.sendMessage(orderStatusMessage2);
                break;
            case 5:
                orderStatus.setCloseTime(new Date());
                break;
            case 6:
                orderStatus.setCommentTime(new Date());
                break;
            default:
                return null;


        }


        int count = this.orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        return count == 1;

    }


    public List<Long> querySkuIdByOrderId(Long id) {
        Example example = new Example(OrderDetail.class);
        example.createCriteria().andEqualTo("orderId", id);
        List<OrderDetail> orderDetailList = this.orderDetailMapper.selectByExample(example);
        List<Long> ids = new ArrayList<>();
        orderDetailList.forEach(orderDetail -> ids.add(orderDetail.getSkuId()));
        return ids;
    }

    public OrderStatus queryOrderStatusById(Long id) {
        return this.orderStatusMapper.selectByPrimaryKey(id);
    }


    public List<Long> queryStock(Order order) {
        List<Long> skuId = new ArrayList<>();
        order.getOrderDetails().forEach(orderDetail -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(orderDetail.getSkuId());
            if (stock.getStock() - orderDetail.getNum() < 0) {
                //先判断库存是否充足
                skuId.add(orderDetail.getSkuId());
            }
        });

        return skuId;
    }


    public Long findSkuIdByOrderId(Long id) {
        Example example = new Example(OrderDetail.class);
        example.createCriteria().andEqualTo("orderId", id);
        List<OrderDetail> orderDetail = this.orderDetailMapper.selectByExample(example);
        return orderDetail.get(0).getSkuId();
    }

}

