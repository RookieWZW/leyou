package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/3.
 */
public interface BrandMapper extends Mapper<Brand>,SelectByIdListMapper<Brand,Long> {

    @Insert("INSERT INTO tb_category_brand (category_id, brand_id) VALUES (#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid") Long cid, @Param("bid") Long id);


    @Delete("DELETE FROM tb_category_brand WHERE brand_id=#{bid}")
    void deleteByBrandIdInCategoryBrand(@Param("bid") Long bid);

    @Select("select b.* from tb_brand b left join tb_category_brand cb ON b.id=cb.brand_id WHERE cb.category_id=#{cid}")
    List<Brand> queryBrandByCategoryId(Long cid);
}
