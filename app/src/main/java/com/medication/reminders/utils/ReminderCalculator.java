package com.medication.reminders.utils;

import android.text.TextUtils;

import com.medication.reminders.database.entity.MedicationSchedule;
import com.medication.reminders.enums.ReminderCycleType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * 负责基于计划计算下一次提醒时间
 */
public class ReminderCalculator {

    public static long computeNextReminderEpochMillis(MedicationSchedule schedule, long now) {
        if (schedule == null || !schedule.isEnabled()) {
            return 0L;
        }

        ReminderCycleType cycleType = ReminderCycleType.fromIndex(schedule.getCycleTypeIndex());
        switch (cycleType) {
            case DAILY:
                return nextFromDaily(schedule, now);
            case WEEKLY:
                return nextFromWeekly(schedule, now);
            case MONTHLY:
                return nextFromMonthly(schedule, now);
            case EVERY_X_DAYS:
                return nextFromEveryXDays(schedule, now);
            default:
                return 0L;
        }
    }



    private static List<int[]> parseTimes(String timesCsv) {
        if (TextUtils.isEmpty(timesCsv)) return Collections.emptyList();
        String[] items = timesCsv.split(",");
        List<int[]> result = new ArrayList<>();
        for (String s : items) {
            String t = s.trim();
            if (t.isEmpty()) continue;
            String[] hm = t.split(":");
            if (hm.length != 2) continue;
            try {
                int h = Integer.parseInt(hm[0]);
                int m = Integer.parseInt(hm[1]);
                if (h >= 0 && h < 24 && m >= 0 && m < 60) {
                    result.add(new int[]{h, m});
                }
            } catch (NumberFormatException ignored) { }
        }
        // 按时间排序
        result.sort((a, b) -> a[0] != b[0] ? a[0] - b[0] : a[1] - b[1]);
        return result;
    }

    private static long nextFromDaily(MedicationSchedule s, long now) {
        List<int[]> times = parseTimes(s.getTimesOfDay());
        if (times.isEmpty()) return 0L;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        for (int[] hm : times) {
            Calendar c = (Calendar) cal.clone();
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.HOUR_OF_DAY, hm[0]);
            c.set(Calendar.MINUTE, hm[1]);
            if (c.getTimeInMillis() > now) return c.getTimeInMillis();
        }
        // 明天第一条
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        c.add(Calendar.DAY_OF_YEAR, 1);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.HOUR_OF_DAY, times.get(0)[0]);
        c.set(Calendar.MINUTE, times.get(0)[1]);
        return c.getTimeInMillis();
    }

    private static long nextFromWeekly(MedicationSchedule s, long now) {
        List<int[]> times = parseTimes(s.getTimesOfDay());
        if (times.isEmpty()) return 0L;
        int mask = s.getDaysOfWeekMask();
        Calendar base = Calendar.getInstance();
        base.setTimeInMillis(now);
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) base.clone();
            day.add(Calendar.DAY_OF_YEAR, i);
            int dow = day.get(Calendar.DAY_OF_WEEK); // 周日=1 ... 周六=7
            int bit = mapCalendarDowToMaskBit(dow);
            if ((mask & bit) == 0) continue;
            for (int[] hm : times) {
                Calendar c = (Calendar) day.clone();
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.HOUR_OF_DAY, hm[0]);
                c.set(Calendar.MINUTE, hm[1]);
                if (c.getTimeInMillis() > now) return c.getTimeInMillis();
            }
        }
        // 一周后最早一条
        Calendar nextWeek = (Calendar) base.clone();
        nextWeek.add(Calendar.DAY_OF_YEAR, 7);
        // 再次循环查找匹配日的第一条
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) nextWeek.clone();
            day.add(Calendar.DAY_OF_YEAR, i);
            int dow = day.get(Calendar.DAY_OF_WEEK);
            int bit = mapCalendarDowToMaskBit(dow);
            if ((mask & bit) == 0) continue;
            Calendar c = (Calendar) day.clone();
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.HOUR_OF_DAY, times.get(0)[0]);
            c.set(Calendar.MINUTE, times.get(0)[1]);
            return c.getTimeInMillis();
        }
        return 0L;
    }

    private static int mapCalendarDowToMaskBit(int calendarDow) {
        // 约定：mask 从高到低 1<<6 周一 ... 1<<0 周日
        switch (calendarDow) {
            case Calendar.MONDAY: return 1 << 6;
            case Calendar.TUESDAY: return 1 << 5;
            case Calendar.WEDNESDAY: return 1 << 4;
            case Calendar.THURSDAY: return 1 << 3;
            case Calendar.FRIDAY: return 1 << 2;
            case Calendar.SATURDAY: return 1 << 1;
            case Calendar.SUNDAY: return 1;
            default: return 0;
        }
    }

    private static long nextFromMonthly(MedicationSchedule s, long now) {
        List<int[]> times = parseTimes(s.getTimesOfDay());
        if (times.isEmpty()) return 0L;
        int dayOfMonth = Math.max(1, Math.min(31, s.getDayOfMonth()));
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);

        // 本月指定日
        Calendar candidate = (Calendar) cal.clone();
        candidate.set(Calendar.DAY_OF_MONTH, Math.min(dayOfMonth, candidate.getActualMaximum(Calendar.DAY_OF_MONTH)));
        for (int[] hm : times) {
            Calendar c = (Calendar) candidate.clone();
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.HOUR_OF_DAY, hm[0]);
            c.set(Calendar.MINUTE, hm[1]);
            if (c.getTimeInMillis() > now) return c.getTimeInMillis();
        }
        // 下一月第一条
        Calendar nextMonth = (Calendar) candidate.clone();
        nextMonth.add(Calendar.MONTH, 1);
        nextMonth.set(Calendar.DAY_OF_MONTH, Math.min(dayOfMonth, nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH)));
        nextMonth.set(Calendar.SECOND, 0);
        nextMonth.set(Calendar.MILLISECOND, 0);
        nextMonth.set(Calendar.HOUR_OF_DAY, times.get(0)[0]);
        nextMonth.set(Calendar.MINUTE, times.get(0)[1]);
        return nextMonth.getTimeInMillis();
    }

    private static long nextFromEveryXDays(MedicationSchedule s, long now) {
        List<int[]> times = parseTimes(s.getTimesOfDay());
        if (times.isEmpty()) return 0L;
        int x = Math.max(1, s.getIntervalDays());
        long start = s.getStartDateMillis();
        if (start <= 0) start = now;

        // 找到 >= now 的最近周期的日子
        Calendar calNow = Calendar.getInstance();
        calNow.setTimeInMillis(now);
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(start);
        normalizeToZeroClock(day);

        while (day.getTimeInMillis() <= now) {
            day.add(Calendar.DAY_OF_YEAR, x);
        }
        // 回退一个周期以检查今天晚些时候的时间点
        Calendar prev = (Calendar) day.clone();
        prev.add(Calendar.DAY_OF_YEAR, -x);

        // 先检查 prev 当天是否还有未过的时间点
        long candidate = firstTimeOfDayAfter(prev, times, now);
        if (candidate > now) return candidate;

        // 否则取 day 当天的第一条
        Calendar firstNext = (Calendar) day.clone();
        firstNext.set(Calendar.HOUR_OF_DAY, times.get(0)[0]);
        firstNext.set(Calendar.MINUTE, times.get(0)[1]);
        firstNext.set(Calendar.SECOND, 0);
        firstNext.set(Calendar.MILLISECOND, 0);
        return firstNext.getTimeInMillis();
    }

    private static void normalizeToZeroClock(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private static long firstTimeOfDayAfter(Calendar day, List<int[]> times, long after) {
        for (int[] hm : times) {
            Calendar c = (Calendar) day.clone();
            c.set(Calendar.HOUR_OF_DAY, hm[0]);
            c.set(Calendar.MINUTE, hm[1]);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            if (c.getTimeInMillis() > after) return c.getTimeInMillis();
        }
        return 0L;
    }
}

