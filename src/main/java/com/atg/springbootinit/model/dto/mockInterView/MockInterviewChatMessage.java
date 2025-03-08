package com.atg.springbootinit.model.dto.mockInterView;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/3/8 9:09
*/
@Data
public class MockInterviewChatMessage implements Serializable {

    private String role;

    private String message;

    private static final long serialVersionUID = 1L;
}
