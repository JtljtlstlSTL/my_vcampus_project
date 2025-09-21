package com.vcampus.server.core.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品分类实体 - 对应表 tblProductCategory
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory {
    private Integer categoryId;   // category_id
    private String categoryCode;  // category_code
    private String categoryName;  // category_name
    private Integer sortOrder;    // sort_order
}

