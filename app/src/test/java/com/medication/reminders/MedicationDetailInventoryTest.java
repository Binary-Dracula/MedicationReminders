package com.medication.reminders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.test.core.app.ApplicationProvider;

import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.view.MedicationDetailActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

/**
 * 药物详情页面库存状态显示测试
 * 测试库存信息显示和状态警告功能
 */
@RunWith(RobolectricTestRunner.class)
public class MedicationDetailInventoryTest {

    private MedicationDetailActivity activity;
    private ActivityController<MedicationDetailActivity> controller;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        
        // 创建带有药物ID的Intent
        Intent intent = new Intent(context, MedicationDetailActivity.class);
        intent.putExtra("medication_id", 1L);
        
        controller = Robolectric.buildActivity(MedicationDetailActivity.class, intent);
        activity = controller.get();
    }

    /**
     * 测试库存充足时的显示状态
     */
    @Test
    public void testSufficientStockDisplay() {
        // 创建库存充足的药物
        MedicationInfo medication = createTestMedication(100, 50, 2, 10, "片");
        
        // 模拟显示库存信息的方法调用
        // 由于无法直接调用private方法，这里测试业务逻辑
        assertFalse("库存充足时不应显示为库存不足", medication.isLowStock());
        assertFalse("库存充足时不应显示为缺货", medication.isOutOfStock());
        assertEquals("剩余量百分比计算正确", 50, medication.getRemainingPercentage());
    }

    /**
     * 测试库存不足时的显示状态
     */
    @Test
    public void testLowStockDisplay() {
        // 创建库存不足的药物（剩余量等于阈值）
        MedicationInfo medication = createTestMedication(100, 10, 2, 10, "片");
        
        assertTrue("库存不足时应显示为库存不足", medication.isLowStock());
        assertFalse("库存不足但未缺货时不应显示为缺货", medication.isOutOfStock());
        assertEquals("剩余量百分比计算正确", 10, medication.getRemainingPercentage());
    }

    /**
     * 测试缺货时的显示状态
     */
    @Test
    public void testOutOfStockDisplay() {
        // 创建缺货的药物
        MedicationInfo medication = createTestMedication(100, 0, 2, 10, "片");
        
        assertFalse("缺货时不应显示为库存不足", medication.isLowStock());
        assertTrue("缺货时应显示为缺货", medication.isOutOfStock());
        assertEquals("缺货时剩余量百分比应为0", 0, medication.getRemainingPercentage());
    }

    /**
     * 测试库存信息字段显示
     */
    @Test
    public void testInventoryFieldsDisplay() {
        MedicationInfo medication = createTestMedication(100, 25, 3, 15, "粒");
        
        // 验证各个字段的值
        assertEquals("总量正确", 100, medication.getTotalQuantity());
        assertEquals("剩余量正确", 25, medication.getRemainingQuantity());
        assertEquals("每次用量正确", 3, medication.getDosagePerIntake());
        assertEquals("库存提醒阈值正确", 15, medication.getLowStockThreshold());
        assertEquals("单位正确", "粒", medication.getUnit());
    }

    /**
     * 测试边界情况 - 剩余量刚好等于阈值
     */
    @Test
    public void testBoundaryCondition_ExactThreshold() {
        MedicationInfo medication = createTestMedication(100, 20, 2, 20, "片");
        
        assertTrue("剩余量等于阈值时应显示为库存不足", medication.isLowStock());
        assertFalse("剩余量等于阈值时不应显示为缺货", medication.isOutOfStock());
    }

    /**
     * 测试边界情况 - 剩余量刚好大于阈值
     */
    @Test
    public void testBoundaryCondition_AboveThreshold() {
        MedicationInfo medication = createTestMedication(100, 21, 2, 20, "片");
        
        assertFalse("剩余量大于阈值时不应显示为库存不足", medication.isLowStock());
        assertFalse("剩余量大于阈值时不应显示为缺货", medication.isOutOfStock());
    }

    /**
     * 测试不同单位的显示
     */
    @Test
    public void testDifferentUnits() {
        String[] units = {"片", "粒", "毫升", "克", "袋"};
        
        for (String unit : units) {
            MedicationInfo medication = createTestMedication(50, 25, 1, 5, unit);
            assertEquals("单位设置正确", unit, medication.getUnit());
        }
    }

    /**
     * 创建测试用的药物信息
     */
    private MedicationInfo createTestMedication(int totalQuantity, int remainingQuantity, 
                                              int dosagePerIntake, int lowStockThreshold, String unit) {
        MedicationInfo medication = new MedicationInfo();
        medication.setId(1L);
        medication.setName("测试药物");
        medication.setColor("WHITE");
        medication.setDosageForm("TABLET");
        medication.setTotalQuantity(totalQuantity);
        medication.setRemainingQuantity(remainingQuantity);
        medication.setDosagePerIntake(dosagePerIntake);
        medication.setLowStockThreshold(lowStockThreshold);
        medication.setUnit(unit);
        medication.setCreatedAt(System.currentTimeMillis());
        medication.setUpdatedAt(System.currentTimeMillis());
        return medication;
    }
}