package com.leyou.client;

/**
 * Created by RookieWangZhiWei on 2019/4/23.
 */
import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service")
public interface BrandClient extends BrandApi {
}