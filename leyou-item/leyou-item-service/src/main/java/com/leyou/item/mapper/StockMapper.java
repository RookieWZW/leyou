package com.leyou.item.mapper;

import com.leyou.item.pojo.Stock;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by RookieWangZhiWei on 2019/4/15.
 */
public interface StockMapper extends Mapper<Stock>, SelectByIdListMapper<Stock,Long> {
}