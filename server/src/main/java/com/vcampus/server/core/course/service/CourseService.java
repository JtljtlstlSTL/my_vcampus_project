package com.vcampus.server.core.course.service;
import com.vcampus.server.core.course.entity.Course;
import com.vcampus.server.core.course.dao.CourseDao;
import com.vcampus.server.core.course.dao.CourseDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

public class CourseService {
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    private final CourseDao courseDao = new CourseDaoImpl();

    public List<Map<String, Object>> getSelectedCourses(String cardNum) {
        return courseDao.getSelectedCourses(cardNum);
    }

    public List<Map<String, Object>> getAvailableCourses(String cardNum) {
        return courseDao.getAvailableCourses(cardNum);
    }

    public boolean selectCourse(String cardNum, String sectionId) {
        return courseDao.selectCourse(cardNum, sectionId);
    }

    public boolean dropCourse(String cardNum, String sectionId) {
        return courseDao.dropCourse(cardNum, sectionId);
    }

    public boolean insertCourse(Course course) {
        return courseDao.insertCourse(course);
    }

    public boolean updateCourse(Course course) {
        return courseDao.updateCourse(course);
    }

    // 删除String重载
    // public boolean deleteCourse(String courseId) {
    //     return courseDao.deleteCourse(courseId);
    // }

    // 修正int重载，直接传递int
    public boolean deleteCourse(int courseId) {
        try {
            return courseDao.deleteCourse(courseId);
        } catch (Exception e) {
            logger.error("删除课程时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    public Course getCourseById(String courseId) {
        return courseDao.getCourseById(courseId);
    }

    public List<Course> getAllCourses() {
        return courseDao.getAllCourses();
    }

    public List<Map<String, Object>> getScoreList(String cardNum) {
        return courseDao.getScoreList(cardNum);
    }

    // 教学班管理方法
    public List<Map<String, Object>> getAllSections() {
        try {
            return courseDao.getAllSections();
        } catch (Exception e) {
            // 记录错误日志并返回空列表，避免整个界面崩溃
            logger.error("获取教学班列表时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    // 新增：课程查询功能
    public List<Course> searchCourses(String keyword) {
        try {
            return courseDao.searchCourses(keyword);
        } catch (Exception e) {
            logger.error("搜索课程时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    public List<Map<String, Object>> searchSections(String keyword) {
        try {
            return courseDao.searchSections(keyword);
        } catch (Exception e) {
            logger.error("搜索教学班时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    public List<Course> getCoursesByDepartment(String department) {
        try {
            return courseDao.getCoursesByDepartment(department);
        } catch (Exception e) {
            logger.error("按院系查询课程时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    // 教师端功能服务方法
    public List<Map<String, Object>> getTeacherSections(String teacherId) {
        try {
            return courseDao.getTeacherSections(teacherId);
        } catch (Exception e) {
            logger.error("获取教师教学班时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    public List<Map<String, Object>> getSectionStudents(int sectionId) {
        try {
            return courseDao.getSectionStudents(sectionId);
        } catch (Exception e) {
            logger.error("获取教学班学生时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    public boolean updateStudentScore(String cardNum, int sectionId, double score, double gpa) {
        try {
            return courseDao.updateStudentScore(cardNum, sectionId, score, gpa);
        } catch (Exception e) {
            logger.error("更新学生成绩时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean batchUpdateScores(int sectionId, String scoresData) {
        try {
            return courseDao.batchUpdateScores(sectionId, scoresData);
        } catch (Exception e) {
            logger.error("批量更新成绩时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    public Map<String, Object> getSectionDetails(int sectionId) {
        try {
            return courseDao.getSectionDetails(sectionId);
        } catch (Exception e) {
            logger.error("获取教学班详情时发生错误: {}", e.getMessage(), e);
            return new java.util.HashMap<>();
        }
    }

    public List<Map<String, Object>> getAllCoursesAsMap() {
        try {
            List<Course> courses = courseDao.getAllCourses();
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (Course course : courses) {
                Map<String, Object> courseMap = new java.util.HashMap<>();
                // 修复：统一字段名，确保前端能正确获取所有字段
                courseMap.put("course_Id", course.getCourse_Id());
                courseMap.put("courseName", course.getCourseName());
                courseMap.put("credit", course.getCredit());        // 改为小写，与前端一致
                courseMap.put("department", course.getDepartment()); // 改为小写，与前端一致
                result.add(courseMap);
            }
            return result;
        } catch (Exception e) {
            logger.error("获取所有课程时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    // 新增：根据教师ID获取课程列表
    public List<Map<String, Object>> getCoursesByTeacher(String teacherId) {
        try {
            return courseDao.getCoursesByTeacher(teacherId);
        } catch (Exception e) {
            logger.error("获取教师课程时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    // 新增：根据教学班ID获取成绩列表
    public List<Map<String, Object>> getScoreList(int sectionId) {
        try {
            return courseDao.getScoreList(sectionId);
        } catch (Exception e) {
            logger.error("获取成绩列表时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    // 新增：教学班管理方法
    public boolean deleteSection(int sectionId) {
        try {
            return courseDao.deleteSection(sectionId);
        } catch (Exception e) {
            logger.error("删除教学班时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean insertSection(int courseId, String term, String teacherId, String room, int capacity, String schedule) {
        try {
            return courseDao.insertSection(courseId, term, teacherId, room, capacity, schedule);
        } catch (Exception e) {
            logger.error("新增教学班时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean updateSection(int sectionId, int courseId, String term, String teacherId, String room, int capacity, String schedule) {
        try {
            return courseDao.updateSection(sectionId, courseId, term, teacherId, room, capacity, schedule);
        } catch (Exception e) {
            logger.error("修改教学班时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    // 新增：获取学生成绩统计
    public Map<String, Object> getStudentGradeStatistics(String cardNum) {
        try {
            return courseDao.getStudentGradeStatistics(cardNum);
        } catch (Exception e) {
            logger.error("获取学生成绩统计时发生错误: {}", e.getMessage(), e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 提交评教数据
     */
    public boolean submitEvaluation(String studentId, int sectionId, double score) {
        try {
            return courseDao.submitEvaluation(studentId, sectionId, score);
        } catch (Exception e) {
            logger.error("提交评教时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取评教统计信息
     */
    public Map<String, Object> getEvaluationStats(int sectionId) {
        try {
            return courseDao.getEvaluationStats(sectionId);
        } catch (Exception e) {
            logger.error("获取评教统计时发生错误: {}", e.getMessage(), e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 获取所有学生的成绩统计
     */
    public List<Map<String, Object>> getAllStudentGrades() {
        try {
            return courseDao.getAllStudentGrades();
        } catch (Exception e) {
            logger.error("获取所有学生成绩时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 获取所有教师的评教统计
     */
    public List<Map<String, Object>> getAllTeacherEvaluations() {
        try {
            return courseDao.getAllTeacherEvaluations();
        } catch (Exception e) {
            logger.error("获取所有教师评教时发生错误: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 获取单个学生的平均成绩和GPA
     */
    public Map<String, Object> getStudentAverageGrades(String studentId) {
        try {
            return courseDao.getStudentAverageGrades(studentId);
        } catch (Exception e) {
            logger.error("获取学生平均成绩时发生错误: {}", e.getMessage(), e);
            return new java.util.HashMap<>();
        }
    }
}
