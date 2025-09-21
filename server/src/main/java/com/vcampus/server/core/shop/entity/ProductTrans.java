package com.vcampus.server.core.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.vcampus.server.core.shop.enums.OrderStatus;

/**
 * 商品购买记录实体 - 对应数据库表 tblProduct_trans
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTrans {
    private Integer transId;        // trans_Id - 主键
    private Integer productId;      // product_Id
    private String cardNum;         // cardNum
    private Integer qty;            // Qty
    private BigDecimal amount;      // Amount
    private LocalDateTime transTime;// Trans_time
    private OrderStatus status;     // status（CREATED/PAID/CANCELLED/REFUNDED）

    public boolean isPaid() {
        return OrderStatus.PAID.equals(status);
    }

    public boolean isCancellable() {
        return OrderStatus.CREATED.equals(status);
    }
}
