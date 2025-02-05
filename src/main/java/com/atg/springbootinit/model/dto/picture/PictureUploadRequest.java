package com.atg.springbootinit.model.dto.picture;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/5 19:27
*/
@Data
public class PictureUploadRequest implements Serializable {

    private Long id;

    private static final long serialVersionUID = 1L;
}
