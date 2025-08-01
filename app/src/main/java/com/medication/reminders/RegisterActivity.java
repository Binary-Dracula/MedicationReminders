package com.medication.reminders;

import android.os.Bundle;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import com.medication.reminders.databinding.ActivityRegisterBinding;

/**
 * 注册活动类 - 处理用户注册界面和交互
 * 实现MVVM模式和ViewBinding，为老年用户提供友好的界面
 */
public class RegisterActivity extends AppCompatActivity {
    
    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;
    private android.os.Handler debounceHandler;
    private static final int VALIDATION_DEBOUNCE_DELAY = 500; // 500ms防抖延迟
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化ViewBinding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 初始化防抖处理器
        debounceHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        
        // 使用UserRepository初始化ViewModel
        UserRepository userRepository = new UserRepository(this);
        viewModel = new RegisterViewModel(userRepository);
        
        // 设置UI和观察者
        setupUI();
        setupObservers();
    }
    
    /**
     * Setup basic UI elements and click listeners
     */
    private void setupUI() {
        // Set up register button click listener - handles user interaction
        binding.btnRegister.setOnClickListener(v -> handleRegisterClick());
        
        // Initially hide progress bar and message
        binding.progressBar.setVisibility(android.view.View.GONE);
        binding.tvMessage.setVisibility(android.view.View.GONE);
        
        // Setup input field focus listeners for better UX
        setupInputFieldListeners();
    }
    
    /**
     * Setup input field listeners for enhanced user experience with real-time validation
     */
    private void setupInputFieldListeners() {
        // Setup real-time validation for username
        binding.etUsername.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearGlobalErrorMessage();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                validateUsernameRealTime(s.toString());
            }
        });
        
        // Setup real-time validation for phone number
        binding.etPhone.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearGlobalErrorMessage();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                validatePhoneRealTime(s.toString());
            }
        });
        
        // Setup real-time validation for email
        binding.etEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearGlobalErrorMessage();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                validateEmailRealTime(s.toString());
            }
        });
        
        // Setup real-time validation for password
        binding.etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearGlobalErrorMessage();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                validatePasswordRealTime(s.toString());
            }
        });
    }
    
    /**
     * Clear global error message when user starts typing
     */
    private void clearGlobalErrorMessage() {
        // 只清除全局错误消息，保持字段级错误显示
        if (binding.tvMessage.getVisibility() == android.view.View.VISIBLE && 
            binding.tvMessage.getCurrentTextColor() == getResources().getColor(com.medication.reminders.R.color.error_color, getTheme())) {
            hideMessageWithAnimation();
        }
    }
    
    /**
     * 用户名字段的实时验证（带防抖功能）
     */
    private void validateUsernameRealTime(String username) {
        // 移除之前的防抖任务
        debounceHandler.removeCallbacksAndMessages("username_validation");
        
        if (username.isEmpty()) {
            binding.tilUsername.setError(null);
            return;
        }
        
        // 使用防抖延迟执行验证，避免频繁验证影响性能
        debounceHandler.postDelayed(() -> {
            UserValidator.ValidationResult result = UserValidator.validateUsername(username);
            if (!result.isValid()) {
                binding.tilUsername.setError(result.getErrorMessage());
                // 为老年用户提供语音反馈
                binding.tilUsername.announceForAccessibility("用户名输入错误: " + result.getErrorMessage());
            } else {
                binding.tilUsername.setError(null);
                // 成功时的正面反馈
                binding.tilUsername.announceForAccessibility("用户名格式正确");
            }
        }, VALIDATION_DEBOUNCE_DELAY);
    }
    
    /**
     * 手机号字段的实时验证（带防抖功能）
     */
    private void validatePhoneRealTime(String phone) {
        // 移除之前的防抖任务
        debounceHandler.removeCallbacksAndMessages("phone_validation");
        
        if (phone.isEmpty()) {
            binding.tilPhone.setError(null);
            return;
        }
        
        // 使用防抖延迟执行验证
        debounceHandler.postDelayed(() -> {
            UserValidator.ValidationResult result = UserValidator.validatePhoneNumber(phone);
            if (!result.isValid()) {
                binding.tilPhone.setError(result.getErrorMessage());
                binding.tilPhone.announceForAccessibility("手机号输入错误: " + result.getErrorMessage());
            } else {
                binding.tilPhone.setError(null);
                binding.tilPhone.announceForAccessibility("手机号格式正确");
            }
        }, VALIDATION_DEBOUNCE_DELAY);
    }
    
    /**
     * 邮箱字段的实时验证（带防抖功能）
     */
    private void validateEmailRealTime(String email) {
        // 移除之前的防抖任务
        debounceHandler.removeCallbacksAndMessages("email_validation");
        
        if (email.isEmpty()) {
            binding.tilEmail.setError(null);
            return;
        }
        
        // 使用防抖延迟执行验证
        debounceHandler.postDelayed(() -> {
            UserValidator.ValidationResult result = UserValidator.validateEmail(email);
            if (!result.isValid()) {
                binding.tilEmail.setError(result.getErrorMessage());
                binding.tilEmail.announceForAccessibility("邮箱输入错误: " + result.getErrorMessage());
            } else {
                binding.tilEmail.setError(null);
                binding.tilEmail.announceForAccessibility("邮箱格式正确");
            }
        }, VALIDATION_DEBOUNCE_DELAY);
    }
    
    /**
     * 密码字段的实时验证（带防抖功能）
     */
    private void validatePasswordRealTime(String password) {
        // 移除之前的防抖任务
        debounceHandler.removeCallbacksAndMessages("password_validation");
        
        if (password.isEmpty()) {
            binding.tilPassword.setError(null);
            return;
        }
        
        // 使用防抖延迟执行验证
        debounceHandler.postDelayed(() -> {
            UserValidator.ValidationResult result = UserValidator.validatePassword(password);
            if (!result.isValid()) {
                binding.tilPassword.setError(result.getErrorMessage());
                binding.tilPassword.announceForAccessibility("密码输入错误: " + result.getErrorMessage());
            } else {
                binding.tilPassword.setError(null);
                binding.tilPassword.announceForAccessibility("密码格式正确");
            }
        }, VALIDATION_DEBOUNCE_DELAY);
    }
    
    /**
     * Setup LiveData observers to respond to ViewModel state changes
     */
    private void setupObservers() {
        // Observe loading state - handles requirement 4.3, 4.4 for UI updates
        viewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading != null) {
                    updateLoadingState(isLoading);
                }
            }
        });
        
        // Observe error messages - handles requirements 2.1-2.7 for error display
        viewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    showError(errorMessage);
                } else {
                    // Clear error message when null
                    hideMessage();
                }
            }
        });
        
        // Observe registration success - handles success state display
        viewModel.getRegisterSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success != null && success) {
                    showSuccess();
                }
            }
        });
    }
    
    /**
     * Handle register button click with enhanced validation - get input and call ViewModel
     * Implements requirements 2.1-2.7 for input validation through ViewModel
     */
    private void handleRegisterClick() {
        // Hide any previous messages
        hideMessage();
        
        // Validate all fields first
        if (!validateAllFields()) {
            // Show general error message if validation fails
            showError("请检查并修正上述输入错误");
            return;
        }
        
        // Get input values from EditTexts - handles requirements 1.1-1.6
        String username = getInputText(binding.etUsername);
        String phoneNumber = getInputText(binding.etPhone);
        String email = getInputText(binding.etEmail);
        String password = getInputText(binding.etPassword);
        
        // Call ViewModel to handle registration with validation
        viewModel.registerUser(username, phoneNumber, email, password);
    }
    
    /**
     * Safely get text from EditText
     * @param editText EditText to get text from
     * @return String text or empty string if null
     */
    private String getInputText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
    
    /**
     * Update UI loading state - implements loading state UI updates
     * @param isLoading true if loading, false otherwise
     */
    private void updateLoadingState(boolean isLoading) {
        if (isLoading) {
            // Show loading indicator
            binding.progressBar.setVisibility(android.view.View.VISIBLE);
            
            // Disable register button to prevent multiple submissions
            binding.btnRegister.setEnabled(false);
            binding.btnRegister.setText("注册中...");
            
            // Hide any previous messages
            binding.tvMessage.setVisibility(android.view.View.GONE);
            
            // Disable input fields during loading
            setInputFieldsEnabled(false);
        } else {
            // Hide loading indicator
            binding.progressBar.setVisibility(android.view.View.GONE);
            
            // Re-enable register button
            binding.btnRegister.setEnabled(true);
            binding.btnRegister.setText("立即注册");
            
            // Re-enable input fields
            setInputFieldsEnabled(true);
        }
    }
    
    /**
     * Enable or disable all input fields
     * @param enabled true to enable, false to disable
     */
    private void setInputFieldsEnabled(boolean enabled) {
        binding.etUsername.setEnabled(enabled);
        binding.etPhone.setEnabled(enabled);
        binding.etEmail.setEnabled(enabled);
        binding.etPassword.setEnabled(enabled);
    }
    
    /**
     * 向用户显示增强用户体验的错误消息 - 实现需求4.3的错误显示
     * @param message 要显示的错误消息
     */
    private void showError(String message) {
        // 不清除字段特定的错误，让它们和全局错误消息同时显示
        // clearAllFieldErrors(); // 注释掉这行，保持字段级错误显示
        
        binding.tvMessage.setText(message);
        binding.tvMessage.setTextColor(getResources().getColor(com.medication.reminders.R.color.error_color, getTheme()));
        binding.tvMessage.setBackgroundResource(com.medication.reminders.R.drawable.error_message_background);
        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
        
        // 添加轻微的震动效果以吸引老年用户注意
        addShakeAnimation(binding.tvMessage);
        
        // 添加淡入动画以提供更好的用户体验
        binding.tvMessage.setAlpha(0f);
        binding.tvMessage.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
        
        // 确保消息具有无障碍功能
        binding.tvMessage.setContentDescription("错误提示: " + message);
        binding.tvMessage.announceForAccessibility("注册错误: " + message);
        
        // 移除自动隐藏逻辑，错误消息将持续显示直到用户有输入动作
    }
    
    /**
     * 向用户显示增强用户体验的成功消息 - 实现需求4.4的成功显示
     */
    private void showSuccess() {
        // 注册成功时清除所有字段错误
        clearAllFieldErrors();
        
        binding.tvMessage.setText("🎉 注册成功！欢迎使用药物提醒应用");
        binding.tvMessage.setTextColor(getResources().getColor(com.medication.reminders.R.color.success_color, getTheme()));
        binding.tvMessage.setBackgroundResource(com.medication.reminders.R.drawable.success_message_background);
        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
        
        // 添加成功的缩放动画效果
        addSuccessAnimation(binding.tvMessage);
        
        // 添加淡入动画以提供更好的用户体验
        binding.tvMessage.setAlpha(0f);
        binding.tvMessage.animate()
            .alpha(1f)
            .setDuration(500)
            .start();
        
        // 确保消息具有无障碍功能
        binding.tvMessage.setContentDescription("成功提示: 注册成功");
        binding.tvMessage.announceForAccessibility("恭喜您！注册成功，欢迎使用药物提醒应用");
        
        // 注册成功后清除输入字段
        clearInputFieldsWithAnimation();
        
        // 3秒后导航到主活动（给老年用户更多时间阅读成功消息）
        binding.tvMessage.postDelayed(() -> {
            navigateToMainActivity();
        }, 3000);
    }
    
    /**
     * Clear all field-specific error messages
     */
    private void clearAllFieldErrors() {
        binding.tilUsername.setError(null);
        binding.tilPhone.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
    }
    
    /**
     * Hide message TextView
     */
    private void hideMessage() {
        binding.tvMessage.setVisibility(android.view.View.GONE);
        binding.tvMessage.setText("");
    }
    
    /**
     * Clear all input fields
     */
    private void clearInputFields() {
        binding.etUsername.setText("");
        binding.etPhone.setText("");
        binding.etEmail.setText("");
        binding.etPassword.setText("");
    }
    
    /**
     * 注册成功后导航到主活动
     */
    private void navigateToMainActivity() {
        // 显示友好的提示消息
        android.widget.Toast.makeText(this, "注册完成！正在为您准备应用界面...", android.widget.Toast.LENGTH_LONG).show();
        
        // 在实际应用中，这里会导航到主药物提醒界面
        // Intent intent = new Intent(this, MainActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        
        // 目前只是结束注册活动
        finish();
    }
    
    /**
     * 为消息添加震动动画效果
     */
    private void addShakeAnimation(android.view.View view) {
        android.view.animation.Animation shake = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        view.startAnimation(shake);
    }
    
    /**
     * 为成功消息添加缩放动画效果
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
     * 带动画效果地隐藏消息
     */
    private void hideMessageWithAnimation() {
        if (binding != null && binding.tvMessage != null) {
            binding.tvMessage.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    binding.tvMessage.setVisibility(android.view.View.GONE);
                    binding.tvMessage.setText("");
                })
                .start();
        }
    }
    
    /**
     * 带动画效果地清除所有输入字段
     */
    private void clearInputFieldsWithAnimation() {
        // 为每个输入字段添加淡出效果
        binding.etUsername.animate().alpha(0.5f).setDuration(200).withEndAction(() -> {
            binding.etUsername.setText("");
            binding.etUsername.animate().alpha(1.0f).setDuration(200).start();
        }).start();
        
        binding.etPhone.animate().alpha(0.5f).setDuration(200).withEndAction(() -> {
            binding.etPhone.setText("");
            binding.etPhone.animate().alpha(1.0f).setDuration(200).start();
        }).start();
        
        binding.etEmail.animate().alpha(0.5f).setDuration(200).withEndAction(() -> {
            binding.etEmail.setText("");
            binding.etEmail.animate().alpha(1.0f).setDuration(200).start();
        }).start();
        
        binding.etPassword.animate().alpha(0.5f).setDuration(200).withEndAction(() -> {
            binding.etPassword.setText("");
            binding.etPassword.animate().alpha(1.0f).setDuration(200).start();
        }).start();
    }
    
    /**
     * Validate all fields before allowing registration
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateAllFields() {
        String username = getInputText(binding.etUsername);
        String phone = getInputText(binding.etPhone);
        String email = getInputText(binding.etEmail);
        String password = getInputText(binding.etPassword);
        
        boolean isValid = true;
        
        // Validate username
        if (username.isEmpty()) {
            binding.tilUsername.setError("请输入用户名");
            isValid = false;
        } else {
            UserValidator.ValidationResult result = UserValidator.validateUsername(username);
            if (!result.isValid()) {
                binding.tilUsername.setError(result.getErrorMessage());
                isValid = false;
            } else {
                binding.tilUsername.setError(null);
            }
        }
        
        // Validate phone
        if (phone.isEmpty()) {
            binding.tilPhone.setError("请输入手机号");
            isValid = false;
        } else {
            UserValidator.ValidationResult result = UserValidator.validatePhoneNumber(phone);
            if (!result.isValid()) {
                binding.tilPhone.setError(result.getErrorMessage());
                isValid = false;
            } else {
                binding.tilPhone.setError(null);
            }
        }
        
        // Validate email
        if (email.isEmpty()) {
            binding.tilEmail.setError("请输入邮箱");
            isValid = false;
        } else {
            UserValidator.ValidationResult result = UserValidator.validateEmail(email);
            if (!result.isValid()) {
                binding.tilEmail.setError(result.getErrorMessage());
                isValid = false;
            } else {
                binding.tilEmail.setError(null);
            }
        }
        
        // Validate password
        if (password.isEmpty()) {
            binding.tilPassword.setError("请输入密码");
            isValid = false;
        } else {
            UserValidator.ValidationResult result = UserValidator.validatePassword(password);
            if (!result.isValid()) {
                binding.tilPassword.setError(result.getErrorMessage());
                isValid = false;
            } else {
                binding.tilPassword.setError(null);
            }
        }
        
        return isValid;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除所有待处理的回调以防止内存泄漏
        if (binding != null && binding.tvMessage != null) {
            binding.tvMessage.removeCallbacks(null);
        }
        
        // 清理防抖处理器
        if (debounceHandler != null) {
            debounceHandler.removeCallbacksAndMessages(null);
            debounceHandler = null;
        }
        
        // 清理绑定引用
        binding = null;
        viewModel = null;
    }
}