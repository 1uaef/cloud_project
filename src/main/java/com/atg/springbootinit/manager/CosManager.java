package com.atg.springbootinit.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.atg.springbootinit.config.CosClientConfig;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;

import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

/**
 * Cos 对象存储操作
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key           唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传图片对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return
     */
//    public PutObjectResult putPictureObject(String key, File file) {
//        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
//
//        // 创建 PicOperations 对象并设置 is_pic_info
//        PicOperations picOperations = new PicOperations();
//        picOperations.setIsPicInfo(1);
//
//        // 将 PicOperations 设置到 PutObjectRequest 中
//        putObjectRequest.setPicOperations(picOperations);
//
//        // 执行上传操作
//        return cosClient.putObject(putObjectRequest);
//    }
    /**
    **
     * 上传图片对象--优化
     *
     * @param key  唯一键
     * @param file 文件
     * @return
             */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);

        // 创建 PicOperations 对象并设置 is_pic_info
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        // 设置图片规则
        List<PicOperations.Rule> ruleList = new LinkedList<>();
        PicOperations.Rule pictureRule = new PicOperations.Rule();
        // 1.  设置图片压缩

        String webpKey = FileUtil.mainName(key) + ".webp";
        pictureRule.setFileId(webpKey);
        pictureRule.setBucket(cosClientConfig.getBucket());
        pictureRule.setRule("imageMogr2/format/webp");
        ruleList.add(pictureRule);
        //  缩略图处理
        if (file.length() > 1024 * 2){
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            String thumbnailKey = FileUtil.mainName(key) +"_thumbnail."+FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            // 缩放规则
            /**
             * 将图片缩放到宽度为 200 像素，高度为 200 像素，并保持图片的宽高比。
             * 以图片的中心为基准，裁剪出一个 200x200 像素的区域作为缩略图。
             * 这样处理后的图片将是一个 200x200 像素的正方形缩略图，且图片内容会尽量保持在中心位置
             */
            thumbnailRule.setRule("imageMogr2/thumbnail/!200x200r/gravity/Center/crop/200x200/");
            ruleList.add(thumbnailRule);

        }
        // 将 PicOperations 设置到 PutObjectRequest 中
        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);

        // 执行上传操作
        return cosClient.putObject(putObjectRequest);
    }
}
