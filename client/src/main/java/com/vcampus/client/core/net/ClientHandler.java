package com.vcampus.client.core.net;

import com.vcampus.common.message.Response;
import com.vcampus.common.util.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Socket客户端处理器
 * 处理来自服务器的响应
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {
    
    private final Map<String, CompletableFuture<Response>> pendingRequests;
    
    public ClientHandler(Map<String, CompletableFuture<Response>> pendingRequests) {
        this.pendingRequests = pendingRequests;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String jsonResponse = (String) msg;
        log.debug("📨 收到服务器响应: {}", jsonResponse);
        
        try {
            Response response = JsonUtils.fromJson(jsonResponse, Response.class);
            if (response != null && response.getId() != null) {
                String responseId = response.getId().toString();
                CompletableFuture<Response> future = pendingRequests.remove(responseId);
                
                if (future != null) {
                    log.debug("✅ 找到对应的请求，完成响应: {}", responseId);
                    future.complete(response);
                } else {
                    log.warn("⚠️ 收到未知请求ID的响应: {}", responseId);
                }
            } else {
                log.warn("⚠️ 收到无效响应: {}", jsonResponse);
            }
        } catch (Exception e) {
            log.error("💥 解析响应异常: {}", jsonResponse, e);
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("🔌 与服务器的连接已断开");
        
        // 通知所有待处理的请求
        pendingRequests.forEach((id, future) -> {
            future.completeExceptionally(new RuntimeException("连接已断开"));
        });
        pendingRequests.clear();
        
        super.channelInactive(ctx);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("🚨 客户端连接异常", cause);
        ctx.close();
    }
}
