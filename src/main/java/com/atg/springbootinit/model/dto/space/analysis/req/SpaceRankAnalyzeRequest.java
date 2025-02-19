package com.atg.springbootinit.model.dto.space.analysis.req;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/19 14:21
*/
@Data
public class SpaceRankAnalyzeRequest  implements Serializable {

    /**
     * 排名前 N 的空间
     */
    private Integer topN = 10;

    private static final long serialVersionUID = 1L;
}
