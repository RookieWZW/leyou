package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by RookieWangZhiWei on 2019/4/23.
 */
@Service
public class GoodsService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    public Map<String, Object> loadModel(Long spuId) {
        SpuBo spuBo = this.goodsClient.queryGoodsById(spuId);

        SpuDetail spuDetail = spuBo.getSpuDetail();


        List<Sku> skuList = spuBo.getSkus();

        List<Long> ids = new ArrayList<>();

        ids.add(spuBo.getCid1());
        ids.add(spuBo.getCid2());
        ids.add(spuBo.getCid3());
        List<Category> categoryList = this.categoryClient.queryCategoryByIds(ids).getBody();


        Brand brand = this.brandClient.queryBrandByIds(Collections.singletonList(spuBo.getBrandId())).get(0);


        String allSpecJson = spuDetail.getSpecifications();

        List<Map<String, Object>> allSpecs = JsonUtils.nativeRead(allSpecJson, new TypeReference<List<Map<String, Object>>>() {
        });
        Map<Integer, String> specName = new HashMap<>();
        Map<Integer, Object> specValue = new HashMap<>();
        this.getAllSpecifications(allSpecs, specName, specValue);

        String specJson = spuDetail.getSpecTemplate();
        Map<String, String[]> specs = JsonUtils.nativeRead(specJson, new TypeReference<Map<String, String[]>>() {
        });
        Map<Integer, String> specialParamName = new HashMap<>();
        Map<Integer, String[]> specialParamValue = new HashMap<>();
        this.getSpecialSpec(specs, specName, specValue, specialParamName, specialParamValue);


        List<Map<String, Object>> groups = this.getGroupsSpec(allSpecs, specName, specValue);


        Map<String, Object> map = new HashMap<>();
        map.put("spu", spuBo);
        map.put("spuDetail", spuDetail);
        map.put("skus", skuList);
        map.put("brand", brand);
        map.put("categories", categoryList);
        map.put("specName", specName);
        map.put("specValue", specValue);
        map.put("groups", groups);
        map.put("specialParamName", specialParamName);
        map.put("specialParamValue", specialParamValue);

        return map;

    }

    private List<Map<String, Object>> getGroupsSpec(List<Map<String, Object>> allSpecs, Map<Integer, String> specName, Map<Integer, Object> specValue) {
        List<Map<String, Object>> groups = new ArrayList<>();

        int i = 0;
        int j = 0;
        for (Map<String, Object> spec : allSpecs) {
            List<Map<String, Object>> params = (List<Map<String, Object>>) spec.get("params");
            List<Map<String, Object>> temp = new ArrayList<>();
            for (Map<String, Object> param : params) {
                for (Map.Entry<Integer, String> entry :
                        specName.entrySet()) {
                    if (entry.getValue().equals(param.get("k").toString())) {
                        String value = specValue.get(entry.getKey()) != null ? specValue.get(entry.getKey()).toString() : "无";
                        Map<String, Object> temp3 = new HashMap<>(16);
                        temp3.put("id", ++j);
                        temp3.put("name", entry.getValue());
                        temp3.put("value", value);
                        temp.add(temp3);
                    }
                }
            }
            Map<String, Object> temp2 = new HashMap<>(16);
            temp2.put("params", temp);
            temp2.put("id", ++i);
            temp2.put("name", spec.get("group"));
            groups.add(temp2);
        }
        return groups;
    }

    private void getSpecialSpec(Map<String, String[]> specs, Map<Integer, String> specName, Map<Integer, Object> specValue, Map<Integer, String> specialParamName, Map<Integer, String[]> specialParamValue) {
        if (specs != null) {
            for (Map.Entry<String, String[]> entry : specs.entrySet()) {
                String key = entry.getKey();
                for (Map.Entry<Integer, String> e : specName.entrySet()) {
                    if (e.getValue().equals(key)) {
                        specialParamName.put(e.getKey(), e.getValue());
                        String s = specValue.get(e.getKey()).toString();
                        String result = StringUtils.substring(s, 1, s.length() - 1);
                        specialParamValue.put(e.getKey(), result.split(","));
                    }
                }
            }
        }
    }

    private void getAllSpecifications(List<Map<String, Object>> allSpecs, Map<Integer, String> specName, Map<Integer, Object> specValue) {
        String k = "k";
        String v = "v";
        String unit = "unit";
        String numerical = "numerical";
        String options = "options";
        int i = 0;
        if (allSpecs != null) {
            for (Map<String, Object> s : allSpecs) {
                List<Map<String, Object>> params = (List<Map<String, Object>>) s.get("params");
                for (Map<String, Object> param : params) {
                    String result;
                    if (param.get(v) == null) {
                        result = "无";
                    } else {
                        result = param.get(v).toString();
                    }
                    if (param.containsKey(numerical) && (boolean) param.get(numerical)) {
                        if (result.contains(".")) {
                            Double d = Double.valueOf(result);
                            if (d.intValue() == d) {
                                result = d.intValue() + "";
                            }
                        }
                        i++;
                        specName.put(i, param.get(k).toString());
                        specValue.put(i, result + param.get(unit).toString());
                    } else if (param.containsKey(options)) {
                        i++;
                        specName.put(i, param.get(k).toString());
                        specValue.put(i, param.get(options));
                    } else {
                        i++;
                        specName.put(i, param.get(k).toString());
                        specValue.put(i, param.get(v));
                    }
                }
            }
        }
    }
}
