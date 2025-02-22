package com.atg.springbootinit.manager.auth;


import cn.dev33.satoken.stp.StpLogic;
import org.springframework.stereotype.Component;

/*
author: atg
time: 2025/2/21 14:22
*/

/**
 * StpLogic 门面类，管理项目中所有的 StpLogic 账号体系
 */

@Component
public class StpKit {
    public static final String SPACE_TYPE = "space";

    /**
     *  space对象 管理space表中的所有账号的登录 权限认证
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);
}
