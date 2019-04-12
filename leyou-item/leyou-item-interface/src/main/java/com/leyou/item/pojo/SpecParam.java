package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by RookieWangZhiWei on 2019/4/12.
 */
@Data
@Table(name = "tb_spec_param")
public class SpecParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Long cid;

    private Long groupId;

    private String name;

    @Column(name = "`numeric`")
    private Boolean numeric;

    private String unit;

    private Boolean generic;

    private Boolean searching;

    private String segments;


}
