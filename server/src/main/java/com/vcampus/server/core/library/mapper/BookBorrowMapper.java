package com.vcampus.server.core.library.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.vcampus.server.core.library.entity.core.BookBorrow;
import com.vcampus.server.core.library.entity.result.BorrowResult;
import com.vcampus.server.core.library.entity.result.OverdueCheckResult;
import com.vcampus.server.core.library.entity.result.RenewResult;
import com.vcampus.server.core.library.entity.result.ReturnResult;
import com.vcampus.server.core.library.entity.view.OverdueDetails;
import com.vcampus.server.core.library.entity.view.RecentBorrow;
import com.vcampus.server.core.library.entity.view.UserBorrowHistory;

/**
 * 图书借阅记录数据访问接口 - 使用MyBatis
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface BookBorrowMapper {
    
    // ==================== 基础CRUD操作 ====================
    
    /**
     * 根据ID查找借阅记录
     */
    BookBorrow findById(@Param("transId") Integer transId);
    
    /**
     * 查找所有借阅记录
     */
    List<BookBorrow> findAll();
    
    /**
     * 根据图书ID查找借阅记录
     */
    List<BookBorrow> findByBookId(@Param("bookId") Integer bookId);
    
    /**
     * 根据用户卡号查找借阅记录
     */
    List<BookBorrow> findByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 根据状态查找借阅记录
     */
    List<BookBorrow> findByStatus(@Param("status") String status);
    
    /**
     * 插入借阅记录
     */
    int insert(BookBorrow bookBorrow);
    
    /**
     * 更新借阅记录
     */
    int update(BookBorrow bookBorrow);
    
    /**
     * 根据ID删除借阅记录
     */
    int deleteById(@Param("transId") Integer transId);
    
    /**
     * 检查借阅记录是否存在
     */
    boolean existsById(@Param("transId") Integer transId);
    
    /**
     * 统计借阅记录数量
     */
    long count();
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 查询用户当前借阅中的图书
     */
    List<BookBorrow> findCurrentBorrowsByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 查询用户已归还的图书
     */
    List<BookBorrow> findReturnedBorrowsByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 查询用户逾期的图书
     */
    List<BookBorrow> findOverdueBorrowsByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 查询所有逾期的借阅记录
     */
    List<BookBorrow> findAllOverdueBorrows();
    
    /**
     * 查询图书当前借阅中的记录
     */
    List<BookBorrow> findCurrentBorrowsByBookId(@Param("bookId") Integer bookId);
    
    /** 查询图书的借阅历史 */
    List<BookBorrow> findBorrowHistoryByBookId(@Param("bookId") Integer bookId);
    
    /** 查询图书的借阅历史（包含用户姓名） */
    List<Map<String, Object>> findBorrowHistoryByBookIdWithUserName(@Param("bookId") Integer bookId);
    
    /**
     * 查询用户借阅历史
     */
    List<BookBorrow> findBorrowHistoryByUser(@Param("cardNum") String cardNum);
    
    /**
     * 查询逾期记录
     */
    List<BookBorrow> findOverdueRecords();
    
    /**
     * 查询即将到期的记录
     */
    List<BookBorrow> findExpiringRecords(@Param("days") int days);
    
    /**
     * 查询最近借阅记录
     */
    List<BookBorrow> findRecentBorrows(@Param("limit") int limit);
    
    /** 统计用户借阅数量 */
    long countBorrowsByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 统计用户当前借阅数量
     */
    long countCurrentBorrowsByCardNum(@Param("cardNum") String cardNum);
    
    /** 统计图书借阅次数 */
    long countBorrowsByBookId(@Param("bookId") Integer bookId);
    
    /**
     * 统计逾期数量
     */
    long countOverdueRecords();
    
    /**
     * 检查用户是否已借阅某本图书
     */
    boolean isBookBorrowedByUser(@Param("bookId") Integer bookId, @Param("cardNum") String cardNum);
    
    /**
     * 更新状态
     */
    int updateStatus(@Param("transId") Integer transId, @Param("status") String status);
    
    /**
     * 更新归还时间
     */
    int updateReturnTime(@Param("transId") Integer transId, @Param("returnTime") LocalDateTime returnTime);
    
    /**
     * 更新归还信息
     */
    int updateReturnInfo(@Param("transId") Integer transId, 
                        @Param("returnTime") LocalDateTime returnTime, 
                        @Param("status") String status);
    
    /**
     * 更新续借信息
     */
    int updateRenewInfo(@Param("transId") Integer transId, 
                       @Param("newDueTime") LocalDateTime newDueTime, 
                       @Param("newRenewCount") int newRenewCount);
    
    // ==================== 存储过程调用方法 ====================
    
    /**
     * 借阅图书 - 调用存储过程 sp_borrow_book
     */
    BorrowResult borrowBook(@Param("bookId") Integer bookId, 
                           @Param("cardNum") String cardNum, 
                           @Param("borrowDays") Integer borrowDays);
    
    /**
     * 归还图书 - 调用存储过程 sp_return_book
     */
    ReturnResult returnBook(@Param("transId") Integer transId);
    
    /**
     * 续借图书 - 调用存储过程 sp_renew_book
     */
    RenewResult renewBook(@Param("transId") Integer transId, 
                         @Param("extendDays") Integer extendDays);
    
    /**
     * 逾期检查 - 调用存储过程 sp_check_overdue
     */
    OverdueCheckResult checkOverdue();
    
    // ==================== 视图查询方法 ====================
    
    /**
     * 查询用户借阅历史 - 使用视图 v_user_borrow_history
     */
    List<UserBorrowHistory> getUserBorrowHistory(@Param("cardNum") String cardNum);
    
    /**
     * 查询所有用户借阅历史 - 使用视图 v_user_borrow_history
     */
    List<UserBorrowHistory> getAllUserBorrowHistory();
    
    /**
     * 查询逾期详情 - 使用视图 v_overdue_details
     */
    List<OverdueDetails> getOverdueDetails();
    
    /**
     * 查询最近借阅记录 - 使用视图 v_recent_borrows
     */
    List<RecentBorrow> getRecentBorrows(@Param("limit") Integer limit);
    
    /**
     * 获取指定图书的借阅次数
     */
    int getBorrowCountByBookId(@Param("bookId") int bookId);
    
    /**
     * 获取今日借阅数量
     */
    int getTodayBorrowCount(@Param("today") String today);
    
    /**
     * 获取今日归还数量
     */
    int getTodayReturnCount(@Param("today") String today);
    
    /**
     * 获取今日逾期数量
     */
    int getTodayOverdueCount(@Param("today") String today);
    
    /**
     * 获取最近7天的借阅趋势数据
     */
    Map<String, Double> getBorrowTrendLast7Days();
    
    /**
     * 获取用户借阅排名
     * 按借阅数量降序排列，最多返回前10名
     */
    List<Map<String, Object>> getUserBorrowRanking();
    
    /**
     * 获取各分类借阅统计
     * 统计每个分类的历史总借阅次数
     */
    List<Map<String, Object>> getCategoryBorrowStatistics();
}