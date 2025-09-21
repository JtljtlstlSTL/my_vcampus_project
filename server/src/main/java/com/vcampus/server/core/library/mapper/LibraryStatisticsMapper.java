package com.vcampus.server.core.library.mapper;

import com.vcampus.server.core.library.entity.search.UserStatistics;
import com.vcampus.server.core.library.entity.view.UserBorrowStatistics;
import com.vcampus.server.core.library.entity.view.CategoryStatistics;
import com.vcampus.server.core.library.entity.view.OverdueStatistics;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 图书馆统计信息数据访问接口 - 使用MyBatis
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface LibraryStatisticsMapper {
    
    // ==================== 存储过程调用方法 ====================
    
    /**
     * 获取用户统计信息 - 调用存储过程 sp_get_user_statistics
     */
    UserStatistics getUserStatistics(@Param("cardNum") String cardNum);
    
    // ==================== 视图查询方法 ====================
    
    /**
     * 查询用户借阅统计 - 使用视图 v_user_borrow_statistics
     */
    List<UserBorrowStatistics> getUserBorrowStatistics();
    
    /**
     * 查询分类统计 - 使用视图 v_category_statistics
     */
    List<CategoryStatistics> getCategoryStatistics();
    
    /**
     * 查询逾期统计 - 使用视图 v_overdue_statistics
     */
    List<OverdueStatistics> getOverdueStatistics();
}
