package com.vcampus.server.core.library.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 最近借阅记录实体类 - 对应视图 v_recent_borrows
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentBorrow {
    private Integer transId; // 借阅记录ID
    private String cardNum; // 用户卡号
    private String userName; // 用户姓名
    private String bookTitle; // 图书标题
    private String bookAuthor; // 图书作者
    private LocalDateTime borrowTime; // 借阅时间
    private LocalDateTime dueTime; // 应还时间
    private String status; // 状态
    private Integer daysSinceBorrow; // 借阅天数
}
