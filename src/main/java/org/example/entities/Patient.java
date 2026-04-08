package org.example.entities;

public class Patient extends User {

    public Patient() {
        super();
        this.setDtype("patient");
        this.setRole("ROLE_PATIENT");
    }
}