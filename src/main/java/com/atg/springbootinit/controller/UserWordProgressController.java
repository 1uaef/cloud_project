package com.atg.springbootinit.controller;
import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.dto.user_word_progress.UserWordProgressAddRequest;
import com.atg.springbootinit.model.dto.user_word_progress.UserWordProgressQueryRequest;
import com.atg.springbootinit.model.vo.UserWordProgressVO;
import com.atg.springbootinit.service.UserWordProgressService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/*
author: atg
time: 2025/3/30 16:07
*/


/**
 * 用户单词进度控制器
 */
@RestController
@RequestMapping("/userWordProgress")
public class UserWordProgressController {
    @Resource
    private UserWordProgressService userWordProgressService;
    /**
     * 添加或更新学习记录
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addProgress(@RequestBody UserWordProgressAddRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Boolean result = userWordProgressService.addOrUpdateProgress(request, httpRequest);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询学习记录
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<UserWordProgressVO>> listProgressByPage(@RequestBody UserWordProgressQueryRequest request,
                                                                     HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Page<UserWordProgressVO> result = userWordProgressService.listProgressByPage(request, httpRequest);
        return ResultUtils.success(result);
    }

}
