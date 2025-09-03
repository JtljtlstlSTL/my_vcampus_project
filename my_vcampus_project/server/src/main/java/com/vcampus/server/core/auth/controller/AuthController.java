package com.vcampus.server.core.auth.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.common.util.StringUtils;
import com.vcampus.common.util.security.PasswordUtils;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.common.entity.base.User;
import com.vcampus.server.core.auth.service.UserService;
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
        log.info("User login request");

        String username = request.getParam("username");
        String password = request.getParam("password");

        // 参数校验
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return Response.Builder.badRequest("Username and password cannot be empty");
        }

        // 用户验证
        User user = UserService.login(username, password);
        if (user == null) {
            log.warn("Login failed: {}", username);
            return Response.Builder.error("Incorrect username or password");
        }

        // 创建会话
        List<String> roles = user.getRoles();
        String[] rolesArray = roles.toArray(new String[0]);
        Session session = new Session(user.getCardNum().toString(), user.getName(), rolesArray);

        log.info("Login successful: {} [{}]", user.getName(), user.getPrimaryRole());

        // 准备响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("cardNum", user.getCardNum());
        data.put("userName", user.getName());
        data.put("roles", user.getRoles());
        data.put("primaryRole", user.getPrimaryRole());
        data.put("gender", user.getGender().getLabel());
        data.put("phone", user.getPhone());
        data.put("loginTime", System.currentTimeMillis());

        return Response.Builder.success("Login successful", data).withSession(session);
    }

    /**
     * 用户注销
     */
    @RouteMapping(uri = "auth/logout", description = "用户注销")
    public Response logout(Request request) {
        log.info("User logout request");

        Session currentSession = request.getSession();
        if (currentSession == null || !currentSession.isActive()) {
            return Response.Builder.error("Not logged in or session expired");
        }

        String userName = currentSession.getUserName();

        // 使会话失效
        currentSession.invalidate();

        log.info("Logout successful: {}", userName);

        return Response.Builder.success("Logout successful").withSession(currentSession);
    }

    /**
     * 获取当前用户信息
     */
    @RouteMapping(uri = "auth/userinfo", description = "获取当前用户信息")
    public Response getCurrentUserInfo(Request request) {
        log.debug("Get user info request");

        Session session = request.getSession();
        if (session == null || !session.isActive()) {
            return Response.Builder.forbidden("Please login first");
        }

        // 准备用户信息
        Map<String, Object> data = new HashMap<>();
        data.put("userId", session.getUserId());
        data.put("userName", session.getUserName());
        data.put("roles", session.getRoles());
        data.put("createTime", session.getCreateTime());
        data.put("lastAccessTime", session.getLastAccessTime());

        return Response.Builder.success("User info retrieved", data);
    }

    /**
     * 修改密码 - 允许所有已登录用户修改密码
     */
    @RouteMapping(uri = "auth/changepassword", description = "修改密码")
    public Response changePassword(Request request) {
        log.info("Password change request");

        Session session = request.getSession();
        if (session == null || !session.isActive()) {
            return Response.Builder.forbidden("Please login first");
        }

        String oldPassword = request.getParam("oldPassword");
        String newPassword = request.getParam("newPassword");

        // 参数校验
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
            return Response.Builder.badRequest("Old password and new password cannot be empty");
        }

        // 获取卡号（优先使用请求参数，如果没有则使用会话中的用户ID）
        String cardNum = request.getParam("cardNum");
        if (StringUtils.isBlank(cardNum)) {
            cardNum = session.getUserId();
        }
        
        // 验证旧密码
        User user = UserService.getUserByCardNum(cardNum);
        if (user == null) {
            return Response.Builder.error("User not found");
        }

        // 使用PasswordUtils验证旧密码
        if (!PasswordUtils.verifyPassword(oldPassword, user.getPassword())) {
            return Response.Builder.error("Incorrect old password");
        }

        // 更新密码
        boolean success = UserService.changePassword(cardNum, oldPassword, newPassword);
        if (!success) {
            return Response.Builder.error("Password update failed");
        }

        log.info("Password change successful: {}", session.getUserName());

        return Response.Builder.success("Password changed successfully");
    }
}
