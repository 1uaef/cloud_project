package com.atg.springbootinit.service;


import com.atg.springbootinit.model.dto.space.SpaceAddRequest;
import com.atg.springbootinit.model.dto.space.SpaceQueryRequest;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.SpaceVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 啊汤哥
* @description 针对表【space(空间)】的数据库-空间分析-操作Service
*/
public interface SpaceAnalyzeService extends IService<Space> {


}
