package com.vcampus.server.core.shop.dao;

import com.vcampus.server.core.shop.entity.ProductCategory;
import com.vcampus.server.core.shop.mapper.ProductCategoryMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 商品分类数据访问对象 - 使用MyBatis
 */
public class ProductCategoryDao {
    private static ProductCategoryDao instance;
    private final SqlSessionFactory sqlSessionFactory;

    private ProductCategoryDao() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mybatis-config.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("初始化MyBatis失败", e);
        }
    }

    public static synchronized ProductCategoryDao getInstance() {
        if (instance == null) {
            instance = new ProductCategoryDao();
        }
        return instance;
    }

    public Optional<ProductCategory> findById(Integer categoryId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductCategoryMapper mapper = session.getMapper(ProductCategoryMapper.class);
            return Optional.ofNullable(mapper.findById(categoryId));
        }
    }

    public List<ProductCategory> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductCategoryMapper mapper = session.getMapper(ProductCategoryMapper.class);
            return mapper.findAll();
        }
    }

    public Optional<ProductCategory> findByCode(String categoryCode) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductCategoryMapper mapper = session.getMapper(ProductCategoryMapper.class);
            return Optional.ofNullable(mapper.findByCode(categoryCode));
        }
    }

    public Optional<ProductCategory> findByName(String categoryName) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductCategoryMapper mapper = session.getMapper(ProductCategoryMapper.class);
            return Optional.ofNullable(mapper.findByName(categoryName));
        }
    }

    public ProductCategory save(ProductCategory category) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductCategoryMapper mapper = session.getMapper(ProductCategoryMapper.class);
            if (category.getCategoryId() == null) {
                mapper.insert(category);
            } else {
                mapper.update(category);
            }
            session.commit();
            return category;
        }
    }

    public void deleteById(Integer categoryId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductCategoryMapper mapper = session.getMapper(ProductCategoryMapper.class);
            mapper.deleteById(categoryId);
            session.commit();
        }
    }

    public boolean existsById(Integer categoryId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductCategoryMapper mapper = session.getMapper(ProductCategoryMapper.class);
            return mapper.existsById(categoryId);
        }
    }

    public long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductCategoryMapper mapper = session.getMapper(ProductCategoryMapper.class);
            return mapper.count();
        }
    }
}

