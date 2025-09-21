package com.vcampus.server.core.library.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 逾期统计实体类 - 对应视图 v_overdue_statistics
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueStatistics {
    private String userType; // 用户类型
    private Long overdueCount; // 逾期数量
    private Double avgOverdueDays; // 平均逾期天数
    private Long maxOverdueDays; // 最大逾期天数
}
