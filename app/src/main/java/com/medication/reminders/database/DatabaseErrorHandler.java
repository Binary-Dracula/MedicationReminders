package com.medication.reminders.database;

import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * 数据库错误处理工具类
 * 提供统一的数据库错误处理和用户友好的错误信息
 */
public class DatabaseErrorHandler {
    
    private static final String TAG = "DatabaseErrorHandler";
    
    /**
     * 数据库错误类型枚举
     */
    public enum DatabaseError {
        // 约束违反错误
        USERNAME_EXISTS("用户名已存在"),
        EMAIL_EXISTS("邮箱地址已存在"),
        PHONE_EXISTS("电话号码已存在"),
        CONSTRAINT_VIOLATION("数据约束违反"),
        
        // 数据操作错误
        INSERT_FAILED("数据插入失败"),
        UPDATE_FAILED("数据更新失败"),
        DELETE_FAILED("数据删除失败"),
        QUERY_FAILED("数据查询失败"),
        
        // 连接和配置错误
        DATABASE_CONNECTION_FAILED("数据库连接失败"),
        DATABASE_CORRUPTED("数据库文件损坏"),
        DISK_FULL("存储空间不足"),
        
        // 通用错误
        UNKNOWN_ERROR("未知数据库错误"),
        OPERATION_TIMEOUT("操作超时"),
        PERMISSION_DENIED("权限不足");
        
        private final String message;
        
        DatabaseError(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * 数据库操作结果类
     * @param <T> 结果数据类型
     */
    public static class DatabaseResult<T> {
        private final boolean success;
        private final T data;
        private final DatabaseError error;
        private final String errorMessage;
        
        private DatabaseResult(boolean success, T data, DatabaseError error, String errorMessage) {
            this.success = success;
            this.data = data;
            this.error = error;
            this.errorMessage = errorMessage;
        }
        
        /**
         * 创建成功结果
         * @param data 结果数据
         * @param <T> 数据类型
         * @return 成功的DatabaseResult
         */
        public static <T> DatabaseResult<T> success(T data) {
            return new DatabaseResult<>(true, data, null, null);
        }
        
        /**
         * 创建失败结果
         * @param error 错误类型
         * @param <T> 数据类型
         * @return 失败的DatabaseResult
         */
        public static <T> DatabaseResult<T> failure(DatabaseError error) {
            return new DatabaseResult<>(false, null, error, error.getMessage());
        }
        
        /**
         * 创建失败结果（自定义错误信息）
         * @param error 错误类型
         * @param customMessage 自定义错误信息
         * @param <T> 数据类型
         * @return 失败的DatabaseResult
         */
        public static <T> DatabaseResult<T> failure(DatabaseError error, String customMessage) {
            return new DatabaseResult<>(false, null, error, customMessage);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public T getData() {
            return data;
        }
        
        public DatabaseError getError() {
            return error;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    /**
     * 处理数据库异常并返回用户友好的错误信息
     * @param exception 数据库异常
     * @param operation 操作类型描述
     * @return 处理后的错误信息
     */
    public static DatabaseError handleException(Exception exception, String operation) {
        Log.e(TAG, "数据库操作失败: " + operation, exception);
        
        if (exception instanceof SQLiteConstraintException) {
            return handleConstraintException((SQLiteConstraintException) exception);
        } else if (exception instanceof SQLiteException) {
            return handleSQLiteException((SQLiteException) exception);
        } else {
            return DatabaseError.UNKNOWN_ERROR;
        }
    }
    
    /**
     * 处理约束违反异常
     * @param exception 约束异常
     * @return 对应的错误类型
     */
    private static DatabaseError handleConstraintException(SQLiteConstraintException exception) {
        String message = exception.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            
            if (lowerMessage.contains("username")) {
                return DatabaseError.USERNAME_EXISTS;
            } else if (lowerMessage.contains("email")) {
                return DatabaseError.EMAIL_EXISTS;
            } else if (lowerMessage.contains("phone")) {
                return DatabaseError.PHONE_EXISTS;
            }
        }
        
        return DatabaseError.CONSTRAINT_VIOLATION;
    }
    
    /**
     * 处理SQLite异常
     * @param exception SQLite异常
     * @return 对应的错误类型
     */
    private static DatabaseError handleSQLiteException(SQLiteException exception) {
        String message = exception.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            
            if (lowerMessage.contains("disk") || lowerMessage.contains("space")) {
                return DatabaseError.DISK_FULL;
            } else if (lowerMessage.contains("corrupt")) {
                return DatabaseError.DATABASE_CORRUPTED;
            } else if (lowerMessage.contains("permission")) {
                return DatabaseError.PERMISSION_DENIED;
            } else if (lowerMessage.contains("timeout")) {
                return DatabaseError.OPERATION_TIMEOUT;
            }
        }
        
        return DatabaseError.UNKNOWN_ERROR;
    }
    
    /**
     * 执行数据库操作并处理异常
     * @param operation 数据库操作
     * @param operationName 操作名称（用于日志）
     * @param <T> 返回数据类型
     * @return 操作结果
     */
    public static <T> DatabaseResult<T> executeOperation(DatabaseOperation<T> operation, String operationName) {
        try {
            T result = operation.execute();
            Log.d(TAG, "数据库操作成功: " + operationName);
            return DatabaseResult.success(result);
        } catch (Exception e) {
            DatabaseError error = handleException(e, operationName);
            Log.e(TAG, "数据库操作失败: " + operationName + " - " + error.getMessage(), e);
            return DatabaseResult.failure(error);
        }
    }
    
    /**
     * 数据库操作接口
     * @param <T> 返回数据类型
     */
    public interface DatabaseOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * 验证插入操作结果
     * @param insertId 插入操作返回的ID
     * @return 验证结果
     */
    public static DatabaseResult<Long> validateInsertResult(long insertId) {
        if (insertId > 0) {
            return DatabaseResult.success(insertId);
        } else {
            return DatabaseResult.failure(DatabaseError.INSERT_FAILED);
        }
    }
    
    /**
     * 验证更新操作结果
     * @param affectedRows 更新操作影响的行数
     * @return 验证结果
     */
    public static DatabaseResult<Integer> validateUpdateResult(int affectedRows) {
        if (affectedRows > 0) {
            return DatabaseResult.success(affectedRows);
        } else {
            return DatabaseResult.failure(DatabaseError.UPDATE_FAILED);
        }
    }
    
    /**
     * 验证删除操作结果
     * @param affectedRows 删除操作影响的行数
     * @return 验证结果
     */
    public static DatabaseResult<Integer> validateDeleteResult(int affectedRows) {
        if (affectedRows > 0) {
            return DatabaseResult.success(affectedRows);
        } else {
            return DatabaseResult.failure(DatabaseError.DELETE_FAILED);
        }
    }
    
    /**
     * 验证查询操作结果
     * @param result 查询结果
     * @param <T> 结果类型
     * @return 验证结果
     */
    public static <T> DatabaseResult<T> validateQueryResult(T result) {
        if (result != null) {
            return DatabaseResult.success(result);
        } else {
            return DatabaseResult.failure(DatabaseError.QUERY_FAILED, "未找到匹配的数据");
        }
    }
    
    /**
     * 记录数据库操作日志
     * @param operation 操作类型
     * @param tableName 表名
     * @param success 是否成功
     * @param details 详细信息
     */
    public static void logDatabaseOperation(String operation, String tableName, boolean success, String details) {
        String logMessage = String.format("数据库操作 - 操作: %s, 表: %s, 状态: %s, 详情: %s", 
            operation, tableName, success ? "成功" : "失败", details);
        
        if (success) {
            Log.d(TAG, logMessage);
        } else {
            Log.e(TAG, logMessage);
        }
    }
    
    /**
     * 检查数据库连接状态
     * @param database 数据库实例
     * @return 连接状态检查结果
     */
    public static DatabaseResult<Boolean> checkDatabaseConnection(MedicationDatabase database) {
        try {
            if (database != null && database.isOpen()) {
                // 执行简单查询测试连接
                database.userDao().getUserCountSync();
                return DatabaseResult.success(true);
            } else {
                return DatabaseResult.failure(DatabaseError.DATABASE_CONNECTION_FAILED);
            }
        } catch (Exception e) {
            DatabaseError error = handleException(e, "数据库连接检查");
            return DatabaseResult.failure(error);
        }
    }
    
    /**
     * 获取数据库操作建议
     * @param error 错误类型
     * @return 操作建议
     */
    public static String getOperationSuggestion(DatabaseError error) {
        switch (error) {
            case USERNAME_EXISTS:
                return "请尝试使用其他用户名";
            case EMAIL_EXISTS:
                return "该邮箱已被注册，请使用其他邮箱或尝试登录";
            case PHONE_EXISTS:
                return "该电话号码已被注册，请使用其他号码或尝试登录";
            case DISK_FULL:
                return "请清理设备存储空间后重试";
            case DATABASE_CORRUPTED:
                return "数据库文件损坏，建议重新安装应用";
            case PERMISSION_DENIED:
                return "请检查应用权限设置";
            case OPERATION_TIMEOUT:
                return "操作超时，请检查网络连接后重试";
            default:
                return "请稍后重试，如问题持续存在请联系技术支持";
        }
    }
}