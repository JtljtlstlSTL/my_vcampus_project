package com.vcampus.server.core.library.enums;

/**
 * 图书状态枚举
 * 
 * @author VCampus Team
 * @version 1.0
 */
public enum BookStatus {
    
    /**
     * 在库
     */
    IN_LIBRARY("IN_LIBRARY", "在库"),
    
    /**
     * 可借阅
     */
    AVAILABLE("AVAILABLE", "可借阅"),
    
    /**
     * 已借出
     */
    BORROWED("BORROWED", "已借出");
    
    private final String code;
    private final String description;
    
    BookStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 状态代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果代码无效
     */
    public static BookStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("图书状态代码不能为空");
        }
        
        for (BookStatus status : values()) {
            if (status.code.equals(code.trim().toUpperCase())) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("无效的图书状态代码: " + code);
    }
    
    /**
     * 检查状态是否可借阅
     * 
     * @return 是否可借阅
     */
    public boolean isAvailable() {
        return this == IN_LIBRARY || this == AVAILABLE;
    }
    
    /**
     * 检查状态是否可借出
     * 
     * @return 是否可借出
     */
    public boolean isBorrowable() {
        return this == IN_LIBRARY || this == AVAILABLE;
    }
    
    /**
     * 检查状态是否已借出
     * 
     * @return 是否已借出
     */
    public boolean isBorrowed() {
        return this == BORROWED;
    }
    
    @Override
    public String toString() {
        return code;
    }
}