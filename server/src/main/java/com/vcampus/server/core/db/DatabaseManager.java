package com.vcampus.server.core.db;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 数据库管理器 - 管理MyBatis SqlSessionFactory
 */
public class DatabaseManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    
    private static SqlSessionFactory sqlSessionFactory;
    private static final String MYBATIS_CONFIG_PATH = "mybatis-config.xml";
    
    static {
        try {
            // 加载MyBatis配置文件
            InputStream inputStream = Resources.getResourceAsStream(MYBATIS_CONFIG_PATH);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            logger.info("MyBatis SqlSessionFactory 初始化成功");
        } catch (IOException e) {
            logger.error("初始化MyBatis SqlSessionFactory失败", e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }
    
    /**
     * 获取SqlSessionFactory实例
     * @return SqlSessionFactory
     */
    public static SqlSessionFactory getSqlSessionFactory() {
        if (sqlSessionFactory == null) {
            throw new RuntimeException("SqlSessionFactory未初始化");
        }
        return sqlSessionFactory;
    }
    
    /**
     * 重新加载配置（用于配置更新）
     */
    public static void reload() {
        try {
            InputStream inputStream = Resources.getResourceAsStream(MYBATIS_CONFIG_PATH);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            logger.info("MyBatis配置重新加载成功");
        } catch (IOException e) {
            logger.error("重新加载MyBatis配置失败", e);
            throw new RuntimeException("配置重新加载失败", e);
        }
    }
}
