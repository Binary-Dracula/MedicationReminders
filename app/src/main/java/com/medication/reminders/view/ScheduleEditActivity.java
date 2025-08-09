package com.medication.reminders.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.medication.reminders.R;
import com.medication.reminders.database.entity.MedicationSchedule;
import com.medication.reminders.repository.MedicationScheduleRepository;
import com.medication.reminders.enums.ReminderCycleType;

/**
 * 简易的用药计划编辑页：演示如何创建每日/每周/每月/每隔X天的提醒
 */
public class ScheduleEditActivity extends AppCompatActivity {

    private Spinner typeSpinner;
    private EditText timesEdit;
    private LinearLayout weekDaysContainer;
    private CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    private EditText dayOfMonthEdit;
    private EditText intervalDaysEdit;
    private Button saveButton;

    private long medicationId;
    private MedicationScheduleRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_edit);

        medicationId = getIntent().getLongExtra("medication_id", -1);
        if (medicationId <= 0) {
            Toast.makeText(this, "缺少药品ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = new MedicationScheduleRepository(getApplication());
        initViews();
        setupSpinner();
        preloadOrNew();
        setupSave();
    }

    /**
     * 根据选择的提醒周期类型动态显示相关字段
     */
    private void updateFieldsVisibility(ReminderCycleType cycleType) {
        // 默认隐藏所有动态字段
        weekDaysContainer.setVisibility(View.GONE);
        dayOfMonthEdit.setVisibility(View.GONE);
        intervalDaysEdit.setVisibility(View.GONE);
        
        // 根据类型显示相应字段
        switch (cycleType) {
            case DAILY:
                // 每日：不需要额外字段
                break;
            case WEEKLY:
                // 每周：显示星期选择
                weekDaysContainer.setVisibility(View.VISIBLE);
                break;
            case MONTHLY:
                // 每月：显示每月几号
                dayOfMonthEdit.setVisibility(View.VISIBLE);
                break;
            case EVERY_X_DAYS:
                // 每隔X天：显示间隔天数
                intervalDaysEdit.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void initViews() {
        typeSpinner = findViewById(R.id.spinnerType);
        timesEdit = findViewById(R.id.editTimes);
        weekDaysContainer = findViewById(R.id.weekDaysContainer);
        cbMon = findViewById(R.id.cbMon);
        cbTue = findViewById(R.id.cbTue);
        cbWed = findViewById(R.id.cbWed);
        cbThu = findViewById(R.id.cbThu);
        cbFri = findViewById(R.id.cbFri);
        cbSat = findViewById(R.id.cbSat);
        cbSun = findViewById(R.id.cbSun);
        dayOfMonthEdit = findViewById(R.id.editDayOfMonth);
        intervalDaysEdit = findViewById(R.id.editIntervalDays);
        saveButton = findViewById(R.id.btnSave);
    }

    private void setupSpinner() {
        String[] items = ReminderCycleType.getDisplayNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        
        // 添加选择监听器，实现动态显示
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFieldsVisibility(ReminderCycleType.fromIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不处理
            }
        });
    }

    /**
     * 若该药品已有计划，则预填并进入“编辑模式”
     */
    private void preloadOrNew() {
        new Thread(() -> {
            java.util.List<MedicationSchedule> list = repository.getSchedulesForMedicationSync(medicationId);
            if (list != null && !list.isEmpty()) {
                MedicationSchedule s = list.get(0);
                runOnUiThread(() -> applyScheduleToUI(s));
            }
        }).start();
    }

    private void applyScheduleToUI(MedicationSchedule s) {
        typeSpinner.setSelection(s.getCycleTypeIndex());
        
        timesEdit.setText(s.getTimesOfDay());
        int mask = s.getDaysOfWeekMask();
        cbMon.setChecked((mask & (1 << 6)) != 0);
        cbTue.setChecked((mask & (1 << 5)) != 0);
        cbWed.setChecked((mask & (1 << 4)) != 0);
        cbThu.setChecked((mask & (1 << 3)) != 0);
        cbFri.setChecked((mask & (1 << 2)) != 0);
        cbSat.setChecked((mask & (1 << 1)) != 0);
        cbSun.setChecked((mask & 1) != 0);
        dayOfMonthEdit.setText(String.valueOf(Math.max(0, s.getDayOfMonth())));
        intervalDaysEdit.setText(String.valueOf(Math.max(0, s.getIntervalDays())));
        saveButton.setText("保存修改");
        saveButton.setTag(s);
    }

    private void setupSave() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedIndex = typeSpinner.getSelectedItemPosition();
                ReminderCycleType cycleType = ReminderCycleType.fromIndex(selectedIndex);
                String timesCsv = timesEdit.getText().toString().trim();
                if (TextUtils.isEmpty(timesCsv)) {
                    Toast.makeText(ScheduleEditActivity.this, "请至少填写一个时间，如 08:00,20:00", Toast.LENGTH_LONG).show();
                    return;
                }

                MedicationSchedule s;
                Object tag = saveButton.getTag();
                if (tag instanceof MedicationSchedule) {
                    s = (MedicationSchedule) tag; // 编辑模式
                } else {
                    s = new MedicationSchedule(); // 新增模式
                }
                s.setMedicationId(medicationId);
                s.setCycleTypeIndex(cycleType.getIndex());
                s.setTimesOfDay(timesCsv);
                s.setTimesPerDay(Math.max(1, timesCsv.split(",").length));
                s.setStartDateMillis(System.currentTimeMillis());

                switch (cycleType) {
                    case WEEKLY:
                        int mask = 0;
                        if (cbMon.isChecked()) mask |= (1 << 6);
                        if (cbTue.isChecked()) mask |= (1 << 5);
                        if (cbWed.isChecked()) mask |= (1 << 4);
                        if (cbThu.isChecked()) mask |= (1 << 3);
                        if (cbFri.isChecked()) mask |= (1 << 2);
                        if (cbSat.isChecked()) mask |= (1 << 1);
                        if (cbSun.isChecked()) mask |= 1;
                        s.setDaysOfWeekMask(mask);
                        break;
                    case MONTHLY:
                        int dom = parseIntSafe(dayOfMonthEdit.getText().toString().trim(), 1);
                        s.setDayOfMonth(dom);
                        break;
                    case EVERY_X_DAYS:
                        int x = parseIntSafe(intervalDaysEdit.getText().toString().trim(), 1);
                        s.setIntervalDays(x);
                        break;
                    case DAILY:
                    default:
                        // 每日不需要额外设置
                        break;
                }

                saveButton.setEnabled(false);
                boolean isEdit = (saveButton.getTag() instanceof MedicationSchedule);
                if (isEdit) {
                    repository.updateAndReschedule(s, new MedicationScheduleRepository.Callback() {
                        @Override public void onSuccess(long id) {
                            runOnUiThread(() -> { Toast.makeText(ScheduleEditActivity.this, R.string.schedule_save_success, Toast.LENGTH_LONG).show(); finish(); });
                        }
                        @Override public void onError(String message) {
                            runOnUiThread(() -> { saveButton.setEnabled(true); Toast.makeText(ScheduleEditActivity.this, message == null ? getString(R.string.schedule_save_failed) : message, Toast.LENGTH_LONG).show(); });
                        }
                    });
                } else {
                    repository.insertAndSchedule(s, new MedicationScheduleRepository.Callback() {
                        @Override public void onSuccess(long id) {
                            runOnUiThread(() -> { Toast.makeText(ScheduleEditActivity.this, R.string.schedule_save_success, Toast.LENGTH_LONG).show(); finish(); });
                        }
                        @Override public void onError(String message) {
                            runOnUiThread(() -> { saveButton.setEnabled(true); Toast.makeText(ScheduleEditActivity.this, message == null ? getString(R.string.schedule_save_failed) : message, Toast.LENGTH_LONG).show(); });
                        }
                    });
                }
            }
        });
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception ignored) { return def; }
    }
}

