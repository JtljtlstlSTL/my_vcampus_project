package com.vcampus.server.core.library.dao;

import com.vcampus.server.core.library.entity.core.UserPersonalCategory;
import com.vcampus.server.core.library.mapper.UserPersonalCategoryMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 用户个人分类数据访问对象 - 使用MyBatis
 * 提供用户个人分类的CRUD操作和业务查询方法
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class UserPersonalCategoryDao {
    
    private static UserPersonalCategoryDao instance;
    private final SqlSessionFactory sqlSessionFactory;
    
    private UserPersonalCategoryDao() {
        // 初始化MyBatis SqlSessionFactory
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mybatis-config.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("初始化MyBatis失败", e);
        }
    }
    
    public static synchronized UserPersonalCategoryDao getInstance() {
        if (instance == null) {
            instance = new UserPersonalCategoryDao();
        }
        return instance;
    }
    
    // ==================== 基础CRUD方法 ====================
    
    /**
     * 根据ID查找分类
     */
    public Optional<UserPersonalCategory> findById(Integer categoryId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            UserPersonalCategory category = mapper.findById(categoryId);
            return Optional.ofNullable(category);
        }
    }
    
    /**
     * 根据用户卡号和分类名称查找分类
     */
    public Optional<UserPersonalCategory> findByCardNumAndName(String cardNum, String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            UserPersonalCategory category = mapper.findByCardNumAndCategoryName(cardNum, categoryName);
            return Optional.ofNullable(category);
        }
    }
    
    /**
     * 根据用户卡号查找所有分类
     */
    public List<UserPersonalCategory> findByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            return mapper.findByCardNum(cardNum);
        }
    }
    
    /**
     * 插入分类
     */
    public UserPersonalCategory insert(UserPersonalCategory category) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            int result = mapper.insert(category);
            if (result > 0) {
                return category;
            }
            return null;
        }
    }
    
    /**
     * 更新分类
     */
    public UserPersonalCategory update(UserPersonalCategory category) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            int result = mapper.update(category);
            if (result > 0) {
                return category;
            }
            return null;
        }
    }
    
    /**
     * 删除分类
     */
    public boolean deleteById(Integer categoryId) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            return mapper.deleteById(categoryId) > 0;
        }
    }
    
    /**
     * 根据用户卡号和分类名称删除分类
     */
    public boolean deleteByCardNumAndName(String cardNum, String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            return mapper.deleteByCardNumAndCategoryName(cardNum, categoryName) > 0;
        }
    }
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 检查分类是否存在
     */
    public boolean existsByCardNumAndName(String cardNum, String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            return mapper.existsByCardNumAndName(cardNum, categoryName);
        }
    }
    
    /**
     * 统计用户分类数量
     */
    public long countByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            return mapper.countByCardNum(cardNum);
        }
    }
    
    /**
     * 获取用户所有分类名称
     */
    public List<String> getCategoryNamesByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            return mapper.getCategoryNamesByCardNum(cardNum);
        }
    }
    
    /**
     * 更新分类排序
     */
    public boolean updateSortOrder(Integer categoryId, Integer sortOrder) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            UserPersonalCategoryMapper mapper = session.getMapper(UserPersonalCategoryMapper.class);
            return mapper.updateSortOrder(categoryId, sortOrder, java.time.LocalDateTime.now()) > 0;
        }
    }
}
