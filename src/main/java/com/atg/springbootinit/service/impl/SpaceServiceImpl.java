package com.atg.springbootinit.service.impl;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.dto.space.SpaceAddRequest;
import com.atg.springbootinit.model.dto.space.SpaceQueryRequest;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.enums.SpaceLevelEnum;
import com.atg.springbootinit.model.vo.SpaceVO;
import com.atg.springbootinit.model.vo.SpaceVO;
import com.atg.springbootinit.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atg.springbootinit.service.SpaceService;
import com.atg.springbootinit.mapper.SpaceMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
* @author 啊汤哥
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-02-11 09:40:37
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{
    @Resource
    private UserService userService;


    @Override
    public Long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 填充参数默认值
        // 校验参数
        // 检验权限
        // 一个用户只能创建一个空间
        return 0L;
    }

    @Override
    public void validSpace(Space space,boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);
        
        if (add){
            if (StrUtil.isNotBlank(spaceName)) {
                throw new BusinessException( ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (enumByValue == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间等级错误");
            }
        }
        
        if (StrUtil.isNotBlank(spaceName)) {
            throw new BusinessException( ErrorCode.PARAMS_ERROR, "空间名称不能为空");
        }
        if (enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间等级错误");
        }
        
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User byId = userService.getById(userId);
            spaceVO.setUser(userService.getUserVO(byId));
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 1,2,3,4
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    
    }


    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(sortField), sortField, sortOrder);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortOrder), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevel = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevel == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间等级错误");
        }
        long maxSize = spaceLevel.getMaxSize();
        long maxCount = spaceLevel.getMaxCount();
        if (space.getMaxSize() == null){
            space.setMaxSize(maxSize);
        }
        if (space.getMaxCount() == null){
            space.setMaxCount(maxCount);
        }
    }



}




