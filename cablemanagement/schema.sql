-- Province table
CREATE TABLE IF NOT EXISTS Province (
    province_id INTEGER PRIMARY KEY AUTOINCREMENT,
    province_name TEXT NOT NULL UNIQUE
);

-- District table
CREATE TABLE IF NOT EXISTS District (
    district_id INTEGER PRIMARY KEY AUTOINCREMENT,
    district_name TEXT NOT NULL,
    province_id INTEGER NOT NULL,
    FOREIGN KEY (province_id) REFERENCES Province(province_id)
);

-- Tehsil table
CREATE TABLE IF NOT EXISTS Tehsil (
    tehsil_id INTEGER PRIMARY KEY AUTOINCREMENT,
    tehsil_name TEXT NOT NULL,
    district_id INTEGER NOT NULL,
    FOREIGN KEY (district_id) REFERENCES District(district_id)
);

-- Category table
CREATE TABLE IF NOT EXISTS Category (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_name TEXT NOT NULL UNIQUE
);

-- Designation table
CREATE TABLE IF NOT EXISTS Designation (
    designation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    designation_title TEXT NOT NULL UNIQUE
);

-- Manufacturer table
CREATE TABLE IF NOT EXISTS Manufacturer (
    manufacturer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    manufacturer_name TEXT NOT NULL,
    tehsil_id INTEGER NOT NULL,
    FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)
);

-- Brand table
CREATE TABLE IF NOT EXISTS Brand (
    brand_id INTEGER PRIMARY KEY AUTOINCREMENT,
    brand_name TEXT NOT NULL,
    manufacturer_id INTEGER NOT NULL,
    FOREIGN KEY (manufacturer_id) REFERENCES Manufacturer(manufacturer_id)
);

-- Customer table
CREATE TABLE IF NOT EXISTS Customer (
    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_name TEXT NOT NULL,
    contact_number TEXT,
    address TEXT,
    tehsil_id INTEGER NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)
);

-- Supplier table
CREATE TABLE IF NOT EXISTS Supplier (
    supplier_id INTEGER PRIMARY KEY AUTOINCREMENT,
    supplier_name TEXT NOT NULL,
    contact_number TEXT,
    address TEXT,
    tehsil_id INTEGER NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)
);

-- Bank table
CREATE TABLE IF NOT EXISTS Bank (
    bank_id INTEGER PRIMARY KEY,
    bank_name TEXT NOT NULL,
    account_number TEXT,
    branch_name TEXT,
    balance REAL DEFAULT 0.0
);

-- Employee table
CREATE TABLE IF NOT EXISTS Employee (
    employee_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_name TEXT NOT NULL,
    phone_number TEXT,
    cnic TEXT,
    address TEXT,
    hire_date TEXT NOT NULL,
    designation_id INTEGER NOT NULL,
    salary_type TEXT NOT NULL CHECK(salary_type IN ('monthly', 'daily', 'hourly', 'task')),
    salary_amount REAL NOT NULL,
    is_active INTEGER DEFAULT 1,
    FOREIGN KEY (designation_id) REFERENCES Designation(designation_id)
);

-- Employee Attendance table
CREATE TABLE IF NOT EXISTS Employee_Attendance (
    attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    attendance_date TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('present', 'absent', 'leave')),
    working_hours REAL DEFAULT 0,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

-- Employee Advance Salary table
CREATE TABLE IF NOT EXISTS Employee_Advance_Salary (
    advance_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    amount REAL NOT NULL,
    advance_date TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'granted' CHECK(status IN ('granted', 'adjusted', 'refunded')),
    created_date TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

-- Employee Loan table
CREATE TABLE IF NOT EXISTS Employee_Loan (
    loan_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    loan_amount REAL NOT NULL,
    loan_date TEXT NOT NULL,
    due_date TEXT,
    description TEXT,
    status TEXT DEFAULT 'active' CHECK(status IN ('active', 'paid', 'defaulted', 'written_off')),
    remaining_amount REAL NOT NULL,
    created_date TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

-- Salesman table
CREATE TABLE IF NOT EXISTS Salesman (
    salesman_id INTEGER PRIMARY KEY AUTOINCREMENT,
    salesman_name TEXT NOT NULL,
    contact_number TEXT,
    address TEXT,
    commission_rate REAL DEFAULT 0.0
);

-- Raw Stock table
CREATE TABLE IF NOT EXISTS Raw_Stock (
    stock_id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_name TEXT NOT NULL,
    brand_id INTEGER NOT NULL,
    unit_id INTEGER NOT NULL, 
    quantity INTEGER NOT NULL,
    unit_price REAL NOT NULL,
    total_cost REAL NOT NULL,
    supplier_id INTEGER,
    purchase_date TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (brand_id) REFERENCES Brand(brand_id),
    FOREIGN KEY (unit_id) REFERENCES Unit(unit_id),
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);


-- Raw Purchase Invoice table
CREATE TABLE IF NOT EXISTS Raw_Purchase_Invoice (
    raw_purchase_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_number TEXT NOT NULL UNIQUE,
    supplier_id INTEGER NOT NULL,
    invoice_date TEXT NOT NULL,
    total_amount REAL NOT NULL,
    discount_amount REAL DEFAULT 0,
    paid_amount REAL DEFAULT 0,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);

-- Raw Purchase Invoice Item table
CREATE TABLE IF NOT EXISTS Raw_Purchase_Invoice_Item (
    raw_purchase_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_purchase_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    FOREIGN KEY (raw_purchase_invoice_id) REFERENCES Raw_Purchase_Invoice(raw_purchase_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)
);

-- Raw Purchase Return Invoice table
CREATE TABLE IF NOT EXISTS Raw_Purchase_Return_Invoice (
    raw_purchase_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_invoice_number TEXT NOT NULL UNIQUE,
    original_invoice_id INTEGER NOT NULL,
    supplier_id INTEGER NOT NULL,
    return_date TEXT NOT NULL,
    total_return_amount REAL NOT NULL,
    FOREIGN KEY (original_invoice_id) REFERENCES Raw_Purchase_Invoice(raw_purchase_invoice_id),
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);

-- Raw Purchase Return Invoice Item table
CREATE TABLE IF NOT EXISTS Raw_Purchase_Return_Invoice_Item (
    raw_purchase_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_purchase_return_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    FOREIGN KEY (raw_purchase_return_invoice_id) REFERENCES Raw_Purchase_Return_Invoice(raw_purchase_return_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)
);

-- Production_Stock_Raw_Usage table
CREATE TABLE IF NOT EXISTS Production_Stock_Raw_Usage (
    usage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity_used REAL NOT NULL,
    FOREIGN KEY (production_invoice_id) REFERENCES Production_Invoice(production_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)
);

-- Raw Stock Usage table
CREATE TABLE IF NOT EXISTS Raw_Stock_Usage (
    raw_stock_usage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_stock_id INTEGER NOT NULL,
    usage_date TEXT NOT NULL,
    quantity_used REAL NOT NULL,
    reference TEXT,
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)
);

CREATE TABLE IF NOT EXISTS ProductionStock (
    production_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_name TEXT NOT NULL,
    brand_id INTEGER NOT NULL,
    unit_id INTEGER NOT NULL,         -- Added unit_id here
    quantity INTEGER NOT NULL,
    unit_cost REAL NOT NULL,
    total_cost REAL NOT NULL,
    production_date TEXT DEFAULT CURRENT_TIMESTAMP,
    sale_price REAL DEFAULT 0.0,
    manufacturer TEXT NOT NULL,
    category TEXT NOT NULL,
    FOREIGN KEY (brand_id) REFERENCES Brand(brand_id),
    FOREIGN KEY (unit_id) REFERENCES Unit(unit_id),
    FOREIGN KEY (manufacturer) REFERENCES Manufacturer(manufacturer_id),
    FOREIGN KEY (category) REFERENCES Category(category_id)
);


-- Unit table
CREATE TABLE IF NOT EXISTS Unit (
    unit_id INTEGER PRIMARY KEY AUTOINCREMENT,
    unit_name TEXT NOT NULL UNIQUE
);

-- Production Invoice table
CREATE TABLE IF NOT EXISTS Production_Invoice (
    production_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_date TEXT NOT NULL,
    notes TEXT
);

-- Production Invoice Item table
CREATE TABLE IF NOT EXISTS Production_Invoice_Item (
    production_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_invoice_id INTEGER NOT NULL,
    production_id INTEGER NOT NULL,
    quantity_produced REAL NOT NULL,
    FOREIGN KEY (production_invoice_id) REFERENCES Production_Invoice(production_invoice_id),
    FOREIGN KEY (production_id) REFERENCES ProductionStock(production_id)
);

-- Production Return Invoice table
CREATE TABLE IF NOT EXISTS Production_Return_Invoice (
    production_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_invoice_number TEXT NOT NULL UNIQUE,
    original_production_invoice_id INTEGER NOT NULL,
    return_date TEXT NOT NULL,
    total_return_quantity REAL NOT NULL,
    total_return_amount REAL NOT NULL,
    notes TEXT,
    FOREIGN KEY (original_production_invoice_id) REFERENCES Production_Invoice(production_invoice_id)
);

-- Production Return Invoice Item table
CREATE TABLE IF NOT EXISTS Production_Return_Invoice_Item (
    production_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_return_invoice_id INTEGER NOT NULL,
    production_id INTEGER NOT NULL,
    quantity_returned REAL NOT NULL,
    unit_cost REAL NOT NULL,
    total_cost REAL NOT NULL,
    FOREIGN KEY (production_return_invoice_id) REFERENCES Production_Return_Invoice(production_return_invoice_id),
    FOREIGN KEY (production_id) REFERENCES ProductionStock(production_id)
);

-- Cash Transaction table
CREATE TABLE IF NOT EXISTS Cash_Transaction (
    cash_transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_date TEXT NOT NULL,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('cash_in', 'cash_out', 'transfer_to_bank', 'transfer_from_bank')),
    amount REAL NOT NULL,
    description TEXT
);

-- Bank Transaction table
CREATE TABLE IF NOT EXISTS Bank_Transaction (
    bank_transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    bank_id INTEGER NOT NULL,
    transaction_date TEXT NOT NULL,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('deposit', 'withdraw', 'transfer_in', 'transfer_out', 'invoice_payment')),
    amount REAL NOT NULL,
    description TEXT,
    related_bank_id INTEGER,
    FOREIGN KEY (bank_id) REFERENCES Bank(bank_id),
    FOREIGN KEY (related_bank_id) REFERENCES Bank(bank_id)
);

-- Raw Stock Use Invoice table
CREATE TABLE IF NOT EXISTS Raw_Stock_Use_Invoice (
    raw_stock_use_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    use_invoice_number TEXT NOT NULL UNIQUE,
    usage_date TEXT NOT NULL,
    total_usage_amount REAL NOT NULL DEFAULT 0.0,
    reference_purpose TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Raw Stock Use Invoice Item table
CREATE TABLE IF NOT EXISTS Raw_Stock_Use_Invoice_Item (
    raw_stock_use_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_stock_use_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity_used REAL NOT NULL,
    unit_cost REAL NOT NULL,
    total_cost REAL NOT NULL,
    FOREIGN KEY (raw_stock_use_invoice_id) REFERENCES Raw_Stock_Use_Invoice(raw_stock_use_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)
);

-- Sales Invoice table
CREATE TABLE IF NOT EXISTS Sales_Invoice (
    sales_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sales_invoice_number TEXT NOT NULL UNIQUE,
    customer_id INTEGER NOT NULL,
    sales_date TEXT NOT NULL,
    total_amount REAL NOT NULL DEFAULT 0.0,
    discount_amount REAL NOT NULL DEFAULT 0.0,
    paid_amount REAL NOT NULL DEFAULT 0.0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

CREATE TABLE IF NOT EXISTS Sales_Invoice_Item (
    sales_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sales_invoice_id INTEGER NOT NULL,
    production_stock_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    discount_percentage REAL NOT NULL DEFAULT 0.0,
    discount_amount REAL NOT NULL DEFAULT 0.0,
    total_price REAL NOT NULL,
    FOREIGN KEY (sales_invoice_id) REFERENCES Sales_Invoice(sales_invoice_id),
    FOREIGN KEY (production_stock_id) REFERENCES ProductionStock(production_id)
);

-- Sales Return Invoice table
CREATE TABLE IF NOT EXISTS Sales_Return_Invoice (
    sales_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_invoice_number TEXT NOT NULL UNIQUE,
    original_sales_invoice_id INTEGER NOT NULL,
    customer_id INTEGER NOT NULL,
    return_date TEXT NOT NULL,
    total_return_amount REAL NOT NULL DEFAULT 0.0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (original_sales_invoice_id) REFERENCES Sales_Invoice(sales_invoice_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

-- Sales Return Invoice Item table
CREATE TABLE IF NOT EXISTS Sales_Return_Invoice_Item (
    sales_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sales_return_invoice_id INTEGER NOT NULL,
    production_stock_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    total_price REAL NOT NULL,
    FOREIGN KEY (sales_return_invoice_id) REFERENCES Sales_Return_Invoice(sales_return_invoice_id),
    FOREIGN KEY (production_stock_id) REFERENCES ProductionStock(production_id)
);

-- Contract based employee table
CREATE TABLE IF NOT EXISTS Contract_Employee (
    employee_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    cnic TEXT NOT NULL,
    address TEXT NOT NULL,
    remarks TEXT NOT NULL,
    task TEXT NOT NULL,
    num_tasks INTEGER NOT NULL,
    cost_per_task REAL NOT NULL,
    total_tasks_done INTEGER NOT NULL,
    date TEXT NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);



---------------------------------------------------------------------
---               Sample Data Insertion
---------------------------------------------------------------------


-- -- Province
-- INSERT OR IGNORE INTO Province (province_name) VALUES ('Punjab'), ('Sindh'), ('Khyber Pakhtunkhwa'), ('Balochistan');

-- -- District
-- INSERT OR IGNORE INTO District (district_name, province_id) VALUES 
-- ('Lahore', 1),
-- ('Karachi', 2),
-- ('Peshawar', 3),
-- ('Quetta', 4);

-- -- Tehsil
-- INSERT OR IGNORE INTO Tehsil (tehsil_name, district_id) VALUES 
-- ('Model Town', 1),
-- ('Korangi', 2),
-- ('Hayatabad', 3),
-- -- Province
-- INSERT OR IGNORE INTO Province (province_name) VALUES ('Punjab'), ('Sindh'), ('Khyber Pakhtunkhwa'), ('Balochistan');

-- -- District
-- INSERT OR IGNORE INTO District (district_name, province_id) VALUES 
-- ('Lahore', 1),
-- ('Karachi', 2),
-- ('Peshawar', 3),
-- ('Quetta', 4);

-- -- Tehsil
-- INSERT OR IGNORE INTO Tehsil (tehsil_name, district_id) VALUES 
-- ('Model Town', 1),
-- ('Korangi', 2),
-- ('Hayatabad', 3),
-- ('Sariab', 4);

-- -- Category
-- INSERT OR IGNORE INTO Category (category_name) VALUES ('Electronics'), ('Furniture'), ('Food'), ('Textile');

-- -- Designation
-- INSERT OR IGNORE INTO Designation (designation_title) VALUES ('Manager'), ('Worker'), ('Accountant'), ('Sales Executive');

-- -- Manufacturer
-- INSERT OR IGNORE INTO Manufacturer (manufacturer_name, tehsil_id) VALUES 
-- ('ABC Industries', 1),
-- ('XYZ Manufacturing', 2);

-- -- Brand
-- INSERT OR IGNORE INTO Brand (brand_name, manufacturer_id) VALUES 
-- ('TechBrand', 1),
-- ('HomeComfort', 2);

-- -- Unit
-- INSERT OR IGNORE INTO Unit (unit_name) VALUES ('Piece'), ('Kilogram'), ('Liter'), ('Meter');

-- -- Customer
-- INSERT OR IGNORE INTO Customer (customer_name, contact_number, address, tehsil_id, balance) VALUES 
-- ('Ali Khan', '03001234567', 'Lahore, Punjab', 1, 1000.00),
-- ('Sara Ahmed', '03111234567', 'Karachi, Sindh', 2, 500.00);

-- -- Supplier
-- INSERT OR IGNORE INTO Supplier (supplier_name, contact_number, address, tehsil_id, balance) VALUES 
-- ('Imran Traders', '03021234567', 'Lahore', 1, 2000.00),
-- ('Usman Suppliers', '03211234567', 'Karachi', 2, 1500.00);

-- -- Bank
-- INSERT OR IGNORE INTO Bank (bank_id, bank_name, account_number, branch_name, balance) VALUES 
-- (1, 'HBL', '1234567890', 'Main Branch', 100000.00),
-- (2, 'UBL', '9876543210', 'City Branch', 50000.00);

-- -- Salesman
-- INSERT OR IGNORE INTO Salesman (salesman_name, contact_number, address, commission_rate) VALUES 
-- ('Rashid Mehmood', '03331234567', 'Lahore', 5.0),
-- ('Kashif Iqbal', '03451234567', 'Karachi', 7.0);

-- ('Sariab', 4);

-- -- Category
-- INSERT OR IGNORE INTO Category (category_name) VALUES ('Electronics'), ('Furniture'), ('Food'), ('Textile');

-- -- Designation
-- INSERT OR IGNORE INTO Designation (designation_title) VALUES ('Manager'), ('Worker'), ('Accountant'), ('Sales Executive');

-- -- Manufacturer
-- INSERT OR IGNORE INTO Manufacturer (manufacturer_name, tehsil_id) VALUES 
-- ('ABC Industries', 1),
-- ('XYZ Manufacturing', 2);

-- -- Brand
-- INSERT OR IGNORE INTO Brand (brand_name, manufacturer_id) VALUES 
-- ('TechBrand', 1),
-- ('HomeComfort', 2);

-- -- Unit
-- INSERT OR IGNORE INTO Unit (unit_name) VALUES ('Piece'), ('Kilogram'), ('Liter'), ('Meter');

-- -- Customer
-- INSERT OR IGNORE INTO Customer (customer_name, contact_number, address, tehsil_id, balance) VALUES 
-- ('Ali Khan', '03001234567', 'Lahore, Punjab', 1, 1000.00),
-- ('Sara Ahmed', '03111234567', 'Karachi, Sindh', 2, 500.00);

-- -- Supplier
-- INSERT OR IGNORE INTO Supplier (supplier_name, contact_number, address, tehsil_id, balance) VALUES 
-- ('Imran Traders', '03021234567', 'Lahore', 1, 2000.00),
-- ('Usman Suppliers', '03211234567', 'Karachi', 2, 1500.00);

-- -- Bank
-- INSERT OR IGNORE INTO Bank (bank_id, bank_name, account_number, branch_name, balance) VALUES 
-- (1, 'HBL', '1234567890', 'Main Branch', 100000.00),
-- (2, 'UBL', '9876543210', 'City Branch', 50000.00);

-- -- Salesman
-- INSERT OR IGNORE INTO Salesman (salesman_name, contact_number, address, commission_rate) VALUES 
-- ('Rashid Mehmood', '03331234567', 'Lahore', 5.0),
-- ('Kashif Iqbal', '03451234567', 'Karachi', 7.0);
