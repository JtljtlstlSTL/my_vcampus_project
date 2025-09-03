package com.vcampus.server.core.academic.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 教师数据访问接口 - MyBatis Mapper
 */
@Mapper
public interface StaffMapper {

    /**
     * 获取所有教师信息（包含用户基本信息）
     */
    @Select("SELECT s.cardNum, s.staff_Id as staffId, s.Title as title, s.Department as department, " +
            "u.name, u.age, u.gender, u.phone " +
            "FROM tblStaff s " +
            "LEFT JOIN tblUser u ON s.cardNum = u.cardNum")
    List<Map<String, Object>> getAllStaffWithUserInfo();

    /**
     * 根据卡号查找教师
     */
    @Select("SELECT s.cardNum, s.staff_Id as staffId, s.Title as title, s.Department as department, " +
            "u.name, u.age, u.gender, u.phone " +
            "FROM tblStaff s " +
            "LEFT JOIN tblUser u ON s.cardNum = u.cardNum " +
            "WHERE s.cardNum = #{cardNum}")
    Map<String, Object> findByCardNumWithUserInfo(@Param("cardNum") String cardNum);

    /**
     * 根据工号查找教师
     */
    @Select("SELECT s.cardNum, s.staff_Id as staffId, s.Title as title, s.Department as department, " +
            "u.name, u.age, u.gender, u.phone " +
            "FROM tblStaff s " +
            "LEFT JOIN tblUser u ON s.cardNum = u.cardNum " +
            "WHERE s.staff_Id = #{staffId}")
    Map<String, Object> findByStaffIdWithUserInfo(@Param("staffId") String staffId);

    /**
     * 插入教师信息到tblStaff表
     */
    @Insert("INSERT INTO tblStaff (cardNum, staff_Id, Title, Department) " +
            "VALUES (#{cardNum}, #{staffId}, #{title}, #{department})")
    int insertStaffInfo(@Param("cardNum") String cardNum,
                       @Param("staffId") String staffId,
                       @Param("title") String title,
                       @Param("department") String department);

    /**
     * 更新教师信息
     */
    @Update("UPDATE tblStaff SET staff_Id = #{staffId}, Title = #{title}, " +
            "Department = #{department} WHERE cardNum = #{cardNum}")
    int updateStaffInfo(@Param("cardNum") String cardNum,
                       @Param("staffId") String staffId,
                       @Param("title") String title,
                       @Param("department") String department);

    /**
     * 删除教师信息
     */
    @Delete("DELETE FROM tblStaff WHERE cardNum = #{cardNum}")
    int deleteByCardNum(@Param("cardNum") String cardNum);

    /**
     * 检查卡号是否已存在
     */
    @Select("SELECT COUNT(*) FROM tblStaff WHERE cardNum = #{cardNum}")
    int isCardNumExists(@Param("cardNum") String cardNum);

    /**
     * 检查工号是否已存在
     */
    @Select("SELECT COUNT(*) FROM tblStaff WHERE staff_Id = #{staffId}")
    int isStaffIdExists(@Param("staffId") String staffId);

    /**
     * 检查工号是否已存在（排除指定卡号）
     */
    @Select("SELECT COUNT(*) FROM tblStaff WHERE staff_Id = #{staffId} AND cardNum != #{cardNum}")
    int isStaffIdExistsExcludeCardNum(@Param("staffId") String staffId, @Param("cardNum") String cardNum);
}
