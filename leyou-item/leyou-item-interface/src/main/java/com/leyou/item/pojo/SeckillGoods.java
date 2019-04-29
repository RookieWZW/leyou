package com.leyou.item.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by RookieWangZhiWei on 2019/4/29.
 */
@Data
@Table(name = "tb_seckill_sku")
public class SeckillGoods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 秒杀商品的id
     */
    private Long skuId;
    /**
     * 秒杀开始时间
     */
    private Date startTime;
    /**
     * 秒杀结束时间
     */
    private Date endTime;
    /**
     * 秒杀价格
     */
    private Double seckillPrice;
    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 是否可以秒杀
     */
    private Boolean enable;

    /**
     * 秒杀库存
     */
    @JsonIgnore
    @Transient
    private Integer stock;

    @JsonIgnore
    @Transient
    private Integer seckillTotal;
}
