package com.vcampus.server.core.library.entity.view;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户借阅历史视图实体类 - 对应数据库视图 v_user_borrow_history
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBorrowHistory {
    private Integer transId; // 借阅记录ID
    private String cardNum; // 用户卡号
    private String userName; // 用户姓名
    private String userType; // 用户类型
    private String bookTitle; // 图书标题
    private String bookAuthor; // 图书作者
    private String bookCategory; // 图书分类
    private String bookCategoryName; // 图书分类名称
    private LocalDateTime borrowTime; // 借阅时间
    private LocalDateTime dueTime; // 应还时间
    private LocalDateTime returnTime; // 归还时间
    private String borrowStatus; // 借阅状态
    private Integer renewCount; // 续借次数
    private String displayStatus; // 显示状态（借阅中/已逾期/已归还）
}
