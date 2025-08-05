package com.medication.reminders.models;


import com.medication.reminders.repository.UserRepository;
import com.medication.reminders.utils.UserValidator;

/**
 * Interface for UserRepository to enable testing with mock implementations
 * 用户仓库接口，支持测试时使用模拟实现
 */
public interface UserRepositoryInterface {
    
    /**
     * Save user information after validation
     * 保存用户信息（验证后）
     * @param username 用户名
     * @param email 邮箱
     * @param phone 电话
     * @param password 密码
     * @return RepositoryResult containing operation status and message
     */
    UserRepository.RepositoryResult saveUser(String username, String email, String phone, String password);
    
    /**
     * Validate user information using UserValidator
     * 使用UserValidator验证用户信息
     * @param username 用户名
     * @param email 邮箱
     * @param phone 电话
     * @param password 密码
     * @return ValidationResult containing validation status and error message
     */
    UserValidator.ValidationResult validateUserInfo(String username, String email, String phone, String password);
    
    /**
     * Check if username already exists
     * 检查用户名是否已存在
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    boolean isUsernameExists(String username);
}