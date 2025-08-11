package com.medication.reminders.viewmodels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.medication.reminders.database.entity.MedicationIntakeRecord;
import com.medication.reminders.models.RepositoryCallback;
import com.medication.reminders.repository.MedicationIntakeRecordRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MedicationIntakeRecordViewModel类 - 用药记录业务逻辑层
 * 基于MVVM架构模式，管理用药记录的所有业务逻辑
 * 提供LiveData数据绑定，实现响应式UI更新
 * 包含查询操作和错误处理逻辑
 */
public class MedicationIntakeRecordViewModel extends AndroidViewModel {
    
    private static final String TAG = "MedicationIntakeRecordViewModel";
    
    // Repository依赖
    private MedicationIntakeRecordRepository intakeRecordRepository;
    
    // 线程管理
    private Handler mainHandler;
    private ExecutorService executorService;
    private boolean isTestMode = false;
    
    // ========== LiveData属性 - UI状态管理 ==========
    
    // 用药记录数据相关
    private LiveData<List<MedicationIntakeRecord>> allIntakeRecords;
    private LiveData<List<MedicationIntakeRecord>> recentIntakeRecords;
    private LiveData<Integer> intakeRecordCount;
    private MutableLiveData<MedicationIntakeRecord> selectedIntakeRecord;
    
    // 操作状态相关
    private MutableLiveData<String> operationResult;
    private MutableLiveData<Boolean> operationSuccess;
    private MutableLiveData<Boolean> isLoading;
    
    // 错误处理相关
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<String> successMessage;
    
    /**
     * 构造函数 - 初始化MedicationIntakeRecordRepository和LiveData属性
     * @param application 应用程序实例
     */
    public MedicationIntakeRecordViewModel(@NonNull Application application) {
        super(application);
        initializeViewModel(application);
    }
    
    /**
     * 测试构造函数 - 支持同步行为
     * @param application 应用程序实例
     * @param isTestMode 是否为测试模式
     */
    public MedicationIntakeRecordViewModel(@NonNull Application application, boolean isTestMode) {
        super(application);
        this.isTestMode = isTestMode;
        initializeViewModel(application);
    }
    
    /**
     * 初始化ViewModel的通用方法
     * @param application 应用程序实例
     */
    private void initializeViewModel(Application application) {
        // 初始化Repository
        this.intakeRecordRepository = MedicationIntakeRecordRepository.getInstance(application);
        
        // 初始化线程管理
        if (!isTestMode) {
            try {
                this.mainHandler = new Handler(Looper.getMainLooper());
            } catch (RuntimeException e) {
                // 在测试环境中，Looper可能不可用
                this.isTestMode = true;
            }
        }
        this.executorService = Executors.newFixedThreadPool(2);
        
        // 初始化LiveData属性
        initializeLiveData();
        
        // 加载初始数据
        loadInitialData();
    }
    
    /**
     * 初始化LiveData属性
     */
    private void initializeLiveData() {
        // 数据相关
        this.selectedIntakeRecord = new MutableLiveData<>();
        
        // 状态相关
        this.operationResult = new MutableLiveData<>();
        this.operationSuccess = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
        
        // 错误处理相关
        this.errorMessage = new MutableLiveData<>();
        this.successMessage = new MutableLiveData<>();
    }
    
    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        // 获取所有用药记录
        this.allIntakeRecords = intakeRecordRepository.getAllIntakeRecords();
        
        // 获取最近的用药记录（限制10条）
        this.recentIntakeRecords = intakeRecordRepository.getRecentIntakeRecords(10);
        
        // 获取用药记录总数
        this.intakeRecordCount = intakeRecordRepository.getIntakeRecordCount();
    }
    
    // ========== Getter方法 - 提供LiveData访问 ==========
    
    /**
     * 获取所有用药记录的LiveData
     * @return 所有用药记录列表的LiveData
     */
    public LiveData<List<MedicationIntakeRecord>> getAllIntakeRecords() {
        return allIntakeRecords;
    }
    
    /**
     * 获取最近用药记录的LiveData
     * @return 最近用药记录列表的LiveData
     */
    public LiveData<List<MedicationIntakeRecord>> getRecentIntakeRecords() {
        return recentIntakeRecords;
    }
    
    /**
     * 获取用药记录总数的LiveData
     * @return 用药记录总数的LiveData
     */
    public LiveData<Integer> getIntakeRecordCount() {
        return intakeRecordCount;
    }
    
    /**
     * 获取选中的用药记录
     * @return 选中用药记录的LiveData
     */
    public LiveData<MedicationIntakeRecord> getSelectedIntakeRecord() {
        return selectedIntakeRecord;
    }
    
    /**
     * 获取操作结果消息
     * @return 操作结果消息的LiveData
     */
    public LiveData<String> getOperationResult() {
        return operationResult;
    }
    
    /**
     * 获取操作成功状态
     * @return 操作成功状态的LiveData
     */
    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }
    
    /**
     * 获取加载状态
     * @return 加载状态的LiveData
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * 获取错误消息
     * @return 错误消息的LiveData
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 获取成功消息
     * @return 成功消息的LiveData
     */
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    // ========== 业务方法 ==========
    
    /**
     * 根据ID获取用药记录
     * @param recordId 记录ID
     */
    public void getIntakeRecordById(long recordId) {
        setLoading(true);
        clearMessages();
        
        intakeRecordRepository.getIntakeRecordById(recordId, new RepositoryCallback<MedicationIntakeRecord>() {
            @Override
            public void onSuccess(MedicationIntakeRecord record) {
                runOnMainThread(() -> {
                    selectedIntakeRecord.setValue(record);
                    setLoading(false);
                    setSuccessMessage(getApplication().getString(com.medication.reminders.R.string.intake_record_load_success));
                });
            }
            
            @Override
            public void onError(String error) {
                runOnMainThread(() -> {
                    setLoading(false);
                    setErrorMessage(getApplication().getString(com.medication.reminders.R.string.intake_record_load_failed, error));
                });
            }
        });
    }
    
    /**
     * 根据药物名称获取用药记录
     * @param medicationName 药物名称
     * @return 该药物的用药记录列表的LiveData
     */
    public LiveData<List<MedicationIntakeRecord>> getIntakeRecordsByMedicationName(String medicationName) {
        return intakeRecordRepository.getIntakeRecordsByMedicationName(medicationName);
    }
    
    /**
     * 根据时间范围获取用药记录
     * @param startTime 开始时间（时间戳）
     * @param endTime 结束时间（时间戳）
     * @return 时间范围内的用药记录列表的LiveData
     */
    public LiveData<List<MedicationIntakeRecord>> getIntakeRecordsByTimeRange(long startTime, long endTime) {
        return intakeRecordRepository.getIntakeRecordsByTimeRange(startTime, endTime);
    }
    
    /**
     * 刷新用药记录数据
     */
    public void refreshIntakeRecords() {
        setLoading(true);
        clearMessages();
        
        // 重新加载数据
        loadInitialData();
        
        runOnMainThread(() -> {
            setLoading(false);
            setSuccessMessage(getApplication().getString(com.medication.reminders.R.string.intake_record_refreshed));
        });
    }
    
    // ========== 工具方法 ==========
    
    /**
     * 设置加载状态
     * @param loading 是否正在加载
     */
    private void setLoading(boolean loading) {
        runOnMainThread(() -> isLoading.setValue(loading));
    }
    
    /**
     * 设置错误消息
     * @param message 错误消息
     */
    private void setErrorMessage(String message) {
        runOnMainThread(() -> {
            errorMessage.setValue(message);
            operationSuccess.setValue(false);
            operationResult.setValue(message);
        });
    }
    
    /**
     * 设置成功消息
     * @param message 成功消息
     */
    private void setSuccessMessage(String message) {
        runOnMainThread(() -> {
            successMessage.setValue(message);
            operationSuccess.setValue(true);
            operationResult.setValue(message);
        });
    }
    
    /**
     * 清除所有消息
     */
    private void clearMessages() {
        runOnMainThread(() -> {
            errorMessage.setValue(null);
            successMessage.setValue(null);
            operationResult.setValue(null);
            operationSuccess.setValue(null);
        });
    }
    
    /**
     * 在主线程中运行任务
     * @param task 要执行的任务
     */
    private void runOnMainThread(Runnable task) {
        if (isTestMode || mainHandler == null) {
            // 测试模式或主线程Handler不可用时，直接执行
            task.run();
        } else {
            // 正常模式下，在主线程执行
            mainHandler.post(task);
        }
    }
    
    /**
     * 在后台线程中执行任务
     * @param task 要执行的任务
     */
    private void executeInBackground(Runnable task) {
        if (isTestMode) {
            // 测试模式下直接执行
            task.run();
        } else {
            // 正常模式下在后台线程执行
            executorService.submit(task);
        }
    }
    
    /**
     * 检查ViewModel是否处于测试模式
     * @return 如果是测试模式返回true，否则返回false
     */
    public boolean isTestMode() {
        return isTestMode;
    }
    

    // ========== 生命周期管理 ==========
    
    /**
     * 清理资源
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        
        // 清理线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        // 清理Repository资源
        if (intakeRecordRepository != null) {
            intakeRecordRepository.cleanup();
        }
    }
}