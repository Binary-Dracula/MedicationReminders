package com.medication.reminders.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.medication.reminders.database.entity.MedicationSchedule;

import java.util.List;

@Dao
public interface MedicationScheduleDao {

    @Query("SELECT * FROM medication_schedules WHERE enabled = 1 ORDER BY next_reminder_at ASC")
    LiveData<List<MedicationSchedule>> getAllEnabledSchedules();

    @Query("SELECT * FROM medication_schedules WHERE enabled = 1 ORDER BY next_reminder_at ASC")
    List<MedicationSchedule> getAllEnabledSchedulesSync();

    @Query("SELECT * FROM medication_schedules WHERE medication_id = :medicationId ORDER BY created_at DESC")
    LiveData<List<MedicationSchedule>> getSchedulesForMedication(long medicationId);

    @Query("SELECT * FROM medication_schedules WHERE medication_id = :medicationId ORDER BY created_at DESC")
    List<MedicationSchedule> getSchedulesForMedicationSync(long medicationId);

    @Query("SELECT * FROM medication_schedules WHERE id = :id LIMIT 1")
    MedicationSchedule getByIdSync(long id);

    @Insert
    long insert(MedicationSchedule schedule);

    @Update
    int update(MedicationSchedule schedule);

    @Delete
    int delete(MedicationSchedule schedule);

    @Query("DELETE FROM medication_schedules WHERE id = :id")
    int deleteById(long id);

    @Query("UPDATE medication_schedules SET next_reminder_at = :nextAt, updated_at = :updatedAt WHERE id = :id")
    int updateNextReminder(long id, long nextAt, long updatedAt);

    @Query("UPDATE medication_schedules SET enabled = :enabled, updated_at = :updatedAt WHERE id = :id")
    int setEnabled(long id, boolean enabled, long updatedAt);
}

