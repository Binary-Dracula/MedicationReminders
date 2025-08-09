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
    private MutableLiveData<Integer> totalQuantity = new MutableLiveData<>(0);
    private MutableLiveData<Integer> remainingQuantity = new MutableLiveData<>(0);
    private MutableLiveData<String> unit = new MutableLiveData<>("片");
    private MutableLiveData<String> photoPath = new MutableLiveData<>("");
    
    // 库存管理字段
    private MutableLiveData<Integer> dosagePerIntake = new MutableLiveData<>(1);
    private MutableLiveData<Integer> lowStockThreshold = new MutableLiveData<>(5);
    
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
    private MutableLiveData<String> totalQuantityError = new MutableLiveData<>();
    private MutableLiveData<String> remainingQuantityError = new MutableLiveData<>();
    private MutableLiveData<String> unitError = new MutableLiveData<>();
    
    // 库存管理字段验证状态
    private MutableLiveData<String> dosagePerIntakeError = new MutableLiveData<>();
    private MutableLiveData<String> lowStockThresholdError = new MutableLiveData<>();
    
    // Photo management fields
    private MutableLiveData<Boolean> hasPhoto = new MutableLiveData<>(false);
    private MutableLiveData<String> photoPreviewPath = new MutableLiveData<>();

    // Edit mode fields
    private long editingMedicationId = -1L;
    private boolean isEditMode = false;
    
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

    public LiveData<Integer> getTotalQuantity() { return totalQuantity; }
    public LiveData<Integer> getRemainingQuantity() { return remainingQuantity; }
    public LiveData<String> getUnit() { return unit; }
    
    // 库存管理字段的Getter方法
    public LiveData<Integer> getDosagePerIntake() { return dosagePerIntake; }
    public LiveData<Integer> getLowStockThreshold() { return lowStockThreshold; }
    
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
    public LiveData<String> getTotalQuantityError() { return totalQuantityError; }
    public LiveData<String> getRemainingQuantityError() { return remainingQuantityError; }
    public LiveData<String> getUnitError() { return unitError; }
    
    // 库存管理字段错误状态的Getter方法
    public LiveData<String> getDosagePerIntakeError() { return dosagePerIntakeError; }
    public LiveData<String> getLowStockThresholdError() { return lowStockThresholdError; }
    
    public LiveData<Boolean> getHasPhoto() {
        return hasPhoto;
    }
    
    public LiveData<String> getPhotoPreviewPath() {
        return photoPreviewPath;
    }

    public boolean isEditMode() { return isEditMode; }
    public long getEditingMedicationId() { return editingMedicationId; }

    // Expose medication LiveData for loading existing data
    public LiveData<MedicationInfo> getMedicationById(long id) {
        return repository.getMedicationById(id);
    }

    // Initialize edit mode from existing medication
    public void startEditFrom(MedicationInfo medication) {
        if (medication == null) return;
        editingMedicationId = medication.getId();
        isEditMode = true;
        medicationName.postValue(nonNull(medication.getName()));
        selectedColor.postValue(nonNull(medication.getColor()));
        selectedDosageForm.postValue(nonNull(medication.getDosageForm()));
        photoPath.postValue(nonNull(medication.getPhotoPath()));
        photoPreviewPath.postValue(nonNull(medication.getPhotoPath()));
        hasPhoto.postValue(medication.getPhotoPath() != null && !medication.getPhotoPath().trim().isEmpty());
        totalQuantity.postValue(medication.getTotalQuantity());
        remainingQuantity.postValue(medication.getRemainingQuantity());
        unit.postValue(nonNull(medication.getUnit()));
        
        // 设置库存管理字段
        dosagePerIntake.postValue(medication.getDosagePerIntake());
        lowStockThreshold.postValue(medication.getLowStockThreshold());
    }

    private String nonNull(String s) { return s == null ? "" : s; }
    
    // Setters for form data (called from UI)
    
    /**
     * Set medication name (no real-time validation)
     * 
     * @param name The medication name
     */
    public void setMedicationName(String name) {
        medicationName.postValue(name != null ? name.trim() : "");
    }
    
    /**
     * Set selected color (no real-time validation)
     * 
     * @param color The selected color
     */
    public void setSelectedColor(String color) {
        selectedColor.postValue(color);
    }
    
    /**
     * Set selected dosage form (no real-time validation)
     * 
     * @param dosageForm The selected dosage form
     */
    public void setSelectedDosageForm(String dosageForm) {
        selectedDosageForm.postValue(dosageForm);
    }

    public void setTotalQuantity(Integer value) { totalQuantity.postValue(value == null ? 0 : Math.max(0, value)); }
    public void setRemainingQuantity(Integer value) { remainingQuantity.postValue(value == null ? 0 : Math.max(0, value)); }
    public void setUnit(String value) { unit.postValue(value == null ? "" : value); }
    
    /**
     * 设置每次用量
     * 
     * @param value 每次用量，必须为正整数
     */
    public void setDosagePerIntake(Integer value) { 
        dosagePerIntake.postValue(value == null ? 1 : Math.max(1, value)); 
    }
    
    /**
     * 设置库存提醒阈值
     * 
     * @param value 库存提醒阈值，必须为非负整数
     */
    public void setLowStockThreshold(Integer value) { 
        lowStockThreshold.postValue(value == null ? 5 : Math.max(0, value)); 
    }
    
    /**
     * Set photo path and update photo-related states
     * 
     * @param path The photo file path
     */
    public void setPhotoPath(String path) {
        photoPath.postValue(path);
        photoPreviewPath.postValue(path);
        hasPhoto.postValue(path != null && !path.trim().isEmpty());
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
            nameError.postValue("药物名称是必需的");
        } else if (name.trim().length() > 100) {
            nameError.postValue("药物名称不能超过100个字符");
        } else {
            nameError.postValue(null);
        }
    }
    
    /**
     * Validate selected color
     */
    private void validateColor() {
        String color = selectedColor.getValue();
        if (color == null || color.trim().isEmpty()) {
            colorError.postValue("请选择药物颜色");
        } else {
            colorError.postValue(null);
        }
    }
    
    /**
     * Validate selected dosage form
     */
    private void validateDosageForm() {
        String dosageForm = selectedDosageForm.getValue();
        if (dosageForm == null || dosageForm.trim().isEmpty()) {
            dosageFormError.postValue("请选择药物剂型");
        } else {
            dosageFormError.postValue(null);
        }
    }

    private void validateQuantitiesAndUnit() {
        Integer total = totalQuantity.getValue();
        Integer remaining = remainingQuantity.getValue();
        String u = unit.getValue();
        Integer dosageIntake = dosagePerIntake.getValue();
        Integer stockThreshold = lowStockThreshold.getValue();

        // 验证总量
        if (total == null || total < 0) {
            totalQuantityError.postValue("总量必须是非负整数");
        } else {
            totalQuantityError.postValue(null);
        }

        // 验证剩余量
        if (remaining == null || remaining < 0) {
            remainingQuantityError.postValue("剩余量必须是非负整数，且不能大于总量");
        } else if (total != null && remaining != null && remaining > total) {
            remainingQuantityError.postValue("剩余量必须是非负整数，且不能大于总量");
        } else {
            remainingQuantityError.postValue(null);
        }

        // 验证单位
        if (u == null || u.trim().isEmpty()) {
            unitError.postValue("请选择单位");
        } else {
            unitError.postValue(null);
        }
        
        // 验证每次用量
        if (dosageIntake == null || dosageIntake <= 0) {
            dosagePerIntakeError.postValue("每次用量必须是正整数");
        } else if (remaining != null && dosageIntake > remaining) {
            dosagePerIntakeError.postValue("每次用量不能大于剩余量");
        } else {
            dosagePerIntakeError.postValue(null);
        }
        
        // 验证库存提醒阈值
        if (stockThreshold == null || stockThreshold < 0) {
            lowStockThresholdError.postValue("库存提醒阈值必须是非负整数");
        } else if (total != null && stockThreshold > total) {
            lowStockThresholdError.postValue("库存提醒阈值不能大于总量");
        } else {
            lowStockThresholdError.postValue(null);
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
        validateQuantitiesAndUnit();
        
        return nameError.getValue() == null && 
               colorError.getValue() == null && 
               dosageFormError.getValue() == null &&
               totalQuantityError.getValue() == null &&
               remainingQuantityError.getValue() == null &&
               unitError.getValue() == null &&
               dosagePerIntakeError.getValue() == null &&
               lowStockThresholdError.getValue() == null;
    }
    
    // Save medication logic
    
    /**
     * Save medication to database
     * Performs validation and calls repository
     */
    public void saveMedication() { saveMedication(false); }
    
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
            errorMessage.postValue("请检查并修正表单中的错误");
            return;
        }
        
        // Set loading state
        isLoading.postValue(true);
        
        // Create medication object
        MedicationInfo medication = new MedicationInfo();
        medication.setName(medicationName.getValue().trim());
        medication.setColor(selectedColor.getValue());
        medication.setDosageForm(selectedDosageForm.getValue());
        medication.setPhotoPath(photoPath.getValue());
        medication.setTotalQuantity(totalQuantity.getValue() == null ? 0 : totalQuantity.getValue());
        medication.setRemainingQuantity(remainingQuantity.getValue() == null ? 0 : remainingQuantity.getValue());
        medication.setUnit(unit.getValue());
        
        // 设置库存管理字段值
        medication.setDosagePerIntake(dosagePerIntake.getValue() == null ? 1 : dosagePerIntake.getValue());
        medication.setLowStockThreshold(lowStockThreshold.getValue() == null ? 5 : lowStockThreshold.getValue());

        if (isEditMode && editingMedicationId > 0) {
            medication.setId(editingMedicationId);
            repository.updateMedication(medication, new MedicationRepository.UpdateCallback() {
                @Override
                public void onSuccess() {
                    isLoading.postValue(false);
                    saveSuccess.postValue(true);
                }

                @Override
                public void onError(String errorMessage) {
                    isLoading.postValue(false);
                    AddMedicationViewModel.this.errorMessage.postValue(errorMessage);
                }
            });
        } else {
            // Save to repository (insert)
            repository.insertMedication(medication, allowDuplicate, new MedicationRepository.InsertCallback() {
                @Override
                public void onSuccess(long id) {
                    clearForm();
                    isLoading.postValue(false);
                    saveSuccess.postValue(true);
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
        totalQuantity.postValue(0);
        remainingQuantity.postValue(0);
        unit.postValue("片");
        
        // 重置库存管理字段
        dosagePerIntake.postValue(1);
        lowStockThreshold.postValue(5);
        
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
        totalQuantityError.postValue(null);
        remainingQuantityError.postValue(null);
        unitError.postValue(null);
        
        // 清除库存管理字段错误状态
        dosagePerIntakeError.postValue(null);
        lowStockThresholdError.postValue(null);
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
        Integer total = totalQuantity.getValue();
        Integer remaining = remainingQuantity.getValue();
        String u = unit.getValue();
        Integer dosageIntake = dosagePerIntake.getValue();
        Integer stockThreshold = lowStockThreshold.getValue();
        
        return (name != null && !name.trim().isEmpty()) ||
               (color != null && !color.trim().isEmpty()) ||
               (dosageForm != null && !dosageForm.trim().isEmpty()) ||
               (photo != null && !photo.trim().isEmpty()) ||
               (total != null && total > 0) ||
               (remaining != null && remaining > 0) ||
               (u != null && !u.trim().isEmpty() && !"片".equals(u.trim())) ||
               (dosageIntake != null && dosageIntake != 1) ||
               (stockThreshold != null && stockThreshold != 5);
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
        medication.setTotalQuantity(totalQuantity.getValue() != null ? totalQuantity.getValue() : 0);
        medication.setRemainingQuantity(remainingQuantity.getValue() != null ? remainingQuantity.getValue() : 0);
        medication.setUnit(unit.getValue() != null ? unit.getValue() : "");
        
        // 设置库存管理字段
        medication.setDosagePerIntake(dosagePerIntake.getValue() != null ? dosagePerIntake.getValue() : 1);
        medication.setLowStockThreshold(lowStockThreshold.getValue() != null ? lowStockThreshold.getValue() : 5);
        
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