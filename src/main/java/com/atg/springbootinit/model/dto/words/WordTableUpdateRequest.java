package com.atg.springbootinit.model.dto.words;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/3/22 22:58
*/
@Data
public class WordTableUpdateRequest implements Serializable {

    /**
     * 单词的唯一标识
     */
    private Long word_Id;

    /**
     * 英文单词
     */
    private String word;

    /**
     * 标签
     */
    private String tags;

    /**
     * 单词的中文释义
     */
    private String definition;

    private static final long serialVersionUID = 1L;
}
