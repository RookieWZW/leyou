package com.leyou.user.service;

import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import com.leyou.utils.CodeUtils;

import com.leyou.utils.JsonUtils;
import com.leyou.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by RookieWangZhiWei on 2019/4/24.
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "user:code:phone";

    private static final String KEY_PREFIX2 = "leyou:user:info";

    private Logger logger = LoggerFactory.getLogger(UserService.class);


    public Boolean checkData(String data, Integer type) {
        User user = new User();
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                return null;
        }

        return this.userMapper.selectCount(user) == 0;
    }

    public Boolean sendVerifyCode(String phone) {
        String code = NumberUtils.generateCode(6);

        try {
            Map<String, String> msg = new HashMap<>();
            msg.put("phone", phone);
            msg.put("code", code);

            this.amqpTemplate.convertAndSend("leyou.sms.exchange", "sms.verify.code", msg);

            this.stringRedisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);

            return true;
        } catch (Exception e) {
            logger.error("发送短信失败。phone：{}，code：{}", phone, code);
            return false;
        }
    }

    public Boolean register(User user, String code) {
        String key = KEY_PREFIX + user.getPassword();

        String codeCache = this.stringRedisTemplate.opsForValue().get(key);

        if (!codeCache.equals(code)) {
            return false;
        }

        user.setId(null);
        user.setCreated(new Date());

        String encodePassword = CodeUtils.passwordBcryptEncode(user.getUsername().trim(), user.getPassword().trim());

        user.setPassword(encodePassword);

        boolean result = this.userMapper.insertSelective(user) == 1;
        if (result) {
            try {
                this.stringRedisTemplate.delete(KEY_PREFIX + user.getPhone());

            } catch (Exception e) {
                logger.error("删除缓存验证码失败，code:{}", code, e);
            }
        }
        return result;
    }

    public User queryUser(String username, String password) {

        BoundHashOperations<String, Object, Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX2);

        String userStr = (String) hashOperations.get(username);

        User user;

        if (StringUtils.isEmpty(userStr)) {
            User record = new User();
            record.setUsername(username);
            user = this.userMapper.selectOne(record);
            System.out.println("username::"+username+"    user:" + user);
            if (user!=null) {

                hashOperations.put(user.getUsername(), JsonUtils.serialize(user));
            }
        } else {
            user = JsonUtils.parse(userStr, User.class);
        }

        if (user == null) {
            return null;
        }

        boolean result = CodeUtils.passwordConfirm(username + password, user.getPassword());
        System.out.println("result:"+ result);
        if (!result) {
            return null;
        }

        return user;
    }

    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        User user = this.queryUser(username, oldPassword);

        if (user == null) {
            return false;
        }

        User updateUser = new User();

        updateUser.setId(user.getId());

        String encodePassword = CodeUtils.passwordBcryptEncode(username.trim(), newPassword.trim());

        updateUser.setPassword(encodePassword);

        this.userMapper.updateByPrimaryKeySelective(updateUser);

        BoundHashOperations<String, Object, Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX2 + username);

        hashOperations.delete(user);

        return true;
    }
}
