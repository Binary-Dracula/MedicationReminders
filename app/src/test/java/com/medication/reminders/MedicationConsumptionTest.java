package com.medication.reminders;

import static org.junit.Assert.*;

import org.junit.Test;

import com.medication.reminders.database.entity.MedicationInfo;

/**
 * 用药扣减功能测试类
 * 测试用药扣减的核心业务逻辑
 */
public class MedicationConsumptionTest {
    
    /**
     * 测试库存扣减逻辑
     * 验证剩余量 = max(0, 剩余量 - 每次用量)
     */
    @Test
    public void testInventoryDeductionLogic() {
        // 测试正常扣减
        MedicationInfo medication1 = createTestMedication(1L, "测试药物1", 100, 50, 2, 5);
        int newQuantity1 = Math.max(0, medication1.getRemainingQuantity() - medication1.getDosagePerIntake());
        assertEquals("剩余量应该从50减少到48", 48, newQuantity1);
        
        // 测试库存不足时扣减
        MedicationInfo medication2 = createTestMedication(2L, "测试药物2", 100, 1, 2, 5);
        int newQuantity2 = Math.max(0, medication2.getRemainingQuantity() - medication2.getDosagePerIntake());
        assertEquals("剩余量不足时应该设置为0", 0, newQuantity2);
        
        // 测试刚好用完
        MedicationInfo medication3 = createTestMedication(3L, "测试药物3", 100, 2, 2, 5);
        int newQuantity3 = Math.max(0, medication3.getRemainingQuantity() - medication3.getDosagePerIntake());
        assertEquals("剩余量刚好用完应该为0", 0, newQuantity3);
    }
    
    /**
     * 测试库存状态检查逻辑
     */
    @Test
    public void testStockStatusCheck() {
        // 测试库存充足
        MedicationInfo medication1 = createTestMedication(1L, "测试药物1", 100, 50, 2, 5);
        assertFalse("库存充足时不应该是低库存", medication1.isLowStock());
        assertFalse("库存充足时不应该缺货", medication1.isOutOfStock());
        
        // 测试库存不足
        MedicationInfo medication2 = createTestMedication(2L, "测试药物2", 100, 5, 2, 5);
        assertTrue("剩余量等于阈值时应该是低库存", medication2.isLowStock());
        assertFalse("剩余量等于阈值时不应该缺货", medication2.isOutOfStock());
        
        // 测试库存不足（小于阈值）
        MedicationInfo medication3 = createTestMedication(3L, "测试药物3", 100, 3, 2, 5);
        assertTrue("剩余量小于阈值时应该是低库存", medication3.isLowStock());
        assertFalse("剩余量小于阈值时不应该缺货", medication3.isOutOfStock());
        
        // 测试缺货
        MedicationInfo medication4 = createTestMedication(4L, "测试药物4", 100, 0, 2, 5);
        assertFalse("剩余量为0时不应该是低库存", medication4.isLowStock());
        assertTrue("剩余量为0时应该缺货", medication4.isOutOfStock());
    }
    
    /**
     * 测试用药记录创建的数据结构
     */
    @Test
    public void testIntakeRecordCreation() {
        // 测试用药记录的基本信息
        String medicationName = "测试药物";
        int dosageTaken = 2;
        long currentTime = System.currentTimeMillis();
        
        // 模拟创建用药记录的过程
        com.medication.reminders.database.entity.MedicationIntakeRecord record = 
            new com.medication.reminders.database.entity.MedicationIntakeRecord();
        record.setMedicationName(medicationName);
        record.setIntakeTime(currentTime);
        record.setDosageTaken(dosageTaken);
        
        // 验证记录内容
        assertEquals("药物名称应该正确", medicationName, record.getMedicationName());
        assertEquals("服用剂量应该正确", dosageTaken, record.getDosageTaken());
        assertEquals("服用时间应该正确", currentTime, record.getIntakeTime());
    }
    
    /**
     * 测试边界情况处理
     */
    @Test
    public void testBoundaryConditions() {
        // 测试每次用量为0的情况
        MedicationInfo medication1 = createTestMedication(1L, "测试药物1", 100, 50, 0, 5);
        int newQuantity1 = Math.max(0, medication1.getRemainingQuantity() - medication1.getDosagePerIntake());
        assertEquals("每次用量为0时剩余量不变", 50, newQuantity1);
        
        // 测试剩余量为0的情况
        MedicationInfo medication2 = createTestMedication(2L, "测试药物2", 100, 0, 2, 5);
        int newQuantity2 = Math.max(0, medication2.getRemainingQuantity() - medication2.getDosagePerIntake());
        assertEquals("剩余量为0时仍为0", 0, newQuantity2);
        
        // 测试大剂量用药
        MedicationInfo medication3 = createTestMedication(3L, "测试药物3", 100, 5, 10, 5);
        int newQuantity3 = Math.max(0, medication3.getRemainingQuantity() - medication3.getDosagePerIntake());
        assertEquals("大剂量用药时应设为0", 0, newQuantity3);
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