package com.vcampus.server.core.auth.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.vcampus.common.entity.base.User;

/**
 * 用户数据访问接口 - MyBatis Mapper
 */
@Mapper
public interface UserMapper {

    /**
     * 根据卡号查找用户
     */
    @Select("SELECT * FROM tblUser WHERE cardNum = #{cardNum}")
    User findByCardNum(@Param("cardNum") String cardNum);

    /**
     * 根据卡号查找用户（返回Map格式）
     */
    @Select("SELECT * FROM tblUser WHERE cardNum = #{cardNum}")
    Map<String, Object> findByCardNumAsMap(@Param("cardNum") String cardNum);

    /**
     * 获取所有用户
     */
    @Select("SELECT * FROM tblUser")
    List<User> getAllUsers();

    /**
     * 获取所有用户（返回Map格式）
     */
    @Select("SELECT * FROM tblUser")
    List<Map<String, Object>> getAllUsersAsMap();

    /**
     * 插入用户信息
     */
    @Insert("INSERT INTO tblUser (cardNum, cardNumPassword, Name, Age, Gender, Phone, userType) " +
            "VALUES (#{cardNum}, #{password}, #{name}, #{age}, #{gender}, #{phone}, #{userType})")
    int insertUser(@Param("cardNum") String cardNum,
                  @Param("password") String password,
                  @Param("name") String name,
                  @Param("age") Integer age,
                  @Param("gender") String gender,
                  @Param("phone") String phone,
                  @Param("userType") String userType);

    /**
     * 更新用户信息
     */
    @Update("UPDATE tblUser SET Name = #{name}, Age = #{age}, Gender = #{gender}, " +
            "Phone = #{phone}, userType = #{userType} WHERE cardNum = #{cardNum}")
    int updateUser(@Param("cardNum") String cardNum,
                  @Param("name") String name,
                  @Param("age") Integer age,
                  @Param("gender") String gender,
                  @Param("phone") String phone,
                  @Param("userType") String userType);

    /**
     * 更新用户密码
     */
    @Update("UPDATE tblUser SET cardNumPassword = #{password} WHERE cardNum = #{cardNum}")
    int updatePassword(@Param("cardNum") String cardNum, @Param("password") String password);

    /**
     * 删除用户
     */
    @Delete("DELETE FROM tblUser WHERE cardNum = #{cardNum}")
    int deleteByCardNum(@Param("cardNum") String cardNum);

    /**
     * 检查卡号是否已存在
     */
    @Select("SELECT COUNT(*) FROM tblUser WHERE cardNum = #{cardNum}")
    int isCardNumExists(@Param("cardNum") String cardNum);

    /**
     * 根据用户类型查找用户
     */
    @Select("SELECT * FROM tblUser WHERE userType = #{userType}")
    List<User> findByUserType(@Param("userType") String userType);

    /**
     * 根据用户类型查找用户（返回Map格式）
     */
    @Select("SELECT * FROM tblUser WHERE userType = #{userType}")
    List<Map<String, Object>> findByUserTypeAsMap(@Param("userType") String userType);

    /**
     * 根据姓名模糊查询用户
     */
    @Select("SELECT * FROM tblUser WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<User> findByNameLike(@Param("name") String name);

    /**
     * 根据姓名模糊查询用户（返回Map格式）
     */
    @Select("SELECT * FROM tblUser WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Map<String, Object>> findByNameLikeAsMap(@Param("name") String name);
}
