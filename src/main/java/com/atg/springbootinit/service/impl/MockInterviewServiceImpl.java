package com.atg.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.manager.Ai.AiManager;
import com.atg.springbootinit.model.dto.mockInterView.MockInterviewAddRequest;
import com.atg.springbootinit.model.dto.mockInterView.MockInterviewChatMessage;
import com.atg.springbootinit.model.dto.mockInterView.MockInterviewEventRequest;
import com.atg.springbootinit.model.dto.mockInterView.MockInterviewQueryRequest;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.enums.MockInterviewEventEnum;
import com.atg.springbootinit.model.enums.MockInterviewStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atg.springbootinit.model.entity.MockInterview;
import com.atg.springbootinit.service.MockInterviewService;
import com.atg.springbootinit.mapper.MockInterviewMapper;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 啊汤哥
 * @description 针对表【mock_interview(模拟面试)】的数据库操作Service实现
 * @createDate 2025-03-07 11:21:15
 */
@Service
public class MockInterviewServiceImpl extends ServiceImpl<MockInterviewMapper, MockInterview>
        implements MockInterviewService {


    @Resource
    private AiManager aiManager;

    @Override
    public Long createMockInterview(MockInterviewAddRequest mockInterviewAddRequest, User LoginUser) {
        // 1. 参数校验
        if (mockInterviewAddRequest == null) {
            throw new RuntimeException("参数错误");
        }
        // 2. 数据封装到实体类
        String workExperience = mockInterviewAddRequest.getWorkExperience();
        MockInterview mockInterview = getMockInterview(mockInterviewAddRequest, LoginUser, workExperience);
        // 3. 插入数据
        boolean save = this.save(mockInterview);
        if (!save) {
            throw new RuntimeException("创建失败");
        }
        // 4. 返回 结果
        return mockInterview.getId();
    }

    @Override
    public QueryWrapper<MockInterview> getQueryWrapper(MockInterviewQueryRequest mockInterviewQueryRequest) {
        QueryWrapper<MockInterview> queryWrapper = new QueryWrapper<>();
        if (mockInterviewQueryRequest == null) {
            return queryWrapper;
        }
        // 对象中取值 -- 拼接查询(补充查询条件)
        Long id = mockInterviewQueryRequest.getId();
        String workExperience = mockInterviewQueryRequest.getWorkExperience();
        String jobPosition = mockInterviewQueryRequest.getJobPosition();
        String difficulty = mockInterviewQueryRequest.getDifficulty();
        Integer status = mockInterviewQueryRequest.getStatus();
        Long userId = mockInterviewQueryRequest.getUserId();
        int current = mockInterviewQueryRequest.getCurrent();
        int pageSize = mockInterviewQueryRequest.getPageSize();
        String sortField = mockInterviewQueryRequest.getSortField();
        String sortOrder = mockInterviewQueryRequest.getSortOrder();

        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.like(StringUtils.isNotEmpty(workExperience), "workExperience", workExperience);
        queryWrapper.like(StringUtils.isNotEmpty(jobPosition), "jobPosition", jobPosition);
        queryWrapper.like(StringUtils.isNotEmpty(difficulty), "difficulty", difficulty);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(StringUtils.isNotEmpty(sortField), sortOrder.equals("asc"), sortField);
        return queryWrapper;
    }

    private MockInterview getMockInterview(MockInterviewAddRequest mockInterviewAddRequest, User LoginUser, String workExperience) {
        String jobPosition = mockInterviewAddRequest.getJobPosition();
        String difficulty = mockInterviewAddRequest.getDifficulty();
        if (workExperience == null || jobPosition == null || difficulty == null) {
            throw new RuntimeException("参数错误");
        }
        MockInterview mockInterview = new MockInterview();
        mockInterview.setWorkExperience(workExperience);
        mockInterview.setJobPosition(jobPosition);
        mockInterview.setDifficulty(difficulty);
        mockInterview.setUserId(LoginUser.getId());
        mockInterview.setStatus(MockInterviewStatusEnum.TO_START.getValue());
        return mockInterview;
    }

    @Override
    public String handleMockInterviewEvent(MockInterviewEventRequest mockInterviewEventRequest, User loginUser) {
        Long id = mockInterviewEventRequest.getId();
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        // 获取模拟面试 id 看在不在
        MockInterview mockInterview = this.getById(id);
        ThrowUtils.throwIf(mockInterview == null, ErrorCode.PARAMS_ERROR, "模拟面试未创建");
        // 获取当前用户  是不是创建者

        if (!Objects.equals(loginUser.getId(), mockInterview.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }
        // 判断当前状态
        String event = mockInterviewEventRequest.getEvent();
        MockInterviewEventEnum enumByValue = MockInterviewEventEnum.getEnumByValue(event);

        switch (enumByValue) {
            // 开始 用户进入的模拟事件  构造消息 调用ai接口 然后存储 在返回
            case START:
                return handChatEventStart(mockInterview);

            case CHAT:
                return handChatEventChat(mockInterviewEventRequest, mockInterview);


            case END:
                return handChatEventEnd(mockInterview);


            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

    }

    private String handChatEventEnd(MockInterview mockInterview) {
        String historyMessages = mockInterview.getMessages();
        final List<MockInterviewChatMessage> chatMessagesHistoryList = JSONUtil.parseArray(historyMessages).toList(MockInterviewChatMessage.class);
        // 构造消息列表之前聊天的消息 + 当前消息
        final List<ChatMessage> chatMessages = chatMessagesHistoryList.stream().map(mockInterviewChatMessage -> {
            return ChatMessage.builder().role(ChatMessageRole.valueOf(StringUtils.upperCase(mockInterviewChatMessage.getRole()))).content(mockInterviewChatMessage.getMessage()).build();
        }).collect(Collectors.toList());
        String userPrompt = "结束";
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();
        chatMessages.add(userMessage);

        String chatAnswer = aiManager.doChat(chatMessages);

        chatMessages.add(ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(chatAnswer).build());

        // 保存消息记录 并且更新状态
        List<MockInterviewChatMessage> mockInterviewChatMessages = chatMessages.stream().map(chatMessage -> {
            MockInterviewChatMessage mockInterviewChatMessage = new MockInterviewChatMessage();
            mockInterviewChatMessage.setRole(chatMessage.getRole().value());
            mockInterviewChatMessage.setMessage(chatMessage.getContent().toString());
            return mockInterviewChatMessage;
        }).collect(Collectors.toList());

        String jsonStr = JSONUtil.toJsonStr(mockInterviewChatMessages);
        MockInterview updateMockInterView = new MockInterview();
        updateMockInterView.setId(mockInterview.getId());
        updateMockInterView.setMessages(jsonStr);
        updateMockInterView.setStatus(MockInterviewStatusEnum.IN_PROGRESS.getValue());
        boolean b = this.updateById(updateMockInterView);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR, "模拟面试更新失败");
        // 返回的是调用ai返回的接口
        return chatAnswer;
    }

    private String handChatEventChat(MockInterviewEventRequest mockInterviewEventRequest, MockInterview mockInterview) {
        // 构造消息列表， 把之前聊天的消息喂给ai 然后继续生成
        String message = mockInterviewEventRequest.getMessage();
        // 这个是在数据库中获取的
        String historyMessages = mockInterview.getMessages();

        final List<MockInterviewChatMessage> chatMessagesHistoryList = JSONUtil.parseArray(historyMessages).toList(MockInterviewChatMessage.class);
        // 构造消息列表之前聊天的消息 + 当前消息
        /**
         * {
         *     "role": "user",
         *     "message": "开始"
         * }
         */
        final List<ChatMessage> chatMessages = chatMessagesHistoryList.stream().map(mockInterviewChatMessage -> {
            return ChatMessage.builder().role(ChatMessageRole.valueOf(StringUtils.upperCase(mockInterviewChatMessage.getRole()))).content(mockInterviewChatMessage.getMessage()).build();
        }).collect(Collectors.toList());
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(message).build();
        chatMessages.add(userMessage);
        String chatAnswer = aiManager.doChat(chatMessages);

        ChatMessage chatAssistantMessage = ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(chatAnswer).build();
        chatMessages.add(chatAssistantMessage);
        // 消息记录对象的转换
        List<MockInterviewChatMessage> mockInterviewChatMessags = chatMessages.stream().map(chatMessage -> {
            MockInterviewChatMessage mockInterviewChatMessage = new MockInterviewChatMessage();
            mockInterviewChatMessage.setRole(chatMessage.getRole().value());
            mockInterviewChatMessage.setMessage(chatMessage.getContent().toString());
            return mockInterviewChatMessage;
        }).collect(Collectors.toList());
        // 转成json
        String jsonNewRecord = JSONUtil.toJsonStr(mockInterviewChatMessags);
        MockInterview newMockInterview = new MockInterview();
        newMockInterview.setId(mockInterview.getId());
        newMockInterview.setMessages(jsonNewRecord);

        // 如果ai主动结束面试 则修改状态
        if (chatAnswer.contains("【面试结束】")) {
            newMockInterview.setStatus(MockInterviewStatusEnum.ENDED.getValue());
        }
        boolean b = this.updateById(newMockInterview);
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新失败");
        }
        return chatAnswer;
    }

    private String handChatEventStart(MockInterview mockInterview) {
        // 定义 AI 的 Prompt
        String systemPrompt = String.format("你是一位严厉的程序员面试官，我是候选人，来应聘 %s 的 %s 岗位，面试难度为 %s。请你向我依次提出问题（最多 20 个问题），我也会依次回复。在这期间请完全保持真人面试官的口吻，比如适当引导学员、或者表达出你对学员回答的态度。\n" +
                "必须满足如下要求：\n" +
                "1. 当学员回复 “开始” 时，你要正式开始面试\n" +
                "2. 当学员表示希望 “结束面试” 时，你要结束面试\n" +
                "3. 此外，当你觉得这场面试可以结束时（比如候选人回答结果较差、不满足工作年限的招聘需求、或者候选人态度不礼貌），必须主动提出面试结束，不用继续询问更多问题了。并且要在回复中包含字符串【面试结束】\n" +
                "4. 面试结束后，应该给出候选人整场面试的表现和总结。", mockInterview.getWorkExperience(), mockInterview.getJobPosition(), mockInterview.getDifficulty());

        String userPrompt = "开始";
        // 定义存储消息列表
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();
        messages.add(systemMessage);
        messages.add(userMessage);

        // 调用 AI 接口
        String chatAnswer = aiManager.doChat(messages);
        ChatMessage Sequential_dialogue = ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(chatAnswer).build();
        messages.add(Sequential_dialogue);
        //  存入数据库得转JSON
        // 保存消息记录
        List<MockInterviewChatMessage> mockInterviewChatMessages = messages.stream().map(chatMessage -> {
            MockInterviewChatMessage mockInterviewChatMessage = new MockInterviewChatMessage();
            mockInterviewChatMessage.setRole(chatMessage.getRole().value());
            mockInterviewChatMessage.setMessage(chatMessage.getContent().toString());
            return mockInterviewChatMessage;
        }).collect(Collectors.toList());

        String jsonStr = JSONUtil.toJsonStr(mockInterviewChatMessages);

        // 存储消息 到数据库
        MockInterview upMockInterview = new MockInterview();
        upMockInterview.setId(mockInterview.getId());
        upMockInterview.setMessages(jsonStr);
        upMockInterview.setStatus(MockInterviewStatusEnum.IN_PROGRESS.getValue());
        boolean b = this.updateById(upMockInterview);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "存储消息失败");
        }
        // 返回消息
        return chatAnswer;
    }
}




