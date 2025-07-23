

--REGESTRATION PART 


CREATE TABLE Province (
    province_id INTEGER PRIMARY KEY AUTOINCREMENT,
    province_name TEXT NOT NULL UNIQUE
);





CREATE TABLE District (
    district_id INTEGER PRIMARY KEY AUTOINCREMENT,
    district_name TEXT NOT NULL,
    province_id INTEGER NOT NULL,
    FOREIGN KEY (province_id) REFERENCES Province(province_id)
);




CREATE TABLE Tehsil (
    tehsil_id INTEGER PRIMARY KEY AUTOINCREMENT,
    tehsil_name TEXT NOT NULL,
    district_id INTEGER NOT NULL,
    FOREIGN KEY (district_id) REFERENCES District(district_id)
);






CREATE TABLE Category (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_name TEXT NOT NULL UNIQUE
);




CREATE TABLE Manufacturer (
    manufacturer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    manufacturer_name TEXT NOT NULL,
    tehsil_id INTEGER NOT NULL,
    FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)
);




CREATE TABLE Brand (
    brand_id INTEGER PRIMARY KEY AUTOINCREMENT,
    brand_name TEXT NOT NULL,
    manufacturer_id INTEGER NOT NULL,
    tehsil_id INTEGER NOT NULL,
    FOREIGN KEY (manufacturer_id) REFERENCES Manufacturer(manufacturer_id),
    FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)
);




CREATE TABLE Unit (
    unit_id INTEGER PRIMARY KEY AUTOINCREMENT,
    unit_name TEXT NOT NULL UNIQUE
);




CREATE TABLE Designation (
    designation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    designation_title TEXT NOT NULL UNIQUE
);






CREATE TABLE Customer (
    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_name TEXT NOT NULL,
    customer_contact TEXT,
    tehsil_id INTEGER NOT NULL,
    FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)
);





CREATE TABLE Supplier (
    supplier_id INTEGER PRIMARY KEY AUTOINCREMENT,
    supplier_name TEXT NOT NULL,
    supplier_contact TEXT,
    tehsil_id INTEGER NOT NULL,
    FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)
);



INSERT INTO Province (province_name) VALUES
('Punjab'),
('Sindh'),
('Khyber Pakhtunkhwa'),
('Balochistan');

INSERT INTO District (district_name, province_id) VALUES
('Lahore', 1),
('Karachi', 2),
('Peshawar', 3),
('Quetta', 4);


INSERT INTO Tehsil (tehsil_name, district_id) VALUES
('Model Town', 1),
('Gulshan-e-Iqbal', 2),
('Saddar', 3),
('Satellite Town', 4);

INSERT INTO Category (category_name) VALUES
('Copper Wire'),
('PVC Insulation'),
('Aluminum Conductor');


INSERT INTO Manufacturer (manufacturer_name, tehsil_id) VALUES
('CableTech Industries', 1),
('Wires & Co', 2);



INSERT INTO Brand (brand_name, manufacturer_id, tehsil_id) VALUES
('PowerFlex', 1, 1),
('SafeWire', 2, 2);



INSERT INTO Unit (unit_name) VALUES
('Meter'),
('Roll'),
('Kg');





INSERT INTO Designation (designation_title) VALUES
('Manager'),
('Technician'),
('Sales Representative');




INSERT INTO Customer (customer_name, customer_contact, tehsil_id) VALUES
('Ali Traders', '03001234567', 1),
('Pak Electric House', '03111234567', 2);





INSERT INTO Supplier (supplier_name, supplier_contact, tehsil_id) VALUES
('RawMetals Pvt Ltd', '03221234567', 1),
('Insulation Depot', '03331234567', 2);



--RAW STOCKS PART
CREATE TABLE Raw_Stock (
    raw_stock_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_stock_name TEXT NOT NULL,
    category_id INTEGER NOT NULL,
    brand_id INTEGER NOT NULL,
    unit_id INTEGER NOT NULL,
    opening_quantity REAL DEFAULT 0,
    purchase_price_per_unit REAL NOT NULL,
    reorder_level REAL DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES Category(category_id),
    FOREIGN KEY (brand_id) REFERENCES Brand(brand_id),
    FOREIGN KEY (unit_id) REFERENCES Unit(unit_id)
);


CREATE TABLE Raw_Purchase_Invoice (
    raw_purchase_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_number TEXT NOT NULL UNIQUE,
    supplier_id INTEGER NOT NULL,
    invoice_date TEXT NOT NULL,
    total_amount REAL NOT NULL,
    discount_amount REAL DEFAULT 0,
    paid_amount REAL DEFAULT 0,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);





CREATE TABLE Raw_Purchase_Invoice_Item (
    raw_purchase_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_purchase_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    FOREIGN KEY (raw_purchase_invoice_id) REFERENCES Raw_Purchase_Invoice(raw_purchase_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(raw_stock_id)
);

CREATE TABLE Raw_Purchase_Return_Invoice (
    raw_purchase_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_invoice_number TEXT NOT NULL UNIQUE,
    original_invoice_id INTEGER NOT NULL,
    supplier_id INTEGER NOT NULL,
    return_date TEXT NOT NULL,
    total_return_amount REAL NOT NULL,
    FOREIGN KEY (original_invoice_id) REFERENCES Raw_Purchase_Invoice(raw_purchase_invoice_id),
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);

CREATE TABLE Raw_Purchase_Return_Invoice_Item (
    raw_purchase_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_purchase_return_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    FOREIGN KEY (raw_purchase_return_invoice_id) REFERENCES Raw_Purchase_Return_Invoice(raw_purchase_return_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(raw_stock_id)
);



CREATE TABLE Raw_Stock_Usage (
    raw_stock_usage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_stock_id INTEGER NOT NULL,
    usage_date TEXT NOT NULL,
    quantity_used REAL NOT NULL,
    reference TEXT, -- e.g., linked to a production batch or comment
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(raw_stock_id)
);

-- Raw Stock Use Invoice Tables
CREATE TABLE Raw_Stock_Use_Invoice (
    raw_stock_use_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    use_invoice_number TEXT NOT NULL UNIQUE,
    usage_date TEXT NOT NULL,
    total_usage_amount REAL NOT NULL,
    reference_purpose TEXT -- Overall purpose/reference for the usage
);

CREATE TABLE Raw_Stock_Use_Invoice_Item (
    raw_stock_use_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_stock_use_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity_used REAL NOT NULL,
    unit_cost REAL NOT NULL, -- Cost per unit for this item
    FOREIGN KEY (raw_stock_use_invoice_id) REFERENCES Raw_Stock_Use_Invoice(raw_stock_use_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(raw_stock_id)
);


INSERT INTO Raw_Stock (raw_stock_name, category_id, brand_id, unit_id, opening_quantity, purchase_price_per_unit, reorder_level) VALUES
('Copper Wire 8mm', 1, 1, 1, 1000, 250.00, 200),
('PVC Granules', 2, 2, 3, 500, 100.00, 100);

INSERT INTO Raw_Purchase_Invoice (invoice_number, supplier_id, invoice_date, total_amount, discount_amount, paid_amount) VALUES
('INV-RP-001', 1, '2025-07-01', 25000, 1000, 20000),
('INV-RP-002', 2, '2025-07-03', 10000, 0, 10000);

-- Items for invoice INV-RP-001
INSERT INTO Raw_Purchase_Invoice_Item (raw_purchase_invoice_id, raw_stock_id, quantity, unit_price) VALUES
(1, 1, 100, 250.00);  -- Copper Wire

-- Items for invoice INV-RP-002
INSERT INTO Raw_Purchase_Invoice_Item (raw_purchase_invoice_id, raw_stock_id, quantity, unit_price) VALUES
(2, 2, 100, 100.00);  -- PVC Granules

INSERT INTO Raw_Purchase_Return_Invoice (return_invoice_number, original_invoice_id, supplier_id, return_date, total_return_amount) VALUES
('INV-RPR-001', 1, 1, '2025-07-04', 5000);

INSERT INTO Raw_Purchase_Return_Invoice_Item (raw_purchase_return_invoice_id, raw_stock_id, quantity, unit_price) VALUES
(1, 1, 20, 250.00); -- Returned 20 Copper Wire

INSERT INTO Raw_Stock_Usage (raw_stock_id, usage_date, quantity_used, reference) VALUES
(1, '2025-07-05', 50, 'Used for Production Batch 1'),  -- Copper Wire
(2, '2025-07-05', 30, 'Used for Production Batch 1');  -- PVC


--PRODUCTION STOCK PART


CREATE TABLE Production_Stock (
    production_stock_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_stock_name TEXT NOT NULL,
    category_id INTEGER NOT NULL,
    brand_id INTEGER NOT NULL,
    unit_id INTEGER NOT NULL,
    sale_price_per_unit REAL NOT NULL,
    opening_quantity REAL DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES Category(category_id),
    FOREIGN KEY (brand_id) REFERENCES Brand(brand_id),
    FOREIGN KEY (unit_id) REFERENCES Unit(unit_id)
);


CREATE TABLE Production_Invoice (
    production_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_date TEXT NOT NULL,
    notes TEXT
);





CREATE TABLE Production_Invoice_Item (
    production_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_invoice_id INTEGER NOT NULL,
    production_stock_id INTEGER NOT NULL,
    quantity_produced REAL NOT NULL,
    FOREIGN KEY (production_invoice_id) REFERENCES Production_Invoice(production_invoice_id),
    FOREIGN KEY (production_stock_id) REFERENCES Production_Stock(production_stock_id)
);





CREATE TABLE Production_Stock_Raw_Usage (
    usage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity_used REAL NOT NULL,
    FOREIGN KEY (production_invoice_id) REFERENCES Production_Invoice(production_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(raw_stock_id)
);



CREATE TABLE Production_Return_Invoice (
    production_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_invoice_number TEXT NOT NULL UNIQUE,
    original_production_invoice_id INTEGER NOT NULL,
    return_date TEXT NOT NULL,
    total_return_quantity REAL NOT NULL,
    reference TEXT,
    FOREIGN KEY (original_production_invoice_id) REFERENCES Production_Invoice(production_invoice_id)
);

CREATE TABLE Production_Return_Invoice_Item (
    production_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_return_invoice_id INTEGER NOT NULL,
    production_stock_id INTEGER NOT NULL,
    quantity_returned REAL NOT NULL,
    unit_price REAL NOT NULL,
    FOREIGN KEY (production_return_invoice_id) REFERENCES Production_Return_Invoice(production_return_invoice_id),
    FOREIGN KEY (production_stock_id) REFERENCES Production_Stock(production_stock_id)
);




INSERT INTO Production_Invoice (production_date, notes) VALUES
('2025-07-05', 'Batch #1 - Copper & PVC cables');

INSERT INTO Production_Invoice_Item (production_invoice_id, production_stock_id, quantity_produced) VALUES
(1, 1, 10),  -- 10 rolls of Copper Cable
(1, 2, 5);   -- 5 rolls of PVC Sheathed Wire

INSERT INTO Production_Stock_Raw_Usage (production_invoice_id, raw_stock_id, quantity_used) VALUES
(1, 1, 100),  -- Used 100m Copper Wire
(1, 2, 50);   -- Used 50kg PVC Granules

INSERT INTO Production_Return_Invoice (return_date, reference, total_return_quantity) VALUES
('2025-07-06', 'Customer return - damaged roll', 3);







--BOOKS PART 
CREATE VIEW View_Purchase_Book AS
SELECT 
    Raw_Purchase_Invoice.invoice_number,
    Raw_Purchase_Invoice.invoice_date,
    Supplier.supplier_name,
    Raw_Purchase_Invoice.total_amount,
    Raw_Purchase_Invoice.discount_amount,
    Raw_Purchase_Invoice.paid_amount
FROM Raw_Purchase_Invoice
JOIN Supplier ON Raw_Purchase_Invoice.supplier_id = Supplier.supplier_id;


CREATE VIEW View_Return_Purchase_Book AS
SELECT 
    Raw_Purchase_Return_Invoice.return_invoice_number,
    Raw_Purchase_Return_Invoice.return_date,
    Supplier.supplier_name,
    Raw_Purchase_Return_Invoice.total_return_amount,
    Raw_Purchase_Invoice.invoice_number AS original_invoice
FROM Raw_Purchase_Return_Invoice
JOIN Supplier ON Raw_Purchase_Return_Invoice.supplier_id = Supplier.supplier_id
LEFT JOIN Raw_Purchase_Invoice ON Raw_Purchase_Return_Invoice.original_invoice_id = Raw_Purchase_Invoice.raw_purchase_invoice_id;



CREATE VIEW View_Raw_Stock_Book AS
SELECT 
    Raw_Stock_Usage.usage_date,
    Raw_Stock.raw_stock_name,
    Raw_Stock_Usage.quantity_used,
    Raw_Stock_Usage.reference
FROM Raw_Stock_Usage
JOIN Raw_Stock ON Raw_Stock_Usage.raw_stock_id = Raw_Stock.raw_stock_id;


CREATE VIEW View_Return_Raw_Stock_Book AS
SELECT 
    Raw_Purchase_Return_Invoice.return_date,
    Raw_Stock.raw_stock_name,
    Raw_Purchase_Return_Invoice_Item.quantity,
    Raw_Purchase_Return_Invoice_Item.unit_price,
    Supplier.supplier_name
FROM Raw_Purchase_Return_Invoice_Item
JOIN Raw_Stock ON Raw_Purchase_Return_Invoice_Item.raw_stock_id = Raw_Stock.raw_stock_id
JOIN Raw_Purchase_Return_Invoice ON Raw_Purchase_Return_Invoice_Item.raw_purchase_return_invoice_id = Raw_Purchase_Return_Invoice.raw_purchase_return_invoice_id
JOIN Supplier ON Raw_Purchase_Return_Invoice.supplier_id = Supplier.supplier_id;


CREATE VIEW View_Production_Book AS
SELECT 
    Production_Invoice.production_date,
    Production_Stock.production_stock_name,
    Production_Invoice_Item.quantity_produced,
    Production_Invoice.notes
FROM Production_Invoice
JOIN Production_Invoice_Item ON Production_Invoice.production_invoice_id = Production_Invoice_Item.production_invoice_id
JOIN Production_Stock ON Production_Invoice_Item.production_stock_id = Production_Stock.production_stock_id;


CREATE VIEW View_Return_Production_Book AS
SELECT 
    pri.return_invoice_number,
    pri.return_date,
    pri.reference,
    pri.total_return_quantity,
    pi.production_date AS original_production_date,
    pi.notes AS original_production_notes
FROM Production_Return_Invoice pri
JOIN Production_Invoice pi ON pri.original_production_invoice_id = pi.production_invoice_id;

-- View for production invoice items that can be returned
CREATE VIEW View_Production_Invoice_Items_For_Return AS
SELECT 
    pi.production_invoice_id,
    pi.production_date,
    pi.notes,
    ps.production_stock_id,
    ps.production_stock_name,
    b.brand_name,
    u.unit_name,
    pii.quantity_produced,
    ps.sale_price_per_unit,
    COALESCE(SUM(prii.quantity_returned), 0) AS total_returned,
    (pii.quantity_produced - COALESCE(SUM(prii.quantity_returned), 0)) AS available_for_return
FROM Production_Invoice pi
JOIN Production_Invoice_Item pii ON pi.production_invoice_id = pii.production_invoice_id
JOIN Production_Stock ps ON pii.production_stock_id = ps.production_stock_id
JOIN Brand b ON ps.brand_id = b.brand_id
JOIN Unit u ON ps.unit_id = u.unit_id
LEFT JOIN Production_Return_Invoice_Item prii ON pii.production_stock_id = prii.production_stock_id
GROUP BY pi.production_invoice_id, ps.production_stock_id
HAVING available_for_return > 0;



CREATE VIEW View_Sales_Book AS
SELECT 
    Sales_Invoice.sales_invoice_number,
    Sales_Invoice.sales_date,
    Customer.customer_name,
    Sales_Invoice.total_amount,
    Sales_Invoice.discount_amount,
    Sales_Invoice.paid_amount
FROM Sales_Invoice
JOIN Customer ON Sales_Invoice.customer_id = Customer.customer_id;


CREATE VIEW View_Return_Sales_Book AS
SELECT 
    Sales_Return_Invoice.return_invoice_number,
    Sales_Return_Invoice.return_date,
    Customer.customer_name,
    Sales_Return_Invoice.total_return_amount
FROM Sales_Return_Invoice
JOIN Customer ON Sales_Return_Invoice.customer_id = Customer.customer_id;





--BANK MANAGEMENT PART

CREATE TABLE Bank (
    bank_id INTEGER PRIMARY KEY AUTOINCREMENT,
    bank_name TEXT NOT NULL,
    account_number TEXT NOT NULL UNIQUE,
    branch_name TEXT,
    account_title TEXT
);


CREATE TABLE Bank_Transaction (
    bank_transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_date TEXT NOT NULL,
    bank_id INTEGER NOT NULL,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('deposit', 'withdraw', 'transfer_in', 'transfer_out', 'invoice_payment')),
    amount REAL NOT NULL,
    description TEXT,
    related_bank_id INTEGER, -- for transfers
    FOREIGN KEY (bank_id) REFERENCES Bank(bank_id),
    FOREIGN KEY (related_bank_id) REFERENCES Bank(bank_id)
);



CREATE TABLE Cash_Transaction (
    cash_transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_date TEXT NOT NULL,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('cash_in', 'cash_out', 'transfer_to_bank', 'transfer_from_bank')),
    amount REAL NOT NULL,
    description TEXT
);




CREATE VIEW View_Cash_Ledger AS
SELECT
    transaction_type,
    SUM(amount) AS total_amount
FROM Cash_Transaction
GROUP BY transaction_type;



CREATE VIEW View_Cash_In_Hand AS
SELECT 
    IFNULL(SUM(CASE WHEN transaction_type IN ('cash_in', 'transfer_from_bank') THEN amount ELSE 0 END), 0) -
    IFNULL(SUM(CASE WHEN transaction_type IN ('cash_out', 'transfer_to_bank') THEN amount ELSE 0 END), 0) 
    AS current_cash_balance
FROM Cash_Transaction;



CREATE TABLE Salesman (
    salesman_id INTEGER PRIMARY KEY AUTOINCREMENT,
    salesman_name TEXT NOT NULL,
    phone_number TEXT,
    cnic TEXT,
    address TEXT
);




INSERT INTO Bank (bank_name, account_number, branch_name, account_title) VALUES
('HBL', '100200300', 'Peshawar Branch', 'WireTech Industries'),
('UBL', '200300400', 'Lahore Branch', 'WireTech Industries'),
('MCB', '300400500', 'Karachi Branch', 'WireTech Payroll');


INSERT INTO Bank_Transaction (transaction_date, bank_id, transaction_type, amount, description)
VALUES
('2025-07-01', 1, 'deposit', 500000.00, 'Initial capital'),
('2025-07-02', 2, 'deposit', 250000.00, 'Wire sales deposit'),
('2025-07-03', 1, 'withdraw', 100000.00, 'Raw stock purchase'),
('2025-07-04', 1, 'transfer_out', 50000.00, 'Transfer to MCB'),
('2025-07-04', 3, 'transfer_in', 50000.00, 'Received from HBL');



INSERT INTO Cash_Transaction (transaction_date, transaction_type, amount, description)
VALUES
('2025-07-01', 'cash_in', 30000.00, 'Opening cash'),
('2025-07-02', 'cash_out', 5000.00, 'Local transport expense'),
('2025-07-03', 'transfer_to_bank', 15000.00, 'Cash deposit to HBL'),
('2025-07-04', 'transfer_from_bank', 20000.00, 'Cash withdrawal from MCB');



INSERT INTO Salesman (salesman_name, phone_number, cnic, address) VALUES
('Imran Khan', '03001234567', '12345-6789012-3', 'Peshawar, KP'),
('Ali Shah', '03009876543', '54321-0987654-3', 'Lahore, Punjab');





--EMPLOYEE MANAGEMENT PART

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


CREATE TABLE IF NOT EXISTS Employee_Attendance (
    attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    attendance_date TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('present', 'absent', 'leave')),
    working_hours REAL DEFAULT 0,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);




CREATE TABLE IF NOT EXISTS Employee_Salary_Payment (
    salary_payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    payment_date TEXT NOT NULL,
    salary_amount REAL NOT NULL,
    description TEXT,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);


CREATE TABLE IF NOT EXISTS Advance_Salary (
    advance_salary_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    advance_date TEXT NOT NULL,
    amount REAL NOT NULL,
    description TEXT,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);




CREATE TABLE IF NOT EXISTS Employee_Loan (
    loan_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    loan_date TEXT NOT NULL,
    amount REAL NOT NULL,
    is_settled INTEGER DEFAULT 0, -- 0 = not paid, 1 = paid
    description TEXT,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);


CREATE VIEW IF NOT EXISTS View_Attendance_Report AS
SELECT 
    Employee.employee_name,
    Employee_Attendance.attendance_date,
    Employee_Attendance.status,
    Employee_Attendance.working_hours
FROM Employee_Attendance
JOIN Employee ON Employee_Attendance.employee_id = Employee.employee_id;




CREATE VIEW IF NOT EXISTS View_Employee_Loan_Report AS
SELECT 
    Employee.employee_name,
    Employee_Loan.loan_date,
    Employee_Loan.amount,
    Employee_Loan.is_settled
FROM Employee_Loan
JOIN Employee ON Employee_Loan.employee_id = Employee.employee_id;



INSERT INTO Employee (employee_name, phone_number, cnic, address, hire_date, designation_id, salary_type, salary_amount)
VALUES
('Zahid Khan', '03111222333', '11111-1111111-1', 'Peshawar', '2024-12-01', 1, 'monthly', 35000.00),
('Faisal Mehmood', '03211234567', '22222-2222222-2', 'Mardan', '2025-01-15', 2, 'daily', 1200.00),
('Rashid Ali', '03331234567', '33333-3333333-3', 'Lahore', '2025-02-01', 3, 'hourly', 250.00);




INSERT INTO Employee_Attendance (employee_id, attendance_date, status, working_hours)
VALUES
(1, '2025-07-01', 'present', 8),
(1, '2025-07-02', 'present', 8),
(2, '2025-07-01', 'absent', 0),
(2, '2025-07-02', 'present', 9),
(3, '2025-07-01', 'present', 7);



INSERT INTO Employee_Salary_Payment (employee_id, payment_date, salary_amount, description)
VALUES
(1, '2025-07-01', 35000.00, 'Monthly salary'),
(2, '2025-07-02', 1200.00, 'Daily wage'),
(3, '2025-07-02', 1750.00, 'Hourly wage - 7 hrs');



INSERT INTO Advance_Salary (employee_id, advance_date, amount, description)
VALUES
(1, '2025-06-20', 5000.00, 'Advance before Eid'),
(2, '2025-07-01', 2000.00, 'Emergency need');




INSERT INTO Employee_Loan (employee_id, loan_date, amount, is_settled, description)
VALUES
(1, '2025-03-10', 15000.00, 0, 'Personal Loan'),
(3, '2025-04-01', 10000.00, 1, 'Medical expense loan');





--REPORTS PART

CREATE VIEW IF NOT EXISTS View_Purchase_Report AS
SELECT 
    Raw_Purchase_Invoice.invoice_number,
    Raw_Purchase_Invoice.invoice_date,
    Supplier.supplier_name,
    Raw_Purchase_Invoice.total_amount,
    Raw_Purchase_Invoice.discount_amount,
    Raw_Purchase_Invoice.paid_amount
FROM Raw_Purchase_Invoice
JOIN Supplier ON Raw_Purchase_Invoice.supplier_id = Supplier.supplier_id;





CREATE VIEW IF NOT EXISTS View_Sales_Report AS
SELECT 
    Sales_Invoice.invoice_number,
    Sales_Invoice.invoice_date,
    Customer.customer_name,
    Sales_Invoice.total_amount,
    Sales_Invoice.discount_amount,
    Sales_Invoice.paid_amount
FROM Sales_Invoice
JOIN Customer ON Sales_Invoice.customer_id = Customer.customer_id;




CREATE VIEW IF NOT EXISTS View_Return_Purchase_Report AS
SELECT 
    Return_Purchase_Invoice.invoice_number,
    Return_Purchase_Invoice.invoice_date,
    Supplier.supplier_name,
    Return_Purchase_Invoice.total_amount,
    Return_Purchase_Invoice.discount_amount,
    Return_Purchase_Invoice.paid_amount
FROM Return_Purchase_Invoice
JOIN Supplier ON Return_Purchase_Invoice.supplier_id = Supplier.supplier_id;





CREATE VIEW IF NOT EXISTS View_Return_Sales_Report AS
SELECT 
    Return_Sales_Invoice.invoice_number,
    Return_Sales_Invoice.invoice_date,
    Customer.customer_name,
    Return_Sales_Invoice.total_amount,
    Return_Sales_Invoice.discount_amount,
    Return_Sales_Invoice.paid_amount
FROM Return_Sales_Invoice
JOIN Customer ON Return_Sales_Invoice.customer_id = Customer.customer_id;




CREATE VIEW IF NOT EXISTS View_Bank_Transfer_Report AS
SELECT 
    bt1.transaction_date,
    b1.bank_name AS from_bank,
    b2.bank_name AS to_bank,
    bt1.amount
FROM Bank_Transaction bt1
JOIN Bank b1 ON bt1.bank_id = b1.bank_id
JOIN Bank b2 ON bt1.related_bank_id = b2.bank_id
WHERE bt1.transaction_type = 'transfer_out';






CREATE VIEW IF NOT EXISTS View_Profit_Report AS
SELECT 
    Sales_Invoice.invoice_number,
    Sales_Invoice.invoice_date,
    Sales_Invoice.total_amount AS sale_amount,
    SUM(Sales_Invoice_Item.cost_price * Sales_Invoice_Item.quantity) AS cost_amount,
    Sales_Invoice.total_amount - 
    SUM(Sales_Invoice_Item.cost_price * Sales_Invoice_Item.quantity) AS profit
FROM Sales_Invoice
JOIN Sales_Invoice_Item ON Sales_Invoice.sales_invoice_id = Sales_Invoice_Item.sales_invoice_id
GROUP BY Sales_Invoice.sales_invoice_id;





-- Summary of purchases and sales (net)
CREATE VIEW IF NOT EXISTS View_Summary_Report AS
SELECT 
    (SELECT SUM(total_amount) FROM Raw_Purchase_Invoice) AS total_purchases,
    (SELECT SUM(total_amount) FROM Sales_Invoice) AS total_sales,
    (SELECT SUM(total_amount) FROM Return_Purchase_Invoice) AS total_purchase_returns,
    (SELECT SUM(total_amount) FROM Return_Sales_Invoice) AS total_sales_returns;

	
	
	
	
	CREATE VIEW IF NOT EXISTS View_Balance_Sheet AS
SELECT 
    (SELECT IFNULL(SUM(amount), 0) FROM Cash_Transaction 
     WHERE transaction_type IN ('cash_in', 'transfer_from_bank')) -
    (SELECT IFNULL(SUM(amount), 0) FROM Cash_Transaction 
     WHERE transaction_type IN ('cash_out', 'transfer_to_bank')) AS cash_balance,

    (SELECT IFNULL(SUM(amount), 0) FROM Bank_Transaction 
     WHERE transaction_type IN ('deposit', 'transfer_in')) -
    (SELECT IFNULL(SUM(amount), 0) FROM Bank_Transaction 
     WHERE transaction_type IN ('withdraw', 'transfer_out')) AS bank_balance;

	 
	 
	 
	 
	 
	 CREATE VIEW IF NOT EXISTS View_Customers_General_Report AS
SELECT 
    customer_name,
    phone_number,
    address
FROM Customer;





CREATE VIEW IF NOT EXISTS View_Suppliers_General_Report AS
SELECT 
    supplier_name,
    phone_number,
    address
FROM Supplier;





CREATE VIEW IF NOT EXISTS View_Area_Wise_Customer_Supplier_Report AS
SELECT 
    'Customer' AS party_type,
    customer_name AS name,
    Tehsil.tehsil_name,
    District.district_name,
    Province.province_name
FROM Customer
JOIN Tehsil ON Customer.tehsil_id = Tehsil.tehsil_id
JOIN District ON Tehsil.district_id = District.district_id
JOIN Province ON District.province_id = Province.province_id

UNION ALL

SELECT 
    'Supplier' AS party_type,
    supplier_name,
    Tehsil.tehsil_name,
    District.district_name,
    Province.province_name
FROM Supplier
JOIN Tehsil ON Supplier.tehsil_id = Tehsil.tehsil_id
JOIN District ON Tehsil.district_id = District.district_id
JOIN Province ON District.province_id = Province.province_id;





CREATE VIEW IF NOT EXISTS View_Brand_Wise_Salesman_Sales_Report AS
SELECT 
    Salesman.salesman_name,
    Brand.brand_name,
    SUM(Sales_Invoice_Item.quantity) AS total_quantity,
    SUM(Sales_Invoice_Item.total_price) AS total_sale
FROM Sales_Invoice
JOIN Salesman ON Sales_Invoice.salesman_id = Salesman.salesman_id
JOIN Sales_Invoice_Item ON Sales_Invoice.sales_invoice_id = Sales_Invoice_Item.sales_invoice_id
JOIN Production_Stock ON Sales_Invoice_Item.production_stock_id = Production_Stock.production_stock_id
JOIN Brand ON Production_Stock.brand_id = Brand.brand_id
GROUP BY Salesman.salesman_id, Brand.brand_id;







CREATE VIEW IF NOT EXISTS View_Brand_Wise_Profit_Report AS
SELECT 
    Brand.brand_name,
    SUM(Sales_Invoice_Item.total_price) AS total_sales,
    SUM(Sales_Invoice_Item.cost_price * Sales_Invoice_Item.quantity) AS total_cost,
    SUM(Sales_Invoice_Item.total_price) -
    SUM(Sales_Invoice_Item.cost_price * Sales_Invoice_Item.quantity) AS profit
FROM Sales_Invoice_Item
JOIN Production_Stock ON Sales_Invoice_Item.production_stock_id = Production_Stock.production_stock_id
JOIN Brand ON Production_Stock.brand_id = Brand.brand_id
GROUP BY Brand.brand_id;







CREATE VIEW IF NOT EXISTS View_Customer_Wise_Sales_Report AS
SELECT 
    Customer.customer_name,
    COUNT(Sales_Invoice.sales_invoice_id) AS total_invoices,
    SUM(Sales_Invoice.total_amount) AS total_sales
FROM Sales_Invoice
JOIN Customer ON Sales_Invoice.customer_id = Customer.customer_id
GROUP BY Customer.customer_id;






CREATE VIEW IF NOT EXISTS View_Supplier_Wise_Sales_Report AS
SELECT 
    Supplier.supplier_name,
    COUNT(Raw_Purchase_Invoice.raw_purchase_invoice_id) AS total_invoices,
    SUM(Raw_Purchase_Invoice.total_amount) AS total_supplied
FROM Raw_Purchase_Invoice
JOIN Supplier ON Raw_Purchase_Invoice.supplier_id = Supplier.supplier_id
GROUP BY Supplier.supplier_id;







CREATE VIEW IF NOT EXISTS View_Attendance_Report AS
SELECT 
    Employee.employee_name,
    Employee_Attendance.attendance_date,
    Employee_Attendance.status,
    Employee_Attendance.working_hours
FROM Employee_Attendance
JOIN Employee ON Employee_Attendance.employee_id = Employee.employee_id;



--SETTINGS PART 

CREATE TABLE IF NOT EXISTS User (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT DEFAULT 'user', -- e.g., admin, cashier, manager
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER DEFAULT 1
);





CREATE TABLE IF NOT EXISTS User_Log (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    login_time TEXT,
    logout_time TEXT,
    FOREIGN KEY (user_id) REFERENCES User(user_id)
);




-- Passwords are stored as plain text here for demo. In real apps, use SHA256 or bcrypt.
INSERT INTO User (username, password_hash, role) VALUES
('admin', 'admin123', 'admin'),
('cashier1', 'cash123', 'cashier'),
('manager1', 'manager123', 'manager');



