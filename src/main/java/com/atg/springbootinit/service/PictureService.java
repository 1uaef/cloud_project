package com.atg.springbootinit.service;

import com.atg.springbootinit.model.dto.picture.PictureQueryRequest;
import com.atg.springbootinit.model.dto.picture.PictureReviewRequest;
import com.atg.springbootinit.model.dto.picture.PictureUploadRequest;
import com.atg.springbootinit.model.entity.Picture;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.PictureVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author 啊汤哥
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-02-05 19:23:45
*/
public interface PictureService extends IService<Picture> {
    // 校验图片
    void validPicture(Picture picture);

    /**
     * 上传图片
     */
    PictureVO uploadPicture(MultipartFile multipartFile, User LoginUser, PictureUploadRequest pictureUploadRequest);

    /**
     * 获取查询对象--专门查询请求
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    // 获取单个图片信息--封装
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    // 分页获取图片信息--封装
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    // 图片审核
    void reviewPicture(PictureReviewRequest pictureReviewRequest, User LoginUser);

    // 填充审核参数
    void fillReviewPicture(Picture picture, User LoginUser);
}
