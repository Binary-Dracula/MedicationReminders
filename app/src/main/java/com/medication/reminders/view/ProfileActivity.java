package com.medication.reminders.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.medication.reminders.R;
import com.medication.reminders.databinding.ActivityProfileBinding;
import com.medication.reminders.database.entity.User;
import com.medication.reminders.utils.PermissionUtils;
import com.medication.reminders.viewmodels.UserViewModel;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * ProfileActivity - 个人资料查看界面
 * 实现个人资料数据的显示、照片管理和导航功能
 * 遵循老年用户友好的设计原则
 * 
 * 重构说明：
 * - 移除对 UserRepository 的直接依赖
 * - 使用 UserViewModel 获取和显示用户信息
 * - 观察 currentUser LiveData 更新 UI
 * - 提供编辑入口
 */
public class ProfileActivity extends AppCompatActivity {
    
    private ActivityProfileBinding binding;
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置ViewBinding
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 初始化日期格式化器
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        displayDateFormatter = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        
        // 初始化UserViewModel
        initializeViewModel();
        
        // 设置UI组件
        setupUI();
        
        // 设置观察者
        setupObservers();
        
        // 初始化ActivityResultLauncher
        setupActivityResultLaunchers();
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
        binding.tvTitle.setText(R.string.profile_title);
        
        // 设置按钮点击事件
        binding.btnEditProfile.setOnClickListener(v -> navigateToEditProfile());
        binding.btnChangePhoto.setOnClickListener(v -> showPhotoOptionsDialog());
        binding.btnBack.setOnClickListener(v -> finish());
        
        // 设置个人资料照片点击事件
        binding.ivProfilePhoto.setOnClickListener(v -> showPhotoOptionsDialog());
        
        // 初始状态设置
        showLoadingState(false);
        hideMessage();
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
                    displayUserProfile(user);
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
        
        // 观察个人资料更新状态
        userViewModel.getProfileUpdateStatus().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String updateStatus) {
                if (updateStatus != null && !updateStatus.isEmpty()) {
                    if (updateStatus.contains("成功")) {
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
            public void onChanged(Boolean updateSuccess) {
                if (updateSuccess != null && updateSuccess) {
                    // 个人资料更新成功，UI会自动通过currentUser LiveData更新
                    Toast.makeText(ProfileActivity.this, "个人资料已更新", Toast.LENGTH_SHORT).show();
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
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
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
                        updateProfilePhoto(selectedImageUri);
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
     * 显示用户个人资料信息
     * @param user 用户对象
     */
    private void displayUserProfile(User user) {
        if (user == null) {
            return;
        }
        
        // 显示基本信息
        displayBasicInfo(user);
        
        // 显示联系信息
        displayContactInfo(user);
        
        // 加载个人资料照片
        loadProfilePhoto(user.getProfilePhotoPath());
    }
    
    /**
     * 显示基本信息
     * @param user 用户对象
     */
    private void displayBasicInfo(User user) {
        // 显示完整姓名
        String fullName = user.getFullName();
        binding.tvFullName.setText(fullName != null && !fullName.trim().isEmpty() 
            ? fullName : getString(R.string.profile_not_set));
        
        // 显示性别
        String gender = user.getGender();
        binding.tvGender.setText(formatGender(gender));
        
        // 显示年龄和出生日期
        String birthDate = user.getBirthDate();
        if (birthDate != null && !birthDate.trim().isEmpty()) {
            try {
                Date birth = dateFormatter.parse(birthDate);
                if (birth != null) {
                    // 计算年龄
                    int age = calculateAge(birth);
                    binding.tvAge.setText(age > 0 ? age + getString(R.string.age_suffix) : getString(R.string.profile_not_set));
                    
                    // 格式化出生日期显示
                    binding.tvBirthDate.setText(displayDateFormatter.format(birth));
                } else {
                    binding.tvAge.setText(getString(R.string.profile_not_set));
                    binding.tvBirthDate.setText(getString(R.string.profile_not_set));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                binding.tvAge.setText(getString(R.string.profile_not_set));
                binding.tvBirthDate.setText(birthDate);
            }
        } else {
            binding.tvAge.setText(getString(R.string.profile_not_set));
            binding.tvBirthDate.setText(getString(R.string.profile_not_set));
        }
    }
    
    /**
     * 显示联系信息
     * @param user 用户对象
     */
    private void displayContactInfo(User user) {
        // 显示用户名
        String username = user.getUsername();
        binding.tvUsername.setText(username != null && !username.trim().isEmpty() 
            ? username : getString(R.string.profile_not_set));
        
        // 显示邮箱地址
        String email = user.getEmail();
        binding.tvEmail.setText(email != null && !email.trim().isEmpty() 
            ? email : getString(R.string.profile_not_set));
        
        // 显示电话号码（部分掩盖）
        String phoneNumber = user.getPhone();
        binding.tvPhoneNumber.setText(formatPhoneNumber(phoneNumber));
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
    
    /**
     * 显示照片选择对话框
     */
    private void showPhotoOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.profile_photo_options_title);
        
        String[] options = {
            getString(R.string.take_photo),
            getString(R.string.select_from_gallery),
            getString(R.string.remove_profile_photo)
        };
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // 拍照
                    requestCameraPermissionAndTakePhoto();
                    break;
                case 1: // 从相册选择
                    requestStoragePermissionAndSelectPhoto();
                    break;
                case 2: // 删除照片
                    confirmDeletePhoto();
                    break;
            }
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
     * 确认删除照片
     */
    private void confirmDeletePhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete_photo_title);
        builder.setMessage(R.string.confirm_delete_photo_message);
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            deleteProfilePhoto();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
    
    /**
     * 删除个人资料照片
     */
    private void deleteProfilePhoto() {
        // 通过UserViewModel删除照片
        userViewModel.updateProfilePhoto(null);
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
            updateProfilePhoto(tempUri);
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage(getString(R.string.photo_temp_save_failed));
        }
    }
    
    /**
     * 更新个人资料照片
     * @param photoUri 照片URI
     */
    private void updateProfilePhoto(Uri photoUri) {
        // 通过UserViewModel更新照片
        userViewModel.updateProfilePhoto(photoUri);
    }
    
    /**
     * 导航到编辑个人资料界面
     */
    private void navigateToEditProfile() {
        Intent intent = new Intent(this, ProfileEditActivity.class);
        startActivityForResult(intent, 1001);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // 编辑成功后，UserViewModel会自动通过LiveData更新UI
            Toast.makeText(this, "个人资料已更新", Toast.LENGTH_SHORT).show();
        }
    }
    
    // 工具方法
    
    /**
     * 格式化性别显示
     * @param gender 性别字符串
     * @return 格式化后的性别显示
     */
    private String formatGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return getString(R.string.profile_not_set);
        }
        
        switch (gender.toLowerCase()) {
            case "male":
            case "男性":
                return getString(R.string.gender_male);
            case "female":
            case "女性":
                return getString(R.string.gender_female);
            case "prefer_not_to_say":
            case "不愿透露":
                return getString(R.string.gender_prefer_not_to_say);
            default:
                return gender;
        }
    }
    
    /**
     * 格式化电话号码显示（部分掩盖）
     * @param phoneNumber 电话号码
     * @return 格式化后的电话号码
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return getString(R.string.profile_not_set);
        }
        
        String phone = phoneNumber.trim();
        if (phone.length() > 4) {
            // 只显示最后4位数字
            String masked = getString(R.string.phone_mask_prefix) + phone.substring(phone.length() - 4);
            return masked;
        }
        
        return phone;
    }
    
    /**
     * 计算年龄
     * @param birthDate 出生日期
     * @return 年龄
     */
    private int calculateAge(Date birthDate) {
        if (birthDate == null) {
            return 0;
        }
        
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        
        Calendar now = Calendar.getInstance();
        
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        
        // 检查是否还没到生日
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        
        return Math.max(0, age);
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
            binding.tvMessage.setContentDescription("错误提示: " + message);
            binding.tvMessage.announceForAccessibility("个人资料错误: " + message);
            
            // 同时显示Toast提示
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            
            // 清除ViewModel中的错误消息
            userViewModel.clearErrorMessage();
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
            binding.tvMessage.setContentDescription("成功提示: " + message);
            binding.tvMessage.announceForAccessibility("恭喜您！" + message);
            
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
}