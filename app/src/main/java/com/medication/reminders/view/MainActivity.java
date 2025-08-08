package com.medication.reminders.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.medication.reminders.R;
import com.medication.reminders.repository.UserRepository;


/**
 * MainActivity - 登录成功后的主界面
 * 集成个人资料管理功能和药物提醒功能
 */
public class MainActivity extends AppCompatActivity {
    
    private TextView tvWelcome;
    private TextView tvUserInfo;
    private Button btnAddMedication;
    private Button btnViewMedicationList;
    private Button btnHealthDiary;
    private Button btnProfile;
    
    private UserRepository userRepository;
    private String currentUsername;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化UserRepository
        userRepository = new UserRepository(this);
        
        // Initialize views
        initViews();
        
        // Setup user information from login
        setupUserInfo();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("药物提醒助手");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        
        // 检查个人资料完成状态
        checkProfileCompletionStatus();
    }
    
    /**
     * 初始化UI组件
     */
    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnAddMedication = findViewById(R.id.btnAddMedication);
        btnViewMedicationList = findViewById(R.id.btnViewMedicationList);
        btnHealthDiary = findViewById(R.id.btnHealthDiary);
        btnProfile = findViewById(R.id.btnProfile);
    }
    
    /**
     * 为UI组件设置点击监听器
     */
    private void setupClickListeners() {
        btnAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMedicationActivity.class);
            startActivity(intent);
        });
        
        btnViewMedicationList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MedicationListActivity.class);
            startActivity(intent);
        });
        
        // 健康日记按钮点击监听器
        btnHealthDiary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HealthDiaryListActivity.class);
            startActivity(intent);
        });
        
        // 个人资料按钮点击监听器
        btnProfile.setOnClickListener(v -> {
            navigateToProfile();
        });
    }
    
    /**
     * 从登录Intent设置用户信息
     */
    private void setupUserInfo() {
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        long loginTime = intent.getLongExtra("login_time", System.currentTimeMillis());
        
        // 保存当前用户名
        currentUsername = username;
        
        if (username != null && !username.isEmpty()) {
            // 显示用户名作为欢迎信息
            String displayName = username;
            
            tvWelcome.setText("欢迎回来，" + displayName + "！");
            
            // Format login time for elderly users
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm", java.util.Locale.CHINA);
            String formattedTime = sdf.format(new java.util.Date(loginTime));
            tvUserInfo.setText("登录时间：" + formattedTime);
        } else {
            tvWelcome.setText("欢迎使用药物提醒助手！");
            tvUserInfo.setText("您的健康管理伙伴");
        }
    }
    
    /**
     * 创建选项菜单，添加个人资料入口
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    /**
     * 处理选项菜单项点击
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_profile) {
            navigateToProfile();
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 导航到个人资料界面
     */
    private void navigateToProfile() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            android.widget.Toast.makeText(this, "无法获取用户信息", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("username", currentUsername);
        startActivity(intent);
    }
    
    /**
     * 检查个人资料完成状态
     * 如果用户尚未完成个人资料，提示用户完善信息
     */
    private void checkProfileCompletionStatus() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            return;
        }
        
        // 在后台线程检查个人资料状态
        new Thread(() -> {
            try {
                // 使用getCurrentUser方法获取当前用户信息
                userRepository.getCurrentUserAsync(new com.medication.reminders.models.RepositoryCallback<com.medication.reminders.database.entity.User>() {
                    @Override
                    public void onSuccess(com.medication.reminders.database.entity.User user) {
                        runOnUiThread(() -> {
                            if (user == null || !isProfileComplete(user)) {
                                showProfileCompletionPrompt();
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        // 如果获取用户信息失败，不显示提示
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * 检查个人资料是否完整
     * @param user 用户实体
     * @return true如果个人资料完整，false否则
     */
    private boolean isProfileComplete(com.medication.reminders.database.entity.User user) {
        if (user == null) {
            return false;
        }
        
        // 检查必填字段
        return user.getFullName() != null && !user.getFullName().trim().isEmpty() &&
               user.getGender() != null && !user.getGender().trim().isEmpty() &&
               user.getBirthDate() != null && !user.getBirthDate().trim().isEmpty();
    }
    
    /**
     * 显示个人资料完善提示
     */
    private void showProfileCompletionPrompt() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("完善个人资料")
            .setMessage("为了更好地为您提供个性化服务，建议您完善个人资料信息。现在就去设置吗？")
            .setPositiveButton("立即设置", (dialog, which) -> {
                navigateToProfileEdit();
            })
            .setNegativeButton("稍后再说", (dialog, which) -> dialog.dismiss())
            .setCancelable(false)
            .show();
    }
    
    /**
     * 导航到个人资料编辑界面
     */
    private void navigateToProfileEdit() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            android.widget.Toast.makeText(this, "无法获取用户信息", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, ProfileEditActivity.class);
        intent.putExtra("username", currentUsername);
        intent.putExtra("is_first_setup", true);
        startActivity(intent);
    }
    
    /**
     * 显示退出确认对话框
     */
    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("退出登录")
            .setMessage("您确定要退出当前账户吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                performLogout();
            })
            .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
            .show();
    }
    
    /**
     * 执行退出登录操作
     */
    private void performLogout() {
        // 清除保存的登录凭据（如果用户选择）
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("清除登录信息")
            .setMessage("是否同时清除保存的登录信息？")
            .setPositiveButton("清除", (dialog, which) -> {
                userRepository.clearSavedCredentials();
                navigateToLogin();
            })
            .setNegativeButton("保留", (dialog, which) -> {
                navigateToLogin();
            })
            .show();
    }
    
    /**
     * 导航到登录界面
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // 当从个人资料界面返回时，刷新用户信息显示
        if (currentUsername != null && !currentUsername.isEmpty()) {
            setupUserInfo();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 如果按下返回键，显示退出确认对话框
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("退出应用")
                    .setMessage("您确定要退出药物提醒助手吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        // Clear any saved login state if needed
                        finish();
                        System.exit(0);
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}