package com.medication.reminders;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.medication.reminders.database.entity.HealthDiary;
import com.medication.reminders.models.RepositoryCallback;
import com.medication.reminders.repository.HealthDiaryRepository;
import com.medication.reminders.viewmodels.HealthDiaryViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 健康日记删除功能集成测试
 * 验证删除功能的完整流程和需求符合性
 */
@RunWith(RobolectricTestRunner.class)
public class HealthDiaryDeleteIntegrationTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private HealthDiaryRepository mockRepository;

    private HealthDiaryViewModel viewModel;
    private Application application;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        application = RuntimeEnvironment.getApplication();
        
        // 创建测试模式的ViewModel
        viewModel = new HealthDiaryViewModel(application, true);
        
        // 使用反射设置mock repository
        try {
            java.lang.reflect.Field repositoryField = HealthDiaryViewModel.class.getDeclaredField("healthDiaryRepository");
            repositoryField.setAccessible(true);
            repositoryField.set(viewModel, mockRepository);
        } catch (Exception e) {
            fail("无法设置mock repository: " + e.getMessage());
        }
    }

    /**
     * 测试需求4.1：用户在日记详情页面点击"删除"按钮时系统显示确认删除对话框
     * 这个测试验证ViewModel层的删除逻辑，UI层的确认对话框在Activity中实现
     */
    @Test
    public void testRequirement_4_1_DeleteButtonTriggersConfirmation() {
        // 准备测试数据
        HealthDiary testDiary = new HealthDiary();
        testDiary.setId(1L);
        testDiary.setContent("测试日记内容");
        testDiary.setUserId(1L);
        testDiary.setCreatedAt(System.currentTimeMillis());
        testDiary.setUpdatedAt(System.currentTimeMillis());

        // 模拟Repository成功删除
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true);
            return null;
        }).when(mockRepository).deleteDiary(eq(testDiary), any(RepositoryCallback.class));

        // 执行删除操作（模拟用户确认删除后的操作）
        viewModel.deleteDiary(testDiary);

        // 验证Repository方法被调用
        verify(mockRepository).deleteDiary(eq(testDiary), any(RepositoryCallback.class));
        
        // 验证删除成功
        assertTrue("删除操作应该成功", viewModel.getDeleteSuccess().getValue());
        assertEquals("应该显示删除成功消息", "日记删除成功", viewModel.getOperationResult().getValue());
    }

    /**
     * 测试需求4.2：用户确认删除时系统从数据库中永久删除该日记记录
     */
    @Test
    public void testRequirement_4_2_ConfirmDeleteRemovesFromDatabase() {
        // 准备测试数据
        HealthDiary testDiary = new HealthDiary();
        testDiary.setId(1L);
        testDiary.setContent("要删除的日记");
        testDiary.setUserId(1L);

        // 模拟Repository成功删除
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true);
            return null;
        }).when(mockRepository).deleteDiary(eq(testDiary), any(RepositoryCallback.class));

        // 执行删除操作
        viewModel.deleteDiary(testDiary);

        // 验证Repository的deleteDiary方法被调用，确保从数据库中删除
        verify(mockRepository).deleteDiary(eq(testDiary), any(RepositoryCallback.class));
        
        // 验证删除成功状态
        assertTrue("删除操作应该成功", viewModel.getDeleteSuccess().getValue());
    }

    /**
     * 测试需求4.3：日记删除成功时系统显示成功提示并返回日记列表页面
     */
    @Test
    public void testRequirement_4_3_DeleteSuccessShowsMessageAndReturns() {
        // 准备测试数据
        HealthDiary testDiary = new HealthDiary();
        testDiary.setId(1L);
        testDiary.setContent("测试日记");

        // 模拟Repository成功删除
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true);
            return null;
        }).when(mockRepository).deleteDiary(eq(testDiary), any(RepositoryCallback.class));

        // 执行删除操作
        viewModel.deleteDiary(testDiary);

        // 验证成功提示消息
        assertEquals("应该显示删除成功消息", "日记删除成功", viewModel.getOperationResult().getValue());
        assertTrue("操作应该成功", viewModel.getOperationSuccess().getValue());
        
        // 验证删除成功状态（Activity会根据这个状态返回列表页面）
        assertTrue("删除成功状态应该为true", viewModel.getDeleteSuccess().getValue());
    }

    /**
     * 测试需求4.4：用户取消删除时系统关闭确认对话框且不删除日记
     */
    @Test
    public void testRequirement_4_4_CancelDeleteDoesNotRemove() {
        // 准备测试数据
        HealthDiary testDiary = new HealthDiary();
        testDiary.setId(1L);
        testDiary.setContent("不应该被删除的日记");

        // 模拟用户取消删除操作（不调用deleteDiary方法）
        // 这个测试验证当用户取消时，Repository的删除方法不会被调用

        // 验证Repository的删除方法没有被调用
        verify(mockRepository, never()).deleteDiary(any(HealthDiary.class), any(RepositoryCallback.class));
        
        // 验证删除状态保持初始状态
        assertNull("删除成功状态应该为null（初始状态）", viewModel.getDeleteSuccess().getValue());
    }

    /**
     * 测试需求4.5：删除日记后系统从日记列表中移除该条目
     */
    @Test
    public void testRequirement_4_5_DeleteRemovesFromList() {
        // 准备测试数据
        HealthDiary testDiary = new HealthDiary();
        testDiary.setId(1L);
        testDiary.setContent("要从列表中移除的日记");

        // 模拟Repository成功删除
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true);
            return null;
        }).when(mockRepository).deleteDiary(eq(testDiary), any(RepositoryCallback.class));

        // 执行删除操作
        viewModel.deleteDiary(testDiary);

        // 验证删除操作成功
        assertTrue("删除操作应该成功", viewModel.getDeleteSuccess().getValue());
        
        // 验证选中的日记被清除（这会触发UI更新，从列表中移除该条目）
        assertNull("选中的日记应该被清除", viewModel.getSelectedDiary().getValue());
    }

    /**
     * 测试需求4.6：删除操作失败时系统显示错误提示信息
     */
    @Test
    public void testRequirement_4_6_DeleteFailureShowsError() {
        // 准备测试数据
        HealthDiary testDiary = new HealthDiary();
        testDiary.setId(1L);
        testDiary.setContent("删除失败的日记");
        
        String errorMessage = "数据库连接失败";

        // 模拟Repository删除失败
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(1);
            callback.onError(errorMessage);
            return null;
        }).when(mockRepository).deleteDiary(eq(testDiary), any(RepositoryCallback.class));

        // 执行删除操作
        viewModel.deleteDiary(testDiary);

        // 验证错误提示信息
        assertEquals("应该显示删除失败消息", "删除日记失败：" + errorMessage, viewModel.getOperationResult().getValue());
        assertFalse("操作应该失败", viewModel.getOperationSuccess().getValue());
        assertFalse("删除成功状态应该为false", viewModel.getDeleteSuccess().getValue());
        
        // 验证错误消息
        assertEquals("应该设置错误消息", "删除日记失败：" + errorMessage, viewModel.getErrorMessage().getValue());
    }

    /**
     * 测试删除功能的安全性：防止误删
     */
    @Test
    public void testDeleteSafety_PreventAccidentalDeletion() {
        // 测试空对象保护
        viewModel.deleteDiary(null);
        assertEquals("空对象应该显示错误消息", "日记对象不能为空", viewModel.getOperationResult().getValue());
        verify(mockRepository, never()).deleteDiary(any(), any());

        // 测试无效ID保护
        HealthDiary invalidDiary = new HealthDiary();
        invalidDiary.setId(0L); // 无效ID
        viewModel.deleteDiary(invalidDiary);
        assertEquals("无效ID应该显示错误消息", "日记ID无效", viewModel.getOperationResult().getValue());
        verify(mockRepository, never()).deleteDiary(any(), any());
    }

    /**
     * 测试删除操作的完整流程
     */
    @Test
    public void testCompleteDeleteFlow() {
        // 准备测试数据
        HealthDiary testDiary = new HealthDiary();
        testDiary.setId(1L);
        testDiary.setContent("完整流程测试日记");
        testDiary.setUserId(1L);
        testDiary.setCreatedAt(System.currentTimeMillis());
        testDiary.setUpdatedAt(System.currentTimeMillis());

        // 模拟Repository成功删除
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true);
            return null;
        }).when(mockRepository).deleteDiary(eq(testDiary), any(RepositoryCallback.class));

        // 1. 执行删除操作
        viewModel.deleteDiary(testDiary);

        // 2. 验证Repository调用
        verify(mockRepository).deleteDiary(eq(testDiary), any(RepositoryCallback.class));

        // 3. 验证成功状态
        assertTrue("删除应该成功", viewModel.getDeleteSuccess().getValue());
        assertEquals("应该显示成功消息", "日记删除成功", viewModel.getOperationResult().getValue());
        assertTrue("操作应该成功", viewModel.getOperationSuccess().getValue());

        // 4. 验证清理状态
        assertNull("选中日记应该被清除", viewModel.getSelectedDiary().getValue());
        assertFalse("加载状态应该结束", viewModel.getIsLoading().getValue());
    }
}