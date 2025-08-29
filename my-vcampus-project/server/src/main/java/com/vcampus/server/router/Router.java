package com.vcampus.server.router;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.server.annotation.RouteMapping;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 路由器 - 负责请求分发和权限控制
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class Router {
    
    /**
     * URI到控制器方法的映射
     */
    private final Map<String, RouteInfo> routeMap = new HashMap<>();
    
    /**
     * 控制器实例缓存
     */
    private final Map<Class<?>, Object> controllerInstances = new HashMap<>();
    
    /**
     * 路由信息内部类
     */
    private static class RouteInfo {
        Object controller;
        Method method;
        String role;
        String description;
        
        RouteInfo(Object controller, Method method, String role, String description) {
            this.controller = controller;
            this.method = method;
            this.role = role;
            this.description = description;
        }
    }
    
    /**
     * 初始化路由器，扫描所有控制器
     * 
     * @param packageName 要扫描的包名
     */
    public void initialize(String packageName) {
        log.info("🚀 开始初始化路由器，扫描包: {}", packageName);
        
        Reflections reflections = new Reflections(packageName);
        
        // 扫描所有包含RouteMapping注解方法的类
        Set<Class<?>> controllerClasses = new HashSet<>();
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(RouteMapping.class);
        for (Method method : annotatedMethods) {
            controllerClasses.add(method.getDeclaringClass());
        }
        
        for (Class<?> controllerClass : controllerClasses) {
            registerController(controllerClass);
        }
        
        log.info("✅ 路由器初始化完成，共注册 {} 个路由", routeMap.size());
        printRoutes();
    }
    
    /**
     * 注册控制器
     * 
     * @param controllerClass 控制器类
     */
    private void registerController(Class<?> controllerClass) {
        try {
            // 创建控制器实例
            Object controllerInstance = getOrCreateControllerInstance(controllerClass);
            
            // 扫描所有方法
            Method[] methods = controllerClass.getDeclaredMethods();
            for (Method method : methods) {
                RouteMapping annotation = method.getAnnotation(RouteMapping.class);
                if (annotation != null) {
                    String uri = annotation.uri();
                    String role = annotation.role();
                    String description = annotation.description();
                    
                    RouteInfo routeInfo = new RouteInfo(controllerInstance, method, role, description);
                    routeMap.put(uri, routeInfo);
                    
                    log.debug("📍 注册路由: {} -> {}.{} (权限: {})", 
                            uri, controllerClass.getSimpleName(), method.getName(), role);
                }
            }
        } catch (Exception e) {
            log.error("❌ 注册控制器失败: {}", controllerClass.getName(), e);
        }
    }
    
    /**
     * 获取或创建控制器实例
     * 
     * @param controllerClass 控制器类
     * @return 控制器实例
     */
    private Object getOrCreateControllerInstance(Class<?> controllerClass) throws Exception {
        Object instance = controllerInstances.get(controllerClass);
        if (instance == null) {
            instance = controllerClass.getDeclaredConstructor().newInstance();
            controllerInstances.put(controllerClass, instance);
        }
        return instance;
    }
    
    /**
     * 处理请求
     * 
     * @param request 请求对象
     * @return 响应对象
     */
    public Response route(Request request) {
        String uri = request.getUri();
        
        // 检查路由是否存在
        RouteInfo routeInfo = routeMap.get(uri);
        if (routeInfo == null) {
            log.warn("🚫 未找到路由: {}", uri);
            return Response.Builder.notFound("路由不存在: " + uri);
        }
        
        // 检查权限
        if (!hasPermission(request, routeInfo.role)) {
            log.warn("🔒 权限不足: {} 需要权限: {}", uri, routeInfo.role);
            return Response.Builder.forbidden("权限不足，需要角色: " + routeInfo.role);
        }
        
        try {
            log.debug("📞 调用路由: {} -> {}.{}", uri, 
                    routeInfo.controller.getClass().getSimpleName(), 
                    routeInfo.method.getName());
            
            // 调用控制器方法
            Object result = routeInfo.method.invoke(routeInfo.controller, request);
            
            if (result instanceof Response) {
                Response response = (Response) result;
                return response.withId(request.getId());
            } else {
                return Response.Builder.success(result).withId(request.getId());
            }
        } catch (Exception e) {
            log.error("💥 路由调用异常: {}", uri, e);
            return Response.Builder.internalError("服务器内部错误: " + e.getMessage())
                    .withId(request.getId());
        }
    }
    
    /**
     * 检查权限
     * 
     * @param request 请求对象
     * @param requiredRole 需要的角色
     * @return 是否有权限
     */
    private boolean hasPermission(Request request, String requiredRole) {
        if ("anonymous".equals(requiredRole)) {
            return true;
        }
        
        if (request.getSession() == null) {
            return false;
        }
        
        return request.getSession().hasPermission(requiredRole);
    }
    
    /**
     * 检查路由是否存在
     * 
     * @param uri URI
     * @return 是否存在
     */
    public boolean hasRoute(String uri) {
        return routeMap.containsKey(uri);
    }
    
    /**
     * 获取路由需要的角色
     * 
     * @param uri URI
     * @return 需要的角色
     */
    public String getRequiredRole(String uri) {
        RouteInfo routeInfo = routeMap.get(uri);
        return routeInfo != null ? routeInfo.role : null;
    }
    
    /**
     * 打印所有路由信息
     */
    private void printRoutes() {
        log.info("📋 已注册的路由列表:");
        routeMap.forEach((uri, routeInfo) -> {
            log.info("  {} [{}] -> {}.{} - {}", 
                    uri, 
                    routeInfo.role,
                    routeInfo.controller.getClass().getSimpleName(),
                    routeInfo.method.getName(),
                    routeInfo.description.isEmpty() ? "无描述" : routeInfo.description);
        });
    }
    
    /**
     * 获取所有路由信息
     * 
     * @return 路由信息映射
     */
    public Map<String, String> getAllRoutes() {
        Map<String, String> routes = new HashMap<>();
        routeMap.forEach((uri, routeInfo) -> {
            routes.put(uri, String.format("%s.%s [%s]", 
                    routeInfo.controller.getClass().getSimpleName(),
                    routeInfo.method.getName(),
                    routeInfo.role));
        });
        return routes;
    }
}
