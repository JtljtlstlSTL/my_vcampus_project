package com.vcampus.server.core.library.mapper;

import com.vcampus.server.core.library.entity.core.BookCategory;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 图书分类数据访问接口 - 使用MyBatis
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface BookCategoryMapper {
    
    /**
     * 根据ID查找分类
     */
    BookCategory findById(@Param("categoryId") Integer categoryId);
    
    /**
     * 查找所有分类
     */
    List<BookCategory> findAll();
    
    /**
     * 根据分类代码查找分类
     */
    BookCategory findByCategoryCode(@Param("categoryCode") String categoryCode);
    
    /**
     * 根据分类名称查找分类
     */
    BookCategory findByCategoryName(@Param("categoryName") String categoryName);
    
    /**
     * 根据分类名称模糊查询
     */
    List<BookCategory> findByCategoryNameLike(@Param("categoryName") String categoryName);
    
    /**
     * 根据描述模糊查询
     */
    List<BookCategory> findByDescriptionLike(@Param("description") String description);
    
    /**
     * 按排序顺序查询所有分类
     */
    List<BookCategory> findAllOrderBySort();
    
    /**
     * 按分类代码排序查询所有分类
     */
    List<BookCategory> findAllOrderByCode();
    
    /**
     * 查询基本大类（A-Z）
     */
    List<BookCategory> findBasicCategories();
    
    /**
     * 插入分类
     */
    int insert(BookCategory category);
    
    /**
     * 更新分类
     */
    int update(BookCategory category);
    
    /**
     * 根据ID删除分类
     */
    int deleteById(@Param("categoryId") Integer categoryId);
    
    /**
     * 检查分类是否存在
     */
    boolean existsById(@Param("categoryId") Integer categoryId);
    
    /**
     * 统计分类数量
     */
    long count();
    
    /**
     * 检查分类代码是否已存在
     */
    boolean isCategoryCodeExists(@Param("categoryCode") String categoryCode);
    
    /**
     * 检查分类名称是否已存在
     */
    boolean isCategoryNameExists(@Param("categoryName") String categoryName);
    
    /**
     * 获取下一个排序顺序
     */
    int getNextSortOrder();
    
    /**
     * 更新分类排序顺序
     */
    int updateSortOrder(@Param("categoryId") Integer categoryId, @Param("sortOrder") int sortOrder);
}
