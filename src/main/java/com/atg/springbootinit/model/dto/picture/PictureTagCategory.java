package com.atg.springbootinit.model.dto.picture;


import lombok.Data;

import java.util.List;

/*
author: atg
time: 2025/2/8 8:55
*/
@Data
public class PictureTagCategory {
    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;
}
