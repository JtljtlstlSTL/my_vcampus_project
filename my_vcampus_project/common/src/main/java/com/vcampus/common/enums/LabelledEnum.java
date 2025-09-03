package com.vcampus.common.enums;

/**
 * 标签枚举接口 - 为枚举提供中文标签
 * 
 * 所有需要显示中文名称的枚举都应该实现这个接口
 * 
 * 使用示例：
 * ```java
 * public enum Gender implements LabelledEnum {
 *     MALE("男"), FEMALE("女");
 *     
 *     private final String label;
 *     Gender(String label) { this.label = label; }
 *     
 *     @Override
 *     public String getLabel() { return label; }
 * }
 * ```
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface LabelledEnum {
    
    /**
     * 获取枚举的中文标签
     * 
     * @return 中文标签字符串
     */
    String getLabel();
    
    /**
     * 通过标签查找枚举值（默认实现）
     * 
     * @param enumClass 枚举类
     * @param label 标签
     * @param <E> 枚举类型
     * @return 匹配的枚举值，没找到返回null
     */
    static <E extends Enum<E> & LabelledEnum> E findByLabel(Class<E> enumClass, String label) {
        if (label == null) {
            return null;
        }
        
        for (E enumValue : enumClass.getEnumConstants()) {
            if (label.equals(enumValue.getLabel())) {
                return enumValue;
            }
        }
        return null;
    }
}
