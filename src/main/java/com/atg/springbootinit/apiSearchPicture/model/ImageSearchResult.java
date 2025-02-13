package com.atg.springbootinit.apiSearchPicture.model;


import lombok.Data;

/*
author: atg
time: 2025/2/13 15:21
*/
@Data
public class ImageSearchResult {
    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 来源地址
     */
    private String fromUrl;
}
