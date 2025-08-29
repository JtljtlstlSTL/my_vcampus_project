package com.vcampus.server.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.common.util.StringUtils;
import com.vcampus.server.annotation.RouteMapping;
import com.vcampus.common.entity.User;
import com.vcampus.server.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器 - 处理用户登录、注销等
 * 基于新的用户模型重构
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class AuthController {
    
    /**
     * 用户登录
     */
    @RouteMapping(uri = "auth/login", description = "用户登录")
    public Response login(Request request) {
        log.info("🔐 用户登录请求");
        
        String username = request.getParam("username");
        String password = request.getParam("password");
        
        // 参数校验
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return Response.Builder.badRequest("用户名和密码不能为空");
        }
        
        // 用户验证
        User user = UserService.login(username, password);
        if (user == null) {
            log.warn("🚫 登录失败: {}", username);
            return Response.Builder.error("用户名或密码错误");
        }
        
        // 创建会话
        Session session = new Session(user.getCardNum().toString(), user.getName(), user.getRoles());
        
        log.info("✅ 登录成功: {} [{}]", user.getName(), user.getPrimaryRole());
        
        // 准备响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("cardNum", user.getCardNum());
        data.put("userName", user.getName());
        data.put("roles", user.getRoles());
        data.put("primaryRole", user.getPrimaryRole());
        data.put("gender", user.getGender().getLabel());
        // email 字段已移除
        data.put("phone", user.getPhone());
        data.put("loginTime", System.currentTimeMillis());
        
        return Response.Builder.success("登录成功", data).withSession(session);
    }
    
    /**
     * 用户注销
     */
    @RouteMapping(uri = "auth/logout", role = "student", description = "用户注销")
    public Response logout(Request request) {
        log.info("🚪 用户注销请求");
        
        Session currentSession = request.getSession();
        if (currentSession == null || !currentSession.isActive()) {
            return Response.Builder.error("未登录或会话已失效");
        }
        
        String userName = currentSession.getUserName();
        
        // 使会话失效
        currentSession.invalidate();
        
        log.info("✅ 注销成功: {}", userName);
        
        return Response.Builder.success("注销成功").withSession(currentSession);
    }
    
    /**
     * 获取当前用户信息
     */
    @RouteMapping(uri = "auth/userinfo", role = "student", description = "获取当前用户信息")
    public Response getCurrentUserInfo(Request request) {
        log.debug("👤 获取用户信息请求");
        
        Session session = request.getSession();
        if (session == null || !session.isActive()) {
            return Response.Builder.forbidden("请先登录");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", session.getUserId());
        data.put("userName", session.getUserName());
        data.put("roles", session.getRoles());
        data.put("loginTime", session.getCreateTime());
        data.put("lastAccessTime", session.getLastAccessTime());
        data.put("isActive", session.isActive());
        
        return Response.Builder.success("用户信息获取成功", data);
    }
    
    /**
     * 修改密码
     */
    @RouteMapping(uri = "auth/changePassword", role = "student", description = "修改密码")
    public Response changePassword(Request request) {
        log.info("🔑 修改密码请求");
        
        Session session = request.getSession();
        if (session == null || !session.isActive()) {
            return Response.Builder.forbidden("请先登录");
        }
        
        String oldPassword = request.getParam("oldPassword");
        String newPassword = request.getParam("newPassword");
        
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
            return Response.Builder.badRequest("旧密码和新密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            return Response.Builder.badRequest("新密码长度不能少于6位");
        }
        
        // 修改密码
        Integer cardNum = Integer.parseInt(session.getUserId());
        boolean success = UserService.changePassword(cardNum, oldPassword, newPassword);
        
        if (!success) {
            return Response.Builder.error("旧密码不正确或用户不存在");
        }
        
        log.info("✅ 密码修改成功: {}", session.getUserName());
        
        return Response.Builder.success("密码修改成功");
    }
    
    /**
     * 获取所有用户列表（管理员权限）
     */
    @RouteMapping(uri = "auth/users", role = "admin", description = "获取用户列表")
    public Response getUserList(Request request) {
        log.info("📋 获取用户列表请求");
        
        String role = request.getParam("role");
        String keyword = request.getParam("keyword");
        String pageStr = request.getParam("page");
        String sizeStr = request.getParam("size");
        
        List<User> users;
        if (StringUtils.isNotBlank(keyword)) {
            users = UserService.searchUsers(keyword);
        } else if (StringUtils.isNotBlank(role)) {
            users = UserService.getUsersByRole(role);
        } else {
            int page = StringUtils.isNotBlank(pageStr) ? Integer.parseInt(pageStr) : 1;
            int size = StringUtils.isNotBlank(sizeStr) ? Integer.parseInt(sizeStr) : 20;
            users = UserService.getAllUsers(page, size);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", users.size());
        data.put("users", users);
        data.put("stats", UserService.getUserStats());
        
        return Response.Builder.success("用户列表获取成功", data);
    }
}
