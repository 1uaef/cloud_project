package com.atg.springbootinit.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.atg.springbootinit.manager.websocket.model.PictureEditActionEnum;
import com.atg.springbootinit.manager.websocket.model.PictureEditMessageTypeEnum;
import com.atg.springbootinit.manager.websocket.model.PictureEditRequestMessage;
import com.atg.springbootinit.model.vo.UserVO;


import com.atg.springbootinit.manager.websocket.model.PictureEditResponseMessage;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();


    /**
     * 连接建立成功后，会调用这个方法 --- 发送建立成功的状态给客户端
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // 保存会话
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId"); // 获取图片id
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

    //    收到前端发送的消息，根据消息类别处理消息  --- 比如 ： xx用户进入编辑模式，xx用户退出编辑模式，xx用户执行了xx操作
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum messageTypeEnum = PictureEditMessageTypeEnum.getEnumByValue(type); // 获取消息类型

        // 从session中获取用户信息
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user"); // 获取用户信息
        Long pictureId = (Long) attributes.get("pictureId"); // 获取图片id

        // 根据消息类型处理消息
        switch (messageTypeEnum) {
            case ENTER_EDIT: // 用户进入编辑模式
                handleEnterEditMessage(pictureEditRequestMessage, session, pictureId, user);
                break;
            case EDIT_ACTION:
                handleEditActionMessage(pictureEditRequestMessage, session, pictureId, user);
                break;
            case EXIT_EDIT:
                handleExitEditMessage(pictureEditRequestMessage, session, pictureId, user);
                break;
            default:
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
                pictureEditResponseMessage.setMessage("未知消息类型");
                pictureEditResponseMessage.setUser(userService.getUserVO(user));
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
        }

    }

    private void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, Long pictureId, User user) throws IOException {
        // 确认当前用户是否是编辑用户
        Long editUserId = pictureEditUsers.get(pictureId);
        if (editUserId.equals(user.getId())) {
            pictureEditUsers.remove(pictureId); // 移除编辑用户
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            pictureEditResponseMessage.setMessage("用户 " + user.getUserName() + " 退出了编辑模式");
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage); // 广播消息
        }
    }

    // 用户进行编辑操作 --- 比如：xx用户执行了xx操作 通知团队成员  自己不通知
    private void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, Long pictureId, User user) throws IOException {
        Long editUserId = pictureEditUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction); // 获取操作类型
        if (actionEnum == null) {
            return;
        }
        // 确认当前用户是否是编辑用户
        if (editUserId.equals(user.getId())) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage(); // 构造消息
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            pictureEditResponseMessage.setMessage("用户 " + user.getUserName() + " 执行了 " + actionEnum.getText() + " 操作"); // 消息内容
            pictureEditResponseMessage.setUser(userService.getUserVO(user)); // 用户信息
            pictureEditResponseMessage.setEditAction(editAction); // 编辑动作
            broadcastToPicture(pictureId, pictureEditResponseMessage); // 广播消息
        }
    }

    private void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, Long pictureId, User user) throws IOException {
        // 没有用户编辑才能进行编辑
        if (!pictureEditUsers.containsKey(pictureId)) {
            // 将用户添加到编辑用户列表中
            pictureEditUsers.put(pictureId, user.getId());
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
            pictureEditResponseMessage.setMessage("用户 " + user.getUserName() + " 进入编辑模式");
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }

    }

    // 断开连接
    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        // 移除信息
        Map<String, Object> attributes = session.getAttributes();
        Long pictureId = (Long) attributes.get("pictureId");
        User user = (User) attributes.get("user");
        if (pictureId != null && user != null) {
            // 确认当前用户是否是编辑用户
            Long editUserId = pictureEditUsers.get(pictureId);
            if (editUserId != null && editUserId.equals(user.getId())) {
                pictureEditUsers.remove(pictureId); // 移除编辑用户
            }
        }
        // 删除会话
//        pictureSessions.forEach((key, value) -> {
//            value.remove(session);
//        });
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if (webSocketSessions != null) {
            webSocketSessions.remove(session);
            if (webSocketSessions.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }
        // 构造响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue()); // 消息类型
        assert user != null;
        pictureEditResponseMessage.setMessage("用户 " + user.getUserName() + "离开了编辑模式"); // 消息内容
        pictureEditResponseMessage.setUser(userService.getUserVO(user)); // 用户信息
        broadcastToPicture(pictureId, pictureEditResponseMessage); // 广播消息
    }
}
