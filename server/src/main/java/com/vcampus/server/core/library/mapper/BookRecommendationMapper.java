package com.vcampus.server.core.library.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.vcampus.server.core.library.entity.core.BookRecommendation;
import com.vcampus.server.core.library.entity.view.RecommendationHistory;

/**
 * 图书荐购数据访问接口 - 使用MyBatis
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface BookRecommendationMapper {
    
    // ==================== 基础CRUD操作 ====================
    
    /**
     * 根据ID查找荐购记录
     */
    BookRecommendation findById(@Param("recId") Integer recId);
    
    /**
     * 查找所有荐购记录
     */
    List<BookRecommendation> findAll();
    
    /**
     * 根据用户卡号查找荐购记录
     */
    List<BookRecommendation> findByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 根据状态查找荐购记录
     */
    List<BookRecommendation> findByStatus(@Param("status") String status);
    
    /**
     * 根据ISBN查找荐购记录
     */
    List<BookRecommendation> findByIsbn(@Param("isbn") String isbn);
    
    /**
     * 根据书名模糊查询荐购记录
     */
    List<BookRecommendation> findByTitleLike(@Param("title") String title);
    
    /**
     * 插入荐购记录
     */
    int insert(BookRecommendation recommendation);
    
    /**
     * 更新荐购记录
     */
    int update(BookRecommendation recommendation);
    
    /**
     * 根据ID删除荐购记录
     */
    int deleteById(@Param("recId") Integer recId);
    
    /**
     * 检查荐购记录是否存在
     */
    boolean existsById(@Param("recId") Integer recId);
    
    /**
     * 统计荐购记录数量
     */
    long count();
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 统计用户荐购数量
     */
    long countByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 统计指定状态的荐购数量
     */
    long countByStatus(@Param("status") String status);
    
    /**
     * 更新荐购状态
     */
    int updateStatus(@Param("recId") Integer recId, 
                    @Param("status") String status, 
                    @Param("feedback") String feedback, 
                    @Param("adminCardNum") String adminCardNum);
    
    /**
     * 更新管理员反馈
     */
    int updateAdminFeedback(@Param("recId") Integer recId, 
                           @Param("adminFeedback") String adminFeedback, 
                           @Param("adminCardNum") String adminCardNum);
    
    // ==================== 视图查询方法 ====================
    
    /**
     * 查询用户荐购历史 - 使用视图
     */
    List<RecommendationHistory> getUserRecommendationHistory(@Param("cardNum") String cardNum);
    
    /**
     * 查询所有荐购历史 - 使用视图
     */
    List<RecommendationHistory> getAllRecommendationHistory();
}
