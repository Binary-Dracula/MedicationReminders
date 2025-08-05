package com.medication.reminders.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * User实体类，用于Room数据库
 * 包含用户的所有信息，包括基本认证信息、个人资料、医疗信息和会话管理
 */
@Entity(tableName = "users")
public class User {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    // 基本注册信息
    @ColumnInfo(name = "username")
    @NonNull
    private String username;
    
    @ColumnInfo(name = "email")
    @NonNull
    private String email;
    
    @ColumnInfo(name = "phone")
    @NonNull
    private String phone;
    
    @ColumnInfo(name = "password")
    @NonNull
    private String password;  // 明文存储
    
    // 详细个人资料字段
    @ColumnInfo(name = "full_name")
    private String fullName;        // 完整姓名
    
    @ColumnInfo(name = "gender")
    private String gender;          // 性别 (男性/女性/不愿透露)
    
    @ColumnInfo(name = "birth_date")
    private String birthDate;       // 出生日期 (YYYY-MM-DD格式)
    
    @ColumnInfo(name = "profile_photo_path")
    private String profilePhotoPath; // 个人资料照片路径
    
    // 扩展联系方式
    @ColumnInfo(name = "secondary_phone")
    private String secondaryPhone;   // 备用电话号码
    
    @ColumnInfo(name = "emergency_contact_name")
    private String emergencyContactName; // 紧急联系人姓名
    
    @ColumnInfo(name = "emergency_contact_phone")
    private String emergencyContactPhone; // 紧急联系人电话
    
    @ColumnInfo(name = "emergency_contact_relation")
    private String emergencyContactRelation; // 与紧急联系人关系
    
    // 地址信息
    @ColumnInfo(name = "address")
    private String address;         // 详细地址
    
    // 医疗相关信息
    @ColumnInfo(name = "blood_type")
    private String bloodType;       // 血型
    
    @ColumnInfo(name = "allergies")
    private String allergies;       // 过敏信息
    
    @ColumnInfo(name = "medical_conditions")
    private String medicalConditions; // 既往病史
    
    @ColumnInfo(name = "doctor_name")
    private String doctorName;      // 主治医生姓名
    
    @ColumnInfo(name = "doctor_phone")
    private String doctorPhone;     // 主治医生电话
    
    @ColumnInfo(name = "hospital_name")
    private String hospitalName;    // 常去医院名称
    
    // 会话管理字段
    @ColumnInfo(name = "is_logged_in")
    private boolean isLoggedIn;
    
    @ColumnInfo(name = "remember_me")
    private boolean rememberMe;
    
    @ColumnInfo(name = "last_login_time")
    private long lastLoginTime;
    
    @ColumnInfo(name = "login_attempts")
    private int loginAttempts;
    
    @ColumnInfo(name = "last_attempt_time")
    private long lastAttemptTime;
    
    // 元数据
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    /**
     * 默认构造函数
     */
    public User() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isLoggedIn = false;
        this.rememberMe = false;
        this.loginAttempts = 0;
    }
    
    /**
     * 基本注册信息构造函数
     * @param username 用户名
     * @param email 邮箱地址
     * @param phone 电话号码
     * @param password 密码
     */
    @Ignore
    public User(@NonNull String username, @NonNull String email, @NonNull String phone, @NonNull String password) {
        this();
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }
    
    /**
     * 完整构造函数（用于测试）
     */
    @Ignore
    public User(@NonNull String username, @NonNull String email, @NonNull String phone, @NonNull String password,
                String fullName, String gender, String birthDate, String profilePhotoPath) {
        this(username, email, phone, password);
        this.fullName = fullName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.profilePhotoPath = profilePhotoPath;
    }
    
    // Getters and Setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @NonNull
    public String getUsername() {
        return username;
    }
    
    public void setUsername(@NonNull String username) {
        this.username = username;
        updateTimestamp();
    }
    
    @NonNull
    public String getEmail() {
        return email;
    }
    
    public void setEmail(@NonNull String email) {
        this.email = email;
        updateTimestamp();
    }
    
    @NonNull
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(@NonNull String phone) {
        this.phone = phone;
        updateTimestamp();
    }
    
    @NonNull
    public String getPassword() {
        return password;
    }
    
    public void setPassword(@NonNull String password) {
        this.password = password;
        updateTimestamp();
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
        updateTimestamp();
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
        updateTimestamp();
    }
    
    public String getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
        updateTimestamp();
    }
    
    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }
    
    public void setProfilePhotoPath(String profilePhotoPath) {
        this.profilePhotoPath = profilePhotoPath;
        updateTimestamp();
    }
    
    public String getSecondaryPhone() {
        return secondaryPhone;
    }
    
    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
        updateTimestamp();
    }
    
    public String getEmergencyContactName() {
        return emergencyContactName;
    }
    
    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
        updateTimestamp();
    }
    
    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }
    
    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
        updateTimestamp();
    }
    
    public String getEmergencyContactRelation() {
        return emergencyContactRelation;
    }
    
    public void setEmergencyContactRelation(String emergencyContactRelation) {
        this.emergencyContactRelation = emergencyContactRelation;
        updateTimestamp();
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
        updateTimestamp();
    }
    
    public String getBloodType() {
        return bloodType;
    }
    
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
        updateTimestamp();
    }
    
    public String getAllergies() {
        return allergies;
    }
    
    public void setAllergies(String allergies) {
        this.allergies = allergies;
        updateTimestamp();
    }
    
    public String getMedicalConditions() {
        return medicalConditions;
    }
    
    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
        updateTimestamp();
    }
    
    public String getDoctorName() {
        return doctorName;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
        updateTimestamp();
    }
    
    public String getDoctorPhone() {
        return doctorPhone;
    }
    
    public void setDoctorPhone(String doctorPhone) {
        this.doctorPhone = doctorPhone;
        updateTimestamp();
    }
    
    public String getHospitalName() {
        return hospitalName;
    }
    
    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
        updateTimestamp();
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
        updateTimestamp();
    }
    
    public boolean isRememberMe() {
        return rememberMe;
    }
    
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
        updateTimestamp();
    }
    
    public long getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
        updateTimestamp();
    }
    
    public int getLoginAttempts() {
        return loginAttempts;
    }
    
    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
        updateTimestamp();
    }
    
    public long getLastAttemptTime() {
        return lastAttemptTime;
    }
    
    public void setLastAttemptTime(long lastAttemptTime) {
        this.lastAttemptTime = lastAttemptTime;
        updateTimestamp();
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 更新时间戳
     */
    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 检查是否有基本信息
     * @return 如果用户名、邮箱和电话都不为空则返回true
     */
    public boolean hasBasicInfo() {
        return username != null && !username.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty();
    }
    
    /**
     * 检查是否有扩展个人信息
     * @return 如果姓名、性别和出生日期都不为空则返回true
     */
    public boolean hasExtendedInfo() {
        return fullName != null && !fullName.trim().isEmpty() &&
               gender != null && !gender.trim().isEmpty() &&
               birthDate != null && !birthDate.trim().isEmpty();
    }
    
    /**
     * 检查个人资料是否完整
     * @return 如果有基本信息和扩展信息则返回true
     */
    public boolean isProfileComplete() {
        return hasBasicInfo() && hasExtendedInfo();
    }
    
    /**
     * 检查是否有个人资料照片
     * @return 如果有照片路径则返回true
     */
    public boolean hasProfilePhoto() {
        return profilePhotoPath != null && !profilePhotoPath.trim().isEmpty();
    }
    
    /**
     * 检查是否有医疗信息
     * @return 如果有任何医疗相关信息则返回true
     */
    public boolean hasMedicalInfo() {
        return (bloodType != null && !bloodType.trim().isEmpty()) ||
               (allergies != null && !allergies.trim().isEmpty()) ||
               (medicalConditions != null && !medicalConditions.trim().isEmpty()) ||
               (doctorName != null && !doctorName.trim().isEmpty()) ||
               (doctorPhone != null && !doctorPhone.trim().isEmpty()) ||
               (hospitalName != null && !hospitalName.trim().isEmpty());
    }
    
    /**
     * 检查是否有紧急联系人信息
     * @return 如果有紧急联系人信息则返回true
     */
    public boolean hasEmergencyContact() {
        return emergencyContactName != null && !emergencyContactName.trim().isEmpty() &&
               emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty();
    }
    
    /**
     * 重置登录尝试次数
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.lastAttemptTime = 0;
        updateTimestamp();
    }
    
    /**
     * 增加登录尝试次数
     */
    public void incrementLoginAttempts() {
        this.loginAttempts++;
        this.lastAttemptTime = System.currentTimeMillis();
        updateTimestamp();
    }
    
    /**
     * 设置登录状态
     * @param loggedIn 是否已登录
     */
    public void setLoginStatus(boolean loggedIn) {
        this.isLoggedIn = loggedIn;
        if (loggedIn) {
            this.lastLoginTime = System.currentTimeMillis();
            resetLoginAttempts(); // 成功登录后重置尝试次数
        }
        updateTimestamp();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", password='[PROTECTED]'" +
                ", fullName='" + fullName + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", profilePhotoPath='" + profilePhotoPath + '\'' +
                ", secondaryPhone='" + secondaryPhone + '\'' +
                ", emergencyContactName='" + emergencyContactName + '\'' +
                ", emergencyContactPhone='" + emergencyContactPhone + '\'' +
                ", emergencyContactRelation='" + emergencyContactRelation + '\'' +
                ", address='" + address + '\'' +
                ", bloodType='" + bloodType + '\'' +
                ", allergies='" + allergies + '\'' +
                ", medicalConditions='" + medicalConditions + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", doctorPhone='" + doctorPhone + '\'' +
                ", hospitalName='" + hospitalName + '\'' +
                ", isLoggedIn=" + isLoggedIn +
                ", rememberMe=" + rememberMe +
                ", lastLoginTime=" + lastLoginTime +
                ", loginAttempts=" + loginAttempts +
                ", lastAttemptTime=" + lastAttemptTime +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}