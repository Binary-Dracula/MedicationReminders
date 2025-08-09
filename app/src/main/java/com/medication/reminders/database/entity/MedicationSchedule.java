package com.medication.reminders.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 用药计划实体
 * 支持每日、每周、每月、每隔X天四种类型，并支持每日多次提醒时间
 * 为简化存储，时间列表以逗号分隔的 HH:mm 字符串保存，例如："08:00,12:30,20:15"
 * 每周的星期选择使用位掩码（1-7位对应周一到周日），例如：周一周三周五为 0b0101010 = 42
 */
@Entity(tableName = "medication_schedules", indices = {@Index("medication_id")})
public class MedicationSchedule {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "medication_id")
    private long medicationId;

    @ColumnInfo(name = "cycle_type_index")
    private int cycleTypeIndex = 0; // 枚举索引，默认为0(DAILY)

    @ColumnInfo(name = "times_per_day")
    private int timesPerDay; // 每日次数（与 times_of_day 配合）

    @ColumnInfo(name = "times_of_day")
    private String timesOfDay; // 逗号分隔的 HH:mm 列表

    @ColumnInfo(name = "days_of_week_mask")
    private int daysOfWeekMask; // 周一=1<<6 ... 周日=1<<0，便于循环计算

    @ColumnInfo(name = "day_of_month")
    private int dayOfMonth; // 每月日（1-31），无效日期按月底处理

    @ColumnInfo(name = "interval_days")
    private int intervalDays; // 每隔X天的 X

    @ColumnInfo(name = "start_date_millis")
    private long startDateMillis; // 计划起始日期(当天0点时间戳)

    @ColumnInfo(name = "next_reminder_at")
    private long nextReminderAt; // 下一次提醒时间戳(ms)

    @ColumnInfo(name = "enabled")
    private boolean enabled = true;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public MedicationSchedule() {
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMedicationId() { return medicationId; }
    public void setMedicationId(long medicationId) { this.medicationId = medicationId; }

    public int getCycleTypeIndex() { return cycleTypeIndex; }
    public void setCycleTypeIndex(int cycleTypeIndex) { this.cycleTypeIndex = cycleTypeIndex; }

    public int getTimesPerDay() { return timesPerDay; }
    public void setTimesPerDay(int timesPerDay) { this.timesPerDay = timesPerDay; }

    public String getTimesOfDay() { return timesOfDay; }
    public void setTimesOfDay(String timesOfDay) { this.timesOfDay = timesOfDay; }

    public int getDaysOfWeekMask() { return daysOfWeekMask; }
    public void setDaysOfWeekMask(int daysOfWeekMask) { this.daysOfWeekMask = daysOfWeekMask; }

    public int getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(int dayOfMonth) { this.dayOfMonth = dayOfMonth; }

    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }

    public long getStartDateMillis() { return startDateMillis; }
    public void setStartDateMillis(long startDateMillis) { this.startDateMillis = startDateMillis; }

    public long getNextReminderAt() { return nextReminderAt; }
    public void setNextReminderAt(long nextReminderAt) { this.nextReminderAt = nextReminderAt; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}

