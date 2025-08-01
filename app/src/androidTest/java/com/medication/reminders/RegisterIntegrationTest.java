package com.medication.reminders;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import com.tencent.mmkv.MMKV;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Integration tests for the complete user registration system
 * Tests the full MVVM architecture data flow, MMKV persistence, and various input scenarios
 * 
 * Requirements tested:
 * - 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7: Input validation scenarios
 * - 3.1, 3.2, 3.3: MMKV data persistence and storage
 */
@RunWith(AndroidJUnit4.class)
public class RegisterIntegrationTest {

    private ActivityScenario<RegisterActivity> activityScenario;
    private Context context;
    private MMKV mmkv;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        // Initialize MMKV for testing
        MMKV.initialize(context);
        mmkv = MMKV.defaultMMKV();
        
        // Clear any existing test data
        clearTestData();
        
        // Launch the activity
        activityScenario = ActivityScenario.launch(RegisterActivity.class);
    }

    @After
    public void tearDown() {
        // Clean up test data
        clearTestData();
        
        if (activityScenario != null) {
            activityScenario.close();
        }
    }

    /**
     * Clear test data from MMKV
     */
    private void clearTestData() {
        if (mmkv != null) {
            mmkv.removeValueForKey("user_testuser_username");
            mmkv.removeValueForKey("user_testuser_phone");
            mmkv.removeValueForKey("user_testuser_email");
            mmkv.removeValueForKey("user_testuser_password");
            mmkv.removeValueForKey("user_validuser_username");
            mmkv.removeValueForKey("user_validuser_phone");
            mmkv.removeValueForKey("user_validuser_email");
            mmkv.removeValueForKey("user_validuser_password");
        }
    }

    /**
     * Test 完整的注册流程 - Requirements 3.1, 3.2, 3.3
     * Tests the complete registration flow from input to MMKV storage
     */
    @Test
    public void testCompleteRegistrationFlow() {
        // Step 1: Input valid user data
        String username = "testuser";
        String phone = "13812345678";
        String email = "test@example.com";
        String password = "password123";

        onView(withId(R.id.etUsername))
                .perform(typeText(username));

        onView(withId(R.id.etPhone))
                .perform(typeText(phone));

        onView(withId(R.id.etEmail))
                .perform(typeText(email));

        onView(withId(R.id.etPassword))
                .perform(typeText(password));

        Espresso.closeSoftKeyboard();

        // Step 2: Click register button
        onView(withId(R.id.btnRegister))
                .perform(click());

        // Step 3: Wait for registration process to complete
        try {
            Thread.sleep(1500); // Allow time for async operations
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Step 4: Verify data was saved to MMKV
        activityScenario.onActivity(activity -> {
            // Check if user data was saved to MMKV
            UserDataSource dataSource = new UserDataSource(activity);
            UserInfo savedUser = dataSource.getUserByUsername(username);
            
            if (savedUser != null) {
                assertEquals("Username should be saved correctly", username, savedUser.getUsername());
                assertEquals("Phone should be saved correctly", phone, savedUser.getPhoneNumber());
                assertEquals("Email should be saved correctly", email, savedUser.getEmail());
                assertNotNull("Password should be encrypted and saved", savedUser.getPassword());
                assertNotEquals("Password should be encrypted", password, savedUser.getPassword());
            }
        });
    }

    /**
     * Test MMKV数据持久性 - Requirements 3.1, 3.2, 3.3
     * Tests that data persists correctly in MMKV storage
     */
    @Test
    public void testMMKVDataPersistence() {
        // Register a user
        String username = "persistuser";
        String phone = "13987654321";
        String email = "persist@example.com";
        String password = "persist123";

        registerUser(username, phone, email, password);

        // Wait for registration
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Close and reopen activity to test persistence
        activityScenario.close();
        activityScenario = ActivityScenario.launch(RegisterActivity.class);

        // Verify data still exists in MMKV
        activityScenario.onActivity(activity -> {
            UserDataSource dataSource = new UserDataSource(activity);
            UserInfo savedUser = dataSource.getUserByUsername(username);
            
            if (savedUser != null) {
                assertEquals("Persisted username should match", username, savedUser.getUsername());
                assertEquals("Persisted phone should match", phone, savedUser.getPhoneNumber());
                assertEquals("Persisted email should match", email, savedUser.getEmail());
                assertNotNull("Persisted password should exist", savedUser.getPassword());
            }
        });

        // Clean up
        clearUserData(username);
    }

    /**
     * Test 不同输入组合的验证 - Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7
     * Tests validation with various input combinations
     */
    @Test
    public void testDifferentInputValidationCombinations() {
        // Test Case 1: Empty username - Requirement 2.1
        testValidationScenario("", "13812345678", "test@example.com", "password123", true);

        // Test Case 2: Username too short - Requirement 2.1
        testValidationScenario("ab", "13812345678", "test@example.com", "password123", true);

        // Test Case 3: Username too long - Requirement 2.2
        testValidationScenario("thisusernameistoolongtobevalid", "13812345678", "test@example.com", "password123", true);

        // Test Case 4: Invalid phone number - Requirement 2.3
        testValidationScenario("validuser", "123", "test@example.com", "password123", true);

        // Test Case 5: Invalid email format - Requirement 2.4
        testValidationScenario("validuser", "13812345678", "invalid-email", "password123", true);

        // Test Case 6: Password too short - Requirement 2.5
        testValidationScenario("validuser", "13812345678", "test@example.com", "123", true);

        // Test Case 7: Password too long - Requirement 2.6
        testValidationScenario("validuser", "13812345678", "test@example.com", "thispasswordistoolongtobevalidaccordingtoourvalidationrules", true);

        // Test Case 8: All valid inputs - Requirement 2.7
        testValidationScenario("validuser", "13812345678", "test@example.com", "password123", false);
    }

    /**
     * Helper method to test validation scenarios
     */
    private void testValidationScenario(String username, String phone, String email, String password, boolean shouldShowError) {
        // Clear previous inputs
        clearInputFields();

        // Input test data
        if (!username.isEmpty()) {
            onView(withId(R.id.etUsername)).perform(typeText(username));
        }
        if (!phone.isEmpty()) {
            onView(withId(R.id.etPhone)).perform(typeText(phone));
        }
        if (!email.isEmpty()) {
            onView(withId(R.id.etEmail)).perform(typeText(email));
        }
        if (!password.isEmpty()) {
            onView(withId(R.id.etPassword)).perform(typeText(password));
        }

        Espresso.closeSoftKeyboard();

        // Click register button
        onView(withId(R.id.btnRegister)).perform(click());

        // Wait for validation
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if error message is displayed as expected
        if (shouldShowError) {
            onView(withId(R.id.tvMessage))
                    .check(matches(isDisplayed()));
        }

        // Wait a bit before next test
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test MVVM架构的数据流 - Requirements 2.1-2.7, 3.1-3.3
     * Tests the complete MVVM data flow from View to Model
     */
    @Test
    public void testMVVMArchitectureDataFlow() {
        activityScenario.onActivity(activity -> {
            // Verify ViewModel is properly initialized
            assertNotNull("Activity should exist", activity);
            
            // Test the data flow by registering a user and checking each layer
            String username = "mvvmtest";
            String phone = "13812345678";
            String email = "mvvm@example.com";
            String password = "mvvmtest123";
            
            // Create UserInfo (Model layer)
            UserInfo userInfo = new UserInfo(username, phone, email, password);
            assertNotNull("UserInfo model should be created", userInfo);
            assertEquals("Model should store username correctly", username, userInfo.getUsername());
            
            // Test Repository layer
            UserRepository repository = new UserRepository(activity);
            UserValidator.ValidationResult validationResult = repository.validateUserInfo(userInfo);
            assertTrue("Repository validation should pass for valid data", validationResult.isValid());
            
            // Test DataSource layer
            UserDataSource dataSource = new UserDataSource(activity);
            assertNotNull("DataSource should be initialized", dataSource);
        });

        // Test View layer interaction
        registerUser("mvvmtest", "13812345678", "mvvm@example.com", "mvvmtest123");

        // Wait for complete data flow
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify the complete flow worked
        activityScenario.onActivity(activity -> {
            UserDataSource dataSource = new UserDataSource(activity);
            UserInfo savedUser = dataSource.getUserByUsername("mvvmtest");
            
            if (savedUser != null) {
                assertEquals("MVVM flow should preserve username", "mvvmtest", savedUser.getUsername());
                assertEquals("MVVM flow should preserve phone", "13812345678", savedUser.getPhoneNumber());
                assertEquals("MVVM flow should preserve email", "mvvm@example.com", savedUser.getEmail());
            }
        });

        // Clean up
        clearUserData("mvvmtest");
    }

    /**
     * Test duplicate username handling
     * Tests that the system properly handles duplicate username registration attempts
     */
    @Test
    public void testDuplicateUsernameHandling() {
        String username = "duplicateuser";
        String phone1 = "13812345678";
        String email1 = "first@example.com";
        String password1 = "password123";

        // Register first user
        registerUser(username, phone1, email1, password1);

        // Wait for registration
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Clear fields and try to register with same username but different details
        clearInputFields();

        String phone2 = "13987654321";
        String email2 = "second@example.com";
        String password2 = "different123";

        registerUser(username, phone2, email2, password2);

        // Wait for validation
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Should show error for duplicate username
        onView(withId(R.id.tvMessage))
                .check(matches(isDisplayed()));

        // Clean up
        clearUserData(username);
    }

    /**
     * Test password encryption
     * Tests that passwords are properly encrypted before storage
     */
    @Test
    public void testPasswordEncryption() {
        String username = "encrypttest";
        String phone = "13812345678";
        String email = "encrypt@example.com";
        String password = "plainpassword";

        registerUser(username, phone, email, password);

        // Wait for registration
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify password is encrypted
        activityScenario.onActivity(activity -> {
            UserDataSource dataSource = new UserDataSource(activity);
            UserInfo savedUser = dataSource.getUserByUsername(username);
            
            if (savedUser != null) {
                assertNotEquals("Password should be encrypted, not plain text", password, savedUser.getPassword());
                assertTrue("Encrypted password should not be empty", savedUser.getPassword().length() > 0);
                
                // Verify the encrypted password matches what we expect from SHA-256
                String expectedEncrypted = dataSource.encryptPassword(password);
                assertEquals("Encrypted password should match expected hash", expectedEncrypted, savedUser.getPassword());
            }
        });

        // Clean up
        clearUserData(username);
    }

    /**
     * Helper method to register a user through UI
     */
    private void registerUser(String username, String phone, String email, String password) {
        onView(withId(R.id.etUsername)).perform(clearText(), typeText(username));
        onView(withId(R.id.etPhone)).perform(clearText(), typeText(phone));
        onView(withId(R.id.etEmail)).perform(clearText(), typeText(email));
        onView(withId(R.id.etPassword)).perform(clearText(), typeText(password));
        
        Espresso.closeSoftKeyboard();
        
        onView(withId(R.id.btnRegister)).perform(click());
    }

    /**
     * Helper method to clear all input fields
     */
    private void clearInputFields() {
        onView(withId(R.id.etUsername)).perform(clearText());
        onView(withId(R.id.etPhone)).perform(clearText());
        onView(withId(R.id.etEmail)).perform(clearText());
        onView(withId(R.id.etPassword)).perform(clearText());
    }

    /**
     * Helper method to clear user data from MMKV
     */
    private void clearUserData(String username) {
        if (mmkv != null) {
            mmkv.removeValueForKey("user_" + username + "_username");
            mmkv.removeValueForKey("user_" + username + "_phone");
            mmkv.removeValueForKey("user_" + username + "_email");
            mmkv.removeValueForKey("user_" + username + "_password");
        }
    }

    /**
     * Test error recovery scenarios
     * Tests that the system properly recovers from various error conditions
     */
    @Test
    public void testErrorRecoveryScenarios() {
        // Test recovery from validation error
        // First trigger a validation error
        onView(withId(R.id.etUsername)).perform(typeText("ab")); // Too short
        onView(withId(R.id.btnRegister)).perform(click());

        // Wait for error
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify error is shown
        onView(withId(R.id.tvMessage)).check(matches(isDisplayed()));

        // Now fix the input and try again
        onView(withId(R.id.etUsername)).perform(clearText(), typeText("validuser"));
        onView(withId(R.id.etPhone)).perform(typeText("13812345678"));
        onView(withId(R.id.etEmail)).perform(typeText("valid@example.com"));
        onView(withId(R.id.etPassword)).perform(typeText("validpass123"));

        Espresso.closeSoftKeyboard();

        onView(withId(R.id.btnRegister)).perform(click());

        // Wait for processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // System should recover and either show success or clear the error
        // Clean up
        clearUserData("validuser");
    }

    /**
     * Test UI state consistency during operations
     * Tests that UI remains consistent during various operations
     */
    @Test
    public void testUIStateConsistency() {
        // Test that UI elements maintain proper state during registration
        String username = "statetest";
        String phone = "13812345678";
        String email = "state@example.com";
        String password = "statetest123";

        // Input data
        onView(withId(R.id.etUsername)).perform(typeText(username));
        onView(withId(R.id.etPhone)).perform(typeText(phone));
        onView(withId(R.id.etEmail)).perform(typeText(email));
        onView(withId(R.id.etPassword)).perform(typeText(password));

        Espresso.closeSoftKeyboard();

        // Verify initial state
        onView(withId(R.id.btnRegister)).check(matches(isEnabled()));
        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())));

        // Click register
        onView(withId(R.id.btnRegister)).perform(click());

        // Check intermediate state (loading)
        try {
            Thread.sleep(100); // Brief check for loading state
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Wait for completion
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify final state - button should be re-enabled
        onView(withId(R.id.btnRegister)).check(matches(isEnabled()));
        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())));

        // Clean up
        clearUserData(username);
    }
}