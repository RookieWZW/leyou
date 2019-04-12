package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/12.
 */
@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> querySpecGroups(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);

        return this.specGroupMapper.select(specGroup);
    }

    public List<SpecParam> querySpecParams(Long gid) {
        SpecParam t = new SpecParam();
        t.setGroupId(gid);
        return this.specParamMapper.select(t);
    }
}
