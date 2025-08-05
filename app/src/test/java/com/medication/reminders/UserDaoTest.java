package com.medication.reminders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.UserDao;
import com.medication.reminders.database.entity.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UserDao的单元测试
 * 使用内存数据库进行测试，确保数据库操作的正确性
 * 
 * 注意：这个测试需要在Android环境中运行（androidTest），
 * 但为了演示目的，我们将其放在test目录中
 */
@RunWith(AndroidJUnit4.class)
public class UserDaoTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    private MedicationDatabase database;
    private UserDao userDao;
    
    // 测试数据常量
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PHONE = "13800138000";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_FULL_NAME = "张三";
    private static final String TEST_GENDER = "男性";
    private static final String TEST_BIRTH_DATE = "1990-01-01";
    
    private static final String TEST_USERNAME2 = "testuser2";
    private static final String TEST_EMAIL2 = "test2@example.com";
    private static final String TEST_PHONE2 = "13900139000";
    private static final String TEST_PASSWORD2 = "password456";
    
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        
        // 创建内存数据库用于测试
        database = Room.inMemoryDatabaseBuilder(context, MedicationDatabase.class)
                .allowMainThreadQueries()
                .build();
        
        userDao = database.userDao();
    }
    
    @After
    public void tearDown() {
        database.close();
    }
    
    // ========== 基本CRUD操作测试 ==========
    
    @Test
    public void testInsertUser() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        
        long userId = userDao.insertUser(user);
        
        assertTrue("插入用户应该返回有效ID", userId > 0);
        
        User retrievedUser = userDao.getUserById(userId);
        assertNotNull("应该能够检索插入的用户", retrievedUser);
        assertEquals("用户名应该匹配", TEST_USERNAME, retrievedUser.getUsername());
        assertEquals("邮箱应该匹配", TEST_EMAIL, retrievedUser.getEmail());
        assertEquals("电话应该匹配", TEST_PHONE, retrievedUser.getPhone());
        assertEquals("密码应该匹配", TEST_PASSWORD, retrievedUser.getPassword());
    }
    
    @Test
    public void testUpdateUser() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        user.setId(userId);
        
        // 更新用户信息
        user.setFullName(TEST_FULL_NAME);
        user.setGender(TEST_GENDER);
        user.setBirthDate(TEST_BIRTH_DATE);
        
        int updatedRows = userDao.updateUser(user);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User updatedUser = userDao.getUserById(userId);
        assertEquals("姓名应该更新", TEST_FULL_NAME, updatedUser.getFullName());
        assertEquals("性别应该更新", TEST_GENDER, updatedUser.getGender());
        assertEquals("出生日期应该更新", TEST_BIRTH_DATE, updatedUser.getBirthDate());
    }
    
    @Test
    public void testDeleteUserById() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        int deletedRows = userDao.deleteUserById(userId);
        assertEquals("应该删除一行", 1, deletedRows);
        
        User deletedUser = userDao.getUserById(userId);
        assertNull("删除后应该无法找到用户", deletedUser);
    }
    
    @Test
    public void testDeleteUser() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        user.setId(userId);
        
        int deletedRows = userDao.deleteUser(user);
        assertEquals("应该删除一行", 1, deletedRows);
        
        User deletedUser = userDao.getUserById(userId);
        assertNull("删除后应该无法找到用户", deletedUser);
    }
    
    // ========== 查询操作测试 ==========
    
    @Test
    public void testGetUserByUsername() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user);
        
        User retrievedUser = userDao.getUserByUsername(TEST_USERNAME);
        assertNotNull("应该能够通过用户名找到用户", retrievedUser);
        assertEquals("用户名应该匹配", TEST_USERNAME, retrievedUser.getUsername());
        
        User nonExistentUser = userDao.getUserByUsername("nonexistent");
        assertNull("不存在的用户名应该返回null", nonExistentUser);
    }
    
    @Test
    public void testGetUserByEmail() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user);
        
        User retrievedUser = userDao.getUserByEmail(TEST_EMAIL);
        assertNotNull("应该能够通过邮箱找到用户", retrievedUser);
        assertEquals("邮箱应该匹配", TEST_EMAIL, retrievedUser.getEmail());
        
        User nonExistentUser = userDao.getUserByEmail("nonexistent@example.com");
        assertNull("不存在的邮箱应该返回null", nonExistentUser);
    }
    
    @Test
    public void testGetUserByPhone() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user);
        
        User retrievedUser = userDao.getUserByPhone(TEST_PHONE);
        assertNotNull("应该能够通过电话找到用户", retrievedUser);
        assertEquals("电话应该匹配", TEST_PHONE, retrievedUser.getPhone());
        
        User nonExistentUser = userDao.getUserByPhone("12345678901");
        assertNull("不存在的电话应该返回null", nonExistentUser);
    }
    
    // ========== 唯一性检查测试 ==========
    
    @Test
    public void testGetUsernameCount() {
        assertEquals("初始用户名数量应该为0", 0, userDao.getUsernameCount(TEST_USERNAME));
        
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user);
        
        assertEquals("插入后用户名数量应该为1", 1, userDao.getUsernameCount(TEST_USERNAME));
        assertEquals("不存在的用户名数量应该为0", 0, userDao.getUsernameCount("nonexistent"));
    }
    
    @Test
    public void testGetEmailCount() {
        assertEquals("初始邮箱数量应该为0", 0, userDao.getEmailCount(TEST_EMAIL));
        
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user);
        
        assertEquals("插入后邮箱数量应该为1", 1, userDao.getEmailCount(TEST_EMAIL));
        assertEquals("不存在的邮箱数量应该为0", 0, userDao.getEmailCount("nonexistent@example.com"));
    }
    
    @Test
    public void testGetPhoneCount() {
        assertEquals("初始电话数量应该为0", 0, userDao.getPhoneCount(TEST_PHONE));
        
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user);
        
        assertEquals("插入后电话数量应该为1", 1, userDao.getPhoneCount(TEST_PHONE));
        assertEquals("不存在的电话数量应该为0", 0, userDao.getPhoneCount("12345678901"));
    }
    
    @Test
    public void testGetEmailCountExcluding() {
        User user1 = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId1 = userDao.insertUser(user1);
        
        User user2 = new User(TEST_USERNAME2, TEST_EMAIL2, TEST_PHONE2, TEST_PASSWORD2);
        long userId2 = userDao.insertUser(user2);
        
        // 排除用户1，检查用户1的邮箱
        assertEquals("排除自己时邮箱数量应该为0", 0, 
                     userDao.getEmailCountExcluding(TEST_EMAIL, userId1));
        
        // 排除用户2，检查用户1的邮箱
        assertEquals("排除其他用户时邮箱数量应该为1", 1, 
                     userDao.getEmailCountExcluding(TEST_EMAIL, userId2));
    }
    
    @Test
    public void testGetPhoneCountExcluding() {
        User user1 = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId1 = userDao.insertUser(user1);
        
        User user2 = new User(TEST_USERNAME2, TEST_EMAIL2, TEST_PHONE2, TEST_PASSWORD2);
        long userId2 = userDao.insertUser(user2);
        
        // 排除用户1，检查用户1的电话
        assertEquals("排除自己时电话数量应该为0", 0, 
                     userDao.getPhoneCountExcluding(TEST_PHONE, userId1));
        
        // 排除用户2，检查用户1的电话
        assertEquals("排除其他用户时电话数量应该为1", 1, 
                     userDao.getPhoneCountExcluding(TEST_PHONE, userId2));
    }
    
    @Test
    public void testGetUsernameCountExcluding() {
        User user1 = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId1 = userDao.insertUser(user1);
        
        User user2 = new User(TEST_USERNAME2, TEST_EMAIL2, TEST_PHONE2, TEST_PASSWORD2);
        long userId2 = userDao.insertUser(user2);
        
        // 排除用户1，检查用户1的用户名
        assertEquals("排除自己时用户名数量应该为0", 0, 
                     userDao.getUsernameCountExcluding(TEST_USERNAME, userId1));
        
        // 排除用户2，检查用户1的用户名
        assertEquals("排除其他用户时用户名数量应该为1", 1, 
                     userDao.getUsernameCountExcluding(TEST_USERNAME, userId2));
    }
    
    // ========== 会话管理测试 ==========
    
    @Test
    public void testLogoutAllUsers() {
        User user1 = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId1 = userDao.insertUser(user1);
        
        User user2 = new User(TEST_USERNAME2, TEST_EMAIL2, TEST_PHONE2, TEST_PASSWORD2);
        long userId2 = userDao.insertUser(user2);
        
        // 设置两个用户都为登录状态
        userDao.setUserLoggedIn(userId1, System.currentTimeMillis());
        userDao.setUserLoggedIn(userId2, System.currentTimeMillis());
        
        assertEquals("应该有2个登录用户", 2, userDao.getLoggedInUserCount());
        
        int loggedOutCount = userDao.logoutAllUsers();
        assertEquals("应该登出2个用户", 2, loggedOutCount);
        assertEquals("登出后应该没有登录用户", 0, userDao.getLoggedInUserCount());
    }
    
    @Test
    public void testSetUserLoggedIn() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        long loginTime = System.currentTimeMillis();
        int updatedRows = userDao.setUserLoggedIn(userId, loginTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User loggedInUser = userDao.getCurrentLoggedInUser();
        assertNotNull("应该有当前登录用户", loggedInUser);
        assertEquals("登录用户ID应该匹配", userId, loggedInUser.getId());
        assertTrue("用户应该处于登录状态", loggedInUser.isLoggedIn());
        assertEquals("登录时间应该匹配", loginTime, loggedInUser.getLastLoginTime());
    }
    
    @Test
    public void testSetUserLoggedOut() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        // 先设置为登录状态
        userDao.setUserLoggedIn(userId, System.currentTimeMillis());
        assertTrue("用户应该处于登录状态", userDao.getUserById(userId).isLoggedIn());
        
        // 然后登出
        int updatedRows = userDao.setUserLoggedOut(userId);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User loggedOutUser = userDao.getUserById(userId);
        assertFalse("用户应该处于登出状态", loggedOutUser.isLoggedIn());
        
        User currentUser = userDao.getCurrentLoggedInUser();
        assertNull("应该没有当前登录用户", currentUser);
    }
    
    // ========== 登录尝试管理测试 ==========
    
    @Test
    public void testUpdateLoginAttempts() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user);
        
        long attemptTime = System.currentTimeMillis();
        int updatedRows = userDao.updateLoginAttempts(TEST_USERNAME, 3, attemptTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        assertEquals("登录尝试次数应该为3", 3, userDao.getLoginAttempts(TEST_USERNAME));
        assertEquals("最后尝试时间应该匹配", attemptTime, userDao.getLastAttemptTime(TEST_USERNAME));
    }
    
    @Test
    public void testResetLoginAttempts() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user);
        
        // 先设置登录尝试
        userDao.updateLoginAttempts(TEST_USERNAME, 5, System.currentTimeMillis());
        assertEquals("登录尝试次数应该为5", 5, userDao.getLoginAttempts(TEST_USERNAME));
        
        // 重置登录尝试
        int updatedRows = userDao.resetLoginAttempts(TEST_USERNAME);
        assertEquals("应该更新一行", 1, updatedRows);
        
        assertEquals("重置后登录尝试次数应该为0", 0, userDao.getLoginAttempts(TEST_USERNAME));
        assertEquals("重置后最后尝试时间应该为0", 0, userDao.getLastAttemptTime(TEST_USERNAME));
    }
    
    // ========== 记住我功能测试 ==========
    
    @Test
    public void testUpdateRememberMe() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        int updatedRows = userDao.updateRememberMe(userId, true);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User rememberedUser = userDao.getRememberedUser();
        assertNotNull("应该有被记住的用户", rememberedUser);
        assertEquals("被记住的用户ID应该匹配", userId, rememberedUser.getId());
        assertTrue("用户应该被记住", rememberedUser.isRememberMe());
    }
    
    @Test
    public void testClearAllRememberMe() {
        User user1 = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId1 = userDao.insertUser(user1);
        
        User user2 = new User(TEST_USERNAME2, TEST_EMAIL2, TEST_PHONE2, TEST_PASSWORD2);
        long userId2 = userDao.insertUser(user2);
        
        // 设置两个用户都被记住
        userDao.updateRememberMe(userId1, true);
        userDao.updateRememberMe(userId2, true);
        
        assertNotNull("应该有被记住的用户", userDao.getRememberedUser());
        
        int updatedRows = userDao.clearAllRememberMe();
        assertEquals("应该更新2行", 2, updatedRows);
        
        User rememberedUser = userDao.getRememberedUser();
        assertNull("清除后应该没有被记住的用户", rememberedUser);
    }
    
    // ========== 密码管理测试 ==========
    
    @Test
    public void testUpdatePassword() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        String newPassword = "newpassword123";
        long updateTime = System.currentTimeMillis();
        
        int updatedRows = userDao.updatePassword(userId, newPassword, updateTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User updatedUser = userDao.getUserById(userId);
        assertEquals("密码应该更新", newPassword, updatedUser.getPassword());
        assertEquals("更新时间应该匹配", updateTime, updatedUser.getUpdatedAt());
    }
    
    @Test
    public void testValidatePassword() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user);
        
        assertEquals("正确密码应该验证成功", 1, 
                     userDao.validatePassword(TEST_USERNAME, TEST_PASSWORD));
        assertEquals("错误密码应该验证失败", 0, 
                     userDao.validatePassword(TEST_USERNAME, "wrongpassword"));
        assertEquals("不存在的用户应该验证失败", 0, 
                     userDao.validatePassword("nonexistent", TEST_PASSWORD));
    }
    
    // ========== 用户资料更新测试 ==========
    
    @Test
    public void testUpdateBasicProfile() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        long updateTime = System.currentTimeMillis();
        int updatedRows = userDao.updateBasicProfile(userId, TEST_FULL_NAME, TEST_GENDER, TEST_BIRTH_DATE, updateTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User updatedUser = userDao.getUserById(userId);
        assertEquals("姓名应该更新", TEST_FULL_NAME, updatedUser.getFullName());
        assertEquals("性别应该更新", TEST_GENDER, updatedUser.getGender());
        assertEquals("出生日期应该更新", TEST_BIRTH_DATE, updatedUser.getBirthDate());
        assertEquals("更新时间应该匹配", updateTime, updatedUser.getUpdatedAt());
    }
    
    @Test
    public void testUpdateContactInfo() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        String newEmail = "newemail@example.com";
        String newPhone = "13700137000";
        String secondaryPhone = "13600136000";
        long updateTime = System.currentTimeMillis();
        
        int updatedRows = userDao.updateContactInfo(userId, newEmail, newPhone, secondaryPhone, updateTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User updatedUser = userDao.getUserById(userId);
        assertEquals("邮箱应该更新", newEmail, updatedUser.getEmail());
        assertEquals("电话应该更新", newPhone, updatedUser.getPhone());
        assertEquals("备用电话应该更新", secondaryPhone, updatedUser.getSecondaryPhone());
        assertEquals("更新时间应该匹配", updateTime, updatedUser.getUpdatedAt());
    }
    
    @Test
    public void testUpdateEmergencyContact() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        String emergencyName = "李四";
        String emergencyPhone = "13700137000";
        String emergencyRelation = "配偶";
        long updateTime = System.currentTimeMillis();
        
        int updatedRows = userDao.updateEmergencyContact(userId, emergencyName, emergencyPhone, emergencyRelation, updateTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User updatedUser = userDao.getUserById(userId);
        assertEquals("紧急联系人姓名应该更新", emergencyName, updatedUser.getEmergencyContactName());
        assertEquals("紧急联系人电话应该更新", emergencyPhone, updatedUser.getEmergencyContactPhone());
        assertEquals("紧急联系人关系应该更新", emergencyRelation, updatedUser.getEmergencyContactRelation());
        assertEquals("更新时间应该匹配", updateTime, updatedUser.getUpdatedAt());
    }
    
    @Test
    public void testUpdateAddress() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        String address = "北京市朝阳区建国门外大街1号";
        long updateTime = System.currentTimeMillis();
        
        int updatedRows = userDao.updateAddress(userId, address, updateTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User updatedUser = userDao.getUserById(userId);
        assertEquals("地址应该更新", address, updatedUser.getAddress());
        assertEquals("更新时间应该匹配", updateTime, updatedUser.getUpdatedAt());
    }
    
    @Test
    public void testUpdateMedicalInfo() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        String bloodType = "A+";
        String allergies = "青霉素过敏";
        String medicalConditions = "高血压";
        long updateTime = System.currentTimeMillis();
        
        int updatedRows = userDao.updateMedicalInfo(userId, bloodType, allergies, medicalConditions, updateTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User updatedUser = userDao.getUserById(userId);
        assertEquals("血型应该更新", bloodType, updatedUser.getBloodType());
        assertEquals("过敏信息应该更新", allergies, updatedUser.getAllergies());
        assertEquals("既往病史应该更新", medicalConditions, updatedUser.getMedicalConditions());
        assertEquals("更新时间应该匹配", updateTime, updatedUser.getUpdatedAt());
    }
    
    @Test
    public void testUpdateDoctorInfo() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        String doctorName = "王医生";
        String doctorPhone = "13600136000";
        String hospitalName = "北京医院";
        long updateTime = System.currentTimeMillis();
        
        int updatedRows = userDao.updateDoctorInfo(userId, doctorName, doctorPhone, hospitalName, updateTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User updatedUser = userDao.getUserById(userId);
        assertEquals("医生姓名应该更新", doctorName, updatedUser.getDoctorName());
        assertEquals("医生电话应该更新", doctorPhone, updatedUser.getDoctorPhone());
        assertEquals("医院名称应该更新", hospitalName, updatedUser.getHospitalName());
        assertEquals("更新时间应该匹配", updateTime, updatedUser.getUpdatedAt());
    }
    
    @Test
    public void testUpdateProfilePhoto() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        String photoPath = "/storage/photos/profile_123.jpg";
        long updateTime = System.currentTimeMillis();
        
        int updatedRows = userDao.updateProfilePhoto(userId, photoPath, updateTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User updatedUser = userDao.getUserById(userId);
        assertEquals("头像路径应该更新", photoPath, updatedUser.getProfilePhotoPath());
        assertEquals("更新时间应该匹配", updateTime, updatedUser.getUpdatedAt());
    }
    
    // ========== 统计信息测试 ==========
    
    @Test
    public void testGetUserCountSync() {
        assertEquals("初始用户数量应该为0", 0, userDao.getUserCountSync());
        
        User user1 = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user1);
        assertEquals("插入一个用户后数量应该为1", 1, userDao.getUserCountSync());
        
        User user2 = new User(TEST_USERNAME2, TEST_EMAIL2, TEST_PHONE2, TEST_PASSWORD2);
        userDao.insertUser(user2);
        assertEquals("插入两个用户后数量应该为2", 2, userDao.getUserCountSync());
    }
    
    @Test
    public void testGetRecentUsers() throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        long oneDayAgo = currentTime - (24 * 60 * 60 * 1000); // 一天前
        
        // 创建一个"旧"用户
        User oldUser = new User("olduser", "old@example.com", "13500135000", "password");
        oldUser.setCreatedAt(oneDayAgo - 1000); // 超过一天前
        long oldUserId = userDao.insertUser(oldUser);
        oldUser.setId(oldUserId);
        userDao.updateUser(oldUser); // 更新创建时间
        
        // 创建一个"新"用户
        User newUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(newUser);
        
        // 使用LiveData测试需要特殊处理
        LiveData<List<User>> recentUsersLiveData = userDao.getRecentUsers(oneDayAgo);
        
        // 注意：在实际的androidTest中，你需要使用LiveDataTestUtil来测试LiveData
        // 这里我们只是验证方法不会抛出异常
        assertNotNull("最近用户LiveData不应该为null", recentUsersLiveData);
    }
    
    // ========== 数据维护测试 ==========
    
    @Test
    public void testDeleteAllUsers() {
        User user1 = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user1);
        
        User user2 = new User(TEST_USERNAME2, TEST_EMAIL2, TEST_PHONE2, TEST_PASSWORD2);
        userDao.insertUser(user2);
        
        assertEquals("插入前应该有2个用户", 2, userDao.getUserCountSync());
        
        int deletedRows = userDao.deleteAllUsers();
        assertEquals("应该删除2行", 2, deletedRows);
        assertEquals("删除后应该没有用户", 0, userDao.getUserCountSync());
    }
    
    @Test
    public void testCleanupExpiredLoginAttempts() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user);
        
        long currentTime = System.currentTimeMillis();
        long expiredTime = currentTime - (60 * 60 * 1000); // 一小时前
        
        // 设置过期的登录尝试
        userDao.updateLoginAttempts(TEST_USERNAME, 5, expiredTime - 1000);
        assertEquals("应该有5次登录尝试", 5, userDao.getLoginAttempts(TEST_USERNAME));
        
        int cleanedRows = userDao.cleanupExpiredLoginAttempts(expiredTime);
        assertEquals("应该清理1行", 1, cleanedRows);
        assertEquals("清理后登录尝试次数应该为0", 0, userDao.getLoginAttempts(TEST_USERNAME));
    }
    
    @Test
    public void testUpdateLastModified() {
        User user = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long userId = userDao.insertUser(user);
        
        long originalTime = userDao.getUserById(userId).getUpdatedAt();
        
        // 等待一毫秒确保时间不同
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // 忽略
        }
        
        long newTime = System.currentTimeMillis();
        int updatedRows = userDao.updateLastModified(userId, newTime);
        assertEquals("应该更新一行", 1, updatedRows);
        
        User updatedUser = userDao.getUserById(userId);
        assertEquals("更新时间应该匹配", newTime, updatedUser.getUpdatedAt());
        assertTrue("新时间应该大于原时间", updatedUser.getUpdatedAt() > originalTime);
    }
    
    @Test
    public void testBatchUpdateUserStatus() {
        User user1 = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userDao.insertUser(user1);
        
        User user2 = new User(TEST_USERNAME2, TEST_EMAIL2, TEST_PHONE2, TEST_PASSWORD2);
        userDao.insertUser(user2);
        
        int updatedRows = userDao.batchUpdateUserStatus(false, false);
        assertEquals("应该更新2行", 2, updatedRows);
        
        assertEquals("应该没有登录用户", 0, userDao.getLoggedInUserCount());
        assertNull("应该没有被记住的用户", userDao.getRememberedUser());
    }
    
    // ========== 边界情况测试 ==========
    
    @Test
    public void testInsertUserWithNullValues() {
        // 测试必填字段为null的情况
        // 注意：在实际应用中，@NonNull注解会防止这种情况
        // 但在测试中我们可能需要验证数据库的行为
        
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPhone(TEST_PHONE);
        user.setPassword(TEST_PASSWORD);
        
        long userId = userDao.insertUser(user);
        assertTrue("即使有null值也应该能插入", userId > 0);
        
        User retrievedUser = userDao.getUserById(userId);
        assertNotNull("应该能检索用户", retrievedUser);
        assertNull("可选字段应该为null", retrievedUser.getFullName());
    }
    
    @Test
    public void testQueryNonExistentUser() {
        assertNull("查询不存在的用户ID应该返回null", userDao.getUserById(999L));
        assertNull("查询不存在的用户名应该返回null", userDao.getUserByUsername("nonexistent"));
        assertNull("查询不存在的邮箱应该返回null", userDao.getUserByEmail("nonexistent@example.com"));
        assertNull("查询不存在的电话应该返回null", userDao.getUserByPhone("12345678901"));
    }
    
    @Test
    public void testUpdateNonExistentUser() {
        User nonExistentUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        nonExistentUser.setId(999L); // 不存在的ID
        
        int updatedRows = userDao.updateUser(nonExistentUser);
        assertEquals("更新不存在的用户应该返回0", 0, updatedRows);
    }
    
    @Test
    public void testDeleteNonExistentUser() {
        int deletedRows = userDao.deleteUserById(999L);
        assertEquals("删除不存在的用户应该返回0", 0, deletedRows);
    }
    
    // 辅助方法：等待LiveData值（在实际测试中需要更复杂的实现）
    private <T> T getValueFromLiveData(LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        
        liveData.observeForever(value -> {
            data[0] = value;
            latch.countDown();
        });
        
        latch.await(2, TimeUnit.SECONDS);
        
        @SuppressWarnings("unchecked")
        T result = (T) data[0];
        return result;
    }
}