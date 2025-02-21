package com.atg.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.dto.space_user.SpaceUserAddRequest;
import com.atg.springbootinit.model.dto.space_user.SpaceUserQueryRequest;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.enums.SpaceRoleEnum;
import com.atg.springbootinit.model.vo.SpaceUserVO;
import com.atg.springbootinit.model.vo.SpaceVO;
import com.atg.springbootinit.service.SpaceService;
import com.atg.springbootinit.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atg.springbootinit.model.entity.SpaceUser;
import com.atg.springbootinit.service.SpaceUserService;
import com.atg.springbootinit.mapper.SpaceUserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 啊汤哥
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2025-02-21 08:48:40
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;


    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 参数校验
        if (spaceUserAddRequest == null) {
            throw new RuntimeException("参数错误");
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        validSpaceUser(spaceUser, true); // 校验
        boolean save = this.save(spaceUser);
        if (!save) {
            throw new RuntimeException("添加失败");
        }
        return spaceUser.getId();

    }

    /**
     * 校验: 校验用户， 校验空间 ， 校验创建空间的角色
     *
     * @param spaceUser
     * @param add
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        Long userId = spaceUser.getUserId();
        Long spaceId = spaceUser.getSpaceId();
        // 添加的时候校验
        if (add) {
            ThrowUtils.throwIf(userId == null || spaceId == null, ErrorCode.PARAMS_ERROR);
            User userById = userService.getById(userId);
            ThrowUtils.throwIf(userById == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            // 校验空间是否存在
            Space spaceById = spaceService.getById(spaceId);
            ThrowUtils.throwIf(spaceById == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 修改时校验
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        ThrowUtils.throwIf(spaceRoleEnum == null, ErrorCode.PARAMS_ERROR, "角色错误");

    }

    // 获取封装后的VO 单个 响应给前端的
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        if (spaceUser == null) {
            return null;
        }
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        Long userId = spaceUser.getUserId();
        Long spaceId = spaceUser.getSpaceId();
        if (userId != null) {
            User user = userService.getById(userId);
            spaceUserVO.setUser(userService.getUserVO(user));
        }
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            spaceUserVO.setSpace(spaceService.getSpaceVO(space, request));
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        if (CollUtil.isEmpty(spaceUserList)){
            return Collections.emptyList();
        }
        // 封装转对象
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVo).collect(Collectors.toList());
        // 批量查询用户信息和空间信息，封装到VO中
        // 查询怎么查--根据ID查询
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        // 根据ID查询用户信息 和 空间信息 根据分组查询
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream().collect(Collectors.groupingBy(Space::getId));

        // 填充 封装到VO中
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            // 填充用户信息
            if (userIdUserListMap.containsKey(userId)) {
                User user = userIdUserListMap.get(userId).get(0);
                spaceUserVO.setUser(userService.getUserVO(user));
            }
            // 填充空间信息
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                Space space = spaceIdSpaceListMap.get(spaceId).get(0);
                spaceUserVO.setSpace(SpaceVO.objToVo(space));
            }
        }
        );
        return spaceUserVOList;

    }

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {

        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }
}




