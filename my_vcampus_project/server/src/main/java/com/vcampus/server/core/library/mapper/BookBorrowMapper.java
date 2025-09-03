package com.vcampus.server.core.library.mapper;

import com.vcampus.server.core.library.entity.BookBorrow;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 图书借阅记录数据访问接口 - 使用MyBatis
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface BookBorrowMapper {
    
    /**
     * 根据ID查找借阅记录
     */
    BookBorrow findById(@Param("transId") Integer transId);
    
    /**
     * 查找所有借阅记录
     */
    List<BookBorrow> findAll();
    
    /**
     * 根据用户卡号查询借阅记录
     */
    List<BookBorrow> findByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 根据图书ID查询借阅记录
     */
    List<BookBorrow> findByBookId(@Param("bookId") Integer bookId);
    
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
    
    /**
     * 查询图书的借阅历史
     */
    List<BookBorrow> findBorrowHistoryByBookId(@Param("bookId") Integer bookId);
    
    /**
     * 查询最近借阅记录
     */
    List<BookBorrow> findRecentBorrows(@Param("limit") int limit);
    
    /**
     * 插入借阅记录
     */
    int insert(BookBorrow borrow);
    
    /**
     * 更新借阅记录
     */
    int update(BookBorrow borrow);
    
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
    
    /**
     * 统计用户借阅数量
     */
    long countBorrowsByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 统计用户当前借阅数量
     */
    long countCurrentBorrowsByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 统计图书借阅次数
     */
    long countBorrowsByBookId(@Param("bookId") Integer bookId);
    
    /**
     * 检查用户是否已借阅某本图书
     */
    boolean isBookBorrowedByUser(@Param("bookId") Integer bookId, @Param("cardNum") String cardNum);
    
    /**
     * 更新借阅记录状态
     */
    int updateStatus(@Param("transId") Integer transId, @Param("status") String status);
    
    /**
     * 更新归还时间
     */
    int updateReturnTime(@Param("transId") Integer transId, @Param("returnTime") LocalDateTime returnTime);
    
    /**
     * 更新续借信息
     */
    int updateRenewInfo(@Param("transId") Integer transId, 
                       @Param("newDueTime") LocalDateTime newDueTime, 
                       @Param("newRenewCount") int newRenewCount);
}
