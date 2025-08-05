package com.medication.reminders.models;

/**
 * 用户错误枚举类
 * 定义所有用户相关操作可能出现的错误类型和对应的用户友好错误信息
 * 用于统一错误处理和提供一致的用户体验
 */
public enum UserError {
    
    // ========== 注册验证错误 ==========
    USERNAME_EXISTS("用户名已存在，请选择其他用户名"),
    INVALID_USERNAME("用户名格式不正确，请使用3-20个字符，只能包含字母、数字和下划线"),
    INVALID_EMAIL("邮箱格式不正确，请输入有效的邮箱地址"),
    INVALID_PHONE("电话号码格式不正确，请输入有效的手机号码"),
    WEAK_PASSWORD("密码强度不够，请使用6-20个字符，包含字母和数字"),
    INVALID_FULL_NAME("姓名格式不正确，请输入2-20个字符的真实姓名"),
    INVALID_BIRTH_DATE("出生日期格式不正确，请选择有效的日期"),
    INVALID_GENDER("请选择性别"),
    
    // ========== 登录错误 ==========
    USER_NOT_FOUND("用户不存在，请检查用户名是否正确"),
    WRONG_PASSWORD("密码错误，请重新输入"),
    ACCOUNT_LOCKED("账户已被锁定，请稍后再试"),
    TOO_MANY_ATTEMPTS("登录尝试次数过多，请稍后再试"),
    LOGIN_FAILED("登录失败，请检查用户名和密码"),
    
    // ========== 个人资料错误 ==========
    PROFILE_UPDATE_FAILED("个人资料更新失败，请重试"),
    INVALID_PROFILE_DATA("个人资料数据不完整或格式不正确"),
    PHOTO_UPLOAD_FAILED("头像上传失败，请重试"),
    INVALID_CONTACT_INFO("联系信息格式不正确"),
    INVALID_MEDICAL_INFO("医疗信息格式不正确"),
    INVALID_ADDRESS("地址信息格式不正确"),
    
    // ========== 密码管理错误 ==========
    CURRENT_PASSWORD_WRONG("当前密码不正确"),
    NEW_PASSWORD_SAME("新密码不能与当前密码相同"),
    PASSWORD_CHANGE_FAILED("密码修改失败，请重试"),
    
    // ========== 会话管理错误 ==========
    SESSION_EXPIRED("会话已过期，请重新登录"),
    LOGOUT_FAILED("登出失败，请重试"),
    AUTO_LOGIN_FAILED("自动登录失败，请手动登录"),
    
    // ========== 数据验证错误 ==========
    EMPTY_USERNAME("请输入用户名"),
    EMPTY_PASSWORD("请输入密码"),
    EMPTY_EMAIL("请输入邮箱地址"),
    EMPTY_PHONE("请输入电话号码"),
    EMPTY_FULL_NAME("请输入完整姓名"),
    FORM_VALIDATION_FAILED("表单验证失败，请检查输入信息"),
    
    // ========== 系统错误 ==========
    DATABASE_ERROR("数据库操作失败，请重试"),
    NETWORK_ERROR("网络连接失败，请检查网络设置"),
    PERMISSION_DENIED("权限不足，无法执行此操作"),
    FILE_OPERATION_ERROR("文件操作失败，请重试"),
    UNKNOWN_ERROR("发生未知错误，请重试"),
    
    // ========== 业务逻辑错误 ==========
    USER_ALREADY_LOGGED_IN("用户已登录"),
    USER_NOT_LOGGED_IN("用户未登录，请先登录"),
    OPERATION_NOT_ALLOWED("当前状态下不允许此操作"),
    DATA_CONFLICT("数据冲突，请刷新后重试"),
    RESOURCE_NOT_FOUND("请求的资源不存在"),
    
    // ========== 输入限制错误 ==========
    USERNAME_TOO_SHORT("用户名至少需要3个字符"),
    USERNAME_TOO_LONG("用户名不能超过20个字符"),
    PASSWORD_TOO_SHORT("密码至少需要6个字符"),
    PASSWORD_TOO_LONG("密码不能超过20个字符"),
    FULL_NAME_TOO_SHORT("姓名至少需要2个字符"),
    FULL_NAME_TOO_LONG("姓名不能超过20个字符"),
    PHONE_INVALID_LENGTH("电话号码长度不正确"),
    EMAIL_TOO_LONG("邮箱地址过长"),
    
    // ========== 特殊业务错误 ==========
    EMERGENCY_CONTACT_REQUIRED("紧急联系人信息不完整"),
    MEDICAL_INFO_INVALID("医疗信息格式不正确"),
    DOCTOR_INFO_INVALID("医生信息格式不正确"),
    HOSPITAL_INFO_INVALID("医院信息格式不正确"),
    BLOOD_TYPE_INVALID("血型信息不正确"),
    ALLERGY_INFO_INVALID("过敏信息格式不正确");
    
    private final String message;
    
    /**
     * 构造函数
     * @param message 用户友好的错误信息
     */
    UserError(String message) {
        this.message = message;
    }
    
    /**
     * 获取错误信息
     * @return 用户友好的错误信息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 获取错误代码
     * @return 错误枚举的名称作为错误代码
     */
    public String getCode() {
        return this.name();
    }
    
    /**
     * 根据错误代码获取UserError枚举
     * @param code 错误代码
     * @return 对应的UserError枚举，如果不存在则返回UNKNOWN_ERROR
     */
    public static UserError fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return UNKNOWN_ERROR;
        }
        
        try {
            return UserError.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN_ERROR;
        }
    }
    
    /**
     * 检查是否为系统级错误
     * @return 如果是系统级错误返回true，否则返回false
     */
    public boolean isSystemError() {
        return this == DATABASE_ERROR || 
               this == NETWORK_ERROR || 
               this == PERMISSION_DENIED || 
               this == FILE_OPERATION_ERROR || 
               this == UNKNOWN_ERROR;
    }
    
    /**
     * 检查是否为验证错误
     * @return 如果是验证错误返回true，否则返回false
     */
    public boolean isValidationError() {
        return this == INVALID_USERNAME || 
               this == INVALID_EMAIL || 
               this == INVALID_PHONE || 
               this == WEAK_PASSWORD || 
               this == INVALID_FULL_NAME || 
               this == INVALID_BIRTH_DATE || 
               this == INVALID_GENDER ||
               this == EMPTY_USERNAME ||
               this == EMPTY_PASSWORD ||
               this == EMPTY_EMAIL ||
               this == EMPTY_PHONE ||
               this == EMPTY_FULL_NAME ||
               this == FORM_VALIDATION_FAILED;
    }
    
    /**
     * 检查是否为认证错误
     * @return 如果是认证错误返回true，否则返回false
     */
    public boolean isAuthenticationError() {
        return this == USER_NOT_FOUND || 
               this == WRONG_PASSWORD || 
               this == ACCOUNT_LOCKED || 
               this == TOO_MANY_ATTEMPTS || 
               this == LOGIN_FAILED ||
               this == SESSION_EXPIRED ||
               this == USER_NOT_LOGGED_IN;
    }
    
    /**
     * 检查是否为业务逻辑错误
     * @return 如果是业务逻辑错误返回true，否则返回false
     */
    public boolean isBusinessError() {
        return this == USERNAME_EXISTS || 
               this == USER_ALREADY_LOGGED_IN || 
               this == OPERATION_NOT_ALLOWED || 
               this == DATA_CONFLICT || 
               this == RESOURCE_NOT_FOUND;
    }
    
    /**
     * 获取错误的严重程度
     * @return 错误严重程度：1-低，2-中，3-高
     */
    public int getSeverity() {
        if (isSystemError()) {
            return 3; // 高严重程度
        } else if (isAuthenticationError() || isBusinessError()) {
            return 2; // 中等严重程度
        } else {
            return 1; // 低严重程度
        }
    }
    
    /**
     * 获取建议的用户操作
     * @return 建议用户采取的操作
     */
    public String getSuggestedAction() {
        switch (this) {
            case USERNAME_EXISTS:
                return "请尝试其他用户名";
            case INVALID_USERNAME:
            case INVALID_EMAIL:
            case INVALID_PHONE:
            case WEAK_PASSWORD:
            case INVALID_FULL_NAME:
                return "请检查并修正输入格式";
            case USER_NOT_FOUND:
            case WRONG_PASSWORD:
                return "请检查用户名和密码";
            case ACCOUNT_LOCKED:
            case TOO_MANY_ATTEMPTS:
                return "请稍后再试";
            case SESSION_EXPIRED:
                return "请重新登录";
            case DATABASE_ERROR:
            case NETWORK_ERROR:
                return "请检查网络连接后重试";
            default:
                return "请重试或联系客服";
        }
    }
    
    @Override
    public String toString() {
        return "UserError{" +
                "code='" + getCode() + '\'' +
                ", message='" + message + '\'' +
                ", severity=" + getSeverity() +
                '}';
    }
}