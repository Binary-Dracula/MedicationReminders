package com.medication.reminders;

import com.medication.reminders.database.entity.User;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * User实体类的单元测试
 * 测试构造函数、getter、setter和业务逻辑方法
 */
public class UserEntityTest {
    
    private User user;
    
    // 测试数据常量
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PHONE = "13800138000";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_FULL_NAME = "张三";
    private static final String TEST_GENDER = "男性";
    private static final String TEST_BIRTH_DATE = "1990-01-01";
    private static final String TEST_PHOTO_PATH = "/path/to/photo.jpg";
    private static final String TEST_SECONDARY_PHONE = "13900139000";
    private static final String TEST_EMERGENCY_NAME = "李四";
    private static final String TEST_EMERGENCY_PHONE = "13700137000";
    private static final String TEST_EMERGENCY_RELATION = "配偶";
    private static final String TEST_ADDRESS = "北京市朝阳区";
    private static final String TEST_BLOOD_TYPE = "A+";
    private static final String TEST_ALLERGIES = "青霉素过敏";
    private static final String TEST_MEDICAL_CONDITIONS = "高血压";
    private static final String TEST_DOCTOR_NAME = "王医生";
    private static final String TEST_DOCTOR_PHONE = "13600136000";
    private static final String TEST_HOSPITAL_NAME = "北京医院";
    private static final long TEST_TIMESTAMP = 1640995200000L; // 2022-01-01 00:00:00 UTC
    
    @Before
    public void setUp() {
        user = new User();
    }
    
    @Test
    public void testDefaultConstructor() {
        User newUser = new User();
        
        assertEquals("默认ID应该为0", 0, newUser.getId());
        assertNull("默认用户名应该为null", newUser.getUsername());
        assertNull("默认邮箱应该为null", newUser.getEmail());
        assertNull("默认电话应该为null", newUser.getPhone());
        assertNull("默认密码应该为null", newUser.getPassword());
        assertNull("默认姓名应该为null", newUser.getFullName());
        assertNull("默认性别应该为null", newUser.getGender());
        assertNull("默认出生日期应该为null", newUser.getBirthDate());
        assertNull("默认照片路径应该为null", newUser.getProfilePhotoPath());
        
        assertFalse("默认登录状态应该为false", newUser.isLoggedIn());
        assertFalse("默认记住我状态应该为false", newUser.isRememberMe());
        assertEquals("默认登录尝试次数应该为0", 0, newUser.getLoginAttempts());
        
        assertTrue("创建时间应该大于0", newUser.getCreatedAt() > 0);
        assertTrue("更新时间应该大于0", newUser.getUpdatedAt() > 0);
        assertEquals("创建时间和更新时间应该相等", newUser.getCreatedAt(), newUser.getUpdatedAt());
    }
    
    @Test
    public void testBasicConstructor() {
        long beforeTime = System.currentTimeMillis();
        User newUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);
        long afterTime = System.currentTimeMillis();
        
        assertEquals("用户名应该设置正确", TEST_USERNAME, newUser.getUsername());
        assertEquals("邮箱应该设置正确", TEST_EMAIL, newUser.getEmail());
        assertEquals("电话应该设置正确", TEST_PHONE, newUser.getPhone());
        assertEquals("密码应该设置正确", TEST_PASSWORD, newUser.getPassword());
        
        assertFalse("登录状态应该为false", newUser.isLoggedIn());
        assertFalse("记住我状态应该为false", newUser.isRememberMe());
        assertEquals("登录尝试次数应该为0", 0, newUser.getLoginAttempts());
        
        assertTrue("创建时间应该在测试时间范围内", 
                   newUser.getCreatedAt() >= beforeTime && newUser.getCreatedAt() <= afterTime);
        assertTrue("更新时间应该在测试时间范围内", 
                   newUser.getUpdatedAt() >= beforeTime && newUser.getUpdatedAt() <= afterTime);
    }
    
    @Test
    public void testCompleteConstructor() {
        User newUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD,
                               TEST_FULL_NAME, TEST_GENDER, TEST_BIRTH_DATE, TEST_PHOTO_PATH);
        
        assertEquals("用户名应该设置正确", TEST_USERNAME, newUser.getUsername());
        assertEquals("邮箱应该设置正确", TEST_EMAIL, newUser.getEmail());
        assertEquals("电话应该设置正确", TEST_PHONE, newUser.getPhone());
        assertEquals("密码应该设置正确", TEST_PASSWORD, newUser.getPassword());
        assertEquals("姓名应该设置正确", TEST_FULL_NAME, newUser.getFullName());
        assertEquals("性别应该设置正确", TEST_GENDER, newUser.getGender());
        assertEquals("出生日期应该设置正确", TEST_BIRTH_DATE, newUser.getBirthDate());
        assertEquals("照片路径应该设置正确", TEST_PHOTO_PATH, newUser.getProfilePhotoPath());
    }
    
    @Test
    public void testBasicSettersAndGetters() {
        user.setId(123L);
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPhone(TEST_PHONE);
        user.setPassword(TEST_PASSWORD);
        
        assertEquals("ID应该设置正确", 123L, user.getId());
        assertEquals("用户名应该设置正确", TEST_USERNAME, user.getUsername());
        assertEquals("邮箱应该设置正确", TEST_EMAIL, user.getEmail());
        assertEquals("电话应该设置正确", TEST_PHONE, user.getPhone());
        assertEquals("密码应该设置正确", TEST_PASSWORD, user.getPassword());
    }
    
    @Test
    public void testProfileSettersAndGetters() {
        user.setFullName(TEST_FULL_NAME);
        user.setGender(TEST_GENDER);
        user.setBirthDate(TEST_BIRTH_DATE);
        user.setProfilePhotoPath(TEST_PHOTO_PATH);
        
        assertEquals("姓名应该设置正确", TEST_FULL_NAME, user.getFullName());
        assertEquals("性别应该设置正确", TEST_GENDER, user.getGender());
        assertEquals("出生日期应该设置正确", TEST_BIRTH_DATE, user.getBirthDate());
        assertEquals("照片路径应该设置正确", TEST_PHOTO_PATH, user.getProfilePhotoPath());
    }
    
    @Test
    public void testContactSettersAndGetters() {
        user.setSecondaryPhone(TEST_SECONDARY_PHONE);
        user.setEmergencyContactName(TEST_EMERGENCY_NAME);
        user.setEmergencyContactPhone(TEST_EMERGENCY_PHONE);
        user.setEmergencyContactRelation(TEST_EMERGENCY_RELATION);
        user.setAddress(TEST_ADDRESS);
        
        assertEquals("备用电话应该设置正确", TEST_SECONDARY_PHONE, user.getSecondaryPhone());
        assertEquals("紧急联系人姓名应该设置正确", TEST_EMERGENCY_NAME, user.getEmergencyContactName());
        assertEquals("紧急联系人电话应该设置正确", TEST_EMERGENCY_PHONE, user.getEmergencyContactPhone());
        assertEquals("紧急联系人关系应该设置正确", TEST_EMERGENCY_RELATION, user.getEmergencyContactRelation());
        assertEquals("地址应该设置正确", TEST_ADDRESS, user.getAddress());
    }
    
    @Test
    public void testMedicalSettersAndGetters() {
        user.setBloodType(TEST_BLOOD_TYPE);
        user.setAllergies(TEST_ALLERGIES);
        user.setMedicalConditions(TEST_MEDICAL_CONDITIONS);
        user.setDoctorName(TEST_DOCTOR_NAME);
        user.setDoctorPhone(TEST_DOCTOR_PHONE);
        user.setHospitalName(TEST_HOSPITAL_NAME);
        
        assertEquals("血型应该设置正确", TEST_BLOOD_TYPE, user.getBloodType());
        assertEquals("过敏信息应该设置正确", TEST_ALLERGIES, user.getAllergies());
        assertEquals("既往病史应该设置正确", TEST_MEDICAL_CONDITIONS, user.getMedicalConditions());
        assertEquals("医生姓名应该设置正确", TEST_DOCTOR_NAME, user.getDoctorName());
        assertEquals("医生电话应该设置正确", TEST_DOCTOR_PHONE, user.getDoctorPhone());
        assertEquals("医院名称应该设置正确", TEST_HOSPITAL_NAME, user.getHospitalName());
    }
    
    @Test
    public void testSessionSettersAndGetters() {
        user.setLoggedIn(true);
        user.setRememberMe(true);
        user.setLastLoginTime(TEST_TIMESTAMP);
        user.setLoginAttempts(3);
        user.setLastAttemptTime(TEST_TIMESTAMP);
        
        assertTrue("登录状态应该设置正确", user.isLoggedIn());
        assertTrue("记住我状态应该设置正确", user.isRememberMe());
        assertEquals("最后登录时间应该设置正确", TEST_TIMESTAMP, user.getLastLoginTime());
        assertEquals("登录尝试次数应该设置正确", 3, user.getLoginAttempts());
        assertEquals("最后尝试时间应该设置正确", TEST_TIMESTAMP, user.getLastAttemptTime());
    }
    
    @Test
    public void testTimestampSettersAndGetters() {
        user.setCreatedAt(TEST_TIMESTAMP);
        user.setUpdatedAt(TEST_TIMESTAMP + 1000);
        
        assertEquals("创建时间应该设置正确", TEST_TIMESTAMP, user.getCreatedAt());
        assertEquals("更新时间应该设置正确", TEST_TIMESTAMP + 1000, user.getUpdatedAt());
    }
    
    @Test
    public void testUpdateTimestampOnSetters() {
        long initialTime = user.getUpdatedAt();
        
        // 等待一毫秒确保时间戳不同
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // 忽略
        }
        
        user.setUsername("newusername");
        assertTrue("设置用户名应该更新时间戳", user.getUpdatedAt() > initialTime);
        
        long secondTime = user.getUpdatedAt();
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // 忽略
        }
        
        user.setEmail("new@example.com");
        assertTrue("设置邮箱应该更新时间戳", user.getUpdatedAt() > secondTime);
    }
    
    @Test
    public void testHasBasicInfo() {
        // 测试空用户
        assertFalse("空用户应该没有基本信息", user.hasBasicInfo());
        
        // 测试部分信息
        user.setUsername(TEST_USERNAME);
        assertFalse("只有用户名不算有基本信息", user.hasBasicInfo());
        
        user.setEmail(TEST_EMAIL);
        assertFalse("只有用户名和邮箱不算有基本信息", user.hasBasicInfo());
        
        user.setPhone(TEST_PHONE);
        assertTrue("有用户名、邮箱和电话算有基本信息", user.hasBasicInfo());
        
        // 测试空字符串
        user.setUsername("   ");
        assertFalse("空白用户名不算有基本信息", user.hasBasicInfo());
    }
    
    @Test
    public void testHasExtendedInfo() {
        // 测试空用户
        assertFalse("空用户应该没有扩展信息", user.hasExtendedInfo());
        
        // 测试部分信息
        user.setFullName(TEST_FULL_NAME);
        assertFalse("只有姓名不算有扩展信息", user.hasExtendedInfo());
        
        user.setGender(TEST_GENDER);
        assertFalse("只有姓名和性别不算有扩展信息", user.hasExtendedInfo());
        
        user.setBirthDate(TEST_BIRTH_DATE);
        assertTrue("有姓名、性别和出生日期算有扩展信息", user.hasExtendedInfo());
        
        // 测试空字符串
        user.setFullName("");
        assertFalse("空姓名不算有扩展信息", user.hasExtendedInfo());
    }
    
    @Test
    public void testIsProfileComplete() {
        // 测试空用户
        assertFalse("空用户资料不完整", user.isProfileComplete());
        
        // 设置基本信息
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPhone(TEST_PHONE);
        assertFalse("只有基本信息资料不完整", user.isProfileComplete());
        
        // 设置扩展信息
        user.setFullName(TEST_FULL_NAME);
        user.setGender(TEST_GENDER);
        user.setBirthDate(TEST_BIRTH_DATE);
        assertTrue("有基本信息和扩展信息资料完整", user.isProfileComplete());
    }
    
    @Test
    public void testHasProfilePhoto() {
        // 测试空照片路径
        assertFalse("空照片路径应该返回false", user.hasProfilePhoto());
        
        user.setProfilePhotoPath("");
        assertFalse("空字符串照片路径应该返回false", user.hasProfilePhoto());
        
        user.setProfilePhotoPath("   ");
        assertFalse("空白字符串照片路径应该返回false", user.hasProfilePhoto());
        
        user.setProfilePhotoPath(TEST_PHOTO_PATH);
        assertTrue("有效照片路径应该返回true", user.hasProfilePhoto());
    }
    
    @Test
    public void testHasMedicalInfo() {
        // 测试空医疗信息
        assertFalse("空医疗信息应该返回false", user.hasMedicalInfo());
        
        user.setBloodType(TEST_BLOOD_TYPE);
        assertTrue("有血型信息应该返回true", user.hasMedicalInfo());
        
        user.setBloodType(null);
        user.setAllergies(TEST_ALLERGIES);
        assertTrue("有过敏信息应该返回true", user.hasMedicalInfo());
        
        user.setAllergies(null);
        user.setMedicalConditions(TEST_MEDICAL_CONDITIONS);
        assertTrue("有既往病史应该返回true", user.hasMedicalInfo());
        
        user.setMedicalConditions(null);
        user.setDoctorName(TEST_DOCTOR_NAME);
        assertTrue("有医生姓名应该返回true", user.hasMedicalInfo());
        
        user.setDoctorName(null);
        user.setDoctorPhone(TEST_DOCTOR_PHONE);
        assertTrue("有医生电话应该返回true", user.hasMedicalInfo());
        
        user.setDoctorPhone(null);
        user.setHospitalName(TEST_HOSPITAL_NAME);
        assertTrue("有医院名称应该返回true", user.hasMedicalInfo());
        
        // 测试空白字符串
        user.setHospitalName("   ");
        assertFalse("空白医院名称不算有医疗信息", user.hasMedicalInfo());
    }
    
    @Test
    public void testHasEmergencyContact() {
        // 测试空紧急联系人信息
        assertFalse("空紧急联系人信息应该返回false", user.hasEmergencyContact());
        
        user.setEmergencyContactName(TEST_EMERGENCY_NAME);
        assertFalse("只有姓名不算有完整紧急联系人信息", user.hasEmergencyContact());
        
        user.setEmergencyContactPhone(TEST_EMERGENCY_PHONE);
        assertTrue("有姓名和电话算有紧急联系人信息", user.hasEmergencyContact());
        
        // 测试空白字符串
        user.setEmergencyContactName("   ");
        assertFalse("空白姓名不算有紧急联系人信息", user.hasEmergencyContact());
    }
    
    @Test
    public void testResetLoginAttempts() {
        user.setLoginAttempts(5);
        user.setLastAttemptTime(TEST_TIMESTAMP);
        
        user.resetLoginAttempts();
        
        assertEquals("重置后登录尝试次数应该为0", 0, user.getLoginAttempts());
        assertEquals("重置后最后尝试时间应该为0", 0, user.getLastAttemptTime());
    }
    
    @Test
    public void testIncrementLoginAttempts() {
        long beforeTime = System.currentTimeMillis();
        
        user.incrementLoginAttempts();
        
        long afterTime = System.currentTimeMillis();
        
        assertEquals("增加后登录尝试次数应该为1", 1, user.getLoginAttempts());
        assertTrue("最后尝试时间应该在测试时间范围内", 
                   user.getLastAttemptTime() >= beforeTime && user.getLastAttemptTime() <= afterTime);
        
        user.incrementLoginAttempts();
        assertEquals("再次增加后登录尝试次数应该为2", 2, user.getLoginAttempts());
    }
    
    @Test
    public void testSetLoginStatus() {
        long beforeTime = System.currentTimeMillis();
        
        user.setLoginAttempts(3);
        user.setLoginStatus(true);
        
        long afterTime = System.currentTimeMillis();
        
        assertTrue("设置登录状态为true", user.isLoggedIn());
        assertTrue("最后登录时间应该在测试时间范围内", 
                   user.getLastLoginTime() >= beforeTime && user.getLastLoginTime() <= afterTime);
        assertEquals("成功登录后尝试次数应该重置为0", 0, user.getLoginAttempts());
        assertEquals("成功登录后最后尝试时间应该重置为0", 0, user.getLastAttemptTime());
        
        user.setLoginStatus(false);
        assertFalse("设置登录状态为false", user.isLoggedIn());
    }
    
    @Test
    public void testToString() {
        user.setId(123L);
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPhone(TEST_PHONE);
        user.setPassword(TEST_PASSWORD);
        user.setFullName(TEST_FULL_NAME);
        
        String result = user.toString();
        
        assertNotNull("toString不应该返回null", result);
        assertTrue("toString应该包含类名", result.contains("User"));
        assertTrue("toString应该包含id", result.contains("id=123"));
        assertTrue("toString应该包含用户名", result.contains("username='" + TEST_USERNAME + "'"));
        assertTrue("toString应该包含邮箱", result.contains("email='" + TEST_EMAIL + "'"));
        assertTrue("toString应该包含电话", result.contains("phone='" + TEST_PHONE + "'"));
        assertTrue("toString应该隐藏密码", result.contains("password='[PROTECTED]'"));
        assertTrue("toString应该包含姓名", result.contains("fullName='" + TEST_FULL_NAME + "'"));
    }
    
    @Test
    public void testToStringWithNullValues() {
        user.setId(123L);
        user.setUsername(null);
        user.setEmail(null);
        user.setPhone(null);
        user.setPassword(null);
        
        String result = user.toString();
        
        assertNotNull("toString不应该返回null", result);
        assertTrue("toString应该处理null用户名", result.contains("username=null"));
        assertTrue("toString应该处理null邮箱", result.contains("email=null"));
        assertTrue("toString应该处理null电话", result.contains("phone=null"));
        assertTrue("toString应该处理null密码", result.contains("password=null"));
    }
    
    @Test
    public void testSettersWithNullValues() {
        user.setUsername(null);
        user.setEmail(null);
        user.setPhone(null);
        user.setPassword(null);
        user.setFullName(null);
        user.setGender(null);
        user.setBirthDate(null);
        user.setProfilePhotoPath(null);
        
        assertNull("用户名应该为null", user.getUsername());
        assertNull("邮箱应该为null", user.getEmail());
        assertNull("电话应该为null", user.getPhone());
        assertNull("密码应该为null", user.getPassword());
        assertNull("姓名应该为null", user.getFullName());
        assertNull("性别应该为null", user.getGender());
        assertNull("出生日期应该为null", user.getBirthDate());
        assertNull("照片路径应该为null", user.getProfilePhotoPath());
    }
    
    @Test
    public void testSettersWithEmptyStrings() {
        user.setUsername("");
        user.setEmail("");
        user.setPhone("");
        user.setPassword("");
        user.setFullName("");
        user.setGender("");
        user.setBirthDate("");
        user.setProfilePhotoPath("");
        
        assertEquals("用户名应该为空字符串", "", user.getUsername());
        assertEquals("邮箱应该为空字符串", "", user.getEmail());
        assertEquals("电话应该为空字符串", "", user.getPhone());
        assertEquals("密码应该为空字符串", "", user.getPassword());
        assertEquals("姓名应该为空字符串", "", user.getFullName());
        assertEquals("性别应该为空字符串", "", user.getGender());
        assertEquals("出生日期应该为空字符串", "", user.getBirthDate());
        assertEquals("照片路径应该为空字符串", "", user.getProfilePhotoPath());
    }
    
    @Test
    public void testBoundaryValues() {
        user.setId(Long.MIN_VALUE);
        assertEquals("最小ID值应该处理正确", Long.MIN_VALUE, user.getId());
        
        user.setId(Long.MAX_VALUE);
        assertEquals("最大ID值应该处理正确", Long.MAX_VALUE, user.getId());
        
        user.setCreatedAt(Long.MIN_VALUE);
        user.setUpdatedAt(Long.MAX_VALUE);
        assertEquals("最小时间戳应该处理正确", Long.MIN_VALUE, user.getCreatedAt());
        assertEquals("最大时间戳应该处理正确", Long.MAX_VALUE, user.getUpdatedAt());
        
        user.setLoginAttempts(Integer.MIN_VALUE);
        assertEquals("最小登录尝试次数应该处理正确", Integer.MIN_VALUE, user.getLoginAttempts());
        
        user.setLoginAttempts(Integer.MAX_VALUE);
        assertEquals("最大登录尝试次数应该处理正确", Integer.MAX_VALUE, user.getLoginAttempts());
    }
    
    @Test
    public void testUnicodeCharacters() {
        String unicodeName = "张三李四王五赵六";
        String unicodeAddress = "北京市朝阳区建国门外大街1号";
        String unicodeAllergies = "对青霉素、磺胺类药物过敏";
        
        user.setFullName(unicodeName);
        user.setAddress(unicodeAddress);
        user.setAllergies(unicodeAllergies);
        
        assertEquals("Unicode姓名应该处理正确", unicodeName, user.getFullName());
        assertEquals("Unicode地址应该处理正确", unicodeAddress, user.getAddress());
        assertEquals("Unicode过敏信息应该处理正确", unicodeAllergies, user.getAllergies());
    }
    
    @Test
    public void testLongTextFields() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("a");
        }
        String longString = longText.toString();
        
        user.setAddress(longString);
        user.setAllergies(longString);
        user.setMedicalConditions(longString);
        
        assertEquals("长地址文本应该处理正确", longString, user.getAddress());
        assertEquals("长过敏信息应该处理正确", longString, user.getAllergies());
        assertEquals("长既往病史应该处理正确", longString, user.getMedicalConditions());
    }
}