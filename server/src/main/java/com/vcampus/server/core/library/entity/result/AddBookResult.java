package com.vcampus.server.core.library.entity.result;

import com.vcampus.server.core.library.entity.core.Book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加图书结果实体类 - 对应存储过程 sp_add_book 返回结果
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddBookResult {
    private boolean success; // 是否成功
    private String message; // 结果信息
    private Book book; // 添加的图书信息
}
