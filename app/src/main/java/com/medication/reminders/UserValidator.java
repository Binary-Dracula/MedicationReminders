package com.medication.reminders;

import java.util.regex.Pattern;

/**
 * UserValidator class for validating user input data
 * Provides validation methods for username, phone number, email, and password
 */
public class UserValidator {

    // Regular expression patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^1[3-9]\\d{9}$"
    );

    /**
     * Validate username
     * @param username Username to validate
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "用户名不能为空");
        }
        
        String trimmedUsername = username.trim();
        if (trimmedUsername.length() < 3) {
            return new ValidationResult(false, "用户名至少需要3个字符");
        }
        
        if (trimmedUsername.length() > 20) {
            return new ValidationResult(false, "用户名不能超过20个字符");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate phone number (11-digit Chinese phone number format)
     * @param phoneNumber Phone number to validate
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return new ValidationResult(false, "手机号不能为空");
        }
        
        String trimmedPhone = phoneNumber.trim();
        if (!PHONE_PATTERN.matcher(trimmedPhone).matches()) {
            return new ValidationResult(false, "请输入正确的手机号格式");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate email address
     * @param email Email address to validate
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "邮箱不能为空");
        }
        
        String trimmedEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            return new ValidationResult(false, "请输入正确的邮箱格式");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate password
     * @param password Password to validate
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "密码不能为空");
        }
        
        if (password.length() < 6) {
            return new ValidationResult(false, "密码至少需要6个字符");
        }
        
        if (password.length() > 20) {
            return new ValidationResult(false, "密码不能超过20个字符");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validate all user information
     * @param userInfo UserInfo object to validate
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validateUserInfo(UserInfo userInfo) {
        if (userInfo == null) {
            return new ValidationResult(false, "用户信息不能为空");
        }

        ValidationResult usernameResult = validateUsername(userInfo.getUsername());
        if (!usernameResult.isValid()) {
            return usernameResult;
        }

        ValidationResult phoneResult = validatePhoneNumber(userInfo.getPhoneNumber());
        if (!phoneResult.isValid()) {
            return phoneResult;
        }

        ValidationResult emailResult = validateEmail(userInfo.getEmail());
        if (!emailResult.isValid()) {
            return emailResult;
        }

        ValidationResult passwordResult = validatePassword(userInfo.getPassword());
        if (!passwordResult.isValid()) {
            return passwordResult;
        }

        return new ValidationResult(true, null);
    }

    /**
     * ValidationResult class to hold validation results
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}