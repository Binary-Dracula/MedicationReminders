package com.medication.reminders;

/**
 * UserInfo data model class for user registration
 * Contains user information including username, phone number, email, and password
 */
public class UserInfo {
    private String username;
    private String phoneNumber;
    private String email;
    private String password;

    /**
     * Default constructor
     */
    public UserInfo() {
    }

    /**
     * Constructor with all fields
     * @param username User's username (3-20 characters)
     * @param phoneNumber User's phone number (11-digit Chinese format)
     * @param email User's email address
     * @param password User's password (6-20 characters)
     */
    public UserInfo(String username, String phoneNumber, String email, String password) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
    }

    /**
     * Get username
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username
     * @param username username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get phone number
     * @return phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Set phone number
     * @param phoneNumber phone number to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Get email
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set email
     * @param email email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get password
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password
     * @param password password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "username='" + username + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}