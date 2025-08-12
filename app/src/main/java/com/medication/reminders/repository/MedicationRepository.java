package com.medication.reminders.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.medication.reminders.R;
import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.MedicationDao;
import com.medication.reminders.database.dao.MedicationIntakeRecordDao;
import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.database.entity.MedicationIntakeRecord;
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

    private Application application;
    private MedicationDao medicationDao;
    private MedicationIntakeRecordDao intakeRecordDao;
    private LiveData<List<MedicationInfo>> allMedications;
    private ExecutorService databaseWriteExecutor;
    
    /**
     * Constructor initializes the repository with database access
     * 
     * @param application Application context for database initialization
     */
    public MedicationRepository(Application application) {
        this.application = application;
        MedicationDatabase db = MedicationDatabase.getDatabase(application);
        medicationDao = db.medicationDao();
        intakeRecordDao = db.medicationIntakeRecordDao();
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
                    callback.onError(application.getString(R.string.error_database_save_failed) + e.getMessage());
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
                    callback.onError(application.getString(R.string.error_database_save_failed) + e.getMessage());
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
        
        // Basic quantity & unit validation
        if (medication.getTotalQuantity() < 0) {
            result.addError("总量必须是非负整数");
        }
        if (medication.getRemainingQuantity() < 0) {
            result.addError("剩余量必须是非负整数");
        }
        if (medication.getRemainingQuantity() > medication.getTotalQuantity()) {
            result.addError("剩余量不能大于总量");
        }
        if (medication.getUnit() == null || medication.getUnit().trim().isEmpty()) {
            result.addError("请选择单位");
        }

        return result;
    }
    
    /**
     * 用药扣减功能
     * 当用户确认用药时，自动减少药物剩余量并创建用药记录
     * 
     * @param medicationId 药物ID
     * @param callback 回调接口处理结果
     */
    public void consumeMedication(long medicationId, ConsumeCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                // 获取药物信息
                MedicationInfo medication = medicationDao.getMedicationByIdSync(medicationId);
                if (medication == null) {
                    if (callback != null) {
                        callback.onError("未找到指定的药物");
                    }
                    return;
                }
                
                // 检查库存是否足够
                int dosagePerIntake = medication.getDosagePerIntake();
                int currentRemaining = medication.getRemainingQuantity();
                
                // 计算用药后的剩余量：剩余量 = max(0, 剩余量 - 每次用量)
                int newRemainingQuantity = Math.max(0, currentRemaining - dosagePerIntake);
                
                // 更新药物剩余量
                long currentTime = System.currentTimeMillis();
                int rowsUpdated = medicationDao.updateMedicationQuantity(medicationId, newRemainingQuantity, currentTime);
                
                if (rowsUpdated == 0) {
                    if (callback != null) {
                        callback.onError("更新药物库存失败");
                    }
                    return;
                }
                
                // 创建用药记录
                MedicationIntakeRecord intakeRecord = new MedicationIntakeRecord();
                intakeRecord.setMedicationName(medication.getName());
                intakeRecord.setIntakeTime(currentTime);
                intakeRecord.setDosageTaken(dosagePerIntake);
                
                long recordId = intakeRecordDao.insertIntakeRecord(intakeRecord);
                
                if (recordId > 0) {
                    // 检查库存状态并返回结果
                    boolean isLowStock = newRemainingQuantity <= medication.getLowStockThreshold() && newRemainingQuantity > 0;
                    boolean isOutOfStock = newRemainingQuantity == 0;
                    
                    if (callback != null) {
                        callback.onSuccess(newRemainingQuantity, isLowStock, isOutOfStock);
                    }
                } else {
                    if (callback != null) {
                        callback.onError("创建用药记录失败");
                    }
                }
                
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("用药操作失败: " + e.getMessage());
                }
            }
        });
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
    
    /**
     * 用药扣减操作的回调接口
     */
    public interface ConsumeCallback {
        /**
         * 用药成功时调用
         * 
         * @param newRemainingQuantity 更新后的剩余量
         * @param isLowStock 是否库存不足
         * @param isOutOfStock 是否缺货
         */
        void onSuccess(int newRemainingQuantity, boolean isLowStock, boolean isOutOfStock);
        
        /**
         * 用药失败时调用
         * 
         * @param errorMessage 错误信息
         */
        void onError(String errorMessage);
    }
}