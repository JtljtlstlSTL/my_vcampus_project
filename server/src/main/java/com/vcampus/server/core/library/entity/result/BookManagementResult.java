package com.vcampus.server.core.library.entity.result;

import com.vcampus.server.core.library.entity.core.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图书管理操作结果类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookManagementResult {
    
    /**
     * 操作是否成功
     */
    private boolean success;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 图书ID（用于单个操作）
     */
    private Integer bookId;
    
    /**
     * 图书对象（用于单个操作）
     */
    private Book book;
    
    /**
     * 图书列表（用于批量操作）
     */
    private List<Book> books;
    
    /**
     * 操作数量（用于批量操作）
     */
    private Integer count;
    
    /**
     * 成功数量（用于批量操作）
     */
    private Integer successCount;
    
    /**
     * 失败数量（用于批量操作）
     */
    private Integer failCount;
    
    /**
     * 错误详情（用于批量操作）
     */
    private List<String> errors;
    
    /**
     * 创建成功结果
     * 
     * @param message 成功消息
     * @param book 图书对象
     * @return 成功结果
     */
    public static BookManagementResult success(String message, Book book) {
        return BookManagementResult.builder()
                .success(true)
                .message(message)
                .book(book)
                .bookId(book != null ? book.getBookId() : null)
                .build();
    }
    
    /**
     * 创建成功结果（带ID）
     * 
     * @param message 成功消息
     * @param bookId 图书ID
     * @return 成功结果
     */
    public static BookManagementResult success(String message, Integer bookId) {
        return BookManagementResult.builder()
                .success(true)
                .message(message)
                .bookId(bookId)
                .build();
    }
    
    /**
     * 创建成功结果（批量操作）
     * 
     * @param message 成功消息
     * @param books 图书列表
     * @return 成功结果
     */
    public static BookManagementResult success(String message, List<Book> books) {
        return BookManagementResult.builder()
                .success(true)
                .message(message)
                .books(books)
                .count(books != null ? books.size() : 0)
                .build();
    }
    
    /**
     * 创建成功结果（批量操作统计）
     * 
     * @param message 成功消息
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @return 成功结果
     */
    public static BookManagementResult success(String message, Integer successCount, Integer failCount) {
        return BookManagementResult.builder()
                .success(true)
                .message(message)
                .successCount(successCount)
                .failCount(failCount)
                .count(successCount + failCount)
                .build();
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 失败消息
     * @return 失败结果
     */
    public static BookManagementResult failure(String message) {
        return BookManagementResult.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * 创建失败结果（带错误详情）
     * 
     * @param message 失败消息
     * @param errors 错误详情列表
     * @return 失败结果
     */
    public static BookManagementResult failure(String message, List<String> errors) {
        return BookManagementResult.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .count(errors != null ? errors.size() : 0)
                .build();
    }
    
    /**
     * 创建失败结果（批量操作）
     * 
     * @param message 失败消息
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @param errors 错误详情列表
     * @return 失败结果
     */
    public static BookManagementResult failure(String message, Integer successCount, Integer failCount, List<String> errors) {
        return BookManagementResult.builder()
                .success(false)
                .message(message)
                .successCount(successCount)
                .failCount(failCount)
                .count(successCount + failCount)
                .errors(errors)
                .build();
    }
    
    /**
     * 创建成功结果（仅消息）
     * 
     * @param message 成功消息
     * @return 成功结果
     */
    public static BookManagementResult success(String message) {
        return BookManagementResult.builder()
                .success(true)
                .message(message)
                .build();
    }
    

}
