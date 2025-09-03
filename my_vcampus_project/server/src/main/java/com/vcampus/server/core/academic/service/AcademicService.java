package com.vcampus.server.core.academic.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.vcampus.server.core.academic.mapper.StaffMapper;
import com.vcampus.server.core.academic.mapper.StudentMapper;
import com.vcampus.server.core.auth.mapper.UserMapper;
import com.vcampus.server.core.db.DatabaseManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 学术管理服务类 - MyBatis版本
 * 处理学生和教师信息的业务逻辑
 */
@Slf4j
public class AcademicService {

    private final SqlSessionFactory sqlSessionFactory;

    public AcademicService() {
        this.sqlSessionFactory = DatabaseManager.getSqlSessionFactory();
    }

    // 学生相关业务方法
    public List<Map<String, Object>> getAllStudentsWithUserInfo() throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper studentMapper = session.getMapper(StudentMapper.class);
            return studentMapper.getAllStudentsWithUserInfo();
        }
    }

    public Map<String, Object> getStudentByCardNum(String cardNum) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper studentMapper = session.getMapper(StudentMapper.class);
            return studentMapper.findByCardNumWithUserInfo(cardNum);
        }
    }

    public Map<String, Object> getStudentByStudentId(String studentId) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper studentMapper = session.getMapper(StudentMapper.class);
            return studentMapper.findByStudentIdWithUserInfo(studentId);
        }
    }

    public boolean addStudent(Map<String, Object> studentData) throws Exception {
        // 验证学生信息
        validateStudentData(studentData);

        String cardNum = (String) studentData.get("cardNum");
        String studentId = (String) studentData.get("studentId");

        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper studentMapper = session.getMapper(StudentMapper.class);
            UserMapper userMapper = session.getMapper(UserMapper.class);

            // 检查卡号是否已存在
            if (userMapper.isCardNumExists(cardNum) > 0) {
                throw new Exception("卡号已存在: " + cardNum);
            }

            // 检查学号是否已存在
            if (studentMapper.isStudentIdExists(studentId) > 0) {
                throw new Exception("学号已存在: " + studentId);
            }

            // 插入用户基本信息
            userMapper.insertUser(
                cardNum,
                "123456", // 默认密码
                (String) studentData.get("name"),
                convertToInteger(studentData.get("age")),
                (String) studentData.get("gender"),
                (String) studentData.get("phone"),
                "student"
            );

            // 插入学生扩展信息
            studentMapper.insertStudentInfo(
                cardNum,
                studentId,
                convertToInteger(studentData.get("grade")),
                (String) studentData.get("major"),
                (String) studentData.get("department")
            );

            session.commit();
            return true;
        }
    }

    public boolean updateStudent(Map<String, Object> studentData) throws Exception {
        // 验证学生信息
        validateStudentData(studentData);

        String cardNum = (String) studentData.get("cardNum");
        String studentId = (String) studentData.get("studentId");

        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper studentMapper = session.getMapper(StudentMapper.class);
            UserMapper userMapper = session.getMapper(UserMapper.class);

            // 检查学生是否存在
            Map<String, Object> existingStudent = studentMapper.findByCardNumWithUserInfo(cardNum);
            if (existingStudent == null) {
                throw new Exception("学生不存在: " + cardNum);
            }

            // 检查学号是否被其他学生使用
            if (studentMapper.isStudentIdExistsExcludeCardNum(studentId, cardNum) > 0) {
                throw new Exception("学号已被其他学生使用: " + studentId);
            }

            // 更新用户基本信息
            userMapper.updateUser(
                cardNum,
                (String) studentData.get("name"),
                convertToInteger(studentData.get("age")),
                (String) studentData.get("gender"),
                (String) studentData.get("phone"),
                "student"
            );

            // 更新学生扩展信息
            studentMapper.updateStudentInfo(
                cardNum,
                studentId,
                convertToInteger(studentData.get("grade")),
                (String) studentData.get("major"),
                (String) studentData.get("department")
            );

            session.commit();
            return true;
        }
    }

    public boolean deleteStudent(String cardNum) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper userMapper = session.getMapper(UserMapper.class);
            
            // 删除用户（由于外键约束，会自动删除关联的学生记录）
            int rowsAffected = userMapper.deleteByCardNum(cardNum);
            session.commit();
            return rowsAffected > 0;
        }
    }

    // 教师相关业务方法
    public List<Map<String, Object>> getAllStaffWithUserInfo() throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StaffMapper staffMapper = session.getMapper(StaffMapper.class);
            return staffMapper.getAllStaffWithUserInfo();
        }
    }

    public Map<String, Object> getStaffByCardNum(String cardNum) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StaffMapper staffMapper = session.getMapper(StaffMapper.class);
            return staffMapper.findByCardNumWithUserInfo(cardNum);
        }
    }

    public Map<String, Object> getStaffByStaffId(String staffId) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StaffMapper staffMapper = session.getMapper(StaffMapper.class);
            return staffMapper.findByStaffIdWithUserInfo(staffId);
        }
    }

    public boolean addStaff(Map<String, Object> staffData) throws Exception {
        // 验证教师信息
        validateStaffData(staffData);

        String cardNum = (String) staffData.get("cardNum");
        String staffId = (String) staffData.get("staffId");

        try (SqlSession session = sqlSessionFactory.openSession()) {
            StaffMapper staffMapper = session.getMapper(StaffMapper.class);
            UserMapper userMapper = session.getMapper(UserMapper.class);

            // 检查卡号是否已存在
            if (userMapper.isCardNumExists(cardNum) > 0) {
                throw new Exception("卡号已存在: " + cardNum);
            }

            // 检查工号是否已存在
            if (staffMapper.isStaffIdExists(staffId) > 0) {
                throw new Exception("工号已存在: " + staffId);
            }

            // 插入用户基本信息
            userMapper.insertUser(
                cardNum,
                "123456", // 默认密码
                (String) staffData.get("name"),
                convertToInteger(staffData.get("age")),
                (String) staffData.get("gender"),
                (String) staffData.get("phone"),
                "staff"
            );

            // 插入教师扩展信息
            staffMapper.insertStaffInfo(
                cardNum,
                staffId,
                (String) staffData.get("title"),
                (String) staffData.get("department")
            );

            session.commit();
            return true;
        }
    }

    public boolean updateStaff(Map<String, Object> staffData) throws Exception {
        // 验证教师信息
        validateStaffData(staffData);

        String cardNum = (String) staffData.get("cardNum");
        String staffId = (String) staffData.get("staffId");

        try (SqlSession session = sqlSessionFactory.openSession()) {
            StaffMapper staffMapper = session.getMapper(StaffMapper.class);
            UserMapper userMapper = session.getMapper(UserMapper.class);

            // 检查教师是否存在
            Map<String, Object> existingStaff = staffMapper.findByCardNumWithUserInfo(cardNum);
            if (existingStaff == null) {
                throw new Exception("教师不存在: " + cardNum);
            }

            // 检查工号是否被其他教师使用
            if (staffMapper.isStaffIdExistsExcludeCardNum(staffId, cardNum) > 0) {
                throw new Exception("工号已被其他教师使用: " + staffId);
            }

            // 更新用户基本信息
            userMapper.updateUser(
                cardNum,
                (String) staffData.get("name"),
                convertToInteger(staffData.get("age")),
                (String) staffData.get("gender"),
                (String) staffData.get("phone"),
                "staff"
            );

            // 更新教师扩展信息
            staffMapper.updateStaffInfo(
                cardNum,
                staffId,
                (String) staffData.get("title"),
                (String) staffData.get("department")
            );

            session.commit();
            return true;
        }
    }

    public boolean deleteStaff(String cardNum) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper userMapper = session.getMapper(UserMapper.class);
            
            // 删除用户（由于外键约束，会自动删除关联的教师记录）
            int rowsAffected = userMapper.deleteByCardNum(cardNum);
            session.commit();
            return rowsAffected > 0;
        }
    }

    // 辅助方法
    private void validateStudentData(Map<String, Object> studentData) throws Exception {
        if (studentData == null) {
            throw new Exception("学生信息不能为空");
        }

        String cardNum = (String) studentData.get("cardNum");
        String studentId = (String) studentData.get("studentId");
        String name = (String) studentData.get("name");

        if (cardNum == null || cardNum.trim().isEmpty()) {
            throw new Exception("卡号不能为空");
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new Exception("学号不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("姓名不能为空");
        }
    }

    private void validateStaffData(Map<String, Object> staffData) throws Exception {
        if (staffData == null) {
            throw new Exception("教师信息不能为空");
        }

        String cardNum = (String) staffData.get("cardNum");
        String staffId = (String) staffData.get("staffId");
        String name = (String) staffData.get("name");

        if (cardNum == null || cardNum.trim().isEmpty()) {
            throw new Exception("卡号不能为空");
        }
        if (staffId == null || staffId.trim().isEmpty()) {
            throw new Exception("工号不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("姓名不能为空");
        }
    }

    private Integer convertToInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}