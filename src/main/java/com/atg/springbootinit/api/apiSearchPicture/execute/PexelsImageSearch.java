package com.atg.springbootinit.api.apiSearchPicture.execute;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
author: atg
time: 2025/2/13 17:06
*/
@Slf4j
public class PexelsImageSearch  {
    private static final String API_KEY = "9czuMmPDnd4ZIEpWWoM0KMxLAuptVW3tvt0Pl4bAGsWWyJiRtz6TYLdZ";
    private static final String ENDPOINT = "https://api.pexels.com/v1/search";

    // 默认的页码和每页图片数量
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    public List<String> searchImages(String query) {
        // 构建请求URL
        return  searchPictures(query, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
    }

    private List<String> searchPictures(String query, int defaultPage, int defaultPageSize) {
        List<String> imageUrl = new ArrayList<>();
        String apiUrl = String.format("%s?query=%s&per_page=%d&page=%d",
                ENDPOINT, query, defaultPageSize, defaultPage);

        try {
            OkHttpClient client = new OkHttpClient();
            // 构造请求
            Request authorization = new Request.Builder().url(apiUrl)
                    .addHeader("Authorization", API_KEY)
                    .build();
            String responseBody = client.newCall(authorization).execute().body().string();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode photosNode = objectMapper.readTree(responseBody).get("photos");

            if (photosNode != null) {
                for (JsonNode photoNode : photosNode) {
                    // 获取每张图片的原图URL
                    imageUrl.add(photoNode.get("src").get("original").asText());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return imageUrl;
    }
    public static void main(String[] args) {
        // 测试不指定页面和每页数量的搜索
        PexelsImageSearch imageSearch = new PexelsImageSearch();
        List<String> imageUrls = imageSearch.searchImages("美女");
        System.out.println("搜索到的图片URLs: ");
        imageUrls.forEach(System.out::println);
    }
}
