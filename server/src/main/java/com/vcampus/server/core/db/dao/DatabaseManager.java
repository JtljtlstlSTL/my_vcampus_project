package com.vcampus.server.utility;

import java.sql.Connection;
import java.sql.SQLException;

import com.vcampus.common.db.DbHelper;
import com.vcampus.server.core.auth.dao.UserDao;

/**
 * 数据库管理器 - 管理所有DAO实例和数据库连接
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private final UserDao userDao;
    
    private DatabaseManager() {
        this.userDao = UserDao.getInstance();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * 获取用户DAO
     */
    public UserDao getUserDao() {
        return userDao;
    }
    
    /**
     * 初始化数据库连接
     */
    public void initDatabase() {
        try {
            DbHelper.init();
            System.out.println("数据库连接初始化成功");
        } catch (Exception e) {
            System.err.println("数据库连接初始化失败: " + e.getMessage());
            throw new RuntimeException("数据库初始化失败", e);
        }
    }
    
    /**
     * 测试数据库连接
     */
    public boolean testConnection() {
        try (Connection conn = DbHelper.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 关闭数据库连接
     */
    public void closeDatabase() {
        try {
            DbHelper.close();
            System.out.println("数据库连接已关闭");
        } catch (Exception e) {
            System.err.println("关闭数据库连接失败: " + e.getMessage());
        }
    }
}
