package com.vcampus.server.core.library.service;

import com.vcampus.server.core.library.entity.core.MyBookshelf;
import com.vcampus.server.core.library.entity.view.BookshelfView;

import java.util.List;

/**
 * 我的书架服务接口
 * 提供用户书架管理的核心业务功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface MyBookshelfService {
    
    // ==================== 书架管理 ====================
    
    /**
     * 添加图书到书架
     * @param cardNum 用户卡号
     * @param bookId 图书ID
     * @param categoryName 分类名称
     * @param notes 备注
     * @return 操作结果
     */
    boolean addBookToShelf(String cardNum, Integer bookId, String categoryName, String notes);
    
    /**
     * 从书架移除图书
     * @param cardNum 用户卡号
     * @param bookId 图书ID
     * @return 操作结果
     */
    boolean removeBookFromShelf(String cardNum, Integer bookId);
    
    /**
     * 更新书架记录分类
     * @param shelfId 书架ID
     * @param newCategoryName 新分类名称
     * @return 操作结果
     */
    boolean updateShelfCategory(Integer shelfId, String newCategoryName);
    
    /**
     * 更新书架记录备注
     * @param shelfId 书架ID
     * @param notes 备注
     * @return 操作结果
     */
    boolean updateShelfNotes(Integer shelfId, String notes);
    
    // ==================== 查询功能 ====================
    
    /**
     * 获取用户所有书架记录
     * @param cardNum 用户卡号
     * @return 书架记录列表
     */
    List<BookshelfView> getUserBookshelf(String cardNum);
    
    /**
     * 根据分类获取用户书架记录
     * @param cardNum 用户卡号
     * @param categoryName 分类名称
     * @return 书架记录列表
     */
    List<BookshelfView> getUserBookshelfByCategory(String cardNum, String categoryName);
    
    /**
     * 获取用户所有分类名称
     * @param cardNum 用户卡号
     * @return 分类名称列表
     */
    List<String> getUserCategoryNames(String cardNum);
    
    /**
     * 检查图书是否已在书架中
     * @param cardNum 用户卡号
     * @param bookId 图书ID
     * @return 是否在书架中
     */
    boolean isBookInShelf(String cardNum, Integer bookId);
    
    /**
     * 获取用户书架统计信息
     * @param cardNum 用户卡号
     * @return 统计信息Map
     */
    java.util.Map<String, Object> getBookshelfStatistics(String cardNum);
}
