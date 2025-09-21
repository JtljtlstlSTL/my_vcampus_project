package com.vcampus.server.core.auth.constant;

/**
 * 认证授权模块常量定义
 * 
 * @author VCampus Team
 */
public class AuthConstant {
    
    // 用户角色常量
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_STAFF = "STAFF";
    
    // 用户状态常量
    public static final String USER_STATUS_ACTIVE = "ACTIVE";
    public static final String USER_STATUS_INACTIVE = "INACTIVE";
    public static final String USER_STATUS_LOCKED = "LOCKED";
    public static final String USER_STATUS_DELETED = "DELETED";
    
    // 会话相关常量
    public static final String SESSION_TOKEN_KEY = "X-Auth-Token";
    public static final long SESSION_TIMEOUT = 3600000L; // 1小时
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_ROLE = "role";
    
    // 密码策略常量
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 20;
    public static final int PASSWORD_SALT_LENGTH = 16;
    
    private AuthConstant() {
        // 工具类，禁止实例化
    }
}
