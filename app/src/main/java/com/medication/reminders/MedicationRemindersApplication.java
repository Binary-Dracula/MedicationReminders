package com.medication.reminders;

import android.app.Application;
import com.tencent.mmkv.MMKV;

/**
 * Application class for MedicationReminders app
 * Handles global initialization including MMKV setup
 */
public class MedicationRemindersApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize MMKV
        String rootDir = MMKV.initialize(this);
        System.out.println("MMKV root: " + rootDir);
    }
}