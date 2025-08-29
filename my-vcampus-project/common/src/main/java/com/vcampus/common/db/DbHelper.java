package com.vcampus.common.db;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VCampus数据库工具类 - DbHelper
 * 
 * 核心功能：
 * 1. 数据库连接管理 - 连接池、事务管理
 * 2. SQL执行器 - 统一的CRUD操作
 * 3. 结果集处理 - ResultSet到对象的映射
 * 4. 批量操作支持 - 提高性能
 * 
 * 设计特点：
 * - 极简：专注于数据库连接和SQL执行
 * - 轻量级：不依赖ORM框架
 * - 灵活性：支持原生SQL和对象映射
 * - 易用性：简单的API设计
 * 
 * @author VCampus Team
 * @version 2.0
 */
public class DbHelper {
    
    private static final Map<String, DataSourceConfig> dataSources = new ConcurrentHashMap<>();
    private static final String DEFAULT_DATASOURCE = "default";
    private static boolean initialized = false;
    
    private DbHelper() {}
    
    /**
     * 初始化数据库连接池
     */
    public static synchronized void init() {
        if (!initialized) {
            dataSources.put(DEFAULT_DATASOURCE, new DataSourceConfig("com.vcampus.common.db.impl.MysqlDataSource"));
            initialized = true;
        }
    }
    
    /**
     * 初始化数据库连接池（别名方法）
     */
    public static synchronized void initialize() {
        init();
    }
    
    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(DEFAULT_DATASOURCE);
    }
    
    /**
     * 获取指定数据源的连接
     */
    public static Connection getConnection(String dataSourceName) throws SQLException {
        DataSourceConfig config = dataSources.get(dataSourceName);
        if (config == null) {
            throw new SQLException("DataSource not found: " + dataSourceName);
        }
        return config.getDataSource().getConnection();
    }
    
    /**
     * 执行查询并返回结果集
     */
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        setParameters(stmt, params);
        return stmt.executeQuery();
    }
    
    /**
     * 执行更新操作
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        setParameters(stmt, params);
        return stmt.executeUpdate();
    }
    
    /**
     * 设置PreparedStatement参数
     */
    private static void setParameters(PreparedStatement stmt, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }
    
    /**
     * 关闭连接资源
     */
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { System.err.println("Error closing ResultSet: " + e.getMessage()); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { System.err.println("Error closing Statement: " + e.getMessage()); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { System.err.println("Error closing Connection: " + e.getMessage()); }
    }
    
    /**
     * 关闭数据库连接池
     */
    public static synchronized void close() {
        if (initialized) {
            // 关闭所有数据源连接
            for (DataSourceConfig config : dataSources.values()) {
                try {
                    javax.sql.DataSource ds = config.getDataSource();
                    if (ds instanceof AutoCloseable) {
                        ((AutoCloseable) ds).close();
                    }
                } catch (Exception e) {
                    System.err.println("Error closing DataSource: " + e.getMessage());
                }
            }
            dataSources.clear();
            initialized = false;
        }
    }
    
    /**
     * 数据源配置类
     */
    public static class DataSourceConfig {
        private final String className;
        
        public DataSourceConfig(String className) {
            this.className = className;
        }
        
        public javax.sql.DataSource getDataSource() {
            try {
                Class<?> clazz = Class.forName(className);
                // 调用getInstance()方法获取单例实例
                java.lang.reflect.Method getInstanceMethod = clazz.getMethod("getInstance");
                return (javax.sql.DataSource) getInstanceMethod.invoke(null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create DataSource: " + className, e);
            }
        }
    }
}
