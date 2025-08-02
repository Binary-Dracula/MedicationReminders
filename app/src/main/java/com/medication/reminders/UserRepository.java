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
     * Authenticate user login credentials
     * @param username Username
     * @param password Plain text password
     * @return AuthenticationResult containing login status and message
     */
    public AuthenticationResult authenticateUser(String username, String password) {
        try {
            // Validate input parameters
            if (username == null || username.trim().isEmpty()) {
                return new AuthenticationResult(false, "请输入用户名");
            }
            if (password == null || password.trim().isEmpty()) {
                return new AuthenticationResult(false, "请输入密码");
            }
            
            // Check if user exists
            UserInfo storedUser = getUserByUsername(username.trim());
            if (storedUser == null) {
                return new AuthenticationResult(false, "用户名不存在");
            }
            
            // Verify password
            String encryptedPassword = userDataSource.encryptPassword(password);
            if (encryptedPassword == null || !encryptedPassword.equals(storedUser.getPassword())) {
                // Increment login attempts on failed authentication
                incrementLoginAttempts(username.trim());
                return new AuthenticationResult(false, "密码错误");
            }
            
            // Check login attempts limit
            int attempts = getLoginAttempts(username.trim());
            if (attempts >= 3) {
                long lastAttemptTime = userDataSource.getLastLoginAttemptTime(username.trim());
                long currentTime = System.currentTimeMillis();
                long timeDiff = currentTime - lastAttemptTime;
                
                // 30 seconds = 30000 milliseconds
                if (timeDiff < 30000) {
                    return new AuthenticationResult(false, "登录失败次数过多，请稍后再试");
                } else {
                    // Reset attempts after 30 seconds
                    resetLoginAttempts(username.trim());
                }
            }
            
            // Authentication successful - reset login attempts
            resetLoginAttempts(username.trim());
            return new AuthenticationResult(true, "登录成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthenticationResult(false, "登录失败，请重试");
        }
    }
    
    /**
     * Save login credentials for "remember me" functionality
     * @param username Username to remember
     * @param password Plain text password to remember
     * @return true if save successful, false otherwise
     */
    public boolean saveLoginCredentials(String username, String password) {
        try {
            if (username == null || password == null) {
                return false;
            }
            
            return userDataSource.saveRememberedCredentials(username.trim(), password);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get saved login credentials
     * @return SavedCredentials object containing username and password, null if not found
     */
    public SavedCredentials getSavedCredentials() {
        try {
            return userDataSource.getRememberedCredentials();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Clear saved login credentials
     * @return true if clear successful, false otherwise
     */
    public boolean clearSavedCredentials() {
        try {
            return userDataSource.clearRememberedCredentials();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Increment login attempts for a user
     * @param username Username
     */
    public void incrementLoginAttempts(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        int currentAttempts = getLoginAttempts(username.trim());
        userDataSource.saveLoginAttempts(username.trim(), currentAttempts + 1);
    }
    
    /**
     * Get login attempts count for a user
     * @param username Username
     * @return Number of login attempts
     */
    public int getLoginAttempts(String username) {
        if (username == null || username.trim().isEmpty()) {
            return 0;
        }
        
        return userDataSource.getLoginAttempts(username.trim());
    }
    
    /**
     * Reset login attempts for a user
     * @param username Username
     */
    public void resetLoginAttempts(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        userDataSource.saveLoginAttempts(username.trim(), 0);
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
    
    /**
     * AuthenticationResult class to hold authentication operation results
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        
        public AuthenticationResult(boolean success, String message) {
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
    
    /**
     * SavedCredentials class to hold saved login credentials
     */
    public static class SavedCredentials {
        private final String username;
        private final String password;
        
        public SavedCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
    }
}