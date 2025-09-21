package com.vcampus.server.core.system.constant;

/**
 * 系统管理模块常量定义
 * 
 * @author VCampus Team
 */
public class SystemConstant {
    
    // 系统状态常量
    public static final String SYSTEM_STATUS_RUNNING = "RUNNING";
    public static final String SYSTEM_STATUS_MAINTENANCE = "MAINTENANCE";
    public static final String SYSTEM_STATUS_SHUTDOWN = "SHUTDOWN";
    
    // 日志级别常量
    public static final String LOG_LEVEL_DEBUG = "DEBUG";
    public static final String LOG_LEVEL_INFO = "INFO";
    public static final String LOG_LEVEL_WARN = "WARN";
    public static final String LOG_LEVEL_ERROR = "ERROR";
    
    // 配置键常量
    public static final String CONFIG_SERVER_PORT = "server.port";
    public static final String CONFIG_DATABASE_URL = "database.url";
    public static final String CONFIG_MAX_CONNECTIONS = "database.maxConnections";
    public static final String CONFIG_SESSION_TIMEOUT = "session.timeout";
    
    // 系统默认值
    public static final int DEFAULT_SERVER_PORT = 8080;
    public static final int DEFAULT_MAX_CONNECTIONS = 20;
    public static final long DEFAULT_SESSION_TIMEOUT = 3600000L;
    
    // 操作类型常量
    public static final String OPERATION_CREATE = "CREATE";
    public static final String OPERATION_UPDATE = "UPDATE";
    public static final String OPERATION_DELETE = "DELETE";
    public static final String OPERATION_LOGIN = "LOGIN";
    public static final String OPERATION_LOGOUT = "LOGOUT";
    
    private SystemConstant() {
        // 工具类，禁止实例化
    }
}
