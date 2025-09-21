package com.vcampus.server.core.library.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.vcampus.server.core.library.entity.core.BookBorrow;
import com.vcampus.server.core.library.entity.result.BorrowResult;
import com.vcampus.server.core.library.entity.result.RenewResult;
import com.vcampus.server.core.library.entity.result.ReturnResult;
import com.vcampus.server.core.library.entity.view.OverdueDetails;
import com.vcampus.server.core.library.entity.view.RecentBorrow;
import com.vcampus.server.core.library.entity.view.UserBorrowHistory;
import com.vcampus.server.core.library.enums.UserType;

/**
 * 图书借还书管理服务接口
 * 提供图书借阅、归还、续借等核心业务功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface LibraryBorrowService {
    
    // ==================== 借阅管理 ====================
    
    /**
     * 借阅图书
     * @param bookId 图书ID
     * @param cardNum 用户卡号
     * @param userType 用户类型
     * @return 借阅结果
     */
    BorrowResult borrowBook(Integer bookId, String cardNum, UserType userType);
    
    /**
     * 归还图书
     * @param transId 借阅记录ID
     * @param cardNum 用户卡号（用于权限验证）
     * @return 归还结果
     */
    ReturnResult returnBook(Integer transId, String cardNum);
    
    /**
     * 续借图书
     * @param transId 借阅记录ID
     * @param cardNum 用户卡号（用于权限验证）
     * @return 续借结果
     */
    RenewResult renewBook(Integer transId, String cardNum);
    
    // ==================== 查询功能 ====================
    
    /**
     * 获取用户当前借阅的图书
     * @param cardNum 用户卡号
     * @return 当前借阅列表
     */
    List<BookBorrow> getCurrentBorrows(String cardNum);
    
    /**
     * 获取用户借阅历史
     * @param cardNum 用户卡号
     * @return 借阅历史列表
     */
    List<UserBorrowHistory> getBorrowHistory(String cardNum);
    
    /**
     * 获取用户逾期记录
     * @param cardNum 用户卡号
     * @return 逾期记录列表
     */
    List<BookBorrow> getOverdueBorrows(String cardNum);
    
    /**
     * 获取图书借阅历史
     * @param bookId 图书ID
     * @return 图书借阅历史
     */
    List<BookBorrow> getBookBorrowHistory(Integer bookId);
    
    /**
     * 获取图书借阅历史（包含用户姓名）
     * @param bookId 图书ID
     * @return 图书借阅历史（包含用户姓名）
     */
    List<Map<String, Object>> getBookBorrowHistoryWithUserName(Integer bookId);
    
    /**
     * 获取最近借阅记录
     * @param limit 限制数量
     * @return 最近借阅记录
     */
    List<RecentBorrow> getRecentBorrows(Integer limit);
    
    /**
     * 获取所有逾期详情（管理员功能）
     * @return 逾期详情列表
     */
    List<OverdueDetails> getAllOverdueDetails();
    
    // ==================== 验证功能 ====================
    
    /**
     * 检查用户是否可以借阅图书
     * @param cardNum 用户卡号
     * @param userType 用户类型
     * @param bookId 图书ID
     * @return 是否可以借阅
     */
    boolean canBorrowBook(String cardNum, UserType userType, Integer bookId);
    
    /**
     * 检查用户是否可以续借图书
     * @param transId 借阅记录ID
     * @param cardNum 用户卡号
     * @return 是否可以续借
     */
    boolean canRenewBook(Integer transId, String cardNum);
    
    /**
     * 检查用户是否可以归还图书
     * @param transId 借阅记录ID
     * @param cardNum 用户卡号
     * @return 是否可以归还
     */
    boolean canReturnBook(Integer transId, String cardNum);
    
    // ==================== 统计功能 ====================
    
    /**
     * 获取用户借阅统计
     * @param cardNum 用户卡号
     * @return 借阅统计信息
     */
    BorrowStatistics getUserBorrowStatistics(String cardNum);
    
    /**
     * 获取图书借阅统计
     * @param bookId 图书ID
     * @return 图书借阅统计
     */
    BookBorrowStatistics getBookBorrowStatistics(Integer bookId);
    
    // ==================== 内部类 ====================
    
    /**
     * 借阅统计信息
     */
    class BorrowStatistics {
        private final long totalBorrows;      // 总借阅次数
        private final long currentBorrows;    // 当前借阅数量
        private final long overdueCount;      // 逾期数量
        private final long totalRenews;       // 总续借次数
        
        public BorrowStatistics(long totalBorrows, long currentBorrows, long overdueCount, long totalRenews) {
            this.totalBorrows = totalBorrows;
            this.currentBorrows = currentBorrows;
            this.overdueCount = overdueCount;
            this.totalRenews = totalRenews;
        }
        
        // Getters
        public long getTotalBorrows() { return totalBorrows; }
        public long getCurrentBorrows() { return currentBorrows; }
        public long getOverdueCount() { return overdueCount; }
        public long getTotalRenews() { return totalRenews; }
    }
    
    /**
     * 图书借阅统计信息
     */
    class BookBorrowStatistics {
        private final long totalBorrows;      // 总借阅次数
        private final long currentBorrows;    // 当前借出数量
        private final long availableCount;    // 可借数量
        private final LocalDateTime lastBorrowTime; // 最后借阅时间
        
        public BookBorrowStatistics(long totalBorrows, long currentBorrows, long availableCount, LocalDateTime lastBorrowTime) {
            this.totalBorrows = totalBorrows;
            this.currentBorrows = currentBorrows;
            this.availableCount = availableCount;
            this.lastBorrowTime = lastBorrowTime;
        }
        
        // Getters
        public long getTotalBorrows() { return totalBorrows; }
        public long getCurrentBorrows() { return currentBorrows; }
        public long getAvailableCount() { return availableCount; }
        public LocalDateTime getLastBorrowTime() { return lastBorrowTime; }
    }
}
