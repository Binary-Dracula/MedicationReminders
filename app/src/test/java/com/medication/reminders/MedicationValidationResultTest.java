package com.medication.reminders;

import org.junit.Test;
import org.junit.Before;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for MedicationValidationResult class
 * Tests validation result handling, error management, and utility methods
 */
public class MedicationValidationResultTest {
    
    private MedicationValidationResult result;
    
    @Before
    public void setUp() {
        result = new MedicationValidationResult();
    }
    
    @Test
    public void testDefaultConstructor() {
        MedicationValidationResult newResult = new MedicationValidationResult();
        
        assertTrue("Default result should be valid", newResult.isValid());
        assertFalse("Default result should have no errors", newResult.hasErrors());
        assertEquals("Default result should have 0 errors", 0, newResult.getErrorMessages().size());
        assertNull("Default result should have no first error", newResult.getFirstError());
        assertEquals("Default result should have empty error string", "", newResult.getAllErrorsAsString());
    }
    
    @Test
    public void testConstructorWithValidStatus() {
        MedicationValidationResult validResult = new MedicationValidationResult(true);
        
        assertTrue("Valid result should be valid", validResult.isValid());
        assertFalse("Valid result should have no errors", validResult.hasErrors());
        assertEquals("Valid result should have 0 errors", 0, validResult.getErrorMessages().size());
    }
    
    @Test
    public void testConstructorWithInvalidStatus() {
        MedicationValidationResult invalidResult = new MedicationValidationResult(false);
        
        assertFalse("Invalid result should be invalid", invalidResult.isValid());
        assertFalse("Invalid result without errors should have no errors", invalidResult.hasErrors());
        assertEquals("Invalid result should have 0 errors initially", 0, invalidResult.getErrorMessages().size());
    }
    
    @Test
    public void testSetValid() {
        result.setValid(false);
        assertFalse("Result should be invalid after setting to false", result.isValid());
        
        result.setValid(true);
        assertTrue("Result should be valid after setting to true", result.isValid());
    }
    
    @Test
    public void testAddSingleError() {
        String errorMessage = "Test error message";
        
        result.addError(errorMessage);
        
        assertFalse("Result should be invalid after adding error", result.isValid());
        assertTrue("Result should have errors", result.hasErrors());
        assertEquals("Should have 1 error", 1, result.getErrorMessages().size());
        assertEquals("First error should match", errorMessage, result.getFirstError());
        assertTrue("Error list should contain the error", result.getErrorMessages().contains(errorMessage));
    }
    
    @Test
    public void testAddMultipleErrors() {
        String error1 = "First error";
        String error2 = "Second error";
        String error3 = "Third error";
        
        result.addError(error1);
        result.addError(error2);
        result.addError(error3);
        
        assertFalse("Result should be invalid", result.isValid());
        assertTrue("Result should have errors", result.hasErrors());
        assertEquals("Should have 3 errors", 3, result.getErrorMessages().size());
        assertEquals("First error should be the first added", error1, result.getFirstError());
        
        List<String> errors = result.getErrorMessages();
        assertEquals("First error in list should match", error1, errors.get(0));
        assertEquals("Second error in list should match", error2, errors.get(1));
        assertEquals("Third error in list should match", error3, errors.get(2));
    }
    
    @Test
    public void testAddErrorsFromList() {
        List<String> errorList = Arrays.asList("Error 1", "Error 2", "Error 3");
        
        result.addErrors(errorList);
        
        assertFalse("Result should be invalid", result.isValid());
        assertTrue("Result should have errors", result.hasErrors());
        assertEquals("Should have 3 errors", 3, result.getErrorMessages().size());
        assertEquals("First error should match", "Error 1", result.getFirstError());
        
        for (String error : errorList) {
            assertTrue("Should contain error: " + error, result.getErrorMessages().contains(error));
        }
    }
    
    @Test
    public void testAddEmptyErrorList() {
        List<String> emptyList = Arrays.asList();
        
        result.addErrors(emptyList);
        
        assertFalse("Result should be invalid after adding empty list", result.isValid());
        assertFalse("Result should have no errors", result.hasErrors());
        assertEquals("Should have 0 errors", 0, result.getErrorMessages().size());
    }
    
    @Test
    public void testGetFirstErrorWithNoErrors() {
        assertNull("First error should be null when no errors", result.getFirstError());
    }
    
    @Test
    public void testGetAllErrorsAsStringSingle() {
        String errorMessage = "Single error";
        result.addError(errorMessage);
        
        assertEquals("Single error string should match", errorMessage, result.getAllErrorsAsString());
    }
    
    @Test
    public void testGetAllErrorsAsStringMultiple() {
        result.addError("Error 1");
        result.addError("Error 2");
        result.addError("Error 3");
        
        String expected = "Error 1\nError 2\nError 3";
        assertEquals("Multiple errors should be joined with newlines", expected, result.getAllErrorsAsString());
    }
    
    @Test
    public void testGetAllErrorsAsStringEmpty() {
        assertEquals("Empty errors should return empty string", "", result.getAllErrorsAsString());
    }
    
    @Test
    public void testClearErrors() {
        result.addError("Error 1");
        result.addError("Error 2");
        
        assertFalse("Result should be invalid before clearing", result.isValid());
        assertTrue("Result should have errors before clearing", result.hasErrors());
        
        result.clearErrors();
        
        assertTrue("Result should be valid after clearing", result.isValid());
        assertFalse("Result should have no errors after clearing", result.hasErrors());
        assertEquals("Should have 0 errors after clearing", 0, result.getErrorMessages().size());
        assertNull("First error should be null after clearing", result.getFirstError());
        assertEquals("Error string should be empty after clearing", "", result.getAllErrorsAsString());
    }
    
    @Test
    public void testHasErrors() {
        assertFalse("Should have no errors initially", result.hasErrors());
        
        result.addError("Test error");
        assertTrue("Should have errors after adding", result.hasErrors());
        
        result.clearErrors();
        assertFalse("Should have no errors after clearing", result.hasErrors());
    }
    
    @Test
    public void testToString() {
        String toStringResult = result.toString();
        
        assertNotNull("toString should not return null", toStringResult);
        assertTrue("toString should contain class name", toStringResult.contains("MedicationValidationResult"));
        assertTrue("toString should contain isValid", toStringResult.contains("isValid=true"));
        assertTrue("toString should contain errorMessages", toStringResult.contains("errorMessages=[]"));
    }
    
    @Test
    public void testToStringWithErrors() {
        result.addError("Test error");
        String toStringResult = result.toString();
        
        assertNotNull("toString should not return null", toStringResult);
        assertTrue("toString should contain class name", toStringResult.contains("MedicationValidationResult"));
        assertTrue("toString should contain isValid=false", toStringResult.contains("isValid=false"));
        assertTrue("toString should contain error message", toStringResult.contains("Test error"));
    }
    
    @Test
    public void testErrorMessageImmutability() {
        result.addError("Original error");
        List<String> errors = result.getErrorMessages();
        
        // Try to modify the returned list
        try {
            errors.add("New error");
            // If we reach here, the list is mutable, which might be okay
            // but we should verify the original result is not affected
            assertEquals("Original result should still have 1 error", 1, result.getErrorMessages().size());
        } catch (UnsupportedOperationException e) {
            // This is expected if the list is immutable
            assertEquals("Should still have 1 error", 1, result.getErrorMessages().size());
        }
    }
    
    @Test
    public void testNullErrorMessage() {
        result.addError(null);
        
        assertFalse("Result should be invalid", result.isValid());
        assertTrue("Result should have errors", result.hasErrors());
        assertEquals("Should have 1 error", 1, result.getErrorMessages().size());
        assertNull("First error should be null", result.getFirstError());
        assertTrue("Error list should contain null", result.getErrorMessages().contains(null));
    }
    
    @Test
    public void testEmptyErrorMessage() {
        result.addError("");
        
        assertFalse("Result should be invalid", result.isValid());
        assertTrue("Result should have errors", result.hasErrors());
        assertEquals("Should have 1 error", 1, result.getErrorMessages().size());
        assertEquals("First error should be empty string", "", result.getFirstError());
    }
    
    @Test
    public void testLongErrorMessage() {
        StringBuilder longError = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longError.append("Long error message ");
        }
        String longErrorString = longError.toString();
        
        result.addError(longErrorString);
        
        assertFalse("Result should be invalid", result.isValid());
        assertEquals("Long error should be preserved", longErrorString, result.getFirstError());
    }
    
    @Test
    public void testSpecialCharactersInErrorMessage() {
        String specialError = "Error with special chars: @#$%^&*()[]{}|\\:;\"'<>,.?/~`";
        result.addError(specialError);
        
        assertEquals("Special characters should be preserved", specialError, result.getFirstError());
    }
    
    @Test
    public void testUnicodeInErrorMessage() {
        String unicodeError = "Unicode error: 药物名称是必需的 🏥💊";
        result.addError(unicodeError);
        
        assertEquals("Unicode characters should be preserved", unicodeError, result.getFirstError());
    }
    
    @Test
    public void testMultipleOperationsSequence() {
        // Test a complex sequence of operations
        result.addError("Error 1");
        assertFalse("Should be invalid", result.isValid());
        
        result.setValid(true);
        assertTrue("Should be valid after setting", result.isValid());
        assertTrue("Should still have errors", result.hasErrors());
        
        result.addError("Error 2");
        assertFalse("Should be invalid after adding error", result.isValid());
        assertEquals("Should have 2 errors", 2, result.getErrorMessages().size());
        
        result.clearErrors();
        assertTrue("Should be valid after clearing", result.isValid());
        assertFalse("Should have no errors", result.hasErrors());
        
        result.addErrors(Arrays.asList("Error 3", "Error 4"));
        assertFalse("Should be invalid", result.isValid());
        assertEquals("Should have 2 errors", 2, result.getErrorMessages().size());
    }
}