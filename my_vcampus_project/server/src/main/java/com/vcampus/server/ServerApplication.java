package com.vcampus.server;

import com.vcampus.server.net.NettyServer;
import com.vcampus.server.router.Router;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * VCampus服务器应用程序入口
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class ServerApplication {
    
    private static final int DEFAULT_PORT = 8080;
    private static NettyServer server;
    private static Router router;
    
    public static void main(String[] args) {
        // 打印启动横幅
        printBanner();
        
        // 解析命令行参数
        int port = parsePort(args);
        
        // 初始化组件
        initializeComponents();
        
        // 注册关闭钩子
        registerShutdownHook();
        
        // 启动服务器
        startServer(port);
        
        // 启动控制台
        startConsole();
    }
    
    /**
     * 打印启动横幅
     */
    private static void printBanner() {
        System.out.println();
        System.out.println("VCampus Virtual Campus Server v1.0.0");
        System.out.println("https://github.com/vcampus-project");
        System.out.println();
    }
    
    /**
     * 解析端口号
     * 
     * @param args 命令行参数
     * @return 端口号
     */
    private static int parsePort(String[] args) {
        int port = DEFAULT_PORT;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) {
                    log.warn("Invalid port number: {}, using default port: {}", port, DEFAULT_PORT);
                    port = DEFAULT_PORT;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid port format: {}, using default port: {}", args[0], DEFAULT_PORT);
            }
        }
        
        return port;
    }
    
    /**
     * 初始化组件
     */
    private static void initializeComponents() {
        log.info("Initializing server components...");
        
        // 初始化路由器
        router = new Router();
        router.initialize("com.vcampus.server.controller");
        
        log.info("Server components initialization completed");
    }
    
    /**
     * 注册关闭钩子
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down server...");
            if (server != null) {
                server.shutdown();
            }
            log.info("Server shutdown completed, goodbye!");
        }));
    }
    
    /**
     * 启动服务器
     * 
     * @param port 端口号
     */
    private static void startServer(int port) {
        server = new NettyServer(port, router);
        
        // 在新线程中启动服务器
        Thread serverThread = new Thread(() -> {
            server.start();
        });
        serverThread.setDaemon(false);
        serverThread.start();
        
        // 等待一段时间确保服务器启动
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 启动控制台
     */
    private static void startConsole() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Server console started, type 'help' for available commands");
        System.out.print("vcampus> ");
        
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim().toLowerCase();
            
            try {
                handleCommand(command);
            } catch (Exception e) {
                System.out.println("Command execution failed: " + e.getMessage());
                log.error("Command execution exception", e);
            }
            
            System.out.print("vcampus> ");
        }
    }
    
    /**
     * 处理控制台命令
     * 
     * @param command 命令
     */
    private static void handleCommand(String command) {
        switch (command) {
            case "help":
            case "h":
                showHelp();
                break;
                
            case "status":
            case "s":
                showStatus();
                break;
                
            case "routes":
            case "r":
                showRoutes();
                break;
                
            case "stats":
                showStats();
                break;
                
            case "reset":
                resetStats();
                break;
                
            case "stop":
            case "exit":
            case "quit":
            case "q":
                System.out.println("Shutting down server...");
                System.exit(0);
                break;
                
            case "":
                // 空命令，不处理
                break;
                
            default:
                System.out.println("Unknown command: " + command + ", type 'help' for available commands");
                break;
        }
    }
    
    /**
     * 显示帮助信息
     */
    private static void showHelp() {
        System.out.println("Available commands:");
        System.out.println("  help, h      - Show this help information");
        System.out.println("  status, s    - Show server status");
        System.out.println("  routes, r    - Show all routes");
        System.out.println("  stats        - Show server statistics");
        System.out.println("  reset        - Reset statistics");
        System.out.println("  stop, quit   - Stop server");
    }
    
    /**
     * 显示服务器状态
     */
    private static void showStatus() {
        System.out.println("Server Status:");
        System.out.println("  Status: " + (server.isRunning() ? "Running" : "Stopped"));
        System.out.println("  Port: " + server.getPort());
        System.out.println("  Routes: " + router.getAllRoutes().size());
    }
    
    /**
     * 显示所有路由
     */
    private static void showRoutes() {
        System.out.println("Registered routes:");
        router.getAllRoutes().forEach((uri, info) -> {
            System.out.println("  " + uri + " -> " + info);
        });
    }
    
    /**
     * 显示统计信息
     */
    private static void showStats() {
        System.out.println(com.vcampus.server.net.ServerHandler.getServerStats());
    }
    
    /**
     * 重置统计信息
     */
    private static void resetStats() {
        com.vcampus.server.net.ServerHandler.resetStats();
        System.out.println("Statistics reset completed");
    }
}
