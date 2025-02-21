package com.atg.springbootinit.model.dto.space_user;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/21 8:54
*/
@Data
public class SpaceUserEditRequest implements Serializable {
    /**
     * 空间 id
     */
    private Long id;


    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}
