package com.medication.reminders;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.repository.MedicationRepository;
import com.medication.reminders.viewmodels.AddMedicationViewModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * 测试AddMedicationViewModel中库存管理字段的功能
 */
@RunWith(RobolectricTestRunner.class)
public class AddMedicationInventoryTest {

    @Mock
    private MedicationRepository mockRepository;

    private AddMedicationViewModel viewModel;
    private Application application;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        application = RuntimeEnvironment.getApplication();
        
        // 创建一个可以注入mock repository的ViewModel
        viewModel = new AddMedicationViewModel(application) {
            @Override
            protected MedicationRepository createRepository(Application application) {
                return mockRepository;
            }
        };
    }

    @Test
    public void testInventoryFieldsInitialization() {
        // 验证库存字段的默认值
        assertEquals(Integer.valueOf(1), viewModel.getDosagePerIntake().getValue());
        assertEquals(Integer.valueOf(5), viewModel.getLowStockThreshold().getValue());
    }

    @Test
    public void testSetDosagePerIntake() {
        // 测试设置每次用量
        viewModel.setDosagePerIntake(2);
        assertEquals(Integer.valueOf(2), viewModel.getDosagePerIntake().getValue());
        
        // 测试负值会被设置为1
        viewModel.setDosagePerIntake(-1);
        assertEquals(Integer.valueOf(1), viewModel.getDosagePerIntake().getValue());
        
        // 测试null值会被设置为1
        viewModel.setDosagePerIntake(null);
        assertEquals(Integer.valueOf(1), viewModel.getDosagePerIntake().getValue());
    }

    @Test
    public void testSetLowStockThreshold() {
        // 测试设置库存提醒阈值
        viewModel.setLowStockThreshold(10);
        assertEquals(Integer.valueOf(10), viewModel.getLowStockThreshold().getValue());
        
        // 测试负值会被设置为0
        viewModel.setLowStockThreshold(-1);
        assertEquals(Integer.valueOf(0), viewModel.getLowStockThreshold().getValue());
        
        // 测试null值会被设置为5
        viewModel.setLowStockThreshold(null);
        assertEquals(Integer.valueOf(5), viewModel.getLowStockThreshold().getValue());
    }

    @Test
    public void testInventoryFieldsValidation() {
        // 设置基本有效数据
        viewModel.setMedicationName("测试药物");
        viewModel.setSelectedColor("WHITE");
        viewModel.setSelectedDosageForm("TABLET");
        viewModel.setTotalQuantity(100);
        viewModel.setRemainingQuantity(50);
        viewModel.setUnit("片");
        
        // 测试有效的库存字段
        viewModel.setDosagePerIntake(2);
        viewModel.setLowStockThreshold(10);
        
        // 模拟保存成功
        doAnswer(invocation -> {
            MedicationRepository.InsertCallback callback = invocation.getArgument(2);
            callback.onSuccess(1L);
            return null;
        }).when(mockRepository).insertMedication(any(MedicationInfo.class), anyBoolean(), any(MedicationRepository.InsertCallback.class));
        
        viewModel.saveMedication();
        
        // 验证没有错误
        assertNull(viewModel.getDosagePerIntakeError().getValue());
        assertNull(viewModel.getLowStockThresholdError().getValue());
    }

    @Test
    public void testInventoryFieldsValidationErrors() {
        // 设置基本有效数据
        viewModel.setMedicationName("测试药物");
        viewModel.setSelectedColor("WHITE");
        viewModel.setSelectedDosageForm("TABLET");
        viewModel.setTotalQuantity(10);
        viewModel.setRemainingQuantity(5);
        viewModel.setUnit("片");
        
        // 设置无效的库存字段
        viewModel.setDosagePerIntake(10); // 大于剩余量
        viewModel.setLowStockThreshold(15); // 大于总量
        
        viewModel.saveMedication();
        
        // 验证有错误消息
        assertNotNull(viewModel.getDosagePerIntakeError().getValue());
        assertNotNull(viewModel.getLowStockThresholdError().getValue());
    }

    @Test
    public void testEditModeInventoryFields() {
        // 创建一个包含库存信息的药物
        MedicationInfo medication = new MedicationInfo();
        medication.setId(1L);
        medication.setName("测试药物");
        medication.setColor("WHITE");
        medication.setDosageForm("TABLET");
        medication.setTotalQuantity(100);
        medication.setRemainingQuantity(80);
        medication.setUnit("片");
        medication.setDosagePerIntake(3);
        medication.setLowStockThreshold(15);
        
        // 模拟编辑模式
        viewModel.startEditFrom(medication);
        
        // 验证库存字段被正确加载
        assertEquals(Integer.valueOf(3), viewModel.getDosagePerIntake().getValue());
        assertEquals(Integer.valueOf(15), viewModel.getLowStockThreshold().getValue());
        assertTrue(viewModel.isEditMode());
        assertEquals(1L, viewModel.getEditingMedicationId());
    }

    @Test
    public void testHasFormDataWithInventoryFields() {
        // 测试只有库存字段时是否被认为有数据
        viewModel.setDosagePerIntake(3); // 不是默认值1
        assertTrue(viewModel.hasFormData());
        
        // 重置
        viewModel.clearForm();
        viewModel.setLowStockThreshold(10); // 不是默认值5
        assertTrue(viewModel.hasFormData());
    }

    @Test
    public void testClearFormResetsInventoryFields() {
        // 设置库存字段
        viewModel.setDosagePerIntake(3);
        viewModel.setLowStockThreshold(10);
        
        // 验证字段已设置
        assertEquals(Integer.valueOf(3), viewModel.getDosagePerIntake().getValue());
        assertEquals(Integer.valueOf(10), viewModel.getLowStockThreshold().getValue());
        
        // 清除表单
        viewModel.clearForm();
        
        // 验证库存字段被重置为默认值
        Integer dosageValue = viewModel.getDosagePerIntake().getValue();
        Integer thresholdValue = viewModel.getLowStockThreshold().getValue();
        
        System.out.println("After clearForm - dosagePerIntake: " + dosageValue + ", lowStockThreshold: " + thresholdValue);
        
        assertEquals("DosagePerIntake should be reset to 1", Integer.valueOf(1), dosageValue);
        assertEquals("LowStockThreshold should be reset to 5", Integer.valueOf(5), thresholdValue);
    }

    @Test
    public void testGetCurrentMedicationDataIncludesInventoryFields() {
        // 设置所有字段包括库存字段
        viewModel.setMedicationName("测试药物");
        viewModel.setSelectedColor("WHITE");
        viewModel.setSelectedDosageForm("TABLET");
        viewModel.setTotalQuantity(100);
        viewModel.setRemainingQuantity(80);
        viewModel.setUnit("片");
        viewModel.setDosagePerIntake(2);
        viewModel.setLowStockThreshold(15);
        
        MedicationInfo medication = viewModel.getCurrentMedicationData();
        
        // 验证库存字段被包含在返回的数据中
        assertEquals(2, medication.getDosagePerIntake());
        assertEquals(15, medication.getLowStockThreshold());
    }
}