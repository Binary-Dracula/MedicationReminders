package com.medication.reminders.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.medication.reminders.database.entity.MedicationInfo;
import com.medication.reminders.repository.MedicationRepository;
import com.medication.reminders.database.entity.MedicationSchedule;
import com.medication.reminders.repository.MedicationScheduleRepository;

import java.util.List;


/**
 * MedicationListViewModel - ViewModel for medication list display
 * Manages medication data for the list view
 */
public class MedicationListViewModel extends AndroidViewModel {
    
    private MedicationRepository repository;
    private MedicationScheduleRepository scheduleRepository;
    private LiveData<List<MedicationInfo>> allMedications;
    
    public MedicationListViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicationRepository(application);
        scheduleRepository = new MedicationScheduleRepository(application);
        allMedications = repository.getAllMedications();
    }
    
    /**
     * Get all medications as LiveData
     */
    public LiveData<List<MedicationInfo>> getAllMedications() {
        return allMedications;
    }

    public LiveData<List<MedicationSchedule>> getSchedules(long medicationId) {
        return scheduleRepository.getSchedulesForMedication(medicationId);
    }
    
    /**
     * Refresh medications (trigger repository refresh if needed)
     */
    public void refreshMedications() {
        // Repository automatically provides updated data through LiveData
        // This method can be used for manual refresh if needed in the future
    }
    
    /**
     * Delete a medication
     */
    public void deleteMedication(MedicationInfo medication) {
        repository.deleteMedication(medication, new MedicationRepository.DeleteCallback() {
            @Override
            public void onSuccess() {
                // Medication deleted successfully
            }
            
            @Override
            public void onError(String errorMessage) {
                // Handle error if needed
            }
        });
    }
}