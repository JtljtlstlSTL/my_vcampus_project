package com.vcampus.common.message;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户会话信息类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    
    /**
     * 用户ID/学号/工号
     */
    private String userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 用户角色列表
     */
    private String[] roles;
    
    /**
     * 会话创建时间
     */
    private long createTime = System.currentTimeMillis();
    
    /**
     * 最后访问时间
     */
    private long lastAccessTime = System.currentTimeMillis();
    
    /**
     * 会话是否有效
     */
    private boolean active = true;
    
    /**
     * 便捷构造方法
     * 
     * @param userId 用户ID
     * @param userName 用户姓名
     * @param roles 角色数组
     */
    public Session(String userId, String userName, String... roles) {
        this.userId = userId;
        this.userName = userName;
        this.roles = roles;
    }
    
    /**
     * 检查用户是否具有指定权限
     * 
     * @param requiredRole 需要的角色
     * @return 是否有权限
     */
    public boolean hasPermission(String requiredRole) {
        // 匿名访问
        if ("anonymous".equals(requiredRole)) {
            return true;
        }
        
        // 会话无效
        if (!active || roles == null) {
            return false;
        }
        
        // 检查角色
        for (String role : roles) {
            if (role.equals(requiredRole) || "admin".equals(role)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否为管理员
     * 
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return hasPermission("admin");
    }
    
    /**
     * 获取所有角色
     * 
     * @return 角色集合
     */
    public Set<String> getRoleSet() {
        if (roles == null) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(roles));
    }
    
    /**
     * 添加角色
     * 
     * @param newRole 新角色
     */
    public void addRole(String newRole) {
        if (roles == null) {
            roles = new String[]{newRole};
            return;
        }
        
        // 检查是否已存在
        for (String role : roles) {
            if (role.equals(newRole)) {
                return;
            }
        }
        
        // 添加新角色
        String[] newRoles = Arrays.copyOf(roles, roles.length + 1);
        newRoles[roles.length] = newRole;
        this.roles = newRoles;
    }
    
    /**
     * 更新最后访问时间
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    /**
     * 使会话失效
     */
    public void invalidate() {
        this.active = false;
    }
    
    /**
     * 检查会话是否过期（30分钟无操作则过期）
     * 
     * @return 是否过期
     */
    public boolean isExpired() {
        long timeout = 30 * 60 * 1000; // 30分钟
        return System.currentTimeMillis() - lastAccessTime > timeout;
    }
    
    @Override
    public String toString() {
        return String.format("Session{userId='%s', userName='%s', roles=%s, active=%s}", 
                userId, userName, Arrays.toString(roles), active);
    }
}
