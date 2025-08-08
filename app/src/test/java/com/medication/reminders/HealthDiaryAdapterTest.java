package com.medication.reminders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.medication.reminders.adapter.HealthDiaryAdapter;
import com.medication.reminders.database.entity.HealthDiary;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HealthDiaryAdapter单元测试
 * 测试健康日记适配器的基本功能
 */
@RunWith(RobolectricTestRunner.class)
public class HealthDiaryAdapterTest {
    
    @Mock
    private HealthDiaryAdapter.OnDiaryClickListener mockClickListener;
    
    private HealthDiaryAdapter adapter;
    private List<HealthDiary> testDiaries;
    private Context context;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        
        // 创建测试数据
        testDiaries = createTestDiaries();
        
        // 创建适配器
        adapter = new HealthDiaryAdapter(testDiaries, mockClickListener);
    }
    
    /**
     * 创建测试用的健康日记数据
     */
    private List<HealthDiary> createTestDiaries() {
        List<HealthDiary> diaries = new ArrayList<>();
        
        // 创建第一条日记
        HealthDiary diary1 = new HealthDiary();
        diary1.setId(1L);
        diary1.setUserId(1L);
        diary1.setContent("今天感觉很好，血压正常。");
        diary1.setCreatedAt(System.currentTimeMillis() - 86400000); // 1天前
        diary1.setUpdatedAt(System.currentTimeMillis() - 86400000);
        diaries.add(diary1);
        
        // 创建第二条日记
        HealthDiary diary2 = new HealthDiary();
        diary2.setId(2L);
        diary2.setUserId(1L);
        diary2.setContent("头有点疼，可能是天气原因。");
        diary2.setCreatedAt(System.currentTimeMillis() - 43200000); // 12小时前
        diary2.setUpdatedAt(System.currentTimeMillis()); // 刚刚更新
        diaries.add(diary2);
        
        return diaries;
    }
    
    /**
     * 测试适配器的基本功能
     */
    @Test
    public void testAdapterBasicFunctionality() {
        // 测试项目数量
        assertEquals("适配器项目数量应该正确", 2, adapter.getItemCount());
        
        // 测试空列表
        HealthDiaryAdapter emptyAdapter = new HealthDiaryAdapter(null, mockClickListener);
        assertEquals("空列表适配器项目数量应该为0", 0, emptyAdapter.getItemCount());
        
        // 测试空ArrayList
        HealthDiaryAdapter emptyListAdapter = new HealthDiaryAdapter(new ArrayList<>(), mockClickListener);
        assertEquals("空ArrayList适配器项目数量应该为0", 0, emptyListAdapter.getItemCount());
    }
    
    /**
     * 测试更新日记列表功能
     */
    @Test
    public void testUpdateDiaries() {
        // 初始状态
        assertEquals("初始项目数量", 2, adapter.getItemCount());
        
        // 创建新的日记列表
        List<HealthDiary> newDiaries = new ArrayList<>();
        HealthDiary newDiary = new HealthDiary();
        newDiary.setId(3L);
        newDiary.setUserId(1L);
        newDiary.setContent("新的日记内容");
        newDiary.setCreatedAt(System.currentTimeMillis());
        newDiary.setUpdatedAt(System.currentTimeMillis());
        newDiaries.add(newDiary);
        
        // 更新数据
        adapter.updateDiaries(newDiaries);
        
        // 验证更新后的数量
        assertEquals("更新后项目数量", 1, adapter.getItemCount());
        
        // 测试更新为null
        adapter.updateDiaries(null);
        assertEquals("更新为null后项目数量", 0, adapter.getItemCount());
    }
    
    /**
     * 测试构造函数参数验证
     */
    @Test
    public void testConstructorParameters() {
        // 测试null点击监听器
        HealthDiaryAdapter adapterWithNullListener = new HealthDiaryAdapter(testDiaries, null);
        assertNotNull("适配器应该能够处理null点击监听器", adapterWithNullListener);
        assertEquals("项目数量应该正确", 2, adapterWithNullListener.getItemCount());
        
        // 测试null日记列表
        HealthDiaryAdapter adapterWithNullList = new HealthDiaryAdapter(null, mockClickListener);
        assertNotNull("适配器应该能够处理null日记列表", adapterWithNullList);
        assertEquals("null列表的项目数量应该为0", 0, adapterWithNullList.getItemCount());
    }
    
    /**
     * 测试日记内容处理
     */
    @Test
    public void testDiaryContentHandling() {
        List<HealthDiary> specialDiaries = new ArrayList<>();
        
        // 测试空内容
        HealthDiary emptyContentDiary = new HealthDiary();
        emptyContentDiary.setId(1L);
        emptyContentDiary.setUserId(1L);
        emptyContentDiary.setContent("");
        emptyContentDiary.setCreatedAt(System.currentTimeMillis());
        emptyContentDiary.setUpdatedAt(System.currentTimeMillis());
        specialDiaries.add(emptyContentDiary);
        
        // 测试null内容
        HealthDiary nullContentDiary = new HealthDiary();
        nullContentDiary.setId(2L);
        nullContentDiary.setUserId(1L);
        nullContentDiary.setContent(null);
        nullContentDiary.setCreatedAt(System.currentTimeMillis());
        nullContentDiary.setUpdatedAt(System.currentTimeMillis());
        specialDiaries.add(nullContentDiary);
        
        // 测试只有空格的内容
        HealthDiary whitespaceContentDiary = new HealthDiary();
        whitespaceContentDiary.setId(3L);
        whitespaceContentDiary.setUserId(1L);
        whitespaceContentDiary.setContent("   ");
        whitespaceContentDiary.setCreatedAt(System.currentTimeMillis());
        whitespaceContentDiary.setUpdatedAt(System.currentTimeMillis());
        specialDiaries.add(whitespaceContentDiary);
        
        HealthDiaryAdapter specialAdapter = new HealthDiaryAdapter(specialDiaries, mockClickListener);
        assertEquals("特殊内容适配器项目数量", 3, specialAdapter.getItemCount());
    }
    
    /**
     * 测试时间戳处理
     */
    @Test
    public void testTimestampHandling() {
        List<HealthDiary> timestampDiaries = new ArrayList<>();
        
        // 测试相同的创建和更新时间
        HealthDiary sameTimeDiary = new HealthDiary();
        sameTimeDiary.setId(1L);
        sameTimeDiary.setUserId(1L);
        sameTimeDiary.setContent("测试内容");
        long currentTime = System.currentTimeMillis();
        sameTimeDiary.setCreatedAt(currentTime);
        sameTimeDiary.setUpdatedAt(currentTime);
        timestampDiaries.add(sameTimeDiary);
        
        // 测试不同的创建和更新时间
        HealthDiary differentTimeDiary = new HealthDiary();
        differentTimeDiary.setId(2L);
        differentTimeDiary.setUserId(1L);
        differentTimeDiary.setContent("测试内容2");
        differentTimeDiary.setCreatedAt(currentTime - 3600000); // 1小时前创建
        differentTimeDiary.setUpdatedAt(currentTime); // 刚刚更新
        timestampDiaries.add(differentTimeDiary);
        
        HealthDiaryAdapter timestampAdapter = new HealthDiaryAdapter(timestampDiaries, mockClickListener);
        assertEquals("时间戳测试适配器项目数量", 2, timestampAdapter.getItemCount());
    }
}