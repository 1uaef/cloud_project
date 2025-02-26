package com.atg.springbootinit.manager.websocket.disruptor;
import com.atg.springbootinit.manager.websocket.model.PictureEditRequestMessage;
import com.atg.springbootinit.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/*
author: atg
time: 2025/2/26 14:49
*/

/**
 * 图片编辑事件
 */

@Data
public class PictureEditEvent {
    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片 id
     */
    private Long pictureId;
}
