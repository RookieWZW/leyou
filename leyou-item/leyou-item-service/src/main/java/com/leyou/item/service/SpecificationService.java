package com.leyou.item.service;

import com.leyou.item.mapper.SpecificationMapper;
import com.leyou.item.pojo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/12.
 */
@Service
public class SpecificationService {


    @Autowired
    private SpecificationMapper specificationMapper;

    public Specification queryById(Long id) {

        return this.specificationMapper.selectByPrimaryKey(id);
    }

    public void saveSpecification(Specification specification) {
        this.specificationMapper.insert(specification);
    }

    public void updateSpecification(Specification specification) {
        this.specificationMapper.updateByPrimaryKeySelective(specification);
    }
    public void deleteSpecification(Specification specification) {
        this.specificationMapper.deleteByPrimaryKey(specification);
    }
}
