package com.atg.springbootinit.controller;


import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.dto.space.analysis.SpaceUsageAnalyzeRequest;
import com.atg.springbootinit.model.dto.space.analysis.SpaceUsageAnalyzeResponse;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.service.SpaceAnalyzeService;
import com.atg.springbootinit.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/*
author: atg
time: 2025/2/18 17:24
*/
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    @Resource
    private UserService userService;

    /**
     * 获取空间使用状态
     */
    @PostMapping("/analyzeSpaceUsage")
    public BaseResponse<SpaceUsageAnalyzeResponse> analyzeSpaceUsage(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = spaceAnalyzeService.analyzeSpaceUsage(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyzeResponse);

    }
}
