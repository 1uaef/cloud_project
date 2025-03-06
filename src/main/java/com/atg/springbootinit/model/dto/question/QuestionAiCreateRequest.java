package com.atg.springbootinit.model.dto.question;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/3/6 9:33
*/
@Data
public class QuestionAiCreateRequest implements Serializable {

    private String questionType;
    private int num = 3;

    private static final long serialVersionUID = 1L;
}
