package com.medication.reminders;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * LoginViewModel class for handling login business logic
 * Manages login UI state and "remember me" functionality
 */
public class LoginViewModel extends ViewModel {
    
    // LiveData properties for UI state management
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<Boolean> loginSuccess;
    private MutableLiveData<Boolean> rememberMe;
    private MutableLiveData<String> savedUsername;
    private MutableLiveData<String> savedPassword;
    
    // UserRepository dependency
    private UserRepository userRepository;
    
    // Flag for testing to enable synchronous behavior
    private boolean isTestMode = false;
    
    /**
     * Constructor - initializes LiveData properties and UserRepository
     * @param userRepository UserRepository instance for data operations
     */
    public LoginViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        
        // Initialize LiveData properties
        this.errorMessage = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
        this.loginSuccess = new MutableLiveData<>(false);
        this.rememberMe = new MutableLiveData<>(false);
        this.savedUsername = new MutableLiveData<>("");
        this.savedPassword = new MutableLiveData<>("");
    }
    
    /**
     * Constructor for testing with synchronous behavior
     * @param userRepository UserRepository instance for data operations
     * @param isTestMode Whether to run in test mode (synchronous)
     */
    public LoginViewModel(UserRepository userRepository, boolean isTestMode) {
        this(userRepository);
        this.isTestMode = isTestMode;
    }
    
    // Getter methods for LiveData properties
    
    /**
     * Get error message LiveData
     * @return MutableLiveData<String> for error messages
     */
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Get loading state LiveData
     * @return MutableLiveData<Boolean> for loading state
     */
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Get login success state LiveData
     * @return MutableLiveData<Boolean> for login success state
     */
    public MutableLiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }
    
    /**
     * Get remember me state LiveData
     * @return MutableLiveData<Boolean> for remember me checkbox state
     */
    public MutableLiveData<Boolean> getRememberMe() {
        return rememberMe;
    }
    
    /**
     * Get saved username LiveData
     * @return MutableLiveData<String> for saved username
     */
    public MutableLiveData<String> getSavedUsername() {
        return savedUsername;
    }
    
    /**
     * Get saved password LiveData
     * @return MutableLiveData<String> for saved password
     */
    public MutableLiveData<String> getSavedPassword() {
        return savedPassword;
    }
    
    /**
     * Set remember me state
     * @param remember Boolean value for remember me checkbox
     */
    public void setRememberMe(boolean remember) {
        this.rememberMe.setValue(remember);
    }
    
    /**
     * Clear error message
     */
    public void clearErrorMessage() {
        this.errorMessage.setValue(null);
    }
    
    /**
     * Reset login success state
     */
    public void resetLoginSuccess() {
        this.loginSuccess.setValue(false);
    }
    
    /**
     * Handle user login process with performance optimizations
     * @param username Username input
     * @param password Password input
     * @param rememberMe Whether to remember login credentials
     */
    public void loginUser(String username, String password, boolean rememberMe) {
        // Set loading state
        isLoading.setValue(true);
        clearErrorMessage();
        
        if (isTestMode) {
            // Synchronous execution for testing
            performLoginSync(username, password, rememberMe);
        } else {
            // Asynchronous execution for production
            performLoginAsync(username, password, rememberMe);
        }
    }
    
    /**
     * Synchronous login for testing
     */
    private void performLoginSync(String username, String password, boolean rememberMe) {
        try {
            // Validate login input (quick validation first)
            String validationError = validateLoginInput(username, password);
            if (validationError != null) {
                isLoading.setValue(false);
                errorMessage.setValue(validationError);
                return;
            }
            
            // Trim username once for consistency
            String trimmedUsername = username.trim();
            
            // Check login attempts limit before authentication
            int attempts = userRepository.getLoginAttempts(trimmedUsername);
            if (attempts >= 3) {
                isLoading.setValue(false);
                errorMessage.setValue("登录失败次数过多，请稍后再试");
                return;
            }
            
            // Authenticate user
            UserRepository.AuthenticationResult result = userRepository.authenticateUser(trimmedUsername, password);
            
            if (result.isSuccess()) {
                // Login successful
                loginSuccess.setValue(true);
                
                // Handle "remember me" functionality
                if (rememberMe) {
                    boolean saveSuccess = userRepository.saveLoginCredentials(trimmedUsername, password);
                    if (saveSuccess) {
                        this.rememberMe.setValue(true);
                    }
                } else {
                    userRepository.clearSavedCredentials();
                    this.rememberMe.setValue(false);
                }
                
                errorMessage.setValue(result.getMessage());
            } else {
                // Login failed
                loginSuccess.setValue(false);
                errorMessage.setValue(result.getMessage());
            }
            
            isLoading.setValue(false);
            
        } catch (Exception e) {
            e.printStackTrace();
            loginSuccess.setValue(false);
            errorMessage.setValue("登录失败，请重试");
            isLoading.setValue(false);
        }
    }
    
    /**
     * Asynchronous login for production
     */
    private void performLoginAsync(String username, String password, boolean rememberMe) {
        // Use background thread for authentication to avoid UI blocking
        new Thread(() -> {
            try {
                // Validate login input (quick validation first)
                String validationError = validateLoginInput(username, password);
                if (validationError != null) {
                    // Post to main thread for UI updates
                    postToMainThread(() -> {
                        isLoading.setValue(false);
                        errorMessage.setValue(validationError);
                    });
                    return;
                }
                
                // Trim username once for consistency
                String trimmedUsername = username.trim();
                
                // Check login attempts limit before authentication
                int attempts = userRepository.getLoginAttempts(trimmedUsername);
                if (attempts >= 3) {
                    postToMainThread(() -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("登录失败次数过多，请稍后再试");
                    });
                    return;
                }
                
                // Authenticate user (potentially time-consuming operation)
                UserRepository.AuthenticationResult result = userRepository.authenticateUser(trimmedUsername, password);
                
                // Post results to main thread
                postToMainThread(() -> {
                    if (result.isSuccess()) {
                        // Login successful
                        loginSuccess.setValue(true);
                        
                        // Handle "remember me" functionality
                        if (rememberMe) {
                            // Save credentials in background to avoid UI delay
                            new Thread(() -> {
                                boolean saveSuccess = userRepository.saveLoginCredentials(trimmedUsername, password);
                                postToMainThread(() -> {
                                    if (saveSuccess) {
                                        this.rememberMe.setValue(true);
                                    }
                                });
                            }).start();
                        } else {
                            // Clear saved credentials if "remember me" is not checked
                            new Thread(() -> {
                                userRepository.clearSavedCredentials();
                                postToMainThread(() -> {
                                    this.rememberMe.setValue(false);
                                });
                            }).start();
                        }
                        
                        errorMessage.setValue(result.getMessage());
                    } else {
                        // Login failed
                        loginSuccess.setValue(false);
                        errorMessage.setValue(result.getMessage());
                    }
                    
                    isLoading.setValue(false);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                postToMainThread(() -> {
                    loginSuccess.setValue(false);
                    errorMessage.setValue("登录失败，请重试");
                    isLoading.setValue(false);
                });
            }
        }).start();
    }
    
    /**
     * Helper method to post operations to main thread
     * @param runnable Operation to run on main thread
     */
    private void postToMainThread(Runnable runnable) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(runnable);
    }
    
    /**
     * Load saved login credentials from storage with performance optimization
     */
    public void loadSavedCredentials() {
        if (isTestMode) {
            // Synchronous execution for testing
            loadSavedCredentialsSync();
        } else {
            // Asynchronous execution for production
            loadSavedCredentialsAsync();
        }
    }
    
    /**
     * Synchronous load for testing
     */
    private void loadSavedCredentialsSync() {
        try {
            UserRepository.SavedCredentials credentials = userRepository.getSavedCredentials();
            
            if (credentials != null && credentials.getUsername() != null && credentials.getPassword() != null) {
                savedUsername.setValue(credentials.getUsername());
                savedPassword.setValue(credentials.getPassword());
                rememberMe.setValue(true);
            } else {
                savedUsername.setValue("");
                savedPassword.setValue("");
                rememberMe.setValue(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            savedUsername.setValue("");
            savedPassword.setValue("");
            rememberMe.setValue(false);
        }
    }
    
    /**
     * Asynchronous load for production
     */
    private void loadSavedCredentialsAsync() {
        // Load credentials in background thread to avoid UI blocking
        new Thread(() -> {
            try {
                UserRepository.SavedCredentials credentials = userRepository.getSavedCredentials();
                
                // Post results to main thread for UI updates
                postToMainThread(() -> {
                    if (credentials != null && credentials.getUsername() != null && credentials.getPassword() != null) {
                        savedUsername.setValue(credentials.getUsername());
                        savedPassword.setValue(credentials.getPassword());
                        rememberMe.setValue(true);
                    } else {
                        savedUsername.setValue("");
                        savedPassword.setValue("");
                        rememberMe.setValue(false);
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                postToMainThread(() -> {
                    savedUsername.setValue("");
                    savedPassword.setValue("");
                    rememberMe.setValue(false);
                });
            }
        }).start();
    }
    
    /**
     * Clear saved login credentials from storage with performance optimization
     */
    public void clearSavedCredentials() {
        if (isTestMode) {
            // Synchronous execution for testing
            clearSavedCredentialsSync();
        } else {
            // Asynchronous execution for production
            clearSavedCredentialsAsync();
        }
    }
    
    /**
     * Synchronous clear for testing
     */
    private void clearSavedCredentialsSync() {
        try {
            boolean clearSuccess = userRepository.clearSavedCredentials();
            
            if (clearSuccess) {
                savedUsername.setValue("");
                savedPassword.setValue("");
                rememberMe.setValue(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Even if clearing fails, update UI to reflect user intent
            savedUsername.setValue("");
            savedPassword.setValue("");
            rememberMe.setValue(false);
        }
    }
    
    /**
     * Asynchronous clear for production
     */
    private void clearSavedCredentialsAsync() {
        // Clear credentials in background thread to avoid UI blocking
        new Thread(() -> {
            try {
                boolean clearSuccess = userRepository.clearSavedCredentials();
                
                // Post results to main thread for UI updates
                postToMainThread(() -> {
                    if (clearSuccess) {
                        savedUsername.setValue("");
                        savedPassword.setValue("");
                        rememberMe.setValue(false);
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                // Even if clearing fails, update UI to reflect user intent
                postToMainThread(() -> {
                    savedUsername.setValue("");
                    savedPassword.setValue("");
                    rememberMe.setValue(false);
                });
            }
        }).start();
    }
    
    /**
     * Validate login input fields with performance optimization
     * @param username Username input
     * @param password Password input
     * @return Error message if validation fails, null if validation passes
     */
    public String validateLoginInput(String username, String password) {
        // Early return for null checks to improve performance
        if (username == null && password == null) {
            return "请输入用户名和密码";
        }
        
        if (username == null || username.trim().isEmpty()) {
            return password == null || password.trim().isEmpty() ? "请输入用户名和密码" : "请输入用户名";
        }
        
        if (password == null || password.trim().isEmpty()) {
            return "请输入密码";
        }
        
        // Trim username once and reuse
        String trimmedUsername = username.trim();
        int usernameLength = trimmedUsername.length();
        int passwordLength = password.length();
        
        // Validate username length (3-20 characters) with single length check
        if (usernameLength < 3) {
            return "用户名至少需要3个字符";
        }
        if (usernameLength > 20) {
            return "用户名不能超过20个字符";
        }
        
        // Validate password length (6-20 characters) with single length check
        if (passwordLength < 6) {
            return "密码至少需要6个字符";
        }
        if (passwordLength > 20) {
            return "密码不能超过20个字符";
        }
        
        return null; // Validation passed
    }
}