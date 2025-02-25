package com.atg.springbootinit.manager.websocket;
import cn.hutool.core.collection.CollUtil;
import com.atg.springbootinit.manager.websocket.model.PictureEditMessageTypeEnum;
import com.atg.springbootinit.model.vo.UserVO;


import com.atg.springbootinit.manager.websocket.model.PictureEditResponseMessage;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
author: atg
time: 2025/2/25 17:09
*/

// 处理编辑器的websocket的消息
@Component
@Slf4j
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    private UserService userService;


    // 每张图片编辑的状态，key为图片id，value为状态 userId
    private final Map<Long, Long> pictureEditUsers = new ConcurrentHashMap<>();

    // 保存的会话  key: 图片id，value: 会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions  = new ConcurrentHashMap<>();


    /**
     * 连接建立成功后，会调用这个方法 --- 发送建立成功的状态给客户端
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // 保存会话
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long)session.getAttributes().get("pictureId"); // 获取图片id
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet()); // 如果不存在，则创建一个新的集合
        pictureSessions.get(pictureId).add(session); // 添加会话到集合中
        // 发送建立成功的状态给客户端
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String format = String.format("用户%s连接成功", user.getUserName());
        responseMessage.setMessage(format);
        responseMessage.setUser(userService.getUserVO(user)); // 设置用户信息
        // 广播给所有会话
        broadcastToPicture(pictureId, responseMessage);
    }

    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage responseMessage) throws IOException {
        broadcastToPicture(pictureId, responseMessage, null);
    }

    /**
     * 广播给该图片的所有用户（支持排除掉某个 Session）
     *
     * @param pictureId
     * @param excludeSession
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws IOException {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 支持 long 基本类型
            objectMapper.registerModule(module);
            // 序列化为 JSON 字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessionSet) {
                // 排除掉的 session 不发送
                if (session.equals(excludeSession)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

//    收到前端发送的消息，根据消息类别处理消息
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }
}
