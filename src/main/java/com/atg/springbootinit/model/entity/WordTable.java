package com.atg.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 单词表
 * @TableName word_table
 */
@TableName(value ="word_table")
@Data
public class WordTable {
    /**
     * 单词的唯一标识，自动递增
     */
    @TableId(type = IdType.AUTO)
    private Long word_id;

    /**
     * 用户的唯一标识，关联用户表
     */
    private Long user_id;

    /**
     * 英文单词，如“apple”
     */
    private String word;

    /**
     * 单词的中文释义，如“苹果”
     */
    private String definition;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（0：未删除，1：已删除）
     */
    private Integer isDelete;
}