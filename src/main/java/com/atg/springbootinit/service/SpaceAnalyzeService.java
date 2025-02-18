package com.atg.springbootinit.service;


import com.atg.springbootinit.model.dto.space.SpaceAddRequest;
import com.atg.springbootinit.model.dto.space.SpaceQueryRequest;
import com.atg.springbootinit.model.dto.space.analysis.SpaceUsageAnalyzeRequest;
import com.atg.springbootinit.model.dto.space.analysis.SpaceUsageAnalyzeResponse;
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

    // 分析空间使用情况---包含全部 和 公开 还有 私有
    SpaceUsageAnalyzeResponse analyzeSpaceUsage(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User LoginUser);


}
