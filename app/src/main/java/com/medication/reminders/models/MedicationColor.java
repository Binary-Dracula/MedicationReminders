package com.medication.reminders.models;

import android.content.Context;

import com.medication.reminders.R;

/**
 * Enum representing medication colors with internationalization support
 * Each color option references a string resource for localization
 */
public enum MedicationColor {
    WHITE(R.string.color_white),
    YELLOW(R.string.color_yellow),
    BLUE(R.string.color_blue),
    RED(R.string.color_red),
    GREEN(R.string.color_green),
    PINK(R.string.color_pink),
    ORANGE(R.string.color_orange),
    BROWN(R.string.color_brown),
    PURPLE(R.string.color_purple),
    CLEAR(R.string.color_clear),
    OTHER(R.string.color_other);
    
    private final int stringResId;
    
    /**
     * Constructor for MedicationColor enum
     * @param stringResId Resource ID for the color name string
     */
    MedicationColor(int stringResId) {
        this.stringResId = stringResId;
    }
    
    /**
     * Get the string resource ID for this color
     * @return String resource ID
     */
    public int getStringResId() {
        return stringResId;
    }
    
    /**
     * Get the localized display name for this color
     * @param context Android context for accessing string resources
     * @return Localized color name
     */
    public String getDisplayName(Context context) {
        return context.getString(stringResId);
    }
    
    /**
     * Get MedicationColor enum from string value
     * @param colorString String representation of the color
     * @return MedicationColor enum or null if not found
     */
    public static MedicationColor fromString(String colorString) {
        if (colorString == null) {
            return null;
        }
        
        try {
            return MedicationColor.valueOf(colorString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Get all color options as an array
     * @return Array of all MedicationColor values
     */
    public static MedicationColor[] getAllColors() {
        return MedicationColor.values();
    }
}