package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class myDataBase {
    // UPDATED: Changed 'pidev_test' to 'pidev' to match your pgAdmin
    private static final String URL = "jdbc:postgresql://127.0.0.1:32770/pidev";
    private static final String USER = "pidev";
    private static final String PASSWORD = "pidev";

    private static myDataBase instance;
    private Connection connection;

    private myDataBase() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established to 'pidev'!");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found", e);
        }
    }

    public static myDataBase getInstance() throws SQLException {
        if (instance == null) {
            instance = new myDataBase();
        } else if (instance.getConnection().isClosed()) {
            instance = new myDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}