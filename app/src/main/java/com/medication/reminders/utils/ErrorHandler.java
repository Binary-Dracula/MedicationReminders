package com.medication.reminders.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.medication.reminders.R;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLException;

/**
 * 错误处理工具类
 * 提供统一的错误处理和用户友好的错误提示
 * 支持不同类型的异常处理和错误消息转换
 */
public class ErrorHandler {
    
    private static final String TAG = "ErrorHandler";
    
    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        NETWORK_ERROR,          // 网络错误
        DATABASE_ERROR,         // 数据库错误
        PERMISSION_ERROR,       // 权限错误
        STORAGE_ERROR,          // 存储错误
        VALIDATION_ERROR,       // 验证错误
        TIMEOUT_ERROR,          // 超时错误
        AUTHENTICATION_ERROR,   // 认证错误
        SYSTEM_ERROR,           // 系统错误
        UNKNOWN_ERROR          // 未知错误
    }
    
    /**
     * 错误信息类
     */
    public static class ErrorInfo {
        private final ErrorType type;
        private final String message;
        private final String friendlyMessage;
        private final Throwable cause;
        
        public ErrorInfo(ErrorType type, String message, String friendlyMessage, Throwable cause) {
            this.type = type;
            this.message = message;
            this.friendlyMessage = friendlyMessage;
            this.cause = cause;
        }
        
        public ErrorType getType() { return type; }
        public String getMessage() { return message; }
        public String getFriendlyMessage() { return friendlyMessage; }
        public Throwable getCause() { return cause; }
    }
    
    /**
     * 处理异常并返回错误信息
     * @param context 上下文
     * @param throwable 异常对象
     * @param operation 操作描述
     * @return 错误信息对象
     */
    public static ErrorInfo handleException(Context context, Throwable throwable, String operation) {
        if (context == null || throwable == null) {
            return new ErrorInfo(ErrorType.UNKNOWN_ERROR, 
                "未知错误", 
                context != null ? context.getString(R.string.friendly_error_general) : "操作失败", 
                throwable);
        }
        
        ErrorType errorType = determineErrorType(throwable);
        String technicalMessage = buildTechnicalMessage(throwable, operation);
        String friendlyMessage = getFriendlyMessage(context, errorType, throwable);
        
        // 记录错误日志
        logError(operation, technicalMessage, throwable);
        
        return new ErrorInfo(errorType, technicalMessage, friendlyMessage, throwable);
    }
    
    /**
     * 确定错误类型
     * @param throwable 异常对象
     * @return 错误类型
     */
    private static ErrorType determineErrorType(Throwable throwable) {
        if (throwable instanceof ConnectException || 
            throwable instanceof UnknownHostException) {
            return ErrorType.NETWORK_ERROR;
        } else if (throwable instanceof SocketTimeoutException) {
            return ErrorType.TIMEOUT_ERROR;
        } else if (throwable instanceof SQLException) {
            return ErrorType.DATABASE_ERROR;
        } else if (throwable instanceof SecurityException) {
            return ErrorType.PERMISSION_ERROR;
        } else if (throwable instanceof IllegalArgumentException ||
                   throwable instanceof IllegalStateException) {
            return ErrorType.VALIDATION_ERROR;
        } else if (throwable.getMessage() != null) {
            String message = throwable.getMessage().toLowerCase();
            if (message.contains("network") || message.contains("connection")) {
                return ErrorType.NETWORK_ERROR;
            } else if (message.contains("storage") || message.contains("space")) {
                return ErrorType.STORAGE_ERROR;
            } else if (message.contains("permission") || message.contains("unauthorized")) {
                return ErrorType.PERMISSION_ERROR;
            } else if (message.contains("timeout")) {
                return ErrorType.TIMEOUT_ERROR;
            } else if (message.contains("login") || message.contains("authentication")) {
                return ErrorType.AUTHENTICATION_ERROR;
            } else if (message.contains("database") || message.contains("sql")) {
                return ErrorType.DATABASE_ERROR;
            }
        }
        
        return ErrorType.UNKNOWN_ERROR;
    }
    
    /**
     * 构建技术错误消息
     * @param throwable 异常对象
     * @param operation 操作描述
     * @return 技术错误消息
     */
    private static String buildTechnicalMessage(Throwable throwable, String operation) {
        StringBuilder message = new StringBuilder();
        
        if (operation != null && !operation.isEmpty()) {
            message.append(operation).append("失败: ");
        }
        
        if (throwable.getMessage() != null) {
            message.append(throwable.getMessage());
        } else {
            message.append(throwable.getClass().getSimpleName());
        }
        
        return message.toString();
    }
    
    /**
     * 获取用户友好的错误消息
     * @param context 上下文
     * @param errorType 错误类型
     * @param throwable 异常对象
     * @return 用户友好的错误消息
     */
    private static String getFriendlyMessage(Context context, ErrorType errorType, Throwable throwable) {
        switch (errorType) {
            case NETWORK_ERROR:
                return context.getString(R.string.friendly_error_network);
            case DATABASE_ERROR:
                return context.getString(R.string.error_diary_database_failed);
            case PERMISSION_ERROR:
                return context.getString(R.string.friendly_error_permission);
            case STORAGE_ERROR:
                return context.getString(R.string.friendly_error_storage);
            case VALIDATION_ERROR:
                return context.getString(R.string.friendly_error_data);
            case TIMEOUT_ERROR:
                return context.getString(R.string.friendly_error_timeout);
            case AUTHENTICATION_ERROR:
                return context.getString(R.string.friendly_error_login);
            case SYSTEM_ERROR:
                return context.getString(R.string.friendly_error_system);
            default:
                return context.getString(R.string.friendly_error_general);
        }
    }
    
    /**
     * 记录错误日志
     * @param operation 操作描述
     * @param message 错误消息
     * @param throwable 异常对象
     */
    private static void logError(String operation, String message, Throwable throwable) {
        String logMessage = String.format("[%s] %s", operation != null ? operation : "Unknown", message);
        Log.e(TAG, logMessage, throwable);
    }
    
    /**
     * 显示错误Toast消息
     * @param context 上下文
     * @param errorInfo 错误信息
     */
    public static void showErrorToast(Context context, ErrorInfo errorInfo) {
        if (context != null && errorInfo != null) {
            Toast.makeText(context, errorInfo.getFriendlyMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 显示错误Toast消息（简化版本）
     * @param context 上下文
     * @param throwable 异常对象
     * @param operation 操作描述
     */
    public static void showErrorToast(Context context, Throwable throwable, String operation) {
        ErrorInfo errorInfo = handleException(context, throwable, operation);
        showErrorToast(context, errorInfo);
    }
    
    /**
     * 显示成功Toast消息
     * @param context 上下文
     * @param message 成功消息
     */
    public static void showSuccessToast(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示成功Toast消息（使用资源ID）
     * @param context 上下文
     * @param messageResId 成功消息资源ID
     */
    public static void showSuccessToast(Context context, int messageResId) {
        if (context != null) {
            Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示信息Toast消息
     * @param context 上下文
     * @param message 信息消息
     */
    public static void showInfoToast(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示信息Toast消息（使用资源ID）
     * @param context 上下文
     * @param messageResId 信息消息资源ID
     */
    public static void showInfoToast(Context context, int messageResId) {
        if (context != null) {
            Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 健康日记专用错误处理方法
     */
    public static class HealthDiary {
        
        /**
         * 处理健康日记相关的错误
         * @param context 上下文
         * @param throwable 异常对象
         * @param operation 操作类型（add, update, delete, load）
         * @return 错误信息对象
         */
        public static ErrorInfo handleDiaryError(Context context, Throwable throwable, String operation) {
            ErrorInfo baseError = handleException(context, throwable, "健康日记" + operation);
            
            // 根据操作类型提供更具体的错误消息
            String specificMessage = getSpecificDiaryErrorMessage(context, baseError.getType(), operation);
            
            return new ErrorInfo(baseError.getType(), baseError.getMessage(), specificMessage, baseError.getCause());
        }
        
        /**
         * 获取健康日记特定操作的错误消息
         * @param context 上下文
         * @param errorType 错误类型
         * @param operation 操作类型
         * @return 特定的错误消息
         */
        private static String getSpecificDiaryErrorMessage(Context context, ErrorType errorType, String operation) {
            switch (operation.toLowerCase()) {
                case "add":
                case "添加":
                    return getAddDiaryErrorMessage(context, errorType);
                case "update":
                case "更新":
                case "编辑":
                    return getUpdateDiaryErrorMessage(context, errorType);
                case "delete":
                case "删除":
                    return getDeleteDiaryErrorMessage(context, errorType);
                case "load":
                case "加载":
                case "获取":
                    return getLoadDiaryErrorMessage(context, errorType);
                default:
                    return getFriendlyMessage(context, errorType, null);
            }
        }
        
        private static String getAddDiaryErrorMessage(Context context, ErrorType errorType) {
            switch (errorType) {
                case NETWORK_ERROR:
                    return "网络连接异常，日记添加失败，请检查网络后重试";
                case DATABASE_ERROR:
                    return "数据保存失败，日记添加失败，请检查存储空间后重试";
                case STORAGE_ERROR:
                    return "存储空间不足，无法添加日记，请清理空间后重试";
                case AUTHENTICATION_ERROR:
                    return "登录状态已过期，请重新登录后添加日记";
                case VALIDATION_ERROR:
                    return "日记内容格式不正确，请检查后重新添加";
                default:
                    return context.getString(R.string.diary_save_failed);
            }
        }
        
        private static String getUpdateDiaryErrorMessage(Context context, ErrorType errorType) {
            switch (errorType) {
                case NETWORK_ERROR:
                    return "网络连接异常，日记更新失败，请检查网络后重试";
                case DATABASE_ERROR:
                    return "数据更新失败，日记更新失败，请稍后重试";
                case PERMISSION_ERROR:
                    return "没有权限更新此日记，请检查权限设置";
                case AUTHENTICATION_ERROR:
                    return "登录状态已过期，请重新登录后更新日记";
                case VALIDATION_ERROR:
                    return "日记内容格式不正确，请检查后重新更新";
                default:
                    return context.getString(R.string.diary_update_failed);
            }
        }
        
        private static String getDeleteDiaryErrorMessage(Context context, ErrorType errorType) {
            switch (errorType) {
                case NETWORK_ERROR:
                    return "网络连接异常，日记删除失败，请检查网络后重试";
                case DATABASE_ERROR:
                    return "数据删除失败，日记删除失败，请稍后重试";
                case PERMISSION_ERROR:
                    return "没有权限删除此日记，请检查权限设置";
                case AUTHENTICATION_ERROR:
                    return "登录状态已过期，请重新登录后删除日记";
                default:
                    return context.getString(R.string.diary_delete_failed);
            }
        }
        
        private static String getLoadDiaryErrorMessage(Context context, ErrorType errorType) {
            switch (errorType) {
                case NETWORK_ERROR:
                    return "网络连接异常，日记加载失败，请检查网络后重试";
                case DATABASE_ERROR:
                    return "数据读取失败，日记加载失败，请稍后重试";
                case AUTHENTICATION_ERROR:
                    return "登录状态已过期，请重新登录后加载日记";
                default:
                    return context.getString(R.string.diary_load_error);
            }
        }
        
        /**
         * 显示健康日记操作成功消息
         * @param context 上下文
         * @param operation 操作类型
         */
        public static void showDiarySuccessMessage(Context context, String operation) {
            if (context == null || operation == null) return;
            
            int messageResId;
            switch (operation.toLowerCase()) {
                case "add":
                case "添加":
                    messageResId = R.string.success_diary_added;
                    break;
                case "update":
                case "更新":
                case "编辑":
                    messageResId = R.string.success_diary_updated;
                    break;
                case "delete":
                case "删除":
                    messageResId = R.string.success_diary_deleted;
                    break;
                case "load":
                case "加载":
                    messageResId = R.string.success_diary_loaded;
                    break;
                default:
                    messageResId = R.string.success_operation_completed;
                    break;
            }
            
            showSuccessToast(context, messageResId);
        }
    }
    
    /**
     * 网络错误处理工具
     */
    public static class Network {
        
        /**
         * 检查是否为网络相关错误
         * @param throwable 异常对象
         * @return 是否为网络错误
         */
        public static boolean isNetworkError(Throwable throwable) {
            return throwable instanceof ConnectException ||
                   throwable instanceof UnknownHostException ||
                   throwable instanceof SocketTimeoutException ||
                   (throwable.getMessage() != null && 
                    (throwable.getMessage().toLowerCase().contains("network") ||
                     throwable.getMessage().toLowerCase().contains("connection")));
        }
        
        /**
         * 获取网络错误的友好提示
         * @param context 上下文
         * @return 网络错误提示
         */
        public static String getNetworkErrorMessage(Context context) {
            return context.getString(R.string.friendly_error_network);
        }
    }
    
    /**
     * 数据库错误处理工具
     */
    public static class Database {
        
        /**
         * 检查是否为数据库相关错误
         * @param throwable 异常对象
         * @return 是否为数据库错误
         */
        public static boolean isDatabaseError(Throwable throwable) {
            return throwable instanceof SQLException ||
                   (throwable.getMessage() != null && 
                    (throwable.getMessage().toLowerCase().contains("database") ||
                     throwable.getMessage().toLowerCase().contains("sql")));
        }
        
        /**
         * 获取数据库错误的友好提示
         * @param context 上下文
         * @return 数据库错误提示
         */
        public static String getDatabaseErrorMessage(Context context) {
            return context.getString(R.string.error_diary_database_failed);
        }
    }
}