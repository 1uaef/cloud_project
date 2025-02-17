package com.atg.springbootinit.api.apiSearchPicture.execute;


import com.atg.springbootinit.api.apiSearchPicture.model.ImageSearchResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/*
author: atg
time: 2025/2/13 17:35
*/
@Slf4j
public class GetImageListApi {

    public static List<ImageSearchResult> getImageListApi(String query){
        PexelsImageSearch pexelsImageSearch = new PexelsImageSearch();
        List<String> imageUrls = pexelsImageSearch.searchImages(query);
        // 转换返回对象
        return dataConvert(imageUrls);
    }

    private static List<ImageSearchResult> dataConvert(List<String> imageUrls) {
        return imageUrls.stream().map(imageUrl -> {
            ImageSearchResult imageSearchResult = new ImageSearchResult();
            imageSearchResult.setThumbUrl(imageUrl);
            imageSearchResult.setFromUrl(imageUrl);
            return imageSearchResult;
        }).collect(Collectors.toList());


    }
    public static void main(String[] args) {
        // 示例：通过 Pexels API 获取与“nature”相关的图片列表
        String query = "nature";
        List<ImageSearchResult> imageList = getImageListApi(query);
        System.out.println("搜索成功，获取到图片列表：" + imageList);
    }
}
