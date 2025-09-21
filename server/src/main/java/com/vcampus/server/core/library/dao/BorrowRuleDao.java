package com.vcampus.server.core.library.dao;

import com.vcampus.server.core.library.entity.core.BorrowRule;
import com.vcampus.server.core.library.mapper.BorrowRuleMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 借阅规则数据访问对象 - 使用MyBatis
 * 提供借阅规则的CRUD操作和业务查询方法
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class BorrowRuleDao {
    
    private static BorrowRuleDao instance;
    private final SqlSessionFactory sqlSessionFactory;
    
    private BorrowRuleDao() {
        // 初始化MyBatis SqlSessionFactory
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mybatis-config.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("初始化MyBatis失败", e);
        }
    }
    
    public static synchronized BorrowRuleDao getInstance() {
        if (instance == null) {
            instance = new BorrowRuleDao();
        }
        return instance;
    }
    
    // ==================== 基础CRUD方法 ====================
    
    /**
     * 根据ID查找规则
     */
    public Optional<BorrowRule> findById(Integer ruleId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            BorrowRule rule = mapper.findById(ruleId);
            return Optional.ofNullable(rule);
        }
    }
    
    /**
     * 查找所有规则
     */
    public List<BorrowRule> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            return mapper.findAll();
        }
    }
    
    /**
     * 保存规则（新增或更新）
     */
    public BorrowRule save(BorrowRule rule) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            if (rule.getRuleId() == null) {
                mapper.insert(rule);
            } else {
                mapper.update(rule);
            }
            session.commit();
            return rule;
        }
    }
    
    /**
     * 根据ID删除规则
     */
    public void deleteById(Integer ruleId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            mapper.deleteById(ruleId);
            session.commit();
        }
    }
    
    /**
     * 检查规则是否存在
     */
    public boolean existsById(Integer ruleId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            return mapper.existsById(ruleId);
        }
    }
    
    /**
     * 统计规则数量
     */
    public long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            return mapper.count();
        }
    }
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 根据用户类型查找借阅规则
     */
    public Optional<BorrowRule> findByUserType(com.vcampus.server.core.library.enums.UserType userType) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            BorrowRule rule = mapper.findByUserType(userType.getCode());
            return Optional.ofNullable(rule);
        }
    }
    
    /**
     * 查询所有启用的借阅规则
     */
    public List<BorrowRule> findAllActiveRules() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            return mapper.findAllActiveRules();
        }
    }
    
    /**
     * 查询所有借阅规则（包括禁用的）
     */
    public List<BorrowRule> findAllRules() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            return mapper.findAllRules();
        }
    }
    
    /**
     * 根据最大借阅数量范围查询规则
     */
    public List<BorrowRule> findByMaxBorrowCountRange(int minCount, int maxCount) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            return mapper.findByMaxBorrowCountRange(minCount, maxCount);
        }
    }
    
    /**
     * 根据最大借阅天数范围查询规则
     */
    public List<BorrowRule> findByMaxBorrowDaysRange(int minDays, int maxDays) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            return mapper.findByMaxBorrowDaysRange(minDays, maxDays);
        }
    }
    
    /**
     * 根据逾期罚金范围查询规则
     */
    public List<BorrowRule> findByOverdueFineRange(BigDecimal minFine, BigDecimal maxFine) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            return mapper.findByOverdueFineRange(minFine, maxFine);
        }
    }
    
    /**
     * 检查用户类型是否已有规则
     */
    public boolean isUserTypeRuleExists(com.vcampus.server.core.library.enums.UserType userType) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            return mapper.isUserTypeRuleExists(userType.getCode());
        }
    }
    
    /**
     * 检查用户类型是否已有启用的规则
     */
    public boolean isActiveUserTypeRuleExists(com.vcampus.server.core.library.enums.UserType userType) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            return mapper.isActiveUserTypeRuleExists(userType.getCode());
        }
    }
    
    /**
     * 启用规则
     */
    public void activateRule(Integer ruleId) {
        updateRuleStatus(ruleId, true);
    }
    
    /**
     * 禁用规则
     */
    public void deactivateRule(Integer ruleId) {
        updateRuleStatus(ruleId, false);
    }
    
    /**
     * 更新规则状态
     */
    private void updateRuleStatus(Integer ruleId, boolean isActive) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BorrowRuleMapper mapper = session.getMapper(BorrowRuleMapper.class);
            int affected = mapper.updateRuleStatus(ruleId, isActive);
            if (affected == 0) {
                throw new RuntimeException("更新规则状态失败：未找到ID为 " + ruleId + " 的规则");
            }
            session.commit();
        }
    }
}