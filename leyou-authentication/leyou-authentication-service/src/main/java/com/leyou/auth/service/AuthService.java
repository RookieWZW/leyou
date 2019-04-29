package com.leyou.auth.service;

/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
public interface AuthService {
    /**
     * 用户授权
     * @param username
     * @param password
     * @return
     */
    String authentication(String username, String password);
}
