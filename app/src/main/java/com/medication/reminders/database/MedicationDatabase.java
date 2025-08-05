package com.medication.reminders.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.medication.reminders.database.dao.MedicationDao;
import com.medication.reminders.database.dao.UserDao;
import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.database.entity.User;
import com.medication.reminders.utils.Converters;


/**
 * Room数据库类，用于药物和用户管理
 * 使用单例模式确保数据库实例唯一
 */
@Database(
    entities = {MedicationInfo.class, User.class},
    version = 2,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class MedicationDatabase extends RoomDatabase {
    
    /**
     * 获取MedicationDao的抽象方法
     * 
     * @return MedicationDao实例
     */
    public abstract MedicationDao medicationDao();
    
    /**
     * 获取UserDao的抽象方法
     * 
     * @return UserDao实例
     */
    public abstract UserDao userDao();
    
    // Singleton instance
    private static volatile MedicationDatabase INSTANCE;
    
    /**
     * 使用单例模式获取数据库实例
     * 线程安全的双重检查锁定实现
     * 
     * @param context 应用程序上下文
     * @return MedicationDatabase实例
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
                    // 允许在主线程执行简单查询
                    // 注意：应谨慎使用，仅用于简单查询
                    .allowMainThreadQueries()
                    // 使用破坏性迁移策略进行数据库版本升级
                    // 这将删除现有数据并重新创建表结构
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * 关闭数据库实例
     * 用于测试或应用程序销毁时
     */
    public static void closeDatabase() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
    
    /**
     * 获取数据库版本信息
     * @return 当前数据库版本
     */
    public static int getDatabaseVersion() {
        return 2;
    }
    
    /**
     * 检查数据库是否已初始化
     * @return 如果数据库实例存在且打开则返回true
     */
    public static boolean isDatabaseInitialized() {
        return INSTANCE != null && INSTANCE.isOpen();
    }
}