package com.medication.reminders.viewmodels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.medication.reminders.database.entity.HealthDiary;
import com.medication.reminders.models.RepositoryCallback;
import com.medication.reminders.repository.HealthDiaryRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HealthDiaryViewModel类 - 健康日记业务逻辑层
 * 基于MVVM架构模式，管理健康日记的所有业务逻辑
 * 提供LiveData数据绑定，实现响应式UI更新
 * 包含CRUD操作、输入验证和错误处理逻辑
 */
public class HealthDiaryViewModel extends AndroidViewModel {
    
    private static final String TAG = "HealthDiaryViewModel";
    
    // Repository依赖
    private HealthDiaryRepository healthDiaryRepository;
    
    // 线程管理
    private Handler mainHandler;
    private ExecutorService executorService;
    private boolean isTestMode = false;
    
    // ========== LiveData属性 - UI状态管理 ==========
    
    // 日记数据相关
    private MutableLiveData<List<HealthDiary>> userDiaries = new MutableLiveData<>();
    private LiveData<Integer> diaryCount;
    private MutableLiveData<HealthDiary> selectedDiary;
    
    // 操作状态相关
    private MutableLiveData<String> operationResult;
    private MutableLiveData<Boolean> operationSuccess;
    private MutableLiveData<Boolean> isLoading;
    
    // 错误处理相关
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<String> successMessage;
    private MutableLiveData<String> validationError;
    
    // CRUD操作状态
    private MutableLiveData<Boolean> addSuccess;
    private MutableLiveData<Boolean> updateSuccess;
    private MutableLiveData<Boolean> deleteSuccess;
    
    /**
     * 构造函数 - 初始化HealthDiaryRepository和LiveData属性
     * @param application 应用程序实例
     */
    public HealthDiaryViewModel(@NonNull Application application) {
        super(application);
        initializeViewModel(application);
    }
    
    /**
     * 测试构造函数 - 支持同步行为
     * @param application 应用程序实例
     * @param isTestMode 是否为测试模式
     */
    public HealthDiaryViewModel(@NonNull Application application, boolean isTestMode) {
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
        this.healthDiaryRepository = HealthDiaryRepository.getInstance(application);
        
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
        
        // 设置与Repository的连接
        setupRepositoryConnection();
    }
    
    /**
     * 初始化所有LiveData属性
     */
    private void initializeLiveData() {
        // 日记数据相关
        this.selectedDiary = new MutableLiveData<>();
        
        // 操作状态相关
        this.operationResult = new MutableLiveData<>();
        this.operationSuccess = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>();
        
        // 错误处理相关
        this.errorMessage = new MutableLiveData<>();
        this.successMessage = new MutableLiveData<>();
        this.validationError = new MutableLiveData<>();
        
        // CRUD操作状态
        this.addSuccess = new MutableLiveData<>();
        this.updateSuccess = new MutableLiveData<>();
        this.deleteSuccess = new MutableLiveData<>();
    }
    
    /**
     * 设置与HealthDiaryRepository的连接
     * 获取用户日记和日记数量的LiveData引用
     */
    private void setupRepositoryConnection() {
        // 只在初始化时获取LiveData引用，避免重复获取导致连接断开
        if (this.userDiaries == null) {
            this.userDiaries.postValue(healthDiaryRepository.getUserDiaries().getValue());
            android.util.Log.d(TAG, "初始化用户日记LiveData连接");
        }
        
        if (this.diaryCount == null) {
            this.diaryCount = healthDiaryRepository.getUserDiaryCount();
            android.util.Log.d(TAG, "初始化日记数量LiveData连接");
        }
        
        // 触发数据刷新（不重新获取LiveData引用）
        if (!isTestMode) {
            executorService.execute(() -> {
                try {
                    // 触发Repository重新查询数据以更新LiveData
                    healthDiaryRepository.getUserDiariesAsync(new RepositoryCallback<List<HealthDiary>>() {
                        @Override
                        public void onSuccess(List<HealthDiary> diaries) {
                            android.util.Log.d(TAG, "数据刷新成功，获取到 " + diaries.size() + " 条日记");
                            // 数据已通过LiveData自动更新，这里不需要额外操作
                            userDiaries.postValue(diaries);
                        }
                        
                        @Override
                        public void onError(String error) {
                            android.util.Log.e(TAG, "数据刷新失败: " + error);
                            postToMainThread(() -> setError("获取数据失败：" + error));
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e(TAG, "数据连接异常", e);
                    postToMainThread(() -> setError("数据连接异常：" + e.getMessage()));
                }
            });
        }
    }

    // ========== LiveData Getter方法 ==========
    
    /**
     * 获取用户日记列表LiveData
     * @return 用户日记列表的LiveData对象
     */
    public LiveData<List<HealthDiary>> getUserDiaries() {
        return userDiaries;
    }
    
    /**
     * 获取日记数量LiveData
     * @return 日记数量的LiveData对象
     */
    public LiveData<Integer> getDiaryCount() {
        return diaryCount;
    }
    
    /**
     * 获取选中日记LiveData
     * @return 选中日记的LiveData对象
     */
    public LiveData<HealthDiary> getSelectedDiary() {
        return selectedDiary;
    }
    
    /**
     * 获取操作结果LiveData
     * @return 操作结果消息的LiveData
     */
    public LiveData<String> getOperationResult() {
        return operationResult;
    }
    
    /**
     * 获取操作成功状态LiveData
     * @return 操作成功状态的LiveData
     */
    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }
    
    /**
     * 获取加载状态LiveData
     * @return 加载状态的LiveData
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * 获取错误消息LiveData
     * @return 错误消息的LiveData
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 获取成功消息LiveData
     * @return 成功消息的LiveData
     */
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    /**
     * 获取验证错误LiveData
     * @return 验证错误的LiveData
     */
    public LiveData<String> getValidationError() {
        return validationError;
    }
    
    /**
     * 获取添加成功状态LiveData
     * @return 添加成功状态的LiveData
     */
    public LiveData<Boolean> getAddSuccess() {
        return addSuccess;
    }
    
    /**
     * 获取更新成功状态LiveData
     * @return 更新成功状态的LiveData
     */
    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }
    
    /**
     * 获取删除成功状态LiveData
     * @return 删除成功状态的LiveData
     */
    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccess;
    }
    
    // ========== 状态管理方法 ==========
    
    /**
     * 清除错误消息
     */
    public void clearErrorMessage() {
        this.errorMessage.setValue(null);
    }
    
    /**
     * 清除成功消息
     */
    public void clearSuccessMessage() {
        this.successMessage.setValue(null);
    }
    
    /**
     * 清除验证错误
     */
    public void clearValidationError() {
        this.validationError.setValue(null);
    }
    
    /**
     * 清除操作结果
     */
    public void clearOperationResult() {
        this.operationResult.setValue(null);
        this.operationSuccess.setValue(false);
    }
    
    /**
     * 清除所有错误状态
     */
    public void clearAllErrors() {
        clearErrorMessage();
        clearValidationError();
        clearOperationResult();
    }
    
    /**
     * 重置CRUD操作状态
     */
    public void resetOperationStates() {
        postToMainThread(() -> {
            this.addSuccess.setValue(false);
            this.updateSuccess.setValue(false);
            this.deleteSuccess.setValue(false);
            this.operationResult.setValue(null);
            this.operationSuccess.setValue(false);
        });
    }
    
    /**
     * 清除选中的日记
     */
    public void clearSelectedDiary() {
        this.selectedDiary.setValue(null);
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 将操作发布到主线程的辅助方法
     * @param runnable 要在主线程上运行的操作
     */
    private void postToMainThread(Runnable runnable) {
        if (isTestMode) {
            // 在测试模式下直接运行
            runnable.run();
        } else {
            // 在生产环境中发布到主线程
            if (mainHandler != null) {
                mainHandler.post(runnable);
            } else {
                runnable.run();
            }
        }
    }
    
    /**
     * 设置加载状态
     * @param loading 是否正在加载
     */
    private void setLoading(boolean loading) {
        postToMainThread(() -> isLoading.setValue(loading));
    }
    
    /**
     * 设置错误消息
     * @param message 错误消息
     */
    private void setError(String message) {
        postToMainThread(() -> {
            errorMessage.setValue(message);
            operationResult.setValue(message);
            operationSuccess.setValue(false);
            isLoading.setValue(false);
        });
    }
    
    /**
     * 设置成功消息
     * @param message 成功消息
     */
    private void setSuccess(String message) {
        postToMainThread(() -> {
            successMessage.setValue(message);
            operationResult.setValue(message);
            operationSuccess.setValue(true);
            isLoading.setValue(false);
        });
    }
    
    /**
     * 设置验证错误
     * @param message 验证错误消息
     */
    private void setValidationError(String message) {
        postToMainThread(() -> {
            validationError.setValue(message);
            operationResult.setValue(message);
            operationSuccess.setValue(false);
            isLoading.setValue(false);
        });
    }
    
    // ========== 输入验证方法 ==========
    
    /**
     * 验证日记内容
     * @param content 日记内容
     * @return 验证错误消息，如果验证通过则返回null
     */
    public String validateDiaryContent(String content) {
        return HealthDiaryRepository.validateDiaryContent(content);
    }
    
    /**
     * 验证日记内容并设置验证错误状态
     * @param content 日记内容
     * @return 如果验证通过返回true，否则返回false
     */
    public boolean validateAndSetError(String content) {
        String validationResult = validateDiaryContent(content);
        if (validationResult != null) {
            setValidationError(validationResult);
            return false;
        }
        clearValidationError();
        return true;
    }
    
    // ========== 日记CRUD操作业务逻辑方法 ==========
    
    /**
     * 添加新的健康日记
     * @param content 日记内容
     */
    public void addDiary(String content) {
        // 清除之前的状态
        clearAllErrors();
        
        // 验证输入
        if (!validateAndSetError(content)) {
            return;
        }
        
        // 设置加载状态
        setLoading(true);
        
        // 创建日记实体
        HealthDiary diary = new HealthDiary();
        diary.setContent(content.trim());
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performAddDiarySync(diary);
        } else {
            performAddDiaryAsync(diary);
        }
    }
    
    /**
     * 同步添加日记（测试用）
     * @param diary 日记实体
     */
    private void performAddDiarySync(HealthDiary diary) {
        healthDiaryRepository.addDiary(diary, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long diaryId) {
                addSuccess.setValue(true);
                setSuccess("日记添加成功");
            }
            
            @Override
            public void onError(String error) {
                addSuccess.setValue(false);
                setError("添加日记失败：" + error);
            }
        });
    }
    
    /**
     * 异步添加日记（生产环境）
     * @param diary 日记实体
     */
    private void performAddDiaryAsync(HealthDiary diary) {
        executorService.execute(() -> {
            healthDiaryRepository.addDiary(diary, new RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long diaryId) {
                    postToMainThread(() -> {
                        addSuccess.setValue(true);
                        setSuccess("日记添加成功");
                    });
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> {
                        addSuccess.setValue(false);
                        setError("添加日记失败：" + error);
                    });
                }
            });
        });
    }
    
    /**
     * 更新健康日记
     * @param diaryId 日记ID
     * @param content 新的日记内容
     */
    public void updateDiary(long diaryId, String content) {
        // 清除之前的状态
        clearAllErrors();
        
        // 验证输入
        if (!validateAndSetError(content)) {
            return;
        }
        
        if (diaryId <= 0) {
            setError("日记ID无效");
            return;
        }
        
        // 设置加载状态
        setLoading(true);
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performUpdateDiarySync(diaryId, content);
        } else {
            performUpdateDiaryAsync(diaryId, content);
        }
    }
    
    /**
     * 同步更新日记（测试用）
     * @param diaryId 日记ID
     * @param content 新内容
     */
    private void performUpdateDiarySync(long diaryId, String content) {
        // 首先获取现有日记
        healthDiaryRepository.getDiaryById(diaryId, new RepositoryCallback<HealthDiary>() {
            @Override
            public void onSuccess(HealthDiary existingDiary) {
                // 更新内容
                existingDiary.setContent(content.trim());
                
                // 执行更新
                healthDiaryRepository.updateDiary(existingDiary, new RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        updateSuccess.setValue(true);
                        setSuccess("日记更新成功");
                    }
                    
                    @Override
                    public void onError(String error) {
                        updateSuccess.setValue(false);
                        setError("更新日记失败：" + error);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                updateSuccess.setValue(false);
                setError("获取日记失败：" + error);
            }
        });
    }
    
    /**
     * 异步更新日记（生产环境）
     * @param diaryId 日记ID
     * @param content 新内容
     */
    private void performUpdateDiaryAsync(long diaryId, String content) {
        executorService.execute(() -> {
            // 首先获取现有日记
            healthDiaryRepository.getDiaryById(diaryId, new RepositoryCallback<HealthDiary>() {
                @Override
                public void onSuccess(HealthDiary existingDiary) {
                    // 更新内容
                    existingDiary.setContent(content.trim());
                    
                    // 执行更新
                    healthDiaryRepository.updateDiary(existingDiary, new RepositoryCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            postToMainThread(() -> {
                                updateSuccess.setValue(true);
                                setSuccess("日记更新成功");
                            });
                        }
                        
                        @Override
                        public void onError(String error) {
                            postToMainThread(() -> {
                                updateSuccess.setValue(false);
                                setError("更新日记失败：" + error);
                            });
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> {
                        updateSuccess.setValue(false);
                        setError("获取日记失败：" + error);
                    });
                }
            });
        });
    }
    
    /**
     * 删除健康日记
     * @param diary 要删除的日记实体
     */
    public void deleteDiary(HealthDiary diary) {
        // 清除之前的状态
        clearAllErrors();
        
        // 验证输入
        if (diary == null) {
            setError("日记对象不能为空");
            return;
        }
        
        if (diary.getId() <= 0) {
            setError("日记ID无效");
            return;
        }
        
        // 设置加载状态
        setLoading(true);
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performDeleteDiarySync(diary);
        } else {
            performDeleteDiaryAsync(diary);
        }
    }
    
    /**
     * 根据ID删除健康日记
     * @param diaryId 日记ID
     */
    public void deleteDiaryById(long diaryId) {
        // 清除之前的状态
        clearAllErrors();
        
        // 验证输入
        if (diaryId <= 0) {
            setError("日记ID无效");
            return;
        }
        
        // 设置加载状态
        setLoading(true);
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performDeleteDiaryByIdSync(diaryId);
        } else {
            performDeleteDiaryByIdAsync(diaryId);
        }
    }
    
    /**
     * 同步删除日记（测试用）
     * @param diary 日记实体
     */
    private void performDeleteDiarySync(HealthDiary diary) {
        healthDiaryRepository.deleteDiary(diary, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                deleteSuccess.setValue(true);
                setSuccess("日记删除成功");
                clearSelectedDiary(); // 清除选中的日记
            }
            
            @Override
            public void onError(String error) {
                deleteSuccess.setValue(false);
                setError("删除日记失败：" + error);
            }
        });
    }
    
    /**
     * 异步删除日记（生产环境）
     * @param diary 日记实体
     */
    private void performDeleteDiaryAsync(HealthDiary diary) {
        executorService.execute(() -> {
            healthDiaryRepository.deleteDiary(diary, new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    postToMainThread(() -> {
                        deleteSuccess.setValue(true);
                        setSuccess("日记删除成功");
                        clearSelectedDiary(); // 清除选中的日记
                    });
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> {
                        deleteSuccess.setValue(false);
                        setError("删除日记失败：" + error);
                    });
                }
            });
        });
    }
    
    /**
     * 同步根据ID删除日记（测试用）
     * @param diaryId 日记ID
     */
    private void performDeleteDiaryByIdSync(long diaryId) {
        healthDiaryRepository.deleteById(diaryId, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                deleteSuccess.setValue(true);
                setSuccess("日记删除成功");
                clearSelectedDiary(); // 清除选中的日记
            }
            
            @Override
            public void onError(String error) {
                deleteSuccess.setValue(false);
                setError("删除日记失败：" + error);
            }
        });
    }
    
    /**
     * 异步根据ID删除日记（生产环境）
     * @param diaryId 日记ID
     */
    private void performDeleteDiaryByIdAsync(long diaryId) {
        executorService.execute(() -> {
            healthDiaryRepository.deleteById(diaryId, new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    postToMainThread(() -> {
                        deleteSuccess.setValue(true);
                        setSuccess("日记删除成功");
                        clearSelectedDiary(); // 清除选中的日记
                    });
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> {
                        deleteSuccess.setValue(false);
                        setError("删除日记失败：" + error);
                    });
                }
            });
        });
    }
    
    // ========== 日记查询方法 ==========
    
    /**
     * 根据ID获取日记详情
     * @param diaryId 日记ID
     */
    public void getDiaryById(long diaryId) {
        // 设置加载状态
        setLoading(true);
        clearAllErrors();
        
        // 验证输入
        if (diaryId <= 0) {
            setError("日记ID无效");
            return;
        }
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performGetDiaryByIdSync(diaryId);
        } else {
            performGetDiaryByIdAsync(diaryId);
        }
    }
    
    /**
     * 同步获取日记详情（测试用）
     * @param diaryId 日记ID
     */
    private void performGetDiaryByIdSync(long diaryId) {
        healthDiaryRepository.getDiaryById(diaryId, new RepositoryCallback<HealthDiary>() {
            @Override
            public void onSuccess(HealthDiary diary) {
                selectedDiary.setValue(diary);
                setSuccess("日记加载成功");
            }
            
            @Override
            public void onError(String error) {
                selectedDiary.setValue(null);
                setError("获取日记失败：" + error);
            }
        });
    }
    
    /**
     * 异步获取日记详情（生产环境）
     * @param diaryId 日记ID
     */
    private void performGetDiaryByIdAsync(long diaryId) {
        executorService.execute(() -> {
            healthDiaryRepository.getDiaryById(diaryId, new RepositoryCallback<HealthDiary>() {
                @Override
                public void onSuccess(HealthDiary diary) {
                    postToMainThread(() -> {
                        selectedDiary.setValue(diary);
                        setSuccess("日记加载成功");
                    });
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> {
                        selectedDiary.setValue(null);
                        setError("获取日记失败：" + error);
                    });
                }
            });
        });
    }
    
    /**
     * 搜索日记
     * @param searchQuery 搜索关键词
     * @param callback 搜索结果回调
     */
    public void searchDiaries(String searchQuery, RepositoryCallback<List<HealthDiary>> callback) {
        // 验证输入
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            callback.onError("搜索关键词不能为空");
            return;
        }
        
        // 设置加载状态
        setLoading(true);
        clearAllErrors();
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performSearchDiariesSync(searchQuery, callback);
        } else {
            performSearchDiariesAsync(searchQuery, callback);
        }
    }
    
    /**
     * 同步搜索日记（测试用）
     * @param searchQuery 搜索关键词
     * @param callback 回调接口
     */
    private void performSearchDiariesSync(String searchQuery, RepositoryCallback<List<HealthDiary>> callback) {
        healthDiaryRepository.searchDiaries(searchQuery, new RepositoryCallback<List<HealthDiary>>() {
            @Override
            public void onSuccess(List<HealthDiary> diaries) {
                setSuccess("搜索完成，找到 " + diaries.size() + " 条日记");
                callback.onSuccess(diaries);
            }
            
            @Override
            public void onError(String error) {
                setError("搜索失败：" + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * 异步搜索日记（生产环境）
     * @param searchQuery 搜索关键词
     * @param callback 回调接口
     */
    private void performSearchDiariesAsync(String searchQuery, RepositoryCallback<List<HealthDiary>> callback) {
        executorService.execute(() -> {
            healthDiaryRepository.searchDiaries(searchQuery, new RepositoryCallback<List<HealthDiary>>() {
                @Override
                public void onSuccess(List<HealthDiary> diaries) {
                    postToMainThread(() -> {
                        setSuccess("搜索完成，找到 " + diaries.size() + " 条日记");
                        callback.onSuccess(diaries);
                    });
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> {
                        setError("搜索失败：" + error);
                        callback.onError(error);
                    });
                }
            });
        });
    }
    
    /**
     * 获取指定日期范围内的日记
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @param callback 查询结果回调
     */
    public void getDiariesByDateRange(long startTime, long endTime, RepositoryCallback<List<HealthDiary>> callback) {
        // 验证输入
        if (startTime > endTime) {
            callback.onError("开始时间不能晚于结束时间");
            return;
        }
        
        // 设置加载状态
        setLoading(true);
        clearAllErrors();
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performGetDiariesByDateRangeSync(startTime, endTime, callback);
        } else {
            performGetDiariesByDateRangeAsync(startTime, endTime, callback);
        }
    }
    
    /**
     * 同步获取日期范围内的日记（测试用）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param callback 回调接口
     */
    private void performGetDiariesByDateRangeSync(long startTime, long endTime, RepositoryCallback<List<HealthDiary>> callback) {
        healthDiaryRepository.getDiariesByDateRange(startTime, endTime, new RepositoryCallback<List<HealthDiary>>() {
            @Override
            public void onSuccess(List<HealthDiary> diaries) {
                setSuccess("查询完成，找到 " + diaries.size() + " 条日记");
                callback.onSuccess(diaries);
            }
            
            @Override
            public void onError(String error) {
                setError("查询失败：" + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * 异步获取日期范围内的日记（生产环境）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param callback 回调接口
     */
    private void performGetDiariesByDateRangeAsync(long startTime, long endTime, RepositoryCallback<List<HealthDiary>> callback) {
        executorService.execute(() -> {
            healthDiaryRepository.getDiariesByDateRange(startTime, endTime, new RepositoryCallback<List<HealthDiary>>() {
                @Override
                public void onSuccess(List<HealthDiary> diaries) {
                    postToMainThread(() -> {
                        setSuccess("查询完成，找到 " + diaries.size() + " 条日记");
                        callback.onSuccess(diaries);
                    });
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> {
                        setError("查询失败：" + error);
                        callback.onError(error);
                    });
                }
            });
        });
    }
    
    // ========== 工具方法 ==========
    
    /**
     * 刷新用户日记列表
     * 重新建立与Repository的连接以刷新LiveData
     */
    public void refreshUserDiaries() {
        setupRepositoryConnection();
    }
    
    /**
     * 刷新日记数据
     * 用于Activity中的数据刷新
     */
    public void refreshDiaries() {
        android.util.Log.d(TAG, "开始刷新日记数据");
        
        // 清除错误状态
        clearAllErrors();
        
        // 设置加载状态
        setLoading(true);
        
        // 触发异步刷新以确保Room重新查询并在数据变更时通知现有LiveData观察者
        if (isTestMode) {
            performRefreshSync();
        } else {
            performRefreshAsync();
        }
    }
    
    /**
     * 同步刷新数据（测试用）
     */
    private void performRefreshSync() {
        // 直接设置加载完成，让LiveData自然更新
        setLoading(false);
    }
    
    /**
     * 异步刷新数据（生产环境）
     */
    private void performRefreshAsync() {
        executorService.execute(() -> {
            try {
                android.util.Log.d(TAG, "执行异步数据刷新");
                
                // 直接获取用户日记数据来触发LiveData更新
                healthDiaryRepository.getUserDiariesAsync(new RepositoryCallback<List<HealthDiary>>() {
                    @Override
                    public void onSuccess(List<HealthDiary> diaries) {
                        android.util.Log.d(TAG, "刷新成功，获取到 " + diaries.size() + " 条日记");
                        postToMainThread(() -> {
                            setLoading(false);
                            userDiaries.postValue(diaries); // 更新LiveData
                            // LiveData会自动更新UI，这里不需要手动设置数据
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        android.util.Log.e(TAG, "刷新失败: " + error);
                        postToMainThread(() -> {
                            setLoading(false);
                            if (error.contains("未登录")) {
                                setError("请先登录后再查看健康日记");
                            } else {
                                setError("刷新数据失败：" + error);
                            }
                        });
                    }
                });
                
            } catch (Exception e) {
                android.util.Log.e(TAG, "数据刷新异常", e);
                postToMainThread(() -> {
                    setLoading(false);
                    setError("数据刷新异常：" + e.getMessage());
                });
            }
        });
    }
    
    /**
     * 获取ViewModel状态信息
     * @return 状态信息字符串
     */
    public String getViewModelStatus() {
        return String.format("HealthDiaryViewModel状态: Repository=%s, 线程池=%s, 测试模式=%s", 
            (healthDiaryRepository != null ? "已连接" : "未连接"),
            (executorService != null && !executorService.isShutdown() ? "运行中" : "已停止"),
            isTestMode);
    }
    
    // ========== 生命周期管理 ==========
    
    /**
     * 清理资源以防止内存泄漏
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        
        // 关闭ViewModel自己的线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        // 清理Handler回调
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        
        // 注意：不要清理Repository资源，因为Repository是单例，可能被其他地方使用
        // Repository的资源清理应该在应用退出时进行，而不是在ViewModel销毁时
    }
}