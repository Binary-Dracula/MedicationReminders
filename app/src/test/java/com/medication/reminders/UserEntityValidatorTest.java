package com.medication.reminders;

import com.medication.reminders.database.entity.User;
import com.medication.reminders.models.ProfileValidationResult;
import com.medication.reminders.utils.UserEntityValidator;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * UserEntityValidator类的单元测试
 * 测试User实体的各种验证方法
 */
public class UserEntityValidatorTest {
    
    private User validUser;
    
    // 测试数据常量
    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PHONE = "13800138000";
    private static final String VALID_PASSWORD = "password123";
    private static final String VALID_FULL_NAME = "张三";
    private static final String VALID_GENDER = "男性";
    private static final String VALID_BIRTH_DATE = "1990-01-01";
    private static final String VALID_BLOOD_TYPE = "A+";
    private static final String VALID_DOCTOR_NAME = "王医生";
    private static final String VALID_DOCTOR_PHONE = "13600136000";
    private static final String VALID_EMERGENCY_NAME = "李四";
    private static final String VALID_EMERGENCY_PHONE = "13700137000";
    
    @Before
    public void setUp() {
        validUser = new User(VALID_USERNAME, VALID_EMAIL, VALID_PHONE, VALID_PASSWORD);
        validUser.setFullName(VALID_FULL_NAME);
        validUser.setGender(VALID_GENDER);
        validUser.setBirthDate(VALID_BIRTH_DATE);
    }
    
    // 测试基本注册信息验证
    
    @Test
    public void testValidateBasicRegistrationInfo_ValidUser() {
        ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(validUser);
        assertTrue("有效用户应该通过基本注册信息验证", result.isValid());
    }
    
    @Test
    public void testValidateBasicRegistrationInfo_NullUser() {
        ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(null);
        assertFalse("null用户应该验证失败", result.isValid());
        assertEquals("错误类型应该是必填字段为空", 
                     ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY, 
                     result.getErrorType());
    }
    
    @Test
    public void testValidateBasicRegistrationInfo_InvalidUsername() {
        validUser.setUsername("ab"); // 太短
        ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(validUser);
        assertFalse("用户名太短应该验证失败", result.isValid());
        assertTrue("错误信息应该包含用户名", result.getErrorMessage().contains("用户名"));
    }
    
    @Test
    public void testValidateBasicRegistrationInfo_InvalidEmail() {
        validUser.setEmail("invalid-email");
        ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(validUser);
        assertFalse("无效邮箱应该验证失败", result.isValid());
        assertTrue("错误信息应该包含邮箱", result.getErrorMessage().contains("邮箱"));
    }
    
    @Test
    public void testValidateBasicRegistrationInfo_InvalidPhone() {
        validUser.setPhone("12345");
        ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(validUser);
        assertFalse("无效电话应该验证失败", result.isValid());
        assertTrue("错误信息应该包含电话", result.getErrorMessage().contains("电话"));
    }
    
    @Test
    public void testValidateBasicRegistrationInfo_InvalidPassword() {
        validUser.setPassword("123"); // 太短
        ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(validUser);
        assertFalse("密码太短应该验证失败", result.isValid());
        assertTrue("错误信息应该包含密码", result.getErrorMessage().contains("密码"));
    }
    
    // 测试扩展个人信息验证
    
    @Test
    public void testValidateExtendedProfileInfo_ValidUser() {
        ProfileValidationResult result = UserEntityValidator.validateExtendedProfileInfo(validUser);
        assertTrue("有效用户应该通过扩展个人信息验证", result.isValid());
    }
    
    @Test
    public void testValidateExtendedProfileInfo_EmptyFields() {
        User emptyUser = new User(VALID_USERNAME, VALID_EMAIL, VALID_PHONE, VALID_PASSWORD);
        ProfileValidationResult result = UserEntityValidator.validateExtendedProfileInfo(emptyUser);
        assertTrue("空扩展字段应该通过验证（可选字段）", result.isValid());
    }
    
    @Test
    public void testValidateExtendedProfileInfo_InvalidFullName() {
        validUser.setFullName("123!@#"); // 包含无效字符
        ProfileValidationResult result = UserEntityValidator.validateExtendedProfileInfo(validUser);
        assertFalse("无效姓名应该验证失败", result.isValid());
        assertTrue("错误信息应该包含姓名", result.getErrorMessage().contains("姓名"));
    }
    
    @Test
    public void testValidateExtendedProfileInfo_InvalidGender() {
        validUser.setGender("无效性别");
        ProfileValidationResult result = UserEntityValidator.validateExtendedProfileInfo(validUser);
        assertFalse("无效性别应该验证失败", result.isValid());
        assertTrue("错误信息应该包含性别", result.getErrorMessage().contains("性别"));
    }
    
    @Test
    public void testValidateExtendedProfileInfo_InvalidBirthDate() {
        validUser.setBirthDate("2030-01-01"); // 未来日期
        ProfileValidationResult result = UserEntityValidator.validateExtendedProfileInfo(validUser);
        assertFalse("未来出生日期应该验证失败", result.isValid());
        assertTrue("错误信息应该包含出生日期", result.getErrorMessage().contains("出生日期"));
    }
    
    // 测试医疗信息验证
    
    @Test
    public void testValidateMedicalInfo_ValidUser() {
        validUser.setBloodType(VALID_BLOOD_TYPE);
        validUser.setAllergies("青霉素过敏");
        validUser.setDoctorName(VALID_DOCTOR_NAME);
        validUser.setDoctorPhone(VALID_DOCTOR_PHONE);
        
        ProfileValidationResult result = UserEntityValidator.validateMedicalInfo(validUser);
        assertTrue("有效医疗信息应该通过验证", result.isValid());
    }
    
    @Test
    public void testValidateMedicalInfo_EmptyFields() {
        ProfileValidationResult result = UserEntityValidator.validateMedicalInfo(validUser);
        assertTrue("空医疗字段应该通过验证（可选字段）", result.isValid());
    }
    
    @Test
    public void testValidateMedicalInfo_InvalidBloodType() {
        validUser.setBloodType("XYZ");
        ProfileValidationResult result = UserEntityValidator.validateMedicalInfo(validUser);
        assertFalse("无效血型应该验证失败", result.isValid());
        assertTrue("错误信息应该包含血型", result.getErrorMessage().contains("血型"));
    }
    
    @Test
    public void testValidateMedicalInfo_TooLongAllergies() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            longText.append("a");
        }
        validUser.setAllergies(longText.toString());
        
        ProfileValidationResult result = UserEntityValidator.validateMedicalInfo(validUser);
        assertFalse("过长的过敏信息应该验证失败", result.isValid());
        assertTrue("错误信息应该包含过敏信息", result.getErrorMessage().contains("过敏信息"));
    }
    
    @Test
    public void testValidateMedicalInfo_InvalidDoctorName() {
        validUser.setDoctorName("123!@#");
        ProfileValidationResult result = UserEntityValidator.validateMedicalInfo(validUser);
        assertFalse("无效医生姓名应该验证失败", result.isValid());
    }
    
    @Test
    public void testValidateMedicalInfo_InvalidDoctorPhone() {
        validUser.setDoctorPhone("12345");
        ProfileValidationResult result = UserEntityValidator.validateMedicalInfo(validUser);
        assertFalse("无效医生电话应该验证失败", result.isValid());
    }
    
    // 测试联系信息验证
    
    @Test
    public void testValidateContactInfo_ValidUser() {
        validUser.setSecondaryPhone("13900139000");
        validUser.setEmergencyContactName(VALID_EMERGENCY_NAME);
        validUser.setEmergencyContactPhone(VALID_EMERGENCY_PHONE);
        validUser.setEmergencyContactRelation("配偶");
        validUser.setAddress("北京市朝阳区");
        
        ProfileValidationResult result = UserEntityValidator.validateContactInfo(validUser);
        assertTrue("有效联系信息应该通过验证", result.isValid());
    }
    
    @Test
    public void testValidateContactInfo_EmptyFields() {
        ProfileValidationResult result = UserEntityValidator.validateContactInfo(validUser);
        assertTrue("空联系字段应该通过验证（可选字段）", result.isValid());
    }
    
    @Test
    public void testValidateContactInfo_InvalidSecondaryPhone() {
        validUser.setSecondaryPhone("12345");
        ProfileValidationResult result = UserEntityValidator.validateContactInfo(validUser);
        assertFalse("无效备用电话应该验证失败", result.isValid());
        assertTrue("错误信息应该包含备用电话", result.getErrorMessage().contains("备用电话"));
    }
    
    @Test
    public void testValidateContactInfo_IncompleteEmergencyContact() {
        validUser.setEmergencyContactName(VALID_EMERGENCY_NAME);
        // 缺少电话号码
        ProfileValidationResult result = UserEntityValidator.validateContactInfo(validUser);
        assertFalse("不完整的紧急联系人信息应该验证失败", result.isValid());
        assertTrue("错误信息应该包含紧急联系人", result.getErrorMessage().contains("紧急联系人"));
    }
    
    @Test
    public void testValidateContactInfo_TooLongAddress() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            longText.append("a");
        }
        validUser.setAddress(longText.toString());
        
        ProfileValidationResult result = UserEntityValidator.validateContactInfo(validUser);
        assertFalse("过长的地址应该验证失败", result.isValid());
        assertTrue("错误信息应该包含地址", result.getErrorMessage().contains("地址"));
    }
    
    // 测试完整用户验证
    
    @Test
    public void testValidateCompleteUser_ValidUser() {
        ProfileValidationResult result = UserEntityValidator.validateCompleteUser(validUser);
        assertTrue("完整有效用户应该通过验证", result.isValid());
    }
    
    @Test
    public void testValidateCompleteUser_InvalidBasicInfo() {
        validUser.setUsername("ab"); // 无效用户名
        ProfileValidationResult result = UserEntityValidator.validateCompleteUser(validUser);
        assertFalse("基本信息无效的用户应该验证失败", result.isValid());
    }
    
    // 测试用户名格式验证
    
    @Test
    public void testValidateUsernameFormat_ValidUsernames() {
        String[] validUsernames = {"user123", "test_user", "abc", "user_name_123", "a".repeat(20)};
        
        for (String username : validUsernames) {
            ProfileValidationResult result = UserEntityValidator.validateUsernameFormat(username);
            assertTrue("用户名 '" + username + "' 应该有效", result.isValid());
        }
    }
    
    @Test
    public void testValidateUsernameFormat_InvalidUsernames() {
        String[] invalidUsernames = {
            null, "", "  ", "ab", "a".repeat(21), // 长度问题
            "user-name", "user.name", "user@name", "用户名", "user name" // 格式问题
        };
        
        for (String username : invalidUsernames) {
            ProfileValidationResult result = UserEntityValidator.validateUsernameFormat(username);
            assertFalse("用户名 '" + username + "' 应该无效", result.isValid());
        }
    }
    
    // 测试年龄计算
    
    @Test
    public void testCalculateAge_ValidDates() {
        // 测试已知年龄的日期
        String birthDate1990 = "1990-01-01";
        int age1990 = UserEntityValidator.calculateAge(birthDate1990);
        assertTrue("1990年出生的人应该大于30岁", age1990 > 30);
        
        String birthDate2000 = "2000-01-01";
        int age2000 = UserEntityValidator.calculateAge(birthDate2000);
        assertTrue("2000年出生的人应该大于20岁", age2000 > 20);
        assertTrue("2000年出生的人应该小于1990年出生的人", age2000 < age1990);
    }
    
    @Test
    public void testCalculateAge_InvalidDates() {
        String[] invalidDates = {null, "", "invalid-date", "2030-01-01", "1900-13-01"};
        
        for (String date : invalidDates) {
            int age = UserEntityValidator.calculateAge(date);
            assertEquals("无效日期 '" + date + "' 应该返回-1", -1, age);
        }
    }
    
    // 测试最低年龄要求
    
    @Test
    public void testMeetsMinimumAge_ValidAge() {
        validUser.setBirthDate("1990-01-01");
        assertTrue("1990年出生的用户应该满足最低年龄要求", 
                   UserEntityValidator.meetsMinimumAge(validUser));
    }
    
    @Test
    public void testMeetsMinimumAge_TooYoung() {
        validUser.setBirthDate("2020-01-01");
        assertFalse("2020年出生的用户不应该满足最低年龄要求", 
                    UserEntityValidator.meetsMinimumAge(validUser));
    }
    
    @Test
    public void testMeetsMinimumAge_NullUser() {
        assertFalse("null用户不应该满足最低年龄要求", 
                    UserEntityValidator.meetsMinimumAge(null));
    }
    
    @Test
    public void testMeetsMinimumAge_NullBirthDate() {
        validUser.setBirthDate(null);
        assertFalse("出生日期为null的用户不应该满足最低年龄要求", 
                    UserEntityValidator.meetsMinimumAge(validUser));
    }
    
    // 测试有效性别选项
    
    @Test
    public void testGetValidGenders() {
        String[] genders = UserEntityValidator.getValidGenders();
        
        assertNotNull("有效性别数组不应该为null", genders);
        assertTrue("应该包含男性选项", java.util.Arrays.asList(genders).contains("男性"));
        assertTrue("应该包含女性选项", java.util.Arrays.asList(genders).contains("女性"));
        assertTrue("应该包含不愿透露选项", java.util.Arrays.asList(genders).contains("不愿透露"));
    }
    
    // 测试边界情况
    
    @Test
    public void testValidation_WhitespaceStrings() {
        validUser.setUsername("   ");
        ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(validUser);
        assertFalse("空白字符串用户名应该验证失败", result.isValid());
        
        validUser.setUsername(VALID_USERNAME);
        validUser.setFullName("   ");
        result = UserEntityValidator.validateExtendedProfileInfo(validUser);
        assertTrue("空白字符串姓名应该通过验证（可选字段）", result.isValid());
    }
    
    @Test
    public void testValidation_BloodTypeVariations() {
        String[] validBloodTypes = {"A", "B", "AB", "O", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        for (String bloodType : validBloodTypes) {
            validUser.setBloodType(bloodType);
            ProfileValidationResult result = UserEntityValidator.validateMedicalInfo(validUser);
            assertTrue("血型 '" + bloodType + "' 应该有效", result.isValid());
        }
        
        String[] invalidBloodTypes = {"C", "XY", "A++", "B--", "ABC"};
        
        for (String bloodType : invalidBloodTypes) {
            validUser.setBloodType(bloodType);
            ProfileValidationResult result = UserEntityValidator.validateMedicalInfo(validUser);
            assertFalse("血型 '" + bloodType + "' 应该无效", result.isValid());
        }
    }
    
    @Test
    public void testValidation_PhoneNumberVariations() {
        String[] validPhones = {"13800138000", "13900139000", "15800158000", "18800188000"};
        
        for (String phone : validPhones) {
            validUser.setPhone(phone);
            ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(validUser);
            assertTrue("电话号码 '" + phone + "' 应该有效", result.isValid());
        }
        
        String[] invalidPhones = {"12800128000", "10800108000", "138001380001", "1380013800"};
        
        for (String phone : invalidPhones) {
            validUser.setPhone(phone);
            ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(validUser);
            assertFalse("电话号码 '" + phone + "' 应该无效", result.isValid());
        }
    }
    
    @Test
    public void testValidation_EmailVariations() {
        String[] validEmails = {
            "test@example.com", "user.name@domain.co.uk", "user+tag@example.org",
            "123@example.com", "test@sub.domain.com"
        };
        
        for (String email : validEmails) {
            validUser.setEmail(email);
            ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(validUser);
            assertTrue("邮箱 '" + email + "' 应该有效", result.isValid());
        }
        
        String[] invalidEmails = {
            "invalid-email", "@example.com", "test@", "test.example.com",
            "test@.com", "test@com", ""
        };
        
        for (String email : invalidEmails) {
            validUser.setEmail(email);
            ProfileValidationResult result = UserEntityValidator.validateBasicRegistrationInfo(validUser);
            assertFalse("邮箱 '" + email + "' 应该无效", result.isValid());
        }
    }
}