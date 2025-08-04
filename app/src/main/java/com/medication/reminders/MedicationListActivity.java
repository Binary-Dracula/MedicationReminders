package com.medication.reminders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

/**
 * MedicationListActivity - Display list of medications with photos
 * Allows users to view all their medications and navigate to details
 */
public class MedicationListActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private MedicationListAdapter adapter;
    private MedicationListViewModel viewModel;
    private LinearLayout tvEmptyMessage;
    private FloatingActionButton fabAddMedication;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_list);
        
        // Initialize views
        initViews();
        
        // Setup ViewModel
        setupViewModel();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup observers
        setupObservers();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup action bar
        setupActionBar();
    }
    
    /**
     * Initialize UI components
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMedications);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        fabAddMedication = findViewById(R.id.fabAddMedication);
    }
    
    /**
     * Setup ViewModel
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MedicationListViewModel.class);
    }
    
    /**
     * Setup RecyclerView with adapter
     */
    private void setupRecyclerView() {
        adapter = new MedicationListAdapter(new ArrayList<>(), this::onMedicationClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    /**
     * Setup observers for LiveData
     */
    private void setupObservers() {
        viewModel.getAllMedications().observe(this, medications -> {
            if (medications != null && !medications.isEmpty()) {
                adapter.updateMedications(medications);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmptyMessage.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvEmptyMessage.setVisibility(View.VISIBLE);
            }
        });
    }
    
    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        fabAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMedicationActivity.class);
            startActivity(intent);
        });
    }
    
    /**
     * Setup action bar
     */
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.medication_list_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    /**
     * Handle medication item click
     */
    private void onMedicationClick(MedicationInfo medication) {
        Intent intent = new Intent(this, MedicationDetailActivity.class);
        intent.putExtra("medication_id", medication.getId());
        startActivity(intent);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning from add/edit medication
        viewModel.refreshMedications();
    }
}