package com.medication.reminders;

import com.medication.reminders.models.MedicationColor;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for MedicationColor enum
 * Tests enum values, string resource references, and utility methods
 */
public class MedicationColorTest {
    
    @Test
    public void testEnumValues() {
        MedicationColor[] colors = MedicationColor.values();
        
        assertEquals("Should have 11 color values", 11, colors.length);
        
        // Test that all expected values exist
        assertNotNull("WHITE should exist", MedicationColor.WHITE);
        assertNotNull("YELLOW should exist", MedicationColor.YELLOW);
        assertNotNull("BLUE should exist", MedicationColor.BLUE);
        assertNotNull("RED should exist", MedicationColor.RED);
        assertNotNull("GREEN should exist", MedicationColor.GREEN);
        assertNotNull("PINK should exist", MedicationColor.PINK);
        assertNotNull("ORANGE should exist", MedicationColor.ORANGE);
        assertNotNull("BROWN should exist", MedicationColor.BROWN);
        assertNotNull("PURPLE should exist", MedicationColor.PURPLE);
        assertNotNull("CLEAR should exist", MedicationColor.CLEAR);
        assertNotNull("OTHER should exist", MedicationColor.OTHER);
    }
    
    @Test
    public void testStringResourceIds() {
        assertEquals("WHITE should have correct resource ID", R.string.color_white, MedicationColor.WHITE.getStringResId());
        assertEquals("YELLOW should have correct resource ID", R.string.color_yellow, MedicationColor.YELLOW.getStringResId());
        assertEquals("BLUE should have correct resource ID", R.string.color_blue, MedicationColor.BLUE.getStringResId());
        assertEquals("RED should have correct resource ID", R.string.color_red, MedicationColor.RED.getStringResId());
        assertEquals("GREEN should have correct resource ID", R.string.color_green, MedicationColor.GREEN.getStringResId());
        assertEquals("PINK should have correct resource ID", R.string.color_pink, MedicationColor.PINK.getStringResId());
        assertEquals("ORANGE should have correct resource ID", R.string.color_orange, MedicationColor.ORANGE.getStringResId());
        assertEquals("BROWN should have correct resource ID", R.string.color_brown, MedicationColor.BROWN.getStringResId());
        assertEquals("PURPLE should have correct resource ID", R.string.color_purple, MedicationColor.PURPLE.getStringResId());
        assertEquals("CLEAR should have correct resource ID", R.string.color_clear, MedicationColor.CLEAR.getStringResId());
        assertEquals("OTHER should have correct resource ID", R.string.color_other, MedicationColor.OTHER.getStringResId());
    }
    
    // Note: getDisplayName tests require Android context, so they are tested in instrumented tests
    
    @Test
    public void testFromStringWithValidValues() {
        assertEquals("WHITE string should return WHITE enum", MedicationColor.WHITE, MedicationColor.fromString("WHITE"));
        assertEquals("YELLOW string should return YELLOW enum", MedicationColor.YELLOW, MedicationColor.fromString("YELLOW"));
        assertEquals("BLUE string should return BLUE enum", MedicationColor.BLUE, MedicationColor.fromString("BLUE"));
        assertEquals("RED string should return RED enum", MedicationColor.RED, MedicationColor.fromString("RED"));
        assertEquals("GREEN string should return GREEN enum", MedicationColor.GREEN, MedicationColor.fromString("GREEN"));
        assertEquals("PINK string should return PINK enum", MedicationColor.PINK, MedicationColor.fromString("PINK"));
        assertEquals("ORANGE string should return ORANGE enum", MedicationColor.ORANGE, MedicationColor.fromString("ORANGE"));
        assertEquals("BROWN string should return BROWN enum", MedicationColor.BROWN, MedicationColor.fromString("BROWN"));
        assertEquals("PURPLE string should return PURPLE enum", MedicationColor.PURPLE, MedicationColor.fromString("PURPLE"));
        assertEquals("CLEAR string should return CLEAR enum", MedicationColor.CLEAR, MedicationColor.fromString("CLEAR"));
        assertEquals("OTHER string should return OTHER enum", MedicationColor.OTHER, MedicationColor.fromString("OTHER"));
    }
    
    @Test
    public void testFromStringWithLowercaseValues() {
        assertEquals("Lowercase white should return WHITE enum", MedicationColor.WHITE, MedicationColor.fromString("white"));
        assertEquals("Lowercase blue should return BLUE enum", MedicationColor.BLUE, MedicationColor.fromString("blue"));
        assertEquals("Lowercase other should return OTHER enum", MedicationColor.OTHER, MedicationColor.fromString("other"));
    }
    
    @Test
    public void testFromStringWithMixedCaseValues() {
        assertEquals("Mixed case White should return WHITE enum", MedicationColor.WHITE, MedicationColor.fromString("White"));
        assertEquals("Mixed case bLuE should return BLUE enum", MedicationColor.BLUE, MedicationColor.fromString("bLuE"));
        assertEquals("Mixed case Other should return OTHER enum", MedicationColor.OTHER, MedicationColor.fromString("Other"));
    }
    
    @Test
    public void testFromStringWithInvalidValues() {
        assertNull("Invalid color should return null", MedicationColor.fromString("INVALID"));
        assertNull("Empty string should return null", MedicationColor.fromString(""));
        assertNull("Null string should return null", MedicationColor.fromString(null));
        assertNull("Random string should return null", MedicationColor.fromString("RANDOM_COLOR"));
        assertNull("Number string should return null", MedicationColor.fromString("123"));
        assertNull("Special chars should return null", MedicationColor.fromString("WHITE@"));
    }
    
    @Test
    public void testGetAllColors() {
        MedicationColor[] allColors = MedicationColor.getAllColors();
        
        assertNotNull("getAllColors should not return null", allColors);
        assertEquals("Should return all 11 colors", 11, allColors.length);
        
        // Verify all colors are present
        boolean foundWhite = false, foundYellow = false, foundBlue = false, foundRed = false;
        boolean foundGreen = false, foundPink = false, foundOrange = false, foundBrown = false;
        boolean foundPurple = false, foundClear = false, foundOther = false;
        
        for (MedicationColor color : allColors) {
            switch (color) {
                case WHITE: foundWhite = true; break;
                case YELLOW: foundYellow = true; break;
                case BLUE: foundBlue = true; break;
                case RED: foundRed = true; break;
                case GREEN: foundGreen = true; break;
                case PINK: foundPink = true; break;
                case ORANGE: foundOrange = true; break;
                case BROWN: foundBrown = true; break;
                case PURPLE: foundPurple = true; break;
                case CLEAR: foundClear = true; break;
                case OTHER: foundOther = true; break;
            }
        }
        
        assertTrue("Should contain WHITE", foundWhite);
        assertTrue("Should contain YELLOW", foundYellow);
        assertTrue("Should contain BLUE", foundBlue);
        assertTrue("Should contain RED", foundRed);
        assertTrue("Should contain GREEN", foundGreen);
        assertTrue("Should contain PINK", foundPink);
        assertTrue("Should contain ORANGE", foundOrange);
        assertTrue("Should contain BROWN", foundBrown);
        assertTrue("Should contain PURPLE", foundPurple);
        assertTrue("Should contain CLEAR", foundClear);
        assertTrue("Should contain OTHER", foundOther);
    }
    
    @Test
    public void testEnumOrdering() {
        MedicationColor[] colors = MedicationColor.values();
        
        // Test that the order is as expected (based on enum declaration order)
        assertEquals("First color should be WHITE", MedicationColor.WHITE, colors[0]);
        assertEquals("Second color should be YELLOW", MedicationColor.YELLOW, colors[1]);
        assertEquals("Third color should be BLUE", MedicationColor.BLUE, colors[2]);
        assertEquals("Last color should be OTHER", MedicationColor.OTHER, colors[colors.length - 1]);
    }
    
    @Test
    public void testEnumToString() {
        assertEquals("WHITE toString should be WHITE", "WHITE", MedicationColor.WHITE.toString());
        assertEquals("BLUE toString should be BLUE", "BLUE", MedicationColor.BLUE.toString());
        assertEquals("OTHER toString should be OTHER", "OTHER", MedicationColor.OTHER.toString());
    }
    
    @Test
    public void testEnumName() {
        assertEquals("WHITE name should be WHITE", "WHITE", MedicationColor.WHITE.name());
        assertEquals("BLUE name should be BLUE", "BLUE", MedicationColor.BLUE.name());
        assertEquals("OTHER name should be OTHER", "OTHER", MedicationColor.OTHER.name());
    }
    
    @Test
    public void testEnumOrdinal() {
        assertEquals("WHITE ordinal should be 0", 0, MedicationColor.WHITE.ordinal());
        assertEquals("YELLOW ordinal should be 1", 1, MedicationColor.YELLOW.ordinal());
        assertEquals("OTHER ordinal should be 10", 10, MedicationColor.OTHER.ordinal());
    }
    
    @Test
    public void testStringResourceIdUniqueness() {
        MedicationColor[] colors = MedicationColor.values();
        
        // Verify that all string resource IDs are unique
        for (int i = 0; i < colors.length; i++) {
            for (int j = i + 1; j < colors.length; j++) {
                assertNotEquals("String resource IDs should be unique for " + colors[i] + " and " + colors[j],
                               colors[i].getStringResId(), colors[j].getStringResId());
            }
        }
    }
    
    // Context interaction tests are covered in instrumented tests
}