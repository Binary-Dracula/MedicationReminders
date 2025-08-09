package com.medication.reminders.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 用药记录实体类
 * 用于记录用户的用药历史，包含药物名称、服用时间和服用剂量
 * 根据库存跟踪功能需求简化设计，只保留核心字段
 */
@Entity(tableName = "medication_intake_record")
public class MedicationIntakeRecord {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "medication_name")
    private String medicationName;          // 药物名称
    
    @ColumnInfo(name = "intake_time")
    private long intakeTime;                // 服用时间（时间戳）
    
    @ColumnInfo(name = "dosage_taken")
    private int dosageTaken;                // 服用剂量
    
    /**
     * 默认构造函数
     */
    public MedicationIntakeRecord() {
        this.intakeTime = System.currentTimeMillis();
        this.dosageTaken = 1;
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param medicationName 药物名称
     * @param intakeTime 服用时间
     * @param dosageTaken 服用剂量
     */
    @Ignore
    public MedicationIntakeRecord(String medicationName, long intakeTime, int dosageTaken) {
        this.medicationName = medicationName;
        this.intakeTime = intakeTime;
        this.dosageTaken = dosageTaken;
    }
    
    /**
     * 简化构造函数，使用当前时间
     * 
     * @param medicationName 药物名称
     * @param dosageTaken 服用剂量
     */
    @Ignore
    public MedicationIntakeRecord(String medicationName, int dosageTaken) {
        this.medicationName = medicationName;
        this.intakeTime = System.currentTimeMillis();
        this.dosageTaken = dosageTaken;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getMedicationName() {
        return medicationName;
    }
    
    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }
    
    public long getIntakeTime() {
        return intakeTime;
    }
    
    public void setIntakeTime(long intakeTime) {
        this.intakeTime = intakeTime;
    }
    
    public int getDosageTaken() {
        return dosageTaken;
    }
    
    public void setDosageTaken(int dosageTaken) {
        this.dosageTaken = dosageTaken;
    }
    
    @Override
    public String toString() {
        return "MedicationIntakeRecord{" +
                "id=" + id +
                ", medicationName='" + medicationName + '\'' +
                ", intakeTime=" + intakeTime +
                ", dosageTaken=" + dosageTaken +
                '}';
    }
}