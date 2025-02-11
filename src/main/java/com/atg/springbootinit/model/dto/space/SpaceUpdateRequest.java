package com.atg.springbootinit.model.dto.space;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/11 9:48
*/
// 更新请求 --管理员
@Data
public class SpaceUpdateRequest implements Serializable {

    private Long id;

    private String spaceName;

    private Integer spaceLevel;

    private Long maxSize;

    private Long maxCount;

    private static final long serialVersionUID = 1L;
}
