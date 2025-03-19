package com.atg.springbootinit.model.dto.user_feed_back;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建用户反馈请求
 *

 */
@Data
public class UserFeedbackAddRequest implements Serializable {

    /**
     * 内容
     */
    private String description;



    private static final long serialVersionUID = 1L;
}