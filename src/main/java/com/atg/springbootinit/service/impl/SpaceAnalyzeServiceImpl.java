package com.atg.springbootinit.service.impl;


import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.mapper.SpaceMapper;
import com.atg.springbootinit.model.dto.space.analysis.req.*;
import com.atg.springbootinit.model.dto.space.analysis.resp.*;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        if (isQueryAll || isQueryPublic) {

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
        } else {
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

    @Override
    public List<SpaceCategoryAnalyzeResponse> analyzeSpaceCategory(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User LoginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 检查权限
        checkSpaceAuthority(spaceCategoryAnalyzeRequest, LoginUser);
        // 3. 构造查询条件

        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        // 分组查询 --- mybatisplus-- 可以先编写SQL查询结果进行测试
        queryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize").groupBy("category");
        // 4. 查询 --- 查询结果中获取值
        List<SpaceCategoryAnalyzeResponse> collect = pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result -> {
                    String category = (String) result.get("category");
                    Long count = ((Number) result.get("count")).longValue();
                    Long totalSize = ((Number) result.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
                })
                .collect(Collectors.toList());
        return collect;

    }

    @Override
    public List<SpaceTagAnalyzeResponse> analyzeSpaceTag(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User LoginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAuthority(spaceTagAnalyzeRequest, LoginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("tags");
        //使用 queryWrapper 查询所有图片的 tags 字段。
        //调用 pictureService.getBaseMapper().selectObjs(queryWrapper) 获取查询结果。
        //将查询结果过滤掉空值，并转换为字符串列表
        List<String> tagJsonCollect = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream().filter(ObjectUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        // 统计标签个数

//        将 tagJsonCollect 中的每个标签字符串解析为列表。
//        使用流式处理将所有标签展平为一个单一的流。
//        对每个标签进行分组并计数，最终生成一个 Map<String, Long>，键为标签，值为该标签出现的次数
        Map<String, Long> tagCountMap = tagJsonCollect.stream()
                .flatMap(tag -> JSONUtil.toList(tag, String.class).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        return tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> analyzeSpaceSize(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User LoginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAuthority(spaceSizeAnalyzeRequest, LoginUser);
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);

        queryWrapper.select("picSize");

        List<Long> picSizeCollect = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream().filter(ObjectUtil::isNotNull)
                .map(size -> (Long) size)
                .collect(Collectors.toList());

        // 定义分段范围 -- 有序
        Map<String, Long> sizeRangeMap = new LinkedHashMap<>();
        sizeRangeMap.put("<100KB", picSizeCollect.stream().filter(size -> size < 100 * 1024).count());
        sizeRangeMap.put("100KB~500KB", picSizeCollect.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRangeMap.put("500KB~1MB", picSizeCollect.stream().filter(size -> size >= 500 * 1024 && size < 1024 * 1024).count());
        sizeRangeMap.put(">1MB", picSizeCollect.stream().filter(size -> size > 1024 * 1024).count());

        return sizeRangeMap.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> analyzeSpaceUser(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User LoginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAuthority(spaceUserAnalyzeRequest, LoginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);

        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);

        // 获取分析的维度--时间
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                queryWrapper.select("YEAR(createTime) as period", "count(*) as count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m' ) as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间维度错误");
        }

        queryWrapper.groupBy("period").orderByAsc("period");
        // 查询结果
        List<Map<String, Object>> queryResult = pictureService.getBaseMapper().selectMaps(queryWrapper);
        // 封装返回
        List<SpaceUserAnalyzeResponse> collect = queryResult.stream()
                .map(result -> {
                    String period = result.get("period").toString();
                    Long count = ((Number) result.get("count")).longValue();
                    return new SpaceUserAnalyzeResponse(period, count);
                })
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<Space> analyzeSpaceRank(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User LoginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        ThrowUtils.throwIf(!userService.isAdmin(LoginUser), ErrorCode.NO_AUTH_ERROR);

        // 构造查询条件
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize")
                .last("limit " + spaceRankAnalyzeRequest.getTopN());

        return spaceService.list(queryWrapper);

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
        if (queryAll) {
//            queryWrapper.lambda().eq(Picture::getIsDelete, 0);
            return;
        }
        // 分析公共图片
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if (queryPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }

        // 根据ID分析某个空间
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有指定特定的空间");
    }


}
