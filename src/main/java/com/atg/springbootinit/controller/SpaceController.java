package com.atg.springbootinit.controller;


import com.atg.springbootinit.annotation.AuthCheck;
import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.DeleteRequest;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.constant.UserConstant;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.manager.auth.SpaceUserAuthManager;
import com.atg.springbootinit.model.dto.space.*;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.enums.SpaceLevelEnum;
import com.atg.springbootinit.model.vo.SpaceVO;
import com.atg.springbootinit.service.SpaceService;
import com.atg.springbootinit.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/*
author: atg
time: 2025/2/6 17:37
*/
@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 添加空间
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {

        if (spaceAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(spaceId);
    }

    /**
     * 删除空间
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long deleteRequestId = deleteRequest.getId();
        Space oldSpace = spaceService.getById(deleteRequestId);
        if (oldSpace == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 删除-// 仅本人或者管理员可删除
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getId().equals(oldSpace.getUserId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = spaceService.removeById(deleteRequestId);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        return ResultUtils.success(true);
    }

    /**
     * 更新空间--管理员可修改
     * todo 看更新的数据是否存在--id
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 实体类转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);

        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);

        spaceService.validSpace(space, false);
        //
        Space oldSpace = spaceService.getById(spaceUpdateRequest.getId());
        if (oldSpace == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        boolean b = spaceService.updateById(space);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 获取空间--管理员可获取
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Space space = spaceService.getById(id);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(space);
    }

    /**
     * 根据 id 获取空间（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Space space = spaceService.getById(id);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
        User user = userService.getLoginUser(request);

        List<String> permissionsList = spaceUserAuthManager.getPermissionsList(space, user);
        spaceVO.setPermissionList(permissionsList);
        return ResultUtils.success(spaceVO);
    }

    /**
     * 获取空间列表（管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        Page<Space> page = spaceService.page(new Page<>(current, pageSize), spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(page);
    }

    /**
     * 获取空间列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        if (spaceQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);

        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize),
                spaceService.getQueryWrapper(spaceQueryRequest));
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));

    }


    /**
     * 编辑空间--用户
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        spaceService.fillSpaceBySpaceLevel(space);
        space.setEditTime(new Date());
        spaceService.validSpace(space, false);

        User loginUser = userService.getLoginUser(request);
        Space oldSpace = spaceService.getById(spaceEditRequest.getId());
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // 获取空间列表
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> getSpaceLevelList() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize())).collect(Collectors.toList());

        return ResultUtils.success(spaceLevelList);

    }

}
