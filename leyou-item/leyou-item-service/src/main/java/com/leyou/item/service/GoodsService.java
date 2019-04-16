package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import com.netflix.discovery.converters.Auto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by RookieWangZhiWei on 2019/4/14.
 */
@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    public PageResult<SpuBo> querySpuPage(Integer page, Integer rows, Boolean saleable, String key) {

        PageHelper.startPage(page, Math.min(rows, 200));

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        // 是否过滤上下架
        if (saleable != null) {
            criteria.orEqualTo("saleable", saleable);
        }
        // 是否模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        Page<Spu> pageInfo = (Page<Spu>) this.spuMapper.selectByExample(example);

        List<SpuBo> list = pageInfo.getResult().stream().map(spu -> {
            SpuBo spuBo = new SpuBo();

            BeanUtils.copyProperties(spu, spuBo);

            List<String> names = this.categoryService.queryNameByIds(
                    Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

            spuBo.setCname(StringUtils.join(names, "/"));

            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());

            spuBo.setBname(brand.getName());

            return spuBo;
        }).collect(Collectors.toList());


        return new PageResult<>(pageInfo.getTotal(), list);


    }

    @Transactional
    public void save(SpuBo spu) {
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        this.spuMapper.insert(spu);

        spu.getSpuDetail().setSpuId(spu.getId());
        this.spuDetailMapper.insert(spu.getSpuDetail());

        saveSkuAndStock(spu.getSkus(), spu.getId());
    }

    private void saveSkuAndStock(List<Sku> skus, Long spuId) {
        for (Sku sku : skus) {
            if (!sku.getEnable()) {
                continue;
            }
            // 保存sku
            sku.setSpuId(spuId);
            // 初始化时间
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);

            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        }
    }

    public SpuDetail querySpuDetailBySpuId(Long id) {
        return this.spuDetailMapper.selectByPrimaryKey(id);
    }

    public List<Sku> querySkuBySpuId(Long id) {
        Sku record = new Sku();
        record.setSpuId(id);
        List<Sku> skus = this.skuMapper.select(record);
        for (Sku sku : skus) {
            // 同时查询出库存
            sku.setStock(this.stockMapper.selectByPrimaryKey(sku.getId()).getStock());
        }
        return skus;
    }

    public SpuBo queryGoodsById(Long id) {

        Spu spu = this.spuMapper.selectByPrimaryKey(id);
        SpuDetail spuDetail = this.spuDetailMapper.selectByPrimaryKey(spu.getId());

        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId", spu.getId());

        List<Sku> skuList = this.skuMapper.selectByExample(example);
        List<Long> skuIdList = new ArrayList<>();

        for (Sku sku : skuList) {
            skuIdList.add(sku.getId());
        }

        List<Stock> stocks = this.stockMapper.selectByIdList(skuIdList);

        for (Sku sku : skuList) {
            for (Stock stock : stocks) {
                if (sku.getId().equals(stock.getSkuId())) {
                    sku.setStock(stock.getStock());
                }
            }
        }

        SpuBo spuBo = new SpuBo(spu.getBrandId(), spu.getCid1(), spu.getCid2(), spu.getCid3(), spu.getTitle(),
                spu.getSubTitle(), spu.getSaleable(), spu.getValid(), spu.getCreateTime(), spu.getLastUpdateTime());
        spuBo.setSpuDetail(spuDetail);
        spuBo.setSkus(skuList);
        return spuBo;
    }


    @Transactional
    public void updateGoods(SpuBo spuBo) {
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setLastUpdateTime(new Date());
        this.spuMapper.updateByPrimaryKeySelective(spuBo);


        SpuDetail spuDetail = spuBo.getSpuDetail();
        String oldTemp = this.spuDetailMapper.selectByPrimaryKey(spuBo.getId()).getSpecTemplate();

        if (spuDetail.getSpecTemplate().equals(oldTemp)) {
            updateSkuAndStock(spuBo.getSkus(), spuBo.getId(), true);
        } else {
            updateSkuAndStock(spuBo.getSkus(), spuBo.getId(), false);
        }
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.updateByPrimaryKeySelective(spuDetail);

    }

    public Sku querySkuById(Long id) {
        return this.skuMapper.selectByPrimaryKey(id);
    }

    private void updateSkuAndStock(List<Sku> skus, Long id, boolean tag) {
        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId", id);

        List<Sku> oldList = this.skuMapper.selectByExample(example);
        if (tag) {
            int count = 0;
            for (Sku sku : skus) {
                if (!sku.getEnable()) {
                    continue;
                }
                for (Sku old : oldList) {
                    if (sku.getOwnSpec().equals(old.getOwnSpec())) {
                        System.out.println("update");

                        List<Sku> list = this.skuMapper.select(old);

                        if (sku.getPrice() == null) {
                            sku.setPrice(0L);
                        }
                        if (sku.getStock() == null) {
                            sku.setStock(0L);
                        }
                        sku.setId(list.get(0).getId());
                        sku.setCreateTime(list.get(0).getCreateTime());
                        sku.setSpuId(list.get(0).getSpuId());
                        sku.setLastUpdateTime(new Date());
                        this.skuMapper.updateByPrimaryKey(sku);

                        Stock stock = new Stock();
                        stock.setSkuId(sku.getId());
                        stock.setStock(sku.getStock());
                        this.stockMapper.updateByPrimaryKeySelective(stock);

                        oldList.remove(old);
                        break;

                    } else {
                        count++;
                    }
                }
                if (count == oldList.size() && count != 0) {
                    List<Sku> addSku = new ArrayList<>();
                    addSku.add(sku);
                    saveSkuAndStock(addSku, id);
                    count = 0;
                } else {
                    count = 0;
                }

            }
            if (oldList.size() != 0) {
                for (Sku sku : oldList) {
                    this.skuMapper.deleteByPrimaryKey(sku.getId());
                    Example example1 = new Example(Stock.class);
                    example1.createCriteria().andEqualTo("skuId", sku.getId());
                    this.stockMapper.deleteByExample(example1);
                }
            }
        } else {
            List<Long> ids = oldList.stream().map(Sku::getId).collect(Collectors.toList());

            Example example1 = new Example(Stock.class);
            example1.createCriteria().andIn("skuId", ids);

            this.stockMapper.deleteByExample(example1);

            Example example2 = new Example(Sku.class);
            example2.createCriteria().andEqualTo("spuId", id);
            this.skuMapper.deleteByExample(example2);

            saveSkuAndStock(skus, id);

        }
    }

    public void deleteGoods(long id) {
        this.spuMapper.deleteByPrimaryKey(id);

        Example example = new Example(SpuDetail.class);
        example.createCriteria().andEqualTo("spuId", id);

        this.spuDetailMapper.deleteByExample(example);

        List<Sku> skuList = this.skuMapper.selectByExample(example);

        for (Sku sku : skuList) {
            this.skuMapper.deleteByPrimaryKey(sku.getId());

            this.stockMapper.deleteByPrimaryKey(sku.getId());
        }
    }

    public void goodsSoldOut(long id) {
        Spu oldSpu = this.spuMapper.selectByPrimaryKey(id);

        Example example = new Example(Sku.class);

        example.createCriteria().andEqualTo("spuId", id);

        List<Sku> skuList = this.skuMapper.selectByExample(example);

        if (oldSpu.getSaleable()) {
            oldSpu.setSaleable(false);

            this.spuMapper.updateByPrimaryKeySelective(oldSpu);

            for (Sku sku : skuList) {
                sku.setEnable(false);
                this.skuMapper.updateByPrimaryKeySelective(sku);
            }


        } else {
            oldSpu.setSaleable(true);
            this.spuMapper.updateByPrimaryKeySelective(oldSpu);

            for (Sku sku : skuList) {
                sku.setEnable(true);
                this.skuMapper.updateByPrimaryKeySelective(sku);
            }
        }
    }
}
