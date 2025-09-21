package com.vcampus.server.core.library.entity.result;

import com.vcampus.server.core.library.entity.core.BookRecommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 荐购操作结果类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 结果信息
     */
    private String message;
    
    /**
     * 荐购记录
     */
    private BookRecommendation recommendation;
    
    /**
     * 荐购ID
     */
    private Integer recId;
    
    // ==================== 静态工厂方法 ====================
    
    /**
     * 创建成功结果
     */
    public static RecommendResult success(String message, BookRecommendation recommendation) {
        return RecommendResult.builder()
                .success(true)
                .message(message)
                .recommendation(recommendation)
                .recId(recommendation != null ? recommendation.getRecId() : null)
                .build();
    }
    
    /**
     * 创建失败结果
     */
    public static RecommendResult failure(String message) {
        return RecommendResult.builder()
                .success(false)
                .message(message)
                .recommendation(null)
                .recId(null)
                .build();
    }
    
    /**
     * 创建成功结果（仅消息）
     */
    public static RecommendResult success(String message) {
        return RecommendResult.builder()
                .success(true)
                .message(message)
                .recommendation(null)
                .recId(null)
                .build();
    }
    
    /**
     * 创建成功结果（带ID）
     */
    public static RecommendResult success(String message, Integer recId) {
        return RecommendResult.builder()
                .success(true)
                .message(message)
                .recommendation(null)
                .recId(recId)
                .build();
    }
}
