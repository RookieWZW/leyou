package com.leyou.order.mapper;

import com.leyou.item.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;


/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
public interface StockMapper extends Mapper<Stock> {


    /**
     * 更新对应商品的库存,且库存必须大于0，否则回滚。
     * @param skuId
     * @param num
     */
    @Update("update tb_stock set stock = stock - #{num} where sku_id = #{skuId} and stock > 0")
    void reduceStock(@Param("skuId") Long skuId, @Param("num") Integer num);

    /**
     * 更新对应商品的秒杀库存,且库存必须大于0，否则回滚。
     * @param skuId
     * @param num
     */
    @Update("update tb_stock set seckill_stock = seckill_stock - #{num} where sku_id = #{skuId} and seckill_stock > 0")
    void reduceSeckStock(@Param("skuId")Long skuId, @Param("num")Integer num);
}
