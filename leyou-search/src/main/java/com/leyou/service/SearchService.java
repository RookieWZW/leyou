package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecClient;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.*;
import com.leyou.repository.GoodsRepository;
import com.leyou.service.SearchService;
import com.leyou.bo.SearchRequest;


import com.leyou.utils.JsonUtils;
import com.leyou.utils.NumberUtils;
import com.leyou.vo.SearchResult;
import com.leyou.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by RookieWangZhiWei on 2019/4/20.
 */
@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecClient specClient;


    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ObjectMapper mapper = new ObjectMapper();


    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();

        List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())).getBody();

        List<Sku> skus = this.goodsClient.querySkuBySpuId(spu.getId());

        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());

        List<Long> prices = new ArrayList<>();

        List<Map<String, Object>> skuLists = new ArrayList<>();

        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> skuMap = new HashMap<>();

            skuMap.put("id", sku.getId());
            skuMap.put("title", sku.getTitle());
            skuMap.put("price", sku.getPrice());

            skuMap.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);

            skuLists.add(skuMap);
        });

        List<Map<String, Object>> genericSpecs = mapper.readValue(spuDetail.getSpecifications(), new TypeReference<List<Map<String, Object>>>() {
        });

        Map<String, Object> specialSpecs = mapper.readValue(spuDetail.getSpecTemplate(), new TypeReference<Map<String, Object>>() {
        });


        Map<String, Object> specMap = new HashMap<>();


        String searchable = "searchable";
        String v = "v";
        String k = "k";
        String options = "options";


        genericSpecs.forEach(m -> {
            List<Map<String, Object>> params = (List<Map<String, Object>>) m.get("params");

            params.forEach(spe -> {
                if ((boolean) spe.get(searchable)) {
                    if (spe.get(v) != null) {
                        specMap.put(spe.get(k).toString(), spe.get(v));
                    } else if (spe.get(options) != null) {
                        specMap.put(spe.get(k).toString(), spe.get(options));
                    }
                }
            });

        });

        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " "));
        goods.setPrice(prices);
        goods.setSkus(mapper.writeValueAsString(skuLists));
        goods.setSpecs(specMap);
        return goods;
    }


    public SearchResult<Goods> search(SearchRequest searchRequest) {
        String key = searchRequest.getKey();

        if (StringUtils.isBlank(key)) {
            return null;
        }

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        QueryBuilder basicQuery = this.buildBasicQueryWithFilter(searchRequest);

        queryBuilder.withQuery(basicQuery);

        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));

        searchWithPageAndSort(queryBuilder, searchRequest);

        String categoryAggName = "category";

        String brandAggName = "brand";

        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));

        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        AggregatedPage<Goods> pageInfo = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());


        Long total = pageInfo.getTotalElements();

        int totalpage = pageInfo.getTotalPages();

        List<Category> categories = getCategoryAggResult(pageInfo.getAggregation(categoryAggName));

        List<Brand> brands = getBrandAggResult(pageInfo.getAggregation(brandAggName));

        List<Map<String, Object>> specs = null;
        if (categories.size() == 1) {
            specs = getSpec(categories.get(0).getId(), basicQuery);
        }

        return new SearchResult<Goods>(total, (long) totalpage, pageInfo.getContent(), categories, brands, specs);

    }


    public void createIndex(Long id) throws IOException {
        SpuBo spuBo = this.goodsClient.queryGoodsById(id);

        Goods goods = this.buildGoods(spuBo);

        this.goodsRepository.save(goods);
    }

    public void deleteIndex(Long id) {
        this.goodsRepository.deleteById(id);
    }


    private QueryBuilder buildBasicQueryWithFilter(SearchRequest searchRequest) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        queryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));

        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();


        Map<String, String> filter = searchRequest.getFilter();

        for (Map.Entry<String, String> entry :
                filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String regex = "^(\\d+\\.?\\d*)-(\\d+\\.?\\d*)$";
            if (!"key".equals(key)) {
                if ("price".equals(key)) {
                    if (!value.contains("元以上")) {
                        String[] nums = StringUtils.substringBefore(value, "元").split("-");
                        filterQueryBuilder.must(QueryBuilders.rangeQuery(key).gte(Double.valueOf(nums[0]) * 100).lt(Double.valueOf(nums[1]) * 100));
                    } else {
                        String num = StringUtils.substringBefore(value, "元以上");
                        filterQueryBuilder.must(QueryBuilders.rangeQuery(key).gte(Double.valueOf(num) * 100));
                    }
                } else {
                    if (value.matches(regex)) {
                        Double[] nums = NumberUtils.searchNumber(value, regex);
                        filterQueryBuilder.must(QueryBuilders.rangeQuery("specs." + key).gte(nums[0]).lt(nums[1]));
                    } else {
                        if (key != "cid3" && key != "brandId") {
                            key = "specs." + key + ".keyword";
                        }
                        //字符串类型，进行term查询
                        filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
                    }
                }
            } else {
                break;
            }
        }
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }

    private List<Map<String, Object>> getSpec(Long id, QueryBuilder basicQuery) {
        String specsJSONStr = this.specClient.querySpecificationByCategoryId(id).getBody();

        List<Map<String, Object>> specs = null;

        specs = JsonUtils.nativeRead(specsJSONStr, new TypeReference<List<Map<String, Object>>>() {
        });

        Set<String> strSpec = new HashSet<>();

        Map<String, String> numericalUnits = new HashMap<>();

        String searchable = "searchable";
        String numerical = "numerical";

        String k = "k";
        String unit = "unit";
        for (Map<String, Object> spec : specs) {
            List<Map<String, Object>> params = (List<Map<String, Object>>) spec.get("params");
            params.forEach(param -> {
                if ((boolean) param.get(searchable)) {
                    if (param.containsKey(numerical) && (boolean) param.get(numerical)) {
                        numericalUnits.put(param.get(k).toString(), param.get(unit).toString());
                    } else {
                        strSpec.add(param.get(k).toString());
                    }
                }
            });


        }
        Map<String, Double> numericalInterval = getNumberInterval(id, numericalUnits.keySet());
        return this.aggForSpec(strSpec, numericalInterval, numericalUnits, basicQuery);
    }

    private Map<String, Double> getNumberInterval(Long id, Set<String> keySet) {
        Map<String, Double> numbericalSpecs = new HashMap<>();

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        queryBuilder.withQuery(QueryBuilders.termQuery("cid", id.toString())).withSourceFilter(new FetchSourceFilter(new String[]{""}, null)).withPageable(PageRequest.of(0, 1));


        for (String key :
                keySet) {
            queryBuilder.addAggregation(AggregationBuilders.stats(key).field("specs." + key));
        }

        Map<String, Aggregation> aggregationMap = this.elasticsearchTemplate.query(queryBuilder.build(), searchResponse -> searchResponse.getAggregations().asMap());

        for (String key :
                keySet) {
            InternalStats stats = (InternalStats) aggregationMap.get(key);
            double interval = this.getInterval(stats.getMin(), stats.getMax(), stats.getSum());
            numbericalSpecs.put(key, interval);
        }
        return numbericalSpecs;
    }


    private double getInterval(double min, double max, Double sum) {
        double interval = (max - min) / 6.0d;
        if (sum.intValue() == sum) {
            int length = StringUtils.substringBefore(String.valueOf(interval), ".").length();
            double factor = Math.pow(10.0, length - 1);
            return Math.round(interval / factor) * factor;
        } else {
            return NumberUtils.scale(interval, 1);
        }
    }


    private List<Map<String, Object>> aggForSpec(Set<String> strSpec, Map<String, Double> numericalInterval, Map<String, String> numericalUnits, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);

        for (Map.Entry<String, Double> entry : numericalInterval.entrySet()) {
            queryBuilder.addAggregation(AggregationBuilders.histogram(entry.getKey()).field("specs." + entry.getKey()).interval(entry.getValue()).minDocCount(1));
        }
        //聚合字符串
        for (String key : strSpec) {
            queryBuilder.addAggregation(AggregationBuilders.terms(key).field("specs." + key + ".keyword"));
        }

        Map<String, Aggregation> aggregationMap = this.elasticsearchTemplate.query(queryBuilder.build(), SearchResponse::getAggregations).asMap();

        for (Map.Entry<String, Double> entry : numericalInterval.entrySet()) {
            Map<String, Object> spec = new HashMap<>();
            String key = entry.getKey();
            spec.put("k", key);
            spec.put("unit", numericalUnits.get(key));
            //获取聚合结果
            InternalHistogram histogram = (InternalHistogram) aggregationMap.get(key);
            spec.put("options", histogram.getBuckets().stream().map(bucket -> {
                Double begin = (Double) bucket.getKey();
                Double end = begin + numericalInterval.get(key);
                //对begin和end取整
                if (NumberUtils.isInt(begin) && NumberUtils.isInt(end)) {
                    //确实是整数，直接取整
                    return begin.intValue() + "-" + end.intValue();
                } else {
                    //小数，取2位小数
                    begin = NumberUtils.scale(begin, 2);
                    end = NumberUtils.scale(end, 2);
                    return begin + "-" + end;
                }
            }).collect(Collectors.toList()));
            specs.add(spec);
        }

        strSpec.forEach(key -> {
            Map<String, Object> spec = new HashMap<>();
            spec.put("k", key);
            StringTerms terms = (StringTerms) aggregationMap.get(key);
            spec.put("options", terms.getBuckets().stream().map((Function<StringTerms.Bucket, Object>) StringTerms.Bucket::getKeyAsString).collect(Collectors.toList()));
            specs.add(spec);
        });


        return specs;
    }


    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        LongTerms brandAgg = (LongTerms) aggregation;

        List<Long> bids = new ArrayList<>();
        for (LongTerms.Bucket bucket : brandAgg.getBuckets()) {
            bids.add(bucket.getKeyAsNumber().longValue());
        }

        return this.brandClient.queryBrandByIds(bids);

    }
    private List<Category> getCategoryAggResult(Aggregation aggregation) {
        LongTerms brandAgg = (LongTerms) aggregation;
        List<Long> cids = new ArrayList<>();
        for (LongTerms.Bucket bucket : brandAgg.getBuckets()){
            cids.add(bucket.getKeyAsNumber().longValue());
        }
        //根据id查询分类名称
        return this.categoryClient.queryCategoryByIds(cids).getBody();
    }

    private void searchWithPageAndSort(NativeSearchQueryBuilder queryBuilder, SearchRequest request) {
        int page = request.getPage();
        int size = request.getDefaultSize();

        queryBuilder.withPageable(PageRequest.of(page - 1, size));

        String sortBy = request.getSortBy();
        Boolean desc = request.getDescending();

        if (StringUtils.isNotBlank(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC : SortOrder.ASC));
        }
    }

}
