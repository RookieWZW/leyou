package com.leyou.item.service;

import com.leyou.item.pojo.Category;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/3.
 */
@Service
public class CategoryService {


    @Autowired
    private CategoryMapper categoryMapper;


    public List<Category> queryListByParent(Long pid) {
        Category category = new Category();
        category.setParentId(pid);

        return this.categoryMapper.select(category);
    }

    public List<Category> queryCategoryByBid(Long bid) {

        return this.categoryMapper.queryCategoryByBid(bid);

    }
}
