package com.atg.springbootinit.model.dto.words;


import com.atg.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

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

    private Long word_id;

    /**
     * 英文单词
     */
    private String word;

    /**
     * 单词的中文释义
     */
    private String definition;
    /**
     * 单词标签
     */
    private String tags;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;


    private static final long serialVersionUID = 1L;
}
