package com.medication.reminders;

import android.content.Context;

import com.medication.reminders.utils.ErrorHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * ErrorHandler工具类的单元测试
 * 测试错误处理和用户友好消息转换功能
 */
@RunWith(RobolectricTestRunner.class)
public class ErrorHandlerTest {
    
    @Mock
    private Context mockContext;
    
    private Context realContext;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        realContext = RuntimeEnvironment.getApplication();
    }
    
    /**
     * 测试网络错误处理
     */
    @Test
    public void testHandleNetworkException() {
        ConnectException networkException = new ConnectException("Network connection failed");
        
        ErrorHandler.ErrorInfo errorInfo = ErrorHandler.handleException(
            realContext, networkException, "测试网络操作");
        
        assertNotNull("错误信息不应为空", errorInfo);
        assertEquals("应识别为网络错误", ErrorHandler.ErrorType.NETWORK_ERROR, errorInfo.getType());
        assertTrue("技术消息应包含操作描述", errorInfo.getMessage().contains("测试网络操作"));
        assertNotNull("友好消息不应为空", errorInfo.getFriendlyMessage());
        assertEquals("异常对象应保持一致", networkException, errorInfo.getCause());
    }
    
    /**
     * 测试超时错误处理
     */
    @Test
    public void testHandleTimeoutException() {
        SocketTimeoutException timeoutException = new SocketTimeoutException("Connection timeout");
        
        ErrorHandler.ErrorInfo errorInfo = ErrorHandler.handleException(
            realContext, timeoutException, "测试超时操作");
        
        assertNotNull("错误信息不应为空", errorInfo);
        assertEquals("应识别为超时错误", ErrorHandler.ErrorType.TIMEOUT_ERROR, errorInfo.getType());
        assertTrue("技术消息应包含操作描述", errorInfo.getMessage().contains("测试超时操作"));
        assertNotNull("友好消息不应为空", errorInfo.getFriendlyMessage());
    }
    
    /**
     * 测试数据库错误处理
     */
    @Test
    public void testHandleDatabaseException() {
        SQLException databaseException = new SQLException("Database operation failed");
        
        ErrorHandler.ErrorInfo errorInfo = ErrorHandler.handleException(
            realContext, databaseException, "测试数据库操作");
        
        assertNotNull("错误信息不应为空", errorInfo);
        assertEquals("应识别为数据库错误", ErrorHandler.ErrorType.DATABASE_ERROR, errorInfo.getType());
        assertTrue("技术消息应包含操作描述", errorInfo.getMessage().contains("测试数据库操作"));
        assertNotNull("友好消息不应为空", errorInfo.getFriendlyMessage());
    }
    
    /**
     * 测试权限错误处理
     */
    @Test
    public void testHandlePermissionException() {
        SecurityException permissionException = new SecurityException("Permission denied");
        
        ErrorHandler.ErrorInfo errorInfo = ErrorHandler.handleException(
            realContext, permissionException, "测试权限操作");
        
        assertNotNull("错误信息不应为空", errorInfo);
        assertEquals("应识别为权限错误", ErrorHandler.ErrorType.PERMISSION_ERROR, errorInfo.getType());
        assertTrue("技术消息应包含操作描述", errorInfo.getMessage().contains("测试权限操作"));
        assertNotNull("友好消息不应为空", errorInfo.getFriendlyMessage());
    }
    
    /**
     * 测试验证错误处理
     */
    @Test
    public void testHandleValidationException() {
        IllegalArgumentException validationException = new IllegalArgumentException("Invalid input");
        
        ErrorHandler.ErrorInfo errorInfo = ErrorHandler.handleException(
            realContext, validationException, "测试验证操作");
        
        assertNotNull("错误信息不应为空", errorInfo);
        assertEquals("应识别为验证错误", ErrorHandler.ErrorType.VALIDATION_ERROR, errorInfo.getType());
        assertTrue("技术消息应包含操作描述", errorInfo.getMessage().contains("测试验证操作"));
        assertNotNull("友好消息不应为空", errorInfo.getFriendlyMessage());
    }
    
    /**
     * 测试未知错误处理
     */
    @Test
    public void testHandleUnknownException() {
        RuntimeException unknownException = new RuntimeException("Unknown error");
        
        ErrorHandler.ErrorInfo errorInfo = ErrorHandler.handleException(
            realContext, unknownException, "测试未知操作");
        
        assertNotNull("错误信息不应为空", errorInfo);
        assertEquals("应识别为未知错误", ErrorHandler.ErrorType.UNKNOWN_ERROR, errorInfo.getType());
        assertTrue("技术消息应包含操作描述", errorInfo.getMessage().contains("测试未知操作"));
        assertNotNull("友好消息不应为空", errorInfo.getFriendlyMessage());
    }
    
    /**
     * 测试空参数处理
     */
    @Test
    public void testHandleNullParameters() {
        // 测试空Context
        ErrorHandler.ErrorInfo errorInfo1 = ErrorHandler.handleException(
            null, new RuntimeException("test"), "测试操作");
        assertNotNull("即使Context为空也应返回错误信息", errorInfo1);
        assertEquals("应识别为未知错误", ErrorHandler.ErrorType.UNKNOWN_ERROR, errorInfo1.getType());
        
        // 测试空异常
        ErrorHandler.ErrorInfo errorInfo2 = ErrorHandler.handleException(
            realContext, null, "测试操作");
        assertNotNull("即使异常为空也应返回错误信息", errorInfo2);
        assertEquals("应识别为未知错误", ErrorHandler.ErrorType.UNKNOWN_ERROR, errorInfo2.getType());
        
        // 测试空操作描述
        ErrorHandler.ErrorInfo errorInfo3 = ErrorHandler.handleException(
            realContext, new RuntimeException("test"), null);
        assertNotNull("即使操作描述为空也应返回错误信息", errorInfo3);
        assertNotNull("技术消息不应为空", errorInfo3.getMessage());
    }
    
    /**
     * 测试健康日记专用错误处理
     */
    @Test
    public void testHealthDiaryErrorHandling() {
        SQLException databaseException = new SQLException("Database error");
        
        // 测试添加日记错误
        ErrorHandler.ErrorInfo addError = ErrorHandler.HealthDiary.handleDiaryError(
            realContext, databaseException, "添加");
        assertNotNull("添加错误信息不应为空", addError);
        assertEquals("应识别为数据库错误", ErrorHandler.ErrorType.DATABASE_ERROR, addError.getType());
        assertTrue("友好消息应包含添加相关信息", addError.getFriendlyMessage().contains("添加"));
        
        // 测试更新日记错误
        ErrorHandler.ErrorInfo updateError = ErrorHandler.HealthDiary.handleDiaryError(
            realContext, databaseException, "更新");
        assertNotNull("更新错误信息不应为空", updateError);
        assertEquals("应识别为数据库错误", ErrorHandler.ErrorType.DATABASE_ERROR, updateError.getType());
        assertTrue("友好消息应包含更新相关信息", updateError.getFriendlyMessage().contains("更新"));
        
        // 测试删除日记错误
        ErrorHandler.ErrorInfo deleteError = ErrorHandler.HealthDiary.handleDiaryError(
            realContext, databaseException, "删除");
        assertNotNull("删除错误信息不应为空", deleteError);
        assertEquals("应识别为数据库错误", ErrorHandler.ErrorType.DATABASE_ERROR, deleteError.getType());
        assertTrue("友好消息应包含删除相关信息", deleteError.getFriendlyMessage().contains("删除"));
        
        // 测试加载日记错误
        ErrorHandler.ErrorInfo loadError = ErrorHandler.HealthDiary.handleDiaryError(
            realContext, databaseException, "加载");
        assertNotNull("加载错误信息不应为空", loadError);
        assertEquals("应识别为数据库错误", ErrorHandler.ErrorType.DATABASE_ERROR, loadError.getType());
        assertTrue("友好消息应包含加载相关信息", loadError.getFriendlyMessage().contains("加载"));
    }
    
    /**
     * 测试网络错误工具方法
     */
    @Test
    public void testNetworkErrorUtils() {
        // 测试网络异常识别
        assertTrue("应识别ConnectException为网络错误", 
            ErrorHandler.Network.isNetworkError(new ConnectException()));
        assertTrue("应识别SocketTimeoutException为网络错误", 
            ErrorHandler.Network.isNetworkError(new SocketTimeoutException()));
        assertFalse("不应将SQLException识别为网络错误", 
            ErrorHandler.Network.isNetworkError(new SQLException()));
        
        // 测试包含网络关键词的异常
        assertTrue("应识别包含network关键词的异常为网络错误", 
            ErrorHandler.Network.isNetworkError(new RuntimeException("network connection failed")));
        assertTrue("应识别包含connection关键词的异常为网络错误", 
            ErrorHandler.Network.isNetworkError(new RuntimeException("connection timeout")));
        
        // 测试网络错误消息
        String networkMessage = ErrorHandler.Network.getNetworkErrorMessage(realContext);
        assertNotNull("网络错误消息不应为空", networkMessage);
        assertFalse("网络错误消息不应为空字符串", networkMessage.isEmpty());
    }
    
    /**
     * 测试数据库错误工具方法
     */
    @Test
    public void testDatabaseErrorUtils() {
        // 测试数据库异常识别
        assertTrue("应识别SQLException为数据库错误", 
            ErrorHandler.Database.isDatabaseError(new SQLException()));
        assertFalse("不应将ConnectException识别为数据库错误", 
            ErrorHandler.Database.isDatabaseError(new ConnectException()));
        
        // 测试包含数据库关键词的异常
        assertTrue("应识别包含database关键词的异常为数据库错误", 
            ErrorHandler.Database.isDatabaseError(new RuntimeException("database operation failed")));
        assertTrue("应识别包含sql关键词的异常为数据库错误", 
            ErrorHandler.Database.isDatabaseError(new RuntimeException("sql syntax error")));
        
        // 测试数据库错误消息
        String databaseMessage = ErrorHandler.Database.getDatabaseErrorMessage(realContext);
        assertNotNull("数据库错误消息不应为空", databaseMessage);
        assertFalse("数据库错误消息不应为空字符串", databaseMessage.isEmpty());
    }
    
    /**
     * 测试错误信息对象的完整性
     */
    @Test
    public void testErrorInfoCompleteness() {
        RuntimeException testException = new RuntimeException("Test error message");
        
        ErrorHandler.ErrorInfo errorInfo = ErrorHandler.handleException(
            realContext, testException, "测试操作");
        
        // 验证所有字段都已正确设置
        assertNotNull("错误类型不应为空", errorInfo.getType());
        assertNotNull("技术消息不应为空", errorInfo.getMessage());
        assertNotNull("友好消息不应为空", errorInfo.getFriendlyMessage());
        assertNotNull("异常对象不应为空", errorInfo.getCause());
        
        // 验证消息内容的合理性
        assertFalse("技术消息不应为空字符串", errorInfo.getMessage().isEmpty());
        assertFalse("友好消息不应为空字符串", errorInfo.getFriendlyMessage().isEmpty());
        assertEquals("异常对象应保持一致", testException, errorInfo.getCause());
        
        // 验证技术消息包含必要信息
        assertTrue("技术消息应包含操作描述", errorInfo.getMessage().contains("测试操作"));
        assertTrue("技术消息应包含异常信息", errorInfo.getMessage().contains("Test error message"));
    }
}