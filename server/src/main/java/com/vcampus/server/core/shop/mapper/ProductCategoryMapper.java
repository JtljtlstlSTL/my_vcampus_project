package com.vcampus.server.core.shop.mapper;

import com.vcampus.server.core.shop.entity.ProductCategory;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 商品分类数据访问接口 - 使用MyBatis
 */
public interface ProductCategoryMapper {

    ProductCategory findById(@Param("categoryId") Integer categoryId);

    List<ProductCategory> findAll();

    ProductCategory findByCode(@Param("categoryCode") String categoryCode);

    ProductCategory findByName(@Param("categoryName") String categoryName);

    int insert(ProductCategory category);

    int update(ProductCategory category);

    int deleteById(@Param("categoryId") Integer categoryId);

    boolean existsById(@Param("categoryId") Integer categoryId);

    long count();
}

