package com.atg.springbootinit.manager.auth;


import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.atg.springbootinit.manager.auth.model.SpaceUserAuthConfig;
import com.atg.springbootinit.manager.auth.model.SpaceUserPermissionConstant;
import com.atg.springbootinit.manager.auth.model.SpaceUserRole;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.SpaceUser;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.enums.SpaceRoleEnum;
import com.atg.springbootinit.model.enums.SpaceTypeEnum;
import com.atg.springbootinit.service.SpaceUserService;
import com.atg.springbootinit.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
author: atg
time: 2025/2/21 13:54
*/
@Component
public class SpaceUserAuthManager {

    @Resource
    private UserService userService;
    @Resource
    private SpaceUserService spaceUserService;


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

    // 获取权限列表  根据的是登录用户获取的权限
    public List<String> getPermissionsList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限列表
        List<String> permissionsByRole = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return permissionsByRole;
            }
            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
        }
        // 获取图库 -- 类型 -- 私有还是团队
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum enumByValue = SpaceTypeEnum.getEnumByValue(spaceType);
        if (enumByValue == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (enumByValue) {
            case PRIVATE: // 私有图库
                if (userService.isAdmin(loginUser) || space.getUserId().equals(loginUser.getId())) {
                    return permissionsByRole;
                } else {
                    return new ArrayList<>();
                }
            case TEAM: // 团队图库   查询对应的角色
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                }
                return getPermissionsByRole(spaceUser.getSpaceRole());

        }
        return new ArrayList<>();
    }
}
