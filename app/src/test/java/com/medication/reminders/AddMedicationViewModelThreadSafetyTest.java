package com.medication.reminders;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.app.Application;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.repository.MedicationRepository;
import com.medication.reminders.viewmodels.AddMedicationViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 测试AddMedicationViewModel的线程安全性
 * 验证修复后的ViewModel不会在后台线程中调用setValue()
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class AddMedicationViewModelThreadSafetyTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application mockApplication;

    @Mock
    private MedicationRepository mockRepository;

    private AddMedicationViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 创建自定义的ViewModel，注入mock repository
        viewModel = new AddMedicationViewModel(mockApplication) {
            @Override
            protected MedicationRepository createRepository(Application application) {
                return mockRepository;
            }
        };
    }

    /**
     * 测试保存成功时的线程安全性
     */
    @Test
    public void testSaveMedicationSuccess_ThreadSafety() throws InterruptedException {
        // 设置表单数据
        viewModel.setMedicationName("测试药物");
        viewModel.setSelectedColor("白色");
        viewModel.setSelectedDosageForm("片剂");
        viewModel.setTotalQuantity(100);
        viewModel.setRemainingQuantity(80);
        viewModel.setUnit("片");
        viewModel.setDosagePerIntake(2);
        viewModel.setLowStockThreshold(10);

        // 设置观察者
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] saveSuccessReceived = {false};
        final boolean[] loadingStateReceived = {false};

        Observer<Boolean> saveSuccessObserver = success -> {
            if (success != null && success) {
                saveSuccessReceived[0] = true;
                latch.countDown();
            }
        };

        Observer<Boolean> loadingObserver = loading -> {
            if (loading != null) {
                loadingStateReceived[0] = true;
            }
        };

        viewModel.getSaveSuccess().observeForever(saveSuccessObserver);
        viewModel.getIsLoading().observeForever(loadingObserver);

        // 模拟Repository的异步回调（在后台线程中执行）
        doAnswer(invocation -> {
            MedicationRepository.InsertCallback callback = invocation.getArgument(2);
            
            // 在新线程中执行回调，模拟真实的后台线程场景
            new Thread(() -> {
                try {
                    Thread.sleep(100); // 模拟数据库操作延迟
                    callback.onSuccess(1L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
            return null;
        }).when(mockRepository).insertMedication(any(MedicationInfo.class), eq(false), any(MedicationRepository.InsertCallback.class));

        // 执行保存操作
        viewModel.saveMedication();

        // 等待异步操作完成
        assertTrue("保存操作应该在合理时间内完成", latch.await(5, TimeUnit.SECONDS));
        assertTrue("应该接收到保存成功状态", saveSuccessReceived[0]);
        assertTrue("应该接收到加载状态变化", loadingStateReceived[0]);

        // 清理观察者
        viewModel.getSaveSuccess().removeObserver(saveSuccessObserver);
        viewModel.getIsLoading().removeObserver(loadingObserver);
    }

    /**
     * 测试保存失败时的线程安全性
     */
    @Test
    public void testSaveMedicationError_ThreadSafety() throws InterruptedException {
        // 设置表单数据
        viewModel.setMedicationName("测试药物");
        viewModel.setSelectedColor("白色");
        viewModel.setSelectedDosageForm("片剂");
        viewModel.setTotalQuantity(100);
        viewModel.setRemainingQuantity(80);
        viewModel.setUnit("片");
        viewModel.setDosagePerIntake(2);
        viewModel.setLowStockThreshold(10);

        // 设置观察者
        CountDownLatch latch = new CountDownLatch(1);
        final String[] errorMessageReceived = {null};

        Observer<String> errorObserver = error -> {
            if (error != null) {
                errorMessageReceived[0] = error;
                latch.countDown();
            }
        };

        viewModel.getErrorMessage().observeForever(errorObserver);

        // 模拟Repository的异步错误回调
        doAnswer(invocation -> {
            MedicationRepository.InsertCallback callback = invocation.getArgument(2);
            
            // 在新线程中执行回调，模拟真实的后台线程场景
            new Thread(() -> {
                try {
                    Thread.sleep(100); // 模拟数据库操作延迟
                    callback.onError("数据库保存失败");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
            return null;
        }).when(mockRepository).insertMedication(any(MedicationInfo.class), eq(false), any(MedicationRepository.InsertCallback.class));

        // 执行保存操作
        viewModel.saveMedication();

        // 等待异步操作完成
        assertTrue("错误处理应该在合理时间内完成", latch.await(5, TimeUnit.SECONDS));
        assertNotNull("应该接收到错误消息", errorMessageReceived[0]);
        assertTrue("错误消息应该包含预期内容", errorMessageReceived[0].contains("数据库保存失败"));

        // 清理观察者
        viewModel.getErrorMessage().removeObserver(errorObserver);
    }

    /**
     * 测试重复药物检测时的线程安全性
     */
    @Test
    public void testSaveMedicationDuplicate_ThreadSafety() throws InterruptedException {
        // 设置表单数据
        viewModel.setMedicationName("重复药物");
        viewModel.setSelectedColor("白色");
        viewModel.setSelectedDosageForm("片剂");
        viewModel.setTotalQuantity(100);
        viewModel.setRemainingQuantity(80);
        viewModel.setUnit("片");
        viewModel.setDosagePerIntake(2);
        viewModel.setLowStockThreshold(10);

        // 设置观察者
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] duplicateDialogShown = {false};
        final String[] duplicateMedicationName = {null};

        Observer<Boolean> dialogObserver = show -> {
            if (show != null && show) {
                duplicateDialogShown[0] = true;
                latch.countDown();
            }
        };

        Observer<String> nameObserver = name -> {
            if (name != null) {
                duplicateMedicationName[0] = name;
            }
        };

        viewModel.getShowDuplicateDialog().observeForever(dialogObserver);
        viewModel.getDuplicateMedicationName().observeForever(nameObserver);

        // 模拟Repository的异步重复检测回调
        doAnswer(invocation -> {
            MedicationRepository.InsertCallback callback = invocation.getArgument(2);
            
            // 在新线程中执行回调，模拟真实的后台线程场景
            new Thread(() -> {
                try {
                    Thread.sleep(100); // 模拟数据库操作延迟
                    callback.onDuplicateFound("重复药物");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
            return null;
        }).when(mockRepository).insertMedication(any(MedicationInfo.class), eq(false), any(MedicationRepository.InsertCallback.class));

        // 执行保存操作
        viewModel.saveMedication();

        // 等待异步操作完成
        assertTrue("重复检测应该在合理时间内完成", latch.await(5, TimeUnit.SECONDS));
        assertTrue("应该显示重复药物对话框", duplicateDialogShown[0]);
        assertEquals("重复药物名称应该正确", "重复药物", duplicateMedicationName[0]);

        // 清理观察者
        viewModel.getShowDuplicateDialog().removeObserver(dialogObserver);
        viewModel.getDuplicateMedicationName().removeObserver(nameObserver);
    }

    /**
     * 测试编辑模式下更新操作的线程安全性
     */
    @Test
    public void testUpdateMedication_ThreadSafety() throws InterruptedException {
        // 创建测试药物并进入编辑模式
        MedicationInfo existingMedication = new MedicationInfo();
        existingMedication.setId(1L);
        existingMedication.setName("现有药物");
        existingMedication.setColor("白色");
        existingMedication.setDosageForm("片剂");
        existingMedication.setTotalQuantity(100);
        existingMedication.setRemainingQuantity(80);
        existingMedication.setUnit("片");
        existingMedication.setDosagePerIntake(2);
        existingMedication.setLowStockThreshold(10);

        viewModel.startEditFrom(existingMedication);

        // 修改数据
        viewModel.setMedicationName("修改后的药物");

        // 设置观察者
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] updateSuccessReceived = {false};

        Observer<Boolean> successObserver = success -> {
            if (success != null && success) {
                updateSuccessReceived[0] = true;
                latch.countDown();
            }
        };

        viewModel.getSaveSuccess().observeForever(successObserver);

        // 模拟Repository的异步更新回调
        doAnswer(invocation -> {
            MedicationRepository.UpdateCallback callback = invocation.getArgument(1);
            
            // 在新线程中执行回调，模拟真实的后台线程场景
            new Thread(() -> {
                try {
                    Thread.sleep(100); // 模拟数据库操作延迟
                    callback.onSuccess();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
            return null;
        }).when(mockRepository).updateMedication(any(MedicationInfo.class), any(MedicationRepository.UpdateCallback.class));

        // 执行更新操作
        viewModel.saveMedication();

        // 等待异步操作完成
        assertTrue("更新操作应该在合理时间内完成", latch.await(5, TimeUnit.SECONDS));
        assertTrue("应该接收到更新成功状态", updateSuccessReceived[0]);

        // 清理观察者
        viewModel.getSaveSuccess().removeObserver(successObserver);
    }

    /**
     * 测试表单数据设置的线程安全性
     */
    @Test
    public void testFormDataSetting_ThreadSafety() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(6);
        
        // 设置观察者来验证数据更新
        viewModel.getMedicationName().observeForever(name -> {
            if ("测试药物".equals(name)) latch.countDown();
        });
        
        viewModel.getSelectedColor().observeForever(color -> {
            if ("白色".equals(color)) latch.countDown();
        });
        
        viewModel.getSelectedDosageForm().observeForever(form -> {
            if ("片剂".equals(form)) latch.countDown();
        });
        
        viewModel.getTotalQuantity().observeForever(quantity -> {
            if (quantity != null && quantity == 100) latch.countDown();
        });
        
        viewModel.getDosagePerIntake().observeForever(dosage -> {
            if (dosage != null && dosage == 2) latch.countDown();
        });
        
        viewModel.getLowStockThreshold().observeForever(threshold -> {
            if (threshold != null && threshold == 10) latch.countDown();
        });

        // 在后台线程中设置表单数据，模拟可能的并发场景
        new Thread(() -> {
            viewModel.setMedicationName("测试药物");
            viewModel.setSelectedColor("白色");
            viewModel.setSelectedDosageForm("片剂");
            viewModel.setTotalQuantity(100);
            viewModel.setDosagePerIntake(2);
            viewModel.setLowStockThreshold(10);
        }).start();

        // 等待所有数据更新完成
        assertTrue("表单数据设置应该在合理时间内完成", latch.await(5, TimeUnit.SECONDS));
    }
}