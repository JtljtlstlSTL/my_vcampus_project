package com.vcampus.common.entity.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 实体接口 - 提供JSON序列化能力
 * 
 * 所有实体类都应该实现这个接口，获得：
 * 1. JSON序列化能力 - toJson()
 * 2. JSON反序列化能力 - fromJson()
 * 3. 统一的Gson实例
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface IEntity {
    
    /**
     * 共享的Gson实例 - 配置了格式化输出
     */
    Gson gson = new GsonBuilder()
            .setPrettyPrinting()        // 格式化JSON输出
            .setDateFormat("yyyy-MM-dd HH:mm:ss")  // 日期格式
            .create();
    
    /**
     * 从JSON字符串创建对象
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化的对象
     */
    static <T extends IEntity> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 将对象转换为JSON字符串
     * 
     * @return JSON字符串
     */
    default String toJson() {
        return gson.toJson(this);
    }
    
    /**
     * 将对象转换为格式化的JSON字符串
     * 
     * @return 格式化的JSON字符串
     */
    default String toPrettyJson() {
        return gson.toJson(this);
    }
}
