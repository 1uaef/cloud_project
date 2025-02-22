package com.atg.springbootinit.manager.auth;


import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.manager.auth.model.SpaceUserPermissionConstant;
import com.atg.springbootinit.model.entity.Picture;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.SpaceUser;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.enums.SpaceRoleEnum;
import com.atg.springbootinit.model.enums.SpaceTypeEnum;
import com.atg.springbootinit.service.PictureService;
import com.atg.springbootinit.service.SpaceService;
import com.atg.springbootinit.service.SpaceUserService;
import com.atg.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.atg.springbootinit.constant.UserConstant.USER_LOGIN_STATE;

/*
author: atg
time: 2025/2/21 14:46
*/
@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;
    // 获取上下文对象
    @Value("/api")
    private String contextPath;
    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private PictureService pictureService;
    @Autowired
    private UserService userService;
    @Autowired
    private SpaceService spaceService;

    public StpInterfaceImpl(SpaceUserAuthManager spaceUserAuthManager) {
        this.spaceUserAuthManager = spaceUserAuthManager;
    }

    /**
     * 请求中获取上下文对象
     */
    public SpaceUserAuthContext getAuthContextByRequest() {
        // 获取request-- 转换httpRequest请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());

        SpaceUserAuthContext authRequest;
        // 获取请求参数 转JSON
        if (ContentType.JSON.getValue().equals(contentType)) {
            // post
            String body = ServletUtil.getBody(request);
            // json 转对象
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            // get
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            // map 转对象
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        // 根据ID字段区分路径
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            // 获取请求业务路径的前缀
            String path = request.getRequestURI();
            String partUrl = path.replace(contextPath + "/", "");
            // 获取前缀的第一个斜杆
            String subBeforeUrl = StrUtil.subBefore(partUrl, "/", true);
            switch (subBeforeUrl) {
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                default:
                    break;
            }
        }
        return authRequest;
    }

    // 根据请求类型获取上下文

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (!StpKit.SPACE_TYPE.equals(loginType)) {
            return new ArrayList<>();
        }
        // 管理员--如果当前用户是管理员，则直接返回权限列表
        List<String> admin_permissionsByRole = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());

        // 获取上下文对象
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        if (isAllFildsNull(authContext)) {
            return admin_permissionsByRole;
        }

        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = loginUser.getId();

        // 获取spaceUser对象
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser != null) {
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        // 获取spaceUserId
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null) {
            // 根据spaceUserId
            SpaceUser byId = spaceUserService.getById(spaceUserId);
            if (byId == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "空间用户不存在");
            }
            // 获取当前登录用户对应的spaceUser
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getUserId, userId)
                    .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                    .one();
            if (loginSpaceUser == null) {
                return new ArrayList<>();
            }
            return spaceUserAuthManager.getPermissionsByRole(loginSpaceUser.getSpaceRole());

        }
        // 如果没有spaceId的情况--尝试通过spaceId or pictureId获取 space 对象
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null) {
            Long pictureId = authContext.getPictureId();
            if(pictureId == null){
                return admin_permissionsByRole;
            }
            Picture picture = pictureService.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getSpaceId, Picture::getId, Picture::getUserId)
                    .one();
            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "图片不存在");
            }
            spaceId = picture.getSpaceId();
            // 公共图库，仅本人或管理员可见
            if (spaceId == null) {
                if (picture.getUserId().equals(userId) || userService.isAdmin(loginUser)){
                    return admin_permissionsByRole;
                }else {
                    // 不是的话 可以查看
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }

        // 根据spaceId获取space对象
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "空间不存在");
        }
        // 根据space类型判断权限
       if (space.getSpaceType().equals(SpaceTypeEnum.PRIVATE.getValue())) {
          if (space.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
              return admin_permissionsByRole;
          }else {
              return new ArrayList<>();
          }
       }else {
           // 团队空间 查询spaceUser 并且获取角色的权限
           spaceUser = spaceUserService.lambdaQuery()
                   .eq(SpaceUser::getSpaceId, spaceId)
                   .eq(SpaceUser::getUserId, userId)
                   .one();

           if (spaceUser == null) {
               return new ArrayList<>();
           }
           return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
       }

    }

    private boolean isAllFildsNull(Object object) {
        if (object == null) {
            return true;
        }
        // 获取所有的字段 并且判断所有的字段是否为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                .map(field -> ReflectUtil.getFieldValue(object, field))
                .allMatch(value -> value == null); // 判断所有字段是否为空
    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        return new ArrayList<>();
    }
}
