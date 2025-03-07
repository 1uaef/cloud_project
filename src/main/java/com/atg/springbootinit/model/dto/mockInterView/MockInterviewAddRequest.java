package com.atg.springbootinit.model.dto.mockInterView;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/3/7 11:23
*/
@Data
public class MockInterviewAddRequest implements Serializable {

    /**
     * 工作年限
     */
    private String workExperience;

    /**
     * 工作岗位
     */
    private String jobPosition;

    /**
     * 面试难度
     */
    private String difficulty;

    private static final long serialVersionUID = 1L;
}