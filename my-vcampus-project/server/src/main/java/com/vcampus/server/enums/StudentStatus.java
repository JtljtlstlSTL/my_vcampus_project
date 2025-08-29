package com.vcampus.server.enums;

/**
 * 学生状态枚举
 * 
 * @author VCampus Team
 * @version 1.0
 */
public enum StudentStatus implements LabelledEnum {
    
    IN_SCHOOL("在籍"),
    GRADUATED("毕业"),
    DROPOUT("退学"),
    SUSPENDED("休学"),
    EXPELLED("开除");
    
    private final String label;
    
    StudentStatus(String label) {
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
     * @return 学生状态枚举
     */
    public static StudentStatus fromLabel(String label) {
        for (StudentStatus status : values()) {
            if (status.getLabel().equals(label)) {
                return status;
            }
        }
        return IN_SCHOOL; // 默认为在籍
    }
}
