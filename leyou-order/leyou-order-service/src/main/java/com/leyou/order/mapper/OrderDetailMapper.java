package com.leyou.order.mapper;

import com.leyou.order.pojo.OrderDetail;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

import java.util.List;


/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
public interface OrderDetailMapper extends Mapper<OrderDetail>, InsertListMapper<OrderDetail> {

}
