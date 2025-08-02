package com.medication.reminders;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.os.Handler;
import android.os.Looper;

/**
 * 用户注册功能的ViewModel
 * 处理注册业务逻辑和UI状态管理
 */
public class RegisterViewModel extends ViewModel {
    
    private UserRepositoryInterface userRepository;
    private Handler mainHandler;
    private java.util.concurrent.ExecutorService executorService;
    private boolean isTestMode = false;
    
    // 用于UI状态管理的LiveData属性
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<Boolean> registerSuccess;
    
    /**
     * 使用UserRepository依赖注入的构造函数
     * @param userRepository 用于用户数据操作的仓库
     */
    public RegisterViewModel(UserRepositoryInterface userRepository) {
        this.userRepository = userRepository;
        try {
            this.mainHandler = new Handler(Looper.getMainLooper());
        } catch (RuntimeException e) {
            // 在测试环境中，Looper可能不可用
            this.isTestMode = true;
        }
        this.executorService = java.util.concurrent.Executors.newSingleThreadExecutor();
        initializeLiveData();
    }
    
    /**
     * 初始化所有LiveData属性
     */
    private void initializeLiveData() {
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();  // 不设置初始值，避免触发观察者
        registerSuccess = new MutableLiveData<>();  // 不设置初始值，避免触发观察者
    }
    
    /**
     * 获取错误消息LiveData
     * @return 包含错误消息的MutableLiveData
     */
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 获取加载状态LiveData
     * @return 包含加载状态的MutableLiveData
     */
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * 获取注册成功状态LiveData
     * @return 包含注册成功状态的MutableLiveData
     */
    public MutableLiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }
    
    /**
     * 使用提供的信息注册新用户，具有增强的错误处理功能
     * 处理输入验证和注册过程
     * @param username 用户选择的用户名
     * @param phoneNumber 用户的手机号
     * @param email 用户的邮箱地址
     * @param password 用户的密码
     */
    public void registerUser(String username, String phoneNumber, String email, String password) {
        // 设置加载状态
        isLoading.setValue(true);
        
        // 重置之前的状态
        errorMessage.setValue(null);
        registerSuccess.setValue(false);
        
        // 根据是否为测试模式选择执行方式
        if (isTestMode) {
            // 在测试模式下同步执行
            performRegistration(username, phoneNumber, email, password);
        } else {
            // 使用线程池在后台线程中执行注册以避免阻塞UI
            executorService.execute(() -> performRegistration(username, phoneNumber, email, password));
        }
    }
    
    /**
     * 执行注册逻辑
     */
    private void performRegistration(String username, String phoneNumber, String email, String password) {
        try {
            // 首先验证输入参数
            if (username == null || phoneNumber == null || email == null || password == null) {
                postError("所有字段都必须填写");
                return;
            }
            
            // 修剪输入值
            String trimmedUsername = username.trim();
            String trimmedPhone = phoneNumber.trim();
            String trimmedEmail = email.trim();
            String trimmedPassword = password.trim();
            
            // 修剪后检查空字段
            if (trimmedUsername.isEmpty() || trimmedPhone.isEmpty() || 
                trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
                postError("所有字段都必须填写，请检查输入内容");
                return;
            }
            
            // 创建UserInfo对象
            UserInfo userInfo = new UserInfo(trimmedUsername, trimmedPhone, trimmedEmail, trimmedPassword);
            
            // 执行输入验证
            String validationError = validateInput(userInfo);
            if (validationError != null) {
                postError(validationError);
                return;
            }
            
            // 实际保存用户数据
            UserRepository.RepositoryResult saveResult = userRepository.saveUser(userInfo);
            
            if (isTestMode) {
                // 在测试模式下直接设置结果
                if (saveResult.isSuccess()) {
                    registerSuccess.setValue(true);
                    errorMessage.setValue(null);
                } else {
                    errorMessage.setValue(saveResult.getMessage());
                    registerSuccess.setValue(false);
                }
                isLoading.setValue(false);
            } else {
                // 3秒后返回实际的保存结果
                mainHandler.postDelayed(() -> {
                    if (saveResult.isSuccess()) {
                        registerSuccess.setValue(true);
                        errorMessage.setValue(null);
                    } else {
                        errorMessage.setValue(saveResult.getMessage());
                        registerSuccess.setValue(false);
                    }
                    isLoading.setValue(false);
                }, 3000);
            }
            
        } catch (Exception e) {
            // 记录异常详情以便调试
            if (!isTestMode) {
                android.util.Log.e("RegisterViewModel", "注册过程中发生异常", e);
            }
            postError("注册过程中发生错误，请检查网络连接后重试");
        }
    }
    
    /**
     * 在主线程上发布错误消息
     * @param message 要发布的错误消息
     */
    private void postError(String message) {
        if (isTestMode) {
            // 在测试模式下直接设置值
            errorMessage.setValue(message);
            registerSuccess.setValue(false);
            isLoading.setValue(false);
        } else {
            mainHandler.post(() -> {
                errorMessage.setValue(message);
                registerSuccess.setValue(false);
                isLoading.setValue(false);
            });
        }
    }
    
    /**
     * 使用UserRepository验证用户输入
     * @param userInfo 包含用户数据的UserInfo对象
     * @return 如果验证失败返回错误消息，验证通过返回null
     */
    private String validateInput(UserInfo userInfo) {
        UserValidator.ValidationResult result = userRepository.validateUserInfo(userInfo);
        return result.isValid() ? null : result.getErrorMessage();
    }
    
    /**
     * 清理资源以防止内存泄漏
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}