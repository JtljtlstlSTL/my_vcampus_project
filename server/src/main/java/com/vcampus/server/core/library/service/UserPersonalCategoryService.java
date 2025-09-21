package com.vcampus.server.core.library.service;

import com.vcampus.server.core.library.entity.core.UserPersonalCategory;

import java.util.List;
import java.util.Optional;

/**
 * 用户个人分类服务接口
 * 提供用户个人分类的业务逻辑
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface UserPersonalCategoryService {
    
    /**
     * 创建分类
     */
    UserPersonalCategory createCategory(UserPersonalCategory category);
    
    /**
     * 更新分类
     */
    UserPersonalCategory updateCategory(UserPersonalCategory category);
    
    /**
     * 删除分类
     */
    boolean deleteCategory(Integer categoryId);
    
    /**
     * 根据用户卡号和分类名称删除分类
     */
    boolean deleteCategory(String cardNum, String categoryName);
    
    /**
     * 根据ID查找分类
     */
    Optional<UserPersonalCategory> getById(Integer categoryId);
    
    /**
     * 根据用户卡号和分类名称查找分类
     */
    Optional<UserPersonalCategory> getByCardNumAndName(String cardNum, String categoryName);
    
    /**
     * 根据用户卡号查找所有分类
     */
    List<UserPersonalCategory> getByCardNum(String cardNum);
    
    /**
     * 获取用户所有分类名称
     */
    List<String> getCategoryNamesByCardNum(String cardNum);
    
    /**
     * 检查分类是否存在
     */
    boolean existsByCardNumAndName(String cardNum, String categoryName);
    
    /**
     * 统计用户分类数量
     */
    long countByCardNum(String cardNum);
    
    /**
     * 更新分类排序
     */
    boolean updateSortOrder(Integer categoryId, Integer sortOrder);
}
