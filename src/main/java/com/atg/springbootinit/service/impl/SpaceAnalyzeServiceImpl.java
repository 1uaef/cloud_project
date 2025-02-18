package com.atg.springbootinit.service.impl;


import cn.hutool.core.util.NumberUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.mapper.PictureMapper;
import com.atg.springbootinit.mapper.SpaceMapper;
import com.atg.springbootinit.model.dto.space.analysis.SpaceAnalyzeRequest;
import com.atg.springbootinit.model.dto.space.analysis.SpaceUsageAnalyzeRequest;
import com.atg.springbootinit.model.dto.space.analysis.SpaceUsageAnalyzeResponse;
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
import java.util.List;

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


    @Override
    public SpaceUsageAnalyzeResponse analyzeSpaceUsage(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User LoginUser) {
        // 1. 校验参数
        Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
        boolean isQueryPublic = spaceUsageAnalyzeRequest.isQueryPublic();
        boolean isQueryAll = spaceUsageAnalyzeRequest.isQueryAll();

        if (isQueryAll || isQueryPublic){

            // 权限校验 ----- 空间权限校验
            checkSpaceAuthority(spaceUsageAnalyzeRequest, LoginUser);

            // 统计
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
            List<Object> pictureObjects = pictureService.getBaseMapper().selectObjs(queryWrapper);

            long useSize = pictureObjects.stream().mapToLong(obj -> (Long) obj).sum();
            long size = pictureObjects.size();

            // 统计空间使用情况 - 返回
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();

            spaceUsageAnalyzeResponse.setUsedSize(useSize);
            spaceUsageAnalyzeResponse.setUsedCount(size);
            // 公共图库 啥都没有限制
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);

            return spaceUsageAnalyzeResponse;
        }

        else{
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!LoginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
            // 构造空间 返回结果
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(space.getTotalSize());
            spaceUsageAnalyzeResponse.setUsedCount(space.getTotalCount());
            spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
            spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());

            // 计算比例
            // 1. 计算空间使用 比例
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            // 2. 计算图片使用 比例
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeUsageRatio);
            spaceUsageAnalyzeResponse.setCountUsageRatio(countUsageRatio);
            return spaceUsageAnalyzeResponse;

        }


    }






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
