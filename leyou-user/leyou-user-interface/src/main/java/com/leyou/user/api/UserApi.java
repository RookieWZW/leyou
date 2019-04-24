package com.leyou.user.api;

import com.leyou.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by RookieWangZhiWei on 2019/4/24.
 */
public interface UserApi {

    @GetMapping("query")
    User queryUser(@RequestParam("username")String username,@RequestParam("password")String password);
}
