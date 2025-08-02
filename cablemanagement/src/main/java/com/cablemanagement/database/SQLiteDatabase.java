package com.cablemanagement.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    
    public List<Object[]> getLastProductionReturnInvoice() {
        List<Object[]> result = new ArrayList<>();
        String query = "SELECT return_invoice_number, return_date, notes FROM Production_Return_Invoice " +
                      "ORDER BY production_return_invoice_id DESC LIMIT 1";
                      
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String jdbcUrl = "jdbc:sqlite:cable_management.db";
            connection = DriverManager.getConnection(jdbcUrl);
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("return_invoice_number"),
                    rs.getString("return_date"),
                    rs.getString("notes")
                };
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    
    public List<Object[]> getProductionReturnInvoiceItems(String returnInvoiceNumber) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT ps.product_name, pri.quantity_returned, pri.unit_cost " +
                      "FROM Production_Return_Invoice_Item pri " +
                      "JOIN Production_Return_Invoice priv ON pri.production_return_invoice_id = priv.production_return_invoice_id " +
                      "JOIN ProductionStock ps ON pri.production_id = ps.production_id " +
                      "WHERE priv.return_invoice_number = ?";
                      
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String jdbcUrl = "jdbc:sqlite:cable_management.db";
            connection = DriverManager.getConnection(jdbcUrl);
            stmt = connection.prepareStatement(query);
            stmt.setString(1, returnInvoiceNumber);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] item = {
                    rs.getString("product_name"),
                    rs.getDouble("quantity_returned"),
                    rs.getDouble("unit_cost")
                };
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return items;
    }
    
    private Connection connection;
    private String databasePath;
    
    // Implement missing methods from db interface

    @Override
    public List<Object[]> getAllSalesReturnInvoicesForDropdown() {
        List<Object[]> returnInvoices = new ArrayList<>();
        String query = "SELECT sr.sales_return_invoice_id, sr.return_invoice_number, c.customer_name, sr.return_date " +
                      "FROM SalesReturnInvoice sr " +
                      "INNER JOIN Customer c ON sr.customer_id = c.customer_id " +
                      "ORDER BY sr.sales_return_invoice_id DESC";
                      
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Object[] invoiceData = {
                    rs.getInt("sales_return_invoice_id"),
                    rs.getString("return_invoice_number"),
                    rs.getString("customer_name"),
                    rs.getString("return_date")
                };
                returnInvoices.add(invoiceData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnInvoices;
    }

    @Override
    public List<Object[]> getSalesReturnInvoiceItemsByInvoiceId(int returnInvoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT sri.*, ps.product_name " +
                      "FROM SalesReturnInvoiceItem sri " +
                      "INNER JOIN ProductionStock ps ON sri.production_stock_id = ps.production_stock_id " +
                      "WHERE sri.sales_return_invoice_id = ?";
                      
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, returnInvoiceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] itemData = {
                        rs.getInt("sales_return_invoice_item_id"),
                        rs.getString("product_name"),
                        rs.getDouble("quantity"),
                        rs.getDouble("unit_price")
                    };
                    items.add(itemData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public boolean updateBankBalance(double newBalance) {
        // Update the balance for all banks (or you may want to specify a bank_id)
        String query = "UPDATE Bank SET balance = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, newBalance);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
    public boolean updateCashTransaction(BankTransaction transaction) {
        String query = "UPDATE Cash_Transaction SET transaction_type = ?, amount = ?, description = ?, transaction_date = ? WHERE transaction_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, transaction.getTransactionType());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getTransactionDate());
            pstmt.setInt(5, transaction.getTransactionId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteCashTransaction(int transactionId) {
        String query = "DELETE FROM Cash_Transaction WHERE transaction_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, transactionId);
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
            // Add sample data for testing reports
            // addSampleDataForTesting();
        }
    
    public SQLiteDatabase(String databasePath) {
        this.databasePath = databasePath;
        // Auto-connect when instantiated
        connect(databasePath, null, null);
        // Initialize all required tables
        initializeDatabase();
        // Add sample data for testing reports
        // addSampleDataForTesting();
    }

    @Override
    public String connect(String url, String user, String password) {
        try {
            String jdbcUrl = "jdbc:sqlite:" + (url != null ? url : databasePath);
            connection = DriverManager.getConnection(jdbcUrl);
            
            try (Statement stmt = connection.createStatement()) {
                // Enable foreign keys
                stmt.execute("PRAGMA foreign_keys = ON");
                
                // Set timeout for busy connections (in milliseconds)
                stmt.execute("PRAGMA busy_timeout = 30000");
                
                // Use WAL mode for better concurrency
                stmt.execute("PRAGMA journal_mode = WAL");
                
                // Optimize for better performance
                stmt.execute("PRAGMA synchronous = NORMAL");
                stmt.execute("PRAGMA cache_size = 10000");
                stmt.execute("PRAGMA temp_store = memory");
                
                System.out.println("DEBUG: SQLite connection configured with lock prevention settings");
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
                    "sale_price REAL DEFAULT 0.0," +
                    "FOREIGN KEY (brand_id) REFERENCES Brand(brand_id)" +
                    ")",

                    // Unit table
                    "CREATE TABLE IF NOT EXISTS Unit (" +
                    "unit_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "unit_name TEXT NOT NULL UNIQUE" +
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
                    ")",

                    // Production Return Invoice table
                    "CREATE TABLE IF NOT EXISTS Production_Return_Invoice (" +
                    "production_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "return_invoice_number TEXT NOT NULL UNIQUE," +
                    "original_production_invoice_id INTEGER NOT NULL," +
                    "return_date TEXT NOT NULL," +
                    "total_return_quantity REAL NOT NULL," +
                    "total_return_amount REAL NOT NULL," +
                    "notes TEXT," +
                    "FOREIGN KEY (original_production_invoice_id) REFERENCES Production_Invoice(production_invoice_id)" +
                    ")",

                    // Production Return Invoice Item table
                    "CREATE TABLE IF NOT EXISTS Production_Return_Invoice_Item (" +
                    "production_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "production_return_invoice_id INTEGER NOT NULL," +
                    "production_id INTEGER NOT NULL," +
                    "quantity_returned REAL NOT NULL," +
                    "unit_cost REAL NOT NULL," +
                    "total_cost REAL NOT NULL," +
                    "FOREIGN KEY (production_return_invoice_id) REFERENCES Production_Return_Invoice(production_return_invoice_id)," +
                    "FOREIGN KEY (production_id) REFERENCES ProductionStock(production_id)" +
                    ")",

                    // --- ADDED: Correct Cash_Transaction table ---
                    "CREATE TABLE IF NOT EXISTS Cash_Transaction (" +
                    "cash_transaction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "transaction_date TEXT NOT NULL," +
                    "transaction_type TEXT NOT NULL CHECK(transaction_type IN ('cash_in', 'cash_out', 'transfer_to_bank', 'transfer_from_bank'))," +
                    "amount REAL NOT NULL," +
                    "description TEXT" +
                    ")",

                    // --- ADDED: Correct Bank_Transaction table ---
                    "CREATE TABLE IF NOT EXISTS Bank_Transaction (" +
                    "bank_transaction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "bank_id INTEGER NOT NULL," +
                    "transaction_date TEXT NOT NULL," +
                    "transaction_type TEXT NOT NULL CHECK(transaction_type IN ('deposit', 'withdraw', 'transfer_in', 'transfer_out', 'invoice_payment'))," +
                    "amount REAL NOT NULL," +
                    "description TEXT," +
                    "related_bank_id INTEGER," +
                    "FOREIGN KEY (bank_id) REFERENCES Bank(bank_id)," +
                    "FOREIGN KEY (related_bank_id) REFERENCES Bank(bank_id)" +
                    ")",

                    // Raw Stock Use Invoice table
                    "CREATE TABLE IF NOT EXISTS Raw_Stock_Use_Invoice (" +
                    "raw_stock_use_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "use_invoice_number TEXT NOT NULL UNIQUE," +
                    "usage_date TEXT NOT NULL," +
                    "total_usage_amount REAL NOT NULL DEFAULT 0.0," +
                    "reference_purpose TEXT," +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ")",

                    // Raw Stock Use Invoice Item table
                    "CREATE TABLE IF NOT EXISTS Raw_Stock_Use_Invoice_Item (" +
                    "raw_stock_use_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "raw_stock_use_invoice_id INTEGER NOT NULL," +
                    "raw_stock_id INTEGER NOT NULL," +
                    "quantity_used REAL NOT NULL," +
                    "unit_cost REAL NOT NULL," +
                    "total_cost REAL NOT NULL," +
                    "FOREIGN KEY (raw_stock_use_invoice_id) REFERENCES Raw_Stock_Use_Invoice(raw_stock_use_invoice_id)," +
                    "FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)" +
                    ")",

                    // Sales Invoice table
                    "CREATE TABLE IF NOT EXISTS Sales_Invoice (" +
                    "sales_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sales_invoice_number TEXT NOT NULL UNIQUE," +
                    "customer_id INTEGER NOT NULL," +
                    "sales_date TEXT NOT NULL," +
                    "total_amount REAL NOT NULL DEFAULT 0.0," +
                    "discount_amount REAL NOT NULL DEFAULT 0.0," +
                    "paid_amount REAL NOT NULL DEFAULT 0.0," +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)" +
                    ")",

                    // Sales Invoice Item table
                    "CREATE TABLE IF NOT EXISTS Sales_Invoice_Item (" +
                    "sales_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sales_invoice_id INTEGER NOT NULL," +
                    "production_stock_id INTEGER NOT NULL," +
                    "quantity REAL NOT NULL," +
                    "unit_price REAL NOT NULL," +
                    "total_price REAL NOT NULL," +
                    "FOREIGN KEY (sales_invoice_id) REFERENCES Sales_Invoice(sales_invoice_id)," +
                    "FOREIGN KEY (production_stock_id) REFERENCES ProductionStock(production_id)" +
                    ")",

                    // Sales Return Invoice table
                    "CREATE TABLE IF NOT EXISTS Sales_Return_Invoice (" +
                    "sales_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "return_invoice_number TEXT NOT NULL UNIQUE," +
                    "original_sales_invoice_id INTEGER NOT NULL," +
                    "customer_id INTEGER NOT NULL," +
                    "return_date TEXT NOT NULL," +
                    "total_return_amount REAL NOT NULL DEFAULT 0.0," +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (original_sales_invoice_id) REFERENCES Sales_Invoice(sales_invoice_id)," +
                    "FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)" +
                    ")",

                    // Sales Return Invoice Item table
                    "CREATE TABLE IF NOT EXISTS Sales_Return_Invoice_Item (" +
                    "sales_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sales_return_invoice_id INTEGER NOT NULL," +
                    "production_stock_id INTEGER NOT NULL," +
                    "quantity REAL NOT NULL," +
                    "unit_price REAL NOT NULL," +
                    "total_price REAL NOT NULL," +
                    "FOREIGN KEY (sales_return_invoice_id) REFERENCES Sales_Return_Invoice(sales_return_invoice_id)," +
                    "FOREIGN KEY (production_stock_id) REFERENCES ProductionStock(production_id)" +
                    ")"
                };
                
            // Execute all table creation queries
            for (String query : createTableQueries) {
                stmt.execute(query);
            }
            
            // Insert some default data if tables are empty
            // insertDefaultData(stmt);
            
            stmt.close();
            System.out.println("Database initialized successfully with all required tables.");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // private void insertDefaultData(Statement stmt) throws SQLException {
    //     // Check if Province table is empty and insert default data
    //     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Province");
    //     rs.next();
    //     if (rs.getInt(1) == 0) {
    //         stmt.execute("INSERT INTO Province (province_name) VALUES " +
    //                     "('Punjab'), ('Sindh'), ('KPK'), ('Balochistan'), ('Gilgit-Baltistan')");
    //     }
    //     rs.close();
        
    //     // Check if Category table is empty and insert default data
    //     rs = stmt.executeQuery("SELECT COUNT(*) FROM Category");
    //     rs.next();
    //     if (rs.getInt(1) == 0) {
    //         stmt.execute("INSERT INTO Category (category_name) VALUES " +
    //                     "('Electric Cables'), ('Fiber Optic'), ('Coaxial'), ('Network Cables'), ('Power Cables'), ('ABC')");
    //     }
    //     rs.close();
        
    //     // Check if District table is empty and insert some default districts
    //     rs = stmt.executeQuery("SELECT COUNT(*) FROM District");
    //     rs.next();
    //     if (rs.getInt(1) == 0) {
    //         stmt.execute("INSERT INTO District (district_name, province_id) VALUES " +
    //                     "('Lahore', 1), ('Karachi', 2), ('Peshawar', 3), ('Quetta', 4), ('Islamabad', 1)");
    //     }
    //     rs.close();
        
    //     // Check if Designation table is empty and insert default data
    //     rs = stmt.executeQuery("SELECT COUNT(*) FROM Designation");
    //     rs.next();
    //     if (rs.getInt(1) == 0) {
    //         stmt.execute("INSERT INTO Designation (designation_title) VALUES " +
    //                     "('Manager'), ('Technician'), ('Sales Representative'), ('Accountant'), ('Supervisor')");
    //     }
    //     rs.close();
        
    //     // Check if Tehsil table is empty and insert some default tehsils
    //     rs = stmt.executeQuery("SELECT COUNT(*) FROM Tehsil");
    //     rs.next();
    //     if (rs.getInt(1) == 0) {
    //         stmt.execute("INSERT INTO Tehsil (tehsil_name, district_id) VALUES " +
    //                     "('Model Town', 1), ('Gulshan', 2), ('University Town', 3), ('Satellite Town', 4), ('F-10', 5)");
    //     }
    //     rs.close();
        
    //     // Check if Unit table is empty and insert default data
    //     rs = stmt.executeQuery("SELECT COUNT(*) FROM Unit");
    //     rs.next();
    //     if (rs.getInt(1) == 0) {
    //         stmt.execute("INSERT INTO Unit (unit_name) VALUES " +
    //                     "('Meter'), ('Roll'), ('Kg'), ('Gram'), ('Piece'), ('Box'), ('Liter')");
    //     }
    //     rs.close();
        
    //     // Check if Manufacturer table is empty and insert some default manufacturers
    //     rs = stmt.executeQuery("SELECT COUNT(*) FROM Manufacturer");
    //     rs.next();
    //     if (rs.getInt(1) == 0) {
    //         stmt.execute("INSERT INTO Manufacturer (manufacturer_name, tehsil_id) VALUES " +
    //                     "('CableTech Industries', 1), ('ABC Manufacturing', 1), ('Test Manufacturer', 1)");
    //     }
    //     rs.close();
        
    //     // Check if Brand table is empty and insert some default brands
    //     rs = stmt.executeQuery("SELECT COUNT(*) FROM Brand");
    //     rs.next();
    //     if (rs.getInt(1) == 0) {
    //         stmt.execute("INSERT INTO Brand (brand_name, manufacturer_id) VALUES " +
    //                     "('PowerFlex', 1), ('ABC', 2), ('aa', 3)");
    //     }
    //     rs.close();
        
    //     // Check if ProductionStock table is empty and insert some default production stock
    //     rs = stmt.executeQuery("SELECT COUNT(*) FROM ProductionStock");
    //     rs.next();
    //     if (rs.getInt(1) == 0) {
    //         stmt.execute("INSERT INTO ProductionStock (product_name, brand_id, quantity, unit_cost, total_cost, sale_price) VALUES " +
    //                     "('Copper Cable 10mm', 1, 50, 450.00, 22500.00, 540.00), " +
    //                     "('PVC Sheathed Wire 6mm', 2, 30, 320.00, 9600.00, 384.00), " +
    //                     "('Fiber Optic Cable', 1, 25, 800.00, 20000.00, 960.00), " +
    //                     "('Power Cable 16mm', 3, 40, 650.00, 26000.00, 780.00)");
    //     }
    //     rs.close();
        
    //     // Ensure all required views exist
    //     ensureViewsExist();
    // }
    
    

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
                    "JOIN Tehsil t ON m.tehsil_id = t.tehsil_id " +
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
        // For now, use a simple approach since Brand table doesn't have tehsil_id
        // We'll just use the first available manufacturer or default manufacturer
        String insertQuery = "INSERT INTO Brand (brand_name, manufacturer_id) VALUES (?, ?)";
        
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setString(1, name);
            insertStmt.setInt(2, 1); // Use default manufacturer_id = 1
            
            return insertStmt.executeUpdate() > 0;
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



    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT c.customer_name, c.contact_number, t.tehsil_name " +
                    "FROM Customer c " +
                    "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                    "ORDER BY c.customer_name";
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String name = rs.getString("customer_name");
                String contact = rs.getString("contact_number");
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
        String insertQuery = "INSERT INTO Customer (customer_name, contact_number, tehsil_id) VALUES (?, ?, ?)";
        
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
        String insertQuery = "INSERT INTO Customer (customer_name, contact_number, tehsil_id) VALUES (?, ?, ?)";
        
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
        String query = "SELECT s.supplier_name, s.contact_number, t.tehsil_name " +
                    "FROM Supplier s " +
                    "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                    "ORDER BY s.supplier_name";
        
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String name = rs.getString("supplier_name");
                String contact = rs.getString("contact_number");
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
        String insertQuery = "INSERT INTO Supplier (supplier_name, contact_number, tehsil_id) VALUES (?, ?, ?)";
        
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
        String insertQuery = "INSERT INTO Supplier (supplier_name, contact_number, tehsil_id) VALUES (?, ?, ?)";
        
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

        // Insert Default Brand with a default manufacturer_id (no tehsil_id needed)
        String insertBrandQuery = "INSERT INTO Brand (brand_name, manufacturer_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertBrandQuery)) {
            pstmt.setString(1, brandName);
            pstmt.setInt(2, 1); // Assume manufacturer_id = 1 exists; adjust as needed
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
                        pstmt.setInt(3, item.getQuantity().intValue());
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

                        pstmt.setInt(1, item.getQuantity().intValue());
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
    
    @Override
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
        String query = "SELECT rpii.raw_stock_id, rs.item_name, c.category_name, " +
                      "b.brand_name, u.unit_name, rpii.quantity, rpii.unit_price " +
                      "FROM Raw_Purchase_Invoice_Item rpii " +
                      "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
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
     * Get current stock quantity for a raw stock item
     */
    private double getCurrentStockQuantity(int stockId) throws SQLException {
        String query = "SELECT quantity FROM Raw_Stock WHERE stock_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, stockId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("quantity");
                }
            }
        }
        return 0.0;
    }

    /**
     * Get current stock quantity for a raw stock item (public method)
     */
    public double getCurrentRawStockQuantity(int stockId) {
        try {
            return getCurrentStockQuantity(stockId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Insert raw purchase return invoice items and update stock quantities
     */
    public boolean insertRawPurchaseReturnInvoiceItems(int returnInvoiceId, 
                                                      List<com.cablemanagement.model.RawStockPurchaseItem> items) {
        String insertQuery = "INSERT INTO Raw_Purchase_Return_Invoice_Item " +
                           "(raw_purchase_return_invoice_id, raw_stock_id, quantity, unit_price) " +
                           "VALUES (?, ?, ?, ?)";
        
        try {
            // Start transaction
            connection.setAutoCommit(false);
            
            // First validate that we have enough stock for all items
            for (com.cablemanagement.model.RawStockPurchaseItem item : items) {
                double currentStock = getCurrentStockQuantity(item.getRawStockId());
                if (currentStock < item.getQuantity()) {
                    System.err.println("Insufficient stock for item ID " + item.getRawStockId() + 
                                     ": Current=" + currentStock + ", Requested=" + item.getQuantity());
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }
            }
            
            // Insert return invoice items
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                for (com.cablemanagement.model.RawStockPurchaseItem item : items) {
                    pstmt.setInt(1, returnInvoiceId);
                    pstmt.setInt(2, item.getRawStockId());
                    pstmt.setDouble(3, item.getQuantity());
                    pstmt.setDouble(4, item.getUnitPrice());
                    pstmt.addBatch();
                }
                
                int[] insertResults = pstmt.executeBatch();
                
                // Check if all items were inserted successfully
                for (int result : insertResults) {
                    if (result <= 0) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        return false;
                    }
                }
            }
            
            // Update raw stock quantities (reduce stock as items are being returned to supplier)
            String updateStockQuery = "UPDATE Raw_Stock SET quantity = quantity - ?, total_cost = total_cost - ? " +
                                    "WHERE stock_id = ?";
            try (PreparedStatement updatePstmt = connection.prepareStatement(updateStockQuery)) {
                for (com.cablemanagement.model.RawStockPurchaseItem item : items) {
                    double totalCostReduction = item.getQuantity() * item.getUnitPrice();
                    updatePstmt.setDouble(1, item.getQuantity()); // reduce quantity
                    updatePstmt.setDouble(2, totalCostReduction); // reduce total cost
                    updatePstmt.setInt(3, item.getRawStockId());
                    updatePstmt.addBatch();
                }
                updatePstmt.executeBatch();
            }
            
            // Commit transaction
            connection.commit();
            connection.setAutoCommit(true);
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
                      "raw_stock_id, quantity_used, unit_cost, total_cost) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false); // Start transaction
            
            for (RawStockUseItem item : items) {
                pstmt.setInt(1, useInvoiceId);
                pstmt.setInt(2, item.getRawStockId());
                pstmt.setDouble(3, item.getQuantityUsed());
                pstmt.setDouble(4, item.getUnitCost());
                pstmt.setDouble(5, item.getTotalCost()); // Add total_cost field
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
                      "FROM Raw_Stock_Use_Invoice rsui " +
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

    /**
     * Get raw stock usage report by date range
     */
    public List<Object[]> getRawStockUsageReportByDateRange(String startDate, String endDate) {
        List<Object[]> usageReport = new ArrayList<>();
        String query = "SELECT rs.item_name, b.brand_name, " +
                      "SUM(rsuii.quantity_used) as total_quantity_used, " +
                      "rsuii.unit_cost, " +
                      "SUM(rsuii.total_cost) as total_cost_used, " +
                      "COUNT(DISTINCT rsui.raw_stock_use_invoice_id) as usage_count " +
                      "FROM Raw_Stock_Use_Invoice_Item rsuii " +
                      "JOIN Raw_Stock_Use_Invoice rsui ON rsuii.raw_stock_use_invoice_id = rsui.raw_stock_use_invoice_id " +
                      "JOIN Raw_Stock rs ON rsuii.raw_stock_id = rs.stock_id " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "WHERE rsui.usage_date BETWEEN ? AND ? " +
                      "GROUP BY rs.stock_id, rs.item_name, b.brand_name, rsuii.unit_cost " +
                      "ORDER BY total_cost_used DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("item_name"),
                        rs.getString("brand_name"),
                        rs.getDouble("total_quantity_used"),
                        rs.getDouble("unit_cost"),
                        rs.getDouble("total_cost_used"),
                        rs.getInt("usage_count")
                    };
                    usageReport.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usageReport;
    }

    /**
     * Get usage summary statistics for a date range
     */
    public Object[] getUsageSummaryStatistics(String startDate, String endDate) {
        String query = "SELECT " +
                      "COUNT(DISTINCT rsui.raw_stock_use_invoice_id) as total_invoices, " +
                      "COUNT(DISTINCT rsuii.raw_stock_id) as unique_items_used, " +
                      "SUM(rsuii.quantity_used) as total_quantity_used, " +
                      "SUM(rsuii.total_cost) as total_cost " +
                      "FROM Raw_Stock_Use_Invoice rsui " +
                      "JOIN Raw_Stock_Use_Invoice_Item rsuii ON rsui.raw_stock_use_invoice_id = rsuii.raw_stock_use_invoice_id " +
                      "WHERE rsui.usage_date BETWEEN ? AND ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                        rs.getInt("total_invoices"),
                        rs.getInt("unique_items_used"),
                        rs.getDouble("total_quantity_used"),
                        rs.getDouble("total_cost")
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[] {0, 0, 0.0, 0.0};
    }

    /**
     * Get usage details for a specific date range (invoices with items)
     */
    public List<Object[]> getRawStockUsageDetails(String startDate, String endDate) {
        List<Object[]> usageDetails = new ArrayList<>();
        String query = "SELECT rsui.use_invoice_number, rsui.usage_date, rsui.reference_purpose, " +
                      "rs.item_name, b.brand_name, rsuii.quantity_used, rsuii.unit_cost, rsuii.total_cost " +
                      "FROM Raw_Stock_Use_Invoice rsui " +
                      "JOIN Raw_Stock_Use_Invoice_Item rsuii ON rsui.raw_stock_use_invoice_id = rsuii.raw_stock_use_invoice_id " +
                      "JOIN Raw_Stock rs ON rsuii.raw_stock_id = rs.stock_id " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "WHERE rsui.usage_date BETWEEN ? AND ? " +
                      "ORDER BY rsui.usage_date DESC, rsui.use_invoice_number";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("use_invoice_number"),
                        rs.getString("usage_date"),
                        rs.getString("reference_purpose"),
                        rs.getString("item_name"),
                        rs.getString("brand_name"),
                        rs.getDouble("quantity_used"),
                        rs.getDouble("unit_cost"),
                        rs.getDouble("total_cost")
                    };
                    usageDetails.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usageDetails;
    }

    // --------------------------
    // Production Stock Operations
    // --------------------------
    @Override
    public List<Object[]> getAllProductionStocks() {
        List<Object[]> productionStocks = new ArrayList<>();
        String query = "SELECT ps.production_id, ps.product_name, " +
                      "b.brand_name, ps.quantity, ps.unit_cost, ps.sale_price, ps.total_cost, ps.production_date " +
                      "FROM ProductionStock ps " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "ORDER BY ps.product_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_id"),        // 0
                    rs.getString("product_name"),      // 1
                    "", // Empty string for product_description (not available) // 2
                    rs.getString("brand_name"),        // 3
                    "", // Empty string for brand_description (not available) // 4
                    rs.getInt("quantity"),             // 5
                    rs.getDouble("unit_cost"),         // 6
                    rs.getDouble("sale_price"),        // 7
                    rs.getDouble("total_cost"),        // 8
                    rs.getString("production_date")    // 9
                };
                productionStocks.add(row);
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to get all production stocks: " + e.getMessage());
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
        String insertQuery = "INSERT INTO Production_Invoice_Item (production_invoice_id, " +
                           "production_id, quantity_produced) VALUES (?, ?, ?)";
        
        try {
            connection.setAutoCommit(false);
            
            // Insert production invoice items
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                for (Object[] item : productionItems) {
                    pstmt.setInt(1, productionInvoiceId);
                    pstmt.setInt(2, (Integer) item[0]); // production_id from ProductionStock table
                    pstmt.setDouble(3, (Double) item[1]); // quantity_produced
                    pstmt.addBatch();
                }
                
                int[] insertResults = pstmt.executeBatch();
                
                // Check if all items were inserted successfully
                for (int result : insertResults) {
                    if (result <= 0) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        return false;
                    }
                }
            }
            
            // Update production stock quantities (increase stock as items are being produced)
            String updateStockQuery = "UPDATE ProductionStock SET quantity = quantity + ? " +
                                    "WHERE production_id = ?";
            try (PreparedStatement updatePstmt = connection.prepareStatement(updateStockQuery)) {
                for (Object[] item : productionItems) {
                    updatePstmt.setDouble(1, (Double) item[1]); // quantity_produced (add to stock)
                    updatePstmt.setInt(2, (Integer) item[0]); // production_id
                    updatePstmt.addBatch();
                }
                updatePstmt.executeBatch();
            }
            
            connection.commit();
            connection.setAutoCommit(true);
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

    /**
     * Get current production stock quantity for validation
     */
    public double getCurrentProductionStockQuantity(int productionId) {
        String query = "SELECT quantity FROM ProductionStock WHERE production_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
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
        // For this method, we'll use salePrice as both cost and sale price
        // In a real implementation, you'd want separate parameters for unit_cost and sale_price
        double unitCost = salePrice * 0.8; // Assume cost is 80% of sale price
        
        String query = "INSERT INTO ProductionStock (product_name, brand_id, quantity, unit_cost, total_cost, sale_price) " +
                      "VALUES (?, (SELECT brand_id FROM Brand WHERE brand_name = ? LIMIT 1), ?, ?, ?, ?)";
        
        try {
            connection.setAutoCommit(false); // Start transaction
            
            // Ensure brand exists
            ensureBrandExists(brand, 1); // Default tehsil_id = 1
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            double totalCost = openingQty * unitCost;
            
            pstmt.setString(1, name);
            pstmt.setString(2, brand);
            pstmt.setInt(3, (int) openingQty);
            pstmt.setDouble(4, unitCost);
            pstmt.setDouble(5, totalCost);
            pstmt.setDouble(6, salePrice);
            
            int result = pstmt.executeUpdate();
            pstmt.close();
            
            connection.commit(); // Commit transaction
            connection.setAutoCommit(true); // Reset auto-commit
            
            return result > 0;
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback on error
                connection.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    // New overloaded method with separate unit cost and sale price parameters
    public boolean insertProductionStock(String name, String category, String brand, String unit, 
                                       double openingQty, double unitCost, double salePrice, double reorderLevel) {
        String query = "INSERT INTO ProductionStock (product_name, brand_id, quantity, unit_cost, total_cost, sale_price) " +
                      "VALUES (?, (SELECT brand_id FROM Brand WHERE brand_name = ? LIMIT 1), ?, ?, ?, ?)";
        
        try {
            connection.setAutoCommit(false); // Start transaction
            
            // Ensure brand exists
            ensureBrandExists(brand, 1); // Default tehsil_id = 1
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            double totalCost = openingQty * unitCost;
            
            pstmt.setString(1, name);
            pstmt.setString(2, brand);
            pstmt.setInt(3, (int) openingQty);
            pstmt.setDouble(4, unitCost);      // Use the actual unit cost passed
            pstmt.setDouble(5, totalCost);
            pstmt.setDouble(6, salePrice);     // Use the actual sale price passed
            
            int result = pstmt.executeUpdate();
            pstmt.close();
            
            connection.commit(); // Commit transaction
            connection.setAutoCommit(true); // Reset auto-commit
            
            System.out.println("DEBUG: Inserted ProductionStock - Name: " + name + 
                             ", Unit Cost: " + unitCost + ", Sale Price: " + salePrice);
            
            return result > 0;
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback on error
                connection.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            System.err.println("ERROR: Failed to insert production stock: " + e.getMessage());
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

    // --------------------------
    // Production Return Invoice Operations  
    // --------------------------
    
    /**
     * Generate auto-increment production return invoice number
     */
    public String generateProductionReturnInvoiceNumber() {
        String query = "SELECT COUNT(*) FROM Production_Return_Invoice";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("PRI-%04d", count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PRI-0001";
    }

    /**
     * Complete production return invoice transaction
     * This method handles the complete flow: insert return invoice, insert items, and update stock
     */
    public boolean processProductionReturnInvoice(int originalProductionInvoiceId, String returnDate,
                                                 String notes, List<Object[]> returnItems) {
        try {
            connection.setAutoCommit(false);
            
            // Calculate totals
            double totalQuantity = 0;
            double totalAmount = 0;
            for (Object[] item : returnItems) {
                totalQuantity += (Double) item[1]; // quantity_returned
                totalAmount += (Double) item[3]; // total_cost
            }
            
            // Generate return invoice number
            String returnInvoiceNumber = generateProductionReturnInvoiceNumber();
            
            // Insert return invoice
            int returnInvoiceId = insertProductionReturnInvoiceAndGetId(
                returnInvoiceNumber, originalProductionInvoiceId, returnDate, 
                totalQuantity, totalAmount, notes
            );
            
            if (returnInvoiceId == -1) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            // Insert return items
            boolean itemsInserted = insertProductionReturnInvoiceItems(returnInvoiceId, returnItems);
            
            if (!itemsInserted) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            connection.commit();
            connection.setAutoCommit(true);
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
    
    /**
     * Get all production invoices for dropdown selection
     */
    public List<Object[]> getAllProductionInvoicesForDropdown() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT pi.production_invoice_id, pi.production_date, pi.notes " +
                      "FROM Production_Invoice pi " +
                      "ORDER BY pi.production_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_invoice_id"),
                    rs.getString("production_date"),
                    rs.getString("notes")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }
    
    /**
     * Get production items from a specific production invoice
     */
    public List<Object[]> getProductionItemsByInvoiceId(int productionInvoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT pii.production_id, ps.product_name, b.brand_name, " +
                      "pii.quantity_produced, ps.unit_cost " +
                      "FROM Production_Invoice_Item pii " +
                      "JOIN ProductionStock ps ON pii.production_id = ps.production_id " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE pii.production_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productionInvoiceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_id"),
                    rs.getString("product_name"),
                    rs.getString("brand_name"),
                    rs.getDouble("quantity_produced"),
                    rs.getDouble("unit_cost")
                };
                items.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    /**
     * Insert production return invoice and return the generated ID
     */
    public int insertProductionReturnInvoiceAndGetId(String returnInvoiceNumber, int originalProductionInvoiceId,
                                                    String returnDate, double totalReturnQuantity, 
                                                    double totalReturnAmount, String notes) {
        String query = "INSERT INTO Production_Return_Invoice " +
                      "(return_invoice_number, original_production_invoice_id, return_date, " +
                      "total_return_quantity, total_return_amount, notes) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, returnInvoiceNumber);
            pstmt.setInt(2, originalProductionInvoiceId);
            pstmt.setString(3, returnDate);
            pstmt.setDouble(4, totalReturnQuantity);
            pstmt.setDouble(5, totalReturnAmount);
            pstmt.setString(6, notes);
            
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
     * Insert production return invoice items
     */
    public boolean insertProductionReturnInvoiceItems(int returnInvoiceId, 
                                                     List<Object[]> returnItems) {
        String query = "INSERT INTO Production_Return_Invoice_Item " +
                      "(production_return_invoice_id, production_id, quantity_returned, unit_cost, total_cost) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            
            for (Object[] item : returnItems) {
                pstmt.setInt(1, returnInvoiceId);
                pstmt.setInt(2, (Integer) item[0]); // production_id
                pstmt.setDouble(3, (Double) item[1]); // quantity_returned
                pstmt.setDouble(4, (Double) item[2]); // unit_cost
                pstmt.setDouble(5, (Double) item[3]); // total_cost
                pstmt.addBatch();
                
                // Update production stock quantity
                updateProductionStockAfterReturn((Integer) item[0], (Double) item[1]);
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
    
    /**
     * Get production stock price by ID
     */
    private double getProductionStockPrice(int productionStockId) {
        String query = "SELECT unit_cost FROM ProductionStock WHERE production_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productionStockId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("unit_cost");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    /**
     * Get available items for return from a specific production invoice
     */
    public List<Object[]> getAvailableItemsForReturn(int productionInvoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT ps.production_id, ps.product_name, b.brand_name, " +
                      "ps.quantity, ps.unit_cost " +
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
                    rs.getString("brand_name"),
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_cost")
                };
                items.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    /**
     * Get all production return invoices
     */
    public List<Object[]> getAllProductionReturnInvoices() {
        List<Object[]> returnInvoices = new ArrayList<>();
        String query = "SELECT pri.production_return_invoice_id, pri.return_invoice_number, pri.return_date, " +
                      "pri.total_return_quantity, pri.total_return_amount, pri.notes, " +
                      "pi.production_date as original_production_date " +
                      "FROM Production_Return_Invoice pri " +
                      "JOIN Production_Invoice pi ON pri.original_production_invoice_id = pi.production_invoice_id " +
                      "ORDER BY pri.return_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_return_invoice_id"),
                    rs.getString("return_invoice_number"),
                    rs.getString("return_date"),
                    rs.getDouble("total_return_quantity"),
                    rs.getDouble("total_return_amount"),
                    rs.getString("notes"),
                    rs.getString("original_production_date")
                };
                returnInvoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnInvoices;
    }

    /**
     * Update production stock quantity after return
     */
    public boolean updateProductionStockAfterReturn(int productionId, double returnedQuantity) {
        String query = "UPDATE ProductionStock SET quantity = quantity - ? WHERE production_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, returnedQuantity);
            pstmt.setInt(2, productionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to decrease production stock when items are sold
    public boolean decreaseProductionStock(int productionId, double soldQuantity) {
        String query = "UPDATE ProductionStock SET quantity = quantity - ? WHERE production_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, soldQuantity);
            pstmt.setInt(2, productionId);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("DEBUG: Decreased stock for production_id " + productionId + " by " + soldQuantity + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to decrease production stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Check if production stock with same name and brand already exists
    public boolean productionStockExists(String productName, String brandName) {
        String query = "SELECT COUNT(*) FROM ProductionStock ps " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE ps.product_name = ? AND b.brand_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, productName);
            pstmt.setString(2, brandName);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to check production stock existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Add quantity to existing production stock
    public boolean addToProductionStock(String productName, String brandName, int addedQuantity, double unitCost, double salePrice) {
        String query = "UPDATE ProductionStock " +
                      "SET quantity = quantity + ?, unit_cost = ?, sale_price = ? " +
                      "WHERE product_name = ? AND brand_id = (SELECT brand_id FROM Brand WHERE brand_name = ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, addedQuantity);
            pstmt.setDouble(2, unitCost);
            pstmt.setDouble(3, salePrice);
            pstmt.setString(4, productName);
            pstmt.setString(5, brandName);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("DEBUG: Added " + addedQuantity + " to existing stock for " + productName + " (" + brandName + "), rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to add to production stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get production return invoice details by ID
     */
    public Object[] getProductionReturnInvoiceById(int returnInvoiceId) {
        String query = "SELECT pri.production_return_invoice_id, pri.return_invoice_number, pri.return_date, " +
                      "pri.total_return_quantity, pri.total_return_amount, pri.notes, " +
                      "pi.production_date as original_production_date " +
                      "FROM Production_Return_Invoice pri " +
                      "JOIN Production_Invoice pi ON pri.original_production_invoice_id = pi.production_invoice_id " +
                      "WHERE pri.production_return_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, returnInvoiceId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Object[] {
                    rs.getInt("production_return_invoice_id"),
                    rs.getString("return_invoice_number"),
                    rs.getString("return_date"),
                    rs.getDouble("total_return_quantity"),
                    rs.getDouble("total_return_amount"),
                    rs.getString("notes"),
                    rs.getString("original_production_date")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get production return invoice items by return invoice ID
     */
    public List<Object[]> getProductionReturnInvoiceItems(int returnInvoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT prii.production_return_invoice_item_id, prii.production_id, " +
                      "ps.product_name, b.brand_name, prii.quantity_returned, " +
                      "prii.unit_cost, prii.total_cost " +
                      "FROM Production_Return_Invoice_Item prii " +
                      "JOIN ProductionStock ps ON prii.production_id = ps.production_id " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE prii.production_return_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, returnInvoiceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_return_invoice_item_id"),
                    rs.getInt("production_id"),
                    rs.getString("product_name"),
                    rs.getString("brand_name"),
                    rs.getDouble("quantity_returned"),
                    rs.getDouble("unit_cost"),
                    rs.getDouble("total_cost")
                };
                items.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
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
    // Sales Invoice Operations
    // --------------------------
    @Override
    public String generateSalesInvoiceNumber() {
        String query = "SELECT MAX(CAST(SUBSTR(sales_invoice_number, 5) AS INTEGER)) as max_number " +
                      "FROM Sales_Invoice WHERE sales_invoice_number LIKE 'SI-%'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                int maxNumber = rs.getInt("max_number");
                return String.format("SI-%03d", maxNumber + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "SI-001";
    }

    @Override
    public List<Object[]> getAllCustomersForDropdown() {
        List<Object[]> customers = new ArrayList<>();
        String query = "SELECT customer_id, customer_name FROM Customer ORDER BY customer_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("customer_id"),
                    rs.getString("customer_name")
                };
                customers.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    @Override
    public int getCustomerIdByName(String customerName) {
        String query = "SELECT customer_id FROM Customer WHERE customer_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, customerName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Object[]> getAllProductionStocksWithPriceForDropdown() {
        List<Object[]> products = new ArrayList<>();
        String query = "SELECT ps.production_id, ps.product_name, " +
                      "CASE WHEN ps.sale_price > 0 THEN ps.sale_price ELSE ps.unit_cost * 1.2 END as sale_price, " +
                      "ps.quantity, 'N/A' as category_name, b.brand_name, 'N/A' as unit_name " +
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
                    rs.getDouble("sale_price"),
                    rs.getDouble("quantity"),
                    rs.getString("category_name"),
                    rs.getString("brand_name"),
                    rs.getString("unit_name")
                };
                products.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public int getProductionStockIdByName(String productName) {
        String query = "SELECT production_id FROM ProductionStock WHERE product_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, productName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("production_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int insertSalesInvoiceAndGetId(String invoiceNumber, int customerId, String salesDate, 
                                         double totalAmount, double discountAmount, double paidAmount) {
        String query = "INSERT INTO Sales_Invoice (sales_invoice_number, customer_id, sales_date, " +
                      "total_amount, discount_amount, paid_amount) VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, invoiceNumber);
            pstmt.setInt(2, customerId);
            pstmt.setString(3, salesDate);
            pstmt.setDouble(4, totalAmount);
            pstmt.setDouble(5, discountAmount);
            pstmt.setDouble(6, paidAmount);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    generatedKeys.close();
                    pstmt.close();
                    return generatedId;
                }
                generatedKeys.close();
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error inserting sales invoice: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean insertSalesInvoiceItems(int salesInvoiceId, List<Object[]> items) {
        String query = "INSERT INTO Sales_Invoice_Item (sales_invoice_id, production_stock_id, quantity, unit_price, total_price) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            for (Object[] item : items) {
                int productionStockId = (Integer) item[0];
                double quantity = (Double) item[1];
                double unitPrice = (Double) item[2];
                
                pstmt.setInt(1, salesInvoiceId);
                pstmt.setInt(2, productionStockId);
                pstmt.setDouble(3, quantity);
                pstmt.setDouble(4, unitPrice);
                pstmt.setDouble(5, quantity * unitPrice); // total_price = quantity * unit_price
                pstmt.addBatch();
                
                // Decrease production stock for each sold item
                if (!decreaseProductionStock(productionStockId, quantity)) {
                    System.err.println("WARNING: Failed to decrease production stock for product ID: " + productionStockId);
                    pstmt.close();
                    return false;
                }
            }
            
            int[] results = pstmt.executeBatch();
            pstmt.close();
            
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            System.out.println("DEBUG: Successfully inserted sales invoice items and updated stock levels");
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting sales invoice items: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertSalesInvoice(String invoiceNumber, int customerId, String salesDate, 
                                     double totalAmount, double discountAmount, double paidAmount, 
                                     List<Object[]> items) {
        try {
            // Set a timeout for database operations to prevent indefinite locks
            connection.setAutoCommit(false);
            
            System.out.println("DEBUG: Starting sales invoice transaction...");
            
            int salesInvoiceId = insertSalesInvoiceAndGetId(invoiceNumber, customerId, salesDate, 
                                                           totalAmount, discountAmount, paidAmount);
            
            if (salesInvoiceId > 0) {
                System.out.println("DEBUG: Sales invoice created with ID: " + salesInvoiceId);
                
                if (insertSalesInvoiceItems(salesInvoiceId, items)) {
                    System.out.println("DEBUG: Sales invoice items inserted successfully");
                    connection.commit();
                    System.out.println("DEBUG: Transaction committed successfully");
                    return true;
                } else {
                    System.out.println("DEBUG: Failed to insert sales invoice items, rolling back");
                    connection.rollback();
                    return false;
                }
            } else {
                System.out.println("DEBUG: Failed to create sales invoice, rolling back");
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in insertSalesInvoice: " + e.getMessage());
            try {
                connection.rollback();
                System.out.println("DEBUG: Transaction rolled back due to exception");
            } catch (SQLException rollbackEx) {
                System.err.println("DEBUG: Failed to rollback transaction: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
                System.out.println("DEBUG: AutoCommit restored to true");
            } catch (SQLException e) {
                System.err.println("DEBUG: Failed to restore AutoCommit: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // --------------------------
    // Sales Return Invoice Operations
    // --------------------------
    @Override
    public String generateSalesReturnInvoiceNumber() {
        String query = "SELECT MAX(CAST(SUBSTR(return_invoice_number, 5) AS INTEGER)) as max_number " +
                      "FROM Sales_Return_Invoice WHERE return_invoice_number LIKE 'SRI-%'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                int maxNumber = rs.getInt("max_number");
                return String.format("SRI-%03d", maxNumber + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "SRI-001";
    }

    @Override
    public List<Object[]> getAllSalesInvoicesForDropdown() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT si.sales_invoice_id, si.sales_invoice_number, c.customer_name, si.sales_date " +
                      "FROM Sales_Invoice si " +
                      "JOIN Customer c ON si.customer_id = c.customer_id " +
                      "ORDER BY si.sales_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("sales_invoice_id"),
                    rs.getString("sales_invoice_number"),
                    rs.getString("customer_name"),
                    rs.getString("sales_date")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    @Override
    public List<Object[]> getSalesInvoiceItemsByInvoiceId(int salesInvoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT sii.production_stock_id, ps.product_name, sii.quantity, sii.unit_price " +
                      "FROM Sales_Invoice_Item sii " +
                      "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                      "WHERE sii.sales_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, salesInvoiceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("production_stock_id"),
                        rs.getString("product_name"),
                        rs.getDouble("quantity"),
                        rs.getDouble("unit_price")
                    };
                    items.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public Object[] getSalesInvoiceById(int salesInvoiceId) {
        String query = "SELECT si.sales_invoice_id, si.sales_invoice_number, si.customer_id, c.customer_name, " +
                      "si.sales_date, si.total_amount, si.discount_amount, si.paid_amount " +
                      "FROM Sales_Invoice si " +
                      "JOIN Customer c ON si.customer_id = c.customer_id " +
                      "WHERE si.sales_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, salesInvoiceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                        rs.getInt("sales_invoice_id"),
                        rs.getString("sales_invoice_number"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("sales_date"),
                        rs.getDouble("total_amount"),
                        rs.getDouble("discount_amount"),
                        rs.getDouble("paid_amount")
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insertSalesReturnInvoiceAndGetId(String returnInvoiceNumber, int originalSalesInvoiceId, 
                                               int customerId, String returnDate, double totalReturnAmount) {
        String query = "INSERT INTO Sales_Return_Invoice (return_invoice_number, original_sales_invoice_id, " +
                      "customer_id, return_date, total_return_amount) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, returnInvoiceNumber);
            pstmt.setInt(2, originalSalesInvoiceId);
            pstmt.setInt(3, customerId);
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

    @Override
    public boolean insertSalesReturnInvoiceItems(int salesReturnInvoiceId, List<Object[]> items) {
        String insertQuery = "INSERT INTO Sales_Return_Invoice_Item (sales_return_invoice_id, production_stock_id, quantity, unit_price, total_price) " +
                           "VALUES (?, ?, ?, ?, ?)";
        
        try {
            // Insert sales return invoice items
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                for (Object[] item : items) {
                    pstmt.setInt(1, salesReturnInvoiceId);
                    pstmt.setInt(2, (Integer) item[0]); // production_stock_id
                    pstmt.setDouble(3, (Double) item[1]); // quantity
                    pstmt.setDouble(4, (Double) item[2]); // unit_price
                    pstmt.setDouble(5, (Double) item[1] * (Double) item[2]); // total_price = quantity * unit_price
                    pstmt.addBatch();
                }
                
                int[] insertResults = pstmt.executeBatch();
                
                // Check if all items were inserted successfully
                for (int result : insertResults) {
                    if (result <= 0) {
                        return false;
                    }
                }
            }
            
            // Update production stock quantities (increase stock as items are being returned)
            String updateStockQuery = "UPDATE ProductionStock SET quantity = quantity + ? " +
                                    "WHERE production_id = ?";
            try (PreparedStatement updatePstmt = connection.prepareStatement(updateStockQuery)) {
                for (Object[] item : items) {
                    updatePstmt.setDouble(1, (Double) item[1]); // quantity (add to stock)
                    updatePstmt.setInt(2, (Integer) item[0]); // production_stock_id
                    updatePstmt.addBatch();
                }
                updatePstmt.executeBatch();
            }
            
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertSalesReturnInvoice(String returnInvoiceNumber, int originalSalesInvoiceId, 
                                           int customerId, String returnDate, double totalReturnAmount, 
                                           List<Object[]> items) {
        try {
            connection.setAutoCommit(false);
            
            int salesReturnInvoiceId = insertSalesReturnInvoiceAndGetId(returnInvoiceNumber, originalSalesInvoiceId, 
                                                                       customerId, returnDate, totalReturnAmount);
            
            if (salesReturnInvoiceId > 0 && insertSalesReturnInvoiceItems(salesReturnInvoiceId, items)) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Object[]> getAllSalesReturnInvoices() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT sri.return_invoice_number, sri.return_date, c.customer_name, " +
                      "sri.total_return_amount, si.sales_invoice_number " +
                      "FROM Sales_Return_Invoice sri " +
                      "JOIN Customer c ON sri.customer_id = c.customer_id " +
                      "JOIN Sales_Invoice si ON sri.original_sales_invoice_id = si.sales_invoice_id " +
                      "ORDER BY sri.return_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("return_invoice_number"),
                    rs.getString("return_date"),
                    rs.getString("customer_name"),
                    rs.getDouble("total_return_amount"),
                    rs.getString("sales_invoice_number")
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

        // Use correct column names for each table
        String cashQuery = "SELECT transaction_date AS date, transaction_type, amount, description, 'cash' AS source " +
                        "FROM Cash_Transaction";
        String bankQuery = "SELECT transaction_date AS date, transaction_type, amount, description, 'bank' AS source " +
                        "FROM Bank_Transaction";

        try {
            // Cash transactions
            try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(cashQuery)) {
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
            }

            // Bank transactions
            try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(bankQuery)) {
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
            }

            // Sort by date descending
            transactions.sort((a, b) -> {
                String dateA = (String) a[0];
                String dateB = (String) b[0];
                return dateB.compareTo(dateA);
            });

        } catch (SQLException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
            e.printStackTrace();
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
        dateColumnMap.put("View_Return_Purchase_Book", "invoice_date");
        dateColumnMap.put("View_Raw_Stock_Book", "invoice_date");
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
                System.out.println("DEBUG: ResultSet has " + columnCount + " columns");
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    System.out.println("DEBUG: Row created with " + row.length + " elements");
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

    @Override
    public List<Object[]> getInvoiceItemsByID(Integer invoiceID) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT rpii.raw_stock_id, rs.item_name, b.brand_name, rpii.quantity, rpii.unit_price " +
                    "FROM Raw_Purchase_Invoice_Item rpii " +
                    "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                    "JOIN Brand b ON rs.brand_id = b.brand_id " +
                    "JOIN Raw_Purchase_Invoice rpi ON rpii.raw_purchase_invoice_id = rpi.raw_purchase_invoice_id " +
                    "WHERE rpi.raw_purchase_invoice_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, invoiceID);
            System.out.println("Executing query: " + query + " with invoiceID: " + invoiceID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("raw_stock_id"),
                        rs.getString("item_name"),
                        rs.getString("brand_name"),
                        rs.getDouble("quantity"),
                        rs.getDouble("unit_price")
                    };
                    System.out.println("Fetched row: " + Arrays.toString(row));
                    items.add(row);
                }
                System.out.println("Total items fetched: " + items.size());
            }
        } catch (SQLException e) {
            System.err.println("SQL Error for invoiceID " + invoiceID + ": " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }  


    /////////////////////////////////////////////////////////////////////////////
    ///                       reports                                        ////
    //////////////////////////////////////////////////////////////////////////////

    public List<Object[]> getPurchaseReportList(Date fromDate, Date toDate) {
        List<Object[]> reports = new ArrayList<>();
        String query = "SELECT " +
                "rpi.invoice_number AS invoiceNumber, " +
                "rpi.invoice_date AS invoiceDate, " +
                "COALESCE(s.supplier_name, 'Unknown Supplier') AS supplierName, " +
                "rpi.total_amount AS totalAmount, " +
                "rpi.discount_amount AS discountAmount, " +
                "rpi.paid_amount AS paidAmount " +
                "FROM Raw_Purchase_Invoice rpi " +
                "LEFT JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                "WHERE rpi.invoice_date BETWEEN ? AND ? " +
                "ORDER BY rpi.invoice_date DESC";
                
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            // Convert Date to String format (YYYY-MM-DD) for SQLite comparison
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = sdf.format(fromDate);
            String toDateStr = sdf.format(toDate);
            
            // Debug logging
            System.out.println("DEBUG: getPurchaseReportList called with dates:");
            System.out.println("DEBUG: fromDate: " + fromDate + " -> " + fromDateStr);
            System.out.println("DEBUG: toDate: " + toDate + " -> " + toDateStr);
            System.out.println("DEBUG: Query: " + query);
            
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("invoiceNumber"),
                        rs.getString("invoiceDate"),
                        rs.getString("supplierName"),
                        rs.getDouble("totalAmount"),
                        rs.getDouble("discountAmount"),
                        rs.getDouble("paidAmount")
                    };
                    reports.add(row);
                }
                System.out.println("DEBUG: Loaded " + reports.size() + " purchase report rows");
            }
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getPurchaseReportList: " + e.getMessage());
            e.printStackTrace();
        }
        return reports;
    }

    @Override
    public ResultSet getPurchaseReport(Date fromDate, Date toDate) {
        String query = "SELECT " +
                "rpi.invoice_number AS invoiceNumber, " +
                "rpi.invoice_date AS invoiceDate, " +
                "COALESCE(s.supplier_name, 'Unknown Supplier') AS supplierName, " +
                "rpi.total_amount AS totalAmount, " +
                "rpi.discount_amount AS discountAmount, " +
                "rpi.paid_amount AS paidAmount " +
                "FROM Raw_Purchase_Invoice rpi " +
                "LEFT JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                "WHERE rpi.invoice_date BETWEEN ? AND ? " +
                "ORDER BY rpi.invoice_date DESC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            // Convert Date to String format (YYYY-MM-DD) for SQLite comparison
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = sdf.format(fromDate);
            String toDateStr = sdf.format(toDate);
            
            // Debug logging
            System.out.println("DEBUG: getPurchaseReport called with dates:");
            System.out.println("DEBUG: fromDate: " + fromDate + " -> " + fromDateStr);
            System.out.println("DEBUG: toDate: " + toDate + " -> " + toDateStr);
            System.out.println("DEBUG: Query: " + query);
            
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            ResultSet rs = pstmt.executeQuery();
            
            // Debug: Just log that we're returning the ResultSet
            System.out.println("DEBUG: Returning ResultSet from getPurchaseReport");
            
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getPurchaseReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getSalesReport(Date fromDate, Date toDate) {
        String query = "SELECT " +
                "si.sales_invoice_number AS sales_invoice_number, " +
                "si.sales_date AS sales_date, " +
                "COALESCE(c.customer_name, 'Unknown Customer') AS customer_name, " +
                "si.total_amount AS total_amount, " +
                "si.discount_amount AS discount_amount, " +
                "si.paid_amount AS paid_amount " +
                "FROM Sales_Invoice si " +
                "LEFT JOIN Customer c ON si.customer_id = c.customer_id " +
                "WHERE si.sales_date BETWEEN ? AND ? " +
                "ORDER BY si.sales_date DESC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            // Convert Date to String format (YYYY-MM-DD) for SQLite comparison
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = sdf.format(fromDate);
            String toDateStr = sdf.format(toDate);
            
            // Debug logging
            System.out.println("DEBUG: getSalesReport called with dates:");
            System.out.println("DEBUG: fromDate: " + fromDate + " -> " + fromDateStr);
            System.out.println("DEBUG: toDate: " + toDate + " -> " + toDateStr);
            System.out.println("DEBUG: Query: " + query);
            
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            ResultSet rs = pstmt.executeQuery();
            
            // Debug: Just log that we're returning the ResultSet
            System.out.println("DEBUG: Returning ResultSet from getSalesReport");
            
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getSalesReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getReturnPurchaseReport(Date fromDate, Date toDate) {
        String query = "SELECT " +
                "rpri.return_invoice_number AS invoiceNumber, " +
                "rpri.return_date AS invoiceDate, " +
                "COALESCE(s.supplier_name, 'Unknown Supplier') AS supplierName, " +
                "rpri.total_return_amount AS totalAmount, " +
                "0.00 AS discountAmount, " +
                "rpri.total_return_amount AS paidAmount " +
                "FROM Raw_Purchase_Return_Invoice rpri " +
                "LEFT JOIN Supplier s ON rpri.supplier_id = s.supplier_id " +
                "WHERE rpri.return_date BETWEEN ? AND ? " +
                "ORDER BY rpri.return_date DESC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            // Convert Date to String format (YYYY-MM-DD) for SQLite comparison
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = sdf.format(fromDate);
            String toDateStr = sdf.format(toDate);
            
            // Debug logging
            System.out.println("DEBUG: getReturnPurchaseReport called with dates:");
            System.out.println("DEBUG: fromDate: " + fromDate + " -> " + fromDateStr);
            System.out.println("DEBUG: toDate: " + toDate + " -> " + toDateStr);
            System.out.println("DEBUG: Query: " + query);
            
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            ResultSet rs = pstmt.executeQuery();
            
            // Debug: Just log that we're returning the ResultSet
            System.out.println("DEBUG: Returning ResultSet from getReturnPurchaseReport");
            
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getReturnPurchaseReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getReturnSalesReport(Date fromDate, Date toDate) {
        System.out.println("DEBUG: getReturnSalesReport called with dates: " + fromDate + " to " + toDate);
        
        // Convert Date to string format for comparison
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = sdf.format(fromDate);
        String toDateStr = sdf.format(toDate);
        
        System.out.println("DEBUG: Date strings: " + fromDateStr + " to " + toDateStr);
        
        String query = "SELECT " +
                "sri.return_invoice_number AS return_invoice_number, " +
                "sri.return_date AS return_date, " +
                "c.customer_name AS customer_name, " +
                "sri.total_return_amount AS total_return_amount " +
                "FROM Sales_Return_Invoice sri " +
                "LEFT JOIN Customer c ON sri.customer_id = c.customer_id " +
                "WHERE sri.return_date >= ? AND sri.return_date <= ? " +
                "ORDER BY sri.return_date DESC";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            System.out.println("DEBUG: Executing return sales report query: " + query);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getReturnSalesReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getBankTransferReport(Date fromDate, Date toDate) {
        System.out.println("DEBUG: getBankTransferReport called with dates: " + fromDate + " to " + toDate);
        
        // Convert Date to String format (YYYY-MM-DD) for SQLite comparison
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = sdf.format(fromDate);
        String toDateStr = sdf.format(toDate);
        
        System.out.println("DEBUG: Date strings: " + fromDateStr + " to " + toDateStr);
        
        // First, let's check all bank transactions to see what's available
        String debugQuery = "SELECT transaction_type, COUNT(*) as count FROM Bank_Transaction GROUP BY transaction_type";
        try (PreparedStatement debugStmt = connection.prepareStatement(debugQuery)) {
            ResultSet debugRs = debugStmt.executeQuery();
            System.out.println("DEBUG: Available transaction types in Bank_Transaction:");
            while (debugRs.next()) {
                System.out.println("  - " + debugRs.getString("transaction_type") + ": " + debugRs.getInt("count") + " records");
            }
            debugRs.close();
        } catch (SQLException e) {
            System.err.println("DEBUG: Error checking transaction types: " + e.getMessage());
        }
        
        // Check transactions in date range
        String dateCheckQuery = "SELECT COUNT(*) as count FROM Bank_Transaction WHERE transaction_date >= ? AND transaction_date <= ?";
        try (PreparedStatement dateStmt = connection.prepareStatement(dateCheckQuery)) {
            dateStmt.setString(1, fromDateStr);
            dateStmt.setString(2, toDateStr);
            ResultSet dateRs = dateStmt.executeQuery();
            if (dateRs.next()) {
                System.out.println("DEBUG: Transactions in date range " + fromDateStr + " to " + toDateStr + ": " + dateRs.getInt("count"));
            }
            dateRs.close();
        } catch (SQLException e) {
            System.err.println("DEBUG: Error checking date range: " + e.getMessage());
        }
        
        String query = "SELECT " +
                "bt.transaction_date, " +
                "COALESCE(" +
                "  CASE WHEN bt.bank_id != 0 THEN (SELECT bank_name FROM Bank WHERE bank_id = bt.bank_id) ELSE 'Cash' END," +
                "  'Unknown'" +
                ") as from_bank, " +
                "COALESCE(" +
                "  CASE WHEN bt.related_bank_id != 0 THEN (SELECT bank_name FROM Bank WHERE bank_id = bt.related_bank_id) ELSE 'Cash' END," +
                "  'Cash'" +
                ") as to_bank, " +
                "bt.amount, " +
                "bt.transaction_type, " +
                "COALESCE(bt.description, 'Bank Transfer') as description " +
                "FROM Bank_Transaction bt " +
                "WHERE bt.transaction_type IN ('transfer_in', 'transfer_out') " +
                "AND bt.transaction_date >= ? AND bt.transaction_date <= ? " +
                "ORDER BY bt.transaction_date DESC";
                
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            System.out.println("DEBUG: Executing bank transfer query: " + query);
            System.out.println("DEBUG: Parameters: " + fromDateStr + ", " + toDateStr);
            
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getBankTransferReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getProfitReport(Date fromDate, Date toDate) {
        System.out.println("DEBUG: Profit Report - Getting data from " + fromDate + " to " + toDate);
        
        // First, let's check table counts for debugging
        try {
            Statement stmt = connection.createStatement();
            ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) as count FROM Sales_Invoice");
            if (countRs.next()) {
                System.out.println("DEBUG: Total Sales_Invoice records: " + countRs.getInt("count"));
            }
            
            countRs = stmt.executeQuery("SELECT COUNT(*) as count FROM Sales_Invoice_Item");
            if (countRs.next()) {
                System.out.println("DEBUG: Total Sales_Invoice_Item records: " + countRs.getInt("count"));
            }
            
            countRs = stmt.executeQuery("SELECT COUNT(*) as count FROM ProductionStock");
            if (countRs.next()) {
                System.out.println("DEBUG: Total ProductionStock records: " + countRs.getInt("count"));
            }
        } catch (SQLException e) {
            System.out.println("DEBUG: Error getting table counts: " + e.getMessage());
        }
        
        String query = "SELECT " +
                      "si.sales_invoice_number, " +
                      "si.sales_date, " +
                      "SUM(sii.quantity * CASE WHEN ps.sale_price > 0 THEN ps.sale_price ELSE sii.unit_price END) as sale_amount, " +
                      "SUM(sii.quantity * ps.unit_cost) as cost_amount, " +
                      "(SUM(sii.quantity * CASE WHEN ps.sale_price > 0 THEN ps.sale_price ELSE sii.unit_price END) - SUM(sii.quantity * ps.unit_cost)) as profit " +
                      "FROM Sales_Invoice si " +
                      "JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id " +
                      "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                      "WHERE si.sales_date BETWEEN ? AND ? " +
                      "GROUP BY si.sales_invoice_number, si.sales_date " +
                      "ORDER BY si.sales_date DESC";
        
        System.out.println("DEBUG: Executing profit query: " + query);
        System.out.println("DEBUG: Date range: " + fromDate + " to " + toDate);
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, fromDate.toString());
            pstmt.setString(2, toDate.toString());
            
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Profit query executed successfully");
            
            // Test query without date filter to see if we get any results
            try {
                Statement testStmt = connection.createStatement();
                ResultSet testRs = testStmt.executeQuery(
                    "SELECT si.sales_invoice_number, si.sales_date, " +
                    "SUM(sii.quantity * CASE WHEN ps.sale_price > 0 THEN ps.sale_price ELSE sii.unit_price END) as sale_amount, " +
                    "SUM(sii.quantity * ps.unit_cost) as cost_amount " +
                    "FROM Sales_Invoice si " +
                    "JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id " +
                    "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                    "GROUP BY si.sales_invoice_number, si.sales_date LIMIT 3"
                );
                int testCount = 0;
                while (testRs.next()) {
                    System.out.println("DEBUG: Test result - Invoice: " + testRs.getString("sales_invoice_number") + 
                                     ", Date: " + testRs.getString("sales_date") + 
                                     ", Sale: " + testRs.getDouble("sale_amount") + 
                                     ", Cost: " + testRs.getDouble("cost_amount"));
                    testCount++;
                }
                System.out.println("DEBUG: Test query returned " + testCount + " results");
            } catch (SQLException e) {
                System.out.println("DEBUG: Test query failed: " + e.getMessage());
            }
            
            return rs;
        } catch (SQLException e) {
            System.out.println("DEBUG: Error executing profit query: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getSummaryReport(Date fromDate, Date toDate) {
        System.out.println("DEBUG: Summary Report - Getting data from " + fromDate + " to " + toDate);
        
        // First, let's check what data exists in each table
        try {
            System.out.println("DEBUG: Checking table contents...");
            
            // Check Raw_Purchase_Invoice table
            String checkPurchases = "SELECT COUNT(*) as count, MIN(invoice_date) as min_date, MAX(invoice_date) as max_date, SUM(total_amount) as total FROM Raw_Purchase_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkPurchases)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Raw_Purchase_Invoice - Count: " + rs.getInt("count") + 
                                     ", Date range: " + rs.getString("min_date") + " to " + rs.getString("max_date") + 
                                     ", Total amount: " + rs.getDouble("total"));
                }
            }
            
            // Check Sales_Invoice table
            String checkSales = "SELECT COUNT(*) as count, MIN(sales_date) as min_date, MAX(sales_date) as max_date, SUM(total_amount) as total FROM Sales_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSales)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Sales_Invoice - Count: " + rs.getInt("count") + 
                                     ", Date range: " + rs.getString("min_date") + " to " + rs.getString("max_date") + 
                                     ", Total amount: " + rs.getDouble("total"));
                }
            }
            
            // Check Bank table
            String checkBank = "SELECT COUNT(*) as count, SUM(balance) as total FROM Bank";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkBank)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Bank - Count: " + rs.getInt("count") + ", Total balance: " + rs.getDouble("total"));
                }
            }
            
            // Check ProductionStock table
            String checkStock = "SELECT COUNT(*) as count, SUM(quantity * unit_cost) as total_value FROM ProductionStock";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkStock)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: ProductionStock - Count: " + rs.getInt("count") + ", Total value: " + rs.getDouble("total_value"));
                }
            }
            
            // Check return tables
            String checkPurchaseReturns = "SELECT COUNT(*) as count, SUM(total_return_amount) as total FROM Raw_Purchase_Return_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkPurchaseReturns)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Raw_Purchase_Return_Invoice - Count: " + rs.getInt("count") + ", Total: " + rs.getDouble("total"));
                }
            }
            
            String checkSalesReturns = "SELECT COUNT(*) as count, SUM(total_return_amount) as total FROM Sales_Return_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSalesReturns)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Sales_Return_Invoice - Count: " + rs.getInt("count") + ", Total: " + rs.getDouble("total"));
                }
            }
            
        } catch (SQLException e) {
            System.out.println("DEBUG: Error checking table contents: " + e.getMessage());
        }
        
        String query = "SELECT " +
                    "(SELECT COALESCE(SUM(total_amount), 0) FROM Raw_Purchase_Invoice WHERE invoice_date BETWEEN ? AND ?) AS total_purchases, " +
                    "(SELECT COALESCE(SUM(total_amount), 0) FROM Sales_Invoice WHERE sales_date BETWEEN ? AND ?) AS total_sales, " +
                    "(SELECT COALESCE(SUM(total_return_amount), 0) FROM Raw_Purchase_Return_Invoice WHERE return_date BETWEEN ? AND ?) AS total_purchase_returns, " +
                    "(SELECT COALESCE(SUM(total_return_amount), 0) FROM Sales_Return_Invoice WHERE return_date BETWEEN ? AND ?) AS total_sales_returns, " +
                    "(SELECT COALESCE(SUM(balance), 0) FROM Bank) AS total_bank_balance, " +
                    "(SELECT COUNT(*) FROM Customer) AS total_customers, " +
                    "(SELECT COUNT(*) FROM Supplier) AS total_suppliers, " +
                    "(SELECT COALESCE(SUM(quantity * unit_cost), 0) FROM ProductionStock) AS total_inventory_value";
        
        System.out.println("DEBUG: Executing summary query: " + query);
        
        try {
            // Don't use try-with-resources since we need to return the ResultSet
            PreparedStatement pstmt = connection.prepareStatement(query);
            
            // Debug the date parameters being used
            System.out.println("DEBUG: Date parameters:");
            System.out.println("  fromDate: " + fromDate + " (SQL Date: " + fromDate.toString() + ")");
            System.out.println("  toDate: " + toDate + " (SQL Date: " + toDate.toString() + ")");
            
            pstmt.setDate(1, fromDate);
            pstmt.setDate(2, toDate);
            pstmt.setDate(3, fromDate);
            pstmt.setDate(4, toDate);
            pstmt.setDate(5, fromDate);
            pstmt.setDate(6, toDate);
            pstmt.setDate(7, fromDate);
            pstmt.setDate(8, toDate);
            
            // Test individual subqueries to see which ones return data
            System.out.println("DEBUG: Testing individual subqueries...");
            
            // Test purchases in date range
            try (PreparedStatement testStmt = connection.prepareStatement(
                    "SELECT COALESCE(SUM(total_amount), 0) as result FROM Raw_Purchase_Invoice WHERE invoice_date BETWEEN ? AND ?")) {
                testStmt.setDate(1, fromDate);
                testStmt.setDate(2, toDate);
                ResultSet testRs = testStmt.executeQuery();
                if (testRs.next()) {
                    System.out.println("  Purchases in date range: " + testRs.getDouble("result"));
                }
            }
            
            // Test sales in date range
            try (PreparedStatement testStmt = connection.prepareStatement(
                    "SELECT COALESCE(SUM(total_amount), 0) as result FROM Sales_Invoice WHERE sales_date BETWEEN ? AND ?")) {
                testStmt.setDate(1, fromDate);
                testStmt.setDate(2, toDate);
                ResultSet testRs = testStmt.executeQuery();
                if (testRs.next()) {
                    System.out.println("  Sales in date range: " + testRs.getDouble("result"));
                }
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("DEBUG: Summary query executed successfully");
            
            return rs;
        } catch (SQLException e) {
            System.out.println("DEBUG: Error executing summary query: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getBalanceSheet() {
        String query = "SELECT bank_name, balance FROM Bank";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getCustomersReport() {
        String query = "SELECT * FROM Customer";
        System.out.println("DEBUG: Executing customers report query: " + query);
        
        try {
            // First check if the table exists and has data
            String countQuery = "SELECT COUNT(*) as total FROM Customer";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int totalCustomers = countRs.getInt("total");
                    System.out.println("DEBUG: Total customers in database: " + totalCustomers);
                }
                countRs.close();
            }
            
            // Now execute the main query
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Successfully executed customers report query");
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getCustomersReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getSuppliersReport() {
        String query = "SELECT * FROM Supplier";
        System.out.println("DEBUG: Executing suppliers report query: " + query);
        
        try {
            // First check if the table exists and has data
            String countQuery = "SELECT COUNT(*) as total FROM Supplier";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int totalSuppliers = countRs.getInt("total");
                    System.out.println("DEBUG: Total suppliers in database: " + totalSuppliers);
                }
                countRs.close();
            }
            
            // Now execute the main query
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Successfully executed suppliers report query");
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getSuppliersReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getBrandSalesReport(Date fromDate, Date toDate) {
        System.out.println("DEBUG: getBrandSalesReport called with dates: " + fromDate + " to " + toDate);
        
        // Convert Date to String format (YYYY-MM-DD) for SQLite comparison
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = sdf.format(fromDate);
        String toDateStr = sdf.format(toDate);
        
        System.out.println("DEBUG: Date strings: " + fromDateStr + " to " + toDateStr);
        
        // First, let's check what tables exist and have data
        try {
            // Check Sales_Invoice table
            String checkSalesQuery = "SELECT COUNT(*) as count FROM Sales_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSalesQuery)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Total Sales_Invoice records: " + rs.getInt("count"));
                }
                rs.close();
            }
            
            // Check Sales_Invoice_Item table
            String checkItemsQuery = "SELECT COUNT(*) as count FROM Sales_Invoice_Item";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkItemsQuery)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Total Sales_Invoice_Item records: " + rs.getInt("count"));
                }
                rs.close();
            }
            
            // Check ProductionStock table
            String checkProdQuery = "SELECT COUNT(*) as count FROM ProductionStock";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkProdQuery)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Total ProductionStock records: " + rs.getInt("count"));
                }
                rs.close();
            }
            
            // Check Brand table
            String checkBrandQuery = "SELECT COUNT(*) as count FROM Brand";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkBrandQuery)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Total Brand records: " + rs.getInt("count"));
                }
                rs.close();
            }
            
            // Check dates in Sales_Invoice
            String checkDatesQuery = "SELECT MIN(sales_date) as min_date, MAX(sales_date) as max_date FROM Sales_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkDatesQuery)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Sales_Invoice date range: " + rs.getString("min_date") + " to " + rs.getString("max_date"));
                }
                rs.close();
            }
            
            // Test the query first without parameters to see all data
            String testQuery = "SELECT b.brand_name, SUM(sii.quantity) AS total_quantity, SUM(sii.total_price) AS total_sales FROM Sales_Invoice si JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id JOIN Brand b ON ps.brand_id = b.brand_id GROUP BY b.brand_name";
            try (PreparedStatement testStmt = connection.prepareStatement(testQuery)) {
                ResultSet testRs = testStmt.executeQuery();
                System.out.println("DEBUG: All brand sales data (no date filter):");
                while (testRs.next()) {
                    System.out.println("  Brand: " + testRs.getString("brand_name") + 
                                     ", Quantity: " + testRs.getDouble("total_quantity") + 
                                     ", Sales: " + testRs.getDouble("total_sales"));
                }
                testRs.close();
            }
            
        } catch (SQLException e) {
            System.err.println("DEBUG: Error checking table data: " + e.getMessage());
        }
        
        String query = "SELECT " +
                      "b.brand_name, " +
                      "SUM(sii.quantity) AS total_quantity, " +
                      "SUM(sii.total_price) AS total_sales, " +
                      "'N/A' AS salesman_name " +
                      "FROM Sales_Invoice si " +
                      "JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id " +
                      "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE si.sales_date >= ? AND si.sales_date <= ? " +
                      "GROUP BY b.brand_name " +
                      "ORDER BY total_sales DESC";
        
        System.out.println("DEBUG: Executing brand sales query: " + query);
        System.out.println("DEBUG: Parameters: " + fromDateStr + ", " + toDateStr);
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Query executed successfully, returning ResultSet");
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getBrandSalesReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getBrandProfitReport(Date fromDate, Date toDate) {
        String query = "SELECT b.brand_name, SUM(s.quantity * s.unit_price - r.unit_cost) AS profit " +
                    "FROM Brand b JOIN Sales_Invoice_Item s ON b.brand_id = s.brand_id " +
                    "JOIN Raw_Stock r ON s.raw_stock_id = r.stock_id " +
                    "WHERE s.invoice_date BETWEEN ? AND ? GROUP BY b.brand_name";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDate(1, fromDate);
            pstmt.setDate(2, toDate);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getCustomerSalesReport(int customerId, Date fromDate, Date toDate) {
        String query = "SELECT * FROM Sales_Invoice WHERE customer_id = ? AND invoice_date BETWEEN ? AND ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            pstmt.setDate(2, fromDate);
            pstmt.setDate(3, toDate);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getSupplierSalesReport(int supplierId, Date fromDate, Date toDate) {
        String query = "SELECT * FROM Raw_Purchase_Invoice WHERE supplier_id = ? AND invoice_date BETWEEN ? AND ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, supplierId);
            pstmt.setDate(2, fromDate);
            pstmt.setDate(3, toDate);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getAttendanceReport(int employeeId, Date fromDate, Date toDate) {
        String query = "SELECT * FROM Employee_Attendance WHERE employee_id = ? AND attendance_date BETWEEN ? AND ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            pstmt.setDate(2, fromDate);
            pstmt.setDate(3, toDate);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getAreaWiseReport() {
        String query = "SELECT 'Customer' as party_type, c.customer_name as name, " +
                    "t.tehsil_name, d.district_name, p.province_name " +
                    "FROM Customer c " +
                    "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                    "LEFT JOIN District d ON t.district_id = d.district_id " +
                    "LEFT JOIN Province p ON d.province_id = p.province_id " +
                    "UNION ALL " +
                    "SELECT 'Supplier' as party_type, s.supplier_name as name, " +
                    "t.tehsil_name, d.district_name, p.province_name " +
                    "FROM Supplier s " +
                    "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                    "LEFT JOIN District d ON t.district_id = d.district_id " +
                    "LEFT JOIN Province p ON d.province_id = p.province_id " +
                    "ORDER BY party_type, name";
        
        System.out.println("DEBUG: Executing area-wise report query: " + query);
        
        try {
            // First check if the tables exist and have data
            String countCustomersQuery = "SELECT COUNT(*) as total FROM Customer";
            try (PreparedStatement countStmt = connection.prepareStatement(countCustomersQuery)) {
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int totalCustomers = countRs.getInt("total");
                    System.out.println("DEBUG: Total customers for area-wise report: " + totalCustomers);
                }
                countRs.close();
            }
            
            String countSuppliersQuery = "SELECT COUNT(*) as total FROM Supplier";
            try (PreparedStatement countStmt = connection.prepareStatement(countSuppliersQuery)) {
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int totalSuppliers = countRs.getInt("total");
                    System.out.println("DEBUG: Total suppliers for area-wise report: " + totalSuppliers);
                }
                countRs.close();
            }
            
            // Now execute the main query
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Successfully executed area-wise report query");
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getAreaWiseReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Ensure all required views exist in the database
     */
    /**
     * Ensure all required views exist in the database
     */
    public void ensureViewsExist() {
        try {
            // Drop and recreate View_Purchase_Book to fix column mismatch
            try {
                connection.createStatement().execute("DROP VIEW IF EXISTS View_Purchase_Book");
            } catch (SQLException e) {
                // Ignore if view doesn't exist
            }
            
            // Create detailed view with item-level data (14 columns as expected)
            String sql = "CREATE VIEW View_Purchase_Book AS " +
                       "SELECT " +
                       "    rpi.raw_purchase_invoice_id, " +           // 0: invoice_id
                       "    rpi.invoice_number, " +                    // 1: invoice_number
                       "    s.supplier_name, " +                      // 2: supplier_name
                       "    rpi.invoice_date, " +                     // 3: invoice_date
                       "    rs.item_name, " +                         // 4: item_name
                       "    b.brand_name, " +                         // 5: brand_name
                       "    m.manufacturer_name, " +                  // 6: manufacturer_name
                       "    rpii.quantity, " +                        // 7: quantity
                       "    rpii.unit_price, " +                      // 8: unit_price
                       "    (rpii.quantity * rpii.unit_price) AS item_total, " + // 9: item_total
                       "    rpi.total_amount, " +                     // 10: total_amount
                       "    rpi.discount_amount, " +                  // 11: discount_amount
                       "    rpi.paid_amount, " +                      // 12: paid_amount
                       "    (rpi.total_amount - rpi.paid_amount) AS balance " + // 13: balance
                       "FROM Raw_Purchase_Invoice rpi " +
                       "JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                       "JOIN Raw_Purchase_Invoice_Item rpii ON rpi.raw_purchase_invoice_id = rpii.raw_purchase_invoice_id " +
                       "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                       "JOIN Brand b ON rs.brand_id = b.brand_id " +
                       "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id";
            connection.createStatement().execute(sql);
            System.out.println("Created View_Purchase_Book with 14 columns (item-level details)");
            
            // Drop and recreate View_Return_Purchase_Book to fix column mismatch
            try {
                connection.createStatement().execute("DROP VIEW IF EXISTS View_Return_Purchase_Book");
            } catch (SQLException e) {
                // Ignore if view doesn't exist
            }
            
            sql = "CREATE VIEW View_Return_Purchase_Book AS " +
                "SELECT " +
                "    rpri.raw_purchase_return_invoice_id AS raw_purchase_invoice_id, " +     // 0: invoice_id
                "    rpri.return_invoice_number AS invoice_number, " +          // 1: invoice_number
                "    s.supplier_name, " +                                       // 2: supplier_name
                "    rpri.return_date AS invoice_date, " +                      // 3: invoice_date
                "    rs.item_name, " +                                          // 4: item_name
                "    b.brand_name, " +                                          // 5: brand_name
                "    m.manufacturer_name, " +                                   // 6: manufacturer_name
                "    rprii.quantity AS quantity, " +                            // 7: quantity
                "    rprii.unit_price, " +                                      // 8: unit_price
                "    (rprii.quantity * rprii.unit_price) AS item_total, " +     // 9: item_total
                "    rpri.total_return_amount AS total_amount, " +              // 10: total_amount
                "    0.0 AS discount_amount, " +                                // 11: discount_amount
                "    rpri.total_return_amount AS paid_amount, " +               // 12: paid_amount
                "    0.0 AS balance " +                                         // 13: balance
                "FROM Raw_Purchase_Return_Invoice rpri " +
                "JOIN Supplier s ON rpri.supplier_id = s.supplier_id " +
                "JOIN Raw_Purchase_Return_Invoice_Item rprii ON rpri.raw_purchase_return_invoice_id = rprii.raw_purchase_return_invoice_id " +
                "JOIN Raw_Stock rs ON rprii.raw_stock_id = rs.stock_id " +
                "JOIN Brand b ON rs.brand_id = b.brand_id " +
                "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id";
            connection.createStatement().execute(sql);
            System.out.println("Created View_Return_Purchase_Book with 14 columns (item-level details)");
            
            // Drop and recreate View_Raw_Stock_Book to fix column mismatch
            try {
                connection.createStatement().execute("DROP VIEW IF EXISTS View_Raw_Stock_Book");
            } catch (SQLException e) {
                // Ignore if view doesn't exist
            }
            
            sql = "CREATE VIEW View_Raw_Stock_Book AS " +
                "SELECT " +
                "    rsui.raw_stock_use_invoice_id AS raw_purchase_invoice_id, " +         // 0: invoice_id
                "    rsui.use_invoice_number AS invoice_number, " +              // 1: invoice_number
                "    'Internal Usage' AS supplier_name, " +                      // 2: supplier_name
                "    rsui.usage_date AS invoice_date, " +                        // 3: invoice_date
                "    rs.item_name, " +                                           // 4: item_name
                "    b.brand_name, " +                                           // 5: brand_name
                "    m.manufacturer_name, " +                                    // 6: manufacturer_name
                "    rsuii.quantity_used AS quantity, " +                        // 7: quantity
                "    rsuii.unit_cost AS unit_price, " +                          // 8: unit_price
                "    rsuii.total_cost AS item_total, " +                         // 9: item_total
                "    rsui.total_usage_amount AS total_amount, " +                // 10: total_amount
                "    0.0 AS discount_amount, " +                                 // 11: discount_amount
                "    rsui.total_usage_amount AS paid_amount, " +                 // 12: paid_amount
                "    0.0 AS balance " +                                          // 13: balance
                "FROM Raw_Stock_Use_Invoice rsui " +
                "JOIN Raw_Stock_Use_Invoice_Item rsuii ON rsui.raw_stock_use_invoice_id = rsuii.raw_stock_use_invoice_id " +
                "JOIN Raw_Stock rs ON rsuii.raw_stock_id = rs.stock_id " +
                "JOIN Brand b ON rs.brand_id = b.brand_id " +
                "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id";
            connection.createStatement().execute(sql);
            System.out.println("Created View_Raw_Stock_Book with 14 columns (item-level details)");
            
        } catch (SQLException e) {
            System.err.println("Error creating views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if a view exists in the database
     */
    private boolean viewExists(String viewName) {
        try {
            String sql = "SELECT name FROM sqlite_master WHERE type='view' AND name=?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, viewName);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Add sample data for testing reports
     */
    // public void addSampleDataForTesting() {
    //     System.out.println("DEBUG: Adding sample data for testing...");
        
    //     try {
    //         // Check if we already have data
    //         String countCustomersQuery = "SELECT COUNT(*) as total FROM Customer";
    //         try (PreparedStatement countStmt = connection.prepareStatement(countCustomersQuery)) {
    //             ResultSet countRs = countStmt.executeQuery();
    //             if (countRs.next() && countRs.getInt("total") > 0) {
    //                 System.out.println("DEBUG: Sample customer data already exists");
    //                 countRs.close();
    //                 return;
    //             }
    //             countRs.close();
    //         }
            
    //         // // Insert sample customers with tehsil assignments
    //         // insertCustomer("Ali Traders", "03001234567", "Lahore");
    //         // insertCustomer("Pak Electric House", "03111234567", "Karachi");
    //         // insertCustomer("Modern Cables Ltd", "03221234567", "Islamabad");
    //         // insertCustomer("Cable World", "03331234567", "Faisalabad");
            
    //         // // Insert sample suppliers with tehsil assignments  
    //         // insertSupplier("RawMetals Pvt Ltd", "03441234567", "Lahore");
    //         // insertSupplier("Insulation Depot", "03551234567", "Karachi");
    //         // insertSupplier("Wire Masters", "03661234567", "Islamabad");
    //         // insertSupplier("Industrial Supplies Co", "03771234567", "Multan");
            
    //         // // Insert sample banks
    //         // insertBank("Allied Bank Limited", "001-2345-6789", "Model Town Branch");
    //         // insertBank("Habib Bank Limited", "123-4567-8901", "Gulshan Branch");
    //         // insertBank("MCB Bank Limited", "456-7890-1234", "Main Boulevard Branch");
    //         // insertBank("Standard Chartered", "789-0123-4567", "Commercial Area Branch");
            
    //         System.out.println("DEBUG: Sample data added successfully");
            
    //     } catch (Exception e) {
    //         System.err.println("DEBUG: Error adding sample data: " + e.getMessage());
    //         e.printStackTrace();
    //     }
    // }

    @Override
public int getSalesInvoiceIdByNumber(String invoiceNumber) {
    String query = "SELECT sales_invoice_id FROM Sales_Invoice WHERE sales_invoice_number = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, invoiceNumber);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("sales_invoice_id");
        }
    } catch (SQLException e) {
        System.err.println("Error getting sales invoice ID: " + e.getMessage());
        e.printStackTrace();
    }
    return -1;
}

}


