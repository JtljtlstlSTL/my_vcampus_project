package com.vcampus.client.core.util;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ID处理工具类
 * 用于确保前端ID格式的一致性，避免浮点数格式导致的后端类型不匹配问题
 */
public class IdUtils {

    /**
     * 将任意类型的ID对象转换为整数
     * 处理各种可能的格式：Number类型、浮点数字符串、整数字符串等
     *
     * @param idObj ID对象
     * @return 整数格式的ID，如果解析失败返回-1
     */
    public static int parseId(Object idObj) {
        if (idObj == null) {
            return -1;
        }

        // 如果是Number类型，直接转换为int
        if (idObj instanceof Number) {
            return ((Number) idObj).intValue();
        }

        // 如果是字符串，尝试解析
        String idStr = idObj.toString().trim();
        if (idStr.isEmpty()) {
            return -1;
        }

        try {
            // 使用BigDecimal来精确处理浮点数字符串
            BigDecimal bd = new BigDecimal(idStr);
            return bd.intValue();
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 安全地将ID对象转换为字符串格式
     * 确保不会出现科学计数法等格式问题
     *
     * @param idObj ID对象
     * @return 字符串格式的ID，如果解析失败返回空字符串
     */
    public static String formatId(Object idObj) {
        if (idObj == null) {
            return "";
        }

        if (idObj instanceof Number) {
            // 使用BigDecimal避免科学计数法
            BigDecimal bd = new BigDecimal(idObj.toString());
            return bd.toPlainString();
        }

        return idObj.toString().trim();
    }

    /**
     * 验证ID是否为有效的正整数
     *
     * @param idObj ID对象
     * @return 如果是有效的正整数返回true，否则返回false
     */
    public static boolean isValidId(Object idObj) {
        int id = parseId(idObj);
        return id > 0;
    }

    /**
     * 将ID对象转换为Long类型
     * 适用于需要Long类型ID的场景
     *
     * @param idObj ID对象
     * @return Long格式的ID，如果解析失败返回-1L
     */
    public static long parseLongId(Object idObj) {
        if (idObj == null) {
            return -1L;
        }

        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }

        String idStr = idObj.toString().trim();
        if (idStr.isEmpty()) {
            return -1L;
        }

        try {
            BigDecimal bd = new BigDecimal(idStr);
            return bd.longValue();
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    /**
     * 批量处理ID列表，确保所有ID都是整数格式
     *
     * @param idList ID列表
     * @return 处理后的整数ID数组
     */
    public static int[] parseIdArray(Object[] idList) {
        if (idList == null || idList.length == 0) {
            return new int[0];
        }

        int[] result = new int[idList.length];
        for (int i = 0; i < idList.length; i++) {
            result[i] = parseId(idList[i]);
        }
        return result;
    }

    /**
     * 安全地从Map中获取ID值并转换为整数
     *
     * @param map 数据Map
     * @param key ID字段的键名
     * @return 整数格式的ID，如果不存在或解析失败返回-1
     */
    public static int getIdFromMap(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return -1;
        }
        return parseId(map.get(key));
    }

    /**
     * 创建适用于前端显示的ID字符串
     * 确保格式统一且易读
     *
     * @param idObj ID对象
     * @param prefix 前缀（如"课程ID: "）
     * @return 格式化的显示字符串
     */
    public static String createDisplayId(Object idObj, String prefix) {
        String formattedId = formatId(idObj);
        if (formattedId.isEmpty()) {
            return prefix + "未知";
        }
        return prefix + formattedId;
    }
}
