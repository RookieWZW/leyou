package com.leyou.upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Created by RookieWangZhiWei on 2019/4/11.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LeyouUploadService {

    public static void main(String[] args) {
        SpringApplication.run(LeyouUploadService.class, args);
    }
}
