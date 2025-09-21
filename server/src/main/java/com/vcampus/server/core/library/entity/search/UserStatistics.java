package com.vcampus.server.core.library.entity.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户统计信息实体类 - 对应存储过程 sp_get_user_statistics 返回结果
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {
    private String cardNum; // 用户卡号
    private String name; // 用户姓名
    private String userType; // 用户类型
    private Long totalBorrows; // 总借阅次数
    private Long currentBorrows; // 当前借阅数量
    private Long overdueCount; // 逾期数量
    private Long returnedCount; // 已归还数量
    private BigDecimal avgBorrowDays; // 平均借阅天数
}
