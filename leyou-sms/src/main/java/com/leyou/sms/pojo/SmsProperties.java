package com.leyou.sms.pojo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
/**
 * Created by RookieWangZhiWei on 2019/4/24.
 */
@Data
@Configuration
@RefreshScope
public class SmsProperties {

    @Value("${leyou.sms.accessKeyId}")
    private String accessKeyId;

    @Value("${leyou.sms.accessKeySecret}")
    private String accessKeySecret;

    @Value("${leyou.sms.signName}")
    private String signName;

    @Value("${leyou.sms.verifyCodeTemplate}")
    private String verifyCodeTemplate;
}
