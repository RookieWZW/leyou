package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/11.
 */
@Service
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
    private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg");


    @Autowired
    private FastFileStorageClient storageClient;

    public String upload(MultipartFile file){

        String contenttype = file.getContentType();

        if (!suffixes.contains(contenttype)){
            logger.info("文件上传失败，类型不支持");
            return null;
        }

        try{
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

            if (bufferedImage == null){
                logger.info("文件上传失败，不是图片");
                return null;
            }

            File dir = new File("F://IDEA//leyou//upload");
            if (!dir.exists()){
                dir.mkdirs();
            }

           // file.transferTo(new File(dir,file.getOriginalFilename()));

           // String url = "http://image.leyou.com/upload/"+file.getOriginalFilename();

            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");

            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(),file.getSize(),extension,null);

            return "http://image.leyou.com/" + storePath.getFullPath();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
