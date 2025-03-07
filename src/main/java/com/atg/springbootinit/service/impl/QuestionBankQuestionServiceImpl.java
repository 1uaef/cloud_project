package com.atg.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.atg.springbootinit.model.dto.question_bank_question.QuestionBankQuestionQueryRequest;
import com.atg.springbootinit.model.entity.Question;
import com.atg.springbootinit.model.entity.QuestionBank;
import com.atg.springbootinit.service.QuestionBankService;
import com.atg.springbootinit.service.QuestionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.constant.CommonConstant;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.mapper.QuestionBankQuestionMapper;

import com.atg.springbootinit.model.entity.QuestionBankQuestion;

import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.QuestionBankQuestionVO;
import com.atg.springbootinit.model.vo.UserVO;
import com.atg.springbootinit.service.QuestionBankQuestionService;
import com.atg.springbootinit.service.UserService;
import com.atg.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目题库关系服务实现
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private QuestionBankService questionBankService;


    @Resource
    private QuestionService questionService;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add                  对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        Long questionBankId = questionBankQuestion.getQuestionBankId();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            QuestionBank byId = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(byId == null, ErrorCode.PARAMS_ERROR, "题库不存在");
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        Long questionId = questionBankQuestion.getQuestionId();
        if (questionId != null) {
            Question byId = questionService.getById(questionId);
            ThrowUtils.throwIf(byId == null, ErrorCode.PARAMS_ERROR, "题目不存在");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        // 创建查询条件构造器
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long notId = questionBankQuestionQueryRequest.getNotId();
        Long id = questionBankQuestionQueryRequest.getId();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        Long userId = questionBankQuestionQueryRequest.getUserId();
        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();


        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.ne(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目题库关系封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // 1. 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestionVO.setUser(userVO);
        return questionBankQuestionVO;
    }

    /**
     * 分页获取题目题库关系封装
     *
     * @param QuestionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> QuestionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> QuestionBankQuestionList = QuestionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> QuestionBankQuestionVOPage = new Page<>(QuestionBankQuestionPage.getCurrent(), QuestionBankQuestionPage.getSize(), QuestionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(QuestionBankQuestionList)) {
            return QuestionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> QuestionBankQuestionVOList = QuestionBankQuestionList.stream().map(QuestionBankQuestion -> {
            return QuestionBankQuestionVO.objToVo(QuestionBankQuestion);
        }).collect(Collectors.toList());

        // 1. 关联查询用户信息
        Set<Long> userIdSet = QuestionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 2. 填充 用户信息
        QuestionBankQuestionVOList.forEach(questionBankQuestionVO -> {
            Long userId = questionBankQuestionVO.getUserId();

            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankQuestionVO.setUser(userService.getUserVO(user));
        });

        QuestionBankQuestionVOPage.setRecords(QuestionBankQuestionVOList);
        return QuestionBankQuestionVOPage;
    }

}
