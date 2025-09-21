package com.vcampus.server.core.library.entity.result;

import com.vcampus.server.core.library.entity.core.MyBookshelf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 书架操作结果类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookshelfResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 结果信息
     */
    private String message;
    
    /**
     * 书架记录
     */
    private MyBookshelf bookshelf;
    
    /**
     * 书架ID
     */
    private Integer shelfId;
    
    // ==================== 静态工厂方法 ====================
    
    /**
     * 创建成功结果
     */
    public static BookshelfResult success(String message, MyBookshelf bookshelf) {
        return BookshelfResult.builder()
                .success(true)
                .message(message)
                .bookshelf(bookshelf)
                .shelfId(bookshelf != null ? bookshelf.getShelfId() : null)
                .build();
    }
    
    /**
     * 创建失败结果
     */
    public static BookshelfResult failure(String message) {
        return BookshelfResult.builder()
                .success(false)
                .message(message)
                .bookshelf(null)
                .shelfId(null)
                .build();
    }
    
    /**
     * 创建成功结果（仅消息）
     */
    public static BookshelfResult success(String message) {
        return BookshelfResult.builder()
                .success(true)
                .message(message)
                .bookshelf(null)
                .shelfId(null)
                .build();
    }
    
    /**
     * 创建成功结果（带ID）
     */
    public static BookshelfResult success(String message, Integer shelfId) {
        return BookshelfResult.builder()
                .success(true)
                .message(message)
                .bookshelf(null)
                .shelfId(shelfId)
                .build();
    }
}
