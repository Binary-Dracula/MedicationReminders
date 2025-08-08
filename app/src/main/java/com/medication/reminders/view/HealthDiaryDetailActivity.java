package com.medication.reminders.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.medication.reminders.R;
import com.medication.reminders.database.entity.HealthDiary;
import com.medication.reminders.databinding.ActivityHealthDiaryDetailBinding;
import com.medication.reminders.utils.ErrorHandler;
import com.medication.reminders.utils.LoadingIndicator;
import com.medication.reminders.viewmodels.HealthDiaryViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 健康日记详情界面
 * 显示完整的日记内容，提供编辑和删除功能
 */
public class HealthDiaryDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_DIARY_ID = "diary_id";
    public static final int REQUEST_CODE_EDIT = 1001;
    
    private ActivityHealthDiaryDetailBinding binding;
    private HealthDiaryViewModel viewModel;
    private long diaryId;
    private HealthDiary currentDiary;
    
    // 工具类
    private LoadingIndicator.Manager loadingManager;
    private boolean isDeleting = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHealthDiaryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 初始化工具类
        loadingManager = LoadingIndicator.createManager(this);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(HealthDiaryViewModel.class);
        
        // 设置工具栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        // 获取传入的日记ID
        diaryId = getIntent().getLongExtra(EXTRA_DIARY_ID, -1);
        if (diaryId == -1) {
            showError(getString(R.string.diary_not_found));
            return;
        }
        
        // 设置点击事件
        setupClickListeners();
        
        // 观察数据变化
        observeViewModel();
        
        // 加载日记详情
        loadDiaryDetail();
    }
    
    /**
     * 设置点击事件监听器
     */
    private void setupClickListeners() {
        // 编辑按钮点击事件
        binding.btnEdit.setOnClickListener(v -> {
            if (currentDiary != null) {
                // 启动编辑界面
                Intent intent = new Intent(this, HealthDiaryEditActivity.class);
                intent.putExtra(HealthDiaryEditActivity.EXTRA_MODE, HealthDiaryEditActivity.MODE_EDIT);
                intent.putExtra(HealthDiaryEditActivity.EXTRA_DIARY_ID, currentDiary.getId());
                startActivityForResult(intent, REQUEST_CODE_EDIT);
            }
        });
        
        // 删除按钮点击事件
        binding.btnDelete.setOnClickListener(v -> {
            if (!isDeleting) {
                showDeleteConfirmDialog();
            }
        });
    }
    
    /**
     * 观察ViewModel数据变化
     */
    private void observeViewModel() {
        // 观察操作结果
        viewModel.getOperationResult().observe(this, result -> {
            if (result != null && !result.isEmpty()) {
                if (result.contains("成功")) {
                    ErrorHandler.showSuccessToast(this, result);
                    if (result.contains("删除")) {
                        // 删除成功，设置结果并返回列表页面
                        setResult(RESULT_OK);
                        finish();
                    }
                } else {
                    ErrorHandler.showInfoToast(this, result);
                }
            }
        });
        
        // 观察加载状态
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    showLoading();
                } else {
                    hideLoading();
                }
            }
        });
        
        // 观察错误状态
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            hideLoading();
            isDeleting = false;
            enableButtons();
            
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showError(errorMessage);
                ErrorHandler.showErrorToast(this, new Exception(errorMessage), "日记操作");
            }
        });
        
        // 观察成功状态
        viewModel.getSuccessMessage().observe(this, successMessage -> {
            hideLoading();
            
            if (successMessage != null && !successMessage.isEmpty()) {
                ErrorHandler.showSuccessToast(this, successMessage);
                if (successMessage.contains("删除")) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
        
        // 观察删除成功状态
        viewModel.getDeleteSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                ErrorHandler.HealthDiary.showDiarySuccessMessage(this, "删除");
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    
    /**
     * 加载日记详情
     */
    private void loadDiaryDetail() {
        showLoading();
        
        // 观察选中的日记
        viewModel.getSelectedDiary().observe(this, diary -> {
            hideLoading();
            if (diary != null) {
                currentDiary = diary;
                displayDiaryDetail(diary);
            } else {
                showError(getString(R.string.diary_not_found));
            }
        });
        
        // 触发加载日记详情
        viewModel.getDiaryById(diaryId);
    }
    
    /**
     * 显示日记详情
     */
    private void displayDiaryDetail(HealthDiary diary) {
        // 显示日记内容
        binding.tvDiaryContent.setText(diary.getContent());
        
        // 显示创建时间
        String createdTime = formatDateTime(diary.getCreatedAt());
        binding.tvCreatedTime.setText(createdTime);
        
        // 显示修改时间（如果有修改过）
        if (diary.getUpdatedAt() > diary.getCreatedAt()) {
            String updatedTime = formatDateTime(diary.getUpdatedAt());
            binding.tvUpdatedTime.setText(updatedTime);
            binding.layoutUpdatedTime.setVisibility(View.VISIBLE);
        } else {
            binding.layoutUpdatedTime.setVisibility(View.GONE);
        }
        
        // 显示内容和操作按钮
        binding.layoutDiaryContent.setVisibility(View.VISIBLE);
        binding.layoutActionButtons.setVisibility(View.VISIBLE);
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
        return formatter.format(new Date(timestamp));
    }
    
    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_diary_title)
                .setMessage(R.string.confirm_delete_diary_message)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    if (currentDiary != null) {
                        deleteDiary();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    /**
     * 删除日记
     */
    private void deleteDiary() {
        if (isDeleting || currentDiary == null) {
            return;
        }
        
        isDeleting = true;
        disableButtons();
        
        // 显示删除加载状态
        LoadingIndicator.HealthDiary.showDeleteLoading(loadingManager);
        
        // 清除之前的错误状态
        viewModel.clearAllErrors();
        
        // 执行删除操作
        viewModel.deleteDiary(currentDiary);
    }
    
    /**
     * 显示加载状态
     */
    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvLoadingMessage.setVisibility(View.VISIBLE);
        binding.layoutDiaryContent.setVisibility(View.GONE);
        binding.layoutActionButtons.setVisibility(View.GONE);
        binding.tvErrorMessage.setVisibility(View.GONE);
        
        // 使用加载管理器显示详情加载
        LoadingIndicator.HealthDiary.showInlineDetailLoading(loadingManager, 
            binding.progressBar, binding.tvLoadingMessage);
    }
    
    /**
     * 隐藏加载状态
     */
    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.tvLoadingMessage.setVisibility(View.GONE);
        loadingManager.hideAll();
    }
    
    /**
     * 显示错误信息
     */
    private void showError(String message) {
        hideLoading();
        binding.tvErrorMessage.setText(message);
        binding.tvErrorMessage.setVisibility(View.VISIBLE);
        binding.layoutDiaryContent.setVisibility(View.GONE);
        binding.layoutActionButtons.setVisibility(View.GONE);
    }
    
    /**
     * 禁用操作按钮
     */
    private void disableButtons() {
        binding.btnEdit.setEnabled(false);
        binding.btnDelete.setEnabled(false);
        binding.btnDelete.setText(getString(R.string.loading_delete_diary));
    }
    
    /**
     * 启用操作按钮
     */
    private void enableButtons() {
        binding.btnEdit.setEnabled(true);
        binding.btnDelete.setEnabled(true);
        binding.btnDelete.setText(getString(R.string.delete_diary));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK) {
            // 编辑完成后重新加载数据
            loadDiaryDetail();
            // 设置结果，通知列表页面刷新
            setResult(RESULT_OK);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理资源
        if (loadingManager != null) {
            loadingManager.cleanup();
        }
        
        binding = null;
    }
}