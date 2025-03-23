package com.atg.springbootinit.model.vo;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/3/23 16:10
*/
@Data
public class WordVO implements Serializable {

    // 单词id
    private Long word_id;

    // 词

    private String word;
    // 词意
    private String definition;

    private static final long serialVersionUID = 1L;
}
