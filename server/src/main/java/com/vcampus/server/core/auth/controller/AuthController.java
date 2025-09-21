package com.vcampus.server.core.auth.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.common.util.StringUtils;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.common.entity.base.User;
import com.vcampus.server.core.auth.service.UserService;
import com.vcampus.server.core.card.dao.CardDao;
import com.vcampus.server.core.card.entity.Card;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
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
        // 兼容：如果未传 username 则尝试 cardNum
        if ((username == null || username.isBlank())) {
            String cardNumParam = request.getParam("cardNum");
            if (cardNumParam != null && !cardNumParam.isBlank()) {
                username = cardNumParam;
                log.info("[LOGIN] 使用 cardNum 参数作为用户名: {}", username);
            }
        }

        // 参数校验
        if (com.vcampus.common.util.StringUtils.isBlank(username) || com.vcampus.common.util.StringUtils.isBlank(password)) {
            return Response.Builder.badRequest("Username and password cannot be empty");
        }

        // 用户验证
        User user;
        try {
            user = UserService.login(username, password);
        } catch (RuntimeException ex) {
            log.warn("Login failed: {} reason={}", username, ex.getMessage());
            return Response.Builder.error(ex.getMessage());
        }
        if (user == null) {
            log.warn("Login failed: {}", username);
            return Response.Builder.error("Incorrect username or password");
        }

        // 自动确保该用户有一条 tblCard 记录 (保持原有逻辑)
        try {
            String cardNum = user.getCardNum() == null ? null : user.getCardNum().toString();
            if (cardNum != null) {
                CardDao cardDao = CardDao.getInstance();
                boolean exists = cardDao.existsByCardNum(cardNum);
                if (!exists) {
                    Card c = new Card();
                    c.setCardNum(cardNum);
                    c.setBalance(new java.math.BigDecimal("0.00"));
                    c.setStatus("正常");
                    cardDao.save(c);
                    log.info("Created initial tblCard record for user {} with balance=0.00", cardNum);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to ensure tblCard record on login for {}: {}", user.getCardNum(), e.getMessage());
        }

        // 创建会话
        java.util.List<String> roles = user.getRoles();
        String primaryRole = user.getPrimaryRole();
        String[] rolesArray = roles.toArray(new String[0]);
        Session session = new Session(user.getCardNum().toString(), user.getName(), rolesArray);

        log.info("Login successful: {} [{}] roles={}", user.getName(), primaryRole, rolesArray);

        // 响应数据
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("cardNum", user.getCardNum());
        data.put("userName", user.getName());
        data.put("roles", roles);
        data.put("primaryRole", primaryRole);
        data.put("gender", user.getGender() == null ? null : user.getGender().getLabel());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar()); // 添加头像信息
        data.put("loginTime", System.currentTimeMillis());
        // 兼容前端 getUserUri 判定：补充 role 字段
        data.put("role", primaryRole);

        // 如果是学生，获取学号信息
        if ("student".equals(primaryRole)) {
            try {
                com.vcampus.server.core.academic.service.AcademicService academicService = 
                    new com.vcampus.server.core.academic.service.AcademicService();
                java.util.Map<String, Object> studentInfo = academicService.getStudentByCardNum(user.getCardNum().toString());
                if (studentInfo != null && studentInfo.get("studentId") != null) {
                    data.put("student_Id", studentInfo.get("studentId"));
                    log.info("学生登录，获取学号: {}", studentInfo.get("studentId"));
                }
            } catch (Exception e) {
                log.warn("获取学生学号失败: {}", e.getMessage());
            }
        }

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

        // 使用UserService修改密码
        boolean success = UserService.changePassword(cardNum, oldPassword, newPassword);

        if (success) {
            log.info("Password changed successfully for user: {}", cardNum);
            return Response.Builder.success("Password changed successfully");
        } else {
            log.warn("Password change failed for user: {}", cardNum);
            return Response.Builder.error("Failed to change password. Please check your old password.");
        }
    }

    /**
     * 更新用户头像
     */
    @RouteMapping(uri = "auth/updateAvatar", description = "更新用户头像")
    public Response updateAvatar(Request request) {
        log.info("Update avatar request");

        Session session = request.getSession();
        if (session == null || !session.isActive()) {
            return Response.Builder.forbidden("Please login first");
        }

        String cardNum = request.getParam("cardNum");
        String avatarUrl = request.getParam("avatar");

        // 参数校验
        if (StringUtils.isBlank(cardNum)) {
            return Response.Builder.badRequest("Card number cannot be empty");
        }

        // 权限检查：用户只能修改自己的头像，或者管理员可以修改任何用户的头像
        String sessionCardNum = session.getUserId();
        String[] roles = session.getRoles();
        boolean isAdmin = roles != null && java.util.Arrays.asList(roles).contains("admin");

        if (!isAdmin && !cardNum.equals(sessionCardNum)) {
            log.warn("User {} attempted to update avatar for user {}", sessionCardNum, cardNum);
            return Response.Builder.forbidden("You can only update your own avatar");
        }

        // 验证用户是否存在
        User user = UserService.getUserByCardNum(cardNum);
        if (user == null) {
            return Response.Builder.error("User not found");
        }

        // 如果avatarUrl为空或者是空字符串，表示恢复默认头像
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            // 根据用户角色和性别设置默认头像
            String userType = user.getUserType();
            String genderLabel = user.getGender() != null ? user.getGender().getLabel() : "男";

            avatarUrl = getDefaultAvatarPath(userType, genderLabel);
            log.info("Setting default avatar for user {}: {}", cardNum, avatarUrl);
        }

        // 更新头像
        boolean success = UserService.updateUserAvatar(cardNum, avatarUrl);

        if (success) {
            log.info("Avatar updated successfully for user: {} -> {}", cardNum, avatarUrl);

            Map<String, Object> data = new HashMap<>();
            data.put("cardNum", cardNum);
            data.put("avatarUrl", avatarUrl);

            return Response.Builder.success("Avatar updated successfully", data);
        } else {
            log.warn("Avatar update failed for user: {}", cardNum);
            return Response.Builder.error("Failed to update avatar");
        }
    }

    /**
     * 根据卡号获取管理员信息
     */
    @RouteMapping(uri = "auth/getAdminByCardNum", description = "根据卡号获取管理员信息")
    public Response getAdminByCardNum(Request request) {
        log.info("Get admin by card number request");

        Session session = request.getSession();
        if (session == null || !session.isActive()) {
            return Response.Builder.forbidden("Please login first");
        }

        String cardNum = request.getParam("cardNum");
        if (StringUtils.isBlank(cardNum)) {
            return Response.Builder.badRequest("Card number cannot be empty");
        }

        // 权限检查：用户只能查看自己的信息，或者管理员可以查看任何用户的信息
        String sessionCardNum = session.getUserId();
        String[] roles = session.getRoles();
        boolean isAdmin = roles != null && java.util.Arrays.asList(roles).contains("admin");

        if (!isAdmin && !cardNum.equals(sessionCardNum)) {
            log.warn("User {} attempted to get info for user {}", sessionCardNum, cardNum);
            return Response.Builder.forbidden("You can only view your own information");
        }

        // 获取用户信息
        User user = UserService.getUserByCardNum(cardNum);
        if (user == null) {
            return Response.Builder.error("User not found");
        }

        // 检查用户是否为管理员
        String userType = user.getUserType();
        if (!"admin".equals(userType) && !"manager".equals(userType)) {
            return Response.Builder.error("User is not an administrator");
        }

        // 准备管理员信息
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("cardNum", user.getCardNum());
        adminData.put("name", user.getName());
        adminData.put("gender", user.getGender() != null ? user.getGender().getLabel() : "男");
        adminData.put("phone", user.getPhone());
        adminData.put("primaryRole", user.getPrimaryRole());
        adminData.put("avatar", user.getAvatar());

        log.info("Admin info retrieved successfully for: {}", cardNum);
        return Response.Builder.success("Admin info retrieved successfully", adminData);
    }

    /**
     * 根据用户角色和性别获取默认头像路径
     */
    private String getDefaultAvatarPath(String userType, String gender) {
        String rolePrefix;

        // 根据用户类型确定角色前缀
        switch (userType != null ? userType.toLowerCase() : "") {
            case "student":
                rolePrefix = "student";
                break;
            case "staff":
            case "teacher":
                rolePrefix = "teacher";
                break;
            case "manager":
            case "admin":
                rolePrefix = "admin";
                break;
            default:
                rolePrefix = "student"; // 默认为学生
        }

        // 根据性别确定后缀
        String genderSuffix = "女".equals(gender) ? "female" : "male";

        return String.format("/avatars/default_%s_%s.png", rolePrefix, genderSuffix);
    }

    /**
     * 更新管理员信息
     */
    @RouteMapping(uri = "auth/updateAdminInfo", description = "更新管理员信息")
    public Response updateAdminInfo(Request request) {
        log.info("Update admin info request");

        Session session = request.getSession();
        if (session == null || !session.isActive()) {
            return Response.Builder.forbidden("Please login first");
        }

        String cardNum = request.getParam("cardNum");
        if (StringUtils.isBlank(cardNum)) {
            return Response.Builder.badRequest("Card number cannot be empty");
        }

        // 权限检查：用户只能更新自己的信息，或者管理员可以更新任何用户的信息
        String sessionCardNum = session.getUserId();
        String[] roles = session.getRoles();
        boolean isAdmin = roles != null && java.util.Arrays.asList(roles).contains("admin");

        if (!isAdmin && !cardNum.equals(sessionCardNum)) {
            log.warn("User {} attempted to update info for user {}", sessionCardNum, cardNum);
            return Response.Builder.forbidden("You can only update your own information");
        }

        try {
            // 获取参数
            String name = request.getParam("name");
            String gender = request.getParam("gender");
            String phone = request.getParam("phone");

            // 验证参数
            if (StringUtils.isBlank(name)) {
                return Response.Builder.badRequest("Name cannot be empty");
            }

            // 更新用户信息
            boolean success = UserService.updateUserInfo(cardNum, name, gender, phone);
            
            if (success) {
                log.info("Admin info updated successfully for cardNum: {}", cardNum);
                return Response.Builder.success("Admin info updated successfully");
            } else {
                return Response.Builder.error("Failed to update admin info");
            }

        } catch (Exception e) {
            log.error("Error updating admin info", e);
            return Response.Builder.internalError("Error updating admin info: " + e.getMessage());
        }
    }
}
