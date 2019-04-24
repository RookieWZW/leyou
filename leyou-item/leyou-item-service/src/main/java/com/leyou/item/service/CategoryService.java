package com.leyou.item.service;

import com.leyou.item.pojo.Category;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
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

        return this.categoryMapper.queryByBrandId(bid);

    }



    public void saveCategory(Category category) {
        category.setId(null);

        this.categoryMapper.insert(category);
        Category parent = new Category();
        parent.setId(category.getParentId());
        parent.setIsParent(true);
        this.categoryMapper.updateByPrimaryKeySelective(parent);
    }

    public void deleteCategory(Long id) {

        Category category = this.categoryMapper.selectByPrimaryKey(id);

        if (category.getIsParent()) {
            List<Category> list = new ArrayList<>();

            queryAllLeafNode(category, list);


            List<Category> list2 = new ArrayList<>();

            queryAllNode(category, list2);

            for (Category c : list2) {
                this.categoryMapper.delete(c);
            }

            for (Category c : list) {
                this.categoryMapper.deleteByCategoryIdInCategoryBrand(c.getId());
            }
        } else {
            Example example = new Example(Category.class);
            example.createCriteria().andEqualTo("parentId", category.getParentId());

            List<Category> list = this.categoryMapper.selectByExample(example);
            if (list.size() != 1) {
                this.categoryMapper.deleteByPrimaryKey(category.getId());
                this.categoryMapper.deleteByCategoryIdInCategoryBrand(category.getId());
            } else {
                this.categoryMapper.deleteByPrimaryKey(category.getId());

                Category parent = new Category();
                parent.setId(category.getParentId());
                parent.setIsParent(false);
                this.categoryMapper.updateByPrimaryKey(parent);


                this.categoryMapper.deleteByCategoryIdInCategoryBrand(category.getId());

            }
        }
    }

    public List<String> queryNameByIds(List<Long> asList) {
        List<String> names = new ArrayList<>();
        if (asList != null && asList.size() != 0) {
            for (Long id : asList) {
                names.add(this.categoryMapper.queryNameById(id));
            }
        }
        return names;
    }

    public List<Category> queryLast() {
        List<Category> last = this.categoryMapper.selectLast();

        return last;
    }

    public List<Category> queryCategoryByIds(List<Long> ids) {
        return this.categoryMapper.selectByIdList(ids);
    }

    public List<Category> queryAllCategoryLevelByCid3(Long id) {

        List<Category> categoryList = new ArrayList<>();
        Category category = this.categoryMapper.selectByPrimaryKey(id);
        while (category.getParentId() != 0) {
            categoryList.add(category);
            category = this.categoryMapper.selectByPrimaryKey(category.getParentId());
        }

        categoryList.add(category);
        return categoryList;
    }

    public void queryAllLeafNode(Category category, List<Category> leafNode) {
        if (!category.getIsParent()) {
            leafNode.add(category);
        }

        Example example = new Example(Category.class);
        example.createCriteria().andEqualTo("parentId", category.getId());
        List<Category> list = this.categoryMapper.selectByExample(example);

        for (Category category1 : list) {
            queryAllLeafNode(category1, leafNode);
        }
    }

    public void queryAllNode(Category category, List<Category> node) {
        node.add(category);
        Example example = new Example(Category.class);
        example.createCriteria().andEqualTo("parentId", category.getId());
        List<Category> list = this.categoryMapper.selectByExample(example);

        for (Category category1 : list) {
            queryAllNode(category1, node);
        }

    }
}
