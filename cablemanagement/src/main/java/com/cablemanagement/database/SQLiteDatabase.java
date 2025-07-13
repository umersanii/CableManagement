package com.cablemanagement.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.cablemanagement.model.Brand;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Manufacturer;
import com.cablemanagement.model.Supplier;

public class SQLiteDatabase implements db {
    
    private Connection connection;
    private String databasePath;
    
    public SQLiteDatabase() {
        this.databasePath = "cable_management.db";
        // Auto-connect when instantiated
        connect(null, null, null);
        // Initialize all required tables
        initializeDatabase();
    }
    
    public SQLiteDatabase(String databasePath) {
        this.databasePath = databasePath;
        // Auto-connect when instantiated
        connect(databasePath, null, null);
        // Initialize all required tables
        initializeDatabase();
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
                "position TEXT," +
                "salary REAL," +
                "contact_number TEXT," +
                "address TEXT," +
                "hire_date TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")",
                
                // Attendance table
                "CREATE TABLE IF NOT EXISTS Attendance (" +
                "attendance_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "employee_id INTEGER NOT NULL," +
                "date TEXT NOT NULL," +
                "status TEXT NOT NULL," +
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
                "CREATE TABLE IF NOT EXISTS RawStock (" +
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
                "CREATE TABLE IF NOT EXISTS CashTransaction (" +
                "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "description TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "transaction_type TEXT NOT NULL," +
                "date TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")",
                
                // Bank Transaction table
                "CREATE TABLE IF NOT EXISTS BankTransaction (" +
                "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "bank_id INTEGER NOT NULL," +
                "description TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "transaction_type TEXT NOT NULL," +
                "date TEXT DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (bank_id) REFERENCES Bank(bank_id)" +
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
                        "('Electric Cables'), ('Fiber Optic'), ('Coaxial'), ('Network Cables'), ('Power Cables')");
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
        
        // Check if Tehsil table is empty and insert some default tehsils
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Tehsil");
        rs.next();
        if (rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Tehsil (tehsil_name, district_id) VALUES " +
                        "('Model Town', 1), ('Gulshan', 2), ('University Town', 3), ('Satellite Town', 4), ('F-10', 5)");
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
        String query = "SELECT customer_name, customer_contact FROM Customer ORDER BY customer_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String name = rs.getString("customer_name");
                String contact = rs.getString("customer_contact");
                customers.add(new Customer(name, contact));
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
        String query = "SELECT supplier_name, supplier_contact FROM Supplier ORDER BY supplier_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String name = rs.getString("supplier_name");
                String contact = rs.getString("supplier_contact");
                suppliers.add(new Supplier(name, contact));
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
        String query = "SELECT r.raw_stock_id, r.raw_stock_name, c.category_name, b.brand_name, " +
                      "u.unit_name, r.opening_quantity, r.purchase_price_per_unit, r.reorder_level " +
                      "FROM Raw_Stock r " +
                      "JOIN Category c ON r.category_id = c.category_id " +
                      "JOIN Brand b ON r.brand_id = b.brand_id " +
                      "JOIN Unit u ON r.unit_id = u.unit_id " +
                      "ORDER BY r.raw_stock_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("raw_stock_id"),
                    rs.getString("raw_stock_name"),
                    rs.getString("category_name"),
                    rs.getString("brand_name"),
                    rs.getString("unit_name"),
                    rs.getDouble("opening_quantity"),
                    rs.getDouble("purchase_price_per_unit"),
                    rs.getDouble("reorder_level")
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
        String query = "INSERT INTO Raw_Stock (raw_stock_name, category_id, brand_id, unit_id, " +
                      "opening_quantity, purchase_price_per_unit, reorder_level) " +
                      "SELECT ?, c.category_id, b.brand_id, u.unit_id, ?, ?, ? " +
                      "FROM Category c, Brand b, Unit u " +
                      "WHERE c.category_name = ? AND b.brand_name = ? AND u.unit_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, openingQty);
            pstmt.setDouble(3, purchasePrice);
            pstmt.setDouble(4, reorderLevel);
            pstmt.setString(5, category);
            pstmt.setString(6, brand);
            pstmt.setString(7, unit);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
                      "FROM Raw_Stock_Usage rsu " +
                      "JOIN Raw_Stock rs ON rsu.raw_stock_id = rs.raw_stock_id " +
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
    // Production Stock Operations
    // --------------------------
    @Override
    public List<Object[]> getAllProductionStocks() {
        List<Object[]> productionStocks = new ArrayList<>();
        String query = "SELECT ps.production_stock_id, ps.production_stock_name, c.category_name, " +
                      "b.brand_name, u.unit_name, ps.sale_price_per_unit, ps.opening_quantity " +
                      "FROM Production_Stock ps " +
                      "JOIN Category c ON ps.category_id = c.category_id " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "JOIN Unit u ON ps.unit_id = u.unit_id " +
                      "ORDER BY ps.production_stock_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_stock_id"),
                    rs.getString("production_stock_name"),
                    rs.getString("category_name"),
                    rs.getString("brand_name"),
                    rs.getString("unit_name"),
                    rs.getDouble("sale_price_per_unit"),
                    rs.getDouble("opening_quantity")
                };
                productionStocks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productionStocks;
    }

    @Override
    public List<Object[]> getAllProductionInvoices() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT pi.production_invoice_id, pi.production_date, pi.notes, " +
                      "ps.production_stock_name, pii.quantity_produced " +
                      "FROM Production_Invoice pi " +
                      "JOIN Production_Invoice_Item pii ON pi.production_invoice_id = pii.production_invoice_id " +
                      "JOIN Production_Stock ps ON pii.production_stock_id = ps.production_stock_id " +
                      "ORDER BY pi.production_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_invoice_id"),
                    rs.getString("production_date"),
                    rs.getString("notes"),
                    rs.getString("production_stock_name"),
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
        String query = "SELECT bank_id, bank_name, account_number, branch_name, account_title FROM Bank ORDER BY bank_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("bank_id"),
                    rs.getString("bank_name"),
                    rs.getString("account_number"),
                    rs.getString("branch_name"),
                    rs.getString("account_title")
                };
                banks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return banks;
    }

    @Override
    public boolean insertBank(String bankName, String accountNumber, String branchName, String accountTitle) {
        String query = "INSERT INTO Bank (bank_name, account_number, branch_name, account_title) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, bankName);
            pstmt.setString(2, accountNumber);
            pstmt.setString(3, branchName);
            pstmt.setString(4, accountTitle);
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
        String query = "SELECT transaction_date, transaction_type, amount, description " +
                      "FROM Cash_Transaction " +
                      "ORDER BY transaction_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("transaction_date"),
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
        String query = "SELECT e.employee_id, e.employee_name, e.phone_number, d.designation_title, " +
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
        String query = "SELECT e.employee_name, el.loan_date, el.amount, " +
                      "CASE WHEN el.is_settled = 1 THEN 'Paid' ELSE 'Pending' END as status, " +
                      "el.description " +
                      "FROM Employee_Loan el " +
                      "JOIN Employee e ON el.employee_id = e.employee_id " +
                      "ORDER BY el.loan_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getString("loan_date"),
                    rs.getDouble("amount"),
                    rs.getString("status"),
                    rs.getString("description")
                };
                loans.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    // --------------------------
    // Salesman Operations
    // --------------------------
    @Override
    public List<Object[]> getAllSalesmen() {
        List<Object[]> salesmen = new ArrayList<>();
        String query = "SELECT salesman_id, salesman_name, phone_number, cnic, address FROM Salesman ORDER BY salesman_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("salesman_id"),
                    rs.getString("salesman_name"),
                    rs.getString("phone_number"),
                    rs.getString("cnic"),
                    rs.getString("address")
                };
                salesmen.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesmen;
    }

    @Override
    public boolean insertSalesman(String name, String phone, String cnic, String address) {
        String query = "INSERT INTO Salesman (salesman_name, phone_number, cnic, address) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, cnic);
            pstmt.setString(4, address);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
