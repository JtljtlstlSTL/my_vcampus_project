package com.vcampus.common.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询条件构建器 - 用于构建动态SQL查询条件
 */
public class QueryCondition {
    private List<Condition> conditions = new ArrayList<>();
    private List<OrderBy> orderByList = new ArrayList<>();
    
    /**
     * 创建查询条件
     */
    public static QueryCondition create() {
        return new QueryCondition();
    }
    
    /**
     * 等于条件
     */
    public QueryCondition eq(String field, Object value) {
        if (value != null) {
            conditions.add(new Condition(field, "=", value));
        }
        return this;
    }
    
    /**
     * 模糊查询
     */
    public QueryCondition like(String field, String value) {
        if (value != null && !value.trim().isEmpty()) {
            conditions.add(new Condition(field, "LIKE", "%" + value + "%"));
        }
        return this;
    }
    
    /**
     * 大于条件
     */
    public QueryCondition gt(String field, Object value) {
        if (value != null) {
            conditions.add(new Condition(field, ">", value));
        }
        return this;
    }
    
    /**
     * 小于条件
     */
    public QueryCondition lt(String field, Object value) {
        if (value != null) {
            conditions.add(new Condition(field, "<", value));
        }
        return this;
    }
    
    /**
     * 升序排序
     */
    public QueryCondition orderByAsc(String field) {
        orderByList.add(new OrderBy(field, "ASC"));
        return this;
    }
    
    /**
     * 降序排序
     */
    public QueryCondition orderByDesc(String field) {
        orderByList.add(new OrderBy(field, "DESC"));
        return this;
    }
    
    /**
     * 获取所有条件
     */
    public List<Condition> getConditions() {
        return conditions;
    }
    
    /**
     * 获取排序条件
     */
    public List<OrderBy> getOrderByList() {
        return orderByList;
    }
    
    /**
     * 查询条件
     */
    public static class Condition {
        private String field;
        private String operator;
        private Object value;
        
        public Condition(String field, String operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
        
        // Getters
        public String getField() { return field; }
        public String getOperator() { return operator; }
        public Object getValue() { return value; }
    }
    
    /**
     * 排序条件
     */
    public static class OrderBy {
        private String field;
        private String direction;
        
        public OrderBy(String field, String direction) {
            this.field = field;
            this.direction = direction;
        }
        
        // Getters
        public String getField() { return field; }
        public String getDirection() { return direction; }
    }
}
