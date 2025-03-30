package com.atg.springbootinit.service;


import com.atg.springbootinit.model.dto.words.WordTableAddRequest;
import com.atg.springbootinit.model.dto.words.WordTableBatchAddRequest;
import com.atg.springbootinit.model.dto.words.WordTableQueryRequest;
import com.atg.springbootinit.model.entity.WordTable;
import com.atg.springbootinit.model.vo.WordVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 啊汤哥
* @description 针对表【word_table(单词表)】的数据库操作Service
* @createDate 2025-03-22 11:06:59
*/
public interface WordTableService extends IService<WordTable> {


    /**
     * 校验单词
     * @param wordTable
     * @param b
     */
    void validWordTable(WordTable wordTable, boolean b);

    /**
     * 批量校验单词
     * @param wordTableAddRequest
     * @param b
     */
    void validWordBatchTable(WordTableAddRequest wordTableAddRequest, boolean b);

    /**
     * 分页获取单词列表
     *
     * @param wordTableQueryRequest
     * @param request
     * @return
     */

    Page<WordVO> listWordTableByPage(WordTableQueryRequest wordTableQueryRequest, HttpServletRequest request);

    /**
     * 获取单词列表
     * @param id
     * @return
     */
    List<WordVO> listWords(Long id);
}
