package com.atg.springbootinit.model.dto.picture;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/*
author: atg
time: 2025/2/15 8:25
*/
@Data
public class PictureBatchByEditRequest implements Serializable {

    private Long spaceId;

    private List<Long> pictureIdList;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 命名规则
     */
    private String nameRule;

    private static final long serialVersionUID = 1L;
}
