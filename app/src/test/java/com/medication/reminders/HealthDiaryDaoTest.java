package com.medication.reminders;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.HealthDiaryDao;
import com.medication.reminders.database.dao.UserDao;
import com.medication.reminders.database.entity.HealthDiary;
import com.medication.reminders.database.entity.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * 健康日记DAO的数据库操作测试
 * 测试HealthDiaryDao的所有CRUD操作和查询功能
 */
@RunWith(RobolectricTestRunner.class)
public class HealthDiaryDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MedicationDatabase database;
    private HealthDiaryDao healthDiaryDao;
    private UserDao userDao;
    
    // 测试数据
    private static final long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CONTENT_1 = "今天感觉身体状况良好，血压正常。";
    private static final String TEST_CONTENT_2 = "今天有点头痛，可能是睡眠不足。";
    private static final String TEST_CONTENT_3 = "血压有点高，需要注意饮食。";

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        
        // 创建内存数据库用于测试
        database = Room.inMemoryDatabaseBuilder(context, MedicationDatabase.class)
                .allowMainThreadQueries()
                .build();
        
        healthDiaryDao = database.healthDiaryDao();
        userDao = database.userDao();
        
        // 创建测试用户
        User testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPhone("13800138000");
        testUser.setPassword("testpassword");
        userDao.insertUser(testUser);
    }

    @After
    public void tearDown() {
        database.close();
    }

    // ========== 插入操作测试 ==========

    @Test
    public void testInsertDiary_Success() {
        // 创建测试日记
        HealthDiary diary = new HealthDiary(TEST_USER_ID, TEST_CONTENT_1);
        
        // 插入日记
        long insertedId = healthDiaryDao.insertDiary(diary);
        
        // 验证插入成功
        assertTrue("插入应该返回有效ID", insertedId > 0);
        
        // 验证数据是否正确插入
        HealthDiary retrievedDiary = getValue(healthDiaryDao.getDiaryById(insertedId));
        assertNotNull("应该能够检索到插入的日记", retrievedDiary);
        assertEquals("用户ID应该匹配", TEST_USER_ID, retrievedDiary.getUserId());
        assertEquals("内容应该匹配", TEST_CONTENT_1, retrievedDiary.getContent());
        assertTrue("创建时间应该大于0", retrievedDiary.getCreatedAt() > 0);
        assertTrue("更新时间应该大于0", retrievedDiary.getUpdatedAt() > 0);
    }

    @Test
    public void testInsertMultipleDiaries() {
        // 创建多个测试日记
        HealthDiary diary1 = new HealthDiary(TEST_USER_ID, TEST_CONTENT_1);
        HealthDiary diary2 = new HealthDiary(TEST_USER_ID, TEST_CONTENT_2);
        HealthDiary diary3 = new HealthDiary(TEST_USER_ID, TEST_CONTENT_3);
        
        // 插入日记
        long id1 = healthDiaryDao.insertDiary(diary1);
        long id2 = healthDiaryDao.insertDiary(diary2);
        long id3 = healthDiaryDao.insertDiary(diary3);
        
        // 验证所有插入都成功
        assertTrue("第一个日记插入应该成功", id1 > 0);
        assertTrue("第二个日记插入应该成功", id2 > 0);
        assertTrue("第三个日记插入应该成功", id3 > 0);
        
        // 验证ID是递增的
        assertTrue("ID应该是递增的", id2 > id1);
        assertTrue("ID应该是递增的", id3 > id2);
        
        // 验证总数量
        Integer count = getValue(healthDiaryDao.getDiaryCountByUserId(TEST_USER_ID));
        assertEquals("用户日记总数应该是3", Integer.valueOf(3), count);
    }

    // ========== 查询操作测试 ==========

    @Test
    public void testGetDiaryById_Success() {
        // 插入测试日记
        HealthDiary diary = new HealthDiary(TEST_USER_ID, TEST_CONTENT_1);
        long insertedId = healthDiaryDao.insertDiary(diary);
        
        // 根据ID查询日记
        HealthDiary retrievedDiary = getValue(healthDiaryDao.getDiaryById(insertedId));
        
        // 验证查询结果
        assertNotNull("应该能够找到日记", retrievedDiary);
        assertEquals("ID应该匹配", insertedId, retrievedDiary.getId());
        assertEquals("用户ID应该匹配", TEST_USER_ID, retrievedDiary.getUserId());
        assertEquals("内容应该匹配", TEST_CONTENT_1, retrievedDiary.getContent());
    }

    @Test
    public void testGetDiaryById_NotFound() {
        // 查询不存在的日记
        HealthDiary retrievedDiary = getValue(healthDiaryDao.getDiaryById(999L));
        
        // 验证查询结果
        assertNull("不存在的日记应该返回null", retrievedDiary);
    }

    @Test
    public void testGetDiariesByUserId_Success() {
        // 插入多个测试日记
        HealthDiary diary1 = new HealthDiary(TEST_USER_ID, TEST_CONTENT_1);
        HealthDiary diary2 = new HealthDiary(TEST_USER_ID, TEST_CONTENT_2);
        HealthDiary diary3 = new HealthDiary(TEST_USER_ID, TEST_CONTENT_3);
        
        // 设置不同的创建时间以测试排序
        long baseTime = System.currentTimeMillis();
        diary1.setCreatedAt(baseTime - 2000);
        diary2.setCreatedAt(baseTime - 1000);
        diary3.setCreatedAt(baseTime);
        
        healthDiaryDao.insertDiary(diary1);
        healthDiaryDao.insertDiary(diary2);
        healthDiaryDao.insertDiary(diary3);
        
        // 查询用户的所有日记
        List<HealthDiary> diaries = getValue(healthDiaryDao.getDiariesByUserId(TEST_USER_ID));
        
        // 验证查询结果
        assertNotNull("日记列表不应该为null", diaries);
        assertEquals("应该有3条日记", 3, diaries.size());
        
        // 验证按创建时间倒序排列（最新的在前面）
        assertTrue("第一条应该是最新的", diaries.get(0).getCreatedAt() >= diaries.get(1).getCreatedAt());
        assertTrue("第二条应该比第三条新", diaries.get(1).getCreatedAt() >= diaries.get(2).getCreatedAt());
        
        // 验证内容
        assertEquals("最新日记内容应该匹配", TEST_CONTENT_3, diaries.get(0).getContent());
        assertEquals("中间日记内容应该匹配", TEST_CONTENT_2, diaries.get(1).getContent());
        assertEquals("最早日记内容应该匹配", TEST_CONTENT_1, diaries.get(2).getContent());
    }

    @Test
    public void testGetDiariesByUserId_EmptyResult() {
        // 查询没有日记的用户
        List<HealthDiary> diaries = getValue(healthDiaryDao.getDiariesByUserId(999L));
        
        // 验证查询结果
        assertNotNull("日记列表不应该为null", diaries);
        assertTrue("日记列表应该为空", diaries.isEmpty());
    }

    @Test
    public void testGetDiaryCountByUserId_Success() {
        // 插入测试日记
        healthDiaryDao.insertDiary(new HealthDiary(TEST_USER_ID, TEST_CONTENT_1));
        healthDiaryDao.insertDiary(new HealthDiary(TEST_USER_ID, TEST_CONTENT_2));
        
        // 查询日记数量
        Integer count = getValue(healthDiaryDao.getDiaryCountByUserId(TEST_USER_ID));
        
        // 验证结果
        assertNotNull("数量不应该为null", count);
        assertEquals("应该有2条日记", Integer.valueOf(2), count);
    }

    @Test
    public void testGetDiaryCountByUserId_Zero() {
        // 查询没有日记的用户
        Integer count = getValue(healthDiaryDao.getDiaryCountByUserId(999L));
        
        // 验证结果
        assertNotNull("数量不应该为null", count);
        assertEquals("应该有0条日记", Integer.valueOf(0), count);
    }

    // ========== 更新操作测试 ==========

    @Test
    public void testUpdateDiary_Success() {
        // 插入测试日记
        HealthDiary diary = new HealthDiary(TEST_USER_ID, TEST_CONTENT_1);
        long insertedId = healthDiaryDao.insertDiary(diary);
        
        // 获取插入的日记
        HealthDiary insertedDiary = getValue(healthDiaryDao.getDiaryById(insertedId));
        assertNotNull("插入的日记不应该为null", insertedDiary);
        
        // 修改内容
        long originalCreatedAt = insertedDiary.getCreatedAt();
        insertedDiary.setContent(TEST_CONTENT_2);
        // setContent方法会自动更新时间戳，但为了确保时间差异，我们手动标记更新
        try {
            Thread.sleep(1); // 确保时间戳不同
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        insertedDiary.markAsUpdated();
        
        // 更新日记
        int updatedRows = healthDiaryDao.updateDiary(insertedDiary);
        
        // 验证更新成功
        assertEquals("应该更新1行", 1, updatedRows);
        
        // 验证更新后的数据
        HealthDiary updatedDiary = getValue(healthDiaryDao.getDiaryById(insertedId));
        assertNotNull("更新后的日记不应该为null", updatedDiary);
        assertEquals("内容应该已更新", TEST_CONTENT_2, updatedDiary.getContent());
        assertEquals("创建时间不应该改变", originalCreatedAt, updatedDiary.getCreatedAt());
        assertTrue("更新时间应该改变", updatedDiary.getUpdatedAt() > originalCreatedAt);
    }

    @Test
    public void testUpdateDiary_NotFound() {
        // 尝试更新不存在的日记
        HealthDiary nonExistentDiary = new HealthDiary(TEST_USER_ID, TEST_CONTENT_1);
        nonExistentDiary.setId(999L);
        
        int updatedRows = healthDiaryDao.updateDiary(nonExistentDiary);
        
        // 验证更新失败
        assertEquals("不存在的日记更新应该返回0", 0, updatedRows);
    }

    // ========== 删除操作测试 ==========

    @Test
    public void testDeleteDiary_Success() {
        // 插入测试日记
        HealthDiary diary = new HealthDiary(TEST_USER_ID, TEST_CONTENT_1);
        long insertedId = healthDiaryDao.insertDiary(diary);
        
        // 获取插入的日记
        HealthDiary insertedDiary = getValue(healthDiaryDao.getDiaryById(insertedId));
        assertNotNull("插入的日记不应该为null", insertedDiary);
        
        // 删除日记
        int deletedRows = healthDiaryDao.deleteDiary(insertedDiary);
        
        // 验证删除成功
        assertEquals("应该删除1行", 1, deletedRows);
        
        // 验证日记已被删除
        HealthDiary deletedDiary = getValue(healthDiaryDao.getDiaryById(insertedId));
        assertNull("删除后的日记应该为null", deletedDiary);
        
        // 验证用户日记数量减少
        Integer count = getValue(healthDiaryDao.getDiaryCountByUserId(TEST_USER_ID));
        assertEquals("用户日记数量应该为0", Integer.valueOf(0), count);
    }

    @Test
    public void testDeleteDiary_NotFound() {
        // 尝试删除不存在的日记
        HealthDiary nonExistentDiary = new HealthDiary(TEST_USER_ID, TEST_CONTENT_1);
        nonExistentDiary.setId(999L);
        
        int deletedRows = healthDiaryDao.deleteDiary(nonExistentDiary);
        
        // 验证删除失败
        assertEquals("不存在的日记删除应该返回0", 0, deletedRows);
    }

    @Test
    public void testDeleteMultipleDiaries() {
        // 插入多个测试日记
        HealthDiary diary1 = new HealthDiary(TEST_USER_ID, TEST_CONTENT_1);
        HealthDiary diary2 = new HealthDiary(TEST_USER_ID, TEST_CONTENT_2);
        HealthDiary diary3 = new HealthDiary(TEST_USER_ID, TEST_CONTENT_3);
        
        long id1 = healthDiaryDao.insertDiary(diary1);
        long id2 = healthDiaryDao.insertDiary(diary2);
        long id3 = healthDiaryDao.insertDiary(diary3);
        
        // 验证插入成功
        Integer initialCount = getValue(healthDiaryDao.getDiaryCountByUserId(TEST_USER_ID));
        assertEquals("初始应该有3条日记", Integer.valueOf(3), initialCount);
        
        // 删除第二条日记
        HealthDiary diaryToDelete = getValue(healthDiaryDao.getDiaryById(id2));
        assertNotNull("要删除的日记不应该为null", diaryToDelete);
        
        int deletedRows = healthDiaryDao.deleteDiary(diaryToDelete);
        assertEquals("应该删除1行", 1, deletedRows);
        
        // 验证删除后的状态
        Integer finalCount = getValue(healthDiaryDao.getDiaryCountByUserId(TEST_USER_ID));
        assertEquals("删除后应该有2条日记", Integer.valueOf(2), finalCount);
        
        // 验证正确的日记被删除
        assertNull("被删除的日记应该不存在", getValue(healthDiaryDao.getDiaryById(id2)));
        assertNotNull("其他日记应该仍然存在", getValue(healthDiaryDao.getDiaryById(id1)));
        assertNotNull("其他日记应该仍然存在", getValue(healthDiaryDao.getDiaryById(id3)));
    }

    // ========== 数据完整性测试 ==========

    @Test
    public void testForeignKeyConstraint() {
        // 尝试插入引用不存在用户的日记
        HealthDiary diary = new HealthDiary(999L, TEST_CONTENT_1); // 不存在的用户ID
        
        try {
            healthDiaryDao.insertDiary(diary);
            fail("应该因为外键约束失败而抛出异常");
        } catch (Exception e) {
            // 预期的异常，外键约束应该阻止插入
            assertTrue("异常消息应该包含外键相关信息", 
                e.getMessage().toLowerCase().contains("foreign key") || 
                e.getMessage().toLowerCase().contains("constraint"));
        }
    }

    @Test
    public void testCascadeDelete() {
        // 插入测试日记
        HealthDiary diary = new HealthDiary(TEST_USER_ID, TEST_CONTENT_1);
        long diaryId = healthDiaryDao.insertDiary(diary);
        
        // 验证日记存在
        assertNotNull("日记应该存在", getValue(healthDiaryDao.getDiaryById(diaryId)));
        
        // 删除用户（应该级联删除日记）
        User user = userDao.getUserById(TEST_USER_ID);
        assertNotNull("用户应该存在", user);
        userDao.deleteUser(user);
        
        // 验证日记被级联删除
        assertNull("日记应该被级联删除", getValue(healthDiaryDao.getDiaryById(diaryId)));
        Integer count = getValue(healthDiaryDao.getDiaryCountByUserId(TEST_USER_ID));
        assertEquals("用户日记数量应该为0", Integer.valueOf(0), count);
    }

    // ========== 边界条件测试 ==========

    @Test
    public void testLongContent() {
        // 创建长内容（接近最大长度）
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 4999; i++) {
            longContent.append("a");
        }
        
        HealthDiary diary = new HealthDiary(TEST_USER_ID, longContent.toString());
        long insertedId = healthDiaryDao.insertDiary(diary);
        
        // 验证长内容可以正确存储和检索
        assertTrue("长内容插入应该成功", insertedId > 0);
        
        HealthDiary retrievedDiary = getValue(healthDiaryDao.getDiaryById(insertedId));
        assertNotNull("应该能够检索长内容日记", retrievedDiary);
        assertEquals("长内容应该完整保存", longContent.toString(), retrievedDiary.getContent());
        assertEquals("内容长度应该正确", 4999, retrievedDiary.getContent().length());
    }

    @Test
    public void testSpecialCharacters() {
        // 测试包含特殊字符的内容
        String specialContent = "测试特殊字符：\n换行符\t制表符\"引号'单引号\\反斜杠&符号<>标签💊药物表情";
        
        HealthDiary diary = new HealthDiary(TEST_USER_ID, specialContent);
        long insertedId = healthDiaryDao.insertDiary(diary);
        
        // 验证特殊字符可以正确存储和检索
        assertTrue("特殊字符内容插入应该成功", insertedId > 0);
        
        HealthDiary retrievedDiary = getValue(healthDiaryDao.getDiaryById(insertedId));
        assertNotNull("应该能够检索特殊字符日记", retrievedDiary);
        assertEquals("特殊字符应该完整保存", specialContent, retrievedDiary.getContent());
    }

    // ========== 辅助方法 ==========

    /**
     * 从LiveData中获取值的辅助方法
     * 用于在测试中同步获取LiveData的值
     */
    private <T> T getValue(LiveData<T> liveData) {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T value) {
                data[0] = value;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        
        liveData.observeForever(observer);
        
        try {
            // 等待最多2秒获取数据
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new RuntimeException("LiveData值获取超时");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("等待LiveData值时被中断", e);
        }
        
        @SuppressWarnings("unchecked")
        T result = (T) data[0];
        return result;
    }
}