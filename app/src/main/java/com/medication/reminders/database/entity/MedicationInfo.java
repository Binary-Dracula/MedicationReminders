package com.medication.reminders.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * MedicationInfo entity class for Room database
 * Represents a medication record with all necessary information
 */
@Entity(tableName = "medications")
public class MedicationInfo {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "color")
    private String color;
    
    @ColumnInfo(name = "dosage_form")
    private String dosageForm;
    
    @ColumnInfo(name = "photo_path")
    private String photoPath;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    @ColumnInfo(name = "remaining_quantity")
    private int remainingQuantity;
    
    @ColumnInfo(name = "total_quantity")
    private int totalQuantity;
    
    @ColumnInfo(name = "unit")
    private String unit; // 单位：片、粒、毫升等
    
    @ColumnInfo(name = "dosage_per_intake")
    private int dosagePerIntake = 1; // 每次用量，默认为1
    
    @ColumnInfo(name = "low_stock_threshold")
    private int lowStockThreshold = 5; // 库存提醒阈值，默认为5
    
    // Default constructor
    public MedicationInfo() {
    }
    
    // Constructor with all fields except id (auto-generated)
    @Ignore
    public MedicationInfo(String name, String color, String dosageForm, String photoPath, 
                         long createdAt, long updatedAt, int remainingQuantity, 
                         int totalQuantity, String unit, int dosagePerIntake, int lowStockThreshold) {
        this.name = name;
        this.color = color;
        this.dosageForm = dosageForm;
        this.photoPath = photoPath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.remainingQuantity = remainingQuantity;
        this.totalQuantity = totalQuantity;
        this.unit = unit;
        this.dosagePerIntake = dosagePerIntake;
        this.lowStockThreshold = lowStockThreshold;
    }
    
    // Constructor with required fields only
    @Ignore
    public MedicationInfo(String name, String color, String dosageForm) {
        this.name = name;
        this.color = color;
        this.dosageForm = dosageForm;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.remainingQuantity = 0;
        this.totalQuantity = 0;
        this.unit = "片";
        this.dosagePerIntake = 1;
        this.lowStockThreshold = 5;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getDosageForm() {
        return dosageForm;
    }
    
    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }
    
    public String getPhotoPath() {
        return photoPath;
    }
    
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
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
    
    public int getRemainingQuantity() {
        return remainingQuantity;
    }
    
    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
    
    public int getTotalQuantity() {
        return totalQuantity;
    }
    
    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public int getDosagePerIntake() {
        return dosagePerIntake;
    }
    
    public void setDosagePerIntake(int dosagePerIntake) {
        this.dosagePerIntake = dosagePerIntake;
    }
    
    public int getLowStockThreshold() {
        return lowStockThreshold;
    }
    
    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
    
    /**
     * 获取剩余量百分比
     * @return 剩余量百分比 (0-100)
     */
    public int getRemainingPercentage() {
        if (totalQuantity <= 0) {
            return 0;
        }
        return (int) ((remainingQuantity * 100.0) / totalQuantity);
    }
    
    /**
     * 检查是否需要补充药品
     * @param threshold 阈值百分比
     * @return 是否需要补充
     */
    public boolean needsRefill(int threshold) {
        return getRemainingPercentage() <= threshold;
    }
    
    /**
     * 减少剩余量
     * @param amount 减少的数量
     */
    public void reduceQuantity(int amount) {
        this.remainingQuantity = Math.max(0, this.remainingQuantity - amount);
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 检查是否库存不足
     * @return 如果剩余量小于等于提醒阈值且大于0则返回true
     */
    public boolean isLowStock() {
        return remainingQuantity <= lowStockThreshold && remainingQuantity > 0;
    }
    
    /**
     * 检查是否缺货
     * @return 如果剩余量为0则返回true
     */
    public boolean isOutOfStock() {
        return remainingQuantity == 0;
    }
    
    @Override
    public String toString() {
        return "MedicationInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", dosageForm='" + dosageForm + '\'' +
                ", photoPath='" + photoPath + '\'' +
                ", remainingQuantity=" + remainingQuantity +
                ", totalQuantity=" + totalQuantity +
                ", unit='" + unit + '\'' +
                ", dosagePerIntake=" + dosagePerIntake +
                ", lowStockThreshold=" + lowStockThreshold +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}