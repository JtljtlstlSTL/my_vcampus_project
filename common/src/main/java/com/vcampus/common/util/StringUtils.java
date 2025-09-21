package com.vcampus.common.util;

import java.util.Collection;

/**
 * 字符串工具类
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class StringUtils {
    
    /**
     * 检查字符串是否为空
     * 
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * 检查字符串是否不为空
     * 
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 检查字符串是否为空白
     * 
     * @param str 字符串
     * @return 是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 检查字符串是否不为空白
     * 
     * @param str 字符串
     * @return 是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * 安全的字符串trim，null安全
     * 
     * @param str 字符串
     * @return trim后的字符串，null则返回null
     */
    public static String safeTrim(String str) {
        return str == null ? null : str.trim();
    }
    
    /**
     * 字符串默认值，如果为空则返回默认值
     * 
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 字符串或默认值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }
    
    /**
     * 字符串默认值，如果为空白则返回默认值
     * 
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 字符串或默认值
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }
    
    /**
     * 连接字符串数组
     * 
     * @param delimiter 分隔符
     * @param elements 字符串数组
     * @return 连接后的字符串
     */
    public static String join(String delimiter, String... elements) {
        if (elements == null || elements.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(elements[i]);
        }
        return sb.toString();
    }
    
    /**
     * 连接字符串集合
     * 
     * @param delimiter 分隔符
     * @param elements 字符串集合
     * @return 连接后的字符串
     */
    public static String join(String delimiter, Collection<String> elements) {
        if (elements == null || elements.isEmpty()) {
            return "";
        }
        return String.join(delimiter, elements);
    }
    
    /**
     * 重复字符串
     * 
     * @param str 要重复的字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    public static String repeat(String str, int count) {
        if (str == null || count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * 首字母大写
     * 
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * 首字母小写
     * 
     * @param str 字符串
     * @return 首字母小写的字符串
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}
