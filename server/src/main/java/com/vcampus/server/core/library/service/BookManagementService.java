package com.vcampus.server.core.library.service;

import java.io.InputStream;
import java.util.List;

import com.vcampus.server.core.library.entity.core.Book;
import com.vcampus.server.core.library.entity.result.BookManagementResult;
import com.vcampus.server.core.library.entity.result.ExcelImportResult;
import com.vcampus.server.core.library.entity.search.PopularBook;
import com.vcampus.server.core.library.enums.BookStatus;

/**
 * 图书管理服务接口
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface BookManagementService {
    
    // ==================== 基础CRUD操作 ====================
    
    /**
     * 添加图书
     * 
     * @param book 图书信息
     * @return 操作结果
     */
    BookManagementResult addBook(Book book);
    
    /**
     * 更新图书信息
     * 
     * @param book 图书信息
     * @return 操作结果
     */
    BookManagementResult updateBook(Book book);
    
    /**
     * 删除图书
     * 
     * @param bookId 图书ID
     * @return 操作结果
     */
    BookManagementResult deleteBook(Integer bookId);
    
    /**
     * 根据ID查询图书
     * 
     * @param bookId 图书ID
     * @return 图书信息
     */
    Book getBookById(Integer bookId);
    
    /**
     * 根据ISBN查询图书
     * 
     * @param isbn ISBN
     * @return 图书信息
     */
    Book getBookByIsbn(String isbn);
    
    // ==================== 查询功能 ====================
    
    /**
     * 查询所有图书
     * 
     * @return 图书列表
     */
    List<Book> getAllBooks();
    
    /**
     * 分页查询图书
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 图书列表
     */
    List<Book> getBooksByPage(int page, int size);
    
    /**
     * 根据书名模糊查询
     * 
     * @param title 书名关键词
     * @return 图书列表
     */
    List<Book> searchBooksByTitle(String title);
    
    /**
     * 根据作者查询
     * 
     * @param author 作者
     * @return 图书列表
     */
    List<Book> searchBooksByAuthor(String author);
    
    /**
     * 根据出版社查询
     * 
     * @param publisher 出版社
     * @return 图书列表
     */
    List<Book> searchBooksByPublisher(String publisher);
    
    /**
     * 根据分类查询
     * 
     * @param category 分类代码
     * @return 图书列表
     */
    List<Book> getBooksByCategory(String category);
    
    /**
     * 根据状态查询
     * 
     * @param status 图书状态
     * @return 图书列表
     */
    List<Book> getBooksByStatus(BookStatus status);
    
    /**
     * 复合查询
     * 
     * @param title 书名（可选）
     * @param author 作者（可选）
     * @param publisher 出版社（可选）
     * @param category 分类（可选）
     * @param status 状态（可选）
     * @return 图书列表
     */
    List<Book> searchBooks(String title, String author, String publisher, String category, BookStatus status);
    
    /**
     * 智能搜索图书
     * 根据关键词自动判断搜索类型：ISBN精确搜索、分类精确搜索、或书名/作者/出版社模糊搜索
     * 
     * @param keyword 搜索关键词
     * @return 图书列表
     */
    List<Book> smartSearchBooks(String keyword);
    
    /**
     * 获取热门图书
     * 
     * @param limit 限制数量
     * @return 热门图书列表
     */
    List<PopularBook> getPopularBooks(int limit);
    
    // ==================== 批量操作 ====================
    
    /**
     * 批量添加图书
     * 
     * @param books 图书列表
     * @return 操作结果
     */
    BookManagementResult batchAddBooks(List<Book> books);
    
    /**
     * 批量更新图书
     * 
     * @param books 图书列表
     * @return 操作结果
     */
    BookManagementResult batchUpdateBooks(List<Book> books);
    
    /**
     * 批量删除图书
     * 
     * @param bookIds 图书ID列表
     * @return 操作结果
     */
    BookManagementResult batchDeleteBooks(List<Integer> bookIds);
    
    /**
     * 批量更新图书状态
     * 
     * @param bookIds 图书ID列表
     * @param status 新状态
     * @return 操作结果
     */
    BookManagementResult batchUpdateBookStatus(List<Integer> bookIds, BookStatus status);
    
    // ==================== Excel导入导出 ====================
    
    /**
     * 从Excel文件导入图书
     * 
     * @param inputStream Excel文件输入流
     * @param fileName 文件名
     * @return 导入结果
     */
    ExcelImportResult importBooksFromExcel(InputStream inputStream, String fileName);
    
    /**
     * 导出图书信息到Excel
     * 
     * @param books 图书列表
     * @param fileName 文件名
     * @return 导出结果
     */
    BookManagementResult exportBooksToExcel(List<Book> books, String fileName);
    
    // ==================== 统计功能 ====================
    
    /**
     * 统计图书总数
     * 
     * @return 图书总数
     */
    long countAllBooks();
    
    /**
     * 统计各状态图书数量
     * 
     * @return 状态统计
     */
    java.util.Map<BookStatus, Long> countBooksByStatus();
    
    /**
     * 统计各分类图书数量
     * 
     * @return 分类统计
     */
    java.util.Map<String, Long> countBooksByCategory();
    
    /**
     * 统计可借阅图书数量
     * 
     * @return 可借阅图书数量
     */
    long countAvailableBooks();
    
    /**
     * 统计借出图书数量
     * 
     * @return 借出图书数量
     */
    long countBorrowedBooks();
    
    // ==================== 库存管理 ====================
    
    /**
     * 更新图书副本数量
     * 
     * @param bookId 图书ID
     * @param totalCopies 总副本数
     * @param availableCopies 可借副本数
     * @return 操作结果
     */
    BookManagementResult updateBookCopies(Integer bookId, Integer totalCopies, Integer availableCopies);
    
    // ==================== 借阅记录管理 ====================
    
    /**
     * 获取所有借阅记录
     * 
     * @return 借阅记录列表
     */
    List<java.util.Map<String, Object>> getAllBorrowRecords();
    
    /**
     * 搜索借阅记录
     * 
     * @param userSearch 用户搜索关键词
     * @param bookSearch 图书搜索关键词
     * @param status 借阅状态
     * @param borrowDate 借阅日期
     * @param dueDate 应还日期
     * @param returnDate 归还日期
     * @return 借阅记录列表
     */
    List<java.util.Map<String, Object>> searchBorrowRecords(String userSearch, String bookSearch, String status, String borrowDate, String dueDate, String returnDate);
    
    /**
     * 减少图书副本
     * 
     * @param bookId 图书ID
     * @param copies 减少的副本数
     * @return 操作结果
     */
    BookManagementResult reduceBookCopies(Integer bookId, Integer copies);
    
    /**
     * 增加图书副本
     * 
     * @param bookId 图书ID
     * @param copies 增加的副本数
     * @return 操作结果
     */
    BookManagementResult addBookCopies(Integer bookId, Integer copies);
    
    // ==================== 借阅管理操作 ====================
    
    /**
     * 强制归还图书（管理员操作）
     * 
     * @param transId 借阅记录ID
     * @return 操作结果
     */
    BookManagementResult forceReturnBook(Integer transId);
    
    /**
     * 管理员续借图书
     * 
     * @param transId 借阅记录ID
     * @param extendDays 续借天数
     * @return 操作结果
     */
    BookManagementResult adminRenewBook(Integer transId, Integer extendDays);
    
    /**
     * 处理逾期图书
     * 
     * @param transId 借阅记录ID
     * @return 操作结果
     */
    BookManagementResult handleOverdueBook(Integer transId);
    
    /**
     * 获取逾期借阅记录
     * 
     * @return 逾期借阅记录列表
     */
    List<java.util.Map<String, Object>> getOverdueBorrowRecords();
    
    // ==================== 数据验证 ====================
    
    /**
     * 验证图书信息
     * 
     * @param book 图书信息
     * @return 验证结果
     */
    BookManagementResult validateBook(Book book);
    
    /**
     * 检查ISBN是否已存在
     * 
     * @param isbn ISBN
     * @param excludeBookId 排除的图书ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isIsbnExists(String isbn, Integer excludeBookId);
    
    /**
     * 检查图书是否可以删除
     * 
     * @param bookId 图书ID
     * @return 是否可以删除
     */
    boolean canDeleteBook(Integer bookId);
    
    /**
     * 获取用户借阅排名
     * 
     * @return 用户借阅排名列表，最多返回前10名
     */
    List<java.util.Map<String, Object>> getUserBorrowRanking();
    
    /**
     * 获取所有分类列表
     * 
     * @return 分类列表，包含分类代码和名称
     */
    List<java.util.Map<String, Object>> getAllCategories();
}
