package com.atg.springbootinit.manager.upload;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.config.CosClientConfig;
import com.atg.springbootinit.constant.FileConstant;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.manager.CosManager;
import com.atg.springbootinit.model.dto.file.UploadPictureRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/*
author: atg
time: 2025/2/9 13:53
*/

@Service
@Slf4j

public abstract class PictureUploadTemplate {


    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    // 通用图片上传
    public UploadPictureRequest uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片
        validatePicture(inputSource);

        /**
         *  上传图片
         *  1. 校验图片
         *  2. 拼接图片文件名
         *  3. 创建文件地址
         *  4. 上传图片
         *   4.1 获取图片信息
         *   4.2 计算对应的长宽高
         *   4.3 封装返回结果
         *  5. 删除临时文件
         */
        String uuid = RandomUtil.randomString(16);
        // 2. 获取文件名
        String originalFilename = getOriginalFilename(inputSource);
        // 3.上传文件名
        String uploadedFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));

        String filepath = String.format("/%s/%s", uploadPathPrefix, uploadedFilename);

        File file = null;
        try {
            file = File.createTempFile(filepath, null);
            // 4.处理文件来源
            transferTo(inputSource, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filepath, file);
            // 5. 获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 获取图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)){
                CIObject ciObject = objectList.get(0);
                // 缩略图默认等于压缩图
                CIObject thumbnailCiObject = ciObject;
                if (objectList.size() >1){
                    thumbnailCiObject = objectList.get(1);
                }
                return buildResult( originalFilename, ciObject,thumbnailCiObject);
            }
            return getUploadPictureRequest(imageInfo, filepath, originalFilename, file);


        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 6. 删除临时文件
            deleteFile(file, filepath);
        }
    }


    private UploadPictureRequest buildResult(String originFilename,CIObject compressedCiObject, CIObject thumbnailCiObject) {
        UploadPictureRequest uploadPictureResult = new UploadPictureRequest();
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        // 设置图片为压缩后的地址
        uploadPictureResult.setUrl(FileConstant.COS_HOST + "/" + compressedCiObject.getKey());
        // 设置缩略图为压缩前的地址
        uploadPictureResult.setThumbnailUrl(FileConstant.COS_HOST + "/" + thumbnailCiObject.getKey());
        return uploadPictureResult;
    }

    @NotNull
    private static UploadPictureRequest getUploadPictureRequest(ImageInfo imageInfo, String filepath, String originalFilename, File file) {
        // 计算长宽
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();

        // 封装返回结果
        UploadPictureRequest uploadPictureRequest = new UploadPictureRequest();
        uploadPictureRequest.setUrl(FileConstant.COS_HOST + "/" + filepath);
        uploadPictureRequest.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureRequest.setPicSize(FileUtil.size(file));
        uploadPictureRequest.setPicWidth(picWidth);
        uploadPictureRequest.setPicHeight(picHeight);
        uploadPictureRequest.setPicScale(picScale);
        uploadPictureRequest.setPicFormat(imageInfo.getFormat());
        return uploadPictureRequest;
    }


    // 校验图片参数
    protected abstract void validatePicture(Object inputSource);

    // 获取文件名
    protected abstract String getOriginalFilename(Object inputSource);

    // 处理文件来源
    protected abstract void transferTo(Object inputSource, File file) throws Exception;


    private static void deleteFile(File file, String filepath) {
        if (file != null) {
            // 删除临时文件
            boolean delete = file.delete();
            if (!delete) {
                log.error("file delete error, filepath = {}", filepath);
            }
        }
    }

}
