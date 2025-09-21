package com.vcampus.server.core.card.service;

import com.vcampus.server.core.card.entity.Card;

public interface CardService {
    Card findByCardNum(String cardNum);
    boolean recharge(String cardNum, double amount);
    boolean consume(String cardNum, double amount); // 扣款/消费方法，返回是否成功
    // TODO: 其他业务方法
}
