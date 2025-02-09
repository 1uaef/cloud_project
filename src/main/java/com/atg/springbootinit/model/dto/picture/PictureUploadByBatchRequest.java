package com.atg.springbootinit.model.dto.picture;


import lombok.Data;

import java.util.List;

/*
author: atg
time: 2025/2/7 12:14
*/
@Data
public class PictureUploadByBatchRequest {


    /**
     * 搜索词
     */
    private String searchText;
    /**
     * 默认数量
     */
    private Integer count = 10;

    /**
     * 名称前缀
     */
    private String namePrefix;


    private static final long serialVersionUID = 1L;
}
