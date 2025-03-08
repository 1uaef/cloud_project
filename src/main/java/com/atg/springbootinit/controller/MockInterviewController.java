package com.atg.springbootinit.controller;


import com.atg.springbootinit.annotation.AuthCheck;
import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.DeleteRequest;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.dto.mockInterView.MockInterviewAddRequest;
import com.atg.springbootinit.model.dto.mockInterView.MockInterviewEventRequest;
import com.atg.springbootinit.model.dto.mockInterView.MockInterviewQueryRequest;
import com.atg.springbootinit.model.entity.MockInterview;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.service.MockInterviewService;
import com.atg.springbootinit.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/*
author: atg
time: 2025/3/7 11:26
description: 模拟面试控制器
*/
@RestController
@RequestMapping("/mockInterview")
@Slf4j
public class MockInterviewController {

    @Resource
    private MockInterviewService mockInterviewService;

    @Resource
    private UserService userService;

    // 添加面试
    @PostMapping("/add")
    public BaseResponse<Long> addMockInterview(@RequestBody MockInterviewAddRequest mockInterviewAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(mockInterviewAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long mockInterview = mockInterviewService.createMockInterview(mockInterviewAddRequest, loginUser);
        return ResultUtils.success(mockInterview);

    }

    // 删除面试
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteMockInterview(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        // TODO: 删除之前 判断是面试是否存在
        Long id = deleteRequest.getId();
        MockInterview byId = mockInterviewService.getById(id);
        ThrowUtils.throwIf(byId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return getBooleanBaseResponse(loginUser, byId.getUserId(), userService.isAdmin(loginUser), mockInterviewService.removeById(id), id, byId);
    }

    static BaseResponse<Boolean> getBooleanBaseResponse(User loginUser, Long userId, boolean admin, boolean b, Long id, MockInterview byId) {
        if (!userId.equals(loginUser.getId()) && !admin) {
            // TODO: 删除面试 仅本人或者管理员可删除
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = b;
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // 根据id 获取面试记录
    @PostMapping("/get")
    public BaseResponse<MockInterview> getMockInterviewById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        MockInterview mockInterview = mockInterviewService.getById(id);
        return ResultUtils.success(mockInterview);
    }

    // 分页获取用户面试记录 （自己）
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<MockInterview>> listMockInterviewVoByPage(@RequestBody MockInterviewQueryRequest mockInterviewQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(mockInterviewQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long size = mockInterviewQueryRequest.getPageSize();
        long current = mockInterviewQueryRequest.getCurrent();
        long pageSize = mockInterviewQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 15, ErrorCode.PARAMS_ERROR);
        // 查询的是自己
        mockInterviewQueryRequest.setUserId(loginUser.getId());
        Page<MockInterview> queryPage = new Page<>(current, pageSize);
        Page<MockInterview> mockInterviewPage = mockInterviewService.page(queryPage, mockInterviewService.getQueryWrapper(mockInterviewQueryRequest));
        return ResultUtils.success(mockInterviewPage);
    }

    // 分页获取用户面试记录 （管理员）
    @PostMapping("/list/page/")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<MockInterview>> listMockInterviewByPageAdmin(@RequestBody MockInterviewQueryRequest mockInterviewQueryRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(mockInterviewQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = mockInterviewQueryRequest.getCurrent();
        long pageSize = mockInterviewQueryRequest.getPageSize();

        Page<MockInterview> queryPage = new Page<>(current, pageSize);
        Page<MockInterview> mockInterviewPage = mockInterviewService.page(queryPage, mockInterviewService.getQueryWrapper(mockInterviewQueryRequest));
        return ResultUtils.success(mockInterviewPage);
    }

    // 处理模拟面试
    @PostMapping("/process/interview")

    public BaseResponse<String> processMockInterview(@RequestBody MockInterviewEventRequest mockInterviewEventRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(mockInterviewEventRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String chatMessage = mockInterviewService.handleMockInterviewEvent(mockInterviewEventRequest, loginUser);
        return ResultUtils.success(chatMessage);

    }
}
