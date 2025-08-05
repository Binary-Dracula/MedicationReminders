package com.medication.reminders.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.medication.reminders.database.entity.MedicationInfo;

import java.util.List;

/**
 * Data Access Object (DAO) for MedicationInfo entity
 * Defines all database operations for medication management
 */
@Dao
public interface MedicationDao {
    
    /**
     * Get all medications ordered by creation date (newest first)
     * Returns LiveData for observing data changes
     * 
     * @return LiveData list of all medications
     */
    @Query("SELECT * FROM medications ORDER BY created_at DESC")
    LiveData<List<MedicationInfo>> getAllMedications();
    
    /**
     * Get a specific medication by its ID
     * 
     * @param id The medication ID
     * @return LiveData of the medication
     */
    @Query("SELECT * FROM medications WHERE id = :id")
    LiveData<MedicationInfo> getMedicationById(long id);
    
    /**
     * Get a medication by name (for duplicate checking)
     * This is a synchronous method for validation purposes
     * 
     * @param name The medication name
     * @return The medication if found, null otherwise
     */
    @Query("SELECT * FROM medications WHERE name = :name LIMIT 1")
    MedicationInfo getMedicationByName(String name);
    
    /**
     * Insert a new medication into the database
     * 
     * @param medication The medication to insert
     * @return The ID of the inserted medication
     */
    @Insert
    long insertMedication(MedicationInfo medication);
    
    /**
     * Update an existing medication
     * 
     * @param medication The medication to update
     * @return The number of rows updated
     */
    @Update
    int updateMedication(MedicationInfo medication);
    
    /**
     * Delete a medication from the database
     * 
     * @param medication The medication to delete
     * @return The number of rows deleted
     */
    @Delete
    int deleteMedication(MedicationInfo medication);
    
    /**
     * Delete a medication by its ID
     * 
     * @param id The medication ID
     * @return The number of rows deleted
     */
    @Query("DELETE FROM medications WHERE id = :id")
    int deleteMedicationById(long id);
    
    /**
     * Check if a medication name already exists (for duplicate checking)
     * 
     * @param name The medication name to check
     * @return The count of medications with this name
     */
    @Query("SELECT COUNT(*) FROM medications WHERE name = :name")
    int getMedicationCountByName(String name);
    
    /**
     * Search medications by name (case-insensitive)
     * 
     * @param searchQuery The search query
     * @return LiveData list of matching medications
     */
    @Query("SELECT * FROM medications WHERE name LIKE '%' || :searchQuery || '%' ORDER BY created_at DESC")
    LiveData<List<MedicationInfo>> searchMedicationsByName(String searchQuery);
    
    /**
     * Get medications by color
     * 
     * @param color The medication color
     * @return LiveData list of medications with the specified color
     */
    @Query("SELECT * FROM medications WHERE color = :color ORDER BY created_at DESC")
    LiveData<List<MedicationInfo>> getMedicationsByColor(String color);
    
    /**
     * Get medications by dosage form
     * 
     * @param dosageForm The medication dosage form
     * @return LiveData list of medications with the specified dosage form
     */
    @Query("SELECT * FROM medications WHERE dosage_form = :dosageForm ORDER BY created_at DESC")
    LiveData<List<MedicationInfo>> getMedicationsByDosageForm(String dosageForm);
    
    /**
     * Get total count of medications
     * 
     * @return LiveData of the total medication count
     */
    @Query("SELECT COUNT(*) FROM medications")
    LiveData<Integer> getMedicationCount();
    
    /**
     * Delete all medications (for testing purposes)
     * 
     * @return The number of rows deleted
     */
    @Query("DELETE FROM medications")
    int deleteAllMedications();
}