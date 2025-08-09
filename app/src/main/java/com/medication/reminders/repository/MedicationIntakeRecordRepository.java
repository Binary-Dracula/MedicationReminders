package com.medication.reminders.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.medication.reminders.database.DatabaseErrorHandler;
import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.MedicationIntakeRecordDao;
import com.medication.reminders.database.dao.UserDao;
import com.medication.reminders.database.entity.MedicationIntakeRecord;
import com.medication.reminders.database.entity.User;
import com.medication.reminders.models.BaseDataAccess;
import com.medication.reminders.models.RepositoryCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MedicationIntakeRecordRepository类，实现Repository模式
 * 基于Room数据库的用药记录数据访问层，提供统一的用药记录管理接口
 * 遵循MVVM架构模式，封装数据访问逻辑
 */
public class MedicationIntakeRecordRepository implements BaseDataAccess<MedicationIntakeRecord, Long> {
    
    private static final String TAG = "MedicationIntakeRecordRepository";
    
    private MedicationIntakeRecordDao intakeRecordDao;
    private UserDao userDao;
    private Context context;
    private ExecutorService executorService;
    
    // 单例实例
    private static volatile MedicationIntakeRecordRepository INSTANCE;
    
    /**
     * 构造函数（支持单例和直接实例化）
     * @param context 应用程序上下文
     */
    public MedicationIntakeRecordRepository(Context context) {
        initializeRepository(context);
    }
    
    /**
     * 初始化Repository的通用方法
     * @param context 应用程序上下文
     */
    private void initializeRepository(Context context) {
        MedicationDatabase database = MedicationDatabase.getDatabase(context);
        this.intakeRecordDao = database.medicationIntakeRecordDao();
        this.userDao = database.userDao();
        this.context = context.getApplicationContext();
        this.executorService = Executors.newFixedThreadPool(2);
        
        Log.d(TAG, "MedicationIntakeRecordRepository 初始化完成");
    }
    
    /**
     * 获取MedicationIntakeRecordRepository单例实例
     * @param context 应用程序上下文
     * @return MedicationIntakeRecordRepository实例
     */
    public static MedicationIntakeRecordRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MedicationIntakeRecordRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MedicationIntakeRecordRepository(context);
                }
            }
        }
        return INSTANCE;
    }
    
    // ========== BaseDataAccess接口实现 ==========
    
    /**
     * 插入新的用药记录
     * @param entity 用药记录实体
     * @param callback 操作结果回调
     */
    @Override
    public void insert(MedicationIntakeRecord entity, RepositoryCallback<Long> callback) {
        addIntakeRecord(entity, callback);
    }
    
    /**
     * 更新现有用药记录
     * @param entity 用药记录实体
     * @param callback 操作结果回调
     */
    @Override
    public void update(MedicationIntakeRecord entity, RepositoryCallback<Boolean> callback) {
        updateIntakeRecord(entity, callback);
    }
    
    /**
     * 根据ID删除用药记录
     * @param id 记录ID
     * @param callback 操作结果回调
     */
    @Override
    public void deleteById(Long id, RepositoryCallback<Boolean> callback) {
        executeTask(() -> {
            try {
                // 获取记录实体
                MedicationIntakeRecord record = intakeRecordDao.getIntakeRecordByIdSync(id);
                if (record == null) {
                    callback.onError("用药记录不存在");
                    return;
                }
                
                // 删除记录
                int result = intakeRecordDao.deleteIntakeRecord(record);
                DatabaseErrorHandler.DatabaseResult<Integer> deleteResult = 
                    DatabaseErrorHandler.validateDeleteResult(result);
                
                if (deleteResult.isSuccess()) {
                    DatabaseErrorHandler.logDatabaseOperation("DELETE", "medication_intake_record", true, 
                        "用药记录删除成功，ID: " + id);
                    callback.onSuccess(true);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("DELETE", "medication_intake_record", false, 
                        deleteResult.getErrorMessage());
                    callback.onError(deleteResult.getErrorMessage());
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "删除用药记录");
                DatabaseErrorHandler.logDatabaseOperation("DELETE", "medication_intake_record", false, 
                    error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 根据ID查找用药记录
     * @param id 记录ID
     * @param callback 查询结果回调
     */
    @Override
    public void findById(Long id, RepositoryCallback<MedicationIntakeRecord> callback) {
        getIntakeRecordById(id, callback);
    }
    
    /**
     * 检查用药记录是否存在
     * @param id 记录ID
     * @param callback 检查结果回调
     */
    @Override
    public void exists(Long id, RepositoryCallback<Boolean> callback) {
        executeTask(() -> {
            try {
                MedicationIntakeRecord record = intakeRecordDao.getIntakeRecordByIdSync(id);
                callback.onSuccess(record != null);
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "检查用药记录存在性");
                callback.onError(error.getMessage());
            }
        });
    }
    
    // ========== 用药记录业务方法 ==========
    
    /**
     * 添加新的用药记录
     * @param record 用药记录实体
     * @param callback 操作结果回调
     */
    public void addIntakeRecord(MedicationIntakeRecord record, RepositoryCallback<Long> callback) {
        executeTask(() -> {
            try {
                // 验证输入参数
                if (record == null) {
                    callback.onError("用药记录对象不能为空");
                    return;
                }
                
                if (record.getMedicationName() == null || record.getMedicationName().trim().isEmpty()) {
                    callback.onError("药物名称不能为空");
                    return;
                }
                
                if (record.getDosageTaken() <= 0) {
                    callback.onError("服用剂量必须大于0");
                    return;
                }
                
                // 设置时间戳
                if (record.getIntakeTime() <= 0) {
                    record.setIntakeTime(System.currentTimeMillis());
                }
                
                // 插入记录到数据库
                long recordId = intakeRecordDao.insertIntakeRecord(record);
                DatabaseErrorHandler.DatabaseResult<Long> result = 
                    DatabaseErrorHandler.validateInsertResult(recordId);
                
                if (result.isSuccess()) {
                    DatabaseErrorHandler.logDatabaseOperation("INSERT", "medication_intake_record", true, 
                        "用药记录添加成功，ID: " + recordId);
                    callback.onSuccess(recordId);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("INSERT", "medication_intake_record", false, 
                        result.getErrorMessage());
                    callback.onError(result.getErrorMessage());
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "添加用药记录");
                DatabaseErrorHandler.logDatabaseOperation("INSERT", "medication_intake_record", false, 
                    error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 更新用药记录
     * @param record 用药记录实体
     * @param callback 操作结果回调
     */
    public void updateIntakeRecord(MedicationIntakeRecord record, RepositoryCallback<Boolean> callback) {
        executeTask(() -> {
            try {
                // 验证输入参数
                if (record == null) {
                    callback.onError("用药记录对象不能为空");
                    return;
                }
                
                if (record.getId() <= 0) {
                    callback.onError("用药记录ID无效");
                    return;
                }
                
                // 检查记录是否存在
                MedicationIntakeRecord existingRecord = intakeRecordDao.getIntakeRecordByIdSync(record.getId());
                if (existingRecord == null) {
                    callback.onError("用药记录不存在");
                    return;
                }
                
                // 更新记录
                int result = intakeRecordDao.updateIntakeRecord(record);
                DatabaseErrorHandler.DatabaseResult<Integer> updateResult = 
                    DatabaseErrorHandler.validateUpdateResult(result);
                
                if (updateResult.isSuccess()) {
                    DatabaseErrorHandler.logDatabaseOperation("UPDATE", "medication_intake_record", true, 
                        "用药记录更新成功，ID: " + record.getId());
                    callback.onSuccess(true);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("UPDATE", "medication_intake_record", false, 
                        updateResult.getErrorMessage());
                    callback.onError(updateResult.getErrorMessage());
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "更新用药记录");
                DatabaseErrorHandler.logDatabaseOperation("UPDATE", "medication_intake_record", false, 
                    error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 根据ID获取用药记录
     * @param recordId 记录ID
     * @param callback 查询结果回调
     */
    public void getIntakeRecordById(long recordId, RepositoryCallback<MedicationIntakeRecord> callback) {
        executeTask(() -> {
            try {
                // 获取记录
                MedicationIntakeRecord record = intakeRecordDao.getIntakeRecordByIdSync(recordId);
                if (record == null) {
                    callback.onError("用药记录不存在");
                    return;
                }
                
                callback.onSuccess(record);
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "获取用药记录");
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 获取所有用药记录（LiveData）
     * @return 用药记录列表的LiveData
     */
    public LiveData<List<MedicationIntakeRecord>> getAllIntakeRecords() {
        Log.d(TAG, "获取所有用药记录");
        return intakeRecordDao.getAllIntakeRecords();
    }
    
    /**
     * 获取最近的用药记录（LiveData）
     * @param limit 记录数量限制
     * @return 最近用药记录列表的LiveData
     */
    public LiveData<List<MedicationIntakeRecord>> getRecentIntakeRecords(int limit) {
        Log.d(TAG, "获取最近 " + limit + " 条用药记录");
        return intakeRecordDao.getRecentIntakeRecords(limit);
    }
    
    /**
     * 根据药物名称获取用药记录（LiveData）
     * @param medicationName 药物名称
     * @return 该药物的用药记录列表的LiveData
     */
    public LiveData<List<MedicationIntakeRecord>> getIntakeRecordsByMedicationName(String medicationName) {
        Log.d(TAG, "获取药物 " + medicationName + " 的用药记录");
        return intakeRecordDao.getIntakeRecordsByMedicationName(medicationName);
    }
    
    /**
     * 获取指定时间范围内的用药记录（LiveData）
     * @param startTime 开始时间（时间戳）
     * @param endTime 结束时间（时间戳）
     * @return 时间范围内的用药记录列表的LiveData
     */
    public LiveData<List<MedicationIntakeRecord>> getIntakeRecordsByTimeRange(long startTime, long endTime) {
        Log.d(TAG, "获取时间范围内的用药记录: " + startTime + " - " + endTime);
        return intakeRecordDao.getIntakeRecordsByTimeRange(startTime, endTime);
    }
    
    /**
     * 获取用药记录总数（LiveData）
     * @return 用药记录总数的LiveData
     */
    public LiveData<Integer> getIntakeRecordCount() {
        Log.d(TAG, "获取用药记录总数");
        return intakeRecordDao.getIntakeRecordCount();
    }
    
    /**
     * 异步获取所有用药记录
     * @param callback 查询结果回调
     */
    public void getAllIntakeRecordsAsync(RepositoryCallback<List<MedicationIntakeRecord>> callback) {
        executeTask(() -> {
            try {
                // 由于DAO中没有同步方法，我们需要添加一个
                // 这里暂时使用LiveData的getValue()方法，但这不是最佳实践
                // 在实际应用中，应该在DAO中添加同步查询方法
                callback.onError("暂不支持异步获取所有记录，请使用LiveData方式");
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "获取所有用药记录");
                callback.onError(error.getMessage());
            }
        });
    }
    
    // ========== 清理方法 ==========
    
    /**
     * 清理资源（用于测试或应用关闭时）
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        Log.d(TAG, "MedicationIntakeRecordRepository 资源清理完成");
    }
    
    // ========== 工具方法 ==========
    
    /**
     * 安全地执行异步任务
     * 如果线程池已关闭，会重新创建线程池
     * @param task 要执行的任务
     */
    private void executeTask(Runnable task) {
        try {
            // 检查线程池状态，如果已关闭则重新创建
            if (executorService == null || executorService.isShutdown() || executorService.isTerminated()) {
                synchronized (this) {
                    if (executorService == null || executorService.isShutdown() || executorService.isTerminated()) {
                        Log.d(TAG, "重新创建线程池");
                        executorService = Executors.newFixedThreadPool(2);
                    }
                }
            }
            
            executorService.submit(task);
        } catch (Exception e) {
            Log.e(TAG, "执行异步任务失败", e);
            // 如果线程池执行失败，尝试在当前线程执行
            try {
                task.run();
            } catch (Exception taskException) {
                Log.e(TAG, "在当前线程执行任务也失败", taskException);
            }
        }
    }
    
    /**
     * 验证用药记录内容
     * @param record 用药记录
     * @return 验证结果消息，null表示验证通过
     */
    public static String validateIntakeRecord(MedicationIntakeRecord record) {
        if (record == null) {
            return "用药记录不能为空";
        }
        
        if (record.getMedicationName() == null || record.getMedicationName().trim().isEmpty()) {
            return "药物名称不能为空";
        }
        
        if (record.getDosageTaken() <= 0) {
            return "服用剂量必须大于0";
        }
        
        if (record.getIntakeTime() <= 0) {
            return "服用时间无效";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 获取Repository实例状态信息
     * @return 状态信息字符串
     */
    public String getRepositoryStatus() {
        return String.format("MedicationIntakeRecordRepository状态: 数据库=%s, 线程池=%s", 
            (intakeRecordDao != null ? "已连接" : "未连接"),
            (executorService != null && !executorService.isShutdown() ? "运行中" : "已停止"));
    }
}