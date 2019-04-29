package com.leyou.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
@EnableConfigServer
@SpringBootApplication
public class LyConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(LyConfigApplication.class,args);
    }
}
