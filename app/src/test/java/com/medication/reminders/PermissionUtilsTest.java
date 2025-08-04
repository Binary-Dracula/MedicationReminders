package com.medication.reminders;

import android.Manifest;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.request.PermissionBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PermissionUtils class
 * Tests permission status checking methods and null parameter handling
 * Note: Due to the complexity of mocking PermissionX library internals,
 * these tests focus on null parameter handling and permission status checking
 */
@RunWith(MockitoJUnitRunner.class)
public class PermissionUtilsTest {

    @Mock
    private FragmentActivity mockActivity;

    @Mock
    private Context mockContext;

    @Mock
    private PermissionUtils.PermissionCallback mockCallback;

    @Mock
    private PermissionUtils.SimplePermissionCallback mockSimpleCallback;

    @Mock
    private PermissionBuilder mockPermissionBuilder;

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final String STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

    @Before
    public void setUp() {
        // Setup mock activity with string resources
        when(mockActivity.getString(R.string.camera_permission_reason))
                .thenReturn("需要相机权限来拍摄药物照片");
        when(mockActivity.getString(R.string.storage_permission_reason))
                .thenReturn("需要存储权限来访问相册");
        when(mockActivity.getString(R.string.camera_permission_settings_message))
                .thenReturn("请在设置中手动授权相机权限");
        when(mockActivity.getString(R.string.storage_permission_settings_message))
                .thenReturn("请在设置中手动授权存储权限");
        when(mockActivity.getString(R.string.confirm)).thenReturn("确定");
        when(mockActivity.getString(R.string.cancel)).thenReturn("取消");
        when(mockActivity.getString(R.string.go_to_settings)).thenReturn("去设置");
    }

    @Test
    public void testRequestCameraPermission_CallsPermissionX() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Act
            PermissionUtils.requestCameraPermission(mockActivity, mockCallback);

            // Assert - verify that PermissionX.init is called with the correct activity
            mockedPermissionX.verify(() -> PermissionX.init(mockActivity));
        }
    }

    @Test
    public void testRequestCameraPermission_NullActivity() {
        // Act
        PermissionUtils.requestCameraPermission(null, mockCallback);

        // Assert
        verify(mockCallback).onResult(false, null, null);
    }

    @Test
    public void testRequestCameraPermission_NullCallback() {
        // Act - should not throw exception even with null callback
        try {
            PermissionUtils.requestCameraPermission(mockActivity, null);
            // If we reach here, the method handled null callback gracefully
            assertTrue("Method should handle null callback without throwing exception", true);
        } catch (Exception e) {
            fail("Method should not throw exception with null callback: " + e.getMessage());
        }
    }

    @Test
    public void testRequestStoragePermission_CallsPermissionX() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Act
            PermissionUtils.requestStoragePermission(mockActivity, mockCallback);

            // Assert - verify that PermissionX.init is called with the correct activity
            mockedPermissionX.verify(() -> PermissionX.init(mockActivity));
        }
    }

    @Test
    public void testRequestStoragePermission_NullActivity() {
        // Act
        PermissionUtils.requestStoragePermission(null, mockCallback);

        // Assert
        verify(mockCallback).onResult(false, null, null);
    }

    @Test
    public void testRequestStoragePermission_NullCallback() {
        // Act - should not throw exception even with null callback
        try {
            PermissionUtils.requestStoragePermission(mockActivity, null);
            // If we reach here, the method handled null callback gracefully
            assertTrue("Method should handle null callback without throwing exception", true);
        } catch (Exception e) {
            fail("Method should not throw exception with null callback: " + e.getMessage());
        }
    }

    @Test
    public void testRequestCameraAndStoragePermissions_CallsPermissionX() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Act
            PermissionUtils.requestCameraAndStoragePermissions(mockActivity, mockCallback);

            // Assert - verify that PermissionX.init is called with the correct activity
            mockedPermissionX.verify(() -> PermissionX.init(mockActivity));
        }
    }

    @Test
    public void testRequestCameraAndStoragePermissions_NullActivity() {
        // Act
        PermissionUtils.requestCameraAndStoragePermissions(null, mockCallback);

        // Assert
        verify(mockCallback).onResult(false, null, null);
    }

    @Test
    public void testRequestCameraAndStoragePermissions_NullCallback() {
        // Act - should not throw exception even with null callback
        try {
            PermissionUtils.requestCameraAndStoragePermissions(mockActivity, null);
            // If we reach here, the method handled null callback gracefully
            assertTrue("Method should handle null callback without throwing exception", true);
        } catch (Exception e) {
            fail("Method should not throw exception with null callback: " + e.getMessage());
        }
    }

    @Test
    public void testIsCameraPermissionGranted_True() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Arrange
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION))
                    .thenReturn(true);

            // Act
            boolean result = PermissionUtils.isCameraPermissionGranted(mockContext);

            // Assert
            assertTrue(result);
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION));
        }
    }

    @Test
    public void testIsCameraPermissionGranted_False() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Arrange
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION))
                    .thenReturn(false);

            // Act
            boolean result = PermissionUtils.isCameraPermissionGranted(mockContext);

            // Assert
            assertFalse(result);
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION));
        }
    }

    @Test
    public void testIsCameraPermissionGranted_NullContext() {
        // Act
        boolean result = PermissionUtils.isCameraPermissionGranted(null);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testIsStoragePermissionGranted_True() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Arrange
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION))
                    .thenReturn(true);

            // Act
            boolean result = PermissionUtils.isStoragePermissionGranted(mockContext);

            // Assert
            assertTrue(result);
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION));
        }
    }

    @Test
    public void testIsStoragePermissionGranted_False() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Arrange
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION))
                    .thenReturn(false);

            // Act
            boolean result = PermissionUtils.isStoragePermissionGranted(mockContext);

            // Assert
            assertFalse(result);
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION));
        }
    }

    @Test
    public void testIsStoragePermissionGranted_NullContext() {
        // Act
        boolean result = PermissionUtils.isStoragePermissionGranted(null);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testAreCameraAndStoragePermissionsGranted_BothTrue() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Arrange
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION))
                    .thenReturn(true);
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION))
                    .thenReturn(true);

            // Act
            boolean result = PermissionUtils.areCameraAndStoragePermissionsGranted(mockContext);

            // Assert
            assertTrue(result);
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION));
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION));
        }
    }

    @Test
    public void testAreCameraAndStoragePermissionsGranted_CameraFalse() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Arrange
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION))
                    .thenReturn(false);
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION))
                    .thenReturn(true);

            // Act
            boolean result = PermissionUtils.areCameraAndStoragePermissionsGranted(mockContext);

            // Assert
            assertFalse(result);
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION));
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION));
        }
    }

    @Test
    public void testAreCameraAndStoragePermissionsGranted_StorageFalse() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Arrange
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION))
                    .thenReturn(true);
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION))
                    .thenReturn(false);

            // Act
            boolean result = PermissionUtils.areCameraAndStoragePermissionsGranted(mockContext);

            // Assert
            assertFalse(result);
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION));
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION));
        }
    }

    @Test
    public void testAreCameraAndStoragePermissionsGranted_BothFalse() {
        try (MockedStatic<PermissionX> mockedPermissionX = mockStatic(PermissionX.class)) {
            // Arrange
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION))
                    .thenReturn(false);
            mockedPermissionX.when(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION))
                    .thenReturn(false);

            // Act
            boolean result = PermissionUtils.areCameraAndStoragePermissionsGranted(mockContext);

            // Assert
            assertFalse(result);
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, CAMERA_PERMISSION));
            mockedPermissionX.verify(() -> PermissionX.isGranted(mockContext, STORAGE_PERMISSION));
        }
    }

    @Test
    public void testAreCameraAndStoragePermissionsGranted_NullContext() {
        // Act
        boolean result = PermissionUtils.areCameraAndStoragePermissionsGranted(null);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testRequestCameraPermissionSimple_NullActivity() {
        // Act
        PermissionUtils.requestCameraPermissionSimple(null, mockSimpleCallback);

        // Assert - callback should be called with denied result
        verify(mockSimpleCallback).onDenied();
        verify(mockSimpleCallback, never()).onGranted();
    }

    @Test
    public void testRequestCameraPermissionSimple_NullCallback() {
        // Act - should not throw exception even with null callback
        try {
            PermissionUtils.requestCameraPermissionSimple(mockActivity, null);
            // If we reach here, the method handled null callback gracefully
            assertTrue("Method should handle null callback without throwing exception", true);
        } catch (Exception e) {
            fail("Method should not throw exception with null callback: " + e.getMessage());
        }
    }

    @Test
    public void testRequestStoragePermissionSimple_NullActivity() {
        // Act
        PermissionUtils.requestStoragePermissionSimple(null, mockSimpleCallback);

        // Assert - callback should be called with denied result
        verify(mockSimpleCallback).onDenied();
        verify(mockSimpleCallback, never()).onGranted();
    }

    @Test
    public void testRequestStoragePermissionSimple_NullCallback() {
        // Act - should not throw exception even with null callback
        try {
            PermissionUtils.requestStoragePermissionSimple(mockActivity, null);
            // If we reach here, the method handled null callback gracefully
            assertTrue("Method should handle null callback without throwing exception", true);
        } catch (Exception e) {
            fail("Method should not throw exception with null callback: " + e.getMessage());
        }
    }

    @Test
    public void testPermissionCallbackInterface() {
        // Test that the PermissionCallback interface can be implemented
        PermissionUtils.PermissionCallback callback = new PermissionUtils.PermissionCallback() {
            @Override
            public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                // Implementation for testing
                assertTrue("Callback should be callable", true);
            }
        };

        // Act
        callback.onResult(true, Collections.singletonList(CAMERA_PERMISSION), Collections.emptyList());

        // Assert - no exception should be thrown
        assertNotNull("Callback should not be null", callback);
    }

    @Test
    public void testSimplePermissionCallbackInterface() {
        // Test that the SimplePermissionCallback interface can be implemented
        PermissionUtils.SimplePermissionCallback callback = new PermissionUtils.SimplePermissionCallback() {
            @Override
            public void onGranted() {
                // Implementation for testing
            }

            @Override
            public void onDenied() {
                // Implementation for testing
            }
        };

        // Act
        callback.onGranted();
        callback.onDenied();

        // Assert - no exception should be thrown
        assertNotNull("Callback should not be null", callback);
    }
}