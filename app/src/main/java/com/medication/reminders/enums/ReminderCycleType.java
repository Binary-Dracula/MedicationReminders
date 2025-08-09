package com.medication.reminders.enums;

/**
 * 提醒周期类型枚举
 */
public enum ReminderCycleType {
    DAILY(0, "每日"),
    WEEKLY(1, "每周"),
    MONTHLY(2, "每月"),
    EVERY_X_DAYS(3, "每隔X天");

    private final int index;
    private final String displayName;

    ReminderCycleType(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据索引获取枚举值
     */
    public static ReminderCycleType fromIndex(int index) {
        for (ReminderCycleType type : values()) {
            if (type.index == index) {
                return type;
            }
        }
        return DAILY; // 默认返回每日
    }

    /**
     * 获取所有显示名称的数组
     */
    public static String[] getDisplayNames() {
        ReminderCycleType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].displayName;
        }
        return names;
    }
}