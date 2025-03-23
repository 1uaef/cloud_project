package com.atg.springbootinit.model.dto.words;


/*
author: atg
time: 2025/3/22 11:12
*/

import java.util.List;
import lombok.Data;

import java.io.Serializable;

/**
 * 批量添加单词请求
 */
@Data
public class WordTableBatchAddRequest implements Serializable {

    /**
     * 单词列表
     */
    private List<WordTableAddRequest> wordList;

    private static final long serialVersionUID = 1L;
}
