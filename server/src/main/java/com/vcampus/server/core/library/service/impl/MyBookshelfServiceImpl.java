package com.vcampus.server.core.library.service.impl;

import com.vcampus.server.core.library.dao.MyBookshelfDao;
import com.vcampus.server.core.library.entity.core.MyBookshelf;
import com.vcampus.server.core.library.entity.core.UserPersonalCategory;
import com.vcampus.server.core.library.entity.view.BookshelfView;
import com.vcampus.server.core.library.service.MyBookshelfService;
import com.vcampus.server.core.library.service.UserPersonalCategoryService;
import com.vcampus.server.core.library.service.impl.UserPersonalCategoryServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 我的书架服务实现类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class MyBookshelfServiceImpl implements MyBookshelfService {
    
    private final MyBookshelfDao myBookshelfDao;
    
    public MyBookshelfServiceImpl() {
        this.myBookshelfDao = MyBookshelfDao.getInstance();
    }
    
    // ==================== 书架管理 ====================
    
    @Override
    public boolean addBookToShelf(String cardNum, Integer bookId, String categoryName, String notes) {
        log.info("添加图书到书架: cardNum={}, bookId={}, categoryName={}", cardNum, bookId, categoryName);
        
        try {
            // 1. 检查图书是否已在书架中
            if (myBookshelfDao.isBookInBookshelf(cardNum, bookId)) {
                log.warn("图书已在书架中: cardNum={}, bookId={}", cardNum, bookId);
                return false;
            }
            
            // 2. 确保分类存在，如果不存在则自动创建
            String finalCategoryName = ensureCategoryExists(cardNum, categoryName);
            
            // 3. 创建书架记录
            MyBookshelf bookshelf = MyBookshelf.builder()
                    .cardNum(cardNum)
                    .bookId(bookId)
                    .categoryName(finalCategoryName)
                    .addTime(LocalDateTime.now())
                    .notes(notes)
                    .build();
            
            // 4. 保存到数据库
            myBookshelfDao.save(bookshelf);
            
            log.info("图书添加到书架成功: cardNum={}, bookId={}, shelfId={}, category={}", 
                    cardNum, bookId, bookshelf.getShelfId(), finalCategoryName);
            return true;
            
        } catch (Exception e) {
            log.error("添加图书到书架失败: cardNum={}, bookId={}", cardNum, bookId, e);
            return false;
        }
    }
    
    /**
     * 确保分类存在，如果不存在则自动创建
     * 
     * @param cardNum 用户卡号
     * @param categoryName 分类名称
     * @return 分类名称
     */
    private String ensureCategoryExists(String cardNum, String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "默认分类";
        }
        
        try {
            // 检查分类是否已存在
            UserPersonalCategoryService personalCategoryService = UserPersonalCategoryServiceImpl.getInstance();
            List<String> existingCategories = personalCategoryService.getCategoryNamesByCardNum(cardNum);
            
            if (!existingCategories.contains(categoryName)) {
                // 分类不存在，自动创建
                log.info("自动创建分类: cardNum={}, categoryName={}", cardNum, categoryName);
                
                // 生成颜色代码
                String colorCode = generateColorCode(categoryName);
                
                // 创建分类对象
                UserPersonalCategory category = UserPersonalCategory.builder()
                    .cardNum(cardNum)
                    .categoryName(categoryName)
                    .description("从书架自动创建的分类")
                    .colorCode(colorCode)
                    .sortOrder(999)
                    .build();
                
                // 创建分类
                UserPersonalCategory createdCategory = personalCategoryService.createCategory(category);
                
                if (createdCategory != null) {
                    log.info("分类创建成功: cardNum={}, categoryName={}, categoryId={}", 
                        cardNum, categoryName, createdCategory.getCategoryId());
                } else {
                    log.warn("分类创建失败: cardNum={}, categoryName={}", cardNum, categoryName);
                }
            }
            
            return categoryName;
            
        } catch (Exception e) {
            log.error("确保分类存在失败: cardNum={}, categoryName={}", cardNum, categoryName, e);
            return "默认分类";
        }
    }
    
    /**
     * 根据分类名称生成颜色代码
     * 
     * @param categoryName 分类名称
     * @return 颜色代码
     */
    private String generateColorCode(String categoryName) {
        // 预定义的颜色列表
        String[] colors = {
            "#e74c3c", "#3498db", "#2ecc71", "#f39c12", "#9b59b6",
            "#1abc9c", "#34495e", "#e67e22", "#95a5a6", "#f1c40f"
        };
        
        // 根据分类名称的哈希值选择颜色
        int hash = Math.abs(categoryName.hashCode());
        return colors[hash % colors.length];
    }
    
    @Override
    public boolean removeBookFromShelf(String cardNum, Integer bookId) {
        log.info("从书架移除图书: cardNum={}, bookId={}", cardNum, bookId);
        
        try {
            myBookshelfDao.deleteByCardNumAndBookId(cardNum, bookId);
            log.info("图书从书架移除成功: cardNum={}, bookId={}", cardNum, bookId);
            return true;
            
        } catch (Exception e) {
            log.error("从书架移除图书失败: cardNum={}, bookId={}", cardNum, bookId, e);
            return false;
        }
    }
    
    @Override
    public boolean updateShelfCategory(Integer shelfId, String newCategoryName) {
        log.info("更新书架分类: shelfId={}, newCategoryName={}", shelfId, newCategoryName);
        
        try {
            myBookshelfDao.updateCategory(shelfId, newCategoryName);
            log.info("书架分类更新成功: shelfId={}, newCategoryName={}", shelfId, newCategoryName);
            return true;
            
        } catch (Exception e) {
            log.error("更新书架分类失败: shelfId={}, newCategoryName={}", shelfId, newCategoryName, e);
            return false;
        }
    }
    
    @Override
    public boolean updateShelfNotes(Integer shelfId, String notes) {
        log.info("更新书架备注: shelfId={}, notes={}", shelfId, notes);
        
        try {
            myBookshelfDao.updateNotes(shelfId, notes);
            log.info("书架备注更新成功: shelfId={}, notes={}", shelfId, notes);
            return true;
            
        } catch (Exception e) {
            log.error("更新书架备注失败: shelfId={}, notes={}", shelfId, notes, e);
            return false;
        }
    }
    
    // ==================== 查询功能 ====================
    
    @Override
    public List<BookshelfView> getUserBookshelf(String cardNum) {
        log.info("获取用户书架: cardNum={}", cardNum);
        
        try {
            List<BookshelfView> bookshelf = myBookshelfDao.getUserBookshelfView(cardNum);
            log.info("获取用户书架成功: cardNum={}, count={}", cardNum, bookshelf.size());
            return bookshelf;
            
        } catch (Exception e) {
            log.error("获取用户书架失败: cardNum={}", cardNum, e);
            return List.of();
        }
    }
    
    @Override
    public List<BookshelfView> getUserBookshelfByCategory(String cardNum, String categoryName) {
        log.info("根据分类获取用户书架: cardNum={}, categoryName={}", cardNum, categoryName);
        
        try {
            List<BookshelfView> bookshelf = myBookshelfDao.getUserBookshelfViewByCategory(cardNum, categoryName);
            log.info("根据分类获取用户书架成功: cardNum={}, categoryName={}, count={}", 
                    cardNum, categoryName, bookshelf.size());
            return bookshelf;
            
        } catch (Exception e) {
            log.error("根据分类获取用户书架失败: cardNum={}, categoryName={}", cardNum, categoryName, e);
            return List.of();
        }
    }
    
    @Override
    public List<String> getUserCategoryNames(String cardNum) {
        log.info("获取用户分类名称: cardNum={}", cardNum);
        
        try {
            List<String> categories = myBookshelfDao.getUserCategoryNames(cardNum);
            log.info("获取用户分类名称成功: cardNum={}, count={}", cardNum, categories.size());
            return categories;
            
        } catch (Exception e) {
            log.error("获取用户分类名称失败: cardNum={}", cardNum, e);
            return List.of();
        }
    }
    
    @Override
    public boolean isBookInShelf(String cardNum, Integer bookId) {
        log.info("检查图书是否在书架中: cardNum={}, bookId={}", cardNum, bookId);
        
        try {
            boolean inShelf = myBookshelfDao.isBookInBookshelf(cardNum, bookId);
            log.info("检查图书是否在书架中结果: cardNum={}, bookId={}, inShelf={}", 
                    cardNum, bookId, inShelf);
            return inShelf;
            
        } catch (Exception e) {
            log.error("检查图书是否在书架中失败: cardNum={}, bookId={}", cardNum, bookId, e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getBookshelfStatistics(String cardNum) {
        log.info("获取用户书架统计信息: cardNum={}", cardNum);
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总收藏数
            long totalCount = myBookshelfDao.countByCardNum(cardNum);
            statistics.put("totalCount", totalCount);
            
            // 分类统计
            List<String> categories = myBookshelfDao.getUserCategoryNames(cardNum);
            statistics.put("categoryCount", categories.size());
            statistics.put("categories", categories);
            
            // 各分类图书数量
            Map<String, Long> categoryStats = new HashMap<>();
            for (String category : categories) {
                long count = myBookshelfDao.countByCardNumAndCategory(cardNum, category);
                categoryStats.put(category, count);
            }
            statistics.put("categoryStats", categoryStats);
            
            log.info("获取用户书架统计信息成功: cardNum={}, totalCount={}, categoryCount={}", 
                    cardNum, totalCount, categories.size());
            return statistics;
            
        } catch (Exception e) {
            log.error("获取用户书架统计信息失败: cardNum={}", cardNum, e);
            return Map.of();
        }
    }
}
