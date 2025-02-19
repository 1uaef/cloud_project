package com.atg.springbootinit.model.dto.space.analysis.resp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
author: atg
time: 2025/2/18 19:12
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeResponse {
    /**
     * 标签名称
     */
    private String tag;

    /**
     * 使用次数
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
