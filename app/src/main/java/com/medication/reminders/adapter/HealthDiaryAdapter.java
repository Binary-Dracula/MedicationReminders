package com.medication.reminders.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medication.reminders.R;
import com.medication.reminders.database.entity.HealthDiary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * HealthDiaryAdapter - RecyclerView适配器，用于显示健康日记列表
 * 为老年用户提供友好的界面设计，包含大字体和高对比度
 */
public class HealthDiaryAdapter extends RecyclerView.Adapter<HealthDiaryAdapter.HealthDiaryViewHolder> {
    
    private List<HealthDiary> diaries;
    private OnDiaryClickListener clickListener;
    private Context context;
    private SimpleDateFormat dateFormat;
    
    /**
     * 日记点击监听器接口
     */
    public interface OnDiaryClickListener {
        void onDiaryClick(HealthDiary diary);
    }
    
    /**
     * 构造函数
     * @param diaries 健康日记列表
     * @param clickListener 点击监听器
     */
    public HealthDiaryAdapter(List<HealthDiary> diaries, OnDiaryClickListener clickListener) {
        this.diaries = diaries;
        this.clickListener = clickListener;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    }
    
    @NonNull
    @Override
    public HealthDiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_health_diary, parent, false);
        return new HealthDiaryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HealthDiaryViewHolder holder, int position) {
        HealthDiary diary = diaries.get(position);
        holder.bind(diary);
    }
    
    @Override
    public int getItemCount() {
        return diaries != null ? diaries.size() : 0;
    }
    
    /**
     * 更新日记列表数据
     * @param newDiaries 新的日记列表
     */
    public void updateDiaries(List<HealthDiary> newDiaries) {
        this.diaries = newDiaries;
        notifyDataSetChanged();
    }
    
    /**
     * 健康日记ViewHolder
     */
    class HealthDiaryViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvDiaryContent;
        private TextView tvCreatedAt;
        private TextView tvUpdatedAt;
        
        public HealthDiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClickListener();
        }
        
        /**
         * 初始化视图组件
         */
        private void initViews() {
            tvDiaryContent = itemView.findViewById(R.id.tvDiaryContent);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvUpdatedAt = itemView.findViewById(R.id.tvUpdatedAt);
        }
        
        /**
         * 设置点击监听器
         */
        private void setupClickListener() {
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onDiaryClick(diaries.get(position));
                }
            });
        }
        
        /**
         * 绑定数据到视图
         * @param diary 健康日记数据
         */
        public void bind(HealthDiary diary) {
            bindContent(diary);
            bindTimeInfo(diary);
            setupAccessibility(diary);
        }
        
        /**
         * 绑定日记内容
         * @param diary 健康日记数据
         */
        private void bindContent(HealthDiary diary) {
            String content = diary.getContent();
            if (content != null && !content.trim().isEmpty()) {
                tvDiaryContent.setText(content.trim());
            } else {
                tvDiaryContent.setText(context.getString(R.string.diary_empty_content));
                tvDiaryContent.setTextColor(context.getResources().getColor(R.color.high_contrast_text_secondary, null));
            }
        }
        
        /**
         * 绑定时间信息
         * @param diary 健康日记数据
         */
        private void bindTimeInfo(HealthDiary diary) {
            // 设置创建时间
            String createdTime = dateFormat.format(new Date(diary.getCreatedAt()));
            String createdText = context.getString(R.string.diary_created_at, createdTime);
            tvCreatedAt.setText(createdText);
            
            // 设置更新时间（如果有修改）
            if (diary.getUpdatedAt() > diary.getCreatedAt()) {
                String updatedTime = dateFormat.format(new Date(diary.getUpdatedAt()));
                String updatedText = context.getString(R.string.diary_updated_at, updatedTime);
                tvUpdatedAt.setText(updatedText);
                tvUpdatedAt.setVisibility(View.VISIBLE);
            } else {
                tvUpdatedAt.setVisibility(View.GONE);
            }
        }
        
        /**
         * 设置无障碍访问描述
         * @param diary 健康日记数据
         */
        private void setupAccessibility(HealthDiary diary) {
            String content = diary.getContent();
            String contentPreview = content != null && !content.trim().isEmpty() 
                ? content.trim() 
                : context.getString(R.string.diary_empty_content);
            
            // 限制内容预览长度以提高可访问性
            if (contentPreview.length() > 50) {
                contentPreview = contentPreview.substring(0, 50) + "...";
            }
            
            String createdTime = dateFormat.format(new Date(diary.getCreatedAt()));
            String accessibilityText = String.format(Locale.CHINA,
                "健康日记，内容：%s，创建时间：%s，点击查看详情",
                contentPreview, createdTime);
            
            itemView.setContentDescription(accessibilityText);
        }
    }
}