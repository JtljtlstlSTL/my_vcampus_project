package com.vcampus.server.core.card.dao;

import com.vcampus.server.core.card.entity.Card;
import com.vcampus.server.core.card.mapper.CardMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.math.BigDecimal;
import java.util.Optional;

import com.vcampus.server.core.db.DatabaseManager;

/**
 * Card 数据访问对象 - 使用 MyBatis
 */
public class CardDao {

    private static CardDao instance;
    private final SqlSessionFactory sqlSessionFactory;

    private CardDao() {
        this.sqlSessionFactory = DatabaseManager.getSqlSessionFactory();
    }

    public static synchronized CardDao getInstance() {
        if (instance == null) {
            instance = new CardDao();
        }
        return instance;
    }

    public Optional<Card> findByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CardMapper mapper = session.getMapper(CardMapper.class);
            return Optional.ofNullable(mapper.findByCardNum(cardNum));
        }
    }

    public Card save(Card card) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CardMapper mapper = session.getMapper(CardMapper.class);
            if (card.getCardId() == null) {
                mapper.insert(card);
            } else {
                mapper.update(card);
            }
            session.commit();
            return card;
        }
    }

    public boolean existsByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CardMapper mapper = session.getMapper(CardMapper.class);
            return mapper.existsByCardNum(cardNum);
        }
    }

    /**
     * 原子性更新余额（增量，可为正或负）
     * @return 是否更新成功（受影响行数>0）
     */
    public boolean updateBalanceDelta(String cardNum, BigDecimal delta) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CardMapper mapper = session.getMapper(CardMapper.class);
            int affected = mapper.updateBalanceDelta(cardNum, delta);
            session.commit();
            return affected > 0;
        }
    }

    /**
     * 原子性扣款：当余额 >= amount 时执行扣款
     * @return 是否扣款成功（受影响行数>0）
     */
    public boolean decreaseBalanceIfEnough(String cardNum, BigDecimal amount) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CardMapper mapper = session.getMapper(CardMapper.class);
            int affected = mapper.decreaseBalanceIfEnough(cardNum, amount);
            session.commit();
            return affected > 0;
        }
    }

    public void deleteByCardNum(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CardMapper mapper = session.getMapper(CardMapper.class);
            mapper.deleteByCardNum(cardNum);
            session.commit();
        }
    }
}
