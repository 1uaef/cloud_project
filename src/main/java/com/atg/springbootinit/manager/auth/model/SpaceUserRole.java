package com.atg.springbootinit.manager.auth.model;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/*
author: atg
time: 2025/2/21 13:51
*/
@Data
public class SpaceUserRole implements Serializable {

    // 角色标识
    private String key;

    // 角色名称
    private String name;

    // 角色权限
    private List<String> permissions;

    // 角色描述
    private String description;



    private static final long serialVersionUID = 1L;
}
