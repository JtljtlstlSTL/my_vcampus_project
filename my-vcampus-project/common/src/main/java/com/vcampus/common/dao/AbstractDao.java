package com.vcampus.common.dao;

import com.vcampus.common.db.DbHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 抽象基础DAO类 - 实现通用的CRUD操作
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public abstract class AbstractDao<T, ID> implements IDao<T, ID> {
    
    protected final String tableName;
    protected final String idFieldName;
    protected final EntityMapper<T> entityMapper;
    
    public AbstractDao(String tableName, String idFieldName, EntityMapper<T> entityMapper) {
        this.tableName = tableName;
        this.idFieldName = idFieldName;
        this.entityMapper = entityMapper;
    }
    
    @Override
    public Optional<T> findById(ID id) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + idFieldName + " = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(entityMapper.map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
    
    @Override
    public List<T> findAll() {
        String sql = "SELECT * FROM " + tableName;
        List<T> results = new ArrayList<>();
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(entityMapper.map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
        return results;
    }
    
    @Override
    public T save(T entity) {
        ID id = getIdValue(entity);
        if (id == null || !existsById(id)) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }
    
    @Override
    public List<T> saveAll(List<T> entities) {
        List<T> results = new ArrayList<>();
        for (T entity : entities) {
            results.add(save(entity));
        }
        return results;
    }
    
    @Override
    public void delete(T entity) {
        ID id = getIdValue(entity);
        if (id != null) {
            deleteById(id);
        }
    }
    
    @Override
    public void deleteById(ID id) {
        String sql = "DELETE FROM " + tableName + " WHERE " + idFieldName + " = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("删除失败：未找到ID为 " + id + " 的记录");
            }
        } catch (SQLException e) {
            throw new RuntimeException("删除失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("统计失败: " + e.getMessage(), e);
        }
        return 0;
    }
    
    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
    
    /**
     * 插入新记录
     */
    protected T insert(T entity) {
        String sql = getInsertSql();
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParams(stmt, entity);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    setIdValue(entity, rs.getObject(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("插入失败: " + e.getMessage(), e);
        }
        return entity;
    }
    
    /**
     * 更新记录
     */
    protected T update(T entity) {
        String sql = getUpdateSql();
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setUpdateParams(stmt, entity);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("更新失败：未找到要更新的记录");
            }
        } catch (SQLException e) {
            throw new RuntimeException("更新失败: " + e.getMessage(), e);
        }
        return entity;
    }
    
    /**
     * 根据条件查询
     */
    public List<T> findByCondition(QueryCondition condition) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
        List<Object> params = new ArrayList<>();
        
        if (!condition.getConditions().isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < condition.getConditions().size(); i++) {
                QueryCondition.Condition cond = condition.getConditions().get(i);
                if (i > 0) sql.append(" AND ");
                sql.append(cond.getField()).append(" ").append(cond.getOperator()).append(" ?");
                params.add(cond.getValue());
            }
        }
        
        if (!condition.getOrderByList().isEmpty()) {
            sql.append(" ORDER BY ");
            for (int i = 0; i < condition.getOrderByList().size(); i++) {
                QueryCondition.OrderBy orderBy = condition.getOrderByList().get(i);
                if (i > 0) sql.append(", ");
                sql.append(orderBy.getField()).append(" ").append(orderBy.getDirection());
            }
        }
        
        List<T> results = new ArrayList<>();
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(entityMapper.map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("条件查询失败: " + e.getMessage(), e);
        }
        return results;
    }
    
    /**
     * 分页查询
     */
    public PageResult<T> findPage(int page, int size) {
        long total = count();
        String sql = "SELECT * FROM " + tableName + " LIMIT ? OFFSET ?";
        List<T> content = new ArrayList<>();
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, size);
            stmt.setInt(2, page * size);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                content.add(entityMapper.map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("分页查询失败: " + e.getMessage(), e);
        }
        
        return new PageResult<>(content, page, size, total);
    }
    
    // 抽象方法 - 子类必须实现
    protected abstract String getInsertSql();
    protected abstract String getUpdateSql();
    protected abstract void setInsertParams(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract void setUpdateParams(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract ID getIdValue(T entity);
    protected abstract void setIdValue(T entity, Object id);
}
