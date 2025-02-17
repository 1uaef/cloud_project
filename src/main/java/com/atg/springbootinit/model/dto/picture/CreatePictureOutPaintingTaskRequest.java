package com.atg.springbootinit.model.dto.picture;


import com.atg.springbootinit.api.aliyuAi.model.CreateOutPaintingTaskRequest;

import java.io.Serializable;

/*
author: atg
time: 2025/2/17 15:34
*/
@lombok.Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;
}
