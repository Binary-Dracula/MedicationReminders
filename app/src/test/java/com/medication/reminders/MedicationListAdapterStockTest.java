package com.medication.reminders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.medication.reminders.adapter.MedicationListAdapter;
import com.medication.reminders.database.entity.MedicationInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 测试药物列表适配器的库存状态显示功能
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class MedicationListAdapterStockTest {

    private Context context;
    private MedicationListAdapter adapter;
    private List<MedicationInfo> medications;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        medications = new ArrayList<>();
        adapter = new MedicationListAdapter(medications, medication -> {
            // Mock click listener
        });
    }

    /**
     * 测试库存充足状态的药物显示
     */
    @Test
    public void testSufficientStockDisplay() {
        // 创建库存充足的药物（剩余量 > 提醒阈值）
        MedicationInfo medication = createTestMedication("阿司匹林", 100, 50, 5, "片");
        
        // 验证库存状态
        assertFalse("库存充足的药物不应该显示为库存不足", medication.isLowStock());
        assertFalse("库存充足的药物不应该显示为缺货", medication.isOutOfStock());
    }

    /**
     * 测试库存不足状态的药物显示
     */
    @Test
    public void testLowStockDisplay() {
        // 创建库存不足的药物（剩余量 <= 提醒阈值且 > 0）
        MedicationInfo medication = createTestMedication("维生素C", 100, 3, 5, "片");
        
        // 验证库存状态
        assertTrue("库存不足的药物应该显示为库存不足", medication.isLowStock());
        assertFalse("库存不足的药物不应该显示为缺货", medication.isOutOfStock());
    }

    /**
     * 测试缺货状态的药物显示
     */
    @Test
    public void testOutOfStockDisplay() {
        // 创建缺货的药物（剩余量 = 0）
        MedicationInfo medication = createTestMedication("钙片", 100, 0, 5, "片");
        
        // 验证库存状态
        assertFalse("缺货的药物不应该显示为库存不足", medication.isLowStock());
        assertTrue("缺货的药物应该显示为缺货", medication.isOutOfStock());
    }

    /**
     * 测试边界情况：剩余量等于提醒阈值
     */
    @Test
    public void testBoundaryStockDisplay() {
        // 创建剩余量等于提醒阈值的药物
        MedicationInfo medication = createTestMedication("感冒药", 100, 5, 5, "粒");
        
        // 验证库存状态
        assertTrue("剩余量等于提醒阈值的药物应该显示为库存不足", medication.isLowStock());
        assertFalse("剩余量等于提醒阈值的药物不应该显示为缺货", medication.isOutOfStock());
    }

    /**
     * 测试适配器更新药物列表功能
     */
    @Test
    public void testAdapterUpdateMedications() {
        // 创建测试药物列表
        List<MedicationInfo> testMedications = new ArrayList<>();
        testMedications.add(createTestMedication("药物1", 100, 50, 5, "片"));
        testMedications.add(createTestMedication("药物2", 50, 3, 5, "粒"));
        testMedications.add(createTestMedication("药物3", 30, 0, 5, "毫升"));

        // 更新适配器数据
        adapter.updateMedications(testMedications);

        // 验证适配器数据更新
        assertEquals("适配器应该包含3个药物项目", 3, adapter.getItemCount());
    }

    /**
     * 创建测试用的药物信息
     */
    private MedicationInfo createTestMedication(String name, int totalQuantity, 
                                              int remainingQuantity, int lowStockThreshold, String unit) {
        MedicationInfo medication = new MedicationInfo();
        medication.setName(name);
        medication.setColor("WHITE");
        medication.setDosageForm("TABLET");
        medication.setTotalQuantity(totalQuantity);
        medication.setRemainingQuantity(remainingQuantity);
        medication.setLowStockThreshold(lowStockThreshold);
        medication.setUnit(unit);
        medication.setDosagePerIntake(1);
        medication.setCreatedAt(System.currentTimeMillis());
        medication.setUpdatedAt(System.currentTimeMillis());
        return medication;
    }
}