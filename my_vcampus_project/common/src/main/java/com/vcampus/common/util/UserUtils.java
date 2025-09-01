package com.vcampus.common.util;

import com.vcampus.common.entity.User;
import java.util.*;

/**
 * 用户工具类 - 提供用户相关的工具方法
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class UserUtils {
    
    /**
     * 检查用户是否有指定角色
     */
    public static boolean hasRole(User user, String role) {
        if (user == null || role == null) {
            return false;
        }
        
        List<String> roles = user.getRoles();
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
        
        List<String> currentRoles = new ArrayList<>(user.getRoles());
        currentRoles.add(role.trim());
        user.setRoles(currentRoles);
    }
    
    /**
     * 移除角色
     */
    public static void removeRole(User user, String role) {
        if (user == null || role == null) {
            return;
        }
        
        List<String> currentRoles = new ArrayList<>(user.getRoles());
        currentRoles.remove(role);
        user.setRoles(currentRoles);
    }
    
    /**
     * 检查用户是否有多个角色中的任意一个
     */
    public static boolean hasAnyRole(User user, String... roles) {
        if (user == null || roles == null || roles.length == 0) {
            return false;
        }
        
        List<String> userRoles = user.getRoles();
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查用户是否同时拥有所有指定角色
     */
    public static boolean hasAllRoles(User user, String... roles) {
        if (user == null || roles == null || roles.length == 0) {
            return false;
        }
        
        List<String> userRoles = user.getRoles();
        for (String role : roles) {
            if (!userRoles.contains(role)) {
                return false;
            }
        }
        return true;
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
        
        if (username.length() <= 2) {
            return email;
        }
        
        return username.substring(0, 2) + "***" + domain;
    }
    
    /**
     * 姓名脱敏
     */
    public static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        
        return name.charAt(0) + "*" + name.charAt(name.length() - 1);
    }
    
    /**
     * 身份证号脱敏
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        
        return idCard.substring(0, 4) + "********" + idCard.substring(idCard.length() - 4);
    }
}
