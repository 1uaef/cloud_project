package com.atg.springbootinit.model.enums;


/*
author: atg
time: 2025/2/9 9:11
*/

import lombok.Getter;

@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final Integer value;

    PictureReviewStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }
    // 根据value获取值
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (PictureReviewStatusEnum statusEnum : values()) {
            if (statusEnum.value.equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
