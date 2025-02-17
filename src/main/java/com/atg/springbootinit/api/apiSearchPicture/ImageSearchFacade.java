package com.atg.springbootinit.api.apiSearchPicture;


import com.atg.springbootinit.api.apiSearchPicture.execute.GetImageListApi;
import com.atg.springbootinit.api.apiSearchPicture.model.ImageSearchResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/*
author: atg
time: 2025/2/13 17:47
*/


@Slf4j
public class ImageSearchFacade {
    /**
     * 搜索图片
     *
     * @param query 图片关键字
     * @return 图片搜索结果列表
     */
    public static List<ImageSearchResult> searchImage(String query) {
        // 这里可以通过图像URL获取相关的搜索结果。因为我们使用Pexels，我们可以基于图像URL进行直接搜索。
        return GetImageListApi.getImageListApi(query);
    }

    public static void main(String[] args) {
        // 测试通过图像URL进行以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表：" + resultList);
    }
}
