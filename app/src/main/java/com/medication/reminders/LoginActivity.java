package com.medication.reminders;

import android.os.Bundle;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.medication.reminders.databinding.ActivityLoginBinding;

/**
 * LoginActivity class for handling user login interface
 * Extends AppCompatActivity and uses ViewBinding for UI management
 * Implements MVVM pattern with LoginViewModel
 */
public class LoginActivity extends AppCompatActivity {
    
    // ViewBinding instance for accessing UI components
    private ActivityLoginBinding binding;
    
    // LoginViewModel instance for handling business logic
    private LoginViewModel loginViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Create LoginViewModel instance
        UserRepository userRepository = new UserRepository(this);
        loginViewModel = new LoginViewModel(userRepository);
        
        // Set up the layout and basic UI
        setupUI();
        
        // Observe ViewModel LiveData
        observeViewModel();
        
        // Load saved credentials if available
        loginViewModel.loadSavedCredentials();
        
        // Check for auto-login on app startup
        checkAutoLogin();
    }
    
    /**
     * Set up the basic UI components and their initial states
     */
    private void setupUI() {
        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("用户登录");
        }
        
        // Set initial visibility states
        binding.progressBar.setVisibility(android.view.View.GONE);
        binding.tvMessage.setVisibility(android.view.View.GONE);
        
        // Set up click listeners (will be implemented in next task)
        setupClickListeners();
    }
    
    /**
     * Set up click listeners for UI components
     * Implements all user interaction logic for login functionality
     */
    private void setupClickListeners() {
        // Login button click listener - handles login process
        binding.btnLogin.setOnClickListener(v -> {
            handleLoginClick();
        });
        
        // Remember me checkbox listener - handles remember me state changes
        binding.cbRememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loginViewModel.setRememberMe(isChecked);
            
            // If unchecked, clear saved credentials immediately
            if (!isChecked) {
                loginViewModel.clearSavedCredentials();
            }
        });
        
        // Register link click listener - navigates to registration page
        binding.tvRegisterLink.setOnClickListener(v -> {
            navigateToRegister();
        });
        
        // Add input field listeners for better UX
        setupInputFieldListeners();
    }
    
    /**
     * Observe LoginViewModel LiveData changes and update UI accordingly
     */
    private void observeViewModel() {
        // Observe error messages
        loginViewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    showError(errorMessage);
                } else {
                    hideMessage();
                }
            }
        });
        
        // Observe loading state
        loginViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading != null && isLoading) {
                    showLoading();
                } else {
                    hideLoading();
                }
            }
        });
        
        // Observe login success state
        loginViewModel.getLoginSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loginSuccess) {
                if (loginSuccess != null && loginSuccess) {
                    showSuccess("登录成功");
                    
                    // Navigate to main activity after a short delay to let user see success message
                    binding.getRoot().postDelayed(() -> {
                        navigateToMain();
                    }, 2000); // 2 second delay for elderly users to read the message
                }
            }
        });
        
        // Observe remember me state
        loginViewModel.getRememberMe().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean rememberMe) {
                if (rememberMe != null) {
                    binding.cbRememberMe.setChecked(rememberMe);
                }
            }
        });
        
        // Observe saved username
        loginViewModel.getSavedUsername().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String savedUsername) {
                if (savedUsername != null && !savedUsername.isEmpty()) {
                    binding.etUsername.setText(savedUsername);
                }
            }
        });
        
        // Observe saved password
        loginViewModel.getSavedPassword().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String savedPassword) {
                if (savedPassword != null && !savedPassword.isEmpty()) {
                    binding.etPassword.setText(savedPassword);
                }
            }
        });
    }
    
    /**
     * Show error message in the UI with enhanced UX for elderly users
     * @param message Error message to display
     */
    private void showError(String message) {
        binding.tvMessage.setText(message);
        binding.tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        binding.tvMessage.setBackgroundResource(R.drawable.error_message_background);
        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
        
        // Add fade-in animation for better user experience
        binding.tvMessage.setAlpha(0f);
        binding.tvMessage.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
        
        // Add accessibility announcement for elderly users
        binding.tvMessage.setContentDescription("错误提示: " + message);
        binding.tvMessage.announceForAccessibility("登录错误: " + message);
        
        // Add shake animation to draw attention
        addShakeAnimation(binding.tvMessage);
    }
    
    /**
     * Show success message in the UI with enhanced UX for elderly users
     * @param message Success message to display
     */
    private void showSuccess(String message) {
        binding.tvMessage.setText("🎉 " + message);
        binding.tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        binding.tvMessage.setBackgroundResource(R.drawable.success_message_background);
        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
        
        // Add fade-in animation for better user experience
        binding.tvMessage.setAlpha(0f);
        binding.tvMessage.animate()
            .alpha(1f)
            .setDuration(500)
            .start();
        
        // Add accessibility announcement for elderly users
        binding.tvMessage.setContentDescription("成功提示: " + message);
        binding.tvMessage.announceForAccessibility("恭喜您！" + message);
        
        // Add success animation effect
        addSuccessAnimation(binding.tvMessage);
    }
    
    /**
     * Hide message TextView
     */
    private void hideMessage() {
        binding.tvMessage.setVisibility(android.view.View.GONE);
    }
    
    /**
     * Show loading indicator and disable login button
     */
    private void showLoading() {
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("登录中...");
    }
    
    /**
     * Hide loading indicator and enable login button
     */
    private void hideLoading() {
        binding.progressBar.setVisibility(android.view.View.GONE);
        binding.btnLogin.setEnabled(true);
        binding.btnLogin.setText("立即登录");
    }
    
    /**
     * Handle login button click - get input and call LoginViewModel
     * Implements requirements 5.4, 5.5, 5.6, 5.7, 5.8
     */
    private void handleLoginClick() {
        // Clear any previous error messages
        loginViewModel.clearErrorMessage();
        
        // Get input values from EditTexts
        String username = getInputText(binding.etUsername);
        String password = getInputText(binding.etPassword);
        boolean rememberMe = binding.cbRememberMe.isChecked();
        
        // Call ViewModel to handle login process
        loginViewModel.loginUser(username, password, rememberMe);
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
     * Setup input field listeners for enhanced user experience
     * Clears error messages when user starts typing
     */
    private void setupInputFieldListeners() {
        // Username field listener
        binding.etUsername.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error message when user starts typing
                loginViewModel.clearErrorMessage();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        // Password field listener
        binding.etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error message when user starts typing
                loginViewModel.clearErrorMessage();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    
    /**
     * Navigate to register activity
     * Implements requirement 7.7 for navigation between login and register
     */
    private void navigateToRegister() {
        android.content.Intent intent = new android.content.Intent(this, RegisterActivity.class);
        startActivity(intent);
        
        // Add transition animation for better user experience
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    
    /**
     * Navigate to main activity after successful login
     * Implements navigation logic after login success
     */
    private void navigateToMain() {
        // Show friendly message to user
        android.widget.Toast.makeText(this, "登录成功！正在为您准备应用界面...", android.widget.Toast.LENGTH_LONG).show();
        
        // Create intent for MainActivity (placeholder for now)
        android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        // Add user information to intent for personalized experience
        String username = getInputText(binding.etUsername);
        intent.putExtra("username", username);
        intent.putExtra("login_time", System.currentTimeMillis());
        
        startActivity(intent);
        
        // Add smooth transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        
        // Finish login activity
        finish();
    }
    
    /**
     * Add shake animation effect for error messages
     * @param view View to animate
     */
    private void addShakeAnimation(android.view.View view) {
        android.view.animation.Animation shake = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        view.startAnimation(shake);
    }
    
    /**
     * Add success animation effect for success messages
     * @param view View to animate
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
     * Check for automatic login on app startup with performance optimization
     * Implements requirement 8.4 for auto-login functionality
     */
    private void checkAutoLogin() {
        // Check auto-login in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                // Get saved credentials
                UserRepository userRepository = new UserRepository(this);
                UserRepository.SavedCredentials credentials = userRepository.getSavedCredentials();
                
                if (credentials != null && credentials.getUsername() != null && credentials.getPassword() != null) {
                    // Post to main thread for UI updates
                    runOnUiThread(() -> {
                        // Show auto-login message to user
                        showAutoLoginMessage();
                        
                        // Perform automatic login after a short delay
                        binding.getRoot().postDelayed(() -> {
                            performAutoLogin(credentials.getUsername(), credentials.getPassword());
                        }, 1500); // 1.5 second delay to show the message
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                // If auto-login fails, just continue with normal login flow
                // No UI update needed as user can proceed normally
            }
        }).start();
    }
    
    /**
     * Show auto-login message to inform user
     */
    private void showAutoLoginMessage() {
        binding.tvMessage.setText("检测到已保存的登录信息，正在为您自动登录...");
        binding.tvMessage.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        binding.tvMessage.setBackgroundResource(R.drawable.message_background);
        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
        
        // Add fade-in animation
        binding.tvMessage.setAlpha(0f);
        binding.tvMessage.animate()
            .alpha(1f)
            .setDuration(500)
            .start();
        
        // Add accessibility announcement
        binding.tvMessage.announceForAccessibility("正在为您自动登录，请稍候");
    }
    
    /**
     * Perform automatic login with saved credentials
     * @param username Saved username
     * @param password Saved password
     */
    private void performAutoLogin(String username, String password) {
        // Fill in the credentials (for user visibility)
        binding.etUsername.setText(username);
        binding.etPassword.setText(password);
        binding.cbRememberMe.setChecked(true);
        
        // Perform login
        loginViewModel.loginUser(username, password, true);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up ViewBinding reference to prevent memory leaks
        binding = null;
        
        // Clean up ViewModel reference
        if (loginViewModel != null) {
            // Clear any pending operations in ViewModel
            loginViewModel.clearErrorMessage();
            loginViewModel.resetLoginSuccess();
            loginViewModel = null;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Clear sensitive information from memory when app goes to background
        // This is important for elderly users' security
        if (binding != null && binding.etPassword != null) {
            // Clear password field for security (but keep username for UX)
            // Only clear if "remember me" is not checked
            if (!binding.cbRememberMe.isChecked()) {
                binding.etPassword.setText("");
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Refresh UI state when returning to the activity
        if (loginViewModel != null) {
            // Reset any error states that might be stale
            loginViewModel.clearErrorMessage();
            loginViewModel.resetLoginSuccess();
        }
    }
}