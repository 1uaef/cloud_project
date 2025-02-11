package com.atg.springbootinit.service;


import com.atg.springbootinit.model.dto.space.SpaceQueryRequest;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.SpaceVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 啊汤哥
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-02-11 09:40:37
*/
public interface SpaceService extends IService<Space> {
    // 校验空间 --- 修改 / 更新
    void validSpace(Space space, boolean add);

    // 获取单个空间信息--封装  展示详情信息
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取查询对象--专门查询请求
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    // 分页获取空间信息--封装
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 根据空间级别填充空间对象
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

}
