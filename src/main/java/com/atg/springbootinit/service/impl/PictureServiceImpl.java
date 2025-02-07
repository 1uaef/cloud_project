package com.atg.springbootinit.service.impl;

import java.util.List;

import java.util.Date;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.constant.CommonConstant;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.manager.FileManager;
import com.atg.springbootinit.model.dto.file.UploadPictureRequest;
import com.atg.springbootinit.model.dto.picture.PictureQueryRequest;
import com.atg.springbootinit.model.dto.picture.PictureUploadRequest;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.PictureVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
     *
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
        if (pictureId != null && pictureId > 0) {
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
        if (pictureId != null && pictureId > 0) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        // 6. 操作数据库，保存或更新图片信息
        boolean isSuccess = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!isSuccess, ErrorCode.OPERATION_ERROR, "图片上传失败");

        // 7. 返回图片信息
        return PictureVO.objToVo(picture);

    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }

        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 收索
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("introduction", searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);

        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);

        // 数组查询 --tags
        if (CollectionUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }


}




