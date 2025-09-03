package com.vcampus.client.core.net;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.common.util.JsonUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 基于Netty的客户端
 * 与服务端完全兼容的通信框架
 *
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class NettyClient {

    private final String host;
    private final int port;
    private final EventLoopGroup group;
    private Channel channel;
    private final Map<String, CompletableFuture<Response>> pendingRequests;
    private final Object lock = new Object();

    // 添加Session管理
    private Session currentSession;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.group = new NioEventLoopGroup();
        this.pendingRequests = new ConcurrentHashMap<>();
    }

    /**
     * 连接到服务器
     */
    public CompletableFuture<Boolean> connect() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                    .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                    .addLast(new ClientHandler(pendingRequests));
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(host, port);
            channelFuture.addListener((ChannelFutureListener) cf -> {
                if (cf.isSuccess()) {
                    channel = cf.channel();
                    log.info("✅ 成功连接到服务器: {}:{}", host, port);
                    future.complete(true);
                } else {
                    log.error("❌ 连接服务器失败: {}:{}", host, port, cf.cause());
                    future.completeExceptionally(cf.cause());
                }
            });

        } catch (Exception e) {
            log.error("💥 连接初始化失败", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * 发送请求并等待响应
     */
    public CompletableFuture<Response> sendRequest(Request request) {
        if (channel == null || !channel.isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("客户端未连接到服务器")
            );
        }

        CompletableFuture<Response> future = new CompletableFuture<>();
        String requestId = request.getId().toString();

        // 注册等待响应的请求
        pendingRequests.put(requestId, future);

        try {
            // 发送请求
            String jsonRequest = JsonUtils.toJson(request);
            channel.writeAndFlush(jsonRequest).addListener((ChannelFutureListener) cf -> {
                if (cf.isSuccess()) {
                    log.debug("📤 请求发送成功: {}", requestId);
                } else {
                    log.error("❌ 请求发送失败: {}", requestId, cf.cause());
                    pendingRequests.remove(requestId);
                    future.completeExceptionally(cf.cause());
                }
            });

            // 设置超时 - 增加到30秒以处理数据库查询
            future.orTimeout(30, TimeUnit.SECONDS).whenComplete((response, throwable) -> {
                if (throwable != null) {
                    pendingRequests.remove(requestId);
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        log.warn("⏰ 请求超时 (30秒): {} - 可能服务器响应缓慢或数据库连接问题", requestId);
                    }
                }
            });

        } catch (Exception e) {
            log.error("💥 发送请求异常: {}", requestId, e);
            pendingRequests.remove(requestId);
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        synchronized (lock) {
            if (channel != null) {
                channel.close();
                channel = null;
            }
            if (group != null) {
                group.shutdownGracefully();
            }
            pendingRequests.clear();
            log.info("🔌 客户端已断开连接");
        }
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    /**
     * 获取连接信息
     */
    public String getConnectionInfo() {
        return String.format("%s:%d", host, port);
    }

    /**
     * 设置当前用户会话
     */
    public void setCurrentSession(Session session) {
        this.currentSession = session;
        log.debug("🔐 设置用户会话: {}", session != null ? session.getUserId() : "null");
    }

    /**
     * 获取当前用户会话
     */
    public Session getCurrentSession() {
        return this.currentSession;
    }

    /**
     * 清除当前用户会话
     */
    public void clearSession() {
        this.currentSession = null;
        log.debug("🔓 清除用户会话");
    }
}
