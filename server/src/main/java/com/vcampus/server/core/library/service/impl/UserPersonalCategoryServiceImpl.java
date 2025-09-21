package com.vcampus.server.core.library.service.impl;

import com.vcampus.server.core.library.dao.UserPersonalCategoryDao;
import com.vcampus.server.core.library.entity.core.UserPersonalCategory;
import com.vcampus.server.core.library.service.UserPersonalCategoryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * 用户个人分类服务实现类
 * 提供用户个人分类的业务逻辑实现
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class UserPersonalCategoryServiceImpl implements UserPersonalCategoryService {
    
    private static UserPersonalCategoryServiceImpl instance;
    private final UserPersonalCategoryDao categoryDao;
    
    private UserPersonalCategoryServiceImpl() {
        this.categoryDao = UserPersonalCategoryDao.getInstance();
    }
    
    public static synchronized UserPersonalCategoryServiceImpl getInstance() {
        if (instance == null) {
            instance = new UserPersonalCategoryServiceImpl();
        }
        return instance;
    }
    
    @Override
    public UserPersonalCategory createCategory(UserPersonalCategory category) {
        if (category.getCardNum() == null || category.getCardNum().trim().isEmpty()) {
            throw new IllegalArgumentException("用户卡号不能为空");
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        // 检查分类是否已存在
        if (existsByCardNumAndName(category.getCardNum(), category.getCategoryName())) {
            throw new IllegalArgumentException("分类名称已存在: " + category.getCategoryName());
        }
        
        return categoryDao.insert(category);
    }
    
    @Override
    public UserPersonalCategory updateCategory(UserPersonalCategory category) {
        if (category.getCategoryId() == null || !categoryDao.findById(category.getCategoryId()).isPresent()) {
            throw new RuntimeException("分类不存在: " + category.getCategoryId());
        }
        return categoryDao.update(category);
    }
    
    @Override
    public boolean deleteCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        return categoryDao.deleteById(categoryId);
    }
    
    @Override
    public boolean deleteCategory(String cardNum, String categoryName) {
        if (cardNum == null || cardNum.trim().isEmpty()) {
            throw new IllegalArgumentException("用户卡号不能为空");
        }
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        return categoryDao.deleteByCardNumAndName(cardNum, categoryName);
    }
    
    @Override
    public Optional<UserPersonalCategory> getById(Integer categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        return categoryDao.findById(categoryId);
    }
    
    @Override
    public Optional<UserPersonalCategory> getByCardNumAndName(String cardNum, String categoryName) {
        if (cardNum == null || cardNum.trim().isEmpty()) {
            throw new IllegalArgumentException("用户卡号不能为空");
        }
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        return categoryDao.findByCardNumAndName(cardNum, categoryName);
    }
    
    @Override
    public List<UserPersonalCategory> getByCardNum(String cardNum) {
        if (cardNum == null || cardNum.trim().isEmpty()) {
            throw new IllegalArgumentException("用户卡号不能为空");
        }
        return categoryDao.findByCardNum(cardNum);
    }
    
    @Override
    public List<String> getCategoryNamesByCardNum(String cardNum) {
        if (cardNum == null || cardNum.trim().isEmpty()) {
            throw new IllegalArgumentException("用户卡号不能为空");
        }
        return categoryDao.getCategoryNamesByCardNum(cardNum);
    }
    
    @Override
    public boolean existsByCardNumAndName(String cardNum, String categoryName) {
        if (cardNum == null || cardNum.trim().isEmpty()) {
            throw new IllegalArgumentException("用户卡号不能为空");
        }
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        return categoryDao.existsByCardNumAndName(cardNum, categoryName);
    }
    
    @Override
    public long countByCardNum(String cardNum) {
        if (cardNum == null || cardNum.trim().isEmpty()) {
            throw new IllegalArgumentException("用户卡号不能为空");
        }
        return categoryDao.countByCardNum(cardNum);
    }
    
    @Override
    public boolean updateSortOrder(Integer categoryId, Integer sortOrder) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (sortOrder == null) {
            throw new IllegalArgumentException("排序顺序不能为空");
        }
        return categoryDao.updateSortOrder(categoryId, sortOrder);
    }
}
