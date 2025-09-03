package com.vcampus.server.core.library.enums;

/**
 * 借阅状态枚举 - 对应数据库 tblBook_trans.Status 字段
 * @author VCampus Team
 * @version 1.0
 */
public enum BorrowStatus {
    BORROWED("借阅中"), // 借阅中 - 图书已被借出，尚未归还
    RETURNED("已归还"), // 已归还 - 图书已归还
    OVERDUE("已逾期"); // 已逾期 - 图书借阅已超过归还期限
    
    private final String description;
    
    BorrowStatus(String description) { // 构造函数
        this.description = description;
    }
    
    public String getDescription() { // 获取状态描述
        return description;
    }
    
    public static BorrowStatus fromString(String value) { // 根据字符串值获取枚举
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        for (BorrowStatus status : BorrowStatus.values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return null;
    }
    
    public static BorrowStatus fromDescription(String description) { // 根据描述获取枚举
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        
        for (BorrowStatus status : BorrowStatus.values()) {
            if (status.description.equals(description.trim())) {
                return status;
            }
        }
        return null;
    }
    
    public boolean isActive() { // 判断是否为活跃状态（借阅中或逾期）
        return this == BORROWED || this == OVERDUE;
    }
    
    public boolean isCompleted() { // 判断是否为已完成状态（已归还）
        return this == RETURNED;
    }
    
    @Override
    public String toString() {
        return name() + "(" + description + ")";
    }
}
