package com.medication.reminders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.medication.reminders.models.ProfileValidationResult;

import org.junit.Test;

/**
 * Unit tests for ProfileValidationResult class
 */
public class ProfileValidationResultTest {
    
    @Test
    public void testDefaultConstructor() {
        ProfileValidationResult result = new ProfileValidationResult();
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        assertNull(result.getFieldName());
        assertNull(result.getErrorType());
    }
    
    @Test
    public void testFailureConstructorWithErrorType() {
        String fieldName = "姓名";
        String errorMessage = "姓名不能为空";
        ProfileValidationResult.ValidationErrorType errorType = 
            ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY;
        
        ProfileValidationResult result = new ProfileValidationResult(fieldName, errorMessage, errorType);
        
        assertFalse(result.isValid());
        assertEquals(fieldName, result.getFieldName());
        assertEquals(errorMessage, result.getErrorMessage());
        assertEquals(errorType, result.getErrorType());
    }
    
    @Test
    public void testFailureConstructorWithoutErrorType() {
        String fieldName = "邮箱";
        String errorMessage = "邮箱格式不正确";
        
        ProfileValidationResult result = new ProfileValidationResult(fieldName, errorMessage);
        
        assertFalse(result.isValid());
        assertEquals(fieldName, result.getFieldName());
        assertEquals(errorMessage, result.getErrorMessage());
        assertEquals(ProfileValidationResult.ValidationErrorType.UNKNOWN_ERROR, result.getErrorType());
    }
    
    @Test
    public void testSuccessStaticMethod() {
        ProfileValidationResult result = ProfileValidationResult.success();
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        assertNull(result.getFieldName());
        assertNull(result.getErrorType());
    }
    
    @Test
    public void testFailureStaticMethodWithErrorType() {
        String fieldName = "电话号码";
        String errorMessage = "电话号码格式不正确";
        ProfileValidationResult.ValidationErrorType errorType = 
            ProfileValidationResult.ValidationErrorType.INVALID_FORMAT;
        
        ProfileValidationResult result = ProfileValidationResult.failure(fieldName, errorMessage, errorType);
        
        assertFalse(result.isValid());
        assertEquals(fieldName, result.getFieldName());
        assertEquals(errorMessage, result.getErrorMessage());
        assertEquals(errorType, result.getErrorType());
    }
    
    @Test
    public void testFailureStaticMethodWithoutErrorType() {
        String fieldName = "出生日期";
        String errorMessage = "出生日期无效";
        
        ProfileValidationResult result = ProfileValidationResult.failure(fieldName, errorMessage);
        
        assertFalse(result.isValid());
        assertEquals(fieldName, result.getFieldName());
        assertEquals(errorMessage, result.getErrorMessage());
        assertEquals(ProfileValidationResult.ValidationErrorType.UNKNOWN_ERROR, result.getErrorType());
    }
    
    @Test
    public void testSettersAndGetters() {
        ProfileValidationResult result = new ProfileValidationResult();
        
        // Test setting valid to false
        result.setValid(false);
        assertFalse(result.isValid());
        
        // Test setting error message
        String errorMessage = "测试错误消息";
        result.setErrorMessage(errorMessage);
        assertEquals(errorMessage, result.getErrorMessage());
        
        // Test setting field name
        String fieldName = "测试字段";
        result.setFieldName(fieldName);
        assertEquals(fieldName, result.getFieldName());
        
        // Test setting error type
        ProfileValidationResult.ValidationErrorType errorType = 
            ProfileValidationResult.ValidationErrorType.INVALID_LENGTH;
        result.setErrorType(errorType);
        assertEquals(errorType, result.getErrorType());
    }
    
    @Test
    public void testIsErrorType() {
        ProfileValidationResult.ValidationErrorType errorType = 
            ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY;
        
        ProfileValidationResult result = ProfileValidationResult.failure("字段", "错误", errorType);
        
        assertTrue(result.isErrorType(ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY));
        assertFalse(result.isErrorType(ProfileValidationResult.ValidationErrorType.INVALID_FORMAT));
        
        // Test with successful result
        ProfileValidationResult successResult = ProfileValidationResult.success();
        assertFalse(successResult.isErrorType(ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY));
    }
    
    @Test
    public void testGetDisplayMessage() {
        // Test successful result
        ProfileValidationResult successResult = ProfileValidationResult.success();
        assertNull(successResult.getDisplayMessage());
        
        // Test with field name and error message
        String fieldName = "姓名";
        String errorMessage = "姓名不能为空";
        ProfileValidationResult result = ProfileValidationResult.failure(fieldName, errorMessage);
        assertEquals(fieldName + ": " + errorMessage, result.getDisplayMessage());
        
        // Test with only error message
        ProfileValidationResult resultNoField = new ProfileValidationResult(null, errorMessage);
        assertEquals(errorMessage, resultNoField.getDisplayMessage());
        
        // Test with no error message
        ProfileValidationResult resultNoMessage = new ProfileValidationResult(fieldName, null);
        assertEquals("验证失败", resultNoMessage.getDisplayMessage());
        
        // Test with neither field name nor error message
        ProfileValidationResult resultEmpty = new ProfileValidationResult(null, null);
        assertEquals("验证失败", resultEmpty.getDisplayMessage());
    }
    
    @Test
    public void testToString() {
        String fieldName = "用户名";
        String errorMessage = "用户名格式不正确";
        ProfileValidationResult.ValidationErrorType errorType = 
            ProfileValidationResult.ValidationErrorType.INVALID_FORMAT;
        
        ProfileValidationResult result = ProfileValidationResult.failure(fieldName, errorMessage, errorType);
        String resultString = result.toString();
        
        assertTrue(resultString.contains("ProfileValidationResult"));
        assertTrue(resultString.contains("isValid=false"));
        assertTrue(resultString.contains(fieldName));
        assertTrue(resultString.contains(errorMessage));
        assertTrue(resultString.contains(errorType.toString()));
    }
    
    @Test
    public void testValidationErrorTypeEnum() {
        // Test that all enum values exist
        ProfileValidationResult.ValidationErrorType[] types = 
            ProfileValidationResult.ValidationErrorType.values();
        
        assertTrue(types.length > 0);
        
        // Test specific enum values
        assertNotNull(ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
        assertNotNull(ProfileValidationResult.ValidationErrorType.INVALID_FORMAT);
        assertNotNull(ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        assertNotNull(ProfileValidationResult.ValidationErrorType.INVALID_DATE);
        assertNotNull(ProfileValidationResult.ValidationErrorType.INVALID_AGE);
        assertNotNull(ProfileValidationResult.ValidationErrorType.DUPLICATE_VALUE);
        assertNotNull(ProfileValidationResult.ValidationErrorType.FILE_ERROR);
        assertNotNull(ProfileValidationResult.ValidationErrorType.UNKNOWN_ERROR);
    }
    
    @Test
    public void testMultipleFailureResults() {
        ProfileValidationResult result1 = ProfileValidationResult.failure("字段1", "错误1");
        ProfileValidationResult result2 = ProfileValidationResult.failure("字段2", "错误2");
        
        assertFalse(result1.isValid());
        assertFalse(result2.isValid());
        
        assertEquals("字段1", result1.getFieldName());
        assertEquals("字段2", result2.getFieldName());
        
        assertEquals("错误1", result1.getErrorMessage());
        assertEquals("错误2", result2.getErrorMessage());
        
        // Ensure they are independent
        assertNotEquals(result1.getFieldName(), result2.getFieldName());
        assertNotEquals(result1.getErrorMessage(), result2.getErrorMessage());
    }
}