package com.atg.springbootinit.manager.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.atg.springbootinit.manager.auth.SpaceUserAuthManager;
import com.atg.springbootinit.manager.auth.model.SpaceUserPermissionConstant;
import com.atg.springbootinit.model.entity.Picture;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.enums.SpaceTypeEnum;
import com.atg.springbootinit.service.PictureService;
import com.atg.springbootinit.service.SpaceService;
import com.atg.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/*
author: atg
time: 2025/2/25 16:40
*/

/*
 * WebSocket 拦截器，建立连接前要先校验
 */
@Component
@Slf4j
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest HttpServletRequest = servletRequest.getServletRequest();
            // 请求中获取参数 --- 图片id
            String pictureId = HttpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("pictureId是空");
                return false;
            }

            // 判断用户是否有权限访问该图片
            Picture picture = pictureService.getById(pictureId);
            if (ObjUtil.isEmpty(picture)) {
                log.error("图片不存在");
            }

            // 获取当前登录用户的权限
            User loginUser = userService.getLoginUser(HttpServletRequest);
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("用户未登录");
                return false;
            }

            Long spaceId = picture.getSpaceId();
            Space space = null;
            if (spaceId != null) {
                space = spaceService.getById(spaceId);
                if (ObjUtil.isEmpty(space)) {
                    log.error("空间不存在");
                    return false;
                }
                // 团队协同编辑
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.error("空间类型不匹配");
                    return false;
                }
            }

            // 判断是否拥有权限
            List<String> permissionsList = spaceUserAuthManager.getPermissionsList(space, loginUser);
            if (!permissionsList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                // 如果权限合法，则将用户信息放入 attributes 中
                log.error("用户没有权限");
                return false;
            }
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", pictureId);
        }
        return true;
    }
        @Override
        public void afterHandshake (ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler
        wsHandler, Exception exception){

        }
    }
