package com.vcampus.server.core.library.dao;

import com.vcampus.server.core.library.entity.core.MyBookshelf;
import com.vcampus.server.core.library.entity.view.BookshelfView;
import com.vcampus.server.core.library.mapper.MyBookshelfMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 我的书架数据访问对象 - 使用MyBatis
 * 提供书架记录的CRUD操作和业务查询方法
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class MyBookshelfDao {
    
    private static MyBookshelfDao instance;
    private final SqlSessionFactory sqlSessionFactory;
    
    private MyBookshelfDao() {
        // 初始化MyBatis SqlSessionFactory
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mybatis-config.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("初始化MyBatis失败", e);
        }
    }
    
    public static synchronized MyBookshelfDao getInstance() {
        if (instance == null) {
            instance = new MyBookshelfDao();
        }
        return instance;
    }
    
    // ==================== 基础CRUD方法 ====================
    
    /**
     * 根据ID查找书架记录
     */
    public Optional<MyBookshelf> findById(Integer shelfId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            MyBookshelf bookshelf = mapper.findById(shelfId);
            return Optional.ofNullable(bookshelf);
        }
    }
    
    /**
     * 查找所有书架记录
     */
    public List<MyBookshelf> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.findAll();
        }
    }
    
    /**
     * 保存书架记录（新增或更新）
     */
    public MyBookshelf save(MyBookshelf bookshelf) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            if (bookshelf.getShelfId() == null) {
                mapper.insert(bookshelf);
            } else {
                mapper.update(bookshelf);
            }
            session.commit();
            return bookshelf;
        }
    }
    
    /**
     * 根据ID删除书架记录
     */
    public void deleteById(Integer shelfId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            mapper.deleteById(shelfId);
            session.commit();
        }
    }
    
    /**
     * 检查书架记录是否存在
     */
    public boolean existsById(Integer shelfId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.existsById(shelfId);
        }
    }
    
    /**
     * 统计书架记录数量
     */
    public long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.count();
        }
    }
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 根据用户卡号查询书架记录
     */
    public List<MyBookshelf> findByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.findByCardNum(cardNum);
        }
    }
    
    /**
     * 根据用户卡号和分类查询书架记录
     */
    public List<MyBookshelf> findByCardNumAndCategory(String cardNum, String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.findByCardNumAndCategory(cardNum, categoryName);
        }
    }
    
    /**
     * 根据用户卡号和图书ID查询书架记录
     */
    public Optional<MyBookshelf> findByCardNumAndBookId(String cardNum, Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            MyBookshelf bookshelf = mapper.findByCardNumAndBookId(cardNum, bookId);
            return Optional.ofNullable(bookshelf);
        }
    }
    
    /**
     * 根据图书ID查询书架记录
     */
    public List<MyBookshelf> findByBookId(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.findByBookId(bookId);
        }
    }
    
    /**
     * 根据分类名称查询书架记录
     */
    public List<MyBookshelf> findByCategoryName(String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.findByCategoryName(categoryName);
        }
    }
    
    /**
     * 统计用户书架数量
     */
    public long countByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.countByCardNum(cardNum);
        }
    }
    
    /**
     * 统计用户指定分类的书架数量
     */
    public long countByCardNumAndCategory(String cardNum, String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.countByCardNumAndCategory(cardNum, categoryName);
        }
    }
    
    /**
     * 检查用户是否已收藏某本图书
     */
    public boolean isBookInBookshelf(String cardNum, Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.isBookInBookshelf(cardNum, bookId);
        }
    }
    
    /**
     * 根据用户卡号和图书ID删除书架记录
     */
    public void deleteByCardNumAndBookId(String cardNum, Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            int affected = mapper.deleteByCardNumAndBookId(cardNum, bookId);
            if (affected == 0) {
                throw new RuntimeException("删除失败：未找到用户 " + cardNum + " 的图书 " + bookId + " 的书架记录");
            }
            session.commit();
        }
    }
    
    /**
     * 更新书架记录分类
     */
    public void updateCategory(Integer shelfId, String newCategoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            int affected = mapper.updateCategory(shelfId, newCategoryName);
            if (affected == 0) {
                throw new RuntimeException("更新分类失败：未找到ID为 " + shelfId + " 的书架记录");
            }
            session.commit();
        }
    }
    
    /**
     * 更新书架记录备注
     */
    public void updateNotes(Integer shelfId, String notes) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            int affected = mapper.updateNotes(shelfId, notes);
            if (affected == 0) {
                throw new RuntimeException("更新备注失败：未找到ID为 " + shelfId + " 的书架记录");
            }
            session.commit();
        }
    }
    
    // ==================== 视图查询方法 ====================
    
    /**
     * 查询用户书架视图 - 使用视图
     */
    public List<BookshelfView> getUserBookshelfView(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.getUserBookshelfView(cardNum);
        }
    }
    
    /**
     * 根据分类查询用户书架视图 - 使用视图
     */
    public List<BookshelfView> getUserBookshelfViewByCategory(String cardNum, String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.getUserBookshelfViewByCategory(cardNum, categoryName);
        }
    }
    
    /**
     * 获取用户所有分类名称
     */
    public List<String> getUserCategoryNames(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBookshelfMapper mapper = session.getMapper(MyBookshelfMapper.class);
            return mapper.getUserCategoryNames(cardNum);
        }
    }
}
