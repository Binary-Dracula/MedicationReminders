package com.medication.reminders;

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
    
    // Default constructor
    public MedicationInfo() {
    }
    
    // Constructor with all fields except id (auto-generated)
    @Ignore
    public MedicationInfo(String name, String color, String dosageForm, String photoPath, long createdAt, long updatedAt) {
        this.name = name;
        this.color = color;
        this.dosageForm = dosageForm;
        this.photoPath = photoPath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Constructor with required fields only
    @Ignore
    public MedicationInfo(String name, String color, String dosageForm) {
        this.name = name;
        this.color = color;
        this.dosageForm = dosageForm;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
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
    
    @Override
    public String toString() {
        return "MedicationInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", dosageForm='" + dosageForm + '\'' +
                ", photoPath='" + photoPath + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}