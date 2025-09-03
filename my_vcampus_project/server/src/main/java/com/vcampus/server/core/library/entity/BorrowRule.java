package com.vcampus.server.core.library.entity;

import java.math.BigDecimal;

import com.vcampus.server.core.library.enums.UserType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 借阅规则实体类 - 对应数据库表 tblBorrowRule
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRule {
    private Integer ruleId; // 规则ID - 主键，自增
    private UserType userType; // 用户类型
    private Integer maxBorrowCount; // 最大借阅数量
    private Integer maxBorrowDays; // 最大借阅天数
    private Integer maxRenewCount; // 最大续借次数
    private Integer renewExtendDays; // 续借延长天数
    private BigDecimal overdueFine; // 逾期罚金（每天）
    private Boolean isActive; // 是否启用
    
    // 静态工厂方法，创建默认规则
    public static BorrowRule createDefault(UserType userType) {
        return BorrowRule.builder()
                .userType(userType)
                .maxBorrowCount(5)
                .maxBorrowDays(30)
                .maxRenewCount(2)
                .renewExtendDays(15)
                .overdueFine(new BigDecimal("0.50"))
                .isActive(true)
                .build();
    }
    
    // 静态工厂方法，创建自定义规则
    public static BorrowRule create(UserType userType, Integer maxBorrowCount, Integer maxBorrowDays, 
                                   Integer maxRenewCount, Integer renewExtendDays, BigDecimal overdueFine) {
        return BorrowRule.builder()
                .userType(userType)
                .maxBorrowCount(maxBorrowCount)
                .maxBorrowDays(maxBorrowDays)
                .maxRenewCount(maxRenewCount)
                .renewExtendDays(renewExtendDays)
                .overdueFine(overdueFine)
                .isActive(true)
                .build();
    }
    
    // 业务方法
    public boolean isValid() { // 判断规则是否有效
        return userType != null && 
               maxBorrowCount != null && maxBorrowCount > 0 &&
               maxBorrowDays != null && maxBorrowDays > 0 &&
               maxRenewCount != null && maxRenewCount >= 0 &&
               renewExtendDays != null && renewExtendDays > 0 &&
               overdueFine != null && overdueFine.compareTo(BigDecimal.ZERO) >= 0 &&
               isActive != null;
    }
    
    public boolean canBorrow(int currentBorrowCount) { // 判断是否可以借阅指定数量的图书
        return isActive && currentBorrowCount < maxBorrowCount;
    }
    
    public boolean canRenew(int currentRenewCount) { // 判断是否可以续借指定次数
        return isActive && currentRenewCount < maxRenewCount;
    }
    
    public BigDecimal calculateOverdueFine(int overdueDays) { // 计算逾期罚金
        if (overdueDays <= 0 || overdueFine == null) {
            return BigDecimal.ZERO;
        }
        return overdueFine.multiply(new BigDecimal(overdueDays));
    }
    
    public String getUserTypeDisplayName() { // 获取用户类型的显示名称
        return userType != null ? userType.getDescription() : "未知";
    }
    
    public String getRuleDescription() { // 获取规则的简要描述
        if (!isValid()) {
            return "无效规则";
        }
        
        return String.format("%s: 最多借%d本，借期%d天，可续借%d次，每次延长%d天，逾期罚金%.2f元/天",
                getUserTypeDisplayName(),
                maxBorrowCount,
                maxBorrowDays,
                maxRenewCount,
                renewExtendDays,
                overdueFine.doubleValue());
    }
}
