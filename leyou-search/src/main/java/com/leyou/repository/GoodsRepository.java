package com.leyou.repository;

import com.leyou.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Created by RookieWangZhiWei on 2019/4/20.
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
