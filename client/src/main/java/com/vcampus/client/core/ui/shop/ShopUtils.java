package com.vcampus.client.core.ui.shop;

import java.util.Map;

/**
 * 前端辅助工具：统一解析商品 ID 字段的兼容逻辑
 */
public final class ShopUtils {
    private ShopUtils() {}

    // 尝试多种常见字段名，按优先级返回第一个非空字符串
    public static String findProductId(Map<String, Object> item) {
        if (item == null) return null;
        String[] keys = new String[]{"productId", "product_id", "productid", "id", "sku", "code", "productCode", "product_code"};
        for (String k : keys) {
            try {
                if (item.containsKey(k)) {
                    Object v = item.get(k);
                    if (v != null) return v.toString();
                }
            } catch (Exception ignored) {}
        }
        // 尝试遍历所有键，寻找看起来像 id 的值（兜底）
        try {
            for (Map.Entry<String, Object> e : item.entrySet()) {
                String k = e.getKey();
                if (k == null) continue;
                String lk = k.toLowerCase();
                if (lk.endsWith("id") || lk.contains("sku") || lk.contains("code")) {
                    Object v = e.getValue();
                    if (v != null) return v.toString();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static Integer findProductIdAsInteger(Map<String, Object> item) {
        String s = findProductId(item);
        if (s == null) return null;
        try { return Integer.parseInt(s); } catch (Exception e) {
            try { return (int) Double.parseDouble(s); } catch (Exception ex) { return null; }
        }
    }
}

