package com.vcampus.server.core.library.mapper;

import com.vcampus.server.core.library.entity.core.UserPersonalCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户个人分类Mapper接口
 * 提供用户个人分类的数据库操作
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Mapper
public interface UserPersonalCategoryMapper {
    
    // ==================== 基础CRUD方法 ====================
    
    /**
     * 根据ID查找分类
     */
    @Select("SELECT * FROM tblUserPersonalCategory WHERE category_id = #{categoryId}")
    UserPersonalCategory findById(@Param("categoryId") Integer categoryId);
    
    /**
     * 查找所有分类
     */
    @Select("SELECT * FROM tblUserPersonalCategory ORDER BY sort_order ASC, create_time DESC")
    List<UserPersonalCategory> findAll();
    
    /**
     * 根据用户卡号和分类名称查找分类
     */
    @Select("SELECT * FROM tblUserPersonalCategory WHERE cardNum = #{cardNum} AND category_name = #{categoryName}")
    UserPersonalCategory findByCardNumAndCategoryName(@Param("cardNum") String cardNum, 
                                                    @Param("categoryName") String categoryName);
    
    /**
     * 根据用户卡号查找所有分类
     */
    @Select("SELECT * FROM tblUserPersonalCategory WHERE cardNum = #{cardNum} ORDER BY sort_order ASC, create_time ASC")
    List<UserPersonalCategory> findByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 插入分类
     */
    @Insert("INSERT INTO tblUserPersonalCategory (cardNum, category_name, description, color_code, sort_order, create_time, update_time) " +
            "VALUES (#{cardNum}, #{categoryName}, #{description}, #{colorCode}, #{sortOrder}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "categoryId")
    int insert(UserPersonalCategory category);
    
    /**
     * 更新分类
     */
    @Update("UPDATE tblUserPersonalCategory SET category_name = #{categoryName}, description = #{description}, " +
            "color_code = #{colorCode}, sort_order = #{sortOrder}, update_time = #{updateTime} " +
            "WHERE category_id = #{categoryId}")
    int update(UserPersonalCategory category);
    
    /**
     * 删除分类
     */
    @Delete("DELETE FROM tblUserPersonalCategory WHERE category_id = #{categoryId}")
    int deleteById(@Param("categoryId") Integer categoryId);
    
    /**
     * 根据用户卡号和分类名称删除分类
     */
    @Delete("DELETE FROM tblUserPersonalCategory WHERE cardNum = #{cardNum} AND category_name = #{categoryName}")
    int deleteByCardNumAndCategoryName(@Param("cardNum") String cardNum, 
                                      @Param("categoryName") String categoryName);
    
    // ==================== 业务查询方法 ====================
    
    /**
     * 检查分类是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM tblUserPersonalCategory WHERE cardNum = #{cardNum} AND category_name = #{categoryName}")
    boolean existsByCardNumAndName(@Param("cardNum") String cardNum, 
                                  @Param("categoryName") String categoryName);
    
    /**
     * 统计用户分类数量
     */
    @Select("SELECT COUNT(*) FROM tblUserPersonalCategory WHERE cardNum = #{cardNum}")
    long countByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 统计所有分类数量
     */
    @Select("SELECT COUNT(*) FROM tblUserPersonalCategory")
    long count();
    
    /**
     * 获取用户所有分类名称
     */
    @Select("SELECT category_name FROM tblUserPersonalCategory WHERE cardNum = #{cardNum} ORDER BY sort_order ASC, create_time ASC")
    List<String> getCategoryNamesByCardNum(@Param("cardNum") String cardNum);
    
    /**
     * 更新分类排序
     */
    @Update("UPDATE tblUserPersonalCategory SET sort_order = #{sortOrder}, update_time = #{updateTime} WHERE category_id = #{categoryId}")
    int updateSortOrder(@Param("categoryId") Integer categoryId, 
                       @Param("sortOrder") Integer sortOrder, 
                       @Param("updateTime") java.time.LocalDateTime updateTime);
}
