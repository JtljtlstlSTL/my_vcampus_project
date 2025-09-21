package com.vcampus.server.core.library.entity.core;

import com.vcampus.server.core.library.enums.RecommendStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图书荐购实体类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRecommendation {
    
    /**
     * 荐购ID
     */
    private Integer recId;
    
    /**
     * 荐购用户卡号
     */
    private String cardNum;
    
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
     * 推荐购买数量
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
    private RecommendStatus status;
    
    /**
     * 管理员反馈
     */
    private String adminFeedback;
    
    /**
     * 处理管理员卡号
     */
    private String adminCardNum;
    
    /**
     * 处理时间
     */
    private LocalDateTime processTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 检查是否可以处理
     */
    public boolean canProcess() {
        return status == RecommendStatus.PENDING;
    }
    
    /**
     * 检查是否已处理
     */
    public boolean isProcessed() {
        return status != RecommendStatus.PENDING;
    }
    
    /**
     * 检查是否已通过
     */
    public boolean isApproved() {
        return status == RecommendStatus.APPROVED;
    }
    
    /**
     * 检查是否已拒绝
     */
    public boolean isRejected() {
        return status == RecommendStatus.REJECTED;
    }
    
    /**
     * 检查是否已采购
     */
    public boolean isPurchased() {
        return status == RecommendStatus.PURCHASED;
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "未知";
    }
    
    /**
     * 获取处理状态描述
     */
    public String getProcessStatusDescription() {
        if (status == null) {
            return "未知";
        }
        
        switch (status) {
            case PENDING:
                return "等待审核";
            case APPROVED:
                return "审核通过";
            case REJECTED:
                return "审核拒绝";
            case PURCHASED:
                return "已采购";
            default:
                return "未知状态";
        }
    }
}
