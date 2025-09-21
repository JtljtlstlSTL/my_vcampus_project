package com.vcampus.server.core.course.dao;

import com.vcampus.server.core.course.entity.Course;
import com.vcampus.server.core.course.mapper.CourseMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseDaoImpl implements CourseDao {
    private static final Logger logger = LoggerFactory.getLogger(CourseDaoImpl.class);
    private static CourseDaoImpl instance;
    private final SqlSessionFactory sqlSessionFactory;

    public CourseDaoImpl() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mybatis-config.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("初始化MyBatis失败", e);
        }
    }

    public static synchronized CourseDaoImpl getInstance() {
        if (instance == null) {
            instance = new CourseDaoImpl();
        }
        return instance;
    }

    @Override
    public boolean insertCourse(Course course) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected = mapper.insertCourse(course);
            session.commit();
            return affected > 0;
        }
    }

    @Override
    public boolean updateCourse(Course course) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected = mapper.updateCourse(course);
            session.commit();
            return affected > 0;
        }
    }

    @Override
    public boolean deleteCourse(int courseId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected = mapper.deleteCourse(courseId);
            session.commit();
            return affected > 0;
        }
    }

    @Override
    public Course getCourseById(String courseId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getCourseById(Integer.parseInt(courseId));
        }
    }

    @Override
    public List<Course> getAllCourses() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getAllCourses();
        }
    }

    @Override
    public List<Map<String, Object>> getSelectedCourses(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getSelectedCourses(cardNum);
        } catch (Exception e) {
            logger.error("获取已选课程失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getAvailableCourses(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getAvailableCourses(cardNum);
        } catch (Exception e) {
            logger.error("获取可选课程失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getScoreList(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getScoreList(cardNum);
        } catch (Exception e) {
            logger.error("获取成绩列表失败", e);
            return new ArrayList<>();
        }
    }

    // 教学班管理方法实现
    @Override
    public List<Map<String, Object>> getAllSections() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            List<Map<String, Object>> result = mapper.getAllSections();
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            logger.error("获取所有教学班失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean insertSection(int courseId, String term, String teacherId, String room, int capacity, String schedule) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected = mapper.insertSection(courseId, term, teacherId, room, capacity, schedule);
            session.commit();
            return affected > 0;
        } catch (Exception e) {
            logger.error("创建教学班失败", e);
            return false;
        }
    }

    @Override
    public boolean updateSection(int sectionId, int courseId, String term, String teacherId, String room, int capacity, String schedule) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected = mapper.updateSection(sectionId, courseId, term, teacherId, room, capacity, schedule);
            session.commit();
            return affected > 0;
        } catch (Exception e) {
            logger.error("更新教学班失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteSection(int sectionId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected = mapper.deleteSection(sectionId);
            session.commit();
            return affected > 0;
        } catch (Exception e) {
            logger.error("删除教学班失败", e);
            return false;
        }
    }

    /**
     * 通过 cardNum 查询 studentId
     */
    private Integer getStudentIdByCardNum(SqlSession session, String cardNum) {
        return session.selectOne("com.vcampus.server.core.course.mapper.CourseMapper.getStudentIdByCardNum", cardNum);
    }

    /**
     * 检查 section 是否存在
     */
    private boolean sectionExists(SqlSession session, int sectionId) {
        Integer count = session.selectOne("com.vcampus.server.core.course.mapper.CourseMapper.sectionExists", sectionId);
        return count != null && count > 0;
    }

    /**
     * 检查时间冲突 - 使用智能时间解析工具
     */
    private boolean hasTimeConflict(SqlSession session, String cardNum, int sectionId) {
        CourseMapper mapper = session.getMapper(CourseMapper.class);

        // 获取学生已选课程的所有时间安排
        List<String> selectedSchedules = mapper.getStudentSchedules(cardNum);

        // 获取要选择的教学班的时间安排
        String newSchedule = mapper.getSectionSchedule(sectionId);

        if (newSchedule == null || newSchedule.trim().isEmpty()) {
            // 如果新课程没有时间安排，不算冲突
            return false;
        }

        // 使用智能工具检查时间冲突
        for (String selectedSchedule : selectedSchedules) {
            if (com.vcampus.server.core.course.util.ScheduleConflictChecker.hasConflict(selectedSchedule, newSchedule)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否已选择同一门课程的其他教学班
     */
    private boolean hasSameCourseConflict(SqlSession session, String cardNum, int sectionId) {
        CourseMapper mapper = session.getMapper(CourseMapper.class);
        int conflictCount = mapper.checkSameCourseConflict(cardNum, sectionId);
        return conflictCount > 0;
    }

    @Override
    public boolean selectCourse(String cardNum, String sectionId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Integer studentId = getStudentIdByCardNum(session, cardNum);
            if (studentId == null) {
                throw new RuntimeException("学生不存在，无法选课");
            }
            int id = parseSectionId(sectionId);
            if (!sectionExists(session, id)) {
                throw new RuntimeException("教学班不存在，无法选课");
            }

            // 检查是否已选择同一门课程
            if (hasSameCourseConflict(session, cardNum, id)) {
                throw new RuntimeException("选课失败：不能重复选择同一门课程的不同教学班");
            }

            // 检查时间冲突
            if (hasTimeConflict(session, cardNum, id)) {
                throw new RuntimeException("选课失败：与已选课程时间冲突");
            }

            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected;
            try {
                affected = mapper.selectCourse(cardNum, id);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException("选课数据库操作失败: " + e.getMessage(), e);
            }
            if (affected <= 0) {
                throw new RuntimeException("选课失败，可能已选过或人数已满");
            }
            return true;
        }
    }

    @Override
    public boolean dropCourse(String cardNum, String sectionId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Integer studentId = getStudentIdByCardNum(session, cardNum);
            if (studentId == null) {
                throw new RuntimeException("学生不存在，无法退课");
            }
            int id = parseSectionId(sectionId);
            if (!sectionExists(session, id)) {
                throw new RuntimeException("教学班不存在，无法退课");
            }
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected;
            try {
                affected = mapper.dropCourse(cardNum, id);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException("退课数据库操作失败: " + e.getMessage(), e);
            }
            if (affected <= 0) {
                throw new RuntimeException("退课失败，可能未选该课");
            }
            return true;
        }
    }

    // 新增：课程查询功能的实现
    @Override
    public List<Course> searchCourses(String keyword) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            List<Course> result = mapper.searchCourses(keyword);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            logger.error("搜索课程失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> searchSections(String keyword) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            List<Map<String, Object>> result = mapper.searchSections(keyword);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            logger.error("搜索教学班失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Course> getCoursesByDepartment(String department) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            List<Course> result = mapper.getCoursesByDepartment(department);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            logger.error("按院系获取课程失败", e);
            return new ArrayList<>();
        }
    }

    // 教师端功能实现
    @Override
    public List<Map<String, Object>> getTeacherSections(String teacherId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getTeacherSections(teacherId);
        } catch (Exception e) {
            logger.error("获取教师教学班失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getSectionStudents(int sectionId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getSectionStudents(sectionId);
        } catch (Exception e) {
            logger.error("获取教学班学生失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateStudentScore(String cardNum, int sectionId, double score, double gpa) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected = mapper.updateStudentScore(cardNum, sectionId, score, gpa);
            session.commit();
            return affected > 0;
        } catch (Exception e) {
            logger.error("更新学生成绩失败", e);
            return false;
        }
    }

    @Override
    public boolean batchUpdateScores(int sectionId, String scoresData) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 这里需要解析scoresData并批量更新，暂时返回false，需要具体实现
            // TODO: 实现批量更新逻辑
            return false;
        } catch (Exception e) {
            logger.error("批量更新成绩失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getSectionDetails(int sectionId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getSectionDetails(sectionId);
        } catch (Exception e) {
            logger.error("获取教学班详情失败", e);
            return new java.util.HashMap<>();
        }
    }

    @Override
    public List<Map<String, Object>> getStudentGrades(int sectionId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getStudentGrades(sectionId);
        } catch (Exception e) {
            logger.error("获取学生成绩失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getStudentGradeStatistics(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getStudentGradeStatistics(cardNum);
        } catch (Exception e) {
            logger.error("获取学生成绩统计失败", e);
            return new java.util.HashMap<>();
        }
    }

    // 通过学号更新学生成绩
    public boolean updateStudentScoreByCardNum(String cardNum, int sectionId, double score, double gpa) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected = mapper.updateStudentScore(cardNum, sectionId, score, gpa);
            session.commit();
            return affected > 0;
        } catch (Exception e) {
            logger.error("通过学号更新学生成绩失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 兼容前端传递的 "1.0"、"2.0"、"0.0" 等字符串
     */
    private int parseSectionId(String sectionId) {
        try {
            if (sectionId == null) throw new NumberFormatException("sectionId is null");
            sectionId = sectionId.trim();
            if (sectionId.contains(".")) {
                double d = Double.parseDouble(sectionId);
                return (int) d;
            } else {
                return Integer.parseInt(sectionId);
            }
        } catch (Exception e) {
            throw new RuntimeException("sectionId解析失败: " + sectionId, e);
        }
    }

    @Override
    public List<Map<String, Object>> getCoursesByTeacher(String teacherId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getCoursesByTeacher(teacherId);
        } catch (Exception e) {
            logger.error("获取教师课程失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getScoreList(int sectionId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getScoreListBySectionId(sectionId);
        } catch (Exception e) {
            logger.error("获取成绩列表(按教学班)失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean submitEvaluation(String studentId, int sectionId, double score) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            int affected = mapper.submitEvaluation(studentId, sectionId, score);
            session.commit();
            return affected > 0;
        } catch (Exception e) {
            logger.error("提交评教失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getEvaluationStats(int sectionId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getEvaluationStats(sectionId);
        } catch (Exception e) {
            logger.error("获取评教统计失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public List<Map<String, Object>> getAllStudentGrades() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getAllStudentGrades();
        } catch (Exception e) {
            logger.error("获取所有学生成绩失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getAllTeacherEvaluations() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getAllTeacherEvaluations();
        } catch (Exception e) {
            logger.error("获取所有教师评教失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getStudentAverageGrades(String studentId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CourseMapper mapper = session.getMapper(CourseMapper.class);
            return mapper.getStudentAverageGrades(studentId);
        } catch (Exception e) {
            logger.error("获取学生平均成绩失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}
