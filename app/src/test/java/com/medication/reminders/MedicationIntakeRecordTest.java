package com.medication.reminders;

import org.junit.Test;
import static org.junit.Assert.*;

import com.medication.reminders.database.entity.MedicationIntakeRecord;

/**
 * 用药记录实体类的单元测试
 * 验证新的简化MedicationIntakeRecord实体的基本功能
 */
public class MedicationIntakeRecordTest {

    @Test
    public void testDefaultConstructor() {
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        
        // 验证默认值
        assertEquals(1, record.getDosageTaken());
        assertTrue("服用时间应该接近当前时间", 
            Math.abs(System.currentTimeMillis() - record.getIntakeTime()) < 1000);
    }

    @Test
    public void testParameterizedConstructor() {
        String medicationName = "阿司匹林";
        long intakeTime = System.currentTimeMillis() - 1000;
        int dosageTaken = 2;
        
        MedicationIntakeRecord record = new MedicationIntakeRecord(medicationName, intakeTime, dosageTaken);
        
        assertEquals(medicationName, record.getMedicationName());
        assertEquals(intakeTime, record.getIntakeTime());
        assertEquals(dosageTaken, record.getDosageTaken());
    }

    @Test
    public void testSimplifiedConstructor() {
        String medicationName = "维生素C";
        int dosageTaken = 3;
        
        MedicationIntakeRecord record = new MedicationIntakeRecord(medicationName, dosageTaken);
        
        assertEquals(medicationName, record.getMedicationName());
        assertEquals(dosageTaken, record.getDosageTaken());
        assertTrue("服用时间应该接近当前时间", 
            Math.abs(System.currentTimeMillis() - record.getIntakeTime()) < 1000);
    }

    @Test
    public void testGettersAndSetters() {
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        
        // 测试ID
        record.setId(123L);
        assertEquals(123L, record.getId());
        
        // 测试药物名称
        String medicationName = "感冒灵";
        record.setMedicationName(medicationName);
        assertEquals(medicationName, record.getMedicationName());
        
        // 测试服用时间
        long intakeTime = System.currentTimeMillis() - 5000;
        record.setIntakeTime(intakeTime);
        assertEquals(intakeTime, record.getIntakeTime());
        
        // 测试服用剂量
        int dosageTaken = 4;
        record.setDosageTaken(dosageTaken);
        assertEquals(dosageTaken, record.getDosageTaken());
    }

    @Test
    public void testToString() {
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        record.setId(1L);
        record.setMedicationName("测试药物");
        record.setIntakeTime(1234567890L);
        record.setDosageTaken(2);
        
        String result = record.toString();
        
        assertTrue("toString应该包含ID", result.contains("id=1"));
        assertTrue("toString应该包含药物名称", result.contains("medicationName='测试药物'"));
        assertTrue("toString应该包含服用时间", result.contains("intakeTime=1234567890"));
        assertTrue("toString应该包含服用剂量", result.contains("dosageTaken=2"));
    }

    @Test
    public void testNullMedicationName() {
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        record.setMedicationName(null);
        
        assertNull("药物名称可以为null", record.getMedicationName());
    }

    @Test
    public void testZeroDosage() {
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        record.setDosageTaken(0);
        
        assertEquals("服用剂量可以为0", 0, record.getDosageTaken());
    }

    @Test
    public void testNegativeDosage() {
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        record.setDosageTaken(-1);
        
        assertEquals("服用剂量可以为负数（虽然不推荐）", -1, record.getDosageTaken());
    }
}