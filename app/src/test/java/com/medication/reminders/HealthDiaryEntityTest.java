package com.medication.reminders;

import org.junit.Test;
import static org.junit.Assert.*;

import com.medication.reminders.database.entity.HealthDiary;

/**
 * 健康日记实体类的单元测试
 */
public class HealthDiaryEntityTest {

    @Test
    public void testHealthDiaryCreation() {
        // 测试基本构造函数
        long userId = 1L;
        String content = "今天感觉身体状况良好，血压正常。";
        
        HealthDiary diary = new HealthDiary(userId, content);
        
        assertEquals(userId, diary.getUserId());
        assertEquals(content, diary.getContent());
        assertTrue(diary.getCreatedAt() > 0);
        assertTrue(diary.getUpdatedAt() > 0);
        assertEquals(diary.getCreatedAt(), diary.getUpdatedAt());
    }

    @Test
    public void testContentValidation() {
        HealthDiary diary = new HealthDiary(1L, "有效内容");
        assertTrue("有效内容应该通过验证", diary.isContentValid());
        
        // 测试空内容
        diary.setContent("");
        assertFalse("空内容应该验证失败", diary.isContentValid());
        
        // 测试null内容
        diary.setContent(null);
        assertFalse("null内容应该验证失败", diary.isContentValid());
        
        // 测试过长内容
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 5001; i++) {
            longContent.append("a");
        }
        diary.setContent(longContent.toString());
        assertFalse("过长内容应该验证失败", diary.isContentValid());
    }

    @Test
    public void testContentPreview() {
        String longContent = "这是一个很长的健康日记内容，用来测试内容预览功能是否正常工作。";
        HealthDiary diary = new HealthDiary(1L, longContent);
        
        String preview = diary.getContentPreview(10);
        // 验证预览长度正确且包含省略号
        assertTrue("预览应该包含省略号", preview.endsWith("..."));
        assertTrue("预览长度应该是13（10个字符+3个省略号）", preview.length() == 13);
        
        String shortContent = "短内容";
        diary.setContent(shortContent);
        assertEquals(shortContent, diary.getContentPreview(100));
    }

    @Test
    public void testTimestampUpdate() throws InterruptedException {
        HealthDiary diary = new HealthDiary(1L, "原始内容");
        long originalUpdatedAt = diary.getUpdatedAt();
        
        // 等待一毫秒确保时间戳不同
        Thread.sleep(1);
        
        diary.setContent("更新后的内容");
        assertTrue("更新内容后时间戳应该改变", diary.getUpdatedAt() > originalUpdatedAt);
        assertTrue("日记应该被标记为已修改", diary.isModified());
    }

    @Test
    public void testFormattedDates() {
        HealthDiary diary = new HealthDiary(1L, "测试内容");
        
        String formattedCreated = diary.getFormattedCreatedDate();
        String formattedUpdated = diary.getFormattedUpdatedDate();
        String shortDate = diary.getShortCreatedDate();
        
        assertNotNull("格式化创建日期不应为null", formattedCreated);
        assertNotNull("格式化更新日期不应为null", formattedUpdated);
        assertNotNull("简短日期不应为null", shortDate);
        
        assertTrue("格式化日期应包含年份", formattedCreated.contains("202"));
        assertTrue("简短日期应该更短", shortDate.length() < formattedCreated.length());
    }

    @Test
    public void testContentLength() {
        String content = "测试内容长度";
        HealthDiary diary = new HealthDiary(1L, content);
        
        assertEquals(content.length(), diary.getContentLength());
        
        diary.setContent("");
        assertEquals(0, diary.getContentLength());
    }

    @Test
    public void testEqualsAndHashCode() {
        HealthDiary diary1 = new HealthDiary(1L, "相同内容");
        diary1.setId(1L);
        diary1.setCreatedAt(1000L);
        diary1.setUpdatedAt(1000L);
        
        HealthDiary diary2 = new HealthDiary(1L, "相同内容");
        diary2.setId(1L);
        diary2.setCreatedAt(1000L);
        diary2.setUpdatedAt(1000L);
        
        assertEquals("相同内容的日记应该相等", diary1, diary2);
        assertEquals("相同内容的日记应该有相同的hashCode", diary1.hashCode(), diary2.hashCode());
        
        diary2.setContent("不同内容");
        assertNotEquals("不同内容的日记不应该相等", diary1, diary2);
    }

    @Test
    public void testToString() {
        HealthDiary diary = new HealthDiary(1L, "测试toString方法的内容");
        String toString = diary.toString();
        
        assertNotNull("toString不应为null", toString);
        assertTrue("toString应包含类名", toString.contains("HealthDiary"));
        assertTrue("toString应包含用户ID", toString.contains("userId=1"));
    }

    @Test
    public void testDefaultConstructor() {
        HealthDiary diary = new HealthDiary();
        
        assertTrue("默认构造函数应设置创建时间", diary.getCreatedAt() > 0);
        assertTrue("默认构造函数应设置更新时间", diary.getUpdatedAt() > 0);
        assertEquals("初始创建时间和更新时间应相等", diary.getCreatedAt(), diary.getUpdatedAt());
    }

    @Test
    public void testMarkAsUpdated() throws InterruptedException {
        HealthDiary diary = new HealthDiary(1L, "测试内容");
        long originalUpdatedAt = diary.getUpdatedAt();
        
        Thread.sleep(1);
        diary.markAsUpdated();
        
        assertTrue("手动标记更新后时间戳应该改变", diary.getUpdatedAt() > originalUpdatedAt);
    }
}