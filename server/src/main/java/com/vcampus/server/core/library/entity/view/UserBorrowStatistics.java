package com.vcampus.server.core.library.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户借阅统计实体类 - 对应视图 v_user_borrow_statistics
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBorrowStatistics {
    private String cardNum; // 用户卡号
    private String name; // 用户姓名
    private String userType; // 用户类型
    private Long totalBorrows; // 总借阅次数
    private Long currentBorrows; // 当前借阅数量
    private Long overdueCount; // 逾期数量
    private Long returnedCount; // 已归还数量
}
