package com.medication.reminders.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.medication.reminders.R;
import com.medication.reminders.repository.UserRepository;
import com.medication.reminders.utils.ExactAlarmPermissionHelper;

/**
 * MainActivity - 登录成功后的主界面
 * 提供药物管理、健康日记、用药记录等核心功能入口
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvUserInfo;
    private Button btnAddMedication;
    private Button btnViewMedicationList;
    private Button btnHealthDiary;
    private Button btnIntakeRecord;
    private Button btnProfile;
    private static final int REQ_POST_NOTIFICATIONS = 1001;

    private UserRepository userRepository;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userRepository = new UserRepository(this);

        initViews();
        setupUserInfo();
        setupClickListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("药物提醒助手");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        requestPermissionsIfNeeded();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnAddMedication = findViewById(R.id.btnAddMedication);
        btnViewMedicationList = findViewById(R.id.btnViewMedicationList);
        btnHealthDiary = findViewById(R.id.btnHealthDiary);
        btnIntakeRecord = findViewById(R.id.btnIntakeRecord);
        btnProfile = findViewById(R.id.btnProfile);
    }

    private void setupClickListeners() {
        btnAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMedicationActivity.class);
            startActivity(intent);
        });

        btnViewMedicationList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MedicationListActivity.class);
            startActivity(intent);
        });

        btnHealthDiary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HealthDiaryListActivity.class);
            startActivity(intent);
        });

        btnIntakeRecord.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MedicationIntakeRecordListActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> navigateToProfile());
    }

    private void setupUserInfo() {
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        long loginTime = intent.getLongExtra("login_time", System.currentTimeMillis());

        currentUsername = username;

        if (username != null && !username.isEmpty()) {
            tvWelcome.setText("欢迎回来，" + username + "！");

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm", java.util.Locale.CHINA);
            String formattedTime = sdf.format(new java.util.Date(loginTime));
            tvUserInfo.setText("登录时间：" + formattedTime);
        } else {
            tvWelcome.setText("欢迎使用药物提醒助手！");
            tvUserInfo.setText("您的健康管理伙伴");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToProfile() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            android.widget.Toast.makeText(this, "无法获取用户信息", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("username", currentUsername);
        startActivity(intent);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("您确定要退出当前账户吗？")
                .setPositiveButton("确定", (dialog, which) -> performLogout())
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("清除登录信息")
                .setMessage("是否同时清除保存的登录信息？")
                .setPositiveButton("清除", (dialog, which) -> {
                    userRepository.clearSavedCredentials();
                    navigateToLogin();
                })
                .setNegativeButton("保留", (dialog, which) -> navigateToLogin())
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUsername != null && !currentUsername.isEmpty()) {
            setupUserInfo();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setTitle("退出应用")
                    .setMessage("您确定要退出药物提醒助手吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        finish();
                        System.exit(0);
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void requestPermissionsIfNeeded() {
        if (!ExactAlarmPermissionHelper.canScheduleExactAlarms(this)) {
            android.widget.Toast.makeText(this, getString(R.string.exact_alarm_permission_tip), android.widget.Toast.LENGTH_LONG).show();
            ExactAlarmPermissionHelper.requestExactAlarmPermission(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
            }
        }
    }

}