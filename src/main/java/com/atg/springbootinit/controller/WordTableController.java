package com.atg.springbootinit.controller;

import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.DeleteRequest;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.dto.words.WordTableAddRequest;
import com.atg.springbootinit.model.dto.words.WordTableBatchAddRequest;
import com.atg.springbootinit.model.dto.words.WordTableQueryRequest;
import com.atg.springbootinit.model.dto.words.WordTableUpdateRequest;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.entity.WordTable;
import com.atg.springbootinit.model.vo.WordVO;
import com.atg.springbootinit.service.UserService;
import com.atg.springbootinit.service.WordTableService;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/*
author: atg
time: 2025/3/22 11:14
*/


/**
 * 单词表接口
 */
@RestController
@RequestMapping("/wordTable")
@Slf4j
public class WordTableController {
    @Resource
    private WordTableService wordTableService;

    @Resource
    private UserService userService;

    /**
     * 添加单词
     *
     * @param wordTableAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addWordTable(@RequestBody WordTableAddRequest wordTableAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(wordTableAddRequest == null, ErrorCode.PARAMS_ERROR);
        WordTable wordTable = new WordTable();
        BeanUtils.copyProperties(wordTableAddRequest, wordTable);

        // 数据校验
        wordTableService.validWordTable(wordTable, true);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        wordTable.setUser_id(loginUser.getId());
        boolean save = wordTableService.save(wordTable);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        Long wordId = wordTable.getWord_id();
        return ResultUtils.success(wordId);
    }

    /**
     * 批量添加单词
     *
     * @param wordTableBatchAddRequest
     * @param request
     * @return
     */
    @PostMapping("/batchAdd")
    public BaseResponse<Boolean> batchAddWordTable(@RequestBody WordTableBatchAddRequest wordTableBatchAddRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(wordTableBatchAddRequest == null, ErrorCode.PARAMS_ERROR);

        // 校验用户是否登录
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        List<WordTableAddRequest> wordList = wordTableBatchAddRequest.getWordList();
        List<WordTable> wordEntityList = new ArrayList<>();

        for (WordTableAddRequest wordTableAddRequest : wordList) {
            // 校验每个单词请求
            wordTableService.validWordBatchTable(wordTableAddRequest, true);

            // 创建新的 WordTable 对象
            WordTable wordTable = new WordTable();
            BeanUtils.copyProperties(wordTableAddRequest, wordTable); // 将 wordTableAddRequest 的属性复制到 wordTable
            wordTable.setUser_id(loginUser.getId()); // 设置用户 ID

            wordEntityList.add(wordTable); // 添加到列表
        }

        // 批量保存
        boolean saveBatch = wordTableService.saveBatch(wordEntityList);
        ThrowUtils.throwIf(!saveBatch, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);
    }

    /**
     * 更新单词
     *
     * @param wordTableUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateWordTable(@RequestBody WordTableUpdateRequest wordTableUpdateRequest, HttpServletRequest request) {

        // 校验参数
        ThrowUtils.throwIf(wordTableUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验用户是否登录
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Long wordId = wordTableUpdateRequest.getWord_Id();
        WordTable byId = wordTableService.getById(wordId);
        ThrowUtils.throwIf(byId == null, ErrorCode.PARAMS_ERROR, "单词不存在");
        // 校验单词是否属于当前用户
        ThrowUtils.throwIf(!isEquals(byId, loginUser), ErrorCode.NO_AUTH_ERROR, "没有权限");
        WordTable wordTable = new WordTable();
//        wordTableService.validWordTable(wordTableUpdateRequest, false);
        BeanUtils.copyProperties(wordTableUpdateRequest, wordTable);
        wordTable.setWord_id(wordId);

        boolean update = wordTableService.updateById(wordTable);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> DeleteWordTable(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验用户是否登录
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Long id = deleteRequest.getId();
        boolean b = wordTableService.removeById(id);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 分页获取单词列表
     *
     * @param wordTableQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<WordVO>> listWordTableByPage(@RequestBody WordTableQueryRequest wordTableQueryRequest, HttpServletRequest request) {
        Page<WordVO> wordTablePage = wordTableService.listWordTableByPage(wordTableQueryRequest, request);
        return ResultUtils.success(wordTablePage);
    }

    // 查询所有的单词



    private boolean isEquals(WordTable byId, User loginUser) {
        return byId.getUser_id().equals(loginUser.getId());
    }




}
