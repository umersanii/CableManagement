package com.cablemanagement.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cablemanagement.model.Bank;
import com.cablemanagement.model.BankTransaction;
import com.cablemanagement.model.Brand;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Manufacturer;
import com.cablemanagement.model.RawStockPurchaseItem;
import com.cablemanagement.model.RawStockUseItem;
import com.cablemanagement.model.Supplier;


public class SQLiteDatabase implements db {
    
    private Connection connection;
    private String databasePath;
    
    // Implement missing methods from db interface

    @Override
    public boolean updateBankBalance(double newBalance) {
        // Update the balance for all banks (or you may want to specify a bank_id)
        String query = "UPDATE Bank SET balance = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, newBalance);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteBank(int bankId) {
        String query = "DELETE FROM Bank WHERE bank_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, bankId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public double getCashBalance() {
        // This method is similar to getCurrentCashBalance()
        return getCurrentCashBalance();
    }

    @Override
    public boolean insertCashTransaction(BankTransaction transaction) {
        // Assuming BankTransaction has getters for required fields
        String query = "INSERT INTO Cash_Transaction (transaction_type, amount, description, transaction_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, transaction.getTransactionType());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getTransactionDate());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateBank(Bank bank) {
        String query = "UPDATE Bank SET branch_name = ?, balance = ?, account_number = ? WHERE bank_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, bank.getBranchName());
            pstmt.setDouble(2, bank.getBalance());
            pstmt.setString(3, bank.getAccountNumber());
            pstmt.setInt(4, bank.getBankId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

public boolean insertBankTransaction(BankTransaction transaction) {
    String query = "INSERT INTO Bank_Transaction (bank_id, transaction_date, transaction_type, amount, description, related_bank_id) " +
                   "VALUES (?, ?, ?, ?, ?, ?)";
    
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        
        // Verify bank_id exists first
        if (!bankExists(transaction.getBankId())) {
            System.err.println("Bank ID " + transaction.getBankId() + " doesn't exist");
            return false;
        }
        
        // Verify related_bank_id exists if specified
        if (transaction.getRelatedBankId() != 0 && !bankExists(transaction.getRelatedBankId())) {
            System.err.println("Related Bank ID " + transaction.getRelatedBankId() + " doesn't exist");
            return false;
        }
        
        pstmt.setInt(1, transaction.getBankId());
        pstmt.setString(2, transaction.getTransactionDate());
        pstmt.setString(3, transaction.getTransactionType());
        pstmt.setDouble(4, transaction.getAmount());
        pstmt.setString(5, transaction.getDescription());
        
        // Handle null related_bank_id
        if (transaction.getRelatedBankId() != 0) {
            pstmt.setInt(6, transaction.getRelatedBankId());
        } else {
            pstmt.setNull(6, Types.INTEGER);
        }
        
        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Error inserting transaction: " + e.getMessage());
        return false;
    }
}

private boolean bankExists(int bankId) throws SQLException {
    String query = "SELECT 1 FROM Bank WHERE bank_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setInt(1, bankId);
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next();
        }
    }
}
    public SQLiteDatabase() {
        this.databasePath = "cable_management.db";
        // Auto-connect when instantiated
        connect(null, null, null);
        // Initialize all required tables
        initializeDatabase();
        // Migrate schema if needed
        migrateSchema();
    }
    
    public SQLiteDatabase(String databasePath) {
        this.databasePath = databasePath;
        // Auto-connect when instantiated
        connect(databasePath, null, null);
        // Initialize all required tables
        initializeDatabase();
        // Migrate schema if needed
        migrateSchema();
    }

    @Override
    public String connect(String url, String user, String password) {
        try {
            String jdbcUrl = "jdbc:sqlite:" + (url != null ? url : databasePath);
            connection = DriverManager.getConnection(jdbcUrl);
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            
            return "Connected to SQLite database successfully";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed to connect: " + e.getMessage();
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Object executeQuery(String query) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            List<List<Object>> results = new ArrayList<>();
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                results.add(row);
            }
            return results;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int executeUpdate(String query) {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean SignIn(String userId, String password) {
        // Ensure User table exists first
        ensureUserTableExists();
        
        String query = "SELECT COUNT(*) FROM User WHERE username = ? AND password_hash = ? AND is_active = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void ensureUserTableExists() {
        try {
            // Check if User table exists
            String checkTableQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='User'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(checkTableQuery);
            
            if (!rs.next()) {
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
                
                System.out.println("User table created with default credentials:");
                System.out.println("- admin / admin123");
                System.out.println("- cashier1 / cash123");
                System.out.println("- manager1 / manager123");
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeDatabase() {
        try {
            Statement stmt = connection.createStatement();
            
            // Create all required tables based on the schema
                String[] createTableQueries = {
                    // Province table
                    "CREATE TABLE IF NOT EXISTS Province (" +
                    "province_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "province_name TEXT NOT NULL UNIQUE" +
                    ")",
                    
                    // District table
                    "CREATE TABLE IF NOT EXISTS District (" +
                    "district_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "district_name TEXT NOT NULL," +
                    "province_id INTEGER NOT NULL," +
                    "FOREIGN KEY (province_id) REFERENCES Province(province_id)" +
                    ")",
                    
                    // Tehsil table
                    "CREATE TABLE IF NOT EXISTS Tehsil (" +
                    "tehsil_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "tehsil_name TEXT NOT NULL," +
                    "district_id INTEGER NOT NULL," +
                    "FOREIGN KEY (district_id) REFERENCES District(district_id)" +
                    ")",
                    
                    // Category table
                    "CREATE TABLE IF NOT EXISTS Category (" +
                    "category_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "category_name TEXT NOT NULL UNIQUE" +
                    ")",
                    
                    // Designation table
                    "CREATE TABLE IF NOT EXISTS Designation (" +
                    "designation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "designation_title TEXT NOT NULL UNIQUE" +
                    ")",
                    
                    // Manufacturer table
                    "CREATE TABLE IF NOT EXISTS Manufacturer (" +
                    "manufacturer_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "manufacturer_name TEXT NOT NULL," +
                    "tehsil_id INTEGER NOT NULL," +
                    "FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)" +
                    ")",
                    
                    // Brand table
                    "CREATE TABLE IF NOT EXISTS Brand (" +
                    "brand_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "brand_name TEXT NOT NULL," +
                    "manufacturer_id INTEGER NOT NULL," +
                    "FOREIGN KEY (manufacturer_id) REFERENCES Manufacturer(manufacturer_id)" +
                    ")",
                    
                    // Customer table
                    "CREATE TABLE IF NOT EXISTS Customer (" +
                    "customer_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "customer_name TEXT NOT NULL," +
                    "contact_number TEXT," +
                    "address TEXT," +
                    "tehsil_id INTEGER NOT NULL," +
                    "FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)" +
                    ")",
                    
                    // Supplier table
                    "CREATE TABLE IF NOT EXISTS Supplier (" +
                    "supplier_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "supplier_name TEXT NOT NULL," +
                    "contact_number TEXT," +
                    "address TEXT," +
                    "tehsil_id INTEGER NOT NULL," +
                    "FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)" +
                    ")",
                    
                    // Bank table
                    "CREATE TABLE IF NOT EXISTS Bank (" +
                    "bank_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "bank_name TEXT NOT NULL," +
                    "account_number TEXT," +
                    "branch_name TEXT," +
                    "balance REAL DEFAULT 0.0" +
                    ")",
                    
                    // Employee table
                    "CREATE TABLE IF NOT EXISTS Employee (" +
                    "employee_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "employee_name TEXT NOT NULL," +
                    "phone_number TEXT," +
                    "cnic TEXT," +
                    "address TEXT," +
                    "hire_date TEXT NOT NULL," +
                    "designation_id INTEGER NOT NULL," +
                    "salary_type TEXT NOT NULL CHECK(salary_type IN ('monthly', 'daily', 'hourly', 'task'))," +
                    "salary_amount REAL NOT NULL," +
                    "is_active INTEGER DEFAULT 1," +
                    "FOREIGN KEY (designation_id) REFERENCES Designation(designation_id)" +
                    ")",
                    
                    // Employee Attendance table
                    "CREATE TABLE IF NOT EXISTS Employee_Attendance (" +
                    "attendance_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "employee_id INTEGER NOT NULL," +
                    "attendance_date TEXT NOT NULL," +
                    "status TEXT NOT NULL CHECK(status IN ('present', 'absent', 'leave'))," +
                    "working_hours REAL DEFAULT 0," +
                    "FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)" +
                    ")",
                    
                    // Employee Advance Salary table
                    "CREATE TABLE IF NOT EXISTS Employee_Advance_Salary (" +
                    "advance_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "employee_id INTEGER NOT NULL," +
                    "amount REAL NOT NULL," +
                    "advance_date TEXT NOT NULL," +
                    "description TEXT," +
                    "status TEXT DEFAULT 'granted' CHECK(status IN ('granted', 'adjusted', 'refunded'))," +
                    "created_date TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)" +
                    ")",
                    
                    // Employee Loan table
                    "CREATE TABLE IF NOT EXISTS Employee_Loan (" +
                    "loan_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "employee_id INTEGER NOT NULL," +
                    "loan_amount REAL NOT NULL," +
                    "loan_date TEXT NOT NULL," +
                    "due_date TEXT," +
                    "description TEXT," +
                    "status TEXT DEFAULT 'active' CHECK(status IN ('active', 'paid', 'defaulted', 'written_off'))," +
                    "remaining_amount REAL NOT NULL," +
                    "created_date TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)" +
                    ")",
                    
                    // Salesman table
                    "CREATE TABLE IF NOT EXISTS Salesman (" +
                    "salesman_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "salesman_name TEXT NOT NULL," +
                    "contact_number TEXT," +
                    "address TEXT," +
                    "commission_rate REAL DEFAULT 0.0" +
                    ")",
                    
                    // Raw Stock table
                    "CREATE TABLE IF NOT EXISTS Raw_Stock (" +
                    "stock_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "item_name TEXT NOT NULL," +
                    "brand_id INTEGER NOT NULL," +
                    "quantity INTEGER NOT NULL," +
                    "unit_price REAL NOT NULL," +
                    "total_cost REAL NOT NULL," +
                    "supplier_id INTEGER," +
                    "purchase_date TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (brand_id) REFERENCES Brand(brand_id)," +
                    "FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)" +
                    ")",
                    
                    // Raw Purchase Invoice table
                    "CREATE TABLE IF NOT EXISTS Raw_Purchase_Invoice (" +
                    "raw_purchase_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "invoice_number TEXT NOT NULL UNIQUE," +
                    "supplier_id INTEGER NOT NULL," +
                    "invoice_date TEXT NOT NULL," +
                    "total_amount REAL NOT NULL," +
                    "discount_amount REAL DEFAULT 0," +
                    "paid_amount REAL DEFAULT 0," +
                    "FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)" +
                    ")",
                    
                    // Raw Purchase Invoice Item table
                    "CREATE TABLE IF NOT EXISTS Raw_Purchase_Invoice_Item (" +
                    "raw_purchase_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "raw_purchase_invoice_id INTEGER NOT NULL," +
                    "raw_stock_id INTEGER NOT NULL," +
                    "quantity REAL NOT NULL," +
                    "unit_price REAL NOT NULL," +
                    "FOREIGN KEY (raw_purchase_invoice_id) REFERENCES Raw_Purchase_Invoice(raw_purchase_invoice_id)," +
                    "FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)" +
                    ")",

                    // Raw Purchase Return Invoice table
                    "CREATE TABLE IF NOT EXISTS Raw_Purchase_Return_Invoice (" +
                    "raw_purchase_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "return_invoice_number TEXT NOT NULL UNIQUE," +
                    "original_invoice_id INTEGER NOT NULL," +
                    "supplier_id INTEGER NOT NULL," +
                    "return_date TEXT NOT NULL," +
                    "total_return_amount REAL NOT NULL," +
                    "FOREIGN KEY (original_invoice_id) REFERENCES Raw_Purchase_Invoice(raw_purchase_invoice_id)," +
                    "FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)" +
                    ")",

                    // Raw Purchase Return Invoice Item table
                    "CREATE TABLE IF NOT EXISTS Raw_Purchase_Return_Invoice_Item (" +
                    "raw_purchase_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "raw_purchase_return_invoice_id INTEGER NOT NULL," +
                    "raw_stock_id INTEGER NOT NULL," +
                    "quantity REAL NOT NULL," +
                    "unit_price REAL NOT NULL," +
                    "FOREIGN KEY (raw_purchase_return_invoice_id) REFERENCES Raw_Purchase_Return_Invoice(raw_purchase_return_invoice_id)," +
                    "FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)" +
                    ")",
                    // Production_Stock_Raw_Usage table
                    "CREATE TABLE IF NOT EXISTS Production_Stock_Raw_Usage (" +
                    "usage_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "production_invoice_id INTEGER NOT NULL," +
                    "raw_stock_id INTEGER NOT NULL," +
                    "quantity_used REAL NOT NULL," +
                    "FOREIGN KEY (production_invoice_id) REFERENCES Production_Invoice(production_invoice_id)," +
                    "FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)" +
                    ")",

                    // Raw Stock Usage table
                    "CREATE TABLE IF NOT EXISTS Raw_Stock_Usage (" +
                    "raw_stock_usage_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "raw_stock_id INTEGER NOT NULL," +
                    "usage_date TEXT NOT NULL," +
                    "quantity_used REAL NOT NULL," +
                    "reference TEXT," +
                    "FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)" +
                    ")",
                    // Production Stock table
                    "CREATE TABLE IF NOT EXISTS ProductionStock (" +
                    "production_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "product_name TEXT NOT NULL," +
                    "brand_id INTEGER NOT NULL," +
                    "quantity INTEGER NOT NULL," +
                    "unit_cost REAL NOT NULL," +
                    "total_cost REAL NOT NULL," +
                    "production_date TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (brand_id) REFERENCES Brand(brand_id)" +
                    ")",
                    
                    // Cash Transaction table
                    "CREATE TABLE IF NOT EXISTS Unit (" +
                    "unit_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "unit_name TEXT NOT NULL UNIQUE" +
                    ")",
                    
                    "CREATE TABLE IF NOT EXISTS Cash_Transaction (" +
                    "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "description TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "transaction_type TEXT NOT NULL," +
                    "date TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ")",
                    
                    // Bank Transaction table
                    "CREATE TABLE IF NOT EXISTS Bank_Transaction (" +
                    "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "bank_id INTEGER NOT NULL," +
                    "description TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "transaction_type TEXT NOT NULL," +
                    "date TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (bank_id) REFERENCES Bank(bank_id)" +
                    ")",
                    
                    // Production Invoice table
                    "CREATE TABLE IF NOT EXISTS Production_Invoice (" +
                    "production_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "production_date TEXT NOT NULL," +
                    "notes TEXT" +
                    ")",
                    
                    // Production Invoice Item table
                    "CREATE TABLE IF NOT EXISTS Production_Invoice_Item (" +
                    "production_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "production_invoice_id INTEGER NOT NULL," +
                    "production_id INTEGER NOT NULL," +
                    "quantity_produced REAL NOT NULL," +
                    "FOREIGN KEY (production_invoice_id) REFERENCES Production_Invoice(production_invoice_id)," +
                    "FOREIGN KEY (production_id) REFERENCES ProductionStock(production_id)" +
                    ")"
                };
                
            // Execute all table creation queries
            for (String query : createTableQueries) {
                stmt.execute(query);
            }
            
            // Insert some default data if tables are empty
            insertDefaultData(stmt);
            
            stmt.close();
            System.out.println("Database initialized successfully with all required tables.");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void migrateSchema() {
        try {
            Statement stmt = connection.createStatement();
            
            // Check if Employee table has the new schema columns
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "Employee", null);
            
            boolean hasDesignationId = false;
            boolean hasSalaryType = false;
            boolean hasSalaryAmount = false;
            boolean hasPhoneNumber = false;
            boolean hasCnic = false;
            boolean hasIsActive = false;
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                switch (columnName.toLowerCase()) {
                    case "designation_id":
                        hasDesignationId = true;
                        break;
                    case "salary_type":
                        hasSalaryType = true;
                        break;
                    case "salary_amount":
                        hasSalaryAmount = true;
                        break;
                    case "phone_number":
                        hasPhoneNumber = true;
                        break;
                    case "cnic":
                        hasCnic = true;
                        break;
                    case "is_active":
                        hasIsActive = true;
                        break;
                }
            }
            columns.close();
            
            // Add missing columns if they don't exist
            if (!hasDesignationId) {
                stmt.execute("ALTER TABLE Employee ADD COLUMN designation_id INTEGER");
                System.out.println("Added designation_id column to Employee table");
            }
            if (!hasSalaryType) {
                stmt.execute("ALTER TABLE Employee ADD COLUMN salary_type TEXT DEFAULT 'monthly'");
                System.out.println("Added salary_type column to Employee table");
            }
            if (!hasSalaryAmount) {
                stmt.execute("ALTER TABLE Employee ADD COLUMN salary_amount REAL DEFAULT 0");
                System.out.println("Added salary_amount column to Employee table");
            }
            if (!hasPhoneNumber) {
                stmt.execute("ALTER TABLE Employee ADD COLUMN phone_number TEXT");
                System.out.println("Added phone_number column to Employee table");
            }
            if (!hasCnic) {
                stmt.execute("ALTER TABLE Employee ADD COLUMN cnic TEXT");
                System.out.println("Added cnic column to Employee table");
            }
            if (!hasIsActive) {
                stmt.execute("ALTER TABLE Employee ADD COLUMN is_active INTEGER DEFAULT 1");
                System.out.println("Added is_active column to Employee table");
            }
            
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error during schema migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void insertDefaultData(Statement stmt) throws SQLException {
        // Check if Province table is empty and insert default data
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Province");
        rs.next();
        if (rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Province (province_name) VALUES " +
                        "('Punjab'), ('Sindh'), ('KPK'), ('Balochistan'), ('Gilgit-Baltistan')");
        }
        rs.close();
        
        // Check if Category table is empty and insert default data
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Category");
        rs.next();
        if (rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Category (category_name) VALUES " +
                        "('Electric Cables'), ('Fiber Optic'), ('Coaxial'), ('Network Cables'), ('Power Cables'), ('ABC')");
        }
        rs.close();
        
        // Check if District table is empty and insert some default districts
        rs = stmt.executeQuery("SELECT COUNT(*) FROM District");
        rs.next();
        if (rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO District (district_name, province_id) VALUES " +
                        "('Lahore', 1), ('Karachi', 2), ('Peshawar', 3), ('Quetta', 4), ('Islamabad', 1)");
        }
        rs.close();
        
        // Check if Designation table is empty and insert default data
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Designation");
        rs.next();
        if (rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Designation (designation_title) VALUES " +
                        "('Manager'), ('Technician'), ('Sales Representative'), ('Accountant'), ('Supervisor')");
        }
        rs.close();
        
        // Check if Tehsil table is empty and insert some default tehsils
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Tehsil");
        rs.next();
        if (rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Tehsil (tehsil_name, district_id) VALUES " +
                        "('Model Town', 1), ('Gulshan', 2), ('University Town', 3), ('Satellite Town', 4), ('F-10', 5)");
        }
        rs.close();
        
        // Check if Unit table is empty and insert default data
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Unit");
        rs.next();
        if (rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Unit (unit_name) VALUES " +
                        "('Meter'), ('Roll'), ('Kg'), ('Gram'), ('Piece'), ('Box'), ('Liter')");
        }
        rs.close();
        
        // Check if Manufacturer table is empty and insert some default manufacturers
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Manufacturer");
        rs.next();
        if (rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Manufacturer (manufacturer_name, tehsil_id) VALUES " +
                        "('CableTech Industries', 1), ('ABC Manufacturing', 1), ('Test Manufacturer', 1)");
        }
        rs.close();
        
        // Check if Brand table is empty and insert some default brands
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Brand");
        rs.next();
        if (rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Brand (brand_name, manufacturer_id, tehsil_id) VALUES " +
                        "('PowerFlex', 1, 1), ('ABC', 2, 1), ('aa', 3, 1)");
        }
        rs.close();
    }

    @Override
    public List<String> getAllTehsils() {
        List<String> tehsils = new ArrayList<>();
        String query = "SELECT tehsil_name FROM Tehsil ORDER BY tehsil_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                tehsils.add(rs.getString("tehsil_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tehsils;
    }

    @Override
    public List<String> getTehsilsByDistrict(String districtName) {
        List<String> tehsils = new ArrayList<>();
        String query = "SELECT t.tehsil_name FROM Tehsil t " +
                      "JOIN District d ON t.district_id = d.district_id " +
                      "WHERE d.district_name = ? ORDER BY t.tehsil_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, districtName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tehsils.add(rs.getString("tehsil_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tehsils;
    }

    @Override
    public boolean insertTehsil(String tehsilName, String districtName) {
        String getDistrictQuery = "SELECT district_id FROM District WHERE district_name = ?";
        String insertQuery = "INSERT INTO Tehsil (tehsil_name, district_id) VALUES (?, ?)";
        
        try (PreparedStatement getStmt = connection.prepareStatement(getDistrictQuery)) {
            getStmt.setString(1, districtName);
            
            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    int districtId = rs.getInt("district_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, tehsilName);
                        insertStmt.setInt(2, districtId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean tehsilExists(String tehsilName) {
        String query = "SELECT COUNT(*) FROM Tehsil WHERE tehsil_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tehsilName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllDistricts() {
        List<String> districts = new ArrayList<>();
        String query = "SELECT district_name FROM District ORDER BY district_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                districts.add(rs.getString("district_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return districts;
    }

    @Override
    public List<String> getDistrictsByProvince(String provinceName) {
        List<String> districts = new ArrayList<>();
        String query = "SELECT d.district_name FROM District d " +
                      "JOIN Province p ON d.province_id = p.province_id " +
                      "WHERE p.province_name = ? ORDER BY d.district_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, provinceName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    districts.add(rs.getString("district_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return districts;
    }

    @Override
    public boolean insertDistrict(String districtName, String provinceName) {
        String getProvinceQuery = "SELECT province_id FROM Province WHERE province_name = ?";
        String insertQuery = "INSERT INTO District (district_name, province_id) VALUES (?, ?)";
        
        try (PreparedStatement getStmt = connection.prepareStatement(getProvinceQuery)) {
            getStmt.setString(1, provinceName);
            
            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    int provinceId = rs.getInt("province_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, districtName);
                        insertStmt.setInt(2, provinceId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllProvinces() {
        List<String> provinces = new ArrayList<>();
        String query = "SELECT province_name FROM Province ORDER BY province_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                provinces.add(rs.getString("province_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return provinces;
    }

    @Override
    public boolean insertProvince(String provinceName) {
        String query = "INSERT INTO Province (province_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, provinceName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT category_name FROM Category ORDER BY category_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public boolean insertCategory(String categoryName) {
        String query = "INSERT INTO Category (category_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, categoryName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Manufacturer> getAllManufacturers() {
        List<Manufacturer> manufacturers = new ArrayList<>();
        String query = "SELECT m.manufacturer_name, t.tehsil_name, d.district_name, p.province_name " +
                      "FROM Manufacturer m " +
                      "JOIN Tehsil t ON m.tehsil_id = t.tehsil_id " +
                      "JOIN District d ON t.district_id = d.district_id " +
                      "JOIN Province p ON d.province_id = p.province_id " +
                      "ORDER BY m.manufacturer_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String name = rs.getString("manufacturer_name");
                String province = rs.getString("province_name");
                String district = rs.getString("district_name");
                String tehsil = rs.getString("tehsil_name");
                
                manufacturers.add(new Manufacturer(name, province, district, tehsil));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return manufacturers;
    }

    @Override
    public boolean insertManufacturer(String name, String province, String district, String tehsil) {
        String getTehsilQuery = "SELECT t.tehsil_id FROM Tehsil t " +
                               "JOIN District d ON t.district_id = d.district_id " +
                               "JOIN Province p ON d.province_id = p.province_id " +
                               "WHERE p.province_name = ? AND d.district_name = ? AND t.tehsil_name = ?";
        String insertQuery = "INSERT INTO Manufacturer (manufacturer_name, tehsil_id) VALUES (?, ?)";
        
        try (PreparedStatement getStmt = connection.prepareStatement(getTehsilQuery)) {
            getStmt.setString(1, province);
            getStmt.setString(2, district);
            getStmt.setString(3, tehsil);
            
            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setInt(2, tehsilId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean manufacturerExists(String name) {
        String query = "SELECT COUNT(*) FROM Manufacturer WHERE manufacturer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Brand> getAllBrands() {
        List<Brand> brands = new ArrayList<>();
        String query = "SELECT b.brand_name, t.tehsil_name, d.district_name, p.province_name " +
                      "FROM Brand b " +
                      "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id " +
                      "JOIN Tehsil t ON b.tehsil_id = t.tehsil_id " +
                      "JOIN District d ON t.district_id = d.district_id " +
                      "JOIN Province p ON d.province_id = p.province_id " +
                      "ORDER BY b.brand_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String name = rs.getString("brand_name");
                String province = rs.getString("province_name");
                String district = rs.getString("district_name");
                String tehsil = rs.getString("tehsil_name");
                
                brands.add(new Brand(name, province, district, tehsil));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brands;
    }

    @Override
    public boolean insertBrand(String name, String province, String district, String tehsil) {
        String getIdsQuery = "SELECT m.manufacturer_id, t.tehsil_id FROM Manufacturer m " +
                            "JOIN Tehsil t ON m.tehsil_id = t.tehsil_id " +
                            "JOIN District d ON t.district_id = d.district_id " +
                            "JOIN Province p ON d.province_id = p.province_id " +
                            "WHERE p.province_name = ? AND d.district_name = ? AND t.tehsil_name = ? " +
                            "LIMIT 1";
        String insertQuery = "INSERT INTO Brand (brand_name, manufacturer_id, tehsil_id) VALUES (?, ?, ?)";
        
        try (PreparedStatement getStmt = connection.prepareStatement(getIdsQuery)) {
            getStmt.setString(1, province);
            getStmt.setString(2, district);
            getStmt.setString(3, tehsil);
            
            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    int manufacturerId = rs.getInt("manufacturer_id");
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setInt(2, manufacturerId);
                        insertStmt.setInt(3, tehsilId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean brandExists(String name) {
        String query = "SELECT COUNT(*) FROM Brand WHERE brand_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT c.customer_name, c.customer_contact, t.tehsil_name " +
                      "FROM Customer c " +
                      "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                      "ORDER BY c.customer_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String name = rs.getString("customer_name");
                String contact = rs.getString("customer_contact");
                String tehsil = rs.getString("tehsil_name");
                if (tehsil == null) tehsil = "";
                customers.add(new Customer(name, contact, tehsil));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    @Override
    public boolean insertCustomer(String name, String contact) {
        String getTehsilQuery = "SELECT tehsil_id FROM Tehsil LIMIT 1";
        String insertQuery = "INSERT INTO Customer (customer_name, customer_contact, tehsil_id) VALUES (?, ?, ?)";
        
        try (Statement getStmt = connection.createStatement();
             ResultSet rs = getStmt.executeQuery(getTehsilQuery)) {
            
            if (rs.next()) {
                int tehsilId = rs.getInt("tehsil_id");
                
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, contact);
                    insertStmt.setInt(3, tehsilId);
                    
                    return insertStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertCustomer(String name, String contact, String tehsilName) {
        String getTehsilIdQuery = "SELECT tehsil_id FROM Tehsil WHERE tehsil_name = ?";
        String insertQuery = "INSERT INTO Customer (customer_name, customer_contact, tehsil_id) VALUES (?, ?, ?)";
        
        try (PreparedStatement getTehsilStmt = connection.prepareStatement(getTehsilIdQuery)) {
            getTehsilStmt.setString(1, tehsilName);
            
            try (ResultSet rs = getTehsilStmt.executeQuery()) {
                if (rs.next()) {
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, contact);
                        insertStmt.setInt(3, tehsilId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean customerExists(String name) {
        String query = "SELECT COUNT(*) FROM Customer WHERE customer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT s.supplier_name, s.supplier_contact, t.tehsil_name " +
                      "FROM Supplier s " +
                      "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                      "ORDER BY s.supplier_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String name = rs.getString("supplier_name");
                String contact = rs.getString("supplier_contact");
                String tehsil = rs.getString("tehsil_name");
                if (tehsil == null) tehsil = "";
                suppliers.add(new Supplier(name, contact, tehsil));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    @Override
    public boolean insertSupplier(String name, String contact) {
        String getTehsilQuery = "SELECT tehsil_id FROM Tehsil LIMIT 1";
        String insertQuery = "INSERT INTO Supplier (supplier_name, supplier_contact, tehsil_id) VALUES (?, ?, ?)";
        
        try (Statement getStmt = connection.createStatement();
             ResultSet rs = getStmt.executeQuery(getTehsilQuery)) {
            
            if (rs.next()) {
                int tehsilId = rs.getInt("tehsil_id");
                
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, contact);
                    insertStmt.setInt(3, tehsilId);
                    
                    return insertStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertSupplier(String name, String contact, String tehsilName) {
        String getTehsilIdQuery = "SELECT tehsil_id FROM Tehsil WHERE tehsil_name = ?";
        String insertQuery = "INSERT INTO Supplier (supplier_name, supplier_contact, tehsil_id) VALUES (?, ?, ?)";
        
        try (PreparedStatement getTehsilStmt = connection.prepareStatement(getTehsilIdQuery)) {
            getTehsilStmt.setString(1, tehsilName);
            
            try (ResultSet rs = getTehsilStmt.executeQuery()) {
                if (rs.next()) {
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, contact);
                        insertStmt.setInt(3, tehsilId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean supplierExists(String name) {
        String query = "SELECT COUNT(*) FROM Supplier WHERE supplier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllUnits() {
        List<String> units = new ArrayList<>();
        String query = "SELECT unit_name FROM Unit ORDER BY unit_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                units.add(rs.getString("unit_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return units;
    }

    @Override
    public boolean insertUnit(String unitName) {
        String query = "INSERT INTO Unit (unit_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean unitExists(String unitName) {
        String query = "SELECT COUNT(*) FROM Unit WHERE unit_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertUser(String username, String password, String role) {
        ensureUserTableExists();
        String query = "INSERT INTO User (username, password_hash, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role != null ? role : "user");
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean userExists(String username) {
        ensureUserTableExists();
        String query = "SELECT COUNT(*) FROM User WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        ensureUserTableExists();
        
        // First verify old password
        if (!SignIn(username, oldPassword)) {
            return false;
        }
        
        // Update password
        String query = "UPDATE User SET password_hash = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllUsers() {
        ensureUserTableExists();
        List<String> users = new ArrayList<>();
        String query = "SELECT username FROM User WHERE is_active = 1 ORDER BY username";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // --------------------------
    // Raw Stock Operations
    // --------------------------
    @Override
    public List<Object[]> getAllRawStocks() {
        List<Object[]> rawStocks = new ArrayList<>();
        String query = "SELECT rs.stock_id, rs.item_name, b.brand_name, " +
                      "rs.quantity, rs.unit_price, rs.total_cost " +
                      "FROM Raw_Stock rs " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "ORDER BY rs.item_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("stock_id"),
                    rs.getString("item_name"),
                    rs.getString("brand_name"),
                    (double) rs.getInt("quantity"),  // Convert int to double for consistency
                    rs.getDouble("unit_price"),
                    rs.getDouble("total_cost")
                };
                rawStocks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rawStocks;
    }

@Override
public boolean insertRawStock(String name, String category, String brand, String unit, 
                             double openingQty, double purchasePrice, double reorderLevel) {
    if (name == null || name.trim().isEmpty()) {
        System.err.println("Invalid item name for insertRawStock: null or empty");
        return false;
    }
    double totalCost = openingQty * purchasePrice;
    int quantity = (int) Math.round(openingQty);
    
    String query = "INSERT INTO Raw_Stock (item_name, brand_id, quantity, unit_price, total_cost, supplier_id) " +
                  "SELECT ?, b.brand_id, ?, ?, ?, ? FROM Brand b WHERE b.brand_name = ?";
    
    try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
        pstmt.setString(1, name);
        pstmt.setInt(2, quantity);
        pstmt.setDouble(3, purchasePrice);
        pstmt.setDouble(4, totalCost);
        pstmt.setInt(5, 1); // Use supplier_id = 1 (matches 'rewf')
        pstmt.setString(6, brand);
        
        System.out.println("Attempting to insert Raw_Stock: item_name=" + name + ", brand=" + brand + 
                          ", quantity=" + quantity + ", unit_price=" + purchasePrice + ", supplier_id=1");
        
        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected == 0) {
            System.err.println("Failed to insert Raw_Stock: no rows affected for item " + name + " with brand " + brand);
            return false;
        }
        
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                int stockId = generatedKeys.getInt(1);
                System.out.println("Successfully inserted Raw_Stock with stock_id: " + stockId);
                return true;
            } else {
                System.err.println("No generated key returned for Raw_Stock: " + name);
                return false;
            }
        }
    } catch (SQLException e) {
        System.err.println("Error inserting Raw_Stock for item " + name + ": " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}
  

@Override
    public boolean insertRawPurchaseInvoice(String invoiceNumber, int supplierId, String invoiceDate, 
                                           double totalAmount, double discountAmount, double paidAmount) {
        String query = "INSERT INTO Raw_Purchase_Invoice (invoice_number, supplier_id, invoice_date, " +
                      "total_amount, discount_amount, paid_amount) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, invoiceNumber);
            pstmt.setInt(2, supplierId);
            pstmt.setString(3, invoiceDate);
            pstmt.setDouble(4, totalAmount);
            pstmt.setDouble(5, discountAmount);
            pstmt.setDouble(6, paidAmount);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public int insertRawPurchaseInvoiceAndGetId(String invoiceNumber, int supplierId, String invoiceDate, 
                                              double totalAmount, double discountAmount, double paidAmount) {
        String query = "INSERT INTO Raw_Purchase_Invoice (invoice_number, supplier_id, invoice_date, " +
                      "total_amount, discount_amount, paid_amount) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, invoiceNumber);
            pstmt.setInt(2, supplierId);
            pstmt.setString(3, invoiceDate);
            pstmt.setDouble(4, totalAmount);
            pstmt.setDouble(5, discountAmount);
            pstmt.setDouble(6, paidAmount);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if failed
    }

    // New methods for enhanced invoice functionality
    @Override
    public String generateNextInvoiceNumber(String prefix) {
        String query = "SELECT MAX(CAST(SUBSTR(invoice_number, LENGTH(?) + 1) AS INTEGER)) " +
                      "FROM Raw_Purchase_Invoice WHERE invoice_number LIKE ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, prefix);
            pstmt.setString(2, prefix + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int maxNumber = rs.getInt(1);
                    return prefix + String.format("%06d", maxNumber + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If no invoices found or error, start with 000001
        return prefix + "000001";
    }
    
    @Override
    public List<Object[]> getAllRawStocksForDropdown() {
        List<Object[]> rawStocks = new ArrayList<>();
        String query = "SELECT rs.stock_id, rs.item_name, 'General' as category_name, b.brand_name, " +
                      "'Piece' as unit_name, rs.unit_price FROM Raw_Stock rs " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "ORDER BY rs.item_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("stock_id"),
                    rs.getString("item_name"),
                    rs.getString("category_name"),
                    rs.getString("brand_name"),
                    rs.getString("unit_name"),
                    rs.getDouble("unit_price")
                };
                rawStocks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rawStocks;
    }
    
    @Override
    public List<String> getAllSupplierNames() {
        List<String> supplierNames = new ArrayList<>();
        String query = "SELECT supplier_name FROM Supplier ORDER BY supplier_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                supplierNames.add(rs.getString("supplier_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return supplierNames;
    }
    
    @Override
    public int getSupplierIdByName(String supplierName) {
        String query = "SELECT supplier_id FROM Supplier WHERE supplier_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, supplierName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("supplier_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if not found
    }
    
public int getRawStockIdByName(String itemName) {
    if (itemName == null || itemName.trim().isEmpty()) {
        System.err.println("Invalid item name provided to getRawStockIdByName: null or empty");
        return -1;
    }
    String query = "SELECT stock_id FROM Raw_Stock WHERE item_name = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, itemName);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int stockId = rs.getInt("stock_id");
            System.out.println("Found stock_id: " + stockId + " for item_name: " + itemName);
            return stockId;
        } else {
            System.out.println("No stock_id found for item_name: " + itemName);
            return -1;
        }
    } catch (SQLException e) {
        System.err.println("Error retrieving stock_id for item_name " + itemName + ": " + e.getMessage());
        e.printStackTrace();
        return -1;
    }
}


@Override
public boolean ensureBrandExists(String brandName, int tehsilId) {
    String checkBrandQuery = "SELECT brand_id FROM Brand WHERE brand_name = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(checkBrandQuery)) {
        pstmt.setString(1, brandName);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return true; // Brand exists
        }
    } catch (SQLException e) {
        System.err.println("Error checking brand existence: " + e.getMessage());
        return false;
    }

    // Insert Default Brand with a default manufacturer_id
    String insertBrandQuery = "INSERT INTO Brand (brand_name, manufacturer_id, tehsil_id) VALUES (?, ?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(insertBrandQuery)) {
        pstmt.setString(1, brandName);
        pstmt.setInt(2, 1); // Assume manufacturer_id = 1 exists; adjust as needed
        pstmt.setInt(3, tehsilId);
        int rowsAffected = pstmt.executeUpdate();
        System.out.println("Inserted Default Brand: " + brandName);
        return rowsAffected > 0;
    } catch (SQLException e) {
        System.err.println("Failed to insert brand " + brandName + ": " + e.getMessage());
        return false;
    }
}

@Override
public boolean insertSimpleRawPurchaseInvoice(String invoiceNumber, String supplierName, String invoiceDate,
                                             double totalAmount, double discountAmount, double paidAmount,
                                             List<RawStockPurchaseItem> items) {
    try {
        connection.setAutoCommit(false); // Start transaction
        System.out.println("Starting insertSimpleRawPurchaseInvoice: invoiceNumber=" + invoiceNumber);

        // 1. Validate inputs
        if (items == null || items.isEmpty()) {
            System.err.println("Error: Items list is null or empty for invoice " + invoiceNumber);
            connection.rollback();
            return false;
        }
        System.out.println("Items list size: " + items.size());
        for (int i = 0; i < items.size(); i++) {
            RawStockPurchaseItem item = items.get(i);
            System.out.println("Item " + (i + 1) + ": name=" + (item != null ? item.getRawStockName() : "null") + 
                              ", quantity=" + (item != null ? item.getQuantity() : "null") + 
                              ", unit_price=" + (item != null ? item.getUnitPrice() : "null"));
        }

        // 2. Get supplier_id
        int supplierId = getSupplierIdByName(supplierName);
        if (supplierId == -1) {
            System.err.println("Supplier not found: " + supplierName);
            connection.rollback();
            return false;
        }
        System.out.println("Found supplier_id: " + supplierId + " for supplier: " + supplierName);

        // 3. Get a valid tehsil_id
        int tehsilId = -1;
        String getTehsilQuery = "SELECT tehsil_id FROM Tehsil LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(getTehsilQuery)) {
            if (rs.next()) {
                tehsilId = rs.getInt("tehsil_id");
                System.out.println("Found tehsil_id: " + tehsilId);
            } else {
                System.err.println("No tehsil found in Tehsil table");
                connection.rollback();
                return false;
            }
        }

        // 4. Ensure Default Brand exists
        if (!ensureBrandExists("Default Brand", tehsilId)) {
            System.err.println("Failed to ensure Default Brand exists");
            connection.rollback();
            return false;
        }

        // 5. Insert into Raw_Purchase_Invoice
        String insertInvoiceQuery = "INSERT INTO Raw_Purchase_Invoice (invoice_number, supplier_id, invoice_date, total_amount, discount_amount, paid_amount) " +
                                   "VALUES (?, ?, ?, ?, ?, ?)";
        int rawPurchaseInvoiceId;
        try (PreparedStatement pstmt = connection.prepareStatement(insertInvoiceQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, invoiceNumber);
            pstmt.setInt(2, supplierId);
            pstmt.setString(3, invoiceDate);
            pstmt.setDouble(4, totalAmount);
            pstmt.setDouble(5, discountAmount);
            pstmt.setDouble(6, paidAmount);

            System.out.println("Inserting Raw_Purchase_Invoice: invoiceNumber=" + invoiceNumber + ", supplierId=" + supplierId +
                              ", invoiceDate=" + invoiceDate + ", totalAmount=" + totalAmount +
                              ", discountAmount=" + discountAmount + ", paidAmount=" + paidAmount);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("Failed to insert into Raw_Purchase_Invoice");
                connection.rollback();
                return false;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    rawPurchaseInvoiceId = generatedKeys.getInt(1);
                    System.out.println("Inserted Raw_Purchase_Invoice with ID: " + rawPurchaseInvoiceId);
                } else {
                    System.err.println("Failed to retrieve generated invoice ID");
                    connection.rollback();
                    return false;
                }
            }
        }

        // 6. Insert items into Raw_Purchase_Invoice_Item
        String insertItemQuery = "INSERT INTO Raw_Purchase_Invoice_Item (raw_purchase_invoice_id, raw_stock_id, quantity, unit_price) " +
                    "VALUES (?, ?, ?, ?)";
        // Print all items that are going to be inserted
        System.out.println("Items to be inserted into Raw_Purchase_Invoice_Item:");
        for (RawStockPurchaseItem item : items) {
            System.out.println("Raw Purchase Invoice ID: " + rawPurchaseInvoiceId +
            ", Raw Stock ID: " + (item != null ? item.getRawStockId() : "null") +
            ", Quantity: " + (item != null ? item.getQuantity() : "null") +
            ", Unit Price: " + (item != null ? item.getUnitPrice() : "null"));
        }
                // Disable foreign key checks
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = OFF");
        }
        try (PreparedStatement pstmt = connection.prepareStatement(insertItemQuery)) {
            for (RawStockPurchaseItem item : items) {
                try {
                    if (item == null || item.getRawStockName() == null || item.getRawStockName().trim().isEmpty()) {
                        System.err.println("Invalid item: null or empty RawStockName");
                        connection.rollback();
                        return false;
                    }
                    System.out.println("Processing item: " + item.getRawStockName() + ", quantity=" + item.getQuantity() + 
                                      ", unit_price=" + item.getUnitPrice());

                    int rawStockId = getRawStockIdByName(item.getRawStockName());
                    System.out.println("Initial getRawStockIdByName for " + item.getRawStockName() + ": " + rawStockId);
                    if (rawStockId == -1) {
                        boolean inserted = insertRawStock(item.getRawStockName(), "General", "Default Brand", "Piece", 0, item.getUnitPrice(), 0);
                        if (!inserted) {
                            System.err.println("Failed to insert new raw stock: " + item.getRawStockName());
                            connection.rollback();
                            return false;
                        }
                        rawStockId = getRawStockIdByName(item.getRawStockName());
                        System.out.println("Post-insert getRawStockIdByName for " + item.getRawStockName() + ": " + rawStockId);
                        if (rawStockId == -1) {
                            System.err.println("Failed to retrieve new raw stock ID for: " + item.getRawStockName());
                            connection.rollback();
                            return false;
                        }
                    }

                    // Verify raw_stock_id exists in Raw_Stock
                    String verifyStockQuery = "SELECT stock_id FROM Raw_Stock WHERE stock_id = ?";
                    try (PreparedStatement verifyStmt = connection.prepareStatement(verifyStockQuery)) {
                        verifyStmt.setInt(1, rawStockId);
                        ResultSet rs = verifyStmt.executeQuery();
                        if (!rs.next()) {
                            System.err.println("Invalid raw_stock_id: " + rawStockId + " does not exist in Raw_Stock for item: " + item.getRawStockName());
                            connection.rollback();
                            return false;
                        }
                    }

                    pstmt.setInt(1, rawPurchaseInvoiceId);
                    pstmt.setInt(2, rawStockId);
                    pstmt.setInt(3, (int) item.getQuantity());
                    pstmt.setDouble(4, item.getUnitPrice());
                    System.out.println("Adding batch for Raw_Purchase_Invoice_Item: raw_purchase_invoice_id=" + rawPurchaseInvoiceId +
                                      ", raw_stock_id=" + rawStockId + ", quantity=" + item.getQuantity() +
                                      ", unit_price=" + item.getUnitPrice());
                    pstmt.addBatch();
                } catch (SQLException e) {
                    System.err.println("SQLException in item loop for " + item.getRawStockName() + ": " + e.getMessage());
                    e.printStackTrace();
                    connection.rollback();
                    return false;
                }
            }
            System.out.println("Executing batch insert for Raw_Purchase_Invoice_Item");
            pstmt.executeBatch();
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }

        // 7. Update Raw_Stock quantities
        String updateStockQuery = "UPDATE Raw_Stock SET quantity = quantity + ?, total_cost = total_cost + ? WHERE stock_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateStockQuery)) {
            for (RawStockPurchaseItem item : items) {
                try {
                    if (item == null || item.getRawStockName() == null || item.getRawStockName().trim().isEmpty()) {
                        System.err.println("Invalid item for stock update: null or empty RawStockName");
                        connection.rollback();
                        return false;
                    }
                    int rawStockId = getRawStockIdByName(item.getRawStockName());
                    if (rawStockId == -1) {
                        System.err.println("Raw stock ID not found for update: " + item.getRawStockName());
                        connection.rollback();
                        return false;
                    }
                    double totalCost = item.getQuantity() * item.getUnitPrice();
                    System.out.println("Updating stock for item: " + item.getRawStockName() + 
                                      ", Quantity: " + item.getQuantity() + ", Total Cost: " + totalCost);

                    pstmt.setInt(1, (int) item.getQuantity());
                    pstmt.setDouble(2, totalCost);
                    pstmt.setInt(3, rawStockId);
                    pstmt.addBatch();
                } catch (SQLException e) {
                    System.err.println("SQLException in stock update loop for " + item.getRawStockName() + ": " + e.getMessage());
                    e.printStackTrace();
                    connection.rollback();
                    return false;
                }
            }
            System.out.println("Executing batch update for Raw_Stock");
            pstmt.executeBatch();
        }

        connection.commit();
        System.out.println("Successfully inserted Raw_Purchase_Invoice and items for invoice: " + invoiceNumber);
        return true;
    } catch (SQLException e) {
        System.err.println("SQLException during insertSimpleRawPurchaseInvoice: " + e.getMessage());
        e.printStackTrace();
        try {
            connection.rollback();
            System.err.println("Rolled back transaction due to error");
        } catch (SQLException rollbackEx) {
            System.err.println("Failed to rollback transaction: " + rollbackEx.getMessage());
        }
        return false;
    } finally {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("Failed to restore auto-commit: " + e.getMessage());
        }
    }
}
@
Override
    public List<Object[]> getAllRawPurchaseInvoices() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT rpi.invoice_number, rpi.invoice_date, s.supplier_name, " +
                      "rpi.total_amount, rpi.discount_amount, rpi.paid_amount " +
                      "FROM Raw_Purchase_Invoice rpi " +
                      "JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                      "ORDER BY rpi.invoice_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("invoice_number"),
                    rs.getString("invoice_date"),
                    rs.getString("supplier_name"),
                    rs.getDouble("total_amount"),
                    rs.getDouble("discount_amount"),
                    rs.getDouble("paid_amount")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    @Override
    public List<Object[]> getAllRawStockUsage() {
        List<Object[]> usage = new ArrayList<>();
        String query = "SELECT rsu.usage_date, rs.raw_stock_name, rsu.quantity_used, rsu.reference " +
                      "FROM RawStock_Usage rsu " +
                      "JOIN Raw_Stock rs ON rsu.raw_stock_id = rs.stock_id " +
                      "ORDER BY rsu.usage_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("usage_date"),
                    rs.getString("raw_stock_name"),
                    rs.getDouble("quantity_used"),
                    rs.getString("reference")
                };
                usage.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usage;
    }

    // --------------------------
    // Raw Purchase Return Invoice Operations
    // --------------------------
    
    /**
     * Generate auto-increment return invoice number
     */
    public String generateReturnInvoiceNumber() {
        String query = "SELECT COUNT(*) + 1 as next_id FROM Raw_Purchase_Return_Invoice";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                return String.format("INV-RPR-%03d", nextId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "INV-RPR-001"; // fallback
    }
    
    /**
     * Get all raw purchase invoices for dropdown selection
     */
    public List<Object[]> getAllRawPurchaseInvoicesForDropdown() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT rpi.raw_purchase_invoice_id, rpi.invoice_number, s.supplier_name, " +
                      "rpi.invoice_date, rpi.total_amount " +
                      "FROM Raw_Purchase_Invoice rpi " +
                      "JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                      "ORDER BY rpi.invoice_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("raw_purchase_invoice_id"),
                    rs.getString("invoice_number"),
                    rs.getString("supplier_name"),
                    rs.getString("invoice_date"),
                    rs.getDouble("total_amount")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }
    
    /**
     * Get raw stock items from a specific purchase invoice
     */
    public List<Object[]> getRawStockItemsByInvoiceId(int invoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT rpii.raw_stock_id, rs.raw_stock_name, c.category_name, " +
                      "b.brand_name, u.unit_name, rpii.quantity, rpii.unit_price " +
                      "FROM Raw_Purchase_Invoice_Item rpii " +
                      "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.raw_stock_id " +
                      "JOIN Category c ON rs.category_id = c.category_id " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "JOIN Unit u ON rs.unit_id = u.unit_id " +
                      "WHERE rpii.raw_purchase_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("raw_stock_id"),
                    rs.getString("raw_stock_name"),
                    rs.getString("category_name"),
                    rs.getString("brand_name"),
                    rs.getString("unit_name"),
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_price")
                };
                items.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    /**
     * Insert raw purchase return invoice and return the generated ID
     */
    public int insertRawPurchaseReturnInvoiceAndGetId(String returnInvoiceNumber, int originalInvoiceId, 
                                                     int supplierId, String returnDate, double totalReturnAmount) {
        String insertQuery = "INSERT INTO Raw_Purchase_Return_Invoice " +
                           "(return_invoice_number, original_invoice_id, supplier_id, return_date, total_return_amount) " +
                           "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, returnInvoiceNumber);
            pstmt.setInt(2, originalInvoiceId);
            pstmt.setInt(3, supplierId);
            pstmt.setString(4, returnDate);
            pstmt.setDouble(5, totalReturnAmount);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Insert raw purchase return invoice items
     */
    public boolean insertRawPurchaseReturnInvoiceItems(int returnInvoiceId, 
                                                      List<com.cablemanagement.model.RawStockPurchaseItem> items) {
        String insertQuery = "INSERT INTO Raw_Purchase_Return_Invoice_Item " +
                           "(raw_purchase_return_invoice_id, raw_stock_id, quantity, unit_price) " +
                           "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            for (com.cablemanagement.model.RawStockPurchaseItem item : items) {
                pstmt.setInt(1, returnInvoiceId);
                pstmt.setInt(2, item.getRawStockId());
                pstmt.setDouble(3, item.getQuantity());
                pstmt.setDouble(4, item.getUnitPrice());
                pstmt.addBatch();
            }
            
            int[] result = pstmt.executeBatch();
            return result.length == items.size();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all raw purchase return invoices
     */
    public List<Object[]> getAllRawPurchaseReturnInvoices() {
        List<Object[]> returnInvoices = new ArrayList<>();
        String query = "SELECT rpri.return_invoice_number, rpri.return_date, s.supplier_name, " +
                      "rpi.invoice_number as original_invoice, rpri.total_return_amount " +
                      "FROM Raw_Purchase_Return_Invoice rpri " +
                      "JOIN Supplier s ON rpri.supplier_id = s.supplier_id " +
                      "JOIN Raw_Purchase_Invoice rpi ON rpri.original_invoice_id = rpi.raw_purchase_invoice_id " +
                      "ORDER BY rpri.return_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("return_invoice_number"),
                    rs.getString("return_date"),
                    rs.getString("supplier_name"),
                    rs.getString("original_invoice"),
                    rs.getDouble("total_return_amount")
                };
                returnInvoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnInvoices;
    }

    // --------------------------
    // Raw Stock Use Invoice Operations
    // --------------------------
    
    /**
     * Generate auto-increment use invoice number
     */
    @Override
    public String generateUseInvoiceNumber() {
        String query = "SELECT COUNT(*) FROM Raw_Stock_Use_Invoice";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("INV-RSU-%03d", count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "INV-RSU-001";
    }
    
    /**
     * Get all raw stocks with their units for dropdown selection
     */
    @Override
    public List<Object[]> getAllRawStocksWithUnitsForDropdown() {
        List<Object[]> rawStocks = new ArrayList<>();
        String query = "SELECT rs.stock_id, rs.item_name, b.brand_name, " +
                      "'N/A' as unit_name, rs.quantity, rs.unit_price " +
                      "FROM Raw_Stock rs " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "WHERE rs.quantity > 0 " +
                      "ORDER BY rs.item_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("stock_id"),
                    rs.getString("item_name"),
                    "N/A", // category_name (not available in Raw_Stock table)
                    rs.getString("brand_name"),
                    rs.getString("unit_name"),
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_price")
                };
                rawStocks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rawStocks;
    }
    
    /**
     * Insert raw stock use invoice and return the generated ID
     */
    @Override
    public int insertRawStockUseInvoiceAndGetId(String useInvoiceNumber, String usageDate, 
                                               double totalUsageAmount, String referencePurpose) {
        String query = "INSERT INTO Raw_Stock_Use_Invoice (use_invoice_number, usage_date, " +
                      "total_usage_amount, reference_purpose) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, useInvoiceNumber);
            pstmt.setString(2, usageDate);
            pstmt.setDouble(3, totalUsageAmount);
            pstmt.setString(4, referencePurpose);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if insertion failed
    }
    
    /**
     * Insert raw stock use invoice items
     */
    @Override
    public boolean insertRawStockUseInvoiceItems(int useInvoiceId, List<RawStockUseItem> items) {
        String query = "INSERT INTO Raw_Stock_Use_Invoice_Item (raw_stock_use_invoice_id, " +
                      "raw_stock_id, quantity_used, unit_cost) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false); // Start transaction
            
            for (RawStockUseItem item : items) {
                pstmt.setInt(1, useInvoiceId);
                pstmt.setInt(2, item.getRawStockId());
                pstmt.setDouble(3, item.getQuantityUsed());
                pstmt.setDouble(4, item.getUnitCost());
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            connection.commit(); // Commit transaction
            connection.setAutoCommit(true); // Reset auto-commit
            
            // Check if all items were inserted successfully
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback on error
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all raw stock use invoices
     */
    @Override
    public List<Object[]> getAllRawStockUseInvoices() {
        List<Object[]> useInvoices = new ArrayList<>();
        String query = "SELECT rsui.use_invoice_number, rsui.usage_date, " +
                      "rsui.total_usage_amount, rsui.reference_purpose " +
                      "FROM RawStock_Use_Invoice rsui " +
                      "ORDER BY rsui.usage_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("use_invoice_number"),
                    rs.getString("usage_date"),
                    rs.getDouble("total_usage_amount"),
                    rs.getString("reference_purpose")
                };
                useInvoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return useInvoices;
    }

    // --------------------------
    // Production Stock Operations
    // --------------------------
    @Override
    public List<Object[]> getAllProductionStocks() {
        List<Object[]> productionStocks = new ArrayList<>();
        String query = "SELECT ps.production_id, ps.product_name, b.brand_name, " +
                      "ps.quantity, ps.unit_cost, ps.total_cost, ps.production_date " +
                      "FROM ProductionStock ps " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "ORDER BY ps.product_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_id"),
                    rs.getString("product_name"),
                    rs.getString("brand_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_cost"),
                    rs.getDouble("total_cost"),
                    rs.getString("production_date")
                };
                productionStocks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productionStocks;
    }

    @Override
    public List<Object[]> getAllProductionStocksForDropdown() {
        List<Object[]> productionStocks = new ArrayList<>();
        String query = "SELECT ps.production_id, ps.product_name, b.brand_name, " +
                      "'N/A' as unit_name, ps.unit_cost, ps.quantity " +
                      "FROM ProductionStock ps " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE ps.quantity > 0 " +
                      "ORDER BY ps.product_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_id"),
                    rs.getString("product_name"),
                    "N/A", // category_name (not available in ProductionStock table)
                    rs.getString("brand_name"),
                    rs.getString("unit_name"),
                    rs.getDouble("unit_cost"),
                    rs.getDouble("quantity")
                };
                productionStocks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productionStocks;
    }

    @Override
    public String generateProductionInvoiceNumber() {
        String query = "SELECT COUNT(*) FROM Production_Invoice";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                return String.format("PI-%04d", count + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PI-0001";
    }

    @Override
    public int insertProductionInvoiceAndGetId(String productionDate, String notes) {
        String query = "INSERT INTO Production_Invoice (production_date, notes) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, productionDate);
            pstmt.setString(2, notes);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean insertProductionInvoiceItems(int productionInvoiceId, List<Object[]> productionItems) {
        String query = "INSERT INTO Production_Invoice_Item (production_invoice_id, " +
                      "production_id, quantity_produced) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            
            for (Object[] item : productionItems) {
                pstmt.setInt(1, productionInvoiceId);
                pstmt.setInt(2, (Integer) item[0]); // production_id from ProductionStock table
                pstmt.setDouble(3, (Double) item[1]); // quantity_produced
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertProductionStockRawUsage(int productionInvoiceId, List<Object[]> rawMaterialsUsed) {
        String query = "INSERT INTO Production_Stock_Raw_Usage (production_invoice_id, " +
                      "raw_stock_id, quantity_used) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            
            for (Object[] material : rawMaterialsUsed) {
                pstmt.setInt(1, productionInvoiceId);
                pstmt.setInt(2, (Integer) material[0]); // stock_id from Raw_Stock table
                pstmt.setDouble(3, (Double) material[1]); // quantity_used
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            
            // Update raw stock quantities
            String updateStockQuery = "UPDATE Raw_Stock SET quantity = quantity - ? " +
                                    "WHERE stock_id = ?";
            try (PreparedStatement updatePstmt = connection.prepareStatement(updateStockQuery)) {
                for (Object[] material : rawMaterialsUsed) {
                    updatePstmt.setDouble(1, (Double) material[1]); // quantity_used
                    updatePstmt.setInt(2, (Integer) material[0]); // stock_id
                    updatePstmt.addBatch();
                }
                updatePstmt.executeBatch();
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertProductionStock(String name, String category, String brand, String unit, 
                                       double openingQty, double salePrice, double reorderLevel) {
        String query = "INSERT INTO ProductionStock (product_name, brand_id, quantity, unit_cost, total_cost) " +
                      "VALUES (?, (SELECT brand_id FROM Brand WHERE brand_name = ? LIMIT 1), ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false); // Start transaction
            
            // Ensure brand exists
            ensureBrandExists(brand, 1); // Default tehsil_id = 1
            
            double totalCost = openingQty * salePrice;
            
            pstmt.setString(1, name);
            pstmt.setString(2, brand);
            pstmt.setInt(3, (int) openingQty);
            pstmt.setDouble(4, salePrice);
            pstmt.setDouble(5, totalCost);
            
            int result = pstmt.executeUpdate();
            connection.commit(); // Commit transaction
            connection.setAutoCommit(true); // Reset auto-commit
            
            return result > 0;
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback on error
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Object[]> getAllProductionInvoices() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT pi.production_invoice_id, pi.production_date, pi.notes, " +
                      "ps.product_name, pii.quantity_produced " +
                      "FROM Production_Invoice pi " +
                      "JOIN Production_Invoice_Item pii ON pi.production_invoice_id = pii.production_invoice_id " +
                      "JOIN ProductionStock ps ON pii.production_id = ps.production_id " +
                      "ORDER BY pi.production_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_invoice_id"),
                    rs.getString("production_date"),
                    rs.getString("notes"),
                    rs.getString("product_name"),
                    rs.getDouble("quantity_produced")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    @Override
    public List<Object[]> getAllSalesInvoices() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT si.sales_invoice_number, si.sales_date, c.customer_name, " +
                      "si.total_amount, si.discount_amount, si.paid_amount " +
                      "FROM Sales_Invoice si " +
                      "JOIN Customer c ON si.customer_id = c.customer_id " +
                      "ORDER BY si.sales_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("sales_invoice_number"),
                    rs.getString("sales_date"),
                    rs.getString("customer_name"),
                    rs.getDouble("total_amount"),
                    rs.getDouble("discount_amount"),
                    rs.getDouble("paid_amount")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    // --------------------------
    // Bank Management Operations
    // --------------------------
    @Override
    public List<Object[]> getAllBanks() {
        List<Object[]> banks = new ArrayList<>();
        String query = "SELECT bank_id, bank_name, account_number, branch_name, balance FROM Bank ORDER BY bank_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("bank_id"),
                    rs.getString("bank_name"),
                    rs.getString("account_number"),
                    rs.getString("branch_name"),
                    rs.getDouble("balance")
                };
                banks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return banks;
    }

    @Override
    public boolean insertBank(String bankName, String accountNumber, String branchName) {
        String query = "INSERT INTO Bank (bank_name, account_number, branch_name) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, bankName);
            pstmt.setString(2, accountNumber);
            pstmt.setString(3, branchName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Object[]> getAllBankTransactions() {
        List<Object[]> transactions = new ArrayList<>();
        String query = "SELECT bt.transaction_date, b.bank_name, bt.transaction_type, " +
                      "bt.amount, bt.description " +
                      "FROM Bank_Transaction bt " +
                      "JOIN Bank b ON bt.bank_id = b.bank_id " +
                      "ORDER BY bt.transaction_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("transaction_date"),
                    rs.getString("bank_name"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getString("description")
                };
                transactions.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

@Override
public List<Object[]> getAllCashTransactions() {
    List<Object[]> transactions = new ArrayList<>();
    String query = "SELECT " +
                 "    transaction_date AS date, " +
                 "    transaction_type, " +
                 "    amount, " +
                 "    description, " +
                 "    'cash' AS source " +
                 "FROM Cash_Transaction " +
                 "UNION ALL " +
                 "SELECT " +
                 "    transaction_date AS date, " +
                 "    transaction_type, " +
                 "    amount, " +
                 "    description, " +
                 "    'bank' AS source " +
                 "FROM Bank_Transaction " +
                 "ORDER BY date DESC";

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        
        while (rs.next()) {
            Object[] row = new Object[] {
                rs.getString("date"),
                rs.getString("transaction_type"),
                rs.getDouble("amount"),
                rs.getString("description"),
                rs.getString("source")
            };
            transactions.add(row);
        }
    } catch (SQLException e) {
        System.err.println("Error loading transactions: " + e.getMessage());
    }
    return transactions;
}
@Override
    public double getCurrentCashBalance() {
        String query = "SELECT " +
                      "IFNULL(SUM(CASE WHEN transaction_type IN ('cash_in', 'transfer_from_bank') THEN amount ELSE 0 END), 0) - " +
                      "IFNULL(SUM(CASE WHEN transaction_type IN ('cash_out', 'transfer_to_bank') THEN amount ELSE 0 END), 0) " +
                      "AS current_cash_balance " +
                      "FROM Cash_Transaction";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getDouble("current_cash_balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // --------------------------
    // Employee Management Operations
    // --------------------------
    @Override
    public List<Object[]> getAllEmployees() {
        List<Object[]> employees = new ArrayList<>();
        String query = "SELECT e.employee_id, e.employee_name, e.phone_number, e.cnic, e.address, d.designation_title, " +
                      "e.salary_type, e.salary_amount, " +
                      "CASE WHEN e.is_active = 1 THEN 'Active' ELSE 'Inactive' END as status " +
                      "FROM Employee e " +
                      "JOIN Designation d ON e.designation_id = d.designation_id " +
                      "ORDER BY e.employee_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("employee_id"),
                    rs.getString("employee_name"),
                    rs.getString("phone_number"),
                    rs.getString("cnic"),
                    rs.getString("address"),
                    rs.getString("designation_title"),
                    rs.getString("salary_type"),
                    rs.getDouble("salary_amount"),
                    rs.getString("status")
                };
                employees.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    @Override
    public boolean insertEmployee(String name, String phone, String cnic, String address, 
                                 String designation, String salaryType, double salaryAmount) {
        String query = "INSERT INTO Employee (employee_name, phone_number, cnic, address, hire_date, " +
                      "designation_id, salary_type, salary_amount) " +
                      "SELECT ?, ?, ?, ?, DATE('now'), d.designation_id, ?, ? " +
                      "FROM Designation d " +
                      "WHERE d.designation_title = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, cnic);
            pstmt.setString(4, address);
            pstmt.setString(5, salaryType);
            pstmt.setDouble(6, salaryAmount);
            pstmt.setString(7, designation);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Object[]> getAllEmployeeAttendance() {
        List<Object[]> attendance = new ArrayList<>();
        String query = "SELECT e.employee_name, ea.attendance_date, ea.status, ea.working_hours " +
                      "FROM Employee_Attendance ea " +
                      "JOIN Employee e ON ea.employee_id = e.employee_id " +
                      "ORDER BY ea.attendance_date DESC, e.employee_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getString("attendance_date"),
                    rs.getString("status"),
                    rs.getDouble("working_hours")
                };
                attendance.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendance;
    }

    @Override
    public List<Object[]> getAllEmployeeSalaryPayments() {
        List<Object[]> payments = new ArrayList<>();
        String query = "SELECT e.employee_name, esp.payment_date, esp.salary_amount, esp.description " +
                      "FROM Employee_Salary_Payment esp " +
                      "JOIN Employee e ON esp.employee_id = e.employee_id " +
                      "ORDER BY esp.payment_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getString("payment_date"),
                    rs.getDouble("salary_amount"),
                    rs.getString("description")
                };
                payments.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public List<Object[]> getAllEmployeeLoans() {
        List<Object[]> loans = new ArrayList<>();
        String query = "SELECT e.employee_name, el.loan_amount, el.loan_date, el.due_date, el.description, " +
                      "el.status, el.remaining_amount, el.loan_id " +
                      "FROM Employee_Loan el " +
                      "JOIN Employee e ON el.employee_id = e.employee_id " +
                      "ORDER BY el.loan_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getDouble("loan_amount"),
                    rs.getString("loan_date"),
                    rs.getString("due_date"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getDouble("remaining_amount"),
                    rs.getInt("loan_id")
                };
                loans.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    @Override
    public boolean updateEmployee(int employeeId, String name, String phone, String cnic, String address, 
                                 String designation, String salaryType, double salaryAmount) {
        String query = "UPDATE Employee SET employee_name = ?, phone_number = ?, cnic = ?, address = ?, " +
                      "designation_id = (SELECT designation_id FROM Designation WHERE designation_title = ?), " +
                      "salary_type = ?, salary_amount = ? " +
                      "WHERE employee_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, cnic);
            pstmt.setString(4, address);
            pstmt.setString(5, designation);
            pstmt.setString(6, salaryType);
            pstmt.setDouble(7, salaryAmount);
            pstmt.setInt(8, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteEmployee(int employeeId) {
        String query = "UPDATE Employee SET is_active = 0 WHERE employee_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --------------------------
    // Employee Attendance Operations
    // --------------------------
    @Override
    public boolean insertEmployeeAttendance(int employeeId, String attendanceDate, String status, double workingHours) {
        // Check if attendance already exists for this employee on this date
        String checkQuery = "SELECT COUNT(*) FROM Employee_Attendance WHERE employee_id = ? AND attendance_date = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, employeeId);
            checkStmt.setString(2, attendanceDate);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Attendance already exists, update it instead
                String updateQuery = "UPDATE Employee_Attendance SET status = ?, working_hours = ? WHERE employee_id = ? AND attendance_date = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, status.toLowerCase());
                    updateStmt.setDouble(2, workingHours);
                    updateStmt.setInt(3, employeeId);
                    updateStmt.setString(4, attendanceDate);
                    return updateStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Insert new attendance record
        String insertQuery = "INSERT INTO Employee_Attendance (employee_id, attendance_date, status, working_hours) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setInt(1, employeeId);
            pstmt.setString(2, attendanceDate);
            pstmt.setString(3, status.toLowerCase());
            pstmt.setDouble(4, workingHours);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Object[]> getEmployeeAttendanceByDateRange(String startDate, String endDate) {
        List<Object[]> attendance = new ArrayList<>();
        String query = "SELECT e.employee_id, e.employee_name, ea.attendance_date, ea.status, ea.working_hours " +
                      "FROM Employee_Attendance ea " +
                      "JOIN Employee e ON ea.employee_id = e.employee_id " +
                      "WHERE ea.attendance_date >= ? AND ea.attendance_date <= ? " +
                      "ORDER BY ea.attendance_date DESC, e.employee_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("employee_id"),
                    rs.getString("employee_name"),
                    rs.getString("attendance_date"),
                    rs.getString("status"),
                    rs.getDouble("working_hours")
                };
                attendance.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendance;
    }

    @Override
    public List<Object[]> getEmployeeAttendanceByEmployee(int employeeId) {
        List<Object[]> attendance = new ArrayList<>();
        String query = "SELECT e.employee_name, ea.attendance_date, ea.status, ea.working_hours " +
                      "FROM Employee_Attendance ea " +
                      "JOIN Employee e ON ea.employee_id = e.employee_id " +
                      "WHERE ea.employee_id = ? " +
                      "ORDER BY ea.attendance_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getString("attendance_date"),
                    rs.getString("status"),
                    rs.getDouble("working_hours")
                };
                attendance.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendance;
    }

    @Override
    public int getEmployeeIdByName(String employeeName) {
        String query = "SELECT employee_id FROM Employee WHERE employee_name = ? AND is_active = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, employeeName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("employee_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if employee not found
    }

    // --------------------------
    // Salesman Operations
    // --------------------------
    @Override
    public List<Object[]> getAllSalesmen() {
        List<Object[]> salesmen = new ArrayList<>();
        String query = "SELECT salesman_id, salesman_name, contact_number, address, commission_rate FROM Salesman ORDER BY salesman_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("salesman_id"),
                    rs.getString("salesman_name"),
                    rs.getString("contact_number"),
                    rs.getString("address"),
                    rs.getDouble("commission_rate")
                };
                salesmen.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesmen;
    }

    @Override
    public boolean insertSalesman(String name, String contact, String address, double commissionRate) {
        String query = "INSERT INTO Salesman (salesman_name, contact_number, address, commission_rate) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, address);
            pstmt.setDouble(4, commissionRate);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateSalesman(int salesmanId, String name, String contact, String address, double commissionRate) {
        String query = "UPDATE Salesman SET salesman_name = ?, contact_number = ?, address = ?, commission_rate = ? WHERE salesman_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, address);
            pstmt.setDouble(4, commissionRate);
            pstmt.setInt(5, salesmanId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    ///////////////////////////////////////////////////////////////////
    /// Un sare Functions ki mock implementation
    /// 
    /// 
    /// 
    /// 
    

    // Delete Methods
    @Override
    public boolean deleteCategory(String categoryName) {
        String query = "DELETE FROM Category WHERE category_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, categoryName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteManufacturer(String name) {
        String query = "DELETE FROM Manufacturer WHERE manufacturer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteBrand(String name) {
        String query = "DELETE FROM Brand WHERE brand_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteProvince(String provinceName) {
        String query = "DELETE FROM Province WHERE province_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, provinceName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteDistrict(String districtName) {
        String query = "DELETE FROM District WHERE district_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, districtName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteTehsil(String tehsilName) {
        String query = "DELETE FROM Tehsil WHERE tehsil_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tehsilName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteUnit(String unitName) {
        String query = "DELETE FROM Unit WHERE unit_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteCustomer(String name) {
        String query = "DELETE FROM Customer WHERE customer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteSupplier(String name) {
        String query = "DELETE FROM Supplier WHERE supplier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --------------------------
    // Designation Operations Implementation
    // --------------------------
    @Override
    public List<Object[]> getAllDesignations() {
        List<Object[]> designations = new ArrayList<>();
        String query = "SELECT designation_id, designation_title FROM Designation ORDER BY designation_title";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("designation_id"),
                    rs.getString("designation_title")
                };
                designations.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return designations;
    }

    @Override
    public boolean insertDesignation(String designationTitle) {
        String query = "INSERT INTO Designation (designation_title) VALUES (?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, designationTitle);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateDesignation(int designationId, String designationTitle) {
        String query = "UPDATE Designation SET designation_title = ? WHERE designation_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, designationTitle);
            pstmt.setInt(2, designationId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteDesignation(int designationId) {
        // First check if the designation is being used by any employee
        String checkQuery = "SELECT COUNT(*) FROM Employee WHERE designation_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, designationId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                // Designation is being used, cannot delete
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // If not being used, proceed with deletion
        String deleteQuery = "DELETE FROM Designation WHERE designation_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, designationId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getLastAttendanceStatus(int empId) {
        String query = "SELECT status FROM Employee_Attendance WHERE employee_id = ? ORDER BY attendance_date DESC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, empId);
            try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("status");
            }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --------------------------
    // Employee Advance Salary Operations
    // --------------------------
    public boolean insertAdvanceSalary(int employeeId, double amount, String advanceDate, String description) {
        String query = "INSERT INTO Employee_Advance_Salary (employee_id, amount, advance_date, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, advanceDate);
            pstmt.setString(4, description);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Object[]> getAllAdvanceSalaries() {
        List<Object[]> advances = new ArrayList<>();
        String query = "SELECT e.employee_name, eas.amount, eas.advance_date, eas.description, eas.status " +
                      "FROM Employee_Advance_Salary eas " +
                      "JOIN Employee e ON eas.employee_id = e.employee_id " +
                      "ORDER BY eas.advance_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getDouble("amount"),
                    rs.getString("advance_date"),
                    rs.getString("description"),
                    rs.getString("status")
                };
                advances.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return advances;
    }

    public List<Object[]> getAdvanceSalariesByDateRange(String startDate, String endDate) {
        List<Object[]> advances = new ArrayList<>();
        String query = "SELECT e.employee_name, eas.amount, eas.advance_date, eas.description, eas.status " +
                      "FROM Employee_Advance_Salary eas " +
                      "JOIN Employee e ON eas.employee_id = e.employee_id " +
                      "WHERE eas.advance_date >= ? AND eas.advance_date <= ? " +
                      "ORDER BY eas.advance_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getDouble("amount"),
                    rs.getString("advance_date"),
                    rs.getString("description"),
                    rs.getString("status")
                };
                advances.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return advances;
    }

    // --------------------------
    // Salary Report Operations
    // --------------------------
    public List<Object[]> getSalaryReportByDateRange(String startDate, String endDate) {
        List<Object[]> salaryData = new ArrayList<>();
        String query = "SELECT e.employee_id, e.employee_name, d.designation_title, e.salary_type, e.salary_amount, " +
                      "COALESCE(SUM(CASE WHEN ea.status = 'present' THEN ea.working_hours ELSE 0 END), 0) as total_hours, " +
                      "COALESCE(COUNT(CASE WHEN ea.status = 'present' THEN 1 END), 0) as present_days, " +
                      "COALESCE(COUNT(CASE WHEN ea.status = 'absent' THEN 1 END), 0) as absent_days " +
                      "FROM Employee e " +
                      "LEFT JOIN Designation d ON e.designation_id = d.designation_id " +
                      "LEFT JOIN Employee_Attendance ea ON e.employee_id = ea.employee_id " +
                      "AND ea.attendance_date >= ? AND ea.attendance_date <= ? " +
                      "WHERE e.is_active = 1 " +
                      "GROUP BY e.employee_id, e.employee_name, d.designation_title, e.salary_type, e.salary_amount " +
                      "ORDER BY e.employee_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("employee_id"),
                    rs.getString("employee_name"),
                    rs.getString("designation_title"),
                    rs.getString("salary_type"),
                    rs.getDouble("salary_amount"),
                    rs.getDouble("total_hours"),
                    rs.getInt("present_days"),
                    rs.getInt("absent_days")
                };
                salaryData.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salaryData;
    }
    
    // --------------------------
    // Employee Loan Operations
    // --------------------------
    public boolean insertEmployeeLoan(int employeeId, double loanAmount, String loanDate, String dueDate, String description) {
        String query = "INSERT INTO Employee_Loan (employee_id, loan_amount, loan_date, due_date, description, remaining_amount) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            pstmt.setDouble(2, loanAmount);
            pstmt.setString(3, loanDate);
            pstmt.setString(4, dueDate);
            pstmt.setString(5, description);
            pstmt.setDouble(6, loanAmount); // Initially, remaining amount equals loan amount
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Object[]> getEmployeeLoansByDateRange(String startDate, String endDate) {
        List<Object[]> loans = new ArrayList<>();
        String query = "SELECT e.employee_name, el.loan_amount, el.loan_date, el.due_date, el.description, " +
                      "el.status, el.remaining_amount, el.loan_id " +
                      "FROM Employee_Loan el " +
                      "JOIN Employee e ON el.employee_id = e.employee_id " +
                      "WHERE el.loan_date >= ? AND el.loan_date <= ? " +
                      "ORDER BY el.loan_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getDouble("loan_amount"),
                    rs.getString("loan_date"),
                    rs.getString("due_date"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getDouble("remaining_amount"),
                    rs.getInt("loan_id")
                };
                loans.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    public List<Object[]> getLoansByEmployee(String employeeName) {
        List<Object[]> loans = new ArrayList<>();
        String query = "SELECT e.employee_name, el.loan_amount, el.loan_date, el.due_date, el.description, " +
                      "el.status, el.remaining_amount, el.loan_id " +
                      "FROM Employee_Loan el " +
                      "JOIN Employee e ON el.employee_id = e.employee_id " +
                      "WHERE e.employee_name LIKE ? " +
                      "ORDER BY el.loan_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + employeeName + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getDouble("loan_amount"),
                    rs.getString("loan_date"),
                    rs.getString("due_date"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getDouble("remaining_amount"),
                    rs.getInt("loan_id")
                };
                loans.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    public boolean updateLoanStatus(int loanId, String status, double remainingAmount) {
        String query = "UPDATE Employee_Loan SET status = ?, remaining_amount = ? WHERE loan_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setDouble(2, remainingAmount);
            pstmt.setInt(3, loanId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

public List<Object[]> getViewData(String viewName, Map<String, String> filters) {
    List<Object[]> results = new ArrayList<>();
    StringBuilder query = new StringBuilder("SELECT * FROM " + viewName);
    List<String> values = new ArrayList<>();

    // Map view names to their respective date columns
    Map<String, String> dateColumnMap = new HashMap<>();
    dateColumnMap.put("View_Purchase_Book", "invoice_date");
    dateColumnMap.put("View_Raw_Stock_Book", "usage_date");
    // Add other views and their date columns as needed

    String dateColumn = dateColumnMap.getOrDefault(viewName, "date"); // Default to 'date' if view not mapped

    System.out.println("View Name: " + viewName);
    if (filters != null && !filters.isEmpty()) {
        query.append(" WHERE ");
        List<String> clauses = new ArrayList<>();

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.equals("fromDate")) {
                clauses.add(dateColumn + " >= ?");
                values.add(value);
            } else if (key.equals("toDate")) {
                clauses.add(dateColumn + " <= ?");
                values.add(value);
            } else {
                clauses.add(key + " LIKE ?");
                values.add("%" + value + "%");
            }
        }
        query.append(String.join(" AND ", clauses));
    }

    System.out.println("Query: " + query.toString() + ", Values: " + values);
    try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
        for (int i = 0; i < values.size(); i++) {
            stmt.setString(i + 1, values.get(i));
        }
        try (ResultSet rs = stmt.executeQuery()) {
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                results.add(row);
            }
        }
    } catch (SQLException e) {
        System.err.println("SQL Error: " + e.getMessage());
        e.printStackTrace();
    }
    System.out.println("Rows returned: " + results.size());
    System.out.println("View_" + viewName + " results: " + results.size() + " rows");
    System.out.println("Table items count: " + results.size());
    return results;
}
@Override
    public List<Object[]> getAllProductionStock() {
        // Dummy implementation: returns an empty list
        return new ArrayList<>();
    }

    @Override
    public List<Object[]> getAllRawStock() {
        // Dummy implementation: returns an empty list
        return new ArrayList<>();
    }


}
