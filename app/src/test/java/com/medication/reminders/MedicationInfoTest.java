package com.medication.reminders;

import com.medication.reminders.database.entity.MedicationInfo;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * Unit tests for MedicationInfo entity class
 * Tests constructors, getters, setters, and toString method
 */
public class MedicationInfoTest {
    
    private MedicationInfo medication;
    private static final String TEST_NAME = "Aspirin";
    private static final String TEST_COLOR = "WHITE";
    private static final String TEST_DOSAGE_FORM = "TABLET";
    private static final String TEST_PHOTO_PATH = "/path/to/photo.jpg";
    private static final long TEST_TIMESTAMP = 1640995200000L; // 2022-01-01 00:00:00 UTC
    
    @Before
    public void setUp() {
        medication = new MedicationInfo();
    }
    
    @Test
    public void testDefaultConstructor() {
        MedicationInfo med = new MedicationInfo();
        
        assertEquals("Default ID should be 0", 0, med.getId());
        assertNull("Default name should be null", med.getName());
        assertNull("Default color should be null", med.getColor());
        assertNull("Default dosage form should be null", med.getDosageForm());
        assertNull("Default photo path should be null", med.getPhotoPath());
        assertEquals("Default created at should be 0", 0, med.getCreatedAt());
        assertEquals("Default updated at should be 0", 0, med.getUpdatedAt());
    }
    
    @Test
    public void testConstructorWithAllFields() {
        MedicationInfo med = new MedicationInfo(TEST_NAME, TEST_COLOR, TEST_DOSAGE_FORM, 
                                               TEST_PHOTO_PATH, TEST_TIMESTAMP, TEST_TIMESTAMP);
        
        assertEquals("Name should be set correctly", TEST_NAME, med.getName());
        assertEquals("Color should be set correctly", TEST_COLOR, med.getColor());
        assertEquals("Dosage form should be set correctly", TEST_DOSAGE_FORM, med.getDosageForm());
        assertEquals("Photo path should be set correctly", TEST_PHOTO_PATH, med.getPhotoPath());
        assertEquals("Created at should be set correctly", TEST_TIMESTAMP, med.getCreatedAt());
        assertEquals("Updated at should be set correctly", TEST_TIMESTAMP, med.getUpdatedAt());
    }
    
    @Test
    public void testConstructorWithRequiredFields() {
        long beforeTime = System.currentTimeMillis();
        MedicationInfo med = new MedicationInfo(TEST_NAME, TEST_COLOR, TEST_DOSAGE_FORM);
        long afterTime = System.currentTimeMillis();
        
        assertEquals("Name should be set correctly", TEST_NAME, med.getName());
        assertEquals("Color should be set correctly", TEST_COLOR, med.getColor());
        assertEquals("Dosage form should be set correctly", TEST_DOSAGE_FORM, med.getDosageForm());
        assertNull("Photo path should be null", med.getPhotoPath());
        
        // Check that timestamps are set to current time
        assertTrue("Created at should be between before and after time", 
                   med.getCreatedAt() >= beforeTime && med.getCreatedAt() <= afterTime);
        assertTrue("Updated at should be between before and after time", 
                   med.getUpdatedAt() >= beforeTime && med.getUpdatedAt() <= afterTime);
        assertEquals("Created at and updated at should be equal", 
                     med.getCreatedAt(), med.getUpdatedAt());
    }
    
    @Test
    public void testSettersAndGetters() {
        medication.setId(123L);
        medication.setName(TEST_NAME);
        medication.setColor(TEST_COLOR);
        medication.setDosageForm(TEST_DOSAGE_FORM);
        medication.setPhotoPath(TEST_PHOTO_PATH);
        medication.setCreatedAt(TEST_TIMESTAMP);
        medication.setUpdatedAt(TEST_TIMESTAMP + 1000);
        
        assertEquals("ID should be set correctly", 123L, medication.getId());
        assertEquals("Name should be set correctly", TEST_NAME, medication.getName());
        assertEquals("Color should be set correctly", TEST_COLOR, medication.getColor());
        assertEquals("Dosage form should be set correctly", TEST_DOSAGE_FORM, medication.getDosageForm());
        assertEquals("Photo path should be set correctly", TEST_PHOTO_PATH, medication.getPhotoPath());
        assertEquals("Created at should be set correctly", TEST_TIMESTAMP, medication.getCreatedAt());
        assertEquals("Updated at should be set correctly", TEST_TIMESTAMP + 1000, medication.getUpdatedAt());
    }
    
    @Test
    public void testSettersWithNullValues() {
        medication.setName(null);
        medication.setColor(null);
        medication.setDosageForm(null);
        medication.setPhotoPath(null);
        
        assertNull("Name should be null", medication.getName());
        assertNull("Color should be null", medication.getColor());
        assertNull("Dosage form should be null", medication.getDosageForm());
        assertNull("Photo path should be null", medication.getPhotoPath());
    }
    
    @Test
    public void testSettersWithEmptyStrings() {
        medication.setName("");
        medication.setColor("");
        medication.setDosageForm("");
        medication.setPhotoPath("");
        
        assertEquals("Name should be empty string", "", medication.getName());
        assertEquals("Color should be empty string", "", medication.getColor());
        assertEquals("Dosage form should be empty string", "", medication.getDosageForm());
        assertEquals("Photo path should be empty string", "", medication.getPhotoPath());
    }
    
    @Test
    public void testToString() {
        medication.setId(123L);
        medication.setName(TEST_NAME);
        medication.setColor(TEST_COLOR);
        medication.setDosageForm(TEST_DOSAGE_FORM);
        medication.setPhotoPath(TEST_PHOTO_PATH);
        medication.setCreatedAt(TEST_TIMESTAMP);
        medication.setUpdatedAt(TEST_TIMESTAMP + 1000);
        
        String result = medication.toString();
        
        assertNotNull("toString should not return null", result);
        assertTrue("toString should contain class name", result.contains("MedicationInfo"));
        assertTrue("toString should contain id", result.contains("id=123"));
        assertTrue("toString should contain name", result.contains("name='" + TEST_NAME + "'"));
        assertTrue("toString should contain color", result.contains("color='" + TEST_COLOR + "'"));
        assertTrue("toString should contain dosage form", result.contains("dosageForm='" + TEST_DOSAGE_FORM + "'"));
        assertTrue("toString should contain photo path", result.contains("photoPath='" + TEST_PHOTO_PATH + "'"));
        assertTrue("toString should contain created at", result.contains("createdAt=" + TEST_TIMESTAMP));
        assertTrue("toString should contain updated at", result.contains("updatedAt=" + (TEST_TIMESTAMP + 1000)));
    }
    
    @Test
    public void testToStringWithNullValues() {
        medication.setId(123L);
        medication.setName(null);
        medication.setColor(null);
        medication.setDosageForm(null);
        medication.setPhotoPath(null);
        medication.setCreatedAt(0);
        medication.setUpdatedAt(0);
        
        String result = medication.toString();
        
        assertNotNull("toString should not return null", result);
        assertTrue("toString should handle null name", result.contains("name=null"));
        assertTrue("toString should handle null color", result.contains("color=null"));
        assertTrue("toString should handle null dosage form", result.contains("dosageForm=null"));
        assertTrue("toString should handle null photo path", result.contains("photoPath=null"));
    }
    
    @Test
    public void testLongNameHandling() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longName.append("a");
        }
        String longNameString = longName.toString();
        
        medication.setName(longNameString);
        assertEquals("Long name should be set correctly", longNameString, medication.getName());
    }
    
    @Test
    public void testSpecialCharactersInName() {
        String specialName = "Aspirin-100mg (Extra Strength) 50/50 Co-codamol";
        medication.setName(specialName);
        assertEquals("Special characters in name should be handled", specialName, medication.getName());
    }
    
    @Test
    public void testUnicodeCharactersInName() {
        String unicodeName = "阿司匹林 Aspirin 100mg";
        medication.setName(unicodeName);
        assertEquals("Unicode characters should be handled", unicodeName, medication.getName());
    }
    
    @Test
    public void testTimestampBoundaryValues() {
        medication.setCreatedAt(Long.MIN_VALUE);
        medication.setUpdatedAt(Long.MAX_VALUE);
        
        assertEquals("Min timestamp should be handled", Long.MIN_VALUE, medication.getCreatedAt());
        assertEquals("Max timestamp should be handled", Long.MAX_VALUE, medication.getUpdatedAt());
    }
    
    @Test
    public void testIdBoundaryValues() {
        medication.setId(Long.MIN_VALUE);
        assertEquals("Min ID should be handled", Long.MIN_VALUE, medication.getId());
        
        medication.setId(Long.MAX_VALUE);
        assertEquals("Max ID should be handled", Long.MAX_VALUE, medication.getId());
    }
}