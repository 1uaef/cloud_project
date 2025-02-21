package com.atg.springbootinit.model.enums;


import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
author: atg
time: 2025/2/21 9:00
*/
@Getter
public enum SpaceRoleEnum {
    VIEWER("viewer", "查看者"),
    EDITOR("editor", "编辑者"),
    ADMIN("admin", "管理员");
    private final String value;
    private final String desc;

    SpaceRoleEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据枚举值获取枚举
     * @param value
     * @return
     */
    public static SpaceRoleEnum getEnumByValue(String value) {
        for (SpaceRoleEnum spaceRoleEnum : SpaceRoleEnum.values()) {
            if (spaceRoleEnum.getValue().equals(value)) {
                return spaceRoleEnum;
            }
        }
        return null;
    }

    /**
     * 获取所有的枚举值
     */
    public static String[] getEnumValues() {
        SpaceRoleEnum[] spaceRoleEnums = SpaceRoleEnum.values();
        String[] values = new String[spaceRoleEnums.length];
        for (int i = 0; i < spaceRoleEnums.length; i++) {
            values[i] = spaceRoleEnums[i].getValue();

        }
        return values;
    }

    /**
     * 获取所有的文本列表
     * @param
     */
    public static List<String> getAllTexts(){
        return Arrays.stream(SpaceRoleEnum.values())
                .map(SpaceRoleEnum::getDesc)
                .collect(Collectors.toList());
    }


}
