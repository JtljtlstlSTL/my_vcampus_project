package com.vcampus.server.core.course.mapper;

import com.vcampus.server.core.course.entity.Course;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface CourseMapper {
    int insertCourse(Course course);
    int updateCourse(Course course);
    int deleteCourse(@Param("courseId") int courseId);
    Course getCourseById(@Param("courseId") int courseId);
    List<Course> getAllCourses();

    // 选课相关
    List<Map<String, Object>> getSelectedCourses(@Param("cardNum") String cardNum);
    List<Map<String, Object>> getAvailableCourses(@Param("cardNum") String cardNum);
    int selectCourse(@Param("cardNum") String cardNum, @Param("sectionId") int sectionId);
    int dropCourse(@Param("cardNum") String cardNum, @Param("sectionId") int sectionId);

    // 时间冲突检测
    int checkTimeConflict(@Param("cardNum") String cardNum, @Param("sectionId") int sectionId);
    List<String> getStudentSchedules(@Param("cardNum") String cardNum);
    String getSectionSchedule(@Param("sectionId") int sectionId);

    // 同一课程重复选择检测
    int checkSameCourseConflict(@Param("cardNum") String cardNum, @Param("sectionId") int sectionId);
    int getSectionCourseId(@Param("sectionId") int sectionId);

    // 获取学生成绩列表（已结课有成绩）
    List<Map<String, Object>> getScoreList(@Param("cardNum") String cardNum);

    // 教学班管理方法
    List<Map<String, Object>> getAllSections();
    List<Course> searchCourses(@Param("keyword") String keyword);
    List<Map<String, Object>> searchSections(@Param("keyword") String keyword);
    List<Course> getCoursesByDepartment(@Param("department") String department);
    int insertSection(@Param("courseId") int courseId, @Param("term") String term,
                     @Param("teacherId") String teacherId, @Param("room") String room,
                     @Param("capacity") int capacity, @Param("schedule") String schedule);
    int updateSection(@Param("sectionId") int sectionId, @Param("courseId") int courseId,
                     @Param("term") String term, @Param("teacherId") String teacherId,
                     @Param("room") String room, @Param("capacity") int capacity,
                     @Param("schedule") String schedule);
    int deleteSection(@Param("sectionId") int sectionId);

    // 教师端功能：课程和成绩管理
    List<Map<String, Object>> getTeacherSections(@Param("teacherId") String teacherId);
    List<Map<String, Object>> getSectionStudents(@Param("sectionId") int sectionId);
    int updateStudentScore(@Param("cardNum") String cardNum, @Param("sectionId") int sectionId,
                          @Param("score") double score, @Param("gpa") double gpa);
    Map<String, Object> getSectionDetails(@Param("sectionId") int sectionId);
    List<Map<String, Object>> getStudentGrades(@Param("sectionId") int sectionId);

    // 辅助查询方法
    Integer getStudentIdByCardNum(@Param("cardNum") String cardNum);
    Integer sectionExists(@Param("sectionId") int sectionId);

    // 新增：根据教师ID获取课程列表
    List<Map<String, Object>> getCoursesByTeacher(@Param("teacherId") String teacherId);
    // 新增：根据教学班ID获取成绩列表
    List<Map<String, Object>> getScoreListBySectionId(@Param("sectionId") int sectionId);
    
    // 新增：获取学生成绩统计
    Map<String, Object> getStudentGradeStatistics(@Param("cardNum") String cardNum);

    // 评教相关方法
    int submitEvaluation(@Param("studentId") String studentId, @Param("sectionId") int sectionId, @Param("score") double score);
    Map<String, Object> getEvaluationStats(@Param("sectionId") int sectionId);
    
    // 教务管理相关方法
    List<Map<String, Object>> getAllStudentGrades();
    List<Map<String, Object>> getAllTeacherEvaluations();
    
    // 获取单个学生的平均成绩和GPA
    Map<String, Object> getStudentAverageGrades(@Param("studentId") String studentId);
}
