package com.vcampus.server.core.library.entity.view;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 荐购历史视图类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationHistory {
    
    /**
     * 荐购ID
     */
    private Integer recId;
    
    /**
     * 用户卡号
     */
    private String cardNum;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 用户类型
     */
    private String userType;
    
    /**
     * 书名
     */
    private String bookTitle;
    
    /**
     * 作者
     */
    private String bookAuthor;
    
    /**
     * 出版社
     */
    private String bookPublisher;
    
    /**
     * ISBN
     */
    private String bookIsbn;
    
    /**
     * 分类代码
     */
    private String bookCategory;
    
    /**
     * 分类名称
     */
    private String bookCategoryName;
    
    /**
     * 推荐数量
     */
    private Integer recommendQty;
    
    /**
     * 荐购理由
     */
    private String recommendReason;
    
    /**
     * 荐购时间
     */
    private LocalDateTime recommendTime;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDescription;
    
    /**
     * 管理员反馈
     */
    private String adminFeedback;
    
    /**
     * 处理管理员
     */
    private String adminCardNum;
    
    /**
     * 处理管理员姓名
     */
    private String adminName;
    
    /**
     * 处理时间
     */
    private LocalDateTime processTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 获取状态显示文本
     */
    public String getDisplayStatus() {
        if (statusDescription != null && !statusDescription.isEmpty()) {
            return statusDescription;
        }
        
        switch (status) {
            case "PENDING":
                return "待审核";
            case "APPROVED":
                return "已通过";
            case "REJECTED":
                return "已拒绝";
            case "PURCHASED":
                return "已采购";
            default:
                return "未知状态";
        }
    }
    
    /**
     * 检查是否已处理
     */
    public boolean isProcessed() {
        return !"PENDING".equals(status);
    }
    
    /**
     * 检查是否有管理员反馈
     */
    public boolean hasAdminFeedback() {
        return adminFeedback != null && !adminFeedback.trim().isEmpty();
    }
    
    /**
     * 获取处理时间描述
     */
    public String getProcessTimeDescription() {
        if (processTime == null) {
            return "未处理";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.temporal.ChronoUnit.DAYS.between(processTime, now);
        
        if (days == 0) {
            return "今天处理";
        } else if (days == 1) {
            return "昨天处理";
        } else if (days < 7) {
            return days + "天前处理";
        } else if (days < 30) {
            return (days / 7) + "周前处理";
        } else {
            return (days / 30) + "个月前处理";
        }
    }
}
