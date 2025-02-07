package com.atg.springbootinit.model.dto.picture;


import lombok.Data;

import java.util.List;

/*
author: atg
time: 2025/2/7 12:13
*/
@Data
public class PictureUpdateRequest {

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
