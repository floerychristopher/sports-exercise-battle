package com.seb.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    // Connection parameters
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/seb_db";
    private static final String DB_USER = "webserver";
    private static final String DB_PASSWORD = "webserver";

    // To store instance of DatabaseConfig
    private static DatabaseConfig instance;

    // Load PostgreSQL driver in memory
    private DatabaseConfig() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL driver not found");
            e.printStackTrace();
            throw new RuntimeException("Failed to load database driver", e);
        }
    }

    // Get DatabaseConfig instance
    // "synchronized" ensures that only one instance of DatabaseConfig exist (Singleton)
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    // Creates and returns new connection do DB
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Safely close connection
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Failed to close database connection");
                e.printStackTrace();
            }
        }
    }

    public boolean testConnection() {
        Connection connection = null;
        try {
            connection = getConnection();
            return connection.isValid(5); // Test with 5-second timeout
        } catch (SQLException e) {
            System.err.println("Database connection test failed");
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(connection);
        }
    }

    // === Test database connection ===

    public static void main(String[] args) {
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        boolean isConnected = dbConfig.testConnection();

        if(isConnected) {
            System.out.println("DB connection successful!");
            // Executing simple query to further verify
            try (Connection conn = dbConfig.getConnection()) {
                System.out.println("Connection valid: " + conn.isValid(5));
                System.out.println("Database product name: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("Database version: " + conn.getMetaData().getDatabaseProductVersion());
            } catch (SQLException e) {
                System.err.println("Error executing test query: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Failed to connect to DB...");
        }
    }
}
