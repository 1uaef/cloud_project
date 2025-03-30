package com.atg.springbootinit.model.dto.user_word_progress;
import com.atg.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/*
author: atg
time: 2025/3/30 15:53
*/



/**
 * 查询用户单词进度请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserWordProgressQueryRequest extends PageRequest implements Serializable {
    /**
     * 是否掌握
     */
    private Boolean isKnown;

    private static final long serialVersionUID = 1L;
}