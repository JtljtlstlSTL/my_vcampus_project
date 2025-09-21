package com.vcampus.server.core.shop.service.impl;

import com.vcampus.server.core.shop.dao.ProductCategoryDao;
import com.vcampus.server.core.shop.dao.ProductDao;
import com.vcampus.server.core.shop.entity.Product;
import com.vcampus.server.core.shop.enums.ProductStatus;
import com.vcampus.server.core.shop.service.ProductService;

import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {
    private static ProductServiceImpl instance;
    private final ProductDao productDao;
    private final ProductCategoryDao categoryDao;

    private ProductServiceImpl() {
        this.productDao = ProductDao.getInstance();
        this.categoryDao = ProductCategoryDao.getInstance();
    }

    public static synchronized ProductServiceImpl getInstance() {
        if (instance == null) {
            instance = new ProductServiceImpl();
        }
        return instance;
    }

    @Override
    public Product createProduct(Product product) {
        if (product.getCategory() != null) {
            if (!categoryDao.findByCode(product.getCategory()).isPresent()) {
                throw new RuntimeException("分类不存在: " + product.getCategory());
            }
        }
        return productDao.save(product);
    }

    @Override
    public Product updateProduct(Product product) {
        if (product.getProductId() == null || !productDao.existsById(product.getProductId())) {
            throw new RuntimeException("商品不存在: " + product.getProductId());
        }
        if (product.getCategory() != null) {
            if (!categoryDao.findByCode(product.getCategory()).isPresent()) {
                throw new RuntimeException("分类不存在: " + product.getCategory());
            }
        }
        return productDao.save(product);
    }

    @Override
    public Optional<Product> getById(Integer productId) {
        return productDao.findById(productId);
    }

    @Override
    public Optional<Product> getByCode(String productCode) {
        return productDao.findByCode(productCode);
    }

    @Override
    public List<Product> listAll() {
        return productDao.findAll();
    }

    @Override
    public List<Product> findByNameLike(String name) {
        return productDao.findByNameLike(name);
    }

    @Override
    public List<Product> findByCategory(String categoryCode) {
        return productDao.findByCategory(categoryCode);
    }

    @Override
    public List<Product> findAvailableProducts() {
        return productDao.findAvailableProducts();
    }

    @Override
    public void changeStock(Integer productId, int delta) {
        // 并发安全：正数使用原子自增，负数使用原子扣减；0 则不操作
        if (delta == 0) return;
        if (delta > 0) {
            productDao.increaseStock(productId, delta);
            // 如果之前状态为 SOLD_OUT 且增加后库存>0，则自动恢复为 ON_SHELF
            try {
                java.util.Optional<Product> maybe = productDao.findById(productId);
                if (maybe.isPresent()) {
                    Product p = maybe.get();
                    Integer stock = p.getStock();
                    com.vcampus.server.core.shop.enums.ProductStatus st = p.getStatus();
                    if (stock != null && stock > 0 && com.vcampus.server.core.shop.enums.ProductStatus.SOLD_OUT.equals(st)) {
                        productDao.updateStatus(productId, com.vcampus.server.core.shop.enums.ProductStatus.ON_SHELF);
                    }
                }
            } catch (Exception ignore) { /* 不影响主流程 */ }
        } else {
            boolean ok = productDao.decreaseStockIfEnough(productId, Math.abs(delta));
            if (!ok) {
                throw new RuntimeException("库存不足: 无法扣减 " + Math.abs(delta));
            }
        }
    }

    @Override
    public void changeStatus(Integer productId, ProductStatus status) {
        if (!productDao.existsById(productId)) {
            throw new RuntimeException("商品不存在: " + productId);
        }
        productDao.updateStatus(productId, status);
    }

    @Override
    public void deleteById(Integer productId) {
        productDao.deleteById(productId);
    }
}
