package com.vcampus.server.core.auth.controller;

import com.vcampus.common.entity.base.User;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.server.core.auth.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器 - 处理客户端登录请求
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class LoginController {
    
    /**
     * 处理用户登录请求
     * 
     * @param request 登录请求
     * @return 登录响应结果
     */
    public static Response handleLogin(Request request) {
        System.out.println("收到登录请求: " + request.getUri());
        
        try {
            // 1. 参数获取 (兼容 username -> cardNum)
            String cardNum = request.getParam("cardNum");
            if (cardNum == null || cardNum.isBlank()) {
                String username = request.getParam("username");
                if (username != null && !username.isBlank()) {
                    cardNum = username; // 兼容旧客户端用 username 传 cardNum 或工号
                    log.info("[LOGIN] 使用 username 兼容映射为 cardNum='{}'", cardNum);
                }
            }
            String password = request.getParam("password");
            if (cardNum == null || cardNum.isBlank() || password == null || password.trim().isEmpty()) {
                return Response.Builder.badRequest("卡号/用户名 和密码不能为空");
            }
            
            // 2. 认证
            User user = UserService.authenticateUser(cardNum, password);
            if (user == null) {
                log.warn("[LOGIN] 认证失败 cardNum={}", cardNum);
                return Response.Builder.error("用户名或密码错误");
            }

            // 3. 角色规范化
            String rawType = user.getUserType();
            String role = (rawType == null || rawType.isBlank()) ? "student" : rawType.trim().toLowerCase();
            // teacher 与 staff 互通；为 teacher 添加 staff 角色，便于只注册 staff 路由
            String[] roles;
            switch (role) {
                case "teacher":
                    roles = new String[]{"teacher", "staff"};
                    break;
                case "staff":
                    roles = new String[]{"staff"};
                    break;
                case "admin":
                    roles = new String[]{"admin"};
                    break;
                default:
                    roles = new String[]{role};
            }
            Session session = new Session(user.getCardNum(), user.getName(), roles);

            // 4. 用户信息
            Map<String, Object> userData = new HashMap<>();
            userData.put("cardNum", user.getCardNum());
            userData.put("name", user.getName());
            userData.put("gender", user.getGender() != null ? user.getGender().name() : "UNSPECIFIED");
            userData.put("phone", user.getPhone());
            userData.put("userType", role);
            // 兼容前端旧逻辑：添加 role / primaryRole 字段，避免 getUserUri 回退 student
            userData.put("role", role);
            userData.put("primaryRole", role);

            log.info("[LOGIN] 成功 cardNum={} role(s)={}", user.getCardNum(), java.util.Arrays.toString(roles));

            return Response.Builder.success("登录成功", userData)
                .withSession(session);

        } catch (Exception e) {
            log.error("[LOGIN] 异常", e);
            return Response.Builder.internalError("系统错误，请稍后重试");
        }
    }
    
    /**
     * 生成会话ID
     */
    private static String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 验证会话
     */
    public static Map<String, Object> validateSession(String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        // 暂时简单验证，后续会实现真实的会话管理
        if (sessionId != null && sessionId.startsWith("session_")) {
            response.put("success", true);
            response.put("valid", true);
        } else {
            response.put("success", true);
            response.put("valid", false);
            response.put("message", "会话无效");
        }
        
        return response;
    }
}
