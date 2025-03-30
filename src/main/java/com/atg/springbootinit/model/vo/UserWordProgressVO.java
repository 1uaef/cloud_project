package com.atg.springbootinit.model.vo;
import com.atg.springbootinit.model.entity.WordTable;
import lombok.Data;

import java.util.Date;

/*
author: atg
time: 2025/3/30 15:54
*/




/**
 * 用户单词进度视图对象
 */
@Data
public class UserWordProgressVO {
    /**
     * 进度ID
     */
    private Integer progress_id;

    /**
     * 单词信息
     */
    private WordVO word;

    /**
     * 是否掌握
     */
    private Boolean is_known;

    /**
     * 学习次数
     */
    private Integer study_count;

    /**
     * 正确次数
     */
    private Integer correct_count;

    /**
     * 困难次数
     */
    private Integer difficult_count;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}