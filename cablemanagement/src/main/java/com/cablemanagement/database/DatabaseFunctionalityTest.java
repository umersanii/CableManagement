package com.cablemanagement.database;

import com.cablemanagement.config;

public class DatabaseFunctionalityTest {
    public static void main(String[] args) {
        System.out.println("Testing Database Functionality...");
        
        if (config.database == null) {
            System.out.println("❌ Database is null");
            return;
        }
        
        if (!config.database.isConnected()) {
            System.out.println("❌ Database not connected");
            return;
        }
        
        System.out.println("✅ Database connected successfully");
        
        // Test category operations
        System.out.println("\n=== Testing Categories ===");
        boolean categoryInserted = config.database.insertCategory("Test Category");
        System.out.println("Category insert: " + (categoryInserted ? "✅ SUCCESS" : "❌ FAILED"));
        
        System.out.println("Existing categories: " + config.database.getAllCategories().size());
        
        // Test province operations
        System.out.println("\n=== Testing Provinces ===");
        boolean provinceInserted = config.database.insertProvince("Test Province");
        System.out.println("Province insert: " + (provinceInserted ? "✅ SUCCESS" : "❌ FAILED"));
        
        System.out.println("Existing provinces: " + config.database.getAllProvinces().size());
        
        // Test user operations
        System.out.println("\n=== Testing Users ===");
        boolean userInserted = config.database.insertUser("testuser", "testpass", "user");
        System.out.println("User insert: " + (userInserted ? "✅ SUCCESS" : "❌ FAILED"));
        
        System.out.println("Existing users: " + config.database.getAllUsers().size());
        
        // Test login
        System.out.println("\n=== Testing Login ===");
        boolean loginWorked = config.database.SignIn("testuser", "testpass");
        System.out.println("Test login: " + (loginWorked ? "✅ SUCCESS" : "❌ FAILED"));
        
        // Test password change
        System.out.println("\n=== Testing Password Change ===");
        boolean passwordChanged = config.database.changePassword("testuser", "testpass", "newpass");
        System.out.println("Password change: " + (passwordChanged ? "✅ SUCCESS" : "❌ FAILED"));
        
        boolean newLoginWorked = config.database.SignIn("testuser", "newpass");
        System.out.println("Login with new password: " + (newLoginWorked ? "✅ SUCCESS" : "❌ FAILED"));
        
        System.out.println("\n=== Database Functionality Test Complete ===");
    }
}
