package com.medication.reminders;

import android.content.Context;

/**
 * UserRepository class implementing Repository pattern
 * Coordinates between UserDataSource and UserValidator to handle user operations
 */
public class UserRepository implements UserRepositoryInterface {
    private UserDataSource userDataSource;
    
    /**
     * Constructor - initializes UserDataSource
     * @param context Application context
     */
    public UserRepository(Context context) {
        this.userDataSource = new UserDataSource(context);
    }
    
    /**
     * Save user information after validation
     * @param userInfo UserInfo object to save
     * @return RepositoryResult containing operation status and message
     */
    public RepositoryResult saveUser(UserInfo userInfo) {
        try {
            // First validate user information
            UserValidator.ValidationResult validationResult = validateUserInfo(userInfo);
            if (!validationResult.isValid()) {
                return new RepositoryResult(false, validationResult.getErrorMessage());
            }
            
            // Check if username already exists
            if (isUsernameExists(userInfo.getUsername())) {
                return new RepositoryResult(false, "用户名已存在，请选择其他用户名");
            }
            
            // Save user information using UserDataSource
            boolean saveSuccess = userDataSource.saveUserInfo(userInfo);
            if (saveSuccess) {
                return new RepositoryResult(true, "注册成功");
            } else {
                return new RepositoryResult(false, "注册失败，请重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new RepositoryResult(false, "注册失败，请重试");
        }
    }
    
    /**
     * Validate user information using UserValidator
     * @param userInfo UserInfo object to validate
     * @return ValidationResult containing validation status and error message
     */
    public UserValidator.ValidationResult validateUserInfo(UserInfo userInfo) {
        return UserValidator.validateUserInfo(userInfo);
    }
    
    /**
     * Check if username already exists
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    public boolean isUsernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        return userDataSource.isUserExists(username.trim());
    }
    
    /**
     * Get user information by username
     * @param username Username to search for
     * @return UserInfo object if found, null otherwise
     */
    public UserInfo getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        return userDataSource.getUserByUsername(username.trim());
    }
    
    /**
     * Verify user credentials (for future login functionality)
     * @param username Username
     * @param password Plain text password
     * @return true if credentials are valid, false otherwise
     */
    public boolean verifyUserCredentials(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        
        UserInfo storedUser = getUserByUsername(username);
        if (storedUser == null) {
            return false;
        }
        
        // Encrypt the provided password and compare with stored encrypted password
        String encryptedPassword = userDataSource.encryptPassword(password);
        return encryptedPassword != null && encryptedPassword.equals(storedUser.getPassword());
    }
    
    /**
     * Get user registration time
     * @return Registration timestamp, 0 if not found
     */
    public long getUserRegisterTime() {
        return userDataSource.getUserRegisterTime();
    }
    
    /**
     * Clear all user data (for testing or logout purposes)
     */
    public void clearUserData() {
        userDataSource.clearUserData();
    }
    
    /**
     * RepositoryResult class to hold repository operation results
     */
    public static class RepositoryResult {
        private final boolean success;
        private final String message;
        
        public RepositoryResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}