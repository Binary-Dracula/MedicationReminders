package com.medication.reminders;

import com.medication.reminders.models.MedicationValidationResult;
import com.medication.reminders.utils.MedicationValidator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for MedicationValidator class
 * Tests the basic validation logic without Android context
 */
public class MedicationValidatorTest {
    
    @Test
    public void testIsValidNameMethod() {
        assertTrue("Valid name should return true", MedicationValidator.isValidName("Aspirin"));
        assertFalse("Empty name should return false", MedicationValidator.isValidName(""));
        assertFalse("Null name should return false", MedicationValidator.isValidName(null));
        assertFalse("Whitespace-only name should return false", MedicationValidator.isValidName("   "));
        
        // Test length limits
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            longName.append("a");
        }
        assertFalse("Too long name should return false", MedicationValidator.isValidName(longName.toString()));
        
        // Test valid characters
        assertTrue("Name with numbers should be valid", MedicationValidator.isValidName("Aspirin 100mg"));
        assertTrue("Name with hyphens should be valid", MedicationValidator.isValidName("Co-codamol"));
        assertTrue("Name with parentheses should be valid", MedicationValidator.isValidName("Aspirin (100mg)"));
        assertTrue("Name with periods should be valid", MedicationValidator.isValidName("Dr. Smith's Medicine"));
        assertTrue("Name with slashes should be valid", MedicationValidator.isValidName("50/50 Co-codamol"));
        assertTrue("Name with plus signs should be valid", MedicationValidator.isValidName("Vitamin C+"));
        
        // Test invalid characters
        assertFalse("Name with special chars should be invalid", MedicationValidator.isValidName("Aspirin@100mg"));
        assertFalse("Name with hash should be invalid", MedicationValidator.isValidName("Medicine#1"));
        assertFalse("Name with dollar sign should be invalid", MedicationValidator.isValidName("Medicine$"));
    }
    
    @Test
    public void testIsValidColorMethod() {
        // Test all valid colors
        assertTrue("WHITE should be valid", MedicationValidator.isValidColor("WHITE"));
        assertTrue("YELLOW should be valid", MedicationValidator.isValidColor("YELLOW"));
        assertTrue("BLUE should be valid", MedicationValidator.isValidColor("BLUE"));
        assertTrue("RED should be valid", MedicationValidator.isValidColor("RED"));
        assertTrue("GREEN should be valid", MedicationValidator.isValidColor("GREEN"));
        assertTrue("PINK should be valid", MedicationValidator.isValidColor("PINK"));
        assertTrue("ORANGE should be valid", MedicationValidator.isValidColor("ORANGE"));
        assertTrue("BROWN should be valid", MedicationValidator.isValidColor("BROWN"));
        assertTrue("PURPLE should be valid", MedicationValidator.isValidColor("PURPLE"));
        assertTrue("CLEAR should be valid", MedicationValidator.isValidColor("CLEAR"));
        assertTrue("OTHER should be valid", MedicationValidator.isValidColor("OTHER"));
        
        // Test invalid cases
        assertFalse("Invalid color should return false", MedicationValidator.isValidColor("INVALID"));
        assertFalse("Empty color should return false", MedicationValidator.isValidColor(""));
        assertFalse("Null color should return false", MedicationValidator.isValidColor(null));
        assertFalse("Lowercase color should return false", MedicationValidator.isValidColor("white"));
        assertFalse("Mixed case color should return false", MedicationValidator.isValidColor("White"));
    }
    
    @Test
    public void testIsValidDosageFormMethod() {
        // Test all valid dosage forms
        assertTrue("PILL should be valid", MedicationValidator.isValidDosageForm("PILL"));
        assertTrue("TABLET should be valid", MedicationValidator.isValidDosageForm("TABLET"));
        assertTrue("CAPSULE should be valid", MedicationValidator.isValidDosageForm("CAPSULE"));
        assertTrue("LIQUID should be valid", MedicationValidator.isValidDosageForm("LIQUID"));
        assertTrue("INJECTION should be valid", MedicationValidator.isValidDosageForm("INJECTION"));
        assertTrue("POWDER should be valid", MedicationValidator.isValidDosageForm("POWDER"));
        assertTrue("CREAM should be valid", MedicationValidator.isValidDosageForm("CREAM"));
        assertTrue("PATCH should be valid", MedicationValidator.isValidDosageForm("PATCH"));
        assertTrue("INHALER should be valid", MedicationValidator.isValidDosageForm("INHALER"));
        assertTrue("OTHER should be valid", MedicationValidator.isValidDosageForm("OTHER"));
        
        // Test invalid cases
        assertFalse("Invalid dosage form should return false", MedicationValidator.isValidDosageForm("INVALID"));
        assertFalse("Empty dosage form should return false", MedicationValidator.isValidDosageForm(""));
        assertFalse("Null dosage form should return false", MedicationValidator.isValidDosageForm(null));
        assertFalse("Lowercase dosage form should return false", MedicationValidator.isValidDosageForm("tablet"));
        assertFalse("Mixed case dosage form should return false", MedicationValidator.isValidDosageForm("Tablet"));
    }
    

    
    @Test
    public void testValidationResultMethods() {
        MedicationValidationResult result = new MedicationValidationResult();
        
        assertTrue("New result should be valid", result.isValid());
        assertFalse("New result should have no errors", result.hasErrors());
        
        result.addError("Test error");
        
        assertFalse("Result with error should be invalid", result.isValid());
        assertTrue("Result with error should have errors", result.hasErrors());
        assertEquals("Should have 1 error", 1, result.getErrorMessages().size());
        assertEquals("First error should match", "Test error", result.getFirstError());
        
        result.clearErrors();
        
        assertTrue("Cleared result should be valid", result.isValid());
        assertFalse("Cleared result should have no errors", result.hasErrors());
    }
    
    @Test
    public void testGetMaxAndMinLengthMethods() {
        assertEquals("Max length should be 100", 100, MedicationValidator.getMaxMedicationNameLength());
        assertEquals("Min length should be 1", 1, MedicationValidator.getMinMedicationNameLength());
    }
    
    @Test
    public void testValidNameWithBoundaryLengths() {
        // Test minimum length (1 character)
        assertTrue("1 character name should be valid", MedicationValidator.isValidName("A"));
        
        // Test maximum length (100 characters)
        StringBuilder maxName = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            maxName.append("a");
        }
        assertTrue("100 character name should be valid", MedicationValidator.isValidName(maxName.toString()));
        
        // Test over maximum length (101 characters)
        maxName.append("a");
        assertFalse("101 character name should be invalid", MedicationValidator.isValidName(maxName.toString()));
    }
    
    @Test
    public void testValidNameWithUnicodeCharacters() {
        assertTrue("Unicode name should be valid", MedicationValidator.isValidName("阿司匹林"));
        assertTrue("Mixed unicode and latin should be valid", MedicationValidator.isValidName("阿司匹林 Aspirin"));
    }
}