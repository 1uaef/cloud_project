package com.atg.springbootinit.model.dto.question_bank;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建题库请求
 *

 */
@Data
public class QuestionBankAddRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String description;

    /**
     * 图片 URL
     */
    private String picture;

    private static final long serialVersionUID = 1L;
}