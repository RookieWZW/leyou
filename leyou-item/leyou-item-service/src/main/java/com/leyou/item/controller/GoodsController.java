package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/14.
 */
@RestController
@RequestMapping("goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable",defaultValue = "true") Boolean saleable

    ){

        PageResult<SpuBo> result = this.goodsService.querySpuPage(page, rows, sortBy,desc,saleable, key);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spu){
        try{
            this.goodsService.save(spu);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }


    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("id")Long id){
        SpuDetail detail = this.goodsService.querySpuDetailBySpuId(id);
        if (detail == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }else {
            return ResponseEntity.ok(detail);
        }
    }

    @GetMapping("sku/list/{id}")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@PathVariable("id")Long id){
        List<Sku> skus = this.goodsService.querySkuBySpuId(id);
        if (skus == null || skus.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(skus);

    }

    @GetMapping("/spu/{id}")
    public ResponseEntity<SpuBo> queryGoodsById(@PathVariable("id") Long id){
        SpuBo spuBo=this.goodsService.queryGoodsById(id);
        if (spuBo == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(spuBo);
    }

    @PutMapping("spu/out/{id}")
    public ResponseEntity<Void> goodsSoldOut(@PathVariable("id")String ids){
        String separator="-";
        if (ids.contains(separator)){
            String[] goodsId = ids.split(separator);
            for (String id : goodsId) {
                this.goodsService.goodsSoldOut(Long.parseLong(id));
            }
        }else{
            this.goodsService.goodsSoldOut(Long.parseLong(ids));
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/spu/{id}")
    public ResponseEntity<Void> deleteGoods(@PathVariable("id")String ids){
        String separator="-";
        if (ids.contains(separator)){
            String[] goodsId = ids.split(separator);
            for (String id:goodsId){
                this.goodsService.deleteGoods(Long.parseLong(id));
            }
        }
        else {
            this.goodsService.deleteGoods(Long.parseLong(ids));
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/sku/{id}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("id")Long id){
        Sku sku = this.goodsService.querySkuById(id);
        if (sku == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(sku);
    }


}
