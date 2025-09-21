package com.vcampus.server.core.library.entity.core;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 我的书架实体类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyBookshelf {
    
    /**
     * 书架ID
     */
    private Integer shelfId;
    
    /**
     * 用户卡号
     */
    private String cardNum;
    
    /**
     * 图书ID
     */
    private Integer bookId;
    
    /**
     * 自定义分类名称
     */
    private String categoryName;
    
    /**
     * 添加时间
     */
    private LocalDateTime addTime;
    
    /**
     * 个人备注
     */
    private String notes;
    
    // ==================== 业务方法 ====================
    
    /**
     * 检查是否有备注
     */
    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
    
    /**
     * 获取备注内容（如果为空则返回默认值）
     */
    public String getNotesOrDefault() {
        return hasNotes() ? notes : "暂无备注";
    }
    
    /**
     * 检查分类名称是否有效
     */
    public boolean hasValidCategory() {
        return categoryName != null && !categoryName.trim().isEmpty();
    }
    
    /**
     * 获取分类名称（如果为空则返回默认值）
     */
    public String getCategoryNameOrDefault() {
        return hasValidCategory() ? categoryName : "未分类";
    }
    
    /**
     * 获取添加时间描述
     */
    public String getAddTimeDescription() {
        if (addTime == null) {
            return "未知时间";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.temporal.ChronoUnit.DAYS.between(addTime, now);
        
        if (days == 0) {
            return "今天";
        } else if (days == 1) {
            return "昨天";
        } else if (days < 7) {
            return days + "天前";
        } else if (days < 30) {
            return (days / 7) + "周前";
        } else if (days < 365) {
            return (days / 30) + "个月前";
        } else {
            return (days / 365) + "年前";
        }
    }
}
