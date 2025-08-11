package com.medication.reminders.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.medication.reminders.R;
import com.medication.reminders.adapter.HealthDiaryAdapter;
import com.medication.reminders.database.entity.HealthDiary;
import com.medication.reminders.utils.ErrorHandler;
import com.medication.reminders.utils.LoadingIndicator;
import com.medication.reminders.viewmodels.HealthDiaryViewModel;

import java.util.ArrayList;

/**
 * HealthDiaryListActivity - 健康日记列表界面
 * 显示用户的所有健康日记，支持查看详情和添加新日记
 * 为老年用户提供友好的界面设计
 */
public class HealthDiaryListActivity extends AppCompatActivity implements HealthDiaryAdapter.OnDiaryClickListener {
    
    // 请求码常量
    private static final int REQUEST_ADD_DIARY = 1001;
    private static final int REQUEST_VIEW_DIARY = 1002;
    
    private RecyclerView recyclerViewHealthDiary;
    private HealthDiaryAdapter adapter;
    private HealthDiaryViewModel viewModel;
    private FloatingActionButton fabAddDiary;
    
    // 状态布局
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutLoading;
    private LinearLayout layoutError;
    private Button btnRetry;
    
    // 加载指示器组件
    private ProgressBar progressBar;
    private TextView tvLoadingMessage;
    private TextView tvErrorMessage;
    
    // 工具类
    private LoadingIndicator.Manager loadingManager;
    private boolean isRefreshing = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_diary_list);
        
        android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_activity_create_start));
        
        // 初始化视图组件
        initViews();
        
        // 初始化工具类
        initUtils();
        
        // 设置ViewModel
        setupViewModel();
        
        // 设置RecyclerView
        setupRecyclerView();
        
        // 设置观察者
        setupObservers();
        
        // 设置点击监听器
        setupClickListeners();
        
        // 设置ActionBar
        setupActionBar();
        
        android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_activity_create_complete));
        
        // 先检查用户登录状态，然后加载数据
        checkUserLoginStatus();
        
        // 初始加载数据
        refreshDiaryList();
    }
    
    /**
     * 初始化UI组件
     */
    private void initViews() {
        recyclerViewHealthDiary = findViewById(R.id.recyclerViewHealthDiary);
        fabAddDiary = findViewById(R.id.fabAddDiary);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutError = findViewById(R.id.layoutError);
        btnRetry = findViewById(R.id.btnRetry);
        
        // 加载指示器组件
        progressBar = findViewById(R.id.progressBar);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
    }
    
    /**
     * 初始化工具类
     */
    private void initUtils() {
        loadingManager = LoadingIndicator.createManager(this);
    }
    
    /**
     * 设置ViewModel
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HealthDiaryViewModel.class);
    }
    
    /**
     * 设置RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new HealthDiaryAdapter(new ArrayList<>(), this);
        recyclerViewHealthDiary.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHealthDiary.setAdapter(adapter);
        
        // 设置RecyclerView的可访问性
        recyclerViewHealthDiary.setContentDescription(getString(R.string.health_diary_list_title));
    }
    
    /**
     * 设置LiveData观察者
     */
    private void setupObservers() {
        // 观察日记列表数据
        viewModel.getUserDiaries().observe(this, diaries -> {
            android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_received_data, (diaries != null ? diaries.size() + getString(R.string.diary_list_data_count) : getString(R.string.diary_list_data_null))));
            
            // 隐藏加载指示器
            loadingManager.hideAll();
            hideAllStates();
            
            if (diaries != null && !diaries.isEmpty()) {
                android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_show_list, diaries.size()));
                // 显示日记列表
                adapter.updateDiaries(diaries);
                recyclerViewHealthDiary.setVisibility(View.VISIBLE);
                
                // 显示成功加载消息（仅在刷新时）
                if (isRefreshing) {
                    ErrorHandler.showSuccessToast(this, getString(R.string.diary_list_load_success, diaries.size()));
                    isRefreshing = false;
                }
            } else {
                android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_show_empty));
                // 显示空状态
                showEmptyState();
                if (isRefreshing) {
                    isRefreshing = false;
                }
            }
        });
        
        // 观察操作结果
        viewModel.getOperationResult().observe(this, result -> {
            if (result != null && !result.isEmpty()) {
                // 根据结果类型显示不同的提示
                if (result.contains(getString(R.string.diary_operation_success_contains))) {
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
                    if (!isRefreshing) {
                        showLoadingState();
                    }
                } else {
                    loadingManager.hideAll();
                }
            }
        });
        
        // 观察错误状态
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            loadingManager.hideAll();
            isRefreshing = false;
            
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showErrorState(errorMessage);
                ErrorHandler.showErrorToast(this, new Exception(errorMessage), getString(R.string.diary_list_load_error));
            }
        });
        
        // 观察成功状态
        viewModel.getSuccessMessage().observe(this, successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                ErrorHandler.showSuccessToast(this, successMessage);
            }
        });
        
        // 观察操作成功状态
        viewModel.getOperationSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                // 操作成功后刷新数据
                refreshDiaryList();
            }
        });
    }
    
    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
        // 添加日记按钮
        fabAddDiary.setOnClickListener(v -> {
            // 跳转到添加日记界面
            Intent intent = new Intent(this, HealthDiaryEditActivity.class);
            intent.putExtra(HealthDiaryEditActivity.EXTRA_MODE, HealthDiaryEditActivity.MODE_ADD);
            startActivityForResult(intent, REQUEST_ADD_DIARY);
        });
        
        // 重试按钮
        btnRetry.setOnClickListener(v -> {
            retryLoadDiaries();
        });
    }
    
    /**
     * 设置ActionBar
     */
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.health_diary_list_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    /**
     * 显示加载状态
     */
    private void showLoadingState() {
        hideAllStates();
        
        // 使用内联加载指示器
        LoadingIndicator.HealthDiary.showInlineListLoading(loadingManager, progressBar, tvLoadingMessage);
        layoutLoading.setVisibility(View.VISIBLE);
    }
    
    /**
     * 显示空状态
     */
    private void showEmptyState() {
        hideAllStates();
        layoutEmptyState.setVisibility(View.VISIBLE);
    }
    
    /**
     * 显示错误状态
     */
    private void showErrorState() {
        showErrorState(getString(R.string.diary_load_error));
    }
    
    /**
     * 显示错误状态（带自定义消息）
     * @param errorMessage 错误消息
     */
    private void showErrorState(String errorMessage) {
        hideAllStates();
        layoutError.setVisibility(View.VISIBLE);
        
        if (tvErrorMessage != null) {
            tvErrorMessage.setText(errorMessage);
        }
    }
    
    /**
     * 隐藏所有状态布局
     */
    private void hideAllStates() {
        recyclerViewHealthDiary.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        
        // 隐藏加载指示器
        loadingManager.hideAll();
    }
    
    /**
     * 刷新日记列表
     */
    private void refreshDiaryList() {
        android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_refresh_start));
        isRefreshing = true;
        showLoadingState();
        
        // 添加调试信息
        android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_viewmodel_status, (viewModel != null ? getString(R.string.diary_list_viewmodel_initialized) : getString(R.string.diary_list_viewmodel_not_initialized))));
        android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_adapter_status, (adapter != null ? getString(R.string.diary_list_adapter_initialized, adapter.getItemCount()) : getString(R.string.diary_list_adapter_not_initialized))));
        
        viewModel.refreshDiaries();
    }
    
    /**
     * 重试加载日记
     */
    private void retryLoadDiaries() {
        showLoadingState();
        viewModel.refreshDiaries();
    }
    
    /**
     * 处理日记项点击事件
     * @param diary 被点击的日记
     */
    @Override
    public void onDiaryClick(HealthDiary diary) {
        if (diary != null) {
            Intent intent = new Intent(this, HealthDiaryDetailActivity.class);
            intent.putExtra(HealthDiaryDetailActivity.EXTRA_DIARY_ID, diary.getId());
            startActivityForResult(intent, REQUEST_VIEW_DIARY);
        }
    }
    
    /**
     * 处理ActionBar返回按钮点击
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    /**
     * 处理从其他Activity返回的结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ADD_DIARY:
                    // 添加日记成功，刷新列表
                    refreshDiaryList();
                    ErrorHandler.HealthDiary.showDiarySuccessMessage(this, getString(R.string.diary_add_success_message));
                    break;
                case REQUEST_VIEW_DIARY:
                    // 从详情页返回，可能有编辑或删除操作，刷新列表
                    refreshDiaryList();
                    break;
            }
        }
    }
    
    /**
     * 当Activity恢复时刷新数据
     */
    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_activity_resume));
        
        // 检查用户登录状态
        checkUserLoginStatus();
        
        // 刷新日记列表数据，确保从其他页面返回时数据是最新的
        refreshDiaryList();
    }
    
    /**
     * 检查用户登录状态
     */
    private void checkUserLoginStatus() {
        android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_check_login_status));
        
        // 异步检查用户登录状态
        new Thread(() -> {
            try {
                // 直接使用数据库DAO检查用户状态
                com.medication.reminders.database.MedicationDatabase database = 
                    com.medication.reminders.database.MedicationDatabase.getDatabase(this);
                com.medication.reminders.database.dao.UserDao userDao = database.userDao();
                
                // 检查是否有已登录用户
                com.medication.reminders.database.entity.User currentUser = userDao.getCurrentLoggedInUser();
                
                if (currentUser == null) {
                    android.util.Log.w(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_no_logged_user));
                    
                    // 在主线程显示提示信息
                    runOnUiThread(() -> {
                        showErrorState(getString(R.string.diary_list_login_required));
                    });
                } else {
                    android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_logged_user_info, currentUser.getUsername(), currentUser.getId()));
                    
                    // 检查该用户是否有日记数据
                    com.medication.reminders.database.dao.HealthDiaryDao diaryDao = database.healthDiaryDao();
                    int diaryCount = diaryDao.getDiaryCountByUserIdSync(currentUser.getId());
                    android.util.Log.d(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_user_diary_count, currentUser.getUsername(), diaryCount));
                }
            } catch (Exception e) {
                android.util.Log.e(getString(R.string.log_tag_health_diary_list), getString(R.string.diary_list_check_user_status_failed), e);
                runOnUiThread(() -> {
                    showErrorState(getString(R.string.diary_list_check_user_status_error, e.getMessage()));
                });
            }
        }).start();
    }
    
    /**
     * 清理资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingManager != null) {
            loadingManager.cleanup();
        }
    }
    
    /**
     * 处理返回键按下事件
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 可以在这里添加退出确认逻辑，如果需要的话
    }
}