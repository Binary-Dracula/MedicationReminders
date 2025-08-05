package com.medication.reminders.models;

import android.content.Context;

import com.medication.reminders.R;

/**
 * Enum representing medication dosage forms with internationalization support
 * Each dosage form option references a string resource for localization
 */
public enum MedicationDosageForm {
    PILL(R.string.dosage_form_pill),
    TABLET(R.string.dosage_form_tablet),
    CAPSULE(R.string.dosage_form_capsule),
    LIQUID(R.string.dosage_form_liquid),
    INJECTION(R.string.dosage_form_injection),
    POWDER(R.string.dosage_form_powder),
    CREAM(R.string.dosage_form_cream),
    PATCH(R.string.dosage_form_patch),
    INHALER(R.string.dosage_form_inhaler),
    OTHER(R.string.dosage_form_other);
    
    private final int stringResId;
    
    /**
     * Constructor for MedicationDosageForm enum
     * @param stringResId Resource ID for the dosage form name string
     */
    MedicationDosageForm(int stringResId) {
        this.stringResId = stringResId;
    }
    
    /**
     * Get the string resource ID for this dosage form
     * @return String resource ID
     */
    public int getStringResId() {
        return stringResId;
    }
    
    /**
     * Get the localized display name for this dosage form
     * @param context Android context for accessing string resources
     * @return Localized dosage form name
     */
    public String getDisplayName(Context context) {
        return context.getString(stringResId);
    }
    
    /**
     * Get MedicationDosageForm enum from string value
     * @param dosageFormString String representation of the dosage form
     * @return MedicationDosageForm enum or null if not found
     */
    public static MedicationDosageForm fromString(String dosageFormString) {
        if (dosageFormString == null) {
            return null;
        }
        
        try {
            return MedicationDosageForm.valueOf(dosageFormString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Get all dosage form options as an array
     * @return Array of all MedicationDosageForm values
     */
    public static MedicationDosageForm[] getAllDosageForms() {
        return MedicationDosageForm.values();
    }
}