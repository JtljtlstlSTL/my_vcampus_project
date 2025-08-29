package com.vcampus.common.db.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

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
        HikariConfig config = new HikariConfig();
        
        // 数据库连接配置
        config.setJdbcUrl("jdbc:mysql://localhost:3306/vcampus?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8");
        config.setUsername("root");
        config.setPassword("password");
        
        // 连接池配置
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // 连接测试配置
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        
        dataSource = new HikariDataSource(config);
    }
    
    public static DataSource getInstance() {
        return dataSource;
    }
}
