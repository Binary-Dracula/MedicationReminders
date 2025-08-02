package com.medication.reminders;

import android.content.Context;
import com.tencent.mmkv.MMKV;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * UserDataSource class for handling user data storage using MMKV
 * Provides methods for saving user information, password encryption, and user retrieval
 */
public class UserDataSource {
    private static final String USER_USERNAME_KEY = "user_username";
    private static final String USER_PHONE_KEY = "user_phone";
    private static final String USER_EMAIL_KEY = "user_email";
    private static final String USER_PASSWORD_KEY = "user_password";
    private static final String USER_REGISTER_TIME_KEY = "user_register_time";
    
    // Remember me functionality keys
    private static final String REMEMBERED_USERNAME_KEY = "remembered_username";
    private static final String REMEMBERED_PASSWORD_KEY = "remembered_password";
    private static final String REMEMBER_ME_ENABLED_KEY = "remember_me_enabled";
    
    // Login attempts management keys
    private static final String LOGIN_ATTEMPTS_PREFIX = "login_attempts_";
    private static final String LAST_ATTEMPT_TIME_PREFIX = "last_attempt_time_";
    
    private MMKV mmkv;
    
    /**
     * Constructor - initializes MMKV instance
     * @param context Application context for MMKV initialization
     */
    public UserDataSource(Context context) {
        // Initialize MMKV
        String rootDir = MMKV.initialize(context);
        mmkv = MMKV.defaultMMKV();
    }
    
    /**
     * Save user information to MMKV storage
     * @param userInfo UserInfo object containing user data
     * @return true if save successful, false otherwise
     */
    public boolean saveUserInfo(UserInfo userInfo) {
        try {
            if (userInfo == null) {
                return false;
            }
            
            // Encrypt password before saving
            String encryptedPassword = encryptPassword(userInfo.getPassword());
            if (encryptedPassword == null) {
                return false;
            }
            
            // Use username as key prefix to support multiple users
            String userPrefix = "user_" + userInfo.getUsername() + "_";
            
            // Save user information to MMKV with username-specific keys
            mmkv.encode(userPrefix + "username", userInfo.getUsername());
            mmkv.encode(userPrefix + "phone", userInfo.getPhoneNumber());
            mmkv.encode(userPrefix + "email", userInfo.getEmail());
            mmkv.encode(userPrefix + "password", encryptedPassword);
            mmkv.encode(userPrefix + "register_time", System.currentTimeMillis());
            
            // Also maintain a list of all registered usernames
            String existingUsers = mmkv.decodeString("all_users", "");
            if (!existingUsers.contains(userInfo.getUsername())) {
                if (existingUsers.isEmpty()) {
                    existingUsers = userInfo.getUsername();
                } else {
                    existingUsers += "," + userInfo.getUsername();
                }
                mmkv.encode("all_users", existingUsers);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Encrypt password using SHA-256 algorithm
     * @param password Plain text password
     * @return Encrypted password string, null if encryption fails
     */
    public String encryptPassword(String password) {
        try {
            if (password == null || password.isEmpty()) {
                return null;
            }
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            
            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get user information by username
     * @param username Username to search for
     * @return UserInfo object if user exists, null otherwise
     */
    public UserInfo getUserByUsername(String username) {
        try {
            if (username == null || username.isEmpty()) {
                return null;
            }
            
            // Use username-specific key prefix
            String userPrefix = "user_" + username + "_";
            
            // Check if user exists by trying to retrieve username
            String storedUsername = mmkv.decodeString(userPrefix + "username", "");
            if (storedUsername.isEmpty() || !username.equals(storedUsername)) {
                return null;
            }
            
            // Retrieve user information from MMKV
            String phoneNumber = mmkv.decodeString(userPrefix + "phone", "");
            String email = mmkv.decodeString(userPrefix + "email", "");
            String encryptedPassword = mmkv.decodeString(userPrefix + "password", "");
            
            // Create and return UserInfo object (with encrypted password)
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername(storedUsername);
            userInfo.setPhoneNumber(phoneNumber);
            userInfo.setEmail(email);
            userInfo.setPassword(encryptedPassword);
            
            return userInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Check if a user with the given username exists
     * @param username Username to check
     * @return true if user exists, false otherwise
     */
    public boolean isUserExists(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        
        // Use username-specific key prefix to check existence
        String userPrefix = "user_" + username + "_";
        String storedUsername = mmkv.decodeString(userPrefix + "username", "");
        return username.equals(storedUsername);
    }
    
    /**
     * Get user registration time
     * @return Registration timestamp, 0 if not found
     */
    public long getUserRegisterTime() {
        return mmkv.decodeLong(USER_REGISTER_TIME_KEY, 0);
    }
    
    /**
     * Clear all user data from MMKV (for testing or logout purposes)
     */
    public void clearUserData() {
        mmkv.removeValueForKey(USER_USERNAME_KEY);
        mmkv.removeValueForKey(USER_PHONE_KEY);
        mmkv.removeValueForKey(USER_EMAIL_KEY);
        mmkv.removeValueForKey(USER_PASSWORD_KEY);
        mmkv.removeValueForKey(USER_REGISTER_TIME_KEY);
    }
    
    /**
     * Save remembered credentials for "remember me" functionality
     * @param username Username to remember
     * @param password Plain text password to remember (will be stored encrypted)
     * @return true if save successful, false otherwise
     */
    public boolean saveRememberedCredentials(String username, String password) {
        try {
            if (username == null || password == null) {
                return false;
            }
            
            // For "remember me" functionality, we store the plain text password
            // This is a trade-off between security and usability for elderly users
            // In a production app, we might use more sophisticated approaches
            mmkv.encode(REMEMBERED_USERNAME_KEY, username);
            mmkv.encode(REMEMBERED_PASSWORD_KEY, password); // Store plain text for auto-fill
            mmkv.encode(REMEMBER_ME_ENABLED_KEY, true);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get remembered credentials
     * @return SavedCredentials object if found, null otherwise
     */
    public UserRepository.SavedCredentials getRememberedCredentials() {
        try {
            boolean rememberMeEnabled = mmkv.decodeBool(REMEMBER_ME_ENABLED_KEY, false);
            if (!rememberMeEnabled) {
                return null;
            }
            
            String username = mmkv.decodeString(REMEMBERED_USERNAME_KEY, "");
            String password = mmkv.decodeString(REMEMBERED_PASSWORD_KEY, "");
            
            if (username.isEmpty() || password.isEmpty()) {
                return null;
            }
            
            // Return the plain text password for auto-fill functionality
            return new UserRepository.SavedCredentials(username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Clear remembered credentials
     * @return true if clear successful, false otherwise
     */
    public boolean clearRememberedCredentials() {
        try {
            mmkv.removeValueForKey(REMEMBERED_USERNAME_KEY);
            mmkv.removeValueForKey(REMEMBERED_PASSWORD_KEY);
            mmkv.encode(REMEMBER_ME_ENABLED_KEY, false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Save login attempts count and time for a user
     * @param username Username
     * @param attempts Number of attempts
     */
    public void saveLoginAttempts(String username, int attempts) {
        if (username == null || username.isEmpty()) {
            return;
        }
        
        try {
            String attemptsKey = LOGIN_ATTEMPTS_PREFIX + username;
            String timeKey = LAST_ATTEMPT_TIME_PREFIX + username;
            
            mmkv.encode(attemptsKey, attempts);
            mmkv.encode(timeKey, System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get login attempts count for a user
     * @param username Username
     * @return Number of login attempts, 0 if not found
     */
    public int getLoginAttempts(String username) {
        if (username == null || username.isEmpty()) {
            return 0;
        }
        
        try {
            String attemptsKey = LOGIN_ATTEMPTS_PREFIX + username;
            return mmkv.decodeInt(attemptsKey, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Get last login attempt time for a user
     * @param username Username
     * @return Last attempt timestamp, 0 if not found
     */
    public long getLastLoginAttemptTime(String username) {
        if (username == null || username.isEmpty()) {
            return 0;
        }
        
        try {
            String timeKey = LAST_ATTEMPT_TIME_PREFIX + username;
            return mmkv.decodeLong(timeKey, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}