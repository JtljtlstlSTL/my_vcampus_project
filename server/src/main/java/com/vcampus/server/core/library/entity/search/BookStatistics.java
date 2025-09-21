package com.vcampus.server.core.library.entity.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书统计信息实体类 - 对应存储过程 sp_get_book_statistics 返回结果
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookStatistics {
    private Integer bookId; // 图书ID
    private String title; // 书名
    private String author; // 作者
    private String category; // 分类代码
    private String categoryName; // 分类名称
    private Integer totalQty; // 馆藏总数量
    private Integer availQty; // 当前可借数量
    private Long totalBorrows; // 总借阅次数
    private Long currentBorrows; // 当前借阅数量
    private Long overdueCount; // 逾期数量
    private Long returnedCount; // 已归还数量
}
