package com.atg.springbootinit.service;


import com.atg.springbootinit.model.dto.space.analysis.req.*;
import com.atg.springbootinit.model.dto.space.analysis.resp.*;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 啊汤哥
* @description 针对表【space(空间)】的数据库-空间分析-操作Service
*/
public interface SpaceAnalyzeService extends IService<Space> {

    // 分析空间使用情况---包含全部 和 公开 还有 私有
    SpaceUsageAnalyzeResponse analyzeSpaceUsage(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User LoginUser);

    // 按照分类分组查询图片表的数据
    List<SpaceCategoryAnalyzeResponse> analyzeSpaceCategory(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User LoginUser);

    // 按照标签查询使用情况
    List<SpaceTagAnalyzeResponse> analyzeSpaceTag(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User LoginUser);

    // 分段统计空间大小
    List<SpaceSizeAnalyzeResponse> analyzeSpaceSize(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User LoginUser);

    // 用户行为分析 -- 在哪一个时间段 上传的数量
    List<SpaceUserAnalyzeResponse> analyzeSpaceUser(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User LoginUser);


    // 获取空间使用量的排行榜
    List<Space> analyzeSpaceRank(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User LoginUser);

}
