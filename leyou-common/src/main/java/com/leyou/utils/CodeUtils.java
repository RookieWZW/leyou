package com.leyou.utils;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Created by RookieWangZhiWei on 2019/4/22.
 */
public class CodeUtils {

    public static String passwordBcryptEncode(String username,String password){

        return new BCryptPasswordEncoder().encode(username + password);
    }

    public static Boolean passwordConfirm(String rawPassword,String encodePassword){
        return new BCryptPasswordEncoder().matches(rawPassword,encodePassword);
    }
}
