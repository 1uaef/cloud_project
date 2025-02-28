package com.atg.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.constant.CommonConstant;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.mapper.QuestionBankMapper;
import com.atg.springbootinit.model.dto.question_bank.QuestionBankList;
import com.atg.springbootinit.model.dto.question_bank.QuestionBankQueryRequest;
import com.atg.springbootinit.model.entity.QuestionBank;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.vo.QuestionBankVO;
import com.atg.springbootinit.model.vo.UserVO;
import com.atg.springbootinit.service.QuestionBankService;
import com.atg.springbootinit.service.UserService;
import com.atg.springbootinit.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题库服务实现
 */
@Service
@Slf4j
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param questionBank
     * @param add          对创建的数据进行校验
     */
    @Override
    public void validQuestionBank(QuestionBank questionBank, boolean add) {
        ThrowUtils.throwIf(questionBank == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = questionBank.getTitle();
        String description = questionBank.getDescription();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }

        if (StringUtils.isNotBlank(description)) {
            ThrowUtils.throwIf(description.length() > 300, ErrorCode.PARAMS_ERROR, "描述过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionBankQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest questionBankQueryRequest) {
        QueryWrapper<QuestionBank> queryWrapper = new QueryWrapper<>();
        if (questionBankQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionBankQueryRequest.getId();
        Long notId = questionBankQueryRequest.getNotId();
        String searchText = questionBankQueryRequest.getSearchText();
        String title = questionBankQueryRequest.getTitle();
        String description = questionBankQueryRequest.getDescription();
        String picture = questionBankQueryRequest.getPicture();
        String sortField = questionBankQueryRequest.getSortField();
        String sortOrder = questionBankQueryRequest.getSortOrder();
        Long userId = questionBankQueryRequest.getUserId();

        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("description", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(description), "content", description);

        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(picture), "picture", picture);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库封装
     *
     * @param questionBank
     * @param request
     * @return
     */
    @Override
    public QuestionBankVO getQuestionBankVO(QuestionBank questionBank, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankVO questionBankVO = QuestionBankVO.objToVo(questionBank);

        // region 可选
        // 1. 关联查询用户信息 -- 哪个用户发布的题库
        Long userId = questionBank.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankVO.setUser(userVO);

        return questionBankVO;
    }

    /**
     * 分页获取题库封装
     *
     * @param questionBankPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankVO> getQuestionBankVOPage(Page<QuestionBank> questionBankPage, HttpServletRequest request) {
        List<QuestionBank> questionBankList = questionBankPage.getRecords();

        Page<QuestionBankVO> questionBankVo = new Page<>(questionBankPage.getCurrent(), questionBankPage.getSize(), questionBankPage.getTotal());
        if (CollUtil.isEmpty(questionBankList)) {
            return questionBankVo;
        }

        // 对象转封装类
        List<QuestionBankVO> questionBankVOList = questionBankList.stream().map(questionBank -> {
            return QuestionBankVO.objToVo(questionBank);
        }).collect(Collectors.toList());

        // 关联查询用户信息 --- 1. 查询的是用户ID 集合 2. 根据用户ID集合查询用户信息 3. 封装用户信息
        Set<Long> userIdSet = questionBankList.stream().map(QuestionBank::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        questionBankVOList.forEach(questionBankVO -> {
            Long userId = questionBankVO.getUserId(); // 获取用户ID
            User user = null;
            if (userIdListMap.containsKey(userId)) {
                user = userIdListMap.get(userId).get(0); // 获取用户信息
            }
            questionBankVO.setUser(userService.getUserVO(user)); // 封装用户信息
        });
        questionBankVo.setRecords(questionBankVOList);
        return questionBankVo;


    }

    @Override
    public QuestionBankList getQuestionBankList() {
        // 获取所有题库实体
        List<QuestionBank> questionBanks = this.list();

        // 提取每个题库实体的 title 属性并封装成 QuestionBankList 对象
        List<String> questionBankCollect = questionBanks.stream().map(QuestionBank::getTitle).collect(Collectors.toList());


        QuestionBankList questionBankList = new QuestionBankList();
        questionBankList.setQuestionBankList(questionBankCollect);

        return questionBankList;
    }
}
