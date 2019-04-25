package com.leyou.cart.pojo;

import lombok.Data;

/**
 * Created by RookieWangZhiWei on 2019/4/25.
 */
@Data
public class Cart {
    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片
     */
    private String image;

    /**
     * 加入购物车时的价格
     */
    private Long price;

    /**
     * 购买数量
     */
    private Integer num;

    /**
     * 商品规格参数
     */
    private String ownSpec;
}
