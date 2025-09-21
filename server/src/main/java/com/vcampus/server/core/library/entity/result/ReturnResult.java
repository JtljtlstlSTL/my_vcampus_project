package com.vcampus.server.core.library.entity.result;

import com.vcampus.server.core.library.entity.core.BookBorrow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 归还结果实体类 - 对应存储过程 sp_return_book 返回结果
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResult {
    private boolean success; // 是否成功
    private String message; // 结果信息
    private BookBorrow borrow; // 归还记录
    private Integer transId; // 借阅记录ID
    private Integer overdueDays; // 逾期天数
    
    // 静态工厂方法
    public static ReturnResult success(String message, BookBorrow borrow) {
        return ReturnResult.builder()
                .success(true)
                .message(message)
                .borrow(borrow)
                .transId(borrow != null ? borrow.getTransId() : null)
                .build();
    }
    
    public static ReturnResult failure(String message) {
        return ReturnResult.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    public static ReturnResult failure(String message, BookBorrow borrow) {
        return ReturnResult.builder()
                .success(false)
                .message(message)
                .borrow(borrow)
                .transId(borrow != null ? borrow.getTransId() : null)
                .build();
    }
}
