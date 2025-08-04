package com.medication.reminders;

import java.util.ArrayList;
import java.util.List;

/**
 * Result class for medication validation
 * Contains validation status and detailed error messages
 */
public class MedicationValidationResult {
    
    private boolean isValid;
    private List<String> errorMessages;
    
    /**
     * Default constructor - creates a valid result
     */
    public MedicationValidationResult() {
        this.isValid = true;
        this.errorMessages = new ArrayList<>();
    }
    
    /**
     * Constructor with validation status
     * @param isValid Whether the validation passed
     */
    public MedicationValidationResult(boolean isValid) {
        this.isValid = isValid;
        this.errorMessages = new ArrayList<>();
    }
    
    /**
     * Check if the validation passed
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return isValid;
    }
    
    /**
     * Set the validation status
     * @param valid true if valid, false otherwise
     */
    public void setValid(boolean valid) {
        this.isValid = valid;
    }
    
    /**
     * Get all error messages
     * @return List of error messages
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }
    
    /**
     * Add an error message and mark as invalid
     * @param errorMessage Error message to add
     */
    public void addError(String errorMessage) {
        this.isValid = false;
        this.errorMessages.add(errorMessage);
    }
    
    /**
     * Add multiple error messages and mark as invalid
     * @param errorMessages List of error messages to add
     */
    public void addErrors(List<String> errorMessages) {
        this.isValid = false;
        this.errorMessages.addAll(errorMessages);
    }
    
    /**
     * Get the first error message
     * @return First error message or null if no errors
     */
    public String getFirstError() {
        return errorMessages.isEmpty() ? null : errorMessages.get(0);
    }
    
    /**
     * Get all error messages as a single string
     * @return Combined error messages separated by newlines
     */
    public String getAllErrorsAsString() {
        if (errorMessages.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errorMessages.size(); i++) {
            sb.append(errorMessages.get(i));
            if (i < errorMessages.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    /**
     * Check if there are any errors
     * @return true if there are errors, false otherwise
     */
    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }
    
    /**
     * Clear all errors and mark as valid
     */
    public void clearErrors() {
        this.isValid = true;
        this.errorMessages.clear();
    }
    
    @Override
    public String toString() {
        return "MedicationValidationResult{" +
                "isValid=" + isValid +
                ", errorMessages=" + errorMessages +
                '}';
    }
}