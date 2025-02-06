package com.atg.springbootinit.service.impl;
import java.util.Date;

import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.manager.FileManager;
import com.atg.springbootinit.model.dto.file.UploadPictureRequest;
import com.atg.springbootinit.model.dto.picture.PictureUploadRequest;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.PictureVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atg.springbootinit.model.entity.Picture;
import com.atg.springbootinit.service.PictureService;
import com.atg.springbootinit.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author 啊汤哥
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-02-05 19:23:45
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {
    @Resource
    private FileManager fileManager;

    /**
     * 上传图片
     * @param multipartFile
     * @param LoginUser
     * @param pictureUploadRequest
     * @return
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, User LoginUser, PictureUploadRequest pictureUploadRequest) {
        // 1. 校验用户是否登录
        if (LoginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 2. 判断新增还是更新
        Long pictureId = null;
        if (pictureUploadRequest != null) {
           pictureId = pictureUploadRequest.getId();
        }


        // 3. 如果更新的话，检查是否有该图片
        if (pictureId != null && pictureId > 0){
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR);
        }
        // 4. 上传图片并获取上传结果
        String uploadPathPrefix = String.format("public/%s", LoginUser.getId());
        UploadPictureRequest uploadPictureRequest = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 5. 构造图片实体对象并设置相关属性
        Picture picture = new Picture();
        picture.setUrl(uploadPictureRequest.getUrl());
        picture.setName(uploadPictureRequest.getPicName());
        picture.setPicSize(uploadPictureRequest.getPicSize());
        picture.setPicWidth(uploadPictureRequest.getPicWidth());
        picture.setPicHeight(uploadPictureRequest.getPicHeight());
        picture.setPicScale(uploadPictureRequest.getPicScale());
        picture.setPicFormat(uploadPictureRequest.getPicFormat());
        picture.setUserId(LoginUser.getId());
        // 5.1 如果是更新，设置id
        if (pictureId != null && pictureId > 0){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        // 6. 操作数据库，保存或更新图片信息
        boolean isSuccess = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!isSuccess, ErrorCode.OPERATION_ERROR, "图片上传失败");

        // 7. 返回图片信息
        return PictureVO.objToVo(picture);



    }
}




