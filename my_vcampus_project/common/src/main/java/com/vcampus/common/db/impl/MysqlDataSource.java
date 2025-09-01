package com.vcampus.common.db.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * MySQL数据源实现
 * 使用HikariCP连接池
 */
public class MysqlDataSource {
    
    private static HikariDataSource dataSource;
    
    static {
        initializeDataSource();
    }
    
    private static void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // 从配置文件读取数据库连接配置
            Properties props = loadDatabaseProperties();
            
            // 数据库连接配置
            config.setJdbcUrl(props.getProperty("db.url", "jdbc:mysql://localhost:3306/virtual_campus?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8"));
            config.setUsername(props.getProperty("db.username", "root"));
            config.setPassword(props.getProperty("db.password", "123456"));
            config.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
            
            // 连接池配置
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "5")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
            
            // 连接测试配置
            config.setConnectionTestQuery(props.getProperty("db.pool.connectionTestQuery", "SELECT 1"));
            config.setValidationTimeout(Long.parseLong(props.getProperty("db.pool.validationTimeout", "5000")));
            
            dataSource = new HikariDataSource(config);
            
            // 测试连接
            testConnection();
            
        } catch (Exception e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 加载数据库配置文件
     */
    private static Properties loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = MysqlDataSource.class.getClassLoader().getResourceAsStream("config/database.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                System.out.println("Database config file not found, using default values");
            }
        } catch (IOException e) {
            System.err.println("Failed to load database config: " + e.getMessage());
        }
        return props;
    }
    
    /**
     * 测试数据库连接
     */
    private static void testConnection() {
        try (var conn = dataSource.getConnection()) {
            System.out.println("Database connection test successful");
        } catch (Exception e) {
            System.err.println("Database connection test failed: " + e.getMessage());
        }
    }
    
    public static DataSource getInstance() {
        return dataSource;
    }
}
