package com.vcampus.server.core.library.entity.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户个人分类实体类
 * 用于存储用户创建的个人图书分类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPersonalCategory {
    
    /**
     * 个人分类ID
     */
    private Integer categoryId;
    
    /**
     * 用户卡号
     */
    private String cardNum;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 分类颜色代码
     */
    private String colorCode;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 检查分类名称是否有效
     */
    public boolean hasValidName() {
        return categoryName != null && !categoryName.trim().isEmpty();
    }
    
    /**
     * 获取分类名称（如果为空则返回默认值）
     */
    public String getCategoryNameOrDefault() {
        return hasValidName() ? categoryName : "未命名分类";
    }
    
    /**
     * 检查是否有描述
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }
    
    /**
     * 获取描述内容（如果为空则返回默认值）
     */
    public String getDescriptionOrDefault() {
        return hasDescription() ? description : "暂无描述";
    }
    
    /**
     * 获取颜色代码（如果为空则返回默认值）
     */
    public String getColorCodeOrDefault() {
        return colorCode != null && !colorCode.trim().isEmpty() ? colorCode : "#3498db";
    }
    
    /**
     * 获取排序顺序（如果为空则返回默认值）
     */
    public Integer getSortOrderOrDefault() {
        return sortOrder != null ? sortOrder : 0;
    }
    
    /**
     * 静态工厂方法，创建分类
     */
    public static UserPersonalCategory create(String cardNum, String categoryName) {
        UserPersonalCategory category = new UserPersonalCategory();
        category.setCardNum(cardNum);
        category.setCategoryName(categoryName);
        category.setColorCode("#3498db");
        category.setSortOrder(0);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return category;
    }
    
    /**
     * 静态工厂方法，创建分类（带描述）
     */
    public static UserPersonalCategory create(String cardNum, String categoryName, String description) {
        UserPersonalCategory category = new UserPersonalCategory();
        category.setCardNum(cardNum);
        category.setCategoryName(categoryName);
        category.setDescription(description);
        category.setColorCode("#3498db");
        category.setSortOrder(0);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return category;
    }
    
    /**
     * 静态工厂方法，创建分类（完整参数）
     */
    public static UserPersonalCategory create(String cardNum, String categoryName, String description, 
                                            String colorCode, Integer sortOrder) {
        UserPersonalCategory category = new UserPersonalCategory();
        category.setCardNum(cardNum);
        category.setCategoryName(categoryName);
        category.setDescription(description);
        category.setColorCode(colorCode);
        category.setSortOrder(sortOrder);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return category;
    }
}
