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

import java.util.List;
import java.util.Map;

/**
 * 学生端图书借还书控制器
 * 提供学生用户的图书借阅、归还、续借等功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class StudentLibraryController {
    
    private final LibraryBorrowService libraryBorrowService;
    
    public StudentLibraryController() {
        this.libraryBorrowService = new LibraryBorrowServiceImpl();
    }
    
    // ==================== 借阅管理 ====================
    
    /**
     * 学生借阅图书
     * URI: library/student/borrow
     * 权限: student
     */
    @RouteMapping(uri = "library/student/borrow", role = "student", description = "学生借阅图书")
    public Response borrowBook(Request request) {
        log.info("学生借阅图书请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("student")) {
                return Response.Builder.forbidden("需要学生权限");
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
            UserType userType = UserType.STUDENT;
            
            // 4. 执行借阅
            BorrowResult result = libraryBorrowService.borrowBook(bookId, cardNum, userType);
            
            if (result.isSuccess()) {
                return Response.Builder.success("借阅成功", result);
            } else {
                return Response.Builder.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("学生借阅图书异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 学生归还图书
     * URI: library/student/return
     * 权限: student
     */
    @RouteMapping(uri = "library/student/return", role = "student", description = "学生归还图书")
    public Response returnBook(Request request) {
        log.info("学生归还图书请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("student")) {
                return Response.Builder.forbidden("需要学生权限");
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
            log.error("学生归还图书异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 学生续借图书
     * URI: library/student/renew
     * 权限: student
     */
    @RouteMapping(uri = "library/student/renew", role = "student", description = "学生续借图书")
    public Response renewBook(Request request) {
        log.info("学生续借图书请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("student")) {
                return Response.Builder.forbidden("需要学生权限");
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
            log.error("学生续借图书异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    // ==================== 查询功能 ====================
    
    /**
     * 获取学生当前借阅的图书
     * URI: library/student/current-borrows
     * 权限: student
     */
    @RouteMapping(uri = "library/student/current-borrows", role = "student", description = "获取学生当前借阅的图书")
    public Response getCurrentBorrows(Request request) {
        log.info("获取学生当前借阅请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("student")) {
                return Response.Builder.forbidden("需要学生权限");
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
            log.error("获取学生当前借阅异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取学生借阅历史
     * URI: library/student/borrow-history
     * 权限: student
     */
    @RouteMapping(uri = "library/student/borrow-history", role = "student", description = "获取学生借阅历史")
    public Response getBorrowHistory(Request request) {
        log.info("获取学生借阅历史请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("student")) {
                return Response.Builder.forbidden("需要学生权限");
            }
            
            // 2. 获取用户信息
            String cardNum = request.getSession().getUserId();
            
            // 3. 查询借阅历史
            List<UserBorrowHistory> borrowHistory = libraryBorrowService.getBorrowHistory(cardNum);
            
            return Response.Builder.success("查询成功", borrowHistory);
            
        } catch (Exception e) {
            log.error("获取学生借阅历史异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取学生逾期记录
     * URI: library/student/overdue-borrows
     * 权限: student
     */
    @RouteMapping(uri = "library/student/overdue-borrows", role = "student", description = "获取学生逾期记录")
    public Response getOverdueBorrows(Request request) {
        log.info("获取学生逾期记录请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("student")) {
                return Response.Builder.forbidden("需要学生权限");
            }
            
            // 2. 获取用户信息
            String cardNum = request.getSession().getUserId();
            
            // 3. 查询逾期记录
            List<BookBorrow> overdueBorrows = libraryBorrowService.getOverdueBorrows(cardNum);
            
            return Response.Builder.success("查询成功", overdueBorrows);
            
        } catch (Exception e) {
            log.error("获取学生逾期记录异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取学生借阅统计
     * URI: library/student/borrow-statistics
     * 权限: student
     */
    @RouteMapping(uri = "library/student/borrow-statistics", role = "student", description = "获取学生借阅统计")
    public Response getBorrowStatistics(Request request) {
        log.info("获取学生借阅统计请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("student")) {
                return Response.Builder.forbidden("需要学生权限");
            }
            
            // 2. 获取用户信息
            String cardNum = request.getSession().getUserId();
            
            // 3. 查询借阅统计
            LibraryBorrowService.BorrowStatistics statistics = 
                    libraryBorrowService.getUserBorrowStatistics(cardNum);
            
            return Response.Builder.success("查询成功", statistics);
            
        } catch (Exception e) {
            log.error("获取学生借阅统计异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    // ==================== 验证功能 ====================
    
    /**
     * 检查学生是否可以借阅指定图书
     * URI: library/student/can-borrow
     * 权限: student
     */
    @RouteMapping(uri = "library/student/can-borrow", role = "student", description = "检查学生是否可以借阅指定图书")
    public Response canBorrowBook(Request request) {
        log.info("检查学生借阅权限请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("student")) {
                return Response.Builder.forbidden("需要学生权限");
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
            UserType userType = UserType.STUDENT;
            
            // 4. 检查借阅权限
            boolean canBorrow = libraryBorrowService.canBorrowBook(cardNum, userType, bookId);
            
            Map<String, Object> result = Map.of(
                "canBorrow", canBorrow,
                "bookId", bookId,
                "cardNum", cardNum
            );
            
            return Response.Builder.success("检查完成", result);
            
        } catch (Exception e) {
            log.error("检查学生借阅权限异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 检查学生是否可以续借指定图书
     * URI: library/student/can-renew
     * 权限: student
     */
    @RouteMapping(uri = "library/student/can-renew", role = "student", description = "检查学生是否可以续借指定图书")
    public Response canRenewBook(Request request) {
        log.info("检查学生续借权限请求: {}", request.getUri());
        
        try {
            // 1. 验证会话
            if (request.getSession() == null || !request.getSession().hasPermission("student")) {
                return Response.Builder.forbidden("需要学生权限");
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
            log.error("检查学生续借权限异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
}
