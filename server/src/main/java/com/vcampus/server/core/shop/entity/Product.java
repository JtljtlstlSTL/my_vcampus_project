package com.vcampus.server.core.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.vcampus.server.core.shop.enums.ProductStatus;

/**
 * 商品实体类 - 对应数据库表 tblProduct
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Integer productId;      // product_Id - 主键
    private String productCode;     // Product_code - 唯一
    private String productName;     // Productname
    private BigDecimal price;       // Price
    private Integer stock;          // Stock
    private ProductStatus status;   // Product_status（ON_SHELF/OFF_SHELF/SOLD_OUT）
    private String description;     // Product_description
    private String category;        // Product_category
    private LocalDateTime updatedAt; // updated_at

    // 工厂方法：创建默认商品
    public static Product createDefault() {
        return Product.builder()
                .price(new BigDecimal("0.00"))
                .stock(0)
                .status(ProductStatus.OFF_SHELF)
                .build();
    }

    // 工厂方法：创建新上架商品
    public static Product createNew(String productCode, String productName, String category, BigDecimal price, int stock, String description) {
        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .productCode(productCode)
                .productName(productName)
                .category(category)
                .price(price)
                .stock(stock)
                .status(stock > 0 ? ProductStatus.ON_SHELF : ProductStatus.SOLD_OUT)
                .description(description)
                .updatedAt(now)
                .build();
    }

    public boolean isAvailable() {
        return stock != null && stock > 0 && ProductStatus.ON_SHELF.equals(status);
    }
}
