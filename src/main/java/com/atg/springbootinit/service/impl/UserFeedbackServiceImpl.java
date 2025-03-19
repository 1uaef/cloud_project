package com.atg.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.mapper.FeedbackMapper;
import com.atg.springbootinit.model.dto.user_feed_back.UserFeedbackQueryRequest;
import com.atg.springbootinit.model.entity.Feedback;
import com.atg.springbootinit.model.enums.SuggestionStatusEnum;
import com.atg.springbootinit.model.vo.UserFeedbackVO;
import com.atg.springbootinit.service.UserFeedbackService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.constant.CommonConstant;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.UserVO;

import com.atg.springbootinit.service.UserService;
import com.atg.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户反馈服务实现
 *

 */
@Service
@Slf4j
public class UserFeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements UserFeedbackService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param Feedback
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validFeedback(Feedback Feedback, boolean add) {
        ThrowUtils.throwIf(Feedback == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String description = Feedback.getDescription();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(description), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(description)) {
            ThrowUtils.throwIf(description.length() > 500, ErrorCode.PARAMS_ERROR, "反馈过程描述不能超过500个字符");
        }
    }

    /**
     * 获取查询条件
     *
     * @param FeedbackQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Feedback> getQueryWrapper(UserFeedbackQueryRequest FeedbackQueryRequest) {
        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        if (FeedbackQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        String description = FeedbackQueryRequest.getDescription();
        Integer status = FeedbackQueryRequest.getStatus();
        Long userId = FeedbackQueryRequest.getUserId();
        String sortField = FeedbackQueryRequest.getSortField();
        String sortOrder = FeedbackQueryRequest.getSortOrder();
        // todo 补充需要的查询条件
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 状态查询，使用枚举编码
        if (status != null) {
            String statusCode = getStatusCodeByValue(status);
            if (statusCode != null) {
                queryWrapper.eq("status", statusCode);
            }
        }
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    // 根据状态值获取枚举编码
    private String getStatusCodeByValue(int value) {
        for (SuggestionStatusEnum statusEnum : SuggestionStatusEnum.values()) {
            if (Integer.parseInt(statusEnum.getValue()) == value) {
                return statusEnum.getValue();
            }
        }
        return null;

    }

    /**
     * 获取用户反馈封装
     *
     * @param Feedback
     * @param request
     * @return
     */
    @Override
    public UserFeedbackVO getUserFeedbackVO(Feedback Feedback, HttpServletRequest request) {
        // 对象转封装类

        UserFeedbackVO FeedbackVO = UserFeedbackVO.objToVo(Feedback);
        BeanUtils.copyProperties(Feedback, FeedbackVO);
        return FeedbackVO;
    }

    /**
     * 分页获取用户反馈封装
     *
     * @param FeedbackPage
     * @param request
     * @return
     */
    @Override
    public Page<UserFeedbackVO> getUserFeedbackVOPage(Page<Feedback> FeedbackPage, HttpServletRequest request) {
        List<Feedback> FeedbackList = FeedbackPage.getRecords();
        Page<UserFeedbackVO> FeedbackVOPage = new Page<>(FeedbackPage.getCurrent(), FeedbackPage.getSize(), FeedbackPage.getTotal());
        if (CollUtil.isEmpty(FeedbackList)) {
            return FeedbackVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserFeedbackVO> FeedbackVOList = FeedbackList.stream().map(Feedback -> {
            return UserFeedbackVO.objToVo(Feedback);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // 填充信息

        FeedbackVOPage.setRecords(FeedbackVOList);
        return FeedbackVOPage;
    }

    @Override
    public void toExamine(Long id, Integer status) {
        Feedback feedback = getById(id);
        if (feedback == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "反馈不存在");
        }
        if (status == 1) {
            feedback.setStatus(Integer.parseInt(SuggestionStatusEnum.APPROVED.getValue()));
        }
        if (status == 2) {
            feedback.setStatus(Integer.parseInt(SuggestionStatusEnum.REJECTED.getValue()));
        }
        boolean updateById = this.updateById(feedback);
        if (!updateById) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败");
        }
        log.info("用户反馈审核成功，id：{}，状态：{}", id, status);
    }


}
