package com.medication.reminders.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.medication.reminders.database.DatabaseErrorHandler;
import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.UserDao;
import com.medication.reminders.database.entity.User;
import com.medication.reminders.models.ProfileValidationResult;
import com.medication.reminders.models.RepositoryCallback;

import com.medication.reminders.utils.UserValidator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UserRepository类，实现Repository模式
 * 基于Room数据库的用户数据访问层，提供统一的用户管理接口
 */
public class UserRepository implements com.medication.reminders.models.UserRepositoryInterface {
    
    private static final String TAG = "UserRepository";
    
    private UserDao userDao;
    private Context context;
    private ExecutorService executorService;
    private MutableLiveData<Long> currentUserId;
    
    // 单例实例
    private static volatile UserRepository INSTANCE;
    
    /**
     * 构造函数（支持单例和直接实例化）
     * @param context 应用程序上下文
     */
    public UserRepository(Context context) {
        initializeRepository(context);
    }
    
    /**
     * 初始化Repository的通用方法
     * @param context 应用程序上下文
     */
    private void initializeRepository(Context context) {
        MedicationDatabase database = MedicationDatabase.getDatabase(context);
        this.userDao = database.userDao();
        this.context = context.getApplicationContext();
        this.executorService = Executors.newFixedThreadPool(2);
        this.currentUserId = new MutableLiveData<>();
        
        // 初始化时检查是否有已登录用户
        initializeCurrentUser();
    }
    

    
    /**
     * 获取UserRepository单例实例
     * @param context 应用程序上下文
     * @return UserRepository实例
     */
    public static UserRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UserRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserRepository(context);
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * 初始化当前用户
     * 检查是否有已登录用户或被记住的用户
     */
    private void initializeCurrentUser() {
        executorService.execute(() -> {
            try {
                // 首先检查是否有已登录用户
                User loggedInUser = userDao.getCurrentLoggedInUser();
                if (loggedInUser != null) {
                    currentUserId.postValue(loggedInUser.getId());
                    Log.d(TAG, "找到已登录用户: " + loggedInUser.getUsername());
                    return;
                }
                
                // 检查是否有被记住的用户
                User rememberedUser = userDao.getRememberedUser();
                if (rememberedUser != null) {
                    // 自动登录被记住的用户
                    long loginTime = System.currentTimeMillis();
                    int result = userDao.setUserLoggedIn(rememberedUser.getId(), loginTime);
                    if (result > 0) {
                        currentUserId.postValue(rememberedUser.getId());
                        Log.d(TAG, "自动登录被记住的用户: " + rememberedUser.getUsername());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "初始化当前用户失败", e);
            }
        });
    }
    
    // ========== 用户注册相关方法 ==========
    
    /**
     * 用户注册 - 检查username唯一性
     * @param username 用户名
     * @param email 邮箱地址
     * @param phone 电话号码
     * @param password 密码
     * @param callback 回调接口
     */
    public void registerUser(String username, String email, String phone, String password, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                // 验证输入参数
                if (username == null || username.trim().isEmpty()) {
                    callback.onError("用户名不能为空");
                    return;
                }
                if (email == null || email.trim().isEmpty()) {
                    callback.onError("邮箱不能为空");
                    return;
                }
                if (phone == null || phone.trim().isEmpty()) {
                    callback.onError("电话号码不能为空");
                    return;
                }
                if (password == null || password.trim().isEmpty()) {
                    callback.onError("密码不能为空");
                    return;
                }
                
                // 使用统一的UserValidator进行验证
                com.medication.reminders.models.ProfileValidationResult validationResult = 
                    com.medication.reminders.utils.UserValidator.validateRegistrationForm(
                        username.trim(), email.trim(), phone.trim(), password);
                if (!validationResult.isValid()) {
                    callback.onError(validationResult.getErrorMessage());
                    return;
                }
                
                // 创建User对象
                User user = new User(username.trim(), email.trim(), phone.trim(), password);
                
                // 检查用户名唯一性
                int usernameCount = userDao.getUsernameCount(username.trim());
                if (usernameCount > 0) {
                    callback.onError("用户名已存在，请选择其他用户名");
                    return;
                }
                
                // 检查邮箱唯一性
                int emailCount = userDao.getEmailCount(email.trim());
                if (emailCount > 0) {
                    callback.onError("邮箱地址已被注册，请使用其他邮箱");
                    return;
                }
                
                // 检查电话号码唯一性
                int phoneCount = userDao.getPhoneCount(phone.trim());
                if (phoneCount > 0) {
                    callback.onError("电话号码已被注册，请使用其他号码");
                    return;
                }
                
                // 插入用户到数据库
                long userId = userDao.insertUser(user);
                DatabaseErrorHandler.DatabaseResult<Long> result = DatabaseErrorHandler.validateInsertResult(userId);
                
                if (result.isSuccess()) {
                    DatabaseErrorHandler.logDatabaseOperation("INSERT", "users", true, "用户注册成功，ID: " + userId);
                    callback.onSuccess(userId);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("INSERT", "users", false, result.getErrorMessage());
                    callback.onError(result.getErrorMessage());
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = DatabaseErrorHandler.handleException(e, "用户注册");
                DatabaseErrorHandler.logDatabaseOperation("INSERT", "users", false, error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    // ========== 用户登录相关方法 ==========
    
    /**
     * 用户登录 - 基于username查找，成功后设置当前用户ID
     * @param username 用户名
     * @param password 密码
     * @param rememberMe 是否记住我
     * @param callback 回调接口
     */
    public void loginUser(String username, String password, boolean rememberMe, RepositoryCallback<User> callback) {
        executorService.execute(() -> {
            try {
                // 验证输入参数
                if (username == null || username.trim().isEmpty()) {
                    callback.onError("请输入用户名");
                    return;
                }
                if (password == null || password.trim().isEmpty()) {
                    callback.onError("请输入密码");
                    return;
                }
                
                // 查找用户
                User user = userDao.getUserByUsername(username.trim());
                if (user == null) {
                    // 增加登录尝试次数
                    incrementLoginAttempts(username.trim());
                    callback.onError("用户名不存在");
                    return;
                }
                
                // 检查登录尝试次数
                if (user.getLoginAttempts() >= 3) {
                    long currentTime = System.currentTimeMillis();
                    long timeDiff = currentTime - user.getLastAttemptTime();
                    
                    // 30秒锁定时间
                    if (timeDiff < 30000) {
                        callback.onError("登录失败次数过多，请30秒后再试");
                        return;
                    } else {
                        // 重置登录尝试次数
                        userDao.resetLoginAttempts(username.trim());
                        user.setLoginAttempts(0);
                    }
                }
                
                // 验证密码（明文比较）
                if (!password.equals(user.getPassword())) {
                    // 增加登录尝试次数
                    incrementLoginAttempts(username.trim());
                    callback.onError("密码错误");
                    return;
                }
                
                // 登录成功 - 清除所有用户的登录状态
                userDao.logoutAllUsers();
                
                // 设置当前用户为已登录
                long loginTime = System.currentTimeMillis();
                int result = userDao.setUserLoggedIn(user.getId(), loginTime);
                
                if (result > 0) {
                    // 设置记住我状态
                    if (rememberMe) {
                        userDao.updateRememberMe(user.getId(), true);
                    } else {
                        userDao.clearAllRememberMe();
                    }
                    
                    // 重置登录尝试次数
                    userDao.resetLoginAttempts(username.trim());
                    
                    // 更新当前用户ID
                    currentUserId.postValue(user.getId());
                    
                    // 更新用户对象状态
                    user.setLoggedIn(true);
                    user.setLastLoginTime(loginTime);
                    user.setRememberMe(rememberMe);
                    user.resetLoginAttempts();
                    
                    DatabaseErrorHandler.logDatabaseOperation("LOGIN", "users", true, "用户登录成功: " + username);
                    callback.onSuccess(user);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("LOGIN", "users", false, "设置登录状态失败");
                    callback.onError("登录失败，请重试");
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = DatabaseErrorHandler.handleException(e, "用户登录");
                DatabaseErrorHandler.logDatabaseOperation("LOGIN", "users", false, error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 增加登录尝试次数
     * @param username 用户名
     */
    private void incrementLoginAttempts(String username) {
        try {
            User user = userDao.getUserByUsername(username);
            if (user != null) {
                int newAttempts = user.getLoginAttempts() + 1;
                long attemptTime = System.currentTimeMillis();
                userDao.updateLoginAttempts(username, newAttempts, attemptTime);
            }
        } catch (Exception e) {
            Log.e(TAG, "增加登录尝试次数失败", e);
        }
    }
    
    // ========== 用户资料管理 ==========
    
    /**
     * 获取当前用户 - 基于当前登录用户ID
     * @return 当前用户的LiveData对象
     */
    public LiveData<User> getCurrentUser() {
        Long userId = currentUserId.getValue();
        if (userId != null && userId > 0) {
            return userDao.getUserByIdLiveData(userId);
        } else {
            // 返回空的LiveData
            MutableLiveData<User> emptyLiveData = new MutableLiveData<>();
            emptyLiveData.setValue(null);
            return emptyLiveData;
        }
    }
    
    /**
     * 异步获取当前用户
     * @param callback 回调接口
     */
    public void getCurrentUserAsync(RepositoryCallback<User> callback) {
        executorService.execute(() -> {
            try {
                Long userId = currentUserId.getValue();
                if (userId != null && userId > 0) {
                    User user = userDao.getUserById(userId);
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("未找到当前用户");
                    }
                } else {
                    // 尝试从数据库获取已登录用户
                    User loggedInUser = userDao.getCurrentLoggedInUser();
                    if (loggedInUser != null) {
                        currentUserId.postValue(loggedInUser.getId());
                        callback.onSuccess(loggedInUser);
                    } else {
                        callback.onError("没有用户登录");
                    }
                }
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = DatabaseErrorHandler.handleException(e, "获取当前用户");
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 更新用户资料 - 基于当前用户ID
     * @param user 用户对象
     * @param callback 回调接口
     */
    public void updateUserProfile(User user, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                if (user == null) {
                    callback.onError("用户对象不能为空");
                    return;
                }
                
                // 使用统一的UserValidator进行验证
                ProfileValidationResult validationResult = UserValidator.validateCompleteUser(user);
                if (!validationResult.isValid()) {
                    callback.onError(validationResult.getErrorMessage());
                    return;
                }
                
                // 检查用户是否存在
                User existingUser = userDao.getUserById(user.getId());
                if (existingUser == null) {
                    callback.onError("用户不存在");
                    return;
                }
                
                // 检查唯一性约束（排除当前用户）
                if (!user.getUsername().equals(existingUser.getUsername())) {
                    int usernameCount = userDao.getUsernameCountExcluding(user.getUsername(), user.getId());
                    if (usernameCount > 0) {
                        callback.onError("用户名已存在");
                        return;
                    }
                }
                
                if (!user.getEmail().equals(existingUser.getEmail())) {
                    int emailCount = userDao.getEmailCountExcluding(user.getEmail(), user.getId());
                    if (emailCount > 0) {
                        callback.onError("邮箱地址已被使用");
                        return;
                    }
                }
                
                if (!user.getPhone().equals(existingUser.getPhone())) {
                    int phoneCount = userDao.getPhoneCountExcluding(user.getPhone(), user.getId());
                    if (phoneCount > 0) {
                        callback.onError("电话号码已被使用");
                        return;
                    }
                }
                
                // 更新时间戳
                user.setUpdatedAt(System.currentTimeMillis());
                
                // 更新用户信息
                int result = userDao.updateUser(user);
                DatabaseErrorHandler.DatabaseResult<Integer> updateResult = DatabaseErrorHandler.validateUpdateResult(result);
                
                if (updateResult.isSuccess()) {
                    DatabaseErrorHandler.logDatabaseOperation("UPDATE", "users", true, "用户资料更新成功，ID: " + user.getId());
                    callback.onSuccess(true);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("UPDATE", "users", false, updateResult.getErrorMessage());
                    callback.onError(updateResult.getErrorMessage());
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = DatabaseErrorHandler.handleException(e, "更新用户资料");
                DatabaseErrorHandler.logDatabaseOperation("UPDATE", "users", false, error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    // ========== 用户会话管理 ==========
    
    /**
     * 用户登出
     * @param callback 回调接口
     */
    public void logoutUser(RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                // 清除所有用户的登录状态
                int result = userDao.logoutAllUsers();
                
                // 清除当前用户ID
                currentUserId.postValue(null);
                
                DatabaseErrorHandler.logDatabaseOperation("LOGOUT", "users", true, "用户登出成功");
                callback.onSuccess(true);
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = DatabaseErrorHandler.handleException(e, "用户登出");
                DatabaseErrorHandler.logDatabaseOperation("LOGOUT", "users", false, error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    // ========== 注册时唯一性检查 ==========
    
    /**
     * 注册时唯一性检查
     * @param username 用户名
     * @param callback 回调接口
     */
    public void checkUsernameExists(String username, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                if (username == null || username.trim().isEmpty()) {
                    callback.onSuccess(false);
                    return;
                }
                
                int count = userDao.getUsernameCount(username.trim());
                callback.onSuccess(count > 0);
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = DatabaseErrorHandler.handleException(e, "检查用户名存在性");
                callback.onError(error.getMessage());
            }
        });
    }
    
    // ========== 密码管理 ==========
    
    /**
     * 密码管理 - 基于用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param callback 回调接口
     */
    public void changePassword(String oldPassword, String newPassword, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                Long userId = currentUserId.getValue();
                if (userId == null || userId <= 0) {
                    callback.onError("没有用户登录");
                    return;
                }
                
                // 获取当前用户
                User user = userDao.getUserById(userId);
                if (user == null) {
                    callback.onError("用户不存在");
                    return;
                }
                
                // 验证旧密码
                if (!oldPassword.equals(user.getPassword())) {
                    callback.onError("旧密码错误");
                    return;
                }
                
                // 验证新密码
                if (newPassword == null || newPassword.trim().isEmpty()) {
                    callback.onError("新密码不能为空");
                    return;
                }
                
                // 更新密码
                long updateTime = System.currentTimeMillis();
                int result = userDao.updatePassword(userId, newPassword, updateTime);
                DatabaseErrorHandler.DatabaseResult<Integer> updateResult = DatabaseErrorHandler.validateUpdateResult(result);
                
                if (updateResult.isSuccess()) {
                    DatabaseErrorHandler.logDatabaseOperation("UPDATE_PASSWORD", "users", true, "密码更新成功，用户ID: " + userId);
                    callback.onSuccess(true);
                } else {
                    DatabaseErrorHandler.logDatabaseOperation("UPDATE_PASSWORD", "users", false, updateResult.getErrorMessage());
                    callback.onError("密码更新失败");
                }
                
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = DatabaseErrorHandler.handleException(e, "修改密码");
                callback.onError(error.getMessage());
            }
        });
    }
    
    // ========== 会话管理 ==========
    
    /**
     * 会话管理 - 检查被记住的用户
     * @param callback 回调接口
     */
    public void checkRememberedUser(RepositoryCallback<User> callback) {
        executorService.execute(() -> {
            try {
                User rememberedUser = userDao.getRememberedUser();
                if (rememberedUser != null) {
                    callback.onSuccess(rememberedUser);
                } else {
                    callback.onError("没有被记住的用户");
                }
            } catch (Exception e) {
                DatabaseErrorHandler.DatabaseError error = DatabaseErrorHandler.handleException(e, "检查被记住的用户");
                callback.onError(error.getMessage());
            }
        });
    }
    
    /**
     * 获取当前用户ID
     * @return 当前用户ID的LiveData
     */
    public LiveData<Long> getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * 同步获取当前用户ID
     * @return 当前用户ID，如果没有则返回0
     */
    public long getCurrentUserIdSync() {
        Long userId = currentUserId.getValue();
        return userId != null ? userId : 0;
    }
    
    // ========== 清理方法 ==========
    
    /**
     * 清理资源（用于测试或应用关闭时）
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // ========== 向后兼容性支持 ==========
    
    /**
     * RepositoryResult类，用于保持向后兼容性
     * 包装操作结果和消息
     */
    public static class RepositoryResult {
        private final boolean success;
        private final String message;
        
        public RepositoryResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * AuthenticationResult类，用于认证操作结果
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        
        public AuthenticationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * SavedCredentials类，用于保存的登录凭据
     */
    public static class SavedCredentials {
        private final String username;
        private final String password;
        
        public SavedCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
    }
    
    // ========== 向后兼容的同步方法 ==========
    
    /**
     * 同步方式验证用户凭据（向后兼容）
     * @param username 用户名
     * @param password 密码
     * @return 认证结果
     */
    public AuthenticationResult authenticateUser(String username, String password) {
        try {
            User user = userDao.getUserByUsername(username);
            if (user == null) {
                return new AuthenticationResult(false, "用户名不存在");
            }
            
            if (!password.equals(user.getPassword())) {
                return new AuthenticationResult(false, "密码错误");
            }
            
            return new AuthenticationResult(true, "登录成功");
        } catch (Exception e) {
            return new AuthenticationResult(false, "登录失败，请重试");
        }
    }
    
    /**
     * 获取登录尝试次数（向后兼容）
     * @param username 用户名
     * @return 登录尝试次数
     */
    public int getLoginAttempts(String username) {
        try {
            return userDao.getLoginAttempts(username);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 保存登录凭据（向后兼容）
     * @param username 用户名
     * @param password 密码
     * @return 是否成功
     */
    public boolean saveLoginCredentials(String username, String password) {
        try {
            User user = userDao.getUserByUsername(username);
            if (user != null) {
                userDao.updateRememberMe(user.getId(), true);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取保存的凭据（向后兼容）
     * @return 保存的凭据
     */
    public SavedCredentials getSavedCredentials() {
        try {
            User rememberedUser = userDao.getRememberedUser();
            if (rememberedUser != null) {
                return new SavedCredentials(rememberedUser.getUsername(), rememberedUser.getPassword());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 清除保存的凭据（向后兼容）
     * @return 是否成功
     */
    public boolean clearSavedCredentials() {
        try {
            userDao.clearAllRememberMe();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 设置当前用户（向后兼容）
     * @param username 用户名
     */
    public void setCurrentUser(String username) {
        try {
            User user = userDao.getUserByUsername(username);
            if (user != null) {
                currentUserId.postValue(user.getId());
            }
        } catch (Exception e) {
            Log.e(TAG, "设置当前用户失败", e);
        }
    }
    
    /**
     * 获取当前用户名（向后兼容）
     * @return 当前用户名
     */
    public String getCurrentUsername() {
        try {
            Long userId = currentUserId.getValue();
            if (userId != null && userId > 0) {
                User user = userDao.getUserById(userId);
                return user != null ? user.getUsername() : null;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    

    
    /**
     * 更新注册信息（向后兼容 - 需要适配）
     * @param oldUsername 旧用户名
     * @param newUsername 新用户名
     * @param newEmail 新邮箱
     * @param newPhone 新电话
     * @return 操作结果
     */
    public RepositoryResult updateRegistrationInfo(String oldUsername, String newUsername, String newEmail, String newPhone) {
        // 这个方法需要根据实际需求来实现
        return new RepositoryResult(false, "方法未实现");
    }
    
    /**
     * 保存个人资料照片（向后兼容 - 需要适配）
     * @param username 用户名
     * @param photoUri 照片URI
     * @param callback 回调
     */
    public void saveProfilePhoto(String username, Object photoUri, Object callback) {
        // 这个方法需要根据实际的 PhotoManager 来实现
        // 暂时不实现
    }
    
    /**
     * 获取个人资料照片路径（向后兼容 - 需要适配）
     * @param username 用户名
     * @return 照片路径
     */
    public String getProfilePhotoPath(String username) {
        // 这个方法需要根据实际的 PhotoManager 来实现
        return null;
    }
    
    /**
     * 删除个人资料照片（向后兼容 - 需要适配）
     * @param username 用户名
     * @return 操作结果
     */
    public RepositoryResult deleteProfilePhoto(String username) {
        // 这个方法需要根据实际的 PhotoManager 来实现
        return new RepositoryResult(false, "方法未实现");
    }
    
    // ========== UserRepositoryInterface 实现 ==========
    
    /**
     * 保存用户信息（接口实现）
     * @param username 用户名
     * @param email 邮箱
     * @param phone 电话
     * @param password 密码
     * @return 操作结果
     */
    @Override
    public RepositoryResult saveUser(String username, String email, String phone, String password) {
        if (username == null || email == null || phone == null || password == null) {
            return new RepositoryResult(false, "用户信息不能为空");
        }
        
        try {
            // 创建 User 实体
            User user = new User(username, email, phone, password);
            
            // 验证用户信息
            com.medication.reminders.models.ProfileValidationResult validationResult = UserValidator.validateRegistrationForm(username, email, phone, password);
            if (!validationResult.isValid()) {
                return new RepositoryResult(false, validationResult.getErrorMessage());
            }
            
            // 检查唯一性
            int usernameCount = userDao.getUsernameCount(username);
            if (usernameCount > 0) {
                return new RepositoryResult(false, "用户名已存在");
            }
            
            int emailCount = userDao.getEmailCount(email);
            if (emailCount > 0) {
                return new RepositoryResult(false, "邮箱已被注册");
            }
            
            int phoneCount = userDao.getPhoneCount(phone);
            if (phoneCount > 0) {
                return new RepositoryResult(false, "电话号码已被注册");
            }
            
            // 插入用户
            long userId = userDao.insertUser(user);
            if (userId > 0) {
                return new RepositoryResult(true, "注册成功");
            } else {
                return new RepositoryResult(false, "注册失败");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "保存用户失败", e);
            return new RepositoryResult(false, "注册失败，请重试");
        }
    }
    
    /**
     * 验证用户信息（接口实现）
     * @param username 用户名
     * @param email 邮箱
     * @param phone 电话
     * @param password 密码
     * @return 验证结果
     */
    @Override
    public com.medication.reminders.utils.UserValidator.ValidationResult validateUserInfo(String username, String email, String phone, String password) {
        // 使用UserValidator进行验证
        com.medication.reminders.models.ProfileValidationResult result = UserValidator.validateRegistrationForm(username, email, phone, password);
        
        // 转换为ValidationResult
        return new com.medication.reminders.utils.UserValidator.ValidationResult(result.isValid(), result.getErrorMessage());
    }
    
    /**
     * 检查用户名是否存在（接口实现）
     * @param username 用户名
     * @return 是否存在
     */
    @Override
    public boolean isUsernameExists(String username) {
        try {
            return userDao.getUsernameCount(username) > 0;
        } catch (Exception e) {
            Log.e(TAG, "检查用户名存在性失败", e);
            return false;
        }
    }
    
    /**
     * 清除用户数据（向后兼容 - 用于测试）
     */
    public void clearUserData() {
        try {
            userDao.deleteAllUsers();
            currentUserId.postValue(null);
        } catch (Exception e) {
            Log.e(TAG, "清除用户数据失败", e);
        }
    }
}