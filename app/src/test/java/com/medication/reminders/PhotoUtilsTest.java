package com.medication.reminders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.medication.reminders.utils.PhotoUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Unit tests for PhotoUtils class
 * Tests photo saving, loading, deletion, and utility methods
 */
@RunWith(MockitoJUnitRunner.class)
public class PhotoUtilsTest {

    @Mock
    private Context mockContext;

    @Mock
    private Bitmap mockBitmap;

    @Mock
    private File mockFilesDir;

    @Mock
    private File mockPhotosDir;

    @Mock
    private File mockPhotoFile;

    @Mock
    private FileOutputStream mockFileOutputStream;

    private static final String TEST_FILENAME = "test_photo.jpg";
    private static final String TEST_PATH = "/data/data/com.medication.reminders/files/medication_photos/test_photo.jpg";

    @Before
    public void setUp() {
        // Setup mock context
        when(mockContext.getFilesDir()).thenReturn(mockFilesDir);
    }

    @Test
    public void testSavePhotoToInternalStorage_Success() throws IOException {
        // Arrange
        when(mockFilesDir.getAbsolutePath()).thenReturn("/data/data/com.medication.reminders/files");
        when(mockPhotosDir.exists()).thenReturn(false);
        when(mockPhotosDir.mkdirs()).thenReturn(true);
        when(mockPhotoFile.getAbsolutePath()).thenReturn(TEST_PATH);
        when(mockBitmap.compress(eq(Bitmap.CompressFormat.JPEG), eq(85), any(FileOutputStream.class)))
                .thenReturn(true);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(mockFilesDir, "medication_photos"))
                    .thenReturn(mockPhotosDir);
            mockedFile.when(() -> new File(mockPhotosDir, TEST_FILENAME))
                    .thenReturn(mockPhotoFile);

            try (MockedStatic<FileOutputStream> mockedFOS = mockStatic(FileOutputStream.class)) {
                mockedFOS.when(() -> new FileOutputStream(mockPhotoFile))
                        .thenReturn(mockFileOutputStream);

                // Act
                String result = PhotoUtils.savePhotoToInternalStorage(mockContext, mockBitmap, TEST_FILENAME);

                // Assert
                assertEquals(TEST_PATH, result);
                verify(mockPhotosDir).mkdirs();
                verify(mockBitmap).compress(eq(Bitmap.CompressFormat.JPEG), eq(85), eq(mockFileOutputStream));
            }
        }
    }

    @Test
    public void testSavePhotoToInternalStorage_NullContext() {
        // Act
        String result = PhotoUtils.savePhotoToInternalStorage(null, mockBitmap, TEST_FILENAME);

        // Assert
        assertNull(result);
    }

    @Test
    public void testSavePhotoToInternalStorage_NullBitmap() {
        // Act
        String result = PhotoUtils.savePhotoToInternalStorage(mockContext, null, TEST_FILENAME);

        // Assert
        assertNull(result);
    }

    @Test
    public void testSavePhotoToInternalStorage_DirectoryCreationFails() {
        // Arrange
        when(mockPhotosDir.exists()).thenReturn(false);
        when(mockPhotosDir.mkdirs()).thenReturn(false);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(mockFilesDir, "medication_photos"))
                    .thenReturn(mockPhotosDir);

            // Act
            String result = PhotoUtils.savePhotoToInternalStorage(mockContext, mockBitmap, TEST_FILENAME);

            // Assert
            assertNull(result);
        }
    }

    @Test
    public void testSavePhotoToInternalStorage_CompressionFails() throws IOException {
        // Arrange
        when(mockPhotosDir.exists()).thenReturn(true);
        when(mockPhotoFile.getAbsolutePath()).thenReturn(TEST_PATH);
        when(mockBitmap.compress(eq(Bitmap.CompressFormat.JPEG), eq(85), any(FileOutputStream.class)))
                .thenReturn(false);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(mockFilesDir, "medication_photos"))
                    .thenReturn(mockPhotosDir);
            mockedFile.when(() -> new File(mockPhotosDir, TEST_FILENAME))
                    .thenReturn(mockPhotoFile);

            try (MockedStatic<FileOutputStream> mockedFOS = mockStatic(FileOutputStream.class)) {
                mockedFOS.when(() -> new FileOutputStream(mockPhotoFile))
                        .thenReturn(mockFileOutputStream);

                // Act
                String result = PhotoUtils.savePhotoToInternalStorage(mockContext, mockBitmap, TEST_FILENAME);

                // Assert
                assertNull(result);
            }
        }
    }

    @Test
    public void testSavePhotoToInternalStorage_GeneratesFilenameWhenNull() throws IOException {
        // Arrange
        when(mockPhotosDir.exists()).thenReturn(true);
        when(mockPhotoFile.getAbsolutePath()).thenReturn(TEST_PATH);
        when(mockBitmap.compress(eq(Bitmap.CompressFormat.JPEG), eq(85), any(FileOutputStream.class)))
                .thenReturn(true);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(mockFilesDir, "medication_photos"))
                    .thenReturn(mockPhotosDir);
            mockedFile.when(() -> new File(eq(mockPhotosDir), anyString()))
                    .thenReturn(mockPhotoFile);

            try (MockedStatic<FileOutputStream> mockedFOS = mockStatic(FileOutputStream.class)) {
                mockedFOS.when(() -> new FileOutputStream(mockPhotoFile))
                        .thenReturn(mockFileOutputStream);

                // Act
                String result = PhotoUtils.savePhotoToInternalStorage(mockContext, mockBitmap, null);

                // Assert
                assertEquals(TEST_PATH, result);
            }
        }
    }

    @Test
    public void testSavePhotoToInternalStorage_AddsExtensionWhenMissing() throws IOException {
        // Arrange
        String filenameWithoutExtension = "test_photo";
        when(mockPhotosDir.exists()).thenReturn(true);
        when(mockPhotoFile.getAbsolutePath()).thenReturn(TEST_PATH);
        when(mockBitmap.compress(eq(Bitmap.CompressFormat.JPEG), eq(85), any(FileOutputStream.class)))
                .thenReturn(true);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(mockFilesDir, "medication_photos"))
                    .thenReturn(mockPhotosDir);
            mockedFile.when(() -> new File(mockPhotosDir, "test_photo.jpg"))
                    .thenReturn(mockPhotoFile);

            try (MockedStatic<FileOutputStream> mockedFOS = mockStatic(FileOutputStream.class)) {
                mockedFOS.when(() -> new FileOutputStream(mockPhotoFile))
                        .thenReturn(mockFileOutputStream);

                // Act
                String result = PhotoUtils.savePhotoToInternalStorage(mockContext, mockBitmap, filenameWithoutExtension);

                // Assert
                assertEquals(TEST_PATH, result);
            }
        }
    }

    @Test
    public void testLoadPhotoFromPath_Success() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(true);
        when(mockPhotoFile.canRead()).thenReturn(true);

        try (MockedStatic<File> mockedFile = mockStatic(File.class);
             MockedStatic<BitmapFactory> mockedBitmapFactory = mockStatic(BitmapFactory.class)) {
            
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);
            mockedBitmapFactory.when(() -> BitmapFactory.decodeFile(TEST_PATH)).thenReturn(mockBitmap);

            // Act
            Bitmap result = PhotoUtils.loadPhotoFromPath(TEST_PATH);

            // Assert
            assertEquals(mockBitmap, result);
        }
    }

    @Test
    public void testLoadPhotoFromPath_NullPath() {
        // Act
        Bitmap result = PhotoUtils.loadPhotoFromPath(null);

        // Assert
        assertNull(result);
    }

    @Test
    public void testLoadPhotoFromPath_EmptyPath() {
        // Act
        Bitmap result = PhotoUtils.loadPhotoFromPath("");

        // Assert
        assertNull(result);
    }

    @Test
    public void testLoadPhotoFromPath_FileDoesNotExist() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(false);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            Bitmap result = PhotoUtils.loadPhotoFromPath(TEST_PATH);

            // Assert
            assertNull(result);
        }
    }

    @Test
    public void testLoadPhotoFromPath_FileNotReadable() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(true);
        when(mockPhotoFile.canRead()).thenReturn(false);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            Bitmap result = PhotoUtils.loadPhotoFromPath(TEST_PATH);

            // Assert
            assertNull(result);
        }
    }

    @Test
    public void testLoadPhotoFromPath_BitmapFactoryReturnsNull() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(true);
        when(mockPhotoFile.canRead()).thenReturn(true);

        try (MockedStatic<File> mockedFile = mockStatic(File.class);
             MockedStatic<BitmapFactory> mockedBitmapFactory = mockStatic(BitmapFactory.class)) {
            
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);
            mockedBitmapFactory.when(() -> BitmapFactory.decodeFile(TEST_PATH)).thenReturn(null);

            // Act
            Bitmap result = PhotoUtils.loadPhotoFromPath(TEST_PATH);

            // Assert
            assertNull(result);
        }
    }

    @Test
    public void testGenerateUniqueFilename() {
        // Act
        String filename1 = PhotoUtils.generateUniqueFilename();
        String filename2 = PhotoUtils.generateUniqueFilename();

        // Assert
        assertNotNull(filename1);
        assertNotNull(filename2);
        assertNotEquals(filename1, filename2); // Should be unique
        assertTrue(filename1.startsWith("medication_photo_"));
        assertTrue(filename1.endsWith(".jpg"));
        assertTrue(filename2.startsWith("medication_photo_"));
        assertTrue(filename2.endsWith(".jpg"));
    }

    @Test
    public void testDeletePhoto_Success() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(true);
        when(mockPhotoFile.delete()).thenReturn(true);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            boolean result = PhotoUtils.deletePhoto(TEST_PATH);

            // Assert
            assertTrue(result);
            verify(mockPhotoFile).delete();
        }
    }

    @Test
    public void testDeletePhoto_NullPath() {
        // Act
        boolean result = PhotoUtils.deletePhoto(null);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testDeletePhoto_EmptyPath() {
        // Act
        boolean result = PhotoUtils.deletePhoto("");

        // Assert
        assertFalse(result);
    }

    @Test
    public void testDeletePhoto_FileDoesNotExist() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(false);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            boolean result = PhotoUtils.deletePhoto(TEST_PATH);

            // Assert
            assertTrue(result); // Should return true if file doesn't exist (effectively deleted)
        }
    }

    @Test
    public void testDeletePhoto_DeleteFails() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(true);
        when(mockPhotoFile.delete()).thenReturn(false);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            boolean result = PhotoUtils.deletePhoto(TEST_PATH);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    public void testPhotoExists_Success() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(true);
        when(mockPhotoFile.canRead()).thenReturn(true);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            boolean result = PhotoUtils.photoExists(TEST_PATH);

            // Assert
            assertTrue(result);
        }
    }

    @Test
    public void testPhotoExists_NullPath() {
        // Act
        boolean result = PhotoUtils.photoExists(null);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testPhotoExists_EmptyPath() {
        // Act
        boolean result = PhotoUtils.photoExists("");

        // Assert
        assertFalse(result);
    }

    @Test
    public void testPhotoExists_FileDoesNotExist() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(false);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            boolean result = PhotoUtils.photoExists(TEST_PATH);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    public void testPhotoExists_FileNotReadable() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(true);
        when(mockPhotoFile.canRead()).thenReturn(false);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            boolean result = PhotoUtils.photoExists(TEST_PATH);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    public void testGetPhotoSize_Success() {
        // Arrange
        long expectedSize = 1024L;
        when(mockPhotoFile.exists()).thenReturn(true);
        when(mockPhotoFile.canRead()).thenReturn(true);
        when(mockPhotoFile.length()).thenReturn(expectedSize);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            long result = PhotoUtils.getPhotoSize(TEST_PATH);

            // Assert
            assertEquals(expectedSize, result);
        }
    }

    @Test
    public void testGetPhotoSize_NullPath() {
        // Act
        long result = PhotoUtils.getPhotoSize(null);

        // Assert
        assertEquals(-1L, result);
    }

    @Test
    public void testGetPhotoSize_EmptyPath() {
        // Act
        long result = PhotoUtils.getPhotoSize("");

        // Assert
        assertEquals(-1L, result);
    }

    @Test
    public void testGetPhotoSize_FileDoesNotExist() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(false);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            long result = PhotoUtils.getPhotoSize(TEST_PATH);

            // Assert
            assertEquals(-1L, result);
        }
    }

    @Test
    public void testGetPhotoSize_FileNotReadable() {
        // Arrange
        when(mockPhotoFile.exists()).thenReturn(true);
        when(mockPhotoFile.canRead()).thenReturn(false);

        try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
            mockedFile.when(() -> new File(TEST_PATH)).thenReturn(mockPhotoFile);

            // Act
            long result = PhotoUtils.getPhotoSize(TEST_PATH);

            // Assert
            assertEquals(-1L, result);
        }
    }

    @Test
    public void testScalePhoto_NullBitmap() {
        // Act
        Bitmap result = PhotoUtils.scalePhoto(null, 100, 100);

        // Assert
        assertNull(result);
    }

    @Test
    public void testScalePhoto_NoScalingNeeded() {
        // Arrange
        when(mockBitmap.getWidth()).thenReturn(50);
        when(mockBitmap.getHeight()).thenReturn(50);

        // Act
        Bitmap result = PhotoUtils.scalePhoto(mockBitmap, 100, 100);

        // Assert
        assertEquals(mockBitmap, result); // Should return original bitmap
    }

    @Test
    public void testScalePhoto_ScalingNeeded() {
        // Arrange
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(mockBitmap.getWidth()).thenReturn(200);
        when(mockBitmap.getHeight()).thenReturn(200);

        try (MockedStatic<Bitmap> mockedBitmap = mockStatic(Bitmap.class)) {
            mockedBitmap.when(() -> Bitmap.createScaledBitmap(mockBitmap, 100, 100, true))
                    .thenReturn(scaledBitmap);

            // Act
            Bitmap result = PhotoUtils.scalePhoto(mockBitmap, 100, 100);

            // Assert
            assertEquals(scaledBitmap, result);
        }
    }

    @Test
    public void testScalePhoto_ScalingWithAspectRatio() {
        // Arrange
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(mockBitmap.getWidth()).thenReturn(200);
        when(mockBitmap.getHeight()).thenReturn(100);

        try (MockedStatic<Bitmap> mockedBitmap = mockStatic(Bitmap.class)) {
            mockedBitmap.when(() -> Bitmap.createScaledBitmap(mockBitmap, 100, 50, true))
                    .thenReturn(scaledBitmap);

            // Act
            Bitmap result = PhotoUtils.scalePhoto(mockBitmap, 100, 100);

            // Assert
            assertEquals(scaledBitmap, result);
        }
    }

    @Test
    public void testScalePhoto_OutOfMemoryError() {
        // Arrange
        when(mockBitmap.getWidth()).thenReturn(200);
        when(mockBitmap.getHeight()).thenReturn(200);

        try (MockedStatic<Bitmap> mockedBitmap = mockStatic(Bitmap.class)) {
            mockedBitmap.when(() -> Bitmap.createScaledBitmap(mockBitmap, 100, 100, true))
                    .thenThrow(new OutOfMemoryError("Test OOM"));

            // Act
            Bitmap result = PhotoUtils.scalePhoto(mockBitmap, 100, 100);

            // Assert
            assertEquals(mockBitmap, result); // Should return original bitmap on OOM
        }
    }
}