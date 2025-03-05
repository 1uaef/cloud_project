package com.atg.springbootinit.controller;

import com.atg.springbootinit.model.dto.question_bank_question.QuestionBankQuestionAddRequest;
import com.atg.springbootinit.model.dto.question_bank_question.QuestionBankQuestionEditRequest;
import com.atg.springbootinit.model.dto.question_bank_question.QuestionBankQuestionQueryRequest;
import com.atg.springbootinit.model.dto.question_bank_question.QuestionBankQuestionUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.atg.springbootinit.annotation.AuthCheck;
import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.DeleteRequest;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.constant.UserConstant;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;

import com.atg.springbootinit.model.entity.QuestionBankQuestion;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.QuestionBankQuestionVO;
import com.atg.springbootinit.service.QuestionBankQuestionService;
import com.atg.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目题库关系接口
 *

 */
@RestController
@RequestMapping("/questionBankQuestion")
@Slf4j
public class QuestionBankQuestionController {

    @Resource

    private QuestionBankQuestionService QuestionBankQuestionService;

    @Resource
    private UserService userService;


    /**
     * 创建题目题库关系
     *
     * @param QuestionBankQuestionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestionBankQuestion(@RequestBody QuestionBankQuestionAddRequest QuestionBankQuestionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(QuestionBankQuestionAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBankQuestion QuestionBankQuestion = new QuestionBankQuestion();
        BeanUtils.copyProperties(QuestionBankQuestionAddRequest, QuestionBankQuestion);
        // 数据校验
        QuestionBankQuestionService.validQuestionBankQuestion(QuestionBankQuestion, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        QuestionBankQuestion.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = QuestionBankQuestionService.save(QuestionBankQuestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionBankQuestionId = QuestionBankQuestion.getId();
        return ResultUtils.success(newQuestionBankQuestionId);
    }

    /**
     * 删除题目题库关系
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionBankQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionBankQuestion oldQuestionBankQuestion = QuestionBankQuestionService.getById(id);
        ThrowUtils.throwIf(oldQuestionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionBankQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = QuestionBankQuestionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目题库关系（仅管理员可用）
     *
     * @param questionBankQuestionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionBankQuestion(@RequestBody QuestionBankQuestionUpdateRequest questionBankQuestionUpdateRequest) {
        if (questionBankQuestionUpdateRequest == null ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
        BeanUtils.copyProperties(questionBankQuestionUpdateRequest, questionBankQuestion);
        // 数据校验
        QuestionBankQuestionService.validQuestionBankQuestion(questionBankQuestion, false);
        // 判断是否存在
        long id = questionBankQuestionUpdateRequest.getId();
        QuestionBankQuestion oldQuestionBankQuestion = QuestionBankQuestionService.getById(id);
        ThrowUtils.throwIf(oldQuestionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = QuestionBankQuestionService.updateById(questionBankQuestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目题库关系（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionBankQuestionVO> getQuestionBankQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        QuestionBankQuestion QuestionBankQuestion = QuestionBankQuestionService.getById(id);
        ThrowUtils.throwIf(QuestionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionService.getQuestionBankQuestionVO(QuestionBankQuestion, request);
        return ResultUtils.success(questionBankQuestionVO);
    }

    /**
     * 分页获取题目题库关系列表（仅管理员可用）
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBankQuestion>> listQuestionBankQuestionByPage(@RequestBody QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        long current = questionBankQuestionQueryRequest.getCurrent();
        long size = questionBankQuestionQueryRequest.getPageSize();
        // 查询数据库
        Page<QuestionBankQuestion> QuestionBankQuestionPage = QuestionBankQuestionService.page(new Page<>(current, size),
                QuestionBankQuestionService.getQueryWrapper(questionBankQuestionQueryRequest));
        return ResultUtils.success(QuestionBankQuestionPage);
    }

    /**
     * 分页获取题目题库关系列表（封装类）
     *
     * @param questionBankQuestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionBankQuestionVO>> listQuestionBankQuestionVOByPage(@RequestBody QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionBankQuestionQueryRequest.getCurrent();
        long size = questionBankQuestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBankQuestion> QuestionBankQuestionPage = QuestionBankQuestionService.page(new Page<>(current, size),
                QuestionBankQuestionService.getQueryWrapper(questionBankQuestionQueryRequest));
        // 获取封装类
        return ResultUtils.success(QuestionBankQuestionService.getQuestionBankQuestionVOPage(QuestionBankQuestionPage, request));
    }

    /**
     * 分页获取当前登录用户创建的题目题库关系列表
     *
     * @param QuestionBankQuestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionBankQuestionVO>> listMyQuestionBankQuestionVOByPage(@RequestBody QuestionBankQuestionQueryRequest QuestionBankQuestionQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(QuestionBankQuestionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        QuestionBankQuestionQueryRequest.setUserId(loginUser.getId());
        long current = QuestionBankQuestionQueryRequest.getCurrent();
        long size = QuestionBankQuestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBankQuestion> QuestionBankQuestionPage = QuestionBankQuestionService.page(new Page<>(current, size),
                QuestionBankQuestionService.getQueryWrapper(QuestionBankQuestionQueryRequest));
        // 获取封装类
        return ResultUtils.success(QuestionBankQuestionService.getQuestionBankQuestionVOPage(QuestionBankQuestionPage, request));
    }


}
