package com.medication.reminders;

import static org.junit.Assert.*;

import android.content.Context;

import com.medication.reminders.database.entity.HealthDiary;
import com.medication.reminders.repository.HealthDiaryRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * HealthDiaryRepository单元测试
 * 测试健康日记Repository层的基本功能
 */
@RunWith(RobolectricTestRunner.class)
public class HealthDiaryRepositoryTest {
    
    private Context context;
    private HealthDiaryRepository repository;
    
    // 测试数据
    private static final long TEST_USER_ID = 1L;
    private static final long TEST_DIARY_ID = 1L;
    private static final String TEST_CONTENT = "今天感觉身体状况良好，血压正常。";
    private static final String TEST_UPDATED_CONTENT = "今天感觉身体状况良好，血压正常，心情愉快。";
    
    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        repository = new HealthDiaryRepository(context);
    }
    
    /**
     * 测试Repository实例化
     */
    @Test
    public void testRepositoryInstantiation() {
        assertNotNull("Repository实例不应该为null", repository);
        assertTrue("Repository应该是HealthDiaryRepository类型", repository instanceof HealthDiaryRepository);
    }
    
    /**
     * 测试Repository单例模式
     */
    @Test
    public void testRepositorySingleton() {
        HealthDiaryRepository instance1 = HealthDiaryRepository.getInstance(context);
        HealthDiaryRepository instance2 = HealthDiaryRepository.getInstance(context);
        
        assertNotNull("第一个实例不应该为null", instance1);
        assertNotNull("第二个实例不应该为null", instance2);
        assertSame("两个实例应该是同一个对象", instance1, instance2);
    }
    
    /**
     * 测试获取用户健康日记列表（基本功能）
     */
    @Test
    public void testGetUserDiaries_Basic() {
        // 执行测试
        try {
            repository.getUserDiaries();
            // 如果没有抛出异常，说明基本功能正常
            assertTrue("获取用户日记列表方法应该正常执行", true);
        } catch (Exception e) {
            fail("获取用户日记列表不应该抛出异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试获取用户健康日记数量（基本功能）
     */
    @Test
    public void testGetUserDiaryCount_Basic() {
        // 执行测试
        try {
            repository.getUserDiaryCount();
            // 如果没有抛出异常，说明基本功能正常
            assertTrue("获取用户日记数量方法应该正常执行", true);
        } catch (Exception e) {
            fail("获取用户日记数量不应该抛出异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试Repository状态信息
     */
    @Test
    public void testGetRepositoryStatus() {
        String status = repository.getRepositoryStatus();
        assertNotNull("状态信息不应该为null", status);
        assertTrue("状态信息应该包含Repository名称", status.contains("HealthDiaryRepository"));
    }
    
    /**
     * 测试Repository清理方法
     */
    @Test
    public void testRepositoryCleanup() {
        try {
            repository.cleanup();
            // 如果没有抛出异常，说明清理方法正常
            assertTrue("Repository清理方法应该正常执行", true);
        } catch (Exception e) {
            fail("Repository清理不应该抛出异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试BaseDataAccess接口实现
     */
    @Test
    public void testBaseDataAccessInterface() {
        // 验证Repository实现了BaseDataAccess接口
        assertTrue("Repository应该实现BaseDataAccess接口", 
            repository instanceof com.medication.reminders.models.BaseDataAccess);
    }
    
    /**
     * 测试内容验证方法
     */
    @Test
    public void testValidateDiaryContent() {
        // 测试空内容
        String result1 = HealthDiaryRepository.validateDiaryContent("");
        assertEquals("空内容应该返回错误信息", "日记内容不能为空", result1);
        
        // 测试null内容
        String result2 = HealthDiaryRepository.validateDiaryContent(null);
        assertEquals("null内容应该返回错误信息", "日记内容不能为空", result2);
        
        // 测试过长内容
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 5001; i++) {
            longContent.append("a");
        }
        String result3 = HealthDiaryRepository.validateDiaryContent(longContent.toString());
        assertEquals("过长内容应该返回错误信息", "日记内容不能超过5000字符", result3);
        
        // 测试正常内容
        String result4 = HealthDiaryRepository.validateDiaryContent(TEST_CONTENT);
        assertNull("正常内容应该通过验证", result4);
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 创建测试用的健康日记对象
     */
    private HealthDiary createTestDiary() {
        HealthDiary diary = new HealthDiary();
        diary.setUserId(TEST_USER_ID);
        diary.setContent(TEST_CONTENT);
        diary.setCreatedAt(System.currentTimeMillis());
        diary.setUpdatedAt(System.currentTimeMillis());
        return diary;
    }
    
    /**
     * 创建测试用的健康日记列表
     */
    private List<HealthDiary> createTestDiaryList() {
        List<HealthDiary> diaries = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            HealthDiary diary = new HealthDiary();
            diary.setId(i);
            diary.setUserId(TEST_USER_ID);
            diary.setContent("测试日记内容 " + i);
            diary.setCreatedAt(System.currentTimeMillis() - (i * 1000));
            diary.setUpdatedAt(System.currentTimeMillis() - (i * 1000));
            diaries.add(diary);
        }
        
        return diaries;
    }
}