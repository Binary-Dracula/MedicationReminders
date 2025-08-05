package com.medication.reminders;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.medication.reminders.database.entity.User;
import com.medication.reminders.models.RepositoryCallback;
import com.medication.reminders.models.UserError;
import com.medication.reminders.repository.UserRepository;
import com.medication.reminders.viewmodels.UserViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

/**
 * UserViewModel单元测试
 * 测试用户相关的业务逻辑和UI状态管理
 */
@RunWith(RobolectricTestRunner.class)
public class UserViewModelTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private Application mockApplication;
    
    @Mock
    private UserRepository mockUserRepository;
    
    @Mock
    private Observer<String> stringObserver;
    
    @Mock
    private Observer<Boolean> booleanObserver;
    
    @Mock
    private Observer<User> userObserver;
    
    private UserViewModel userViewModel;
    
    // 测试数据常量
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PHONE = "13800138000";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_FULL_NAME = "张三";
    private static final String TEST_GENDER = "男性";
    private static final String TEST_BIRTH_DATE = "1990-01-01";
    private static final long TEST_USER_ID = 1L;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock UserRepository.getInstance()
        try (MockedStatic<UserRepository> mockedStatic = mockStatic(UserRepository.class)) {
            mockedStatic.when(() -> UserRepository.getInstance(any(Application.class)))
                    .thenReturn(mockUserRepository);
            
            userViewModel = new UserViewModel(mockApplication);
        }
    }
    
    // ========== 用户注册测试 ==========
    
    @Test
    public void testRegisterUser_Success() {
        // 准备测试数据
        ArgumentCaptor<RepositoryCallback<Long>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // 观察注册状态
        userViewModel.getRegistrationStatus().observeForever(stringObserver);
        userViewModel.getIsLoading().observeForever(booleanObserver);
        
        // 执行注册
        userViewModel.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        
        // 验证Repository方法被调用
        verify(mockUserRepository).registerUser(eq(TEST_USERNAME), eq(TEST_EMAIL), 
                eq(TEST_PHONE), eq(TEST_PASSWORD), callbackCaptor.capture());
        
        // 验证加载状态
        verify(booleanObserver).onChanged(true);
        
        // 模拟成功回调
        RepositoryCallback<Long> callback = callbackCaptor.getValue();
        callback.onSuccess(TEST_USER_ID);
        
        // 验证状态更新
        verify(stringObserver).onChanged("注册成功");
        verify(booleanObserver).onChanged(false);
    }
    
    @Test
    public void testRegisterUser_UsernameExists() {
        // 准备测试数据
        ArgumentCaptor<RepositoryCallback<Long>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // 观察注册状态
        userViewModel.getRegistrationStatus().observeForever(stringObserver);
        userViewModel.getIsLoading().observeForever(booleanObserver);
        
        // 执行注册
        userViewModel.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        
        // 验证Repository方法被调用
        verify(mockUserRepository).registerUser(eq(TEST_USERNAME), eq(TEST_EMAIL), 
                eq(TEST_PHONE), eq(TEST_PASSWORD), callbackCaptor.capture());
        
        // 模拟失败回调
        RepositoryCallback<Long> callback = callbackCaptor.getValue();
        callback.onError("用户名已存在");
        
        // 验证状态更新
        verify(stringObserver).onChanged("用户名已存在");
        verify(booleanObserver).onChanged(false);
    }
    
    @Test
    public void testRegisterUser_EmptyUsername() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行注册（用户名为空）
        userViewModel.registerUser("", TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        
        // 验证表单验证错误
        verify(stringObserver).onChanged("用户名不能为空");
        
        // 验证Repository方法不应该被调用
        verify(mockUserRepository, never()).registerUser(anyString(), anyString(), 
                anyString(), anyString(), any());
    }
    
    @Test
    public void testRegisterUser_InvalidEmail() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行注册（邮箱格式错误）
        userViewModel.registerUser(TEST_USERNAME, "invalid-email", TEST_PHONE, TEST_PASSWORD);
        
        // 验证表单验证错误
        verify(stringObserver).onChanged("邮箱格式不正确");
        
        // 验证Repository方法不应该被调用
        verify(mockUserRepository, never()).registerUser(anyString(), anyString(), 
                anyString(), anyString(), any());
    }
    
    @Test
    public void testRegisterUser_InvalidPhone() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行注册（电话格式错误）
        userViewModel.registerUser(TEST_USERNAME, TEST_EMAIL, "123", TEST_PASSWORD);
        
        // 验证表单验证错误
        verify(stringObserver).onChanged("电话号码格式不正确");
        
        // 验证Repository方法不应该被调用
        verify(mockUserRepository, never()).registerUser(anyString(), anyString(), 
                anyString(), anyString(), any());
    }
    
    @Test
    public void testRegisterUser_WeakPassword() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行注册（密码太弱）
        userViewModel.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, "123");
        
        // 验证表单验证错误
        verify(stringObserver).onChanged("密码长度至少6位");
        
        // 验证Repository方法不应该被调用
        verify(mockUserRepository, never()).registerUser(anyString(), anyString(), 
                anyString(), anyString(), any());
    }
    
    // ========== 用户登录测试 ==========
    
    @Test
    public void testLoginUser_Success() {
        // 准备测试数据
        User testUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        testUser.setId(TEST_USER_ID);
        testUser.setLoggedIn(true);
        testUser.setRememberMe(true);
        
        ArgumentCaptor<RepositoryCallback<User>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // 观察登录状态
        userViewModel.getLoginStatus().observeForever(stringObserver);
        userViewModel.getIsLoading().observeForever(booleanObserver);
        
        // 执行登录
        userViewModel.loginUser(TEST_USERNAME, TEST_PASSWORD, true);
        
        // 验证Repository方法被调用
        verify(mockUserRepository).loginUser(eq(TEST_USERNAME), eq(TEST_PASSWORD), 
                eq(true), callbackCaptor.capture());
        
        // 验证加载状态
        verify(booleanObserver).onChanged(true);
        
        // 模拟成功回调
        RepositoryCallback<User> callback = callbackCaptor.getValue();
        callback.onSuccess(testUser);
        
        // 验证状态更新
        verify(stringObserver).onChanged("登录成功");
        verify(booleanObserver).onChanged(false);
    }
    
    @Test
    public void testLoginUser_WrongPassword() {
        // 准备测试数据
        ArgumentCaptor<RepositoryCallback<User>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // 观察登录状态
        userViewModel.getLoginStatus().observeForever(stringObserver);
        userViewModel.getIsLoading().observeForever(booleanObserver);
        
        // 执行登录
        userViewModel.loginUser(TEST_USERNAME, "wrongpassword", false);
        
        // 验证Repository方法被调用
        verify(mockUserRepository).loginUser(eq(TEST_USERNAME), eq("wrongpassword"), 
                eq(false), callbackCaptor.capture());
        
        // 模拟失败回调
        RepositoryCallback<User> callback = callbackCaptor.getValue();
        callback.onError("密码错误");
        
        // 验证状态更新
        verify(stringObserver).onChanged("密码错误");
        verify(booleanObserver).onChanged(false);
    }
    
    @Test
    public void testLoginUser_UserNotExists() {
        // 准备测试数据
        ArgumentCaptor<RepositoryCallback<User>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // 观察登录状态
        userViewModel.getLoginStatus().observeForever(stringObserver);
        
        // 执行登录
        userViewModel.loginUser("nonexistentuser", TEST_PASSWORD, false);
        
        // 验证Repository方法被调用
        verify(mockUserRepository).loginUser(eq("nonexistentuser"), eq(TEST_PASSWORD), 
                eq(false), callbackCaptor.capture());
        
        // 模拟失败回调
        RepositoryCallback<User> callback = callbackCaptor.getValue();
        callback.onError("用户名不存在");
        
        // 验证状态更新
        verify(stringObserver).onChanged("用户名不存在");
    }
    
    @Test
    public void testLoginUser_EmptyUsername() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行登录（用户名为空）
        userViewModel.loginUser("", TEST_PASSWORD, false);
        
        // 验证表单验证错误
        verify(stringObserver).onChanged("请输入用户名");
        
        // 验证Repository方法不应该被调用
        verify(mockUserRepository, never()).loginUser(anyString(), anyString(), 
                anyBoolean(), any());
    }
    
    @Test
    public void testLoginUser_EmptyPassword() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行登录（密码为空）
        userViewModel.loginUser(TEST_USERNAME, "", false);
        
        // 验证表单验证错误
        verify(stringObserver).onChanged("请输入密码");
        
        // 验证Repository方法不应该被调用
        verify(mockUserRepository, never()).loginUser(anyString(), anyString(), 
                anyBoolean(), any());
    }
    
    // ========== 获取当前用户测试 ==========
    
    @Test
    public void testGetCurrentUser() {
        // 准备测试数据
        User testUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        testUser.setId(TEST_USER_ID);
        
        MutableLiveData<User> mockLiveData = new MutableLiveData<>();
        mockLiveData.setValue(testUser);
        
        // Mock Repository方法
        when(mockUserRepository.getCurrentUser()).thenReturn(mockLiveData);
        
        // 执行测试
        LiveData<User> result = userViewModel.getCurrentUser();
        
        // 验证结果
        assertNotNull("应该返回LiveData对象", result);
        assertEquals("应该返回相同的LiveData对象", mockLiveData, result);
        
        // 验证Repository方法被调用
        verify(mockUserRepository).getCurrentUser();
    }
    
    // ========== 用户资料更新测试 ==========
    
    @Test
    public void testUpdateBasicProfile_Success() {
        // 准备测试数据
        User testUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        testUser.setId(TEST_USER_ID);
        testUser.setFullName(TEST_FULL_NAME);
        testUser.setGender(TEST_GENDER);
        testUser.setBirthDate(TEST_BIRTH_DATE);
        
        ArgumentCaptor<RepositoryCallback<Boolean>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // Mock getCurrentUserAsync返回测试用户
        doAnswer(invocation -> {
            RepositoryCallback<User> callback = invocation.getArgument(0);
            callback.onSuccess(testUser);
            return null;
        }).when(mockUserRepository).getCurrentUserAsync(any());
        
        // 观察资料更新状态
        userViewModel.getProfileUpdateStatus().observeForever(stringObserver);
        userViewModel.getIsLoading().observeForever(booleanObserver);
        
        // 执行资料更新
        userViewModel.updateBasicProfile(TEST_FULL_NAME, TEST_GENDER, TEST_BIRTH_DATE);
        
        // 验证Repository方法被调用
        verify(mockUserRepository).updateUserProfile(any(User.class), callbackCaptor.capture());
        
        // 验证加载状态
        verify(booleanObserver).onChanged(true);
        
        // 模拟成功回调
        RepositoryCallback<Boolean> callback = callbackCaptor.getValue();
        callback.onSuccess(true);
        
        // 验证状态更新
        verify(stringObserver).onChanged("资料更新成功");
        verify(booleanObserver).onChanged(false);
    }
    
    @Test
    public void testUpdateBasicProfile_NoCurrentUser() {
        // Mock getCurrentUserAsync返回null
        doAnswer(invocation -> {
            RepositoryCallback<User> callback = invocation.getArgument(0);
            callback.onError("用户未登录");
            return null;
        }).when(mockUserRepository).getCurrentUserAsync(any());
        
        // 观察资料更新状态
        userViewModel.getProfileUpdateStatus().observeForever(stringObserver);
        
        // 执行资料更新
        userViewModel.updateBasicProfile(TEST_FULL_NAME, TEST_GENDER, TEST_BIRTH_DATE);
        
        // 验证错误状态
        verify(stringObserver).onChanged("请先登录");
        
        // 验证Repository的updateUserProfile方法不应该被调用
        verify(mockUserRepository, never()).updateUserProfile(any(User.class), any());
    }
    
    @Test
    public void testUpdateBasicProfile_InvalidFullName() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行资料更新（姓名为空）
        userViewModel.updateBasicProfile("", TEST_GENDER, TEST_BIRTH_DATE);
        
        // 验证表单验证错误
        verify(stringObserver).onChanged("姓名不能为空");
        
        // 验证Repository方法不应该被调用
        verify(mockUserRepository, never()).updateUserProfile(any(User.class), any());
    }
    
    @Test
    public void testUpdateBasicProfile_InvalidBirthDate() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行资料更新（出生日期格式错误）
        userViewModel.updateBasicProfile(TEST_FULL_NAME, TEST_GENDER, "invalid-date");
        
        // 验证表单验证错误
        verify(stringObserver).onChanged("出生日期格式不正确");
        
        // 验证Repository方法不应该被调用
        verify(mockUserRepository, never()).updateUserProfile(any(User.class), any());
    }
    
    @Test
    public void testUpdateContactInfo_Success() {
        // 准备测试数据
        User testUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        testUser.setId(TEST_USER_ID);
        
        String secondaryPhone = "13900139000";
        String emergencyName = "李四";
        String emergencyPhone = "13700137000";
        String emergencyRelation = "配偶";
        
        ArgumentCaptor<RepositoryCallback<Boolean>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // Mock getCurrentUserAsync返回测试用户
        doAnswer(invocation -> {
            RepositoryCallback<User> callback = invocation.getArgument(0);
            callback.onSuccess(testUser);
            return null;
        }).when(mockUserRepository).getCurrentUserAsync(any());
        
        // 观察资料更新状态
        userViewModel.getProfileUpdateStatus().observeForever(stringObserver);
        
        // 执行联系信息更新
        userViewModel.updateContactInfo(secondaryPhone, emergencyName, emergencyPhone, emergencyRelation);
        
        // 验证Repository方法被调用
        verify(mockUserRepository).updateUserProfile(any(User.class), callbackCaptor.capture());
        
        // 模拟成功回调
        RepositoryCallback<Boolean> callback = callbackCaptor.getValue();
        callback.onSuccess(true);
        
        // 验证状态更新
        verify(stringObserver).onChanged("联系信息更新成功");
    }
    
    @Test
    public void testUpdateMedicalInfo_Success() {
        // 准备测试数据
        User testUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        testUser.setId(TEST_USER_ID);
        
        String bloodType = "A+";
        String allergies = "青霉素过敏";
        String medicalConditions = "高血压";
        String doctorName = "王医生";
        String doctorPhone = "13600136000";
        String hospitalName = "北京医院";
        
        ArgumentCaptor<RepositoryCallback<Boolean>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // Mock getCurrentUserAsync返回测试用户
        doAnswer(invocation -> {
            RepositoryCallback<User> callback = invocation.getArgument(0);
            callback.onSuccess(testUser);
            return null;
        }).when(mockUserRepository).getCurrentUserAsync(any());
        
        // 观察资料更新状态
        userViewModel.getProfileUpdateStatus().observeForever(stringObserver);
        
        // 执行医疗信息更新
        userViewModel.updateMedicalInfo(bloodType, allergies, medicalConditions, 
                doctorName, doctorPhone, hospitalName);
        
        // 验证Repository方法被调用
        verify(mockUserRepository).updateUserProfile(any(User.class), callbackCaptor.capture());
        
        // 模拟成功回调
        RepositoryCallback<Boolean> callback = callbackCaptor.getValue();
        callback.onSuccess(true);
        
        // 验证状态更新
        verify(stringObserver).onChanged("医疗信息更新成功");
    }
    
    // ========== 用户登出测试 ==========
    
    @Test
    public void testLogoutUser_Success() {
        // 准备测试数据
        ArgumentCaptor<RepositoryCallback<Boolean>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // 观察登录状态
        userViewModel.getLoginStatus().observeForever(stringObserver);
        userViewModel.getIsLoading().observeForever(booleanObserver);
        
        // 执行登出
        userViewModel.logoutUser();
        
        // 验证Repository方法被调用
        verify(mockUserRepository).logoutUser(callbackCaptor.capture());
        
        // 验证加载状态
        verify(booleanObserver).onChanged(true);
        
        // 模拟成功回调
        RepositoryCallback<Boolean> callback = callbackCaptor.getValue();
        callback.onSuccess(true);
        
        // 验证状态更新
        verify(stringObserver).onChanged("已退出登录");
        verify(booleanObserver).onChanged(false);
    }
    
    @Test
    public void testLogoutUser_Failure() {
        // 准备测试数据
        ArgumentCaptor<RepositoryCallback<Boolean>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // 观察登录状态
        userViewModel.getLoginStatus().observeForever(stringObserver);
        
        // 执行登出
        userViewModel.logoutUser();
        
        // 验证Repository方法被调用
        verify(mockUserRepository).logoutUser(callbackCaptor.capture());
        
        // 模拟失败回调
        RepositoryCallback<Boolean> callback = callbackCaptor.getValue();
        callback.onError("登出失败");
        
        // 验证状态更新
        verify(stringObserver).onChanged("登出失败");
    }
    
    // ========== 记住我功能测试 ==========
    
    @Test
    public void testCheckRememberedUser_Success() {
        // 准备测试数据
        User testUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        testUser.setId(TEST_USER_ID);
        testUser.setRememberMe(true);
        
        ArgumentCaptor<RepositoryCallback<User>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // 观察登录状态
        userViewModel.getLoginStatus().observeForever(stringObserver);
        
        // 执行检查记住的用户
        userViewModel.checkRememberedUser();
        
        // 验证Repository方法被调用
        verify(mockUserRepository).checkRememberedUser(callbackCaptor.capture());
        
        // 模拟成功回调
        RepositoryCallback<User> callback = callbackCaptor.getValue();
        callback.onSuccess(testUser);
        
        // 验证状态更新
        verify(stringObserver).onChanged("自动登录成功");
    }
    
    @Test
    public void testCheckRememberedUser_NoRememberedUser() {
        // 准备测试数据
        ArgumentCaptor<RepositoryCallback<User>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // 观察登录状态
        userViewModel.getLoginStatus().observeForever(stringObserver);
        
        // 执行检查记住的用户
        userViewModel.checkRememberedUser();
        
        // 验证Repository方法被调用
        verify(mockUserRepository).checkRememberedUser(callbackCaptor.capture());
        
        // 模拟失败回调（没有记住的用户）
        RepositoryCallback<User> callback = callbackCaptor.getValue();
        callback.onError("没有记住的用户");
        
        // 验证状态更新（应该是静默失败，不显示错误）
        verify(stringObserver, never()).onChanged(anyString());
    }
    
    // ========== 表单验证测试 ==========
    
    @Test
    public void testValidateRegistrationForm_AllValid() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行表单验证（所有字段都有效）
        userViewModel.validateRegistrationForm(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        
        // 验证没有错误信息
        verify(stringObserver, never()).onChanged(anyString());
    }
    
    @Test
    public void testValidateRegistrationForm_MultipleErrors() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行表单验证（多个字段无效）
        userViewModel.validateRegistrationForm("", "invalid-email", "123", "");
        
        // 验证返回第一个错误
        verify(stringObserver).onChanged("用户名不能为空");
    }
    
    @Test
    public void testValidateProfileForm_AllValid() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行表单验证（所有字段都有效）
        userViewModel.validateProfileForm(TEST_FULL_NAME, TEST_GENDER, TEST_BIRTH_DATE);
        
        // 验证没有错误信息
        verify(stringObserver, never()).onChanged(anyString());
    }
    
    @Test
    public void testValidateProfileForm_InvalidFields() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 执行表单验证（字段无效）
        userViewModel.validateProfileForm("", "", "invalid-date");
        
        // 验证返回第一个错误
        verify(stringObserver).onChanged("姓名不能为空");
    }
    
    // ========== 加载状态测试 ==========
    
    @Test
    public void testIsLoadingInitialValue() {
        // 观察加载状态
        userViewModel.getIsLoading().observeForever(booleanObserver);
        
        // 验证初始值为false
        verify(booleanObserver).onChanged(false);
    }
    
    @Test
    public void testLoadingStatesDuringOperations() {
        // 准备测试数据
        ArgumentCaptor<RepositoryCallback<Long>> callbackCaptor = 
                ArgumentCaptor.forClass(RepositoryCallback.class);
        
        // 观察加载状态
        userViewModel.getIsLoading().observeForever(booleanObserver);
        
        // 执行注册操作
        userViewModel.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        
        // 验证加载状态变为true
        verify(booleanObserver, atLeastOnce()).onChanged(true);
        
        // 模拟操作完成
        verify(mockUserRepository).registerUser(anyString(), anyString(), anyString(), 
                anyString(), callbackCaptor.capture());
        RepositoryCallback<Long> callback = callbackCaptor.getValue();
        callback.onSuccess(TEST_USER_ID);
        
        // 验证加载状态变为false
        verify(booleanObserver, atLeastOnce()).onChanged(false);
    }
    
    // ========== 边界情况测试 ==========
    
    @Test
    public void testNullInputHandling() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 测试null输入
        userViewModel.registerUser(null, null, null, null);
        
        // 验证处理null输入
        verify(stringObserver).onChanged("用户名不能为空");
        
        // 验证Repository方法不应该被调用
        verify(mockUserRepository, never()).registerUser(anyString(), anyString(), 
                anyString(), anyString(), any());
    }
    
    @Test
    public void testEmptyStringInputHandling() {
        // 观察表单验证错误
        userViewModel.getFormValidationError().observeForever(stringObserver);
        
        // 测试空字符串输入
        userViewModel.loginUser("   ", "   ", false);
        
        // 验证处理空白字符串
        verify(stringObserver).onChanged("请输入用户名");
        
        // 验证Repository方法不应该被调用
        verify(mockUserRepository, never()).loginUser(anyString(), anyString(), 
                anyBoolean(), any());
    }
    
    @Test
    public void testConcurrentOperations() {
        // 观察加载状态
        userViewModel.getIsLoading().observeForever(booleanObserver);
        
        // 同时执行多个操作
        userViewModel.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        userViewModel.loginUser(TEST_USERNAME, TEST_PASSWORD, false);
        
        // 验证只有一个操作在进行（第二个操作应该被忽略或排队）
        verify(mockUserRepository).registerUser(anyString(), anyString(), anyString(), 
                anyString(), any());
        
        // 注意：具体的并发处理策略取决于ViewModel的实现
        // 这里只是验证基本的调用情况
    }
    
    // ========== 内存泄漏预防测试 ==========
    
    @Test
    public void testViewModelCleanup() {
        // 这个测试主要是确保ViewModel正确处理生命周期
        // 在实际应用中，可能需要测试onCleared()方法的调用
        
        // 观察LiveData
        userViewModel.getLoginStatus().observeForever(stringObserver);
        userViewModel.getRegistrationStatus().observeForever(stringObserver);
        userViewModel.getIsLoading().observeForever(booleanObserver);
        
        // 移除观察者
        userViewModel.getLoginStatus().removeObserver(stringObserver);
        userViewModel.getRegistrationStatus().removeObserver(stringObserver);
        userViewModel.getIsLoading().removeObserver(booleanObserver);
        
        // 验证没有异常抛出
        // 在实际测试中，可能需要更复杂的内存泄漏检测
    }
}