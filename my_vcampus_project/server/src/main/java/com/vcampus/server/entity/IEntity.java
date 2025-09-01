package com.vcampus.server.entity;

import com.vcampus.common.util.JsonUtils;

/**
 * 实体接口 - 提供JSON序列化能力
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface IEntity {
    
    /**
     * 从JSON字符串反序列化为实体对象
     * 
     * @param json JSON字符串
     * @param clazz 实体类型
     * @param <T> 实体类泛型
     * @return 实体对象
     */
    static <T extends IEntity> T fromJson(String json, Class<T> clazz) {
        return JsonUtils.fromJson(json, clazz);
    }
    
    /**
     * 将实体对象序列化为JSON字符串
     * 
     * @return JSON字符串
     */
    default String toJson() {
        return JsonUtils.toJson(this);
    }
}
