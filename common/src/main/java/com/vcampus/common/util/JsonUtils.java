package com.vcampus.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON工具类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class JsonUtils {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final JsonSerializer<LocalDateTime> LOCAL_DATE_TIME_SERIALIZER = (src, typeOfSrc, context) -> {
        return src == null ? null : new JsonPrimitive(src.format(DATE_TIME_FORMATTER));
    };

    private static final JsonDeserializer<LocalDateTime> LOCAL_DATE_TIME_DESERIALIZER = (json, typeOfT, context) -> {
        if (json == null || json.getAsString() == null || json.getAsString().isBlank()) return null;
        try { return LocalDateTime.parse(json.getAsString(), DATE_TIME_FORMATTER); } catch (Exception e) { throw new JsonParseException(e); }
    };

    private static final JsonSerializer<LocalDate> LOCAL_DATE_SERIALIZER = (src, typeOfSrc, context) -> {
        return src == null ? null : new JsonPrimitive(src.format(DATE_FORMATTER));
    };

    private static final JsonDeserializer<LocalDate> LOCAL_DATE_DESERIALIZER = (json, typeOfT, context) -> {
        if (json == null || json.getAsString() == null || json.getAsString().isBlank()) return null;
        try { return LocalDate.parse(json.getAsString(), DATE_FORMATTER); } catch (Exception e) { throw new JsonParseException(e); }
    };

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_SERIALIZER)
            .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_DESERIALIZER)
            .registerTypeAdapter(LocalDate.class, LOCAL_DATE_SERIALIZER)
            .registerTypeAdapter(LocalDate.class, LOCAL_DATE_DESERIALIZER)
            .create();
    
    // Compact gson for network transport (no pretty printing)
    private static final Gson compactGson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_SERIALIZER)
            .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_DESERIALIZER)
            .registerTypeAdapter(LocalDate.class, LOCAL_DATE_SERIALIZER)
            .registerTypeAdapter(LocalDate.class, LOCAL_DATE_DESERIALIZER)
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
     * 对象转紧凑 JSON 字符串（适用于网络传输，单行）
     */
    public static String toCompactJson(Object obj) {
        if (obj == null) return "null";
        try {
            return compactGson.toJson(obj);
        } catch (Exception e) {
            log.error("对象转紧凑JSON失败: {}", e.getMessage());
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
     * JSON字符串转对象（支持 Type 参数以处理泛型类型）
     *
     * @param json JSON字符串
     * @param type 目标类型（含泛型信息）
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, java.lang.reflect.Type type) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            log.error("JSON转对象失败: {}, JSON: {}", e.getMessage(), json);
            return null;
        }
    }

    /**
     * 将 JSON 解析为 List<Map<String,Object>>（用于解析客户端传来的 items）
     */
    public static java.util.List<java.util.Map<String, Object>> fromJsonToListOfMap(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<java.util.Map<String, Object>>>(){}.getType();
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            log.error("JSON转List<Map>失败: {}, JSON: {}", e.getMessage(), json);
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
