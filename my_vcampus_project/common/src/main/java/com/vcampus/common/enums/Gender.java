package com.vcampus.common.enums;

/**
 * 性别枚举
 * 
 * @author VCampus Team
 * @version 1.0
 */
public enum Gender implements LabelledEnum {
    
    MALE("男"),
    FEMALE("女");
    
    /**
     * 从数据库值获取枚举
     */
    public static Gender fromDatabase(String dbValue) {
        if (dbValue == null) return MALE;
        
        switch (dbValue) {
            case "男": return MALE;
            case "女": return FEMALE;
            default: return MALE;
        }
    }
    
    /**
     * 转换为数据库值
     */
    public String toDatabase() {
        return this.label;
    }
    
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
        return MALE;
    }
}
