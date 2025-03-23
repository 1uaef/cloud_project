package com.atg.springbootinit.service.impl;

import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.dto.words.WordTableAddRequest;
import com.atg.springbootinit.model.dto.words.WordTableQueryRequest;
import com.atg.springbootinit.model.vo.WordVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atg.springbootinit.service.WordTableService;
import com.atg.springbootinit.mapper.WordTableMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.atg.springbootinit.model.entity.WordTable;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 啊汤哥
 * @description 针对表【word_table(单词表)】的数据库操作Service实现
 * @createDate 2025-03-22 11:06:59
 */
@Service
public class WordTableServiceImpl extends ServiceImpl<WordTableMapper, WordTable> implements WordTableService {

    @Resource
    private UserServiceImpl userService;


    @Override
    public void validWordTable(WordTable wordTable, boolean add) {
        ThrowUtils.throwIf(wordTable == null, ErrorCode.PARAMS_ERROR, "单词表不能为空");
        String word = wordTable.getWord();
        String definition = wordTable.getDefinition();
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(word), ErrorCode.PARAMS_ERROR);
            boolean validWord = isValidWord(word);

            ThrowUtils.throwIf(!validWord, ErrorCode.PARAMS_ERROR, "单词格式不正确");
        }
        ThrowUtils.throwIf(StringUtils.isBlank(definition), ErrorCode.PARAMS_ERROR);


    }

    @Override
    public void validWordBatchTable(WordTableAddRequest wordTableAddRequest, boolean b) {
        ThrowUtils.throwIf(wordTableAddRequest == null, ErrorCode.PARAMS_ERROR, "单词表不能为空");
        String word = wordTableAddRequest.getWord();
        String definition = wordTableAddRequest.getDefinition();
        if (b) {
            ThrowUtils.throwIf(StringUtils.isBlank(word), ErrorCode.PARAMS_ERROR);
            boolean validWord = isValidWord(word);
            ThrowUtils.throwIf(!validWord, ErrorCode.PARAMS_ERROR, "单词格式不正确");
        }
        ThrowUtils.throwIf(StringUtils.isBlank(definition), ErrorCode.PARAMS_ERROR);
    }

    @Override
    public Page<WordVO> listWordTableByPage(WordTableQueryRequest wordTableQueryRequest, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        QueryWrapper<WordTable> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("isDelete", 0);
        if (wordTableQueryRequest.getWord() != null) {
            queryWrapper.like("word", wordTableQueryRequest.getWord());
        }
        if (wordTableQueryRequest.getDefinition() != null) {
            queryWrapper.like("definition", wordTableQueryRequest.getDefinition());
        }
        Page<WordTable> page = new Page<>(wordTableQueryRequest.getCurrent(), wordTableQueryRequest.getPageSize());
        Page<WordTable> pageResult  = this.page(page, queryWrapper);
        // 将 WordTable 列表转换为 WordVO 列表
        List<WordVO> wordVOList = pageResult.getRecords().stream().map(wordTable -> {
            WordVO wordVO = new WordVO();
            BeanUtils.copyProperties(wordTable, wordVO);
            return wordVO;
        }).collect(Collectors.toList());
        // 创建一个新的 Page<WordVO> 对象，并设置分页信息和记录列表
        Page<WordVO> wordVOPage = new Page<>(pageResult.getCurrent(), pageResult.getSize());
        wordVOPage.setRecords(wordVOList);
        wordVOPage.setTotal(pageResult.getTotal());
        return wordVOPage;

    }

    /**
     * 校验给定的字符串是否为一个有效的单词。
     * 有效的单词仅包含字母（大小写均可）。
     *
     * @param word 要校验的字符串
     * @return 如果是有效的单词，返回true；否则返回false
     */
    public static boolean isValidWord(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        // 使用正则表达式检查字符串是否仅包含字母
        return word.matches("[a-zA-Z]+");
    }




}




