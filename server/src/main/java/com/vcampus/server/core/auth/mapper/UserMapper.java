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
     * 插入用户信息（完整字段版本）
     */
    @Insert("INSERT INTO tblUser (cardNum, cardNumPassword, Name, BirthDate, Gender, Phone, userType, Ethnicity, IdCard, Hometown) " +
            "VALUES (#{cardNum}, #{password}, #{name}, #{birthDate}, #{gender}, #{phone}, #{userType}, #{ethnicity}, #{idCard}, #{hometown})")
    int insertUser(@Param("cardNum") String cardNum,
                   @Param("password") String password,
                   @Param("name") String name,
                   @Param("birthDate") String birthDate,
                   @Param("gender") String gender,
                   @Param("phone") String phone,
                   @Param("userType") String userType,
                   @Param("ethnicity") String ethnicity,
                   @Param("idCard") String idCard,
                   @Param("hometown") String hometown);

    /**
     * 更新用户信息
     */
    @Update("UPDATE tblUser SET Name = #{name}, BirthDate = #{birthDate}, Gender = #{gender}, " +
            "Phone = #{phone}, userType = #{userType}, Ethnicity = #{ethnicity}, IdCard = #{idCard}, Hometown = #{hometown} WHERE cardNum = #{cardNum}")
    int updateUser(@Param("cardNum") String cardNum,
                   @Param("name") String name,
                   @Param("birthDate") String birthDate,
                   @Param("gender") String gender,
                   @Param("phone") String phone,
                   @Param("userType") String userType,
                   @Param("ethnicity") String ethnicity,
                   @Param("idCard") String idCard,
                   @Param("hometown") String hometown);

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

    /**
     * 更新用户个人信息（不包括密码和用户类型）
     */
    @Update("UPDATE tblUser SET " +
            "Name = #{name}, " +
            "BirthDate = CASE WHEN #{birthDate} IS NULL OR #{birthDate} = '' THEN BirthDate ELSE #{birthDate} END, " +
            "Gender = CASE WHEN #{gender} IS NULL OR #{gender} = '' THEN Gender ELSE #{gender} END, " +
            "Phone = #{phone}, " +
            "Ethnicity = CASE WHEN #{ethnicity} IS NULL OR #{ethnicity} = '' THEN Ethnicity ELSE #{ethnicity} END, " +
            "IdCard = CASE WHEN #{idCard} IS NULL OR #{idCard} = '' THEN IdCard ELSE #{idCard} END, " +
            "Hometown = CASE WHEN #{hometown} IS NULL OR #{hometown} = '' THEN Hometown ELSE #{hometown} END " +
            "WHERE cardNum = #{cardNum}")
    int updatePersonalInfo(@Param("cardNum") String cardNum,
                          @Param("name") String name,
                          @Param("birthDate") String birthDate,
                          @Param("gender") String gender,
                          @Param("phone") String phone,
                          @Param("ethnicity") String ethnicity,
                          @Param("idCard") String idCard,
                          @Param("hometown") String hometown);
}
