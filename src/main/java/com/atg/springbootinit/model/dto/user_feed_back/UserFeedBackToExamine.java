package com.atg.springbootinit.model.dto.user_feed_back;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/3/19 16:04
*/
@Data
public class UserFeedBackToExamine implements Serializable {

    private Long id;

    private Integer status;

    private static final long serialVersionUID = 1L;
}
