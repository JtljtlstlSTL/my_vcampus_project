package com.vcampus.server.core.library.dao;

import com.vcampus.server.core.library.entity.core.BookBorrow;
import com.vcampus.server.core.library.entity.view.UserBorrowHistory;
import com.vcampus.server.core.library.entity.view.OverdueDetails;
import com.vcampus.server.core.library.entity.view.RecentBorrow;
import com.vcampus.server.core.library.entity.result.BorrowResult;
import com.vcampus.server.core.library.entity.result.ReturnResult;
import com.vcampus.server.core.library.entity.result.RenewResult;
import com.vcampus.server.core.library.entity.result.OverdueCheckResult;
import com.vcampus.server.core.library.mapper.BookBorrowMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * 图书借阅记录数据访问对象 - 使用MyBatis
 * 提供借阅记录的CRUD操作和业务查询方法
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class BookBorrowDao {
    
    private static BookBorrowDao instance;
    private final SqlSessionFactory sqlSessionFactory;
    
    private BookBorrowDao() {
        // 初始化MyBatis SqlSessionFactory
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mybatis-config.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("初始化MyBatis失败", e);
        }
    }
    
    public static synchronized BookBorrowDao getInstance() {
        if (instance == null) {
            instance = new BookBorrowDao();
        }
        return instance;
    }
    
    // ==================== 基础CRUD方法 ====================
    
    /**
     * 根据ID查找借阅记录
     */
    public Optional<BookBorrow> findById(Integer transId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            BookBorrow borrow = mapper.findById(transId);
            return Optional.ofNullable(borrow);
        }
    }
    
    /**
     * 查找所有借阅记录
     */
    public List<BookBorrow> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findAll();
        }
    }
    
    /**
     * 保存借阅记录（新增或更新）
     */
    public BookBorrow save(BookBorrow borrow) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            if (borrow.getTransId() == null) {
                mapper.insert(borrow);
            } else {
                mapper.update(borrow);
            }
            session.commit();
            return borrow;
        }
    }
    
    /**
     * 根据ID删除借阅记录
     */
    public void deleteById(Integer transId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            mapper.deleteById(transId);
            session.commit();
        }
    }
    
    /**
     * 检查借阅记录是否存在
     */
    public boolean existsById(Integer transId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.existsById(transId);
        }
    }
    
    /**
     * 统计借阅记录数量
     */
    public long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.count();
        }
    }
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 根据用户卡号查询借阅记录
     */
    public List<BookBorrow> findByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findByCardNum(cardNum);
        }
    }
    
    /**
     * 根据图书ID查询借阅记录
     */
    public List<BookBorrow> findByBookId(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findByBookId(bookId);
        }
    }
    
    /**
     * 查询用户当前借阅中的图书
     */
    public List<BookBorrow> findCurrentBorrowsByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findCurrentBorrowsByCardNum(cardNum);
        }
    }
    
    /**
     * 查询用户已归还的图书
     */
    public List<BookBorrow> findReturnedBorrowsByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findReturnedBorrowsByCardNum(cardNum);
        }
    }
    
    /**
     * 查询用户逾期的图书
     */
    public List<BookBorrow> findOverdueBorrowsByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findOverdueBorrowsByCardNum(cardNum);
        }
    }
    
    /**
     * 查询所有逾期的借阅记录
     */
    public List<BookBorrow> findAllOverdueBorrows() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findAllOverdueBorrows();
        }
    }
    
    /**
     * 查询图书当前借阅中的记录
     */
    public List<BookBorrow> findCurrentBorrowsByBookId(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findCurrentBorrowsByBookId(bookId);
        }
    }
    
    /**
     * 查询图书的借阅历史
     */
    public List<BookBorrow> findBorrowHistoryByBookId(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findBorrowHistoryByBookId(bookId);
        }
    }
    
    /**
     * 查询图书的借阅历史（包含用户姓名）
     */
    public List<Map<String, Object>> findBorrowHistoryByBookIdWithUserName(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findBorrowHistoryByBookIdWithUserName(bookId);
        }
    }
    
    /**
     * 查询最近借阅记录
     */
    public List<BookBorrow> findRecentBorrows(int limit) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.findRecentBorrows(limit);
        }
    }
    
    /**
     * 统计用户借阅数量
     */
    public long countBorrowsByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.countBorrowsByCardNum(cardNum);
        }
    }
    
    /**
     * 统计用户当前借阅数量
     */
    public long countCurrentBorrowsByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.countCurrentBorrowsByCardNum(cardNum);
        }
    }
    
    /**
     * 统计图书借阅次数
     */
    public long countBorrowsByBookId(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.countBorrowsByBookId(bookId);
        }
    }
    
    /**
     * 检查用户是否已借阅某本图书
     */
    public boolean isBookBorrowedByUser(Integer bookId, String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.isBookBorrowedByUser(bookId, cardNum);
        }
    }
    
    /**
     * 更新借阅记录状态
     */
    public void updateStatus(Integer transId, com.vcampus.server.core.library.enums.BorrowStatus status) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            int affected = mapper.updateStatus(transId, status.name());
            if (affected == 0) {
                throw new RuntimeException("更新状态失败：未找到ID为 " + transId + " 的借阅记录");
            }
            session.commit();
        }
    }
    
    /**
     * 更新归还时间
     */
    public void updateReturnTime(Integer transId, LocalDateTime returnTime) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            int affected = mapper.updateReturnTime(transId, returnTime);
            if (affected == 0) {
                throw new RuntimeException("更新归还时间失败：未找到ID为 " + transId + " 的借阅记录");
            }
            session.commit();
        }
    }
    
    /**
     * 更新续借信息
     */
    public void updateRenewInfo(Integer transId, LocalDateTime newDueTime, int newRenewCount) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            int affected = mapper.updateRenewInfo(transId, newDueTime, newRenewCount);
            if (affected == 0) {
                throw new RuntimeException("更新续借信息失败：未找到ID为 " + transId + " 的借阅记录");
            }
            session.commit();
        }
    }
    
    // ==================== 存储过程调用方法 ====================
    
    /**
     * 借阅图书 - 调用存储过程 sp_borrow_book
     * @param bookId 图书ID
     * @param cardNum 用户卡号
     * @param borrowDays 借阅天数
     * @return 借阅结果信息
     */
    public BorrowResult borrowBook(Integer bookId, String cardNum, Integer borrowDays) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.borrowBook(bookId, cardNum, borrowDays);
        }
    }
    
    /**
     * 归还图书 - 调用存储过程 sp_return_book
     * @param transId 借阅记录ID
     * @return 归还结果信息
     */
    public ReturnResult returnBook(Integer transId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.returnBook(transId);
        }
    }
    
    /**
     * 续借图书 - 调用存储过程 sp_renew_book
     * @param transId 借阅记录ID
     * @param extendDays 续借天数
     * @return 续借结果信息
     */
    public RenewResult renewBook(Integer transId, Integer extendDays) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.renewBook(transId, extendDays);
        }
    }
    
    /**
     * 逾期检查 - 调用存储过程 sp_check_overdue
     * @return 逾期检查结果
     */
    public OverdueCheckResult checkOverdue() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.checkOverdue();
        }
    }
    
    // ==================== 视图查询方法 ====================
    
    /**
     * 查询用户借阅历史 - 使用视图 v_user_borrow_history
     * @param cardNum 用户卡号
     * @return 用户借阅历史列表
     */
    public List<UserBorrowHistory> getUserBorrowHistory(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getUserBorrowHistory(cardNum);
        }
    }
    
    /**
     * 查询所有用户借阅历史 - 使用视图 v_user_borrow_history
     * @return 所有用户借阅历史列表
     */
    public List<UserBorrowHistory> getAllUserBorrowHistory() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getAllUserBorrowHistory();
        }
    }
    
    /**
     * 查询逾期详情 - 使用视图 v_overdue_details
     * @return 逾期详情列表
     */
    public List<OverdueDetails> getOverdueDetails() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getOverdueDetails();
        }
    }
    
    /**
     * 查询最近借阅记录 - 使用视图 v_recent_borrows
     * @param limit 限制数量
     * @return 最近借阅记录列表
     */
    public List<RecentBorrow> getRecentBorrows(Integer limit) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getRecentBorrows(limit);
        }
    }
    
    /**
     * 获取指定图书的借阅次数
     * @param bookId 图书ID
     * @return 借阅次数
     */
    public int getBorrowCountByBookId(int bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getBorrowCountByBookId(bookId);
        }
    }
    
    /**
     * 获取今日借阅数量
     * @param today 今日日期
     * @return 今日借阅数量
     */
    public int getTodayBorrowCount(String today) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getTodayBorrowCount(today);
        }
    }
    
    /**
     * 获取今日归还数量
     * @param today 今日日期
     * @return 今日归还数量
     */
    public int getTodayReturnCount(String today) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getTodayReturnCount(today);
        }
    }
    
    /**
     * 获取今日逾期数量
     * @param today 今日日期
     * @return 今日逾期数量
     */
    public int getTodayOverdueCount(String today) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getTodayOverdueCount(today);
        }
    }
    
    /**
     * 获取最近7天的借阅趋势数据
     * @return 最近7天的借阅趋势数据
     */
    public Map<String, Double> getBorrowTrendLast7Days() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getBorrowTrendLast7Days();
        }
    }
    
    /**
     * 获取用户借阅排名
     * 
     * @return 用户借阅排名列表，按借阅数量降序排列，最多返回前10名
     */
    public List<Map<String, Object>> getUserBorrowRanking() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getUserBorrowRanking();
        }
    }
    
    /**
     * 获取各分类借阅统计
     */
    public List<Map<String, Object>> getCategoryBorrowStatistics() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookBorrowMapper mapper = session.getMapper(BookBorrowMapper.class);
            return mapper.getCategoryBorrowStatistics();
        }
    }
    

}