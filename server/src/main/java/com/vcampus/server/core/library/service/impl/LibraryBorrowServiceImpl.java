package com.vcampus.server.core.library.service.impl;

import com.vcampus.server.core.library.dao.BookDao;
import com.vcampus.server.core.library.dao.BookBorrowDao;
import com.vcampus.server.core.library.dao.BorrowRuleDao;
import com.vcampus.server.core.library.entity.core.Book;
import com.vcampus.server.core.library.entity.core.BookBorrow;
import com.vcampus.server.core.library.entity.core.BorrowRule;
import com.vcampus.server.core.library.entity.result.BorrowResult;
import com.vcampus.server.core.library.entity.result.ReturnResult;
import com.vcampus.server.core.library.entity.result.RenewResult;
import com.vcampus.server.core.library.entity.view.UserBorrowHistory;
import com.vcampus.server.core.library.entity.view.OverdueDetails;
import com.vcampus.server.core.library.entity.view.RecentBorrow;
import com.vcampus.server.core.library.enums.BorrowStatus;
import com.vcampus.server.core.library.enums.UserType;
import com.vcampus.server.core.library.service.LibraryBorrowService;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 图书借还书管理服务实现类
 * 实现图书借阅、归还、续借等核心业务逻辑
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class LibraryBorrowServiceImpl implements LibraryBorrowService {
    
    private final BookDao bookDao;
    private final BookBorrowDao bookBorrowDao;
    private final BorrowRuleDao borrowRuleDao;
    
    public LibraryBorrowServiceImpl() {
        this.bookDao = BookDao.getInstance();
        this.bookBorrowDao = BookBorrowDao.getInstance();
        this.borrowRuleDao = BorrowRuleDao.getInstance();
    }
    
    // ==================== 借阅管理 ====================
    
    @Override
    public BorrowResult borrowBook(Integer bookId, String cardNum, UserType userType) {
        log.info("开始处理借阅请求: bookId={}, cardNum={}, userType={}", bookId, cardNum, userType);
        
        try {
            // 1. 验证借阅权限
            if (!canBorrowBook(cardNum, userType, bookId)) {
                return BorrowResult.failure("借阅权限验证失败");
            }
            
            // 2. 获取借阅规则
            Optional<BorrowRule> ruleOpt = borrowRuleDao.findByUserType(userType);
            if (!ruleOpt.isPresent()) {
                return BorrowResult.failure("未找到用户类型的借阅规则");
            }
            BorrowRule rule = ruleOpt.get();
            
            // 3. 调用DAO层执行借阅操作
            BorrowResult result = bookBorrowDao.borrowBook(bookId, cardNum, rule.getMaxBorrowDays());
            
            if (result.isSuccess()) {
                log.info("借阅成功: bookId={}, cardNum={}, transId={}", 
                        bookId, cardNum, result.getTransId());
            } else {
                log.warn("借阅失败: bookId={}, cardNum={}, reason={}", 
                        bookId, cardNum, result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("借阅图书异常: bookId={}, cardNum={}", bookId, cardNum, e);
            return BorrowResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public ReturnResult returnBook(Integer transId, String cardNum) {
        log.info("开始处理归还请求: transId={}, cardNum={}", transId, cardNum);
        
        try {
            // 1. 验证归还权限
            if (!canReturnBook(transId, cardNum)) {
                return ReturnResult.failure("归还权限验证失败");
            }
            
            // 2. 调用DAO层执行归还操作
            ReturnResult result = bookBorrowDao.returnBook(transId);
            
            if (result.isSuccess()) {
                log.info("归还成功: transId={}, cardNum={}", transId, cardNum);
            } else {
                log.warn("归还失败: transId={}, cardNum={}, reason={}", 
                        transId, cardNum, result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("归还图书异常: transId={}, cardNum={}", transId, cardNum, e);
            return ReturnResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public RenewResult renewBook(Integer transId, String cardNum) {
        log.info("开始处理续借请求: transId={}, cardNum={}", transId, cardNum);
        
        try {
            // 1. 获取借阅记录
            Optional<BookBorrow> borrowOpt = bookBorrowDao.findById(transId);
            if (!borrowOpt.isPresent()) {
                return RenewResult.failure("借阅记录不存在");
            }
            
            BookBorrow borrow = borrowOpt.get();
            
            // 2. 验证借阅记录归属
            if (!borrow.getCardNum().equals(cardNum)) {
                return RenewResult.failure("无权续借此图书");
            }
            
            // 3. 检查借阅状态
            if (borrow.getStatus() != BorrowStatus.BORROWED) {
                return RenewResult.failure("只能续借已借出的图书");
            }
            
            // 4. 检查是否逾期
            if (borrow.getDueTime().isBefore(LocalDateTime.now())) {
                return RenewResult.failure("逾期图书不能续借");
            }
            
            // 5. 检查距离还书时间是否小于等于10天
            long daysToDue = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), borrow.getDueTime());
            if (daysToDue > 10) {
                return RenewResult.failure("只能在距离还书时间10天内续借");
            }
            
            // 6. 获取用户类型和借阅规则
            UserType userType = UserType.STUDENT; // TODO: 从用户服务获取实际用户类型
            
            // 7. 获取借阅规则
            Optional<BorrowRule> ruleOpt = borrowRuleDao.findByUserType(userType);
            if (!ruleOpt.isPresent()) {
                return RenewResult.failure("未找到用户类型的借阅规则");
            }
            BorrowRule rule = ruleOpt.get();
            
            // 8. 检查续借次数限制
            if (borrow.getRenewCount() >= rule.getMaxRenewCount()) {
                return RenewResult.failure("续借次数已达上限");
            }
            
            // 9. 调用DAO层执行续借操作
            RenewResult result = bookBorrowDao.renewBook(transId, rule.getRenewExtendDays());
            
            if (result.isSuccess()) {
                log.info("续借成功: transId={}, cardNum={}, newDueTime={}", 
                        transId, cardNum, result.getNewDueTime());
            } else {
                log.warn("续借失败: transId={}, cardNum={}, reason={}", 
                        transId, cardNum, result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("续借图书异常: transId={}, cardNum={}", transId, cardNum, e);
            return RenewResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 查询功能 ====================
    
    @Override
    public List<BookBorrow> getCurrentBorrows(String cardNum) {
        log.debug("查询用户当前借阅: cardNum={}", cardNum);
        // 使用视图查询获取包含图书信息的完整数据
        List<UserBorrowHistory> history = bookBorrowDao.getUserBorrowHistory(cardNum);
        // 过滤出当前借阅中的记录
        return history.stream()
                .filter(h -> "BORROWED".equals(h.getBorrowStatus()) || "OVERDUE".equals(h.getBorrowStatus()))
                .map(this::convertToBookBorrow)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 将UserBorrowHistory转换为BookBorrow（用于兼容现有接口）
     */
    private BookBorrow convertToBookBorrow(UserBorrowHistory history) {
        return BookBorrow.builder()
                .transId(history.getTransId())
                .bookId(null) // 视图中没有bookId，需要根据bookTitle查询
                .cardNum(history.getCardNum())
                .borrowTime(history.getBorrowTime())
                .returnTime(history.getReturnTime())
                .dueTime(history.getDueTime())
                .status(com.vcampus.server.core.library.enums.BorrowStatus.valueOf(history.getBorrowStatus()))
                .renewCount(history.getRenewCount())
                .remarks(history.getDisplayStatus())
                .build();
    }
    
    @Override
    public List<UserBorrowHistory> getBorrowHistory(String cardNum) {
        log.debug("查询用户借阅历史: cardNum={}", cardNum);
        return bookBorrowDao.getUserBorrowHistory(cardNum);
    }
    
    @Override
    public List<BookBorrow> getOverdueBorrows(String cardNum) {
        log.debug("查询用户逾期记录: cardNum={}", cardNum);
        return bookBorrowDao.findOverdueBorrowsByCardNum(cardNum);
    }
    
    @Override
    public List<BookBorrow> getBookBorrowHistory(Integer bookId) {
        log.debug("查询图书借阅历史: bookId={}", bookId);
        return bookBorrowDao.findBorrowHistoryByBookId(bookId);
    }
    
    @Override
    public List<Map<String, Object>> getBookBorrowHistoryWithUserName(Integer bookId) {
        log.debug("查询图书借阅历史（包含用户姓名）: bookId={}", bookId);
        return bookBorrowDao.findBorrowHistoryByBookIdWithUserName(bookId);
    }
    
    @Override
    public List<RecentBorrow> getRecentBorrows(Integer limit) {
        log.debug("查询最近借阅记录: limit={}", limit);
        return bookBorrowDao.getRecentBorrows(limit);
    }
    
    @Override
    public List<OverdueDetails> getAllOverdueDetails() {
        log.debug("查询所有逾期详情");
        return bookBorrowDao.getOverdueDetails();
    }
    
    // ==================== 验证功能 ====================
    
    @Override
    public boolean canBorrowBook(String cardNum, UserType userType, Integer bookId) {
        try {
            // 1. 检查图书是否存在且可借
            Optional<Book> bookOpt = bookDao.findById(bookId);
            if (!bookOpt.isPresent()) {
                log.warn("图书不存在: bookId={}", bookId);
                return false;
            }
            
            Book book = bookOpt.get();
            if (!book.isAvailable()) {
                log.warn("图书不可借: bookId={}, availQty={}", bookId, book.getAvailQty());
                return false;
            }
            
            // 2. 检查用户是否已借阅该图书
            if (bookBorrowDao.isBookBorrowedByUser(bookId, cardNum)) {
                log.warn("用户已借阅该图书: bookId={}, cardNum={}", bookId, cardNum);
                return false;
            }
            
            // 3. 检查用户借阅规则
            Optional<BorrowRule> ruleOpt = borrowRuleDao.findByUserType(userType);
            if (!ruleOpt.isPresent()) {
                log.warn("未找到用户类型借阅规则: userType={}", userType);
                return false;
            }
            
            BorrowRule rule = ruleOpt.get();
                         if (!rule.getIsActive()) {
                 log.warn("借阅规则未启用: userType={}", userType);
                 return false;
             }
            
            // 4. 检查用户当前借阅数量
            long currentBorrowCount = bookBorrowDao.countCurrentBorrowsByCardNum(cardNum);
            if (!rule.canBorrow((int) currentBorrowCount)) {
                log.warn("用户借阅数量已达上限: cardNum={}, current={}, max={}", 
                        cardNum, currentBorrowCount, rule.getMaxBorrowCount());
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("验证借阅权限异常: cardNum={}, bookId={}", cardNum, bookId, e);
            return false;
        }
    }
    
    @Override
    public boolean canRenewBook(Integer transId, String cardNum) {
        try {
            // 1. 检查借阅记录是否存在
            Optional<BookBorrow> borrowOpt = bookBorrowDao.findById(transId);
            if (!borrowOpt.isPresent()) {
                log.warn("借阅记录不存在: transId={}", transId);
                return false;
            }
            
            BookBorrow borrow = borrowOpt.get();
            
            // 2. 检查是否为该用户的借阅记录
            if (!cardNum.equals(borrow.getCardNum())) {
                log.warn("借阅记录不属于该用户: transId={}, cardNum={}, recordCardNum={}", 
                        transId, cardNum, borrow.getCardNum());
                return false;
            }
            
            // 3. 检查借阅状态
            if (borrow.getStatus() != BorrowStatus.BORROWED) {
                log.warn("借阅记录状态不允许续借: transId={}, status={}", 
                        transId, borrow.getStatus());
                return false;
            }
            
            // 4. 检查是否逾期
            if (borrow.isOverdue()) {
                log.warn("逾期图书不能续借: transId={}, overdueDays={}", 
                        transId, borrow.getOverdueDays());
                return false;
            }
            
            // 5. 检查续借次数限制
            // 这里需要获取用户类型和借阅规则，暂时使用默认值
            UserType userType = UserType.STUDENT; // TODO: 从用户服务获取实际用户类型
            Optional<BorrowRule> ruleOpt = borrowRuleDao.findByUserType(userType);
            if (!ruleOpt.isPresent()) {
                log.warn("未找到用户类型借阅规则: userType={}", userType);
                return false;
            }
            
            BorrowRule rule = ruleOpt.get();
            if (!rule.canRenew(borrow.getRenewCount())) {
                log.warn("续借次数已达上限: transId={}, renewCount={}, maxRenew={}", 
                        transId, borrow.getRenewCount(), rule.getMaxRenewCount());
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("验证续借权限异常: transId={}, cardNum={}", transId, cardNum, e);
            return false;
        }
    }
    
    @Override
    public boolean canReturnBook(Integer transId, String cardNum) {
        try {
            // 1. 检查借阅记录是否存在
            Optional<BookBorrow> borrowOpt = bookBorrowDao.findById(transId);
            if (!borrowOpt.isPresent()) {
                log.warn("借阅记录不存在: transId={}", transId);
                return false;
            }
            
            BookBorrow borrow = borrowOpt.get();
            
            // 2. 检查是否为该用户的借阅记录
            if (!cardNum.equals(borrow.getCardNum())) {
                log.warn("借阅记录不属于该用户: transId={}, cardNum={}, recordCardNum={}", 
                        transId, cardNum, borrow.getCardNum());
                return false;
            }
            
            // 3. 检查借阅状态
            if (borrow.getStatus() != BorrowStatus.BORROWED && borrow.getStatus() != BorrowStatus.OVERDUE) {
                log.warn("借阅记录状态不允许归还: transId={}, status={}", 
                        transId, borrow.getStatus());
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("验证归还权限异常: transId={}, cardNum={}", transId, cardNum, e);
            return false;
        }
    }
    
    // ==================== 统计功能 ====================
    
    @Override
    public BorrowStatistics getUserBorrowStatistics(String cardNum) {
        try {
            long totalBorrows = bookBorrowDao.countBorrowsByCardNum(cardNum);
            long currentBorrows = bookBorrowDao.countCurrentBorrowsByCardNum(cardNum);
            long overdueCount = bookBorrowDao.findOverdueBorrowsByCardNum(cardNum).size();
            
            // 计算总续借次数
            List<BookBorrow> allBorrows = bookBorrowDao.findByCardNum(cardNum);
            long totalRenews = allBorrows.stream()
                    .mapToLong(BookBorrow::getRenewCount)
                    .sum();
            
            return new BorrowStatistics(totalBorrows, currentBorrows, overdueCount, totalRenews);
            
        } catch (Exception e) {
            log.error("获取用户借阅统计异常: cardNum={}", cardNum, e);
            return new BorrowStatistics(0, 0, 0, 0);
        }
    }
    
    @Override
    public BookBorrowStatistics getBookBorrowStatistics(Integer bookId) {
        try {
            long totalBorrows = bookBorrowDao.countBorrowsByBookId(bookId);
            long currentBorrows = bookBorrowDao.findCurrentBorrowsByBookId(bookId).size();
            
            Optional<Book> bookOpt = bookDao.findById(bookId);
            long availableCount = bookOpt.map(Book::getAvailQty).orElse(0);
            
            // 获取最后借阅时间
            List<BookBorrow> borrowHistory = bookBorrowDao.findBorrowHistoryByBookId(bookId);
            LocalDateTime lastBorrowTime = borrowHistory.stream()
                    .map(BookBorrow::getBorrowTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            
            return new BookBorrowStatistics(totalBorrows, currentBorrows, availableCount, lastBorrowTime);
            
        } catch (Exception e) {
            log.error("获取图书借阅统计异常: bookId={}", bookId, e);
            return new BookBorrowStatistics(0, 0, 0, null);
        }
    }
}
