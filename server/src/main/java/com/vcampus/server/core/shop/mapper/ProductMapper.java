package com.vcampus.server.core.shop.mapper;

import com.vcampus.server.core.shop.entity.Product;
import com.vcampus.server.core.shop.enums.ProductStatus;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 商品数据访问接口 - 使用MyBatis
 */
public interface ProductMapper {

    Product findById(@Param("productId") Integer productId);

    List<Product> findAll();

    Product findByCode(@Param("productCode") String productCode);

    List<Product> findByNameLike(@Param("name") String name);

    List<Product> findByCategory(@Param("category") String category);

    List<Product> findAvailableProducts();

    int insert(Product product);

    int update(Product product);

    int deleteById(@Param("productId") Integer productId);

    boolean existsById(@Param("productId") Integer productId);

    long count();

    int updateStock(@Param("productId") Integer productId, @Param("stock") int stock);

    int updateStatus(@Param("productId") Integer productId, @Param("status") ProductStatus status);

    int decreaseStockIfEnough(@Param("productId") Integer productId, @Param("qty") int qty);

    int increaseStock(@Param("productId") Integer productId, @Param("qty") int qty);
}
