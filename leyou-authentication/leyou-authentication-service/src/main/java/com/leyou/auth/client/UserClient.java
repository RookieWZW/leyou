package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by RookieWangZhiWei on 2019/4/24.
 */
@FeignClient(value = "user-service")
public interface UserClient extends UserApi {
}
