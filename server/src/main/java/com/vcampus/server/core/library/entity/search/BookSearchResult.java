package com.vcampus.server.core.library.entity.search;

import com.vcampus.server.core.library.entity.core.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图书搜索结果实体类 - 对应存储过程 sp_search_books 返回结果
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchResult {
    private List<Book> books; // 搜索结果图书列表
    private int totalCount; // 总数量
    private LocalDateTime searchTime; // 搜索时间
}
