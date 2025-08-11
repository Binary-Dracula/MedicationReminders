package com.medication.reminders.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.medication.reminders.R;
import com.medication.reminders.databinding.ActivityProfileEditBinding;
import com.medication.reminders.database.entity.User;
import com.medication.reminders.models.ProfileValidationResult;
import com.medication.reminders.utils.PermissionUtils;
import com.medication.reminders.utils.UserValidator;
import com.medication.reminders.viewmodels.UserViewModel;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * ProfileEditActivity - 个人资料编辑界面
 * 实现个人资料数据的编辑、验证、照片管理和保存功能
 * 遵循老年用户友好的设计原则和无障碍访问要求
 * 
 * 重构说明：
 * - 移除对 UserRepository 的直接依赖
 * - 使用 UserViewModel 保存资料更改
 * - 观察 profileUpdateStatus LiveData 更新 UI
 * - 在保存时进行数据验证
 */
public class ProfileEditActivity extends AppCompatActivity {
    
    private ActivityProfileEditBinding binding;
    private UserViewModel userViewModel;
    
    // 照片选择相关的ActivityResultLauncher
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    
    // 权限请求相关的ActivityResultLauncher
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncher;
    
    // 日期格式化器
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat displayDateFormatter;
    
    // 性别选项适配器
    private ArrayAdapter<String> genderAdapter;
    
    // 当前编辑的用户
    private User currentUser;
    
    // 标记是否有未保存的更改
    private boolean hasUnsavedChanges = false;
    
    // 日期选择对话框
    private DatePickerDialog datePickerDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置ViewBinding
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 初始化日期格式化器
        dateFormatter = new SimpleDateFormat(getString(R.string.date_format_storage), Locale.getDefault());
        displayDateFormatter = new SimpleDateFormat(getString(R.string.date_format_display), Locale.getDefault());
        
        // 初始化UserViewModel
        initializeViewModel();
        
        // 设置UI组件
        setupUI();
        
        // 设置观察者
        setupObservers();
        
        // 初始化ActivityResultLauncher
        setupActivityResultLaunchers();
        
        // 设置表单验证
        setupFormValidation();
    }
    
    /**
     * 初始化UserViewModel
     */
    private void initializeViewModel() {
        try {
            userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage(getString(R.string.initialization_failed));
            finish();
        }
    }
    
    /**
     * 设置UI组件
     */
    private void setupUI() {
        // 设置标题
        binding.tvTitle.setText(R.string.edit_profile_title);
        
        // 设置按钮点击事件
        binding.btnSave.setOnClickListener(v -> validateAndSaveProfile());
        binding.btnCancel.setOnClickListener(v -> handleCancelEdit());
        binding.btnChangePhoto.setOnClickListener(v -> showPhotoOptionsDialog());
        binding.btnRemovePhoto.setOnClickListener(v -> confirmRemovePhoto());
        
        // 设置出生日期选择
        binding.etBirthDate.setOnClickListener(v -> showDatePicker());
        binding.tilBirthDate.setEndIconOnClickListener(v -> showDatePicker());
        
        // 设置性别下拉选择
        setupGenderSpinner();
        
        // 初始状态设置
        showLoadingState(false);
        hideMessage();
        
        // 设置保存按钮初始状态为禁用
        binding.btnSave.setEnabled(false);
    }
    
    /**
     * 设置性别下拉选择器
     */
    private void setupGenderSpinner() {
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);
        genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderOptions);
        binding.actvGender.setAdapter(genderAdapter);
        
        // 设置性别选择监听器
        binding.actvGender.setOnItemClickListener((parent, view, position, id) -> {
            hasUnsavedChanges = true;
            validateForm();
        });
    }
    
    /**
     * 设置LiveData观察者
     */
    private void setupObservers() {
        // 观察当前用户数据
        userViewModel.getCurrentUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    currentUser = user;
                    populateFormFields(user);
                } else {
                    showErrorMessage(getString(R.string.profile_not_found));
                }
            }
        });
        
        // 观察加载状态
        userViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                showLoadingState(isLoading != null && isLoading);
            }
        });
        
        // 观察个人资料更新状态
        userViewModel.getProfileUpdateStatus().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String updateStatus) {
                if (updateStatus != null && !updateStatus.isEmpty()) {
                    if (updateStatus.contains(getString(R.string.profile_edit_success_contains))) {
                        showSuccessMessage(updateStatus);
                    } else {
                        showErrorMessage(updateStatus);
                    }
                }
            }
        });
        
        // 观察个人资料更新成功状态
        userViewModel.getProfileUpdateSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success != null && success) {
                    hasUnsavedChanges = false;
                    showSuccessMessage(getString(R.string.profile_saved_successfully));
                    
                    // 延迟返回上一页面
                    binding.getRoot().postDelayed(() -> {
                        setResult(RESULT_OK);
                        finish();
                    }, 1500);
                }
            }
        });
        
        // 观察错误消息
        userViewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                    showErrorMessage(errorMessage);
                }
            }
        });
        
        // 观察成功消息
        userViewModel.getSuccessMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String successMessage) {
                if (successMessage != null && !successMessage.trim().isEmpty()) {
                    showSuccessMessage(successMessage);
                }
            }
        });
        
        // 观察表单验证错误
        userViewModel.getFormValidationError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String validationError) {
                if (validationError != null && !validationError.isEmpty()) {
                    showErrorMessage(validationError);
                    // 同时在相应的输入字段显示错误
                    showFieldValidationErrors(validationError);
                }
            }
        });
    }
    
    /**
     * 设置ActivityResultLauncher
     */
    private void setupActivityResultLaunchers() {
        // 相机拍照结果处理
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get(getString(R.string.profile_photo_bitmap_key));
                        if (imageBitmap != null) {
                            // 将Bitmap保存为临时文件并更新个人资料照片
                            saveBitmapAndUpdatePhoto(imageBitmap);
                        }
                    }
                }
            }
        );
        
        // 相册选择结果处理
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        userViewModel.updateProfilePhoto(selectedImageUri);
                        hasUnsavedChanges = true;
                    }
                }
            }
        );
        
        // 相机权限请求结果处理
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    showErrorMessage(getString(R.string.camera_permission_required));
                }
            }
        );
        
        // 存储权限请求结果处理
        storagePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    showErrorMessage(getString(R.string.storage_permission_required));
                }
            }
        );
    }
    
    /**
     * 设置表单验证
     */
    private void setupFormValidation() {
        // 为所有输入字段添加文本变化监听器
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                hasUnsavedChanges = true;
                validateForm();
                // 清除ViewModel中的错误信息
                userViewModel.clearErrorMessage();
                userViewModel.clearFormValidationError();
            }
        };
        
        binding.etFullName.addTextChangedListener(textWatcher);
        binding.etUsername.addTextChangedListener(textWatcher);
        binding.etEmail.addTextChangedListener(textWatcher);
        binding.etPhoneNumber.addTextChangedListener(textWatcher);
        binding.etSecondaryPhone.addTextChangedListener(textWatcher);
        binding.etEmergencyContactName.addTextChangedListener(textWatcher);
        binding.etEmergencyContactPhone.addTextChangedListener(textWatcher);
        binding.etEmergencyContactRelation.addTextChangedListener(textWatcher);
        binding.etAddress.addTextChangedListener(textWatcher);
        binding.etBloodType.addTextChangedListener(textWatcher);
        binding.etAllergies.addTextChangedListener(textWatcher);
        binding.etMedicalConditions.addTextChangedListener(textWatcher);
        binding.etDoctorName.addTextChangedListener(textWatcher);
        binding.etDoctorPhone.addTextChangedListener(textWatcher);
        binding.etHospitalName.addTextChangedListener(textWatcher);
    }
    
    /**
     * 使用用户数据填充表单字段
     * @param user 用户对象
     */
    private void populateFormFields(User user) {
        if (user == null) {
            return;
        }
        
        // 填充基本信息字段
        binding.etFullName.setText(user.getFullName() != null ? user.getFullName() : "");
        binding.etUsername.setText(user.getUsername() != null ? user.getUsername() : "");
        binding.etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        binding.etPhoneNumber.setText(user.getPhone() != null ? user.getPhone() : "");

        // 填充扩展联系方式
        binding.etSecondaryPhone.setText(user.getSecondaryPhone() != null ? user.getSecondaryPhone() : "");
        binding.etEmergencyContactName.setText(user.getEmergencyContactName() != null ? user.getEmergencyContactName() : "");
        binding.etEmergencyContactPhone.setText(user.getEmergencyContactPhone() != null ? user.getEmergencyContactPhone() : "");
        binding.etEmergencyContactRelation.setText(user.getEmergencyContactRelation() != null ? user.getEmergencyContactRelation() : "");

        // 填充地址信息
        binding.etAddress.setText(user.getAddress() != null ? user.getAddress() : "");

        // 填充医疗相关信息
        binding.etBloodType.setText(user.getBloodType() != null ? user.getBloodType() : "");
        binding.etAllergies.setText(user.getAllergies() != null ? user.getAllergies() : "");
        binding.etMedicalConditions.setText(user.getMedicalConditions() != null ? user.getMedicalConditions() : "");
        binding.etDoctorName.setText(user.getDoctorName() != null ? user.getDoctorName() : "");
        binding.etDoctorPhone.setText(user.getDoctorPhone() != null ? user.getDoctorPhone() : "");
        binding.etHospitalName.setText(user.getHospitalName() != null ? user.getHospitalName() : "");
        
        // 设置性别选择
        String gender = user.getGender();
        if (gender != null && !gender.trim().isEmpty()) {
            binding.actvGender.setText(gender, false);
        }
        
        // 设置出生日期
        String birthDate = user.getBirthDate();
        if (birthDate != null && !birthDate.trim().isEmpty()) {
            try {
                Date date = dateFormatter.parse(birthDate);
                if (date != null) {
                    binding.etBirthDate.setText(displayDateFormatter.format(date));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                binding.etBirthDate.setText(birthDate);
            }
        }
        
        // 加载个人资料照片
        loadProfilePhoto(user.getProfilePhotoPath());
        
        // 重置未保存更改标记
        hasUnsavedChanges = false;
        
        // 验证表单
        validateForm();
    }
    
    /**
     * 验证表单并启用/禁用保存按钮
     */
    private void validateForm() {
        boolean isFormValid = true;
        
        // 清除之前的错误状态
        clearFieldErrors();
        
        // 验证完整姓名
        String fullName = binding.etFullName.getText().toString().trim();
        ProfileValidationResult nameResult = UserValidator.validateFullName(fullName);
        if (!nameResult.isValid()) {
            binding.tilFullName.setError(nameResult.getErrorMessage());
            isFormValid = false;
        }
        
        // 验证性别
        String gender = binding.actvGender.getText().toString().trim();
        ProfileValidationResult genderResult = UserValidator.validateGender(gender);
        if (!genderResult.isValid()) {
            binding.tilGender.setError(genderResult.getErrorMessage());
            isFormValid = false;
        }
        
        // 验证出生日期
        String birthDateDisplay = binding.etBirthDate.getText().toString().trim();
        String birthDate = convertDisplayDateToStorageFormat(birthDateDisplay);
        ProfileValidationResult birthDateResult = UserValidator.validateBirthDate(birthDate);
        if (!birthDateResult.isValid()) {
            binding.tilBirthDate.setError(birthDateResult.getErrorMessage());
            isFormValid = false;
        }
        
        // 验证用户名
        String username = binding.etUsername.getText().toString().trim();
        ProfileValidationResult usernameResult = UserValidator.validateUsername(username);
        if (!usernameResult.isValid()) {
            binding.tilUsername.setError(usernameResult.getErrorMessage());
            isFormValid = false;
        }
        
        // 验证邮箱
        String email = binding.etEmail.getText().toString().trim();
        ProfileValidationResult emailResult = UserValidator.validateEmail(email);
        if (!emailResult.isValid()) {
            binding.tilEmail.setError(emailResult.getErrorMessage());
            isFormValid = false;
        }
        
        // 验证电话号码
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim();
        ProfileValidationResult phoneResult = UserValidator.validatePhoneNumber(phoneNumber);
        if (!phoneResult.isValid()) {
            binding.tilPhoneNumber.setError(phoneResult.getErrorMessage());
            isFormValid = false;
        }

        // 验证扩展字段
        String secondaryPhone = binding.etSecondaryPhone.getText().toString().trim();
        ProfileValidationResult secondaryPhoneResult = UserValidator.validateSecondaryPhone(secondaryPhone);
        if (!secondaryPhoneResult.isValid()) {
            binding.tilSecondaryPhone.setError(secondaryPhoneResult.getErrorMessage());
            isFormValid = false;
        }

        String emergencyContactName = binding.etEmergencyContactName.getText().toString().trim();
        ProfileValidationResult emergencyContactNameResult = UserValidator.validateEmergencyContactName(emergencyContactName);
        if (!emergencyContactNameResult.isValid()) {
            binding.tilEmergencyContactName.setError(emergencyContactNameResult.getErrorMessage());
            isFormValid = false;
        }

        String emergencyContactPhone = binding.etEmergencyContactPhone.getText().toString().trim();
        ProfileValidationResult emergencyContactPhoneResult = UserValidator.validateEmergencyContactPhone(emergencyContactPhone);
        if (!emergencyContactPhoneResult.isValid()) {
            binding.tilEmergencyContactPhone.setError(emergencyContactPhoneResult.getErrorMessage());
            isFormValid = false;
        }

        String emergencyContactRelation = binding.etEmergencyContactRelation.getText().toString().trim();
        ProfileValidationResult emergencyContactRelationResult = UserValidator.validateEmergencyContactRelation(emergencyContactRelation);
        if (!emergencyContactRelationResult.isValid()) {
            binding.tilEmergencyContactRelation.setError(emergencyContactRelationResult.getErrorMessage());
            isFormValid = false;
        }

        String address = binding.etAddress.getText().toString().trim();
        ProfileValidationResult addressResult = UserValidator.validateAddress(address);
        if (!addressResult.isValid()) {
            binding.tilAddress.setError(addressResult.getErrorMessage());
            isFormValid = false;
        }

        String bloodType = binding.etBloodType.getText().toString().trim();
        ProfileValidationResult bloodTypeResult = UserValidator.validateBloodType(bloodType);
        if (!bloodTypeResult.isValid()) {
            binding.tilBloodType.setError(bloodTypeResult.getErrorMessage());
            isFormValid = false;
        }

        String allergies = binding.etAllergies.getText().toString().trim();
        ProfileValidationResult allergiesResult = UserValidator.validateAllergies(allergies);
        if (!allergiesResult.isValid()) {
            binding.tilAllergies.setError(allergiesResult.getErrorMessage());
            isFormValid = false;
        }

        String medicalConditions = binding.etMedicalConditions.getText().toString().trim();
        ProfileValidationResult medicalConditionsResult = UserValidator.validateMedicalConditions(medicalConditions);
        if (!medicalConditionsResult.isValid()) {
            binding.tilMedicalConditions.setError(medicalConditionsResult.getErrorMessage());
            isFormValid = false;
        }

        String doctorName = binding.etDoctorName.getText().toString().trim();
        ProfileValidationResult doctorNameResult = UserValidator.validateDoctorName(doctorName);
        if (!doctorNameResult.isValid()) {
            binding.tilDoctorName.setError(doctorNameResult.getErrorMessage());
            isFormValid = false;
        }

        String doctorPhone = binding.etDoctorPhone.getText().toString().trim();
        ProfileValidationResult doctorPhoneResult = UserValidator.validateDoctorPhone(doctorPhone);
        if (!doctorPhoneResult.isValid()) {
            binding.tilDoctorPhone.setError(doctorPhoneResult.getErrorMessage());
            isFormValid = false;
        }

        String hospitalName = binding.etHospitalName.getText().toString().trim();
        ProfileValidationResult hospitalNameResult = UserValidator.validateHospitalName(hospitalName);
        if (!hospitalNameResult.isValid()) {
            binding.tilHospitalName.setError(hospitalNameResult.getErrorMessage());
            isFormValid = false;
        }
        
        // 启用或禁用保存按钮
        binding.btnSave.setEnabled(isFormValid);
    }
    
    /**
     * 清除所有字段的错误状态
     */
    private void clearFieldErrors() {
        binding.tilFullName.setError(null);
        binding.tilGender.setError(null);
        binding.tilBirthDate.setError(null);
        binding.tilUsername.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhoneNumber.setError(null);
        binding.tilSecondaryPhone.setError(null);
        binding.tilEmergencyContactName.setError(null);
        binding.tilEmergencyContactPhone.setError(null);
        binding.tilEmergencyContactRelation.setError(null);
        binding.tilAddress.setError(null);
        binding.tilBloodType.setError(null);
        binding.tilAllergies.setError(null);
        binding.tilMedicalConditions.setError(null);
        binding.tilDoctorName.setError(null);
        binding.tilDoctorPhone.setError(null);
        binding.tilHospitalName.setError(null);
    }
    
    /**
     * 根据验证错误消息在相应字段显示错误
     * @param validationError 验证错误消息
     */
    private void showFieldValidationErrors(String validationError) {
        // 根据错误消息内容判断是哪个字段的错误
        if (validationError.contains(getString(R.string.profile_edit_field_name))) {
            binding.tilFullName.setError(validationError);
        } else if (validationError.contains(getString(R.string.profile_edit_field_gender))) {
            binding.tilGender.setError(validationError);
        } else if (validationError.contains(getString(R.string.profile_edit_field_birth_date)) || validationError.contains(getString(R.string.profile_edit_field_birth_date_alt))) {
            binding.tilBirthDate.setError(validationError);
        } else if (validationError.contains(getString(R.string.profile_edit_field_username))) {
            binding.tilUsername.setError(validationError);
        } else if (validationError.contains(getString(R.string.profile_edit_field_email)) || validationError.contains(getString(R.string.profile_edit_field_email_alt))) {
            binding.tilEmail.setError(validationError);
        } else if (validationError.contains(getString(R.string.profile_edit_field_phone)) || validationError.contains(getString(R.string.profile_edit_field_phone_alt))) {
            binding.tilPhoneNumber.setError(validationError);
        }
    }
    
    /**
     * 验证并保存个人资料
     */
    private void validateAndSaveProfile() {
        // 再次验证表单
        validateForm();
        
        if (!binding.btnSave.isEnabled()) {
            showErrorMessage(getString(R.string.error_form_validation_failed));
            return;
        }
        
        // 创建更新后的用户对象
        User updatedUser = createUpdatedUser();
        if (updatedUser == null) {
            showErrorMessage(getString(R.string.error_create_profile_failed));
            return;
        }
        
        // 通过UserViewModel更新用户资料
        userViewModel.updateUserProfile(updatedUser);
    }
    
    /**
     * 创建更新后的用户对象
     * @return 更新后的User对象
     */
    private User createUpdatedUser() {
        if (currentUser == null) {
            return null;
        }
        
        try {
            // 复制当前用户对象
            User updatedUser = new User();
            updatedUser.setId(currentUser.getId());
            updatedUser.setUsername(binding.etUsername.getText().toString().trim());
            updatedUser.setEmail(binding.etEmail.getText().toString().trim());
            updatedUser.setPhone(binding.etPhoneNumber.getText().toString().trim());
            updatedUser.setPassword(currentUser.getPassword()); // 保持原密码
            
            // 设置个人资料信息
            updatedUser.setFullName(binding.etFullName.getText().toString().trim());
            updatedUser.setGender(binding.actvGender.getText().toString().trim());
            
            // 转换出生日期格式
            String birthDateDisplay = binding.etBirthDate.getText().toString().trim();
            String birthDate = convertDisplayDateToStorageFormat(birthDateDisplay);
            updatedUser.setBirthDate(birthDate);
            
            // 保持现有的其他字段
            updatedUser.setProfilePhotoPath(currentUser.getProfilePhotoPath());
            updatedUser.setSecondaryPhone(binding.etSecondaryPhone.getText().toString().trim());
            updatedUser.setEmergencyContactName(binding.etEmergencyContactName.getText().toString().trim());
            updatedUser.setEmergencyContactPhone(binding.etEmergencyContactPhone.getText().toString().trim());
            updatedUser.setEmergencyContactRelation(binding.etEmergencyContactRelation.getText().toString().trim());
            updatedUser.setAddress(binding.etAddress.getText().toString().trim());
            updatedUser.setBloodType(binding.etBloodType.getText().toString().trim());
            updatedUser.setAllergies(binding.etAllergies.getText().toString().trim());
            updatedUser.setMedicalConditions(binding.etMedicalConditions.getText().toString().trim());
            updatedUser.setDoctorName(binding.etDoctorName.getText().toString().trim());
            updatedUser.setDoctorPhone(binding.etDoctorPhone.getText().toString().trim());
            updatedUser.setHospitalName(binding.etHospitalName.getText().toString().trim());
            
            // 保持会话管理字段
            updatedUser.setLoggedIn(currentUser.isLoggedIn());
            updatedUser.setRememberMe(currentUser.isRememberMe());
            updatedUser.setLastLoginTime(currentUser.getLastLoginTime());
            updatedUser.setLoginAttempts(currentUser.getLoginAttempts());
            updatedUser.setLastAttemptTime(currentUser.getLastAttemptTime());
            
            // 更新时间戳
            updatedUser.setCreatedAt(currentUser.getCreatedAt());
            updatedUser.setUpdatedAt(System.currentTimeMillis());
            
            return updatedUser;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 处理取消编辑
     */
    private void handleCancelEdit() {
        if (hasUnsavedChanges) {
            showDiscardChangesDialog();
        } else {
            finish();
        }
    }
    
    /**
     * 显示放弃更改确认对话框
     */
    private void showDiscardChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_discard_changes_title);
        builder.setMessage(R.string.confirm_discard_changes_message);
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            hasUnsavedChanges = false;
            finish();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
    
    /**
     * 显示日期选择器
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        // 如果已有出生日期，使用该日期作为初始值
        String currentBirthDate = binding.etBirthDate.getText().toString().trim();
        if (!currentBirthDate.isEmpty()) {
            try {
                Date date = displayDateFormatter.parse(currentBirthDate);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (ParseException e) {
                // 使用默认日期
            }
        } else {
            // 默认设置为50年前
            calendar.add(Calendar.YEAR, -50);
        }
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                
                String formattedDate = displayDateFormatter.format(selectedDate.getTime());
                binding.etBirthDate.setText(formattedDate);
                
                hasUnsavedChanges = true;
                validateForm();
            }
        }, year, month, day);
        
        // 设置日期范围限制
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -13); // 最小13岁
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -120); // 最大120岁
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        datePickerDialog.setTitle(R.string.select_birth_date);
        datePickerDialog.show();
    }
    
    /**
     * 将显示格式的日期转换为存储格式
     * @param displayDate 显示格式的日期（yyyy年MM月dd日）
     * @return 存储格式的日期（yyyy-MM-dd）
     */
    private String convertDisplayDateToStorageFormat(String displayDate) {
        if (displayDate == null || displayDate.trim().isEmpty()) {
            return "";
        }
        
        try {
            Date date = displayDateFormatter.parse(displayDate.trim());
            if (date != null) {
                return dateFormatter.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return displayDate.trim();
    }
    
    /**
     * 显示照片选择对话框
     */
    private void showPhotoOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.profile_photo_options_title);
        
        String[] options = {
            getString(R.string.take_photo),
            getString(R.string.select_from_gallery)
        };
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // 拍照
                    requestCameraPermissionAndTakePhoto();
                    break;
                case 1: // 从相册选择
                    requestStoragePermissionAndSelectPhoto();
                    break;
            }
        });
        
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
    
    /**
     * 确认删除照片
     */
    private void confirmRemovePhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_remove_photo_title);
        builder.setMessage(R.string.confirm_remove_photo_message);
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            userViewModel.updateProfilePhoto(null);
            hasUnsavedChanges = true;
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
    
    /**
     * 请求相机权限并拍照
     */
    private void requestCameraPermissionAndTakePhoto() {
        if (PermissionUtils.isCameraPermissionGranted(this)) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }
    
    /**
     * 请求存储权限并选择照片
     */
    private void requestStoragePermissionAndSelectPhoto() {
        if (PermissionUtils.isStoragePermissionGranted(this)) {
            openGallery();
        } else {
            storagePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }
    
    /**
     * 打开相机
     */
    private void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                showErrorMessage(getString(R.string.camera_unavailable));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage(getString(R.string.open_camera_failed));
        }
    }
    
    /**
     * 打开相册
     */
    private void openGallery() {
        try {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (pickPhotoIntent.resolveActivity(getPackageManager()) != null) {
                galleryLauncher.launch(pickPhotoIntent);
            } else {
                showErrorMessage(getString(R.string.gallery_unavailable));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage(getString(R.string.open_gallery_failed));
        }
    }
    
    /**
     * 将Bitmap保存为临时文件并更新照片
     * @param bitmap 要保存的Bitmap
     */
    private void saveBitmapAndUpdatePhoto(Bitmap bitmap) {
        try {
            // 创建临时文件
            File tempFile = new File(getCacheDir(), "temp_profile_photo_" + System.currentTimeMillis() + ".jpg");
            
            // 保存Bitmap到文件
            java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            
            // 更新个人资料照片
            Uri tempUri = Uri.fromFile(tempFile);
            userViewModel.updateProfilePhoto(tempUri);
            hasUnsavedChanges = true;
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage(getString(R.string.photo_temp_save_failed));
        }
    }
    
    /**
     * 加载个人资料照片
     * @param photoPath 照片路径
     */
    private void loadProfilePhoto(String photoPath) {
        if (photoPath != null && !photoPath.trim().isEmpty()) {
            try {
                File photoFile = new File(photoPath);
                if (photoFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                    if (bitmap != null) {
                        binding.ivProfilePhoto.setImageBitmap(bitmap);
                        binding.ivProfilePhoto.setContentDescription(getString(R.string.user_profile_photo_description));
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 显示默认头像
        binding.ivProfilePhoto.setImageResource(R.drawable.ic_medication_default);
        binding.ivProfilePhoto.setContentDescription(getString(R.string.default_profile_photo_description));
    }
    
    // UI状态管理方法
    
    /**
     * 显示加载状态
     * @param isLoading 是否正在加载
     */
    private void showLoadingState(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.llMainContent.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.llMainContent.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 显示错误消息
     * @param message 错误消息
     */
    private void showErrorMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            binding.tvMessage.setText(message);
            binding.tvMessage.setBackgroundResource(R.drawable.error_message_background);
            binding.tvMessage.setVisibility(View.VISIBLE);
            
            // 添加淡入动画
            binding.tvMessage.setAlpha(0f);
            binding.tvMessage.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
            
            // 为老年用户添加无障碍播报
            binding.tvMessage.setContentDescription(getString(R.string.profile_edit_error_content_description, message));
            binding.tvMessage.announceForAccessibility(getString(R.string.profile_edit_error_announcement, message));
            
            // 同时显示Toast提示
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            
            // 清除ViewModel中的错误消息
            userViewModel.clearErrorMessage();
            
            // 3秒后自动隐藏消息
            binding.getRoot().postDelayed(this::hideMessage, 3000);
        }
    }
    
    /**
     * 显示成功消息
     * @param message 成功消息
     */
    private void showSuccessMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            binding.tvMessage.setText("🎉 " + message);
            binding.tvMessage.setBackgroundResource(R.drawable.success_message_background);
            binding.tvMessage.setVisibility(View.VISIBLE);
            
            // 添加淡入动画
            binding.tvMessage.setAlpha(0f);
            binding.tvMessage.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
            
            // 为老年用户添加无障碍播报
            binding.tvMessage.setContentDescription(getString(R.string.profile_edit_success_content_description, message));
            binding.tvMessage.announceForAccessibility(getString(R.string.profile_edit_success_announcement, message));
            
            // 添加成功动画效果
            addSuccessAnimation(binding.tvMessage);
            
            // 同时显示Toast提示
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            
            // 清除ViewModel中的成功消息
            userViewModel.clearSuccessMessage();
        }
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
     * 隐藏消息
     */
    private void hideMessage() {
        binding.tvMessage.setVisibility(View.GONE);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理资源
        if (binding != null) {
            binding = null;
        }
        
        // 清理ViewModel引用
        if (userViewModel != null) {
            userViewModel.clearErrorMessage();
            userViewModel.clearSuccessMessage();
            userViewModel.resetProfileUpdateStatus();
            userViewModel = null;
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
            userViewModel.resetProfileUpdateStatus();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            handleCancelEdit();
            return true; // 阻止默认的返回行为
        }
        return super.onKeyDown(keyCode, event);
    }
}