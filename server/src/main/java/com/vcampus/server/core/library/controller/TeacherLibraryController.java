package com.vcampus.server.core.library.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.server.core.library.entity.core.BookBorrow;
import com.vcampus.server.core.library.entity.result.BorrowResult;
import com.vcampus.server.core.library.entity.result.ReturnResult;
import com.vcampus.server.core.library.entity.result.RenewResult;
import com.vcampus.server.core.library.entity.view.UserBorrowHistory;
import com.vcampus.server.core.library.entity.view.OverdueDetails;
import com.vcampus.server.core.library.entity.view.RecentBorrow;
import com.vcampus.server.core.library.enums.UserType;
import com.vcampus.server.core.library.service.LibraryBorrowService;
import com.vcampus.server.core.library.service.impl.LibraryBorrowServiceImpl;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师端图书借还书控制器
 * 提供教师用户的图书借阅、归还、续借等功能
 * 教师具有比学生更高的借阅权限
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class TeacherLibraryController {
    
    private final LibraryBorrowService libraryBorrowService;
    
    public TeacherLibraryController() {
        this.libraryBorrowService = new LibraryBorrowServiceImpl();
    }
    
    // ==================== 借阅管理 ====================
    
    /**
     * 教师借阅图书
     * URI: library/teacher/borrow
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/borrow", role = "teacher", description = "教师借阅图书")
    public Response borrowBook(Request request) {
        log.info("教师借阅图书请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
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
            
            // 3. 获取用户信息
            String cardNum = request.getSession().getUserId();
            UserType userType = UserType.STAFF; // 教师属于教职工类型
            
            // 4. 执行借阅
            BorrowResult result = libraryBorrowService.borrowBook(bookId, cardNum, userType);
            
            if (result.isSuccess()) {
                return Response.Builder.success("借阅成功", result);
            } else {
                return Response.Builder.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("教师借阅图书异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 教师归还图书
     * URI: library/teacher/return
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/return", role = "teacher", description = "教师归还图书")
    public Response returnBook(Request request) {
        log.info("教师归还图书请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
            }
            
            // 2. 获取参数
            String transIdStr = request.getParam("transId");
            if (transIdStr == null || transIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("借阅记录ID不能为空");
            }
            
            Integer transId;
            try {
                transId = Integer.valueOf(transIdStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("借阅记录ID格式错误");
            }
            
            // 3. 获取用户信息
            String cardNum = request.getSession().getUserId();
            
            // 4. 执行归还
            ReturnResult result = libraryBorrowService.returnBook(transId, cardNum);
            
            if (result.isSuccess()) {
                return Response.Builder.success("归还成功", result);
            } else {
                return Response.Builder.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("教师归还图书异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 教师续借图书
     * URI: library/teacher/renew
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/renew", role = "teacher", description = "教师续借图书")
    public Response renewBook(Request request) {
        log.info("教师续借图书请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
            }
            
            // 2. 获取参数
            String transIdStr = request.getParam("transId");
            if (transIdStr == null || transIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("借阅记录ID不能为空");
            }
            
            Integer transId;
            try {
                transId = Integer.valueOf(transIdStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("借阅记录ID格式错误");
            }
            
            // 3. 获取用户信息
            String cardNum = request.getSession().getUserId();
            
            // 4. 执行续借
            RenewResult result = libraryBorrowService.renewBook(transId, cardNum);
            
            if (result.isSuccess()) {
                return Response.Builder.success("续借成功", result);
            } else {
                return Response.Builder.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("教师续借图书异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    // ==================== 查询功能 ====================
    
    /**
     * 获取教师当前借阅的图书
     * URI: library/teacher/current-borrows
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/current-borrows", role = "teacher", description = "获取教师当前借阅的图书")
    public Response getCurrentBorrows(Request request) {
        log.info("获取教师当前借阅请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
            }
            
            // 2. 获取用户信息
            String cardNum = request.getSession().getUserId();
            
            // 3. 查询借阅历史并过滤出当前借阅
            List<UserBorrowHistory> allHistory = libraryBorrowService.getBorrowHistory(cardNum);
            List<UserBorrowHistory> currentBorrows = allHistory.stream()
                    .filter(h -> "BORROWED".equals(h.getBorrowStatus()) || "OVERDUE".equals(h.getBorrowStatus()))
                    .collect(java.util.stream.Collectors.toList());
            
            return Response.Builder.success("查询成功", currentBorrows);
            
        } catch (Exception e) {
            log.error("获取教师当前借阅异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取教师借阅历史
     * URI: library/teacher/borrow-history
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/borrow-history", role = "teacher", description = "获取教师借阅历史")
    public Response getBorrowHistory(Request request) {
        log.info("获取教师借阅历史请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
            }
            
            // 2. 获取用户信息
            String cardNum = request.getSession().getUserId();
            
            // 3. 查询借阅历史
            List<UserBorrowHistory> borrowHistory = libraryBorrowService.getBorrowHistory(cardNum);
            
            return Response.Builder.success("查询成功", borrowHistory);
            
        } catch (Exception e) {
            log.error("获取教师借阅历史异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取教师逾期记录
     * URI: library/teacher/overdue-borrows
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/overdue-borrows", role = "teacher", description = "获取教师逾期记录")
    public Response getOverdueBorrows(Request request) {
        log.info("获取教师逾期记录请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
            }
            
            // 2. 获取用户信息
            String cardNum = request.getSession().getUserId();
            
            // 3. 查询逾期记录
            List<BookBorrow> overdueBorrows = libraryBorrowService.getOverdueBorrows(cardNum);
            
            return Response.Builder.success("查询成功", overdueBorrows);
            
        } catch (Exception e) {
            log.error("获取教师逾期记录异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取教师借阅统计
     * URI: library/teacher/borrow-statistics
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/borrow-statistics", role = "teacher", description = "获取教师借阅统计")
    public Response getBorrowStatistics(Request request) {
        log.info("获取教师借阅统计请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
            }
            
            // 2. 获取用户信息
            String cardNum = request.getSession().getUserId();
            
            // 3. 查询借阅统计
            LibraryBorrowService.BorrowStatistics statistics = 
                    libraryBorrowService.getUserBorrowStatistics(cardNum);
            
            return Response.Builder.success("查询成功", statistics);
            
        } catch (Exception e) {
            log.error("获取教师借阅统计异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    // ==================== 教师特有功能 ====================
    
    /**
     * 获取最近借阅记录（教师可以查看所有用户的借阅情况）
     * URI: library/teacher/recent-borrows
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/recent-borrows", role = "teacher", description = "获取最近借阅记录")
    public Response getRecentBorrows(Request request) {
        log.info("获取最近借阅记录请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
            }
            
            // 2. 获取参数
            String limitStr = request.getParam("limit");
            Integer limit = 10; // 默认限制10条
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                try {
                    limit = Integer.valueOf(limitStr);
                    if (limit <= 0 || limit > 100) {
                        limit = 10; // 限制在1-100之间
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
     * 获取图书借阅历史（教师可以查看任意图书的借阅情况）
     * URI: library/teacher/book-borrow-history
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/book-borrow-history", role = "teacher", description = "获取图书借阅历史")
    public Response getBookBorrowHistory(Request request) {
        log.info("获取图书借阅历史请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
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
            
            // 3. 查询图书借阅历史（包含用户姓名）
            List<Map<String, Object>> borrowRecords = libraryBorrowService.getBookBorrowHistoryWithUserName(bookId);
            
            return Response.Builder.success("查询成功", borrowRecords);
            
        } catch (Exception e) {
            log.error("获取图书借阅历史异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取图书借阅统计（教师可以查看任意图书的借阅统计）
     * URI: library/teacher/book-borrow-statistics
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/book-borrow-statistics", role = "teacher", description = "获取图书借阅统计")
    public Response getBookBorrowStatistics(Request request) {
        log.info("获取图书借阅统计请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
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
            
            // 3. 查询图书借阅统计
            LibraryBorrowService.BookBorrowStatistics statistics = 
                    libraryBorrowService.getBookBorrowStatistics(bookId);
            
            return Response.Builder.success("查询成功", statistics);
            
        } catch (Exception e) {
            log.error("获取图书借阅统计异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    // ==================== 验证功能 ====================
    
    /**
     * 检查教师是否可以借阅指定图书
     * URI: library/teacher/can-borrow
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/can-borrow", role = "teacher", description = "检查教师是否可以借阅指定图书")
    public Response canBorrowBook(Request request) {
        log.info("检查教师借阅权限请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
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
            
            // 3. 获取用户信息
            String cardNum = request.getSession().getUserId();
            UserType userType = UserType.STAFF; // 教师属于教职工类型
            
            // 4. 检查借阅权限
            boolean canBorrow = libraryBorrowService.canBorrowBook(cardNum, userType, bookId);
            
            Map<String, Object> result = Map.of(
                "canBorrow", canBorrow,
                "bookId", bookId,
                "cardNum", cardNum,
                "userType", userType.getDescription()
            );
            
            return Response.Builder.success("检查完成", result);
            
        } catch (Exception e) {
            log.error("检查教师借阅权限异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 检查教师是否可以续借指定图书
     * URI: library/teacher/can-renew
     * 权限: teacher
     */
    @RouteMapping(uri = "library/teacher/can-renew", role = "teacher", description = "检查教师是否可以续借指定图书")
    public Response canRenewBook(Request request) {
        log.info("检查教师续借权限请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("teacher")) {
                return Response.Builder.forbidden("需要教师权限");
            }
            
            // 2. 获取参数
            String transIdStr = request.getParam("transId");
            if (transIdStr == null || transIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("借阅记录ID不能为空");
            }
            
            Integer transId;
            try {
                transId = Integer.valueOf(transIdStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("借阅记录ID格式错误");
            }
            
            // 3. 获取用户信息
            String cardNum = request.getSession().getUserId();
            
            // 4. 检查续借权限
            boolean canRenew = libraryBorrowService.canRenewBook(transId, cardNum);
            
            Map<String, Object> result = Map.of(
                "canRenew", canRenew,
                "transId", transId,
                "cardNum", cardNum
            );
            
            return Response.Builder.success("检查完成", result);
            
        } catch (Exception e) {
            log.error("检查教师续借权限异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
}
