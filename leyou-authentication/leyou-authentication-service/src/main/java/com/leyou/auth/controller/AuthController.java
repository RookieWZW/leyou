package com.leyou.auth.controller;

import com.leyou.auth.properties.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;

/**
 * Created by RookieWangZhiWei on 2019/4/11.
 */
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;


    @Autowired
    private JwtProperties prop;


    @PostMapping("accredit")
    public ResponseEntity<Void> authentication(@RequestParam("username") String username, @RequestParam("password") String password,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        String token = this.authService.authentication(username, password);
        if (StringUtils.isBlank(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        CookieUtils.setCookie(request,response,prop.getCookieName(),token,prop.getCookieMaxAge(),true);
        return ResponseEntity.ok().build();
    }
}
