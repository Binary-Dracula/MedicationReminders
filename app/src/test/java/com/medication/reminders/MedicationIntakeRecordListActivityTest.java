package com.medication.reminders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.medication.reminders.adapter.MedicationIntakeRecordAdapter;
import com.medication.reminders.database.entity.MedicationIntakeRecord;
import com.medication.reminders.view.MedicationIntakeRecordListActivity;
import com.medication.reminders.viewmodels.MedicationIntakeRecordViewModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * MedicationIntakeRecordListActivity的单元测试
 * 测试用药记录列表页面的基本功能
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class MedicationIntakeRecordListActivityTest {
    
    @Mock
    private MedicationIntakeRecordViewModel mockViewModel;
    
    private MedicationIntakeRecordListActivity activity;
    private Context context;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        
        // 设置ViewModel的模拟行为
        MutableLiveData<List<MedicationIntakeRecord>> recordsLiveData = new MutableLiveData<>();
        MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
        MutableLiveData<String> errorLiveData = new MutableLiveData<>();
        MutableLiveData<String> successLiveData = new MutableLiveData<>();
        
        when(mockViewModel.getAllIntakeRecords()).thenReturn(recordsLiveData);
        when(mockViewModel.getIsLoading()).thenReturn(loadingLiveData);
        when(mockViewModel.getErrorMessage()).thenReturn(errorLiveData);
        when(mockViewModel.getSuccessMessage()).thenReturn(successLiveData);
    }
    
    @Test
    public void testActivityCreation() {
        // 测试Activity能够正常创建
        activity = Robolectric.buildActivity(MedicationIntakeRecordListActivity.class)
                .create()
                .get();
        
        assertNotNull("Activity应该能够正常创建", activity);
        assertNotNull("Activity应该有标题", activity.getTitle());
    }
    
    @Test
    public void testEmptyStateDisplay() {
        // 测试空状态显示
        activity = Robolectric.buildActivity(MedicationIntakeRecordListActivity.class)
                .create()
                .start()
                .resume()
                .get();
        
        // 验证空状态布局存在
        assertNotNull("空状态布局应该存在", activity.findViewById(R.id.emptyStateLayout));
        assertNotNull("空状态消息应该存在", activity.findViewById(R.id.tvEmptyMessage));
    }
    
    @Test
    public void testRecyclerViewSetup() {
        // 测试RecyclerView设置
        activity = Robolectric.buildActivity(MedicationIntakeRecordListActivity.class)
                .create()
                .start()
                .resume()
                .get();
        
        RecyclerView recyclerView = activity.findViewById(R.id.recyclerViewIntakeRecords);
        assertNotNull("RecyclerView应该存在", recyclerView);
        assertNotNull("RecyclerView应该有LayoutManager", recyclerView.getLayoutManager());
        assertNotNull("RecyclerView应该有Adapter", recyclerView.getAdapter());
    }
    
    @Test
    public void testIntentHandling() {
        // 测试Intent处理
        Intent intent = new Intent(context, MedicationIntakeRecordListActivity.class);
        
        activity = Robolectric.buildActivity(MedicationIntakeRecordListActivity.class, intent)
                .create()
                .get();
        
        assertNotNull("Activity应该能够处理Intent", activity);
    }
    
    @Test
    public void testAdapterCreation() {
        // 测试适配器创建
        List<MedicationIntakeRecord> testRecords = createTestRecords();
        MedicationIntakeRecordAdapter.OnIntakeRecordClickListener mockListener = 
            mock(MedicationIntakeRecordAdapter.OnIntakeRecordClickListener.class);
        
        MedicationIntakeRecordAdapter adapter = new MedicationIntakeRecordAdapter(testRecords, mockListener);
        
        assertNotNull("适配器应该能够正常创建", adapter);
        assertEquals("适配器应该有正确的项目数量", testRecords.size(), adapter.getItemCount());
    }
    
    @Test
    public void testAdapterUpdateRecords() {
        // 测试适配器更新记录
        List<MedicationIntakeRecord> initialRecords = createTestRecords();
        List<MedicationIntakeRecord> newRecords = createTestRecords();
        newRecords.add(createTestRecord("新药物", System.currentTimeMillis(), 2));
        
        MedicationIntakeRecordAdapter.OnIntakeRecordClickListener mockListener = 
            mock(MedicationIntakeRecordAdapter.OnIntakeRecordClickListener.class);
        
        MedicationIntakeRecordAdapter adapter = new MedicationIntakeRecordAdapter(initialRecords, mockListener);
        
        int initialCount = adapter.getItemCount();
        adapter.updateIntakeRecords(newRecords);
        int newCount = adapter.getItemCount();
        
        assertEquals("适配器应该更新为新的记录数量", newRecords.size(), newCount);
        assertTrue("新记录数量应该大于初始记录数量", newCount > initialCount);
    }
    
    /**
     * 创建测试用的用药记录列表
     */
    private List<MedicationIntakeRecord> createTestRecords() {
        List<MedicationIntakeRecord> records = new ArrayList<>();
        
        records.add(createTestRecord("阿司匹林", System.currentTimeMillis() - 3600000, 1)); // 1小时前
        records.add(createTestRecord("维生素C", System.currentTimeMillis() - 7200000, 2)); // 2小时前
        records.add(createTestRecord("钙片", System.currentTimeMillis() - 10800000, 1)); // 3小时前
        
        return records;
    }
    
    /**
     * 创建测试用的用药记录
     */
    private MedicationIntakeRecord createTestRecord(String medicationName, long intakeTime, int dosageTaken) {
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        record.setId(System.currentTimeMillis()); // 使用时间戳作为ID
        record.setMedicationName(medicationName);
        record.setIntakeTime(intakeTime);
        record.setDosageTaken(dosageTaken);
        return record;
    }
}