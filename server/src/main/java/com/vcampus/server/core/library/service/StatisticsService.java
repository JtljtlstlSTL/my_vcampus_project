package com.vcampus.server.core.library.service;

import java.util.List;
import java.util.Map;

/**
 * 统计分析服务接口
 * 提供图书管理系统的各种统计分析和报表功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface StatisticsService {
    
    // ==================== 仪表板统计 ====================
    
    /**
     * 获取仪表板总体统计数据
     * @return 包含总图书数、总用户数、当前借阅数、逾期数等关键指标
     */
    Map<String, Object> getDashboardStatistics();
    
    /**
     * 获取今日统计数据
     * @return 今日借阅、归还、新增用户等数据
     */
    Map<String, Object> getTodayStatistics();
    
    // ==================== 图书统计 ====================
    
    /**
     * 获取图书总体统计
     * @return 图书总数、在馆数、借出数、维修数等
     */
    Map<String, Object> getBookStatistics();
    
    /**
     * 获取分类统计信息
     * @return 各分类图书数量和借阅情况
     */
    List<Map<String, Object>> getCategoryStatistics();
    
    /**
     * 获取图书分类统计数据（用于图表）
     * @return 包含categories字段的统计数据
     */
    Map<String, Object> getBookCategoryStatistics();
    
    /**
     * 获取借阅率统计数据（用于图表）
     * @return 包含rates字段的统计数据
     */
    Map<String, Object> getBorrowRateStatistics();
    
    /**
     * 获取借阅趋势统计数据（用于图表）
     * @return 包含trend字段的统计数据
     */
    Map<String, Object> getBorrowTrendStatistics();
    
    /**
     * 获取用户活跃度统计数据（用于图表）
     * @return 包含activity字段的统计数据
     */
    Map<String, Object> getUserActivityStatistics();
    
    /**
     * 获取热门图书排行榜
     * @param limit 返回数量限制
     * @return 按借阅量排序的热门图书列表
     */
    List<Map<String, Object>> getPopularBooks(int limit);
    
    /**
     * 获取库存预警信息
     * @return 库存不足的图书列表
     */
    List<Map<String, Object>> getInventoryAlerts();
    
    // ==================== 借阅统计 ====================
    
    /**
     * 获取借阅总体统计
     * @return 总借阅数、当前借阅数、逾期数等
     */
    Map<String, Object> getBorrowStatistics();
    
    /**
     * 获取借阅趋势数据
     * @param period 统计周期：day/week/month
     * @param days 统计天数
     * @return 借阅趋势数据
     */
    List<Map<String, Object>> getBorrowTrend(String period, int days);
    
    /**
     * 获取逾期统计信息
     * @return 逾期图书和用户统计
     */
    Map<String, Object> getOverdueStatistics();
    
    /**
     * 获取借阅状态分布
     * @return 各状态借阅记录数量
     */
    List<Map<String, Object>> getBorrowStatusDistribution();
    
    // ==================== 用户统计 ====================
    
    /**
     * 获取用户总体统计
     * @return 总用户数、活跃用户数、新用户数等
     */
    Map<String, Object> getUserStatistics();
    
    /**
     * 获取活跃用户排行榜
     * @param limit 返回数量限制
     * @return 按借阅量排序的活跃用户列表
     */
    List<Map<String, Object>> getActiveUsers(int limit);
    
    // ==================== 报表生成 ====================
    
    /**
     * 生成日报数据
     * @param date 指定日期
     * @return 日报统计数据
     */
    Map<String, Object> generateDailyReport(String date);
    
    /**
     * 生成周报数据
     * @param weekStart 周开始日期
     * @return 周报统计数据
     */
    Map<String, Object> generateWeeklyReport(String weekStart);
    
    /**
     * 生成月报数据
     * @param month 月份 (YYYY-MM)
     * @return 月报统计数据
     */
    Map<String, Object> generateMonthlyReport(String month);
    
    /**
     * 生成年报数据
     * @param year 年份
     * @return 年报统计数据
     */
    Map<String, Object> generateYearlyReport(String year);
    
    // ==================== 新增统计方法 ====================
    
    /**
     * 获取今日借阅数量
     * @param date 日期
     * @return 借阅数量
     */
    int getTodayBorrowCount(String date);
    
    /**
     * 获取今日归还数量
     * @param date 日期
     * @return 归还数量
     */
    int getTodayReturnCount(String date);
    
    /**
     * 获取图书总数
     * @return 图书总数
     */
    long getTotalBooksCount();
    
    /**
     * 获取可借阅图书数量
     * @return 可借阅图书数量
     */
    long getAvailableBooksCount();
    
    /**
     * 获取借出图书数量
     * @return 借出图书数量
     */
    long getBorrowedBooksCount();
    
    /**
     * 获取逾期图书数量
     * @return 逾期图书数量
     */
    long getOverdueBooksCount();
    
    /**
     * 获取所有活跃用户数量
     * @return 活跃用户数量
     */
    int getAllActiveUsersCount();
}
