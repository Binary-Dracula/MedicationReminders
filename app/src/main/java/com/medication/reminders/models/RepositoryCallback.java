package com.medication.reminders.models;

/**
 * Repository操作回调接口
 * 用于异步数据库操作的结果回调
 * @param <T> 回调数据类型
 */
public interface RepositoryCallback<T> {
    
    /**
     * 操作成功时调用
     * @param result 操作结果数据
     */
    void onSuccess(T result);
    
    /**
     * 操作失败时调用
     * @param error 错误信息
     */
    void onError(String error);
}