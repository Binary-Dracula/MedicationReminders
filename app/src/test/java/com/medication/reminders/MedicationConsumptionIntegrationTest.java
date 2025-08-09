package com.medication.reminders;

import static org.junit.Assert.*;

import org.junit.Test;

import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.database.entity.MedicationIntakeRecord;

/**
 * 用药扣减功能集成测试
 * 验证完整的用药流程逻辑
 */
public class MedicationConsumptionIntegrationTest {
    
    /**
     * 测试完整的用药流程
     * 模拟从用药确认到库存更新和记录创建的完整过程
     */
    @Test
    public void testCompleteConsumptionFlow() {
        // 1. 创建测试药物
        MedicationInfo medication = createTestMedication(1L, "阿司匹林", 100, 20, 1, 5);
        
        // 2. 模拟用药前的状态检查
        assertFalse("用药前库存应该充足", medication.isLowStock());
        assertFalse("用药前不应该缺货", medication.isOutOfStock());
        assertEquals("用药前剩余量应该是20", 20, medication.getRemainingQuantity());
        
        // 3. 模拟用药扣减逻辑
        int dosagePerIntake = medication.getDosagePerIntake();
        int currentRemaining = medication.getRemainingQuantity();
        int newRemainingQuantity = Math.max(0, currentRemaining - dosagePerIntake);
        
        // 4. 验证扣减结果
        assertEquals("扣减后剩余量应该是19", 19, newRemainingQuantity);
        
        // 5. 模拟更新药物信息
        medication.setRemainingQuantity(newRemainingQuantity);
        medication.setUpdatedAt(System.currentTimeMillis());
        
        // 6. 模拟创建用药记录
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        record.setMedicationName(medication.getName());
        record.setIntakeTime(System.currentTimeMillis());
        record.setDosageTaken(dosagePerIntake);
        
        // 7. 验证用药记录
        assertEquals("记录中的药物名称应该正确", "阿司匹林", record.getMedicationName());
        assertEquals("记录中的用药剂量应该正确", 1, record.getDosageTaken());
        assertTrue("记录的时间应该是合理的", record.getIntakeTime() > 0);
        
        // 8. 验证用药后的库存状态
        assertFalse("用药后库存仍然充足", medication.isLowStock());
        assertFalse("用药后不应该缺货", medication.isOutOfStock());
    }
    
    /**
     * 测试库存不足场景的完整流程
     */
    @Test
    public void testLowStockScenario() {
        // 1. 创建库存接近阈值的药物
        MedicationInfo medication = createTestMedication(2L, "维生素C", 100, 6, 1, 5);
        
        // 2. 执行用药扣减
        int newRemainingQuantity = Math.max(0, medication.getRemainingQuantity() - medication.getDosagePerIntake());
        medication.setRemainingQuantity(newRemainingQuantity);
        
        // 3. 验证库存状态
        assertEquals("扣减后剩余量应该是5", 5, newRemainingQuantity);
        assertTrue("应该触发库存不足提醒", medication.isLowStock());
        assertFalse("不应该缺货", medication.isOutOfStock());
        
        // 4. 再次用药
        newRemainingQuantity = Math.max(0, medication.getRemainingQuantity() - medication.getDosagePerIntake());
        medication.setRemainingQuantity(newRemainingQuantity);
        
        // 5. 验证继续用药后的状态
        assertEquals("再次扣减后剩余量应该是4", 4, newRemainingQuantity);
        assertTrue("仍然应该是库存不足", medication.isLowStock());
    }
    
    /**
     * 测试缺货场景的完整流程
     */
    @Test
    public void testOutOfStockScenario() {
        // 1. 创建只剩1片的药物
        MedicationInfo medication = createTestMedication(3L, "感冒药", 100, 1, 1, 5);
        
        // 2. 执行用药扣减
        int newRemainingQuantity = Math.max(0, medication.getRemainingQuantity() - medication.getDosagePerIntake());
        medication.setRemainingQuantity(newRemainingQuantity);
        
        // 3. 验证缺货状态
        assertEquals("扣减后剩余量应该是0", 0, newRemainingQuantity);
        assertFalse("缺货时不应该显示库存不足", medication.isLowStock());
        assertTrue("应该标记为缺货", medication.isOutOfStock());
        
        // 4. 尝试再次用药（剩余量已为0）
        int attemptedConsumption = Math.max(0, medication.getRemainingQuantity() - medication.getDosagePerIntake());
        
        // 5. 验证仍然为0
        assertEquals("缺货后再次用药剩余量仍为0", 0, attemptedConsumption);
    }
    
    /**
     * 测试大剂量用药场景
     */
    @Test
    public void testLargeDosageConsumption() {
        // 1. 创建每次用量较大的药物
        MedicationInfo medication = createTestMedication(4L, "止痛药", 100, 10, 3, 5);
        
        // 2. 执行用药扣减
        int newRemainingQuantity = Math.max(0, medication.getRemainingQuantity() - medication.getDosagePerIntake());
        medication.setRemainingQuantity(newRemainingQuantity);
        
        // 3. 验证扣减结果
        assertEquals("大剂量扣减后剩余量应该是7", 7, newRemainingQuantity);
        
        // 4. 再次用药
        newRemainingQuantity = Math.max(0, medication.getRemainingQuantity() - medication.getDosagePerIntake());
        medication.setRemainingQuantity(newRemainingQuantity);
        
        // 5. 验证再次扣减结果
        assertEquals("再次大剂量扣减后剩余量应该是4", 4, newRemainingQuantity);
        assertTrue("应该触发库存不足提醒", medication.isLowStock());
    }
    
    /**
     * 创建测试用的药物信息
     */
    private MedicationInfo createTestMedication(long id, String name, int totalQuantity, 
                                              int remainingQuantity, int dosagePerIntake, 
                                              int lowStockThreshold) {
        MedicationInfo medication = new MedicationInfo();
        medication.setId(id);
        medication.setName(name);
        medication.setColor("白色");
        medication.setDosageForm("片剂");
        medication.setTotalQuantity(totalQuantity);
        medication.setRemainingQuantity(remainingQuantity);
        medication.setDosagePerIntake(dosagePerIntake);
        medication.setLowStockThreshold(lowStockThreshold);
        medication.setUnit("片");
        medication.setCreatedAt(System.currentTimeMillis());
        medication.setUpdatedAt(System.currentTimeMillis());
        return medication;
    }
}