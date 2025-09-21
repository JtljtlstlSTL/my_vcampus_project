package com.vcampus.server.core.library.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书借阅统计实体类 - 对应视图 v_book_borrow_statistics
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookBorrowStatistics {
    private Integer bookId; // 图书ID
    private String title; // 书名
    private String author; // 作者
    private String category; // 分类代码
    private String categoryName; // 分类名称
    private Integer totalQty; // 总数量
    private Integer availQty; // 可借数量
    private Long totalBorrows; // 总借阅次数
    private Long currentBorrows; // 当前借阅数量
    private Long overdueCount; // 逾期数量
    private Double borrowRate; // 借阅率
}
