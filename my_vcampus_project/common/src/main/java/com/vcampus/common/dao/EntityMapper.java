package com.vcampus.common.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 实体映射接口 - 将数据库结果集映射为Java对象
 * @param <T> 实体类型
 */
@FunctionalInterface
public interface EntityMapper<T> {
    
    /**
     * 将ResultSet映射为实体对象
     * @param rs 数据库结果集
     * @return 实体对象
     * @throws SQLException SQL异常
     */
    T map(ResultSet rs) throws SQLException;
}
