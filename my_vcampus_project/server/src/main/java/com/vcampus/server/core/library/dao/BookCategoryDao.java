package com.vcampus.server.core.library.dao;

import com.vcampus.server.core.library.entity.BookCategory;
import com.vcampus.server.core.library.mapper.BookCategoryMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 图书分类数据访问对象 - 使用MyBatis
 * 提供图书分类的CRUD操作和业务查询方法
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class BookCategoryDao {
    
    private static BookCategoryDao instance;
    private final SqlSessionFactory sqlSessionFactory;
    
    private BookCategoryDao() {
        // 初始化MyBatis SqlSessionFactory
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mybatis-config.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("初始化MyBatis失败", e);
        }
    }
    
    public static synchronized BookCategoryDao getInstance() {
        if (instance == null) {
            instance = new BookCategoryDao();
        }
        return instance;
    }
    
    // ==================== 基础CRUD方法 ====================
    
    /**
     * 根据ID查找分类
     */
    public Optional<BookCategory> findById(Integer categoryId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            BookCategory category = mapper.findById(categoryId);
            return Optional.ofNullable(category);
        }
    }
    
    /**
     * 查找所有分类
     */
    public List<BookCategory> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.findAll();
        }
    }
    
    /**
     * 保存分类（新增或更新）
     */
    public BookCategory save(BookCategory category) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            if (category.getCategoryId() == null) {
                mapper.insert(category);
            } else {
                mapper.update(category);
            }
            session.commit();
            return category;
        }
    }
    
    /**
     * 根据ID删除分类
     */
    public void deleteById(Integer categoryId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            mapper.deleteById(categoryId);
            session.commit();
        }
    }
    
    /**
     * 检查分类是否存在
     */
    public boolean existsById(Integer categoryId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.existsById(categoryId);
        }
    }
    
    /**
     * 统计分类数量
     */
    public long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.count();
        }
    }
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 根据分类代码查找分类
     */
    public Optional<BookCategory> findByCategoryCode(String categoryCode) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            BookCategory category = mapper.findByCategoryCode(categoryCode);
            return Optional.ofNullable(category);
        }
    }
    
    /**
     * 根据分类名称查找分类
     */
    public Optional<BookCategory> findByCategoryName(String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            BookCategory category = mapper.findByCategoryName(categoryName);
            return Optional.ofNullable(category);
        }
    }
    
    /**
     * 根据分类名称模糊查询
     */
    public List<BookCategory> findByCategoryNameLike(String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.findByCategoryNameLike(categoryName);
        }
    }
    
    /**
     * 根据描述模糊查询
     */
    public List<BookCategory> findByDescriptionLike(String description) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.findByDescriptionLike(description);
        }
    }
    
    /**
     * 按排序顺序查询所有分类
     */
    public List<BookCategory> findAllOrderBySort() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.findAllOrderBySort();
        }
    }
    
    /**
     * 按分类代码排序查询所有分类
     */
    public List<BookCategory> findAllOrderByCode() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.findAllOrderByCode();
        }
    }
    
    /**
     * 查询基本大类（A-Z）
     */
    public List<BookCategory> findBasicCategories() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.findBasicCategories();
        }
    }
    
    /**
     * 检查分类代码是否已存在
     */
    public boolean isCategoryCodeExists(String categoryCode) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.isCategoryCodeExists(categoryCode);
        }
    }
    
    /**
     * 检查分类名称是否已存在
     */
    public boolean isCategoryNameExists(String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.isCategoryNameExists(categoryName);
        }
    }
    
    /**
     * 获取下一个排序顺序
     */
    public int getNextSortOrder() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            return mapper.getNextSortOrder();
        }
    }
    
    /**
     * 更新分类排序顺序
     */
    public void updateSortOrder(Integer categoryId, int newSortOrder) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookCategoryMapper mapper = session.getMapper(BookCategoryMapper.class);
            int affected = mapper.updateSortOrder(categoryId, newSortOrder);
            if (affected == 0) {
                throw new RuntimeException("更新排序顺序失败：未找到ID为 " + categoryId + " 的分类");
            }
            session.commit();
        }
    }
}