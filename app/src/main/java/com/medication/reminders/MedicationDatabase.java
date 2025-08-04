package com.medication.reminders;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Room database class for medication management
 * Uses singleton pattern to ensure single database instance
 */
@Database(
    entities = {MedicationInfo.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class MedicationDatabase extends RoomDatabase {
    
    /**
     * Abstract method to get the MedicationDao
     * 
     * @return MedicationDao instance
     */
    public abstract MedicationDao medicationDao();
    
    // Singleton instance
    private static volatile MedicationDatabase INSTANCE;
    
    /**
     * Get the database instance using singleton pattern
     * Thread-safe implementation with double-checked locking
     * 
     * @param context Application context
     * @return MedicationDatabase instance
     */
    public static MedicationDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MedicationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        MedicationDatabase.class,
                        "medication_database"
                    )
                    // Allow queries on the main thread for simple operations
                    // Note: This should be used carefully and only for simple queries
                    .allowMainThreadQueries()
                    // Add any migration strategies here if needed in future versions
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Close the database instance
     * Useful for testing or when the application is being destroyed
     */
    public static void closeDatabase() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}