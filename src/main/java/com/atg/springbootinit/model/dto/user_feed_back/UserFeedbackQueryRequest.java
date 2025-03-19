package com.atg.springbootinit.model.dto.user_feed_back;

import com.atg.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询用户反馈请求
 *

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFeedbackQueryRequest extends PageRequest implements Serializable {

    /**
     * 内容
     */
    private String description;
    /**
     * 状态
     */
    private Integer status;



    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}