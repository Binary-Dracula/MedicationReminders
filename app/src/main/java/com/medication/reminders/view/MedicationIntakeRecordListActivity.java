package com.medication.reminders.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.medication.reminders.R;
import com.medication.reminders.adapter.MedicationIntakeRecordAdapter;
import com.medication.reminders.database.entity.MedicationIntakeRecord;
import com.medication.reminders.utils.LoadingIndicator;
import com.medication.reminders.viewmodels.MedicationIntakeRecordViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 用药记录列表页面
 * 显示用户的用药记录历史，按时间倒序排列
 * 支持点击查看详情，提供空状态显示和返回导航功能
 */
public class MedicationIntakeRecordListActivity extends AppCompatActivity implements MedicationIntakeRecordAdapter.OnIntakeRecordClickListener {
    
    private static final String TAG = "MedicationIntakeRecordListActivity";
    
    // UI组件
    private RecyclerView recyclerViewIntakeRecords;
    private LinearLayout emptyStateLayout;
    private TextView tvEmptyMessage;
    private LoadingIndicator.Manager loadingManager;
    
    // 数据和适配器
    private MedicationIntakeRecordAdapter adapter;
    private List<MedicationIntakeRecord> intakeRecords;
    
    // ViewModel
    private MedicationIntakeRecordViewModel viewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_intake_record_list);
        
        Log.d(TAG, getString(R.string.intake_record_list_create));
        
        // 初始化UI组件
        initializeViews();
        
        // 设置标题栏
        setupActionBar();
        
        // 初始化ViewModel
        initializeViewModel();
        
        // 设置RecyclerView
        setupRecyclerView();
        
        // 观察数据变化
        observeData();
        
        // 设置加载指示器
        setupLoadingIndicator();
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeViews() {
        recyclerViewIntakeRecords = findViewById(R.id.recyclerViewIntakeRecords);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        
        // 初始化加载管理器
        loadingManager = LoadingIndicator.createManager(this);
        
        // 初始化数据列表
        intakeRecords = new ArrayList<>();
        
        Log.d(TAG, getString(R.string.intake_record_ui_init_complete));
    }
    
    /**
     * 设置标题栏
     */
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.intake_record_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        Log.d(TAG, getString(R.string.intake_record_actionbar_setup_complete));
    }
    
    /**
     * 初始化ViewModel
     */
    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(MedicationIntakeRecordViewModel.class);
        Log.d(TAG, getString(R.string.intake_record_viewmodel_init_complete));
    }
    
    /**
     * 设置RecyclerView
     */
    private void setupRecyclerView() {
        // 设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewIntakeRecords.setLayoutManager(layoutManager);
        
        // 创建并设置适配器
        adapter = new MedicationIntakeRecordAdapter(intakeRecords, this);
        recyclerViewIntakeRecords.setAdapter(adapter);
        
        // 设置无障碍描述
        recyclerViewIntakeRecords.setContentDescription(getString(R.string.intake_record_list_content_description));
        
        Log.d(TAG, getString(R.string.intake_record_recyclerview_setup_complete));
    }
    
    /**
     * 观察数据变化
     */
    private void observeData() {
        // 观察所有用药记录
        viewModel.getAllIntakeRecords().observe(this, records -> {
            Log.d(TAG, getString(R.string.intake_record_received_data, (records != null ? records.size() : 0)));
            updateIntakeRecordsList(records);
        });
        
        // 观察加载状态
        viewModel.getIsLoading().observe(this, isLoading -> {
            Log.d(TAG, getString(R.string.intake_record_loading_state_change, String.valueOf(isLoading)));
            updateLoadingState(isLoading);
        });
        
        // 观察错误消息
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Log.e(TAG, getString(R.string.intake_record_received_error, errorMessage));
                showErrorMessage(errorMessage);
            }
        });
        
        // 观察成功消息
        viewModel.getSuccessMessage().observe(this, successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Log.d(TAG, getString(R.string.intake_record_received_success, successMessage));
                // 可以在这里显示成功提示，但对于列表页面通常不需要
            }
        });
        
        Log.d(TAG, getString(R.string.intake_record_data_observer_setup_complete));
    }
    
    /**
     * 设置加载指示器
     */
    private void setupLoadingIndicator() {
        // 加载管理器已在initializeViews中初始化
        Log.d(TAG, getString(R.string.intake_record_loading_indicator_setup_complete));
    }
    
    /**
     * 更新用药记录列表
     * @param records 用药记录列表
     */
    private void updateIntakeRecordsList(List<MedicationIntakeRecord> records) {
        if (records == null || records.isEmpty()) {
            // 显示空状态
            showEmptyState();
            Log.d(TAG, getString(R.string.intake_record_show_empty_state));
        } else {
            // 显示记录列表
            showRecordsList(records);
            Log.d(TAG, getString(R.string.intake_record_show_list, records.size()));
        }
    }
    
    /**
     * 显示空状态
     */
    private void showEmptyState() {
        recyclerViewIntakeRecords.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setText(getString(R.string.no_intake_records_message));
            tvEmptyMessage.setContentDescription(getString(R.string.intake_record_empty_message_content_description));
        }
    }
    
    /**
     * 显示记录列表
     * @param records 用药记录列表
     */
    private void showRecordsList(List<MedicationIntakeRecord> records) {
        emptyStateLayout.setVisibility(View.GONE);
        recyclerViewIntakeRecords.setVisibility(View.VISIBLE);
        
        // 更新适配器数据
        this.intakeRecords.clear();
        this.intakeRecords.addAll(records);
        adapter.updateIntakeRecords(this.intakeRecords);
    }
    
    /**
     * 更新加载状态
     * @param isLoading 是否正在加载
     */
    private void updateLoadingState(boolean isLoading) {
        if (loadingManager != null) {
            if (isLoading) {
                loadingManager.showProgressDialog(getString(R.string.intake_record_loading_message));
            } else {
                loadingManager.hideProgressDialog();
            }
        }
    }
    
    /**
     * 显示错误消息
     * @param errorMessage 错误消息
     */
    private void showErrorMessage(String errorMessage) {
        // 可以使用Toast或Snackbar显示错误消息
        // 这里简单地在空状态中显示错误信息
        showEmptyState();
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setText(getString(R.string.intake_record_load_failed_message, errorMessage));
            tvEmptyMessage.setContentDescription(getString(R.string.intake_record_load_failed_content_description, errorMessage));
        }
    }
    
    // ========== 适配器回调方法 ==========
    
    /**
     * 处理用药记录点击事件
     * @param record 被点击的用药记录
     */
    @Override
    public void onIntakeRecordClick(MedicationIntakeRecord record) {
        Log.d(TAG, getString(R.string.intake_record_clicked, record.getMedicationName(), record.getId()));
        
        // 导航到用药记录详情页面
        Intent intent = new Intent(this, MedicationIntakeRecordDetailActivity.class);
        intent.putExtra(getString(R.string.intent_key_record_id), record.getId());
        intent.putExtra(getString(R.string.intent_key_medication_name), record.getMedicationName());
        startActivity(intent);
    }
    
    // ========== 生命周期方法 ==========
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, getString(R.string.intake_record_page_resume));
        
        // 页面恢复时刷新数据
        if (viewModel != null) {
            viewModel.refreshIntakeRecords();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理加载管理器
        if (loadingManager != null) {
            loadingManager.cleanup();
        }
        
        Log.d(TAG, getString(R.string.intake_record_page_destroy));
    }
    
    // ========== 导航方法 ==========
    
    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, getString(R.string.intake_record_navigate_back));
        onBackPressed();
        return true;
    }
    
    @Override
    public void onBackPressed() {
        Log.d(TAG, getString(R.string.intake_record_back_pressed));
        super.onBackPressed();
    }
}