package com.medication.reminders.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.MedicationScheduleDao;
import com.medication.reminders.database.entity.MedicationSchedule;
import com.medication.reminders.utils.ReminderCalculator;
import com.medication.reminders.utils.ReminderScheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用药计划仓库：负责增删改查与调度
 */
public class MedicationScheduleRepository {

    private final MedicationScheduleDao scheduleDao;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Application application;

    public MedicationScheduleRepository(Application application) {
        this.application = application;
        MedicationDatabase db = MedicationDatabase.getDatabase(application);
        this.scheduleDao = db.medicationScheduleDao();
    }

    public LiveData<List<MedicationSchedule>> getAllEnabledSchedules() {
        return scheduleDao.getAllEnabledSchedules();
    }

    public LiveData<List<MedicationSchedule>> getSchedulesForMedication(long medicationId) {
        return scheduleDao.getSchedulesForMedication(medicationId);
    }

    public List<MedicationSchedule> getSchedulesForMedicationSync(long medicationId) {
        return scheduleDao.getSchedulesForMedicationSync(medicationId);
    }

    public void insertAndSchedule(MedicationSchedule schedule, Callback callback) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                schedule.setUpdatedAt(now);
                long next = ReminderCalculator.computeNextReminderEpochMillis(schedule, now);
                schedule.setNextReminderAt(next);
                long id = scheduleDao.insert(schedule);
                schedule.setId(id);
                ReminderScheduler.scheduleNext(application, schedule);
                if (callback != null) callback.onSuccess(id);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void updateAndReschedule(MedicationSchedule schedule, Callback callback) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                long next = ReminderCalculator.computeNextReminderEpochMillis(schedule, now);
                schedule.setNextReminderAt(next);
                schedule.setUpdatedAt(now);
                scheduleDao.update(schedule);
                ReminderScheduler.scheduleNext(application, schedule);
                if (callback != null) callback.onSuccess(schedule.getId());
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void disableAndCancel(long scheduleId) {
        executor.execute(() -> {
            scheduleDao.setEnabled(scheduleId, false, System.currentTimeMillis());
            ReminderScheduler.cancel(application, scheduleId);
        });
    }

    public interface Callback {
        void onSuccess(long id);
        void onError(String message);
    }
}

