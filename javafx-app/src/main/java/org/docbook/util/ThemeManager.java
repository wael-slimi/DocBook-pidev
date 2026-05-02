package org.docbook.util;

import javafx.scene.Scene;
import javafx.scene.Parent;
import org.docbook.entities.users.User;

import java.util.HashSet;
import java.util.Set;

public class ThemeManager {

    private static final String LIGHT_CSS = "/css/style.css";
    private static final String DARK_CSS = "/css/dark.css";
    private static String currentTheme = "light";
    private static Scene activeScene;

    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        activeScene = scene;

        scene.getStylesheets().clear();
        scene.getStylesheets().add(currentTheme.equals("dark") ? ThemeManager.class.getResource(DARK_CSS).toExternalForm() : ThemeManager.class.getResource(LIGHT_CSS).toExternalForm());

        if (scene.getRoot() instanceof Parent) {
            ((Parent) scene.getRoot()).requestLayout();
        }
    }

    public static void toggleTheme() {
        currentTheme = currentTheme.equals("dark") ? "light" : "dark";
        if (activeScene != null) {
            applyTheme(activeScene);
        }
    }

    public static void setTheme(String theme) {
        if ("dark".equalsIgnoreCase(theme) || "light".equalsIgnoreCase(theme)) {
            currentTheme = theme.toLowerCase();
        }
        if (activeScene != null) {
            applyTheme(activeScene);
        }
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }

    public static void loadFromUser(User user) {
        if (user != null) {
            String pref = "light";
            try {
                java.lang.reflect.Field f = user.getClass().getDeclaredField("themePreference");
                f.setAccessible(true);
                String val = (String) f.get(user);
                if (val != null) pref = val;
            } catch (Exception e) {
                pref = "light";
            }
            currentTheme = pref;
        }
    }
}
