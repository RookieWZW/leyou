package com.leyou.item.api;

import com.leyou.item.pojo.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.xml.ws.RequestWrapper;
import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/20.
 */
@RequestMapping("category")
public interface CategoryApi {

    @GetMapping("names")
    ResponseEntity<List<String>> queryNameByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("all")
    ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids")List<Long> ids);
}
