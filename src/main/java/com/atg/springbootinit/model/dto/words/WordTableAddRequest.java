package com.atg.springbootinit.model.dto.words;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/*
author: atg
time: 2025/3/22 11:10
*/


/**
 * 添加单词请求
 */
@Data
public class WordTableAddRequest implements Serializable {

    /**
     * 英文单词
     */
    private String word;

    /**
     * 单词的中文释义
     */
    private String definition;

    /**
     * 单词的标签
     */

    private String tags;

    private static final long serialVersionUID = 1L;
}
