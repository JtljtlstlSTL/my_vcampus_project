package com.vcampus.server.core.library.dao;

import com.vcampus.server.core.library.entity.core.Book;
import com.vcampus.server.core.library.entity.view.BookBorrowStatus;
import com.vcampus.server.core.library.entity.view.BookBorrowStatistics;
import com.vcampus.server.core.library.entity.search.BookSearchResult;
import com.vcampus.server.core.library.entity.search.BookStatistics;
import com.vcampus.server.core.library.entity.search.PopularBook;
import com.vcampus.server.core.library.entity.result.AddBookResult;
import com.vcampus.server.core.library.entity.result.UpdateBookResult;
import com.vcampus.server.core.library.mapper.BookMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * 图书数据访问对象 - 使用MyBatis
 * 提供图书实体的CRUD操作和业务查询方法
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class BookDao {
    
    private static BookDao instance;
    private final SqlSessionFactory sqlSessionFactory;
    
    private BookDao() {
        // 初始化MyBatis SqlSessionFactory
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mybatis-config.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("初始化MyBatis失败", e);
        }
    }
    
    public static synchronized BookDao getInstance() {
        if (instance == null) {
            instance = new BookDao();
        }
        return instance;
    }
    
    // ==================== 基础CRUD方法 ====================
    
    /** 根据ID查找图书 */
    public Optional<Book> findById(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            Book book = mapper.findById(bookId);
            return Optional.ofNullable(book);
        }
    }
    
    /** 查找所有图书 */
    public List<Book> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findAll();
        }
    }
    
    /** 保存图书（新增或更新） */
    public Book save(Book book) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            if (book.getBookId() == null) {
                mapper.insert(book);
            } else {
                mapper.update(book);
            }
            session.commit();
            return book;
        }
    }
    
    /** 根据ID删除图书 */
    public void deleteById(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            mapper.deleteById(bookId);
            session.commit();
        }
    }
    
    /**
     * 检查图书是否存在
     */
    public boolean existsById(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.existsById(bookId);
        }
    }
    
    /**
     * 统计图书数量
     */
    public long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.count();
        }
    }
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 根据ISBN查找图书
     */
    public Optional<Book> findByIsbn(String isbn) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            Book book = mapper.findByIsbn(isbn);
            return Optional.ofNullable(book);
        }
    }
    
    /**
     * 根据书名模糊查询
     */
    public List<Book> findByTitleLike(String title) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findByTitleLike(title);
        }
    }
    
    /**
     * 根据作者模糊查询
     */
    public List<Book> findByAuthorLike(String author) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findByAuthorLike(author);
        }
    }
    
    /**
     * 根据出版社模糊查询
     */
    public List<Book> findByPublisherLike(String publisher) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findByPublisherLike(publisher);
        }
    }
    
    /**
     * 根据分类查询图书
     */
    public List<Book> findByCategory(String category) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findByCategory(category);
        }
    }
    
    /**
     * 获取各分类图书数量统计
     */
    public Map<String, Integer> getCategoryStatistics() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            List<Map<String, Object>> resultList = mapper.getCategoryStatistics();
            
            // 将List<Map<String, Object>>转换为Map<String, Integer>
            Map<String, Integer> categoryMap = new HashMap<>();
            for (Map<String, Object> row : resultList) {
                String category = (String) row.get("Category");
                Integer count = ((Number) row.get("count")).intValue();
                categoryMap.put(category, count);
            }
            
            return categoryMap;
        }
    }
    
    /**
     * 根据位置查询图书
     */
    public List<Book> findByLocation(String location) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findByLocation(location);
        }
    }
    
    /**
     * 查询可借阅的图书
     */
    public List<Book> findAvailableBooks() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findAvailableBooks();
        }
    }
    
    /**
     * 查询已借完的图书
     */
    public List<Book> findOutOfStockBooks() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findOutOfStockBooks();
        }
    }
    
    /**
     * 智能搜索图书
     */
    public List<Book> searchBooks(String keyword) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.searchBooks(keyword);
        }
    }
    
    /**
     * 检查ISBN是否已存在
     */
    public boolean isIsbnExists(String isbn) {
        return findByIsbn(isbn).isPresent();
    }
    
    /**
     * 更新图书库存
     */
    public void updateStock(Integer bookId, int newAvailQty) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            int affected = mapper.updateStock(bookId, newAvailQty);
            if (affected == 0) {
                throw new RuntimeException("更新库存失败：未找到ID为 " + bookId + " 的图书");
            }
            session.commit();
        }
    }
    
    /**
     * 更新图书状态
     */
    public void updateStatus(Integer bookId, com.vcampus.server.core.library.enums.BookStatus status) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            int affected = mapper.updateStatus(bookId, status.name());
            if (affected == 0) {
                throw new RuntimeException("更新状态失败：未找到ID为 " + bookId + " 的图书");
            }
            session.commit();
        }
    }
    
    // ==================== 存储过程调用方法 ====================
    
    /**
     * 添加图书 - 调用存储过程 sp_add_book
     * @param isbn ISBN号
     * @param title 书名
     * @param author 作者
     * @param publisher 出版社
     * @param publishDate 出版日期
     * @param category 分类代码
     * @param location 馆藏位置
     * @param totalQty 总数量
     * @return 添加结果
     */
    public AddBookResult addBook(String isbn, String title, String author, String publisher, 
                                java.time.LocalDate publishDate, String category, String location, Integer totalQty) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.addBook(isbn, title, author, publisher, publishDate, category, location, totalQty);
        }
    }
    
    /**
     * 更新图书信息 - 调用存储过程 sp_update_book
     * @param bookId 图书ID
     * @param title 书名
     * @param author 作者
     * @param publisher 出版社
     * @param publishDate 出版日期
     * @param category 分类代码
     * @param location 馆藏位置
     * @param totalQty 总数量
     * @return 更新结果
     */
    public UpdateBookResult updateBook(Integer bookId, String title, String author, String publisher, 
                                      java.time.LocalDate publishDate, String category, String location, Integer totalQty) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.updateBook(bookId, title, author, publisher, publishDate, category, location, totalQty);
        }
    }
    
    /**
     * 获取图书统计信息 - 调用存储过程 sp_get_book_statistics
     * @param bookId 图书ID
     * @return 图书统计信息
     */
    public BookStatistics getBookStatistics(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.getBookStatistics(bookId);
        }
    }
    
    
    /**
     * 搜索热门图书 - 调用存储过程 sp_search_popular_books
     * @param category 分类
     * @param limit 限制数量
     * @return 热门图书列表
     */
    public List<PopularBook> searchPopularBooks(String category, Integer limit) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.searchPopularBooks(category, limit);
        }
    }
    
    /**
     * 按分类搜索图书 - 调用存储过程 sp_search_books_by_category
     * @param category 分类
     * @param availableOnly 仅可借阅
     * @param limit 限制数量
     * @return 分类图书列表
     */
    public List<BookSearchResult> searchBooksByCategory(String category, boolean availableOnly, Integer limit) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.searchBooksByCategory(category, availableOnly, limit);
        }
    }
    
    // ==================== 视图查询方法 ====================
    
    /**
     * 查询图书借阅状态 - 使用视图 v_book_borrow_status
     * @return 图书借阅状态列表
     */
    public List<BookBorrowStatus> getBookBorrowStatus() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.getBookBorrowStatus();
        }
    }
    
    /**
     * 查询图书借阅统计 - 使用视图 v_book_borrow_statistics
     * @return 图书借阅统计列表
     */
    public List<BookBorrowStatistics> getBookBorrowStatistics() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.getBookBorrowStatistics();
        }
    }
    
    /**
     * 查询热门图书 - 使用视图 v_popular_books
     * @return 热门图书列表
     */
    public List<PopularBook> getPopularBooks() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.getPopularBooks();
        }
    }
    
    // ==================== 缺失的方法 ====================
    
    /**
     * 分页查询图书
     */
    public List<Book> findByPage(int offset, int size) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findByPage(offset, size);
        }
    }
    
    /**
     * 根据状态查询图书
     */
    public List<Book> findByStatus(com.vcampus.server.core.library.enums.BookStatus status) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findByStatus(status);
        }
    }
    
    /**
     * 根据条件查询图书
     */
    public List<Book> findByConditions(String title, String author, String publisher, 
                                     String category, com.vcampus.server.core.library.enums.BookStatus status) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findByConditions(title, author, publisher, category, status);
        }
    }
    
    /**
     * 根据状态统计图书数量
     */
    public long countByStatus(com.vcampus.server.core.library.enums.BookStatus status) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.countByStatus(status);
        }
    }
    
    /**
     * 根据分类统计图书数量
     */
    public java.util.Map<String, Long> countByCategory() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.countByCategory();
        }
    }

}
