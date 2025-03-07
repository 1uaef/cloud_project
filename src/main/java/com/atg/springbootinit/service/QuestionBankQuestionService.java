package com.atg.springbootinit.service;

import com.atg.springbootinit.model.dto.question_bank_question.QuestionBankQuestionQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atg.springbootinit.model.entity.QuestionBankQuestion;
import com.atg.springbootinit.model.vo.QuestionBankQuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 题目题库关系服务
 *

 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add 对创建的数据进行校验
     */
    void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest);
    
    /**
     * 获取题目题库关系封装
     *
     * @param QuestionBankQuestion
     * @param request
     * @return
     */
    QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion QuestionBankQuestion, HttpServletRequest request);

    /**
     * 分页获取题目题库关系封装
     *
     * @param QuestionBankQuestionPage
     * @param request
     * @return
     */
    Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> QuestionBankQuestionPage, HttpServletRequest request);
}
