package com.medication.reminders.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.medication.reminders.database.entity.MedicationSchedule;
import com.medication.reminders.view.ReminderReceiver;

/**
 * 使用 AlarmManager 调度下一次提醒
 */
public class ReminderScheduler {

    public static void scheduleNext(Context context, MedicationSchedule schedule) {
        if (context == null || schedule == null || !schedule.isEnabled()) return;
        long triggerAt = schedule.getNextReminderAt();
        if (triggerAt <= 0) return;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = buildPendingIntent(context, schedule);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    public static void cancel(Context context, long scheduleId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = buildPendingIntent(context, scheduleId);
        am.cancel(pi);
    }

    private static PendingIntent buildPendingIntent(Context context, MedicationSchedule schedule) {
        Intent i = new Intent(context, ReminderReceiver.class);
        i.setAction("com.medication.reminders.ACTION_REMIND");
        i.putExtra("schedule_id", schedule.getId());
        i.putExtra("medication_id", schedule.getMedicationId());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, (int) schedule.getId(), i, flags);
    }

    private static PendingIntent buildPendingIntent(Context context, long scheduleId) {
        Intent i = new Intent(context, ReminderReceiver.class);
        i.setAction("com.medication.reminders.ACTION_REMIND");
        i.putExtra("schedule_id", scheduleId);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, (int) scheduleId, i, flags);
    }
}

