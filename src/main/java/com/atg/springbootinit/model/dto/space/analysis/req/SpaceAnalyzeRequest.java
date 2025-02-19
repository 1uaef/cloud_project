package com.atg.springbootinit.model.dto.space.analysis.req;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/18 16:27
*/
@Data
public class SpaceAnalyzeRequest implements Serializable {
    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 是否查询公共图库
     */
    private boolean queryPublic;

    /**
     * 全空间分析
     */
    private boolean queryAll;

    private static final long serialVersionUID = 1L;
}
