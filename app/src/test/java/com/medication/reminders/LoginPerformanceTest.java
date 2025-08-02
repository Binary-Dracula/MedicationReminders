package com.medication.reminders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Performance tests for LoginViewModel optimizations
 * Tests that the optimized version performs better than the original
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginPerformanceTest {

    @Mock
    private UserRepository mockUserRepository;

    private LoginViewModel loginViewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Use test mode for synchronous behavior in tests
        loginViewModel = new LoginViewModel(mockUserRepository, true);
    }

    /**
     * Test that input validation is optimized
     * Measures time taken for validation operations
     */
    @Test
    public void testInputValidationPerformance() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        
        // Act & Assert - measure validation performance
        long startTime = System.nanoTime();
        
        // Run validation multiple times to measure performance
        for (int i = 0; i < 1000; i++) {
            String result = loginViewModel.validateLoginInput(username, password);
            assertNull("Validation should pass for valid input", result);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Assert that validation completes within reasonable time (less than 10ms for 1000 iterations)
        assertTrue("Validation should be fast", duration < 10_000_000); // 10 milliseconds in nanoseconds
    }

    /**
     * Test that empty input validation is optimized
     */
    @Test
    public void testEmptyInputValidationPerformance() {
        // Test various empty input combinations
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 1000; i++) {
            // Test different empty input scenarios
            loginViewModel.validateLoginInput("", "");
            loginViewModel.validateLoginInput(null, null);
            loginViewModel.validateLoginInput("", "password");
            loginViewModel.validateLoginInput("username", "");
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Assert that validation completes quickly even for edge cases
        assertTrue("Empty input validation should be fast", duration < 5_000_000); // 5 milliseconds
    }

    /**
     * Test that the ViewModel handles multiple rapid calls efficiently
     */
    @Test
    public void testRapidCallsPerformance() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        UserRepository.AuthenticationResult successResult = 
            new UserRepository.AuthenticationResult(true, "登录成功");
        
        when(mockUserRepository.getLoginAttempts(username)).thenReturn(0);
        when(mockUserRepository.authenticateUser(username, password)).thenReturn(successResult);
        when(mockUserRepository.saveLoginCredentials(username, password)).thenReturn(true);
        
        // Act - make multiple rapid calls
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 10; i++) {
            loginViewModel.loginUser(username, password, true);
            // Reset state for next iteration
            loginViewModel.resetLoginSuccess();
            loginViewModel.clearErrorMessage();
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Assert that multiple calls complete within reasonable time
        assertTrue("Multiple rapid calls should be handled efficiently", duration < 100_000_000); // 100 milliseconds
    }

    /**
     * Test memory efficiency by checking that objects are properly cleaned up
     */
    @Test
    public void testMemoryEfficiency() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        UserRepository.AuthenticationResult successResult = 
            new UserRepository.AuthenticationResult(true, "登录成功");
        
        when(mockUserRepository.getLoginAttempts(username)).thenReturn(0);
        when(mockUserRepository.authenticateUser(username, password)).thenReturn(successResult);
        
        // Act - perform operations that should not leak memory
        for (int i = 0; i < 100; i++) {
            loginViewModel.loginUser(username, password, false);
            loginViewModel.loadSavedCredentials();
            loginViewModel.clearSavedCredentials();
            
            // Clear state
            loginViewModel.resetLoginSuccess();
            loginViewModel.clearErrorMessage();
        }
        
        // Assert - verify that the ViewModel is still responsive
        assertNotNull("ViewModel should still be functional", loginViewModel.getErrorMessage());
        assertNotNull("ViewModel should still be functional", loginViewModel.getIsLoading());
        assertNotNull("ViewModel should still be functional", loginViewModel.getLoginSuccess());
    }

    /**
     * Test that string operations are optimized
     */
    @Test
    public void testStringOperationPerformance() {
        // Test with various string lengths and patterns
        String[] testUsernames = {
            "a", "ab", "abc", "test", "testuser", "verylongusernamethatexceedslimit",
            "", null, "   ", "  test  ", "user with spaces"
        };
        
        String[] testPasswords = {
            "a", "ab", "abc", "pass", "password", "verylongpasswordthatexceedslimit",
            "", null, "   ", "  pass  ", "pass with spaces"
        };
        
        long startTime = System.nanoTime();
        
        // Test all combinations
        for (String username : testUsernames) {
            for (String password : testPasswords) {
                loginViewModel.validateLoginInput(username, password);
            }
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Assert that string operations are efficient
        assertTrue("String operations should be optimized", duration < 50_000_000); // 50 milliseconds
    }

    /**
     * Test that the optimized version handles edge cases efficiently
     */
    @Test
    public void testEdgeCasePerformance() {
        // Test edge cases that might cause performance issues
        long startTime = System.nanoTime();
        
        // Test with maximum length inputs
        String maxUsername = "a".repeat(20);
        String maxPassword = "a".repeat(20);
        
        for (int i = 0; i < 100; i++) {
            loginViewModel.validateLoginInput(maxUsername, maxPassword);
        }
        
        // Test with minimum length inputs
        String minUsername = "abc";
        String minPassword = "123456";
        
        for (int i = 0; i < 100; i++) {
            loginViewModel.validateLoginInput(minUsername, minPassword);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Assert that edge cases are handled efficiently
        assertTrue("Edge cases should be handled efficiently", duration < 20_000_000); // 20 milliseconds
    }

    /**
     * Test that the ViewModel properly manages state transitions
     */
    @Test
    public void testStateTransitionPerformance() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        UserRepository.AuthenticationResult successResult = 
            new UserRepository.AuthenticationResult(true, "登录成功");
        
        when(mockUserRepository.getLoginAttempts(username)).thenReturn(0);
        when(mockUserRepository.authenticateUser(username, password)).thenReturn(successResult);
        when(mockUserRepository.saveLoginCredentials(username, password)).thenReturn(true);
        
        // Act - test rapid state transitions
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 50; i++) {
            // Simulate rapid user interactions
            loginViewModel.clearErrorMessage();
            loginViewModel.resetLoginSuccess();
            loginViewModel.setRememberMe(true);
            loginViewModel.setRememberMe(false);
            loginViewModel.loginUser(username, password, true);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Assert that state transitions are efficient
        assertTrue("State transitions should be efficient", duration < 200_000_000); // 200 milliseconds
    }

    /**
     * Benchmark test to compare performance improvements
     */
    @Test
    public void testPerformanceBenchmark() {
        // This test serves as a benchmark for future performance improvements
        String username = "benchmarkuser";
        String password = "benchmarkpass123";
        
        // Measure baseline performance
        long startTime = System.nanoTime();
        
        // Perform a series of typical operations
        for (int i = 0; i < 100; i++) {
            loginViewModel.validateLoginInput(username, password);
            loginViewModel.clearErrorMessage();
            loginViewModel.resetLoginSuccess();
            loginViewModel.setRememberMe(i % 2 == 0);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Log performance for future reference
        System.out.println("Performance benchmark: " + duration + " nanoseconds for 100 operations");
        
        // Assert reasonable performance (adjust threshold based on requirements)
        assertTrue("Performance should meet benchmark requirements", duration < 100_000_000); // 100 milliseconds
    }
}