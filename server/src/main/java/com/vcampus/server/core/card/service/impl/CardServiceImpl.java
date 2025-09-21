package com.vcampus.server.core.card.service.impl;

import com.vcampus.server.core.card.dao.CardDao;
import com.vcampus.server.core.card.entity.Card;
import com.vcampus.server.core.card.mapper.CardMapper;
import com.vcampus.server.core.card.service.CardService;
import com.vcampus.server.core.db.DatabaseManager;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.math.BigDecimal;

public class CardServiceImpl implements CardService {
    private static CardServiceImpl instance;
    private final CardDao cardDao;
    private final SqlSessionFactory sqlSessionFactory;

    private CardServiceImpl() {
        this.cardDao = CardDao.getInstance();
        this.sqlSessionFactory = DatabaseManager.getSqlSessionFactory();
    }

    public static synchronized CardServiceImpl getInstance() {
        if (instance == null) instance = new CardServiceImpl();
        return instance;
    }

    @Override
    public Card findByCardNum(String cardNum) {
        return cardDao.findByCardNum(cardNum).orElse(null);
    }

    @Override
    public boolean recharge(String cardNum, double amount) {
        if (cardNum == null || cardNum.isEmpty()) throw new IllegalArgumentException("cardNum不能为空");
        if (amount <= 0) throw new IllegalArgumentException("充值金额必须大于0");
        BigDecimal delta = BigDecimal.valueOf(amount);
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CardMapper mapper = session.getMapper(CardMapper.class);
            int affected = mapper.updateBalanceDelta(cardNum, delta);
            if (affected == 0) {
                session.commit();
                return false;
            }
            // 插入交易记录到 tblCard_trans（使用同一连接/事务）
            java.sql.Connection conn = session.getConnection();
            String sql = "INSERT INTO tblCard_trans (cardNum, Trans_time, Trans_type, Amount) VALUES (?, CURRENT_TIMESTAMP(6), ?, ?)";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cardNum);
                ps.setString(2, "RECHARGE");
                ps.setBigDecimal(3, delta);
                ps.executeUpdate();
            }
            session.commit();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("充值失败", e);
        }
    }

    @Override
    public boolean consume(String cardNum, double amount) {
        if (cardNum == null || cardNum.isEmpty()) throw new IllegalArgumentException("cardNum不能为空");
        if (amount <= 0) throw new IllegalArgumentException("消费金额必须大于0");
        BigDecimal amt = BigDecimal.valueOf(amount);
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CardMapper mapper = session.getMapper(CardMapper.class);
            int affected = mapper.decreaseBalanceIfEnough(cardNum, amt);
            if (affected == 0) {
                session.commit();
                return false; // 余额不足或卡不存在
            }
            // 插入交易记录到 tblCard_trans（使用同一连接/事务）
            java.sql.Connection conn = session.getConnection();
            String sql = "INSERT INTO tblCard_trans (cardNum, Trans_time, Trans_type, Amount) VALUES (?, CURRENT_TIMESTAMP(6), ?, ?)";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cardNum);
                ps.setString(2, "CONSUME");
                ps.setBigDecimal(3, amt);
                ps.executeUpdate();
            }
            session.commit();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("消费失败", e);
        }
    }
}
