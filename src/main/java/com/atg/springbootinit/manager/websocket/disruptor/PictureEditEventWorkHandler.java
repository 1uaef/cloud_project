package com.atg.springbootinit.manager.websocket.disruptor;
import cn.hutool.json.JSONUtil;
import com.atg.springbootinit.manager.websocket.PictureEditHandler;
import com.atg.springbootinit.manager.websocket.model.PictureEditMessageTypeEnum;
import com.atg.springbootinit.manager.websocket.model.PictureEditResponseMessage;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.manager.websocket.model.PictureEditRequestMessage;
import com.atg.springbootinit.service.UserService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;


import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/*
author: atg
time: 2025/2/26 15:11
*/
@Slf4j
@Component
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {
    @Resource
    private PictureEditHandler pictureEditHandler;
    @Resource
    private UserService userService;

    @Override
    public void onEvent(PictureEditEvent pictureEditEvent) throws Exception {
        PictureEditRequestMessage pictureEditRequestMessage = pictureEditEvent.getPictureEditRequestMessage();
        WebSocketSession session = pictureEditEvent.getSession();
        User user = pictureEditEvent.getUser();
        Long pictureId = pictureEditEvent.getPictureId();

        // 获取消息类型
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum messageTypeEnum = PictureEditMessageTypeEnum.getEnumByValue(type);


        // 根据消息类型处理消息
        switch (messageTypeEnum) {
            case ENTER_EDIT: // 用户进入编辑模式
                pictureEditHandler.handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EDIT_ACTION:
                pictureEditHandler.handleEditActionMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EXIT_EDIT:
                pictureEditHandler.handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            default:
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
                pictureEditResponseMessage.setMessage("未知消息类型");
                pictureEditResponseMessage.setUser(userService.getUserVO(user));
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));

        }
    }
}