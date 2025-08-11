package com.medication.reminders.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.medication.reminders.R;
import com.medication.reminders.models.MedicationColor;
import com.medication.reminders.models.MedicationDosageForm;
import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.utils.PhotoUtils;
import com.medication.reminders.viewmodels.MedicationListViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * MedicationDetailActivity - Display detailed information about a medication
 * Shows all medication information including photo in a detailed view
 */
public class MedicationDetailActivity extends AppCompatActivity {
    
    private TextView tvMedicationName;
    private TextView tvMedicationColor;
    private TextView tvMedicationDosageForm;
    private TextView tvStockInfo;
    private TextView tvDosagePerIntake;
    private TextView tvLowStockThreshold;
    private TextView tvCreatedDate;
    private TextView tvUpdatedDate;
    private ImageView ivMedicationPhoto;
    private Button btnEditMedication;
    private Button btnOpenSchedule;
    
    // 库存状态警告相关组件
    private androidx.cardview.widget.CardView cardInventoryWarning;
    private TextView tvInventoryWarningTitle;
    private TextView tvInventoryWarningMessage;
    private Button btnReplenishStock;
    
    private MedicationListViewModel viewModel;
    private long medicationId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_detail);
        
        // Get medication ID from intent
        medicationId = getIntent().getLongExtra(getString(R.string.intent_key_medication_id), -1);
        
        if (medicationId == -1) {
            finish();
            return;
        }
        
        // Initialize views
        initViews();
        
        // Setup ViewModel
        setupViewModel();
        
        // Setup action bar
        setupActionBar();
        
        // Load medication details
        loadMedicationDetails();
    }
    
    /**
     * Initialize UI components
     */
    private void initViews() {
        tvMedicationName = findViewById(R.id.tvMedicationName);
        tvMedicationColor = findViewById(R.id.tvMedicationColor);
        tvMedicationDosageForm = findViewById(R.id.tvMedicationDosageForm);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvUpdatedDate = findViewById(R.id.tvUpdatedDate);
        ivMedicationPhoto = findViewById(R.id.ivMedicationPhoto);
        tvStockInfo = findViewById(R.id.tvStockInfo);
        tvDosagePerIntake = findViewById(R.id.tvDosagePerIntake);
        tvLowStockThreshold = findViewById(R.id.tvLowStockThreshold);
        btnEditMedication = findViewById(R.id.btnEditMedication);
        btnOpenSchedule = findViewById(R.id.btnOpenSchedule);
        
        // 库存状态警告相关组件
        cardInventoryWarning = findViewById(R.id.cardInventoryWarning);
        tvInventoryWarningTitle = findViewById(R.id.tvInventoryWarningTitle);
        tvInventoryWarningMessage = findViewById(R.id.tvInventoryWarningMessage);
        btnReplenishStock = findViewById(R.id.btnReplenishStock);

        btnEditMedication.setOnClickListener(v -> openEditPage());
        btnOpenSchedule.setOnClickListener(v -> openSchedulePage());
        btnReplenishStock.setOnClickListener(v -> openEditPage()); // 补充库存也是跳转到编辑页面
    }
    
    /**
     * Setup ViewModel
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MedicationListViewModel.class);
    }
    
    /**
     * Setup action bar
     */
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.medication_detail_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    /**
     * Load medication details
     */
    private void loadMedicationDetails() {
        viewModel.getAllMedications().observe(this, medications -> {
            if (medications != null) {
                for (MedicationInfo medication : medications) {
                    if (medication.getId() == medicationId) {
                        displayMedicationDetails(medication);
                        break;
                    }
                }
            }
        });
    }
    
    /**
     * Display medication details in UI
     */
    private void displayMedicationDetails(MedicationInfo medication) {
        tvMedicationName.setText(medication.getName());
        
        // Display color
        String colorDisplay = getColorDisplayName(medication.getColor());
        tvMedicationColor.setText(getString(R.string.medication_color_display, colorDisplay));
        
        // Display dosage form
        String dosageFormDisplay = getDosageFormDisplayName(medication.getDosageForm());
        tvMedicationDosageForm.setText(getString(R.string.medication_dosage_form_display, dosageFormDisplay));
        
        // Display dates
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.datetime_format_display), Locale.CHINA);
        tvCreatedDate.setText(getString(R.string.medication_created_time, sdf.format(new Date(medication.getCreatedAt()))));
        tvUpdatedDate.setText(getString(R.string.medication_updated_time, sdf.format(new Date(medication.getUpdatedAt()))));
        
        // Display photo
        displayMedicationPhoto(medication.getPhotoPath());

        // Display comprehensive inventory information
        displayInventoryInformation(medication);
        
        // Display inventory status warning if needed
        displayInventoryStatusWarning(medication);
        
        // Update action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(medication.getName());
        }
    }
    
    /**
     * Display medication photo or default icon
     */
    private void displayMedicationPhoto(String photoPath) {
        if (photoPath != null && !photoPath.isEmpty()) {
            Bitmap bitmap = PhotoUtils.loadPhotoFromPath(photoPath);
            if (bitmap != null) {
                ivMedicationPhoto.setImageBitmap(bitmap);
                ivMedicationPhoto.setContentDescription(getString(R.string.medication_photo_content_description));
            } else {
                setDefaultMedicationIcon();
            }
        } else {
            setDefaultMedicationIcon();
        }
    }
    
    /**
     * Set default medication icon when no photo is available
     */
    private void setDefaultMedicationIcon() {
        ivMedicationPhoto.setImageResource(R.drawable.ic_medication_default);
        ivMedicationPhoto.setContentDescription(getString(R.string.default_medication_icon_content_description));
    }
    
    /**
     * 显示完整的库存信息
     * @param medication 药物信息
     */
    private void displayInventoryInformation(MedicationInfo medication) {
        int total = medication.getTotalQuantity();
        int remaining = medication.getRemainingQuantity();
        String unit = medication.getUnit() == null ? getString(R.string.medication_unit_default) : medication.getUnit();
        int percent = medication.getRemainingPercentage();
        int dosagePerIntake = medication.getDosagePerIntake();
        int lowStockThreshold = medication.getLowStockThreshold();
        
        // 显示库存信息，根据状态设置不同颜色
        String stockInfo = getString(R.string.medication_stock_info, remaining, total, unit, percent);
        tvStockInfo.setText(stockInfo);
        
        // 根据库存状态设置文字颜色
        if (medication.isOutOfStock()) {
            tvStockInfo.setTextColor(getResources().getColor(R.color.stock_out, null));
        } else if (medication.isLowStock()) {
            tvStockInfo.setTextColor(getResources().getColor(R.color.stock_low, null));
        } else {
            tvStockInfo.setTextColor(getResources().getColor(R.color.stock_sufficient, null));
        }
        
        // 显示每次用量
        tvDosagePerIntake.setText(getString(R.string.dosage_per_intake_display, dosagePerIntake, unit));
        
        // 显示库存提醒阈值
        tvLowStockThreshold.setText(getString(R.string.low_stock_threshold_display, lowStockThreshold, unit));
    }
    
    /**
     * 显示库存状态警告信息
     * @param medication 药物信息
     */
    private void displayInventoryStatusWarning(MedicationInfo medication) {
        if (medication.isOutOfStock()) {
            // 缺货状态
            cardInventoryWarning.setVisibility(android.view.View.VISIBLE);
            tvInventoryWarningTitle.setText(getString(R.string.inventory_out_of_stock_title));
            tvInventoryWarningTitle.setTextColor(getResources().getColor(R.color.stock_out, null));
            tvInventoryWarningMessage.setText(getString(R.string.inventory_out_of_stock_message, medication.getName()));
            tvInventoryWarningMessage.setTextColor(getResources().getColor(R.color.stock_out, null));
            btnReplenishStock.setText(getString(R.string.replenish_stock_now));
            btnReplenishStock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.stock_out, null)));
        } else if (medication.isLowStock()) {
            // 库存不足状态
            cardInventoryWarning.setVisibility(android.view.View.VISIBLE);
            tvInventoryWarningTitle.setText(getString(R.string.inventory_low_stock_title));
            tvInventoryWarningTitle.setTextColor(getResources().getColor(R.color.stock_low, null));
            
            String unit = medication.getUnit() == null ? getString(R.string.medication_unit_default) : medication.getUnit();
            String warningMessage = getString(R.string.inventory_low_stock_message, 
                medication.getName(), 
                medication.getRemainingQuantity(), 
                unit,
                medication.getLowStockThreshold(),
                unit);
            tvInventoryWarningMessage.setText(warningMessage);
            tvInventoryWarningMessage.setTextColor(getResources().getColor(R.color.stock_low, null));
            btnReplenishStock.setText(getString(R.string.replenish_stock));
            btnReplenishStock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.stock_low, null)));
        } else {
            // 库存充足，隐藏警告卡片
            cardInventoryWarning.setVisibility(android.view.View.GONE);
        }
    }

    /** 跳转药物编辑页 */
    private void openEditPage() {
        Intent i = new Intent(this, AddMedicationActivity.class);
        i.putExtra(getString(R.string.intent_key_edit_medication_id), medicationId);
        startActivity(i);
    }

    /** 跳转提醒设置页 */
    private void openSchedulePage() {
        Intent i = new Intent(this, ScheduleEditActivity.class);
        i.putExtra(getString(R.string.intent_key_medication_id), medicationId);
        startActivity(i);
    }
    
    /**
     * Get display name for color
     */
    private String getColorDisplayName(String color) {
        try {
            MedicationColor medicationColor = MedicationColor.valueOf(color.toUpperCase());
            return medicationColor.getDisplayName(this);
        } catch (IllegalArgumentException e) {
            return color;
        }
    }
    
    /**
     * Get display name for dosage form
     */
    private String getDosageFormDisplayName(String dosageForm) {
        try {
            MedicationDosageForm form = MedicationDosageForm.valueOf(dosageForm.toUpperCase());
            return form.getDisplayName(this);
        } catch (IllegalArgumentException e) {
            return dosageForm;
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}