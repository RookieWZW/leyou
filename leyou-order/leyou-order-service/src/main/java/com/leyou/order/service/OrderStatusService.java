package com.leyou.order.service;


import com.leyou.order.service.OrderStatusService;
import com.leyou.order.vo.CommentsParameter;
import com.leyou.order.vo.OrderStatusMessage;
import com.leyou.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
@Service
public class OrderStatusService {
    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusService.class);

    public void sendMessage(OrderStatusMessage orderStatusMessage){
        String json = JsonUtils.serialize(orderStatusMessage);

        MessageProperties properties;

        if (orderStatusMessage.getType() ==1){
            properties = MessagePropertiesBuilder.newInstance().setExpiration("60000").setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();

        }else {
            properties = MessagePropertiesBuilder.newInstance().setExpiration("90000").setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
        }

        Message message = MessageBuilder.withBody(json.getBytes()).andProperties(properties).build();

        try{
            this.amqpTemplate.convertAndSend("","leyou.order.delay.queue",message);

        }catch (Exception e){
            LOGGER.error("延时消息发送异常，订单号为：id：{}，用户id为：{}",orderStatusMessage.getOrderId(),orderStatusMessage.getUserId(),e);

        }
    }

    public void sendComments(CommentsParameter commentsParameter){
        String json = JsonUtils.serialize(commentsParameter);
        try {
            this.amqpTemplate.convertAndSend("leyou.comments.exchange","user.comments", json);
        }catch (Exception e){
            LOGGER.error("评论消息发送异常，订单id：{}",commentsParameter.getOrderId(),e);
        }
    }
}
