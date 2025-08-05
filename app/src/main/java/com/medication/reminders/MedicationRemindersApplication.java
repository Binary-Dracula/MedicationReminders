package com.medication.reminders;

import android.app.Application;

/**
 * Application class for MedicationReminders app
 * 应用程序全局初始化类
 */
public class MedicationRemindersApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 应用程序初始化完成
        System.out.println("MedicationReminders应用程序已启动");
    }
}