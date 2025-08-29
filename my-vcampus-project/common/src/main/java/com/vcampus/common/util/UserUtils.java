package com.vcampus.common.util;

import com.vcampus.common.entity.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户工具类 - 处理用户相关的业务逻辑
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class UserUtils {
    
    // =============== 角色管理 ===============
    
    /**
     * 检查用户是否有指定角色
     */
    public static boolean hasRole(User user, String role) {
        if (user == null || role == null) {
            return false;
        }
        
        String[] roles = user.getRoles();
        for (String r : roles) {
            if (r.equals(role) || r.equals("admin")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查用户是否有权限
     */
    public static boolean hasPermission(User user, String permission) {
        if ("anonymous".equals(permission)) {
            return true;
        }
        return hasRole(user, permission);
    }
    
    /**
     * 检查是否为管理员
     */
    public static boolean isAdmin(User user) {
        return user != null && hasRole(user, "admin");
    }
    
    /**
     * 添加角色
     */
    public static void addRole(User user, String role) {
        if (user == null || role == null || role.trim().isEmpty()) {
            return;
        }
        
        Set<String> currentRoles = new HashSet<>(Arrays.asList(user.getRoles()));
        currentRoles.add(role.trim());
        user.setRoles(currentRoles.toArray(new String[0]));
    }
    
    /**
     * 移除角色
     */
    public static void removeRole(User user, String role) {
        if (user == null || role == null) {
            return;
        }
        
        Set<String> currentRoles = new HashSet<>(Arrays.asList(user.getRoles()));
        currentRoles.remove(role);
        user.setRoles(currentRoles.toArray(new String[0]));
    }
    

    
    // =============== 数据脱敏 ===============
    
    /**
     * 获取脱敏后的用户信息
     */
    public static User sanitized(User user) {
        if (user == null) return null;
        
        User sanitized = new User();
        sanitized.setCardNum(user.getCardNum());
        sanitized.setName(user.getName());
        sanitized.setUserType(user.getUserType());
        sanitized.setGender(user.getGender());
        sanitized.setPhone(maskPhone(user.getPhone()));
        // password = null (不复制)
        return sanitized;
    }
    
    /**
     * 手机号脱敏
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    /**
     * 邮箱脱敏
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        int atIndex = email.indexOf("@");
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (username.length() <= 3) {
            return username + domain;
        }
        return username.substring(0, 2) + "***" + username.substring(username.length() - 1) + domain;
    }
    
    // =============== 信息展示 ===============
    
    /**
     * 获取用户摘要信息
     */
    public static String getSummary(User user) {
        if (user == null) return "未知用户";
        
        return String.format("%s[%d] - %s (%s)", 
                user.getUserType() != null ? user.getUserType() : "未知", 
                user.getCardNum(), 
                user.getName(), 
                user.getUserType() != null ? user.getUserType() : "未知");
    }
}
