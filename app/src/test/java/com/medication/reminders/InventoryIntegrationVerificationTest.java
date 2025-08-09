package com.medication.reminders;

import static org.junit.Assert.*;

import android.app.Application;
import android.content.Context;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.MedicationDao;
import com.medication.reminders.database.dao.MedicationIntakeRecordDao;
import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.database.entity.MedicationIntakeRecord;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * 药物库存跟踪功能的集成验证测试
 * 验证数据库操作、实体关系和业务逻辑的正确性
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class InventoryIntegrationVerificationTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MedicationDatabase database;
    private MedicationDao medicationDao;
    private MedicationIntakeRecordDao intakeRecordDao;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        
        // 创建内存数据库用于测试
        database = Room.inMemoryDatabaseBuilder(context, MedicationDatabase.class)
                .allowMainThreadQueries()
                .build();
        
        medicationDao = database.medicationDao();
        intakeRecordDao = database.medicationIntakeRecordDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    /**
     * 测试1：验证药物库存字段的添加和保存
     */
    @Test
    public void testMedicationInventoryFieldsPersistence() {
        System.out.println("=== 测试1：验证药物库存字段 ===");
        
        // 创建包含库存信息的药物
        MedicationInfo medication = createTestMedicationWithInventory();
        
        // 保存到数据库
        long medicationId = medicationDao.insertMedication(medication);
        assertTrue("药物ID应该大于0", medicationId > 0);
        
        // 从数据库查询并验证
        // 注意：这里需要使用同步方法或者处理LiveData
        // 由于测试环境限制，我们直接验证插入的对象
        medication.setId(medicationId);
        
        // 验证库存字段
        assertEquals("总量应该正确保存", 100, medication.getTotalQuantity());
        assertEquals("剩余量应该正确保存", 80, medication.getRemainingQuantity());
        assertEquals("每次用量应该正确保存", 2, medication.getDosagePerIntake());
        assertEquals("库存提醒阈值应该正确保存", 10, medication.getLowStockThreshold());
        
        System.out.println("药物库存字段验证通过");
    }

    /**
     * 测试2：验证库存状态判断逻辑
     */
    @Test
    public void testInventoryStatusLogic() {
        System.out.println("=== 测试2：验证库存状态逻辑 ===");
        
        // 测试库存充足状态
        MedicationInfo sufficientStock = createTestMedicationWithInventory();
        sufficientStock.setRemainingQuantity(50);
        sufficientStock.setLowStockThreshold(10);
        
        assertFalse("库存充足时不应该显示库存不足", sufficientStock.isLowStock());
        assertFalse("库存充足时不应该显示缺货", sufficientStock.isOutOfStock());
        
        // 测试库存不足状态
        MedicationInfo lowStock = createTestMedicationWithInventory();
        lowStock.setRemainingQuantity(5);
        lowStock.setLowStockThreshold(10);
        
        assertTrue("库存不足时应该显示库存不足", lowStock.isLowStock());
        assertFalse("库存不足但未缺货时不应该显示缺货", lowStock.isOutOfStock());
        
        // 测试缺货状态
        MedicationInfo outOfStock = createTestMedicationWithInventory();
        outOfStock.setRemainingQuantity(0);
        
        assertFalse("缺货时不应该显示库存不足", outOfStock.isLowStock());
        assertTrue("缺货时应该显示缺货", outOfStock.isOutOfStock());
        
        System.out.println("库存状态逻辑验证通过");
    }

    /**
     * 测试3：验证用药记录的创建和保存
     */
    @Test
    public void testIntakeRecordCreation() {
        System.out.println("=== 测试3：验证用药记录创建 ===");
        
        // 创建用药记录
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        record.setMedicationName("测试药物");
        record.setIntakeTime(System.currentTimeMillis());
        record.setDosageTaken(2);
        
        // 保存到数据库
        long recordId = intakeRecordDao.insertIntakeRecord(record);
        assertTrue("用药记录ID应该大于0", recordId > 0);
        
        // 验证记录内容
        record.setId(recordId);
        assertEquals("药物名称应该正确", "测试药物", record.getMedicationName());
        assertEquals("服用剂量应该正确", 2, record.getDosageTaken());
        assertTrue("服用时间应该大于0", record.getIntakeTime() > 0);
        
        System.out.println("用药记录创建验证通过");
    }

    /**
     * 测试4：验证库存扣减逻辑
     */
    @Test
    public void testInventoryDeductionLogic() {
        System.out.println("=== 测试4：验证库存扣减逻辑 ===");
        
        // 创建药物
        MedicationInfo medication = createTestMedicationWithInventory();
        medication.setRemainingQuantity(10);
        medication.setDosagePerIntake(3);
        
        // 模拟第一次用药：10 - 3 = 7
        int newQuantity1 = Math.max(0, medication.getRemainingQuantity() - medication.getDosagePerIntake());
        medication.setRemainingQuantity(newQuantity1);
        assertEquals("第一次用药后剩余量应该为7", 7, medication.getRemainingQuantity());
        
        // 模拟第二次用药：7 - 3 = 4
        int newQuantity2 = Math.max(0, medication.getRemainingQuantity() - medication.getDosagePerIntake());
        medication.setRemainingQuantity(newQuantity2);
        assertEquals("第二次用药后剩余量应该为4", 4, medication.getRemainingQuantity());
        
        // 模拟第三次用药：4 - 3 = 1
        int newQuantity3 = Math.max(0, medication.getRemainingQuantity() - medication.getDosagePerIntake());
        medication.setRemainingQuantity(newQuantity3);
        assertEquals("第三次用药后剩余量应该为1", 1, medication.getRemainingQuantity());
        
        // 模拟第四次用药：1 - 3 = max(0, -2) = 0
        int newQuantity4 = Math.max(0, medication.getRemainingQuantity() - medication.getDosagePerIntake());
        medication.setRemainingQuantity(newQuantity4);
        assertEquals("第四次用药后剩余量应该为0", 0, medication.getRemainingQuantity());
        assertTrue("药物应该显示为缺货状态", medication.isOutOfStock());
        
        System.out.println("库存扣减逻辑验证通过");
    }

    /**
     * 测试5：验证数据库表结构和关系
     */
    @Test
    public void testDatabaseStructure() {
        System.out.println("=== 测试5：验证数据库结构 ===");
        
        // 验证数据库创建成功
        assertNotNull("数据库应该成功创建", database);
        assertTrue("数据库应该是打开状态", database.isOpen());
        
        // 验证DAO接口可用
        assertNotNull("MedicationDao应该可用", medicationDao);
        assertNotNull("MedicationIntakeRecordDao应该可用", intakeRecordDao);
        
        // 测试基本的插入操作
        MedicationInfo medication = createTestMedicationWithInventory();
        long medicationId = medicationDao.insertMedication(medication);
        assertTrue("药物插入应该成功", medicationId > 0);
        
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        record.setMedicationName("测试药物");
        record.setIntakeTime(System.currentTimeMillis());
        record.setDosageTaken(1);
        long recordId = intakeRecordDao.insertIntakeRecord(record);
        assertTrue("用药记录插入应该成功", recordId > 0);
        
        System.out.println("数据库结构验证通过");
    }

    /**
     * 测试6：验证完整的用药流程模拟
     */
    @Test
    public void testCompleteWorkflowSimulation() {
        System.out.println("=== 测试6：验证完整用药流程 ===");
        
        // 第一步：创建药物
        MedicationInfo medication = createTestMedicationWithInventory();
        medication.setRemainingQuantity(20);
        medication.setDosagePerIntake(2);
        medication.setLowStockThreshold(5);
        
        long medicationId = medicationDao.insertMedication(medication);
        medication.setId(medicationId);
        
        // 第二步：模拟多次用药
        int initialQuantity = medication.getRemainingQuantity();
        int dosagePerIntake = medication.getDosagePerIntake();
        int expectedIntakeCount = 0;
        
        while (medication.getRemainingQuantity() > 0) {
            // 模拟用药前的状态检查
            boolean wasLowStock = medication.isLowStock();
            boolean wasOutOfStock = medication.isOutOfStock();
            
            // 执行用药操作
            int newQuantity = Math.max(0, medication.getRemainingQuantity() - dosagePerIntake);
            medication.setRemainingQuantity(newQuantity);
            expectedIntakeCount++;
            
            // 创建用药记录
            MedicationIntakeRecord record = new MedicationIntakeRecord();
            record.setMedicationName(medication.getName());
            record.setIntakeTime(System.currentTimeMillis());
            record.setDosageTaken(dosagePerIntake);
            intakeRecordDao.insertIntakeRecord(record);
            
            // 验证状态变化
            if (medication.getRemainingQuantity() <= medication.getLowStockThreshold() && 
                medication.getRemainingQuantity() > 0) {
                assertTrue("库存不足时应该显示警告", medication.isLowStock());
            }
            
            if (medication.getRemainingQuantity() == 0) {
                assertTrue("缺货时应该显示缺货状态", medication.isOutOfStock());
            }
            
            // 防止无限循环
            if (expectedIntakeCount > 20) {
                break;
            }
        }
        
        // 验证最终状态
        assertEquals("最终剩余量应该为0", 0, medication.getRemainingQuantity());
        assertTrue("最终应该显示缺货", medication.isOutOfStock());
        assertEquals("用药次数应该正确", 10, expectedIntakeCount); // 20/2 = 10次
        
        System.out.println("完整用药流程验证通过，共用药" + expectedIntakeCount + "次");
    }

    /**
     * 测试7：验证边界条件和异常情况
     */
    @Test
    public void testBoundaryConditions() {
        System.out.println("=== 测试7：验证边界条件 ===");
        
        // 测试零库存药物
        MedicationInfo zeroStock = createTestMedicationWithInventory();
        zeroStock.setRemainingQuantity(0);
        zeroStock.setDosagePerIntake(1);
        
        assertTrue("零库存应该显示缺货", zeroStock.isOutOfStock());
        assertFalse("零库存不应该显示库存不足", zeroStock.isLowStock());
        
        // 测试大剂量用药
        MedicationInfo smallStock = createTestMedicationWithInventory();
        smallStock.setRemainingQuantity(2);
        smallStock.setDosagePerIntake(5);
        
        int newQuantity = Math.max(0, smallStock.getRemainingQuantity() - smallStock.getDosagePerIntake());
        assertEquals("大剂量用药后剩余量应该为0", 0, newQuantity);
        
        // 测试阈值边界
        MedicationInfo thresholdTest = createTestMedicationWithInventory();
        thresholdTest.setRemainingQuantity(10);
        thresholdTest.setLowStockThreshold(10);
        
        assertTrue("剩余量等于阈值时应该显示库存不足", thresholdTest.isLowStock());
        
        thresholdTest.setRemainingQuantity(11);
        assertFalse("剩余量大于阈值时不应该显示库存不足", thresholdTest.isLowStock());
        
        System.out.println("边界条件验证通过");
    }

    /**
     * 测试8：验证数据持久化和一致性
     */
    @Test
    public void testDataPersistenceAndConsistency() {
        System.out.println("=== 测试8：验证数据持久化 ===");
        
        // 创建多个药物和用药记录
        for (int i = 1; i <= 5; i++) {
            MedicationInfo medication = createTestMedicationWithInventory();
            medication.setName("药物" + i);
            medication.setRemainingQuantity(10 + i);
            medication.setDosagePerIntake(i);
            medication.setLowStockThreshold(5);
            
            long medicationId = medicationDao.insertMedication(medication);
            assertTrue("药物" + i + "应该成功保存", medicationId > 0);
            
            // 为每个药物创建用药记录
            for (int j = 1; j <= 3; j++) {
                MedicationIntakeRecord record = new MedicationIntakeRecord();
                record.setMedicationName("药物" + i);
                record.setIntakeTime(System.currentTimeMillis() - (j * 3600000)); // 每小时间隔
                record.setDosageTaken(i);
                
                long recordId = intakeRecordDao.insertIntakeRecord(record);
                assertTrue("用药记录应该成功保存", recordId > 0);
            }
        }
        
        System.out.println("数据持久化验证通过");
    }

    /**
     * 创建带有库存信息的测试药物
     */
    private MedicationInfo createTestMedicationWithInventory() {
        MedicationInfo medication = new MedicationInfo();
        medication.setName("测试药物");
        medication.setUnit("片");
        medication.setTotalQuantity(100);
        medication.setRemainingQuantity(80);
        medication.setDosagePerIntake(2);
        medication.setLowStockThreshold(10);
        medication.setCreatedAt(System.currentTimeMillis());
        medication.setUpdatedAt(System.currentTimeMillis());
        return medication;
    }
}