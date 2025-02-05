package com.atg.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atg.springbootinit.model.entity.Picture;
import com.atg.springbootinit.service.PictureService;
import com.atg.springbootinit.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
* @author 啊汤哥
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-02-05 19:23:45
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

}




