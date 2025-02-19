package com.atg.springbootinit.model.dto.space.analysis.req;


import lombok.Data;
import lombok.EqualsAndHashCode;

/*
author: atg
time: 2025/2/19 14:05
*/
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceUserAnalyzeRequest  extends SpaceAnalyzeRequest {
    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 时间维度：day / week / month
     */
    private String timeDimension;
}
