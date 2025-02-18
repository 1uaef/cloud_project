package com.atg.springbootinit.manager.upload;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/*
author: atg
time: 2025/2/9 14:07
*/
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void validatePicture(Object inputSource) {
        // 1. 校验URL是否存在
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(fileUrl == null, ErrorCode.PARAMS_ERROR, "上传图片不能为空");
        // 2. 校验URL是否合法--验证URL格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传图片URL格式错误");
        }
        // 3. 校验URL协议 http 或者 https 开头
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "上传图片URL协议错误");
        // 4. 验证UR图片 是否存在  发送 HEAD 请求以验证文件是否存在
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.GET, fileUrl).execute();
            ThrowUtils.throwIf(!httpResponse.isOk(), ErrorCode.PARAMS_ERROR, "上传图片URL不存在");
            // 5. 校验图片格式
            String header = httpResponse.header("Content-Type");
            if (StrUtil.isNotBlank(header)){
                List<String> list = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp");
                ThrowUtils.throwIf(!list.contains(header), ErrorCode.PARAMS_ERROR, "上传图片格式错误");
            }
            // 6. 校验图片大小
            String pictureSize = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(pictureSize)){
                ThrowUtils.throwIf(Integer.parseInt(pictureSize) > 1024 * 1024 * 10, ErrorCode.PARAMS_ERROR, "上传图片大小不能超过 10M");
            }
        }
        finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }


    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
//        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void transferTo(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        // 下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }
}
