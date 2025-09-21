package com.vcampus.server.core.shop.mapper;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.vcampus.server.core.shop.enums.ProductStatus;

/**
 * 安全的 ProductStatus 类型处理器：容错解析数据库中的字符串，避免因未知值抛出异常
 */
public class ProductStatusTypeHandler extends BaseTypeHandler<ProductStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ProductStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter == null ? null : parameter.name());
    }

    @Override
    public ProductStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return toEnum(s);
    }

    @Override
    public ProductStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return toEnum(s);
    }

    @Override
    public ProductStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return toEnum(s);
    }

    private ProductStatus toEnum(String s) {
        if (s == null) return null;
        try {
            return ProductStatus.valueOf(s.trim());
        } catch (Exception e) {
            // 忽略未知值，返回 null，调用方需处理空值
            return null;
        }
    }
}

