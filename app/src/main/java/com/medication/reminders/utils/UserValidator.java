package com.medication.reminders.utils;

import android.content.Context;

import com.medication.reminders.MedicationRemindersApplication;
import com.medication.reminders.R;
import com.medication.reminders.database.entity.User;
import com.medication.reminders.models.ProfileValidationResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 统一的用户信息验证器类
 * 整合所有用户信息相关的验证逻辑，包括注册表单和个人资料表单验证
 */
public class UserValidator {

    // 正则表达式模式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^1[3-9]\\d{9}$" // 中国手机号格式
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,20}$" // 3-20个字符，字母、数字、下划线
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[\\u4e00-\\u9fa5a-zA-Z\\s'-]{1,100}$" // 中文字符、字母、空格、连字符、撇号
    );
    
    private static final Pattern BLOOD_TYPE_PATTERN = Pattern.compile(
        "^(A|B|AB|O)[+-]?$" // 血型格式：A、B、AB、O，可选+/-
    );
    
    // 日期格式
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    // 验证常量
    private static final int MIN_AGE = 13;
    private static final int MAX_AGE = 120;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 20;
    private static final int MAX_TEXT_FIELD_LENGTH = 500; // 文本字段最大长度
    private static final int MAX_RELATION_LENGTH = 50;
    private static final int MAX_ADDRESS_LENGTH = 200;
    private static final int MAX_MEDICAL_FIELD_LENGTH = 500;
    
    // 有效的性别选项
    private static final String[] VALID_GENDERS = {"男性", "女性", "不愿透露"};

    /**
     * 验证注册表单
     * @param username 用户名
     * @param email 邮箱
     * @param phone 电话号码
     * @param password 密码
     * @return 验证结果
     */
    public static ProfileValidationResult validateRegistrationForm(String username, String email, String phone, String password) {
        // 验证用户名
        ProfileValidationResult usernameResult = validateUsername(username);
        if (!usernameResult.isValid()) {
            return usernameResult;
        }
        
        // 验证邮箱
        ProfileValidationResult emailResult = validateEmail(email);
        if (!emailResult.isValid()) {
            return emailResult;
        }
        
        // 验证电话号码
        ProfileValidationResult phoneResult = validatePhoneNumber(phone);
        if (!phoneResult.isValid()) {
            return phoneResult;
        }
        
        // 验证密码
        ProfileValidationResult passwordResult = validatePassword(password);
        if (!passwordResult.isValid()) {
            return passwordResult;
        }
        
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateSecondaryPhone(String secondaryPhone) {
        if (secondaryPhone == null || secondaryPhone.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (!PHONE_PATTERN.matcher(secondaryPhone.trim()).matches()) {
            return ProfileValidationResult.failure("备用电话", "备用电话号码格式不正确", ProfileValidationResult.ValidationErrorType.INVALID_FORMAT);
        }
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateEmergencyContactName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (name.trim().length() > MAX_NAME_LENGTH) {
            return ProfileValidationResult.failure("紧急联系人姓名", "紧急联系人姓名过长", ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateEmergencyContactPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return ProfileValidationResult.failure("紧急联系人电话", "紧急联系人电话号码格式不正确", ProfileValidationResult.ValidationErrorType.INVALID_FORMAT);
        }
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateEmergencyContactRelation(String relation) {
        if (relation == null || relation.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (relation.trim().length() > MAX_RELATION_LENGTH) {
            return ProfileValidationResult.failure("紧急联系人关系", "紧急联系人关系过长", ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (address.trim().length() > MAX_ADDRESS_LENGTH) {
            return ProfileValidationResult.failure("地址", "地址过长", ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateBloodType(String bloodType) {
        if (bloodType == null || bloodType.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (!BLOOD_TYPE_PATTERN.matcher(bloodType.trim().toUpperCase()).matches()) {
            return ProfileValidationResult.failure("血型", "血型格式不正确 (e.g., A+, O-)", ProfileValidationResult.ValidationErrorType.INVALID_FORMAT);
        }
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateAllergies(String allergies) {
        if (allergies == null || allergies.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (allergies.trim().length() > MAX_MEDICAL_FIELD_LENGTH) {
            return ProfileValidationResult.failure("过敏史", "过敏史描述过长", ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateMedicalConditions(String conditions) {
        if (conditions == null || conditions.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (conditions.trim().length() > MAX_MEDICAL_FIELD_LENGTH) {
            return ProfileValidationResult.failure("医疗状况", "医疗状况描述过长", ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateDoctorName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (name.trim().length() > MAX_NAME_LENGTH) {
            return ProfileValidationResult.failure("医生姓名", "医生姓名过长", ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateDoctorPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return ProfileValidationResult.failure("医生电话", "医生电话号码格式不正确", ProfileValidationResult.ValidationErrorType.INVALID_FORMAT);
        }
        return ProfileValidationResult.success();
    }

    public static ProfileValidationResult validateHospitalName(String name) {
        Context context = MedicationRemindersApplication.getAppContext();
        if (name == null || name.trim().isEmpty()) {
            return ProfileValidationResult.success(); // Optional field
        }
        if (name.trim().length() > MAX_NAME_LENGTH) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_hospital_name),
                context.getString(R.string.error_invalid_length_max, context.getString(R.string.error_field_hospital_name), MAX_NAME_LENGTH),
                ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        return ProfileValidationResult.success();
    }

    /**
     * 验证个人资料表单
     * @param fullName 完整姓名
     * @param gender 性别
     * @param birthDate 出生日期
     * @return 验证结果
     */
    public static ProfileValidationResult validateProfileForm(String fullName, String gender, String birthDate) {
        // 验证姓名
        ProfileValidationResult nameResult = validateFullName(fullName);
        if (!nameResult.isValid()) {
            return nameResult;
        }
        
        // 验证性别
        ProfileValidationResult genderResult = validateGender(gender);
        if (!genderResult.isValid()) {
            return genderResult;
        }
        
        // 验证出生日期
        ProfileValidationResult birthDateResult = validateBirthDate(birthDate);
        if (!birthDateResult.isValid()) {
            return birthDateResult;
        }
        
        return ProfileValidationResult.success();
    }

    /**
     * 验证用户名
     * @param username 用户名
     * @return 验证结果
     */
    public static ProfileValidationResult validateUsername(String username) {
        Context context = MedicationRemindersApplication.getAppContext();
        if (username == null || username.trim().isEmpty()) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_username),
                context.getString(R.string.error_required_field_empty, context.getString(R.string.error_field_username)),
                ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
        }
        String trimmedUsername = username.trim();
        if (trimmedUsername.length() < MIN_USERNAME_LENGTH || trimmedUsername.length() > MAX_USERNAME_LENGTH) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_username),
                context.getString(R.string.error_invalid_length, context.getString(R.string.error_field_username), MIN_USERNAME_LENGTH, MAX_USERNAME_LENGTH),
                ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        if (!USERNAME_PATTERN.matcher(trimmedUsername).matches()) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_username),
                context.getString(R.string.error_username_invalid_chars),
                ProfileValidationResult.ValidationErrorType.INVALID_FORMAT);
        }
        return ProfileValidationResult.success();
    }

    /**
     * 验证邮箱地址
     * @param email 邮箱地址
     * @return 验证结果
     */
    public static ProfileValidationResult validateEmail(String email) {
        Context context = MedicationRemindersApplication.getAppContext();
        if (email == null || email.trim().isEmpty()) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_email),
                context.getString(R.string.error_required_field_empty, context.getString(R.string.error_field_email)),
                ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
        }
        String trimmedEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_email),
                context.getString(R.string.error_invalid_format, context.getString(R.string.error_field_email)),
                ProfileValidationResult.ValidationErrorType.INVALID_FORMAT);
        }
        return ProfileValidationResult.success();
    }

    /**
     * 验证电话号码
     * @param phoneNumber 电话号码
     * @return 验证结果
     */
    public static ProfileValidationResult validatePhoneNumber(String phoneNumber) {
        Context context = MedicationRemindersApplication.getAppContext();
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_phone),
                context.getString(R.string.error_required_field_empty, context.getString(R.string.error_field_phone)),
                ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
        }
        String trimmedPhone = phoneNumber.trim();
        if (!PHONE_PATTERN.matcher(trimmedPhone).matches()) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_phone),
                context.getString(R.string.error_phone_invalid),
                ProfileValidationResult.ValidationErrorType.INVALID_FORMAT);
        }
        return ProfileValidationResult.success();
    }

    /**
     * 验证密码
     * @param password 密码
     * @return 验证结果
     */
    public static ProfileValidationResult validatePassword(String password) {
        Context context = MedicationRemindersApplication.getAppContext();
        if (password == null || password.isEmpty()) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_password),
                context.getString(R.string.error_required_field_empty, context.getString(R.string.error_field_password)),
                ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_password),
                context.getString(R.string.error_password_too_short, MIN_PASSWORD_LENGTH),
                ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_password),
                context.getString(R.string.error_password_too_long, MAX_PASSWORD_LENGTH),
                ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        return ProfileValidationResult.success();
    }

    /**
     * 验证完整姓名
     * @param fullName 完整姓名
     * @return 验证结果
     */
    public static ProfileValidationResult validateFullName(String fullName) {
        Context context = MedicationRemindersApplication.getAppContext();
        if (fullName == null || fullName.trim().isEmpty()) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_full_name),
                context.getString(R.string.error_required_field_empty, context.getString(R.string.error_field_full_name)),
                ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
        }
        String trimmedName = fullName.trim();
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_full_name),
                context.getString(R.string.error_invalid_length_max, context.getString(R.string.error_field_full_name), MAX_NAME_LENGTH),
                ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_full_name),
                context.getString(R.string.error_name_invalid_chars),
                ProfileValidationResult.ValidationErrorType.INVALID_FORMAT);
        }
        return ProfileValidationResult.success();
    }

    /**
     * 验证性别
     * @param gender 性别
     * @return 验证结果
     */
    public static ProfileValidationResult validateGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return ProfileValidationResult.failure("性别", "请选择性别", 
                ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
        }
        
        String trimmedGender = gender.trim();
        
        for (String validGender : VALID_GENDERS) {
            if (validGender.equals(trimmedGender)) {
                return ProfileValidationResult.success();
            }
        }
        
        return ProfileValidationResult.failure("性别", "请选择有效的性别选项", 
            ProfileValidationResult.ValidationErrorType.INVALID_FORMAT);
    }

    /**
     * 验证出生日期
     * @param birthDate 出生日期 (YYYY-MM-DD格式)
     * @return 验证结果
     */
    public static ProfileValidationResult validateBirthDate(String birthDate) {
        if (birthDate == null || birthDate.trim().isEmpty()) {
            return ProfileValidationResult.failure("出生日期", "出生日期不能为空", 
                ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
        }
        
        String trimmedDate = birthDate.trim();
        
        // 验证日期格式
        Date parsedDate;
        try {
            DATE_FORMAT.setLenient(false);
            parsedDate = DATE_FORMAT.parse(trimmedDate);
        } catch (ParseException e) {
            return ProfileValidationResult.failure("出生日期", 
                "出生日期格式不正确，请使用YYYY-MM-DD格式", 
                ProfileValidationResult.ValidationErrorType.INVALID_DATE);
        }
        
        // 检查日期不能是未来日期
        Date currentDate = new Date();
        Context context = MedicationRemindersApplication.getAppContext();
        if (parsedDate.after(currentDate)) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_birth_date),
                context.getString(R.string.error_invalid_date_future, context.getString(R.string.error_field_birth_date)),
                ProfileValidationResult.ValidationErrorType.INVALID_DATE);
        }
        
        // 计算年龄并验证
        Calendar birthCalendar = Calendar.getInstance();
        birthCalendar.setTime(parsedDate);
        
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);
        
        int age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);
        
        // 如果今年的生日还没到，年龄减1
        if (currentCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        
        if (age < MIN_AGE) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_birth_date),
                context.getString(R.string.error_invalid_age_min, MIN_AGE),
                ProfileValidationResult.ValidationErrorType.INVALID_AGE);
        }
        if (age > MAX_AGE) {
            return ProfileValidationResult.failure(
                context.getString(R.string.error_field_birth_date),
                context.getString(R.string.error_invalid_age_max, MAX_AGE),
                ProfileValidationResult.ValidationErrorType.INVALID_AGE);
        }
        
        return ProfileValidationResult.success();
    }

    /**
     * 验证个人资料照片路径
     * @param photoPath 照片路径
     * @return 验证结果
     */
    public static ProfileValidationResult validateProfilePhoto(String photoPath) {
        // 个人���料照片是可选的，所以null/empty是有效的
        if (photoPath == null || photoPath.trim().isEmpty()) {
            return ProfileValidationResult.success();
        }

        String trimmedPath = photoPath.trim();

        // 基本路径验证 - 检查是否看起来像有效的文件路径
        if (!trimmedPath.contains("/") && !trimmedPath.contains("\\")) {
            return ProfileValidationResult.failure("个人资料照片",
                "照片路径格式不正确",
                ProfileValidationResult.ValidationErrorType.FILE_ERROR);
        }

        // 检查常见的图片文件扩展名
        String lowerPath = trimmedPath.toLowerCase();
        if (!lowerPath.endsWith(".jpg") && !lowerPath.endsWith(".jpeg") && 
            !lowerPath.endsWith(".png") && !lowerPath.endsWith(".gif")) {
            return ProfileValidationResult.failure("个人资料照片",
                "照片文件格式不支持，请��用JPG、PNG或GIF格式",
                ProfileValidationResult.ValidationErrorType.FILE_ERROR);
        }

        return ProfileValidationResult.success();
    }

    /**
     * 验证文本字段长度
     * @param fieldName 字段名称
     * @param text 文本内容
     * @param maxLength 最大长度
     * @return 验证结果
     */
    public static ProfileValidationResult validateTextFieldLength(String fieldName, String text, int maxLength) {
        if (text != null && text.length() > maxLength) {
            return ProfileValidationResult.failure(fieldName, 
                fieldName + "长度不能超过" + maxLength + "个字符", 
                ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
        }
        
        return ProfileValidationResult.success();
    }

    /**
     * 验证紧急联系人信息的完整性
     * @param emergencyContactName 紧急联系人姓名
     * @param emergencyContactPhone 紧急联系人电话
     * @param emergencyContactRelation 与紧急联系人的关系
     * @return 验证结果
     */
    public static ProfileValidationResult validateEmergencyContactInfo(String emergencyContactName, 
                                                                      String emergencyContactPhone, 
                                                                      String emergencyContactRelation) {
        boolean hasName = emergencyContactName != null && !emergencyContactName.trim().isEmpty();
        boolean hasPhone = emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty();
        boolean hasRelation = emergencyContactRelation != null && !emergencyContactRelation.trim().isEmpty();
        
        // 如果提供了任何紧急联系人信息，则必须提供完整信息
        if (hasName || hasPhone || hasRelation) {
            if (!hasName) {
                return ProfileValidationResult.failure("紧急联系人", 
                    "紧急联系人姓名不能为空", 
                    ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
            }
            
            if (!hasPhone) {
                return ProfileValidationResult.failure("紧急联系人", 
                    "紧急联系人电话不能为空", 
                    ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
            }
            
            if (!hasRelation) {
                return ProfileValidationResult.failure("紧急联系人", 
                    "与紧急联系人的关系不能为空", 
                    ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
            }
            
            // 验证紧急联系人姓名
            ProfileValidationResult nameResult = validateFullName(emergencyContactName);
            if (!nameResult.isValid()) {
                return ProfileValidationResult.failure("紧急联系人姓名", 
                    nameResult.getErrorMessage(), 
                    nameResult.getErrorType());
            }
            
            // 验证紧急联系人电话
            ProfileValidationResult phoneResult = validatePhoneNumber(emergencyContactPhone);
            if (!phoneResult.isValid()) {
                return ProfileValidationResult.failure("紧急联系人电话", 
                    phoneResult.getErrorMessage(), 
                    phoneResult.getErrorType());
            }
            
            // 验证关系长度
            if (emergencyContactRelation.length() > 50) {
                return ProfileValidationResult.failure("紧急联系人关系", 
                    "关系描述不能超过50个字符", 
                    ProfileValidationResult.ValidationErrorType.INVALID_LENGTH);
            }
        }
        
        return ProfileValidationResult.success();
    }

    /**
     * 验证完整的User实体
     * @param user 要验证的User实体
     * @return 验证结果
     */
    public static ProfileValidationResult validateCompleteUser(User user) {
        if (user == null) {
            return ProfileValidationResult.failure("用户信息", "用户信息不能为空", 
                ProfileValidationResult.ValidationErrorType.REQUIRED_FIELD_EMPTY);
        }
        
        // 验证基本注册信息
        ProfileValidationResult registrationResult = validateRegistrationForm(
            user.getUsername(), user.getEmail(), user.getPhone(), user.getPassword());
        if (!registrationResult.isValid()) {
            return registrationResult;
        }
        
        // 验证扩展个人信息（如果提供）
        if (user.getFullName() != null || user.getGender() != null || user.getBirthDate() != null) {
            ProfileValidationResult profileResult = validateProfileForm(
                user.getFullName(), user.getGender(), user.getBirthDate());
            if (!profileResult.isValid()) {
                return profileResult;
            }
        }
        
        // 验证其他可选字段
        ProfileValidationResult photoResult = validateProfilePhoto(user.getProfilePhotoPath());
        if (!photoResult.isValid()) {
            return photoResult;
        }
        
        ProfileValidationResult bloodTypeResult = validateBloodType(user.getBloodType());
        if (!bloodTypeResult.isValid()) {
            return bloodTypeResult;
        }
        
        // 验证文本字段长度
        ProfileValidationResult allergiesResult = validateTextFieldLength("过敏信息", user.getAllergies(), MAX_TEXT_FIELD_LENGTH);
        if (!allergiesResult.isValid()) {
            return allergiesResult;
        }
        
        ProfileValidationResult medicalConditionsResult = validateTextFieldLength("既往病史", user.getMedicalConditions(), MAX_TEXT_FIELD_LENGTH);
        if (!medicalConditionsResult.isValid()) {
            return medicalConditionsResult;
        }
        
        ProfileValidationResult addressResult = validateTextFieldLength("地址", user.getAddress(), MAX_TEXT_FIELD_LENGTH);
        if (!addressResult.isValid()) {
            return addressResult;
        }
        
        // 验证紧急联系人信息
        ProfileValidationResult emergencyContactResult = validateEmergencyContactInfo(
            user.getEmergencyContactName(), user.getEmergencyContactPhone(), user.getEmergencyContactRelation());
        if (!emergencyContactResult.isValid()) {
            return emergencyContactResult;
        }
        
        // 验证备用电话（如果提供）
        if (user.getSecondaryPhone() != null && !user.getSecondaryPhone().trim().isEmpty()) {
            ProfileValidationResult secondaryPhoneResult = validatePhoneNumber(user.getSecondaryPhone());
            if (!secondaryPhoneResult.isValid()) {
                return ProfileValidationResult.failure("备用电话", 
                    secondaryPhoneResult.getErrorMessage(), 
                    secondaryPhoneResult.getErrorType());
            }
        }
        
        // 验证医生信息（如果提供）
        if (user.getDoctorName() != null && !user.getDoctorName().trim().isEmpty()) {
            ProfileValidationResult doctorNameResult = validateFullName(user.getDoctorName());
            if (!doctorNameResult.isValid()) {
                return ProfileValidationResult.failure("医生姓名", 
                    doctorNameResult.getErrorMessage(), 
                    doctorNameResult.getErrorType());
            }
        }
        
        if (user.getDoctorPhone() != null && !user.getDoctorPhone().trim().isEmpty()) {
            ProfileValidationResult doctorPhoneResult = validatePhoneNumber(user.getDoctorPhone());
            if (!doctorPhoneResult.isValid()) {
                return ProfileValidationResult.failure("医生电话", 
                    doctorPhoneResult.getErrorMessage(), 
                    doctorPhoneResult.getErrorType());
            }
        }
        
        ProfileValidationResult hospitalNameResult = validateTextFieldLength("医院名称", user.getHospitalName(), MAX_NAME_LENGTH);
        if (!hospitalNameResult.isValid()) {
            return hospitalNameResult;
        }
        
        return ProfileValidationResult.success();
    }

    /**
     * 获取有效的性别选项
     * @return ��效性别选项数组
     */
    public static String[] getValidGenders() {
        return VALID_GENDERS.clone();
    }

    /**
     * 从出生日期字符串计算年龄
     * @param birthDate YYYY-MM-DD格式的出生日期
     * @return 年龄，如果日期无效则返回-1
     */
    public static int calculateAge(String birthDate) {
        if (birthDate == null || birthDate.trim().isEmpty()) {
            return -1;
        }
        
        try {
            DATE_FORMAT.setLenient(false);
            Date parsedDate = DATE_FORMAT.parse(birthDate.trim());
            
            Calendar birthCalendar = Calendar.getInstance();
            birthCalendar.setTime(parsedDate);
            
            Calendar currentCalendar = Calendar.getInstance();
            
            int age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);
            
            // 如果今年的生日还没到，年龄减1
            if (currentCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            
            return age;
        } catch (ParseException e) {
            return -1;
        }
    }

    /**
     * 检查用户是否满足最低年龄要求
     * @param user 用户实体
     * @return 如果满足年龄要求则返回true
     */
    public static boolean meetsMinimumAge(User user) {
        if (user == null || user.getBirthDate() == null) {
            return false;
        }
        
        int age = calculateAge(user.getBirthDate());
        return age >= MIN_AGE;
    }



    /**
     * 验证结果类（为了向后兼容保留）
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

