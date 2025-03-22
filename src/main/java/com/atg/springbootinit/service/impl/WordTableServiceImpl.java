package com.atg.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atg.springbootinit.service.WordTableService;
import com.atg.springbootinit.mapper.WordTableMapper;
import org.springframework.stereotype.Service;
import com.atg.springbootinit.model.entity.WordTable;

/**
 * @author 啊汤哥
 * @description 针对表【word_table(单词表)】的数据库操作Service实现
 * @createDate 2025-03-22 11:06:59
 */
@Service
public class WordTableServiceImpl extends ServiceImpl<WordTableMapper, WordTable> implements WordTableService {

}




