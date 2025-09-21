package com.vcampus.server.core.library.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类统计实体类 - 对应视图 v_category_statistics
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatistics {
    private String categoryCode; // 分类代码
    private String categoryName; // 分类名称
    private Long totalBooks; // 总图书数量
    private Long totalCopies; // 总副本数量
    private Long availableCopies; // 可借副本数量
    private Long totalBorrows; // 总借阅次数
}
