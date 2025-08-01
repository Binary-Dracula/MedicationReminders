package com.medication.reminders;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for RegisterViewModel
 */
public class RegisterViewModelTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    private TestUserRepository testUserRepository;
    private RegisterViewModel registerViewModel;
    
    @Before
    public void setUp() {
        testUserRepository = new TestUserRepository();
        registerViewModel = new RegisterViewModel(testUserRepository);
    }
    
    @Test
    public void testInitialState() {
        // Test initial LiveData states
        assertNotNull(registerViewModel.getErrorMessage());
        assertNotNull(registerViewModel.getIsLoading());
        assertNotNull(registerViewModel.getRegisterSuccess());
        
        // Initial loading state should be false
        assertEquals(Boolean.FALSE, registerViewModel.getIsLoading().getValue());
        // Initial success state should be false
        assertEquals(Boolean.FALSE, registerViewModel.getRegisterSuccess().getValue());
    }
    
    @Test
    public void testRegisterUser_Success() {
        // Arrange
        String username = "testuser";
        String phone = "13812345678";
        String email = "test@example.com";
        String password = "password123";
        
        testUserRepository.setShouldSucceed(true);
        
        // Act
        registerViewModel.registerUser(username, phone, email, password);
        
        // Assert
        assertEquals(Boolean.TRUE, registerViewModel.getRegisterSuccess().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getIsLoading().getValue());
        assertNull(registerViewModel.getErrorMessage().getValue());
    }
    
    @Test
    public void testRegisterUser_ValidationError() {
        // Arrange
        String username = "ab"; // Too short
        String phone = "13812345678";
        String email = "test@example.com";
        String password = "password123";
        String expectedError = "用户名至少需要3个字符";
        
        testUserRepository.setShouldSucceed(false);
        testUserRepository.setErrorMessage(expectedError);
        
        // Act
        registerViewModel.registerUser(username, phone, email, password);
        
        // Assert
        assertEquals(expectedError, registerViewModel.getErrorMessage().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getIsLoading().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getRegisterSuccess().getValue());
    }
    
    @Test
    public void testRegisterUser_UsernameExists() {
        // Arrange
        String username = "existinguser";
        String phone = "13812345678";
        String email = "test@example.com";
        String password = "password123";
        
        testUserRepository.setShouldSucceed(false);
        testUserRepository.setErrorMessage("用户名已存在，请选择其他用户名");
        
        // Act
        registerViewModel.registerUser(username, phone, email, password);
        
        // Assert
        assertEquals("用户名已存在，请选择其他用户名", registerViewModel.getErrorMessage().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getIsLoading().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getRegisterSuccess().getValue());
    }
    
    @Test
    public void testRegisterUser_SaveFailure() {
        // Arrange
        String username = "testuser";
        String phone = "13812345678";
        String email = "test@example.com";
        String password = "password123";
        
        testUserRepository.setShouldSucceed(false);
        testUserRepository.setErrorMessage("注册失败，请重试");
        
        // Act
        registerViewModel.registerUser(username, phone, email, password);
        
        // Assert
        assertEquals("注册失败，请重试", registerViewModel.getErrorMessage().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getIsLoading().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getRegisterSuccess().getValue());
    }
    
    @Test
    public void testRegisterUser_Exception() {
        // Arrange
        String username = "testuser";
        String phone = "13812345678";
        String email = "test@example.com";
        String password = "password123";
        
        testUserRepository.setShouldThrowException(true);
        
        // Act
        registerViewModel.registerUser(username, phone, email, password);
        
        // Assert
        assertEquals("注册过程中发生错误，请检查网络连接后重试", registerViewModel.getErrorMessage().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getIsLoading().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getRegisterSuccess().getValue());
    }
    
    @Test
    public void testRegisterUser_LoadingState() {
        // Arrange
        String username = "testuser";
        String phone = "13812345678";
        String email = "test@example.com";
        String password = "password123";
        
        testUserRepository.setShouldSucceed(true);
        
        // Act
        registerViewModel.registerUser(username, phone, email, password);
        
        // Assert - loading should be false after completion
        assertEquals(Boolean.FALSE, registerViewModel.getIsLoading().getValue());
    }
    
    @Test
    public void testRegisterUser_StateReset() {
        // Arrange - first set some error state
        String username1 = "ab"; // Invalid username
        testUserRepository.setShouldSucceed(false);
        testUserRepository.setErrorMessage("用户名至少需要3个字符");
        registerViewModel.registerUser(username1, "13812345678", "test@example.com", "password123");
        
        // Verify error state is set
        assertNotNull(registerViewModel.getErrorMessage().getValue());
        
        // Act - register with valid data
        String username2 = "validuser";
        testUserRepository.setShouldSucceed(true);
        testUserRepository.setErrorMessage(null);
        
        registerViewModel.registerUser(username2, "13812345678", "test@example.com", "password123");
        
        // Assert - previous error should be cleared
        assertNull(registerViewModel.getErrorMessage().getValue());
        assertEquals(Boolean.TRUE, registerViewModel.getRegisterSuccess().getValue());
    }
    
    @Test
    public void testRegisterUser_NullParameters() {
        // Test null username
        registerViewModel.registerUser(null, "13812345678", "test@example.com", "password123");
        assertEquals("所有字段都必须填写", registerViewModel.getErrorMessage().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getRegisterSuccess().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getIsLoading().getValue());
        
        // Test null phone
        registerViewModel.registerUser("testuser", null, "test@example.com", "password123");
        assertEquals("所有字段都必须填写", registerViewModel.getErrorMessage().getValue());
        
        // Test null email
        registerViewModel.registerUser("testuser", "13812345678", null, "password123");
        assertEquals("所有字段都必须填写", registerViewModel.getErrorMessage().getValue());
        
        // Test null password
        registerViewModel.registerUser("testuser", "13812345678", "test@example.com", null);
        assertEquals("所有字段都必须填写", registerViewModel.getErrorMessage().getValue());
    }
    
    @Test
    public void testRegisterUser_EmptyStrings() {
        // Test empty username
        registerViewModel.registerUser("", "13812345678", "test@example.com", "password123");
        assertEquals("所有字段都必须填写，请检查输入内容", registerViewModel.getErrorMessage().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getRegisterSuccess().getValue());
        
        // Test empty phone
        registerViewModel.registerUser("testuser", "", "test@example.com", "password123");
        assertEquals("所有字段都必须填写，请检查输入内容", registerViewModel.getErrorMessage().getValue());
        
        // Test empty email
        registerViewModel.registerUser("testuser", "13812345678", "", "password123");
        assertEquals("所有字段都必须填写，请检查输入内容", registerViewModel.getErrorMessage().getValue());
        
        // Test empty password
        registerViewModel.registerUser("testuser", "13812345678", "test@example.com", "");
        assertEquals("所有字段都必须填写，请检查输入内容", registerViewModel.getErrorMessage().getValue());
    }
    
    @Test
    public void testRegisterUser_WhitespaceHandling() {
        // Test whitespace trimming
        String username = "  testuser  ";
        String phone = "  13812345678  ";
        String email = "  test@example.com  ";
        String password = "password123";
        
        testUserRepository.setShouldSucceed(true);
        
        registerViewModel.registerUser(username, phone, email, password);
        
        assertEquals(Boolean.TRUE, registerViewModel.getRegisterSuccess().getValue());
        assertEquals(Boolean.FALSE, registerViewModel.getIsLoading().getValue());
        assertNull(registerViewModel.getErrorMessage().getValue());
    }
    
    @Test
    public void testRegisterUser_InvalidUsername() {
        // Test username too short
        registerViewModel.registerUser("ab", "13812345678", "test@example.com", "password123");
        assertEquals("用户名至少需要3个字符", registerViewModel.getErrorMessage().getValue());
        
        // Test username too long
        StringBuilder longUsernameBuilder = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            longUsernameBuilder.append("a");
        }
        String longUsername = longUsernameBuilder.toString();
        registerViewModel.registerUser(longUsername, "13812345678", "test@example.com", "password123");
        assertEquals("用户名不能超过20个字符", registerViewModel.getErrorMessage().getValue());
    }
    
    @Test
    public void testRegisterUser_InvalidPhone() {
        // Test invalid phone format
        registerViewModel.registerUser("testuser", "12345", "test@example.com", "password123");
        assertEquals("请输入正确的手机号格式", registerViewModel.getErrorMessage().getValue());
        
        // Test phone with wrong starting digit
        registerViewModel.registerUser("testuser", "12812345678", "test@example.com", "password123");
        assertEquals("请输入正确的手机号格式", registerViewModel.getErrorMessage().getValue());
    }
    
    @Test
    public void testRegisterUser_InvalidEmail() {
        // Test invalid email format
        registerViewModel.registerUser("testuser", "13812345678", "invalid-email", "password123");
        assertEquals("请输入正确的邮箱格式", registerViewModel.getErrorMessage().getValue());
        
        // Test email without domain
        registerViewModel.registerUser("testuser", "13812345678", "test@", "password123");
        assertEquals("请输入正确的邮箱格式", registerViewModel.getErrorMessage().getValue());
    }
    
    @Test
    public void testRegisterUser_InvalidPassword() {
        // Test password too short
        registerViewModel.registerUser("testuser", "13812345678", "test@example.com", "12345");
        assertEquals("密码至少需要6个字符", registerViewModel.getErrorMessage().getValue());
        
        // Test password too long
        StringBuilder longPasswordBuilder = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            longPasswordBuilder.append("a");
        }
        String longPassword = longPasswordBuilder.toString();
        registerViewModel.registerUser("testuser", "13812345678", "test@example.com", longPassword);
        assertEquals("密码不能超过20个字符", registerViewModel.getErrorMessage().getValue());
    }
    
    @Test
    public void testRegisterUser_MultipleValidationErrors() {
        // Test that first validation error is returned
        registerViewModel.registerUser("ab", "invalid", "invalid-email", "123");
        assertEquals("用户名至少需要3个字符", registerViewModel.getErrorMessage().getValue());
    }
    
    /**
     * Test implementation of UserRepository for unit testing
     */
    private static class TestUserRepository implements UserRepositoryInterface {
        private boolean shouldSucceed = true;
        private String errorMessage = null;
        private boolean shouldThrowException = false;
        
        public TestUserRepository() {
            // No parent constructor call needed
        }
        
        public void setShouldSucceed(boolean shouldSucceed) {
            this.shouldSucceed = shouldSucceed;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public void setShouldThrowException(boolean shouldThrowException) {
            this.shouldThrowException = shouldThrowException;
        }
        
        @Override
        public UserRepository.RepositoryResult saveUser(UserInfo userInfo) {
            if (shouldThrowException) {
                throw new RuntimeException("Test exception");
            }
            
            if (shouldSucceed) {
                return new UserRepository.RepositoryResult(true, "注册成功");
            } else {
                return new UserRepository.RepositoryResult(false, errorMessage != null ? errorMessage : "注册失败，请重试");
            }
        }
        
        @Override
        public UserValidator.ValidationResult validateUserInfo(UserInfo userInfo) {
            // Use actual UserValidator for proper testing
            return UserValidator.validateUserInfo(userInfo);
        }
        
        @Override
        public boolean isUsernameExists(String username) {
            return false; // For testing, assume username doesn't exist
        }
    }
}