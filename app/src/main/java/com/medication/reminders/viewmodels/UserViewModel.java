package com.medication.reminders.viewmodels;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.medication.reminders.database.entity.User;
import com.medication.reminders.models.RepositoryCallback;
import com.medication.reminders.models.ProfileValidationResult;
import com.medication.reminders.models.UserError;
import com.medication.reminders.repository.UserRepository;
import com.medication.reminders.utils.UserValidator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UserViewModel类 - 统一的用户管理ViewModel
 * 整合用户注册、登录、个人资料管理等所有用户相关的业务逻辑
 * 使用MVVM架构模式，通过Repository模式提供数据访问
 */
public class UserViewModel extends AndroidViewModel {
    
    private static final String TAG = "UserViewModel";
    
    // Repository依赖
    private UserRepository userRepository;
    
    // 线程管理
    private Handler mainHandler;
    private ExecutorService executorService;
    private boolean isTestMode = false;
    
    // ========== LiveData属性 - UI状态管理 ==========
    
    // 当前用户相关
    private LiveData<User> currentUser;
    private LiveData<Long> currentUserId;
    
    // 登录相关状态
    private MutableLiveData<String> loginStatus;
    private MutableLiveData<Boolean> loginSuccess;
    
    // 注册相关状态
    private MutableLiveData<String> registrationStatus;
    private MutableLiveData<Boolean> registrationSuccess;
    
    // 个人资料更新状态
    private MutableLiveData<String> profileUpdateStatus;
    private MutableLiveData<Boolean> profileUpdateSuccess;
    
    // 通用状态
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<String> successMessage;
    
    // 错误状态管理 - 使用UserError枚举
    private MutableLiveData<UserError> currentError;
    private MutableLiveData<String> errorDetails;
    
    // 表单验证
    private MutableLiveData<String> formValidationError;
    
    // ========== 记住用户相关字段 ==========
    private MutableLiveData<LoginInfo> savedLoginInfo = new MutableLiveData<>();

    /**
     * 构造函数 - 初始化UserRepository和LiveData属性
     * @param application 应用程序实例
     */
    public UserViewModel(@NonNull Application application) {
        super(application);
        initializeViewModel(application);
    }
    
    /**
     * 测试构造函数 - 支持同步行为
     * @param application 应用程序实例
     * @param isTestMode 是否为测试模式
     */
    public UserViewModel(@NonNull Application application, boolean isTestMode) {
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
        this.userRepository = UserRepository.getInstance(application);
        
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
        // 登录相关
        this.loginStatus = new MutableLiveData<>();
        this.loginSuccess = new MutableLiveData<>(false);
        
        // 注册相关
        this.registrationStatus = new MutableLiveData<>();
        this.registrationSuccess = new MutableLiveData<>(false);
        
        // 个人资料更新
        this.profileUpdateStatus = new MutableLiveData<>();
        this.profileUpdateSuccess = new MutableLiveData<>(false);
        
        // 通用状态
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
        this.successMessage = new MutableLiveData<>();
        
        // 错误状态管理
        this.currentError = new MutableLiveData<>();
        this.errorDetails = new MutableLiveData<>();
        
        // 表单验证
        this.formValidationError = new MutableLiveData<>();
    }
    
    /**
     * 设置与UserRepository的连接
     * 获取当前用户和用户ID的LiveData引用
     */
    private void setupRepositoryConnection() {
        // 获取当前用户ID的LiveData
        this.currentUserId = userRepository.getCurrentUserId();
        
        // 获取当前用户的LiveData
        this.currentUser = userRepository.getCurrentUser();
    }
    
    // ========== LiveData Getter方法 ==========
    
    /**
     * 获取当前用户LiveData
     * @return 当前用户的LiveData对象
     */
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
    
    /**
     * 获取当前用户ID LiveData
     * @return 当前用户ID的LiveData对象
     */
    public LiveData<Long> getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * 获取登录状态LiveData
     * @return 登录状态消息的LiveData
     */
    public LiveData<String> getLoginStatus() {
        return loginStatus;
    }
    
    /**
     * 获取登录成功状态LiveData
     * @return 登录成功状态的LiveData
     */
    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }
    
    /**
     * 获取注册状态LiveData
     * @return 注册状态消息的LiveData
     */
    public LiveData<String> getRegistrationStatus() {
        return registrationStatus;
    }
    
    /**
     * 获取注册成功状态LiveData
     * @return 注册成功状态的LiveData
     */
    public LiveData<Boolean> getRegistrationSuccess() {
        return registrationSuccess;
    }
    
    /**
     * 获取个人资料更新状态LiveData
     * @return 个人资料更新状态消息的LiveData
     */
    public LiveData<String> getProfileUpdateStatus() {
        return profileUpdateStatus;
    }
    
    /**
     * 获取个人资料更新成功状态LiveData
     * @return 个人资料更新成功状态的LiveData
     */
    public LiveData<Boolean> getProfileUpdateSuccess() {
        return profileUpdateSuccess;
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
     * 获取当前错误LiveData
     * @return 当前错误的LiveData
     */
    public LiveData<UserError> getCurrentError() {
        return currentError;
    }
    
    /**
     * 获取错误详情LiveData
     * @return 错误详情的LiveData
     */
    public LiveData<String> getErrorDetails() {
        return errorDetails;
    }
    
    /**
     * 获取表单验证错误LiveData
     * @return 表单验证错误的LiveData
     */
    public LiveData<String> getFormValidationError() {
        return formValidationError;
    }
    
    /**
     * 获取保存的登录信息
     * @return 保存的登录信息的LiveData
     */
    public LiveData<LoginInfo> getSavedLoginInfo() {
        if (savedLoginInfo.getValue() == null) {
            loadSavedLoginInfo();
        }
        return savedLoginInfo;
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
     * 清除表单验证错误
     */
    public void clearFormValidationError() {
        this.formValidationError.setValue(null);
    }
    
    /**
     * 清除当前错误状态
     */
    public void clearCurrentError() {
        this.currentError.setValue(null);
        this.errorDetails.setValue(null);
    }
    
    /**
     * 清除所有错误状态
     */
    public void clearAllErrors() {
        clearErrorMessage();
        clearCurrentError();
        clearFormValidationError();
    }
    
    /**
     * 重置登录状态
     */
    public void resetLoginStatus() {
        this.loginStatus.setValue(null);
        this.loginSuccess.setValue(false);
    }
    
    /**
     * 重置注册状态
     */
    public void resetRegistrationStatus() {
        this.registrationStatus.setValue(null);
        this.registrationSuccess.setValue(false);
    }
    
    /**
     * 重置个人资料更新状态
     */
    public void resetProfileUpdateStatus() {
        this.profileUpdateStatus.setValue(null);
        this.profileUpdateSuccess.setValue(false);
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
            setLoading(false);
        });
    }
    
    /**
     * 设置成功消息
     * @param message 成功消息
     */
    private void setSuccess(String message) {
        postToMainThread(() -> {
            successMessage.setValue(message);
            setLoading(false);
        });
    }
    
    /**
     * 设置用户错误状态
     * @param userError 用户错误枚举
     */
    private void setUserError(UserError userError) {
        postToMainThread(() -> {
            currentError.setValue(userError);
            errorMessage.setValue(userError.getMessage());
            setLoading(false);
        });
    }
    
    /**
     * 设置用户错误状态（带详细信息）
     * @param userError 用户错误枚举
     * @param details 错误详细信息
     */
    private void setUserError(UserError userError, String details) {
        postToMainThread(() -> {
            currentError.setValue(userError);
            errorMessage.setValue(userError.getMessage());
            errorDetails.setValue(details);
            setLoading(false);
        });
    }
    
    /**
     * 根据错误消息推断并设置用户错误
     * @param errorMessage 错误消息
     */
    private void setErrorFromMessage(String errorMessage) {
        UserError userError = inferUserErrorFromMessage(errorMessage);
        setUserError(userError, errorMessage);
    }
    
    /**
     * 根据错误消息推断UserError类型
     * @param errorMessage 错误消息
     * @return 推断的UserError类型
     */
    private UserError inferUserErrorFromMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return UserError.UNKNOWN_ERROR;
        }
        
        String message = errorMessage.toLowerCase().trim();
        
        // 用户名相关错误
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_username)) && message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_username_exists))) {
            return UserError.USERNAME_EXISTS;
        }
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_username)) && message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_username_format))) {
            return UserError.INVALID_USERNAME;
        }
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_user_not_exists))) {
            return UserError.USER_NOT_FOUND;
        }
        
        // 密码相关错误
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_password)) && message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_password_wrong))) {
            return UserError.WRONG_PASSWORD;
        }
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_password)) && message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_password_strength))) {
            return UserError.WEAK_PASSWORD;
        }
        
        // 邮箱相关错误
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_email)) && message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_email_format))) {
            return UserError.INVALID_EMAIL;
        }
        
        // 电话相关错误
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_phone)) && message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_phone_format))) {
            return UserError.INVALID_PHONE;
        }
        
        // 登录相关错误
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_login)) && message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_login_failed))) {
            return UserError.LOGIN_FAILED;
        }
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_attempts))) {
            return UserError.TOO_MANY_ATTEMPTS;
        }
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_account)) && message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_account_locked))) {
            return UserError.ACCOUNT_LOCKED;
        }
        
        // 数据库相关错误
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_database))) {
            return UserError.DATABASE_ERROR;
        }
        
        // 网络相关错误
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_network))) {
            return UserError.NETWORK_ERROR;
        }
        
        // 会话相关错误
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_session)) && message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_session_expired))) {
            return UserError.SESSION_EXPIRED;
        }
        
        // 个人资料相关错误
        if (message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_profile)) && message.contains(getApplication().getString(com.medication.reminders.R.string.user_error_profile_update))) {
            return UserError.PROFILE_UPDATE_FAILED;
        }
        
        return UserError.UNKNOWN_ERROR;
    }
    
    // ========== 生命周期管理 ==========
    
    /**
     * 清理资源以防止内存泄漏
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        
        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        // 清理Handler回调
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
    
    // ========== 会话管理方法 ==========
    
    /**
     * 检查被记住的用户
     * 在应用启动时调用，检查是否有被记住的用户需要自动登录
     */
    public void checkRememberedUser() {
        if (isTestMode) {
            performCheckRememberedUserSync();
        } else {
            performCheckRememberedUserAsync();
        }
    }
    
    /**
     * 同步检查被记住的用户（测试用）
     */
    private void performCheckRememberedUserSync() {
        userRepository.checkRememberedUser(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                loginSuccess.setValue(true);
                loginStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_auto_login_success));
            }
            
            @Override
            public void onError(String error) {
                // 没有被记住的用户是正常情况，不需要显示错误
                loginSuccess.setValue(false);
            }
        });
    }
    
    /**
     * 异步检查被记住的用户（生产环境）
     */
    private void performCheckRememberedUserAsync() {
        executorService.execute(() -> {
            userRepository.checkRememberedUser(new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    postToMainThread(() -> {
                        loginSuccess.setValue(true);
                        loginStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_auto_login_success));
                    });
                }
                
                @Override
                public void onError(String error) {
                    // 没有被记住的用户是正常情况，不需要显示错误
                    postToMainThread(() -> {
                        loginSuccess.setValue(false);
                    });
                }
            });
        });
    }
    
    /**
     * 用户登出
     * 清除用户会话信息并更新UI状态
     */
    public void logoutUser() {
        setLoading(true);
        clearErrorMessage();
        clearSuccessMessage();
        
        if (isTestMode) {
            performLogoutSync();
        } else {
            performLogoutAsync();
        }
    }
    
    /**
     * 同步登出（测试用）
     */
    private void performLogoutSync() {
        userRepository.logoutUser(new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // 重置所有状态
                resetLoginStatus();
                resetRegistrationStatus();
                resetProfileUpdateStatus();
                setSuccess(getApplication().getString(com.medication.reminders.R.string.user_logout_success));
            }
            
            @Override
            public void onError(String error) {
                setError(getApplication().getString(com.medication.reminders.R.string.user_logout_failed, error));
            }
        });
    }
    
    /**
     * 异步登出（生产环境）
     */
    private void performLogoutAsync() {
        executorService.execute(() -> {
            userRepository.logoutUser(new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    postToMainThread(() -> {
                        // 重置所有状态
                        resetLoginStatus();
                        resetRegistrationStatus();
                        resetProfileUpdateStatus();
                        setSuccess(getApplication().getString(com.medication.reminders.R.string.user_logout_success));
                    });
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> {
                        setError(getApplication().getString(com.medication.reminders.R.string.user_logout_failed, error));
                    });
                }
            });
        });
    }
    
    // ========== 用户注册相关方法 ==========
    
    /**
     * 用户注册方法
     * 处理用户注册逻辑，包括输入验证、用户名唯一性检查和注册状态管理
     * @param username 用户名
     * @param email 邮箱地址
     * @param phone 电话号码
     * @param password 密码
     */
    public void registerUser(String username, String email, String phone, String password) {
        // 设置加载状态
        setLoading(true);
        
        // 重置之前的状态
        resetRegistrationStatus();
        clearErrorMessage();
        clearSuccessMessage();
        clearFormValidationError();
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performRegistrationSync(username, email, phone, password);
        } else {
            performRegistrationAsync(username, email, phone, password);
        }
    }
    
    /**
     * 同步执行注册（测试用）
     * @param username 用户名
     * @param email 邮箱地址
     * @param phone 电话号码
     * @param password 密码
     */
    private void performRegistrationSync(String username, String email, String phone, String password) {
        try {
            // 首先进行表单验证
            String validationError = validateRegistrationForm(username, email, phone, password);
            if (validationError != null) {
                formValidationError.setValue(validationError);
                registrationStatus.setValue(validationError);
                registrationSuccess.setValue(false);
                setLoading(false);
                return;
            }
            
            // 执行注册
            userRepository.registerUser(username, email, phone, password, new RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long userId) {
                    registrationSuccess.setValue(true);
                    registrationStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_registration_success));
                    setSuccess(getApplication().getString(com.medication.reminders.R.string.user_registration_success_with_id, userId));
                }
                
                @Override
                public void onError(String error) {
                    registrationSuccess.setValue(false);
                    registrationStatus.setValue(error);
                    setError(error);
                }
            });
            
        } catch (Exception e) {
            registrationSuccess.setValue(false);
            registrationStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_registration_error));
            setError(getApplication().getString(com.medication.reminders.R.string.user_registration_error_retry));
        }
    }
    
    /**
     * 异步执行注册（生产环境）
     * @param username 用户名
     * @param email 邮箱地址
     * @param phone 电话号码
     * @param password 密码
     */
    private void performRegistrationAsync(String username, String email, String phone, String password) {
        executorService.execute(() -> {
            try {
                // 首先进行表单验证
                String validationError = validateRegistrationForm(username, email, phone, password);
                if (validationError != null) {
                    postToMainThread(() -> {
                        formValidationError.setValue(validationError);
                        registrationStatus.setValue(validationError);
                        registrationSuccess.setValue(false);
                        setLoading(false);
                    });
                    return;
                }
                
                // 执行注册
                userRepository.registerUser(username, email, phone, password, new RepositoryCallback<Long>() {
                    @Override
                    public void onSuccess(Long userId) {
                        postToMainThread(() -> {
                            registrationSuccess.setValue(true);
                            registrationStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_registration_success));
                            setSuccess(getApplication().getString(com.medication.reminders.R.string.user_registration_success_with_id, userId));
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        postToMainThread(() -> {
                            registrationSuccess.setValue(false);
                            registrationStatus.setValue(error);
                            setError(error);
                        });
                    }
                });
                
            } catch (Exception e) {
                postToMainThread(() -> {
                    registrationSuccess.setValue(false);
                    registrationStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_registration_error));
                    setError(getApplication().getString(com.medication.reminders.R.string.user_registration_error_retry));
                });
            }
        });
    }
    
    /**
     * 检查用户名是否存在
     * 用于实时验证用户名唯一性
     * @param username 要检查的用户名
     * @param callback 检查结果回调
     */
    public void checkUsernameExists(String username, RepositoryCallback<Boolean> callback) {
        if (username == null || username.trim().isEmpty()) {
            callback.onError(getApplication().getString(com.medication.reminders.R.string.user_username_empty));
            return;
        }
        
        if (isTestMode) {
            performUsernameCheckSync(username, callback);
        } else {
            performUsernameCheckAsync(username, callback);
        }
    }
    
    /**
     * 同步检查用户名存在性（测试用）
     * @param username 用户名
     * @param callback 回调接口
     */
    private void performUsernameCheckSync(String username, RepositoryCallback<Boolean> callback) {
        userRepository.checkUsernameExists(username.trim(), callback);
    }
    
    /**
     * 异步检查用户名存在性（生产环境）
     * @param username 用户名
     * @param callback 回调接口
     */
    private void performUsernameCheckAsync(String username, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            userRepository.checkUsernameExists(username.trim(), new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean exists) {
                    postToMainThread(() -> callback.onSuccess(exists));
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> callback.onError(error));
                }
            });
        });
    }
    
    // ========== 表单验证方法 ==========
    
    /**
     * 验证注册表单
     * 对注册表单进行完整的输入验证
     * @param username 用户名
     * @param email 邮箱地址
     * @param phone 电话号码
     * @param password 密码
     * @return 验证错误消息，如果验证通过则返回null
     */
    public String validateRegistrationForm(String username, String email, String phone, String password) {
        // 使用统一的UserValidator进行验证
        com.medication.reminders.models.ProfileValidationResult result = 
            com.medication.reminders.utils.UserValidator.validateRegistrationForm(username, email, phone, password);
        
        if (!result.isValid()) {
            return result.getErrorMessage();
        }
        
        return null; // 验证通过
    }
    
    /**
     * 验证个人资料表单
     * 对个人资料表单进行完整的输入验证
     * @param fullName 完整姓名
     * @param gender 性别
     * @param birthDate 出生日期
     * @return 验证错误消息，如果验证通过则返回null
     */
    public String validateProfileForm(String fullName, String gender, String birthDate) {
        // 使用统一的UserValidator进行验证
        com.medication.reminders.models.ProfileValidationResult result = 
            com.medication.reminders.utils.UserValidator.validateProfileForm(fullName, gender, birthDate);
        
        if (!result.isValid()) {
            return result.getErrorMessage();
        }
        
        return null; // 验证通过
    }
    
    // ========== 用户登录相关方法 ==========
    
    /**
     * 用户登录方法
     * 处理用户登录逻辑，包括密码验证、登录尝试管理和"记住我"功能
     * @param username 用户名
     * @param password 密码
     * @param rememberMe 是否记住我
     */
    public void loginUser(String username, String password, boolean rememberMe) {
        // 设置加载状态
        setLoading(true);
        
        // 重置之前的状态
        resetLoginStatus();
        clearErrorMessage();
        clearSuccessMessage();
        clearFormValidationError();
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performLoginSync(username, password, rememberMe);
        } else {
            performLoginAsync(username, password, rememberMe);
        }
    }
    
    /**
     * 同步执行登录（测试用）
     * @param username 用户名
     * @param password 密码
     * @param rememberMe 是否记住我
     */
    private void performLoginSync(String username, String password, boolean rememberMe) {
        try {
            // 首先进行登录表单验证
            String validationError = validateLoginForm(username, password);
            if (validationError != null) {
                formValidationError.setValue(validationError);
                loginStatus.setValue(validationError);
                loginSuccess.setValue(false);
                setLoading(false);
                return;
            }
            
            // 执行登录
            userRepository.loginUser(username, password, rememberMe, new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    loginSuccess.setValue(true);
                    loginStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_login_success));
                    setSuccess(getApplication().getString(com.medication.reminders.R.string.user_login_welcome, user.getUsername()));
                }
                
                @Override
                public void onError(String error) {
                    loginSuccess.setValue(false);
                    loginStatus.setValue(error);
                    setError(error);
                }
            });
            
        } catch (Exception e) {
            loginSuccess.setValue(false);
            loginStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_login_error));
            setError(getApplication().getString(com.medication.reminders.R.string.user_login_error_retry));
        }
    }
    
    /**
     * 异步执行登录（生产环境）
     * @param username 用户名
     * @param password 密码
     * @param rememberMe 是否记住我
     */
    private void performLoginAsync(String username, String password, boolean rememberMe) {
        executorService.execute(() -> {
            try {
                // 首先进行登录表单验证
                String validationError = validateLoginForm(username, password);
                if (validationError != null) {
                    postToMainThread(() -> {
                        formValidationError.setValue(validationError);
                        loginStatus.setValue(validationError);
                        loginSuccess.setValue(false);
                        setLoading(false);
                    });
                    return;
                }
                
                // 执行登录
                userRepository.loginUser(username, password, rememberMe, new RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        postToMainThread(() -> {
                            loginSuccess.setValue(true);
                            loginStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_login_success));
                            setSuccess(getApplication().getString(com.medication.reminders.R.string.user_login_welcome, user.getUsername()));
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        postToMainThread(() -> {
                            loginSuccess.setValue(false);
                            loginStatus.setValue(error);
                            setError(error);
                        });
                    }
                });
                
            } catch (Exception e) {
                postToMainThread(() -> {
                    loginSuccess.setValue(false);
                    loginStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_login_error));
                    setError(getApplication().getString(com.medication.reminders.R.string.user_login_error_retry));
                });
            }
        });
    }
    
    /**
     * 验证登录表单
     * 对登录表单进行输入验证
     * @param username 用户名
     * @param password 密码
     * @return 验证错误消息，如果验证通过则返回null
     */
    public String validateLoginForm(String username, String password) {
        // 检查空值
        if (username == null && password == null) {
            return getApplication().getString(com.medication.reminders.R.string.user_login_validation_both_empty);
        }
        
        if (username == null || username.trim().isEmpty()) {
            return password == null || password.trim().isEmpty() ? getApplication().getString(com.medication.reminders.R.string.user_login_validation_both_empty) : getApplication().getString(com.medication.reminders.R.string.user_login_validation_username_empty);
        }
        
        if (password == null || password.trim().isEmpty()) {
            return getApplication().getString(com.medication.reminders.R.string.user_login_validation_password_empty);
        }
        
        // 使用统一的UserValidator进行基本格式验证
        ProfileValidationResult usernameResult = UserValidator.validateUsername(username);
        if (!usernameResult.isValid()) {
            return usernameResult.getErrorMessage();
        }
        
        ProfileValidationResult passwordResult = UserValidator.validatePassword(password);
        if (!passwordResult.isValid()) {
            return passwordResult.getErrorMessage();
        }
        
        return null; // 验证通过
    }
    
    // ========== 密码管理方法 ==========
    
    /**
     * 修改密码
     * 允许用户修改当前密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        // 设置加载状态
        setLoading(true);
        clearErrorMessage();
        clearSuccessMessage();
        
        // 验证输入
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            setError(getApplication().getString(com.medication.reminders.R.string.user_password_current_empty));
            return;
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            setError(getApplication().getString(com.medication.reminders.R.string.user_password_new_empty));
            return;
        }
        
        if (newPassword.trim().length() < 6) {
            setError(getApplication().getString(com.medication.reminders.R.string.user_password_new_too_short));
            return;
        }
        
        if (newPassword.trim().length() > 20) {
            setError(getApplication().getString(com.medication.reminders.R.string.user_password_new_too_long));
            return;
        }
        
        if (oldPassword.equals(newPassword)) {
            setError(getApplication().getString(com.medication.reminders.R.string.user_password_same_as_old));
            return;
        }
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performChangePasswordSync(oldPassword, newPassword);
        } else {
            performChangePasswordAsync(oldPassword, newPassword);
        }
    }
    
    /**
     * 同步修改密码（测试用）
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    private void performChangePasswordSync(String oldPassword, String newPassword) {
        userRepository.changePassword(oldPassword, newPassword, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                setSuccess(getApplication().getString(com.medication.reminders.R.string.user_password_change_success));
            }
            
            @Override
            public void onError(String error) {
                setError(getApplication().getString(com.medication.reminders.R.string.user_password_change_failed, error));
            }
        });
    }
    
    /**
     * 异步修改密码（生产环境）
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    private void performChangePasswordAsync(String oldPassword, String newPassword) {
        executorService.execute(() -> {
            userRepository.changePassword(oldPassword, newPassword, new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    postToMainThread(() -> {
                        setSuccess(getApplication().getString(com.medication.reminders.R.string.user_password_change_success));
                    });
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> {
                        setError(getApplication().getString(com.medication.reminders.R.string.user_password_change_failed, error));
                    });
                }
            });
        });
    }
    
    // ========== 用户资料管理方法 ==========
    
    /**
     * 更新用户资料
     * 更新当前用户的完整资料信息
     * @param user 包含更新信息的User对象
     */
    public void updateUserProfile(User user) {
        if (user == null) {
            setError(getApplication().getString(com.medication.reminders.R.string.user_profile_info_empty));
            return;
        }
        
        // 设置加载状态
        setLoading(true);
        
        // 重置之前的状态
        resetProfileUpdateStatus();
        clearErrorMessage();
        clearSuccessMessage();
        
        // 根据测试模式选择执行方式
        if (isTestMode) {
            performUpdateProfileSync(user);
        } else {
            performUpdateProfileAsync(user);
        }
    }
    
    /**
     * 同步更新用户资料（测试用）
     * @param user 用户对象
     */
    private void performUpdateProfileSync(User user) {
        userRepository.updateUserProfile(user, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                profileUpdateSuccess.setValue(true);
                profileUpdateStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_profile_update_success));
                setSuccess(getApplication().getString(com.medication.reminders.R.string.user_profile_update_success));
            }
            
            @Override
            public void onError(String error) {
                profileUpdateSuccess.setValue(false);
                profileUpdateStatus.setValue(error);
                setError(getApplication().getString(com.medication.reminders.R.string.user_profile_update_failed, error));
            }
        });
    }
    
    /**
     * 异步更新用户资料（生产环境）
     * @param user 用户对象
     */
    private void performUpdateProfileAsync(User user) {
        executorService.execute(() -> {
            userRepository.updateUserProfile(user, new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    postToMainThread(() -> {
                        profileUpdateSuccess.setValue(true);
                        profileUpdateStatus.setValue(getApplication().getString(com.medication.reminders.R.string.user_profile_update_success));
                        setSuccess(getApplication().getString(com.medication.reminders.R.string.user_profile_update_success));
                    });
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> {
                        profileUpdateSuccess.setValue(false);
                        profileUpdateStatus.setValue(error);
                        setError(getApplication().getString(com.medication.reminders.R.string.user_profile_update_failed, error));
                    });
                }
            });
        });
    }
    
    /**
     * 更新基本个人资料
     * 更新用户的基本个人信息
     * @param fullName 完整姓名
     * @param gender 性别
     * @param birthDate 出生日期
     */
    public void updateBasicProfile(String fullName, String gender, String birthDate) {
        // 获取当前用户
        getCurrentUserForUpdate(currentUser -> {
            if (currentUser != null) {
                // 更新基本信息
                currentUser.setFullName(fullName);
                currentUser.setGender(gender);
                currentUser.setBirthDate(birthDate);
                currentUser.setUpdatedAt(System.currentTimeMillis());
                
                // 执行更新
                updateUserProfile(currentUser);
            } else {
                setError(getApplication().getString(com.medication.reminders.R.string.error_user_not_found));
            }
        });
    }
    
    /**
     * 更新联系信息
     * 更新用户的联系方式信息
     * @param secondaryPhone 备用电话号码
     * @param emergencyContactName 紧急联系人姓名
     * @param emergencyContactPhone 紧急联系人电话
     * @param emergencyContactRelation 与紧急联系人关系
     */
    public void updateContactInfo(String secondaryPhone, String emergencyContactName, 
                                 String emergencyContactPhone, String emergencyContactRelation) {
        // 获取当前用户
        getCurrentUserForUpdate(currentUser -> {
            if (currentUser != null) {
                // 更新联系信息
                currentUser.setSecondaryPhone(secondaryPhone);
                currentUser.setEmergencyContactName(emergencyContactName);
                currentUser.setEmergencyContactPhone(emergencyContactPhone);
                currentUser.setEmergencyContactRelation(emergencyContactRelation);
                currentUser.setUpdatedAt(System.currentTimeMillis());
                
                // 执行更新
                updateUserProfile(currentUser);
            } else {
                setError(getApplication().getString(com.medication.reminders.R.string.error_user_not_found));
            }
        });
    }
    
    /**
     * 更新地址信息
     * 更新用户的地址信息
     * @param address 详细地址
     */
    public void updateAddressInfo(String address) {
        // 获取当前用户
        getCurrentUserForUpdate(currentUser -> {
            if (currentUser != null) {
                // 更新地址信息
                currentUser.setAddress(address);
                currentUser.setUpdatedAt(System.currentTimeMillis());
                
                // 执行更新
                updateUserProfile(currentUser);
            } else {
                setError(getApplication().getString(com.medication.reminders.R.string.error_user_not_found));
            }
        });
    }
    
    /**
     * 更新医疗信息
     * 更新用户的医疗相关信息
     * @param bloodType 血型
     * @param allergies 过敏信息
     * @param medicalConditions 既往病史
     * @param doctorName 主治医生姓名
     * @param doctorPhone 主治医生电话
     * @param hospitalName 常去医院名称
     */
    public void updateMedicalInfo(String bloodType, String allergies, String medicalConditions,
                                 String doctorName, String doctorPhone, String hospitalName) {
        // 获取当前用户
        getCurrentUserForUpdate(currentUser -> {
            if (currentUser != null) {
                // 更新医疗信息
                currentUser.setBloodType(bloodType);
                currentUser.setAllergies(allergies);
                currentUser.setMedicalConditions(medicalConditions);
                currentUser.setDoctorName(doctorName);
                currentUser.setDoctorPhone(doctorPhone);
                currentUser.setHospitalName(hospitalName);
                currentUser.setUpdatedAt(System.currentTimeMillis());
                
                // 执行更新
                updateUserProfile(currentUser);
            } else {
                setError(getApplication().getString(com.medication.reminders.R.string.error_user_not_found));
            }
        });
    }
    
    /**
     * 更新个人资料照片
     * 更新用户的个人资料照片
     * @param photoUri 照片URI
     */
    public void updateProfilePhoto(Uri photoUri) {
        if (photoUri == null) {
            setError(getApplication().getString(com.medication.reminders.R.string.error_photo_uri_empty));
            return;
        }
        
        // 获取当前用户
        getCurrentUserForUpdate(currentUser -> {
            if (currentUser != null) {
                // 更新照片路径
                currentUser.setProfilePhotoPath(photoUri.toString());
                currentUser.setUpdatedAt(System.currentTimeMillis());
                
                // 执行更新
                updateUserProfile(currentUser);
            } else {
                setError(getApplication().getString(com.medication.reminders.R.string.error_user_not_found));
            }
        });
    }
    
    /**
     * 获取当前用户用于更新操作
     * 这是一个辅助方法，用于获取当前用户并执行更新操作
     * @param callback 获取用户后的回调
     */
    private void getCurrentUserForUpdate(UserUpdateCallback callback) {
        if (isTestMode) {
            performGetCurrentUserForUpdateSync(callback);
        } else {
            performGetCurrentUserForUpdateAsync(callback);
        }
    }
    
    /**
     * 同步获取当前用户用于更新（测试用）
     * @param callback 回调接口
     */
    private void performGetCurrentUserForUpdateSync(UserUpdateCallback callback) {
        userRepository.getCurrentUserAsync(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                callback.onUserRetrieved(user);
            }
            
            @Override
            public void onError(String error) {
                setError(getApplication().getString(com.medication.reminders.R.string.error_get_current_user_failed, error));
            }
        });
    }
    
    /**
     * 异步获取当前用户用于更新（生产环境）
     * @param callback 回调接口
     */
    private void performGetCurrentUserForUpdateAsync(UserUpdateCallback callback) {
        executorService.execute(() -> {
            userRepository.getCurrentUserAsync(new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    postToMainThread(() -> callback.onUserRetrieved(user));
                }
                
                @Override
                public void onError(String error) {
                    postToMainThread(() -> setError(getApplication().getString(com.medication.reminders.R.string.error_get_current_user_failed, error)));
                }
            });
        });
    }

    
    /**
     * 用户更新回调接口
     * 用于获取当前用户后执行更新操作
     */
    private interface UserUpdateCallback {
        void onUserRetrieved(User user);
    }

    /**
     * LoginInfo 数据类
     * 用于保存登录时的用户名和密码
     */
    public static class LoginInfo {
        private String username;
        private String password;

        public LoginInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }

    /**
     * 从存储中加载保存的登录信息
     * 异步从UserRepository中获取保存的登录信息
     */
    private void loadSavedLoginInfo() {
        executorService.execute(() -> {
            userRepository.getSavedLoginInfo(new RepositoryCallback<LoginInfo>() {
                @Override
                public void onSuccess(LoginInfo info) {
                    postToMainThread(() -> savedLoginInfo.setValue(info));
                }

                @Override
                public void onError(String error) {
                    // 如果获取失败，设置为 null
                    postToMainThread(() -> savedLoginInfo.setValue(null));
                }
            });
        });
    }
}
