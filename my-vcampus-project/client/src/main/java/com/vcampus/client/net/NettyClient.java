package com.vcampus.client.net;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.util.JsonUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

/**
 * Netty客户端
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class NettyClient {
    
    private final String host;
    private final int port;
    private EventLoopGroup group;
    private Channel channel;
    private boolean connected = false;
    
    // 请求-响应映射
    private final ConcurrentHashMap<UUID, CompletableFuture<Response>> pendingRequests = new ConcurrentHashMap<>();
    
    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    /**
     * 连接到服务器
     * 
     * @return 是否连接成功
     */
    public boolean connect() {
        if (connected) {
            return true;
        }
        
        group = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                    .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                    .addLast(new ClientHandler());
                        }
                    });
            
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
            connected = true;
            
            log.info("🔗 已连接到服务器: {}:{}", host, port);
            return true;
            
        } catch (Exception e) {
            log.error("💥 连接服务器失败: {}:{}", host, port, e);
            disconnect();
            return false;
        }
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        connected = false;
        
        if (channel != null) {
            channel.close();
            channel = null;
        }
        
        if (group != null) {
            group.shutdownGracefully();
            group = null;
        }
        
        // 清理待处理的请求
        pendingRequests.forEach((id, future) -> {
            future.completeExceptionally(new RuntimeException("连接已断开"));
        });
        pendingRequests.clear();
        
        log.info("🔌 已断开与服务器的连接");
    }
    
    /**
     * 发送请求（异步）
     * 
     * @param request 请求对象
     * @return 响应的Future对象
     */
    public CompletableFuture<Response> sendRequestAsync(Request request) {
        if (!connected || channel == null) {
            CompletableFuture<Response> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("未连接到服务器"));
            return future;
        }
        
        UUID requestId = request.getId();
        CompletableFuture<Response> future = new CompletableFuture<>();
        
        // 设置超时
        future.orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((response, throwable) -> {
                    pendingRequests.remove(requestId);
                });
        
        pendingRequests.put(requestId, future);
        
        try {
            String jsonRequest = JsonUtils.toJson(request);
            channel.writeAndFlush(jsonRequest);
            log.debug("📤 发送请求: {} -> {}", request.getUri(), jsonRequest);
        } catch (Exception e) {
            pendingRequests.remove(requestId);
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * 发送请求（同步）
     * 
     * @param request 请求对象
     * @return 响应对象
     * @throws Exception 请求异常
     */
    public Response sendRequest(Request request) throws Exception {
        return sendRequestAsync(request).get(30, TimeUnit.SECONDS);
    }
    
    /**
     * 检查是否已连接
     * 
     * @return 是否已连接
     */
    public boolean isConnected() {
        return connected && channel != null && channel.isActive();
    }
    
    /**
     * 获取服务器地址
     * 
     * @return 服务器地址
     */
    public String getServerAddress() {
        return host + ":" + port;
    }
    
    /**
     * 客户端处理器
     */
    private class ClientHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String jsonResponse = (String) msg;
            log.debug("📨 收到响应: {}", jsonResponse);
            
            try {
                Response response = JsonUtils.fromJson(jsonResponse, Response.class);
                if (response != null && response.getId() != null) {
                    CompletableFuture<Response> future = pendingRequests.remove(response.getId());
                    if (future != null) {
                        future.complete(response);
                    } else {
                        log.warn("⚠️ 收到未知请求ID的响应: {}", response.getId());
                    }
                } else {
                    log.warn("⚠️ 收到无效响应: {}", jsonResponse);
                }
            } catch (Exception e) {
                log.error("💥 处理响应异常: {}", jsonResponse, e);
            }
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.warn("🔌 与服务器的连接已断开");
            connected = false;
            
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
}
