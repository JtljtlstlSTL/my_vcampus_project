package com.vcampus.server.core.library.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.server.core.library.entity.core.BookBorrow;
import com.vcampus.server.core.library.entity.view.UserBorrowHistory;
import com.vcampus.server.core.library.entity.view.OverdueDetails;
import com.vcampus.server.core.library.entity.view.RecentBorrow;
import com.vcampus.server.core.library.enums.UserType;
import com.vcampus.server.core.library.service.LibraryBorrowService;
import com.vcampus.server.core.library.service.impl.LibraryBorrowServiceImpl;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 管理员端图书借还书管理控制器
 * 提供管理员用户的图书借阅管理功能
 * 管理员具有最高权限，可以管理所有用户的借阅记录
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class AdminLibraryController {
    
    private final LibraryBorrowService libraryBorrowService;
    
    public AdminLibraryController() {
        this.libraryBorrowService = new LibraryBorrowServiceImpl();
    }
    
    // ==================== 管理员借阅管理 ====================
    
    // ==================== 管理员查询功能 ====================
    
    /**
     * 获取所有用户借阅历史
     * URI: library/admin/all-borrow-history
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/all-borrow-history", role = "admin", description = "获取所有用户借阅历史")
    public Response getAllBorrowHistory(Request request) {
        log.info("获取所有用户借阅历史请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("admin")) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 获取参数
            String cardNum = request.getParam("cardNum"); // 可选参数，用于筛选特定用户
            
            List<UserBorrowHistory> borrowHistory;
            if (cardNum != null && !cardNum.trim().isEmpty()) {
                // 查询特定用户的借阅历史
                borrowHistory = libraryBorrowService.getBorrowHistory(cardNum);
            } else {
                // 查询所有用户的借阅历史
                // 这里需要调用DAO层的方法，暂时返回空列表
                borrowHistory = List.of(); // TODO: 实现获取所有用户借阅历史的方法
            }
            
            return Response.Builder.success("查询成功", borrowHistory);
            
        } catch (Exception e) {
            log.error("获取所有用户借阅历史异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取所有逾期详情
     * URI: library/admin/all-overdue-details
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/all-overdue-details", role = "admin", description = "获取所有逾期详情")
    public Response getAllOverdueDetails(Request request) {
        log.info("获取所有逾期详情请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("admin")) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 查询所有逾期详情
            List<OverdueDetails> overdueDetails = libraryBorrowService.getAllOverdueDetails();
            
            return Response.Builder.success("查询成功", overdueDetails);
            
        } catch (Exception e) {
            log.error("获取所有逾期详情异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取最近借阅记录
     * URI: library/admin/recent-borrows
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/recent-borrows", role = "admin", description = "获取最近借阅记录")
    public Response getRecentBorrows(Request request) {
        log.info("获取最近借阅记录请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("admin")) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 获取参数
            String limitStr = request.getParam("limit");
            Integer limit = 20; // 管理员默认显示更多记录
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                try {
                    limit = Integer.valueOf(limitStr);
                    if (limit <= 0 || limit > 200) {
                        limit = 20; // 限制在1-200之间
                    }
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }
            
            // 3. 查询最近借阅记录
            List<RecentBorrow> recentBorrows = libraryBorrowService.getRecentBorrows(limit);
            
            return Response.Builder.success("查询成功", recentBorrows);
            
        } catch (Exception e) {
            log.error("获取最近借阅记录异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取指定用户的借阅记录
     * URI: library/admin/user-borrows
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/user-borrows", role = "admin", description = "获取指定用户的借阅记录")
    public Response getUserBorrows(Request request) {
        log.info("获取指定用户借阅记录请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("admin")) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 获取参数
            String cardNum = request.getParam("cardNum");
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("用户卡号不能为空");
            }
            
            // 3. 查询用户借阅记录
            List<UserBorrowHistory> borrowHistory = libraryBorrowService.getBorrowHistory(cardNum);
            List<BookBorrow> currentBorrows = libraryBorrowService.getCurrentBorrows(cardNum);
            List<BookBorrow> overdueBorrows = libraryBorrowService.getOverdueBorrows(cardNum);
            LibraryBorrowService.BorrowStatistics statistics = 
                    libraryBorrowService.getUserBorrowStatistics(cardNum);
            
            Map<String, Object> result = Map.of(
                "cardNum", cardNum,
                "borrowHistory", borrowHistory,
                "currentBorrows", currentBorrows,
                "overdueBorrows", overdueBorrows,
                "statistics", statistics
            );
            
            return Response.Builder.success("查询成功", result);
            
        } catch (Exception e) {
            log.error("获取指定用户借阅记录异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取指定图书的借阅记录
     * URI: library/admin/book-borrows
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/book-borrows", role = "admin", description = "获取指定图书的借阅记录")
    public Response getBookBorrows(Request request) {
        log.info("获取指定图书借阅记录请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("admin")) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 获取参数
            String bookIdStr = request.getParam("bookId");
            if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("图书ID不能为空");
            }
            
            Integer bookId;
            try {
                bookId = Integer.valueOf(bookIdStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("图书ID格式错误");
            }
            
            // 3. 查询图书借阅记录
            List<BookBorrow> bookBorrowHistory = libraryBorrowService.getBookBorrowHistory(bookId);
            LibraryBorrowService.BookBorrowStatistics statistics = 
                    libraryBorrowService.getBookBorrowStatistics(bookId);
            
            Map<String, Object> result = Map.of(
                "bookId", bookId,
                "borrowHistory", bookBorrowHistory,
                "statistics", statistics
            );
            
            return Response.Builder.success("查询成功", result);
            
        } catch (Exception e) {
            log.error("获取指定图书借阅记录异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    // ==================== 管理员统计功能 ====================
    
    /**
     * 获取借阅统计概览
     * URI: library/admin/borrow-overview
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/borrow-overview", role = "admin", description = "获取借阅统计概览")
    public Response getBorrowOverview(Request request) {
        log.info("获取借阅统计概览请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("admin")) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 获取统计信息
            List<OverdueDetails> overdueDetails = libraryBorrowService.getAllOverdueDetails();
            List<RecentBorrow> recentBorrows = libraryBorrowService.getRecentBorrows(10);
            
            // 计算统计信息
            long totalOverdue = overdueDetails.size();
            long totalRecentBorrows = recentBorrows.size();
            
            Map<String, Object> overview = Map.of(
                "totalOverdue", totalOverdue,
                "totalRecentBorrows", totalRecentBorrows,
                "overdueDetails", overdueDetails,
                "recentBorrows", recentBorrows
            );
            
            return Response.Builder.success("查询成功", overview);
            
        } catch (Exception e) {
            log.error("获取借阅统计概览异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    // ==================== 管理员验证功能 ====================
    
    /**
     * 检查用户借阅权限（管理员可以检查任何用户）
     * URI: library/admin/check-user-borrow-permission
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/check-user-borrow-permission", role = "admin", description = "检查用户借阅权限")
    public Response checkUserBorrowPermission(Request request) {
        log.info("检查用户借阅权限请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("admin")) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 获取参数
            String cardNum = request.getParam("cardNum");
            String userTypeStr = request.getParam("userType");
            String bookIdStr = request.getParam("bookId");
            
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("用户卡号不能为空");
            }
            if (userTypeStr == null || userTypeStr.trim().isEmpty()) {
                return Response.Builder.badRequest("用户类型不能为空");
            }
            if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("图书ID不能为空");
            }
            
            Integer bookId;
            try {
                bookId = Integer.valueOf(bookIdStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("图书ID格式错误");
            }
            
            UserType userType = UserType.fromCode(userTypeStr);
            if (userType == null) {
                return Response.Builder.badRequest("用户类型格式错误");
            }
            
            // 3. 检查借阅权限
            boolean canBorrow = libraryBorrowService.canBorrowBook(cardNum, userType, bookId);
            
            Map<String, Object> result = Map.of(
                "canBorrow", canBorrow,
                "bookId", bookId,
                "cardNum", cardNum,
                "userType", userType.getDescription()
            );
            
            return Response.Builder.success("检查完成", result);
            
        } catch (Exception e) {
            log.error("检查用户借阅权限异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
}
