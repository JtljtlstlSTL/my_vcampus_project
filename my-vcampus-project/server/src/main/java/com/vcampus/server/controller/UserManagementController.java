package com.vcampus.server.controller;

import com.vcampus.common.entity.User;
import com.vcampus.common.enums.Gender;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.util.UserUtils;
import com.vcampus.server.annotation.RouteMapping;
import com.vcampus.server.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 用户管理控制器 - 管理员操作用户
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class UserManagementController {
    
    /**
     * 添加教师用户
     */
    @RouteMapping(uri = "admin/add_teacher", role = "admin")
    public Response addTeacher(Request request) {
        try {
            // 获取表单数据
            String name = request.getParams().get("name");
            String gender = request.getParams().get("gender");
            String phone = request.getParams().get("phone");
            String username = request.getParams().get("username"); // 登录用户名
            
            // 验证必填字段
            if (name == null || name.trim().isEmpty()) {
                return Response.Builder.error("姓名不能为空");
            }
            
            // 检查用户名是否可用
            if (username != null && !username.trim().isEmpty()) {
                if (!UserService.isUsernameAvailable(username)) {
                    return Response.Builder.error("用户名已存在：" + username);
                }
            }
            
            // 生成卡号
            Integer cardNum = UserService.generateCardNum("teacher");
            
            // 创建教师用户
            User newTeacher = new User(cardNum, "123456", name.trim(), 
                                      Gender.valueOf(gender), phone, 
                                      "teacher,library_user");
            
            // 保存用户
            boolean success = UserService.addUser(newTeacher, username);
            
            if (success) {
                log.info("管理员添加教师成功：{}", newTeacher.getCardNum());
                return Response.Builder.success("教师添加成功", UserUtils.sanitized(newTeacher));
            } else {
                return Response.Builder.error("添加失败");
            }
            
        } catch (Exception e) {
            log.error("添加教师失败", e);
            return Response.Builder.error("添加失败：" + e.getMessage());
        }
    }
    
    /**
     * 添加学生用户
     */
    @RouteMapping(uri = "admin/add_student", role = "admin")
    public Response addStudent(Request request) {
        try {
            // 获取表单数据
            String name = request.getParams().get("name");
            String gender = request.getParams().get("gender");
            String phone = request.getParams().get("phone");
            String username = request.getParams().get("username");
            
            // 验证必填字段
            if (name == null || name.trim().isEmpty()) {
                return Response.Builder.error("姓名不能为空");
            }
            
            // 检查用户名是否可用
            if (username != null && !username.trim().isEmpty()) {
                if (!UserService.isUsernameAvailable(username)) {
                    return Response.Builder.error("用户名已存在：" + username);
                }
            }
            
            // 生成卡号
            Integer cardNum = UserService.generateCardNum("student");
            
            // 创建学生用户
            User newStudent = new User(cardNum, "123456", name.trim(), 
                                      Gender.valueOf(gender), phone, 
                                      "student,library_user");
            
            // 保存用户
            boolean success = UserService.addUser(newStudent, username);
            
            if (success) {
                log.info("管理员添加学生成功：{}", newStudent.getCardNum());
                return Response.Builder.success("学生添加成功", UserUtils.sanitized(newStudent));
            } else {
                return Response.Builder.error("添加失败");
            }
            
        } catch (Exception e) {
            log.error("添加学生失败", e);
            return Response.Builder.error("添加失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户列表
     */
    @RouteMapping(uri = "admin/users", role = "admin")
    public Response getUserList(Request request) {
        try {
            String pageStr = request.getParams().getOrDefault("page", "0");
            String sizeStr = request.getParams().getOrDefault("size", "20");
            
            int page = Integer.parseInt(pageStr);
            int size = Integer.parseInt(sizeStr);
            
            List<User> users = UserService.getAllUsers(page, size);
            
            return Response.Builder.success("获取用户列表成功", users);
            
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Response.Builder.error("获取失败：" + e.getMessage());
        }
    }
    
    /**
     * 搜索用户
     */
    @RouteMapping(uri = "admin/search_users", role = "admin")
    public Response searchUsers(Request request) {
        try {
            String keyword = request.getParams().get("keyword");
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return Response.Builder.error("搜索关键词不能为空");
            }
            
            List<User> users = UserService.searchUsers(keyword.trim());
            
            return Response.Builder.success("搜索完成", users);
            
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            return Response.Builder.error("搜索失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除用户
     */
    @RouteMapping(uri = "admin/delete_user", role = "admin")
    public Response deleteUser(Request request) {
        try {
            String cardNumStr = request.getParams().get("cardNum");
            
            if (cardNumStr == null || cardNumStr.trim().isEmpty()) {
                return Response.Builder.error("用户卡号不能为空");
            }
            
            Integer cardNum = Integer.parseInt(cardNumStr);
            boolean success = UserService.deleteUser(cardNum);
            
            if (success) {
                log.info("管理员删除用户成功：{}", cardNum);
                return Response.Builder.success("用户删除成功");
            } else {
                return Response.Builder.error("删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Response.Builder.error("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户统计信息
     */
    @RouteMapping(uri = "admin/user_stats", role = "admin")
    public Response getUserStats(Request request) {
        try {
            return Response.Builder.success("获取统计信息成功", UserService.getUserStats());
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            return Response.Builder.error("获取失败：" + e.getMessage());
        }
    }
}
