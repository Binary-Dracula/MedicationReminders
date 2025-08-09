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
                                               TEST_PHOTO_PATH, TEST_TIMESTAMP, TEST_TIMESTAMP, 100, 100, "片", 1, 5);
        
        assertEquals("Name should be set correctly", TEST_NAME, med.getName());
        assertEquals("Color should be set correctly", TEST_COLOR, med.getColor());
        assertEquals("Dosage form should be set correctly", TEST_DOSAGE_FORM, med.getDosageForm());
        assertEquals("Photo path should be set correctly", TEST_PHOTO_PATH, med.getPhotoPath());
        assertEquals("Created at should be set correctly", TEST_TIMESTAMP, med.getCreatedAt());
        assertEquals("Updated at should be set correctly", TEST_TIMESTAMP, med.getUpdatedAt());
        assertEquals("Dosage per intake should be set correctly", 1, med.getDosagePerIntake());
        assertEquals("Low stock threshold should be set correctly", 5, med.getLowStockThreshold());
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
        assertTrue("toString should contain dosagePerIntake", result.contains("dosagePerIntake="));
        assertTrue("toString should contain lowStockThreshold", result.contains("lowStockThreshold="));
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
        assertTrue("toString should handle null name", result.contains("name='null'"));
        assertTrue("toString should handle null color", result.contains("color='null'"));
        assertTrue("toString should handle null dosage form", result.contains("dosageForm='null'"));
        assertTrue("toString should handle null photo path", result.contains("photoPath='null'"));
        assertTrue("toString should contain dosagePerIntake", result.contains("dosagePerIntake=1"));
        assertTrue("toString should contain lowStockThreshold", result.contains("lowStockThreshold=5"));
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
    
    @Test
    public void testDosagePerIntakeSettersAndGetters() {
        medication.setDosagePerIntake(2);
        assertEquals("Dosage per intake should be set correctly", 2, medication.getDosagePerIntake());
        
        medication.setDosagePerIntake(0);
        assertEquals("Zero dosage per intake should be handled", 0, medication.getDosagePerIntake());
    }
    
    @Test
    public void testLowStockThresholdSettersAndGetters() {
        medication.setLowStockThreshold(10);
        assertEquals("Low stock threshold should be set correctly", 10, medication.getLowStockThreshold());
        
        medication.setLowStockThreshold(0);
        assertEquals("Zero threshold should be handled", 0, medication.getLowStockThreshold());
    }
    
    @Test
    public void testIsLowStock() {
        medication.setLowStockThreshold(5);
        
        // Test when remaining quantity is above threshold
        medication.setRemainingQuantity(10);
        assertFalse("Should not be low stock when above threshold", medication.isLowStock());
        
        // Test when remaining quantity equals threshold
        medication.setRemainingQuantity(5);
        assertTrue("Should be low stock when equal to threshold", medication.isLowStock());
        
        // Test when remaining quantity is below threshold but above 0
        medication.setRemainingQuantity(3);
        assertTrue("Should be low stock when below threshold", medication.isLowStock());
        
        // Test when remaining quantity is 0
        medication.setRemainingQuantity(0);
        assertFalse("Should not be low stock when out of stock", medication.isLowStock());
    }
    
    @Test
    public void testIsOutOfStock() {
        // Test when remaining quantity is 0
        medication.setRemainingQuantity(0);
        assertTrue("Should be out of stock when remaining quantity is 0", medication.isOutOfStock());
        
        // Test when remaining quantity is above 0
        medication.setRemainingQuantity(1);
        assertFalse("Should not be out of stock when remaining quantity is above 0", medication.isOutOfStock());
        
        medication.setRemainingQuantity(100);
        assertFalse("Should not be out of stock when remaining quantity is high", medication.isOutOfStock());
    }
    
    @Test
    public void testConstructorWithRequiredFieldsSetsDefaults() {
        MedicationInfo med = new MedicationInfo(TEST_NAME, TEST_COLOR, TEST_DOSAGE_FORM);
        
        assertEquals("Default dosage per intake should be 1", 1, med.getDosagePerIntake());
        assertEquals("Default low stock threshold should be 5", 5, med.getLowStockThreshold());
        assertEquals("Default unit should be 片", "片", med.getUnit());
        assertEquals("Default remaining quantity should be 0", 0, med.getRemainingQuantity());
        assertEquals("Default total quantity should be 0", 0, med.getTotalQuantity());
    }
}