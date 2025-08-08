package com.medication.reminders.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.medication.reminders.database.entity.HealthDiary;

import java.util.List;

/**
 * 健康日记数据访问对象(DAO)
 * 提供健康日记的完整CRUD操作
 */
@Dao
public interface HealthDiaryDao {
    
    /**
     * 插入新的健康日记
     * @param diary 要插入的健康日记对象
     * @return 插入记录的ID
     */
    @Insert
    long insertDiary(HealthDiary diary);
    
    /**
     * 批量插入健康日记
     * @param diaries 要插入的健康日记列表
     * @return 插入记录的ID列表
     */
    @Insert
    List<Long> insertDiaries(List<HealthDiary> diaries);
    
    /**
     * 更新健康日记
     * @param diary 要更新的健康日记对象
     * @return 受影响的行数
     */
    @Update
    int updateDiary(HealthDiary diary);
    
    /**
     * 批量更新健康日记
     * @param diaries 要更新的健康日记列表
     * @return 受影响的行数
     */
    @Update
    int updateDiaries(List<HealthDiary> diaries);
    
    /**
     * 删除健康日记
     * @param diary 要删除的健康日记对象
     * @return 受影响的行数
     */
    @Delete
    int deleteDiary(HealthDiary diary);
    
    /**
     * 批量删除健康日记
     * @param diaries 要删除的健康日记列表
     * @return 受影响的行数
     */
    @Delete
    int deleteDiaries(List<HealthDiary> diaries);
    
    /**
     * 根据用户ID获取所有健康日记，按创建时间倒序排列
     * @param userId 用户ID
     * @return 健康日记列表的LiveData
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<HealthDiary>> getDiariesByUserId(long userId);
    
    /**
     * 根据用户ID同步获取所有健康日记，按创建时间倒序排列
     * @param userId 用户ID
     * @return 健康日记列表
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId ORDER BY created_at DESC")
    List<HealthDiary> getDiariesByUserIdSync(long userId);
    
    /**
     * 根据日记ID获取特定健康日记
     * @param id 日记ID
     * @return 健康日记的LiveData
     */
    @Query("SELECT * FROM health_diary WHERE id = :id LIMIT 1")
    LiveData<HealthDiary> getDiaryById(long id);
    
    /**
     * 根据日记ID同步获取特定健康日记
     * @param id 日记ID
     * @return 健康日记对象，如果不存在则返回null
     */
    @Query("SELECT * FROM health_diary WHERE id = :id LIMIT 1")
    HealthDiary getDiaryByIdSync(long id);
    
    /**
     * 根据用户ID获取健康日记总数
     * @param userId 用户ID
     * @return 日记总数的LiveData
     */
    @Query("SELECT COUNT(*) FROM health_diary WHERE user_id = :userId")
    LiveData<Integer> getDiaryCountByUserId(long userId);
    
    /**
     * 根据用户ID同步获取健康日记总数
     * @param userId 用户ID
     * @return 日记总数
     */
    @Query("SELECT COUNT(*) FROM health_diary WHERE user_id = :userId")
    int getDiaryCountByUserIdSync(long userId);
    
    /**
     * 根据用户ID和时间范围获取健康日记
     * @param userId 用户ID
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 指定时间范围内的健康日记列表
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId AND created_at BETWEEN :startTime AND :endTime ORDER BY created_at DESC")
    LiveData<List<HealthDiary>> getDiariesByUserIdAndTimeRange(long userId, long startTime, long endTime);
    
    /**
     * 根据用户ID和时间范围同步获取健康日记
     * @param userId 用户ID
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 指定时间范围内的健康日记列表
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId AND created_at BETWEEN :startTime AND :endTime ORDER BY created_at DESC")
    List<HealthDiary> getDiariesByUserIdAndTimeRangeSync(long userId, long startTime, long endTime);
    
    /**
     * 根据用户ID获取最新的健康日记
     * @param userId 用户ID
     * @return 最新健康日记的LiveData
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1")
    LiveData<HealthDiary> getLatestDiaryByUserId(long userId);
    
    /**
     * 根据用户ID同步获取最新的健康日记
     * @param userId 用户ID
     * @return 最新健康日记对象，如果不存在则返回null
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1")
    HealthDiary getLatestDiaryByUserIdSync(long userId);
    
    /**
     * 根据用户ID获取分页的健康日记
     * @param userId 用户ID
     * @param limit 每页数量
     * @param offset 偏移量
     * @return 分页的健康日记列表
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    LiveData<List<HealthDiary>> getDiariesByUserIdPaged(long userId, int limit, int offset);
    
    /**
     * 根据用户ID同步获取分页的健康日记
     * @param userId 用户ID
     * @param limit 每页数量
     * @param offset 偏移量
     * @return 分页的健康日记列表
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    List<HealthDiary> getDiariesByUserIdPagedSync(long userId, int limit, int offset);
    
    /**
     * 根据内容关键词搜索健康日记
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 包含关键词的健康日记列表
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId AND content LIKE '%' || :keyword || '%' ORDER BY created_at DESC")
    LiveData<List<HealthDiary>> searchDiariesByKeyword(long userId, String keyword);
    
    /**
     * 根据内容关键词同步搜索健康日记
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 包含关键词的健康日记列表
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId AND content LIKE '%' || :keyword || '%' ORDER BY created_at DESC")
    List<HealthDiary> searchDiariesByKeywordSync(long userId, String keyword);
    
    /**
     * 根据内容模式搜索健康日记（支持LIKE模式）
     * @param userId 用户ID
     * @param searchPattern 搜索模式（如 %keyword%）
     * @return 匹配模式的健康日记列表
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId AND content LIKE :searchPattern ORDER BY created_at DESC")
    List<HealthDiary> searchDiariesByContent(long userId, String searchPattern);
    
    /**
     * 根据时间范围获取健康日记（简化版本）
     * @param userId 用户ID
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 指定时间范围内的健康日记列表
     */
    @Query("SELECT * FROM health_diary WHERE user_id = :userId AND created_at BETWEEN :startTime AND :endTime ORDER BY created_at DESC")
    List<HealthDiary> getDiariesByDateRange(long userId, long startTime, long endTime);
    
    /**
     * 根据用户ID删除所有健康日记
     * @param userId 用户ID
     * @return 受影响的行数
     */
    @Query("DELETE FROM health_diary WHERE user_id = :userId")
    int deleteAllDiariesByUserId(long userId);
    
    /**
     * 根据日记ID删除特定健康日记
     * @param id 日记ID
     * @return 受影响的行数
     */
    @Query("DELETE FROM health_diary WHERE id = :id")
    int deleteDiaryById(long id);
    
    /**
     * 根据用户ID和时间范围删除健康日记
     * @param userId 用户ID
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 受影响的行数
     */
    @Query("DELETE FROM health_diary WHERE user_id = :userId AND created_at BETWEEN :startTime AND :endTime")
    int deleteDiariesByUserIdAndTimeRange(long userId, long startTime, long endTime);
    
    /**
     * 获取数据库中所有健康日记的总数（用于调试和统计）
     * @return 所有健康日记的总数
     */
    @Query("SELECT COUNT(*) FROM health_diary")
    int getTotalDiaryCount();
    
    /**
     * 检查特定用户是否有健康日记
     * @param userId 用户ID
     * @return 如果用户有日记则返回true
     */
    @Query("SELECT EXISTS(SELECT 1 FROM health_diary WHERE user_id = :userId LIMIT 1)")
    boolean hasUserDiaries(long userId);
    
    /**
     * 获取用户最早的健康日记创建时间
     * @param userId 用户ID
     * @return 最早创建时间戳，如果没有日记则返回0
     */
    @Query("SELECT MIN(created_at) FROM health_diary WHERE user_id = :userId")
    long getEarliestDiaryTime(long userId);
    
    /**
     * 获取用户最晚的健康日记创建时间
     * @param userId 用户ID
     * @return 最晚创建时间戳，如果没有日记则返回0
     */
    @Query("SELECT MAX(created_at) FROM health_diary WHERE user_id = :userId")
    long getLatestDiaryTime(long userId);
}