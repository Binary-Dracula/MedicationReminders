package com.medication.reminders.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.medication.reminders.R;
import com.medication.reminders.database.DatabaseErrorHandler;
import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.HealthDiaryDao;
import com.medication.reminders.database.dao.UserDao;
import com.medication.reminders.database.entity.HealthDiary;
import com.medication.reminders.database.entity.User;
import com.medication.reminders.models.BaseDataAccess;
import com.medication.reminders.models.RepositoryCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HealthDiaryRepository类，实现Repository模式
 * 基于Room数据库的健康日记数据访问层，提供统一的健康日记管理接口
 * 遵循MVVM架构模式，封装数据访问逻辑
 */
public class HealthDiaryRepository implements BaseDataAccess<HealthDiary, Long> {
    
    private static final String TAG = "HealthDiaryRepository";
    
    private HealthDiaryDao healthDiaryDao;
    private UserDao userDao;
    private Application context;
    private ExecutorService executorService;
    private UserRepository userRepository;
    
    // 单例实例
    private static volatile HealthDiaryRepository INSTANCE;
    
    /**
     * 构造函数（支持单例和直接实例化）
     * @param context 应用程序上下文
     */
    public HealthDiaryRepository(Application context) {
        initializeRepository(context);
    }
    
    /**
     * 初始化Repository的通用方法
     * @param context 应用程序上下文
     */
    private void initializeRepository(Application context) {
        this.context = context;
        MedicationDatabase database = MedicationDatabase.getDatabase(context);
        this.healthDiaryDao = database.healthDiaryDao();
        this.userDao = database.userDao();
        this.executorService = Executors.newFixedThreadPool(2);
        this.userRepository = UserRepository.getInstance(context);
        
        Log.d(TAG, "HealthDiaryRepository 初始化完成");
    }
    
    /**
     * 获取HealthDiaryRepository单例实例
     * @param context 应用程序上下文
     * @return HealthDiaryRepository实例
     */
    public static HealthDiaryRepository getInstance(Application context) {
        if (INSTANCE == null) {
            synchronized (HealthDiaryRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HealthDiaryRepository(context);
                }
            }
        }
        return INSTANCE;
    }
    
    // ========== BaseDataAccess接口实现 ==========
    
    /**
     * 插入新的健康日记
     * @param entity 健康日记实体
     * @param callback 操作结果回调
     */
    @Override
    public void insert(HealthDiary entity, RepositoryCallback<Long> callback) {
        addDiary(entity, callback);
    }
    
    /**
     * 更新现有健康日记
     * @param entity 健康日记实体
     * @param callback 操作结果回调
     */
    @Override
    public void update(HealthDiary entity, RepositoryCallback<Boolean> callback) {
        updateDiary(entity, callback);
    }
    
    /**
     * 根据ID删除健康日记
     * @param id 日记ID
     * @param callback 操作结果回调
     */
    @Override
    public void deleteById(Long id, RepositoryCallback<Boolean> callback) {
        executeTask(() -> {
            try {
                // 验证用户权限
                long currentUserId = getCurrentLoggedInUserId();
                if (currentUserId <= 0) {
                    callback.onError(context.getString(R.string.error_user_not_logged_in));
                    return;
                }
                
                // 获取日记实体
                HealthDiary diary = healthDiaryDao.getDiaryByIdSync(id);
                if (diary == null) {
                    callback.onError("日记不存在");
                    return;
                }
                
                // 验证用户权限
                if (diary.getUserId() != currentUserId) {
                    callback.onError("无权限删除此日记");
                    return;
                }
                
                // 删除日记
                int result = healthDiaryDao.deleteDiary(diary);
                DatabaseErrorHandler.DatabaseResult<Integer> deleteResult = 
                    DatabaseErrorHandler.validateDeleteResult(result);
                
                if (deleteResult.isSuccess()) {
                    DatabaseErrorHandler.logDatabaseOperation("DELETE", "health_diary", true, 
                        "健康日记删除成功，ID: " + id);
                    callback.onSuccess(true);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("DELETE", "health_diary", false, 
                        deleteResult.getErrorMessage());
                    callback.onError(deleteResult.getErrorMessage());
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "删除健康日记");
                DatabaseErrorHandler.logDatabaseOperation("DELETE", "health_diary", false, 
                    error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 根据ID查找健康日记
     * @param id 日记ID
     * @param callback 查询结果回调
     */
    @Override
    public void findById(Long id, RepositoryCallback<HealthDiary> callback) {
        getDiaryById(id, callback);
    }
    
    /**
     * 检查健康日记是否存在
     * @param id 日记ID
     * @param callback 检查结果回调
     */
    @Override
    public void exists(Long id, RepositoryCallback<Boolean> callback) {
        executeTask(() -> {
            try {
                HealthDiary diary = healthDiaryDao.getDiaryByIdSync(id);
                callback.onSuccess(diary != null);
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "检查健康日记存在性");
                callback.onError(error.getMessage());
            }
        });
    }
    
    // ========== 健康日记业务方法 ==========
    
    /**
     * 添加新的健康日记
     * @param diary 健康日记实体
     * @param callback 操作结果回调
     */
    public void addDiary(HealthDiary diary, RepositoryCallback<Long> callback) {
        executeTask(() -> {
            try {
                // 验证输入参数
                if (diary == null) {
                    callback.onError(context.getString(R.string.error_diary_object_null));
                    return;
                }
                
                if (diary.getContent() == null || diary.getContent().trim().isEmpty()) {
                    callback.onError(context.getString(R.string.error_diary_content_empty));
                    return;
                }
                
                if (diary.getContent().length() > 5000) {
                    callback.onError(context.getString(R.string.error_diary_content_too_long));
                    return;
                }
                
                // 验证用户身份
                long currentUserId = getCurrentLoggedInUserId();
                if (currentUserId <= 0) {
                    callback.onError(context.getString(R.string.error_user_not_logged_in));
                    return;
                }
                
                // 验证用户是否存在
                User user = userDao.getUserById(currentUserId);
                if (user == null) {
                    callback.onError(context.getString(R.string.error_user_not_exists));
                    return;
                }
                
                // 设置用户ID和时间戳
                diary.setUserId(currentUserId);
                long currentTime = System.currentTimeMillis();
                diary.setCreatedAt(currentTime);
                diary.setUpdatedAt(currentTime);
                
                // 插入日记到数据库
                long diaryId = healthDiaryDao.insertDiary(diary);
                DatabaseErrorHandler.DatabaseResult<Long> result = 
                    DatabaseErrorHandler.validateInsertResult(diaryId);
                
                if (result.isSuccess()) {
                    DatabaseErrorHandler.logDatabaseOperation("INSERT", "health_diary", true, 
                        "健康日记添加成功，ID: " + diaryId);
                    callback.onSuccess(diaryId);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("INSERT", "health_diary", false, 
                        result.getErrorMessage());
                    callback.onError(result.getErrorMessage());
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "添加健康日记");
                DatabaseErrorHandler.logDatabaseOperation("INSERT", "health_diary", false, 
                    error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 更新健康日记
     * @param diary 健康日记实体
     * @param callback 操作结果回调
     */
    public void updateDiary(HealthDiary diary, RepositoryCallback<Boolean> callback) {
        executeTask(() -> {
            try {
                // 验证输入参数
                if (diary == null) {
                    callback.onError(context.getString(R.string.error_diary_object_null));
                    return;
                }
                
                if (diary.getId() <= 0) {
                    callback.onError(context.getString(R.string.error_diary_id_invalid));
                    return;
                }
                
                if (diary.getContent() == null || diary.getContent().trim().isEmpty()) {
                    callback.onError(context.getString(R.string.error_diary_content_empty));
                    return;
                }
                
                if (diary.getContent().length() > 5000) {
                    callback.onError(context.getString(R.string.error_diary_content_too_long));
                    return;
                }
                
                // 验证用户身份
                long currentUserId = getCurrentLoggedInUserId();
                if (currentUserId <= 0) {
                    callback.onError(context.getString(R.string.error_user_not_logged_in));
                    return;
                }
                
                // 检查日记是否存在
                HealthDiary existingDiary = healthDiaryDao.getDiaryByIdSync(diary.getId());
                if (existingDiary == null) {
                    callback.onError(context.getString(R.string.error_diary_not_exists));
                    return;
                }
                
                // 验证用户权限
                if (existingDiary.getUserId() != currentUserId) {
                    callback.onError(context.getString(R.string.error_no_permission_modify_diary));
                    return;
                }
                
                // 保持原始创建时间，更新修改时间
                diary.setUserId(currentUserId);
                diary.setCreatedAt(existingDiary.getCreatedAt());
                diary.setUpdatedAt(System.currentTimeMillis());
                
                // 更新日记
                int result = healthDiaryDao.updateDiary(diary);
                DatabaseErrorHandler.DatabaseResult<Integer> updateResult = 
                    DatabaseErrorHandler.validateUpdateResult(result);
                
                if (updateResult.isSuccess()) {
                    DatabaseErrorHandler.logDatabaseOperation("UPDATE", "health_diary", true, 
                        "健康日记更新成功，ID: " + diary.getId());
                    callback.onSuccess(true);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("UPDATE", "health_diary", false, 
                        updateResult.getErrorMessage());
                    callback.onError(updateResult.getErrorMessage());
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "更新健康日记");
                DatabaseErrorHandler.logDatabaseOperation("UPDATE", "health_diary", false, 
                    error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 删除健康日记
     * @param diary 健康日记实体
     * @param callback 操作结果回调
     */
    public void deleteDiary(HealthDiary diary, RepositoryCallback<Boolean> callback) {
        executeTask(() -> {
            try {
                // 验证输入参数
                if (diary == null) {
                    callback.onError(context.getString(R.string.error_diary_object_null));
                    return;
                }
                
                // 验证用户身份
                long currentUserId = getCurrentLoggedInUserId();
                if (currentUserId <= 0) {
                    callback.onError(context.getString(R.string.error_user_not_logged_in));
                    return;
                }
                
                // 验证用户权限
                if (diary.getUserId() != currentUserId) {
                    callback.onError(context.getString(R.string.error_no_permission_delete_diary));
                    return;
                }
                
                // 删除日记
                int result = healthDiaryDao.deleteDiary(diary);
                DatabaseErrorHandler.DatabaseResult<Integer> deleteResult = 
                    DatabaseErrorHandler.validateDeleteResult(result);
                
                if (deleteResult.isSuccess()) {
                    DatabaseErrorHandler.logDatabaseOperation("DELETE", "health_diary", true, 
                        "健康日记删除成功，ID: " + diary.getId());
                    callback.onSuccess(true);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("DELETE", "health_diary", false, 
                        deleteResult.getErrorMessage());
                    callback.onError(deleteResult.getErrorMessage());
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "删除健康日记");
                DatabaseErrorHandler.logDatabaseOperation("DELETE", "health_diary", false, 
                    error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 根据ID获取健康日记
     * @param diaryId 日记ID
     * @param callback 查询结果回调
     */
    public void getDiaryById(long diaryId, RepositoryCallback<HealthDiary> callback) {
        executeTask(() -> {
            try {
                // 验证用户身份
                long currentUserId = getCurrentLoggedInUserId();
                if (currentUserId <= 0) {
                    callback.onError(context.getString(R.string.error_user_not_logged_in));
                    return;
                }
                
                // 获取日记
                HealthDiary diary = healthDiaryDao.getDiaryByIdSync(diaryId);
                if (diary == null) {
                    callback.onError(context.getString(R.string.error_diary_not_exists));
                    return;
                }
                
                // 验证用户权限
                if (diary.getUserId() != currentUserId) {
                    callback.onError(context.getString(R.string.error_no_permission_access_diary));
                    return;
                }
                
                callback.onSuccess(diary);
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "获取健康日记");
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 获取当前用户的所有健康日记（LiveData）
     * @return 当前用户健康日记列表的LiveData
     */
    public LiveData<List<HealthDiary>> getUserDiaries() {
        // 获取当前登录用户ID
        long currentUserId = getCurrentLoggedInUserId();
        Log.d(TAG, "获取用户日记，当前用户ID: " + currentUserId);
        
        if (currentUserId > 0) {
            LiveData<List<HealthDiary>> diariesLiveData = healthDiaryDao.getDiariesByUserId(currentUserId);
            Log.d(TAG, "返回用户ID " + currentUserId + " 的日记LiveData");
            return diariesLiveData;
        } else {
            // 如果没有登录用户，返回空的LiveData
            MutableLiveData<List<HealthDiary>> emptyLiveData = new MutableLiveData<>();
            emptyLiveData.setValue(new java.util.ArrayList<>());
            Log.w(TAG, "没有登录用户，返回空的日记列表");
            return emptyLiveData;
        }
    }
    
    /**
     * 获取当前登录用户的ID
     * 直接从数据库查询当前登录用户
     * @return 当前登录用户ID，如果没有登录用户则返回0
     */
    private long getCurrentLoggedInUserId() {
        try {
            // 直接查询数据库获取当前登录用户
            User loggedInUser = userDao.getCurrentLoggedInUser();
            if (loggedInUser != null) {
                Log.d(TAG, "从数据库获取到登录用户: " + loggedInUser.getUsername() + " (ID: " + loggedInUser.getId() + ")");
                return loggedInUser.getId();
            }
            
            Log.w(TAG, "没有找到登录用户");
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "获取当前用户ID失败", e);
            return 0;
        }
    }
    
    /**
     * 异步获取当前用户的所有健康日记
     * @param callback 查询结果回调
     */
    public void getUserDiariesAsync(RepositoryCallback<List<HealthDiary>> callback) {
        executeTask(() -> {
            try {
                // 验证用户身份
                long currentUserId = getCurrentLoggedInUserId();
                if (currentUserId <= 0) {
                    callback.onError("用户未登录");
                    return;
                }
                
                // 获取用户日记列表
                List<HealthDiary> diaries = healthDiaryDao.getDiariesByUserIdSync(currentUserId);
                callback.onSuccess(diaries);
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "获取用户健康日记列表");
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 获取当前用户的健康日记数量
     * @return 日记数量的LiveData
     */
    public LiveData<Integer> getUserDiaryCount() {
        // 获取当前登录用户ID
        long currentUserId = getCurrentLoggedInUserId();
        Log.d(TAG, "获取用户日记数量，当前用户ID: " + currentUserId);
        
        if (currentUserId > 0) {
            return healthDiaryDao.getDiaryCountByUserId(currentUserId);
        } else {
            // 如果没有登录用户，返回0
            MutableLiveData<Integer> emptyLiveData = new MutableLiveData<>();
            emptyLiveData.setValue(0);
            return emptyLiveData;
        }
    }
    
    /**
     * 异步获取当前用户的健康日记数量
     * @param callback 查询结果回调
     */
    public void getUserDiaryCountAsync(RepositoryCallback<Integer> callback) {
        executeTask(() -> {
            try {
                // 验证用户身份
                long currentUserId = getCurrentLoggedInUserId();
                if (currentUserId <= 0) {
                    callback.onError("用户未登录");
                    return;
                }
                
                // 获取日记数量
                int count = healthDiaryDao.getDiaryCountByUserIdSync(currentUserId);
                callback.onSuccess(count);
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "获取用户健康日记数量");
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 根据内容搜索健康日记
     * @param searchQuery 搜索关键词
     * @param callback 搜索结果回调
     */
    public void searchDiaries(String searchQuery, RepositoryCallback<List<HealthDiary>> callback) {
        executeTask(() -> {
            try {
                // 验证用户身份
                long currentUserId = getCurrentLoggedInUserId();
                if (currentUserId <= 0) {
                    callback.onError(context.getString(R.string.error_user_not_logged_in));
                    return;
                }
                
                // 验证搜索关键词
                if (searchQuery == null || searchQuery.trim().isEmpty()) {
                    callback.onError(context.getString(R.string.error_search_keyword_empty));
                    return;
                }
                
                // 搜索日记
                String searchPattern = "%" + searchQuery.trim() + "%";
                List<HealthDiary> diaries = healthDiaryDao.searchDiariesByContent(currentUserId, searchPattern);
                callback.onSuccess(diaries);
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "搜索健康日记");
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 获取指定日期范围内的健康日记
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @param callback 查询结果回调
     */
    public void getDiariesByDateRange(long startTime, long endTime, RepositoryCallback<List<HealthDiary>> callback) {
        executeTask(() -> {
            try {
                // 验证用户身份
                long currentUserId = getCurrentLoggedInUserId();
                if (currentUserId <= 0) {
                    callback.onError(context.getString(R.string.error_user_not_logged_in));
                    return;
                }
                
                // 验证时间范围
                if (startTime > endTime) {
                    callback.onError(context.getString(R.string.error_invalid_time_range));
                    return;
                }
                
                // 获取指定时间范围的日记
                List<HealthDiary> diaries = healthDiaryDao.getDiariesByDateRange(currentUserId, startTime, endTime);
                callback.onSuccess(diaries);
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "获取指定日期范围的健康日记");
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
        Log.d(TAG, "HealthDiaryRepository 资源清理完成");
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
     * 检查用户是否已登录
     * @return 如果用户已登录返回true，否则返回false
     */
    public boolean isUserLoggedIn() {
        return getCurrentLoggedInUserId() > 0;
    }
    
    /**
     * 获取当前登录用户信息
     * @param callback 回调接口
     */
    public void getCurrentUser(RepositoryCallback<User> callback) {
        executeTask(() -> {
            try {
                long currentUserId = getCurrentLoggedInUserId();
                if (currentUserId > 0) {
                    User user = userDao.getUserById(currentUserId);
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("用户信息不存在");
                    }
                } else {
                    callback.onError("用户未登录");
                }
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = 
                    DatabaseErrorHandler.handleException(e, "获取当前用户信息");
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 验证健康日记内容
     * @param content 日记内容
     * @return 验证结果消息，null表示验证通过
     */
    public static String validateDiaryContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "日记内容不能为空";
        }
        
        if (content.length() > 5000) {
            return "日记内容不能超过5000字符";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 获取Repository实例状态信息
     * @return 状态信息字符串
     */
    public String getRepositoryStatus() {
        return String.format("HealthDiaryRepository状态: 数据库=%s, 线程池=%s", 
            (healthDiaryDao != null ? "已连接" : "未连接"),
            (executorService != null && !executorService.isShutdown() ? "运行中" : "已停止"));
    }
}