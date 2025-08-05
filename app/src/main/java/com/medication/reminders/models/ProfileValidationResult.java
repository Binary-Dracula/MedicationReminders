package com.medication.reminders.models;

/**
 * ProfileValidationResult class for handling profile validation results
 * Contains validation status, error messages, and field-specific information
 */
public class ProfileValidationResult {
    private boolean isValid;
    private String errorMessage;
    private String fieldName;
    private ValidationErrorType errorType;

    /**
     * Enumeration of validation error types
     */
    public enum ValidationErrorType {
        REQUIRED_FIELD_EMPTY,
        INVALID_FORMAT,
        INVALID_LENGTH,
        INVALID_DATE,
        INVALID_AGE,
        DUPLICATE_VALUE,
        FILE_ERROR,
        UNKNOWN_ERROR
    }

    /**
     * Constructor for successful validation
     */
    public ProfileValidationResult() {
        this.isValid = true;
        this.errorMessage = null;
        this.fieldName = null;
        this.errorType = null;
    }

    /**
     * Constructor for failed validation
     * @param fieldName Name of the field that failed validation
     * @param errorMessage Error message describing the validation failure
     * @param errorType Type of validation error
     */
    public ProfileValidationResult(String fieldName, String errorMessage, ValidationErrorType errorType) {
        this.isValid = false;
        this.fieldName = fieldName;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
    }

    /**
     * Constructor for failed validation with default error type
     * @param fieldName Name of the field that failed validation
     * @param errorMessage Error message describing the validation failure
     */
    public ProfileValidationResult(String fieldName, String errorMessage) {
        this(fieldName, errorMessage, ValidationErrorType.UNKNOWN_ERROR);
    }

    /**
     * Static method to create a successful validation result
     * @return ProfileValidationResult indicating success
     */
    public static ProfileValidationResult success() {
        return new ProfileValidationResult();
    }

    /**
     * Static method to create a failed validation result
     * @param fieldName Name of the field that failed validation
     * @param errorMessage Error message describing the validation failure
     * @param errorType Type of validation error
     * @return ProfileValidationResult indicating failure
     */
    public static ProfileValidationResult failure(String fieldName, String errorMessage, ValidationErrorType errorType) {
        return new ProfileValidationResult(fieldName, errorMessage, errorType);
    }

    /**
     * Static method to create a failed validation result with default error type
     * @param fieldName Name of the field that failed validation
     * @param errorMessage Error message describing the validation failure
     * @return ProfileValidationResult indicating failure
     */
    public static ProfileValidationResult failure(String fieldName, String errorMessage) {
        return new ProfileValidationResult(fieldName, errorMessage);
    }

    /**
     * Get validation status
     * @return true if validation passed, false otherwise
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Set validation status
     * @param valid validation status
     */
    public void setValid(boolean valid) {
        isValid = valid;
    }

    /**
     * Get error message
     * @return error message or null if validation passed
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set error message
     * @param errorMessage error message to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Get field name that failed validation
     * @return field name or null if validation passed
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Set field name
     * @param fieldName field name to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Get validation error type
     * @return ValidationErrorType or null if validation passed
     */
    public ValidationErrorType getErrorType() {
        return errorType;
    }

    /**
     * Set validation error type
     * @param errorType error type to set
     */
    public void setErrorType(ValidationErrorType errorType) {
        this.errorType = errorType;
    }

    /**
     * Check if this is a specific type of error
     * @param type ValidationErrorType to check
     * @return true if this result represents the specified error type
     */
    public boolean isErrorType(ValidationErrorType type) {
        return !isValid && errorType == type;
    }

    /**
     * Get a user-friendly error message for display
     * @return formatted error message suitable for UI display
     */
    public String getDisplayMessage() {
        if (isValid) {
            return null;
        }
        
        if (fieldName != null && errorMessage != null) {
            return fieldName + ": " + errorMessage;
        }
        
        return errorMessage != null ? errorMessage : "验证失败";
    }

    @Override
    public String toString() {
        return "ProfileValidationResult{" +
                "isValid=" + isValid +
                ", errorMessage='" + errorMessage + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", errorType=" + errorType +
                '}';
    }
}