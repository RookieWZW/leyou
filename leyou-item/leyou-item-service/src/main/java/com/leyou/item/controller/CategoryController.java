package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/3.
 */
@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    /**
     * @param pid
     * @return 根据父id查询分类列表
     *
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryByParentId(@RequestParam(value = "pid", defaultValue = "0") Long pid) {
        List<Category> list = this.categoryService.queryListByParent(pid);

        if (list == null || list.size() < 1) {
            return new ResponseEntity<List<Category>>(HttpStatus.NOT_FOUND);

        }
        return ResponseEntity.ok(list);
    }

    /**
     *
     * @param category
     * @return 保存新的分类
     */
    @PostMapping
    public ResponseEntity<Void> saveCategory(Category category) {
        System.out.println(category);
        this.categoryService.saveCategory(category);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoryByBid(@PathVariable("bid") Long bid) {
        List<Category> list = this.categoryService.queryCategoryByBid(bid);

        if (list == null || list.size() < 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }


    /**
     *
     * @param cid
     * @return  删除分类节点
     */
    @DeleteMapping("cid/{cid}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("cid") Long cid) {
        this.categoryService.deleteCategory(cid);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("names")
    public ResponseEntity<List<String>> queryNameByIds(@RequestParam("ids") List<Long> ids) {
        List<String> list = categoryService.queryNameByIds(ids);

        if (list == null || list.size() < 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.ok(list);
        }

    }

    @GetMapping("all")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids) {
        List<Category> list = categoryService.queryCategoryByIds(ids);
        if (list == null || list.size() < 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.ok(list);
        }
    }

    @GetMapping("all/level/{cid3}")
    public ResponseEntity<List<Category>> queryAllCategoryLevelByCid3(@PathVariable("cid3") Long cid3) {
        List<Category> list = categoryService.queryAllCategoryLevelByCid3(cid3);
        if (list == null || list.size() < 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.ok(list);
        }
    }
}
