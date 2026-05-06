package org.docbook.util;

import org.docbook.entities.users.User;

public final class AppState {
    private static User currentUser; // Changed from Integer to User
    private static Integer selectedDossierId;

    private AppState() {}

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }

    public static Integer getSelectedDossierId() { return selectedDossierId; }
    public static void setSelectedDossierId(Integer dossierId) { selectedDossierId = dossierId; }
}