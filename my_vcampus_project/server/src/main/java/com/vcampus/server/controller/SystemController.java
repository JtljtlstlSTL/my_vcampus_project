package com.vcampus.server.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.server.annotation.RouteMapping;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统控制器 - 处理系统相关请求
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class SystemController {
    
    /**
     * 心跳检测
     */
    @RouteMapping(uri = "system/heartbeat", description = "系统心跳检测")
    public Response heartbeat(Request request) {
        log.debug("💗 收到心跳请求");
        
        Map<String, Object> data = new HashMap<>();
        data.put("server", "VCampus Server");
        data.put("version", "1.0.0");
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        data.put("status", "healthy");
        
        return Response.Builder.success("服务器运行正常", data);
    }
    
    /**
     * 获取服务器信息
     */
    @RouteMapping(uri = "system/info", description = "获取服务器信息")
    public Response getServerInfo(Request request) {
        log.debug("ℹ️ 获取服务器信息");
        
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> data = new HashMap<>();
        
        // 基本信息
        data.put("server", "VCampus Server");
        data.put("version", "1.0.0");
        data.put("startTime", System.getProperty("java.vm.name"));
        
        // 系统信息
        Map<String, Object> system = new HashMap<>();
        system.put("os", System.getProperty("os.name"));
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("availableProcessors", runtime.availableProcessors());
        data.put("system", system);
        
        // 内存信息
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("max", runtime.maxMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        data.put("memory", memory);
        
        return Response.Builder.success("服务器信息获取成功", data);
    }
    
    /**
     * 获取当前时间
     */
    @RouteMapping(uri = "system/time", description = "获取服务器当前时间")
    public Response getCurrentTime(Request request) {
        log.debug("🕐 获取当前时间");
        
        Map<String, Object> data = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        
        data.put("timestamp", System.currentTimeMillis());
        data.put("datetime", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        data.put("date", now.format(DateTimeFormatter.ISO_LOCAL_DATE));
        data.put("time", now.format(DateTimeFormatter.ISO_LOCAL_TIME));
        
        return Response.Builder.success("当前时间获取成功", data);
    }
    
    /**
     * 回显测试
     */
    @RouteMapping(uri = "system/echo", description = "回显测试")
    public Response echo(Request request) {
        log.debug("📢 回显测试");
        
        Map<String, Object> data = new HashMap<>();
        data.put("originalRequest", request);
        data.put("echo", request.getParams());
        data.put("message", "这是回显测试");
        
        return Response.Builder.success("回显成功", data);
    }
}
