package com.leyou.item.controller;

import com.leyou.item.pojo.Specification;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/12.
 */
@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;



    @GetMapping("{id}")
    public ResponseEntity<String> querySpecificationByCategoryId(@PathVariable("id") Long id){
        Specification spec = this.specificationService.queryById(id);
        if (spec == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spec.getSpecifications());
    }

    @PostMapping
    public ResponseEntity<Void> saveSpecification(Specification specification){
        this.specificationService.saveSpecification(specification);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateSpecification(Specification specification){
        this.specificationService.updateSpecification(specification);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteSpecification(@PathVariable("id")Long id){
        Specification specification = new Specification();
        specification.setCategoryId(id);
        this.specificationService.deleteSpecification(specification);
        return ResponseEntity.status(HttpStatus.OK).build();
    }



}
