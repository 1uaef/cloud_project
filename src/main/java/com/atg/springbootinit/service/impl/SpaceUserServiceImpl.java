package com.atg.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atg.springbootinit.model.entity.SpaceUser;
import com.atg.springbootinit.service.SpaceUserService;
import com.atg.springbootinit.mapper.SpaceUserMapper;
import org.springframework.stereotype.Service;

/**
* @author 啊汤哥
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2025-02-21 08:48:40
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

}




