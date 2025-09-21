package com.vcampus.server.core.library.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.server.core.library.entity.core.Book;
import com.vcampus.server.core.library.entity.search.PopularBook;
import com.vcampus.server.core.library.enums.BookStatus;
import com.vcampus.server.core.library.service.BookManagementService;
import com.vcampus.server.core.library.service.impl.BookManagementServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 用户图书搜索控制器
 * 提供学生和教师的图书搜索功能
 * 包括基础搜索、高级搜索、热门图书推荐等
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class UserBookSearchController {
    
    private final BookManagementService bookManagementService;
    
    public UserBookSearchController() {
        this.bookManagementService = new BookManagementServiceImpl();
    }
    
    // ==================== 基础搜索功能 ====================
    
    /**
     * 根据书名搜索图书
     * URI: library/user/search-by-title
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/search-by-title", role = "admin", description = "根据书名搜索图书")
    public Response searchBooksByTitle(Request request) {
        log.info("处理根据书名搜索图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String title = params.get("title");
            
            if (title == null || title.trim().isEmpty()) {
                return Response.Builder.badRequest("书名关键词不能为空");
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.searchBooksByTitle(title.trim());
            
            log.info("根据书名搜索图书成功: title={}, count={}", title, books.size());
            return Response.Builder.success("搜索成功", books);
            
        } catch (Exception e) {
            log.error("处理根据书名搜索图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据作者搜索图书
     * URI: library/user/search-by-author
     * 权限: student, teacher
     */


    @RouteMapping(uri = "library/user/search-by-author", role = "admin", description = "根据作者搜索图书")
    public Response searchBooksByAuthor(Request request) {
        log.info("处理根据作者搜索图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String author = params.get("author");
            
            if (author == null || author.trim().isEmpty()) {
                return Response.Builder.badRequest("作者不能为空");
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.searchBooksByAuthor(author.trim());
            
            log.info("根据作者搜索图书成功: author={}, count={}", author, books.size());
            return Response.Builder.success("搜索成功", books);
            
        } catch (Exception e) {
            log.error("处理根据作者搜索图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据出版社搜索图书
     * URI: library/user/search-by-publisher
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/search-by-publisher", role = "admin", description = "根据出版社搜索图书")
    public Response searchBooksByPublisher(Request request) {
        log.info("处理根据出版社搜索图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String publisher = params.get("publisher");
            
            if (publisher == null || publisher.trim().isEmpty()) {
                return Response.Builder.badRequest("出版社不能为空");
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.searchBooksByPublisher(publisher.trim());
            
            log.info("根据出版社搜索图书成功: publisher={}, count={}", publisher, books.size());
            return Response.Builder.success("搜索成功", books);
            
        } catch (Exception e) {
            log.error("处理根据出版社搜索图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据分类查询图书
     * URI: library/user/get-by-category
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-by-category", role = "admin", description = "根据分类查询图书")
    public Response getBooksByCategory(Request request) {
        log.info("处理根据分类查询图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String category = params.get("category");
            
            if (category == null || category.trim().isEmpty()) {
                return Response.Builder.badRequest("分类不能为空");
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.getBooksByCategory(category.trim());
            
            log.info("根据分类查询图书成功: category={}, count={}", category, books.size());
            return Response.Builder.success("查询成功", books);
            
        } catch (Exception e) {
            log.error("处理根据分类查询图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 复合搜索图书
     * URI: library/user/search
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/search", role = "student,teacher,admin", description = "复合搜索图书")
    public Response searchBooks(Request request) {
        log.info("处理复合搜索图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String title = params.get("title");
            String author = params.get("author");
            String publisher = params.get("publisher");
            String category = params.get("category");
            String statusStr = params.get("status");
            
            BookStatus status = null;
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                try {
                    status = BookStatus.valueOf(statusStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return Response.Builder.badRequest("无效的状态: " + statusStr);
                }
            }
            
            // 3. 处理分类参数 - 将分类名称转换为分类代码
            String categoryCode = null;
            if (category != null && !category.trim().isEmpty()) {
                categoryCode = convertCategoryNameToCode(category.trim());
                if (categoryCode == null) {
                    // 如果找不到对应的分类代码，返回空列表
                    log.warn("未找到分类对应的代码: {}", category);
                    return Response.Builder.success("搜索成功", new java.util.ArrayList<>());
                }
            }
            
            // 4. 调用服务层
            List<Book> books = bookManagementService.searchBooks(
                    title != null ? title.trim() : null,
                    author != null ? author.trim() : null,
                    publisher != null ? publisher.trim() : null,
                    categoryCode,
                    status
            );
            
            log.info("复合搜索图书成功: count={}", books.size());
            return Response.Builder.success("搜索成功", books);
            
        } catch (Exception e) {
            log.error("处理复合搜索图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 智能搜索图书
     * URI: library/user/smartSearch
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/smartSearch", role = "student,teacher,admin", description = "智能搜索图书")
    public Response smartSearchBooks(Request request) {
        log.info("处理智能搜索图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String keyword = params.get("keyword");
            String category = params.get("category");
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return Response.Builder.badRequest("搜索关键词不能为空");
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.smartSearchBooks(keyword.trim());
            
            // 4. 如果指定了分类，进行筛选
            if (category != null && !category.trim().isEmpty() && !"全部".equals(category.trim())) {
                // 将分类名称转换为分类代码
                String categoryCode = convertCategoryNameToCode(category.trim());
                if (categoryCode != null) {
                    books = books.stream()
                        .filter(book -> categoryCode.equals(book.getCategory()))
                        .collect(java.util.stream.Collectors.toList());
                } else {
                    // 如果找不到对应的分类代码，返回空列表
                    books = new java.util.ArrayList<>();
                }
            }
            
            // 5. 转换为客户端期望的格式
            List<Map<String, Object>> bookMaps = books.stream()
                .map(this::convertBookToMap)
                .collect(java.util.stream.Collectors.toList());
            
            log.info("智能搜索图书成功: count={}, category={}", books.size(), category);
            return Response.Builder.success("搜索成功", bookMaps);
            
        } catch (Exception e) {
            log.error("处理智能搜索图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 图书详情查询 ====================
    
    /**
     * 根据ID查询图书详情
     * URI: library/user/get-by-id
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-by-id", role = "admin", description = "根据ID查询图书详情")
    public Response getBookById(Request request) {
        log.info("处理根据ID查询图书详情请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String bookIdStr = params.get("bookId");
            
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
            Book book = bookManagementService.getBookById(bookId);
            
            if (book != null) {
                log.info("查询图书详情成功: bookId={}, title={}", bookId, book.getTitle());
                return Response.Builder.success("查询成功", book);
            } else {
                log.warn("图书不存在: bookId={}", bookId);
                return Response.Builder.notFound("图书不存在");
            }
            
        } catch (Exception e) {
            log.error("处理根据ID查询图书详情请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据ISBN查询图书详情
     * URI: library/user/get-by-isbn
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-by-isbn", role = "admin", description = "根据ISBN查询图书详情")
    public Response getBookByIsbn(Request request) {
        log.info("处理根据ISBN查询图书详情请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String isbn = params.get("isbn");
            
            if (isbn == null || isbn.trim().isEmpty()) {
                return Response.Builder.badRequest("ISBN不能为空");
            }
            
            // 3. 调用服务层
            Book book = bookManagementService.getBookByIsbn(isbn.trim());
            
            if (book != null) {
                log.info("根据ISBN查询图书详情成功: isbn={}, title={}", isbn, book.getTitle());
                return Response.Builder.success("查询成功", book);
            } else {
                log.warn("图书不存在: isbn={}", isbn);
                return Response.Builder.notFound("图书不存在");
            }
            
        } catch (Exception e) {
            log.error("处理根据ISBN查询图书详情请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 分页查询 ====================
    
    /**
     * 分页查询图书
     * URI: library/user/get-by-page
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-by-page", role = "student,teacher,admin", description = "分页查询图书")
    public Response getBooksByPage(Request request) {
        log.info("处理分页查询图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String pageStr = params.get("page");
            String sizeStr = params.get("size");
            
            int page = 1;
            int size = 10;
            
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try {
                    page = Integer.valueOf(pageStr);
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }
            
            if (sizeStr != null && !sizeStr.trim().isEmpty()) {
                try {
                    size = Integer.valueOf(sizeStr);
                    if (size < 1) size = 10;
                    if (size > 50) size = 50; // 用户端限制最大页面大小
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.getBooksByPage(page, size);
            
            log.info("分页查询图书成功: page={}, size={}, count={}", page, size, books.size());
            return Response.Builder.success("查询成功", books);
            
        } catch (Exception e) {
            log.error("处理分页查询图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 可借阅图书查询 ====================
    
    /**
     * 查询可借阅的图书
     * URI: library/user/get-available
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-available", role = "student,teacher,admin", description = "查询可借阅的图书")
    public Response getAvailableBooks(Request request) {
        log.info("处理查询可借阅图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 调用服务层 - 查询状态为可借阅的图书
            List<Book> books = bookManagementService.getBooksByStatus(BookStatus.AVAILABLE);
            
            log.info("查询可借阅图书成功: count={}", books.size());
            return Response.Builder.success("查询成功", books);
            
        } catch (Exception e) {
            log.error("处理查询可借阅图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 查询可借阅图书（按分类）
     * URI: library/user/get-available-by-category
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-available-by-category", role = "admin", description = "查询可借阅图书（按分类）")
    public Response getAvailableBooksByCategory(Request request) {
        log.info("处理按分类查询可借阅图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String category = params.get("category");
            
            if (category == null || category.trim().isEmpty()) {
                return Response.Builder.badRequest("分类不能为空");
            }
            
            // 3. 调用服务层 - 复合查询：指定分类且可借阅
            List<Book> books = bookManagementService.searchBooks(
                    null, null, null, category.trim(), BookStatus.AVAILABLE
            );
            
            log.info("按分类查询可借阅图书成功: category={}, count={}", category, books.size());
            return Response.Builder.success("查询成功", books);
            
        } catch (Exception e) {
            log.error("处理按分类查询可借阅图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 统计信息 ====================
    
    /**
     * 获取图书统计信息
     * URI: library/user/get-statistics
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-statistics", role = "admin", description = "获取图书统计信息")
    public Response getBookStatistics(Request request) {
        log.info("处理获取图书统计信息请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 调用服务层获取统计信息
            long totalBooks = bookManagementService.countAllBooks();
            long availableBooks = bookManagementService.countAvailableBooks();
            long borrowedBooks = bookManagementService.countBorrowedBooks();
            Map<String, Long> categoryStats = bookManagementService.countBooksByCategory();
            
            Map<String, Object> statistics = Map.of(
                "totalBooks", totalBooks,
                "availableBooks", availableBooks,
                "borrowedBooks", borrowedBooks,
                "categoryStatistics", categoryStats
            );
            
            log.info("获取图书统计信息成功: total={}, available={}, borrowed={}", 
                    totalBooks, availableBooks, borrowedBooks);
            return Response.Builder.success("查询成功", statistics);
            
        } catch (Exception e) {
            log.error("处理获取图书统计信息请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取分类统计信息
     * URI: library/user/get-category-statistics
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-category-statistics", role = "admin", description = "获取分类统计信息")
    public Response getCategoryStatistics(Request request) {
        log.info("处理获取分类统计信息请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 调用服务层
            Map<String, Long> categoryStats = bookManagementService.countBooksByCategory();
            
            log.info("获取分类统计信息成功: {}", categoryStats);
            return Response.Builder.success("查询成功", categoryStats);
            
        } catch (Exception e) {
            log.error("处理获取分类统计信息请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取分类列表
     * URI: library/user/get-categories
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-categories", role = "student,teacher,admin", description = "获取分类列表")
    public Response getCategories(Request request) {
        log.info("处理获取分类列表请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 调用服务层获取分类列表
            List<Map<String, Object>> categories = bookManagementService.getAllCategories();
            
            log.info("获取分类列表成功: count={}", categories.size());
            return Response.Builder.success("查询成功", categories);
            
        } catch (Exception e) {
            log.error("处理获取分类列表请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 热门推荐 ====================
    
    /**
     * 获取热门图书推荐
     * URI: library/user/get-popular-books
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-popular-books", role = "student,teacher,admin", description = "获取热门图书推荐")
    public Response getPopularBooks(Request request) {
        log.info("处理获取热门图书推荐请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String limitStr = params.get("limit");
            String category = params.get("category");
            
            int limit = 10; // 默认推荐10本
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                try {
                    limit = Integer.valueOf(limitStr);
                    if (limit <= 0 || limit > 50) {
                        limit = 10; // 限制在1-50之间
                    }
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }
            
            // 3. 调用服务层 - 返回在库的图书作为热门图书
            List<Book> popularBooks = bookManagementService.getBooksByStatus(BookStatus.IN_LIBRARY);
            
            // 4. 如果指定了分类，进行筛选
            if (category != null && !category.trim().isEmpty() && !"全部".equals(category.trim())) {
                // 将分类名称转换为分类代码
                String categoryCode = convertCategoryNameToCode(category.trim());
                if (categoryCode != null) {
                    popularBooks = popularBooks.stream()
                        .filter(book -> categoryCode.equals(book.getCategory()))
                        .collect(java.util.stream.Collectors.toList());
                } else {
                    // 如果找不到对应的分类代码，返回空列表
                    popularBooks = new java.util.ArrayList<>();
                }
            }
            
            // 5. 限制数量
            if (popularBooks.size() > limit) {
                popularBooks = popularBooks.subList(0, limit);
            }
            
            // 6. 转换为客户端期望的格式
            List<Map<String, Object>> bookMaps = popularBooks.stream()
                .map(this::convertBookToMap)
                .collect(java.util.stream.Collectors.toList());
            
            log.info("获取热门图书推荐成功: count={}, category={}", bookMaps.size(), category);
            return Response.Builder.success("查询成功", bookMaps);
            
        } catch (Exception e) {
            log.error("处理获取热门图书推荐请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取分类热门图书
     * URI: library/user/get-popular-books-by-category
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/user/get-popular-books-by-category", role = "student,teacher,admin", description = "获取分类热门图书")
    public Response getPopularBooksByCategory(Request request) {
        log.info("处理获取分类热门图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String category = params.get("category");
            String limitStr = params.get("limit");
            
            if (category == null || category.trim().isEmpty()) {
                return Response.Builder.badRequest("分类不能为空");
            }
            
            int limit = 10; // 默认推荐10本
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                try {
                    limit = Integer.valueOf(limitStr);
                    if (limit <= 0 || limit > 50) {
                        limit = 10; // 限制在1-50之间
                    }
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }
            
            // 3. 调用服务层 - 查询指定分类的可借阅图书
            List<Book> books = bookManagementService.searchBooks(
                    null, null, null, category.trim(), BookStatus.AVAILABLE
            );
            
            if (books.size() > limit) {
                books = books.subList(0, limit);
            }
            
            log.info("获取分类热门图书成功: category={}, count={}", category, books.size());
            return Response.Builder.success("查询成功", books);
            
        } catch (Exception e) {
            log.error("处理获取分类热门图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 将Book对象转换为客户端期望的Map格式
     */
    private Map<String, Object> convertBookToMap(Book book) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", book.getBookId());
        map.put("title", book.getTitle());
        map.put("author", book.getAuthor());
        map.put("publisher", book.getPublisher());
        map.put("category", book.getCategory());
        // 将状态枚举转换为中文描述
        map.put("status", book.getStatus() != null ? book.getStatus().getDescription() : "未知");
        // 库存显示可借数量
        map.put("stock", book.getAvailQty() != null ? book.getAvailQty() : 0);
        return map;
    }
    
    /**
     * 将分类名称转换为分类代码
     */
    private String convertCategoryNameToCode(String categoryName) {
        // 硬编码的分类名称到代码的映射
        Map<String, String> categoryMapping = new java.util.HashMap<>();
        categoryMapping.put("马克思主义、列宁主义、毛泽东思想、邓小平理论", "A");
        categoryMapping.put("哲学、宗教", "B");
        categoryMapping.put("社会科学总论", "C");
        categoryMapping.put("政治、法律", "D");
        categoryMapping.put("军事", "E");
        categoryMapping.put("经济", "F");
        categoryMapping.put("文化、科学、教育、体育", "G");
        categoryMapping.put("语言、文字", "H");
        categoryMapping.put("文学", "I");
        categoryMapping.put("艺术", "J");
        categoryMapping.put("历史、地理", "K");
        categoryMapping.put("自然科学总论", "N");
        categoryMapping.put("数理科学和化学", "O");
        categoryMapping.put("天文学、地球科学", "P");
        categoryMapping.put("生物科学", "Q");
        categoryMapping.put("医药、卫生", "R");
        categoryMapping.put("农业科学", "S");
        categoryMapping.put("工业技术", "T");
        categoryMapping.put("交通运输", "U");
        categoryMapping.put("航空、航天", "V");
        categoryMapping.put("环境科学、安全科学", "X");
        categoryMapping.put("综合性图书", "Z");
        
        return categoryMapping.get(categoryName);
    }
}
