package com.vcampus.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON工具类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class JsonUtils {
    
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    
    /**
     * 对象转JSON字符串
     * 
     * @param obj 要转换的对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return gson.toJson(obj);
        } catch (Exception e) {
            log.error("对象转JSON失败: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * JSON字符串转对象
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            log.error("JSON转对象失败: {}, JSON: {}", e.getMessage(), json);
            return null;
        }
    }
    
    /**
     * 检查字符串是否为有效的JSON
     * 
     * @param json 要检查的字符串
     * @return 是否为有效JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            gson.fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
    
    /**
     * 美化JSON字符串
     * 
     * @param json 原始JSON字符串
     * @return 格式化后的JSON字符串
     */
    public static String prettyJson(String json) {
        try {
            Object obj = gson.fromJson(json, Object.class);
            return gson.toJson(obj);
        } catch (JsonSyntaxException e) {
            log.warn("JSON格式化失败: {}", e.getMessage());
            return json;
        }
    }
}
