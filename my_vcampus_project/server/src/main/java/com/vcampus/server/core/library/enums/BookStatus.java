package com.vcampus.server.core.library.enums;

/**
 * 图书状态枚举 - 对应数据库 tblBook.Status 字段
 * @author VCampus Team
 * @version 1.0
 */
public enum BookStatus {
    IN_LIBRARY("在馆"), // 在馆 - 图书在图书馆内，可以借阅
    BORROWED("借出"); // 借出 - 图书已被借出
    
    private final String description;
    
    BookStatus(String description) { // 构造函数
        this.description = description;
    }
    
    public String getDescription() { // 获取状态描述
        return description;
    }
    
    public static BookStatus fromString(String value) { // 根据字符串值获取枚举
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        for (BookStatus status : BookStatus.values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return null;
    }
    
    public static BookStatus fromDescription(String description) { // 根据描述获取枚举
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        
        for (BookStatus status : BookStatus.values()) {
            if (status.description.equals(description.trim())) {
                return status;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return name() + "(" + description + ")";
    }
}
