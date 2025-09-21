package com.vcampus.server.core.library.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.server.core.library.entity.core.UserPersonalCategory;
import com.vcampus.server.core.library.service.UserPersonalCategoryService;
import com.vcampus.server.core.library.service.impl.UserPersonalCategoryServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 用户个人分类控制器
 * 提供用户个人分类管理的API接口
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class UserPersonalCategoryController {
    
    private final UserPersonalCategoryService categoryService;
    
    public UserPersonalCategoryController() {
        this.categoryService = UserPersonalCategoryServiceImpl.getInstance();
    }
    
    // ==================== 分类管理 ====================
    
    /**
     * 创建个人分类
     * URI: library/personal-category/create
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/personal-category/create", role = "student", description = "创建个人分类")
    public Response createCategory(Request request) {
        log.info("处理创建个人分类请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String cardNum = request.getSession().getUserId();
            String categoryName = request.getParam("categoryName");
            String description = request.getParam("description");
            String colorCode = request.getParam("colorCode");
            String sortOrderStr = request.getParam("sortOrder");
            
            if (categoryName == null || categoryName.trim().isEmpty()) {
                return Response.Builder.badRequest("分类名称不能为空");
            }
            
            // 3. 创建分类对象
            UserPersonalCategory category = UserPersonalCategory.create(cardNum, categoryName.trim());
            if (description != null && !description.trim().isEmpty()) {
                category.setDescription(description.trim());
            }
            if (colorCode != null && !colorCode.trim().isEmpty()) {
                category.setColorCode(colorCode.trim());
            }
            if (sortOrderStr != null && !sortOrderStr.trim().isEmpty()) {
                try {
                    category.setSortOrder(Integer.valueOf(sortOrderStr.trim()));
                } catch (NumberFormatException e) {
                    return Response.Builder.badRequest("排序顺序格式错误");
                }
            }
            
            // 4. 调用服务层
            UserPersonalCategory createdCategory = categoryService.createCategory(category);
            
            if (createdCategory != null) {
                log.info("创建个人分类成功: cardNum={}, categoryName={}", cardNum, categoryName);
                return Response.Builder.success("创建成功", createdCategory);
            } else {
                log.warn("创建个人分类失败: cardNum={}, categoryName={}", cardNum, categoryName);
                return Response.Builder.error("创建失败");
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("创建个人分类参数错误: {}", e.getMessage());
            return Response.Builder.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("处理创建个人分类请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 更新个人分类
     * URI: library/personal-category/update
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/personal-category/update", role = "student", description = "更新个人分类")
    public Response updateCategory(Request request) {
        log.info("处理更新个人分类请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String categoryIdStr = request.getParam("categoryId");
            String categoryName = request.getParam("categoryName");
            String description = request.getParam("description");
            String colorCode = request.getParam("colorCode");
            String sortOrderStr = request.getParam("sortOrder");
            
            if (categoryIdStr == null || categoryIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("分类ID不能为空");
            }
            
            Integer categoryId;
            try {
                categoryId = Integer.valueOf(categoryIdStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("分类ID格式错误");
            }
            
            // 3. 获取现有分类
            UserPersonalCategory existingCategory = categoryService.getById(categoryId).orElse(null);
            if (existingCategory == null) {
                return Response.Builder.badRequest("分类不存在");
            }
            
            // 4. 更新分类信息
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                existingCategory.setCategoryName(categoryName.trim());
            }
            if (description != null) {
                existingCategory.setDescription(description.trim());
            }
            if (colorCode != null && !colorCode.trim().isEmpty()) {
                existingCategory.setColorCode(colorCode.trim());
            }
            if (sortOrderStr != null && !sortOrderStr.trim().isEmpty()) {
                try {
                    existingCategory.setSortOrder(Integer.valueOf(sortOrderStr.trim()));
                } catch (NumberFormatException e) {
                    return Response.Builder.badRequest("排序顺序格式错误");
                }
            }
            
            // 5. 调用服务层
            UserPersonalCategory updatedCategory = categoryService.updateCategory(existingCategory);
            
            if (updatedCategory != null) {
                log.info("更新个人分类成功: categoryId={}", categoryId);
                return Response.Builder.success("更新成功", updatedCategory);
            } else {
                log.warn("更新个人分类失败: categoryId={}", categoryId);
                return Response.Builder.error("更新失败");
            }
            
        } catch (Exception e) {
            log.error("处理更新个人分类请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 删除个人分类
     * URI: library/personal-category/delete
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/personal-category/delete", role = "student", description = "删除个人分类")
    public Response deleteCategory(Request request) {
        log.info("处理删除个人分类请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String categoryIdStr = request.getParam("categoryId");
            
            if (categoryIdStr == null || categoryIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("分类ID不能为空");
            }
            
            Integer categoryId;
            try {
                categoryId = Integer.valueOf(categoryIdStr);
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("分类ID格式错误");
            }
            
            // 3. 调用服务层
            boolean success = categoryService.deleteCategory(categoryId);
            
            if (success) {
                log.info("删除个人分类成功: categoryId={}", categoryId);
                return Response.Builder.success("删除成功");
            } else {
                log.warn("删除个人分类失败: categoryId={}", categoryId);
                return Response.Builder.error("删除失败");
            }
            
        } catch (Exception e) {
            log.error("处理删除个人分类请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 查询功能 ====================
    
    /**
     * 获取用户个人分类列表
     * URI: library/personal-category/list
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/personal-category/list", role = "student", description = "获取用户个人分类列表")
    public Response getUserCategories(Request request) {
        log.info("处理获取用户个人分类列表请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String cardNum = request.getSession().getUserId();
            
            // 3. 调用服务层
            List<UserPersonalCategory> categories = categoryService.getByCardNum(cardNum);
            
            log.info("获取用户个人分类列表成功: cardNum={}, count={}", cardNum, categories.size());
            return Response.Builder.success("查询成功", categories);
            
        } catch (Exception e) {
            log.error("处理获取用户个人分类列表请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户个人分类名称列表
     * URI: library/personal-category/names
     * 权限: student, teacher
     */
    @RouteMapping(uri = "library/personal-category/names", role = "student", description = "获取用户个人分类名称列表")
    public Response getUserCategoryNames(Request request) {
        log.info("处理获取用户个人分类名称列表请求: {}", request.getUri());
        
        try {
            // 1. 权限验证
            if (request.getSession() == null || 
                (!request.getSession().hasPermission("student") && !request.getSession().hasPermission("teacher"))) {
                return Response.Builder.forbidden("需要学生或教师权限");
            }
            
            // 2. 参数提取
            String cardNum = request.getSession().getUserId();
            
            // 3. 调用服务层
            List<String> categoryNames = categoryService.getCategoryNamesByCardNum(cardNum);
            
            log.info("获取用户个人分类名称列表成功: cardNum={}, count={}", cardNum, categoryNames.size());
            return Response.Builder.success("查询成功", categoryNames);
            
        } catch (Exception e) {
            log.error("处理获取用户个人分类名称列表请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
}
