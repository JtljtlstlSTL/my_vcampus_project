package com.vcampus.server.enums;

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
    
    @Override
    public String getLabel() {
        return label;
    }
    
    /**
     * 从标签获取枚举值
     * 
     * @param label 中文标签
     * @return 性别枚举
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
