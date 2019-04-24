package com.leyou.user.pojo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * Created by RookieWangZhiWei on 2019/4/24.
 */
@Data
@Table(name = "tb_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Length(min = 4,max = 15,message = "用户名只能在4~15位之间")
    private String username;

    @Length(min = 6,max = 25,message = "密码只能在6~25位之间")
    private String password;

    @Pattern(regexp = "^1[35678]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private Date created;
}
