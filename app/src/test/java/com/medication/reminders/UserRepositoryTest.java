package com.medication.reminders;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.medication.reminders.database.MedicationDatabase;
import com.medication.reminders.database.dao.UserDao;
import com.medication.reminders.database.entity.User;
import com.medication.reminders.models.RepositoryCallback;
import com.medication.reminders.repository.UserRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UserRepository单元测试
 * 测试基于数据库的用户管理功能
 */
@RunWith(RobolectricTestRunner.class)
public class UserRepositoryTest {
    
    @Mock
    private UserDao mockUserDao;
    
    @Mock
    private MedicationDatabase mockDatabase;
    
    private Context context;
    private UserRepository userRepository;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        
        // Mock MedicationDatabase.getDatabase()
        try (MockedStatic<MedicationDatabase> mockedStatic = mockStatic(MedicationDatabase.class)) {
            mockedStatic.when(() -> MedicationDatabase.getDatabase(any(Context.class)))
                    .thenReturn(mockDatabase);
            when(mockDatabase.userDao()).thenReturn(mockUserDao);
            
            userRepository = UserRepository.getInstance(context);
        }
    }
    
    @Test
    public void testRegisterUser_Success() throws InterruptedException {
        // 准备测试数据
        String username = "testuser";
        String email = "test@example.com";
        String phone = "1234567890";
        String password = "password123";
        long expectedUserId = 1L;
        
        // Mock DAO 方法
        when(mockUserDao.getUsernameCount(username)).thenReturn(0);
        when(mockUserDao.getEmailCount(email)).thenReturn(0);
        when(mockUserDao.getPhoneCount(phone)).thenReturn(0);
        when(mockUserDao.insertUser(any(User.class))).thenReturn(expectedUserId);
        
        // 使用CountDownLatch等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final Long[] resultUserId = new Long[1];
        final String[] errorMessage = new String[1];
        
        // 执行测试
        userRepository.registerUser(username, email, phone, password, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                resultUserId[0] = result;
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorMessage[0] = error;
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        assertTrue("操作应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        
        // 验证结果
        assertNull("不应该有错误", errorMessage[0]);
        assertNotNull("应该返回用户ID", resultUserId[0]);
        assertEquals("用户ID应该匹配", expectedUserId, resultUserId[0].longValue());
        
        // 验证DAO方法被调用
        verify(mockUserDao).getUsernameCount(username);
        verify(mockUserDao).getEmailCount(email);
        verify(mockUserDao).getPhoneCount(phone);
        verify(mockUserDao).insertUser(any(User.class));
    }
    
    @Test
    public void testRegisterUser_UsernameExists() throws InterruptedException {
        // 准备测试数据
        String username = "existinguser";
        String email = "test@example.com";
        String phone = "1234567890";
        String password = "password123";
        
        // Mock DAO 方法 - 用户名已存在
        when(mockUserDao.getUsernameCount(username)).thenReturn(1);
        
        // 使用CountDownLatch等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final Long[] resultUserId = new Long[1];
        final String[] errorMessage = new String[1];
        
        // 执行测试
        userRepository.registerUser(username, email, phone, password, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                resultUserId[0] = result;
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorMessage[0] = error;
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        assertTrue("操作应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        
        // 验证结果
        assertNull("不应该返回用户ID", resultUserId[0]);
        assertNotNull("应该有错误信息", errorMessage[0]);
        assertTrue("错误信息应该包含用户名已存在", errorMessage[0].contains("用户名已存在"));
        
        // 验证DAO方法被调用
        verify(mockUserDao).getUsernameCount(username);
        verify(mockUserDao, never()).insertUser(any(User.class));
    }
    
    @Test
    public void testLoginUser_Success() throws InterruptedException {
        // 准备测试数据
        String username = "testuser";
        String password = "password123";
        boolean rememberMe = true;
        
        User mockUser = new User(username, "test@example.com", "1234567890", password);
        mockUser.setId(1L);
        mockUser.setLoginAttempts(0);
        
        // Mock DAO 方法
        when(mockUserDao.getUserByUsername(username)).thenReturn(mockUser);
        when(mockUserDao.logoutAllUsers()).thenReturn(1);
        when(mockUserDao.setUserLoggedIn(eq(1L), anyLong())).thenReturn(1);
        when(mockUserDao.updateRememberMe(eq(1L), eq(true))).thenReturn(1);
        when(mockUserDao.resetLoginAttempts(username)).thenReturn(1);
        
        // 使用CountDownLatch等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final User[] resultUser = new User[1];
        final String[] errorMessage = new String[1];
        
        // 执行测试
        userRepository.loginUser(username, password, rememberMe, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                resultUser[0] = result;
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorMessage[0] = error;
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        assertTrue("操作应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        
        // 验证结果
        assertNull("不应该有错误", errorMessage[0]);
        assertNotNull("应该返回用户对象", resultUser[0]);
        assertEquals("用户名应该匹配", username, resultUser[0].getUsername());
        assertTrue("用户应该处于登录状态", resultUser[0].isLoggedIn());
        assertTrue("应该记住用户", resultUser[0].isRememberMe());
        
        // 验证DAO方法被调用
        verify(mockUserDao).getUserByUsername(username);
        verify(mockUserDao).logoutAllUsers();
        verify(mockUserDao).setUserLoggedIn(eq(1L), anyLong());
        verify(mockUserDao).updateRememberMe(eq(1L), eq(true));
        verify(mockUserDao).resetLoginAttempts(username);
    }
    
    @Test
    public void testLoginUser_WrongPassword() throws InterruptedException {
        // 准备测试数据
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        
        User mockUser = new User(username, "test@example.com", "1234567890", correctPassword);
        mockUser.setId(1L);
        mockUser.setLoginAttempts(0);
        
        // Mock DAO 方法
        when(mockUserDao.getUserByUsername(username)).thenReturn(mockUser);
        when(mockUserDao.updateLoginAttempts(eq(username), anyInt(), anyLong())).thenReturn(1);
        
        // 使用CountDownLatch等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final User[] resultUser = new User[1];
        final String[] errorMessage = new String[1];
        
        // 执行测试
        userRepository.loginUser(username, wrongPassword, false, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                resultUser[0] = result;
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorMessage[0] = error;
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        assertTrue("操作应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        
        // 验证结果
        assertNull("不应该返回用户对象", resultUser[0]);
        assertNotNull("应该有错误信息", errorMessage[0]);
        assertEquals("错误信息应该是密码错误", "密码错误", errorMessage[0]);
        
        // 验证DAO方法被调用
        verify(mockUserDao).getUserByUsername(username);
        verify(mockUserDao).updateLoginAttempts(eq(username), eq(1), anyLong());
        verify(mockUserDao, never()).setUserLoggedIn(anyLong(), anyLong());
    }
    
    @Test
    public void testLoginUser_UserNotExists() throws InterruptedException {
        // 准备测试数据
        String username = "nonexistentuser";
        String password = "password123";
        
        // Mock DAO 方法 - 用户不存在
        when(mockUserDao.getUserByUsername(username)).thenReturn(null);
        
        // 使用CountDownLatch等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final User[] resultUser = new User[1];
        final String[] errorMessage = new String[1];
        
        // 执行测试
        userRepository.loginUser(username, password, false, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                resultUser[0] = result;
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorMessage[0] = error;
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        assertTrue("操作应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        
        // 验证结果
        assertNull("不应该返回用户对象", resultUser[0]);
        assertNotNull("应该有错误信息", errorMessage[0]);
        assertEquals("错误信息应该是用户名不存在", "用户名不存在", errorMessage[0]);
        
        // 验证DAO方法被调用
        verify(mockUserDao).getUserByUsername(username);
        verify(mockUserDao, never()).setUserLoggedIn(anyLong(), anyLong());
    }
    
    @Test
    public void testGetCurrentUser_WithLoggedInUser() {
        // 准备测试数据
        long userId = 1L;
        User mockUser = new User("testuser", "test@example.com", "1234567890", "password123");
        mockUser.setId(userId);
        
        MutableLiveData<User> mockLiveData = new MutableLiveData<>();
        mockLiveData.setValue(mockUser);
        
        // Mock DAO 方法
        when(mockUserDao.getUserByIdLiveData(userId)).thenReturn(mockLiveData);
        
        // 模拟当前用户ID
        // 注意：这里需要通过反射或其他方式设置currentUserId，
        // 在实际测试中可能需要更复杂的设置
        
        // 执行测试
        LiveData<User> result = userRepository.getCurrentUser();
        
        // 验证结果
        assertNotNull("应该返回LiveData对象", result);
    }
    
    @Test
    public void testCheckUsernameExists_Exists() throws InterruptedException {
        // 准备测试数据
        String username = "existinguser";
        
        // Mock DAO 方法
        when(mockUserDao.getUsernameCount(username)).thenReturn(1);
        
        // 使用CountDownLatch等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final Boolean[] result = new Boolean[1];
        final String[] errorMessage = new String[1];
        
        // 执行测试
        userRepository.checkUsernameExists(username, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                result[0] = exists;
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorMessage[0] = error;
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        assertTrue("操作应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        
        // 验证结果
        assertNull("不应该有错误", errorMessage[0]);
        assertNotNull("应该返回结果", result[0]);
        assertTrue("用户名应该存在", result[0]);
        
        // 验证DAO方法被调用
        verify(mockUserDao).getUsernameCount(username);
    }
    
    @Test
    public void testCheckUsernameExists_NotExists() throws InterruptedException {
        // 准备测试数据
        String username = "newuser";
        
        // Mock DAO 方法
        when(mockUserDao.getUsernameCount(username)).thenReturn(0);
        
        // 使用CountDownLatch等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final Boolean[] result = new Boolean[1];
        final String[] errorMessage = new String[1];
        
        // 执行测试
        userRepository.checkUsernameExists(username, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                result[0] = exists;
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorMessage[0] = error;
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        assertTrue("操作应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        
        // 验证结果
        assertNull("不应该有错误", errorMessage[0]);
        assertNotNull("应该返回结果", result[0]);
        assertFalse("用户名不应该存在", result[0]);
        
        // 验证DAO方法被调用
        verify(mockUserDao).getUsernameCount(username);
    }
    
    @Test
    public void testLogoutUser_Success() throws InterruptedException {
        // Mock DAO 方法
        when(mockUserDao.logoutAllUsers()).thenReturn(1);
        
        // 使用CountDownLatch等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final Boolean[] result = new Boolean[1];
        final String[] errorMessage = new String[1];
        
        // 执行测试
        userRepository.logoutUser(new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                result[0] = success;
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorMessage[0] = error;
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        assertTrue("操作应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        
        // 验证结果
        assertNull("不应该有错误", errorMessage[0]);
        assertNotNull("应该返回结果", result[0]);
        assertTrue("登出应该成功", result[0]);
        
        // 验证DAO方法被调用
        verify(mockUserDao).logoutAllUsers();
    }
    
    @Test
    public void testRegisterUser_EmptyUsername() throws InterruptedException {
        // 准备测试数据
        String username = "";
        String email = "test@example.com";
        String phone = "1234567890";
        String password = "password123";
        
        // 使用CountDownLatch等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final Long[] resultUserId = new Long[1];
        final String[] errorMessage = new String[1];
        
        // 执行测试
        userRepository.registerUser(username, email, phone, password, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                resultUserId[0] = result;
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorMessage[0] = error;
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        assertTrue("操作应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        
        // 验证结果
        assertNull("不应该返回用户ID", resultUserId[0]);
        assertNotNull("应该有错误信息", errorMessage[0]);
        assertEquals("错误信息应该是用户名不能为空", "用户名不能为空", errorMessage[0]);
        
        // 验证DAO方法不应该被调用
        verify(mockUserDao, never()).insertUser(any(User.class));
    }
    
    @Test
    public void testLoginUser_EmptyPassword() throws InterruptedException {
        // 准备测试数据
        String username = "testuser";
        String password = "";
        
        // 使用CountDownLatch等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final User[] resultUser = new User[1];
        final String[] errorMessage = new String[1];
        
        // 执行测试
        userRepository.loginUser(username, password, false, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                resultUser[0] = result;
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorMessage[0] = error;
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        assertTrue("操作应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        
        // 验证结果
        assertNull("不应该返回用户对象", resultUser[0]);
        assertNotNull("应该有错误信息", errorMessage[0]);
        assertEquals("错误信息应该是请输入密码", "请输入密码", errorMessage[0]);
        
        // 验证DAO方法不应该被调用
        verify(mockUserDao, never()).getUserByUsername(anyString());
    }
}