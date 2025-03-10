package com.atg.springbootinit.manager.websocket.disruptor;


import com.atg.springbootinit.manager.websocket.model.PictureEditRequestMessage;
import com.atg.springbootinit.model.entity.User;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/*
author: atg
time: 2025/2/26 14:54
*/

// 定义事件生产者 -- 发布事件
@Data
@Slf4j
@Component
public class PictureEditEventProducer {

    @Resource
    private Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    public void publishEvent(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId){
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        // 获取到可以防止事件的位置
        long next = ringBuffer.next();
        PictureEditEvent pictureEditEvent = ringBuffer.get(next);
        pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
        pictureEditEvent.setSession(session);
        pictureEditEvent.setUser(user);
        pictureEditEvent.setPictureId(pictureId);
        // 发布事件
        ringBuffer.publish(next);
    }

    //  优雅关闭
    @PreDestroy
    public void shutdown(){
        pictureEditEventDisruptor.shutdown();
    }
}
