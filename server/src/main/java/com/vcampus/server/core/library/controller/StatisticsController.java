package com.vcampus.server.core.library.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.server.core.library.service.StatisticsService;
import com.vcampus.server.core.common.annotation.RouteMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 * 提供图书管理系统的统计分析和报表功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class StatisticsController {
    
    private static final Logger log = LoggerFactory.getLogger(StatisticsController.class);
    
    private final StatisticsService statisticsService;
    
    public StatisticsController() {
        this.statisticsService = new com.vcampus.server.core.library.service.impl.StatisticsServiceImpl(
            com.vcampus.server.core.library.dao.BookDao.getInstance()
        );
    }
    
    /**
     * 获取图书统计
     */
    @RouteMapping(uri = "admin/getBookStatistics", role = "admin", description = "获取图书统计信息")
    public Response getBookStatistics(Request request) {
        try {
            Map<String, Object> statistics = statisticsService.getDashboardStatistics();
            return Response.Builder.success("获取图书统计成功", statistics);
        } catch (Exception e) {
            log.error("获取图书统计失败", e);
            return Response.Builder.error("获取图书统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取图书分类统计
     */
    @RouteMapping(uri = "library/admin/statistics/book-categories", role = "admin", description = "获取图书分类统计")
    public Response getBookCategories(Request request) {
        try {
            Map<String, Object> data = statisticsService.getBookCategoryStatistics();
            return Response.Builder.success("获取图书分类统计成功", data);
        } catch (Exception e) {
            log.error("获取图书分类统计失败", e);
            return Response.Builder.error("获取图书分类统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取借阅率统计
     */
    @RouteMapping(uri = "library/admin/statistics/borrow-rates", role = "admin", description = "获取借阅率统计")
    public Response getBorrowRates(Request request) {
        try {
            Map<String, Object> data = statisticsService.getBorrowRateStatistics();
            return Response.Builder.success("获取借阅率统计成功", data);
        } catch (Exception e) {
            log.error("获取借阅率统计失败", e);
            return Response.Builder.error("获取借阅率统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取借阅趋势统计
     */
    @RouteMapping(uri = "library/admin/statistics/borrow-trend", role = "admin", description = "获取借阅趋势统计")
    public Response getBorrowTrend(Request request) {
        try {
            String period = request.getParam("period");
            if (period == null) {
                period = "4days"; // 默认近4天
            }
            
            List<Map<String, Object>> data = statisticsService.getBorrowTrend(period, 0);
            return Response.Builder.success("获取借阅趋势统计成功", data);
        } catch (Exception e) {
            log.error("获取借阅趋势统计失败", e);
            return Response.Builder.error("获取借阅趋势统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户活跃度统计
     */
    @RouteMapping(uri = "library/admin/statistics/user-activity", role = "admin", description = "获取用户活跃度统计")
    public Response getUserActivity(Request request) {
        try {
            Map<String, Object> data = statisticsService.getUserActivityStatistics();
            return Response.Builder.success("获取用户活跃度统计成功", data);
        } catch (Exception e) {
            log.error("获取用户活跃度统计失败", e);
            return Response.Builder.error("获取用户活跃度统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取借阅统计
     */
    @RouteMapping(uri = "admin/getBorrowStatistics", role = "admin", description = "获取借阅统计信息")
    public Response getBorrowStatistics(Request request) {
        try {
            Map<String, Object> statistics = statisticsService.getBorrowStatistics();
            return Response.Builder.success("获取借阅统计成功", statistics);
        } catch (Exception e) {
            log.error("获取借阅统计失败", e);
            return Response.Builder.error("获取借阅统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户统计
     */
    @RouteMapping(uri = "admin/getUserStatistics", role = "admin", description = "获取用户统计信息")
    public Response getUserStatistics(Request request) {
        try {
            Map<String, Object> statistics = statisticsService.getUserStatistics();
            return Response.Builder.success("获取用户统计成功", statistics);
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            return Response.Builder.error("获取用户统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分类统计
     */
    @RouteMapping(uri = "admin/getCategoryStatistics", role = "admin", description = "获取分类统计信息")
    public Response getCategoryStatistics(Request request) {
        try {
            List<Map<String, Object>> statistics = statisticsService.getCategoryStatistics();
            return Response.Builder.success("获取分类统计成功", statistics);
        } catch (Exception e) {
            log.error("获取分类统计失败", e);
            return Response.Builder.error("获取分类统计失败: " + e.getMessage());
        }
    }
    
    
    /**
     * 获取热门图书
     */
    @RouteMapping(uri = "admin/getPopularBooks", role = "admin", description = "获取热门图书信息")
    public Response getPopularBooks(Request request) {
        try {
            List<Map<String, Object>> books = statisticsService.getPopularBooks(10);
            return Response.Builder.success("获取热门图书成功", books);
        } catch (Exception e) {
            log.error("获取热门图书失败", e);
            return Response.Builder.error("获取热门图书失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取逾期图书
     */
    @RouteMapping(uri = "admin/getOverdueBooks", role = "admin", description = "获取逾期图书信息")
    public Response getOverdueBooks(Request request) {
        try {
            Map<String, Object> overdueData = statisticsService.getOverdueStatistics();
            // 将Map转换为List，因为前端期望的是List
            List<Map<String, Object>> books = new ArrayList<>();
            if (overdueData.containsKey("overdueBooks")) {
                books = (List<Map<String, Object>>) overdueData.get("overdueBooks");
            }
            return Response.Builder.success("获取逾期图书成功", books);
        } catch (Exception e) {
            log.error("获取逾期图书失败", e);
            return Response.Builder.error("获取逾期图书失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取库存预警
     */
    @RouteMapping(uri = "admin/getInventoryAlerts", role = "admin", description = "获取库存预警信息")
    public Response getInventoryAlerts(Request request) {
        try {
            List<Map<String, Object>> alerts = statisticsService.getInventoryAlerts();
            return Response.Builder.success("获取库存预警成功", alerts);
        } catch (Exception e) {
            log.error("获取库存预警失败", e);
            return Response.Builder.error("获取库存预警失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取今日借阅数量
     */
    @RouteMapping(uri = "library/admin/statistics/today-borrows", role = "admin", description = "获取今日借阅数量")
    public Response getTodayBorrows(Request request) {
        try {
            String date = request.getParams().get("date");
            int count = statisticsService.getTodayBorrowCount(date);
            return Response.Builder.success("获取今日借阅数量成功", Map.of("count", count));
        } catch (Exception e) {
            log.error("获取今日借阅数量失败", e);
            return Response.Builder.error("获取今日借阅数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取今日归还数量
     */
    @RouteMapping(uri = "library/admin/statistics/today-returns", role = "admin", description = "获取今日归还数量")
    public Response getTodayReturns(Request request) {
        try {
            String date = request.getParams().get("date");
            int count = statisticsService.getTodayReturnCount(date);
            return Response.Builder.success("获取今日归还数量成功", Map.of("count", count));
        } catch (Exception e) {
            log.error("获取今日归还数量失败", e);
            return Response.Builder.error("获取今日归还数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取图书总数
     */
    @RouteMapping(uri = "library/admin/statistics/total-books", role = "admin", description = "获取图书总数")
    public Response getTotalBooks(Request request) {
        try {
            long count = statisticsService.getTotalBooksCount();
            return Response.Builder.success("获取图书总数成功", Map.of("count", count));
        } catch (Exception e) {
            log.error("获取图书总数失败", e);
            return Response.Builder.error("获取图书总数失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取可借阅图书数量
     */
    @RouteMapping(uri = "library/admin/statistics/available-books", role = "admin", description = "获取可借阅图书数量")
    public Response getAvailableBooks(Request request) {
        try {
            long count = statisticsService.getAvailableBooksCount();
            return Response.Builder.success("获取可借阅图书数量成功", Map.of("count", count));
        } catch (Exception e) {
            log.error("获取可借阅图书数量失败", e);
            return Response.Builder.error("获取可借阅图书数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取借出图书数量
     */
    @RouteMapping(uri = "library/admin/statistics/borrowed-books", role = "admin", description = "获取借出图书数量")
    public Response getBorrowedBooks(Request request) {
        try {
            long count = statisticsService.getBorrowedBooksCount();
            return Response.Builder.success("获取借出图书数量成功", Map.of("count", count));
        } catch (Exception e) {
            log.error("获取借出图书数量失败", e);
            return Response.Builder.error("获取借出图书数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取逾期图书数量
     */
    @RouteMapping(uri = "library/admin/statistics/overdue-books", role = "admin", description = "获取逾期图书数量")
    public Response getOverdueBooksCount(Request request) {
        try {
            long count = statisticsService.getOverdueBooksCount();
            return Response.Builder.success("获取逾期图书数量成功", Map.of("count", count));
        } catch (Exception e) {
            log.error("获取逾期图书数量失败", e);
            return Response.Builder.error("获取逾期图书数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有活跃用户数量
     */
    @RouteMapping(uri = "library/admin/statistics/all-active-users", role = "admin", description = "获取所有活跃用户数量")
    public Response getAllActiveUsers(Request request) {
        try {
            int count = statisticsService.getAllActiveUsersCount();
            return Response.Builder.success("获取所有活跃用户数量成功", Map.of("count", count));
        } catch (Exception e) {
            log.error("获取所有活跃用户数量失败", e);
            return Response.Builder.error("获取所有活跃用户数量失败: " + e.getMessage());
        }
    }
}
