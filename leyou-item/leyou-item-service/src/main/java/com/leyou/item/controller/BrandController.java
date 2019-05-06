package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.service.BrandService;
import com.leyou.item.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/3.
 */
@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;


    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key) {
        PageResult<Brand> result = this.brandService.queryBrandByPageAndSort(page, rows, sortBy, desc, key);

        if (result == null || result.getItems().size() == 0) {
            return new ResponseEntity<PageResult<Brand>>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("categories") List<Long> categories) {
        this.brandService.saveBrand(brand, categories);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand, @RequestParam("categories") List<Long> categories) {
        this.brandService.updateBrand(brand, categories);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("bid/{bid}")
    public ResponseEntity<Void> deleteBrand(@PathVariable("bid") String bid) {
        String separator = "-";
        if (bid.contains(separator)) {
            String[] ids = bid.split(separator);
            for (String id : ids) {
                this.brandService.deleteBrand(Long.parseLong(id));
            }
        } else {
            this.brandService.deleteBrand(Long.parseLong(bid));
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/cid_bid/{bid}")
    public ResponseEntity<Void> deleteByBrandIdInCategoryBrand(@PathVariable("bid") Long bid) {
        this.brandService.deleteByBrandIdInCategoryBrand(bid);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCategoryId(@PathVariable("cid") Long cid) {
        List<Brand> list = this.brandService.queryBrandByCategoryId(cid);

        if (list == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(list);
    }

    @GetMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids) {
        List<Brand> list = this.brandService.queryBrandByBrandIds(ids);
        if (list == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        }
        return ResponseEntity.ok(list);
    }
}
