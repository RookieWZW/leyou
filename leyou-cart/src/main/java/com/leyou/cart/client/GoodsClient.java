package com.leyou.cart.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by RookieWangZhiWei on 2019/4/25.
 */
@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {
}
