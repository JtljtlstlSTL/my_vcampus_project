package com.vcampus.common.dao;

import java.util.List;
import java.util.Optional;

/**
 * 通用DAO接口 - 提供基础的CRUD操作
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public interface IDao<T, ID> {
    
    /**
     * 根据ID查找实体
     */
    Optional<T> findById(ID id);
    
    /**
     * 查找所有实体
     */
    List<T> findAll();
    
    /**
     * 保存实体（新增或更新）
     */
    T save(T entity);
    
    /**
     * 批量保存
     */
    List<T> saveAll(List<T> entities);
    
    /**
     * 删除实体
     */
    void delete(T entity);
    
    /**
     * 根据ID删除
     */
    void deleteById(ID id);
    
    /**
     * 统计总数
     */
    long count();
    
    /**
     * 检查是否存在
     */
    boolean existsById(ID id);
}
