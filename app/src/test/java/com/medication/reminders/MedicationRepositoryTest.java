package com.medication.reminders;

import android.app.Application;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MedicationRepository
 * Tests database operations and validation logic
 */
@RunWith(MockitoJUnitRunner.class)
public class MedicationRepositoryTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private Application mockApplication;
    
    @Mock
    private MedicationDatabase mockDatabase;
    
    @Mock
    private MedicationDao mockDao;
    
    private MedicationRepository repository;
    private MutableLiveData<List<MedicationInfo>> mockAllMedications;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mock LiveData
        mockAllMedications = new MutableLiveData<>();
        List<MedicationInfo> testMedications = new ArrayList<>();
        mockAllMedications.setValue(testMedications);
        
        // Setup mock database and DAO
        when(mockDao.getAllMedications()).thenReturn(mockAllMedications);
        when(mockDatabase.medicationDao()).thenReturn(mockDao);
        
        // Mock static database instance
        try (MockedStatic<MedicationDatabase> mockedStatic = mockStatic(MedicationDatabase.class)) {
            mockedStatic.when(() -> MedicationDatabase.getDatabase(any(Application.class)))
                       .thenReturn(mockDatabase);
            
            repository = new MedicationRepository(mockApplication);
        }
    }
    
    @Test
    public void testGetAllMedications() {
        // Act
        LiveData<List<MedicationInfo>> result = repository.getAllMedications();
        
        // Assert
        assertNotNull(result);
        assertEquals(mockAllMedications, result);
        verify(mockDao).getAllMedications();
    }
    
    @Test
    public void testGetMedicationById() {
        // Arrange
        long testId = 1L;
        MutableLiveData<MedicationInfo> mockMedication = new MutableLiveData<>();
        when(mockDao.getMedicationById(testId)).thenReturn(mockMedication);
        
        // Act
        LiveData<MedicationInfo> result = repository.getMedicationById(testId);
        
        // Assert
        assertNotNull(result);
        assertEquals(mockMedication, result);
        verify(mockDao).getMedicationById(testId);
    }
    
    @Test
    public void testInsertMedication_Success() throws InterruptedException {
        // Arrange
        MedicationInfo medication = createTestMedication();
        when(mockDao.insertMedication(any(MedicationInfo.class))).thenReturn(1L);
        when(mockDao.getMedicationCountByName(medication.getName())).thenReturn(0);
        
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] callbackCalled = {false};
        final long[] returnedId = {-1};
        
        MedicationRepository.InsertCallback callback = new MedicationRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                callbackCalled[0] = true;
                returnedId[0] = id;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMessage) {
                fail("Should not call onError: " + errorMessage);
                latch.countDown();
            }
            
            @Override
            public void onDuplicateFound(String medicationName) {
                fail("Should not call onDuplicateFound");
                latch.countDown();
            }
        };
        
        // Act
        repository.insertMedication(medication, callback);
        
        // Assert
        assertTrue("Callback should be called within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Success callback should be called", callbackCalled[0]);
        assertEquals("Should return correct ID", 1L, returnedId[0]);
        
        // Verify timestamps were set
        assertTrue("Created timestamp should be set", medication.getCreatedAt() > 0);
        assertTrue("Updated timestamp should be set", medication.getUpdatedAt() > 0);
        
        verify(mockDao).insertMedication(medication);
        verify(mockDao).getMedicationCountByName(medication.getName());
    }
    
    @Test
    public void testInsertMedication_ValidationError() throws InterruptedException {
        // Arrange
        MedicationInfo invalidMedication = new MedicationInfo();
        // Leave name empty to trigger validation error
        invalidMedication.setColor("White");
        invalidMedication.setDosageForm("Tablet");
        
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] errorCalled = {false};
        final String[] errorMessage = {null};
        
        MedicationRepository.InsertCallback callback = new MedicationRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                fail("Should not call onSuccess");
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorCalled[0] = true;
                errorMessage[0] = error;
                latch.countDown();
            }
            
            @Override
            public void onDuplicateFound(String medicationName) {
                fail("Should not call onDuplicateFound");
                latch.countDown();
            }
        };
        
        // Act
        repository.insertMedication(invalidMedication, callback);
        
        // Assert
        assertTrue("Callback should be called within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Error callback should be called", errorCalled[0]);
        assertEquals("Should return validation error", "药物名称是必需的", errorMessage[0]);
        
        // Verify DAO methods were not called
        verify(mockDao, never()).insertMedication(any(MedicationInfo.class));
        verify(mockDao, never()).getMedicationCountByName(anyString());
    }
    
    @Test
    public void testInsertMedication_DuplicateFound() throws InterruptedException {
        // Arrange
        MedicationInfo medication = createTestMedication();
        when(mockDao.getMedicationCountByName(medication.getName())).thenReturn(1);
        
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] duplicateCalled = {false};
        final String[] duplicateName = {null};
        
        MedicationRepository.InsertCallback callback = new MedicationRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                fail("Should not call onSuccess");
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMessage) {
                fail("Should not call onError: " + errorMessage);
                latch.countDown();
            }
            
            @Override
            public void onDuplicateFound(String medicationName) {
                duplicateCalled[0] = true;
                duplicateName[0] = medicationName;
                latch.countDown();
            }
        };
        
        // Act
        repository.insertMedication(medication, callback);
        
        // Assert
        assertTrue("Callback should be called within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Duplicate callback should be called", duplicateCalled[0]);
        assertEquals("Should return correct medication name", medication.getName(), duplicateName[0]);
        
        verify(mockDao).getMedicationCountByName(medication.getName());
        verify(mockDao, never()).insertMedication(any(MedicationInfo.class));
    }
    
    @Test
    public void testInsertMedication_WithDuplicateOverride() throws InterruptedException {
        // Arrange
        MedicationInfo medication = createTestMedication();
        when(mockDao.insertMedication(any(MedicationInfo.class))).thenReturn(1L);
        when(mockDao.getMedicationCountByName(medication.getName())).thenReturn(1);
        
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] successCalled = {false};
        
        MedicationRepository.InsertCallback callback = new MedicationRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                successCalled[0] = true;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMessage) {
                fail("Should not call onError: " + errorMessage);
                latch.countDown();
            }
            
            @Override
            public void onDuplicateFound(String medicationName) {
                fail("Should not call onDuplicateFound");
                latch.countDown();
            }
        };
        
        // Act
        repository.insertMedication(medication, true, callback);
        
        // Assert
        assertTrue("Callback should be called within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Success callback should be called", successCalled[0]);
        
        verify(mockDao).insertMedication(medication);
        // Should not check for duplicates when allowDuplicate is true
        verify(mockDao, never()).getMedicationCountByName(anyString());
    }
    
    @Test
    public void testInsertMedication_DatabaseError() throws InterruptedException {
        // Arrange
        MedicationInfo medication = createTestMedication();
        when(mockDao.getMedicationCountByName(medication.getName())).thenReturn(0);
        when(mockDao.insertMedication(any(MedicationInfo.class)))
            .thenThrow(new RuntimeException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] errorCalled = {false};
        final String[] errorMessage = {null};
        
        MedicationRepository.InsertCallback callback = new MedicationRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                fail("Should not call onSuccess");
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorCalled[0] = true;
                errorMessage[0] = error;
                latch.countDown();
            }
            
            @Override
            public void onDuplicateFound(String medicationName) {
                fail("Should not call onDuplicateFound");
                latch.countDown();
            }
        };
        
        // Act
        repository.insertMedication(medication, callback);
        
        // Assert
        assertTrue("Callback should be called within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Error callback should be called", errorCalled[0]);
        assertTrue("Should contain error message", errorMessage[0].contains("数据库保存失败"));
    }
    
    @Test
    public void testUpdateMedication_Success() throws InterruptedException {
        // Arrange
        MedicationInfo medication = createTestMedication();
        medication.setId(1L);
        when(mockDao.updateMedication(any(MedicationInfo.class))).thenReturn(1);
        
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] successCalled = {false};
        
        MedicationRepository.UpdateCallback callback = new MedicationRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                successCalled[0] = true;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMessage) {
                fail("Should not call onError: " + errorMessage);
                latch.countDown();
            }
        };
        
        // Act
        repository.updateMedication(medication, callback);
        
        // Assert
        assertTrue("Callback should be called within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Success callback should be called", successCalled[0]);
        assertTrue("Updated timestamp should be set", medication.getUpdatedAt() > 0);
        
        verify(mockDao).updateMedication(medication);
    }
    
    @Test
    public void testUpdateMedication_NotFound() throws InterruptedException {
        // Arrange
        MedicationInfo medication = createTestMedication();
        medication.setId(1L);
        when(mockDao.updateMedication(any(MedicationInfo.class))).thenReturn(0);
        
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] errorCalled = {false};
        final String[] errorMessage = {null};
        
        MedicationRepository.UpdateCallback callback = new MedicationRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                fail("Should not call onSuccess");
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                errorCalled[0] = true;
                errorMessage[0] = error;
                latch.countDown();
            }
        };
        
        // Act
        repository.updateMedication(medication, callback);
        
        // Assert
        assertTrue("Callback should be called within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Error callback should be called", errorCalled[0]);
        assertEquals("Should return not found error", "未找到要更新的药物记录", errorMessage[0]);
    }
    
    @Test
    public void testDeleteMedication_Success() throws InterruptedException {
        // Arrange
        MedicationInfo medication = createTestMedication();
        when(mockDao.deleteMedication(any(MedicationInfo.class))).thenReturn(1);
        
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] successCalled = {false};
        
        MedicationRepository.DeleteCallback callback = new MedicationRepository.DeleteCallback() {
            @Override
            public void onSuccess() {
                successCalled[0] = true;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMessage) {
                fail("Should not call onError: " + errorMessage);
                latch.countDown();
            }
        };
        
        // Act
        repository.deleteMedication(medication, callback);
        
        // Assert
        assertTrue("Callback should be called within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Success callback should be called", successCalled[0]);
        
        verify(mockDao).deleteMedication(medication);
    }
    
    @Test
    public void testDeleteMedicationById_Success() throws InterruptedException {
        // Arrange
        long medicationId = 1L;
        when(mockDao.deleteMedicationById(medicationId)).thenReturn(1);
        
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] successCalled = {false};
        
        MedicationRepository.DeleteCallback callback = new MedicationRepository.DeleteCallback() {
            @Override
            public void onSuccess() {
                successCalled[0] = true;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMessage) {
                fail("Should not call onError: " + errorMessage);
                latch.countDown();
            }
        };
        
        // Act
        repository.deleteMedicationById(medicationId, callback);
        
        // Assert
        assertTrue("Callback should be called within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Success callback should be called", successCalled[0]);
        
        verify(mockDao).deleteMedicationById(medicationId);
    }
    
    @Test
    public void testIsMedicationNameExists_True() {
        // Arrange
        String medicationName = "Test Medication";
        when(mockDao.getMedicationCountByName(medicationName)).thenReturn(1);
        
        // Act
        boolean result = repository.isMedicationNameExists(medicationName);
        
        // Assert
        assertTrue("Should return true when medication exists", result);
        verify(mockDao).getMedicationCountByName(medicationName);
    }
    
    @Test
    public void testIsMedicationNameExists_False() {
        // Arrange
        String medicationName = "Non-existent Medication";
        when(mockDao.getMedicationCountByName(medicationName)).thenReturn(0);
        
        // Act
        boolean result = repository.isMedicationNameExists(medicationName);
        
        // Assert
        assertFalse("Should return false when medication doesn't exist", result);
        verify(mockDao).getMedicationCountByName(medicationName);
    }
    
    @Test
    public void testIsMedicationNameExists_NullName() {
        // Act
        boolean result = repository.isMedicationNameExists(null);
        
        // Assert
        assertFalse("Should return false for null name", result);
        verify(mockDao, never()).getMedicationCountByName(anyString());
    }
    
    @Test
    public void testIsMedicationNameExists_EmptyName() {
        // Act
        boolean result = repository.isMedicationNameExists("");
        
        // Assert
        assertFalse("Should return false for empty name", result);
        verify(mockDao, never()).getMedicationCountByName(anyString());
    }
    
    @Test
    public void testIsMedicationNameExists_DatabaseError() {
        // Arrange
        String medicationName = "Test Medication";
        when(mockDao.getMedicationCountByName(medicationName))
            .thenThrow(new RuntimeException("Database error"));
        
        // Act
        boolean result = repository.isMedicationNameExists(medicationName);
        
        // Assert
        assertFalse("Should return false on database error", result);
    }
    
    @Test
    public void testSearchMedicationsByName() {
        // Arrange
        String searchQuery = "test";
        MutableLiveData<List<MedicationInfo>> mockSearchResults = new MutableLiveData<>();
        when(mockDao.searchMedicationsByName(searchQuery)).thenReturn(mockSearchResults);
        
        // Act
        LiveData<List<MedicationInfo>> result = repository.searchMedicationsByName(searchQuery);
        
        // Assert
        assertNotNull(result);
        assertEquals(mockSearchResults, result);
        verify(mockDao).searchMedicationsByName(searchQuery);
    }
    
    @Test
    public void testGetMedicationsByColor() {
        // Arrange
        String color = "White";
        MutableLiveData<List<MedicationInfo>> mockColorResults = new MutableLiveData<>();
        when(mockDao.getMedicationsByColor(color)).thenReturn(mockColorResults);
        
        // Act
        LiveData<List<MedicationInfo>> result = repository.getMedicationsByColor(color);
        
        // Assert
        assertNotNull(result);
        assertEquals(mockColorResults, result);
        verify(mockDao).getMedicationsByColor(color);
    }
    
    @Test
    public void testGetMedicationsByDosageForm() {
        // Arrange
        String dosageForm = "Tablet";
        MutableLiveData<List<MedicationInfo>> mockDosageResults = new MutableLiveData<>();
        when(mockDao.getMedicationsByDosageForm(dosageForm)).thenReturn(mockDosageResults);
        
        // Act
        LiveData<List<MedicationInfo>> result = repository.getMedicationsByDosageForm(dosageForm);
        
        // Assert
        assertNotNull(result);
        assertEquals(mockDosageResults, result);
        verify(mockDao).getMedicationsByDosageForm(dosageForm);
    }
    
    @Test
    public void testCleanup() {
        // Act
        repository.cleanup();
        
        // Assert - no exception should be thrown
        // The executor should be shutdown
        // Note: We can't easily test the internal state of ExecutorService
        // but we can ensure the method completes without error
    }
    
    /**
     * Helper method to create a valid test medication
     */
    private MedicationInfo createTestMedication() {
        MedicationInfo medication = new MedicationInfo();
        medication.setName("Test Medication");
        medication.setColor("White");
        medication.setDosageForm("Tablet");
        medication.setPhotoPath("/path/to/photo.jpg");
        return medication;
    }
}