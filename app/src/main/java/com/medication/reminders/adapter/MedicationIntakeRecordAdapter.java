package com.medication.reminders.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medication.reminders.R;
import com.medication.reminders.database.entity.MedicationIntakeRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MedicationIntakeRecordAdapter - RecyclerView适配器，用于显示用药记录列表
 * 为老年用户提供友好的界面设计，包含大字体和高对比度
 */
public class MedicationIntakeRecordAdapter extends RecyclerView.Adapter<MedicationIntakeRecordAdapter.IntakeRecordViewHolder> {
    
    private List<MedicationIntakeRecord> intakeRecords;
    private OnIntakeRecordClickListener clickListener;
    private Context context;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    
    /**
     * 用药记录点击监听器接口
     */
    public interface OnIntakeRecordClickListener {
        void onIntakeRecordClick(MedicationIntakeRecord record);
    }
    
    /**
     * 构造函数
     * @param intakeRecords 用药记录列表
     * @param clickListener 点击监听器
     */
    public MedicationIntakeRecordAdapter(List<MedicationIntakeRecord> intakeRecords, OnIntakeRecordClickListener clickListener) {
        this.intakeRecords = intakeRecords;
        this.clickListener = clickListener;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    }
    
    @NonNull
    @Override
    public IntakeRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication_intake_record, parent, false);
        return new IntakeRecordViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull IntakeRecordViewHolder holder, int position) {
        MedicationIntakeRecord record = intakeRecords.get(position);
        holder.bind(record);
    }
    
    @Override
    public int getItemCount() {
        return intakeRecords != null ? intakeRecords.size() : 0;
    }
    
    /**
     * 更新用药记录列表数据
     * @param newRecords 新的用药记录列表
     */
    public void updateIntakeRecords(List<MedicationIntakeRecord> newRecords) {
        this.intakeRecords = newRecords;
        notifyDataSetChanged();
    }
    
    /**
     * 用药记录ViewHolder
     */
    class IntakeRecordViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvMedicationName;
        private TextView tvIntakeTime;
        private TextView tvDosageTaken;
        private TextView tvIntakeDate;
        
        public IntakeRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClickListener();
        }
        
        /**
         * 初始化视图组件
         */
        private void initViews() {
            tvMedicationName = itemView.findViewById(R.id.tvMedicationName);
            tvIntakeTime = itemView.findViewById(R.id.tvIntakeTime);
            tvDosageTaken = itemView.findViewById(R.id.tvDosageTaken);
            tvIntakeDate = itemView.findViewById(R.id.tvIntakeDate);
        }
        
        /**
         * 设置点击监听器
         */
        private void setupClickListener() {
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onIntakeRecordClick(intakeRecords.get(position));
                }
            });
        }
        
        /**
         * 绑定数据到视图
         * @param record 用药记录数据
         */
        public void bind(MedicationIntakeRecord record) {
            bindMedicationInfo(record);
            bindTimeInfo(record);
            bindDosageInfo(record);
            setupAccessibility(record);
        }
        
        /**
         * 绑定药物信息
         * @param record 用药记录数据
         */
        private void bindMedicationInfo(MedicationIntakeRecord record) {
            String medicationName = record.getMedicationName();
            if (medicationName != null && !medicationName.trim().isEmpty()) {
                tvMedicationName.setText(medicationName.trim());
            } else {
                tvMedicationName.setText("未知药物");
                tvMedicationName.setTextColor(context.getResources().getColor(R.color.high_contrast_text_secondary, null));
            }
        }
        
        /**
         * 绑定时间信息
         * @param record 用药记录数据
         */
        private void bindTimeInfo(MedicationIntakeRecord record) {
            Date intakeDate = new Date(record.getIntakeTime());
            
            // 设置日期
            String dateText = dateFormat.format(intakeDate);
            tvIntakeDate.setText(dateText);
            
            // 设置时间
            String timeText = timeFormat.format(intakeDate);
            tvIntakeTime.setText(timeText);
        }
        
        /**
         * 绑定剂量信息
         * @param record 用药记录数据
         */
        private void bindDosageInfo(MedicationIntakeRecord record) {
            String dosageText = String.format(Locale.CHINA, "%d", record.getDosageTaken());
            tvDosageTaken.setText(dosageText);
        }
        
        /**
         * 设置无障碍访问描述
         * @param record 用药记录数据
         */
        private void setupAccessibility(MedicationIntakeRecord record) {
            String medicationName = record.getMedicationName() != null ? 
                record.getMedicationName().trim() : "未知药物";
            
            Date intakeDate = new Date(record.getIntakeTime());
            String dateText = dateFormat.format(intakeDate);
            String timeText = timeFormat.format(intakeDate);
            
            String accessibilityText = String.format(Locale.CHINA,
                "用药记录，药物：%s，服用时间：%s %s，服用剂量：%d，点击查看详情",
                medicationName, dateText, timeText, record.getDosageTaken());
            
            itemView.setContentDescription(accessibilityText);
        }
    }
}