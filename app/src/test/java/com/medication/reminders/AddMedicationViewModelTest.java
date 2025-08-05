package com.medication.reminders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.repository.MedicationRepository;
import com.medication.reminders.viewmodels.AddMedicationViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for AddMedicationViewModel
 * Tests state management and business logic for adding medications
 */
@RunWith(MockitoJUnitRunner.class)
public class AddMedicationViewModelTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private Application mockApplication;
    
    @Mock
    private MedicationRepository mockRepository;
    
    @Mock
    private Observer<String> stringObserver;
    
    @Mock
    private Observer<Boolean> booleanObserver;
    
    private AddMedicationViewModel viewModel;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create a custom ViewModel that uses our mock repository
        viewModel = new AddMedicationViewModel(mockApplication) {
            @Override
            protected MedicationRepository createRepository(Application application) {
                return mockRepository;
            }
        };
    }
    
    @Test
    public void testInitialState() {
        // Assert initial values
        assertEquals("", viewModel.getMedicationName().getValue());
        assertEquals("", viewModel.getSelectedColor().getValue());
        assertEquals("", viewModel.getSelectedDosageForm().getValue());
        assertEquals("", viewModel.getPhotoPath().getValue());
        assertNull(viewModel.getErrorMessage().getValue());
        assertFalse(viewModel.getSaveSuccess().getValue());
        assertFalse(viewModel.getIsLoading().getValue());
        assertFalse(viewModel.getShowDuplicateDialog().getValue());
        assertNull(viewModel.getDuplicateMedicationName().getValue());
        assertNull(viewModel.getNameError().getValue());
        assertNull(viewModel.getColorError().getValue());
        assertNull(viewModel.getDosageFormError().getValue());
        assertFalse(viewModel.getHasPhoto().getValue());
        assertNull(viewModel.getPhotoPreviewPath().getValue());
    }
    
    @Test
    public void testSetMedicationName_ValidName() {
        // Arrange
        String testName = "Test Medication";
        
        // Act
        viewModel.setMedicationName(testName);
        
        // Assert
        assertEquals(testName, viewModel.getMedicationName().getValue());
        assertNull(viewModel.getNameError().getValue()); // No error for valid name
    }
    
    @Test
    public void testSetMedicationName_EmptyName() {
        // Act
        viewModel.setMedicationName("");
        
        // Assert
        assertEquals("", viewModel.getMedicationName().getValue());
        // No real-time validation - error should be null until save is attempted
        assertNull(viewModel.getNameError().getValue());
    }
    
    @Test
    public void testSetMedicationName_NullName() {
        // Act
        viewModel.setMedicationName(null);
        
        // Assert
        assertEquals("", viewModel.getMedicationName().getValue());
        // No real-time validation - error should be null until save is attempted
        assertNull(viewModel.getNameError().getValue());
    }
    
    @Test
    public void testSetMedicationName_TooLong() {
        // Arrange
        String longName = "A".repeat(101); // 101 characters
        
        // Act
        viewModel.setMedicationName(longName);
        
        // Assert
        assertEquals(longName, viewModel.getMedicationName().getValue());
        // No real-time validation - error should be null until save is attempted
        assertNull(viewModel.getNameError().getValue());
    }
    
    @Test
    public void testSetMedicationName_WithWhitespace() {
        // Arrange
        String nameWithSpaces = "  Test Medication  ";
        
        // Act
        viewModel.setMedicationName(nameWithSpaces);
        
        // Assert
        assertEquals("Test Medication", viewModel.getMedicationName().getValue());
        assertNull(viewModel.getNameError().getValue()); // No error after trimming
    }
    
    @Test
    public void testSetSelectedColor_ValidColor() {
        // Arrange
        String testColor = "White";
        
        // Act
        viewModel.setSelectedColor(testColor);
        
        // Assert
        assertEquals(testColor, viewModel.getSelectedColor().getValue());
        assertNull(viewModel.getColorError().getValue()); // No error for valid color
    }
    
    @Test
    public void testSetSelectedColor_EmptyColor() {
        // Act
        viewModel.setSelectedColor("");
        
        // Assert
        assertEquals("", viewModel.getSelectedColor().getValue());
        // No real-time validation - error should be null until save is attempted
        assertNull(viewModel.getColorError().getValue());
    }
    
    @Test
    public void testSetSelectedDosageForm_ValidForm() {
        // Arrange
        String testForm = "Tablet";
        
        // Act
        viewModel.setSelectedDosageForm(testForm);
        
        // Assert
        assertEquals(testForm, viewModel.getSelectedDosageForm().getValue());
        assertNull(viewModel.getDosageFormError().getValue()); // No error for valid form
    }
    
    @Test
    public void testSetSelectedDosageForm_EmptyForm() {
        // Act
        viewModel.setSelectedDosageForm("");
        
        // Assert
        assertEquals("", viewModel.getSelectedDosageForm().getValue());
        // No real-time validation - error should be null until save is attempted
        assertNull(viewModel.getDosageFormError().getValue());
    }
    
    @Test
    public void testSetPhotoPath_ValidPath() {
        // Arrange
        String testPath = "/path/to/photo.jpg";
        viewModel.getHasPhoto().observeForever(booleanObserver);
        
        // Act
        viewModel.setPhotoPath(testPath);
        
        // Assert
        assertEquals(testPath, viewModel.getPhotoPath().getValue());
        assertEquals(testPath, viewModel.getPhotoPreviewPath().getValue());
        verify(booleanObserver).onChanged(true);
    }
    
    @Test
    public void testSetPhotoPath_EmptyPath() {
        // Arrange
        viewModel.getHasPhoto().observeForever(booleanObserver);
        
        // Act
        viewModel.setPhotoPath("");
        
        // Assert
        assertEquals("", viewModel.getPhotoPath().getValue());
        assertEquals("", viewModel.getPhotoPreviewPath().getValue());
        verify(booleanObserver, atLeast(1)).onChanged(false);
    }
    
    @Test
    public void testSetPhotoPath_NullPath() {
        // Arrange
        viewModel.getHasPhoto().observeForever(booleanObserver);
        
        // Act
        viewModel.setPhotoPath(null);
        
        // Assert
        assertNull(viewModel.getPhotoPath().getValue());
        assertNull(viewModel.getPhotoPreviewPath().getValue());
        verify(booleanObserver, atLeast(1)).onChanged(false);
    }
    
    @Test
    public void testClearPhoto() {
        // Arrange
        viewModel.setPhotoPath("/path/to/photo.jpg");
        viewModel.getHasPhoto().observeForever(booleanObserver);
        
        // Act
        viewModel.clearPhoto();
        
        // Assert
        assertEquals("", viewModel.getPhotoPath().getValue());
        assertEquals("", viewModel.getPhotoPreviewPath().getValue());
        verify(booleanObserver).onChanged(false);
    }
    
    @Test
    public void testSaveMedication_ValidationFailure() {
        // Arrange
        viewModel.getErrorMessage().observeForever(stringObserver);
        // Leave all fields empty to trigger validation failure
        
        // Act
        viewModel.saveMedication();
        
        // Assert
        verify(stringObserver).onChanged("请检查并修正表单中的错误");
        verifyNoInteractions(mockRepository);
    }
    
    @Test
    public void testSaveMedication_Success() {
        // Arrange
        setupValidMedicationData();
        Observer<Boolean> loadingObserver = mock(Observer.class);
        Observer<Boolean> successObserver = mock(Observer.class);
        
        viewModel.getIsLoading().observeForever(loadingObserver);
        viewModel.getSaveSuccess().observeForever(successObserver);
        
        // Mock repository success
        doAnswer(invocation -> {
            MedicationRepository.InsertCallback callback = invocation.getArgument(2);
            callback.onSuccess(1L);
            return null;
        }).when(mockRepository).insertMedication(any(MedicationInfo.class), eq(false), any(MedicationRepository.InsertCallback.class));
        
        // Act
        viewModel.saveMedication();
        
        // Assert
        // Verify loading states (use atLeast since there might be initial values)
        verify(loadingObserver, atLeast(1)).onChanged(true);  // Should set loading to true
        verify(loadingObserver, atLeast(1)).onChanged(false); // Should set loading to false
        
        // Verify success
        verify(successObserver, atLeast(1)).onChanged(true); // saveSuccess should be true
        
        // Verify repository was called
        verify(mockRepository).insertMedication(any(MedicationInfo.class), eq(false), any(MedicationRepository.InsertCallback.class));
    }
    
    @Test
    public void testSaveMedication_RepositoryError() {
        // Arrange
        setupValidMedicationData();
        Observer<Boolean> loadingObserver = mock(Observer.class);
        
        viewModel.getErrorMessage().observeForever(stringObserver);
        viewModel.getIsLoading().observeForever(loadingObserver);
        
        String errorMessage = "Database error";
        
        // Mock repository error
        doAnswer(invocation -> {
            MedicationRepository.InsertCallback callback = invocation.getArgument(2);
            callback.onError(errorMessage);
            return null;
        }).when(mockRepository).insertMedication(any(MedicationInfo.class), eq(false), any(MedicationRepository.InsertCallback.class));
        
        // Act
        viewModel.saveMedication();
        
        // Assert
        verify(stringObserver).onChanged(errorMessage);
        verify(loadingObserver, atLeast(1)).onChanged(true);  // Should set loading to true first
        verify(loadingObserver, atLeast(1)).onChanged(false); // Then loading should be false
    }
    
    @Test
    public void testSaveMedication_DuplicateFound() {
        // Arrange
        setupValidMedicationData();
        viewModel.getShowDuplicateDialog().observeForever(booleanObserver);
        viewModel.getDuplicateMedicationName().observeForever(stringObserver);
        
        String medicationName = "Test Medication";
        
        // Mock repository duplicate found
        doAnswer(invocation -> {
            MedicationRepository.InsertCallback callback = invocation.getArgument(2);
            callback.onDuplicateFound(medicationName);
            return null;
        }).when(mockRepository).insertMedication(any(MedicationInfo.class), eq(false), any(MedicationRepository.InsertCallback.class));
        
        // Act
        viewModel.saveMedication();
        
        // Assert
        verify(booleanObserver).onChanged(true); // showDuplicateDialog should be true
        verify(stringObserver).onChanged(medicationName);
    }
    
    @Test
    public void testSaveMedication_WithDuplicateOverride() {
        // Arrange
        setupValidMedicationData();
        viewModel.getSaveSuccess().observeForever(booleanObserver);
        
        // Mock repository success with duplicate override
        doAnswer(invocation -> {
            MedicationRepository.InsertCallback callback = invocation.getArgument(2);
            callback.onSuccess(1L);
            return null;
        }).when(mockRepository).insertMedication(any(MedicationInfo.class), eq(true), any(MedicationRepository.InsertCallback.class));
        
        // Act
        viewModel.saveMedication(true);
        
        // Assert
        verify(booleanObserver).onChanged(true); // saveSuccess should be true
        verify(mockRepository).insertMedication(any(MedicationInfo.class), eq(true), any(MedicationRepository.InsertCallback.class));
    }
    
    @Test
    public void testHandleDuplicateResponse_Allow() {
        // Arrange
        setupValidMedicationData();
        viewModel.getShowDuplicateDialog().observeForever(booleanObserver);
        
        // Mock repository success
        doAnswer(invocation -> {
            MedicationRepository.InsertCallback callback = invocation.getArgument(2);
            callback.onSuccess(1L);
            return null;
        }).when(mockRepository).insertMedication(any(MedicationInfo.class), eq(true), any(MedicationRepository.InsertCallback.class));
        
        // Act
        viewModel.handleDuplicateResponse(true);
        
        // Assert
        verify(booleanObserver, atLeast(1)).onChanged(false); // showDuplicateDialog should be false
        verify(mockRepository).insertMedication(any(MedicationInfo.class), eq(true), any(MedicationRepository.InsertCallback.class));
    }
    
    @Test
    public void testHandleDuplicateResponse_Deny() {
        // Arrange
        viewModel.getShowDuplicateDialog().observeForever(booleanObserver);
        
        // Act
        viewModel.handleDuplicateResponse(false);
        
        // Assert
        verify(booleanObserver, atLeast(1)).onChanged(false); // showDuplicateDialog should be false
        verifyNoInteractions(mockRepository);
    }
    
    @Test
    public void testClearForm() {
        // Arrange
        setupValidMedicationData();
        viewModel.getSaveSuccess().observeForever(booleanObserver);
        
        // Act
        viewModel.clearForm();
        
        // Assert
        assertEquals("", viewModel.getMedicationName().getValue());
        assertEquals("", viewModel.getSelectedColor().getValue());
        assertEquals("", viewModel.getSelectedDosageForm().getValue());
        assertEquals("", viewModel.getPhotoPath().getValue());
        assertNull(viewModel.getErrorMessage().getValue());
        assertNull(viewModel.getNameError().getValue());
        assertNull(viewModel.getColorError().getValue());
        assertNull(viewModel.getDosageFormError().getValue());
        verify(booleanObserver, atLeast(1)).onChanged(false); // saveSuccess should be false
    }
    
    @Test
    public void testClearErrors() {
        // Arrange
        viewModel.setMedicationName(""); // Trigger name error
        viewModel.setSelectedColor(""); // Trigger color error
        viewModel.setSelectedDosageForm(""); // Trigger dosage form error
        
        // Act
        viewModel.clearErrors();
        
        // Assert
        assertNull(viewModel.getErrorMessage().getValue());
        assertNull(viewModel.getNameError().getValue());
        assertNull(viewModel.getColorError().getValue());
        assertNull(viewModel.getDosageFormError().getValue());
    }
    
    @Test
    public void testResetSaveSuccess() {
        // Arrange
        viewModel.getSaveSuccess().observeForever(booleanObserver);
        
        // Act
        viewModel.resetSaveSuccess();
        
        // Assert
        verify(booleanObserver, atLeast(1)).onChanged(false);
    }
    
    @Test
    public void testHasFormData_EmptyForm() {
        // Act
        boolean result = viewModel.hasFormData();
        
        // Assert
        assertFalse("Empty form should return false", result);
    }
    
    @Test
    public void testHasFormData_WithName() {
        // Arrange
        viewModel.setMedicationName("Test");
        
        // Act
        boolean result = viewModel.hasFormData();
        
        // Assert
        assertTrue("Form with name should return true", result);
    }
    
    @Test
    public void testHasFormData_WithColor() {
        // Arrange
        viewModel.setSelectedColor("White");
        
        // Act
        boolean result = viewModel.hasFormData();
        
        // Assert
        assertTrue("Form with color should return true", result);
    }
    
    @Test
    public void testHasFormData_WithDosageForm() {
        // Arrange
        viewModel.setSelectedDosageForm("Tablet");
        
        // Act
        boolean result = viewModel.hasFormData();
        
        // Assert
        assertTrue("Form with dosage form should return true", result);
    }
    
    @Test
    public void testHasFormData_WithPhoto() {
        // Arrange
        viewModel.setPhotoPath("/path/to/photo.jpg");
        
        // Act
        boolean result = viewModel.hasFormData();
        
        // Assert
        assertTrue("Form with photo should return true", result);
    }
    
    @Test
    public void testGetCurrentMedicationData() {
        // Arrange
        setupValidMedicationData();
        
        // Act
        MedicationInfo result = viewModel.getCurrentMedicationData();
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Medication", result.getName());
        assertEquals("White", result.getColor());
        assertEquals("Tablet", result.getDosageForm());
        assertEquals("/path/to/photo.jpg", result.getPhotoPath());
    }
    
    @Test
    public void testGetCurrentMedicationData_EmptyForm() {
        // Act
        MedicationInfo result = viewModel.getCurrentMedicationData();
        
        // Assert
        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals("", result.getColor());
        assertEquals("", result.getDosageForm());
        assertEquals("", result.getPhotoPath());
    }
    
    @Test
    public void testGetCurrentMedicationData_NullValues() {
        // Arrange - set null values
        viewModel.setMedicationName(null);
        viewModel.setSelectedColor(null);
        viewModel.setSelectedDosageForm(null);
        viewModel.setPhotoPath(null);
        
        // Act
        MedicationInfo result = viewModel.getCurrentMedicationData();
        
        // Assert
        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals("", result.getColor());
        assertEquals("", result.getDosageForm());
        assertEquals("", result.getPhotoPath());
    }
    
    /**
     * Helper method to setup valid medication data for testing
     */
    private void setupValidMedicationData() {
        viewModel.setMedicationName("Test Medication");
        viewModel.setSelectedColor("White");
        viewModel.setSelectedDosageForm("Tablet");
        viewModel.setPhotoPath("/path/to/photo.jpg");
    }
}