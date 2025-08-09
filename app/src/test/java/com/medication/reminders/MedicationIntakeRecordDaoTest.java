package com.medication.reminders;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.MedicationIntakeRecordDao;
import com.medication.reminders.database.entity.MedicationIntakeRecord;

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
 * 用药记录DAO的数据库操作测试
 * 测试MedicationIntakeRecordDao的所有CRUD操作和查询功能
 */
@RunWith(RobolectricTestRunner.class)
public class MedicationIntakeRecordDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MedicationDatabase database;
    private MedicationIntakeRecordDao intakeRecordDao;
    
    // 测试数据
    private static final String TEST_MEDICATION_1 = "阿司匹林";
    private static final String TEST_MEDICATION_2 = "维生素C";
    private static final String TEST_MEDICATION_3 = "感冒灵";
    private static final int TEST_DOSAGE_1 = 1;
    private static final int TEST_DOSAGE_2 = 2;
    private static final int TEST_DOSAGE_3 = 3;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, MedicationDatabase.class)
                .allowMainThreadQueries()
                .build();
        intakeRecordDao = database.medicationIntakeRecordDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testInsertAndGetIntakeRecord() throws InterruptedException {
        // 创建测试用药记录
        MedicationIntakeRecord record = new MedicationIntakeRecord(TEST_MEDICATION_1, TEST_DOSAGE_1);
        
        // 插入记录
        long recordId = intakeRecordDao.insertIntakeRecord(record);
        assertTrue("插入的记录ID应该大于0", recordId > 0);
        
        // 获取记录并验证
        LiveData<MedicationIntakeRecord> liveData = intakeRecordDao.getIntakeRecordById(recordId);
        MedicationIntakeRecord retrievedRecord = getValueFromLiveData(liveData);
        
        assertNotNull("应该能够获取到插入的记录", retrievedRecord);
        assertEquals("药物名称应该匹配", TEST_MEDICATION_1, retrievedRecord.getMedicationName());
        assertEquals("服用剂量应该匹配", TEST_DOSAGE_1, retrievedRecord.getDosageTaken());
        assertEquals("记录ID应该匹配", recordId, retrievedRecord.getId());
    }

    @Test
    public void testGetAllIntakeRecords() throws InterruptedException {
        // 插入多条记录，使用明确的时间戳确保顺序
        long baseTime = System.currentTimeMillis();
        
        MedicationIntakeRecord record1 = new MedicationIntakeRecord(TEST_MEDICATION_1, baseTime - 2000, TEST_DOSAGE_1);
        MedicationIntakeRecord record2 = new MedicationIntakeRecord(TEST_MEDICATION_2, baseTime - 1000, TEST_DOSAGE_2);
        MedicationIntakeRecord record3 = new MedicationIntakeRecord(TEST_MEDICATION_3, baseTime, TEST_DOSAGE_3);
        
        intakeRecordDao.insertIntakeRecord(record1);
        intakeRecordDao.insertIntakeRecord(record2);
        intakeRecordDao.insertIntakeRecord(record3);
        
        // 获取所有记录
        LiveData<List<MedicationIntakeRecord>> liveData = intakeRecordDao.getAllIntakeRecords();
        List<MedicationIntakeRecord> records = getValueFromLiveData(liveData);
        
        assertNotNull("记录列表不应该为null", records);
        assertEquals("应该有3条记录", 3, records.size());
        
        // 验证按时间倒序排列（最新的在前面）
        assertEquals("最新记录应该是第三个药物", TEST_MEDICATION_3, records.get(0).getMedicationName());
        assertEquals("第二新记录应该是第二个药物", TEST_MEDICATION_2, records.get(1).getMedicationName());
        assertEquals("最旧记录应该是第一个药物", TEST_MEDICATION_1, records.get(2).getMedicationName());
    }

    @Test
    public void testGetIntakeRecordsByMedicationName() throws InterruptedException {
        // 插入多条记录，其中两条是同一种药物，使用明确的时间戳
        long baseTime = System.currentTimeMillis();
        
        MedicationIntakeRecord record1 = new MedicationIntakeRecord(TEST_MEDICATION_1, baseTime - 1000, TEST_DOSAGE_1);
        MedicationIntakeRecord record2 = new MedicationIntakeRecord(TEST_MEDICATION_1, baseTime, TEST_DOSAGE_2);
        MedicationIntakeRecord record3 = new MedicationIntakeRecord(TEST_MEDICATION_2, baseTime + 1000, TEST_DOSAGE_3);
        
        intakeRecordDao.insertIntakeRecord(record1);
        intakeRecordDao.insertIntakeRecord(record2);
        intakeRecordDao.insertIntakeRecord(record3);
        
        // 获取特定药物的记录
        LiveData<List<MedicationIntakeRecord>> liveData = 
            intakeRecordDao.getIntakeRecordsByMedicationName(TEST_MEDICATION_1);
        List<MedicationIntakeRecord> records = getValueFromLiveData(liveData);
        
        assertNotNull("记录列表不应该为null", records);
        assertEquals("应该有2条阿司匹林的记录", 2, records.size());
        
        // 验证都是正确的药物
        for (MedicationIntakeRecord record : records) {
            assertEquals("所有记录都应该是阿司匹林", TEST_MEDICATION_1, record.getMedicationName());
        }
        
        // 验证按时间倒序排列
        assertEquals("最新记录的剂量应该是2", TEST_DOSAGE_2, records.get(0).getDosageTaken());
        assertEquals("较旧记录的剂量应该是1", TEST_DOSAGE_1, records.get(1).getDosageTaken());
    }

    @Test
    public void testGetRecentIntakeRecords() throws InterruptedException {
        // 插入5条记录
        for (int i = 1; i <= 5; i++) {
            MedicationIntakeRecord record = new MedicationIntakeRecord("药物" + i, i);
            intakeRecordDao.insertIntakeRecord(record);
            Thread.sleep(10);
        }
        
        // 获取最近3条记录
        LiveData<List<MedicationIntakeRecord>> liveData = intakeRecordDao.getRecentIntakeRecords(3);
        List<MedicationIntakeRecord> records = getValueFromLiveData(liveData);
        
        assertNotNull("记录列表不应该为null", records);
        assertEquals("应该只有3条记录", 3, records.size());
        
        // 验证是最新的3条记录
        assertEquals("最新记录应该是药物5", "药物5", records.get(0).getMedicationName());
        assertEquals("第二新记录应该是药物4", "药物4", records.get(1).getMedicationName());
        assertEquals("第三新记录应该是药物3", "药物3", records.get(2).getMedicationName());
    }

    @Test
    public void testGetIntakeRecordCount() throws InterruptedException {
        // 初始应该没有记录
        LiveData<Integer> countLiveData = intakeRecordDao.getIntakeRecordCount();
        Integer initialCount = getValueFromLiveData(countLiveData);
        assertEquals("初始记录数应该为0", Integer.valueOf(0), initialCount);
        
        // 插入3条记录
        for (int i = 1; i <= 3; i++) {
            MedicationIntakeRecord record = new MedicationIntakeRecord("药物" + i, i);
            intakeRecordDao.insertIntakeRecord(record);
        }
        
        // 验证记录数
        Integer finalCount = getValueFromLiveData(countLiveData);
        assertEquals("最终记录数应该为3", Integer.valueOf(3), finalCount);
    }

    @Test
    public void testUpdateIntakeRecord() throws InterruptedException {
        // 插入记录
        MedicationIntakeRecord record = new MedicationIntakeRecord(TEST_MEDICATION_1, TEST_DOSAGE_1);
        long recordId = intakeRecordDao.insertIntakeRecord(record);
        
        // 获取并修改记录
        LiveData<MedicationIntakeRecord> liveData = intakeRecordDao.getIntakeRecordById(recordId);
        MedicationIntakeRecord retrievedRecord = getValueFromLiveData(liveData);
        retrievedRecord.setMedicationName(TEST_MEDICATION_2);
        retrievedRecord.setDosageTaken(TEST_DOSAGE_2);
        
        // 更新记录
        int updatedRows = intakeRecordDao.updateIntakeRecord(retrievedRecord);
        assertEquals("应该更新1行", 1, updatedRows);
        
        // 验证更新结果
        MedicationIntakeRecord updatedRecord = getValueFromLiveData(liveData);
        assertEquals("药物名称应该已更新", TEST_MEDICATION_2, updatedRecord.getMedicationName());
        assertEquals("服用剂量应该已更新", TEST_DOSAGE_2, updatedRecord.getDosageTaken());
    }

    @Test
    public void testDeleteIntakeRecord() throws InterruptedException {
        // 插入记录
        MedicationIntakeRecord record = new MedicationIntakeRecord(TEST_MEDICATION_1, TEST_DOSAGE_1);
        long recordId = intakeRecordDao.insertIntakeRecord(record);
        
        // 获取记录
        LiveData<MedicationIntakeRecord> liveData = intakeRecordDao.getIntakeRecordById(recordId);
        MedicationIntakeRecord retrievedRecord = getValueFromLiveData(liveData);
        assertNotNull("记录应该存在", retrievedRecord);
        
        // 删除记录
        int deletedRows = intakeRecordDao.deleteIntakeRecord(retrievedRecord);
        assertEquals("应该删除1行", 1, deletedRows);
        
        // 验证记录已删除
        MedicationIntakeRecord deletedRecord = getValueFromLiveData(liveData);
        assertNull("记录应该已被删除", deletedRecord);
    }

    @Test
    public void testDeleteAllIntakeRecords() throws InterruptedException {
        // 插入多条记录
        for (int i = 1; i <= 3; i++) {
            MedicationIntakeRecord record = new MedicationIntakeRecord("药物" + i, i);
            intakeRecordDao.insertIntakeRecord(record);
        }
        
        // 验证记录存在
        LiveData<List<MedicationIntakeRecord>> liveData = intakeRecordDao.getAllIntakeRecords();
        List<MedicationIntakeRecord> records = getValueFromLiveData(liveData);
        assertEquals("应该有3条记录", 3, records.size());
        
        // 删除所有记录
        int deletedRows = intakeRecordDao.deleteAllIntakeRecords();
        assertEquals("应该删除3行", 3, deletedRows);
        
        // 验证所有记录已删除
        List<MedicationIntakeRecord> remainingRecords = getValueFromLiveData(liveData);
        assertTrue("应该没有剩余记录", remainingRecords.isEmpty());
    }

    /**
     * 从LiveData中获取值的辅助方法
     * 使用CountDownLatch等待异步操作完成
     */
    private <T> T getValueFromLiveData(LiveData<T> liveData) throws InterruptedException {
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
        latch.await(2, TimeUnit.SECONDS);
        
        @SuppressWarnings("unchecked")
        T result = (T) data[0];
        return result;
    }
}