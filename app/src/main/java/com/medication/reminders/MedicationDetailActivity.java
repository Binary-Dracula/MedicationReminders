package com.medication.reminders;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
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
    private TextView tvCreatedDate;
    private TextView tvUpdatedDate;
    private ImageView ivMedicationPhoto;
    
    private MedicationListViewModel viewModel;
    private long medicationId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_detail);
        
        // Get medication ID from intent
        medicationId = getIntent().getLongExtra("medication_id", -1);
        
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
            getSupportActionBar().setTitle("药物详情");
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
        tvMedicationColor.setText("颜色：" + colorDisplay);
        
        // Display dosage form
        String dosageFormDisplay = getDosageFormDisplayName(medication.getDosageForm());
        tvMedicationDosageForm.setText("剂型：" + dosageFormDisplay);
        
        // Display dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
        tvCreatedDate.setText("添加时间：" + sdf.format(new Date(medication.getCreatedAt())));
        tvUpdatedDate.setText("更新时间：" + sdf.format(new Date(medication.getUpdatedAt())));
        
        // Display photo
        displayMedicationPhoto(medication.getPhotoPath());
        
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
                ivMedicationPhoto.setContentDescription("药物照片");
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
        ivMedicationPhoto.setContentDescription("默认药物图标");
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