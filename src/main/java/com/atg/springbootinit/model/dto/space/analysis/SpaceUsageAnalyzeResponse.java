package com.atg.springbootinit.model.dto.space.analysis;


import lombok.Data;

/*
author: atg
time: 2025/2/18 16:55
*/
@Data
public class SpaceUsageAnalyzeResponse {
    /**
     * 已使用大小
     */
    private Long usedSize;

    /**
     * 总大小
     */
    private Long maxSize;

    /**
     * 空间使用比例
     */
    private Double sizeUsageRatio;

    /**
     * 当前图片数量
     */
    private Long usedCount;

    /**
     * 最大图片数量
     */
    private Long maxCount;

    /**
     * 图片数量占比
     */
    private Double countUsageRatio;

    private static final long serialVersionUID = 1L;
}
