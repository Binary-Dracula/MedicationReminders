package com.medication.reminders.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.medication.reminders.R;
import com.medication.reminders.database.entity.HealthDiary;
import com.medication.reminders.utils.ErrorHandler;
import com.medication.reminders.utils.LoadingIndicator;
import com.medication.reminders.viewmodels.HealthDiaryViewModel;

/**
 * 健康日记编辑界面
 * 支持新增和编辑日记功能
 */
public class HealthDiaryEditActivity extends AppCompatActivity {
    
    public static final String EXTRA_DIARY_ID = "diary_id";
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_ADD = "add";
    public static final String MODE_EDIT = "edit";
    
    private EditText editTextContent;
    private Button buttonSave;
    private Button buttonCancel;
    
    // 加载指示器组件
    private ProgressBar progressBar;
    private TextView tvLoadingMessage;
    private View layoutLoading;
    
    private HealthDiaryViewModel viewModel;
    private String currentMode;
    private long diaryId = -1;
    private HealthDiary currentDiary;
    
    // 工具类
    private LoadingIndicator.Manager loadingManager;
    private boolean isSaving = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_diary_edit);
        
        // 初始化视图组件
        initViews();
        
        // 初始化工具类
        initUtils();
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(HealthDiaryViewModel.class);
        
        // 获取传入参数，确定是新增还是编辑模式
        parseIntentData();
        
        // 设置界面标题和内容
        setupUI();
        
        // 设置事件监听器
        setupListeners();
        
        // 观察ViewModel数据变化
        observeViewModel();
    }
    
    /**
     * 初始化视图组件
     */
    private void initViews() {
        editTextContent = findViewById(R.id.editTextContent);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        
        // 加载指示器组件
        progressBar = findViewById(R.id.progressBar);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        layoutLoading = findViewById(R.id.layoutLoading);
        
        // 设置返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    /**
     * 初始化工具类
     */
    private void initUtils() {
        loadingManager = LoadingIndicator.createManager(this);
    }
    
    /**
     * 解析Intent传入的数据
     */
    private void parseIntentData() {
        Intent intent = getIntent();
        currentMode = intent.getStringExtra(EXTRA_MODE);
        
        if (currentMode == null) {
            currentMode = MODE_ADD; // 默认为新增模式
        }
        
        if (MODE_EDIT.equals(currentMode)) {
            diaryId = intent.getLongExtra(EXTRA_DIARY_ID, -1);
            if (diaryId == -1) {
                // 编辑模式但没有传入有效ID，切换到新增模式
                currentMode = MODE_ADD;
                Toast.makeText(this, getString(R.string.parameter_error_switch_to_add), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * 设置界面标题和内容
     */
    private void setupUI() {
        if (MODE_ADD.equals(currentMode)) {
            setTitle(getString(R.string.add_health_diary_page_title));
            // 新增模式：输入框为空
            editTextContent.setText("");
            editTextContent.setHint(getString(R.string.diary_content_placeholder));
        } else {
            setTitle(getString(R.string.edit_health_diary_page_title));
            // 编辑模式：加载现有内容
            loadDiaryContent();
        }
    }
    
    /**
     * 加载日记内容（编辑模式）
     */
    private void loadDiaryContent() {
        if (diaryId != -1) {
            // 调用ViewModel的方法来获取日记详情
            viewModel.getDiaryById(diaryId);
            
            // 显示加载状态
            showLoadingState();
            
            // 观察选中的日记数据变化
            viewModel.getSelectedDiary().observe(this, diary -> {
                hideLoadingState();
                
                if (diary != null) {
                    currentDiary = diary;
                    editTextContent.setText(diary.getContent());
                    editTextContent.setSelection(diary.getContent().length()); // 光标移到末尾
                } else {
                    ErrorHandler.showErrorToast(this, new Exception(getString(R.string.diary_does_not_exist)), getString(R.string.diary_load_success_contains));
                    finish();
                }
            });
        }
    }
    
    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        // 保存按钮点击事件
        buttonSave.setOnClickListener(v -> saveDiary());
        
        // 取消按钮点击事件
        buttonCancel.setOnClickListener(v -> {
            // 直接返回，不保存任何内容
            finish();
        });
    }
    
    /**
     * 观察ViewModel数据变化
     */
    private void observeViewModel() {
        // 观察操作结果（仅用于显示信息，不处理界面关闭）
        viewModel.getOperationResult().observe(this, result -> {
            if (result != null && !result.isEmpty()) {
                if (result.contains(getString(R.string.diary_load_success_contains))) {
                    // 加载成功不显示Toast，也不关闭界面
                    // 用户应该看到加载的内容并继续编辑
                } else if (result.contains(getString(R.string.diary_operation_success_contains))) {
                    // 成功消息显示Toast但不关闭界面
                    // 界面关闭由具体的成功状态观察者处理
                    ErrorHandler.showSuccessToast(this, result);
                } else {
                    ErrorHandler.showInfoToast(this, result);
                }
            }
        });
        
        // 观察加载状态
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    showLoadingState();
                } else {
                    hideLoadingState();
                }
            }
        });
        
        // 观察错误状态
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            hideLoadingState();
            isSaving = false;
            enableSaveButton();
            
            if (errorMessage != null && !errorMessage.isEmpty()) {
                ErrorHandler.showErrorToast(this, new Exception(errorMessage), 
                    MODE_ADD.equals(currentMode) ? getString(R.string.diary_add_operation) : getString(R.string.diary_update_operation));
            }
        });
        
        // 观察验证错误
        viewModel.getValidationError().observe(this, validationError -> {
            hideLoadingState();
            isSaving = false;
            enableSaveButton();
            
            if (validationError != null && !validationError.isEmpty()) {
                // 显示验证错误在输入框上
                editTextContent.setError(validationError);
                editTextContent.requestFocus();
                ErrorHandler.showInfoToast(this, validationError);
            }
        });
        
        // 观察添加成功状态
        viewModel.getAddSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                hideLoadingState();
                isSaving = false;
                ErrorHandler.HealthDiary.showDiarySuccessMessage(this, getString(R.string.diary_add_success_message));
                setResult(RESULT_OK);
                finish();
            }
        });
        
        // 观察更新成功状态
        viewModel.getUpdateSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                hideLoadingState();
                isSaving = false;
                ErrorHandler.HealthDiary.showDiarySuccessMessage(this, getString(R.string.diary_update_operation_text));
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    
    /**
     * 保存日记
     */
    private void saveDiary() {
        // 防止重复提交
        if (isSaving) {
            return;
        }
        
        String content = editTextContent.getText().toString().trim();
        
        // 输入内容验证
        if (!validateInput(content)) {
            return;
        }
        
        // 设置保存状态
        isSaving = true;
        disableSaveButton();
        
        // 清除之前的错误状态
        editTextContent.setError(null);
        viewModel.clearAllErrors();
        
        if (MODE_ADD.equals(currentMode)) {
            // 新增模式：创建新日记
            showSavingState(getString(R.string.diary_save_operation));
            viewModel.addDiary(content);
        } else {
            // 编辑模式：更新现有日记
            showSavingState(getString(R.string.diary_update_operation_text));
            viewModel.updateDiary(diaryId, content);
        }
    }
    
    /**
     * 验证输入内容
     * @param content 输入的内容
     * @return 验证是否通过
     */
    private boolean validateInput(String content) {
        if (TextUtils.isEmpty(content)) {
            editTextContent.setError(getString(R.string.diary_content_empty));
            editTextContent.requestFocus();
            ErrorHandler.showInfoToast(this, R.string.diary_content_empty);
            return false;
        }
        
        if (content.length() > 5000) {
            editTextContent.setError(getString(R.string.diary_content_too_long));
            editTextContent.requestFocus();
            ErrorHandler.showInfoToast(this, R.string.diary_content_too_long);
            return false;
        }
        
        return true;
    }
    
    /**
     * 显示加载状态
     */
    private void showLoadingState() {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.VISIBLE);
        }
        LoadingIndicator.HealthDiary.showInlineDetailLoading(loadingManager, progressBar, tvLoadingMessage);
    }
    
    /**
     * 隐藏加载状态
     */
    private void hideLoadingState() {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.GONE);
        }
        loadingManager.hideAll();
    }
    
    /**
     * 显示保存状态
     * @param operation 操作类型
     */
    private void showSavingState(String operation) {
        LoadingIndicator.HealthDiary.showOverlayLoading(loadingManager, layoutLoading, operation);
    }
    
    /**
     * 禁用保存按钮
     */
    private void disableSaveButton() {
        if (buttonSave != null) {
            buttonSave.setEnabled(false);
            buttonSave.setText(MODE_ADD.equals(currentMode) ? 
                getString(R.string.diary_saving) : getString(R.string.diary_updating));
        }
    }
    
    /**
     * 启用保存按钮
     */
    private void enableSaveButton() {
        if (buttonSave != null) {
            buttonSave.setEnabled(true);
            buttonSave.setText(getString(R.string.save_diary));
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 点击返回按钮
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理资源
        if (loadingManager != null) {
            loadingManager.cleanup();
        }
        
        // 重新启用保存按钮（防止内存泄漏时按钮状态异常）
        if (buttonSave != null) {
            buttonSave.setEnabled(true);
        }
    }
}