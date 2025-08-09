package com.medication.reminders;

import android.content.Intent;
import android.widget.Button;

import androidx.test.core.app.ActivityScenario;

import com.medication.reminders.view.MainActivity;
import com.medication.reminders.view.MedicationIntakeRecordListActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 测试MainActivity中用药记录按钮的功能
 */
@RunWith(RobolectricTestRunner.class)
public class MainActivityIntakeRecordTest {

    @Test
    public void testIntakeRecordButtonExists() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                // 验证用药记录按钮存在
                Button btnIntakeRecord = activity.findViewById(R.id.btnIntakeRecord);
                assertNotNull("用药记录按钮应该存在", btnIntakeRecord);
                
                // 验证按钮文本
                String expectedText = activity.getString(R.string.intake_record_button);
                assertEquals("按钮文本应该正确", expectedText, btnIntakeRecord.getText().toString());
            });
        }
    }

    @Test
    public void testIntakeRecordButtonNavigation() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                // 获取用药记录按钮
                Button btnIntakeRecord = activity.findViewById(R.id.btnIntakeRecord);
                assertNotNull("用药记录按钮应该存在", btnIntakeRecord);
                
                // 点击按钮
                btnIntakeRecord.performClick();
                
                // 验证Intent是否正确启动
                ShadowActivity shadowActivity = Shadows.shadowOf(activity);
                Intent startedIntent = shadowActivity.getNextStartedActivity();
                assertNotNull("应该启动新的Activity", startedIntent);
                assertEquals("应该导航到用药记录列表页面", 
                    MedicationIntakeRecordListActivity.class.getName(), 
                    startedIntent.getComponent().getClassName());
            });
        }
    }
}