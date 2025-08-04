package com.medication.reminders;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.IOException;

import com.medication.reminders.databinding.ActivityAddMedicationBinding;

/**
 * Activity for adding new medications to the system
 * Implements elderly-friendly UI design with accessibility features
 * Follows MVVM architecture pattern with data binding
 */
public class AddMedicationActivity extends AppCompatActivity {
    
    private ActivityAddMedicationBinding binding;
    private AddMedicationViewModel viewModel;
    
    // Spinner adapters
    private ArrayAdapter<String> colorAdapter;
    private ArrayAdapter<String> dosageFormAdapter;
    
    // Photo handling
    private static final int CAMERA_REQUEST = 101;
    private static final int GALLERY_REQUEST = 102;
    
    // Photo file for camera capture
    private File currentPhotoFile;
    private Uri currentPhotoUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewBinding
        binding = ActivityAddMedicationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AddMedicationViewModel.class);
        
        // Setup UI components
        setupToolbar();
        setupUI();
        setupSpinners();
        setupObservers();
        setupClickListeners();
    }
    
    /**
     * Setup toolbar with navigation and elderly-friendly styling
     */
    private void setupToolbar() {
        // Use the existing action bar from the theme
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.add_medication_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    
    /**
     * Setup UI components with elderly-friendly configurations
     */
    private void setupUI() {
        // Configure elderly-friendly UI settings
        configureAccessibility();
        
        // Set initial states
        binding.errorMessageTextView.setVisibility(View.GONE);
        binding.photoPreviewImageView.setVisibility(View.GONE);
        
        // Configure text input
        setupTextInputs();
    }
    
    /**
     * Configure accessibility features for elderly users
     */
    private void configureAccessibility() {
        // Ensure minimum touch target sizes (48dp)
        int minTouchTarget = getResources().getDimensionPixelSize(R.dimen.min_touch_target);
        
        // Set minimum heights for interactive elements
        binding.saveButton.setMinHeight(minTouchTarget);
        binding.cancelButton.setMinHeight(minTouchTarget);
        binding.uploadPhotoButton.setMinHeight(minTouchTarget);
        // Spinners already have minimum height set in layout
        
        // Configure content descriptions for screen readers
        binding.medicationNameEditText.setContentDescription(getString(R.string.medication_name_hint));
        binding.colorSpinner.setContentDescription(getString(R.string.color_spinner_description));
        binding.dosageFormSpinner.setContentDescription(getString(R.string.dosage_form_spinner_description));
        binding.uploadPhotoButton.setContentDescription(getString(R.string.photo_upload_button_description));
        binding.saveButton.setContentDescription(getString(R.string.save_button_description));
        binding.cancelButton.setContentDescription(getString(R.string.cancel_button_description));
    }
    
    /**
     * Setup text input fields without real-time validation
     */
    private void setupTextInputs() {
        // Setup medication name input with basic text change handling
        binding.medicationNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update ViewModel with new text (no validation)
                viewModel.setMedicationName(s.toString());
                
                // Clear general error message when user starts typing
                if (binding.errorMessageTextView.getVisibility() == View.VISIBLE) {
                    hideErrorMessage();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                // Clear field-specific error when user starts typing
                if (binding.medicationNameLayout.getError() != null) {
                    binding.medicationNameLayout.setError(null);
                }
                
                // Provide character count feedback only (no validation)
                int length = s.length();
                if (length > 90) { // Warning at 90 characters (limit is 100)
                    binding.medicationNameLayout.setHelperText(
                        String.format("还可输入 %d 个字符", 100 - length));
                } else {
                    binding.medicationNameLayout.setHelperText(null);
                }
            }
        });
    }
    
    /**
     * Setup spinner adapters for color and dosage form selection
     */
    private void setupSpinners() {
        setupColorSpinner();
        setupDosageFormSpinner();
    }
    
    /**
     * Setup color selection spinner
     */
    private void setupColorSpinner() {
        // Create color options array
        MedicationColor[] colors = MedicationColor.getAllColors();
        String[] colorNames = new String[colors.length + 1]; // +1 for prompt
        colorNames[0] = getString(R.string.select_color); // Prompt text
        
        for (int i = 0; i < colors.length; i++) {
            colorNames[i + 1] = colors[i].getDisplayName(this);
        }
        
        // Create and set adapter
        colorAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, colorNames);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.colorSpinner.setAdapter(colorAdapter);
        
        // Set selection listener
        binding.colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip prompt item
                    MedicationColor selectedColor = colors[position - 1];
                    viewModel.setSelectedColor(selectedColor.name());
                    
                    // Clear general error message when user makes selection
                    if (binding.errorMessageTextView.getVisibility() == View.VISIBLE) {
                        hideErrorMessage();
                    }
                    
                    // Provide haptic feedback for selection
                    parent.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                } else {
                    viewModel.setSelectedColor("");
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setSelectedColor("");
            }
        });
    }
    
    /**
     * Setup dosage form selection spinner
     */
    private void setupDosageFormSpinner() {
        // Create dosage form options array
        MedicationDosageForm[] dosageForms = MedicationDosageForm.getAllDosageForms();
        String[] dosageFormNames = new String[dosageForms.length + 1]; // +1 for prompt
        dosageFormNames[0] = getString(R.string.select_dosage_form); // Prompt text
        
        for (int i = 0; i < dosageForms.length; i++) {
            dosageFormNames[i + 1] = dosageForms[i].getDisplayName(this);
        }
        
        // Create and set adapter
        dosageFormAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, dosageFormNames);
        dosageFormAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.dosageFormSpinner.setAdapter(dosageFormAdapter);
        
        // Set selection listener
        binding.dosageFormSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip prompt item
                    MedicationDosageForm selectedForm = dosageForms[position - 1];
                    viewModel.setSelectedDosageForm(selectedForm.name());
                    
                    // Clear general error message when user makes selection
                    if (binding.errorMessageTextView.getVisibility() == View.VISIBLE) {
                        hideErrorMessage();
                    }
                    
                    // Provide haptic feedback for selection
                    parent.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                } else {
                    viewModel.setSelectedDosageForm("");
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setSelectedDosageForm("");
            }
        });
    }
    
    /**
     * Setup observers for ViewModel LiveData
     */
    private void setupObservers() {
        // Observe form data changes for two-way data binding
        observeFormData();
        
        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                showErrorMessage(errorMessage);
                handleSaveError(errorMessage);
            } else {
                hideErrorMessage();
            }
        });
        
        // Observe field-specific errors
        observeFieldErrors();
        
        // Observe save success
        viewModel.getSaveSuccess().observe(this, success -> {
            if (success) {
                showSuccessMessage();
                // Navigation is handled in showSuccessMessage() with delay
            }
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            updateLoadingState(isLoading);
        });
        
        // Observe duplicate dialog
        viewModel.getShowDuplicateDialog().observe(this, showDialog -> {
            if (showDialog) {
                showDuplicateDialog();
            }
        });
        
        // Observe photo state
        observePhotoState();
    }
    
    /**
     * Observe form data changes for two-way data binding
     */
    private void observeFormData() {
        // Observe medication name changes from ViewModel
        viewModel.getMedicationName().observe(this, name -> {
            if (name != null && !name.equals(binding.medicationNameEditText.getText().toString())) {
                binding.medicationNameEditText.setText(name);
            }
        });
        
        // Observe color selection changes from ViewModel
        viewModel.getSelectedColor().observe(this, color -> {
            updateColorSpinnerSelection(color);
        });
        
        // Observe dosage form selection changes from ViewModel
        viewModel.getSelectedDosageForm().observe(this, dosageForm -> {
            updateDosageFormSpinnerSelection(dosageForm);
        });
    }
    
    /**
     * Observe field-specific validation errors (only shown after save attempt)
     */
    private void observeFieldErrors() {
        // Observe name validation errors
        viewModel.getNameError().observe(this, error -> {
            binding.medicationNameLayout.setError(error);
        });
        
        // Observe color validation errors
        viewModel.getColorError().observe(this, error -> {
            // Show error in a toast or custom view since Spinner doesn't have setError
            if (error != null && !error.trim().isEmpty()) {
                showFieldError(getString(R.string.medication_color_label), error);
            }
        });
        
        // Observe dosage form validation errors
        viewModel.getDosageFormError().observe(this, error -> {
            // Show error in a toast or custom view since Spinner doesn't have setError
            if (error != null && !error.trim().isEmpty()) {
                showFieldError(getString(R.string.medication_dosage_form_label), error);
            }
        });
    }
    
    /**
     * Observe photo-related state changes
     */
    private void observePhotoState() {
        // Observe photo preview path
        viewModel.getPhotoPreviewPath().observe(this, photoPath -> {
            if (photoPath != null && !photoPath.trim().isEmpty()) {
                loadPhotoPreview(photoPath);
            } else {
                hidePhotoPreview();
            }
        });
        
        // Observe has photo state
        viewModel.getHasPhoto().observe(this, hasPhoto -> {
            updatePhotoButtonText(hasPhoto);
        });
    }
    
    /**
     * Setup click listeners for buttons
     */
    private void setupClickListeners() {
        // Save button click listener
        binding.saveButton.setOnClickListener(v -> {
            // Provide haptic feedback
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            
            // Clear any existing errors before validation
            hideErrorMessage();
            binding.medicationNameLayout.setError(null);
            
            // Trigger save
            viewModel.saveMedication();
        });
        
        // Cancel button click listener
        binding.cancelButton.setOnClickListener(v -> {
            // Provide haptic feedback
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            handleCancelAction();
        });
        
        // Upload photo button click listener
        binding.uploadPhotoButton.setOnClickListener(v -> {
            // Provide haptic feedback
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            handlePhotoUpload();
        });
    }
    
    /**
     * Handle cancel action with confirmation if form has data
     */
    private void handleCancelAction() {
        if (viewModel.hasFormData()) {
            showCancelConfirmationDialog();
        } else {
            finish();
        }
    }
    
    /**
     * Show cancel confirmation dialog
     */
    private void showCancelConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_cancel_title))
            .setMessage(getString(R.string.confirm_cancel_message))
            .setPositiveButton(getString(R.string.yes), (dialogInterface, which) -> {
                // Clean up any temporary photos
                cleanupTempFiles();
                
                // Set cancelled result
                setResult(RESULT_CANCELED);
                finish();
            })
            .setNegativeButton(getString(R.string.no), null)
            .setCancelable(true)
            .create();
        
        // Make dialog elderly-friendly
        dialog.show();
        
        // Increase button text size for elderly users
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
    }
    
    /**
     * Clean up temporary files
     */
    private void cleanupTempFiles() {
        // Clean up temporary camera file
        if (currentPhotoFile != null && currentPhotoFile.exists()) {
            currentPhotoFile.delete();
        }
        
        // Clean up any unsaved photos
        String currentPhotoPath = viewModel.getPhotoPath().getValue();
        if (currentPhotoPath != null && !currentPhotoPath.trim().isEmpty()) {
            // Only delete if the medication wasn't saved
            if (!viewModel.getSaveSuccess().getValue()) {
                PhotoUtils.deletePhoto(currentPhotoPath);
            }
        }
    }
    
    /**
     * Handle photo upload action
     */
    private void handlePhotoUpload() {
        showPhotoOptionsDialog();
    }
    
    /**
     * Show photo options dialog
     */
    private void showPhotoOptionsDialog() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.photo_options_title))
            .setItems(new String[]{
                getString(R.string.take_photo),
                getString(R.string.select_from_gallery)
            }, (dialog, which) -> {
                if (which == 0) {
                    requestCameraPermissionAndTakePhoto();
                } else {
                    openGallery();
                }
            })
            .show();
    }
    
    /**
     * Request camera permission and take photo
     */
    private void requestCameraPermissionAndTakePhoto() {
        PermissionUtils.requestCameraPermissionSimple(this, new PermissionUtils.SimplePermissionCallback() {
            @Override
            public void onGranted() {
                openCamera();
            }
            
            @Override
            public void onDenied() {
                showPermissionDeniedMessage(getString(R.string.camera_permission_reason));
            }
        });
    }
    
    /**
     * Open camera to take photo
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Check if camera app is available
        if (cameraIntent.resolveActivity(getPackageManager()) == null) {
            showErrorMessage(getString(R.string.error_camera_not_available));
            return;
        }
        
        // Create file for photo
        try {
            currentPhotoFile = createImageFile();
            if (currentPhotoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    currentPhotoFile);
                
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        } catch (IOException e) {
            showErrorMessage(getString(R.string.error_photo_save_failed));
        }
    }
    
    /**
     * Open gallery to select photo
     */
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        
        // Check if gallery app is available
        if (galleryIntent.resolveActivity(getPackageManager()) == null) {
            showErrorMessage(getString(R.string.error_gallery_not_available));
            return;
        }
        
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }
    
    /**
     * Create a temporary image file for camera capture
     */
    private File createImageFile() throws IOException {
        String fileName = PhotoUtils.generateUniqueFilename();
        File storageDir = new File(getFilesDir(), "temp_photos");
        
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new IOException("Failed to create temp photos directory");
        }
        
        return new File(storageDir, fileName);
    }
    
    /**
     * Show permission denied message
     */
    private void showPermissionDeniedMessage(String reason) {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_denied_message))
            .setMessage(reason)
            .setPositiveButton(getString(R.string.ok), null)
            .show();
    }
    
    /**
     * Show error message in UI
     */
    private void showErrorMessage(String message) {
        binding.errorMessageTextView.setText(message);
        binding.errorMessageTextView.setVisibility(View.VISIBLE);
    }
    
    /**
     * Hide error message from UI
     */
    private void hideErrorMessage() {
        binding.errorMessageTextView.setVisibility(View.GONE);
    }
    
    /**
     * Show field-specific error message
     */
    private void showFieldError(String fieldName, String error) {
        String message = fieldName + ": " + error;
        
        // Show error in the main error message area for better visibility
        showErrorMessage(message);
        
        // Also show as toast for immediate feedback
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // Highlight the relevant field
        if (fieldName.equals(getString(R.string.medication_color_label))) {
            highlightSpinner(binding.colorSpinner);
        } else if (fieldName.equals(getString(R.string.medication_dosage_form_label))) {
            highlightSpinner(binding.dosageFormSpinner);
        }
    }
    
    /**
     * Highlight spinner to indicate error
     */
    private void highlightSpinner(android.widget.Spinner spinner) {
        // Add visual feedback by changing background temporarily
        spinner.setBackgroundResource(R.drawable.error_message_background);
        
        // Reset background after 2 seconds
        spinner.postDelayed(() -> {
            spinner.setBackgroundResource(R.drawable.spinner_background);
        }, 2000);
        
        // Request focus for accessibility
        spinner.requestFocus();
    }
    
    /**
     * Show success message and handle navigation
     */
    private void showSuccessMessage() {
        // Show success message in the error message area with success styling
        binding.errorMessageTextView.setText(getString(R.string.medication_saved_successfully));
        binding.errorMessageTextView.setBackgroundResource(R.drawable.success_message_background);
        binding.errorMessageTextView.setTextColor(getResources().getColor(R.color.success_color));
        binding.errorMessageTextView.setVisibility(View.VISIBLE);
        
        // Also show toast for immediate feedback
        Toast.makeText(this, getString(R.string.medication_saved_successfully), 
            Toast.LENGTH_LONG).show();
        
        // Provide haptic feedback for success
        binding.saveButton.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);
        
        // Delay navigation to allow user to see success message
        binding.saveButton.postDelayed(() -> {
            finishWithResult();
        }, 1500); // 1.5 second delay
    }
    
    /**
     * Finish activity with success result
     */
    private void finishWithResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("medication_added", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    /**
     * Update loading state UI
     */
    private void updateLoadingState(boolean isLoading) {
        // Update save button
        binding.saveButton.setEnabled(!isLoading);
        binding.saveButton.setText(isLoading ? 
            getString(R.string.saving) : getString(R.string.save));
        
        // Disable other interactive elements during loading
        binding.cancelButton.setEnabled(!isLoading);
        binding.uploadPhotoButton.setEnabled(!isLoading);
        binding.medicationNameEditText.setEnabled(!isLoading);
        binding.colorSpinner.setEnabled(!isLoading);
        binding.dosageFormSpinner.setEnabled(!isLoading);
        
        // Update visual feedback
        float alpha = isLoading ? 0.6f : 1.0f;
        binding.cancelButton.setAlpha(alpha);
        binding.uploadPhotoButton.setAlpha(alpha);
        binding.medicationNameLayout.setAlpha(alpha);
        binding.colorSpinner.setAlpha(alpha);
        binding.dosageFormSpinner.setAlpha(alpha);
        
        // Show/hide loading indicator if needed
        if (isLoading) {
            showLoadingIndicator();
        } else {
            hideLoadingIndicator();
        }
    }
    
    /**
     * Show loading indicator
     */
    private void showLoadingIndicator() {
        // Create a simple loading message
        if (binding.errorMessageTextView.getVisibility() != View.VISIBLE) {
            binding.errorMessageTextView.setText(getString(R.string.saving));
            binding.errorMessageTextView.setBackgroundResource(R.drawable.loading_background);
            binding.errorMessageTextView.setTextColor(getResources().getColor(R.color.high_contrast_text_primary));
            binding.errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Hide loading indicator
     */
    private void hideLoadingIndicator() {
        // Reset error message view appearance
        binding.errorMessageTextView.setBackgroundResource(R.drawable.error_message_background);
        binding.errorMessageTextView.setTextColor(getResources().getColor(R.color.error_color));
    }
    
    /**
     * Show duplicate medication dialog
     */
    private void showDuplicateDialog() {
        String medicationName = viewModel.getDuplicateMedicationName().getValue();
        String message = getString(R.string.error_medication_name_exists);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm))
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes), (dialogInterface, which) -> {
                viewModel.handleDuplicateResponse(true);
            })
            .setNegativeButton(getString(R.string.no), (dialogInterface, which) -> {
                viewModel.handleDuplicateResponse(false);
                // Focus back to name field for editing
                binding.medicationNameEditText.requestFocus();
            })
            .setCancelable(false) // Force user to make a choice
            .create();
        
        // Make dialog elderly-friendly
        dialog.show();
        
        // Increase button text size for elderly users
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
        
        // Highlight the name field to show where the duplicate is
        binding.medicationNameLayout.setError(getString(R.string.error_medication_name_exists));
    }
    
    /**
     * Load photo preview
     */
    private void loadPhotoPreview(String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            hidePhotoPreview();
            return;
        }
        
        try {
            // Load bitmap from path
            Bitmap bitmap = PhotoUtils.loadPhotoFromPath(photoPath);
            if (bitmap != null) {
                // Scale bitmap for preview to save memory
                Bitmap previewBitmap = PhotoUtils.scalePhoto(bitmap, 400, 400);
                
                // Set bitmap to ImageView
                binding.photoPreviewImageView.setImageBitmap(previewBitmap);
                binding.photoPreviewImageView.setVisibility(View.VISIBLE);
                binding.photoPreviewImageView.setContentDescription(
                    getString(R.string.medication_photo_preview));
                
                // Add click listener for full-size preview
                binding.photoPreviewImageView.setOnClickListener(v -> {
                    showFullSizePhotoDialog(photoPath);
                });
                
                // Clean up original bitmap if different
                if (bitmap != previewBitmap) {
                    bitmap.recycle();
                }
            } else {
                hidePhotoPreview();
                showErrorMessage(getString(R.string.error_photo_load_failed));
            }
        } catch (Exception e) {
            hidePhotoPreview();
            showErrorMessage(getString(R.string.error_photo_load_failed));
        }
    }
    
    /**
     * Show full-size photo dialog
     */
    private void showFullSizePhotoDialog(String photoPath) {
        try {
            Bitmap bitmap = PhotoUtils.loadPhotoFromPath(photoPath);
            if (bitmap != null) {
                android.widget.ImageView imageView = new android.widget.ImageView(this);
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                
                new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.medication_photo_description))
                    .setView(imageView)
                    .setPositiveButton(getString(R.string.close), null)
                    .setNegativeButton(getString(R.string.delete), (dialog, which) -> {
                        confirmDeletePhoto();
                    })
                    .show();
            }
        } catch (Exception e) {
            showErrorMessage(getString(R.string.error_photo_load_failed));
        }
    }
    
    /**
     * Confirm photo deletion
     */
    private void confirmDeletePhoto() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage("您确定要删除这张照片吗？")
            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                deleteCurrentPhoto();
            })
            .setNegativeButton(getString(R.string.no), null)
            .show();
    }
    
    /**
     * Delete current photo
     */
    private void deleteCurrentPhoto() {
        String currentPath = viewModel.getPhotoPath().getValue();
        if (currentPath != null && !currentPath.trim().isEmpty()) {
            // Delete physical file
            PhotoUtils.deletePhoto(currentPath);
            
            // Clear from ViewModel
            viewModel.clearPhoto();
            
            // Show confirmation
            Toast.makeText(this, "照片已删除", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle save error with appropriate user feedback
     */
    private void handleSaveError(String errorMessage) {
        // Provide haptic feedback for error
        binding.saveButton.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
        
        // Focus on the first field that might need attention
        if (errorMessage.contains("名称")) {
            binding.medicationNameEditText.requestFocus();
        } else if (errorMessage.contains("颜色")) {
            binding.colorSpinner.requestFocus();
        } else if (errorMessage.contains("剂型")) {
            binding.dosageFormSpinner.requestFocus();
        }
        
        // Show retry option for certain errors
        if (errorMessage.contains("存储") || errorMessage.contains("系统")) {
            showRetryDialog(errorMessage);
        }
    }
    
    /**
     * Show retry dialog for recoverable errors
     */
    private void showRetryDialog(String errorMessage) {
        new AlertDialog.Builder(this)
            .setTitle("保存失败")
            .setMessage(errorMessage + "\n\n是否重试？")
            .setPositiveButton(getString(R.string.retry), (dialog, which) -> {
                // Clear error and retry save
                viewModel.clearErrors();
                viewModel.saveMedication();
            })
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }
    
    /**
     * Hide photo preview
     */
    private void hidePhotoPreview() {
        binding.photoPreviewImageView.setVisibility(View.GONE);
    }
    
    /**
     * Update photo button text based on photo state
     */
    private void updatePhotoButtonText(boolean hasPhoto) {
        if (hasPhoto) {
            binding.uploadPhotoButton.setText(getString(R.string.upload_photo) + " (已选择)");
        } else {
            binding.uploadPhotoButton.setText(getString(R.string.upload_photo));
        }
    }
    
    /**
     * Update color spinner selection based on ViewModel data
     */
    private void updateColorSpinnerSelection(String colorName) {
        if (colorName == null || colorName.trim().isEmpty()) {
            binding.colorSpinner.setSelection(0); // Select prompt item
            return;
        }
        
        MedicationColor[] colors = MedicationColor.getAllColors();
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].name().equals(colorName)) {
                binding.colorSpinner.setSelection(i + 1); // +1 for prompt item
                break;
            }
        }
    }
    
    /**
     * Update dosage form spinner selection based on ViewModel data
     */
    private void updateDosageFormSpinnerSelection(String dosageFormName) {
        if (dosageFormName == null || dosageFormName.trim().isEmpty()) {
            binding.dosageFormSpinner.setSelection(0); // Select prompt item
            return;
        }
        
        MedicationDosageForm[] dosageForms = MedicationDosageForm.getAllDosageForms();
        for (int i = 0; i < dosageForms.length; i++) {
            if (dosageForms[i].name().equals(dosageFormName)) {
                binding.dosageFormSpinner.setSelection(i + 1); // +1 for prompt item
                break;
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleCancelAction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        handleCancelAction();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != RESULT_OK) {
            return;
        }
        
        switch (requestCode) {
            case CAMERA_REQUEST:
                handleCameraResult();
                break;
            case GALLERY_REQUEST:
                handleGalleryResult(data);
                break;
        }
    }
    
    /**
     * Handle camera capture result
     */
    private void handleCameraResult() {
        if (currentPhotoFile == null || !currentPhotoFile.exists()) {
            showErrorMessage(getString(R.string.error_photo_save_failed));
            return;
        }
        
        try {
            // Load the captured photo
            Bitmap bitmap = PhotoUtils.loadPhotoFromPath(currentPhotoFile.getAbsolutePath());
            if (bitmap == null) {
                showErrorMessage(getString(R.string.error_photo_load_failed));
                return;
            }
            
            // Scale the photo to reduce memory usage
            Bitmap scaledBitmap = PhotoUtils.scalePhoto(bitmap, 800, 800);
            
            // Save the photo to permanent storage
            String savedPath = PhotoUtils.savePhotoToInternalStorage(this, scaledBitmap, null);
            if (savedPath != null) {
                // Update ViewModel with photo path
                viewModel.setPhotoPath(savedPath);
                
                // Clean up temporary file
                if (currentPhotoFile.exists()) {
                    currentPhotoFile.delete();
                }
                
                // Show success message
                Toast.makeText(this, getString(R.string.photo_saved_successfully), Toast.LENGTH_SHORT).show();
            } else {
                showErrorMessage(getString(R.string.error_photo_save_failed));
            }
            
            // Clean up bitmaps
            if (bitmap != scaledBitmap) {
                bitmap.recycle();
            }
            scaledBitmap.recycle();
            
        } catch (Exception e) {
            showErrorMessage(getString(R.string.error_photo_load_failed));
        }
    }
    
    /**
     * Handle gallery selection result
     */
    private void handleGalleryResult(@Nullable Intent data) {
        if (data == null || data.getData() == null) {
            showErrorMessage(getString(R.string.error_gallery_not_available));
            return;
        }
        
        Uri selectedImageUri = data.getData();
        
        try {
            // Load bitmap from URI
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            if (bitmap == null) {
                showErrorMessage(getString(R.string.error_photo_load_failed));
                return;
            }
            
            // Scale the photo to reduce memory usage
            Bitmap scaledBitmap = PhotoUtils.scalePhoto(bitmap, 800, 800);
            
            // Save the photo to internal storage
            String savedPath = PhotoUtils.savePhotoToInternalStorage(this, scaledBitmap, null);
            if (savedPath != null) {
                // Update ViewModel with photo path
                viewModel.setPhotoPath(savedPath);
                
                // Show success message
                Toast.makeText(this, getString(R.string.photo_saved_successfully), Toast.LENGTH_SHORT).show();
            } else {
                showErrorMessage(getString(R.string.error_photo_save_failed));
            }
            
            // Clean up bitmaps
            if (bitmap != scaledBitmap) {
                bitmap.recycle();
            }
            scaledBitmap.recycle();
            
        } catch (IOException e) {
            showErrorMessage(getString(R.string.error_photo_load_failed));
        } catch (Exception e) {
            showErrorMessage(getString(R.string.error_system_error));
        }
    }
    
    // Removed real-time validation methods - validation only happens on save
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up temporary files if activity is being destroyed
        if (isFinishing()) {
            cleanupTempFiles();
        }
        
        // Clean up binding
        binding = null;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Reset save success state when leaving activity
        if (viewModel != null) {
            viewModel.resetSaveSuccess();
        }
    }
}