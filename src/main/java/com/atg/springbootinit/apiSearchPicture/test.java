package com.atg.springbootinit.apiSearchPicture;


import cn.hutool.core.util.URLUtil;

import java.nio.charset.StandardCharsets;

/*
author: atg
time: 2025/2/13 16:16
*/
public class test {
    public static void main(String[] args) {
        String imageUrl = "https://graph.baidu.com/s?card_key=\\u0026entrance=GENERAL\\u0026extUiData%5BisLogoShow%5D=1\\u0026f=all\\u0026isLogoShow=1\\u0026session_id=16301523077552951008\\u0026sign=126e1e97cd54acd88139901739434307\\u0026tpl_from=pc\",\"sign\":\"126e1e97cd54acd88139901739434307";
        String decode = URLUtil.decode(imageUrl, StandardCharsets.UTF_8);
        System.out.println(decode);
    }
}
