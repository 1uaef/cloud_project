package com.atg.springbootinit.manager.Ai;


import cn.hutool.core.collection.CollUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChoice;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/*
author: atg
time: 2025/3/6 8:53
*/
@Service  // 调用这个服务
public class AiManager {


    private final String DEFAULT_MODEL = "deepseek-v3-241226";

    @Resource
    private ArkService arkService;


    public String doChat(String userPrompt) {
        return doChat("", userPrompt, DEFAULT_MODEL);
    }

    /**
     * 允许传入自定义的消息列表--多轮对话，使用默认模型
     */
    public String doChat(List<ChatMessage> messages) {
        return doChat(messages, DEFAULT_MODEL);
    }


    public String doChat(String systemPrompt, String userPrompt) {
        return doChat(systemPrompt, userPrompt, DEFAULT_MODEL);
    }


    public String doChat(String systemPrompt, String userPrompt, String model) {

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();
        messages.add(systemMessage);
        messages.add(userMessage);

        return getChatCallGet(messages, model);

    }


    /**
     * 调用 AI 接口，获取响应字符串（允许传入自定义的消息列表）
     *
     * @param messages
     * @param model
     * @return
     */
    public String doChat(List<ChatMessage> messages, String model) {
        // 构造请求
        return getChatCallGet(messages, model);

    }

    private String getChatCallGet(List<ChatMessage> messages, String model) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build();
        // 调用接口发送请求
        List<ChatCompletionChoice> choices = arkService.createChatCompletion(chatCompletionRequest).getChoices();
        if (CollUtil.isNotEmpty(choices)) {
            return (String) choices.get(0).getMessage().getContent();
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 调用失败，没有返回结果");
    }

}
