package com.vcampus.server.core.library.entity.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 逾期检查结果实体类 - 对应存储过程 sp_check_overdue 返回结果
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueCheckResult {
    private boolean success; // 是否成功
    private String result; // 结果信息
    private Integer updatedCount; // 更新的记录数
}
