package com.vcampus.server.core.library.service.impl;

import com.vcampus.server.core.library.dao.BookRecommendationDao;
import com.vcampus.server.core.library.entity.core.BookRecommendation;
import com.vcampus.server.core.library.entity.result.RecommendResult;
import com.vcampus.server.core.library.entity.view.RecommendationHistory;
import com.vcampus.server.core.library.enums.RecommendStatus;
import com.vcampus.server.core.library.service.BookRecommendationService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 图书荐购服务实现类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class BookRecommendationServiceImpl implements BookRecommendationService {
    
    private final BookRecommendationDao recommendationDao;
    
    public BookRecommendationServiceImpl() {
        this.recommendationDao = BookRecommendationDao.getInstance();
    }
    
    // ==================== 用户荐购功能 ====================
    
    @Override
    public RecommendResult submitRecommendation(String cardNum, String bookTitle, String bookAuthor, 
                                              String bookPublisher, String bookIsbn, String bookCategory, 
                                              Integer recommendQty, String recommendReason) {
        log.info("开始处理荐购申请: cardNum={}, bookTitle={}", cardNum, bookTitle);
        
        try {
            // 1. 参数验证
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return RecommendResult.failure("用户卡号不能为空");
            }
            if (bookTitle == null || bookTitle.trim().isEmpty()) {
                return RecommendResult.failure("书名不能为空");
            }
            if (bookAuthor == null || bookAuthor.trim().isEmpty()) {
                return RecommendResult.failure("作者不能为空");
            }
            if (bookPublisher == null || bookPublisher.trim().isEmpty()) {
                return RecommendResult.failure("出版社不能为空");
            }
            if (bookIsbn == null || bookIsbn.trim().isEmpty()) {
                return RecommendResult.failure("ISBN不能为空");
            }
            if (recommendQty == null || recommendQty <= 0) {
                return RecommendResult.failure("推荐数量必须大于0");
            }
            if (recommendReason == null || recommendReason.trim().isEmpty()) {
                return RecommendResult.failure("荐购理由不能为空");
            }
            
            // 2. 检查是否已存在相同的荐购申请（同一用户同一ISBN）
            List<BookRecommendation> existingRecs = recommendationDao.findByIsbn(bookIsbn);
            for (BookRecommendation existing : existingRecs) {
                if (existing.getCardNum().equals(cardNum) && 
                    existing.getStatus() == RecommendStatus.PENDING) {
                    return RecommendResult.failure("您已提交过该图书的荐购申请，请勿重复提交");
                }
            }
            
            // 3. 创建荐购记录
            BookRecommendation recommendation = BookRecommendation.builder()
                    .cardNum(cardNum.trim())
                    .bookTitle(bookTitle.trim())
                    .bookAuthor(bookAuthor.trim())
                    .bookPublisher(bookPublisher.trim())
                    .bookIsbn(bookIsbn.trim())
                    .bookCategory(bookCategory != null ? bookCategory.trim() : null)
                    .recommendQty(recommendQty)
                    .recommendReason(recommendReason.trim())
                    .recommendTime(LocalDateTime.now())
                    .status(RecommendStatus.PENDING)
                    .build();
            
            // 4. 保存荐购记录
            BookRecommendation savedRecommendation = recommendationDao.save(recommendation);
            
            log.info("荐购申请提交成功: recId={}, cardNum={}, bookTitle={}", 
                    savedRecommendation.getRecId(), cardNum, bookTitle);
            
            return RecommendResult.success("荐购申请提交成功，等待管理员审核", savedRecommendation);
            
        } catch (Exception e) {
            log.error("提交荐购申请异常: cardNum={}, bookTitle={}", cardNum, bookTitle, e);
            return RecommendResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public List<RecommendationHistory> getUserRecommendationHistory(String cardNum) {
        log.info("查询用户荐购历史: cardNum={}", cardNum);
        
        try {
            if (cardNum == null || cardNum.trim().isEmpty()) {
                log.warn("用户卡号为空");
                return List.of();
            }
            
            List<RecommendationHistory> history = recommendationDao.getUserRecommendationHistory(cardNum.trim());
            log.info("查询到用户荐购历史: cardNum={}, count={}", cardNum, history.size());
            
            return history;
            
        } catch (Exception e) {
            log.error("查询用户荐购历史异常: cardNum={}", cardNum, e);
            return List.of();
        }
    }
    
    @Override
    public BookRecommendation getRecommendationDetail(Integer recId, String cardNum) {
        log.info("查询荐购详情: recId={}, cardNum={}", recId, cardNum);
        
        try {
            if (recId == null) {
                log.warn("荐购ID为空");
                return null;
            }
            
            Optional<BookRecommendation> recommendationOpt = recommendationDao.findById(recId);
            if (!recommendationOpt.isPresent()) {
                log.warn("荐购记录不存在: recId={}", recId);
                return null;
            }
            
            BookRecommendation recommendation = recommendationOpt.get();
            
            // 权限验证：用户只能查看自己的荐购记录
            if (cardNum != null && !cardNum.trim().isEmpty() && 
                !recommendation.getCardNum().equals(cardNum.trim())) {
                log.warn("用户无权查看该荐购记录: recId={}, cardNum={}, owner={}", 
                        recId, cardNum, recommendation.getCardNum());
                return null;
            }
            
            return recommendation;
            
        } catch (Exception e) {
            log.error("查询荐购详情异常: recId={}, cardNum={}", recId, cardNum, e);
            return null;
        }
    }
    
    @Override
    public RecommendResult cancelRecommendation(Integer recId, String cardNum) {
        log.info("取消荐购申请: recId={}, cardNum={}", recId, cardNum);
        
        try {
            // 1. 参数验证
            if (recId == null) {
                return RecommendResult.failure("荐购ID不能为空");
            }
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return RecommendResult.failure("用户卡号不能为空");
            }
            
            // 2. 查询荐购记录
            Optional<BookRecommendation> recommendationOpt = recommendationDao.findById(recId);
            if (!recommendationOpt.isPresent()) {
                return RecommendResult.failure("荐购记录不存在");
            }
            
            BookRecommendation recommendation = recommendationOpt.get();
            
            // 3. 权限验证
            if (!recommendation.getCardNum().equals(cardNum.trim())) {
                return RecommendResult.failure("无权取消该荐购申请");
            }
            
            // 4. 状态验证
            if (recommendation.getStatus() != RecommendStatus.PENDING) {
                return RecommendResult.failure("只能取消待审核状态的荐购申请");
            }
            
            // 5. 删除荐购记录
            recommendationDao.deleteById(recId);
            
            log.info("荐购申请取消成功: recId={}, cardNum={}", recId, cardNum);
            return RecommendResult.success("荐购申请已取消");
            
        } catch (Exception e) {
            log.error("取消荐购申请异常: recId={}, cardNum={}", recId, cardNum, e);
            return RecommendResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 管理员功能 ====================
    
    @Override
    public List<RecommendationHistory> getAllRecommendations() {
        log.info("查询所有荐购申请");
        
        try {
            List<RecommendationHistory> allRecommendations = recommendationDao.getAllRecommendationHistory();
            log.info("查询到所有荐购申请: count={}", allRecommendations.size());
            
            return allRecommendations;
            
        } catch (Exception e) {
            log.error("查询所有荐购申请异常", e);
            return List.of();
        }
    }
    
    @Override
    public List<BookRecommendation> getPendingRecommendations() {
        log.info("查询待审核荐购申请");
        
        try {
            List<BookRecommendation> pendingRecommendations = recommendationDao.findAllPending();
            log.info("查询到待审核荐购申请: count={}", pendingRecommendations.size());
            
            return pendingRecommendations;
            
        } catch (Exception e) {
            log.error("查询待审核荐购申请异常", e);
            return List.of();
        }
    }
    
    @Override
    public RecommendResult reviewRecommendation(Integer recId, RecommendStatus status, 
                                              String feedback, String adminCardNum) {
        log.info("审核荐购申请: recId={}, status={}, adminCardNum={}", recId, status, adminCardNum);
        
        try {
            // 1. 参数验证
            if (recId == null) {
                return RecommendResult.failure("荐购ID不能为空");
            }
            if (status == null) {
                return RecommendResult.failure("审核状态不能为空");
            }
            if (adminCardNum == null || adminCardNum.trim().isEmpty()) {
                return RecommendResult.failure("管理员卡号不能为空");
            }
            
            // 2. 查询荐购记录
            Optional<BookRecommendation> recommendationOpt = recommendationDao.findById(recId);
            if (!recommendationOpt.isPresent()) {
                return RecommendResult.failure("荐购记录不存在");
            }
            
            BookRecommendation recommendation = recommendationOpt.get();
            
            // 3. 状态验证
            if (recommendation.getStatus() != RecommendStatus.PENDING) {
                return RecommendResult.failure("只能审核待审核状态的荐购申请");
            }
            
            // 4. 更新荐购状态
            recommendationDao.updateStatus(recId, status, feedback, adminCardNum.trim());
            
            log.info("荐购申请审核成功: recId={}, status={}, adminCardNum={}", 
                    recId, status, adminCardNum);
            
            String statusDesc = status.getDescription();
            String message = String.format("荐购申请已%s", statusDesc);
            return RecommendResult.success(message, recId);
            
        } catch (Exception e) {
            log.error("审核荐购申请异常: recId={}, status={}, adminCardNum={}", 
                    recId, status, adminCardNum, e);
            return RecommendResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public RecommendResult updateAdminFeedback(Integer recId, String adminFeedback, String adminCardNum) {
        log.info("更新管理员反馈: recId={}, adminCardNum={}", recId, adminCardNum);
        
        try {
            // 1. 参数验证
            if (recId == null) {
                return RecommendResult.failure("荐购ID不能为空");
            }
            if (adminCardNum == null || adminCardNum.trim().isEmpty()) {
                return RecommendResult.failure("管理员卡号不能为空");
            }
            
            // 2. 检查荐购记录是否存在
            Optional<BookRecommendation> recommendationOpt = recommendationDao.findById(recId);
            if (!recommendationOpt.isPresent()) {
                return RecommendResult.failure("荐购记录不存在");
            }
            
            // 3. 更新管理员反馈
            int updated = recommendationDao.updateAdminFeedback(recId, adminFeedback, adminCardNum);
            if (updated > 0) {
                log.info("更新管理员反馈成功: recId={}, adminCardNum={}", recId, adminCardNum);
                return RecommendResult.success("管理员反馈更新成功");
            } else {
                log.warn("更新管理员反馈失败: recId={}, adminCardNum={}", recId, adminCardNum);
                return RecommendResult.failure("更新管理员反馈失败");
            }
            
        } catch (Exception e) {
            log.error("更新管理员反馈异常: recId={}, adminCardNum={}", recId, adminCardNum, e);
            return RecommendResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public RecommendResult batchReviewRecommendations(List<Integer> recIds, RecommendStatus status, 
                                                    String feedback, String adminCardNum) {
        log.info("批量审核荐购申请: recIds={}, status={}, adminCardNum={}", recIds, status, adminCardNum);
        
        try {
            // 1. 参数验证
            if (recIds == null || recIds.isEmpty()) {
                return RecommendResult.failure("荐购ID列表不能为空");
            }
            if (status == null) {
                return RecommendResult.failure("审核状态不能为空");
            }
            if (adminCardNum == null || adminCardNum.trim().isEmpty()) {
                return RecommendResult.failure("管理员卡号不能为空");
            }
            
            int successCount = 0;
            int failCount = 0;
            
            // 2. 逐个审核
            for (Integer recId : recIds) {
                try {
                    RecommendResult result = reviewRecommendation(recId, status, feedback, adminCardNum);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                        log.warn("批量审核失败: recId={}, reason={}", recId, result.getMessage());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("批量审核异常: recId={}", recId, e);
                }
            }
            
            String message = String.format("批量审核完成，成功: %d, 失败: %d", successCount, failCount);
            log.info("批量审核荐购申请完成: {}", message);
            
            return RecommendResult.success(message);
            
        } catch (Exception e) {
            log.error("批量审核荐购申请异常: recIds={}, status={}, adminCardNum={}", 
                    recIds, status, adminCardNum, e);
            return RecommendResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 查询统计功能 ====================
    
    @Override
    public List<BookRecommendation> getRecommendationsByStatus(RecommendStatus status) {
        log.info("根据状态查询荐购申请: status={}", status);
        
        try {
            if (status == null) {
                log.warn("状态为空");
                return List.of();
            }
            
            List<BookRecommendation> recommendations = recommendationDao.findByStatus(status);
            log.info("查询到荐购申请: status={}, count={}", status, recommendations.size());
            
            return recommendations;
            
        } catch (Exception e) {
            log.error("根据状态查询荐购申请异常: status={}", status, e);
            return List.of();
        }
    }
    
    @Override
    public List<RecommendationHistory> getRecommendationHistoryByStatus(RecommendStatus status) {
        log.info("根据状态查询荐购历史: status={}", status);
        
        try {
            if (status == null) {
                log.warn("状态为空");
                return List.of();
            }
            
            // 获取所有荐购历史，然后在前端过滤
            List<RecommendationHistory> allHistory = recommendationDao.getAllRecommendationHistory();
            List<RecommendationHistory> filteredHistory = allHistory.stream()
                .filter(history -> status.name().equals(history.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            
            log.info("查询到荐购历史: status={}, count={}", status, filteredHistory.size());
            
            return filteredHistory;
            
        } catch (Exception e) {
            log.error("根据状态查询荐购历史异常: status={}", status, e);
            return List.of();
        }
    }
    
    @Override
    public List<BookRecommendation> getRecommendationsByIsbn(String isbn) {
        log.info("根据ISBN查询荐购申请: isbn={}", isbn);
        
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                log.warn("ISBN为空");
                return List.of();
            }
            
            List<BookRecommendation> recommendations = recommendationDao.findByIsbn(isbn.trim());
            log.info("查询到荐购申请: isbn={}, count={}", isbn, recommendations.size());
            
            return recommendations;
            
        } catch (Exception e) {
            log.error("根据ISBN查询荐购申请异常: isbn={}", isbn, e);
            return List.of();
        }
    }
    
    @Override
    public List<BookRecommendation> searchRecommendationsByTitle(String title) {
        log.info("根据书名搜索荐购申请: title={}", title);
        
        try {
            if (title == null || title.trim().isEmpty()) {
                log.warn("书名为空");
                return List.of();
            }
            
            List<BookRecommendation> recommendations = recommendationDao.findByTitleLike(title.trim());
            log.info("搜索到荐购申请: title={}, count={}", title, recommendations.size());
            
            return recommendations;
            
        } catch (Exception e) {
            log.error("根据书名搜索荐购申请异常: title={}", title, e);
            return List.of();
        }
    }
    
    @Override
    public long countUserRecommendations(String cardNum) {
        log.info("统计用户荐购数量: cardNum={}", cardNum);
        
        try {
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return 0;
            }
            
            long count = recommendationDao.countByCardNum(cardNum.trim());
            log.info("用户荐购数量: cardNum={}, count={}", cardNum, count);
            
            return count;
            
        } catch (Exception e) {
            log.error("统计用户荐购数量异常: cardNum={}", cardNum, e);
            return 0;
        }
    }
    
    @Override
    public long countRecommendationsByStatus(RecommendStatus status) {
        log.info("统计指定状态荐购数量: status={}", status);
        
        try {
            if (status == null) {
                return 0;
            }
            
            long count = recommendationDao.countByStatus(status);
            log.info("指定状态荐购数量: status={}, count={}", status, count);
            
            return count;
            
        } catch (Exception e) {
            log.error("统计指定状态荐购数量异常: status={}", status, e);
            return 0;
        }
    }
    
    @Override
    public Map<RecommendStatus, Long> getRecommendationStatistics() {
        log.info("获取荐购统计信息");
        
        try {
            Map<RecommendStatus, Long> statistics = new HashMap<>();
            
            for (RecommendStatus status : RecommendStatus.values()) {
                long count = countRecommendationsByStatus(status);
                statistics.put(status, count);
            }
            
            log.info("荐购统计信息: {}", statistics);
            return statistics;
            
        } catch (Exception e) {
            log.error("获取荐购统计信息异常", e);
            return new HashMap<>();
        }
    }
}
