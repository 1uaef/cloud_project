package com.atg.springbootinit.model.dto.space;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/11 9:45
*/
@Data
public class SpaceAddRequest implements Serializable {
    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 空间等级 --0-普通  1-专业 2-旗舰
     */
    private Integer spaceLevel;


    private static final long serialVersionUID = 1L;
}
