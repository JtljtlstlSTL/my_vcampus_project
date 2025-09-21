package com.vcampus.server.core.library.dao;

import com.vcampus.server.core.library.entity.core.BookRecommendation;
import com.vcampus.server.core.library.entity.view.RecommendationHistory;
import com.vcampus.server.core.library.enums.RecommendStatus;
import com.vcampus.server.core.library.mapper.BookRecommendationMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 图书荐购数据访问对象 - 使用MyBatis
 * 提供荐购记录的CRUD操作和业务查询方法
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class BookRecommendationDao {
    
    private static BookRecommendationDao instance;
    private final SqlSessionFactory sqlSessionFactory;
    
    private BookRecommendationDao() {
        // 初始化MyBatis SqlSessionFactory
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mybatis-config.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("初始化MyBatis失败", e);
        }
    }
    
    public static synchronized BookRecommendationDao getInstance() {
        if (instance == null) {
            instance = new BookRecommendationDao();
        }
        return instance;
    }
    
    // ==================== 基础CRUD方法 ====================
    
    /**
     * 根据ID查找荐购记录
     */
    public Optional<BookRecommendation> findById(Integer recId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            BookRecommendation recommendation = mapper.findById(recId);
            return Optional.ofNullable(recommendation);
        }
    }
    
    /**
     * 查找所有荐购记录
     */
    public List<BookRecommendation> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.findAll();
        }
    }
    
    /**
     * 保存荐购记录（新增或更新）
     */
    public BookRecommendation save(BookRecommendation recommendation) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            if (recommendation.getRecId() == null) {
                mapper.insert(recommendation);
            } else {
                mapper.update(recommendation);
            }
            session.commit();
            return recommendation;
        }
    }
    
    /**
     * 根据ID删除荐购记录
     */
    public void deleteById(Integer recId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            mapper.deleteById(recId);
            session.commit();
        }
    }
    
    /**
     * 检查荐购记录是否存在
     */
    public boolean existsById(Integer recId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.existsById(recId);
        }
    }
    
    /**
     * 统计荐购记录数量
     */
    public long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.count();
        }
    }
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 根据用户卡号查询荐购记录
     */
    public List<BookRecommendation> findByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.findByCardNum(cardNum);
        }
    }
    
    /**
     * 根据状态查询荐购记录
     */
    public List<BookRecommendation> findByStatus(RecommendStatus status) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.findByStatus(status.name());
        }
    }
    
    /**
     * 查询所有待审核的荐购记录
     */
    public List<BookRecommendation> findAllPending() {
        return findByStatus(RecommendStatus.PENDING);
    }
    
    /**
     * 根据ISBN查询荐购记录
     */
    public List<BookRecommendation> findByIsbn(String isbn) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.findByIsbn(isbn);
        }
    }
    
    /**
     * 根据书名模糊查询荐购记录
     */
    public List<BookRecommendation> findByTitleLike(String title) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.findByTitleLike(title);
        }
    }
    
    /**
     * 统计用户荐购数量
     */
    public long countByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.countByCardNum(cardNum);
        }
    }
    
    /**
     * 统计指定状态的荐购数量
     */
    public long countByStatus(RecommendStatus status) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.countByStatus(status.name());
        }
    }
    
    /**
     * 更新荐购状态
     */
    public void updateStatus(Integer recId, RecommendStatus status, String feedback, String adminCardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            int affected = mapper.updateStatus(recId, status.name(), feedback, adminCardNum);
            if (affected == 0) {
                throw new RuntimeException("更新状态失败：未找到ID为 " + recId + " 的荐购记录");
            }
            session.commit();
        }
    }
    
    /**
     * 更新管理员反馈
     */
    public int updateAdminFeedback(Integer recId, String adminFeedback, String adminCardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            int affected = mapper.updateAdminFeedback(recId, adminFeedback, adminCardNum);
            session.commit();
            return affected;
        }
    }
    
    // ==================== 视图查询方法 ====================
    
    /**
     * 查询用户荐购历史 - 使用视图
     */
    public List<RecommendationHistory> getUserRecommendationHistory(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.getUserRecommendationHistory(cardNum);
        }
    }
    
    /**
     * 查询所有荐购历史 - 使用视图
     */
    public List<RecommendationHistory> getAllRecommendationHistory() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookRecommendationMapper mapper = session.getMapper(BookRecommendationMapper.class);
            return mapper.getAllRecommendationHistory();
        }
    }
}
