package com.vcampus.server.core.library.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.server.core.library.entity.core.Book;
import com.vcampus.server.core.library.entity.result.BookManagementResult;
import com.vcampus.server.core.library.enums.BookStatus;
import com.vcampus.server.core.library.service.BookManagementService;
import com.vcampus.server.core.library.service.impl.BookManagementServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员图书管理控制器
 * 提供管理员用户的图书管理功能
 * 包括图书的增删改查、批量操作、导入导出等
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class AdminBookManagementController {
    
    private final BookManagementService bookManagementService;
    
    public AdminBookManagementController() {
        this.bookManagementService = new BookManagementServiceImpl();
    }
    
    // ==================== 基础CRUD操作 ====================
    
    /**
     * 添加图书
     * URI: library/admin/book/add
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/book/add", role = "admin", description = "添加图书")
    public Response addBook(Request request) {
        log.info("处理添加图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            Book book = extractBookFromParams(params);
            
            if (book == null) {
                return Response.Builder.badRequest("图书信息不完整");
            }
            
            // 3. 调用服务层
            BookManagementResult result = bookManagementService.addBook(book);
            
            if (result.isSuccess()) {
                log.info("添加图书成功: bookId={}, title={}", 
                        result.getBook() != null ? result.getBook().getBookId() : "unknown", 
                        book.getTitle());
                return Response.Builder.success(result.getMessage(), result.getBook());
            } else {
                log.warn("添加图书失败: title={}, reason={}", book.getTitle(), result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理添加图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 更新图书信息
     * URI: library/admin/book/update
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/book/update", role = "admin", description = "更新图书信息")
    public Response updateBook(Request request) {
        log.info("处理更新图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            Book book = extractBookFromParams(params);
            
            if (book == null || book.getBookId() == null) {
                return Response.Builder.badRequest("图书信息不完整，缺少图书ID");
            }
            
            // 3. 调用服务层
            BookManagementResult result = bookManagementService.updateBook(book);
            
            if (result.isSuccess()) {
                log.info("更新图书成功: bookId={}, title={}", book.getBookId(), book.getTitle());
                return Response.Builder.success(result.getMessage(), result.getBook());
            } else {
                log.warn("更新图书失败: bookId={}, reason={}", book.getBookId(), result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理更新图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 删除图书
     * URI: library/admin/book/delete
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/book/delete", role = "admin", description = "删除图书")
    public Response deleteBook(Request request) {
        log.info("处理删除图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
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
            BookManagementResult result = bookManagementService.deleteBook(bookId);
            
            if (result.isSuccess()) {
                log.info("删除图书成功: bookId={}", bookId);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("删除图书失败: bookId={}, reason={}", bookId, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理删除图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询图书
     * URI: library/admin/book/get-by-id
     * 权限: admin
     */
    public Response getBookById(Request request) {
        log.info("处理根据ID查询图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
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
                log.info("查询图书成功: bookId={}, title={}", bookId, book.getTitle());
                return Response.Builder.success("查询成功", book);
            } else {
                log.warn("图书不存在: bookId={}", bookId);
                return Response.Builder.notFound("图书不存在");
            }
            
        } catch (Exception e) {
            log.error("处理根据ID查询图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据ISBN查询图书
     * URI: library/admin/book/get-by-isbn
     * 权限: admin
     */
    public Response getBookByIsbn(Request request) {
        log.info("处理根据ISBN查询图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
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
                log.info("根据ISBN查询图书成功: isbn={}, title={}", isbn, book.getTitle());
                return Response.Builder.success("查询成功", book);
            } else {
                log.warn("图书不存在: isbn={}", isbn);
                return Response.Builder.notFound("图书不存在");
            }
            
        } catch (Exception e) {
            log.error("处理根据ISBN查询图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取图书详情
     * URI: library/admin/book/detail
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/book/detail", role = "admin", description = "获取图书详情")
    public Response getBookDetail(Request request) {
        log.info("处理获取图书详情请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            String bookIdStr = request.getParam("bookId");
            String isbn = request.getParam("isbn");
            
            if ((bookIdStr == null || bookIdStr.trim().isEmpty()) && 
                (isbn == null || isbn.trim().isEmpty())) {
                return Response.Builder.badRequest("图书ID或ISBN不能同时为空");
            }
            
            // 3. 查询图书
            Book book = null;
            if (bookIdStr != null && !bookIdStr.trim().isEmpty()) {
                try {
                    Integer bookId = Integer.valueOf(bookIdStr);
                    book = bookManagementService.getBookById(bookId);
                } catch (NumberFormatException e) {
                    return Response.Builder.badRequest("图书ID格式错误");
                }
            } else if (isbn != null && !isbn.trim().isEmpty()) {
                book = bookManagementService.getBookByIsbn(isbn.trim());
            }
            
            if (book == null) {
                return Response.Builder.notFound("图书不存在");
            }
            
            log.info("获取图书详情成功: bookId={}, title={}", book.getBookId(), book.getTitle());
            return Response.Builder.success("获取图书详情成功", book);
            
        } catch (Exception e) {
            log.error("处理获取图书详情请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 查询功能 ====================
    
    /**
     * 查询所有图书
     * URI: library/admin/book/get-all
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/book/get-all", role = "admin", description = "查询所有图书")
    public Response getAllBooks(Request request) {
        log.info("处理查询所有图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 调用服务层
            List<Book> books = bookManagementService.getAllBooks();
            
            // 3. 转换为Map格式以兼容客户端
            List<Map<String, Object>> bookMaps = books.stream()
                .map(book -> {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", book.getBookId());
                    bookMap.put("isbn", book.getIsbn());
                    bookMap.put("title", book.getTitle());
                    bookMap.put("author", book.getAuthor());
                    bookMap.put("publisher", book.getPublisher());
                    bookMap.put("publishDate", book.getPublishDate());
                    bookMap.put("category", book.getCategory());
                    bookMap.put("location", book.getLocation());
                    bookMap.put("totalQty", book.getTotalQty());
                    bookMap.put("stock", book.getAvailQty()); // 库存使用可借数量
                    bookMap.put("status", book.getStatus() != null ? book.getStatus().name() : "UNKNOWN");
                    bookMap.put("addTime", book.getAddTime());
                    bookMap.put("updateTime", book.getUpdateTime());
                    return bookMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            log.info("查询所有图书成功: count={}", books.size());
            return Response.Builder.success("查询成功", bookMaps);
            
        } catch (Exception e) {
            log.error("处理查询所有图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询图书
     * URI: library/admin/book/get-by-page
     * 权限: admin
     */
    public Response getBooksByPage(Request request) {
        log.info("处理分页查询图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
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
                    if (size > 100) size = 100; // 限制最大页面大小
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
    
    /**
     * 根据书名搜索图书
     * URI: library/admin/book/search-by-title
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/book/search-by-title", role = "admin", description = "根据书名搜索图书")
    public Response searchBooksByTitle(Request request) {
        log.info("处理根据书名搜索图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String title = params.get("title");
            
            if (title == null || title.trim().isEmpty()) {
                return Response.Builder.badRequest("书名关键词不能为空");
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.searchBooksByTitle(title.trim());
            
            // 4. 转换为Map格式以兼容客户端
            List<Map<String, Object>> bookMaps = books.stream()
                .map(book -> {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", book.getBookId());
                    bookMap.put("isbn", book.getIsbn());
                    bookMap.put("title", book.getTitle());
                    bookMap.put("author", book.getAuthor());
                    bookMap.put("publisher", book.getPublisher());
                    bookMap.put("publishDate", book.getPublishDate());
                    bookMap.put("category", book.getCategory());
                    bookMap.put("location", book.getLocation());
                    bookMap.put("totalQty", book.getTotalQty());
                    bookMap.put("stock", book.getAvailQty()); // 库存使用可借数量
                    bookMap.put("status", book.getStatus() != null ? book.getStatus().name() : "UNKNOWN");
                    bookMap.put("addTime", book.getAddTime());
                    bookMap.put("updateTime", book.getUpdateTime());
                    return bookMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            log.info("根据书名搜索图书成功: title={}, count={}", title, books.size());
            return Response.Builder.success("搜索成功", bookMaps);
            
        } catch (Exception e) {
            log.error("处理根据书名搜索图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据作者搜索图书
     * URI: library/admin/book/search-by-author
     * 权限: admin
     */
    public Response searchBooksByAuthor(Request request) {
        log.info("处理根据作者搜索图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String author = params.get("author");
            
            if (author == null || author.trim().isEmpty()) {
                return Response.Builder.badRequest("作者不能为空");
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.searchBooksByAuthor(author.trim());
            
            // 4. 转换为Map格式以兼容客户端
            List<Map<String, Object>> bookMaps = books.stream()
                .map(book -> {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", book.getBookId());
                    bookMap.put("isbn", book.getIsbn());
                    bookMap.put("title", book.getTitle());
                    bookMap.put("author", book.getAuthor());
                    bookMap.put("publisher", book.getPublisher());
                    bookMap.put("publishDate", book.getPublishDate());
                    bookMap.put("category", book.getCategory());
                    bookMap.put("location", book.getLocation());
                    bookMap.put("totalQty", book.getTotalQty());
                    bookMap.put("stock", book.getAvailQty()); // 库存使用可借数量
                    bookMap.put("status", book.getStatus() != null ? book.getStatus().name() : "UNKNOWN");
                    bookMap.put("addTime", book.getAddTime());
                    bookMap.put("updateTime", book.getUpdateTime());
                    return bookMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            log.info("根据作者搜索图书成功: author={}, count={}", author, books.size());
            return Response.Builder.success("搜索成功", bookMaps);
            
        } catch (Exception e) {
            log.error("处理根据作者搜索图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据出版社搜索图书
     * URI: library/admin/book/search-by-publisher
     * 权限: admin
     */
    public Response searchBooksByPublisher(Request request) {
        log.info("处理根据出版社搜索图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String publisher = params.get("publisher");
            
            if (publisher == null || publisher.trim().isEmpty()) {
                return Response.Builder.badRequest("出版社不能为空");
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.searchBooksByPublisher(publisher.trim());
            
            // 4. 转换为Map格式以兼容客户端
            List<Map<String, Object>> bookMaps = books.stream()
                .map(book -> {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", book.getBookId());
                    bookMap.put("isbn", book.getIsbn());
                    bookMap.put("title", book.getTitle());
                    bookMap.put("author", book.getAuthor());
                    bookMap.put("publisher", book.getPublisher());
                    bookMap.put("publishDate", book.getPublishDate());
                    bookMap.put("category", book.getCategory());
                    bookMap.put("location", book.getLocation());
                    bookMap.put("totalQty", book.getTotalQty());
                    bookMap.put("stock", book.getAvailQty()); // 库存使用可借数量
                    bookMap.put("status", book.getStatus() != null ? book.getStatus().name() : "UNKNOWN");
                    bookMap.put("addTime", book.getAddTime());
                    bookMap.put("updateTime", book.getUpdateTime());
                    return bookMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            log.info("根据出版社搜索图书成功: publisher={}, count={}", publisher, books.size());
            return Response.Builder.success("搜索成功", bookMaps);
            
        } catch (Exception e) {
            log.error("处理根据出版社搜索图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据分类查询图书
     * URI: library/admin/book/get-by-category
     * 权限: admin
     */
    public Response getBooksByCategory(Request request) {
        log.info("处理根据分类查询图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String category = params.get("category");
            
            if (category == null || category.trim().isEmpty()) {
                return Response.Builder.badRequest("分类不能为空");
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.getBooksByCategory(category.trim());
            
            // 4. 转换为Map格式以兼容客户端
            List<Map<String, Object>> bookMaps = books.stream()
                .map(book -> {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", book.getBookId());
                    bookMap.put("isbn", book.getIsbn());
                    bookMap.put("title", book.getTitle());
                    bookMap.put("author", book.getAuthor());
                    bookMap.put("publisher", book.getPublisher());
                    bookMap.put("publishDate", book.getPublishDate());
                    bookMap.put("category", book.getCategory());
                    bookMap.put("location", book.getLocation());
                    bookMap.put("totalQty", book.getTotalQty());
                    bookMap.put("stock", book.getAvailQty()); // 库存使用可借数量
                    bookMap.put("status", book.getStatus() != null ? book.getStatus().name() : "UNKNOWN");
                    bookMap.put("addTime", book.getAddTime());
                    bookMap.put("updateTime", book.getUpdateTime());
                    return bookMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            log.info("根据分类查询图书成功: category={}, count={}", category, books.size());
            return Response.Builder.success("查询成功", bookMaps);
            
        } catch (Exception e) {
            log.error("处理根据分类查询图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据状态查询图书
     * URI: library/admin/book/get-by-status
     * 权限: admin
     */
    public Response getBooksByStatus(Request request) {
        log.info("处理根据状态查询图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String statusStr = params.get("status");
            
            if (statusStr == null || statusStr.trim().isEmpty()) {
                return Response.Builder.badRequest("状态不能为空");
            }
            
            BookStatus status;
            try {
                status = BookStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.Builder.badRequest("无效的状态: " + statusStr);
            }
            
            // 3. 调用服务层
            List<Book> books = bookManagementService.getBooksByStatus(status);
            
            log.info("根据状态查询图书成功: status={}, count={}", status, books.size());
            return Response.Builder.success("查询成功", books);
            
        } catch (Exception e) {
            log.error("处理根据状态查询图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 复合查询图书（支持模糊综合搜索）
     * URI: library/admin/book/search
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/book/search", role = "admin", description = "复合查询图书")
    public Response searchBooks(Request request) {
        log.info("处理复合查询图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String keyword = params.get("keyword");
            String title = params.get("title");
            String author = params.get("author");
            String publisher = params.get("publisher");
            String category = params.get("category");
            String statusStr = params.get("status");
            
            List<Book> books;
            
            // 3. 判断搜索类型
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 模糊综合搜索 - 使用智能搜索
                log.info("执行模糊综合搜索: keyword={}", keyword);
                books = bookManagementService.smartSearchBooks(keyword.trim());
            } else {
                // 传统精确搜索
                BookStatus status = null;
                if (statusStr != null && !statusStr.trim().isEmpty()) {
                    try {
                        status = BookStatus.valueOf(statusStr.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return Response.Builder.badRequest("无效的状态: " + statusStr);
                    }
                }
                
                books = bookManagementService.searchBooks(
                        title != null ? title.trim() : null,
                        author != null ? author.trim() : null,
                        publisher != null ? publisher.trim() : null,
                        category != null ? category.trim() : null,
                        status
                );
            }
            
            // 4. 转换为Map格式以兼容客户端
            List<Map<String, Object>> bookMaps = books.stream()
                .map(book -> {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", book.getBookId());
                    bookMap.put("isbn", book.getIsbn());
                    bookMap.put("title", book.getTitle());
                    bookMap.put("author", book.getAuthor());
                    bookMap.put("publisher", book.getPublisher());
                    bookMap.put("publishDate", book.getPublishDate());
                    bookMap.put("category", book.getCategory());
                    bookMap.put("location", book.getLocation());
                    bookMap.put("totalQty", book.getTotalQty());
                    bookMap.put("stock", book.getAvailQty()); // 库存使用可借数量
                    bookMap.put("status", book.getStatus() != null ? book.getStatus().name() : "UNKNOWN");
                    bookMap.put("addTime", book.getAddTime());
                    bookMap.put("updateTime", book.getUpdateTime());
                    return bookMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            log.info("复合查询图书成功: count={}", books.size());
            return Response.Builder.success("查询成功", bookMaps);
            
        } catch (Exception e) {
            log.error("处理复合查询图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 批量操作 ====================
    
    /**
     * 批量添加图书
     * URI: library/admin/book/batch-add
     * 权限: admin
     */
    public Response batchAddBooks(Request request) {
        log.info("处理批量添加图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取 - 这里需要从请求体中获取图书列表
            // 暂时返回未实现提示
            log.warn("批量添加图书功能需要从请求体中解析图书列表，暂未实现");
            return Response.Builder.badRequest("批量添加图书功能暂未实现，需要从请求体中解析图书列表");
            
        } catch (Exception e) {
            log.error("处理批量添加图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 批量导入图书
     * URI: library/admin/batch-import
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/batch-import", role = "admin", description = "批量导入图书")
    public Response batchImportBooks(Request request) {
        log.info("处理批量导入图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            Book book = extractBookFromParams(params);
            
            if (book == null) {
                return Response.Builder.badRequest("图书信息不完整");
            }
            
            // 3. 调用服务层
            BookManagementResult result = bookManagementService.addBook(book);
            
            if (result.isSuccess()) {
                log.info("批量导入图书成功: title={}", book.getTitle());
                return Response.Builder.success(result.getMessage(), result.getBook());
            } else {
                log.warn("批量导入图书失败: title={}, reason={}", book.getTitle(), result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理批量导入图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新图书
     * URI: library/admin/book/batch-update
     * 权限: admin
     */
    public Response batchUpdateBooks(Request request) {
        log.info("处理批量更新图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取 - 这里需要从请求体中获取图书列表
            // 暂时返回未实现提示
            log.warn("批量更新图书功能需要从请求体中解析图书列表，暂未实现");
            return Response.Builder.badRequest("批量更新图书功能暂未实现，需要从请求体中解析图书列表");
            
        } catch (Exception e) {
            log.error("处理批量更新图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除图书
     * URI: library/admin/book/batch-delete
     * 权限: admin
     */
    public Response batchDeleteBooks(Request request) {
        log.info("处理批量删除图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String bookIdsStr = params.get("bookIds");
            
            if (bookIdsStr == null || bookIdsStr.trim().isEmpty()) {
                return Response.Builder.badRequest("图书ID列表不能为空");
            }
            
            // 3. 解析图书ID列表
            List<Integer> bookIds = java.util.Arrays.stream(bookIdsStr.split(","))
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .collect(java.util.stream.Collectors.toList());
            
            // 4. 调用服务层
            BookManagementResult result = bookManagementService.batchDeleteBooks(bookIds);
            
            if (result.isSuccess()) {
                log.info("批量删除图书成功: count={}", bookIds.size());
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("批量删除图书失败: reason={}", result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理批量删除图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新图书状态
     * URI: library/admin/book/batch-update-status
     * 权限: admin
     */
    public Response batchUpdateBookStatus(Request request) {
        log.info("处理批量更新图书状态请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String bookIdsStr = params.get("bookIds");
            String statusStr = params.get("status");
            
            if (bookIdsStr == null || bookIdsStr.trim().isEmpty()) {
                return Response.Builder.badRequest("图书ID列表不能为空");
            }
            if (statusStr == null || statusStr.trim().isEmpty()) {
                return Response.Builder.badRequest("状态不能为空");
            }
            
            // 3. 解析参数
            List<Integer> bookIds = java.util.Arrays.stream(bookIdsStr.split(","))
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .collect(java.util.stream.Collectors.toList());
            
            BookStatus status;
            try {
                status = BookStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.Builder.badRequest("无效的状态: " + statusStr);
            }
            
            // 4. 调用服务层
            BookManagementResult result = bookManagementService.batchUpdateBookStatus(bookIds, status);
            
            if (result.isSuccess()) {
                log.info("批量更新图书状态成功: count={}, status={}", bookIds.size(), status);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("批量更新图书状态失败: reason={}", result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理批量更新图书状态请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 统计功能 ====================
    
    /**
     * 统计图书总数
     * URI: library/admin/book/count-all
     * 权限: admin
     */
    public Response countAllBooks(Request request) {
        log.info("处理统计图书总数请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 调用服务层
            long count = bookManagementService.countAllBooks();
            
            log.info("统计图书总数成功: count={}", count);
            return Response.Builder.success("统计成功", count);
            
        } catch (Exception e) {
            log.error("处理统计图书总数请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 统计各状态图书数量
     * URI: library/admin/book/count-by-status
     * 权限: admin
     */
    public Response countBooksByStatus(Request request) {
        log.info("处理统计各状态图书数量请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 调用服务层
            Map<BookStatus, Long> statistics = bookManagementService.countBooksByStatus();
            
            log.info("统计各状态图书数量成功: {}", statistics);
            return Response.Builder.success("统计成功", statistics);
            
        } catch (Exception e) {
            log.error("处理统计各状态图书数量请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 统计各分类图书数量
     * URI: library/admin/book/count-by-category
     * 权限: admin
     */
    public Response countBooksByCategory(Request request) {
        log.info("处理统计各分类图书数量请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 调用服务层
            Map<String, Long> statistics = bookManagementService.countBooksByCategory();
            
            log.info("统计各分类图书数量成功: {}", statistics);
            return Response.Builder.success("统计成功", statistics);
            
        } catch (Exception e) {
            log.error("处理统计各分类图书数量请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 统计可借阅图书数量
     * URI: library/admin/book/count-available
     * 权限: admin
     */
    public Response countAvailableBooks(Request request) {
        log.info("处理统计可借阅图书数量请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 调用服务层
            long count = bookManagementService.countAvailableBooks();
            
            log.info("统计可借阅图书数量成功: count={}", count);
            return Response.Builder.success("统计成功", count);
            
        } catch (Exception e) {
            log.error("处理统计可借阅图书数量请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 统计借出图书数量
     * URI: library/admin/book/count-borrowed
     * 权限: admin
     */
    public Response countBorrowedBooks(Request request) {
        log.info("处理统计借出图书数量请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 调用服务层
            long count = bookManagementService.countBorrowedBooks();
            
            log.info("统计借出图书数量成功: count={}", count);
            return Response.Builder.success("统计成功", count);
            
        } catch (Exception e) {
            log.error("处理统计借出图书数量请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 库存管理 ====================
    
    /**
     * 更新图书副本数量
     * URI: library/admin/book/update-copies
     * 权限: admin
     */
    public Response updateBookCopies(Request request) {
        log.info("处理更新图书副本数量请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String bookIdStr = params.get("bookId");
            String totalCopiesStr = params.get("totalCopies");
            String availableCopiesStr = params.get("availableCopies");
            
            if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("图书ID不能为空");
            }
            if (totalCopiesStr == null || totalCopiesStr.trim().isEmpty()) {
                return Response.Builder.badRequest("总副本数不能为空");
            }
            if (availableCopiesStr == null || availableCopiesStr.trim().isEmpty()) {
                return Response.Builder.badRequest("可借副本数不能为空");
            }
            
            Integer bookId;
            Integer totalCopies;
            Integer availableCopies;
            
            try {
                bookId = Integer.valueOf(bookIdStr);
                totalCopies = Integer.valueOf(totalCopiesStr);
                availableCopies = Integer.valueOf(availableCopiesStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("参数格式错误");
            }
            
            // 3. 调用服务层
            BookManagementResult result = bookManagementService.updateBookCopies(bookId, totalCopies, availableCopies);
            
            if (result.isSuccess()) {
                log.info("更新图书副本数量成功: bookId={}, totalCopies={}, availableCopies={}", 
                        bookId, totalCopies, availableCopies);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("更新图书副本数量失败: bookId={}, reason={}", bookId, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理更新图书副本数量请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 增加图书副本
     * URI: library/admin/book/add-copies
     * 权限: admin
     */
    public Response addBookCopies(Request request) {
        log.info("处理增加图书副本请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String bookIdStr = params.get("bookId");
            String copiesStr = params.get("copies");
            
            if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("图书ID不能为空");
            }
            if (copiesStr == null || copiesStr.trim().isEmpty()) {
                return Response.Builder.badRequest("增加的副本数不能为空");
            }
            
            Integer bookId;
            Integer copies;
            
            try {
                bookId = Integer.valueOf(bookIdStr);
                copies = Integer.valueOf(copiesStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("参数格式错误");
            }
            
            // 3. 调用服务层
            BookManagementResult result = bookManagementService.addBookCopies(bookId, copies);
            
            if (result.isSuccess()) {
                log.info("增加图书副本成功: bookId={}, copies={}", bookId, copies);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("增加图书副本失败: bookId={}, reason={}", bookId, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理增加图书副本请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 减少图书副本
     * URI: library/admin/book/reduce-copies
     * 权限: admin
     */
    public Response reduceBookCopies(Request request) {
        log.info("处理减少图书副本请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String bookIdStr = params.get("bookId");
            String copiesStr = params.get("copies");
            
            if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("图书ID不能为空");
            }
            if (copiesStr == null || copiesStr.trim().isEmpty()) {
                return Response.Builder.badRequest("减少的副本数不能为空");
            }
            
            Integer bookId;
            Integer copies;
            
            try {
                bookId = Integer.valueOf(bookIdStr);
                copies = Integer.valueOf(copiesStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("参数格式错误");
            }
            
            // 3. 调用服务层
            BookManagementResult result = bookManagementService.reduceBookCopies(bookId, copies);
            
            if (result.isSuccess()) {
                log.info("减少图书副本成功: bookId={}, copies={}", bookId, copies);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("减少图书副本失败: bookId={}, reason={}", bookId, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理减少图书副本请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 数据验证 ====================
    
    /**
     * 验证图书信息
     * URI: library/admin/book/validate
     * 权限: admin
     */
    public Response validateBook(Request request) {
        log.info("处理验证图书信息请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            Book book = extractBookFromParams(params);
            
            if (book == null) {
                return Response.Builder.badRequest("图书信息不完整");
            }
            
            // 3. 调用服务层
            BookManagementResult result = bookManagementService.validateBook(book);
            
            if (result.isSuccess()) {
                log.info("验证图书信息成功: title={}", book.getTitle());
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("验证图书信息失败: title={}, reason={}", book.getTitle(), result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理验证图书信息请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 检查ISBN是否已存在
     * URI: library/admin/book/check-isbn
     * 权限: admin
     */
    public Response checkIsbnExists(Request request) {
        log.info("处理检查ISBN是否存在请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String isbn = params.get("isbn");
            String excludeBookIdStr = params.get("excludeBookId");
            
            if (isbn == null || isbn.trim().isEmpty()) {
                return Response.Builder.badRequest("ISBN不能为空");
            }
            
            Integer excludeBookId = null;
            if (excludeBookIdStr != null && !excludeBookIdStr.trim().isEmpty()) {
                try {
                    excludeBookId = Integer.valueOf(excludeBookIdStr);
                } catch (NumberFormatException e) {
                    return Response.Builder.badRequest("排除图书ID格式错误");
                }
            }
            
            // 3. 调用服务层
            boolean exists = bookManagementService.isIsbnExists(isbn.trim(), excludeBookId);
            
            Map<String, Object> result = Map.of(
                "isbn", isbn,
                "exists", exists,
                "excludeBookId", excludeBookId
            );
            
            log.info("检查ISBN是否存在成功: isbn={}, exists={}", isbn, exists);
            return Response.Builder.success("检查完成", result);
            
        } catch (Exception e) {
            log.error("处理检查ISBN是否存在请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 检查图书是否可以删除
     * URI: library/admin/book/check-deletable
     * 权限: admin
     */
    public Response checkBookDeletable(Request request) {
        log.info("处理检查图书是否可删除请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
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
            boolean deletable = bookManagementService.canDeleteBook(bookId);
            
            Map<String, Object> result = Map.of(
                "bookId", bookId,
                "deletable", deletable
            );
            
            log.info("检查图书是否可删除成功: bookId={}, deletable={}", bookId, deletable);
            return Response.Builder.success("检查完成", result);
            
        } catch (Exception e) {
            log.error("处理检查图书是否可删除请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 从请求参数中提取图书信息
     */
    private Book extractBookFromParams(Map<String, String> params) {
        try {
            String bookIdStr = params.get("bookId");
            String isbn = params.get("isbn");
            String title = params.get("title");
            String author = params.get("author");
            String publisher = params.get("publisher");
            String publishDateStr = params.get("publishDate");
            String category = params.get("category");
            String location = params.get("location");
            String totalQtyStr = params.get("totalQty");
            String availQtyStr = params.get("availQty");
            String statusStr = params.get("status");
            
            // 必填字段验证
            if (title == null || title.trim().isEmpty()) {
                log.warn("提取图书信息失败: 书名不能为空");
                return null;
            }
            if (author == null || author.trim().isEmpty()) {
                log.warn("提取图书信息失败: 作者不能为空");
                return null;
            }
            if (isbn == null || isbn.trim().isEmpty()) {
                log.warn("提取图书信息失败: ISBN不能为空");
                return null;
            }
            if (publisher == null || publisher.trim().isEmpty()) {
                log.warn("提取图书信息失败: 出版社不能为空");
                return null;
            }
            
            Book.BookBuilder builder = Book.builder()
                    .isbn(isbn.trim())
                    .title(title.trim())
                    .author(author.trim())
                    .publisher(publisher.trim())
                    .category(category != null ? category.trim() : null)
                    .location(location != null ? location.trim() : null);
            
            // 处理可选字段
            if (bookIdStr != null && !bookIdStr.trim().isEmpty()) {
                try {
                    builder.bookId(Integer.valueOf(bookIdStr));
                } catch (NumberFormatException e) {
                    // 忽略无效的ID
                }
            }
            
            if (publishDateStr != null && !publishDateStr.trim().isEmpty()) {
                try {
                    builder.publishDate(java.time.LocalDate.parse(publishDateStr.trim()));
                } catch (Exception e) {
                    // 忽略无效的日期
                }
            }
            
            if (totalQtyStr != null && !totalQtyStr.trim().isEmpty()) {
                try {
                    builder.totalQty(Integer.valueOf(totalQtyStr));
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }
            
            if (availQtyStr != null && !availQtyStr.trim().isEmpty()) {
                try {
                    builder.availQty(Integer.valueOf(availQtyStr));
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }
            
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                try {
                    builder.status(BookStatus.valueOf(statusStr.trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的图书状态: {}, 使用默认状态", statusStr);
                    builder.status(BookStatus.IN_LIBRARY);
                }
            } else {
                builder.status(BookStatus.IN_LIBRARY);
            }
            
            // 设置默认值
            if (builder.build().getTotalQty() == null) {
                builder.totalQty(1);
            }
            if (builder.build().getAvailQty() == null) {
                builder.availQty(builder.build().getTotalQty());
            }
            
            Book book = builder.build();
            
            // 基本验证
            if (!isValidIsbn(book.getIsbn())) {
                log.warn("提取图书信息失败: ISBN格式无效: {}", book.getIsbn());
                return null;
            }
            
            return book;
            
        } catch (Exception e) {
            log.error("提取图书信息异常", e);
            return null;
        }
    }
    
    /**
     * 简单的ISBN格式验证
     */
    private boolean isValidIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        
        String cleanIsbn = isbn.trim().replaceAll("[^0-9X]", "");
        return cleanIsbn.length() == 10 || cleanIsbn.length() == 13;
    }
    
    // ==================== Excel导入导出 ====================
    
    /**
     * 从Excel文件导入图书
     * URI: library/admin/book/import
     * 权限: admin
     */
    public Response importBooksFromExcel(Request request) {
        log.info("处理Excel导入图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 获取文件信息
            String fileName = request.getParam("fileName");
            String fileContent = request.getParam("fileContent"); // Base64编码的文件内容
            
            if (fileName == null || fileName.trim().isEmpty()) {
                return Response.Builder.badRequest("文件名不能为空");
            }
            
            if (fileContent == null || fileContent.trim().isEmpty()) {
                return Response.Builder.badRequest("文件内容不能为空");
            }
            
            // 3. 验证文件格式
            if (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls")) {
                return Response.Builder.badRequest("不支持的文件格式，请使用.xlsx或.xls文件");
            }
            
            // 4. 解码文件内容
            byte[] fileBytes;
            try {
                fileBytes = java.util.Base64.getDecoder().decode(fileContent);
            } catch (IllegalArgumentException e) {
                return Response.Builder.badRequest("文件内容格式错误，请确保是Base64编码");
            }
            
            // 5. 创建输入流并调用服务层
            try (java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(fileBytes)) {
                com.vcampus.server.core.library.entity.result.ExcelImportResult result = 
                    bookManagementService.importBooksFromExcel(inputStream, fileName);
                
                if (result.isSuccess()) {
                    log.info("Excel导入成功: fileName={}, 成功: {}, 失败: {}", 
                            fileName, result.getSuccessCount(), result.getFailCount());
                    return Response.Builder.success(result.getMessage(), result);
                                 } else {
                     log.warn("Excel导入失败: fileName={}, reason={}", fileName, result.getMessage());
                     return Response.Builder.badRequest(result.getMessage());
                 }
            }
            
        } catch (Exception e) {
            log.error("处理Excel导入图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 导出图书信息到Excel
     * URI: library/admin/book/export
     * 权限: admin
     */
    public Response exportBooksToExcel(Request request) {
        log.info("处理Excel导出图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 获取导出参数
            String fileName = request.getParam("fileName");
            String category = request.getParam("category");
            String status = request.getParam("status");
            
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "图书数据_" + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            }
            
            // 3. 获取要导出的图书列表
            List<Book> books;
            if (category != null && !category.trim().isEmpty()) {
                books = bookManagementService.getBooksByCategory(category.trim());
            } else if (status != null && !status.trim().isEmpty()) {
                try {
                    BookStatus bookStatus = BookStatus.valueOf(status.trim().toUpperCase());
                    books = bookManagementService.getBooksByStatus(bookStatus);
                } catch (IllegalArgumentException e) {
                    return Response.Builder.badRequest("无效的图书状态: " + status);
                }
            } else {
                books = bookManagementService.getAllBooks();
            }
            
            if (books.isEmpty()) {
                return Response.Builder.badRequest("没有找到要导出的图书数据");
            }
            
            // 4. 调用服务层导出
            BookManagementResult result = bookManagementService.exportBooksToExcel(books, fileName);
            
            if (result.isSuccess()) {
                log.info("Excel导出成功: fileName={}, count={}", fileName, books.size());
                return Response.Builder.success(result.getMessage(), result);
            } else {
                log.warn("Excel导出失败: fileName={}, reason={}", fileName, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理Excel导出图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有借阅记录
     * URI: library/admin/borrow-records
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/borrow-records", role = "admin", description = "获取所有借阅记录")
    public Response getAllBorrowRecords(Request request) {
        log.info("处理获取所有借阅记录请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 调用服务层获取借阅记录
            List<Map<String, Object>> records = bookManagementService.getAllBorrowRecords();
            
            log.info("获取借阅记录成功: count={}", records.size());
            return Response.Builder.success("查询成功", records);
            
        } catch (Exception e) {
            log.error("处理获取借阅记录请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 搜索借阅记录
     * URI: library/admin/search-borrows
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/search-borrows", role = "admin", description = "搜索借阅记录")
    public Response searchBorrowRecords(Request request) {
        log.info("处理搜索借阅记录请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 获取搜索参数
            String userSearch = request.getParam("userSearch");
            String bookSearch = request.getParam("bookSearch");
            String status = request.getParam("status");
            String borrowDate = request.getParam("borrowDate");
            String dueDate = request.getParam("dueDate");
            String returnDate = request.getParam("returnDate");
            
            // 处理null值
            if (userSearch == null) userSearch = "";
            if (bookSearch == null) bookSearch = "";
            if (status == null) status = "";
            if (borrowDate == null) borrowDate = "";
            if (dueDate == null) dueDate = "";
            if (returnDate == null) returnDate = "";
            
            // 3. 调用服务层搜索借阅记录
            List<Map<String, Object>> records = bookManagementService.searchBorrowRecords(userSearch, bookSearch, status, borrowDate, dueDate, returnDate);
            
            log.info("搜索借阅记录成功: count={}", records.size());
            return Response.Builder.success("搜索成功", records);
            
        } catch (Exception e) {
            log.error("处理搜索借阅记录请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 强制归还图书
     * URI: library/admin/force-return-book
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/force-return-book", role = "admin", description = "强制归还图书")
    public Response forceReturnBook(Request request) {
        log.info("处理强制归还图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
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
            
            // 3. 调用服务层强制归还
            BookManagementResult result = bookManagementService.forceReturnBook(transId);
            
            if (result.isSuccess()) {
                log.info("强制归还图书成功: transId={}", transId);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("强制归还图书失败: transId={}, message={}", transId, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理强制归还图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 管理员续借图书
     * URI: library/admin/admin-renew-book
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/admin-renew-book", role = "admin", description = "管理员续借图书")
    public Response adminRenewBook(Request request) {
        log.info("处理管理员续借图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 获取参数
            String transIdStr = request.getParam("transId");
            String extendDaysStr = request.getParam("extendDays");
            
            if (transIdStr == null || transIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("借阅记录ID不能为空");
            }
            if (extendDaysStr == null || extendDaysStr.trim().isEmpty()) {
                return Response.Builder.badRequest("续借天数不能为空");
            }
            
            Integer transId, extendDays;
            try {
                transId = Integer.valueOf(transIdStr);
                extendDays = Integer.valueOf(extendDaysStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("参数格式错误");
            }
            
            // 3. 调用服务层续借
            BookManagementResult result = bookManagementService.adminRenewBook(transId, extendDays);
            
            if (result.isSuccess()) {
                log.info("管理员续借图书成功: transId={}, extendDays={}", transId, extendDays);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("管理员续借图书失败: transId={}, message={}", transId, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理管理员续借图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 处理逾期图书
     * URI: library/admin/handle-overdue-book
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/handle-overdue-book", role = "admin", description = "处理逾期图书")
    public Response handleOverdueBook(Request request) {
        log.info("处理逾期图书请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
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
            
            // 3. 调用服务层处理逾期
            BookManagementResult result = bookManagementService.handleOverdueBook(transId);
            
            if (result.isSuccess()) {
                log.info("处理逾期图书成功: transId={}", transId);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("处理逾期图书失败: transId={}, message={}", transId, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理逾期图书请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取逾期借阅记录
     * URI: library/admin/overdue-borrow-records
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/overdue-borrow-records", role = "admin", description = "获取逾期借阅记录")
    public Response getOverdueBorrowRecords(Request request) {
        log.info("处理获取逾期借阅记录请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 调用服务层获取逾期记录
            List<Map<String, Object>> records = bookManagementService.getOverdueBorrowRecords();
            
            log.info("获取逾期借阅记录成功: count={}", records.size());
            return Response.Builder.success("查询成功", records);
            
        } catch (Exception e) {
            log.error("处理获取逾期借阅记录请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户借阅排名
     * URI: library/admin/statistics/user-borrow-ranking
     * 权限: admin
     */
    @RouteMapping(uri = "library/admin/statistics/user-borrow-ranking", role = "admin", description = "获取用户借阅排名")
    public Response getUserBorrowRanking(Request request) {
        log.info("处理获取用户借阅排名请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("admin") && !session.hasPermission("manager"))) {
                return Response.Builder.forbidden("需要管理员权限");
            }
            
            // 2. 调用服务层获取用户借阅排名
            List<Map<String, Object>> ranking = bookManagementService.getUserBorrowRanking();
            
            Map<String, Object> result = new HashMap<>();
            result.put("users", ranking);
            
            log.info("获取用户借阅排名成功: count={}", ranking.size());
            return Response.Builder.success("查询成功", result);
            
        } catch (Exception e) {
            log.error("处理获取用户借阅排名请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
}