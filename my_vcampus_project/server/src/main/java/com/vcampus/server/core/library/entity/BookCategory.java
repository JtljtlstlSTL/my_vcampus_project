package com.vcampus.server.core.library.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书分类实体类 - 对应数据库表 tblBookCategory，基于中图法（中国图书馆分类法）标准
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCategory {
    private Integer categoryId; // 分类ID - 主键，自增
    private String categoryCode; // 分类代码 - 唯一标识（A-Z）
    private String categoryName; // 分类名称
    private String description; // 分类描述
    private Integer sortOrder; // 排序顺序
    
    // 静态工厂方法，创建分类
    public static BookCategory create(String categoryCode, String categoryName, String description) {
        return BookCategory.builder()
                .categoryCode(categoryCode)
                .categoryName(categoryName)
                .description(description)
                .sortOrder(0)
                .build();
    }
    
    public static BookCategory create(String categoryCode, String categoryName, String description, Integer sortOrder) {
        return BookCategory.builder()
                .categoryCode(categoryCode)
                .categoryName(categoryName)
                .description(description)
                .sortOrder(sortOrder)
                .build();
    }
    
    // 业务方法
    public boolean isValidCategoryCode() { // 判断分类代码是否有效，中图法基本大类为A-Z
        if (categoryCode == null || categoryCode.length() != 1) {
            return false;
        }
        char code = categoryCode.toUpperCase().charAt(0);
        return code >= 'A' && code <= 'Z';
    }
    
    public int getCategoryCodeValue() { // 获取分类代码的ASCII值，用于排序
        if (categoryCode == null || categoryCode.isEmpty()) {
            return 0;
        }
        return categoryCode.toUpperCase().charAt(0);
    }
    
    public boolean isBasicCategory() { // 判断是否为基本大类，中图法基本大类为A-Z，共22个
        if (!isValidCategoryCode()) {
            return false;
        }
        char code = categoryCode.toUpperCase().charAt(0);
        // 中图法基本大类：A-Z，但排除了一些不常用的字母
        return (code >= 'A' && code <= 'Z') && 
               code != 'L' && code != 'M' && code != 'W' && code != 'Y';
    }
    
    public String getFullDisplayName() { // 获取分类的完整显示名称
        if (categoryCode == null || categoryName == null) {
            return "";
        }
        return categoryCode + " - " + categoryName;
    }
    
    public boolean isValid() { // 验证分类数据的完整性
        return categoryCode != null && !categoryCode.trim().isEmpty() &&
               categoryName != null && !categoryName.trim().isEmpty() &&
               isValidCategoryCode();
    }
}
