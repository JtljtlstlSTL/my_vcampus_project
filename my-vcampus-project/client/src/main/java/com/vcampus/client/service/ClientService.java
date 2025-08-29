package com.vcampus.client.service;

import com.vcampus.client.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 客户端服务 - 提供高级API接口
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class ClientService {
    
    private final NettyClient client;
    private Session currentSession;
    
    public ClientService(String host, int port) {
        this.client = new NettyClient(host, port);
    }
    
    /**
     * 连接到服务器
     * 
     * @return 是否连接成功
     */
    public boolean connect() {
        return client.connect();
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        client.disconnect();
        currentSession = null;
    }
    
    /**
     * 检查是否已连接
     * 
     * @return 是否已连接
     */
    public boolean isConnected() {
        return client.isConnected();
    }
    
    /**
     * 发送请求（同步）
     * 
     * @param uri 请求URI
     * @param params 请求参数
     * @return 响应对象
     */
    public Response sendRequest(String uri, Map<String, String> params) {
        try {
            Request request = new Request(uri, params);
            request.setSession(currentSession);
            
            Response response = client.sendRequest(request);
            
            // 更新会话
            if (response.getSession() != null) {
                currentSession = response.getSession();
                log.debug("🔄 会话已更新: {}", currentSession);
            }
            
            return response;
        } catch (Exception e) {
            log.error("💥 发送请求失败: {}", uri, e);
            return Response.Builder.error("请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送请求（异步）
     * 
     * @param uri 请求URI
     * @param params 请求参数
     * @return 响应的Future对象
     */
    public CompletableFuture<Response> sendRequestAsync(String uri, Map<String, String> params) {
        Request request = new Request(uri, params);
        request.setSession(currentSession);
        
        return client.sendRequestAsync(request)
                .thenApply(response -> {
                    // 更新会话
                    if (response.getSession() != null) {
                        currentSession = response.getSession();
                        log.debug("🔄 会话已更新: {}", currentSession);
                    }
                    return response;
                });
    }
    
    /**
     * 发送简单请求（无参数）
     * 
     * @param uri 请求URI
     * @return 响应对象
     */
    public Response sendRequest(String uri) {
        return sendRequest(uri, null);
    }
    
    /**
     * 心跳检测
     * 
     * @return 是否成功
     */
    public boolean heartbeat() {
        try {
            Response response = sendRequest("system/heartbeat");
            return response.isSuccess();
        } catch (Exception e) {
            log.warn("💗 心跳检测失败", e);
            return false;
        }
    }
    
    /**
     * 获取服务器信息
     * 
     * @return 服务器信息
     */
    public Response getServerInfo() {
        return sendRequest("system/info");
    }
    
    /**
     * 获取服务器时间
     * 
     * @return 服务器时间
     */
    public Response getServerTime() {
        return sendRequest("system/time");
    }
    
    /**
     * 回显测试
     * 
     * @param params 测试参数
     * @return 回显结果
     */
    public Response echo(Map<String, String> params) {
        return sendRequest("system/echo", params);
    }
    
    /**
     * 获取当前会话
     * 
     * @return 当前会话
     */
    public Session getCurrentSession() {
        return currentSession;
    }
    
    /**
     * 设置当前会话
     * 
     * @param session 会话对象
     */
    public void setCurrentSession(Session session) {
        this.currentSession = session;
    }
    
    /**
     * 检查是否已登录
     * 
     * @return 是否已登录
     */
    public boolean isLoggedIn() {
        return currentSession != null && currentSession.isActive();
    }
    
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID
     */
    public String getCurrentUserId() {
        return currentSession != null ? currentSession.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名
     */
    public String getCurrentUserName() {
        return currentSession != null ? currentSession.getUserName() : null;
    }
    
    /**
     * 检查当前用户是否有指定权限
     * 
     * @param role 角色
     * @return 是否有权限
     */
    public boolean hasPermission(String role) {
        return currentSession != null && currentSession.hasPermission(role);
    }
    
    /**
     * 获取服务器地址
     * 
     * @return 服务器地址
     */
    public String getServerAddress() {
        return client.getServerAddress();
    }
}
