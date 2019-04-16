package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by RookieWangZhiWei on 2019/4/15.
 */
@Data
@Table(name = "tb_specification")
public class Specification {

    @Id
    private Long categoryId;
    private String specifications;
}
