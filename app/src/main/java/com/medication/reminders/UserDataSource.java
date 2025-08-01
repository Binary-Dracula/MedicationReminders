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
            
            // Save user information to MMKV
            mmkv.encode(USER_USERNAME_KEY, userInfo.getUsername());
            mmkv.encode(USER_PHONE_KEY, userInfo.getPhoneNumber());
            mmkv.encode(USER_EMAIL_KEY, userInfo.getEmail());
            mmkv.encode(USER_PASSWORD_KEY, encryptedPassword);
            mmkv.encode(USER_REGISTER_TIME_KEY, System.currentTimeMillis());
            
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
            
            // Check if user exists by comparing stored username
            String storedUsername = mmkv.decodeString(USER_USERNAME_KEY, "");
            if (!username.equals(storedUsername)) {
                return null;
            }
            
            // Retrieve user information from MMKV
            String phoneNumber = mmkv.decodeString(USER_PHONE_KEY, "");
            String email = mmkv.decodeString(USER_EMAIL_KEY, "");
            String encryptedPassword = mmkv.decodeString(USER_PASSWORD_KEY, "");
            
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
        
        String storedUsername = mmkv.decodeString(USER_USERNAME_KEY, "");
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
}