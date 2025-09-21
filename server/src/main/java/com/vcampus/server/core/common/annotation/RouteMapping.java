package com.vcampus.server.core.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路由映射注解
 * 用于标记控制器方法的路由信息
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RouteMapping {
    
    /**
     * 路由URI
     * 例如: "auth/login", "student/info"
     */
    String uri();
    
    /**
     * 需要的角色权限
     * 默认为匿名访问
     */
    String role() default "anonymous";
    
    /**
     * 路由描述
     */
    String description() default "";
}
