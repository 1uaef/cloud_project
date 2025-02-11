package com.atg.springbootinit.model.dto.space;


import lombok.AllArgsConstructor;
import lombok.Data;

/*
author: atg
time: 2025/2/11 16:17
*/
@Data
@AllArgsConstructor
public class SpaceLevel {



    private int value;


    private String text;

    /**
     * 最大数量
     */
    private long maxCount;

    /**
     * 最大容量
     */
    private long maxSize;
}
