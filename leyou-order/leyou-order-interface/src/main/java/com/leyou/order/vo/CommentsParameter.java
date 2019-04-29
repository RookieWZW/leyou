package com.leyou.order.vo;

import com.leyou.comments.pojo.Review;


/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
public class CommentsParameter {

    private Long orderId;

    private Review review;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }
}
