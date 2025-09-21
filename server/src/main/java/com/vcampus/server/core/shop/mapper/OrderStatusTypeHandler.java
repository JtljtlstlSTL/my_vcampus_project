package com.vcampus.server.core.shop.mapper;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.vcampus.server.core.shop.enums.OrderStatus;

/**
 * 安全的 OrderStatus 类型处理器：容错解析数据库中的字符串，避免因未知值抛出异常
 */
public class OrderStatusTypeHandler extends BaseTypeHandler<OrderStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OrderStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter == null ? null : parameter.name());
    }

    @Override
    public OrderStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return toEnum(s);
    }

    @Override
    public OrderStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return toEnum(s);
    }

    @Override
    public OrderStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return toEnum(s);
    }

    private OrderStatus toEnum(String s) {
        if (s == null) return null;
        try {
            return OrderStatus.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}

