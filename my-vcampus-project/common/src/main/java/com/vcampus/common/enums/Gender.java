package com.vcampus.common.enums;

/**
 * 性别枚举
 * 
 * @author VCampus Team
 * @version 1.0
 */
public enum Gender implements LabelledEnum {
    
    MALE("男"),
    FEMALE("女"),
    UNSPECIFIED("未指定");
    
    private final String label;
    
    Gender(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
    
    /**
     * 从标签获取枚举值
     */
    public static Gender fromLabel(String label) {
        for (Gender gender : values()) {
            if (gender.getLabel().equals(label)) {
                return gender;
            }
        }
        return UNSPECIFIED;
    }
}
