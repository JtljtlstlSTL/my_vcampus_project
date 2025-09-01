package com.vcampus.common.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 分页结果封装类
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> content;        // 当前页数据
    private int page;               // 当前页码（从0开始）
    private int size;               // 每页大小
    private long totalElements;     // 总记录数
    private int totalPages;         // 总页数
    
    /**
     * 创建分页结果
     */
    public PageResult(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }
    
    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
    
    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return page < totalPages - 1;
    }
    
    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return page > 0;
    }
}
