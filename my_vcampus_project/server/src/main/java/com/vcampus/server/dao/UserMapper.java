package com.vcampus.server.dao;

import com.vcampus.common.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口 - 使用MyBatis
 * 
 * @author VCampus Team
 * @version 4.0
 */
public interface UserMapper {
    
    /**
     * 根据卡号查询用户
     */
    User findById(@Param("cardNum") String cardNum);
    
    /**
     * 查询所有用户
     */
    List<User> findAll();
    
    /**
     * 根据用户类型查询
     */
    List<User> findByUserType(@Param("userType") String userType);
    
    /**
     * 根据手机号查询
     */
    User findByPhone(@Param("phone") String phone);
    
    /**
     * 根据姓名模糊查询
     */
    List<User> findByNameLike(@Param("name") String name);
    
    /**
     * 插入用户
     */
    int insert(User user);
    
    /**
     * 更新用户
     */
    int update(User user);
    
    /**
     * 删除用户
     */
    int deleteById(@Param("cardNum") String cardNum);
    
    /**
     * 检查用户是否存在
     */
    boolean existsById(@Param("cardNum") String cardNum);
    
    /**
     * 统计用户数量
     */
    long count();
    
    /**
     * 用户认证
     */
    default Optional<User> authenticate(String cardNum, String password) {
        User user = findById(cardNum);
        if (user != null && password.equals(user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
