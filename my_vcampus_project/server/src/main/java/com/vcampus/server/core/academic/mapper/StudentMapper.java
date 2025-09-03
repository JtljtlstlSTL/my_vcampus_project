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
 * 学生数据访问接口 - MyBatis Mapper
 */
@Mapper
public interface StudentMapper {

    /**
     * 获取所有学生信息（包含用户基本信息）
     */
    @Select("SELECT s.cardNum, s.student_Id as studentId, s.Grade as grade, s.Major as major, s.Department as department, " +
            "u.name, u.age, u.gender, u.phone " +
            "FROM tblStudent s " +
            "LEFT JOIN tblUser u ON s.cardNum = u.cardNum")
    List<Map<String, Object>> getAllStudentsWithUserInfo();

    /**
     * 根据卡号查找学生
     */
    @Select("SELECT s.cardNum, s.student_Id as studentId, s.Grade as grade, s.Major as major, s.Department as department, " +
            "u.name, u.age, u.gender, u.phone " +
            "FROM tblStudent s " +
            "LEFT JOIN tblUser u ON s.cardNum = u.cardNum " +
            "WHERE s.cardNum = #{cardNum}")
    Map<String, Object> findByCardNumWithUserInfo(@Param("cardNum") String cardNum);

    /**
     * 根据学号查找学生
     */
    @Select("SELECT s.cardNum, s.student_Id as studentId, s.Grade as grade, s.Major as major, s.Department as department, " +
            "u.name, u.age, u.gender, u.phone " +
            "FROM tblStudent s " +
            "LEFT JOIN tblUser u ON s.cardNum = u.cardNum " +
            "WHERE s.student_Id = #{studentId}")
    Map<String, Object> findByStudentIdWithUserInfo(@Param("studentId") String studentId);

    /**
     * 插入学生信息到tblstudent表
     */
    @Insert("INSERT INTO tblStudent (cardNum, student_Id, Grade, Major, Department) " +
            "VALUES (#{cardNum}, #{studentId}, #{grade}, #{major}, #{department})")
    int insertStudentInfo(@Param("cardNum") String cardNum,
                         @Param("studentId") String studentId,
                         @Param("grade") Integer grade,
                         @Param("major") String major,
                         @Param("department") String department);

    /**
     * 更新学生信息
     */
    @Update("UPDATE tblStudent SET student_Id = #{studentId}, Grade = #{grade}, " +
            "Major = #{major}, Department = #{department} WHERE cardNum = #{cardNum}")
    int updateStudentInfo(@Param("cardNum") String cardNum,
                         @Param("studentId") String studentId,
                         @Param("grade") Integer grade,
                         @Param("major") String major,
                         @Param("department") String department);

    /**
     * 删除学生信息
     */
    @Delete("DELETE FROM tblStudent WHERE cardNum = #{cardNum}")
    int deleteByCardNum(@Param("cardNum") String cardNum);

    /**
     * 检查卡号是否已存在
     */
    @Select("SELECT COUNT(*) FROM tblStudent WHERE cardNum = #{cardNum}")
    int isCardNumExists(@Param("cardNum") String cardNum);

    /**
     * 检查学号是否已存在
     */
    @Select("SELECT COUNT(*) FROM tblStudent WHERE student_Id = #{studentId}")
    int isStudentIdExists(@Param("studentId") String studentId);

    /**
     * 检查学号是否已存在（排除指定卡号）
     */
    @Select("SELECT COUNT(*) FROM tblStudent WHERE student_Id = #{studentId} AND cardNum != #{cardNum}")
    int isStudentIdExistsExcludeCardNum(@Param("studentId") String studentId, @Param("cardNum") String cardNum);
}
