package com.vcampus.common.entity.base;

import com.vcampus.common.enums.Gender;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * User entity class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private String cardNum;    // User card number - unique identifier across campus (primary key)
    /**
     * 登录密码 (加密存储)
     * 格式: salt:hashedPassword
     * 明文密码会在首次登录时自动迁移为加密格式
     */
    private String password;    // User password - stored as hash
    private String name;        // User name
    private Integer age;        // Age
    private Gender gender;      // Gender
    private String phone;       // Phone number
    private String userType;    // User type/role string
    private String avatar;      // User avatar URL
    // 新增字段以匹配数据库 tblUser 的结构和 DAO/Mapper 调用
    private String birthDate;   // 出生年月（格式如 yyyy-MM 或 yyyy-MM-dd 或 CHAR(7)）
    private String ethnicity;   // 民族
    private String idCard;      // 身份证号
    private String hometown;    // 籍贯

    // 兼容旧代码的便捷构造器（保留 AllArgsConstructor 以生成完整构造器）
    public User(String cardNum, String password, String name, Integer age, Gender gender, String phone, String userType) {
        this.cardNum = cardNum;
        this.password = password;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.userType = userType;
    }

    /**
     * Get user role array
     */
    public List<String> getRoles() {
        if (userType == null || userType.trim().isEmpty()) {
            return Arrays.asList("anonymous");
        }
        return Arrays.asList(userType.split(","));
    }
    
    /**
     * Set user roles
     */
    public void setRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            this.userType = "anonymous";
        } else {
            this.userType = String.join(",", roles);
        }
    }
    
    /**
     * Get primary role (first role)
     */
    public String getPrimaryRole() {
        List<String> roles = getRoles();
        return roles.isEmpty() ? "anonymous" : roles.get(0);
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }
    
    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return hasRole("admin");
    }
    
    /**
     * Check if user is student
     */
    public boolean isStudent() {
        return hasRole("student");
    }
    
    /**
     * Check if user is teacher
     */
    public boolean isTeacher() {
        return hasRole("teacher");
    }
}