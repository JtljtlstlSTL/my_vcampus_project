package com.vcampus.server.core.shop.service;

import com.vcampus.server.core.shop.entity.ProductTrans;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductTransService {
    ProductTrans createTransaction(Integer productId, String cardNum, int qty);
    ProductTrans createTransactionAtomic(Integer productId, String cardNum, int qty);
    List<ProductTrans> createCartTransactionAtomic(Map<Integer, Integer> cartItems, String cardNum);
    Optional<ProductTrans> getById(Integer transId);
    List<ProductTrans> listAll();
    List<ProductTrans> findByCardNum(String cardNum);
    List<ProductTrans> findByProductId(Integer productId);
    void changeStatus(Integer transId, com.vcampus.server.core.shop.enums.OrderStatus status);
    void deleteById(Integer transId);
    long count();
    long countByCardNum(String cardNum);
    long countByProductId(Integer productId);
}
