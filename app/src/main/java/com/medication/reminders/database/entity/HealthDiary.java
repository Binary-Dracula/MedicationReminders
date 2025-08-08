package com.medication.reminders.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 健康日记实体类，用于Room数据库
 * 存储用户的健康日记信息，包括日记内容、创建时间和修改时间
 */
@Entity(
    tableName = "health_diary",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("user_id")}
)
public class HealthDiary {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    /**
     * 用户ID，外键关联User表
     */
    @ColumnInfo(name = "user_id")
    @NonNull
    private long userId;
    
    /**
     * 日记内容，纯文本格式
     */
    @ColumnInfo(name = "content")
    @NonNull
    private String content;
    
    /**
     * 创建时间戳
     */
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    /**
     * 修改时间戳
     */
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    /**
     * 默认构造函数
     */
    public HealthDiary() {
        long currentTime = System.currentTimeMillis();
        this.createdAt = currentTime;
        this.updatedAt = currentTime;
    }
    
    /**
     * 基本构造函数
     * @param userId 用户ID
     * @param content 日记内容
     */
    @Ignore
    public HealthDiary(long userId, @NonNull String content) {
        this();
        this.userId = userId;
        this.content = content;
    }
    
    /**
     * 完整构造函数（用于测试）
     * @param userId 用户ID
     * @param content 日记内容
     * @param createdAt 创建时间
     * @param updatedAt 修改时间
     */
    @Ignore
    public HealthDiary(long userId, @NonNull String content, long createdAt, long updatedAt) {
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    @NonNull
    public String getContent() {
        return content;
    }
    
    public void setContent(@NonNull String content) {
        this.content = content;
        updateTimestamp();
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 更新修改时间戳
     */
    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 手动更新修改时间戳
     */
    public void markAsUpdated() {
        updateTimestamp();
    }
    
    /**
     * 验证日记内容是否有效
     * @return 如果内容不为空且长度在合理范围内则返回true
     */
    public boolean isContentValid() {
        return content != null && 
               !content.trim().isEmpty() && 
               content.length() <= 5000; // 最大5000字符
    }
    
    /**
     * 获取格式化的创建日期
     * @return 格式化的创建日期字符串 (yyyy-MM-dd HH:mm)
     */
    public String getFormattedCreatedDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return formatter.format(new Date(createdAt));
    }
    
    /**
     * 获取格式化的修改日期
     * @return 格式化的修改日期字符串 (yyyy-MM-dd HH:mm)
     */
    public String getFormattedUpdatedDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return formatter.format(new Date(updatedAt));
    }
    
    /**
     * 获取简短的创建日期（仅日期）
     * @return 格式化的创建日期字符串 (MM-dd)
     */
    public String getShortCreatedDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd", Locale.getDefault());
        return formatter.format(new Date(createdAt));
    }
    
    /**
     * 获取内容预览（用于列表显示）
     * @param maxLength 最大长度
     * @return 截断的内容预览
     */
    public String getContentPreview(int maxLength) {
        if (content == null) {
            return "";
        }
        
        String trimmedContent = content.trim();
        if (trimmedContent.length() <= maxLength) {
            return trimmedContent;
        }
        
        return trimmedContent.substring(0, maxLength) + "...";
    }
    
    /**
     * 获取默认的内容预览（100字符）
     * @return 截断的内容预览
     */
    public String getContentPreview() {
        return getContentPreview(100);
    }
    
    /**
     * 检查日记是否已被修改
     * @return 如果修改时间晚于创建时间则返回true
     */
    public boolean isModified() {
        return updatedAt > createdAt;
    }
    
    /**
     * 获取日记的字符数
     * @return 内容的字符数
     */
    public int getContentLength() {
        return content != null ? content.length() : 0;
    }
    
    /**
     * 检查日记是否为今天创建
     * @return 如果是今天创建则返回true
     */
    public boolean isCreatedToday() {
        long today = System.currentTimeMillis();
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        return (today - createdAt) < oneDayInMillis;
    }
    
    @Override
    public String toString() {
        return "HealthDiary{" +
                "id=" + id +
                ", userId=" + userId +
                ", content='" + getContentPreview(50) + '\'' +
                ", createdAt=" + getFormattedCreatedDate() +
                ", updatedAt=" + getFormattedUpdatedDate() +
                ", isModified=" + isModified() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        HealthDiary that = (HealthDiary) obj;
        return id == that.id &&
               userId == that.userId &&
               createdAt == that.createdAt &&
               updatedAt == that.updatedAt &&
               content.equals(that.content);
    }
    
    @Override
    public int hashCode() {
        int result = Long.hashCode(id);
        result = 31 * result + Long.hashCode(userId);
        result = 31 * result + content.hashCode();
        result = 31 * result + Long.hashCode(createdAt);
        result = 31 * result + Long.hashCode(updatedAt);
        return result;
    }
}