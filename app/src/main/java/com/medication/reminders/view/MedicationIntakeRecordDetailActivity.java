package com.medication.reminders.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.medication.reminders.R;
import com.medication.reminders.database.entity.MedicationIntakeRecord;
import com.medication.reminders.utils.LoadingIndicator;
import com.medication.reminders.viewmodels.MedicationIntakeRecordViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 用药记录详情页面
 * 显示单个用药记录的详细信息，包括药物名称、服用时间、服用剂量等
 * 页面为只读模式，不提供编辑功能，确保历史记录的完整性
 * 
 * 功能特点：
 * - 显示完整的用药记录信息
 * - 只读模式，保证历史数据不被修改
 * - 友好的错误处理和加载状态
 * - 支持返回导航
 * - 老年友好的界面设计
 */
public class MedicationIntakeRecordDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "MedicationIntakeRecordDetailActivity";
    
    // Intent参数常量
    public static final String EXTRA_RECORD_ID = "record_id";
    public static final String EXTRA_MEDICATION_NAME = "medication_name";
    
    // UI组件
    private ScrollView contentScrollView;
    private LinearLayout errorStateLayout;
    
    // 内容显示组件
    private TextView tvMedicationName;
    private TextView tvIntakeTime;
    private TextView tvDosageTaken;
    private TextView tvRecordId;
    
    // 错误状态组件
    private TextView tvErrorMessage;
    private Button btnRetry;
    
    // 底部按钮
    private Button btnBack;
    
    // 加载指示器
    private LoadingIndicator.Manager loadingManager;
    
    // 数据和业务逻辑
    private MedicationIntakeRecordViewModel viewModel;
    private long recordId = -1;
    private String medicationName;
    
    // 日期格式化器
    private SimpleDateFormat dateTimeFormatter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_intake_record_detail);
        
        Log.d(TAG, getString(R.string.intake_record_detail_create));
        
        // 初始化日期格式化器
        initializeDateFormatter();
        
        // 获取Intent参数
        getIntentExtras();
        
        // 验证参数有效性
        if (!validateParameters()) {
            Log.e(TAG, getString(R.string.intake_record_detail_param_validation_failed));
            finish();
            return;
        }
        
        // 初始化UI组件
        initializeViews();
        
        // 设置标题栏
        setupActionBar();
        
        // 初始化ViewModel
        initializeViewModel();
        
        // 设置事件监听器
        setupEventListeners();
        
        // 观察数据变化
        observeData();
        
        // 设置加载指示器
        setupLoadingIndicator();
        
        // 加载用药记录数据
        loadIntakeRecordData();
    }
    
    /**
     * 初始化日期格式化器
     */
    private void initializeDateFormatter() {
        dateTimeFormatter = new SimpleDateFormat(getString(R.string.datetime_format_display), Locale.CHINA);
        Log.d(TAG, getString(R.string.intake_record_detail_date_formatter_init));
    }
    
    /**
     * 获取Intent传递的参数
     */
    private void getIntentExtras() {
        if (getIntent() != null) {
            recordId = getIntent().getLongExtra(EXTRA_RECORD_ID, -1);
            medicationName = getIntent().getStringExtra(EXTRA_MEDICATION_NAME);
            
            Log.d(TAG, getString(R.string.intake_record_detail_get_intent_params, recordId, medicationName));
        }
    }
    
    /**
     * 验证参数有效性
     * @return 如果参数有效返回true，否则返回false
     */
    private boolean validateParameters() {
        if (recordId <= 0) {
            Log.e(TAG, getString(R.string.intake_record_detail_invalid_record_id, recordId));
            showErrorAndFinish(getString(R.string.intake_record_detail_invalid_record_id_message));
            return false;
        }
        
        return true;
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeViews() {
        // 主要内容区域
        contentScrollView = findViewById(R.id.contentScrollView);
        
        // 内容显示组件
        tvMedicationName = findViewById(R.id.tvMedicationName);
        tvIntakeTime = findViewById(R.id.tvIntakeTime);
        tvDosageTaken = findViewById(R.id.tvDosageTaken);
        tvRecordId = findViewById(R.id.tvRecordId);
        
        // 错误状态组件
        errorStateLayout = findViewById(R.id.errorStateLayout);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRetry = findViewById(R.id.btnRetry);
        
        // 底部按钮
        btnBack = findViewById(R.id.btnBack);
        
        // 初始化加载管理器
        loadingManager = LoadingIndicator.createManager(this);
        
        Log.d(TAG, getString(R.string.intake_record_detail_ui_init_complete));
    }
    
    /**
     * 设置标题栏
     */
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.intake_record_detail_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        Log.d(TAG, getString(R.string.intake_record_detail_actionbar_setup_complete));
    }
    
    /**
     * 初始化ViewModel
     */
    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(MedicationIntakeRecordViewModel.class);
        Log.d(TAG, getString(R.string.intake_record_detail_viewmodel_init_complete));
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 重试按钮点击事件
        btnRetry.setOnClickListener(v -> {
            Log.d(TAG, getString(R.string.intake_record_detail_user_click_retry));
            loadIntakeRecordData();
        });
        
        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, getString(R.string.intake_record_detail_user_click_back));
            onBackPressed();
        });
        
        Log.d(TAG, getString(R.string.intake_record_detail_event_listener_setup_complete));
    }
    
    /**
     * 观察数据变化
     */
    private void observeData() {
        // 观察选中的用药记录
        viewModel.getSelectedIntakeRecord().observe(this, record -> {
            Log.d(TAG, getString(R.string.intake_record_detail_received_data, (record != null ? record.getMedicationName() : "null")));
            if (record != null) {
                displayIntakeRecord(record);
            }
        });
        
        // 观察加载状态
        viewModel.getIsLoading().observe(this, isLoading -> {
            Log.d(TAG, getString(R.string.intake_record_detail_loading_state_change, String.valueOf(isLoading)));
            updateLoadingState(isLoading);
        });
        
        // 观察错误消息
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Log.e(TAG, getString(R.string.intake_record_detail_received_error, errorMessage));
                showErrorState(errorMessage);
            }
        });
        
        // 观察成功消息
        viewModel.getSuccessMessage().observe(this, successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Log.d(TAG, getString(R.string.intake_record_detail_received_success, successMessage));
                // 成功加载后显示内容
                showContentState();
            }
        });
        
        Log.d(TAG, getString(R.string.intake_record_detail_data_observer_setup_complete));
    }
    
    /**
     * 设置加载指示器
     */
    private void setupLoadingIndicator() {
        // 加载管理器已在initializeViews中初始化
        Log.d(TAG, getString(R.string.intake_record_detail_loading_indicator_setup_complete));
    }
    
    /**
     * 加载用药记录数据
     */
    private void loadIntakeRecordData() {
        Log.d(TAG, getString(R.string.intake_record_detail_load_data_start, recordId));
        
        // 隐藏错误状态
        hideErrorState();
        
        // 通过ViewModel加载数据
        viewModel.getIntakeRecordById(recordId);
    }
    
    /**
     * 显示用药记录详情
     * @param record 用药记录对象
     */
    private void displayIntakeRecord(MedicationIntakeRecord record) {
        Log.d(TAG, getString(R.string.intake_record_detail_display_record, record.toString()));
        
        try {
            // 显示药物名称
            if (tvMedicationName != null) {
                tvMedicationName.setText(record.getMedicationName());
                tvMedicationName.setContentDescription(getString(R.string.intake_record_detail_medication_name_content_description, record.getMedicationName()));
            }
            
            // 显示服用时间
            if (tvIntakeTime != null) {
                String formattedTime = formatDateTime(record.getIntakeTime());
                tvIntakeTime.setText(formattedTime);
                tvIntakeTime.setContentDescription(getString(R.string.intake_record_detail_intake_time_content_description, formattedTime));
            }
            
            // 显示服用剂量
            if (tvDosageTaken != null) {
                String dosageText = getString(R.string.dosage_unit_format, record.getDosageTaken());
                tvDosageTaken.setText(dosageText);
                tvDosageTaken.setContentDescription(getString(R.string.intake_record_detail_dosage_content_description, dosageText));
            }
            
            // 显示记录ID
            if (tvRecordId != null) {
                String recordIdText = getString(R.string.record_id_format, record.getId());
                tvRecordId.setText(recordIdText);
                tvRecordId.setContentDescription(getString(R.string.intake_record_detail_record_id_content_description, record.getId()));
            }
            
            // 显示内容区域
            showContentState();
            
            Log.d(TAG, getString(R.string.intake_record_detail_display_complete));
            
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.intake_record_detail_display_error), e);
            showErrorState(getString(R.string.intake_record_detail_display_error_message, e.getMessage()));
        }
    }
    
    /**
     * 格式化日期时间
     * @param timestamp 时间戳
     * @return 格式化后的日期时间字符串
     */
    private String formatDateTime(long timestamp) {
        try {
            Date date = new Date(timestamp);
            return dateTimeFormatter.format(date);
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.intake_record_detail_date_format_error), e);
            return getString(R.string.intake_record_detail_time_format_error);
        }
    }
    
    /**
     * 显示内容状态
     */
    private void showContentState() {
        if (contentScrollView != null) {
            contentScrollView.setVisibility(View.VISIBLE);
        }
        hideErrorState();
        Log.d(TAG, getString(R.string.intake_record_detail_show_content_state));
    }
    
    /**
     * 显示错误状态
     * @param errorMessage 错误消息
     */
    private void showErrorState(String errorMessage) {
        // 隐藏内容区域
        if (contentScrollView != null) {
            contentScrollView.setVisibility(View.GONE);
        }
        
        // 显示错误状态
        if (errorStateLayout != null) {
            errorStateLayout.setVisibility(View.VISIBLE);
        }
        
        // 设置错误消息
        if (tvErrorMessage != null) {
            tvErrorMessage.setText(errorMessage);
            tvErrorMessage.setContentDescription(getString(R.string.intake_record_detail_error_content_description, errorMessage));
        }
        
        Log.d(TAG, getString(R.string.intake_record_detail_show_error_state, errorMessage));
    }
    
    /**
     * 隐藏错误状态
     */
    private void hideErrorState() {
        if (errorStateLayout != null) {
            errorStateLayout.setVisibility(View.GONE);
        }
        Log.d(TAG, getString(R.string.intake_record_detail_hide_error_state));
    }
    
    /**
     * 更新加载状态
     * @param isLoading 是否正在加载
     */
    private void updateLoadingState(boolean isLoading) {
        if (loadingManager != null) {
            if (isLoading) {
                loadingManager.showProgressDialog(getString(R.string.intake_record_detail_loading_dialog_message));
            } else {
                loadingManager.hideProgressDialog();
            }
        }
    }
    
    /**
     * 显示错误并关闭页面
     * @param errorMessage 错误消息
     */
    private void showErrorAndFinish(String errorMessage) {
        Log.e(TAG, getString(R.string.intake_record_detail_show_error_and_finish, errorMessage));
        
        // 可以显示Toast或对话框
        // 这里简单地关闭页面
        finish();
    }
    
    // ========== 生命周期方法 ==========
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, getString(R.string.intake_record_detail_page_resume));
        
        // 页面恢复时可以刷新数据，但对于详情页面通常不需要
        // 如果需要实时更新，可以在这里重新加载数据
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理加载管理器
        if (loadingManager != null) {
            loadingManager.cleanup();
        }
        
        Log.d(TAG, getString(R.string.intake_record_detail_page_destroy));
    }
    
    // ========== 导航方法 ==========
    
    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, getString(R.string.intake_record_detail_actionbar_back_clicked));
        onBackPressed();
        return true;
    }
    
    @Override
    public void onBackPressed() {
        Log.d(TAG, getString(R.string.intake_record_detail_back_pressed));
        super.onBackPressed();
    }
    
    // ========== 工具方法 ==========
    
    /**
     * 获取当前显示的记录ID
     * @return 记录ID
     */
    public long getRecordId() {
        return recordId;
    }
    
    /**
     * 获取当前显示的药物名称
     * @return 药物名称
     */
    public String getMedicationName() {
        return medicationName;
    }
    
    /**
     * 检查页面是否正在加载
     * @return 如果正在加载返回true，否则返回false
     */
    public boolean isLoading() {
        return viewModel != null && 
               viewModel.getIsLoading().getValue() != null && 
               viewModel.getIsLoading().getValue();
    }
    
    /**
     * 检查页面是否显示错误状态
     * @return 如果显示错误状态返回true，否则返回false
     */
    public boolean isShowingError() {
        return errorStateLayout != null && errorStateLayout.getVisibility() == View.VISIBLE;
    }
    
    /**
     * 获取页面状态信息（用于调试）
     * @return 状态信息字符串
     */
    public String getPageStatus() {
        return getString(R.string.intake_record_detail_status_format, 
            recordId, isLoading(), isShowingError());
    }
}