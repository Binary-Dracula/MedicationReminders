package com.medication.reminders.view;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.medication.reminders.R;
import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.MedicationDao;
import com.medication.reminders.database.dao.MedicationIntakeRecordDao;
import com.medication.reminders.database.dao.MedicationScheduleDao;
import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.database.entity.MedicationIntakeRecord;
import com.medication.reminders.database.entity.MedicationSchedule;
import com.medication.reminders.utils.ReminderCalculator;
import com.medication.reminders.utils.ReminderScheduler;

/**
 * 接收闹钟触发，弹出通知。支持两个动作：延迟10分钟、已服用
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "medication_reminder_channel";
    public static final String ACTION_REMIND = "com.medication.reminders.ACTION_REMIND";
    public static final String ACTION_SNOOZE = "com.medication.reminders.ACTION_SNOOZE";
    public static final String ACTION_TAKEN = "com.medication.reminders.ACTION_TAKEN";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || context == null) return;
        String action = intent.getAction();
        long scheduleId = intent.getLongExtra("schedule_id", -1);
        long medicationId = intent.getLongExtra("medication_id", -1);

        MedicationDatabase db = MedicationDatabase.getDatabase(context);
        MedicationScheduleDao scheduleDao = db.medicationScheduleDao();
        MedicationIntakeRecordDao recordDao = db.medicationIntakeRecordDao();

        if (ACTION_SNOOZE.equals(action)) {
            // 延迟10分钟
            long next = System.currentTimeMillis() + 10 * 60 * 1000L;
            scheduleDao.updateNextReminder(scheduleId, next, System.currentTimeMillis());
            ReminderScheduler.scheduleNext(context, scheduleDao.getByIdSync(scheduleId));
            return;
        }

        if (ACTION_TAKEN.equals(action)) {
            // 记录已服用，写入用药记录并计算下一次
            if (scheduleId > 0 && medicationId > 0) {
                // 获取药物信息以创建用药记录
                MedicationDao medicationDao = db.medicationDao();
                MedicationInfo medication = medicationDao.getMedicationByIdSync(medicationId);
                
                if (medication != null) {
                    MedicationIntakeRecord record = new MedicationIntakeRecord();
                    record.setMedicationName(medication.getName());
                    record.setIntakeTime(System.currentTimeMillis());
                    record.setDosageTaken(medication.getDosagePerIntake());
                    recordDao.insertIntakeRecord(record);
                }

                MedicationSchedule s = scheduleDao.getByIdSync(scheduleId);
                long next = ReminderCalculator.computeNextReminderEpochMillis(s, System.currentTimeMillis());
                scheduleDao.updateNextReminder(scheduleId, next, System.currentTimeMillis());
                s.setNextReminderAt(next);
                ReminderScheduler.scheduleNext(context, s);
            }
            return;
        }

        // 默认：闹钟触发 -> 展示通知
        if (ACTION_REMIND.equals(action)) {
            showNotification(context, scheduleId, medicationId);
        }
    }

    private void showNotification(Context context, long scheduleId, long medicationId) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        ensureChannel(nm);

        // 延迟10分钟
        Intent snooze = new Intent(context, ReminderReceiver.class);
        snooze.setAction(ACTION_SNOOZE);
        snooze.putExtra("schedule_id", scheduleId);
        snooze.putExtra("medication_id", medicationId);
        PendingIntent snoozePi = PendingIntent.getBroadcast(context, (int) (scheduleId * 10 + 1), snooze, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 已服用
        Intent taken = new Intent(context, ReminderReceiver.class);
        taken.setAction(ACTION_TAKEN);
        taken.putExtra("schedule_id", scheduleId);
        taken.putExtra("medication_id", medicationId);
        PendingIntent takenPi = PendingIntent.getBroadcast(context, (int) (scheduleId * 10 + 2), taken, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 点击通知打开主界面（或具体详情页，这里打开主界面）
        Intent openApp = new Intent(context, MainActivity.class);
        PendingIntent contentPi = PendingIntent.getActivity(context, (int) (scheduleId * 10 + 3), openApp, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medication_default)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("到点提醒：请按时服药")
                .setContentIntent(contentPi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_arrow_back, "延迟10分钟", snoozePi)
                .addAction(R.drawable.ic_add_medication, "已服用", takenPi);

        nm.notify((int) scheduleId, b.build());
    }

    private void ensureChannel(NotificationManager nm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "用药提醒", NotificationManager.IMPORTANCE_HIGH);
            ch.enableLights(true);
            ch.setLightColor(Color.GREEN);
            ch.enableVibration(true);
            nm.createNotificationChannel(ch);
        }
    }
}

