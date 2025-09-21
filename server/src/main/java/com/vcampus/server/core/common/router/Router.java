package com.vcampus.server.core.common.router;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.server.core.common.annotation.RouteMapping;
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
        log.info("Starting router initialization, scanning package: {}", packageName);
        
        Reflections reflections = new Reflections(packageName);
        
        // 扫描所有包含RouteMapping注解方法的类
        Set<Class<?>> controllerClasses = new HashSet<>();
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(RouteMapping.class);
        for (Method method : annotatedMethods) {
            controllerClasses.add(method.getDeclaringClass());
        }
        
        log.info("Found {} methods with @RouteMapping annotation", annotatedMethods.size());
        log.info("Found {} controller classes", controllerClasses.size());
        
        for (Class<?> controllerClass : controllerClasses) {
            registerController(controllerClass);
        }
        
        // 如果自动扫描失败，尝试手动注册一些基本路由
        if (routeMap.isEmpty()) {
            log.warn("No routes found by automatic scanning, trying manual registration...");
            registerManualRoutes();
        }
        
        log.info("Router initialization completed, registered {} routes", routeMap.size());
        printRoutes();
    }
    
    /**
     * 手动注册一些基本路由（用于调试）
     */
    private void registerManualRoutes() {

        try {
            // 尝试手动注册AuthController
            Class<?> authControllerClass = Class.forName("com.vcampus.server.core.auth.controller.AuthController");
            registerController(authControllerClass);
            log.info("Manually registered AuthController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find AuthController class: {}", e.getMessage());
        }
        
        try {
            // 尝试手动注册AcademicController
            Class<?> academicControllerClass = Class.forName("com.vcampus.server.core.academic.controller.AcademicController");
            registerController(academicControllerClass);
            log.info("Manually registered AcademicController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find AcademicController class: {}", e.getMessage());
        }

        try {
            // 手动注册ShopController - 这是关键！
            Class<?> shopControllerClass = Class.forName("com.vcampus.server.core.shop.controller.ShopController");
            registerController(shopControllerClass);
            log.info("Manually registered ShopController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find ShopController class: {}", e.getMessage());
        }

        // 新增：手动注册 CardController，确保 card/* 路由存在
        try {
            Class<?> cardControllerClass = Class.forName("com.vcampus.server.core.card.controller.CardController");
            registerController(cardControllerClass);
            log.info("Manually registered CardController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find CardController class: {}", e.getMessage());
        }

               // 新增：手动注册图书馆相关控制器
        try {
            Class<?> studentLibraryControllerClass = Class.forName("com.vcampus.server.core.library.controller.StudentLibraryController");
            registerController(studentLibraryControllerClass);
            log.info("Manually registered StudentLibraryController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find StudentLibraryController class: {}", e.getMessage());
        }

        try {
            Class<?> teacherLibraryControllerClass = Class.forName("com.vcampus.server.core.library.controller.TeacherLibraryController");
            registerController(teacherLibraryControllerClass);
            log.info("Manually registered TeacherLibraryController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find TeacherLibraryController class: {}", e.getMessage());
        }

        try {
            Class<?> adminLibraryControllerClass = Class.forName("com.vcampus.server.core.library.controller.AdminLibraryController");
            registerController(adminLibraryControllerClass);
            log.info("Manually registered AdminLibraryController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find AdminLibraryController class: {}", e.getMessage());
        }

        try {
            Class<?> userBookSearchControllerClass = Class.forName("com.vcampus.server.core.library.controller.UserBookSearchController");
            registerController(userBookSearchControllerClass);
            log.info("Manually registered UserBookSearchController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find UserBookSearchController class: {}", e.getMessage());
        }

        try {
            Class<?> adminBookManagementControllerClass = Class.forName("com.vcampus.server.core.library.controller.AdminBookManagementController");
            registerController(adminBookManagementControllerClass);
            log.info("Manually registered AdminBookManagementController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find AdminBookManagementController class: {}", e.getMessage());
        }

        try {
            Class<?> myBookshelfControllerClass = Class.forName("com.vcampus.server.core.library.controller.MyBookshelfController");
            registerController(myBookshelfControllerClass);
            log.info("Manually registered MyBookshelfController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find MyBookshelfController class: {}", e.getMessage());
        }

        try {
            Class<?> userPersonalCategoryControllerClass = Class.forName("com.vcampus.server.core.library.controller.UserPersonalCategoryController");
            registerController(userPersonalCategoryControllerClass);
            log.info("Manually registered UserPersonalCategoryController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find UserPersonalCategoryController class: {}", e.getMessage());
        }

        // 新增：手动注册 StatisticsController，这是关键的统计路由！
        try {
            Class<?> statisticsControllerClass = Class.forName("com.vcampus.server.core.library.controller.StatisticsController");
            registerController(statisticsControllerClass);
            log.info("Manually registered StatisticsController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find StatisticsController class: {}", e.getMessage());
        }

        try {
            // 新增：手动注册 CourseController，确保 course/* 路由存在
            Class<?> courseControllerClass = Class.forName("com.vcampus.server.core.course.controller.CourseController");
            registerController(courseControllerClass);
            log.info("Manually registered CourseController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find CourseController class: {}", e.getMessage());
        }


        // 新增：手动注册荐购相关控制器
        try {
            Class<?> userRecommendationControllerClass = Class.forName("com.vcampus.server.core.library.controller.UserRecommendationController");
            registerController(userRecommendationControllerClass);
            log.info("Manually registered UserRecommendationController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find UserRecommendationController class: {}", e.getMessage());
        }

        try {
            Class<?> adminRecommendationControllerClass = Class.forName("com.vcampus.server.core.library.controller.AdminRecommendationController");
            registerController(adminRecommendationControllerClass);
            log.info("Manually registered AdminRecommendationController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find AdminRecommendationController class: {}", e.getMessage());
        }

        try {
            // 新增：手动注册 CommentController，确保 comment/* 路由存在
            Class<?> commentControllerClass = Class.forName("com.vcampus.server.core.comment.controller.CommentController");
            registerController(commentControllerClass);
            log.info("Manually registered CommentController");
        } catch (ClassNotFoundException e) {
            log.warn("Could not find CommentController class: {}", e.getMessage());
        }
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
                    
                    log.debug("Registering route: {} -> {}.{} (permission: {})", 
                            uri, controllerClass.getSimpleName(), method.getName(), role);
                }
            }
        } catch (Exception e) {
            log.error("Failed to register controller: {}", controllerClass.getName(), e);
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
            log.warn("Route not found: {}", uri);
            return Response.Builder.notFound("Route does not exist: " + uri);
        }
        
        // 检查权限
        if (!hasPermission(request, routeInfo.role)) {
            log.warn("Insufficient permission: {} requires permission: {}", uri, routeInfo.role);
            return Response.Builder.forbidden("Insufficient permission, requires role: " + routeInfo.role);
        }
        
        try {
            log.debug("Calling route: {} -> {}.{}", uri, 
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
            log.error("Route call exception: {}", uri, e);
            return Response.Builder.internalError("Server internal error: " + e.getMessage())
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

        // 新增：处理 "all" 角色 - 所有已登录用户都可以访问
        if ("all".equals(requiredRole)) {
            return request.getSession() != null;
        }

        if (request.getSession() == null) {
            return false;
        }

        // 处理多角色权限（逗号分隔）
        if (requiredRole != null && requiredRole.contains(",")) {
            String[] roles = requiredRole.split(",");
            for (String role : roles) {
                if (hasSingleRolePermission(request, role.trim())) {
                    return true;
                }
            }
            return false;
        }

        // 处理单角色权限
        return hasSingleRolePermission(request, requiredRole);
    }
    
    /**
     * 检查单个角色权限
     */
    private boolean hasSingleRolePermission(Request request, String requiredRole) {
        if (requiredRole == null || requiredRole.trim().isEmpty()) {
            return false;
        }

        // 兼容处理：将教职工角色视为等价权限（teacher 与 staff 互通）
        try {
            String req = requiredRole.trim().toLowerCase();
            if ("staff".equals(req) || "teacher".equals(req)) {
                // 如果会话包含 staff 或 teacher 或 admin，则允许
                java.util.Set<String> roleSet = request.getSession().getRoleSet();
                for (String r : roleSet) {
                    if (r == null) continue;
                    String lower = r.toLowerCase();
                    if ("staff".equals(lower) || "teacher".equals(lower) || "admin".equals(lower)) return true;
                }
                return false;
            }
            // 兼容：允许教职工访问学生入口（部分学校将学生/教职工权限互通）
            if ("student".equals(req)) {
                java.util.Set<String> roleSet = request.getSession().getRoleSet();
                for (String r : roleSet) {
                    if (r == null) continue;
                    String lower = r.toLowerCase();
                    if ("student".equals(lower) || "staff".equals(lower) || "teacher".equals(lower) || "admin".equals(lower) || "manager".equals(lower)) return true;
                }
                return false;
            }
        } catch (Exception ignored) {}

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
        log.info("Registered routes list:");
        routeMap.forEach((uri, routeInfo) -> {
            log.info("  {} [{}] -> {}.{} - {}", 
                    uri, 
                    routeInfo.role,
                    routeInfo.controller.getClass().getSimpleName(),
                    routeInfo.method.getName(),
                    routeInfo.description.isEmpty() ? "No description" : routeInfo.description);
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
