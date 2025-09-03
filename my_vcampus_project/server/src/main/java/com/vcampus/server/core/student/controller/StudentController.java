package com.vcampus.server.core.student.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.common.util.StringUtils;
import com.vcampus.server.core.common.annotation.RouteMapping;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 学生控制器 - 处理学生相关业务
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class StudentController {
    
    // 模拟学生数据库
    private static final Map<String, StudentInfo> studentDatabase = new ConcurrentHashMap<>();
    
    // 初始化测试数据
    static {
        studentDatabase.put("student001", new StudentInfo(
                "student001", "张三", "男", 20, "计算机科学与技术", "2021", "13800138001"));
        studentDatabase.put("student002", new StudentInfo(
                "student002", "李四", "女", 19, "软件工程", "2022", "13800138002"));
        studentDatabase.put("student003", new StudentInfo(
                "student003", "王五", "男", 21, "信息安全", "2020", "13800138003"));
        studentDatabase.put("student004", new StudentInfo(
                "student004", "赵六", "女", 18, "数据科学与大数据技术", "2023", "13800138004"));
    }
    
    /**
     * 获取学生个人信息
     */
    @RouteMapping(uri = "student/info", role = "student", description = "获取学生个人信息")
    public Response getStudentInfo(Request request) {
        log.info("👨‍🎓 获取学生信息请求");
        
        Session session = request.getSession();
        String studentId = session.getUserId();
        
        StudentInfo student = studentDatabase.get(studentId);
        if (student == null) {
            return Response.Builder.notFound("学生信息不存在");
        }
        
        return Response.Builder.success("学生信息获取成功", student.toMap());
    }
    
    /**
     * 更新学生个人信息
     */
    @RouteMapping(uri = "student/update", role = "student", description = "更新学生个人信息")
    public Response updateStudentInfo(Request request) {
        log.info("✏️ 更新学生信息请求");
        
        Session session = request.getSession();
        String studentId = session.getUserId();
        
        StudentInfo student = studentDatabase.get(studentId);
        if (student == null) {
            return Response.Builder.notFound("学生信息不存在");
        }
        
        // 更新允许修改的字段
        String phone = request.getParam("phone");
        if (StringUtils.isNotBlank(phone)) {
            if (!isValidPhone(phone)) {
                return Response.Builder.badRequest("手机号格式不正确");
            }
            student.phone = phone;
        }
        
        // 在真实项目中，某些字段可能不允许学生自己修改
        // 这里为了演示，允许修改部分信息
        
        log.info("✅ 学生信息更新成功: {}", student.name);
        
        return Response.Builder.success("学生信息更新成功", student.toMap());
    }
    
    /**
     * 获取所有学生列表（教师和管理员权限）
     */
    @RouteMapping(uri = "student/list", role = "teacher", description = "获取学生列表")
    public Response getStudentList(Request request) {
        log.info("📋 获取学生列表请求");
        
        String keyword = request.getParam("keyword");
        String major = request.getParam("major");
        String grade = request.getParam("grade");
        
        List<StudentInfo> students = new ArrayList<>(studentDatabase.values());
        
        // 关键字搜索
        if (StringUtils.isNotBlank(keyword)) {
            students = students.stream()
                    .filter(s -> s.name.contains(keyword) || 
                               s.studentId.contains(keyword) ||
                               s.major.contains(keyword))
                    .collect(Collectors.toList());
        }
        
        // 专业筛选
        if (StringUtils.isNotBlank(major)) {
            students = students.stream()
                    .filter(s -> s.major.equals(major))
                    .collect(Collectors.toList());
        }
        
        // 年级筛选
        if (StringUtils.isNotBlank(grade)) {
            students = students.stream()
                    .filter(s -> s.grade.equals(grade))
                    .collect(Collectors.toList());
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", students.size());
        data.put("students", students.stream().map(StudentInfo::toMap).collect(Collectors.toList()));
        
        // 统计信息
        Map<String, Long> majorStats = students.stream()
                .collect(Collectors.groupingBy(s -> s.major, Collectors.counting()));
        data.put("majorStats", majorStats);
        
        return Response.Builder.success("学生列表获取成功", data);
    }
    
    /**
     * 添加学生（管理员权限）
     */
    @RouteMapping(uri = "student/add", role = "admin", description = "添加学生")
    public Response addStudent(Request request) {
        log.info("➕ 添加学生请求");
        
        String studentId = request.getParam("studentId");
        String name = request.getParam("name");
        String gender = request.getParam("gender");
        String ageStr = request.getParam("age");
        String major = request.getParam("major");
        String grade = request.getParam("grade");
        String phone = request.getParam("phone");
        
        // 参数校验
        if (StringUtils.isBlank(studentId) || StringUtils.isBlank(name)) {
            return Response.Builder.badRequest("学号和姓名不能为空");
        }
        
        if (studentDatabase.containsKey(studentId)) {
            return Response.Builder.error("学号已存在");
        }
        
        int age = 18;
        if (StringUtils.isNotBlank(ageStr)) {
            try {
                age = Integer.parseInt(ageStr);
                if (age < 15 || age > 30) {
                    return Response.Builder.badRequest("年龄范围应在15-30之间");
                }
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("年龄格式不正确");
            }
        }
        
        if (StringUtils.isNotBlank(phone) && !isValidPhone(phone)) {
            return Response.Builder.badRequest("手机号格式不正确");
        }
        
        // 创建学生信息
        StudentInfo student = new StudentInfo(
                studentId,
                StringUtils.defaultIfBlank(name, ""),
                StringUtils.defaultIfBlank(gender, "未知"),
                age,
                StringUtils.defaultIfBlank(major, ""),
                StringUtils.defaultIfBlank(grade, ""),
                StringUtils.defaultIfBlank(phone, "")
        );
        
        studentDatabase.put(studentId, student);
        
        log.info("✅ 学生添加成功: {} [{}]", student.name, student.studentId);
        
        return Response.Builder.success("学生添加成功", student.toMap());
    }
    
    /**
     * 删除学生（管理员权限）
     */
    @RouteMapping(uri = "student/delete", role = "admin", description = "删除学生")
    public Response deleteStudent(Request request) {
        log.info("🗑️ 删除学生请求");
        
        String studentId = request.getParam("studentId");
        if (StringUtils.isBlank(studentId)) {
            return Response.Builder.badRequest("学号不能为空");
        }
        
        StudentInfo student = studentDatabase.remove(studentId);
        if (student == null) {
            return Response.Builder.notFound("学生不存在");
        }
        
        log.info("✅ 学生删除成功: {} [{}]", student.name, student.studentId);
        
        return Response.Builder.success("学生删除成功");
    }
    
    /**
     * 获取专业统计信息
     */
    @RouteMapping(uri = "student/stats", role = "teacher", description = "获取学生统计信息")
    public Response getStudentStats(Request request) {
        log.info("📊 获取学生统计信息请求");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总人数
        stats.put("totalStudents", studentDatabase.size());
        
        // 专业统计
        Map<String, Long> majorStats = studentDatabase.values().stream()
                .collect(Collectors.groupingBy(s -> s.major, Collectors.counting()));
        stats.put("majorStats", majorStats);
        
        // 年级统计
        Map<String, Long> gradeStats = studentDatabase.values().stream()
                .collect(Collectors.groupingBy(s -> s.grade, Collectors.counting()));
        stats.put("gradeStats", gradeStats);
        
        // 性别统计
        Map<String, Long> genderStats = studentDatabase.values().stream()
                .collect(Collectors.groupingBy(s -> s.gender, Collectors.counting()));
        stats.put("genderStats", genderStats);
        
        // 年龄统计
        OptionalDouble avgAge = studentDatabase.values().stream()
                .mapToInt(s -> s.age)
                .average();
        stats.put("averageAge", avgAge.isPresent() ? avgAge.getAsDouble() : 0);
        
        return Response.Builder.success("统计信息获取成功", stats);
    }
    
    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }
    
    /**
     * 学生信息内部类
     */
    private static class StudentInfo {
        String studentId;
        String name;
        String gender;
        int age;
        String major;
        String grade;
        String phone;
        
        StudentInfo(String studentId, String name, String gender, int age, 
                   String major, String grade, String phone) {
            this.studentId = studentId;
            this.name = name;
            this.gender = gender;
            this.age = age;
            this.major = major;
            this.grade = grade;
            this.phone = phone;
        }
        
        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", studentId);
            map.put("name", name);
            map.put("gender", gender);
            map.put("age", age);
            map.put("major", major);
            map.put("grade", grade);
            map.put("phone", phone);
            return map;
        }
    }
}
