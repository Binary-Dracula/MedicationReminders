package com.medication.reminders.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;


import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.MedicationDao;
import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.models.MedicationValidationResult;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Repository class for medication data management
 * Implements Repository pattern to abstract data access logic
 * Provides async database operations using ExecutorService
 */
public class MedicationRepository {
    
    private MedicationDao medicationDao;
    private LiveData<List<MedicationInfo>> allMedications;
    private ExecutorService databaseWriteExecutor;
    
    /**
     * Constructor initializes the repository with database access
     * 
     * @param application Application context for database initialization
     */
    public MedicationRepository(Application application) {
        MedicationDatabase db = MedicationDatabase.getDatabase(application);
        medicationDao = db.medicationDao();
        allMedications = medicationDao.getAllMedications();
        
        // Create a single-threaded executor for database write operations
        databaseWriteExecutor = Executors.newFixedThreadPool(4);
    }
    
    /**
     * Get all medications as LiveData for observation
     * 
     * @return LiveData list of all medications
     */
    public LiveData<List<MedicationInfo>> getAllMedications() {
        return allMedications;
    }
    
    /**
     * Get a specific medication by ID
     * 
     * @param id The medication ID
     * @return LiveData of the medication
     */
    public LiveData<MedicationInfo> getMedicationById(long id) {
        return medicationDao.getMedicationById(id);
    }
    
    /**
     * Insert a new medication asynchronously
     * 
     * @param medication The medication to insert
     * @param callback Callback to handle the result
     */
    public void insertMedication(MedicationInfo medication, InsertCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                // Set timestamps
                long currentTime = System.currentTimeMillis();
                medication.setCreatedAt(currentTime);
                medication.setUpdatedAt(currentTime);
                
                // Validate medication before insertion
                MedicationValidationResult validationResult = validateMedication(medication);
                if (!validationResult.isValid()) {
                    if (callback != null) {
                        callback.onError(validationResult.getFirstError());
                    }
                    return;
                }
                
                // Check for duplicate name
                if (isMedicationNameExists(medication.getName())) {
                    if (callback != null) {
                        callback.onDuplicateFound(medication.getName());
                    }
                    return;
                }
                
                // Insert the medication
                long id = medicationDao.insertMedication(medication);
                if (callback != null) {
                    callback.onSuccess(id);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("数据库保存失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Insert medication with duplicate override option
     * 
     * @param medication The medication to insert
     * @param allowDuplicate Whether to allow duplicate names
     * @param callback Callback to handle the result
     */
    public void insertMedication(MedicationInfo medication, boolean allowDuplicate, InsertCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                // Set timestamps
                long currentTime = System.currentTimeMillis();
                medication.setCreatedAt(currentTime);
                medication.setUpdatedAt(currentTime);
                
                // Validate medication before insertion
                MedicationValidationResult validationResult = validateMedication(medication);
                if (!validationResult.isValid()) {
                    if (callback != null) {
                        callback.onError(validationResult.getFirstError());
                    }
                    return;
                }
                
                // Check for duplicate name if not allowing duplicates
                if (!allowDuplicate && isMedicationNameExists(medication.getName())) {
                    if (callback != null) {
                        callback.onDuplicateFound(medication.getName());
                    }
                    return;
                }
                
                // Insert the medication
                long id = medicationDao.insertMedication(medication);
                if (callback != null) {
                    callback.onSuccess(id);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("数据库保存失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Update an existing medication asynchronously
     * 
     * @param medication The medication to update
     * @param callback Callback to handle the result
     */
    public void updateMedication(MedicationInfo medication, UpdateCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                // Update timestamp
                medication.setUpdatedAt(System.currentTimeMillis());
                
                // Validate medication before update
                MedicationValidationResult validationResult = validateMedication(medication);
                if (!validationResult.isValid()) {
                    if (callback != null) {
                        callback.onError(validationResult.getFirstError());
                    }
                    return;
                }
                
                int rowsUpdated = medicationDao.updateMedication(medication);
                if (callback != null) {
                    if (rowsUpdated > 0) {
                        callback.onSuccess();
                    } else {
                        callback.onError("未找到要更新的药物记录");
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("数据库更新失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Delete a medication asynchronously
     * 
     * @param medication The medication to delete
     * @param callback Callback to handle the result
     */
    public void deleteMedication(MedicationInfo medication, DeleteCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                int rowsDeleted = medicationDao.deleteMedication(medication);
                if (callback != null) {
                    if (rowsDeleted > 0) {
                        callback.onSuccess();
                    } else {
                        callback.onError("未找到要删除的药物记录");
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("数据库删除失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Delete a medication by ID asynchronously
     * 
     * @param id The medication ID to delete
     * @param callback Callback to handle the result
     */
    public void deleteMedicationById(long id, DeleteCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                int rowsDeleted = medicationDao.deleteMedicationById(id);
                if (callback != null) {
                    if (rowsDeleted > 0) {
                        callback.onSuccess();
                    } else {
                        callback.onError("未找到要删除的药物记录");
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("数据库删除失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Check if a medication name already exists
     * This is a synchronous operation for validation purposes
     * 
     * @param name The medication name to check
     * @return true if the name exists, false otherwise
     */
    public boolean isMedicationNameExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        try {
            int count = medicationDao.getMedicationCountByName(name.trim());
            return count > 0;
        } catch (Exception e) {
            // If there's an error checking, assume it doesn't exist
            return false;
        }
    }
    
    /**
     * Get medication count synchronously
     * 
     * @return Future containing the medication count
     */
    public Future<Integer> getMedicationCountAsync() {
        return databaseWriteExecutor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return medicationDao.getMedicationCount().getValue();
            }
        });
    }
    
    /**
     * Search medications by name
     * 
     * @param searchQuery The search query
     * @return LiveData list of matching medications
     */
    public LiveData<List<MedicationInfo>> searchMedicationsByName(String searchQuery) {
        return medicationDao.searchMedicationsByName(searchQuery);
    }
    
    /**
     * Get medications by color
     * 
     * @param color The medication color
     * @return LiveData list of medications with the specified color
     */
    public LiveData<List<MedicationInfo>> getMedicationsByColor(String color) {
        return medicationDao.getMedicationsByColor(color);
    }
    
    /**
     * Get medications by dosage form
     * 
     * @param dosageForm The medication dosage form
     * @return LiveData list of medications with the specified dosage form
     */
    public LiveData<List<MedicationInfo>> getMedicationsByDosageForm(String dosageForm) {
        return medicationDao.getMedicationsByDosageForm(dosageForm);
    }
    
    /**
     * Validate medication information
     * 
     * @param medication The medication to validate
     * @return MedicationValidationResult containing validation results
     */
    private MedicationValidationResult validateMedication(MedicationInfo medication) {
        // Note: We need a context for validation, but since this is a repository,
        // we'll need to pass context from the calling layer
        // For now, we'll do basic validation without context
        MedicationValidationResult result = new MedicationValidationResult();
        
        if (medication == null) {
            result.addError("药物信息不能为空");
            return result;
        }
        
        if (medication.getName() == null || medication.getName().trim().isEmpty()) {
            result.addError("药物名称是必需的");
        }
        
        if (medication.getColor() == null || medication.getColor().trim().isEmpty()) {
            result.addError("请选择药物颜色");
        }
        
        if (medication.getDosageForm() == null || medication.getDosageForm().trim().isEmpty()) {
            result.addError("请选择药物剂型");
        }
        
        return result;
    }
    
    /**
     * Clean up resources
     * Should be called when the repository is no longer needed
     */
    public void cleanup() {
        if (databaseWriteExecutor != null && !databaseWriteExecutor.isShutdown()) {
            databaseWriteExecutor.shutdown();
        }
    }
    
    // Callback interfaces for async operations
    
    /**
     * Callback interface for insert operations
     */
    public interface InsertCallback {
        void onSuccess(long id);
        void onError(String errorMessage);
        void onDuplicateFound(String medicationName);
    }
    
    /**
     * Callback interface for update operations
     */
    public interface UpdateCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
    
    /**
     * Callback interface for delete operations
     */
    public interface DeleteCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}