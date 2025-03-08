package com.atg.springbootinit.service;

import com.atg.springbootinit.model.dto.mockInterView.MockInterviewAddRequest;
import com.atg.springbootinit.model.dto.mockInterView.MockInterviewEventRequest;
import com.atg.springbootinit.model.dto.mockInterView.MockInterviewQueryRequest;
import com.atg.springbootinit.model.entity.MockInterview;
import com.atg.springbootinit.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 啊汤哥
* @description 针对表【mock_interview(模拟面试)】的数据库操作Service
* @createDate 2025-03-07 11:21:15
*/
public interface MockInterviewService extends IService<MockInterview> {

    // 根据用户id 创建模拟面试
    Long createMockInterview(MockInterviewAddRequest mockInterviewAddRequest, User LoginUser);

    // 查询模拟面试 构造查询条件
    QueryWrapper<MockInterview> getQueryWrapper(MockInterviewQueryRequest mockInterviewQueryRequest);

    // 处理模拟面试事件
    String handleMockInterviewEvent(MockInterviewEventRequest mockInterviewEventRequest, User loginUser);

}
