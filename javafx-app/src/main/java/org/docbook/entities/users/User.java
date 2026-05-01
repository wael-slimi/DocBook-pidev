package org.docbook.entities.users;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String role;     // ROLE_DOCTOR or ROLE_PATIENT
    private String dtype;    // doctor or patient
    private boolean isActive = true;
    private boolean isVerified = false; // Default to false for new users
    private String status; // Now matches your DB column
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    private String verificationCode;
    private String avatarUrl;
    private String themePreference = "light";

    public User() {}

    // Constructor for registration
    public User(String name, String email, String password, String role, String dtype) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.dtype = dtype;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDtype() { return dtype; }
    public void setDtype(String dtype) { this.dtype = dtype; }
    public boolean isIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
    public boolean isIsVerified() { return isVerified; }
    public void setIsVerified(boolean isVerified) { this.isVerified = isVerified; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getThemePreference() { return themePreference; }
    public void setThemePreference(String themePreference) { this.themePreference = themePreference; }
}
