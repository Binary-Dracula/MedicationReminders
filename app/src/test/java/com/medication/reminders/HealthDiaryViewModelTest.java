package com.medication.reminders;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.medication.reminders.database.entity.HealthDiary;
import com.medication.reminders.models.RepositoryCallback;
import com.medication.reminders.repository.HealthDiaryRepository;
import com.medication.reminders.viewmodels.HealthDiaryViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HealthDiaryViewModel单元测试类
 * 测试健康日记ViewModel的业务逻辑、LiveData数据绑定和错误处理
 */
@RunWith(RobolectricTestRunner.class)
public class HealthDiaryViewModelTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private HealthDiaryRepository mockRepository;
    
    @Mock
    private Observer<String> stringObserver;
    
    @Mock
    private Observer<Boolean> booleanObserver;
    
    @Mock
    private Observer<Boolean> addSuccessObserver;
    
    @Mock
    private Observer<Boolean> updateSuccessObserver;
    
    @Mock
    private Observer<Boolean> deleteSuccessObserver;
    
    @Mock
    private Observer<HealthDiary> diaryObserver;
    
    @Mock
    private Observer<List<HealthDiary>> diaryListObserver;
    
    @Mock
    private Observer<Integer> integerObserver;
    
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
    
    // ========== 构造函数和初始化测试 ==========
    
    @Test
    public void testViewModelInitialization() {
        // 验证ViewModel正确初始化
        assertNotNull("ViewModel不应为null", viewModel);
        
        // 验证LiveData初始状态
        assertNotNull("操作结果LiveData不应为null", viewModel.getOperationResult());
        assertNotNull("加载状态LiveData不应为null", viewModel.getIsLoading());
        assertNotNull("错误消息LiveData不应为null", viewModel.getErrorMessage());
        assertNotNull("成功消息LiveData不应为null", viewModel.getSuccessMessage());
        assertNotNull("验证错误LiveData不应为null", viewModel.getValidationError());
        
        // 验证初始状态值（应该为null，因为没有设置初始值）
        assertNull("初始操作成功状态应为null", viewModel.getOperationSuccess().getValue());
        assertNull("初始加载状态应为null", viewModel.getIsLoading().getValue());
        assertNull("初始添加成功状态应为null", viewModel.getAddSuccess().getValue());
        assertNull("初始更新成功状态应为null", viewModel.getUpdateSuccess().getValue());
        assertNull("初始删除成功状态应为null", viewModel.getDeleteSuccess().getValue());
    }
    
    @Test
    public void testGetViewModelStatus() {
        String status = viewModel.getViewModelStatus();
        assertNotNull("状态信息不应为null", status);
        assertTrue("状态信息应包含Repository状态", status.contains("Repository"));
        assertTrue("状态信息应包含线程池状态", status.contains("线程池"));
        assertTrue("状态信息应包含测试模式状态", status.contains("测试模式"));
    }
    
    // ========== 输入验证测试 ==========
    
    @Test
    public void testValidateDiaryContent_ValidContent() {
        String validContent = "今天感觉很好，血压正常。";
        String result = viewModel.validateDiaryContent(validContent);
        assertNull("有效内容验证应返回null", result);
    }
    
    @Test
    public void testValidateDiaryContent_EmptyContent() {
        String emptyContent = "";
        String result = viewModel.validateDiaryContent(emptyContent);
        assertEquals("空内容验证应返回相应错误", "日记内容不能为空", result);
    }
    
    @Test
    public void testValidateDiaryContent_NullContent() {
        String result = viewModel.validateDiaryContent(null);
        assertEquals("null内容验证应返回相应错误", "日记内容不能为空", result);
    }
    
    @Test
    public void testValidateDiaryContent_TooLongContent() {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 5001; i++) {
            longContent.append("a");
        }
        String result = viewModel.validateDiaryContent(longContent.toString());
        assertEquals("过长内容验证应返回相应错误", "日记内容不能超过5000字符", result);
    }
    
    @Test
    public void testValidateAndSetError_ValidContent() {
        String validContent = "今天感觉很好。";
        
        // 观察验证错误LiveData
        viewModel.getValidationError().observeForever(stringObserver);
        
        boolean result = viewModel.validateAndSetError(validContent);
        
        assertTrue("有效内容验证应返回true", result);
        verify(stringObserver).onChanged(null); // 应该清除验证错误
    }
    
    @Test
    public void testValidateAndSetError_InvalidContent() {
        String invalidContent = "";
        
        // 观察验证错误LiveData
        viewModel.getValidationError().observeForever(stringObserver);
        
        boolean result = viewModel.validateAndSetError(invalidContent);
        
        assertFalse("无效内容验证应返回false", result);
        verify(stringObserver).onChanged("日记内容不能为空");
    }
    
    // ========== 添加日记测试 ==========
    
    @Test
    public void testAddDiary_Success() {
        String content = "今天感觉很好，血压正常。";
        
        // 观察相关LiveData
        viewModel.getAddSuccess().observeForever(addSuccessObserver);
        viewModel.getSuccessMessage().observeForever(stringObserver);
        
        // 模拟Repository成功响应
        doAnswer(invocation -> {
            RepositoryCallback<Long> callback = invocation.getArgument(1);
            callback.onSuccess(1L);
            return null;
        }).when(mockRepository).addDiary(any(HealthDiary.class), any(RepositoryCallback.class));
        
        // 执行添加操作
        viewModel.addDiary(content);
        
        // 验证结果
        verify(addSuccessObserver).onChanged(true);
        verify(stringObserver).onChanged("日记添加成功");
        verify(mockRepository).addDiary(any(HealthDiary.class), any(RepositoryCallback.class));
    }
    
    @Test
    public void testAddDiary_Failure() {
        String content = "今天感觉很好，血压正常。";
        String errorMessage = "数据库连接失败";
        
        // 观察相关LiveData
        viewModel.getAddSuccess().observeForever(addSuccessObserver);
        viewModel.getErrorMessage().observeForever(stringObserver);
        
        // 模拟Repository失败响应
        doAnswer(invocation -> {
            RepositoryCallback<Long> callback = invocation.getArgument(1);
            callback.onError(errorMessage);
            return null;
        }).when(mockRepository).addDiary(any(HealthDiary.class), any(RepositoryCallback.class));
        
        // 执行添加操作
        viewModel.addDiary(content);
        
        // 验证结果
        verify(addSuccessObserver).onChanged(false);
        verify(stringObserver).onChanged("添加日记失败：" + errorMessage);
    }
    
    @Test
    public void testAddDiary_InvalidContent() {
        String invalidContent = "";
        
        // 观察相关LiveData
        viewModel.getValidationError().observeForever(stringObserver);
        viewModel.getAddSuccess().observeForever(addSuccessObserver);
        
        // 执行添加操作
        viewModel.addDiary(invalidContent);
        
        // 验证结果
        verify(stringObserver).onChanged("日记内容不能为空");
        verify(addSuccessObserver, never()).onChanged(true);
        verify(mockRepository, never()).addDiary(any(HealthDiary.class), any(RepositoryCallback.class));
    }
    
    // ========== 更新日记测试 ==========
    
    @Test
    public void testUpdateDiary_Success() {
        long diaryId = 1L;
        String newContent = "更新后的日记内容";
        HealthDiary existingDiary = new HealthDiary(1L, "原始内容");
        existingDiary.setId(diaryId);
        
        // 观察相关LiveData
        viewModel.getUpdateSuccess().observeForever(updateSuccessObserver);
        viewModel.getSuccessMessage().observeForever(stringObserver);
        
        // 模拟Repository获取现有日记成功
        doAnswer(invocation -> {
            RepositoryCallback<HealthDiary> callback = invocation.getArgument(1);
            callback.onSuccess(existingDiary);
            return null;
        }).when(mockRepository).getDiaryById(eq(diaryId), any(RepositoryCallback.class));
        
        // 模拟Repository更新成功
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true);
            return null;
        }).when(mockRepository).updateDiary(any(HealthDiary.class), any(RepositoryCallback.class));
        
        // 执行更新操作
        viewModel.updateDiary(diaryId, newContent);
        
        // 验证结果
        verify(updateSuccessObserver).onChanged(true);
        verify(stringObserver).onChanged("日记更新成功");
        verify(mockRepository).getDiaryById(eq(diaryId), any(RepositoryCallback.class));
        verify(mockRepository).updateDiary(any(HealthDiary.class), any(RepositoryCallback.class));
    }
    
    @Test
    public void testUpdateDiary_GetDiaryFailure() {
        long diaryId = 1L;
        String newContent = "更新后的日记内容";
        String errorMessage = "日记不存在";
        
        // 观察相关LiveData
        viewModel.getUpdateSuccess().observeForever(updateSuccessObserver);
        viewModel.getErrorMessage().observeForever(stringObserver);
        
        // 模拟Repository获取日记失败
        doAnswer(invocation -> {
            RepositoryCallback<HealthDiary> callback = invocation.getArgument(1);
            callback.onError(errorMessage);
            return null;
        }).when(mockRepository).getDiaryById(eq(diaryId), any(RepositoryCallback.class));
        
        // 执行更新操作
        viewModel.updateDiary(diaryId, newContent);
        
        // 验证结果
        verify(updateSuccessObserver).onChanged(false);
        verify(stringObserver).onChanged("获取日记失败：" + errorMessage);
        verify(mockRepository, never()).updateDiary(any(HealthDiary.class), any(RepositoryCallback.class));
    }
    
    @Test
    public void testUpdateDiary_InvalidId() {
        long invalidId = 0L;
        String content = "有效内容";
        
        // 观察相关LiveData
        viewModel.getErrorMessage().observeForever(stringObserver);
        
        // 执行更新操作
        viewModel.updateDiary(invalidId, content);
        
        // 验证结果
        verify(stringObserver).onChanged("日记ID无效");
        verify(mockRepository, never()).getDiaryById(anyLong(), any(RepositoryCallback.class));
    }
    
    @Test
    public void testUpdateDiary_InvalidContent() {
        long diaryId = 1L;
        String invalidContent = "";
        
        // 观察相关LiveData
        viewModel.getValidationError().observeForever(stringObserver);
        
        // 执行更新操作
        viewModel.updateDiary(diaryId, invalidContent);
        
        // 验证结果
        verify(stringObserver).onChanged("日记内容不能为空");
        verify(mockRepository, never()).getDiaryById(anyLong(), any(RepositoryCallback.class));
    }
    
    // ========== 删除日记测试 ==========
    
    @Test
    public void testDeleteDiary_Success() {
        HealthDiary diary = new HealthDiary(1L, "要删除的日记");
        diary.setId(1L);
        
        // 观察相关LiveData
        viewModel.getDeleteSuccess().observeForever(deleteSuccessObserver);
        viewModel.getSuccessMessage().observeForever(stringObserver);
        viewModel.getSelectedDiary().observeForever(diaryObserver);
        
        // 模拟Repository删除成功
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true);
            return null;
        }).when(mockRepository).deleteDiary(eq(diary), any(RepositoryCallback.class));
        
        // 执行删除操作
        viewModel.deleteDiary(diary);
        
        // 验证结果
        verify(deleteSuccessObserver).onChanged(true);
        verify(stringObserver).onChanged("日记删除成功");
        verify(diaryObserver).onChanged(null); // 应该清除选中的日记
        verify(mockRepository).deleteDiary(eq(diary), any(RepositoryCallback.class));
    }
    
    @Test
    public void testDeleteDiary_Failure() {
        HealthDiary diary = new HealthDiary(1L, "要删除的日记");
        diary.setId(1L);
        String errorMessage = "删除失败";
        
        // 观察相关LiveData
        viewModel.getDeleteSuccess().observeForever(deleteSuccessObserver);
        viewModel.getErrorMessage().observeForever(stringObserver);
        
        // 模拟Repository删除失败
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(1);
            callback.onError(errorMessage);
            return null;
        }).when(mockRepository).deleteDiary(eq(diary), any(RepositoryCallback.class));
        
        // 执行删除操作
        viewModel.deleteDiary(diary);
        
        // 验证结果
        verify(deleteSuccessObserver).onChanged(false);
        verify(stringObserver).onChanged("删除日记失败：" + errorMessage);
    }
    
    @Test
    public void testDeleteDiary_NullDiary() {
        // 观察相关LiveData
        viewModel.getErrorMessage().observeForever(stringObserver);
        
        // 执行删除操作
        viewModel.deleteDiary(null);
        
        // 验证结果
        verify(stringObserver).onChanged("日记对象不能为空");
        verify(mockRepository, never()).deleteDiary(any(HealthDiary.class), any(RepositoryCallback.class));
    }
    
    @Test
    public void testDeleteDiaryById_Success() {
        long diaryId = 1L;
        
        // 观察相关LiveData
        viewModel.getDeleteSuccess().observeForever(deleteSuccessObserver);
        viewModel.getSuccessMessage().observeForever(stringObserver);
        
        // 模拟Repository删除成功
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true);
            return null;
        }).when(mockRepository).deleteById(eq(diaryId), any(RepositoryCallback.class));
        
        // 执行删除操作
        viewModel.deleteDiaryById(diaryId);
        
        // 验证结果
        verify(deleteSuccessObserver).onChanged(true);
        verify(stringObserver).onChanged("日记删除成功");
        verify(mockRepository).deleteById(eq(diaryId), any(RepositoryCallback.class));
    }
    
    @Test
    public void testDeleteDiaryById_InvalidId() {
        long invalidId = 0L;
        
        // 观察相关LiveData
        viewModel.getErrorMessage().observeForever(stringObserver);
        
        // 执行删除操作
        viewModel.deleteDiaryById(invalidId);
        
        // 验证结果
        verify(stringObserver).onChanged("日记ID无效");
        verify(mockRepository, never()).deleteById(anyLong(), any(RepositoryCallback.class));
    }
    
    // ========== 查询日记测试 ==========
    
    @Test
    public void testGetDiaryById_Success() {
        long diaryId = 1L;
        HealthDiary diary = new HealthDiary(1L, "测试日记");
        diary.setId(diaryId);
        
        // 观察相关LiveData
        viewModel.getSelectedDiary().observeForever(diaryObserver);
        viewModel.getSuccessMessage().observeForever(stringObserver);
        
        // 模拟Repository查询成功
        doAnswer(invocation -> {
            RepositoryCallback<HealthDiary> callback = invocation.getArgument(1);
            callback.onSuccess(diary);
            return null;
        }).when(mockRepository).getDiaryById(eq(diaryId), any(RepositoryCallback.class));
        
        // 执行查询操作
        viewModel.getDiaryById(diaryId);
        
        // 验证结果
        verify(diaryObserver).onChanged(diary);
        verify(stringObserver).onChanged("日记加载成功");
        verify(mockRepository).getDiaryById(eq(diaryId), any(RepositoryCallback.class));
    }
    
    @Test
    public void testGetDiaryById_Failure() {
        long diaryId = 1L;
        String errorMessage = "日记不存在";
        
        // 观察相关LiveData
        viewModel.getSelectedDiary().observeForever(diaryObserver);
        viewModel.getErrorMessage().observeForever(stringObserver);
        
        // 模拟Repository查询失败
        doAnswer(invocation -> {
            RepositoryCallback<HealthDiary> callback = invocation.getArgument(1);
            callback.onError(errorMessage);
            return null;
        }).when(mockRepository).getDiaryById(eq(diaryId), any(RepositoryCallback.class));
        
        // 执行查询操作
        viewModel.getDiaryById(diaryId);
        
        // 验证结果
        verify(diaryObserver).onChanged(null);
        verify(stringObserver).onChanged("获取日记失败：" + errorMessage);
    }
    
    @Test
    public void testGetDiaryById_InvalidId() {
        long invalidId = 0L;
        
        // 观察相关LiveData
        viewModel.getErrorMessage().observeForever(stringObserver);
        
        // 执行查询操作
        viewModel.getDiaryById(invalidId);
        
        // 验证结果
        verify(stringObserver).onChanged("日记ID无效");
        verify(mockRepository, never()).getDiaryById(anyLong(), any(RepositoryCallback.class));
    }
    
    // ========== 搜索日记测试 ==========
    
    @Test
    public void testSearchDiaries_Success() {
        String searchQuery = "血压";
        List<HealthDiary> searchResults = new ArrayList<>();
        searchResults.add(new HealthDiary(1L, "今天血压正常"));
        searchResults.add(new HealthDiary(1L, "血压有点高"));
        
        RepositoryCallback<List<HealthDiary>> testCallback = new RepositoryCallback<List<HealthDiary>>() {
            @Override
            public void onSuccess(List<HealthDiary> result) {
                assertEquals("搜索结果数量应正确", 2, result.size());
            }
            
            @Override
            public void onError(String error) {
                fail("搜索不应失败");
            }
        };
        
        // 观察相关LiveData
        viewModel.getSuccessMessage().observeForever(stringObserver);
        
        // 模拟Repository搜索成功
        doAnswer(invocation -> {
            RepositoryCallback<List<HealthDiary>> callback = invocation.getArgument(1);
            callback.onSuccess(searchResults);
            return null;
        }).when(mockRepository).searchDiaries(eq(searchQuery), any(RepositoryCallback.class));
        
        // 执行搜索操作
        viewModel.searchDiaries(searchQuery, testCallback);
        
        // 验证结果
        verify(stringObserver).onChanged("搜索完成，找到 2 条日记");
        verify(mockRepository).searchDiaries(eq(searchQuery), any(RepositoryCallback.class));
    }
    
    @Test
    public void testSearchDiaries_EmptyQuery() {
        String emptyQuery = "";
        
        RepositoryCallback<List<HealthDiary>> testCallback = new RepositoryCallback<List<HealthDiary>>() {
            @Override
            public void onSuccess(List<HealthDiary> result) {
                fail("空查询不应成功");
            }
            
            @Override
            public void onError(String error) {
                assertEquals("错误消息应正确", "搜索关键词不能为空", error);
            }
        };
        
        // 执行搜索操作
        viewModel.searchDiaries(emptyQuery, testCallback);
        
        // 验证不会调用Repository
        verify(mockRepository, never()).searchDiaries(anyString(), any(RepositoryCallback.class));
    }
    
    // ========== 状态管理测试 ==========
    
    @Test
    public void testClearErrorMessage() {
        // 观察错误消息LiveData
        viewModel.getErrorMessage().observeForever(stringObserver);
        
        // 清除错误消息
        viewModel.clearErrorMessage();
        
        // 验证结果
        verify(stringObserver).onChanged(null);
    }
    
    @Test
    public void testClearSuccessMessage() {
        // 观察成功消息LiveData
        viewModel.getSuccessMessage().observeForever(stringObserver);
        
        // 清除成功消息
        viewModel.clearSuccessMessage();
        
        // 验证结果
        verify(stringObserver).onChanged(null);
    }
    
    @Test
    public void testClearValidationError() {
        // 观察验证错误LiveData
        viewModel.getValidationError().observeForever(stringObserver);
        
        // 清除验证错误
        viewModel.clearValidationError();
        
        // 验证结果
        verify(stringObserver).onChanged(null);
    }
    
    @Test
    public void testResetOperationStates() {
        // 观察操作状态LiveData
        viewModel.getAddSuccess().observeForever(addSuccessObserver);
        viewModel.getUpdateSuccess().observeForever(updateSuccessObserver);
        viewModel.getDeleteSuccess().observeForever(deleteSuccessObserver);
        
        // 重置操作状态
        viewModel.resetOperationStates();
        
        // 验证结果
        verify(addSuccessObserver).onChanged(false);
        verify(updateSuccessObserver).onChanged(false);
        verify(deleteSuccessObserver).onChanged(false);
    }
    
    @Test
    public void testClearSelectedDiary() {
        // 观察选中日记LiveData
        viewModel.getSelectedDiary().observeForever(diaryObserver);
        
        // 清除选中日记
        viewModel.clearSelectedDiary();
        
        // 验证结果
        verify(diaryObserver).onChanged(null);
    }
    
    @Test
    public void testClearAllErrors() {
        // 观察所有错误相关LiveData
        viewModel.getErrorMessage().observeForever(stringObserver);
        viewModel.getValidationError().observeForever(stringObserver);
        
        // 清除所有错误
        viewModel.clearAllErrors();
        
        // 验证结果
        verify(stringObserver, times(2)).onChanged(null);
    }
    
    // ========== 日期范围查询测试 ==========
    
    @Test
    public void testGetDiariesByDateRange_Success() {
        long startTime = System.currentTimeMillis() - 86400000; // 昨天
        long endTime = System.currentTimeMillis(); // 现在
        List<HealthDiary> dateRangeResults = new ArrayList<>();
        dateRangeResults.add(new HealthDiary(1L, "昨天的日记"));
        
        RepositoryCallback<List<HealthDiary>> testCallback = new RepositoryCallback<List<HealthDiary>>() {
            @Override
            public void onSuccess(List<HealthDiary> result) {
                assertEquals("日期范围查询结果数量应正确", 1, result.size());
            }
            
            @Override
            public void onError(String error) {
                fail("日期范围查询不应失败");
            }
        };
        
        // 观察相关LiveData
        viewModel.getSuccessMessage().observeForever(stringObserver);
        
        // 模拟Repository查询成功
        doAnswer(invocation -> {
            RepositoryCallback<List<HealthDiary>> callback = invocation.getArgument(2);
            callback.onSuccess(dateRangeResults);
            return null;
        }).when(mockRepository).getDiariesByDateRange(eq(startTime), eq(endTime), any(RepositoryCallback.class));
        
        // 执行查询操作
        viewModel.getDiariesByDateRange(startTime, endTime, testCallback);
        
        // 验证结果
        verify(stringObserver).onChanged("查询完成，找到 1 条日记");
        verify(mockRepository).getDiariesByDateRange(eq(startTime), eq(endTime), any(RepositoryCallback.class));
    }
    
    @Test
    public void testGetDiariesByDateRange_InvalidTimeRange() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime - 86400000; // 结束时间早于开始时间
        
        RepositoryCallback<List<HealthDiary>> testCallback = new RepositoryCallback<List<HealthDiary>>() {
            @Override
            public void onSuccess(List<HealthDiary> result) {
                fail("无效时间范围不应成功");
            }
            
            @Override
            public void onError(String error) {
                assertEquals("错误消息应正确", "开始时间不能晚于结束时间", error);
            }
        };
        
        // 执行查询操作
        viewModel.getDiariesByDateRange(startTime, endTime, testCallback);
        
        // 验证不会调用Repository
        verify(mockRepository, never()).getDiariesByDateRange(anyLong(), anyLong(), any(RepositoryCallback.class));
    }
}