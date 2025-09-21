package com.vcampus.server.core.shop.dao;

import com.vcampus.server.core.shop.entity.ProductTrans;
import com.vcampus.server.core.shop.mapper.ProductTransMapper;
import com.vcampus.server.core.shop.enums.OrderStatus;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 商品交易记录数据访问对象 - 使用MyBatis
 */
public class ProductTransDao {
    private static ProductTransDao instance;
    private final SqlSessionFactory sqlSessionFactory;

    private ProductTransDao() {
        // 统一使用全局 DatabaseManager 提供的 SqlSessionFactory，避免多个连接池/配置不一致
        this.sqlSessionFactory = com.vcampus.server.core.db.DatabaseManager.getSqlSessionFactory();
    }

    public static synchronized ProductTransDao getInstance() {
        if (instance == null) {
            instance = new ProductTransDao();
        }
        return instance;
    }

    public Optional<ProductTrans> findById(Integer transId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            return Optional.ofNullable(mapper.findById(transId));
        }
    }

    public List<ProductTrans> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            return mapper.findAll();
        }
    }

    public List<ProductTrans> findByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            return mapper.findByCardNum(cardNum);
        }
    }

    public List<ProductTrans> findByProductId(Integer productId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            return mapper.findByProductId(productId);
        }
    }

    public List<ProductTrans> findRecentOrders(int limit) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            return mapper.findRecentOrders(limit);
        }
    }

    public ProductTrans save(ProductTrans trans) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            if (trans.getTransId() == null) {
                mapper.insert(trans);
            } else {
                mapper.update(trans);
            }
            session.commit();
            return trans;
        }
    }

    public void deleteById(Integer transId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            mapper.deleteById(transId);
            session.commit();
        }
    }

    public boolean existsById(Integer transId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            return mapper.existsById(transId);
        }
    }

    public long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            return mapper.count();
        }
    }

    public long countOrdersByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            return mapper.countOrdersByCardNum(cardNum);
        }
    }

    public long countOrdersByProductId(Integer productId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            return mapper.countOrdersByProductId(productId);
        }
    }

    public void updateStatus(Integer transId, OrderStatus status) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            mapper.updateStatus(transId, status);
            session.commit();
        }
    }

    public void updateTransTime(Integer transId, java.time.LocalDateTime transTime) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductTransMapper mapper = session.getMapper(ProductTransMapper.class);
            mapper.updateTransTime(transId, transTime);
            session.commit();
        }
    }
}
