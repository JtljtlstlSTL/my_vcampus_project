package com.vcampus.server.core.library.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 逾期详情实体类 - 对应视图 v_overdue_details
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueDetails {
    private Integer transId; // 借阅记录ID
    private String cardNum; // 用户卡号
    private String userName; // 用户姓名
    private String userType; // 用户类型
    private String bookTitle; // 图书标题
    private String bookAuthor; // 图书作者
    private LocalDateTime borrowTime; // 借阅时间
    private LocalDateTime dueTime; // 应还时间
    private Integer overdueDays; // 逾期天数
    private Integer renewCount; // 续借次数
}
