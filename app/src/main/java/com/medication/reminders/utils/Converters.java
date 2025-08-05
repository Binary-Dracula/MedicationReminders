package com.medication.reminders.utils;

import androidx.room.TypeConverter;

import com.medication.reminders.models.MedicationColor;
import com.medication.reminders.models.MedicationDosageForm;

import java.util.Date;

/**
 * Type converters for Room database
 * Handles conversion between complex types and primitive types that Room can persist
 */
public class Converters {
    
    /**
     * Convert timestamp (long) to Date object
     * 
     * @param timestamp The timestamp in milliseconds
     * @return Date object or null if timestamp is null
     */
    @TypeConverter
    public static Date fromTimestamp(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
    
    /**
     * Convert Date object to timestamp (long)
     * 
     * @param date The Date object
     * @return Timestamp in milliseconds or null if date is null
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    
    /**
     * Convert string to MedicationColor enum
     * Used if we decide to store enums in the future
     * 
     * @param colorString The color as string
     * @return MedicationColor enum or null if string is null
     */
    @TypeConverter
    public static MedicationColor fromColorString(String colorString) {
        if (colorString == null) {
            return null;
        }
        try {
            return MedicationColor.valueOf(colorString);
        } catch (IllegalArgumentException e) {
            // Return null or a default value if the string doesn't match any enum
            return null;
        }
    }
    
    /**
     * Convert MedicationColor enum to string
     * 
     * @param color The MedicationColor enum
     * @return String representation or null if color is null
     */
    @TypeConverter
    public static String colorToString(MedicationColor color) {
        return color == null ? null : color.name();
    }
    
    /**
     * Convert string to MedicationDosageForm enum
     * Used if we decide to store enums in the future
     * 
     * @param dosageFormString The dosage form as string
     * @return MedicationDosageForm enum or null if string is null
     */
    @TypeConverter
    public static MedicationDosageForm fromDosageFormString(String dosageFormString) {
        if (dosageFormString == null) {
            return null;
        }
        try {
            return MedicationDosageForm.valueOf(dosageFormString);
        } catch (IllegalArgumentException e) {
            // Return null or a default value if the string doesn't match any enum
            return null;
        }
    }
    
    /**
     * Convert MedicationDosageForm enum to string
     * 
     * @param dosageForm The MedicationDosageForm enum
     * @return String representation or null if dosageForm is null
     */
    @TypeConverter
    public static String dosageFormToString(MedicationDosageForm dosageForm) {
        return dosageForm == null ? null : dosageForm.name();
    }
}