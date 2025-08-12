package com.medication.reminders.models;

/**
 * 用户错误枚举类
 * 定义所有用户相关操作可能出现的错误类型和对应的用户友好错误信息
 * 用于统一错误处理和提供一致的用户体验
 */
public enum UserError {
    
    // ========== 注册验证错误 ==========
    USERNAME_EXISTS("Username already exists, please choose another username"),
    INVALID_USERNAME("Invalid username format, please use 3-20 characters: letters, numbers, and underscores only"),
    INVALID_EMAIL("Invalid email format, please enter a valid email address"),
    INVALID_PHONE("Invalid phone number format, please enter a valid phone number"),
    WEAK_PASSWORD("Password is too weak, please use 6-20 characters including letters and numbers"),
    INVALID_FULL_NAME("Invalid full name format, please enter a real name of 2-20 characters"),
    INVALID_BIRTH_DATE("Invalid birth date format, please select a valid date"),
    INVALID_GENDER("Please select gender"),

    // ========== 登录错误 ==========
    USER_NOT_FOUND("User not found, please check your username"),
    WRONG_PASSWORD("Incorrect password, please try again"),
    ACCOUNT_LOCKED("Account is locked, please try again later"),
    TOO_MANY_ATTEMPTS("Too many login attempts, please try again later"),
    LOGIN_FAILED("Login failed, please check your username and password"),

    // ========== 个人资料错误 ==========
    PROFILE_UPDATE_FAILED("Profile update failed, please try again"),
    INVALID_PROFILE_DATA("Profile data is incomplete or incorrectly formatted"),
    PHOTO_UPLOAD_FAILED("Photo upload failed, please try again"),
    INVALID_CONTACT_INFO("Contact information is incorrectly formatted"),
    INVALID_MEDICAL_INFO("Medical information is incorrectly formatted"),
    INVALID_ADDRESS("Address information is incorrectly formatted"),

    // ========== 密码管理错误 ==========
    CURRENT_PASSWORD_WRONG("Current password is incorrect"),
    NEW_PASSWORD_SAME("New password cannot be the same as the current password"),
    PASSWORD_CHANGE_FAILED("Password change failed, please try again"),

    // ========== 会话管理错误 ==========
    SESSION_EXPIRED("Session has expired, please log in again"),
    LOGOUT_FAILED("Logout failed, please try again"),
    AUTO_LOGIN_FAILED("Automatic login failed, please log in manually"),

    // ========== 数据验证错误 ==========
    EMPTY_USERNAME("Please enter a username"),
    EMPTY_PASSWORD("Please enter a password"),
    EMPTY_EMAIL("Please enter an email address"),
    EMPTY_PHONE("Please enter a phone number"),
    EMPTY_FULL_NAME("Please enter your full name"),
    FORM_VALIDATION_FAILED("Form validation failed, please check the input information"),

    // ========== 系统错误 ==========
    DATABASE_ERROR("Database operation failed, please try again"),
    NETWORK_ERROR("Network connection failed, please check your network settings"),
    PERMISSION_DENIED("Permission denied, unable to perform this operation"),
    FILE_OPERATION_ERROR("File operation failed, please try again"),
    UNKNOWN_ERROR("An unknown error occurred, please try again"),

    // ========== 业务逻辑错误 ==========
    USER_ALREADY_LOGGED_IN("User is already logged in"),
    USER_NOT_LOGGED_IN("User is not logged in, please log in first"),
    OPERATION_NOT_ALLOWED("This operation is not allowed in the current state"),
    DATA_CONFLICT("Data conflict, please refresh and try again"),
    RESOURCE_NOT_FOUND("The requested resource does not exist"),

    // ========== 输入限制错误 ==========
    USERNAME_TOO_SHORT("Username must be at least 3 characters long"),
    USERNAME_TOO_LONG("Username cannot exceed 20 characters"),
    PASSWORD_TOO_SHORT("Password must be at least 6 characters long"),
    PASSWORD_TOO_LONG("Password cannot exceed 20 characters"),
    FULL_NAME_TOO_SHORT("Full name must be at least 2 characters long"),
    FULL_NAME_TOO_LONG("Full name cannot exceed 20 characters"),
    PHONE_INVALID_LENGTH("Phone number length is incorrect"),
    EMAIL_TOO_LONG("Email address is too long"),

    // ========== 特殊业务错误 ==========
    EMERGENCY_CONTACT_REQUIRED("Emergency contact information is incomplete"),
    MEDICAL_INFO_INVALID("Medical information format is incorrect"),
    DOCTOR_INFO_INVALID("Doctor information format is incorrect"),
    HOSPITAL_INFO_INVALID("Hospital information format is incorrect"),
    BLOOD_TYPE_INVALID("Blood type information is incorrect"),
    ALLERGY_INFO_INVALID("Allergy information format is incorrect");

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
     * 检查是���为系统级错误
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

    @Override
    public String toString() {
        return "UserError{" +
                "code='" + getCode() + '\'' +
                ", message='" + message + '\'' +
                ", severity=" + getSeverity() +
                '}';
    }
}