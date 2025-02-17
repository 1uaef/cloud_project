package com.atg.springbootinit.api.aliyuAi;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.atg.springbootinit.api.aliyuAi.model.CreateOutPaintingTaskRequest;
import com.atg.springbootinit.api.aliyuAi.model.CreateOutPaintingTaskResponse;
import com.atg.springbootinit.api.aliyuAi.model.GetOutPaintingTaskResponse;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
author: atg
time: 2025/2/17 14:47
*/
@Slf4j
@Component
public class AliYunAiApi {
    @Value("${aliyun.accessKeyId}")
    private String apiKeyId;

    // 创建任务地址
    private static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    // 根据任务ID查询结果
    private static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    // 创建任务
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {

        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "参数为空");
        }
        // 发起请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization", "Bearer " + apiKeyId)
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        // 处理响应

        try(HttpResponse httpResponse = httpRequest.execute()) {
            if (httpResponse.getStatus() != 200) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建任务失败");
            }
            // 创建成功之后--进行响应
            CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            if (createOutPaintingTaskResponse.getCode()!= null ){
                throw new BusinessException(ErrorCode.OPERATION_ERROR, createOutPaintingTaskResponse.getMessage());
            }
            return createOutPaintingTaskResponse;

        }

    }
    // 根据任务ID查询结果
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {

        if (taskId == null || taskId.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "参数为空");
        }
//        curl -X GET \
//        --header "Authorization: Bearer $DASHSCOPE_API_KEY" \
//        https://dashscope.aliyuncs.com/api/v1/tasks/86ecf553-d340-4e21-xxxxxxxxx
        HttpRequest httpRequest = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header("Authorization", "Bearer " + apiKeyId);
        // 处理响应

        try(HttpResponse httpResponse = httpRequest.execute()) {
            if (httpResponse.getStatus() != 200) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "查询任务失败");
            }
            // 创建成功之后--进行响应
            GetOutPaintingTaskResponse getOutPaintingTaskResponse = JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
            if (getOutPaintingTaskResponse.getRequestId()!= null ){
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
            }
            return getOutPaintingTaskResponse;
        }
    }
}

