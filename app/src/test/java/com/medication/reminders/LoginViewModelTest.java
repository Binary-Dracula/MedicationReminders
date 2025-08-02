package com.medication.reminders;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoginViewModel class
 * Tests login business logic, validation, and state management
 */
public class LoginViewModelTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private UserRepository mockUserRepository;
    
    private LoginViewModel loginViewModel;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loginViewModel = new LoginViewModel(mockUserRepository);
    }
    
    // Test constructor and initial state
    @Test
    public void testConstructor_InitializesCorrectly() {
        assertNotNull(loginViewModel.getErrorMessage());
        assertNotNull(loginViewModel.getIsLoading());
        assertNotNull(loginViewModel.getLoginSuccess());
        assertNotNull(loginViewModel.getRememberMe());
        assertNotNull(loginViewModel.getSavedUsername());
        assertNotNull(loginViewModel.getSavedPassword());
        
        // Check initial values
        assertFalse(loginViewModel.getIsLoading().getValue());
        assertFalse(loginViewModel.getLoginSuccess().getValue());
        assertFalse(loginViewModel.getRememberMe().getValue());
        assertEquals("", loginViewModel.getSavedUsername().getValue());
        assertEquals("", loginViewModel.getSavedPassword().getValue());
    }
    
    // Test validateLoginInput method
    @Test
    public void testValidateLoginInput_BothEmpty_ReturnsError() {
        String result = loginViewModel.validateLoginInput("", "");
        assertEquals("请输入用户名和密码", result);
    }
    
    @Test
    public void testValidateLoginInput_BothNull_ReturnsError() {
        String result = loginViewModel.validateLoginInput(null, null);
        assertEquals("请输入用户名和密码", result);
    }
    
    @Test
    public void testValidateLoginInput_UsernameEmpty_ReturnsError() {
        String result = loginViewModel.validateLoginInput("", "password123");
        assertEquals("请输入用户名", result);
    }
    
    @Test
    public void testValidateLoginInput_UsernameNull_ReturnsError() {
        String result = loginViewModel.validateLoginInput(null, "password123");
        assertEquals("请输入用户名", result);
    }
    
    @Test
    public void testValidateLoginInput_PasswordEmpty_ReturnsError() {
        String result = loginViewModel.validateLoginInput("testuser", "");
        assertEquals("请输入密码", result);
    }
    
    @Test
    public void testValidateLoginInput_PasswordNull_ReturnsError() {
        String result = loginViewModel.validateLoginInput("testuser", null);
        assertEquals("请输入密码", result);
    }
    
    @Test
    public void testValidateLoginInput_UsernameTooShort_ReturnsError() {
        String result = loginViewModel.validateLoginInput("ab", "password123");
        assertEquals("用户名至少需要3个字符", result);
    }
    
    @Test
    public void testValidateLoginInput_UsernameTooLong_ReturnsError() {
        String result = loginViewModel.validateLoginInput("a".repeat(21), "password123");
        assertEquals("用户名不能超过20个字符", result);
    }
    
    @Test
    public void testValidateLoginInput_PasswordTooShort_ReturnsError() {
        String result = loginViewModel.validateLoginInput("testuser", "12345");
        assertEquals("密码至少需要6个字符", result);
    }
    
    @Test
    public void testValidateLoginInput_PasswordTooLong_ReturnsError() {
        String result = loginViewModel.validateLoginInput("testuser", "a".repeat(21));
        assertEquals("密码不能超过20个字符", result);
    }
    
    @Test
    public void testValidateLoginInput_ValidInput_ReturnsNull() {
        String result = loginViewModel.validateLoginInput("testuser", "password123");
        assertNull(result);
    }
    
    @Test
    public void testValidateLoginInput_ValidInputWithSpaces_ReturnsNull() {
        String result = loginViewModel.validateLoginInput("  testuser  ", "password123");
        assertNull(result);
    }
    
    // Test loginUser method - successful login
    @Test
    public void testLoginUser_SuccessfulLogin_WithRememberMe() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        UserRepository.AuthenticationResult successResult = 
            new UserRepository.AuthenticationResult(true, "登录成功");
        
        when(mockUserRepository.getLoginAttempts(username)).thenReturn(0);
        when(mockUserRepository.authenticateUser(username, password)).thenReturn(successResult);
        when(mockUserRepository.saveLoginCredentials(username, password)).thenReturn(true);
        
        // Act
        loginViewModel.loginUser(username, password, true);
        
        // Assert
        assertTrue(loginViewModel.getLoginSuccess().getValue());
        assertTrue(loginViewModel.getRememberMe().getValue());
        assertEquals("登录成功", loginViewModel.getErrorMessage().getValue());
        assertFalse(loginViewModel.getIsLoading().getValue());
        
        verify(mockUserRepository).authenticateUser(username, password);
        verify(mockUserRepository).saveLoginCredentials(username, password);
    }
    
    @Test
    public void testLoginUser_SuccessfulLogin_WithoutRememberMe() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        UserRepository.AuthenticationResult successResult = 
            new UserRepository.AuthenticationResult(true, "登录成功");
        
        when(mockUserRepository.getLoginAttempts(username)).thenReturn(0);
        when(mockUserRepository.authenticateUser(username, password)).thenReturn(successResult);
        when(mockUserRepository.clearSavedCredentials()).thenReturn(true);
        
        // Act
        loginViewModel.loginUser(username, password, false);
        
        // Assert
        assertTrue(loginViewModel.getLoginSuccess().getValue());
        assertFalse(loginViewModel.getRememberMe().getValue());
        assertEquals("登录成功", loginViewModel.getErrorMessage().getValue());
        assertFalse(loginViewModel.getIsLoading().getValue());
        
        verify(mockUserRepository).authenticateUser(username, password);
        verify(mockUserRepository).clearSavedCredentials();
        verify(mockUserRepository, never()).saveLoginCredentials(anyString(), anyString());
    }
    
    // Test loginUser method - failed login
    @Test
    public void testLoginUser_FailedLogin_WrongPassword() {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";
        UserRepository.AuthenticationResult failResult = 
            new UserRepository.AuthenticationResult(false, "密码错误");
        
        when(mockUserRepository.getLoginAttempts(username)).thenReturn(0);
        when(mockUserRepository.authenticateUser(username, password)).thenReturn(failResult);
        
        // Act
        loginViewModel.loginUser(username, password, false);
        
        // Assert
        assertFalse(loginViewModel.getLoginSuccess().getValue());
        assertEquals("密码错误", loginViewModel.getErrorMessage().getValue());
        assertFalse(loginViewModel.getIsLoading().getValue());
        
        verify(mockUserRepository).authenticateUser(username, password);
        verify(mockUserRepository, never()).saveLoginCredentials(anyString(), anyString());
    }
    
    @Test
    public void testLoginUser_FailedLogin_UserNotExists() {
        // Arrange
        String username = "nonexistentuser";
        String password = "password123";
        UserRepository.AuthenticationResult failResult = 
            new UserRepository.AuthenticationResult(false, "用户名不存在");
        
        when(mockUserRepository.getLoginAttempts(username)).thenReturn(0);
        when(mockUserRepository.authenticateUser(username, password)).thenReturn(failResult);
        
        // Act
        loginViewModel.loginUser(username, password, false);
        
        // Assert
        assertFalse(loginViewModel.getLoginSuccess().getValue());
        assertEquals("用户名不存在", loginViewModel.getErrorMessage().getValue());
        assertFalse(loginViewModel.getIsLoading().getValue());
    }
    
    // Test login attempts limit
    @Test
    public void testLoginUser_TooManyAttempts_BlocksLogin() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        
        when(mockUserRepository.getLoginAttempts(username)).thenReturn(3);
        
        // Act
        loginViewModel.loginUser(username, password, false);
        
        // Assert
        assertFalse(loginViewModel.getLoginSuccess().getValue());
        assertEquals("登录失败次数过多，请稍后再试", loginViewModel.getErrorMessage().getValue());
        assertFalse(loginViewModel.getIsLoading().getValue());
        
        verify(mockUserRepository, never()).authenticateUser(anyString(), anyString());
    }
    
    // Test input validation in loginUser
    @Test
    public void testLoginUser_InvalidInput_ShowsValidationError() {
        // Act
        loginViewModel.loginUser("", "", false);
        
        // Assert
        assertFalse(loginViewModel.getLoginSuccess().getValue());
        assertEquals("请输入用户名和密码", loginViewModel.getErrorMessage().getValue());
        assertFalse(loginViewModel.getIsLoading().getValue());
        
        verify(mockUserRepository, never()).authenticateUser(anyString(), anyString());
    }
    
    // Test loadSavedCredentials method
    @Test
    public void testLoadSavedCredentials_WithSavedData_LoadsCorrectly() {
        // Arrange
        UserRepository.SavedCredentials savedCredentials = 
            new UserRepository.SavedCredentials("testuser", "password123");
        when(mockUserRepository.getSavedCredentials()).thenReturn(savedCredentials);
        
        // Act
        loginViewModel.loadSavedCredentials();
        
        // Assert
        assertEquals("testuser", loginViewModel.getSavedUsername().getValue());
        assertEquals("password123", loginViewModel.getSavedPassword().getValue());
        assertTrue(loginViewModel.getRememberMe().getValue());
        
        verify(mockUserRepository).getSavedCredentials();
    }
    
    @Test
    public void testLoadSavedCredentials_NoSavedData_SetsEmptyValues() {
        // Arrange
        when(mockUserRepository.getSavedCredentials()).thenReturn(null);
        
        // Act
        loginViewModel.loadSavedCredentials();
        
        // Assert
        assertEquals("", loginViewModel.getSavedUsername().getValue());
        assertEquals("", loginViewModel.getSavedPassword().getValue());
        assertFalse(loginViewModel.getRememberMe().getValue());
        
        verify(mockUserRepository).getSavedCredentials();
    }
    
    @Test
    public void testLoadSavedCredentials_ExceptionThrown_SetsEmptyValues() {
        // Arrange
        when(mockUserRepository.getSavedCredentials()).thenThrow(new RuntimeException("Database error"));
        
        // Act
        loginViewModel.loadSavedCredentials();
        
        // Assert
        assertEquals("", loginViewModel.getSavedUsername().getValue());
        assertEquals("", loginViewModel.getSavedPassword().getValue());
        assertFalse(loginViewModel.getRememberMe().getValue());
    }
    
    // Test clearSavedCredentials method
    @Test
    public void testClearSavedCredentials_Success_ClearsValues() {
        // Arrange
        when(mockUserRepository.clearSavedCredentials()).thenReturn(true);
        
        // Act
        loginViewModel.clearSavedCredentials();
        
        // Assert
        assertEquals("", loginViewModel.getSavedUsername().getValue());
        assertEquals("", loginViewModel.getSavedPassword().getValue());
        assertFalse(loginViewModel.getRememberMe().getValue());
        
        verify(mockUserRepository).clearSavedCredentials();
    }
    
    @Test
    public void testClearSavedCredentials_Failure_DoesNotClearValues() {
        // Arrange
        loginViewModel.getSavedUsername().setValue("testuser");
        loginViewModel.getSavedPassword().setValue("password123");
        loginViewModel.getRememberMe().setValue(true);
        
        when(mockUserRepository.clearSavedCredentials()).thenReturn(false);
        
        // Act
        loginViewModel.clearSavedCredentials();
        
        // Assert - values should remain unchanged
        assertEquals("testuser", loginViewModel.getSavedUsername().getValue());
        assertEquals("password123", loginViewModel.getSavedPassword().getValue());
        assertTrue(loginViewModel.getRememberMe().getValue());
        
        verify(mockUserRepository).clearSavedCredentials();
    }
    
    @Test
    public void testClearSavedCredentials_ExceptionThrown_DoesNotClearValues() {
        // Arrange
        loginViewModel.getSavedUsername().setValue("testuser");
        loginViewModel.getSavedPassword().setValue("password123");
        loginViewModel.getRememberMe().setValue(true);
        
        when(mockUserRepository.clearSavedCredentials()).thenThrow(new RuntimeException("Database error"));
        
        // Act
        loginViewModel.clearSavedCredentials();
        
        // Assert - values should remain unchanged
        assertEquals("testuser", loginViewModel.getSavedUsername().getValue());
        assertEquals("password123", loginViewModel.getSavedPassword().getValue());
        assertTrue(loginViewModel.getRememberMe().getValue());
    }
    
    // Test utility methods
    @Test
    public void testSetRememberMe_SetsCorrectValue() {
        loginViewModel.setRememberMe(true);
        assertTrue(loginViewModel.getRememberMe().getValue());
        
        loginViewModel.setRememberMe(false);
        assertFalse(loginViewModel.getRememberMe().getValue());
    }
    
    @Test
    public void testClearErrorMessage_ClearsMessage() {
        loginViewModel.getErrorMessage().setValue("Some error");
        loginViewModel.clearErrorMessage();
        assertNull(loginViewModel.getErrorMessage().getValue());
    }
    
    @Test
    public void testResetLoginSuccess_ResetsFalse() {
        loginViewModel.getLoginSuccess().setValue(true);
        loginViewModel.resetLoginSuccess();
        assertFalse(loginViewModel.getLoginSuccess().getValue());
    }
}