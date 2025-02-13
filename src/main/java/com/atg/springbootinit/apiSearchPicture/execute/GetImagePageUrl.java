package com.atg.springbootinit.apiSearchPicture.execute;


import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static cn.hutool.http.HttpStatus.HTTP_OK;

/*
author: atg
time: 2025/2/13 15:22
*/
@Slf4j
public class GetImagePageUrl {

    /**
     * 获取图片的url
     */
    public static String getImageUrl(String url) {
//        image: https%3A%2F%2Fatgcloud-1317657680.cos.ap-guangzhou.myqcloud.com%2Fspace%2F1889505347334795266%2F1882052576192929793%2F2025-02-12_1eyglkwwm8gv4hjy.png
//        tn: pc
//        from: pc
//        image_source: PC_UPLOAD_URL
        // 构造请求的请求头
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", url);
        formData.put("tn", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");

        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();
        //  请求地址
        String requestUrl =  "https://graph.baidu.com/upload?uptime=" + timestamp;
        // 发起响应
        try {
            HttpResponse httpResponse = HttpRequest.post(requestUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Referer", "https://graph.baidu.com/")
                    .form(formData)
                    .execute();
            if (httpResponse.getStatus() != HTTP_OK) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            String body = httpResponse.body();
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);
            // 处理响应结果
            if (result == null || Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片的接口失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 目标地址URL--响应图片结果的URL
            String decode = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            if (decode == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片的url失败");
            }
            return decode;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片的url失败");
        }


    }

    public static void main(String[] args) {
        String imageUrl = "https://www.codefather.cn/logo.png";
        String SearchUrl = getImageUrl(imageUrl);
        System.out.println(SearchUrl);
    }
}
//@Slf4j
//public class GetImagePageUrl {
//
//    /**
//     * 获取以图搜图页面地址
//     *
//     * @param imageUrl
//     * @return
//     */
//    public static String getImageUrl(String imageUrl) {
//        // image: https%3A%2F%2Fwww.codefather.cn%2Flogo.png
//        //tn: pc
//        //from: pc
//        //image_source: PC_UPLOAD_URL
//        //sdkParams:
//        // 1. 准备请求参数
//        Map<String, Object> formData = new HashMap<>();
//        formData.put("image", imageUrl);
//        formData.put("tn", "pc");
//        formData.put("from", "pc");
//        formData.put("image_source", "PC_UPLOAD_URL");
//        // 获取当前时间戳
//        long uptime = System.currentTimeMillis();
//        // 请求地址
//        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
//        try {
//            // 2. 发送请求
//            HttpResponse httpResponse = HttpRequest.post(url)
//                    .form(formData)
//                    .timeout(5000)
//                    .execute();
//            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
//                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
//            }
//            // 解析响应
//            // {"status":0,"msg":"Success","data":{"url":"https://graph.baidu.com/sc","sign":"1262fe97cd54acd88139901734784257"}}
//            String body = httpResponse.body();
//            Map<String, Object> result = JSONUtil.toBean(body, Map.class);
//            // 3. 处理响应结果
//            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
//                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
//            }
//            Map<String, Object> data = (Map<String, Object>) result.get("data");
//            // 对 URL 进行解码
//            String rawUrl = (String) data.get("url");
//            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
//            // 如果 URL 为空
//            if (StrUtil.isBlank(searchResultUrl)) {
//                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效的结果地址");
//            }
//            return searchResultUrl;
//        } catch (Exception e) {
//            log.error("调用百度以图搜图接口失败", e);
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
//        }
//    }
//
//    public static void main(String[] args) {
//        // 测试以图搜图功能
//        String imageUrl = "https://www.codefather.cn/logo.png";
//        String searchResultUrl = getImageUrl(imageUrl);
//        System.out.println("搜索成功，结果 URL：" + searchResultUrl);
//    }
//}
