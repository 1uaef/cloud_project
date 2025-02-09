package com.atg.springbootinit.manager.upload;


import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;

/*
author: atg
time: 2025/2/9 14:04
description: 文件上传
*/
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void validatePicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
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

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void transferTo(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
