package com.vcampus.server.core.library.entity.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门图书实体类 - 对应视图 v_popular_books 和存储过程 sp_search_popular_books
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularBook {
    private Integer bookId; // 图书ID
    private String title; // 书名
    private String author; // 作者
    private String category; // 分类代码
    private String categoryName; // 分类名称
    private Integer totalQty; // 总数量
    private Integer availQty; // 可借数量
    private Long borrowCount; // 借阅次数
    private Integer popularityRank; // 热门排名
}
