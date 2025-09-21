package com.vcampus.server.core.library.entity.core;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.vcampus.server.core.library.enums.BookStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书实体类 - 对应数据库表 tblBook
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Integer bookId; // 图书ID - 主键，自增
    private String isbn; // ISBN号 - 唯一标识
    private String title; // 书名
    private String author; // 作者
    private String publisher; // 出版社
    private LocalDate publishDate; // 出版日期
    private String category; // 图书分类代码（中图法代码A-Z）
    private String location; // 馆藏位置
    private Integer totalQty; // 馆藏总数量
    private Integer availQty; // 当前可借数量
    private BookStatus status; // 图书状态
    private LocalDateTime addTime; // 添加时间
    private LocalDateTime updateTime; // 更新时间
    
    // 静态工厂方法，创建默认图书
    public static Book createDefault() {
        return Book.builder()
                .totalQty(1)
                .availQty(1)
                .status(BookStatus.IN_LIBRARY)
                .build();
    }
    
    // 静态工厂方法，创建新图书
    public static Book createNew(String isbn, String title, String author, String publisher, 
                                LocalDate publishDate, String category, String location, Integer totalQty) {
        return Book.builder()
                .isbn(isbn)
                .title(title)
                .author(author)
                .publisher(publisher)
                .publishDate(publishDate)
                .category(category)
                .location(location)
                .totalQty(totalQty)
                .availQty(totalQty)
                .status(BookStatus.IN_LIBRARY)
                .build();
    }
    
    // 业务方法
    public boolean isAvailable() { // 判断图书是否可借阅
        return availQty != null && availQty > 0 && status == BookStatus.IN_LIBRARY;
    }
    
    public boolean isOutOfStock() { // 判断图书是否已借完
        return availQty == null || availQty <= 0;
    }
    
    public Integer getBorrowedQty() { // 获取借出数量
        if (totalQty == null || availQty == null) {
            return 0;
        }
        return totalQty - availQty;
    }
    
    // 兼容性方法 - 为了与现有代码兼容
    public String getBookIsbn() {
        return this.isbn;
    }
    
    public void setBookStatus(BookStatus status) {
        this.status = status;
    }
}
