package com.vcampus.server.core.library.mapper;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.vcampus.server.core.library.enums.UserType;

/**
 * UserType 枚举类型处理器
 * 用于 MyBatis 映射数据库中的 user_type 字段到 UserType 枚举
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class UserTypeTypeHandler extends BaseTypeHandler<UserType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UserType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getCode());
    }

    @Override
    public UserType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? UserType.fromCode(code) : null;
    }

    @Override
    public UserType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? UserType.fromCode(code) : null;
    }

    @Override
    public UserType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? UserType.fromCode(code) : null;
    }
}
