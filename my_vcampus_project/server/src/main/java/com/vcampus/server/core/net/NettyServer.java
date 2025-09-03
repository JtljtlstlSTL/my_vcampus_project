package com.vcampus.server.core.net;

import com.vcampus.server.core.common.router.Router;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty服务器
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class NettyServer {
    
    private final int port;
    private final Router router;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    
    public NettyServer(int port, Router router) {
        this.port = port;
        this.router = router;
    }
    
    /**
     * 启动服务器
     */
    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                    .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                    .addLast(new ServerHandler(router));
                        }
                    });
            
            // 绑定端口并启动服务器
            channelFuture = bootstrap.bind(port).sync();
            log.info("VCampus server started successfully, listening on port: {}", port);
            log.info("Clients can connect to: localhost:{}", port);
            
            // 等待服务器socket关闭
            channelFuture.channel().closeFuture().sync();
            
        } catch (Exception e) {
            log.error("Server startup failed", e);
        } finally {
            shutdown();
        }
    }
    
    /**
     * 关闭服务器
     */
    public void shutdown() {
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        log.info("VCampus server shutdown completed");
    }
    
    /**
     * 检查服务器是否正在运行
     * 
     * @return 是否运行中
     */
    public boolean isRunning() {
        return channelFuture != null && channelFuture.channel().isActive();
    }
    
    /**
     * 获取服务器端口
     * 
     * @return 端口号
     */
    public int getPort() {
        return port;
    }
}
