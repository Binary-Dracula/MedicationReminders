package com.medication.reminders;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.medication.reminders.utils.LoadingIndicator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * LoadingIndicator工具类的单元测试
 * 测试加载指示器的显示和隐藏功能
 */
@RunWith(RobolectricTestRunner.class)
public class LoadingIndicatorTest {
    
    @Mock
    private ProgressBar mockProgressBar;
    
    @Mock
    private TextView mockLoadingText;
    
    @Mock
    private View mockOverlay;
    
    private Context realContext;
    private LoadingIndicator.Manager loadingManager;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        realContext = RuntimeEnvironment.getApplication();
        loadingManager = LoadingIndicator.createManager(realContext);
    }
    
    /**
     * 测试创建加载管理器
     */
    @Test
    public void testCreateManager() {
        LoadingIndicator.Manager manager = LoadingIndicator.createManager(realContext);
        assertNotNull("加载管理器不应为空", manager);
        assertFalse("初始状态不应显示加载", manager.isShowing());
    }
    
    /**
     * 测试显示和隐藏进度条
     */
    @Test
    public void testShowAndHideProgressBar() {
        String testMessage = "测试加载消息";
        
        // 显示进度条
        loadingManager.showProgressBar(mockProgressBar, mockLoadingText, testMessage);
        
        // 验证显示操作
        verify(mockProgressBar).setVisibility(View.VISIBLE);
        verify(mockLoadingText).setText(testMessage);
        verify(mockLoadingText).setVisibility(View.VISIBLE);
        assertTrue("应显示加载状态", loadingManager.isShowing());
        
        // 隐藏进度条
        loadingManager.hideProgressBar();
        
        // 验证隐藏操作
        verify(mockProgressBar).setVisibility(View.GONE);
        verify(mockLoadingText).setVisibility(View.GONE);
        assertFalse("不应显示加载状态", loadingManager.isShowing());
    }
    
    /**
     * 测试显示和隐藏覆盖层加载
     */
    @Test
    public void testShowAndHideOverlayLoading() {
        String testMessage = "测试覆盖层消息";
        
        // 显示覆盖层加载
        loadingManager.showOverlayLoading(mockOverlay, testMessage);
        
        // 验证显示操作
        verify(mockOverlay).setVisibility(View.VISIBLE);
        assertTrue("应显示加载状态", loadingManager.isShowing());
        
        // 隐藏覆盖层加载
        loadingManager.hideOverlayLoading();
        
        // 验证隐藏操作
        verify(mockOverlay).setVisibility(View.GONE);
        assertFalse("不应显示加载状态", loadingManager.isShowing());
    }
    
    /**
     * 测试更新加载消息
     */
    @Test
    public void testUpdateMessage() {
        String initialMessage = "初始消息";
        String updatedMessage = "更新消息";
        
        // 显示进度条
        loadingManager.showProgressBar(mockProgressBar, mockLoadingText, initialMessage);
        verify(mockLoadingText).setText(initialMessage);
        
        // 更新消息
        loadingManager.updateMessage(updatedMessage);
        verify(mockLoadingText).setText(updatedMessage);
    }
    
    /**
     * 测试隐藏所有加载指示器
     */
    @Test
    public void testHideAll() {
        // 显示多个加载指示器
        loadingManager.showProgressBar(mockProgressBar, mockLoadingText, "测试消息");
        loadingManager.showOverlayLoading(mockOverlay, "覆盖层消息");
        
        assertTrue("应显示加载状态", loadingManager.isShowing());
        
        // 隐藏所有
        loadingManager.hideAll();
        
        // 验证所有都被隐藏
        verify(mockProgressBar).setVisibility(View.GONE);
        verify(mockLoadingText).setVisibility(View.GONE);
        verify(mockOverlay).setVisibility(View.GONE);
        assertFalse("不应显示加载状态", loadingManager.isShowing());
    }
    
    /**
     * 测试空参数处理
     */
    @Test
    public void testNullParameterHandling() {
        // 测试空进度条
        loadingManager.showProgressBar(null, mockLoadingText, "测试消息");
        verify(mockLoadingText).setText("测试消息");
        verify(mockLoadingText).setVisibility(View.VISIBLE);
        
        // 重置mock并测试空文本视图
        reset(mockProgressBar, mockLoadingText);
        loadingManager.showProgressBar(mockProgressBar, null, "测试消息");
        verify(mockProgressBar).setVisibility(View.VISIBLE);
        
        // 测试空覆盖层
        loadingManager.showOverlayLoading(null, "测试消息");
        // 不应抛出异常
        
        // 重置mock并测试空消息
        reset(mockProgressBar, mockLoadingText);
        loadingManager.showProgressBar(mockProgressBar, mockLoadingText, null);
        verify(mockProgressBar).setVisibility(View.VISIBLE);
        verify(mockLoadingText).setVisibility(View.VISIBLE);
        // 应使用默认消息
    }
    
    /**
     * 测试健康日记专用加载方法
     */
    @Test
    public void testHealthDiaryLoadingMethods() {
        // 测试列表加载
        LoadingIndicator.HealthDiary.showInlineListLoading(loadingManager, mockProgressBar, mockLoadingText);
        verify(mockProgressBar).setVisibility(View.VISIBLE);
        verify(mockLoadingText).setVisibility(View.VISIBLE);
        assertTrue("应显示加载状态", loadingManager.isShowing());
        
        // 重置mock
        reset(mockProgressBar, mockLoadingText);
        loadingManager.hideAll();
        
        // 测试详情加载
        LoadingIndicator.HealthDiary.showInlineDetailLoading(loadingManager, mockProgressBar, mockLoadingText);
        verify(mockProgressBar).setVisibility(View.VISIBLE);
        verify(mockLoadingText).setVisibility(View.VISIBLE);
        assertTrue("应显示加载状态", loadingManager.isShowing());
    }
    
    /**
     * 测试健康日记覆盖层加载
     */
    @Test
    public void testHealthDiaryOverlayLoading() {
        // 测试保存操作
        LoadingIndicator.HealthDiary.showOverlayLoading(loadingManager, mockOverlay, "save");
        verify(mockOverlay).setVisibility(View.VISIBLE);
        assertTrue("应显示加载状态", loadingManager.isShowing());
        
        // 重置并测试更新操作
        reset(mockOverlay);
        loadingManager.hideAll();
        
        LoadingIndicator.HealthDiary.showOverlayLoading(loadingManager, mockOverlay, "update");
        verify(mockOverlay).setVisibility(View.VISIBLE);
        assertTrue("应显示加载状态", loadingManager.isShowing());
        
        // 重置并测试删除操作
        reset(mockOverlay);
        loadingManager.hideAll();
        
        LoadingIndicator.HealthDiary.showOverlayLoading(loadingManager, mockOverlay, "delete");
        verify(mockOverlay).setVisibility(View.VISIBLE);
        assertTrue("应显示加载状态", loadingManager.isShowing());
    }
    
    /**
     * 测试全局加载管理器
     */
    @Test
    public void testGlobalManager() {
        LoadingIndicator.Manager globalManager1 = LoadingIndicator.getGlobalManager(realContext);
        LoadingIndicator.Manager globalManager2 = LoadingIndicator.getGlobalManager(realContext);
        
        assertNotNull("全局管理器不应为空", globalManager1);
        assertSame("应返回同一个全局管理器实例", globalManager1, globalManager2);
        
        // 清理全局管理器
        LoadingIndicator.cleanupGlobalManager();
        
        // 获取新的全局管理器
        LoadingIndicator.Manager newGlobalManager = LoadingIndicator.getGlobalManager(realContext);
        assertNotNull("新的全局管理器不应为空", newGlobalManager);
        assertNotSame("清理后应创建新的管理器实例", globalManager1, newGlobalManager);
    }
    
    /**
     * 测试管理器清理
     */
    @Test
    public void testManagerCleanup() {
        // 显示一些加载指示器
        loadingManager.showProgressBar(mockProgressBar, mockLoadingText, "测试消息");
        assertTrue("应显示加载状态", loadingManager.isShowing());
        
        // 清理管理器
        loadingManager.cleanup();
        
        // 验证清理后状态
        assertFalse("清理后不应显示加载状态", loadingManager.isShowing());
    }
    
    /**
     * 测试连续操作
     */
    @Test
    public void testConsecutiveOperations() {
        // 连续显示不同类型的加载指示器
        loadingManager.showProgressBar(mockProgressBar, mockLoadingText, "消息1");
        assertTrue("第一次显示后应为加载状态", loadingManager.isShowing());
        
        loadingManager.showOverlayLoading(mockOverlay, "消息2");
        assertTrue("第二次显示后应为加载状态", loadingManager.isShowing());
        
        // 隐藏进度条，但覆盖层仍显示
        loadingManager.hideProgressBar();
        assertTrue("隐藏进度条后仍应为加载状态（因为覆盖层还在）", loadingManager.isShowing());
        
        // 隐藏覆盖层
        loadingManager.hideOverlayLoading();
        assertFalse("隐藏所有后不应为加载状态", loadingManager.isShowing());
    }
    
    /**
     * 测试状态一致性
     */
    @Test
    public void testStateConsistency() {
        // 初始状态
        assertFalse("初始状态不应显示加载", loadingManager.isShowing());
        
        // 显示后状态
        loadingManager.showProgressBar(mockProgressBar, mockLoadingText, "测试");
        assertTrue("显示后应为加载状态", loadingManager.isShowing());
        
        // 隐藏后状态
        loadingManager.hideProgressBar();
        assertFalse("隐藏后不应为加载状态", loadingManager.isShowing());
        
        // 再次显示
        loadingManager.showOverlayLoading(mockOverlay, "测试");
        assertTrue("再次显示后应为加载状态", loadingManager.isShowing());
        
        // 使用hideAll隐藏
        loadingManager.hideAll();
        assertFalse("使用hideAll后不应为加载状态", loadingManager.isShowing());
    }
}