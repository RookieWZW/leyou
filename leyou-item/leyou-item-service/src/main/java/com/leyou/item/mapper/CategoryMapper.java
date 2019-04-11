package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/3.
 */
public interface CategoryMapper extends Mapper<Category> {

    @Select("SELECT * from tb_category WHERE id IN (select category_id FROM tb_category_brand where brand_id = #{bid})")
    List<Category> queryCategoryByBid(Long bid);
}
