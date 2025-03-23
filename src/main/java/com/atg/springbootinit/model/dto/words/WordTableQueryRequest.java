package com.atg.springbootinit.model.dto.words;


import com.atg.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/*
author: atg
time: 2025/3/23 14:28
*/

/**
 * 查询单词请求
 *

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WordTableQueryRequest extends PageRequest implements Serializable {
    /**
     * 英文单词
     */
    private String word;

    /**
     * 单词的中文释义
     */
    private String definition;


    private static final long serialVersionUID = 1L;
}
