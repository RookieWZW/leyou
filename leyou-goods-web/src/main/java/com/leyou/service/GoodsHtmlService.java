package com.leyou.service;


import com.leyou.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by RookieWangZhiWei on 2019/4/24.
 */
@Service
public class GoodsHtmlService {
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private TemplateEngine templateEngine;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsHtmlService.class);

    public void createHtml(Long spuId) {
        PrintWriter writer = null;
        Map<String, Object> spuMap = this.goodsService.loadModel(spuId);

        Context context = new Context();

        context.setVariables(spuMap);

        File file = new File("F:\\IDEA\\leyou\\nginx-1.14.0\\html\\item\\"+spuId+".html");

        try{
            writer = new PrintWriter(file);
            templateEngine.process("item",context,writer);

        }catch (Exception e){
            LOGGER.error("页面静态化出错：{}"+e,spuId);
        }finally {
            if (writer != null){
                writer.close();
            }
        }
    }

    public void asyncExecute(Long spuId){
        ThreadUtils.execute(() ->createHtml(spuId));
    }
    public void deleteHtml(Long id){
        File file = new File("F:\\IDEA\\leyou\\nginx-1.14.0\\html\\item\\"+id+".html");
        file.deleteOnExit();
    }
}
