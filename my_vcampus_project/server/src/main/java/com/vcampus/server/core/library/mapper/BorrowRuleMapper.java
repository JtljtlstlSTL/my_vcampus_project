package com.vcampus.server.core.library.mapper;

import com.vcampus.server.core.library.entity.BorrowRule;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

/**
 * 借阅规则数据访问接口 - 使用MyBatis
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface BorrowRuleMapper {
    
    /**
     * 根据ID查找规则
     */
    BorrowRule findById(@Param("ruleId") Integer ruleId);
    
    /**
     * 查找所有规则
     */
    List<BorrowRule> findAll();
    
    /**
     * 根据用户类型查找借阅规则
     */
    BorrowRule findByUserType(@Param("userType") String userType);
    
    /**
     * 查询所有启用的借阅规则
     */
    List<BorrowRule> findAllActiveRules();
    
    /**
     * 查询所有借阅规则（包括禁用的）
     */
    List<BorrowRule> findAllRules();
    
    /**
     * 根据最大借阅数量范围查询规则
     */
    List<BorrowRule> findByMaxBorrowCountRange(@Param("minCount") int minCount, @Param("maxCount") int maxCount);
    
    /**
     * 根据最大借阅天数范围查询规则
     */
    List<BorrowRule> findByMaxBorrowDaysRange(@Param("minDays") int minDays, @Param("maxDays") int maxDays);
    
    /**
     * 根据逾期罚金范围查询规则
     */
    List<BorrowRule> findByOverdueFineRange(@Param("minFine") BigDecimal minFine, @Param("maxFine") BigDecimal maxFine);
    
    /**
     * 插入规则
     */
    int insert(BorrowRule rule);
    
    /**
     * 更新规则
     */
    int update(BorrowRule rule);
    
    /**
     * 根据ID删除规则
     */
    int deleteById(@Param("ruleId") Integer ruleId);
    
    /**
     * 检查规则是否存在
     */
    boolean existsById(@Param("ruleId") Integer ruleId);
    
    /**
     * 统计规则数量
     */
    long count();
    
    /**
     * 检查用户类型是否已有规则
     */
    boolean isUserTypeRuleExists(@Param("userType") String userType);
    
    /**
     * 检查用户类型是否已有启用的规则
     */
    boolean isActiveUserTypeRuleExists(@Param("userType") String userType);
    
    /**
     * 更新规则状态
     */
    int updateRuleStatus(@Param("ruleId") Integer ruleId, @Param("isActive") boolean isActive);
}
