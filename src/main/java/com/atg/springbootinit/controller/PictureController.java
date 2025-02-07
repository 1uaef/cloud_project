package com.atg.springbootinit.controller;


import com.atg.springbootinit.annotation.AuthCheck;
import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.constant.UserConstant;
import com.atg.springbootinit.model.dto.picture.PictureUploadRequest;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.PictureVO;
import com.atg.springbootinit.service.PictureService;
import com.atg.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/*
author: atg
time: 2025/2/6 17:37
*/
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, loginUser, pictureUploadRequest);
        return ResultUtils.success(pictureVO);
    }
}
