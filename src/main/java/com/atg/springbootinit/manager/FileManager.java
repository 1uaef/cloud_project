
package com.atg.springbootinit.manager;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.unit.DataUnit;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.config.CosClientConfig;
import com.atg.springbootinit.constant.FileConstant;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.dto.file.UploadPictureRequest;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/*
author: atg
time: 2025/2/5 19:41
*/
@Service
@Slf4j
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    // 通用图片上传
    public UploadPictureRequest uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validatePicture(multipartFile);

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
        // 获取文件名
        String originalFilename = multipartFile.getOriginalFilename();
        // 上传文件名
        String uploadedFilename =  String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));

        String filepath = String.format("/%s/%s", uploadPathPrefix, uploadedFilename);

        File file= null;
        try {
            file =  File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filepath, file);
            // 获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            // 计算长宽
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();

            // 封装返回结果
            UploadPictureRequest uploadPictureRequest = new UploadPictureRequest();
            uploadPictureRequest.setUrl(FileConstant.COS_HOST+"/"+filepath);
            uploadPictureRequest.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureRequest.setPicSize(FileUtil.size(file));
            uploadPictureRequest.setPicWidth(picWidth);
            uploadPictureRequest.setPicHeight(picHeight);
            uploadPictureRequest.setPicScale(picScale);
            uploadPictureRequest.setPicFormat(imageInfo.getFormat());
            return uploadPictureRequest;


        }catch (Exception e){
            log.error("上传文件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }finally {
            // 删除临时文件
            deleteFile(file, filepath);
        }
    }



    private void validatePicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传图片不能为空");
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        if (fileSize > 1024 * 1024 * 2) {
            throw new RuntimeException("文件大小不能超过 10M");
        }

        // 校验文件后缀
        String fileName = multipartFile.getOriginalFilename();
        String fileSuffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!Arrays.asList("jpg", "jpeg", "png", "gif", "bmp").contains(fileSuffix)) {
            throw new RuntimeException("文件格式错误");
        }

    }

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
