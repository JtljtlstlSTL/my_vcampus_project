package com.vcampus.server.core.student.constant;

/**
 * 学生管理模块常量定义
 * 
 * @author VCampus Team
 */
public class StudentConstant {
    
    // 学生状态常量
    public static final String STUDENT_STATUS_ACTIVE = "ACTIVE";
    public static final String STUDENT_STATUS_GRADUATED = "GRADUATED";
    public static final String STUDENT_STATUS_SUSPENDED = "SUSPENDED";
    public static final String STUDENT_STATUS_EXPELLED = "EXPELLED";
    
    // 年级常量
    public static final String GRADE_FRESHMAN = "FRESHMAN";
    public static final String GRADE_SOPHOMORE = "SOPHOMORE";
    public static final String GRADE_JUNIOR = "JUNIOR";
    public static final String GRADE_SENIOR = "SENIOR";
    
    // 专业分类常量
    public static final String MAJOR_COMPUTER_SCIENCE = "计算机科学与技术";
    public static final String MAJOR_SOFTWARE_ENGINEERING = "软件工程";
    public static final String MAJOR_INFORMATION_SECURITY = "信息安全";
    public static final String MAJOR_ARTIFICIAL_INTELLIGENCE = "人工智能";
    
    // 学生证相关常量
    public static final String STUDENT_ID_PREFIX = "STU";
    public static final int STUDENT_ID_LENGTH = 10;
    
    // 注册状态常量
    public static final String REGISTRATION_STATUS_REGISTERED = "REGISTERED";
    public static final String REGISTRATION_STATUS_UNREGISTERED = "UNREGISTERED";
    public static final String REGISTRATION_STATUS_PENDING = "PENDING";
    
    private StudentConstant() {
        // 工具类，禁止实例化
    }
}
