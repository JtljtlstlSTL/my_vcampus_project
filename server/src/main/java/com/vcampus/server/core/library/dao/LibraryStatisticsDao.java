package com.vcampus.server.core.library.dao;

import com.vcampus.server.core.library.entity.search.UserStatistics;
import com.vcampus.server.core.library.entity.view.UserBorrowStatistics;
import com.vcampus.server.core.library.entity.view.CategoryStatistics;
import com.vcampus.server.core.library.entity.view.OverdueStatistics;
import com.vcampus.server.core.library.mapper.LibraryStatisticsMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;


/**
 * 图书馆统计信息数据访问对象 - 使用MyBatis
 * 提供统计查询和报表功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class LibraryStatisticsDao {
    
    private static LibraryStatisticsDao instance;
    private final SqlSessionFactory sqlSessionFactory;
    
    private LibraryStatisticsDao() {
        // 初始化MyBatis SqlSessionFactory
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mybatis-config.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("初始化MyBatis失败", e);
        }
    }
    
    public static synchronized LibraryStatisticsDao getInstance() {
        if (instance == null) {
            instance = new LibraryStatisticsDao();
        }
        return instance;
    }
    
    // ==================== 存储过程调用方法 ====================
    
    /**
     * 获取用户统计信息 - 调用存储过程 sp_get_user_statistics
     * @param cardNum 用户卡号
     * @return 用户统计信息
     */
    public UserStatistics getUserStatistics(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LibraryStatisticsMapper mapper = session.getMapper(LibraryStatisticsMapper.class);
            return mapper.getUserStatistics(cardNum);
        }
    }
    
    // ==================== 视图查询方法 ====================
    
    /**
     * 查询用户借阅统计 - 使用视图 v_user_borrow_statistics
     * @return 用户借阅统计列表
     */
    public List<UserBorrowStatistics> getUserBorrowStatistics() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LibraryStatisticsMapper mapper = session.getMapper(LibraryStatisticsMapper.class);
            return mapper.getUserBorrowStatistics();
        }
    }
    
    /**
     * 查询分类统计 - 使用视图 v_category_statistics
     * @return 分类统计列表
     */
    public List<CategoryStatistics> getCategoryStatistics() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LibraryStatisticsMapper mapper = session.getMapper(LibraryStatisticsMapper.class);
            return mapper.getCategoryStatistics();
        }
    }
    
    /**
     * 查询逾期统计 - 使用视图 v_overdue_statistics
     * @return 逾期统计列表
     */
    public List<OverdueStatistics> getOverdueStatistics() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LibraryStatisticsMapper mapper = session.getMapper(LibraryStatisticsMapper.class);
            return mapper.getOverdueStatistics();
        }
    }
    

}
