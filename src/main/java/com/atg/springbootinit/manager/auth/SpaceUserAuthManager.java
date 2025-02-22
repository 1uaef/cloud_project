package com.atg.springbootinit.manager.auth;


import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.atg.springbootinit.manager.auth.model.SpaceUserAuthConfig;
import com.atg.springbootinit.manager.auth.model.SpaceUserRole;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/*
author: atg
time: 2025/2/21 13:54
*/
@Component
public class SpaceUserAuthManager {

    // 整个项目启动就获取一次，避免每次获取配置文件都去加载
    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    // 可加载配置文件到对象，并提供根据角色获取权限列表的方法。
    static {
        // TODO: 加载配置文件 json
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }
    // 根据角色获取权限列表
    public List<String> getPermissionsByRole(String role) {
        if (StrUtil.isBlank(role)) {
            return new ArrayList<>();
        }
        SpaceUserRole permissionRole = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> r.getKey().equals(role))
                .findFirst()
                .orElse(null);
        if (permissionRole == null) {
            return new ArrayList<>();
        }
        return permissionRole.getPermissions();
    }
}
