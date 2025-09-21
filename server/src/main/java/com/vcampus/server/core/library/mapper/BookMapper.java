package com.vcampus.server.core.library.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.vcampus.server.core.library.entity.core.Book;
import com.vcampus.server.core.library.entity.result.AddBookResult;
import com.vcampus.server.core.library.entity.result.UpdateBookResult;
import com.vcampus.server.core.library.entity.search.BookSearchResult;
import com.vcampus.server.core.library.entity.search.BookStatistics;
import com.vcampus.server.core.library.entity.search.PopularBook;
import com.vcampus.server.core.library.entity.view.BookBorrowStatistics;
import com.vcampus.server.core.library.entity.view.BookBorrowStatus;

/**
 * 图书数据访问接口 - 使用MyBatis
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface BookMapper {
    
    // ==================== 基础CRUD操作 ====================
    
    /**
     * 根据ID查找图书
     */
    Book findById(@Param("bookId") Integer bookId);
    
    /**
     * 查找所有图书
     */
    List<Book> findAll();
    
    /**
     * 根据ISBN查找图书
     */
    Book findByIsbn(@Param("isbn") String isbn);
    
    /**
     * 根据书名模糊查询
     */
    List<Book> findByTitleLike(@Param("title") String title);
    
    /**
     * 根据作者模糊查询
     */
    List<Book> findByAuthorLike(@Param("author") String author);
    
    /**
     * 根据出版社模糊查询
     */
    List<Book> findByPublisherLike(@Param("publisher") String publisher);
    
    /**
     * 根据分类查询图书
     */
    List<Book> findByCategory(@Param("category") String category);
    
    /**
     * 根据位置查询图书
     */
    List<Book> findByLocation(@Param("location") String location);
    
    /**
     * 查询可借阅的图书
     */
    List<Book> findAvailableBooks();
    
    /**
     * 查询已借完的图书
     */
    List<Book> findOutOfStockBooks();
    
    /**
     * 智能搜索图书 - 根据关键词搜索书名、作者、出版社
     */
    List<Book> searchBooks(@Param("keyword") String keyword);
    
    /**
     * 插入图书
     */
    int insert(Book book);
    
    /**
     * 更新图书
     */
    int update(Book book);
    
    /**
     * 根据ID删除图书
     */
    int deleteById(@Param("bookId") Integer bookId);
    
    /**
     * 检查图书是否存在
     */
    boolean existsById(@Param("bookId") Integer bookId);
    
    /**
     * 统计图书数量
     */
    long count();
    
    /**
     * 获取各分类图书数量统计
     */
    List<Map<String, Object>> getCategoryStatistics();
    
    /**
     * 更新图书库存
     */
    int updateStock(@Param("bookId") Integer bookId, @Param("availQty") int availQty);
    
    /**
     * 更新图书状态
     */
    int updateStatus(@Param("bookId") Integer bookId, @Param("status") String status);
    
    // ==================== 存储过程调用方法 ====================
    
    /**
     * 添加图书 - 调用存储过程 sp_add_book
     */
    AddBookResult addBook(@Param("isbn") String isbn,
                         @Param("title") String title,
                         @Param("author") String author,
                         @Param("publisher") String publisher,
                         @Param("publishDate") LocalDate publishDate,
                         @Param("category") String category,
                         @Param("location") String location,
                         @Param("totalQty") Integer totalQty);
    
    /**
     * 更新图书信息 - 调用存储过程 sp_update_book
     */
    UpdateBookResult updateBook(@Param("bookId") Integer bookId,
                               @Param("title") String title,
                               @Param("author") String author,
                               @Param("publisher") String publisher,
                               @Param("publishDate") LocalDate publishDate,
                               @Param("category") String category,
                               @Param("location") String location,
                               @Param("totalQty") Integer totalQty);
    
    /**
     * 获取图书统计信息 - 调用存储过程 sp_get_book_statistics
     */
    BookStatistics getBookStatistics(@Param("bookId") Integer bookId);
    
    
    /**
     * 搜索热门图书 - 调用存储过程 sp_search_popular_books
     */
    List<PopularBook> searchPopularBooks(@Param("category") String category,
                                        @Param("limit") Integer limit);
    
    /**
     * 按分类搜索图书 - 调用存储过程 sp_search_books_by_category
     */
    List<BookSearchResult> searchBooksByCategory(@Param("category") String category,
                                                @Param("availableOnly") boolean availableOnly,
                                                @Param("limit") Integer limit);
    
    // ==================== 视图查询方法 ====================
    
    /**
     * 查询图书借阅状态 - 使用视图 v_book_borrow_status
     */
    List<BookBorrowStatus> getBookBorrowStatus();
    
    /**
     * 查询图书借阅统计 - 使用视图 v_book_borrow_statistics
     */
    List<BookBorrowStatistics> getBookBorrowStatistics();
    
    /**
     * 查询热门图书 - 使用视图 v_popular_books
     */
    List<PopularBook> getPopularBooks();
    
    // ==================== 缺失的方法 ====================
    
    /**
     * 分页查询图书
     */
    List<Book> findByPage(@Param("offset") int offset, @Param("size") int size);
    
    /**
     * 根据状态查询图书
     */
    List<Book> findByStatus(@Param("status") com.vcampus.server.core.library.enums.BookStatus status);
    
    /**
     * 根据条件查询图书
     */
    List<Book> findByConditions(@Param("title") String title, 
                               @Param("author") String author, 
                               @Param("publisher") String publisher, 
                               @Param("category") String category, 
                               @Param("status") com.vcampus.server.core.library.enums.BookStatus status);
    
    /**
     * 根据状态统计图书数量
     */
    long countByStatus(@Param("status") com.vcampus.server.core.library.enums.BookStatus status);
    
    /**
     * 根据分类统计图书数量
     */
    java.util.Map<String, Long> countByCategory();
}