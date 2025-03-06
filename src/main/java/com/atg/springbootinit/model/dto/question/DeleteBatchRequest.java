package com.atg.springbootinit.model.dto.question;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/*
author: atg
time: 2025/3/6 14:35
*/
@Data
public class DeleteBatchRequest implements Serializable {
    private List<Long> ids;
    private static final long serialVersionUID = 1L;
}