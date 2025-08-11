package com.medication.reminders.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.medication.reminders.R;
import com.medication.reminders.databinding.ActivityRegisterBinding;
import com.medication.reminders.utils.UserValidator;
import com.medication.reminders.viewmodels.UserViewModel;

/**
 * 注册活动类 - 处理用户注册界面和交互
 * 实现MVVM模式和ViewBinding，为老年用户提供友好的界面
 * 
 * 重构说明：
 * - 移除对 UserRepository 的直接依赖
 * - 使用 UserViewModel 处理注册逻辑
 * - 观察 registrationStatus LiveData 更新 UI
 * - 在保存时进行表单验证
 */
public class RegisterActivity extends AppCompatActivity {
    
    private ActivityRegisterBinding binding;
    private UserViewModel userViewModel;
    
    // 用户交互状态跟踪
    private boolean userHasInteracted = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        android.util.Log.d("RegisterActivity", "onCreate started");
        
        try {
            // 初始化ViewBinding
            binding = ActivityRegisterBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            android.util.Log.d("RegisterActivity", "ViewBinding initialized");
            
            // 设置标题
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.register_title));
            }
            
            // 初始化UserViewModel
            userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
            android.util.Log.d("RegisterActivity", "UserViewModel initialized");
            
            // 设置UI
            setupUI();
            setupObservers();
            
            android.util.Log.d("RegisterActivity", "onCreate completed successfully");
        } catch (Exception e) {
            android.util.Log.e("RegisterActivity", "Error in onCreate", e);
            // 如果出错，显示错误信息但不崩溃
            Toast.makeText(this, getString(R.string.initialization_error, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 设置UI组件和监听器
     */
    private void setupUI() {
        // 设置按钮点击监听器
        binding.btnRegister.setOnClickListener(v -> handleRegisterClick());
        binding.tvLoginLink.setOnClickListener(v -> navigateToLogin());
        
        // 初始化UI状态
        binding.loadingOverlay.setVisibility(android.view.View.GONE);
        binding.tvMessage.setVisibility(android.view.View.GONE);
        
        // 设置输入字段监听器
        setupInputFieldListeners();
    }
    
    /**
     * 设置输入字段的文本变化监听器
     * 当用户输入时清除错误信息
     */
    private void setupInputFieldListeners() {
        // 用户名输入监听器
        binding.etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilUsername.setError(null);
                binding.tvMessage.setVisibility(android.view.View.GONE);
                userViewModel.clearErrorMessage();
                userViewModel.clearFormValidationError();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // 手机号输入监听器
        binding.etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPhone.setError(null);
                binding.tvMessage.setVisibility(android.view.View.GONE);
                userViewModel.clearErrorMessage();
                userViewModel.clearFormValidationError();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // 邮箱输入监听器
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmail.setError(null);
                binding.tvMessage.setVisibility(android.view.View.GONE);
                userViewModel.clearErrorMessage();
                userViewModel.clearFormValidationError();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // 密码输入监听器
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
                binding.tvMessage.setVisibility(android.view.View.GONE);
                userViewModel.clearErrorMessage();
                userViewModel.clearFormValidationError();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    /**
     * 设置UserViewModel的观察者
     * 观察注册状态、加载状态和错误信息
     */
    private void setupObservers() {
        // 观察注册状态消息
        userViewModel.getRegistrationStatus().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String registrationStatus) {
                if (registrationStatus != null && !registrationStatus.isEmpty()) {
                    if (registrationStatus.contains(getString(R.string.diary_operation_success_contains))) {
                        showSuccess(registrationStatus);
                    } else {
                        showError(registrationStatus);
                    }
                }
            }
        });
        
        // 观察注册成功状态
        userViewModel.getRegistrationSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean registrationSuccess) {
                if (registrationSuccess != null && registrationSuccess) {
                    // 注册成功，延迟后跳转到登录页面
                    binding.getRoot().postDelayed(() -> {
                        navigateToLoginWithSuccess();
                    }, 2000); // 2秒延迟让用户看到成功消息
                }
            }
        });
        
        // 观察加载状态
        userViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading != null && isLoading) {
                    updateLoadingState(true);
                } else if (isLoading != null && !isLoading && hasUserInteracted()) {
                    updateLoadingState(false);
                }
            }
        });
        
        // 观察错误消息
        userViewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    showError(errorMessage);
                }
            }
        });
        
        // 观察成功消息
        userViewModel.getSuccessMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String successMessage) {
                if (successMessage != null && !successMessage.isEmpty()) {
                    showSuccess(successMessage);
                }
            }
        });
        
        // 观察表单验证错误
        userViewModel.getFormValidationError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String validationError) {
                if (validationError != null && !validationError.isEmpty()) {
                    showError(validationError);
                    // 同时在相应的输入字段显示错误
                    showFieldValidationErrors(validationError);
                }
            }
        });
    }
    
    /**
     * 检查用户是否已经交互过
     * @return 用户交互状态
     */
    private boolean hasUserInteracted() {
        return userHasInteracted;
    }
    
    /**
     * 处理注册按钮点击事件
     * 进行表单验证并调用UserViewModel处理注册
     */
    private void handleRegisterClick() {
        // 标记用户已经交互
        userHasInteracted = true;
        
        // 清除之前的错误信息
        userViewModel.clearErrorMessage();
        userViewModel.clearFormValidationError();
        clearAllFieldErrors();
        
        // 验证所有字段
        if (!validateAllFields()) {
            showError(getString(R.string.input_validation_error));
            return;
        }
        
        // 获取输入值
        String username = getInputText(binding.etUsername);
        String phoneNumber = getInputText(binding.etPhone);
        String email = getInputText(binding.etEmail);
        String password = getInputText(binding.etPassword);
        
        // 调用UserViewModel处理注册
        userViewModel.registerUser(username, email, phoneNumber, password);
    }
    
    /**
     * 安全地从EditText获取文本
     * @param editText 输入框
     * @return 文本内容，去除首尾空格
     */
    private String getInputText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
    
    /**
     * 验证所有输入字段
     * @return 验证是否通过
     */
    private boolean validateAllFields() {
        String username = getInputText(binding.etUsername);
        String phone = getInputText(binding.etPhone);
        String email = getInputText(binding.etEmail);
        String password = getInputText(binding.etPassword);
        
        boolean isValid = true;
        
        // 验证用户名
        if (username.isEmpty()) {
            binding.tilUsername.setError(getString(R.string.username_required));
            isValid = false;
        } else {
            com.medication.reminders.models.ProfileValidationResult result = UserValidator.validateUsername(username);
            if (!result.isValid()) {
                binding.tilUsername.setError(result.getErrorMessage());
                isValid = false;
            } else {
                binding.tilUsername.setError(null);
            }
        }
        
        // 验证手机号
        if (phone.isEmpty()) {
            binding.tilPhone.setError(getString(R.string.phone_required));
            isValid = false;
        } else {
            com.medication.reminders.models.ProfileValidationResult result = UserValidator.validatePhoneNumber(phone);
            if (!result.isValid()) {
                binding.tilPhone.setError(result.getErrorMessage());
                isValid = false;
            } else {
                binding.tilPhone.setError(null);
            }
        }
        
        // 验证邮箱
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.email_required));
            isValid = false;
        } else {
            com.medication.reminders.models.ProfileValidationResult result = UserValidator.validateEmail(email);
            if (!result.isValid()) {
                binding.tilEmail.setError(result.getErrorMessage());
                isValid = false;
            } else {
                binding.tilEmail.setError(null);
            }
        }
        
        // 验证密码
        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.password_required));
            isValid = false;
        } else {
            com.medication.reminders.models.ProfileValidationResult result = UserValidator.validatePassword(password);
            if (!result.isValid()) {
                binding.tilPassword.setError(result.getErrorMessage());
                isValid = false;
            } else {
                binding.tilPassword.setError(null);
            }
        }
        
        return isValid;
    }
    
    /**
     * 清除所有字段的错误信息
     */
    private void clearAllFieldErrors() {
        binding.tilUsername.setError(null);
        binding.tilPhone.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
    }
    
    /**
     * 根据验证错误消息在相应字段显示错误
     * @param validationError 验证错误消息
     */
    private void showFieldValidationErrors(String validationError) {
        // 根据错误消息内容判断是哪个字段的错误
        if (validationError.contains(getString(R.string.field_error_username))) {
            binding.tilUsername.setError(validationError);
        } else if (validationError.contains(getString(R.string.field_error_phone)) || validationError.contains("电话")) {
            binding.tilPhone.setError(validationError);
        } else if (validationError.contains(getString(R.string.field_error_email)) || validationError.contains("邮件")) {
            binding.tilEmail.setError(validationError);
        } else if (validationError.contains(getString(R.string.field_error_password))) {
            binding.tilPassword.setError(validationError);
        }
    }
    
    /**
     * 更新加载状态
     * @param isLoading 是否正在加载
     */
    private void updateLoadingState(boolean isLoading) {
        if (isLoading) {
            // 显示全屏loading遮罩
            binding.loadingOverlay.setVisibility(android.view.View.VISIBLE);
            binding.loadingOverlay.setAlpha(0f);
            binding.loadingOverlay.animate().alpha(1f).setDuration(300).start();
            
            // 禁用按钮和输入字段
            binding.btnRegister.setEnabled(false);
            setInputFieldsEnabled(false);
            binding.tvMessage.setVisibility(android.view.View.GONE);
        } else {
            // 隐藏loading遮罩
            binding.loadingOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    binding.loadingOverlay.setVisibility(android.view.View.GONE);
                })
                .start();
            
            // 重新启用按钮和输入字段
            binding.btnRegister.setEnabled(true);
            setInputFieldsEnabled(true);
        }
    }
    
    /**
     * 设置输入字段的启用状态
     * @param enabled 是否启用
     */
    private void setInputFieldsEnabled(boolean enabled) {
        binding.etUsername.setEnabled(enabled);
        binding.etPhone.setEnabled(enabled);
        binding.etEmail.setEnabled(enabled);
        binding.etPassword.setEnabled(enabled);
    }
    
    /**
     * 显示错误消息
     * @param message 错误消息
     */
    private void showError(String message) {
        binding.tvMessage.setText(message);
        binding.tvMessage.setTextColor(getResources().getColor(R.color.error_color, getTheme()));
        binding.tvMessage.setBackgroundResource(R.drawable.error_message_background);
        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
        
        // 添加淡入动画
        binding.tvMessage.setAlpha(0f);
        binding.tvMessage.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
        
        // 为老年用户添加无障碍播报
        binding.tvMessage.setContentDescription(getString(R.string.error_message_content_description, message));
        binding.tvMessage.announceForAccessibility(getString(R.string.register_error_prefix) + message);
    }
    
    /**
     * 显示成功消息
     * @param message 成功消息
     */
    private void showSuccess(String message) {
        binding.tvMessage.setText("🎉 " + message);
        binding.tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        binding.tvMessage.setBackgroundResource(R.drawable.success_message_background);
        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
        
        // 添加淡入动画
        binding.tvMessage.setAlpha(0f);
        binding.tvMessage.animate()
            .alpha(1f)
            .setDuration(500)
            .start();
        
        // 为老年用户添加无障碍播报
        binding.tvMessage.setContentDescription(getString(R.string.success_message_content_description, message));
        binding.tvMessage.announceForAccessibility(getString(R.string.register_success_prefix) + message);
        
        // 添加成功动画效果
        addSuccessAnimation(binding.tvMessage);
    }
    
    /**
     * 为成功消息添加动画效果
     * @param view 要应用动画的视图
     */
    private void addSuccessAnimation(android.view.View view) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(400)
            .start();
    }
    
    /**
     * 导航到登录页面
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
    
    /**
     * 注册成功后导航到登录页面
     */
    private void navigateToLoginWithSuccess() {
        Toast.makeText(this, getString(R.string.register_success_message), Toast.LENGTH_LONG).show();
        
        Intent intent = new Intent(this, LoginActivity.class);
        // 将注册的用户名传递给登录页面，方便用户登录
        String username = getInputText(binding.etUsername);
        intent.putExtra(getString(R.string.intent_key_registered_username), username);
        intent.putExtra(getString(R.string.intent_key_registration_success), true);
        
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理ViewBinding引用以防止内存泄漏
        binding = null;
        
        // 清理ViewModel引用
        if (userViewModel != null) {
            userViewModel.clearErrorMessage();
            userViewModel.clearSuccessMessage();
            userViewModel.resetRegistrationStatus();
            userViewModel = null;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // 当应用进入后台时，清除敏感信息
        if (binding != null && binding.etPassword != null) {
            // 为安全起见清除密码字段
            binding.etPassword.setText("");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // 返回此活动时刷新UI状态
        if (userViewModel != null) {
            // 重置任何可能过时的错误状态
            userViewModel.clearErrorMessage();
            userViewModel.clearSuccessMessage();
            userViewModel.resetRegistrationStatus();
        }
    }
}