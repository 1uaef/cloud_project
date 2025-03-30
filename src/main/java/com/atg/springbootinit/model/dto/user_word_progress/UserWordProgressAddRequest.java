package com.atg.springbootinit.model.dto.user_word_progress;


/*
author: atg
time: 2025/3/30 15:53
*/

import lombok.Data;

import java.io.Serializable;

/**
 * 添加用户单词进度请求
 */
@Data
public class UserWordProgressAddRequest implements Serializable {
    /**
     * 单词ID
     */
    private Long wordId;

    /**
     * 是否掌握
     */
    private Boolean isKnown;

    private static final long serialVersionUID = 1L;
}
