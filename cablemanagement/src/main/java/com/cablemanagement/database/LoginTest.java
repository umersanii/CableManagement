package com.cablemanagement.database;

public class LoginTest {
    public static void main(String[] args) {
        System.out.println("Testing login functionality...");
        
        // Create database instance (should auto-connect)
        SQLiteDatabase db = new SQLiteDatabase();
        
        // Test if connection is working
        System.out.println("Database connected: " + db.isConnected());
        
        // Test login credentials
        String[] testUsers = {"admin", "cashier1", "manager1"};
        String[] testPasswords = {"admin123", "cash123", "manager123"};
        
        for (int i = 0; i < testUsers.length; i++) {
            String username = testUsers[i];
            String password = testPasswords[i];
            
            boolean loginSuccess = db.SignIn(username, password);
            System.out.println("Login test for " + username + "/" + password + ": " + 
                             (loginSuccess ? "SUCCESS" : "FAILED"));
        }
        
        // Test with wrong credentials
        boolean wrongLogin = db.SignIn("admin", "wrongpassword");
        System.out.println("Login test with wrong password: " + 
                         (wrongLogin ? "SUCCESS (BAD!)" : "FAILED (GOOD)"));
        
        // Disconnect
        db.disconnect();
        System.out.println("Testing completed.");
    }
}
