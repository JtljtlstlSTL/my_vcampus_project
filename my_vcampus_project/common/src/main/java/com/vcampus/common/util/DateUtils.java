package com.vcampus.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类 - 提供常用的日期格式化和解析功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class DateUtils {
    
    // 常用日期格式
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String CHINESE_DATE_FORMAT = "yyyy年MM月dd日";
    public static final String SHORT_DATE_FORMAT = "yyyyMMdd";
    
    /**
     * 格式化日期为字符串 (默认格式: yyyy-MM-dd)
     */
    public static String formatDate(Date date) {
        return formatDate(date, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 格式化日期为字符串 (指定格式)
     */
    public static String formatDate(Date date, String format) {
        if (date == null) return "";
        try {
            return new SimpleDateFormat(format).format(date);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 格式化日期时间为字符串 (默认格式: yyyy-MM-dd HH:mm:ss)
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, DEFAULT_DATETIME_FORMAT);
    }
    
    /**
     * 解析字符串为日期 (默认格式: yyyy-MM-dd)
     */
    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 解析字符串为日期 (指定格式)
     */
    public static Date parseDate(String dateStr, String format) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(format).parse(dateStr.trim());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取当前日期字符串
     */
    public static String getCurrentDate() {
        return formatDate(new Date());
    }
    
    /**
     * 获取当前日期时间字符串
     */
    public static String getCurrentDateTime() {
        return formatDateTime(new Date());
    }
    
    /**
     * 计算年龄
     */
    public static Integer calculateAge(Date birthDate) {
        if (birthDate == null) {
            return null;
        }
        
        long diffInMillis = System.currentTimeMillis() - birthDate.getTime();
        return (int) (diffInMillis / (365.25 * 24 * 60 * 60 * 1000));
    }
    
    /**
     * 检查日期字符串是否有效
     */
    public static boolean isValidDate(String dateStr, String format) {
        return parseDate(dateStr, format) != null;
    }
    
    /**
     * 检查日期字符串是否有效 (默认格式)
     */
    public static boolean isValidDate(String dateStr) {
        return isValidDate(dateStr, DEFAULT_DATE_FORMAT);
    }
}
