package com.medication.reminders.models;

/**
 * 基础数据访问接口
 * 定义系统中所有数据访问操作的通用边界
 * 为Repository层提供统一的数据访问抽象
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public interface BaseDataAccess<T, ID> {
    
    /**
     * 插入新实体
     * 
     * @param entity 要插入的实体
     * @param callback 操作结果回调
     */
    void insert(T entity, RepositoryCallback<ID> callback);
    
    /**
     * 更新现有实体
     * 
     * @param entity 要更新的实体
     * @param callback 操作结果回调
     */
    void update(T entity, RepositoryCallback<Boolean> callback);
    
    /**
     * 根据ID删除实体
     * 
     * @param id 实体ID
     * @param callback 操作结果回调
     */
    void deleteById(ID id, RepositoryCallback<Boolean> callback);
    
    /**
     * 根据ID查找实体
     * 
     * @param id 实体ID
     * @param callback 查询结果回调
     */
    void findById(ID id, RepositoryCallback<T> callback);
    
    /**
     * 检查实体是否存在
     * 
     * @param id 实体ID
     * @param callback 检查结果回调
     */
    void exists(ID id, RepositoryCallback<Boolean> callback);
}