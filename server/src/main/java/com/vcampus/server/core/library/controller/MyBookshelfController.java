package com.vcampus.server.core.library.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.server.core.library.entity.view.BookshelfView;
import com.vcampus.server.core.library.service.MyBookshelfService;
import com.vcampus.server.core.library.service.impl.MyBookshelfServiceImpl;
import com.vcampus.server.core.library.service.UserPersonalCategoryService;
import com.vcampus.server.core.library.service.impl.UserPersonalCategoryServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 我的书架控制器
 * 提供用户书架管理的API接口
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class MyBookshelfController {
    
    private final MyBookshelfService myBookshelfService;
    private final UserPersonalCategoryService personalCategoryService;
    
    public MyBookshelfController() {
        this.myBookshelfService = new MyBookshelfServiceImpl();
        this.personalCategoryService = UserPersonalCategoryServiceImpl.getInstance();
    }
    
    // ==================== 书架管理 ====================
    
    /**
     * 添加图书到书架
     * URI: library/bookshelf/add
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/bookshelf/add", role = "student", description = "添加图书到书架")
    public Response addBookToShelf(Request request) {
        log.info("处理添加图书到书架请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String cardNum = request.getSession().getUserId();
            String bookIdStr = request.getParam("bookId");
            String categoryName = request.getParam("categoryName");
            String notes = request.getParam("notes");
            
            if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("图书ID不能为空");
            }
            
            Integer bookId;
            try {
                bookId = Integer.valueOf(bookIdStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("图书ID格式错误");
            }
            
            // 3. 调用服务层
            boolean success = myBookshelfService.addBookToShelf(cardNum, bookId, categoryName, notes);
            
            if (success) {
                log.info("添加图书到书架成功: cardNum={}, bookId={}", cardNum, bookId);
                return Response.Builder.success("添加成功");
            } else {
                log.warn("添加图书到书架失败: cardNum={}, bookId={}", cardNum, bookId);
                return Response.Builder.error("添加失败，图书可能已在书架中");
            }
            
        } catch (Exception e) {
            log.error("处理添加图书到书架请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 从书架移除图书
     * URI: library/bookshelf/remove
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/bookshelf/remove", role = "student", description = "从书架移除图书")
    public Response removeBookFromShelf(Request request) {
        log.info("处理从书架移除图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String cardNum = request.getSession().getUserId();
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
            
            // 3. 调用服务层
            boolean success = myBookshelfService.removeBookFromShelf(cardNum, bookId);
            
            if (success) {
                log.info("从书架移除图书成功: cardNum={}, bookId={}", cardNum, bookId);
                return Response.Builder.success("移除成功");
            } else {
                log.warn("从书架移除图书失败: cardNum={}, bookId={}", cardNum, bookId);
                return Response.Builder.error("移除失败");
            }
            
        } catch (Exception e) {
            log.error("处理从书架移除图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 更新书架分类
     * URI: library/bookshelf/update-category
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/bookshelf/update-category", role = "student", description = "更新书架分类")
    public Response updateShelfCategory(Request request) {
        log.info("处理更新书架分类请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String shelfIdStr = request.getParam("shelfId");
            String newCategoryName = request.getParam("categoryName");
            
            if (shelfIdStr == null || shelfIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("书架ID不能为空");
            }
            if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
                return Response.Builder.badRequest("分类名称不能为空");
            }
            
            Integer shelfId;
            try {
                shelfId = Integer.valueOf(shelfIdStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("书架ID格式错误");
            }
            
            // 3. 调用服务层
            boolean success = myBookshelfService.updateShelfCategory(shelfId, newCategoryName.trim());
            
            if (success) {
                log.info("更新书架分类成功: shelfId={}, newCategoryName={}", shelfId, newCategoryName);
                return Response.Builder.success("更新成功");
            } else {
                log.warn("更新书架分类失败: shelfId={}, newCategoryName={}", shelfId, newCategoryName);
                return Response.Builder.error("更新失败");
            }
            
        } catch (Exception e) {
            log.error("处理更新书架分类请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 查询功能 ====================
    
    /**
     * 获取用户书架
     * URI: library/bookshelf/get
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/bookshelf/get", role = "student", description = "获取用户书架")
    public Response getUserBookshelf(Request request) {
        log.info("处理获取用户书架请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String cardNum = request.getSession().getUserId();
            String categoryName = request.getParam("categoryName");
            
            // 3. 调用服务层
            List<BookshelfView> bookshelf;
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                bookshelf = myBookshelfService.getUserBookshelfByCategory(cardNum, categoryName.trim());
            } else {
                bookshelf = myBookshelfService.getUserBookshelf(cardNum);
            }
            
            log.info("获取用户书架成功: cardNum={}, count={}", cardNum, bookshelf.size());
            return Response.Builder.success("查询成功", bookshelf);
            
        } catch (Exception e) {
            log.error("处理获取用户书架请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户分类列表
     * URI: library/bookshelf/categories
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/bookshelf/categories", role = "student", description = "获取用户分类列表")
    public Response getUserCategories(Request request) {
        log.info("处理获取用户分类列表请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String cardNum = request.getSession().getUserId();
            
            // 3. 调用服务层 - 从个人分类表获取
            List<String> categories = personalCategoryService.getCategoryNamesByCardNum(cardNum);
            
            log.info("获取用户分类列表成功: cardNum={}, count={}", cardNum, categories.size());
            return Response.Builder.success("查询成功", categories);
            
        } catch (Exception e) {
            log.error("处理获取用户分类列表请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 检查图书是否在书架中
     * URI: library/bookshelf/check
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/bookshelf/check", role = "student", description = "检查图书是否在书架中")
    public Response checkBookInShelf(Request request) {
        log.info("处理检查图书是否在书架中请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String cardNum = request.getSession().getUserId();
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
            
            // 3. 调用服务层
            boolean inShelf = myBookshelfService.isBookInShelf(cardNum, bookId);
            
            log.info("检查图书是否在书架中结果: cardNum={}, bookId={}, inShelf={}", 
                    cardNum, bookId, inShelf);
            return Response.Builder.success("查询成功", Map.of("inShelf", inShelf));
            
        } catch (Exception e) {
            log.error("处理检查图书是否在书架中请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取书架统计信息
     * URI: library/bookshelf/statistics
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/bookshelf/statistics", role = "student", description = "获取书架统计信息")
    public Response getBookshelfStatistics(Request request) {
        log.info("处理获取书架统计信息请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String cardNum = request.getSession().getUserId();
            
            // 3. 调用服务层
            Map<String, Object> statistics = myBookshelfService.getBookshelfStatistics(cardNum);
            
            log.info("获取书架统计信息成功: cardNum={}", cardNum);
            return Response.Builder.success("查询成功", statistics);
            
        } catch (Exception e) {
            log.error("处理获取书架统计信息请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
}
