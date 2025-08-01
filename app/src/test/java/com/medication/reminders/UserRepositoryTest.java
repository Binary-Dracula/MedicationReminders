package com.medication.reminders;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for UserRepository class
 * Note: These tests focus on validation logic without MMKV operations
 */
public class UserRepositoryTest {

    private UserInfo validUserInfo;
    
    @Before
    public void setUp() {
        // Create a valid UserInfo for testing
        validUserInfo = new UserInfo();
        validUserInfo.setUsername("testuser");
        validUserInfo.setPhoneNumber("13812345678");
        validUserInfo.setEmail("test@example.com");
        validUserInfo.setPassword("password123");
    }
    
    @Test
    public void testValidateUserInfo_ValidUser_ReturnsTrue() {
        // Test validation logic directly through UserValidator
        UserValidator.ValidationResult result = UserValidator.validateUserInfo(validUserInfo);
        
        assertTrue("Valid user info should pass validation", result.isValid());
        assertNull("Valid user info should have no error message", result.getErrorMessage());
    }
    
    @Test
    public void testValidateUserInfo_InvalidUsername_ReturnsFalse() {
        // Test with short username
        UserInfo invalidUser = new UserInfo();
        invalidUser.setUsername("ab"); // Too short
        invalidUser.setPhoneNumber("13812345678");
        invalidUser.setEmail("test@example.com");
        invalidUser.setPassword("password123");
        
        UserValidator.ValidationResult result = UserValidator.validateUserInfo(invalidUser);
        
        assertFalse("Invalid username should fail validation", result.isValid());
        assertEquals("Should return correct error message", "用户名至少需要3个字符", result.getErrorMessage());
    }
    
    @Test
    public void testValidateUserInfo_InvalidPhoneNumber_ReturnsFalse() {
        // Test with invalid phone number
        UserInfo invalidUser = new UserInfo();
        invalidUser.setUsername("testuser");
        invalidUser.setPhoneNumber("12345"); // Invalid format
        invalidUser.setEmail("test@example.com");
        invalidUser.setPassword("password123");
        
        UserValidator.ValidationResult result = UserValidator.validateUserInfo(invalidUser);
        
        assertFalse("Invalid phone number should fail validation", result.isValid());
        assertEquals("Should return correct error message", "请输入正确的手机号格式", result.getErrorMessage());
    }
    
    @Test
    public void testValidateUserInfo_InvalidEmail_ReturnsFalse() {
        // Test with invalid email
        UserInfo invalidUser = new UserInfo();
        invalidUser.setUsername("testuser");
        invalidUser.setPhoneNumber("13812345678");
        invalidUser.setEmail("invalid-email"); // Invalid format
        invalidUser.setPassword("password123");
        
        UserValidator.ValidationResult result = UserValidator.validateUserInfo(invalidUser);
        
        assertFalse("Invalid email should fail validation", result.isValid());
        assertEquals("Should return correct error message", "请输入正确的邮箱格式", result.getErrorMessage());
    }
    
    @Test
    public void testValidateUserInfo_InvalidPassword_ReturnsFalse() {
        // Test with short password
        UserInfo invalidUser = new UserInfo();
        invalidUser.setUsername("testuser");
        invalidUser.setPhoneNumber("13812345678");
        invalidUser.setEmail("test@example.com");
        invalidUser.setPassword("123"); // Too short
        
        UserValidator.ValidationResult result = UserValidator.validateUserInfo(invalidUser);
        
        assertFalse("Invalid password should fail validation", result.isValid());
        assertEquals("Should return correct error message", "密码至少需要6个字符", result.getErrorMessage());
    }
    
    @Test
    public void testValidateUserInfo_NullUserInfo_ReturnsFalse() {
        UserValidator.ValidationResult result = UserValidator.validateUserInfo(null);
        
        assertFalse("Null user info should fail validation", result.isValid());
        assertEquals("Should return correct error message", "用户信息不能为空", result.getErrorMessage());
    }
    
    @Test
    public void testRepositoryResult_Success() {
        UserRepository.RepositoryResult result = new UserRepository.RepositoryResult(true, "Success message");
        
        assertTrue("Result should be successful", result.isSuccess());
        assertEquals("Should return correct message", "Success message", result.getMessage());
    }
    
    @Test
    public void testRepositoryResult_Failure() {
        UserRepository.RepositoryResult result = new UserRepository.RepositoryResult(false, "Error message");
        
        assertFalse("Result should be unsuccessful", result.isSuccess());
        assertEquals("Should return correct message", "Error message", result.getMessage());
    }
    
    // Note: The following tests would require proper mocking of MMKV and UserDataSource
    // In a real testing environment, we would use dependency injection to mock UserDataSource
    
    /*
    @Test
    public void testSaveUser_ValidUser_ReturnsSuccess() {
        // This test would require mocking UserDataSource
        // when(mockUserDataSource.isUserExists(anyString())).thenReturn(false);
        // when(mockUserDataSource.saveUserInfo(any(UserInfo.class))).thenReturn(true);
        
        // UserRepository.RepositoryResult result = userRepository.saveUser(validUserInfo);
        
        // assertTrue("Valid user should be saved successfully", result.isSuccess());
        // assertEquals("Should return success message", "注册成功", result.getMessage());
    }
    
    @Test
    public void testSaveUser_ExistingUsername_ReturnsFailure() {
        // This test would require mocking UserDataSource
        // when(mockUserDataSource.isUserExists(anyString())).thenReturn(true);
        
        // UserRepository.RepositoryResult result = userRepository.saveUser(validUserInfo);
        
        // assertFalse("Existing username should fail", result.isSuccess());
        // assertEquals("Should return error message", "用户名已存在，请选择其他用户名", result.getMessage());
    }
    
    @Test
    public void testIsUsernameExists_ExistingUser_ReturnsTrue() {
        // This test would require mocking UserDataSource
        // when(mockUserDataSource.isUserExists("testuser")).thenReturn(true);
        
        // boolean exists = userRepository.isUsernameExists("testuser");
        
        // assertTrue("Existing username should return true", exists);
    }
    
    @Test
    public void testIsUsernameExists_NonExistingUser_ReturnsFalse() {
        // This test would require mocking UserDataSource
        // when(mockUserDataSource.isUserExists("nonexistent")).thenReturn(false);
        
        // boolean exists = userRepository.isUsernameExists("nonexistent");
        
        // assertFalse("Non-existing username should return false", exists);
    }
    */
}