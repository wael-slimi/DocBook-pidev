package org.docbook.entities.users;

public class Patient extends User {

    public Patient() {
        super();
        this.setDtype("patient");
        this.setRole("ROLE_PATIENT");
    }
}