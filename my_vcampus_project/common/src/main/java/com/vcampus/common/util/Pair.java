package com.vcampus.common.util;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 键值对工具类 - 用于存储两个相关的值
 * 
 * 常用场景：
 * 1. 返回两个相关的值
 * 2. 作为Map的键值对
 * 3. 临时数据组合
 * 
 * @param <F> 第一个值的类型
 * @param <S> 第二个值的类型
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pair<F, S> {
    
    /**
     * 第一个值
     */
    private F first;
    
    /**
     * 第二个值
     */
    private S second;
    
    /**
     * 静态工厂方法 - 创建Pair实例
     */
    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }
    
    /**
     * 创建字符串Pair
     */
    public static Pair<String, String> ofStrings(String first, String second) {
        return new Pair<>(first, second);
    }
    
    /**
     * 创建整数Pair
     */
    public static Pair<Integer, Integer> ofInts(Integer first, Integer second) {
        return new Pair<>(first, second);
    }
    
    /**
     * 交换first和second的值
     */
    public Pair<S, F> swap() {
        return new Pair<>(second, first);
    }
    
    /**
     * 检查是否包含null值
     */
    public boolean hasNull() {
        return first == null || second == null;
    }
    
    /**
     * 检查两个值是否都不为null
     */
    public boolean bothNotNull() {
        return first != null && second != null;
    }
    
    @Override
    public String toString() {
        return String.format("(%s, %s)", first, second);
    }
}
