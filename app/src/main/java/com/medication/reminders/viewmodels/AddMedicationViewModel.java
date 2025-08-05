package com.medication.reminders.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.repository.MedicationRepository;


/**
 * ViewModel for AddMedicationActivity
 * Manages UI-related data and business logic for adding medications
 * Follows MVVM architecture pattern
 */
public class AddMedicationViewModel extends AndroidViewModel {
    
    private MedicationRepository repository;
    
    // Form data fields
    private MutableLiveData<String> medicationName = new MutableLiveData<>("");
    private MutableLiveData<String> selectedColor = new MutableLiveData<>("");
    private MutableLiveData<String> selectedDosageForm = new MutableLiveData<>("");
    private MutableLiveData<String> photoPath = new MutableLiveData<>("");
    
    // UI state fields
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> showDuplicateDialog = new MutableLiveData<>(false);
    private MutableLiveData<String> duplicateMedicationName = new MutableLiveData<>();
    
    // Validation state fields
    private MutableLiveData<String> nameError = new MutableLiveData<>();
    private MutableLiveData<String> colorError = new MutableLiveData<>();
    private MutableLiveData<String> dosageFormError = new MutableLiveData<>();
    
    // Photo management fields
    private MutableLiveData<Boolean> hasPhoto = new MutableLiveData<>(false);
    private MutableLiveData<String> photoPreviewPath = new MutableLiveData<>();
    
    /**
     * Constructor initializes the ViewModel with repository
     * 
     * @param application Application context for repository initialization
     */
    public AddMedicationViewModel(@NonNull Application application) {
        super(application);
        repository = createRepository(application);
    }
    
    /**
     * Create repository instance - can be overridden for testing
     * 
     * @param application Application context
     * @return MedicationRepository instance
     */
    protected MedicationRepository createRepository(Application application) {
        return new MedicationRepository(application);
    }
    
    // Getters for LiveData (read-only access for UI)
    
    public LiveData<String> getMedicationName() {
        return medicationName;
    }
    
    public LiveData<String> getSelectedColor() {
        return selectedColor;
    }
    
    public LiveData<String> getSelectedDosageForm() {
        return selectedDosageForm;
    }
    
    public LiveData<String> getPhotoPath() {
        return photoPath;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<Boolean> getShowDuplicateDialog() {
        return showDuplicateDialog;
    }
    
    public LiveData<String> getDuplicateMedicationName() {
        return duplicateMedicationName;
    }
    
    public LiveData<String> getNameError() {
        return nameError;
    }
    
    public LiveData<String> getColorError() {
        return colorError;
    }
    
    public LiveData<String> getDosageFormError() {
        return dosageFormError;
    }
    
    public LiveData<Boolean> getHasPhoto() {
        return hasPhoto;
    }
    
    public LiveData<String> getPhotoPreviewPath() {
        return photoPreviewPath;
    }
    
    // Setters for form data (called from UI)
    
    /**
     * Set medication name (no real-time validation)
     * 
     * @param name The medication name
     */
    public void setMedicationName(String name) {
        medicationName.setValue(name != null ? name.trim() : "");
    }
    
    /**
     * Set selected color (no real-time validation)
     * 
     * @param color The selected color
     */
    public void setSelectedColor(String color) {
        selectedColor.setValue(color);
    }
    
    /**
     * Set selected dosage form (no real-time validation)
     * 
     * @param dosageForm The selected dosage form
     */
    public void setSelectedDosageForm(String dosageForm) {
        selectedDosageForm.setValue(dosageForm);
    }
    
    /**
     * Set photo path and update photo-related states
     * 
     * @param path The photo file path
     */
    public void setPhotoPath(String path) {
        photoPath.setValue(path);
        photoPreviewPath.setValue(path);
        hasPhoto.setValue(path != null && !path.trim().isEmpty());
    }
    
    /**
     * Clear photo path and reset photo-related states
     */
    public void clearPhoto() {
        photoPath.postValue("");
        photoPreviewPath.postValue("");
        hasPhoto.postValue(false);
    }
    
    // Validation methods
    
    /**
     * Validate medication name
     */
    private void validateName() {
        String name = medicationName.getValue();
        if (name == null || name.trim().isEmpty()) {
            nameError.setValue("药物名称是必需的");
        } else if (name.trim().length() > 100) {
            nameError.setValue("药物名称不能超过100个字符");
        } else {
            nameError.setValue(null);
        }
    }
    
    /**
     * Validate selected color
     */
    private void validateColor() {
        String color = selectedColor.getValue();
        if (color == null || color.trim().isEmpty()) {
            colorError.setValue("请选择药物颜色");
        } else {
            colorError.setValue(null);
        }
    }
    
    /**
     * Validate selected dosage form
     */
    private void validateDosageForm() {
        String dosageForm = selectedDosageForm.getValue();
        if (dosageForm == null || dosageForm.trim().isEmpty()) {
            dosageFormError.setValue("请选择药物剂型");
        } else {
            dosageFormError.setValue(null);
        }
    }
    
    /**
     * Validate all form fields (only called when saving)
     * 
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateAllFields() {
        validateName();
        validateColor();
        validateDosageForm();
        
        return nameError.getValue() == null && 
               colorError.getValue() == null && 
               dosageFormError.getValue() == null;
    }
    
    // Save medication logic
    
    /**
     * Save medication to database
     * Performs validation and calls repository
     */
    public void saveMedication() {
        saveMedication(false);
    }
    
    /**
     * Save medication with duplicate override option
     * 
     * @param allowDuplicate Whether to allow duplicate medication names
     */
    public void saveMedication(boolean allowDuplicate) {
        // Clear previous errors
        clearErrors();
        
        // Validate all fields
        if (!validateAllFields()) {
            errorMessage.setValue("请检查并修正表单中的错误");
            return;
        }
        
        // Set loading state
        isLoading.setValue(true);
        
        // Create medication object
        MedicationInfo medication = new MedicationInfo();
        medication.setName(medicationName.getValue().trim());
        medication.setColor(selectedColor.getValue());
        medication.setDosageForm(selectedDosageForm.getValue());
        medication.setPhotoPath(photoPath.getValue());
        
        // Save to repository
        repository.insertMedication(medication, allowDuplicate, new MedicationRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                isLoading.postValue(false);
                saveSuccess.postValue(true);
                clearForm();
            }
            
            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                AddMedicationViewModel.this.errorMessage.postValue(errorMessage);
            }
            
            @Override
            public void onDuplicateFound(String medicationName) {
                isLoading.postValue(false);
                duplicateMedicationName.postValue(medicationName);
                showDuplicateDialog.postValue(true);
            }
        });
    }
    
    /**
     * Handle duplicate dialog response
     * 
     * @param allowDuplicate Whether user chose to allow duplicate
     */
    public void handleDuplicateResponse(boolean allowDuplicate) {
        showDuplicateDialog.postValue(false);
        if (allowDuplicate) {
            saveMedication(true);
        }
    }
    
    /**
     * Clear all form data and reset states
     */
    public void clearForm() {
        medicationName.postValue("");
        selectedColor.postValue("");
        selectedDosageForm.postValue("");
        clearPhoto();
        clearErrors();
        saveSuccess.postValue(false);
    }
    
    /**
     * Clear all error states
     */
    public void clearErrors() {
        errorMessage.postValue(null);
        nameError.postValue(null);
        colorError.postValue(null);
        dosageFormError.postValue(null);
    }
    
    /**
     * Reset save success state
     */
    public void resetSaveSuccess() {
        saveSuccess.postValue(false);
    }
    
    /**
     * Check if form has any data
     * 
     * @return true if form has data, false if empty
     */
    public boolean hasFormData() {
        String name = medicationName.getValue();
        String color = selectedColor.getValue();
        String dosageForm = selectedDosageForm.getValue();
        String photo = photoPath.getValue();
        
        return (name != null && !name.trim().isEmpty()) ||
               (color != null && !color.trim().isEmpty()) ||
               (dosageForm != null && !dosageForm.trim().isEmpty()) ||
               (photo != null && !photo.trim().isEmpty());
    }
    
    /**
     * Get current medication data as MedicationInfo object
     * 
     * @return MedicationInfo object with current form data
     */
    public MedicationInfo getCurrentMedicationData() {
        MedicationInfo medication = new MedicationInfo();
        medication.setName(medicationName.getValue() != null ? medicationName.getValue().trim() : "");
        medication.setColor(selectedColor.getValue() != null ? selectedColor.getValue() : "");
        medication.setDosageForm(selectedDosageForm.getValue() != null ? selectedDosageForm.getValue() : "");
        medication.setPhotoPath(photoPath.getValue() != null ? photoPath.getValue() : "");
        return medication;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up repository resources
        if (repository != null) {
            repository.cleanup();
        }
    }
}