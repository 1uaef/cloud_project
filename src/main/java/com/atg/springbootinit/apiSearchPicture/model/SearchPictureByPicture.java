package com.atg.springbootinit.apiSearchPicture.model;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/13 19:52
*/
@Data
public class SearchPictureByPicture implements Serializable {

    // 图片id
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
