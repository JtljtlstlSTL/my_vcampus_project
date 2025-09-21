package com.vcampus.server.core.shop.dao;

import com.vcampus.server.core.shop.entity.Product;
import com.vcampus.server.core.shop.enums.ProductStatus;
import com.vcampus.server.core.shop.mapper.ProductMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;
import java.util.Optional;

import com.vcampus.server.core.db.DatabaseManager;

/**
 * 商品数据访问对象 - 使用MyBatis
 * 提供商品实体的CRUD操作和业务查询方法
 */
public class ProductDao {

    private static ProductDao instance;
    private final SqlSessionFactory sqlSessionFactory;

    private ProductDao() {
        // 使用全局 DatabaseManager 提供的 SqlSessionFactory，避免重复加载配置
        this.sqlSessionFactory = DatabaseManager.getSqlSessionFactory();
    }

    public static synchronized ProductDao getInstance() {
        if (instance == null) {
            instance = new ProductDao();
        }
        return instance;
    }

    // ==================== 基础CRUD ====================

    public Optional<Product> findById(Integer productId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            return Optional.ofNullable(mapper.findById(productId));
        }
    }

    public List<Product> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            return mapper.findAll();
        }
    }

    public Product save(Product product) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            if (product.getProductId() == null) {
                mapper.insert(product);
            } else {
                mapper.update(product);
            }
            session.commit();
            return product;
        }
    }

    public void deleteById(Integer productId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            mapper.deleteById(productId);
            session.commit();
        }
    }

    public boolean existsById(Integer productId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            return mapper.existsById(productId);
        }
    }

    public long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            return mapper.count();
        }
    }

    // ==================== 业务查询 ====================

    public Optional<Product> findByCode(String productCode) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            return Optional.ofNullable(mapper.findByCode(productCode));
        }
    }

    public List<Product> findByNameLike(String name) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            return mapper.findByNameLike(name);
        }
    }

    public List<Product> findByCategory(String category) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            return mapper.findByCategory(category);
        }
    }

    public List<Product> findAvailableProducts() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            try {
                ProductMapper mapper = session.getMapper(ProductMapper.class);
                return mapper.findAvailableProducts();
            } catch (Exception e) {
                // 降级：如果 Mapper 未注册或发生绑定异常，使用 JDBC 原生查询作为后备
                session.clearCache();
                java.sql.Connection conn = session.getConnection();
                java.util.List<Product> result = new java.util.ArrayList<>();
                // 修改：不要以 Stock>0 作为可见性过滤条件，仅按上架状态返回，允许库存为0的商品被展示（前端显示售罄）
                String sql = "SELECT product_Id, Product_code, Productname, Price, Stock, Product_status, Product_description, Product_category, updated_at FROM tblProduct WHERE Product_status = 'ON_SHELF' ORDER BY Productname";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Product p = new Product();
                        p.setProductId(rs.getInt("product_Id"));
                        p.setProductCode(rs.getString("Product_code"));
                        p.setProductName(rs.getString("Productname"));
                        p.setPrice(rs.getBigDecimal("Price"));
                        p.setStock(rs.getInt("Stock"));
                        Object st = rs.getString("Product_status");
                        if (st != null) try { p.setStatus(com.vcampus.server.core.shop.enums.ProductStatus.valueOf(st.toString())); } catch (Exception ex) { /* ignore */ }
                        p.setDescription(rs.getString("Product_description"));
                        p.setCategory(rs.getString("Product_category"));
                        java.sql.Timestamp ts = rs.getTimestamp("updated_at");
                        if (ts != null) {
                            try {
                                p.setUpdatedAt(ts.toLocalDateTime());
                            } catch (Exception ignore) {
                                // 兼容旧字段类型，如实体使用 java.util.Date 或 Timestamp，可直接设为 null 或跳过
                            }
                        }
                        result.add(p);
                    }
                } catch (Exception ex2) {
                    throw new RuntimeException("查询上架商品失败", ex2);
                }
                return result;
            }
        }
    }

    public void updateStock(Integer productId, int stock) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            int affected = mapper.updateStock(productId, stock);
            if (affected == 0) {
                throw new RuntimeException("更新库存失败：未找到ID为 " + productId + " 的商品");
            }
            session.commit();
        }
    }

    public void updateStatus(Integer productId, ProductStatus status) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            int affected = mapper.updateStatus(productId, status);
            if (affected == 0) {
                throw new RuntimeException("更新状态失败：未找到ID为 " + productId + " 的商品");
            }
            session.commit();
        }
    }

    // ==================== 并发安全库存扣减 ====================
    public boolean decreaseStockIfEnough(Integer productId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("扣减数量必须>0");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            int affected = mapper.decreaseStockIfEnough(productId, qty);
            session.commit();
            return affected > 0;
        }
    }

    // 失败补偿：增加库存
    public void increaseStock(Integer productId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("增加数量必须>0");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            int affected = mapper.increaseStock(productId, qty);
            if (affected == 0) {
                throw new RuntimeException("增加库存失败：未找到ID为 " + productId + " 的商品");
            }
            session.commit();
        }
    }
}
