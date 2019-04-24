package com.leyou.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by RookieWangZhiWei on 2019/4/20.
 */
@FeignClient(value = "item-service")
public interface BrandClient extends BrandApi {
}
