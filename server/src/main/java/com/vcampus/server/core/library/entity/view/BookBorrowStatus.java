package com.vcampus.server.core.library.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书借阅状态视图实体类 - 对应数据库视图 v_book_borrow_status
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookBorrowStatus {
    private Integer bookId; // 图书ID
    private String isbn; // ISBN号
    private String title; // 书名
    private String author; // 作者
    private String category; // 分类代码
    private String categoryName; // 分类名称
    private String location; // 馆藏位置
    private Integer totalQty; // 馆藏总数量
    private Integer availQty; // 当前可借数量
    private String bookStatus; // 图书状态
    private String borrowStatus; // 借阅状态（可借阅/已借完）
}
