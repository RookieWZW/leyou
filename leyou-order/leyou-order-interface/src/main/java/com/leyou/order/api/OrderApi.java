package com.leyou.order.api;

import com.leyou.order.pojo.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
@RequestMapping("order")
public interface OrderApi {

    /**
     * 创建订单
     * @param seck
     * @param order
     * @return
     */
    @PostMapping
    ResponseEntity<List<Long>> createOrder(@RequestParam("seck") String seck, @RequestBody @Valid Order order);


    /**
     * 修改订单状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping("{id}/{status}")
    ResponseEntity<Boolean> updateOrderStatus(@PathVariable("id") Long id,@PathVariable("status") Integer status);
}
