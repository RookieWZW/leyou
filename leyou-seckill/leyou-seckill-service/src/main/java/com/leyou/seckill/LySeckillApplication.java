package com.leyou.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("com.leyou.seckill.mapper")
public class LySeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(LySeckillApplication.class,args);
    }
}
