package com.medication.reminders.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.medication.reminders.database.entity.MedicationIntakeRecord;

import java.util.List;

/**
 * 用药记录数据访问对象
 * 提供用药记录的基本CRUD操作
 */
@Dao
public interface MedicationIntakeRecordDao {
    
    /**
     * 插入新的用药记录
     * 
     * @param record 用药记录对象
     * @return 插入记录的ID
     */
    @Insert
    long insertIntakeRecord(MedicationIntakeRecord record);
    
    /**
     * 批量插入用药记录
     * 
     * @param records 用药记录列表
     * @return 插入记录的ID列表
     */
    @Insert
    List<Long> insertIntakeRecords(List<MedicationIntakeRecord> records);
    
    /**
     * 更新用药记录
     * 
     * @param record 要更新的用药记录
     * @return 更新的记录数量
     */
    @Update
    int updateIntakeRecord(MedicationIntakeRecord record);
    
    /**
     * 删除用药记录
     * 
     * @param record 要删除的用药记录
     * @return 删除的记录数量
     */
    @Delete
    int deleteIntakeRecord(MedicationIntakeRecord record);
    
    /**
     * 根据ID获取用药记录
     * 
     * @param recordId 记录ID
     * @return 用药记录的LiveData
     */
    @Query("SELECT * FROM medication_intake_record WHERE id = :recordId")
    LiveData<MedicationIntakeRecord> getIntakeRecordById(long recordId);
    
    /**
     * 获取所有用药记录，按服用时间倒序排列
     * 
     * @return 用药记录列表的LiveData
     */
    @Query("SELECT * FROM medication_intake_record ORDER BY intake_time DESC")
    LiveData<List<MedicationIntakeRecord>> getAllIntakeRecords();
    
    /**
     * 根据药物名称获取用药记录
     * 
     * @param medicationName 药物名称
     * @return 该药物的用药记录列表的LiveData
     */
    @Query("SELECT * FROM medication_intake_record WHERE medication_name = :medicationName ORDER BY intake_time DESC")
    LiveData<List<MedicationIntakeRecord>> getIntakeRecordsByMedicationName(String medicationName);
    
    /**
     * 获取指定时间范围内的用药记录
     * 
     * @param startTime 开始时间（时间戳）
     * @param endTime 结束时间（时间戳）
     * @return 时间范围内的用药记录列表的LiveData
     */
    @Query("SELECT * FROM medication_intake_record WHERE intake_time BETWEEN :startTime AND :endTime ORDER BY intake_time DESC")
    LiveData<List<MedicationIntakeRecord>> getIntakeRecordsByTimeRange(long startTime, long endTime);
    
    /**
     * 获取最近的用药记录（限制数量）
     * 
     * @param limit 记录数量限制
     * @return 最近的用药记录列表的LiveData
     */
    @Query("SELECT * FROM medication_intake_record ORDER BY intake_time DESC LIMIT :limit")
    LiveData<List<MedicationIntakeRecord>> getRecentIntakeRecords(int limit);
    
    /**
     * 获取用药记录总数
     * 
     * @return 用药记录总数的LiveData
     */
    @Query("SELECT COUNT(*) FROM medication_intake_record")
    LiveData<Integer> getIntakeRecordCount();
    
    /**
     * 根据药物名称获取用药记录总数
     * 
     * @param medicationName 药物名称
     * @return 该药物的用药记录总数的LiveData
     */
    @Query("SELECT COUNT(*) FROM medication_intake_record WHERE medication_name = :medicationName")
    LiveData<Integer> getIntakeRecordCountByMedicationName(String medicationName);
    
    /**
     * 删除所有用药记录
     * 
     * @return 删除的记录数量
     */
    @Query("DELETE FROM medication_intake_record")
    int deleteAllIntakeRecords();
    
    /**
     * 根据药物名称删除用药记录
     * 
     * @param medicationName 药物名称
     * @return 删除的记录数量
     */
    @Query("DELETE FROM medication_intake_record WHERE medication_name = :medicationName")
    int deleteIntakeRecordsByMedicationName(String medicationName);
    
    /**
     * 根据ID获取用药记录（同步方法）
     * 
     * @param recordId 记录ID
     * @return 用药记录对象，如果不存在则返回null
     */
    @Query("SELECT * FROM medication_intake_record WHERE id = :recordId")
    MedicationIntakeRecord getIntakeRecordByIdSync(long recordId);
}