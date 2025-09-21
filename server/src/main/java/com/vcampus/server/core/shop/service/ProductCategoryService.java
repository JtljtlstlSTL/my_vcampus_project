package com.vcampus.server.core.shop.service;

import com.vcampus.server.core.shop.entity.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryService {
    ProductCategory createCategory(ProductCategory category);
    ProductCategory updateCategory(ProductCategory category);
    Optional<ProductCategory> getById(Integer categoryId);
    Optional<ProductCategory> getByCode(String categoryCode);
    Optional<ProductCategory> getByName(String categoryName);
    List<ProductCategory> listAll();
    void deleteById(Integer categoryId);
    boolean existsById(Integer categoryId);
}

