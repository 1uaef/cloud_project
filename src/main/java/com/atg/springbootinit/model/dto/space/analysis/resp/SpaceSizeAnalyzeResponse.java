package com.atg.springbootinit.model.dto.space.analysis.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*
author: atg
time: 2025/2/19 13:27
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceSizeAnalyzeResponse implements Serializable {

    // 图片大小范围
    private String sizeRange;

    // 图片数量
    private Long count;

    private static final long serialVersionUID = 1L;
}
