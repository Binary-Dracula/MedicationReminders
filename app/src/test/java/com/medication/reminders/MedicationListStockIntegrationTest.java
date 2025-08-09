package com.medication.reminders;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.medication.reminders.database.entity.MedicationInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * 药物列表库存状态显示集成测试
 * 测试库存状态的完整业务逻辑
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class MedicationListStockIntegrationTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    /**
     * 测试完整的库存状态判断逻辑
     */
    @Test
    public void testCompleteStockStatusLogic() {
        // 测试场景1：库存充足（绿色）
        MedicationInfo sufficientStock = createMedication("阿司匹林", 100, 50, 10);
        assertFalse("库存充足时不应该是低库存", sufficientStock.isLowStock());
        assertFalse("库存充足时不应该是缺货", sufficientStock.isOutOfStock());

        // 测试场景2：库存不足（橙色）
        MedicationInfo lowStock = createMedication("维生素C", 100, 8, 10);
        assertTrue("库存不足时应该是低库存", lowStock.isLowStock());
        assertFalse("库存不足时不应该是缺货", lowStock.isOutOfStock());

        // 测试场景3：缺货（红色）
        MedicationInfo outOfStock = createMedication("钙片", 100, 0, 10);
        assertFalse("缺货时不应该是低库存", outOfStock.isLowStock());
        assertTrue("缺货时应该是缺货状态", outOfStock.isOutOfStock());

        // 测试场景4：边界情况 - 剩余量等于阈值
        MedicationInfo boundaryCase = createMedication("感冒药", 50, 5, 5);
        assertTrue("剩余量等于阈值时应该是低库存", boundaryCase.isLowStock());
        assertFalse("剩余量等于阈值时不应该是缺货", boundaryCase.isOutOfStock());
    }

    /**
     * 测试库存百分比计算
     */
    @Test
    public void testStockPercentageCalculation() {
        // 测试正常情况
        MedicationInfo medication1 = createMedication("药物1", 100, 75, 10);
        assertEquals("75/100应该是75%", 75, medication1.getRemainingPercentage());

        MedicationInfo medication2 = createMedication("药物2", 50, 25, 5);
        assertEquals("25/50应该是50%", 50, medication2.getRemainingPercentage());

        // 测试边界情况
        MedicationInfo medication3 = createMedication("药物3", 100, 0, 10);
        assertEquals("0/100应该是0%", 0, medication3.getRemainingPercentage());

        MedicationInfo medication4 = createMedication("药物4", 30, 30, 5);
        assertEquals("30/30应该是100%", 100, medication4.getRemainingPercentage());

        // 测试总量为0的情况
        MedicationInfo medication5 = createMedication("药物5", 0, 0, 5);
        assertEquals("总量为0时应该返回0%", 0, medication5.getRemainingPercentage());
    }

    /**
     * 测试库存扣减功能
     */
    @Test
    public void testStockReduction() {
        MedicationInfo medication = createMedication("测试药物", 100, 50, 10);
        medication.setDosagePerIntake(2);

        // 模拟用药扣减
        int originalQuantity = medication.getRemainingQuantity();
        medication.reduceQuantity(medication.getDosagePerIntake());

        assertEquals("扣减后剩余量应该正确", 
            originalQuantity - 2, medication.getRemainingQuantity());

        // 测试扣减到负数的情况
        medication.setRemainingQuantity(1);
        medication.reduceQuantity(5);
        assertEquals("扣减到负数时应该设为0", 0, medication.getRemainingQuantity());
    }

    /**
     * 测试不同单位的库存显示
     */
    @Test
    public void testDifferentUnits() {
        MedicationInfo tablets = createMedication("片剂药物", 100, 50, 10);
        tablets.setUnit("片");
        assertEquals("片剂单位应该正确", "片", tablets.getUnit());

        MedicationInfo capsules = createMedication("胶囊药物", 60, 30, 5);
        capsules.setUnit("粒");
        assertEquals("胶囊单位应该正确", "粒", capsules.getUnit());

        MedicationInfo liquid = createMedication("液体药物", 500, 250, 50);
        liquid.setUnit("毫升");
        assertEquals("液体单位应该正确", "毫升", liquid.getUnit());
    }

    /**
     * 测试库存状态变化
     */
    @Test
    public void testStockStatusTransition() {
        MedicationInfo medication = createMedication("状态变化测试", 100, 50, 10);

        // 初始状态：库存充足
        assertFalse("初始状态应该是库存充足", medication.isLowStock());
        assertFalse("初始状态不应该是缺货", medication.isOutOfStock());

        // 扣减到库存不足
        medication.setRemainingQuantity(8);
        assertTrue("扣减后应该是库存不足", medication.isLowStock());
        assertFalse("扣减后不应该是缺货", medication.isOutOfStock());

        // 扣减到缺货
        medication.setRemainingQuantity(0);
        assertFalse("缺货时不应该是库存不足", medication.isLowStock());
        assertTrue("缺货时应该是缺货状态", medication.isOutOfStock());

        // 补充库存
        medication.setRemainingQuantity(50);
        assertFalse("补充后应该是库存充足", medication.isLowStock());
        assertFalse("补充后不应该是缺货", medication.isOutOfStock());
    }

    /**
     * 创建测试用药物信息
     */
    private MedicationInfo createMedication(String name, int totalQuantity, 
                                          int remainingQuantity, int lowStockThreshold) {
        MedicationInfo medication = new MedicationInfo();
        medication.setName(name);
        medication.setColor("WHITE");
        medication.setDosageForm("TABLET");
        medication.setTotalQuantity(totalQuantity);
        medication.setRemainingQuantity(remainingQuantity);
        medication.setLowStockThreshold(lowStockThreshold);
        medication.setUnit("片");
        medication.setDosagePerIntake(1);
        medication.setCreatedAt(System.currentTimeMillis());
        medication.setUpdatedAt(System.currentTimeMillis());
        return medication;
    }
}