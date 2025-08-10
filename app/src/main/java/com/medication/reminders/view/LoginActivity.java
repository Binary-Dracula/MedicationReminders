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
import com.medication.reminders.databinding.ActivityLoginBinding;
import com.medication.reminders.viewmodels.UserViewModel;
import com.medication.reminders.utils.ExactAlarmPermissionHelper;

/**
 * LoginActivity 类，用于处理用户登录界面。
 * 继承自 AppCompatActivity，并使用 ViewBinding 进行 UI 管理。
 * 采用 MVVM 模式，配合 UserViewModel 进行业务逻辑处理。
 * 重构说明：
 * - 移除对 UserRepository 的直接依赖
 * - 使用 UserViewModel 处理登录逻辑
 * - 观察 loginStatus LiveData 更新 UI
 * - 实现"记住我"功能界面
 */
public class LoginActivity extends AppCompatActivity {
    
    // ViewBinding 实例，用于访问 UI 组件
    private ActivityLoginBinding binding;
    
    // UserViewModel 实例，用于处理业务逻辑
    private UserViewModel userViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化 ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 创建 UserViewModel 实例
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        
        // 设置布局和基础 UI
        setupUI();
        
        // 观察 ViewModel 的 LiveData
        observeViewModel();
        
        // 在应用启动时检查是否需要自动登录
        checkAutoLogin();
    }
    
    /**
     * 设置基础 UI 组件及其初始状态
     */
    private void setupUI() {
        // 设置标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("用户登录");
        }
        
        // 设置初始可见性状态
        binding.tvMessage.setVisibility(android.view.View.GONE);
        
        // 设置点击监听器
        setupClickListeners();
    }
    
    /**
     * 为 UI 组件设置点击监听器
     * 实现所有与登录功能相关的用户交互逻辑
     */
    private void setupClickListeners() {
        // 登录按钮点击监听器 - 处理登录流程
        binding.btnLogin.setOnClickListener(v -> {
            handleLoginClick();
        });
        
        // "记住我"复选框监听器 - 处理"记住我"状态变更
        binding.cbRememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 记住我状态由 UserViewModel 内部管理
            // 这里只需要清除错误消息以提升用户体验
            userViewModel.clearErrorMessage();
        });
        
        // 注册链接点击监听器 - 导航到注册页面
        binding.tvRegisterLink.setOnClickListener(v -> {
            navigateToRegister();
        });
        
        // 添加输入框监听器以提升用户体验
        setupInputFieldListeners();
    }
    
    /**
     * 观察 UserViewModel 的 LiveData 变化并相应地更新 UI
     */
    private void observeViewModel() {
        // 观察登录状态消息
        userViewModel.getLoginStatus().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String loginStatus) {
                if (loginStatus != null && !loginStatus.isEmpty()) {
                    if (loginStatus.contains("成功")) {
                        showSuccess(loginStatus);
                    } else {
                        showError(loginStatus);
                    }
                } else {
                    hideMessage();
                }
            }
        });
        
        // 观察登录成功状态
        userViewModel.getLoginSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loginSuccess) {
                if (loginSuccess != null && loginSuccess) {
                    // 短暂延迟后导航到主活动，以便用户看到成功消息
                    binding.getRoot().postDelayed(() -> {
                        navigateToMain();
                    }, 2000); // 为老年用户设置 2 秒延迟以阅读消息
                }
            }
        });
        
        // 观察加载状态
        userViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading != null && isLoading) {
                    showLoading();
                } else {
                    hideLoading();
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
                }
            }
        });
    }
    
    /**
     * 在 UI 中显示错误消息，并为老年用户提供增强的用户体验
     * @param message 要显示的错误消息
     */
    private void showError(String message) {
        binding.tvMessage.setText(message);
        binding.tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        binding.tvMessage.setBackgroundResource(R.drawable.error_message_background);
        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
        
        // 添加淡入动画以改善用户体验
        binding.tvMessage.setAlpha(0f);
        binding.tvMessage.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
        
        // 为老年用户添加无障碍播报
        binding.tvMessage.setContentDescription("错误提示: " + message);
        binding.tvMessage.announceForAccessibility("登录错误: " + message);
        
        // 添加抖动动画以吸引注意力
        addShakeAnimation(binding.tvMessage);
    }
    
    /**
     * 在 UI 中显示成功消息，并为老年用户提供增强的用户体验
     * @param message 要显示的成功消息
     */
    private void showSuccess(String message) {
        binding.tvMessage.setText("🎉 " + message);
        binding.tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        binding.tvMessage.setBackgroundResource(R.drawable.success_message_background);
        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
        
        // 添加淡入动画以改善用户体验
        binding.tvMessage.setAlpha(0f);
        binding.tvMessage.animate()
            .alpha(1f)
            .setDuration(500)
            .start();
        
        // 为老年用户添加无障碍播报
        binding.tvMessage.setContentDescription("成功提示: " + message);
        binding.tvMessage.announceForAccessibility("恭喜您！" + message);
        
        // 添加成功动画效果
        addSuccessAnimation(binding.tvMessage);
    }
    
    /**
     * 隐藏消息 TextView
     */
    private void hideMessage() {
        binding.tvMessage.setVisibility(android.view.View.GONE);
    }
    
    /**
     * 显示加载指示器并禁用登录按钮
     */
    private void showLoading() {
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("登录中...");
    }
    
    /**
     * 隐藏加载指示器并启用登录按钮
     */
    private void hideLoading() {
        binding.btnLogin.setEnabled(true);
        binding.btnLogin.setText("立即登录");
    }
    
    /**
     * 处理登录按钮点击事件 - 获取输入并调用 UserViewModel
     */
    private void handleLoginClick() {
        // 清除之前的任何错误消息
        userViewModel.clearErrorMessage();
        userViewModel.clearFormValidationError();
        
        // 从 EditTexts 获取输入值
        String username = getInputText(binding.etUsername);
        String password = getInputText(binding.etPassword);
        boolean rememberMe = binding.cbRememberMe.isChecked();
        
        // 调用 UserViewModel 处理登录流程
        userViewModel.loginUser(username, password, rememberMe);
    }
    
    /**
     * 安全地从 EditText 获取文本
     * @param editText 要从中获取文本的 EditText
     * @return 字符串文本，如果为 null 则返回空字符串
     */
    private String getInputText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
    
    /**
     * 设置输入框监听器以增强用户体验
     * 当用户开始输入时清除错误消息
     */
    private void setupInputFieldListeners() {
        // 用户名输入框监听器
        binding.etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 当用户开始输入时清除错误消息
                userViewModel.clearErrorMessage();
                userViewModel.clearFormValidationError();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // 密码输入框监听器
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 当用户开始输入时清除错误消息
                userViewModel.clearErrorMessage();
                userViewModel.clearFormValidationError();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    /**
     * 导航到注册活动
     */
    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        
        // 添加过渡动画以改善用户体验
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    
    /**
     * 登录成功后导航到主活动
     */
    private void navigateToMain() {
        // 向用户显示友好消息
        Toast.makeText(this, "登录成功！正在为您准备应用界面...", Toast.LENGTH_LONG).show();
        
        // 为 MainActivity 创建 Intent
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        // 将用户信息添加到 Intent 以提供个性化体验
        String username = getInputText(binding.etUsername);
        intent.putExtra("username", username);
        intent.putExtra("login_time", System.currentTimeMillis());
        
        startActivity(intent);
        
        // 添加平滑的过渡动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        
        // 结束登录活动
        finish();
    }
    
    /**
     * 为错误消息添加抖动动画效果
     * @param view 要应用动画的视图
     */
    private void addShakeAnimation(android.view.View view) {
        android.view.animation.Animation shake = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        view.startAnimation(shake);
    }
    
    /**
     * 为成功消息添加成功动画效果
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
     * 在应用启动时检查自动登录
     */
    private void checkAutoLogin() {
        // 检查是否有被记住的用户需要自动登录
        userViewModel.checkRememberedUser();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理 ViewBinding 引用以防止内存泄漏
        binding = null;
        
        // 清理 ViewModel 引用
        if (userViewModel != null) {
            // 清除 ViewModel 中的任何挂起操作
            userViewModel.clearErrorMessage();
            userViewModel.clearSuccessMessage();
            userViewModel.resetLoginStatus();
            userViewModel = null;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // 当应用进入后台时，从内存中清除敏感信息
        // 这对于老年用户的安全至关重要
        if (binding != null && binding.etPassword != null) {
            // 为安全起见清除密码字段（但保留用户名以提供良好用户体验）
            // 仅在未选中"记住我"时清除
            if (!binding.cbRememberMe.isChecked()) {
                binding.etPassword.setText("");
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // 返回此活动时刷新 UI 状态
        if (userViewModel != null) {
            // 重置任何可能过时的错误状态
            userViewModel.clearErrorMessage();
            userViewModel.clearSuccessMessage();
            userViewModel.resetLoginStatus();
        }
    }
}