package com.vcampus.server.core.library.dao;

import com.vcampus.server.core.library.entity.Book;
import com.vcampus.server.core.library.mapper.BookMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;
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
    
    /**
     * 根据ID查找图书
     */
    public Optional<Book> findById(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            Book book = mapper.findById(bookId);
            return Optional.ofNullable(book);
        }
    }
    
    /**
     * 查找所有图书
     */
    public List<Book> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findAll();
        }
    }
    
    /**
     * 保存图书（新增或更新）
     */
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
    
    /**
     * 根据ID删除图书
     */
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
     * 综合搜索图书（支持多条件组合）
     */
    public List<Book> searchBooks(String keyword, String category, String author, String publisher, String location, boolean availableOnly) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.searchBooks(keyword, category, author, publisher, location, availableOnly);
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
}
