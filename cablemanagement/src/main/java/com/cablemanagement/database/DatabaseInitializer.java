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
                
                // Better parsing: handle multi-line statements properly
                String[] statements = parseSQL(schemaContent);
                
                int successCount = 0;
                int errorCount = 0;
                
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty() && !sql.startsWith("--")) {
                        try {
                            stmt.execute(sql);
                            successCount++;
                            // Log successful view/table creation
                            if (sql.toUpperCase().startsWith("CREATE VIEW")) {
                                String viewName = extractViewName(sql);
                                System.out.println("Created view: " + viewName);
                            } else if (sql.toUpperCase().startsWith("CREATE TABLE")) {
                                String tableName = extractTableName(sql);
                                System.out.println("Created table: " + tableName);
                            }
                        } catch (SQLException e) {
                            // Some statements might fail if tables already exist, which is okay
                            if (!e.getMessage().contains("already exists")) {
                                System.err.println("Error executing SQL: " + sql.substring(0, Math.min(100, sql.length())) + "...");
                                System.err.println("Error: " + e.getMessage());
                                errorCount++;
                            }
                        }
                    }
                }
                
                System.out.println("Database initialized successfully with all required tables.");
                System.out.println("Successfully executed " + successCount + " statements" + 
                                 (errorCount > 0 ? ", " + errorCount + " errors" : ""));
            } else {
                System.err.println("Schema file not found: " + schemaPath);
            }
            
            stmt.close();
            connection.close();
            
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Parse SQL content into individual statements, handling multi-line statements properly
     */
    private static String[] parseSQL(String content) {
        // Remove comments and normalize whitespace
        String[] lines = content.split("\n");
        StringBuilder cleanContent = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            // Skip comment lines and empty lines
            if (!line.isEmpty() && !line.startsWith("--")) {
                cleanContent.append(line).append(" ");
            }
        }
        
        // Split by semicolon but be smarter about it
        String[] statements = cleanContent.toString().split(";");
        return statements;
    }
    
    /**
     * Extract view name from CREATE VIEW statement
     */
    private static String extractViewName(String sql) {
        try {
            String upperSQL = sql.toUpperCase();
            int startIndex = upperSQL.indexOf("VIEW") + 4;
            int endIndex = upperSQL.indexOf("AS", startIndex);
            if (startIndex > 3 && endIndex > startIndex) {
                return sql.substring(startIndex, endIndex).trim().replace("IF NOT EXISTS", "").trim();
            }
        } catch (Exception e) {
            // Ignore extraction errors
        }
        return "Unknown";
    }
    
    /**
     * Extract table name from CREATE TABLE statement
     */
    private static String extractTableName(String sql) {
        try {
            String upperSQL = sql.toUpperCase();
            int startIndex = upperSQL.indexOf("TABLE") + 5;
            int endIndex = upperSQL.indexOf("(", startIndex);
            if (startIndex > 4 && endIndex > startIndex) {
                return sql.substring(startIndex, endIndex).trim().replace("IF NOT EXISTS", "").trim();
            }
        } catch (Exception e) {
            // Ignore extraction errors
        }
        return "Unknown";
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
