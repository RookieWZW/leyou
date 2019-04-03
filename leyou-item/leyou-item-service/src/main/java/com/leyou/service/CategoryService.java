package com.leyou.service;

import com.leyou.item.pojo.Category;
import com.leyou.mapper.CategoryMapper;
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


    public List<Category> queryListByParent(Long pid){
        Category category = new Category();
        category.setParentId(pid);

        return this.categoryMapper.select(category);
    }
}
