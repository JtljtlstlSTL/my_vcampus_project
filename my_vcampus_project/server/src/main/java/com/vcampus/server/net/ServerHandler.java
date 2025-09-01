package com.vcampus.server.net;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.common.util.JsonUtils;
import com.vcampus.server.router.Router;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务器请求处理器
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {
    
    private final Router router;
    private Session clientSession;
    
    // 统计信息
    private static final AtomicLong totalRequests = new AtomicLong(0);
    private static final ConcurrentHashMap<String, AtomicLong> uriStats = new ConcurrentHashMap<>();
    
    public ServerHandler(Router router) {
        this.router = router;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String clientAddress = ctx.channel().remoteAddress().toString();
        log.info("🔗 客户端连接: {}", clientAddress);
        
        // 初始化会话
        clientSession = new Session();
        clientSession.setActive(true);
        
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientAddress = ctx.channel().remoteAddress().toString();
        log.info("🔌 客户端断开: {}", clientAddress);
        
        // 清理会话
        if (clientSession != null) {
            clientSession.invalidate();
        }
        
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String jsonMessage = (String) msg;
        String clientAddress = ctx.channel().remoteAddress().toString();
        
        try {
            // 更新统计
            totalRequests.incrementAndGet();
            
            log.debug("📨 收到请求 [{}]: {}", clientAddress, jsonMessage);
            
            // 解析请求
            Request request = JsonUtils.fromJson(jsonMessage, Request.class);
            if (request == null) {
                log.warn("⚠️ 无效的JSON请求: {}", jsonMessage);
                sendErrorResponse(ctx, "无效的JSON格式");
                return;
            }
            
            // 设置会话信息
            request.setSession(clientSession);
            
            // 更新会话最后访问时间
            if (clientSession != null) {
                clientSession.updateLastAccessTime();
            }
            
            // 更新URI统计
            String uri = request.getUri();
            uriStats.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
            
            log.info("🎯 处理请求: {} [{}]", uri, clientAddress);
            
            // 路由处理
            Response response = router.route(request);
            
            // 更新会话（如果有变化）
            if (response.getSession() != null) {
                clientSession = response.getSession();
                log.debug("🔄 会话已更新: {}", clientSession);
            }
            
            // 发送响应
            sendResponse(ctx, response);
            
            log.debug("✅ 请求处理完成: {} -> {}", uri, response.getStatus());
            
        } catch (Exception e) {
            log.error("💥 处理请求异常 [{}]: {}", clientAddress, jsonMessage, e);
            sendErrorResponse(ctx, "服务器内部错误: " + e.getMessage());
        }
    }
    
    /**
     * 发送响应
     * 
     * @param ctx 通道上下文
     * @param response 响应对象
     */
    private void sendResponse(ChannelHandlerContext ctx, Response response) {
        try {
            String jsonResponse = JsonUtils.toJson(response);
            // 确保发送完整的JSON字符串，添加换行符以便客户端正确读取
            String completeResponse = jsonResponse + "\n";
            ctx.writeAndFlush(completeResponse);
            log.debug("📤 发送响应: {}", jsonResponse);
        } catch (Exception e) {
            log.error("💥 发送响应失败", e);
        }
    }
    
    /**
     * 发送错误响应
     * 
     * @param ctx 通道上下文
     * @param errorMessage 错误消息
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, String errorMessage) {
        Response errorResponse = Response.Builder.error(errorMessage);
        sendResponse(ctx, errorResponse);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String clientAddress = ctx.channel().remoteAddress().toString();
        log.error("🚨 连接异常 [{}]", clientAddress, cause);
        
        // 发送错误响应
        sendErrorResponse(ctx, "连接异常: " + cause.getMessage());
        
        // 关闭连接
        ctx.close();
    }
    
    /**
     * 获取服务器统计信息
     * 
     * @return 统计信息
     */
    public static String getServerStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("📊 服务器统计信息:\\n");
        stats.append(String.format("总请求数: %d\\n", totalRequests.get()));
        stats.append("URI请求统计:\\n");
        
        uriStats.forEach((uri, count) -> {
            stats.append(String.format("  %s: %d\\n", uri, count.get()));
        });
        
        return stats.toString();
    }
    
    /**
     * 重置统计信息
     */
    public static void resetStats() {
        totalRequests.set(0);
        uriStats.clear();
        log.info("📊 服务器统计信息已重置");
    }
}
