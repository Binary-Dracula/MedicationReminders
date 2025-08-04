package com.medication.reminders;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for MedicationDosageForm enum
 * Tests enum values, string resource references, and utility methods
 */
public class MedicationDosageFormTest {
    
    @Test
    public void testEnumValues() {
        MedicationDosageForm[] forms = MedicationDosageForm.values();
        
        assertEquals("Should have 10 dosage form values", 10, forms.length);
        
        // Test that all expected values exist
        assertNotNull("PILL should exist", MedicationDosageForm.PILL);
        assertNotNull("TABLET should exist", MedicationDosageForm.TABLET);
        assertNotNull("CAPSULE should exist", MedicationDosageForm.CAPSULE);
        assertNotNull("LIQUID should exist", MedicationDosageForm.LIQUID);
        assertNotNull("INJECTION should exist", MedicationDosageForm.INJECTION);
        assertNotNull("POWDER should exist", MedicationDosageForm.POWDER);
        assertNotNull("CREAM should exist", MedicationDosageForm.CREAM);
        assertNotNull("PATCH should exist", MedicationDosageForm.PATCH);
        assertNotNull("INHALER should exist", MedicationDosageForm.INHALER);
        assertNotNull("OTHER should exist", MedicationDosageForm.OTHER);
    }
    
    @Test
    public void testStringResourceIds() {
        assertEquals("PILL should have correct resource ID", R.string.dosage_form_pill, MedicationDosageForm.PILL.getStringResId());
        assertEquals("TABLET should have correct resource ID", R.string.dosage_form_tablet, MedicationDosageForm.TABLET.getStringResId());
        assertEquals("CAPSULE should have correct resource ID", R.string.dosage_form_capsule, MedicationDosageForm.CAPSULE.getStringResId());
        assertEquals("LIQUID should have correct resource ID", R.string.dosage_form_liquid, MedicationDosageForm.LIQUID.getStringResId());
        assertEquals("INJECTION should have correct resource ID", R.string.dosage_form_injection, MedicationDosageForm.INJECTION.getStringResId());
        assertEquals("POWDER should have correct resource ID", R.string.dosage_form_powder, MedicationDosageForm.POWDER.getStringResId());
        assertEquals("CREAM should have correct resource ID", R.string.dosage_form_cream, MedicationDosageForm.CREAM.getStringResId());
        assertEquals("PATCH should have correct resource ID", R.string.dosage_form_patch, MedicationDosageForm.PATCH.getStringResId());
        assertEquals("INHALER should have correct resource ID", R.string.dosage_form_inhaler, MedicationDosageForm.INHALER.getStringResId());
        assertEquals("OTHER should have correct resource ID", R.string.dosage_form_other, MedicationDosageForm.OTHER.getStringResId());
    }
    
    // Note: getDisplayName tests require Android context, so they are tested in instrumented tests
    
    @Test
    public void testFromStringWithValidValues() {
        assertEquals("PILL string should return PILL enum", MedicationDosageForm.PILL, MedicationDosageForm.fromString("PILL"));
        assertEquals("TABLET string should return TABLET enum", MedicationDosageForm.TABLET, MedicationDosageForm.fromString("TABLET"));
        assertEquals("CAPSULE string should return CAPSULE enum", MedicationDosageForm.CAPSULE, MedicationDosageForm.fromString("CAPSULE"));
        assertEquals("LIQUID string should return LIQUID enum", MedicationDosageForm.LIQUID, MedicationDosageForm.fromString("LIQUID"));
        assertEquals("INJECTION string should return INJECTION enum", MedicationDosageForm.INJECTION, MedicationDosageForm.fromString("INJECTION"));
        assertEquals("POWDER string should return POWDER enum", MedicationDosageForm.POWDER, MedicationDosageForm.fromString("POWDER"));
        assertEquals("CREAM string should return CREAM enum", MedicationDosageForm.CREAM, MedicationDosageForm.fromString("CREAM"));
        assertEquals("PATCH string should return PATCH enum", MedicationDosageForm.PATCH, MedicationDosageForm.fromString("PATCH"));
        assertEquals("INHALER string should return INHALER enum", MedicationDosageForm.INHALER, MedicationDosageForm.fromString("INHALER"));
        assertEquals("OTHER string should return OTHER enum", MedicationDosageForm.OTHER, MedicationDosageForm.fromString("OTHER"));
    }
    
    @Test
    public void testFromStringWithLowercaseValues() {
        assertEquals("Lowercase pill should return PILL enum", MedicationDosageForm.PILL, MedicationDosageForm.fromString("pill"));
        assertEquals("Lowercase tablet should return TABLET enum", MedicationDosageForm.TABLET, MedicationDosageForm.fromString("tablet"));
        assertEquals("Lowercase capsule should return CAPSULE enum", MedicationDosageForm.CAPSULE, MedicationDosageForm.fromString("capsule"));
        assertEquals("Lowercase other should return OTHER enum", MedicationDosageForm.OTHER, MedicationDosageForm.fromString("other"));
    }
    
    @Test
    public void testFromStringWithMixedCaseValues() {
        assertEquals("Mixed case Pill should return PILL enum", MedicationDosageForm.PILL, MedicationDosageForm.fromString("Pill"));
        assertEquals("Mixed case tAbLeT should return TABLET enum", MedicationDosageForm.TABLET, MedicationDosageForm.fromString("tAbLeT"));
        assertEquals("Mixed case Capsule should return CAPSULE enum", MedicationDosageForm.CAPSULE, MedicationDosageForm.fromString("Capsule"));
        assertEquals("Mixed case Other should return OTHER enum", MedicationDosageForm.OTHER, MedicationDosageForm.fromString("Other"));
    }
    
    @Test
    public void testFromStringWithInvalidValues() {
        assertNull("Invalid dosage form should return null", MedicationDosageForm.fromString("INVALID"));
        assertNull("Empty string should return null", MedicationDosageForm.fromString(""));
        assertNull("Null string should return null", MedicationDosageForm.fromString(null));
        assertNull("Random string should return null", MedicationDosageForm.fromString("RANDOM_FORM"));
        assertNull("Number string should return null", MedicationDosageForm.fromString("123"));
        assertNull("Special chars should return null", MedicationDosageForm.fromString("TABLET@"));
    }
    
    @Test
    public void testGetAllDosageForms() {
        MedicationDosageForm[] allForms = MedicationDosageForm.getAllDosageForms();
        
        assertNotNull("getAllDosageForms should not return null", allForms);
        assertEquals("Should return all 10 dosage forms", 10, allForms.length);
        
        // Verify all dosage forms are present
        boolean foundPill = false, foundTablet = false, foundCapsule = false, foundLiquid = false;
        boolean foundInjection = false, foundPowder = false, foundCream = false, foundPatch = false;
        boolean foundInhaler = false, foundOther = false;
        
        for (MedicationDosageForm form : allForms) {
            switch (form) {
                case PILL: foundPill = true; break;
                case TABLET: foundTablet = true; break;
                case CAPSULE: foundCapsule = true; break;
                case LIQUID: foundLiquid = true; break;
                case INJECTION: foundInjection = true; break;
                case POWDER: foundPowder = true; break;
                case CREAM: foundCream = true; break;
                case PATCH: foundPatch = true; break;
                case INHALER: foundInhaler = true; break;
                case OTHER: foundOther = true; break;
            }
        }
        
        assertTrue("Should contain PILL", foundPill);
        assertTrue("Should contain TABLET", foundTablet);
        assertTrue("Should contain CAPSULE", foundCapsule);
        assertTrue("Should contain LIQUID", foundLiquid);
        assertTrue("Should contain INJECTION", foundInjection);
        assertTrue("Should contain POWDER", foundPowder);
        assertTrue("Should contain CREAM", foundCream);
        assertTrue("Should contain PATCH", foundPatch);
        assertTrue("Should contain INHALER", foundInhaler);
        assertTrue("Should contain OTHER", foundOther);
    }
    
    @Test
    public void testEnumOrdering() {
        MedicationDosageForm[] forms = MedicationDosageForm.values();
        
        // Test that the order is as expected (based on enum declaration order)
        assertEquals("First form should be PILL", MedicationDosageForm.PILL, forms[0]);
        assertEquals("Second form should be TABLET", MedicationDosageForm.TABLET, forms[1]);
        assertEquals("Third form should be CAPSULE", MedicationDosageForm.CAPSULE, forms[2]);
        assertEquals("Last form should be OTHER", MedicationDosageForm.OTHER, forms[forms.length - 1]);
    }
    
    @Test
    public void testEnumToString() {
        assertEquals("PILL toString should be PILL", "PILL", MedicationDosageForm.PILL.toString());
        assertEquals("TABLET toString should be TABLET", "TABLET", MedicationDosageForm.TABLET.toString());
        assertEquals("OTHER toString should be OTHER", "OTHER", MedicationDosageForm.OTHER.toString());
    }
    
    @Test
    public void testEnumName() {
        assertEquals("PILL name should be PILL", "PILL", MedicationDosageForm.PILL.name());
        assertEquals("TABLET name should be TABLET", "TABLET", MedicationDosageForm.TABLET.name());
        assertEquals("OTHER name should be OTHER", "OTHER", MedicationDosageForm.OTHER.name());
    }
    
    @Test
    public void testEnumOrdinal() {
        assertEquals("PILL ordinal should be 0", 0, MedicationDosageForm.PILL.ordinal());
        assertEquals("TABLET ordinal should be 1", 1, MedicationDosageForm.TABLET.ordinal());
        assertEquals("OTHER ordinal should be 9", 9, MedicationDosageForm.OTHER.ordinal());
    }
    
    @Test
    public void testStringResourceIdUniqueness() {
        MedicationDosageForm[] forms = MedicationDosageForm.values();
        
        // Verify that all string resource IDs are unique
        for (int i = 0; i < forms.length; i++) {
            for (int j = i + 1; j < forms.length; j++) {
                assertNotEquals("String resource IDs should be unique for " + forms[i] + " and " + forms[j],
                               forms[i].getStringResId(), forms[j].getStringResId());
            }
        }
    }
    
    // Context interaction tests are covered in instrumented tests
    
    @Test
    public void testCommonDosageFormsPresent() {
        // Test that common medication dosage forms are present
        assertNotNull("PILL should be available for common pills", MedicationDosageForm.PILL);
        assertNotNull("TABLET should be available for tablets", MedicationDosageForm.TABLET);
        assertNotNull("CAPSULE should be available for capsules", MedicationDosageForm.CAPSULE);
        assertNotNull("LIQUID should be available for liquid medications", MedicationDosageForm.LIQUID);
        assertNotNull("INJECTION should be available for injections", MedicationDosageForm.INJECTION);
        assertNotNull("OTHER should be available for uncommon forms", MedicationDosageForm.OTHER);
    }
    
    @Test
    public void testSpecializedDosageFormsPresent() {
        // Test that specialized dosage forms are present
        assertNotNull("POWDER should be available for powdered medications", MedicationDosageForm.POWDER);
        assertNotNull("CREAM should be available for topical creams", MedicationDosageForm.CREAM);
        assertNotNull("PATCH should be available for transdermal patches", MedicationDosageForm.PATCH);
        assertNotNull("INHALER should be available for inhalation medications", MedicationDosageForm.INHALER);
    }
}