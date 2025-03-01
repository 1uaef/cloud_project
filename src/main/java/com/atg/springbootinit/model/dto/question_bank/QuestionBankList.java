package com.atg.springbootinit.model.dto.question_bank;


import lombok.Data;

import java.util.List;

/*
author: atg
time: 2025/2/28 15:13
*/
// 题库列表
@Data
public class QuestionBankList  {

    // 所有题库
    private List<String> questionBankList;


}
