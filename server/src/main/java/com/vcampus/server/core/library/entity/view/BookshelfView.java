package com.vcampus.server.core.library.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 我的书架视图实体类
 * 用于展示用户书架中的图书详细信息
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookshelfView {
    
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
     * 图书标题
     */
    private String bookTitle;
    
    /**
     * 图书作者
     */
    private String bookAuthor;
    
    /**
     * 图书出版社
     */
    private String bookPublisher;
    
    /**
     * 图书ISBN
     */
    private String bookIsbn;
    
    /**
     * 图书分类代码
     */
    private String bookCategory;
    
    /**
     * 图书分类名称
     */
    private String bookCategoryName;
    
    /**
     * 馆藏位置
     */
    private String location;
    
    /**
     * 总数量
     */
    private Integer totalQty;
    
    /**
     * 可借数量
     */
    private Integer availQty;
    
    /**
     * 图书状态
     */
    private String bookStatus;
    
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
     * 检查图书是否可借
     */
    public boolean isBookAvailable() {
        return "IN_LIBRARY".equals(bookStatus) && availQty != null && availQty > 0;
    }
    
    /**
     * 获取图书状态描述
     */
    public String getBookStatusDescription() {
        if ("IN_LIBRARY".equals(bookStatus)) {
            return "在库";
        } else if ("BORROWED".equals(bookStatus)) {
            return "已借出";
        } else {
            return "未知状态";
        }
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
}