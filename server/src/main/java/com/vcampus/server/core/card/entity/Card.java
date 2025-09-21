package com.vcampus.server.core.card.entity;

import java.math.BigDecimal;

public class Card {
    private Integer cardId;
    private String cardNum;
    private BigDecimal balance;
    private String status;

    // getters and setters
    public Integer getCardId() { return cardId; }
    public void setCardId(Integer cardId) { this.cardId = cardId; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

