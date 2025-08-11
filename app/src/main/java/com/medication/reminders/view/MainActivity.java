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
            getSupportActionBar().setTitle(getString(R.string.main_title));
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
        String username = intent.getStringExtra(getString(R.string.intent_key_username));
        long loginTime = intent.getLongExtra(getString(R.string.intent_key_login_time), System.currentTimeMillis());

        currentUsername = username;

        if (username != null && !username.isEmpty()) {
            tvWelcome.setText(getString(R.string.welcome_back_message, username));

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(getString(R.string.datetime_format_display), java.util.Locale.CHINA);
            String formattedTime = sdf.format(new java.util.Date(loginTime));
            tvUserInfo.setText(getString(R.string.login_time_label, formattedTime));
        } else {
            tvWelcome.setText(getString(R.string.welcome_default_message));
            tvUserInfo.setText(getString(R.string.health_partner_message));
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
            android.widget.Toast.makeText(this, getString(R.string.cannot_get_user_info), android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(getString(R.string.intent_key_username), currentUsername);
        startActivity(intent);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout_dialog_title))
                .setMessage(getString(R.string.logout_dialog_message))
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> performLogout())
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.clear_login_dialog_title))
                .setMessage(getString(R.string.clear_login_dialog_message))
                .setPositiveButton(getString(R.string.clear_login_positive), (dialog, which) -> {
                    userRepository.clearSavedCredentials();
                    navigateToLogin();
                })
                .setNegativeButton(getString(R.string.keep_login_negative), (dialog, which) -> navigateToLogin())
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
                    .setTitle(getString(R.string.exit_app_dialog_title))
                    .setMessage(getString(R.string.exit_app_dialog_message))
                    .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                        finish();
                        System.exit(0);
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
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