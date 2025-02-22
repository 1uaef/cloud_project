package com.atg.springbootinit.controller;


import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.DeleteRequest;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.manager.auth.annotation.SaSpaceCheckPermission;
import com.atg.springbootinit.manager.auth.model.SpaceUserPermissionConstant;
import com.atg.springbootinit.model.dto.space.SpaceQueryRequest;
import com.atg.springbootinit.model.dto.space_user.SpaceUserAddRequest;
import com.atg.springbootinit.model.dto.space_user.SpaceUserEditRequest;
import com.atg.springbootinit.model.dto.space_user.SpaceUserQueryRequest;
import com.atg.springbootinit.model.entity.SpaceUser;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.SpaceUserVO;
import com.atg.springbootinit.service.SpaceUserService;
import com.atg.springbootinit.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/*
author: atg
time: 2025/2/21 10:12
*/
@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    /**
     * 创建空间用户
     *
     * @param spaceUserAddRequest
     * @return
     */
    @RequestMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        if (spaceUserAddRequest == null) {
            throw new RuntimeException("参数错误");
        }
        long id = spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(id);
    }

    //  删除团队空间成员
    @RequestMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new RuntimeException("参数错误");
        }
        Long id = deleteRequest.getId();

        SpaceUser byId = spaceUserService.getById(id);
        if (byId == null) {
            throw new RuntimeException("空间成员不存在");
        }
        boolean b = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);

    }

    // 修改团队空间成员信息--edit
    @RequestMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserEditRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(spaceUserEditRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserEditRequest, spaceUser);
        spaceUserService.validSpaceUser(spaceUser, false);
        Long id = spaceUserEditRequest.getId();
        SpaceUser byId = spaceUserService.getById(id);
        if (byId == null) {
            throw new RuntimeException("空间成员不存在");
        }
        boolean b = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    // 查询某个成员在某个空间的信息
    @RequestMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);


        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();

        ThrowUtils.throwIf(spaceId == null || userId == null , ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        if (spaceUser == null) {
            throw new RuntimeException("空间成员不存在");
        }
        return ResultUtils.success(spaceUser);
    }
    // 查询团队空间列表  空间列表包含了查询操作
    @RequestMapping("/list")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody  SpaceUserQueryRequest spaceUserQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    // 查询加入的空间列表
    @RequestMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMySpaceUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }
}


