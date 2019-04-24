package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/3.
 */
public interface CategoryMapper extends Mapper<Category>, SelectByIdListMapper<Category, Long> {


    @Select("SELECT * from tb_category WHERE id IN (select category_id FROM tb_category_brand where brand_id = #{bid})")
    List<Category> queryByBrandId(@Param(("bid")) Long bid);

    @Delete("delete from tb_category_brand where category_id = #{cid}")
    void deleteByCategoryIdInCategoryBrand(@Param("cid") Long cid);


    @Select("select name from tb_category where id =#{id}")
    String queryNameById(Long id);


    @Select("select * from tb_category where id = (select MAX(id) from tb_category)")
    List<Category> selectLast();

}
