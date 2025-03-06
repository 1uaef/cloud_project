package com.atg.springbootinit.config;


import com.volcengine.ark.runtime.service.ArkService;
import lombok.Data;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/*
author: atg
time: 2025/3/6 8:44
*/
@Configuration
// 读取配置文件中的AI 配置
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {

    private String apiKey;

    static String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";

    @Bean
    public ArkService arkService() {
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        ArkService service = ArkService.builder().
                dispatcher(dispatcher).
                connectionPool(connectionPool).
                baseUrl(baseUrl).apiKey(apiKey).build();
        return service;

    }
}
