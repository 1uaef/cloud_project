package com.atg.springbootinit.service;

import com.atg.springbootinit.model.dto.picture.PictureUploadRequest;
import com.atg.springbootinit.model.entity.Picture;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.PictureVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
* @author 啊汤哥
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-02-05 19:23:45
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     */
    PictureVO uploadPicture(MultipartFile multipartFile, User LoginUser, PictureUploadRequest pictureUploadRequest);

}
