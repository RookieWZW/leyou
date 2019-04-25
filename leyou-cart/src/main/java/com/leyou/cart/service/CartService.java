package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.item.pojo.Sku;
import com.leyou.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by RookieWangZhiWei on 2019/4/25.
 */
@Service
public class CartService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    private static String KEY_PREFIX = "leyou:cart:uid:";

    private final Logger logger = LoggerFactory.getLogger(CartService.class);



    public void addCart(Cart cart){
        UserInfo userInfo = LoginInterceptor.getLoginUser();

        String key = KEY_PREFIX+userInfo.getId();

        BoundHashOperations<String,Object,Object> hashOperations = this.stringRedisTemplate.boundHashOps(key);


        Long skuId = cart.getSkuId();

        Integer num = cart.getNum();

        Boolean result = hashOperations.hasKey(skuId.toString());

        if (result){
            String json = hashOperations.get(skuId.toString()).toString();
            cart = JsonUtils.parse(json,Cart.class);

            cart.setNum(cart.getNum() + num);
        }else{
            cart.setUserId(userInfo.getId());

            Sku sku = this.goodsClient.querySkuById(skuId);

            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
        }

        hashOperations.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
    }


    public List<Cart> queryCartList(){
        UserInfo userInfo = LoginInterceptor.getLoginUser();


        String key = KEY_PREFIX+userInfo.getId();
        if (!this.stringRedisTemplate.hasKey(key)) {

            return null;
        }
        BoundHashOperations<String,Object,Object> hashOperations = this.stringRedisTemplate.boundHashOps(key);
        List<Object> carts = hashOperations.values();

        if (CollectionUtils.isEmpty(carts)){
            return null;
        }

        return carts.stream().map( o -> JsonUtils.parse(o.toString(),Cart.class)).collect(Collectors.toList());

    }

    public void updateNum(Long skuId,Integer num){
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String,Object,Object> hashOperations = this.stringRedisTemplate.boundHashOps(key);

        String json = hashOperations.get(skuId.toString()).toString();
        Cart cart = JsonUtils.parse(json,Cart.class);
        cart.setNum(num);

        hashOperations.put(skuId.toString(),JsonUtils.serialize(cart));
    }

    public void deleteCart(String skuId) {

        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String,Object,Object> hashOperations = this.stringRedisTemplate.boundHashOps(key);

        hashOperations.delete(skuId);
    }
}
