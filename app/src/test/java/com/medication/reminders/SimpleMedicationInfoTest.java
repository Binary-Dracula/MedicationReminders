package com.medication.reminders;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Simple test to isolate MedicationInfo issues
 */
public class SimpleMedicationInfoTest {
    
    @Test
    public void testBasicConstructor() {
        try {
            MedicationInfo medication = new MedicationInfo();
            assertNotNull("MedicationInfo should be created", medication);
            assertEquals("Default ID should be 0", 0, medication.getId());
        } catch (Exception e) {
            fail("Exception creating MedicationInfo: " + e.getMessage());
        }
    }
    
    @Test
    public void testBasicSettersGetters() {
        try {
            MedicationInfo medication = new MedicationInfo();
            medication.setName("Test");
            assertEquals("Name should be set", "Test", medication.getName());
        } catch (Exception e) {
            fail("Exception in basic operations: " + e.getMessage());
        }
    }
}