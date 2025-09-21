package com.vcampus.server.core.library.service;

import java.util.List;

import com.vcampus.server.core.library.entity.core.BookRecommendation;
import com.vcampus.server.core.library.entity.result.RecommendResult;
import com.vcampus.server.core.library.entity.view.RecommendationHistory;
import com.vcampus.server.core.library.enums.RecommendStatus;

/**
 * 图书荐购服务接口
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface BookRecommendationService {
    
    // ==================== 用户荐购功能 ====================
    
    /**
     * 提交图书荐购申请
     * 
     * @param cardNum 用户卡号
     * @param bookTitle 书名
     * @param bookAuthor 作者
     * @param bookPublisher 出版社
     * @param bookIsbn ISBN
     * @param bookCategory 分类代码
     * @param recommendQty 推荐数量
     * @param recommendReason 荐购理由
     * @return 荐购结果
     */
    RecommendResult submitRecommendation(String cardNum, String bookTitle, String bookAuthor, 
                                       String bookPublisher, String bookIsbn, String bookCategory, 
                                       Integer recommendQty, String recommendReason);
    
    /**
     * 查看用户荐购历史
     * 
     * @param cardNum 用户卡号
     * @return 荐购历史列表
     */
    List<RecommendationHistory> getUserRecommendationHistory(String cardNum);
    
    /**
     * 根据ID查看荐购详情
     * 
     * @param recId 荐购ID
     * @param cardNum 用户卡号（用于权限验证）
     * @return 荐购详情
     */
    BookRecommendation getRecommendationDetail(Integer recId, String cardNum);
    
    /**
     * 取消荐购申请（仅限待审核状态）
     * 
     * @param recId 荐购ID
     * @param cardNum 用户卡号
     * @return 操作结果
     */
    RecommendResult cancelRecommendation(Integer recId, String cardNum);
    
    // ==================== 管理员功能 ====================
    
    /**
     * 获取所有荐购申请
     * 
     * @return 所有荐购历史列表
     */
    List<RecommendationHistory> getAllRecommendations();
    
    /**
     * 获取待审核的荐购申请
     * 
     * @return 待审核荐购列表
     */
    List<BookRecommendation> getPendingRecommendations();
    
    /**
     * 审核荐购申请
     * 
     * @param recId 荐购ID
     * @param status 审核状态
     * @param feedback 管理员反馈
     * @param adminCardNum 管理员卡号
     * @return 操作结果
     */
    RecommendResult reviewRecommendation(Integer recId, RecommendStatus status, 
                                       String feedback, String adminCardNum);
    
    /**
     * 更新管理员反馈
     * 
     * @param recId 荐购ID
     * @param adminFeedback 管理员反馈
     * @param adminCardNum 管理员卡号
     * @return 操作结果
     */
    RecommendResult updateAdminFeedback(Integer recId, String adminFeedback, String adminCardNum);
    
    /**
     * 批量审核荐购申请
     * 
     * @param recIds 荐购ID列表
     * @param status 审核状态
     * @param feedback 管理员反馈
     * @param adminCardNum 管理员卡号
     * @return 操作结果
     */
    RecommendResult batchReviewRecommendations(List<Integer> recIds, RecommendStatus status, 
                                             String feedback, String adminCardNum);
    
    // ==================== 查询统计功能 ====================
    
    /**
     * 根据状态查询荐购申请
     * 
     * @param status 荐购状态
     * @return 荐购列表
     */
    List<BookRecommendation> getRecommendationsByStatus(RecommendStatus status);
    
    /**
     * 根据状态查询荐购历史
     * 
     * @param status 荐购状态
     * @return 荐购历史列表
     */
    List<RecommendationHistory> getRecommendationHistoryByStatus(RecommendStatus status);
    
    /**
     * 根据ISBN查询荐购申请
     * 
     * @param isbn ISBN
     * @return 荐购列表
     */
    List<BookRecommendation> getRecommendationsByIsbn(String isbn);
    
    /**
     * 根据书名模糊查询荐购申请
     * 
     * @param title 书名关键词
     * @return 荐购列表
     */
    List<BookRecommendation> searchRecommendationsByTitle(String title);
    
    /**
     * 统计用户荐购数量
     * 
     * @param cardNum 用户卡号
     * @return 荐购数量
     */
    long countUserRecommendations(String cardNum);
    
    /**
     * 统计指定状态的荐购数量
     * 
     * @param status 荐购状态
     * @return 荐购数量
     */
    long countRecommendationsByStatus(RecommendStatus status);
    
    /**
     * 获取荐购统计信息
     * 
     * @return 统计信息（各状态数量）
     */
    java.util.Map<RecommendStatus, Long> getRecommendationStatistics();
}
