package com.atg.springbootinit.model.dto.space;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/11 9:47
*/
@Data
public class SpaceEditRequest implements Serializable {
    /**
     * 空间 id
     */
    private Long id;

    private String spaceName;

    private static final long serialVersionUID = 1L;
}
