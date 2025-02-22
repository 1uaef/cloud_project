package com.atg.springbootinit.manager.auth.model;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/*
author: atg
time: 2025/2/21 13:47
*/
@Data
public class SpaceUserAuthConfig implements Serializable {

    private List<SpaceUserPermission> permissions;
    private List<SpaceUserRole> roles;

    private static final long serialVersionUID = 1L;
}
