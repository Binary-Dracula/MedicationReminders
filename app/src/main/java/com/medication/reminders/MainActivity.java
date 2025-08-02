package com.medication.reminders;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity - Main interface after successful login
 * Placeholder for the main medication reminder functionality
 */
public class MainActivity extends AppCompatActivity {
    
    private TextView tvWelcome;
    private TextView tvUserInfo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        initViews();
        
        // Setup user information from login
        setupUserInfo();
        
        // Setup action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("药物提醒助手");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }
    
    /**
     * Initialize UI components
     */
    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserInfo = findViewById(R.id.tvUserInfo);
    }
    
    /**
     * Setup user information from login intent
     */
    private void setupUserInfo() {
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        long loginTime = intent.getLongExtra("login_time", System.currentTimeMillis());
        
        if (username != null && !username.isEmpty()) {
            tvWelcome.setText("欢迎回来，" + username + "！");
            
            // Format login time for elderly users
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm", java.util.Locale.CHINA);
            String formattedTime = sdf.format(new java.util.Date(loginTime));
            tvUserInfo.setText("登录时间：" + formattedTime);
        } else {
            tvWelcome.setText("欢迎使用药物提醒助手！");
            tvUserInfo.setText("您的健康管理伙伴");
        }
    }
    
    @Override
    public void onBackPressed() {
        // Override back button to prevent going back to login
        // Show confirmation dialog for elderly users
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
    }
}