package com.atg.springbootinit.controller;


import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.dto.space.analysis.req.*;
import com.atg.springbootinit.model.dto.space.analysis.resp.*;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.service.SpaceAnalyzeService;
import com.atg.springbootinit.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 获取空间分类使用状态
     */
    @PostMapping("/analyzeSpaceCategory")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> analyzeSpaceCategory(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyzeResponses = spaceAnalyzeService.analyzeSpaceCategory(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceCategoryAnalyzeResponses);

    }

    /**
     * 获取空间标签使用状态
     */
    @PostMapping("/analyzeSpaceTag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> analyzeSpaceTag(
            @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> spaceTagAnalyzeResponses = spaceAnalyzeService.analyzeSpaceTag(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceTagAnalyzeResponses);

    }

    /**
     * 获取空间大小使用状态
     */
    @PostMapping("/analyzeSpaceSize")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> analyzeSpaceSize(
            @RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> spaceSizeAnalyzeResponses = spaceAnalyzeService.analyzeSpaceSize(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceSizeAnalyzeResponses);

    }


    /**
     *  用户行为分析
     */
    @PostMapping("/analyzeSpaceUser")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> analyzeSpaceUser(
            @RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> spaceUserAnalyzeResponses = spaceAnalyzeService.analyzeSpaceUser(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUserAnalyzeResponses);

    }


    /**
     * 获取空间排行
     */
    @PostMapping("/analyzeSpaceRank")
    public BaseResponse<List<Space>> analyzeSpaceRank(
            @RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<Space> spaceUsageAnalyzeResponses = spaceAnalyzeService.analyzeSpaceRank(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyzeResponses);

    }


}
