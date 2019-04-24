package com.leyou.controller;

import com.leyou.bo.SearchRequest;
import com.leyou.client.GoodsClient;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.pojo.Goods;
import com.leyou.repository.GoodsRepository;
import com.leyou.service.SearchService;
import com.leyou.vo.SearchResult;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/20.
 */
@RestController
@RequestMapping
public class SearchController implements InitializingBean {


    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest searchRequest) {
        SearchResult<Goods> result = this.searchService.search(searchRequest);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(result);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.elasticsearchTemplate.createIndex(Goods.class);

        this.elasticsearchTemplate.putMapping(Goods.class);


        List<SpuBo> list = new ArrayList<>();
        int page = 1;
        int row = 100;
        int size;

        do {

            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(page, row, null, true, null, true);
            List<SpuBo> spus = result.getItems();

            size = spus.size();
            page++;
            list.addAll(spus);
        } while (size == 100);

        List<Goods> goodsList = new ArrayList<>();

        for (SpuBo spu : list) {
            try {
                Goods goods = this.searchService.buildGoods(spu);
                goodsList.add(goods);

            } catch (IOException e) {
                System.out.println("search fail:" + spu.getId());
            }
        }
        this.goodsRepository.saveAll(goodsList);
    }
}
