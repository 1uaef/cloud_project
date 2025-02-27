package com.atg.springbootinit.model.dto.user;


import lombok.Data;

import java.io.Serializable;

/*
author: atg
time: 2025/2/27 9:34
*/
@Data
public class UserEditRequest implements Serializable {

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 年级（如：大一）
     */
    private String grade;

    /**
     * 工作经验（如：3年全栈开发经验）
     */
    private String workExperience;

    /**
     * 擅长方向（如：Java/Spring Cloud）
     */
    private String expertiseDirection;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像 URL
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}
