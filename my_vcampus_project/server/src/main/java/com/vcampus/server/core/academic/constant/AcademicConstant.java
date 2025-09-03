package com.vcampus.server.core.academic.constant;

/**
 * 学术事务模块常量定义
 * 
 * @author VCampus Team
 */
public class AcademicConstant {
    
    // 课程状态常量
    public static final String COURSE_STATUS_ACTIVE = "ACTIVE";
    public static final String COURSE_STATUS_INACTIVE = "INACTIVE";
    public static final String COURSE_STATUS_ARCHIVED = "ARCHIVED";
    
    // 选课状态常量
    public static final String ENROLLMENT_STATUS_ENROLLED = "ENROLLED";
    public static final String ENROLLMENT_STATUS_DROPPED = "DROPPED";
    public static final String ENROLLMENT_STATUS_COMPLETED = "COMPLETED";
    
    // 成绩等级常量
    public static final String GRADE_A_PLUS = "A+";
    public static final String GRADE_A = "A";
    public static final String GRADE_B_PLUS = "B+";
    public static final String GRADE_B = "B";
    public static final String GRADE_C_PLUS = "C+";
    public static final String GRADE_C = "C";
    public static final String GRADE_D = "D";
    public static final String GRADE_F = "F";
    
    // 课程类型常量
    public static final String COURSE_TYPE_REQUIRED = "REQUIRED";
    public static final String COURSE_TYPE_ELECTIVE = "ELECTIVE";
    public static final String COURSE_TYPE_GENERAL = "GENERAL";
    
    // 学分相关常量
    public static final int MIN_CREDITS_PER_SEMESTER = 12;
    public static final int MAX_CREDITS_PER_SEMESTER = 25;
    public static final int GRADUATION_CREDITS = 120;
    
    private AcademicConstant() {
        // 工具类，禁止实例化
    }
}
