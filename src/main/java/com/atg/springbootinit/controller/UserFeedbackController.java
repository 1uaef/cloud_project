package com.atg.springbootinit.controller;

import com.atg.springbootinit.model.dto.user_feed_back.UserFeedbackAddRequest;
import com.atg.springbootinit.model.dto.user_feed_back.UserFeedbackQueryRequest;
import com.atg.springbootinit.model.entity.Feedback;
import com.atg.springbootinit.model.enums.SuggestionStatusEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.atg.springbootinit.annotation.AuthCheck;
import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.DeleteRequest;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.constant.UserConstant;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.UserFeedbackVO;
import com.atg.springbootinit.service.UserFeedbackService;
import com.atg.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户反馈接口
 *

 */
@RestController
@RequestMapping("/UserFeedback")
@Slf4j
public class UserFeedbackController {

    @Resource
    private UserFeedbackService UserFeedbackService;

    @Resource
    private UserService userService;


    /**
     * 创建用户反馈
     *
     * @param userFeedbackAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserFeedback(@RequestBody UserFeedbackAddRequest userFeedbackAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userFeedbackAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Feedback UserFeedback = new Feedback();
        BeanUtils.copyProperties(userFeedbackAddRequest, UserFeedback);
        // 数据校验
        UserFeedbackService.validFeedback(UserFeedback, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        UserFeedback.setUserId(loginUser.getId());
        UserFeedback.setStatus(Integer.parseInt(SuggestionStatusEnum.UNDER_REVIEW.getValue()));
        // 写入数据库
        boolean result = UserFeedbackService.save(UserFeedback);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newUserFeedbackId = UserFeedback.getId();
        return ResultUtils.success(newUserFeedbackId);
    }

    /**
     * 删除用户反馈 有管理员可以删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserFeedback(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Feedback oldUserFeedback = UserFeedbackService.getById(id);
        ThrowUtils.throwIf(oldUserFeedback == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅管理员可删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = UserFeedbackService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 分页获取用户反馈列表（仅管理员可用）
     *
     * @param UserFeedbackQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Feedback>> listUserFeedbackByPage(@RequestBody UserFeedbackQueryRequest UserFeedbackQueryRequest) {
        long current = UserFeedbackQueryRequest.getCurrent();
        long size = UserFeedbackQueryRequest.getPageSize();
        // 查询数据库
        Page<Feedback> UserFeedbackPage = UserFeedbackService.page(new Page<>(current, size),
                UserFeedbackService.getQueryWrapper(UserFeedbackQueryRequest));
        return ResultUtils.success(UserFeedbackPage);
    }

    /**
     * 分页获取用户反馈列表（封装类）
     *
     * @param UserFeedbackQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserFeedbackVO>> listUserFeedbackVOByPage(@RequestBody UserFeedbackQueryRequest UserFeedbackQueryRequest,
                                                               HttpServletRequest request) {
        long current = UserFeedbackQueryRequest.getCurrent();
        long size = UserFeedbackQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Feedback> UserFeedbackPage = UserFeedbackService.page(new Page<>(current, size),
                UserFeedbackService.getQueryWrapper(UserFeedbackQueryRequest));
        // 获取封装类
        return ResultUtils.success(UserFeedbackService.getUserFeedbackVOPage(UserFeedbackPage, request));
    }

    /**
     * 分页获取当前登录用户创建的用户反馈列表
     *
     * @param UserFeedbackQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserFeedbackVO>> listMyUserFeedbackVOByPage(@RequestBody UserFeedbackQueryRequest UserFeedbackQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(UserFeedbackQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        UserFeedbackQueryRequest.setUserId(loginUser.getId());
        long current = UserFeedbackQueryRequest.getCurrent();
        long size = UserFeedbackQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Feedback> UserFeedbackPage = UserFeedbackService.page(new Page<>(current, size),
                UserFeedbackService.getQueryWrapper(UserFeedbackQueryRequest));
        // 获取封装类
        return ResultUtils.success(UserFeedbackService.getUserFeedbackVOPage(UserFeedbackPage, request));
    }

}
