package com.vcampus.server.core.library.mapper;

import com.vcampus.server.core.library.entity.Book;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 图书数据访问接口 - 使用MyBatis
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface BookMapper {
    
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
     * 综合搜索图书
     */
    List<Book> searchBooks(@Param("keyword") String keyword, 
                          @Param("category") String category,
                          @Param("author") String author,
                          @Param("publisher") String publisher,
                          @Param("location") String location,
                          @Param("availableOnly") boolean availableOnly);
    
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
     * 更新图书库存
     */
    int updateStock(@Param("bookId") Integer bookId, @Param("availQty") int availQty);
    
    /**
     * 更新图书状态
     */
    int updateStatus(@Param("bookId") Integer bookId, @Param("status") String status);
}
