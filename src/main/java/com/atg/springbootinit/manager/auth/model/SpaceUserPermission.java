package com.atg.springbootinit.manager.auth.model;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/21 13:49
*/
@Data
public class SpaceUserPermission implements Serializable {

    /**
     * 权限key
     */
    private String key;

    /**
     * 权限值
     */
    private String value;

    /**
     * 权限描述
     */
    private String description;

    private static final long serialVersionUID = 1L;
}
