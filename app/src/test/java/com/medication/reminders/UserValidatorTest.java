package com.medication.reminders;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for UserValidator class
 * Tests all validation methods according to requirements 2.1-2.7
 */
public class UserValidatorTest {

    @Test
    public void testValidateUsername_ValidUsernames() {
        // Test valid usernames
        UserValidator.ValidationResult result1 = UserValidator.validateUsername("abc");
        assertTrue("3-character username should be valid", result1.isValid());
        
        UserValidator.ValidationResult result2 = UserValidator.validateUsername("testuser123");
        assertTrue("Normal username should be valid", result2.isValid());
        
        UserValidator.ValidationResult result3 = UserValidator.validateUsername("12345678901234567890");
        assertTrue("20-character username should be valid", result3.isValid());
    }

    @Test
    public void testValidateUsername_InvalidUsernames() {
        // Test null username
        UserValidator.ValidationResult result1 = UserValidator.validateUsername(null);
        assertFalse("Null username should be invalid", result1.isValid());
        assertEquals("用户名不能为空", result1.getErrorMessage());
        
        // Test empty username
        UserValidator.ValidationResult result2 = UserValidator.validateUsername("");
        assertFalse("Empty username should be invalid", result2.isValid());
        assertEquals("用户名不能为空", result2.getErrorMessage());
        
        // Test whitespace-only username
        UserValidator.ValidationResult result3 = UserValidator.validateUsername("   ");
        assertFalse("Whitespace-only username should be invalid", result3.isValid());
        assertEquals("用户名不能为空", result3.getErrorMessage());
        
        // Test username too short (< 3 characters)
        UserValidator.ValidationResult result4 = UserValidator.validateUsername("ab");
        assertFalse("Username with less than 3 characters should be invalid", result4.isValid());
        assertEquals("用户名至少需要3个字符", result4.getErrorMessage());
        
        // Test username too long (> 20 characters)
        UserValidator.ValidationResult result5 = UserValidator.validateUsername("123456789012345678901");
        assertFalse("Username with more than 20 characters should be invalid", result5.isValid());
        assertEquals("用户名不能超过20个字符", result5.getErrorMessage());
    }

    @Test
    public void testValidatePhoneNumber_ValidPhoneNumbers() {
        // Test valid Chinese phone numbers
        UserValidator.ValidationResult result1 = UserValidator.validatePhoneNumber("13812345678");
        assertTrue("Valid phone number starting with 138 should be valid", result1.isValid());
        
        UserValidator.ValidationResult result2 = UserValidator.validatePhoneNumber("15987654321");
        assertTrue("Valid phone number starting with 159 should be valid", result2.isValid());
        
        UserValidator.ValidationResult result3 = UserValidator.validatePhoneNumber("18612345678");
        assertTrue("Valid phone number starting with 186 should be valid", result3.isValid());
    }

    @Test
    public void testValidatePhoneNumber_InvalidPhoneNumbers() {
        // Test null phone number
        UserValidator.ValidationResult result1 = UserValidator.validatePhoneNumber(null);
        assertFalse("Null phone number should be invalid", result1.isValid());
        assertEquals("手机号不能为空", result1.getErrorMessage());
        
        // Test empty phone number
        UserValidator.ValidationResult result2 = UserValidator.validatePhoneNumber("");
        assertFalse("Empty phone number should be invalid", result2.isValid());
        assertEquals("手机号不能为空", result2.getErrorMessage());
        
        // Test phone number with wrong length
        UserValidator.ValidationResult result3 = UserValidator.validatePhoneNumber("1381234567");
        assertFalse("10-digit phone number should be invalid", result3.isValid());
        assertEquals("请输入正确的手机号格式", result3.getErrorMessage());
        
        UserValidator.ValidationResult result4 = UserValidator.validatePhoneNumber("138123456789");
        assertFalse("12-digit phone number should be invalid", result4.isValid());
        assertEquals("请输入正确的手机号格式", result4.getErrorMessage());
        
        // Test phone number with wrong starting digit
        UserValidator.ValidationResult result5 = UserValidator.validatePhoneNumber("12812345678");
        assertFalse("Phone number starting with 12 should be invalid", result5.isValid());
        assertEquals("请输入正确的手机号格式", result5.getErrorMessage());
        
        // Test phone number with non-digits
        UserValidator.ValidationResult result6 = UserValidator.validatePhoneNumber("138abcd5678");
        assertFalse("Phone number with letters should be invalid", result6.isValid());
        assertEquals("请输入正确的手机号格式", result6.getErrorMessage());
    }

    @Test
    public void testValidateEmail_ValidEmails() {
        // Test valid email addresses
        UserValidator.ValidationResult result1 = UserValidator.validateEmail("test@example.com");
        assertTrue("Standard email should be valid", result1.isValid());
        
        UserValidator.ValidationResult result2 = UserValidator.validateEmail("user.name@domain.co.uk");
        assertTrue("Email with dots and multiple domain parts should be valid", result2.isValid());
        
        UserValidator.ValidationResult result3 = UserValidator.validateEmail("user+tag@example.org");
        assertTrue("Email with plus sign should be valid", result3.isValid());
    }

    @Test
    public void testValidateEmail_InvalidEmails() {
        // Test null email
        UserValidator.ValidationResult result1 = UserValidator.validateEmail(null);
        assertFalse("Null email should be invalid", result1.isValid());
        assertEquals("邮箱不能为空", result1.getErrorMessage());
        
        // Test empty email
        UserValidator.ValidationResult result2 = UserValidator.validateEmail("");
        assertFalse("Empty email should be invalid", result2.isValid());
        assertEquals("邮箱不能为空", result2.getErrorMessage());
        
        // Test email without @
        UserValidator.ValidationResult result3 = UserValidator.validateEmail("testexample.com");
        assertFalse("Email without @ should be invalid", result3.isValid());
        assertEquals("请输入正确的邮箱格式", result3.getErrorMessage());
        
        // Test email without domain
        UserValidator.ValidationResult result4 = UserValidator.validateEmail("test@");
        assertFalse("Email without domain should be invalid", result4.isValid());
        assertEquals("请输入正确的邮箱格式", result4.getErrorMessage());
        
        // Test email without local part
        UserValidator.ValidationResult result5 = UserValidator.validateEmail("@example.com");
        assertFalse("Email without local part should be invalid", result5.isValid());
        assertEquals("请输入正确的邮箱格式", result5.getErrorMessage());
        
        // Test email with invalid characters
        UserValidator.ValidationResult result6 = UserValidator.validateEmail("test@exam ple.com");
        assertFalse("Email with spaces should be invalid", result6.isValid());
        assertEquals("请输入正确的邮箱格式", result6.getErrorMessage());
    }

    @Test
    public void testValidatePassword_ValidPasswords() {
        // Test valid passwords
        UserValidator.ValidationResult result1 = UserValidator.validatePassword("123456");
        assertTrue("6-character password should be valid", result1.isValid());
        
        UserValidator.ValidationResult result2 = UserValidator.validatePassword("password123");
        assertTrue("Normal password should be valid", result2.isValid());
        
        UserValidator.ValidationResult result3 = UserValidator.validatePassword("12345678901234567890");
        assertTrue("20-character password should be valid", result3.isValid());
    }

    @Test
    public void testValidatePassword_InvalidPasswords() {
        // Test null password
        UserValidator.ValidationResult result1 = UserValidator.validatePassword(null);
        assertFalse("Null password should be invalid", result1.isValid());
        assertEquals("密码不能为空", result1.getErrorMessage());
        
        // Test empty password
        UserValidator.ValidationResult result2 = UserValidator.validatePassword("");
        assertFalse("Empty password should be invalid", result2.isValid());
        assertEquals("密码不能为空", result2.getErrorMessage());
        
        // Test password too short (< 6 characters)
        UserValidator.ValidationResult result3 = UserValidator.validatePassword("12345");
        assertFalse("Password with less than 6 characters should be invalid", result3.isValid());
        assertEquals("密码至少需要6个字符", result3.getErrorMessage());
        
        // Test password too long (> 20 characters)
        UserValidator.ValidationResult result4 = UserValidator.validatePassword("123456789012345678901");
        assertFalse("Password with more than 20 characters should be invalid", result4.isValid());
        assertEquals("密码不能超过20个字符", result4.getErrorMessage());
    }

    @Test
    public void testValidateUserInfo_ValidUserInfo() {
        UserInfo validUser = new UserInfo("testuser", "13812345678", "test@example.com", "password123");
        UserValidator.ValidationResult result = UserValidator.validateUserInfo(validUser);
        assertTrue("Valid user info should pass validation", result.isValid());
        assertNull("Valid user info should have no error message", result.getErrorMessage());
    }

    @Test
    public void testValidateUserInfo_InvalidUserInfo() {
        // Test null user info
        UserValidator.ValidationResult result1 = UserValidator.validateUserInfo(null);
        assertFalse("Null user info should be invalid", result1.isValid());
        assertEquals("用户信息不能为空", result1.getErrorMessage());
        
        // Test user info with invalid username
        UserInfo invalidUser1 = new UserInfo("ab", "13812345678", "test@example.com", "password123");
        UserValidator.ValidationResult result2 = UserValidator.validateUserInfo(invalidUser1);
        assertFalse("User info with invalid username should be invalid", result2.isValid());
        assertEquals("用户名至少需要3个字符", result2.getErrorMessage());
        
        // Test user info with invalid phone
        UserInfo invalidUser2 = new UserInfo("testuser", "1234567890", "test@example.com", "password123");
        UserValidator.ValidationResult result3 = UserValidator.validateUserInfo(invalidUser2);
        assertFalse("User info with invalid phone should be invalid", result3.isValid());
        assertEquals("请输入正确的手机号格式", result3.getErrorMessage());
        
        // Test user info with invalid email
        UserInfo invalidUser3 = new UserInfo("testuser", "13812345678", "invalid-email", "password123");
        UserValidator.ValidationResult result4 = UserValidator.validateUserInfo(invalidUser3);
        assertFalse("User info with invalid email should be invalid", result4.isValid());
        assertEquals("请输入正确的邮箱格式", result4.getErrorMessage());
        
        // Test user info with invalid password
        UserInfo invalidUser4 = new UserInfo("testuser", "13812345678", "test@example.com", "12345");
        UserValidator.ValidationResult result5 = UserValidator.validateUserInfo(invalidUser4);
        assertFalse("User info with invalid password should be invalid", result5.isValid());
        assertEquals("密码至少需要6个字符", result5.getErrorMessage());
    }
}