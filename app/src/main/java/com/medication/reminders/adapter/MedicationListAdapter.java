package com.medication.reminders.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medication.reminders.R;
import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.models.MedicationColor;
import com.medication.reminders.models.MedicationDosageForm;
import com.medication.reminders.utils.PhotoUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MedicationListAdapter - RecyclerView adapter for medication list
 * Displays medication information with photos in a list format
 */
public class MedicationListAdapter extends RecyclerView.Adapter<MedicationListAdapter.MedicationViewHolder> {
    
    private List<MedicationInfo> medications;
    private OnMedicationClickListener clickListener;
    private Context context;
    
    public interface OnMedicationClickListener {
        void onMedicationClick(MedicationInfo medication);
    }
    
    public MedicationListAdapter(List<MedicationInfo> medications, OnMedicationClickListener clickListener) {
        this.medications = medications;
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        MedicationInfo medication = medications.get(position);
        holder.bind(medication);
    }
    
    @Override
    public int getItemCount() {
        return medications.size();
    }
    
    /**
     * Update the medication list
     */
    public void updateMedications(List<MedicationInfo> newMedications) {
        this.medications = newMedications;
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder for medication items
     */
    class MedicationViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvMedicationName;
        private TextView tvMedicationDetails;
        private TextView tvCreatedDate;
        private TextView tvStockInfo;
        private ImageView ivMedicationPhoto;
        private View colorIndicator;
        
        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicationName = itemView.findViewById(R.id.tvMedicationName);
            tvMedicationDetails = itemView.findViewById(R.id.tvMedicationDetails);
            tvCreatedDate = itemView.findViewById(R.id.tvCreatedDate);
            tvStockInfo = itemView.findViewById(R.id.tvStockInfo);
            ivMedicationPhoto = itemView.findViewById(R.id.ivMedicationPhoto);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onMedicationClick(medications.get(position));
                }
            });
        }
        
        public void bind(MedicationInfo medication) {
            // Set medication name
            tvMedicationName.setText(medication.getName());
            
            // Set medication details (color and dosage form)
            String details = getColorDisplayName(medication.getColor()) + " • " + 
                           getDosageFormDisplayName(medication.getDosageForm());
            tvMedicationDetails.setText(details);
            
            // Set created date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
            String dateText = "添加于 " + sdf.format(new Date(medication.getCreatedAt()));
            tvCreatedDate.setText(dateText);
            
            // Set color indicator
            setColorIndicator(medication.getColor());
            
            // Set medication photo
            setMedicationPhoto(medication.getPhotoPath());

            // Stock info
            int total = medication.getTotalQuantity();
            int remaining = medication.getRemainingQuantity();
            String unit = medication.getUnit() == null ? "" : medication.getUnit();
            int percent = medication.getRemainingPercentage();
            String stockText = context.getString(R.string.medication_stock_info, remaining, total, unit, percent);
            tvStockInfo.setText(stockText);
            
            // Set content descriptions for accessibility
            itemView.setContentDescription("药物：" + medication.getName() + "，" + details);
        }
        
        /**
         * Set color indicator based on medication color
         */
        private void setColorIndicator(String color) {
            int colorRes = getColorResource(color);
            if (colorRes != 0) {
                colorIndicator.setBackgroundColor(context.getResources().getColor(colorRes, null));
            } else {
                colorIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray, null));
            }
        }
        
        /**
         * Set medication photo or default icon
         */
        private void setMedicationPhoto(String photoPath) {
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
                return medicationColor.getDisplayName(context);
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
                return form.getDisplayName(context);
            } catch (IllegalArgumentException e) {
                return dosageForm;
            }
        }
        
        /**
         * Get color resource for color indicator
         */
        private int getColorResource(String color) {
            switch (color.toUpperCase()) {
                case "WHITE": return android.R.color.white;
                case "YELLOW": return android.R.color.holo_orange_light;
                case "BLUE": return android.R.color.holo_blue_light;
                case "RED": return android.R.color.holo_red_light;
                case "GREEN": return android.R.color.holo_green_light;
                case "PINK": return R.color.medication_pink;
                case "ORANGE": return android.R.color.holo_orange_light;
                case "BROWN": return R.color.medication_brown;
                case "PURPLE": return android.R.color.holo_purple;
                case "CLEAR": return android.R.color.transparent;
                default: return 0;
            }
        }
    }
}