package com.vcampus.server.core.library.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.vcampus.server.core.library.entity.core.MyBookshelf;
import com.vcampus.server.core.library.entity.view.BookshelfView;

/**
 * 我的书架数据访问接口 - 使用MyBatis
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface MyBookshelfMapper {
    
    // ==================== 基础CRUD操作 ====================
    
    /**
     * 根据ID查找书架记录
     */
    MyBookshelf findById(@Param("shelfId") Integer shelfId);
    
    /**
     * 查找所有书架记录
     */
    List<MyBookshelf> findAll();
    
    /**
     * 根据用户卡号查找书架记录
     */
    List<MyBookshelf> findByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 根据用户卡号和分类查找书架记录
     */
    List<MyBookshelf> findByCardNumAndCategory(@Param("cardNum") String cardNum, 
                                              @Param("categoryName") String categoryName);
    
    /**
     * 根据用户卡号和图书ID查找书架记录
     */
    MyBookshelf findByCardNumAndBookId(@Param("cardNum") String cardNum, 
                                      @Param("bookId") Integer bookId);
    
    /**
     * 根据图书ID查找书架记录
     */
    List<MyBookshelf> findByBookId(@Param("bookId") Integer bookId);
    
    /**
     * 根据分类名称查找书架记录
     */
    List<MyBookshelf> findByCategoryName(@Param("categoryName") String categoryName);
    
    /**
     * 插入书架记录
     */
    int insert(MyBookshelf bookshelf);
    
    /**
     * 更新书架记录
     */
    int update(MyBookshelf bookshelf);
    
    /**
     * 根据ID删除书架记录
     */
    int deleteById(@Param("shelfId") Integer shelfId);
    
    /**
     * 根据用户卡号和图书ID删除书架记录
     */
    int deleteByCardNumAndBookId(@Param("cardNum") String cardNum, 
                                @Param("bookId") Integer bookId);
    
    /**
     * 检查书架记录是否存在
     */
    boolean existsById(@Param("shelfId") Integer shelfId);
    
    /**
     * 检查用户是否已收藏某本图书
     */
    boolean isBookInBookshelf(@Param("cardNum") String cardNum, 
                             @Param("bookId") Integer bookId);
    
    /**
     * 统计书架记录数量
     */
    long count();
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 统计用户书架数量
     */
    long countByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 统计用户指定分类的书架数量
     */
    long countByCardNumAndCategory(@Param("cardNum") String cardNum, 
                                  @Param("categoryName") String categoryName);
    
    /**
     * 更新书架记录分类
     */
    int updateCategory(@Param("shelfId") Integer shelfId, 
                      @Param("categoryName") String categoryName);
    
    /**
     * 更新书架记录备注
     */
    int updateNotes(@Param("shelfId") Integer shelfId, 
                   @Param("notes") String notes);
    
    // ==================== 视图查询方法 ====================
    
    /**
     * 查询用户书架视图 - 使用视图
     */
    List<BookshelfView> getUserBookshelfView(@Param("cardNum") String cardNum);
    
    /**
     * 根据分类查询用户书架视图 - 使用视图
     */
    List<BookshelfView> getUserBookshelfViewByCategory(@Param("cardNum") String cardNum, 
                                                      @Param("categoryName") String categoryName);
    
    /**
     * 获取用户所有分类名称
     */
    List<String> getUserCategoryNames(@Param("cardNum") String cardNum);
}
