package com.atg.springbootinit.model.dto.space_user;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/21 8:55
*/
@Data
public class SpaceUserQueryRequest implements Serializable {

    /**
     * id
     */
    private Long id;
    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}


