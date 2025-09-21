package com.vcampus.server.core.shop.service.impl;

import com.vcampus.server.core.shop.dao.ProductCategoryDao;
import com.vcampus.server.core.shop.entity.ProductCategory;
import com.vcampus.server.core.shop.service.ProductCategoryService;

import java.util.List;
import java.util.Optional;

public class ProductCategoryServiceImpl implements ProductCategoryService {
    private static ProductCategoryServiceImpl instance;
    private final ProductCategoryDao categoryDao;

    private ProductCategoryServiceImpl() {
        this.categoryDao = ProductCategoryDao.getInstance();
    }

    public static synchronized ProductCategoryServiceImpl getInstance() {
        if (instance == null) {
            instance = new ProductCategoryServiceImpl();
        }
        return instance;
    }

    @Override
    public ProductCategory createCategory(ProductCategory category) {
        if (category.getCategoryCode() == null || category.getCategoryCode().trim().isEmpty()) {
            throw new IllegalArgumentException("分类编码不能为空");
        }
        return categoryDao.save(category);
    }

    @Override
    public ProductCategory updateCategory(ProductCategory category) {
        if (category.getCategoryId() == null || !categoryDao.existsById(category.getCategoryId())) {
            throw new RuntimeException("分类不存在: " + category.getCategoryId());
        }
        return categoryDao.save(category);
    }

    @Override
    public Optional<ProductCategory> getById(Integer categoryId) {
        return categoryDao.findById(categoryId);
    }

    @Override
    public Optional<ProductCategory> getByCode(String categoryCode) {
        return categoryDao.findByCode(categoryCode);
    }

    @Override
    public Optional<ProductCategory> getByName(String categoryName) {
        return categoryDao.findByName(categoryName);
    }

    @Override
    public List<ProductCategory> listAll() {
        return categoryDao.findAll();
    }

    @Override
    public void deleteById(Integer categoryId) {
        categoryDao.deleteById(categoryId);
    }

    @Override
    public boolean existsById(Integer categoryId) {
        return categoryDao.existsById(categoryId);
    }
}

