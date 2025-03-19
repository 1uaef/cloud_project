package com.atg.springbootinit.model.enums;


/*
author: atg
time: 2025/3/19 13:55
*/
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum SuggestionStatusEnum {

    UNDER_REVIEW("正在审核", "0"),
    APPROVED("审核通过", "1"),
    REJECTED("审核不通过", "2");

    private final String text;
    private final String value;

    SuggestionStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return 值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值
     * @return 对应的枚举实例，如果未找到则返回 null
     */
    public static SuggestionStatusEnum getEnumByValue(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        for (SuggestionStatusEnum anEnum : SuggestionStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
