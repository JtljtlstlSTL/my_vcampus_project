package com.vcampus.server.core.course.dao;

import com.vcampus.server.core.course.entity.Course;
import java.util.List;
import java.util.Map;

/**
 * 课程数据访问对象接口
 */
public interface CourseDao {
    boolean insertCourse(Course course);
    boolean updateCourse(Course course);
    boolean deleteCourse(int courseId);
    Course getCourseById(String courseId);
    List<Course> getAllCourses();

    // 选课相关扩展方法
    List<Map<String, Object>> getSelectedCourses(String cardNum);
    List<Map<String, Object>> getAvailableCourses(String cardNum);
    boolean selectCourse(String cardNum, String sectionId);
    boolean dropCourse(String cardNum, String sectionId);

    // 获取学生成绩列表（已结课有成绩）
    List<Map<String, Object>> getScoreList(String cardNum);

    // 教学班管理方法
    List<Map<String, Object>> getAllSections();

    // 新增：课程查询功能（支持模糊查询）
    List<Course> searchCourses(String keyword);
    List<Map<String, Object>> searchSections(String keyword);
    List<Course> getCoursesByDepartment(String department);

    // 教师端功能：课程和成绩管理
    List<Map<String, Object>> getTeacherSections(String teacherId);
    List<Map<String, Object>> getSectionStudents(int sectionId);
    boolean updateStudentScore(String cardNum, int sectionId, double score, double gpa);
    boolean batchUpdateScores(int sectionId, String scoresData);
    Map<String, Object> getSectionDetails(int sectionId);
    List<Map<String, Object>> getStudentGrades(int sectionId);

    boolean insertSection(int courseId, String term, String teacherId, String room, int capacity, String schedule);
    boolean updateSection(int sectionId, int courseId, String term, String teacherId, String room, int capacity, String schedule);
    boolean deleteSection(int sectionId);

    // 新增：根据教师ID获取课程列表
    List<Map<String, Object>> getCoursesByTeacher(String teacherId);

    // 新增：根据教学班ID获取成绩列表
    List<Map<String, Object>> getScoreList(int sectionId);
    
    // 新增：获取学生成绩统计
    Map<String, Object> getStudentGradeStatistics(String cardNum);

    // 评教相关方法
    boolean submitEvaluation(String studentId, int sectionId, double score);
    Map<String, Object> getEvaluationStats(int sectionId);
    
    // 教务管理相关方法
    List<Map<String, Object>> getAllStudentGrades();
    List<Map<String, Object>> getAllTeacherEvaluations();
    
    // 获取单个学生的平均成绩和GPA
    Map<String, Object> getStudentAverageGrades(String studentId);
}
