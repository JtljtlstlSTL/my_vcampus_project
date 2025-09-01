package com.vcampus.server.controller;

import com.vcampus.common.entity.User;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.server.service.UserService;
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
            // 1. 参数验证
            String cardNum = request.getParam("cardNum");
            String password = request.getParam("password");
            
            if (cardNum == null || password == null || password.trim().isEmpty()) {
                return Response.Builder.badRequest("卡号和密码不能为空");
            }
            
            // 2. 调用用户服务验证
            User user = UserService.authenticateUser(cardNum, password);
            
            if (user != null) {
                // 登录成功，创建会话
                Session session = new Session(
                    user.getCardNum(),
                    user.getName(),
                    user.getUserType()
                );
                
                // 用户信息
                Map<String, Object> userData = new HashMap<>();
                userData.put("cardNum", user.getCardNum());
                userData.put("name", user.getName());
                userData.put("gender", user.getGender() != null ? user.getGender().name() : "UNSPECIFIED");
                userData.put("phone", user.getPhone());
                userData.put("userType", user.getUserType());
                
                System.out.println("用户登录成功: " + user.getName() + " (" + user.getUserType() + ")");
                
                return Response.Builder.success("登录成功", userData)
                    .withSession(session);
                
            } else {
                // 登录失败
                return Response.Builder.error("用户名或密码错误");
            }
            
        } catch (Exception e) {
            System.err.println("登录处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
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
