package com.vcampus.server.core.shop.mapper;

import com.vcampus.server.core.shop.entity.ProductTrans;
import com.vcampus.server.core.shop.enums.OrderStatus;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品交易记录数据访问接口 - 使用MyBatis
 */
public interface ProductTransMapper {

    ProductTrans findById(@Param("transId") Integer transId);

    List<ProductTrans> findAll();

    List<ProductTrans> findByCardNum(@Param("cardNum") String cardNum);

    List<ProductTrans> findByProductId(@Param("productId") Integer productId);

    List<ProductTrans> findRecentOrders(@Param("limit") int limit);

    int insert(ProductTrans trans);

    int update(ProductTrans trans);

    int deleteById(@Param("transId") Integer transId);

    boolean existsById(@Param("transId") Integer transId);

    long count();

    long countOrdersByCardNum(@Param("cardNum") String cardNum);

    long countOrdersByProductId(@Param("productId") Integer productId);

    int updateStatus(@Param("transId") Integer transId, @Param("status") OrderStatus status);

    int updateTransTime(@Param("transId") Integer transId, @Param("transTime") LocalDateTime transTime);
}
