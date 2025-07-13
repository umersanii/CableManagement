package com.cablemanagement.database;

import java.sql.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseInitializer {
    
    public static void initializeDatabase(String databasePath, String schemaPath) {
        try {
            // Connect to SQLite database (creates it if it doesn't exist)
            String jdbcUrl = "jdbc:sqlite:" + databasePath;
            Connection connection = DriverManager.getConnection(jdbcUrl);
            
            // Enable foreign key constraints
            Statement stmt = connection.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Read and execute the schema file
            if (Files.exists(Paths.get(schemaPath))) {
                String schemaContent = new String(Files.readAllBytes(Paths.get(schemaPath)));
                
                // Split by semicolon to execute individual statements
                String[] statements = schemaContent.split(";");
                
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty() && !sql.startsWith("--")) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException e) {
                            // Some statements might fail if tables already exist, which is okay
                            if (!e.getMessage().contains("already exists")) {
                                System.err.println("Error executing SQL: " + sql);
                                System.err.println("Error: " + e.getMessage());
                            }
                        }
                    }
                }
                
                System.out.println("Database initialized successfully!");
            } else {
                System.err.println("Schema file not found: " + schemaPath);
            }
            
            stmt.close();
            connection.close();
            
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        String databasePath = "cable_management.db";
        String schemaPath = "Cable_pos_schema.sql";
        
        // Try relative path first, then absolute path
        if (!Files.exists(Paths.get(schemaPath))) {
            schemaPath = "c:\\Users\\fa33z\\Desktop\\CODOC\\CableManagement\\Cable_pos_schema.sql";
        }
        
        initializeDatabase(databasePath, schemaPath);
    }
}
