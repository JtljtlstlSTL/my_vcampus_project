package com.vcampus.server.core.card.mapper;

import com.vcampus.server.core.card.entity.Card;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface CardMapper {

    Card findByCardNum(@Param("cardNum") String cardNum);

    int insert(Card card);

    int update(Card card);

    int deleteByCardNum(@Param("cardNum") String cardNum);

    boolean existsByCardNum(@Param("cardNum") String cardNum);

    /**
     * 以原子方式更新余额： balance = balance + delta
     * 返回受影响行数
     */
    int updateBalanceDelta(@Param("cardNum") String cardNum, @Param("delta") BigDecimal delta);

    /**
     * 原子性扣款：当余额 >= amount 时执行扣款，返回受影响行数
     */
    int decreaseBalanceIfEnough(@Param("cardNum") String cardNum, @Param("amount") BigDecimal amount);
}
