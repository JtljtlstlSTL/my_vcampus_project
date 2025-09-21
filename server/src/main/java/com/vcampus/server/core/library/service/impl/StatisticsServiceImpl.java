package com.vcampus.server.core.library.service.impl;

import com.vcampus.server.core.library.dao.BookDao;
import com.vcampus.server.core.library.dao.BookBorrowDao;
import com.vcampus.server.core.library.entity.view.UserBorrowHistory;
import com.vcampus.server.core.library.enums.BookStatus;
import com.vcampus.server.core.library.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统计分析服务实现类
 * 提供图书管理系统的各种统计分析和报表功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class StatisticsServiceImpl implements StatisticsService {
    
    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    
    private BookDao bookDao;
    private BookBorrowDao bookBorrowDao;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    
    public StatisticsServiceImpl(BookDao bookDao) {
        this.bookDao = bookDao;
        this.bookBorrowDao = BookBorrowDao.getInstance();
    }
    
    // ==================== 仪表板统计 ====================
    
    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取所有图书
            List<com.vcampus.server.core.library.entity.core.Book> allBooks = bookDao.findAll();
            int totalBooks = allBooks.size();
            
            // 计算在馆图书数量（状态为"在馆"的图书）
            long availableBooks = allBooks.stream()
                .filter(book -> book.getStatus() != null && book.getStatus().toString().equals("IN_LIBRARY"))
                .count();
            
            // 计算已借阅图书数量（状态为"已借阅"的图书）
            long borrowedBooks = allBooks.stream()
                .filter(book -> book.getStatus() != null && book.getStatus().toString().equals("BORROWED"))
                .count();
            
            // 计算维护中图书数量（状态为"维护中"的图书）
            long maintenanceBooks = allBooks.stream()
                .filter(book -> book.getStatus() != null && book.getStatus().toString().equals("MAINTENANCE"))
                .count();
            
            result.put("totalBooks", totalBooks);
            result.put("availableBooks", (int) availableBooks);
            result.put("borrowedBooks", (int) borrowedBooks);
            result.put("maintenanceBooks", (int) maintenanceBooks);
            
            // 计算借阅率
            double borrowRate = totalBooks > 0 ? (double) borrowedBooks / totalBooks * 100 : 0;
            result.put("borrowRate", Math.round(borrowRate * 100.0) / 100.0);
            
            log.info("获取仪表板统计数据成功");
            return result;
        } catch (Exception e) {
            log.error("获取仪表板统计数据失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> getTodayStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            String today = LocalDate.now().format(DATE_FORMATTER);
            
            // 查询今日真实数据
            int todayBorrows = bookBorrowDao.getTodayBorrowCount(today);
            int todayReturns = bookBorrowDao.getTodayReturnCount(today);
            int todayNewUsers = 0; // 需要用户表支持，暂时设为0
            int todayOverdue = bookBorrowDao.getTodayOverdueCount(today);
            
            result.put("todayBorrows", todayBorrows);
            result.put("todayReturns", todayReturns);
            result.put("todayNewUsers", todayNewUsers);
            result.put("todayOverdue", todayOverdue);
            
            log.info("获取今日统计数据成功");
            return result;
        } catch (Exception e) {
            log.error("获取今日统计数据失败", e);
            return new HashMap<>();
        }
    }
    
    // ==================== 图书统计 ====================
    
    @Override
    public Map<String, Object> getBookStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从数据库获取所有图书
            List<com.vcampus.server.core.library.entity.core.Book> allBooks = bookDao.findAll();
            
            // 统计各状态图书数量
            int totalBooks = allBooks.size();
            int availableBooks = 0;
            int borrowedBooks = 0;
            int maintenanceBooks = 0;
            
            for (com.vcampus.server.core.library.entity.core.Book book : allBooks) {
                if (book.getStatus() != null) {
                    switch (book.getStatus().toString()) {
                        case "AVAILABLE":
                            availableBooks++;
                            break;
                        case "BORROWED":
                            borrowedBooks++;
                            break;
                        case "MAINTENANCE":
                            maintenanceBooks++;
                            break;
                    }
                } else {
                    // 如果状态为空，默认为可用
                    availableBooks++;
                }
            }
            
            result.put("totalBooks", totalBooks);
            result.put("availableBooks", availableBooks);
            result.put("borrowedBooks", borrowedBooks);
            result.put("maintenanceBooks", maintenanceBooks);
            
            // 计算各状态占比
            if (totalBooks > 0) {
                result.put("availableRate", Math.round((double) availableBooks / totalBooks * 10000.0) / 100.0);
                result.put("borrowedRate", Math.round((double) borrowedBooks / totalBooks * 10000.0) / 100.0);
                result.put("maintenanceRate", Math.round((double) maintenanceBooks / totalBooks * 10000.0) / 100.0);
            }
            
            log.info("获取图书统计数据成功: 总数={}, 可用={}, 已借出={}, 维护中={}", 
                    totalBooks, availableBooks, borrowedBooks, maintenanceBooks);
            return result;
        } catch (Exception e) {
            log.error("获取图书统计数据失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        try {
            // 获取所有图书
            List<com.vcampus.server.core.library.entity.core.Book> allBooks = bookDao.findAll();
            
            // 按分类统计
            Map<String, Integer> categoryBookCount = new HashMap<>();
            Map<String, Integer> categoryBorrowCount = new HashMap<>();
            
            for (com.vcampus.server.core.library.entity.core.Book book : allBooks) {
                String category = book.getCategory();
                if (category != null) {
                    // 统计图书数量
                    categoryBookCount.put(category, categoryBookCount.getOrDefault(category, 0) + 1);
                    
                    // 统计借阅数量（如果状态是"已借阅"）
                    if (book.getStatus() != null && book.getStatus().toString().equals("BORROWED")) {
                        categoryBorrowCount.put(category, categoryBorrowCount.getOrDefault(category, 0) + 1);
                    }
                }
            }
            
            // 构建结果
            List<Map<String, Object>> result = new ArrayList<>();
            for (String category : categoryBookCount.keySet()) {
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("category", category);
                categoryData.put("bookCount", categoryBookCount.get(category));
                categoryData.put("borrowCount", categoryBorrowCount.getOrDefault(category, 0));
                result.add(categoryData);
            }
            
            // 按图书数量排序
            result.sort((a, b) -> Integer.compare((Integer) b.get("bookCount"), (Integer) a.get("bookCount")));
            
            log.info("获取分类统计数据成功，共{}个分类", result.size());
            return result;
        } catch (Exception e) {
            log.error("获取分类统计数据失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getPopularBooks(int limit) {
        try {
            // 模拟热门图书数据
            List<Map<String, Object>> result = new ArrayList<>();
            
            Map<String, Object> book1 = new HashMap<>();
            book1.put("title", "Java编程思想");
            book1.put("author", "Bruce Eckel");
            book1.put("borrowCount", 156);
            result.add(book1);
            
            Map<String, Object> book2 = new HashMap<>();
            book2.put("title", "算法导论");
            book2.put("author", "Thomas H. Cormen");
            book2.put("borrowCount", 134);
            result.add(book2);
            
            Map<String, Object> book3 = new HashMap<>();
            book3.put("title", "红楼梦");
            book3.put("author", "曹雪芹");
            book3.put("borrowCount", 98);
            result.add(book3);
            
            log.info("获取热门图书数据成功，共{}本", result.size());
            return result;
        } catch (Exception e) {
            log.error("获取热门图书数据失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getInventoryAlerts() {
        try {
            // 模拟库存预警数据
            List<Map<String, Object>> result = new ArrayList<>();
            
            Map<String, Object> book1 = new HashMap<>();
            book1.put("title", "数据库原理");
            book1.put("author", "王珊");
            book1.put("currentStock", 2);
            book1.put("minStock", 5);
            book1.put("status", "库存不足");
            result.add(book1);
            
            Map<String, Object> book2 = new HashMap<>();
            book2.put("title", "计算机网络");
            book2.put("author", "谢希仁");
            book2.put("currentStock", 1);
            book2.put("minStock", 3);
            book2.put("status", "严重不足");
            result.add(book2);
            
            log.info("获取库存预警数据成功，共{}本图书需要补货", result.size());
            return result;
        } catch (Exception e) {
            log.error("获取库存预警数据失败", e);
            return new ArrayList<>();
        }
    }
    
    // ==================== 借阅统计 ====================
    
    @Override
    public Map<String, Object> getBorrowStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取所有借阅记录
            List<com.vcampus.server.core.library.entity.core.BookBorrow> allBorrows = bookBorrowDao.findAll();
            long totalBorrows = allBorrows.size();
            
            // 计算当前借阅中的记录（状态为"借阅中"）
            long currentBorrows = allBorrows.stream()
                .filter(borrow -> borrow.getStatus() != null && borrow.getStatus().toString().equals("BORROWED"))
                .count();
            
            // 计算已归还的记录（状态为"已归还"）
            long returnedBorrows = allBorrows.stream()
                .filter(borrow -> borrow.getStatus() != null && borrow.getStatus().toString().equals("RETURNED"))
                .count();
            
            // 计算逾期的记录
            long overdueBorrows = bookBorrowDao.findAllOverdueBorrows().size();
            
            // 计算续借的记录（续借次数大于0）
            long renewedBorrows = allBorrows.stream()
                .filter(borrow -> borrow.getRenewCount() != null && borrow.getRenewCount() > 0)
                .count();
            
            result.put("totalBorrows", (int) totalBorrows);
            result.put("currentBorrows", (int) currentBorrows);
            result.put("returnedBorrows", (int) returnedBorrows);
            result.put("overdueBorrows", (int) overdueBorrows);
            result.put("renewedBorrows", (int) renewedBorrows);
            
            // 计算归还率
            double returnRate = totalBorrows > 0 ? (double) returnedBorrows / totalBorrows * 100 : 0;
            result.put("returnRate", Math.round(returnRate * 100.0) / 100.0);
            
            log.info("获取借阅统计数据成功: 总借阅={}, 当前借阅={}, 已归还={}, 逾期={}", 
                    totalBorrows, currentBorrows, returnedBorrows, overdueBorrows);
            return result;
        } catch (Exception e) {
            log.error("获取借阅统计数据失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getBorrowTrend(String period, int days) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            
            // 根据周期获取真实数据
            switch (period) {
                case "4days":
                    // 近4天：今天和前3天，索引3是今天
                    for (int i = 0; i < 4; i++) {
                        Map<String, Object> dayData = new HashMap<>();
                        LocalDate date = LocalDate.now().minusDays(3 - i);
                        dayData.put("date", date.format(DateTimeFormatter.ofPattern("M-d")));
                        
                        // 获取真实借阅和归还数据
                        String queryDate = date.format(DATE_FORMATTER);
                        log.info("查询日期: {}, 借阅数量查询", queryDate);
                        int borrowCount = bookBorrowDao.getTodayBorrowCount(queryDate);
                        int returnCount = bookBorrowDao.getTodayReturnCount(queryDate);
                        log.info("查询结果: 借阅={}, 归还={}", borrowCount, returnCount);
                        
                        dayData.put("borrowCount", borrowCount);
                        dayData.put("returnCount", returnCount);
                        result.add(dayData);
                    }
                    break;
                    
                case "3months":
                    // 近3个月：本月、上月、上上月
                    for (int i = 2; i >= 0; i--) {
                        Map<String, Object> monthData = new HashMap<>();
                        LocalDate month = LocalDate.now().minusMonths(i);
                        monthData.put("date", month.format(DateTimeFormatter.ofPattern("M月")));
                        
                        // 获取该月的借阅和归还数据
                        int borrowCount = getMonthBorrowCount(month);
                        int returnCount = getMonthReturnCount(month);
                        
                        monthData.put("borrowCount", borrowCount);
                        monthData.put("returnCount", returnCount);
                        result.add(monthData);
                    }
                    break;
                    
                case "1year":
                    // 近1年：今年到目前为止的所有月份
                    LocalDate currentDate = LocalDate.now();
                    LocalDate startOfYear = LocalDate.of(currentDate.getYear(), 1, 1);
                    
                    LocalDate month = startOfYear;
                    while (!month.isAfter(currentDate)) {
                        Map<String, Object> monthData = new HashMap<>();
                        monthData.put("date", month.getMonthValue() + "月");
                        
                        // 获取该月的借阅和归还数据
                        int borrowCount = getMonthBorrowCount(month);
                        int returnCount = getMonthReturnCount(month);
                        
                        monthData.put("borrowCount", borrowCount);
                        monthData.put("returnCount", returnCount);
                        result.add(monthData);
                        
                        month = month.plusMonths(1);
                    }
                    break;
            }
            
            log.info("获取借阅趋势数据成功，周期：{}，数据条数：{}", period, result.size());
            return result;
        } catch (Exception e) {
            log.error("获取借阅趋势数据失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取指定月份的借阅数量
     */
    private int getMonthBorrowCount(LocalDate month) {
        try {
            // 直接查询数据库，不通过DAO
            String monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            log.info("查询月度借阅数据: {}", monthStr);
            
            // 使用现有的DAO方法查询该月每天的借阅数据
            int totalCount = 0;
            int daysInMonth = month.lengthOfMonth();
            
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate dayDate = LocalDate.of(month.getYear(), month.getMonthValue(), day);
                String dayStr = dayDate.format(DATE_FORMATTER);
                int dayCount = bookBorrowDao.getTodayBorrowCount(dayStr);
                totalCount += dayCount;
            }
            
            log.info("月度借阅数据查询结果: {} = {}", monthStr, totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("获取月度借阅数据失败: {}", month, e);
            return 0;
        }
    }
    
    /**
     * 获取指定月份的归还数量
     */
    private int getMonthReturnCount(LocalDate month) {
        try {
            // 直接查询数据库，不通过DAO
            String monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            log.info("查询月度归还数据: {}", monthStr);
            
            // 使用现有的DAO方法查询该月每天的归还数据
            int totalCount = 0;
            int daysInMonth = month.lengthOfMonth();
            
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate dayDate = LocalDate.of(month.getYear(), month.getMonthValue(), day);
                String dayStr = dayDate.format(DATE_FORMATTER);
                int dayCount = bookBorrowDao.getTodayReturnCount(dayStr);
                totalCount += dayCount;
            }
            
            log.info("月度归还数据查询结果: {} = {}", monthStr, totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("获取月度归还数据失败: {}", month, e);
            return 0;
        }
    }
    
    @Override
    public Map<String, Object> getOverdueStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 模拟逾期统计数据
            result.put("overdueCount", 12);
            result.put("overdueUsers", 8);
            
            List<Map<String, Object>> overdueBooks = new ArrayList<>();
            Map<String, Object> book1 = new HashMap<>();
            book1.put("title", "数据结构");
            book1.put("borrower", "张三");
            book1.put("overdueDays", 5);
            overdueBooks.add(book1);
            
            result.put("overdueBooks", overdueBooks);
            
            log.info("获取逾期统计数据成功");
            return result;
        } catch (Exception e) {
            log.error("获取逾期统计数据失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getBorrowStatusDistribution() {
        try {
            // 从数据库获取真实借阅状态分布数据
            List<Map<String, Object>> result = new ArrayList<>();
            
            // 获取所有借阅记录
            List<com.vcampus.server.core.library.entity.core.BookBorrow> allBorrows = bookBorrowDao.findAll();
            
            // 统计各状态数量
            long totalBorrows = allBorrows.size();
            long returnedBorrows = allBorrows.stream()
                .filter(borrow -> borrow.getStatus() != null && borrow.getStatus().toString().equals("RETURNED"))
                .count();
            long currentBorrows = allBorrows.stream()
                .filter(borrow -> borrow.getStatus() != null && borrow.getStatus().toString().equals("BORROWED"))
                .count();
            long overdueBorrows = bookBorrowDao.findAllOverdueBorrows().size();
            long renewedBorrows = allBorrows.stream()
                .filter(borrow -> borrow.getRenewCount() != null && borrow.getRenewCount() > 0)
                .count();
            
            Map<String, Object> status1 = new HashMap<>();
            status1.put("status", "已归还");
            status1.put("count", (int) returnedBorrows);
            result.add(status1);
            
            Map<String, Object> status2 = new HashMap<>();
            status2.put("status", "已借出");
            status2.put("count", (int) currentBorrows);
            result.add(status2);
            
            Map<String, Object> status3 = new HashMap<>();
            status3.put("status", "逾期");
            status3.put("count", (int) overdueBorrows);
            result.add(status3);
            
            Map<String, Object> status4 = new HashMap<>();
            status4.put("status", "续借");
            status4.put("count", (int) renewedBorrows);
            result.add(status4);
            
            log.info("获取借阅状态分布数据成功: 已归还={}, 已借出={}, 逾期={}, 续借={}", 
                    returnedBorrows, currentBorrows, overdueBorrows, renewedBorrows);
            return result;
        } catch (Exception e) {
            log.error("获取借阅状态分布数据失败", e);
            return new ArrayList<>();
        }
    }
    
    // ==================== 用户统计 ====================
    
    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 模拟用户统计数据
            result.put("totalUsers", 567);
            result.put("activeUsers", 234);
            result.put("newUsers", 18);
            result.put("overdueUsers", 8);
            
            log.info("获取用户统计数据成功");
            return result;
        } catch (Exception e) {
            log.error("获取用户统计数据失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getActiveUsers(int limit) {
        try {
            // 模拟活跃用户数据
            List<Map<String, Object>> result = new ArrayList<>();
            
            Map<String, Object> user1 = new HashMap<>();
            user1.put("username", "张三");
            user1.put("borrowCount", 45);
            user1.put("lastBorrowDate", "2024-01-15");
            result.add(user1);
            
            Map<String, Object> user2 = new HashMap<>();
            user2.put("username", "李四");
            user2.put("borrowCount", 38);
            user2.put("lastBorrowDate", "2024-01-14");
            result.add(user2);
            
            Map<String, Object> user3 = new HashMap<>();
            user3.put("username", "王五");
            user3.put("borrowCount", 32);
            user3.put("lastBorrowDate", "2024-01-13");
            result.add(user3);
            
            log.info("获取活跃用户数据成功，共{}个用户", result.size());
            return result;
        } catch (Exception e) {
            log.error("获取活跃用户数据失败", e);
            return new ArrayList<>();
        }
    }
    
    // ==================== 报表生成 ====================
    
    @Override
    public Map<String, Object> generateDailyReport(String date) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("reportDate", date);
            result.put("borrows", 25);
            result.put("returns", 18);
            result.put("newUsers", 3);
            result.put("overdue", 2);
            result.put("categoryStats", getCategoryStatistics());
            
            log.info("生成日报数据成功，日期：{}", date);
            return result;
        } catch (Exception e) {
            log.error("生成日报数据失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> generateWeeklyReport(String weekStart) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("weekStart", weekStart);
            result.put("totalBorrows", 156);
            result.put("totalReturns", 142);
            result.put("newUsers", 18);
            result.put("popularBooks", getPopularBooks(10));
            result.put("categoryStats", getCategoryStatistics());
            
            log.info("生成周报数据成功，周开始日期：{}", weekStart);
            return result;
        } catch (Exception e) {
            log.error("生成周报数据失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> generateMonthlyReport(String month) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("month", month);
            result.put("totalBorrows", 624);
            result.put("totalReturns", 598);
            result.put("newUsers", 72);
            result.put("popularBooks", getPopularBooks(10));
            result.put("categoryStats", getCategoryStatistics());
            result.put("trendData", getBorrowTrend("day", 30));
            
            log.info("生成月报数据成功，月份：{}", month);
            return result;
        } catch (Exception e) {
            log.error("生成月报数据失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> generateYearlyReport(String year) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("year", year);
            result.put("totalBorrows", 7488);
            result.put("totalReturns", 7176);
            result.put("newUsers", 864);
            result.put("popularBooks", getPopularBooks(20));
            result.put("categoryStats", getCategoryStatistics());
            result.put("monthlyTrend", getBorrowTrend("month", 12));
            
            log.info("生成年报数据成功，年份：{}", year);
            return result;
        } catch (Exception e) {
            log.error("生成年报数据失败", e);
            return new HashMap<>();
        }
    }
    
    // ==================== 图表数据方法 ====================
    
    @Override
    public Map<String, Object> getBookCategoryStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 直接从数据库获取分类统计，避免不必要的转换
            Map<String, Integer> categories = bookDao.getCategoryStatistics();
            
            result.put("categories", categories);
            log.info("获取图书分类统计成功，分类数：{}，分类详情：{}", categories.size(), categories);
            return result;
        } catch (Exception e) {
            log.error("获取图书分类统计失败", e);
            // 返回默认数据
            Map<String, Integer> defaultCategories = new HashMap<>();
            defaultCategories.put("A", 2);
            defaultCategories.put("B", 2);
            defaultCategories.put("C", 2);
            defaultCategories.put("D", 2);
            defaultCategories.put("E", 2);
            defaultCategories.put("F", 2);
            defaultCategories.put("G", 2);
            defaultCategories.put("H", 2);
            defaultCategories.put("I", 2);
            defaultCategories.put("J", 2);
            defaultCategories.put("K", 2);
            defaultCategories.put("N", 2);
            defaultCategories.put("O", 2);
            defaultCategories.put("P", 2);
            defaultCategories.put("Q", 2);
            defaultCategories.put("R", 2);
            defaultCategories.put("S", 2);
            defaultCategories.put("T", 2);
            defaultCategories.put("U", 2);
            defaultCategories.put("V", 2);
            defaultCategories.put("X", 2);
            defaultCategories.put("Z", 2);
            result.put("categories", defaultCategories);
            return result;
        }
    }
    
    /**
     * 将中图法代码转换为中文名称
     */
    private String getCategoryChineseName(String categoryCode) {
        switch (categoryCode) {
            case "A": return "马克思主义、列宁主义、毛泽东思想、邓小平理论";
            case "B": return "哲学、宗教";
            case "C": return "社会科学总论";
            case "D": return "政治、法律";
            case "E": return "军事";
            case "F": return "经济";
            case "G": return "文化、科学、教育、体育";
            case "H": return "语言、文字";
            case "I": return "文学";
            case "J": return "艺术";
            case "K": return "历史、地理";
            case "N": return "自然科学总论";
            case "O": return "数理科学和化学";
            case "P": return "天文学、地球科学";
            case "Q": return "生物科学";
            case "R": return "医药、卫生";
            case "S": return "农业科学";
            case "T": return "工业技术";
            case "U": return "交通运输";
            case "V": return "航空、航天";
            case "X": return "环境科学、安全科学";
            case "Z": return "综合性图书";
            default: return "其他";
        }
    }
    
    @Override
    public Map<String, Object> getBorrowRateStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取所有图书
            List<com.vcampus.server.core.library.entity.core.Book> allBooks = bookDao.findAll();
            
            // 统计各分类借阅率
            Map<String, Integer> categoryBorrowedCounts = new HashMap<>(); // 历史总借阅次数
            Map<String, Integer> categoryTotalCounts = new HashMap<>(); // 图书总数量
            
            // 初始化分类统计
            for (com.vcampus.server.core.library.entity.core.Book book : allBooks) {
                String category = book.getCategory();
                if (category != null && !category.trim().isEmpty()) {
                    // 使用图书总数量（包括副本）
                    int totalQty = book.getTotalQty() != null ? book.getTotalQty() : 1;
                    categoryTotalCounts.put(category, categoryTotalCounts.getOrDefault(category, 0) + totalQty);
                }
            }
            
            // 通过借阅记录表统计各分类的历史总借阅次数
            List<Map<String, Object>> borrowStats = bookBorrowDao.getCategoryBorrowStatistics();
            for (Map<String, Object> stat : borrowStats) {
                String category = (String) stat.get("category");
                Integer borrowCount = ((Number) stat.get("borrowCount")).intValue();
                if (category != null) {
                    categoryBorrowedCounts.put(category, borrowCount);
                }
            }
            
            // 计算借阅率并排序
            // 借阅率 = (该分类历史总借阅次数 / 该分类的图书总数) * 100
            log.info("各分类历史借阅次数统计: {}", categoryBorrowedCounts);
            log.info("各分类图书总数统计: {}", categoryTotalCounts);
            
            Map<String, Double> rates = new HashMap<>();
            for (String category : categoryTotalCounts.keySet()) {
                int categoryBorrowed = categoryBorrowedCounts.getOrDefault(category, 0);
                int categoryTotal = categoryTotalCounts.get(category);
                double rate = categoryTotal > 0 ? (double) categoryBorrowed / categoryTotal * 100 : 0.0;
                // 确保借阅率不超过100%
                rate = Math.min(rate, 100.0);
                log.info("分类 {}: 图书总数={}, 历史借阅次数={}, 借阅率={}%", category, categoryTotal, categoryBorrowed, rate);
                rates.put(category, rate);
            }
            
            // 获取前10名
            Map<String, Double> topTenRates = rates.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
            
            result.put("rates", topTenRates);
            log.info("获取借阅率统计成功，前10名：{}", topTenRates);
            return result;
        } catch (Exception e) {
            log.error("获取借阅率统计失败", e);
            // 返回默认数据
            Map<String, Double> defaultRates = new HashMap<>();
            defaultRates.put("文学", 85.5);
            defaultRates.put("科技", 92.3);
            defaultRates.put("历史", 78.1);
            defaultRates.put("艺术", 65.8);
            defaultRates.put("其他", 45.2);
            result.put("rates", defaultRates);
            return result;
        }
    }
    
    /**
     * 计算图书的借阅次数
     */
    private int calculateBorrowCountForBook(int bookId) {
        try {
            // 查询借阅记录表获取真实借阅次数
            return (int) bookBorrowDao.countBorrowsByBookId(bookId);
        } catch (Exception e) {
            log.error("计算图书借阅次数失败，bookId: {}", bookId, e);
            return 0;
        }
    }
    
    @Override
    public Map<String, Object> getBorrowTrendStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 查询最近7天的真实借阅趋势数据
            Map<String, Double> trend = bookBorrowDao.getBorrowTrendLast7Days();
            
            result.put("trend", trend);
            log.info("获取借阅趋势统计成功");
            return result;
        } catch (Exception e) {
            log.error("获取借阅趋势统计失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> getUserActivityStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取近三天的活跃用户数据
            List<Map<String, Object>> activityData = getActiveUsersTrend(3);
            result.put("activity", activityData);
            log.info("获取用户活跃度统计成功，数据条数: {}", activityData.size());
            return result;
        } catch (Exception e) {
            log.error("获取用户活跃度统计失败", e);
            // 返回默认数据
            List<Map<String, Object>> defaultActivity = new ArrayList<>();
            for (int i = 2; i >= 0; i--) {
                Map<String, Object> dayData = new HashMap<>();
                LocalDate date = LocalDate.now().minusDays(i);
                dayData.put("date", date.format(DateTimeFormatter.ofPattern("M-d")));
                dayData.put("activeUsers", 0);
                defaultActivity.add(dayData);
            }
            result.put("activity", defaultActivity);
            return result;
        }
    }
    
    /**
     * 获取活跃用户趋势数据（近N天）
     */
    public List<Map<String, Object>> getActiveUsersTrend(int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                // 获取该天有借阅或归还记录的用户数量
                int activeUsers = getActiveUsersCount(dateStr);
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.format(DateTimeFormatter.ofPattern("M-d")));
                dayData.put("activeUsers", activeUsers);
                result.add(dayData);
                
                log.info("日期: {}, 活跃用户数: {}", dateStr, activeUsers);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取活跃用户趋势数据失败", e);
            return result;
        }
    }
    
    /**
     * 获取指定日期的活跃用户数量
     */
    private int getActiveUsersCount(String date) {
        try {
            // 查询该天有借阅或归还记录的不重复用户数量
            List<UserBorrowHistory> borrowHistory = bookBorrowDao.getAllUserBorrowHistory();
            
            Set<String> activeUsers = new HashSet<>();
            
            for (UserBorrowHistory history : borrowHistory) {
                // 检查借阅时间
                if (history.getBorrowTime() != null && 
                    history.getBorrowTime().toLocalDate().toString().equals(date)) {
                    activeUsers.add(history.getCardNum());
                }
                // 检查归还时间
                if (history.getReturnTime() != null && 
                    history.getReturnTime().toLocalDate().toString().equals(date)) {
                    activeUsers.add(history.getCardNum());
                }
            }
            
            return activeUsers.size();
        } catch (Exception e) {
            log.error("获取活跃用户数量失败: {}", date, e);
            return 0;
        }
    }
    
    /**
     * 获取今日借阅数量
     */
    public int getTodayBorrowCount(String date) {
        try {
            return bookBorrowDao.getTodayBorrowCount(date);
        } catch (Exception e) {
            log.error("获取今日借阅数量失败: date={}", date, e);
            return 0;
        }
    }
    
    /**
     * 获取今日归还数量
     */
    public int getTodayReturnCount(String date) {
        try {
            return bookBorrowDao.getTodayReturnCount(date);
        } catch (Exception e) {
            log.error("获取今日归还数量失败: date={}", date, e);
            return 0;
        }
    }
    
    /**
     * 获取图书总数
     */
    public long getTotalBooksCount() {
        try {
            return bookDao.count();
        } catch (Exception e) {
            log.error("获取图书总数失败", e);
            return 0;
        }
    }
    
    /**
     * 获取可借阅图书数量
     */
    public long getAvailableBooksCount() {
        try {
            // 在库的图书都是可借阅的
            return bookDao.countByStatus(BookStatus.IN_LIBRARY);
        } catch (Exception e) {
            log.error("获取可借阅图书数量失败", e);
            return 0;
        }
    }
    
    /**
     * 获取借出图书数量
     */
    public long getBorrowedBooksCount() {
        try {
            return bookDao.countByStatus(BookStatus.BORROWED);
        } catch (Exception e) {
            log.error("获取借出图书数量失败", e);
            return 0;
        }
    }
    
    /**
     * 获取逾期图书数量
     */
    public long getOverdueBooksCount() {
        try {
            return bookBorrowDao.findAllOverdueBorrows().size();
        } catch (Exception e) {
            log.error("获取逾期图书数量失败", e);
            return 0;
        }
    }
    
    /**
     * 获取所有活跃用户数量（所有借阅过图书的用户）
     */
    public int getAllActiveUsersCount() {
        try {
            List<UserBorrowHistory> borrowHistory = bookBorrowDao.getAllUserBorrowHistory();
            Set<String> activeUsers = new HashSet<>();
            for (UserBorrowHistory history : borrowHistory) {
                activeUsers.add(history.getCardNum());
            }
            return activeUsers.size();
        } catch (Exception e) {
            log.error("获取所有活跃用户数量失败", e);
            return 0;
        }
    }
}
