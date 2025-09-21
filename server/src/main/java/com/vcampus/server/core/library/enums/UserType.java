package com.vcampus.server.core.library.enums;

/**
 * 用户类型枚举 - 对应数据库 tblUser.userType 字段
 * @author VCampus Team
 * @version 1.0
 */
public enum UserType {
    STUDENT("student", "学生"), // 学生 - 在校学生用户
    STAFF("staff", "教职工"), // 教职工 - 教师和工作人员
    MANAGER("manager", "管理员"), // 管理员 - 系统管理员
    // 兼容性常量：如果存在错误映射导致将商品状态映射为 UserType，则避免因缺少枚举常量而抛出异常
    ON_SHELF("ON_SHELF", "在售");

    private final String code;
    private final String description;

    UserType(String code, String description) { // 构造函数
        this.code = code;
        this.description = description;
    }

    public String getCode() { // 获取类型代码
        return code;
    }

    public String getDescription() { // 获取类型描述
        return description;
    }

    public static UserType fromCode(String code) { // 根据代码获取枚举
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        for (UserType type : UserType.values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }

    public static UserType fromDescription(String description) { // 根据描述获取枚举
        if (description == null || description.trim().isEmpty()) {
            return null;
        }

        for (UserType type : UserType.values()) {
            if (type.description.equals(description.trim())) {
                return type;
            }
        }
        return null;
    }

    public boolean isManager() { // 判断是否为管理员
        return this == MANAGER;
    }

    public boolean isRegularUser() { // 判断是否为普通用户（学生或教职工）
        return this == STUDENT || this == STAFF;
    }

    @Override
    public String toString() {
        return code + "(" + description + ")";
    }
}
