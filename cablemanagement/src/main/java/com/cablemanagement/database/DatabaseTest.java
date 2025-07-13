package com.cablemanagement.database;

import java.sql.*;

public class DatabaseTest {
    public static void main(String[] args) {
        try {
            // Connect to the database
            String jdbcUrl = "jdbc:sqlite:cable_management.db";
            Connection connection = DriverManager.getConnection(jdbcUrl);
            
            // Check what tables exist
            String checkTablesQuery = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(checkTablesQuery);
            
            System.out.println("Existing tables:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("name"));
            }
            rs.close();
            
            // Check if User table exists and print all users
            String checkTableQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='User'";
            ResultSet userTableRs = stmt.executeQuery(checkTableQuery);
            
            if (userTableRs.next()) {
                System.out.println("\nUser table exists!");
                
                // Get all users
                String getUsersQuery = "SELECT username, password_hash, role, is_active FROM User";
                ResultSet userRs = stmt.executeQuery(getUsersQuery);
                
                System.out.println("Available users:");
                while (userRs.next()) {
                    String username = userRs.getString("username");
                    String password = userRs.getString("password_hash");
                    String role = userRs.getString("role");
                    int isActive = userRs.getInt("is_active");
                    
                    System.out.println("Username: " + username + 
                                     ", Password: " + password + 
                                     ", Role: " + role + 
                                     ", Active: " + (isActive == 1 ? "Yes" : "No"));
                }
                userRs.close();
            } else {
                System.out.println("\nUser table does not exist!");
                System.out.println("Creating User table and adding default users...");
                
                // Create User table
                String createUserTable = "CREATE TABLE IF NOT EXISTS User (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL UNIQUE," +
                    "password_hash TEXT NOT NULL," +
                    "role TEXT DEFAULT 'user'," +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "is_active INTEGER DEFAULT 1" +
                    ")";
                
                stmt.execute(createUserTable);
                
                // Insert default users
                String insertUsers = "INSERT INTO User (username, password_hash, role) VALUES " +
                    "('admin', 'admin123', 'admin')," +
                    "('cashier1', 'cash123', 'cashier')," +
                    "('manager1', 'manager123', 'manager')";
                
                stmt.execute(insertUsers);
                
                System.out.println("User table created and default users added!");
                System.out.println("Default credentials:");
                System.out.println("- admin / admin123");
                System.out.println("- cashier1 / cash123");
                System.out.println("- manager1 / manager123");
            }
            
            userTableRs.close();
            stmt.close();
            connection.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
