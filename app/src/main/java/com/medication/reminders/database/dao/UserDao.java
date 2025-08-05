package com.medication.reminders.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.medication.reminders.database.entity.User;

import java.util.List;

/**
 * User实体的数据访问对象(DAO)
 * 定义所有用户管理的数据库操作
 */
@Dao
public interface UserDao {
    
    // ========== 基本CRUD操作 ==========
    
    /**
     * 插入新用户到数据库
     * @param user 要插入的用户
     * @return 插入用户的ID
     */
    @Insert
    long insertUser(User user);
    
    /**
     * 更新现有用户信息
     * @param user 要更新的用户
     * @return 更新的行数
     */
    @Update
    int updateUser(User user);
    
    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return 删除的行数
     */
    @Query("DELETE FROM users WHERE id = :id")
    int deleteUserById(long id);
    
    /**
     * 删除用户
     * @param user 要删除的用户
     * @return 删除的行数
     */
    @Delete
    int deleteUser(User user);
    
    // ========== 主要查询操作 ==========
    
    /**
     * 根据ID获取用户（同步方法）
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(long id);
    
    /**
     * 根据ID获取用户（LiveData方式）
     * @param id 用户ID
     * @return 用户的LiveData对象
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<User> getUserByIdLiveData(long id);
    
    /**
     * 根据用户名获取用户（同步方法）
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);
    
    /**
     * 根据用户名获取用户（LiveData方式）
     * @param username 用户名
     * @return 用户的LiveData对象
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    LiveData<User> getUserByUsernameLiveData(String username);
    
    /**
     * 根据邮箱获取用户（同步方法）
     * @param email 邮箱地址
     * @return 用户对象，如果不存在则返回null
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
    
    /**
     * 根据电话号码获取用户（同步方法）
     * @param phone 电话号码
     * @return 用户对象，如果不存在则返回null
     */
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    User getUserByPhone(String phone);
    
    /**
     * 获取所有用户（按创建时间排序）
     * @return 所有用户的LiveData列表
     */
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    LiveData<List<User>> getAllUsers();
    
    // ========== 注册时唯一性检查 ==========
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 用户名的数量（0表示不存在，1表示存在）
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int getUsernameCount(String username);
    
    /**
     * 检查邮箱是否存在
     * @param email 邮箱地址
     * @return 邮箱的数量（0表示不存在，1表示存在）
     */
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int getEmailCount(String email);
    
    /**
     * 检查电话号码是否存在
     * @param phone 电话号码
     * @return 电话号码的数量（0表示不存在，1表示存在）
     */
    @Query("SELECT COUNT(*) FROM users WHERE phone = :phone")
    int getPhoneCount(String phone);
    
    /**
     * 检查用户名是否存在（排除指定用户ID）
     * 用于更新用户信息时的唯一性检查
     * @param username 用户名
     * @param excludeId 要排除的用户ID
     * @return 用户名的数量
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username AND id != :excludeId")
    int getUsernameCountExcluding(String username, long excludeId);
    
    /**
     * 检查邮箱是否存在（排除指定用户ID）
     * @param email 邮箱地址
     * @param excludeId 要排除的用户ID
     * @return 邮箱的数量
     */
    @Query("SELECT COUNT(*) FROM users WHERE email = :email AND id != :excludeId")
    int getEmailCountExcluding(String email, long excludeId);
    
    /**
     * 检查电话号码是否存在（排除指定用户ID）
     * @param phone 电话号码
     * @param excludeId 要排除的用户ID
     * @return 电话号码的数量
     */
    @Query("SELECT COUNT(*) FROM users WHERE phone = :phone AND id != :excludeId")
    int getPhoneCountExcluding(String phone, long excludeId);
    
    // ========== 认证和会话管理 ==========
    
    /**
     * 登出所有用户（将所有用户的登录状态设为false）
     * @return 更新的行数
     */
    @Query("UPDATE users SET is_logged_in = 0")
    int logoutAllUsers();
    
    /**
     * 设置用户登录状态
     * @param userId 用户ID
     * @param loginTime 登录时间
     * @return 更新的行数
     */
    @Query("UPDATE users SET is_logged_in = 1, last_login_time = :loginTime WHERE id = :userId")
    int setUserLoggedIn(long userId, long loginTime);
    
    /**
     * 设置用户登出状态
     * @param userId 用户ID
     * @return 更新的行数
     */
    @Query("UPDATE users SET is_logged_in = 0 WHERE id = :userId")
    int setUserLoggedOut(long userId);
    
    /**
     * 获取当前登录的用户
     * @return 当前登录的用户，如果没有则返回null
     */
    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    User getCurrentLoggedInUser();
    
    /**
     * 获取当前登录的用户（LiveData方式）
     * @return 当前登录用户的LiveData对象
     */
    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    LiveData<User> getCurrentLoggedInUserLiveData();
    
    /**
     * 检查是否有用户登录
     * @return 登录用户的数量
     */
    @Query("SELECT COUNT(*) FROM users WHERE is_logged_in = 1")
    int getLoggedInUserCount();
    
    // ========== 登录尝试管理 ==========
    
    /**
     * 更新用户的登录尝试次数和时间
     * @param username 用户名
     * @param attempts 尝试次数
     * @param attemptTime 尝试时间
     * @return 更新的行数
     */
    @Query("UPDATE users SET login_attempts = :attempts, last_attempt_time = :attemptTime WHERE username = :username")
    int updateLoginAttempts(String username, int attempts, long attemptTime);
    
    /**
     * 重置用户的登录尝试次数
     * @param username 用户名
     * @return 更新的行数
     */
    @Query("UPDATE users SET login_attempts = 0, last_attempt_time = 0 WHERE username = :username")
    int resetLoginAttempts(String username);
    
    /**
     * 根据用户ID重置登录尝试次数
     * @param userId 用户ID
     * @return 更新的行数
     */
    @Query("UPDATE users SET login_attempts = 0, last_attempt_time = 0 WHERE id = :userId")
    int resetLoginAttemptsByUserId(long userId);
    
    /**
     * 获取用户的登录尝试次数
     * @param username 用户名
     * @return 登录尝试次数
     */
    @Query("SELECT login_attempts FROM users WHERE username = :username")
    int getLoginAttempts(String username);
    
    /**
     * 获取用户的最后尝试时间
     * @param username 用户名
     * @return 最后尝试时间戳
     */
    @Query("SELECT last_attempt_time FROM users WHERE username = :username")
    long getLastAttemptTime(String username);
    
    // ========== 记住我功能 ==========
    
    /**
     * 更新用户的记住我状态
     * @param userId 用户ID
     * @param rememberMe 是否记住
     * @return 更新的行数
     */
    @Query("UPDATE users SET remember_me = :rememberMe WHERE id = :userId")
    int updateRememberMe(long userId, boolean rememberMe);
    
    /**
     * 清除所有用户的记住我状态
     * @return 更新的行数
     */
    @Query("UPDATE users SET remember_me = 0")
    int clearAllRememberMe();
    
    /**
     * 获取被记住的用户
     * @return 被记住的用户，如果没有则返回null
     */
    @Query("SELECT * FROM users WHERE remember_me = 1 LIMIT 1")
    User getRememberedUser();
    
    /**
     * 获取被记住的用户（LiveData方式）
     * @return 被记住用户的LiveData对象
     */
    @Query("SELECT * FROM users WHERE remember_me = 1 LIMIT 1")
    LiveData<User> getRememberedUserLiveData();
    
    // ========== 密码管理 ==========
    
    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 更新的行数
     */
    @Query("UPDATE users SET password = :newPassword, updated_at = :updateTime WHERE id = :userId")
    int updatePassword(long userId, String newPassword, long updateTime);
    
    /**
     * 验证用户密码
     * @param username 用户名
     * @param password 密码
     * @return 匹配的用户数量（0表示密码错误，1表示密码正确）
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username AND password = :password")
    int validatePassword(String username, String password);
    
    // ========== 用户资料更新 ==========
    
    /**
     * 更新用户基本资料信息
     * @param userId 用户ID
     * @param fullName 完整姓名
     * @param gender 性别
     * @param birthDate 出生日期
     * @param updateTime 更新时间
     * @return 更新的行数
     */
    @Query("UPDATE users SET full_name = :fullName, gender = :gender, birth_date = :birthDate, updated_at = :updateTime WHERE id = :userId")
    int updateBasicProfile(long userId, String fullName, String gender, String birthDate, long updateTime);
    
    /**
     * 更新用户联系信息
     * @param userId 用户ID
     * @param email 邮箱
     * @param phone 电话
     * @param secondaryPhone 备用电话
     * @param updateTime 更新时间
     * @return 更新的行数
     */
    @Query("UPDATE users SET email = :email, phone = :phone, secondary_phone = :secondaryPhone, updated_at = :updateTime WHERE id = :userId")
    int updateContactInfo(long userId, String email, String phone, String secondaryPhone, long updateTime);
    
    /**
     * 更新用户紧急联系人信息
     * @param userId 用户ID
     * @param emergencyName 紧急联系人姓名
     * @param emergencyPhone 紧急联系人电话
     * @param emergencyRelation 与紧急联系人关系
     * @param updateTime 更新时间
     * @return 更新的行数
     */
    @Query("UPDATE users SET emergency_contact_name = :emergencyName, emergency_contact_phone = :emergencyPhone, emergency_contact_relation = :emergencyRelation, updated_at = :updateTime WHERE id = :userId")
    int updateEmergencyContact(long userId, String emergencyName, String emergencyPhone, String emergencyRelation, long updateTime);
    
    /**
     * 更新用户地址信息
     * @param userId 用户ID
     * @param address 地址
     * @param updateTime 更新时间
     * @return 更新的行数
     */
    @Query("UPDATE users SET address = :address, updated_at = :updateTime WHERE id = :userId")
    int updateAddress(long userId, String address, long updateTime);
    
    /**
     * 更新用户医疗信息
     * @param userId 用户ID
     * @param bloodType 血型
     * @param allergies 过敏信息
     * @param medicalConditions 既往病史
     * @param updateTime 更新时间
     * @return 更新的行数
     */
    @Query("UPDATE users SET blood_type = :bloodType, allergies = :allergies, medical_conditions = :medicalConditions, updated_at = :updateTime WHERE id = :userId")
    int updateMedicalInfo(long userId, String bloodType, String allergies, String medicalConditions, long updateTime);
    
    /**
     * 更新用户医生信息
     * @param userId 用户ID
     * @param doctorName 医生姓名
     * @param doctorPhone 医生电话
     * @param hospitalName 医院名称
     * @param updateTime 更新时间
     * @return 更新的行数
     */
    @Query("UPDATE users SET doctor_name = :doctorName, doctor_phone = :doctorPhone, hospital_name = :hospitalName, updated_at = :updateTime WHERE id = :userId")
    int updateDoctorInfo(long userId, String doctorName, String doctorPhone, String hospitalName, long updateTime);
    
    /**
     * 更新用户头像路径
     * @param userId 用户ID
     * @param photoPath 头像路径
     * @param updateTime 更新时间
     * @return 更新的行数
     */
    @Query("UPDATE users SET profile_photo_path = :photoPath, updated_at = :updateTime WHERE id = :userId")
    int updateProfilePhoto(long userId, String photoPath, long updateTime);
    
    // ========== 搜索和筛选 ==========
    
    /**
     * 根据姓名搜索用户（模糊匹配）
     * @param searchQuery 搜索关键词
     * @return 匹配用户的LiveData列表
     */
    @Query("SELECT * FROM users WHERE full_name LIKE '%' || :searchQuery || '%' OR username LIKE '%' || :searchQuery || '%' ORDER BY created_at DESC")
    LiveData<List<User>> searchUsers(String searchQuery);
    
    /**
     * 根据性别筛选用户
     * @param gender 性别
     * @return 指定性别用户的LiveData列表
     */
    @Query("SELECT * FROM users WHERE gender = :gender ORDER BY created_at DESC")
    LiveData<List<User>> getUsersByGender(String gender);
    
    /**
     * 获取有完整资料的用户
     * @return 有完整资料用户的LiveData列表
     */
    @Query("SELECT * FROM users WHERE full_name IS NOT NULL AND full_name != '' AND gender IS NOT NULL AND gender != '' AND birth_date IS NOT NULL AND birth_date != '' ORDER BY created_at DESC")
    LiveData<List<User>> getUsersWithCompleteProfile();
    
    /**
     * 获取有医疗信息的用户
     * @return 有医疗信息用户的LiveData列表
     */
    @Query("SELECT * FROM users WHERE (blood_type IS NOT NULL AND blood_type != '') OR (allergies IS NOT NULL AND allergies != '') OR (medical_conditions IS NOT NULL AND medical_conditions != '') ORDER BY created_at DESC")
    LiveData<List<User>> getUsersWithMedicalInfo();
    
    // ========== 统计信息 ==========
    
    /**
     * 获取用户总数
     * @return 用户总数的LiveData
     */
    @Query("SELECT COUNT(*) FROM users")
    LiveData<Integer> getUserCount();
    
    /**
     * 获取用户总数（同步方法）
     * @return 用户总数
     */
    @Query("SELECT COUNT(*) FROM users")
    int getUserCountSync();
    
    /**
     * 获取有完整资料的用户数量
     * @return 有完整资料用户数量的LiveData
     */
    @Query("SELECT COUNT(*) FROM users WHERE full_name IS NOT NULL AND full_name != '' AND gender IS NOT NULL AND gender != '' AND birth_date IS NOT NULL AND birth_date != ''")
    LiveData<Integer> getCompleteProfileCount();
    
    /**
     * 获取最近注册的用户（指定天数内）
     * @param daysAgo 天数
     * @return 最近注册用户的LiveData列表
     */
    @Query("SELECT * FROM users WHERE created_at > :daysAgo ORDER BY created_at DESC")
    LiveData<List<User>> getRecentUsers(long daysAgo);
    
    /**
     * 获取最近活跃的用户（指定天数内有登录）
     * @param daysAgo 天数
     * @return 最近活跃用户的LiveData列表
     */
    @Query("SELECT * FROM users WHERE last_login_time > :daysAgo ORDER BY last_login_time DESC")
    LiveData<List<User>> getActiveUsers(long daysAgo);
    
    // ========== 数据维护 ==========
    
    /**
     * 删除所有用户（用于测试目的）
     * @return 删除的行数
     */
    @Query("DELETE FROM users")
    int deleteAllUsers();
    
    /**
     * 清理过期的登录尝试记录（重置超过指定时间的尝试记录）
     * @param expireTime 过期时间戳
     * @return 更新的行数
     */
    @Query("UPDATE users SET login_attempts = 0, last_attempt_time = 0 WHERE last_attempt_time < :expireTime AND last_attempt_time > 0")
    int cleanupExpiredLoginAttempts(long expireTime);
    
    /**
     * 更新用户的最后更新时间
     * @param userId 用户ID
     * @param updateTime 更新时间
     * @return 更新的行数
     */
    @Query("UPDATE users SET updated_at = :updateTime WHERE id = :userId")
    int updateLastModified(long userId, long updateTime);
    
    /**
     * 批量更新用户状态（用于数据迁移）
     * @param isLoggedIn 登录状态
     * @param rememberMe 记住我状态
     * @return 更新的行数
     */
    @Query("UPDATE users SET is_logged_in = :isLoggedIn, remember_me = :rememberMe")
    int batchUpdateUserStatus(boolean isLoggedIn, boolean rememberMe);
}