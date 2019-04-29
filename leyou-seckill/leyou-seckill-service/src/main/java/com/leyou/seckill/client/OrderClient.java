package com.leyou.seckill.client;

import com.leyou.order.api.OrderApi;
import com.leyou.seckill.config.OrderConfig;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
@FeignClient(value = "order-service",configuration = OrderConfig.class)
public interface OrderClient extends OrderApi {
}
