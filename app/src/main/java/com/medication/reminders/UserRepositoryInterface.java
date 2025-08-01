package com.medication.reminders;

/**
 * Interface for UserRepository to enable testing with mock implementations
 */
public interface UserRepositoryInterface {
    
    /**
     * Save user information after validation
     * @param userInfo UserInfo object to save
     * @return RepositoryResult containing operation status and message
     */
    UserRepository.RepositoryResult saveUser(UserInfo userInfo);
    
    /**
     * Validate user information using UserValidator
     * @param userInfo UserInfo object to validate
     * @return ValidationResult containing validation status and error message
     */
    UserValidator.ValidationResult validateUserInfo(UserInfo userInfo);
    
    /**
     * Check if username already exists
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    boolean isUsernameExists(String username);
}