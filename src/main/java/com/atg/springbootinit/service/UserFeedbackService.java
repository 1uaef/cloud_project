package com.atg.springbootinit.service;

import com.atg.springbootinit.model.dto.user_feed_back.UserFeedbackQueryRequest;
import com.atg.springbootinit.model.entity.Feedback;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.atg.springbootinit.model.vo.UserFeedbackVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户反馈服务
 *

 */
public interface UserFeedbackService extends IService<Feedback> {

    /**
     * 校验数据
     *
     * @param UserFeedback
     * @param add 对创建的数据进行校验
     */
    void validFeedback(Feedback UserFeedback, boolean add);

    /**
     * 获取查询条件
     *
     * @param UserFeedbackQueryRequest
     * @return
     */
    QueryWrapper<Feedback> getQueryWrapper(UserFeedbackQueryRequest UserFeedbackQueryRequest);
    
    /**
     * 获取用户反馈封装
     *
     * @param UserFeedback
     * @param request
     * @return
     */
    UserFeedbackVO getUserFeedbackVO (Feedback UserFeedback, HttpServletRequest request);

    /**
     * 分页获取用户反馈封装
     *
     * @param UserFeedbackPage
     * @param request
     * @return
     */
    Page<UserFeedbackVO> getUserFeedbackVOPage(Page<Feedback> UserFeedbackPage, HttpServletRequest request);

    // 审核
    void toExamine(Long id, Integer status);

}
