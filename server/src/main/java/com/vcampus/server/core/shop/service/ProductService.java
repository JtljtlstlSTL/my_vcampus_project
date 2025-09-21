package com.vcampus.server.core.shop.service;

import com.vcampus.server.core.shop.entity.Product;
import com.vcampus.server.core.shop.enums.ProductStatus;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product createProduct(Product product);
    Product updateProduct(Product product);
    Optional<Product> getById(Integer productId);
    Optional<Product> getByCode(String productCode);
    List<Product> listAll();
    List<Product> findByNameLike(String name);
    List<Product> findByCategory(String categoryCode);
    List<Product> findAvailableProducts();
    void changeStock(Integer productId, int delta);
    void changeStatus(Integer productId, ProductStatus status);
    void deleteById(Integer productId);
}

