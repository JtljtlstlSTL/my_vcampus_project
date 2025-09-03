package com.vcampus.common.db;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VCampus Database Utility Class - DbHelper
 * 
 * Core Features:
 * 1. Database Connection Management - Connection Pool, Transaction Management
 * 2. SQL Executor - Unified CRUD Operations
 * 3. Result Set Processing - ResultSet to Object Mapping
 * 4. Batch Operation Support - Performance Improvement
 * 
 * Design Features:
 * - Minimalist: Focus on database connection and SQL execution
 * - Lightweight: No ORM framework dependency
 * - Flexible: Support native SQL and object mapping
 * - Easy to use: Simple API design
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
     * Initialize database connection pool
     */
    public static synchronized void init() {
        if (!initialized) {
            dataSources.put(DEFAULT_DATASOURCE, new DataSourceConfig("com.vcampus.common.db.impl.MysqlDataSource"));
            initialized = true;
        }
    }
    
    /**
     * Initialize database connection pool (alias method)
     */
    public static synchronized void initialize() {
        init();
    }
    
    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(DEFAULT_DATASOURCE);
    }
    
    /**
     * Get connection from specified data source
     */
    public static Connection getConnection(String dataSourceName) throws SQLException {
        DataSourceConfig config = dataSources.get(dataSourceName);
        if (config == null) {
            throw new SQLException("DataSource not found: " + dataSourceName);
        }
        return config.getDataSource().getConnection();
    }
    
    /**
     * Execute query and return result set
     */
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        setParameters(stmt, params);
        return stmt.executeQuery();
    }
    
    /**
     * Execute update operation
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        setParameters(stmt, params);
        return stmt.executeUpdate();
    }
    
    /**
     * Set PreparedStatement parameters
     */
    private static void setParameters(PreparedStatement stmt, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }
    
    /**
     * Close connection resources
     */
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { System.err.println("Error closing ResultSet: " + e.getMessage()); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { System.err.println("Error closing Statement: " + e.getMessage()); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { System.err.println("Error closing Connection: " + e.getMessage()); }
    }
    
    /**
     * Close database connection pool
     */
    public static synchronized void close() {
        if (initialized) {
            // Close all data source connections
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
     * Data source configuration class
     */
    public static class DataSourceConfig {
        private final String className;
        
        public DataSourceConfig(String className) {
            this.className = className;
        }
        
        public javax.sql.DataSource getDataSource() {
            try {
                Class<?> clazz = Class.forName(className);
                // Call getInstance() method to get singleton instance
                java.lang.reflect.Method getInstanceMethod = clazz.getMethod("getInstance");
                return (javax.sql.DataSource) getInstanceMethod.invoke(null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create DataSource: " + className, e);
            }
        }
    }
}
