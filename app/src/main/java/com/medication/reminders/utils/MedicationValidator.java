package com.medication.reminders.utils;

import android.content.Context;
import android.text.TextUtils;

import com.medication.reminders.R;
import com.medication.reminders.models.MedicationColor;
import com.medication.reminders.models.MedicationDosageForm;
import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.models.MedicationValidationResult;


/**
 * Utility class for validating medication information
 * Provides comprehensive validation for MedicationInfo objects
 */
public class MedicationValidator {
    
    // Constants for validation rules
    private static final int MAX_MEDICATION_NAME_LENGTH = 100;
    private static final int MIN_MEDICATION_NAME_LENGTH = 1;
    
    /**
     * Validate a MedicationInfo object
     * @param medication The medication to validate
     * @param context Android context for accessing string resources
     * @return MedicationValidationResult containing validation status and errors
     */
    public static MedicationValidationResult validateMedication(MedicationInfo medication, Context context) {
        MedicationValidationResult result = new MedicationValidationResult();
        
        if (medication == null) {
            result.addError(context.getString(R.string.error_medication_null));
            return result;
        }
        
        // Validate medication name
        validateMedicationName(medication.getName(), result, context);
        
        // Validate color selection
        validateColor(medication.getColor(), result, context);
        
        // Validate dosage form selection
        validateDosageForm(medication.getDosageForm(), result, context);
        
        return result;
    }
    
    /**
     * Validate medication name
     * @param name The medication name to validate
     * @param result The validation result to update
     * @param context Android context for accessing string resources
     */
    private static void validateMedicationName(String name, MedicationValidationResult result, Context context) {
        // Check if name is null or empty
        if (TextUtils.isEmpty(name)) {
            result.addError(context.getString(R.string.error_medication_name_required));
            return;
        }
        
        // Trim whitespace for length validation
        String trimmedName = name.trim();
        
        // Check if name is empty after trimming
        if (trimmedName.isEmpty()) {
            result.addError(context.getString(R.string.error_medication_name_required));
            return;
        }
        
        // Check minimum length
        if (trimmedName.length() < MIN_MEDICATION_NAME_LENGTH) {
            result.addError(context.getString(R.string.error_medication_name_too_short));
            return;
        }
        
        // Check maximum length
        if (trimmedName.length() > MAX_MEDICATION_NAME_LENGTH) {
            result.addError(context.getString(R.string.error_medication_name_too_long, MAX_MEDICATION_NAME_LENGTH));
            return;
        }
        
        // Check for invalid characters (optional - can be customized based on requirements)
        if (!isValidMedicationName(trimmedName)) {
            result.addError(context.getString(R.string.error_medication_name_invalid_characters));
        }
    }
    
    /**
     * Validate color selection
     * @param color The color to validate
     * @param result The validation result to update
     * @param context Android context for accessing string resources
     */
    private static void validateColor(String color, MedicationValidationResult result, Context context) {
        // Check if color is null or empty
        if (TextUtils.isEmpty(color)) {
            result.addError(context.getString(R.string.error_color_not_selected));
            return;
        }
        
        // Check if color is a valid enum value
        MedicationColor medicationColor = MedicationColor.fromString(color);
        if (medicationColor == null) {
            result.addError(context.getString(R.string.error_color_invalid));
        }
    }
    
    /**
     * Validate dosage form selection
     * @param dosageForm The dosage form to validate
     * @param result The validation result to update
     * @param context Android context for accessing string resources
     */
    private static void validateDosageForm(String dosageForm, MedicationValidationResult result, Context context) {
        // Check if dosage form is null or empty
        if (TextUtils.isEmpty(dosageForm)) {
            result.addError(context.getString(R.string.error_dosage_form_not_selected));
            return;
        }
        
        // Check if dosage form is a valid enum value
        MedicationDosageForm medicationDosageForm = MedicationDosageForm.fromString(dosageForm);
        if (medicationDosageForm == null) {
            result.addError(context.getString(R.string.error_dosage_form_invalid));
        }
    }
    
    /**
     * Check if medication name contains only valid characters
     * @param name The medication name to check
     * @return true if valid, false otherwise
     */
    private static boolean isValidMedicationName(String name) {
        // Allow letters, numbers, spaces, hyphens, parentheses, and common punctuation
        // This regex allows most common medication name patterns
        return name.matches("^[a-zA-Z0-9\\s\\-().,/+]+$");
    }
    
    /**
     * Validate only the medication name (utility method)
     * @param name The medication name to validate
     * @param context Android context for accessing string resources
     * @return MedicationValidationResult containing validation status and errors
     */
    public static MedicationValidationResult validateMedicationName(String name, Context context) {
        MedicationValidationResult result = new MedicationValidationResult();
        validateMedicationName(name, result, context);
        return result;
    }
    
    /**
     * Validate only the color selection (utility method)
     * @param color The color to validate
     * @param context Android context for accessing string resources
     * @return MedicationValidationResult containing validation status and errors
     */
    public static MedicationValidationResult validateColor(String color, Context context) {
        MedicationValidationResult result = new MedicationValidationResult();
        validateColor(color, result, context);
        return result;
    }
    
    /**
     * Validate only the dosage form selection (utility method)
     * @param dosageForm The dosage form to validate
     * @param context Android context for accessing string resources
     * @return MedicationValidationResult containing validation status and errors
     */
    public static MedicationValidationResult validateDosageForm(String dosageForm, Context context) {
        MedicationValidationResult result = new MedicationValidationResult();
        validateDosageForm(dosageForm, result, context);
        return result;
    }
    
    /**
     * Check if a medication name is valid without detailed error messages
     * @param name The medication name to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        
        String trimmedName = name.trim();
        return !trimmedName.isEmpty() && 
               trimmedName.length() >= MIN_MEDICATION_NAME_LENGTH && 
               trimmedName.length() <= MAX_MEDICATION_NAME_LENGTH &&
               isValidMedicationName(trimmedName);
    }
    
    /**
     * Check if a color is valid without detailed error messages
     * @param color The color to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidColor(String color) {
        return !TextUtils.isEmpty(color) && MedicationColor.fromString(color) != null;
    }
    
    /**
     * Check if a dosage form is valid without detailed error messages
     * @param dosageForm The dosage form to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidDosageForm(String dosageForm) {
        return !TextUtils.isEmpty(dosageForm) && MedicationDosageForm.fromString(dosageForm) != null;
    }
    
    /**
     * Get the maximum allowed length for medication names
     * @return Maximum medication name length
     */
    public static int getMaxMedicationNameLength() {
        return MAX_MEDICATION_NAME_LENGTH;
    }
    
    /**
     * Get the minimum allowed length for medication names
     * @return Minimum medication name length
     */
    public static int getMinMedicationNameLength() {
        return MIN_MEDICATION_NAME_LENGTH;
    }
}