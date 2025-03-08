package com.atg.springbootinit.model.dto.mockInterView;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/3/7 13:50
*/
@Data
public class MockInterviewEventRequest implements Serializable {
    // 事件类型
    private String event;

    // 消息内容
    private String message;

    // 面试记录id

    private Long id;

    private static final long serialVersionUID = 1L;
}
