package com.atg.springbootinit.service;

import com.atg.springbootinit.model.dto.question_bank.QuestionBankList;
import com.atg.springbootinit.model.dto.question_bank.QuestionBankQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.atg.springbootinit.model.entity.QuestionBank;
import com.atg.springbootinit.model.vo.QuestionBankVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题库服务
 *

 */
public interface QuestionBankService extends IService<QuestionBank> {

    /**
     * 校验数据
     *
     * @param questionBank
     * @param add 对创建的数据进行校验
     */
    void validQuestionBank(QuestionBank questionBank, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionBankQueryRequest
     * @return
     */
    QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest questionBankQueryRequest);
    
    /**
     * 获取题库封装
     *
     * @param questionBank
     * @param request
     * @return
     */
    QuestionBankVO getQuestionBankVO(QuestionBank questionBank, HttpServletRequest request);

    /**
     * 分页获取题库封装
     *
     * @param questionBankPage
     * @param request
     * @return
     */
    Page<QuestionBankVO> getQuestionBankVOPage(Page<QuestionBank> questionBankPage, HttpServletRequest request);


    // 获取题库列表 -- 只要获取名字
    QuestionBankList getQuestionBankList();
}
