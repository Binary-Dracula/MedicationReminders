package com.medication.reminders;

import android.os.Bundle;
import android.content.Intent;
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
                getSupportActionBar().setTitle("用户注册");
            }
            
            // 初始化ViewModel
            UserRepository userRepository = new UserRepository(this);
            viewModel = new RegisterViewModel(userRepository);
            android.util.Log.d("RegisterActivity", "ViewModel initialized");
            
            // 设置UI
            setupUI();
            setupObservers();
            
            android.util.Log.d("RegisterActivity", "onCreate completed successfully");
        } catch (Exception e) {
            android.util.Log.e("RegisterActivity", "Error in onCreate", e);
            // 如果出错，显示错误信息但不崩溃
            android.widget.Toast.makeText(this, "初始化失败: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }
    
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
    
    private void setupInputFieldListeners() {
        // 简化的输入监听器，只清除错误信息
        binding.etUsername.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilUsername.setError(null);
                binding.tvMessage.setVisibility(android.view.View.GONE);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        binding.etPhone.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPhone.setError(null);
                binding.tvMessage.setVisibility(android.view.View.GONE);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        binding.etEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmail.setError(null);
                binding.tvMessage.setVisibility(android.view.View.GONE);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        binding.etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
                binding.tvMessage.setVisibility(android.view.View.GONE);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    
    private void setupObservers() {
        // 观察加载状态 - 添加状态跟踪避免初始化时的误触发
        viewModel.getIsLoading().observe(this, isLoading -> {
            // 只有在明确设置了loading状态时才处理，避免初始化时的null或false值触发跳转
            if (isLoading != null && isLoading) {
                updateLoadingState(true);
            } else if (isLoading != null && !isLoading && hasUserInteracted()) {
                // 只有在用户已经交互过（点击了注册按钮）后才处理loading结束
                updateLoadingState(false);
            }
        });
        
        // 观察错误消息
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showError(errorMessage);
            }
        });
        
        // 观察注册成功状态
        viewModel.getRegisterSuccess().observe(this, success -> {
            if (success != null && success) {
                // 注册成功，loading状态会自动处理跳转
            }
        });
    }
    
    // 添加用户交互状态跟踪
    private boolean userHasInteracted = false;
    
    private boolean hasUserInteracted() {
        return userHasInteracted;
    }
    
    private void handleRegisterClick() {
        // 标记用户已经交互
        userHasInteracted = true;
        
        // 验证所有字段
        if (!validateAllFields()) {
            showError("请检查并修正上述输入错误");
            return;
        }
        
        // 获取输入值
        String username = getInputText(binding.etUsername);
        String phoneNumber = getInputText(binding.etPhone);
        String email = getInputText(binding.etEmail);
        String password = getInputText(binding.etPassword);
        
        // 调用ViewModel处理注册
        viewModel.registerUser(username, phoneNumber, email, password);
    }
    
    private String getInputText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
    
    private boolean validateAllFields() {
        String username = getInputText(binding.etUsername);
        String phone = getInputText(binding.etPhone);
        String email = getInputText(binding.etEmail);
        String password = getInputText(binding.etPassword);
        
        boolean isValid = true;
        
        // 验证用户名
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
        
        // 验证手机号
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
        
        // 验证邮箱
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
        
        // 验证密码
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
            // 隐藏loading遮罩并跳转
            binding.loadingOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    binding.loadingOverlay.setVisibility(android.view.View.GONE);
                    navigateToLogin();
                })
                .start();
            
            // 重新启用按钮和输入字段
            binding.btnRegister.setEnabled(true);
            setInputFieldsEnabled(true);
        }
    }
    
    private void setInputFieldsEnabled(boolean enabled) {
        binding.etUsername.setEnabled(enabled);
        binding.etPhone.setEnabled(enabled);
        binding.etEmail.setEnabled(enabled);
        binding.etPassword.setEnabled(enabled);
    }
    
    private void showError(String message) {
        binding.tvMessage.setText(message);
        binding.tvMessage.setTextColor(getResources().getColor(R.color.error_color, getTheme()));
        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        viewModel = null;
    }
}