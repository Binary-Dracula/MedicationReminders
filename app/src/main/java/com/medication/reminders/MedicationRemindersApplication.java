package com.medication.reminders;

import android.app.Application;
import android.os.Build;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

/**
 * Application class for MedicationReminders app
 * 应用程序全局初始化类
 */
public class MedicationRemindersApplication extends Application {
    private static MedicationRemindersApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // 应用程序初始化完成
        System.out.println("MedicationReminders应用程序已启动");

        // 初始化通知通道
        createReminderChannel();
    }

    private void createReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "medication_reminder_channel";
            String name = "用药提醒";
            NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
}