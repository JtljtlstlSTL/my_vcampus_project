package com.vcampus.common.constant;

/**
 * 系统常量定义
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class SystemConstant {
    
    // 系统版本
    public static final String SYSTEM_VERSION = "1.0.0";
    
    // 系统名称
    public static final String SYSTEM_NAME = "VCampus";
    
    // 默认编码
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    // 分页大小
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    // 会话超时时间（毫秒）
    public static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30分钟
    
    // 系统角色
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ANONYMOUS = "anonymous";
    
    // 日期时间格式
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
}
