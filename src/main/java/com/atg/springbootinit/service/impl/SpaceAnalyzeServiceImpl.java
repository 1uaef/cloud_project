package com.atg.springbootinit.service.impl;


import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.mapper.PictureMapper;
import com.atg.springbootinit.mapper.SpaceMapper;
import com.atg.springbootinit.model.dto.space.analysis.SpaceAnalyzeRequest;
import com.atg.springbootinit.model.entity.Picture;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.service.PictureService;
import com.atg.springbootinit.service.SpaceAnalyzeService;
import com.atg.springbootinit.service.SpaceService;
import com.atg.springbootinit.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/*
author: atg
time: 2025/2/18 16:31
*/
@Slf4j
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;


    /**
     * 空间权限校验
     */
    private void checkSpaceAuthority(SpaceAnalyzeRequest spaceAnalyzeRequest, User LoginUser) {
        // 查询全部空间
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        // 查询公开空间
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        // 只有管理员可以用
        if (queryPublic || queryAll) {
            boolean admin = userService.isAdmin(LoginUser);
            if (!admin) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        // 分析特定空间 --- 仅本人或管理员可修改
        else {
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
            Space byId = spaceService.getById(spaceId);
            ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
            if (!byId.getUserId().equals(LoginUser.getId()) && !userService.isAdmin(LoginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

    }

    /**
     * 根据请求对象 --  封装查询条件-- 补充
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        // 全空间分析
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        if (queryAll){
            queryWrapper.lambda().eq(Picture::getIsDelete,0);
//            return;
        }
        // 分析公共图片
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if (queryPublic){
            queryWrapper.isNull("spaceId");
        }

        // 根据ID分析某个空间
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null){
            queryWrapper.eq("spaceId",spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有指定特定的空间");
    }
}
