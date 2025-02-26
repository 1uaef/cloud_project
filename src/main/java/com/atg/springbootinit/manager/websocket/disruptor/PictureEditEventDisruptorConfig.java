package com.atg.springbootinit.manager.websocket.disruptor;


import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/*
author: atg
time: 2025/2/26 15:21
*/

// 定义的事件及处理器关联到 Disruptor
@Configuration
public class PictureEditEventDisruptorConfig {
    private static final int RING_BUFFER_SIZE = 1024;

    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageEventDisruptor() {
        int bufferSize = RING_BUFFER_SIZE * 256;

        // 创建disruptor
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent ::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("picture-edit-disruptor").build());

        // 设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler); // 多线程处理

        // 启动
        disruptor.start();
        return disruptor;
    }
}
