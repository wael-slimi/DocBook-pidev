package org.example.entities;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String role;     // ROLE_DOCTOR or ROLE_PATIENT
    private String dtype;    // doctor or patient
    private boolean isActive = true;
    private boolean isVerified = true;

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
}