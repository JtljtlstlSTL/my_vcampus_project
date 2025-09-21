package com.vcampus.server.core.library.entity.core;

import java.time.LocalDateTime;

import com.vcampus.server.core.library.enums.BorrowStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书借阅记录实体类 - 对应数据库表 tblBook_trans
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookBorrow {
    private Integer transId; // 借还记录ID - 主键，自增
    private Integer bookId; // 图书ID - 外键
    private String cardNum; // 一卡通号 - 外键
    private LocalDateTime borrowTime; // 借出时间
    private LocalDateTime returnTime; // 归还时间
    private LocalDateTime dueTime; // 应还时间
    private BorrowStatus status; // 借阅状态
    private Integer renewCount; // 续借次数
    private String remarks; // 备注信息
    
    // 静态工厂方法，创建借阅记录
    public static BookBorrow createBorrow(Integer bookId, String cardNum, LocalDateTime dueTime) {
        return BookBorrow.builder()
                .bookId(bookId)
                .cardNum(cardNum)
                .borrowTime(LocalDateTime.now())
                .dueTime(dueTime)
                .status(BorrowStatus.BORROWED)
                .renewCount(0)
                .build();
    }
    
    // 业务方法
    public boolean isOverdue() { // 判断是否已逾期
        return dueTime != null && LocalDateTime.now().isAfter(dueTime) && 
               (status == BorrowStatus.BORROWED || status == BorrowStatus.OVERDUE);
    }
    
    public long getOverdueDays() { // 计算逾期天数
        if (!isOverdue()) {
            return 0;
        }
        return java.time.Duration.between(dueTime, LocalDateTime.now()).toDays();
    }
    
    public long getBorrowDays() { // 计算借阅天数
        if (borrowTime == null) {
            return 0;
        }
        
        LocalDateTime endTime = returnTime != null ? returnTime : LocalDateTime.now();
        return java.time.Duration.between(borrowTime, endTime).toDays();
    }
    
    public boolean canRenew(int maxRenewCount) { // 判断是否可以续借
        return status == BorrowStatus.BORROWED && 
               !isOverdue() && 
               renewCount < maxRenewCount;
    }
    
    public void renew(int extendDays) { // 执行续借操作
        if (canRenew(Integer.MAX_VALUE)) { // 这里应该传入实际的最大续借次数
            this.dueTime = this.dueTime.plusDays(extendDays);
            this.renewCount++;
        }
    }
    
    public void returnBook() { // 执行归还操作
        this.returnTime = LocalDateTime.now();
        this.status = isOverdue() ? BorrowStatus.OVERDUE : BorrowStatus.RETURNED;
    }
}
