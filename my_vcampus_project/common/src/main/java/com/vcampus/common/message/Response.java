package com.vcampus.common.message;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * 统一响应消息类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
public class Response {
    
    /**
     * 响应ID，与请求ID对应
     */
    private UUID id;
    
    /**
     * 响应状态码
     */
    private String status;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private Object data;
    
    /**
     * 更新后的会话信息（如果有变化）
     */
    private transient Session session;
    
    /**
     * 响应时间戳
     */
    private long timestamp = System.currentTimeMillis();
    
    /**
     * 全参数构造器
     */
    public Response(UUID id, String status, String message, Object data, Session session, long timestamp) {
        this.id = id;
        this.status = status;
        this.message = message;
        this.data = data;
        this.session = session;
        this.timestamp = timestamp;
    }
    
    /**
     * 常用响应工厂类
     */
    public static class Builder {
        
        /**
         * 成功响应
         */
        public static Response success() {
            return new Response(null, "SUCCESS", "操作成功", null, null, System.currentTimeMillis());
        }
        
        /**
         * 成功响应带数据
         */
        public static Response success(Object data) {
            return new Response(null, "SUCCESS", "操作成功", data, null, System.currentTimeMillis());
        }
        
        /**
         * 成功响应带消息和数据
         */
        public static Response success(String message, Object data) {
            return new Response(null, "SUCCESS", message, data, null, System.currentTimeMillis());
        }
        
        /**
         * 错误响应
         */
        public static Response error(String message) {
            return new Response(null, "ERROR", message, null, null, System.currentTimeMillis());
        }
        
        /**
         * 权限拒绝
         */
        public static Response forbidden() {
            return new Response(null, "FORBIDDEN", "权限不足", null, null, System.currentTimeMillis());
        }
        
        /**
         * 权限拒绝带消息
         */
        public static Response forbidden(String message) {
            return new Response(null, "FORBIDDEN", message, null, null, System.currentTimeMillis());
        }
        
        /**
         * 未找到资源
         */
        public static Response notFound() {
            return new Response(null, "NOT_FOUND", "请求的资源不存在", null, null, System.currentTimeMillis());
        }
        
        /**
         * 未找到资源带消息
         */
        public static Response notFound(String message) {
            return new Response(null, "NOT_FOUND", message, null, null, System.currentTimeMillis());
        }
        
        /**
         * 参数错误
         */
        public static Response badRequest(String message) {
            return new Response(null, "BAD_REQUEST", message, null, null, System.currentTimeMillis());
        }
        
        /**
         * 服务器内部错误
         */
        public static Response internalError() {
            return new Response(null, "INTERNAL_ERROR", "服务器内部错误", null, null, System.currentTimeMillis());
        }
        
        /**
         * 服务器内部错误带消息
         */
        public static Response internalError(String message) {
            return new Response(null, "INTERNAL_ERROR", message, null, null, System.currentTimeMillis());
        }
    }
    
    /**
     * 检查响应是否成功
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }
    
    /**
     * 设置响应ID（用于请求-响应匹配）
     */
    public Response withId(UUID requestId) {
        this.id = requestId;
        return this;
    }
    
    /**
     * 设置会话信息
     */
    public Response withSession(Session session) {
        this.session = session;
        return this;
    }
}
