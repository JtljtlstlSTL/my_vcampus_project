package com.vcampus.server.core.library.entity.result;

import com.vcampus.server.core.library.entity.core.BookBorrow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 续借结果实体类 - 对应存储过程 sp_renew_book 返回结果
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewResult {
    private boolean success; // 是否成功
    private String message; // 结果信息
    private BookBorrow borrow; // 续借记录
    private Integer transId; // 借阅记录ID
    private java.time.LocalDateTime newDueTime; // 新的应还时间
    
    // 静态工厂方法
    public static RenewResult success(String message, BookBorrow borrow) {
        return RenewResult.builder()
                .success(true)
                .message(message)
                .borrow(borrow)
                .transId(borrow != null ? borrow.getTransId() : null)
                .newDueTime(borrow != null ? borrow.getDueTime() : null)
                .build();
    }
    
    public static RenewResult failure(String message) {
        return RenewResult.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    public static RenewResult failure(String message, BookBorrow borrow) {
        return RenewResult.builder()
                .success(false)
                .message(message)
                .borrow(borrow)
                .transId(borrow != null ? borrow.getTransId() : null)
                .build();
    }
}
