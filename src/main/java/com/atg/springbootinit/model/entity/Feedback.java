package com.atg.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.Data;

/**
 * 反馈建议
 * @TableName feedback
 */
@TableName(value ="feedback")
@Data
public class Feedback {
    /**
     * 建议 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 建议详细描述
     */
    private String description;

    /**
     * 建议状态(0-正在审核、1-审核通过、2-审核不通过)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}