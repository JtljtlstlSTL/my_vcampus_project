package com.vcampus.common.message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一请求消息类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    
    /**
     * 请求唯一标识符，用于请求-响应匹配
     */
    private UUID id = UUID.randomUUID();
    
    /**
     * 请求资源URI，标识要执行的操作
     * 例如: "auth/login", "student/info", "course/list"
     */
    private String uri;
    
    /**
     * 请求参数，键值对形式
     */
    private Map<String, String> params = new HashMap<>();
    
    /**
     * 用户会话信息
     */
    private Session session;
    
    /**
     * 请求时间戳
     */
    private long timestamp = System.currentTimeMillis();
    
    /**
     * 便捷的构造方法
     * 
     * @param uri 请求URI
     */
    public Request(String uri) {
        this.uri = uri;
    }
    
    /**
     * 便捷的构造方法
     * 
     * @param uri 请求URI
     * @param params 请求参数
     */
    public Request(String uri, Map<String, String> params) {
        this.uri = uri;
        this.params = params != null ? params : new HashMap<>();
    }
    
    /**
     * 添加参数
     * 
     * @param key 参数名
     * @param value 参数值
     * @return 当前Request对象，支持链式调用
     */
    public Request addParam(String key, String value) {
        if (this.params == null) {
            this.params = new HashMap<>();
        }
        this.params.put(key, value);
        return this;
    }
    
    /**
     * 获取参数
     * 
     * @param key 参数名
     * @return 参数值，不存在则返回null
     */
    public String getParam(String key) {
        return params != null ? params.get(key) : null;
    }
    
    /**
     * 检查是否包含某个参数
     * 
     * @param key 参数名
     * @return 是否包含
     */
    public boolean hasParam(String key) {
        return params != null && params.containsKey(key);
    }
}
