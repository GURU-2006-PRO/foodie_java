package com.foodscanner.patterns.singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SINGLETON PATTERN
 * Ensures only one database connection instance exists throughout the app.
 */
public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:foodscanner.db";

    private DatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            initSchema();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get connection", e);
        }
        return connection;
    }

    private void initSchema() throws SQLException {
        String scanHistory = """
            CREATE TABLE IF NOT EXISTS scan_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                food_name TEXT NOT NULL,
                scan_result TEXT NOT NULL,
                health_warnings TEXT,
                health_score INTEGER,
                scanned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )""";
        String userConditions = """
            CREATE TABLE IF NOT EXISTS user_conditions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                condition_name TEXT NOT NULL UNIQUE
            )""";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(scanHistory);
            stmt.execute(userConditions);
            stmt.execute("INSERT OR IGNORE INTO user_conditions (condition_name) VALUES ('diabetes')");
            stmt.execute("INSERT OR IGNORE INTO user_conditions (condition_name) VALUES ('hypertension')");
            stmt.execute("INSERT OR IGNORE INTO user_conditions (condition_name) VALUES ('obesity')");
        }
    }
}
