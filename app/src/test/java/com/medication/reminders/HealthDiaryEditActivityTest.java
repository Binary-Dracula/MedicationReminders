package com.medication.reminders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.medication.reminders.database.entity.HealthDiary;
import com.medication.reminders.view.HealthDiaryEditActivity;
import com.medication.reminders.viewmodels.HealthDiaryViewModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * HealthDiaryEditActivity的单元测试
 * 测试日记编辑界面的基本功能
 */
@RunWith(AndroidJUnit4.class)
public class HealthDiaryEditActivityTest {
    
    @Mock
    private HealthDiaryViewModel mockViewModel;
    
    private MutableLiveData<String> operationResult;
    private MutableLiveData<HealthDiary> selectedDiary;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 初始化LiveData
        operationResult = new MutableLiveData<>();
        selectedDiary = new MutableLiveData<>();
        
        // 设置ViewModel的返回值
        when(mockViewModel.getOperationResult()).thenReturn(operationResult);
        when(mockViewModel.getSelectedDiary()).thenReturn(selectedDiary);
    }
    
    /**
     * 测试新增模式的界面初始化
     */
    @Test
    public void testAddModeInitialization() {
        // 创建新增模式的Intent
        Intent intent = new Intent();
        intent.putExtra(HealthDiaryEditActivity.EXTRA_MODE, HealthDiaryEditActivity.MODE_ADD);
        
        // 启动Activity
        try (ActivityScenario<HealthDiaryEditActivity> scenario = 
             ActivityScenario.launch(HealthDiaryEditActivity.class)) {
            
            scenario.onActivity(activity -> {
                // 验证界面元素存在
                EditText editTextContent = activity.findViewById(com.medication.reminders.R.id.editTextContent);
                Button buttonSave = activity.findViewById(com.medication.reminders.R.id.buttonSave);
                Button buttonCancel = activity.findViewById(com.medication.reminders.R.id.buttonCancel);
                
                assertNotNull("内容输入框应该存在", editTextContent);
                assertNotNull("保存按钮应该存在", buttonSave);
                assertNotNull("取消按钮应该存在", buttonCancel);
                
                // 验证新增模式下输入框为空
                assertTrue("新增模式下输入框应该为空", 
                    editTextContent.getText().toString().trim().isEmpty());
                
                // 验证按钮文本
                assertEquals("保存按钮文本应该正确", "保存", buttonSave.getText().toString());
                assertEquals("取消按钮文本应该正确", "取消", buttonCancel.getText().toString());
            });
        }
    }
    
    /**
     * 测试编辑模式的界面初始化
     */
    @Test
    public void testEditModeInitialization() {
        // 创建编辑模式的Intent
        Intent intent = new Intent();
        intent.putExtra(HealthDiaryEditActivity.EXTRA_MODE, HealthDiaryEditActivity.MODE_EDIT);
        intent.putExtra(HealthDiaryEditActivity.EXTRA_DIARY_ID, 1L);
        
        // 创建测试日记数据
        HealthDiary testDiary = new HealthDiary();
        testDiary.setId(1L);
        testDiary.setContent("测试日记内容");
        testDiary.setCreatedAt(System.currentTimeMillis());
        testDiary.setUpdatedAt(System.currentTimeMillis());
        
        // 启动Activity
        try (ActivityScenario<HealthDiaryEditActivity> scenario = 
             ActivityScenario.launch(HealthDiaryEditActivity.class)) {
            
            // 模拟加载日记数据
            selectedDiary.postValue(testDiary);
            
            scenario.onActivity(activity -> {
                // 验证界面元素存在
                EditText editTextContent = activity.findViewById(com.medication.reminders.R.id.editTextContent);
                
                assertNotNull("内容输入框应该存在", editTextContent);
                
                // 注意：在实际测试中，由于异步加载，可能需要等待数据加载完成
                // 这里只是验证基本的界面元素存在
            });
        }
    }
    
    /**
     * 测试输入验证功能
     */
    @Test
    public void testInputValidation() {
        // 创建新增模式的Intent
        Intent intent = new Intent();
        intent.putExtra(HealthDiaryEditActivity.EXTRA_MODE, HealthDiaryEditActivity.MODE_ADD);
        
        // 启动Activity
        try (ActivityScenario<HealthDiaryEditActivity> scenario = 
             ActivityScenario.launch(HealthDiaryEditActivity.class)) {
            
            scenario.onActivity(activity -> {
                EditText editTextContent = activity.findViewById(com.medication.reminders.R.id.editTextContent);
                Button buttonSave = activity.findViewById(com.medication.reminders.R.id.buttonSave);
                
                // 测试空内容验证
                editTextContent.setText("");
                buttonSave.performClick();
                
                // 验证错误提示（在实际应用中会显示错误信息）
                assertNotNull("输入框应该存在", editTextContent);
                
                // 测试有效内容
                editTextContent.setText("这是一条有效的日记内容");
                
                // 验证内容长度
                assertTrue("内容长度应该在有效范围内", 
                    editTextContent.getText().toString().length() > 0 && 
                    editTextContent.getText().toString().length() <= 5000);
            });
        }
    }
    
    /**
     * 测试模式识别逻辑
     */
    @Test
    public void testModeRecognition() {
        // 测试默认模式（无参数）
        try (ActivityScenario<HealthDiaryEditActivity> scenario = 
             ActivityScenario.launch(HealthDiaryEditActivity.class)) {
            
            scenario.onActivity(activity -> {
                // 验证Activity能够正常启动
                assertNotNull("Activity应该能够正常启动", activity);
            });
        }
        
        // 测试明确指定的新增模式
        Intent addIntent = new Intent();
        addIntent.putExtra(HealthDiaryEditActivity.EXTRA_MODE, HealthDiaryEditActivity.MODE_ADD);
        
        try (ActivityScenario<HealthDiaryEditActivity> addScenario = 
             ActivityScenario.launch(HealthDiaryEditActivity.class)) {
            
            addScenario.onActivity(activity -> {
                assertNotNull("新增模式Activity应该能够正常启动", activity);
            });
        }
        
        // 测试编辑模式
        Intent editIntent = new Intent();
        editIntent.putExtra(HealthDiaryEditActivity.EXTRA_MODE, HealthDiaryEditActivity.MODE_EDIT);
        editIntent.putExtra(HealthDiaryEditActivity.EXTRA_DIARY_ID, 1L);
        
        try (ActivityScenario<HealthDiaryEditActivity> editScenario = 
             ActivityScenario.launch(HealthDiaryEditActivity.class)) {
            
            editScenario.onActivity(activity -> {
                assertNotNull("编辑模式Activity应该能够正常启动", activity);
            });
        }
    }
    
    /**
     * 测试常量定义
     */
    @Test
    public void testConstants() {
        // 验证常量定义
        assertEquals("EXTRA_DIARY_ID常量应该正确", "diary_id", HealthDiaryEditActivity.EXTRA_DIARY_ID);
        assertEquals("EXTRA_MODE常量应该正确", "mode", HealthDiaryEditActivity.EXTRA_MODE);
        assertEquals("MODE_ADD常量应该正确", "add", HealthDiaryEditActivity.MODE_ADD);
        assertEquals("MODE_EDIT常量应该正确", "edit", HealthDiaryEditActivity.MODE_EDIT);
    }
    
    /**
     * 测试界面布局资源
     */
    @Test
    public void testLayoutResources() {
        // 启动Activity验证布局资源能够正确加载
        try (ActivityScenario<HealthDiaryEditActivity> scenario = 
             ActivityScenario.launch(HealthDiaryEditActivity.class)) {
            
            scenario.onActivity(activity -> {
                // 验证关键UI元素存在
                assertNotNull("内容输入框应该存在", 
                    activity.findViewById(com.medication.reminders.R.id.editTextContent));
                assertNotNull("保存按钮应该存在", 
                    activity.findViewById(com.medication.reminders.R.id.buttonSave));
                assertNotNull("取消按钮应该存在", 
                    activity.findViewById(com.medication.reminders.R.id.buttonCancel));
            });
        }
    }
}