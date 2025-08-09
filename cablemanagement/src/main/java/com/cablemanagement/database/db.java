package com.cablemanagement.database;

import java.sql.Date;
import java.sql.ResultSet;
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

public interface db {
    
    /**
     * Get the database connection
     * @return The active database connection
     */
    java.sql.Connection getConnection();
    
    /**
     * Get the sales invoice ID by its invoice number
     * @param invoiceNumber The invoice number to look up
     * @return The sales invoice ID, or -1 if not found
     */
    int getSalesInvoiceIdByNumber(String invoiceNumber);
    
    /**
     * Get the sales return invoice ID by its invoice number
     * @param invoiceNumber The invoice number to look up
     * @return The sales return invoice ID, or -1 if not found
     */
    int getSalesReturnInvoiceIdByNumber(String invoiceNumber);

    /**
     * Get the items for a specific sales return invoice
     * @param returnInvoiceId The ID of the return invoice to get items for
     * @return A list of Object arrays containing the return invoice items
     */
    List<Object[]> getSalesReturnInvoiceItemsByInvoiceId(int returnInvoiceId);

    /**
     * Get all sales return invoices for dropdown selection
     * @return A list of Object arrays containing invoice data
     */

    // Add this method to support category deletion
    public boolean deleteCategory(String categoryName);
    public boolean deleteManufacturer(String name);
    public boolean deleteBrand(String name);
    public boolean deleteProvince(String provinceName);
    public boolean deleteDistrict(String districtName);
    public boolean deleteTehsil(String tehsilName);
    public boolean deleteUnit(String unitName);
    public boolean deleteCustomer(String name);
    public boolean deleteSupplier(String name);
    
    ///////////////////////////////////////////////////

    // Sign In
    String connect(String url, String user, String password);

    void disconnect();

    boolean isConnected();

    Object executeQuery(String query);

    int executeUpdate(String query);

    boolean SignIn(String userId, String password);

    // --------------------------
    // Tehsil Operations
    // --------------------------
    List<String> getAllTehsils();

    List<String> getTehsilsByDistrict(String districtName);

    boolean insertTehsil(String tehsilName, String districtName);

    boolean tehsilExists(String tehsilName);

    // --------------------------
    // District Operations
    // --------------------------
    List<String> getAllDistricts();

    List<String> getDistrictsByProvince(String provinceName);

    boolean insertDistrict(String districtName, String provinceName);

    // --------------------------
    // Province Operations
    // --------------------------
    List<String> getAllProvinces();

    boolean insertProvince(String provinceName);

    // --------------------------
    // Category Operations
    // --------------------------
    List<String> getAllCategories();

    boolean insertCategory(String categoryName);

    // --------------------------
    // Manufacturer Operations
    // --------------------------
    List<Manufacturer> getAllManufacturers();

    boolean insertManufacturer(String name, String province, String district, String tehsil);

    // --------------------------
    // Brand Operations
    // --------------------------
    List<Brand> getAllBrands();

    boolean insertBrand(String name, String province, String district, String tehsil);

    // --------------------------
    // Customer Operations
    // --------------------------
    List<Customer> getAllCustomers();

    boolean insertCustomer(String name, String contact, String tehsil, double balance);


    double getCustomerBalance(String customerName);

    double getCustomerCurrentBalance(String customerName);

    /**
     * Get invoice balance details for PDF generation
     * @param customerName Customer name
     * @param invoiceNumber Current invoice number
     * @param currentInvoiceTotal Current invoice total amount
     * @param currentInvoicePaid Current invoice paid amount
     * @return Object array with [previousBalance, totalBalance, netBalance]
     */
    Object[] getCustomerInvoiceBalanceDetails(String customerName, String invoiceNumber, 
                                           double currentInvoiceTotal, double currentInvoicePaid);

    // --------------------------
    // Supplier Operations
    // --------------------------
    List<Supplier> getAllSuppliers();


    boolean insertSupplier(String name, String contact, String tehsil, double balance);

    double getSupplierCurrentBalance(String supplierName);

    /**
     * Get supplier invoice balance details for PDF generation
     * @param supplierName Supplier name
     * @param invoiceNumber Current invoice number
     * @param currentInvoiceTotal Current invoice total amount
     * @param currentInvoicePaid Current invoice paid amount
     * @return Object array with [previousBalance, totalBalance, netBalance]
     */
    Object[] getSupplierInvoiceBalanceDetails(String supplierName, String invoiceNumber, 
                                           double currentInvoiceTotal, double currentInvoicePaid);

    // --------------------------
    // Supplier Update Operations
    // --------------------------
    
    /**
     * Update supplier details
     * @param supplierId The ID of the supplier to update
     * @param name New supplier name
     * @param contact New contact number
     * @param tehsilName New tehsil name
     * @return true if update successful, false otherwise
     */
    boolean updateSupplier(int supplierId, String name, String contact, String tehsilName);
    

    // --------------------------
    // Supplier Payment and Ledger Operations
    // --------------------------
    
    /**
     * Add payment for a supplier
     * @param supplierName Supplier name
     * @param paymentAmount Payment amount (positive for payment made)
     * @param paymentDate Payment date
     * @param description Payment description
     * @return true if payment added successfully, false otherwise
     */
    boolean addSupplierPayment(String supplierName, double paymentAmount, String paymentDate, String description);
    
    /**
     * Add payment for a supplier by ID
     * @param supplierId Supplier ID
     * @param paymentAmount Payment amount (positive for payment made)
     * @param paymentDate Payment date
     * @param description Payment description
     * @return true if payment added successfully, false otherwise
     */
    boolean addSupplierPayment(int supplierId, double paymentAmount, String paymentDate, String description);
    
    /**
     * Get supplier ledger (transaction history)
     * @param supplierName Supplier name
     * @return List of transaction records [date, type, amount, description, balance_after, reference]
     */
    List<Object[]> getSupplierLedger(String supplierName);
    
    /**
     * Get supplier ledger by ID
     * @param supplierId Supplier ID
     * @return List of transaction records [date, type, amount, description, balance_after, reference]
     */
    List<Object[]> getSupplierLedger(int supplierId);
    
    /**
     * Get supplier ledger for date range
     * @param supplierName Supplier name
     * @param startDate Start date
     * @param endDate End date
     * @return List of transaction records [date, type, amount, description, balance_after, reference]
     */
    List<Object[]> getSupplierLedgerByDateRange(String supplierName, String startDate, String endDate);

    // --------------------------
    // Customer Update Operations
    // --------------------------
    
    /**
     * Update customer details
     * @param customerId The ID of the customer to update
     * @param name New customer name
     * @param contact New contact number
     * @param tehsilName New tehsil name
     * @return true if update successful, false otherwise
     */
    boolean updateCustomer(int customerId, String name, String contact, String tehsilName);
    

    /**
     * Get customer ID by name
     * @param customerName The customer name
     * @return Customer ID, or -1 if not found
     */
    int getCustomerIdByName(String customerName);

    // --------------------------
    // Customer Payment and Ledger Operations
    // --------------------------
    
    /**
     * Add payment for a customer
     * @param customerName Customer name
     * @param paymentAmount Payment amount (positive for payment received)
     * @param paymentDate Payment date
     * @param description Payment description
     * @return true if payment added successfully, false otherwise
     */
    boolean addCustomerPayment(String customerName, double paymentAmount, String paymentDate, String description);
    
    /**
     * Add payment for a customer by ID
     * @param customerId Customer ID
     * @param paymentAmount Payment amount (positive for payment received)
     * @param paymentDate Payment date
     * @param description Payment description
     * @return true if payment added successfully, false otherwise
     */
    boolean addCustomerPayment(int customerId, double paymentAmount, String paymentDate, String description);
    
    /**
     * Get customer ledger (transaction history)
     * @param customerName Customer name
     * @return List of transaction records [date, type, amount, description, balance_after, reference]
     */
    List<Object[]> getCustomerLedger(String customerName);
    
    /**
     * Get customer ledger by ID
     * @param customerId Customer ID
     * @return List of transaction records [date, type, amount, description, balance_after, reference]
     */
    List<Object[]> getCustomerLedger(int customerId);
    
    /**
     * Get customer ledger for date range
     * @param customerName Customer name
     * @param startDate Start date
     * @param endDate End date
     * @return List of transaction records [date, type, amount, description, balance_after, reference]
     */
    List<Object[]> getCustomerLedgerByDateRange(String customerName, String startDate, String endDate);

    // --------------------------
    // Unit Operations
    // --------------------------
    List<String> getAllUnits() ;

    boolean insertUnit(String unitName) ;

    // --------------------------
    // User Management Operations
    // --------------------------
    boolean insertUser(String username, String password, String role);
    
    boolean userExists(String username);
    
    boolean changePassword(String username, String oldPassword, String newPassword);
    
    List<String> getAllUsers(); 

    // --------------------------
    // Raw Stock Operations
    // --------------------------
    List<Object[]> getAllRawStocks();
    
    boolean insertRawStock(String name, String category, String brand, String unit, double openingQty, double purchasePrice, double reorderLevel);
    
    boolean updateRawStock(Integer id, String name, String brand, String unit, double quantity, double unitPrice);
    
    // --------------------------
    // Production Stock Operations
    // --------------------------
    List<Object[]> getAllProductionStocks();
    
    boolean insertProductionStock(String name, String category, String brand, String unit, double openingQty, double salePrice, double reorderLevel);
    
    // New method with separate unit cost and sale price parameters
    boolean insertProductionStock(String name, String category, String brand, String unit, 
                                 double openingQty, double unitCost, double salePrice, double reorderLevel);
    
    List<Object[]> getAllRawPurchaseInvoices();
    
    List<Object[]> getAllRawStocksForDropdown();
    
    List<String> getAllSupplierNames();
    
    int getRawStockIdByName(String rawStockName);
    
    Object[] getSupplierDetails(String supplierName);
    
    // Simplified invoice methods that work with existing tables
    boolean insertSimpleRawPurchaseInvoice(String invoiceNumber, String supplierName, String invoiceDate, 
                                          double totalAmount, double discountAmount, double paidAmount, 
                                          List<RawStockPurchaseItem> items);

    // --------------------------
    // Raw Purchase Return Invoice Operations
    // --------------------------
    String generateReturnInvoiceNumber();
    
    List<Object[]> getAllRawPurchaseInvoicesForDropdown();

    int insertRawPurchaseReturnInvoiceAndGetId(String returnInvoiceNumber, int originalInvoiceId, 
                                             int supplierId, String returnDate, double totalReturnAmount);
    
    boolean insertRawPurchaseReturnInvoiceItems(int returnInvoiceId, List<RawStockPurchaseItem> items);
    
    double getCurrentRawStockQuantity(int stockId);

    // --------------------------
    // Raw Stock Use Invoice Operations
    // --------------------------
    String generateUseInvoiceNumber();
    
    List<Object[]> getAllRawStocksWithUnitsForDropdown();
    
    int insertRawStockUseInvoiceAndGetId(String useInvoiceNumber, String usageDate, 
                                        double totalUsageAmount, String referencePurpose);
    
    boolean insertRawStockUseInvoiceItems(int useInvoiceId, List<RawStockUseItem> items);
    
    List<Object[]> getAllRawStockUseInvoices();
    
    List<Object[]> getRawStockUsageReportByDateRange(String startDate, String endDate);
    
    Object[] getUsageSummaryStatistics(String startDate, String endDate);
    
    List<Object[]> getRawStockUsageDetails(String startDate, String endDate);

    // --------------------------
    // Production Invoice Operations
    // --------------------------
    List<Object[]> getAllProductionStocksForDropdown();
    
    String generateProductionInvoiceNumber();
    
    int insertProductionInvoiceAndGetId(String productionDate, String notes);
    
    boolean insertProductionInvoiceItems(int productionInvoiceId, List<Object[]> productionItems);

    boolean insertProductionStockRawUsage(int productionInvoiceId, List<Object[]> rawMaterialsUsed);

    // --------------------------
    // Production Stock Operations  
    // --------------------------
    List<Object[]> getAllProductionInvoices();
    
    List<Object[]> getAllSalesInvoices();

    // --------------------------
    // Sales Invoice Operations
    // --------------------------
    String generateSalesInvoiceNumber();
    
    List<Object[]> getAllCustomersForDropdown();
    
    List<Object[]> getAllProductionStocksWithPriceForDropdown();
    
    int getProductionStockIdByName(String productName);
    
    // Check if production stock exists with given name and brand
    boolean productionStockExists(String productName, String brandName);
    
    // Add quantity to existing production stock
    boolean addToProductionStock(String productName, String brandName, int addedQuantity, double unitCost, double salePrice);
    

    boolean insertSalesInvoiceItems(int salesInvoiceId, List<Object[]> items);
    
    boolean insertSalesInvoice(String invoiceNumber, int customerId, String salesDate, 
                              double totalAmount, double discountAmount, double paidAmount, 
                              List<Object[]> items);

    // --------------------------
    // Sales Return Invoice Operations
    // --------------------------
    String generateSalesReturnInvoiceNumber();
    
    List<Object[]> getAllSalesInvoicesForDropdown();
    
    List<Object[]> getSalesInvoiceItemsByInvoiceId(int salesInvoiceId);
    
    Object[] getSalesInvoiceById(int salesInvoiceId);
    

    boolean insertSalesReturnInvoice(String returnInvoiceNumber, int originalSalesInvoiceId, 
                                    int customerId, String returnDate, double totalReturnAmount, 
                                    List<Object[]> items, boolean updateBalance);
    
    // Overloaded method for backward compatibility
    boolean insertSalesReturnInvoice(String returnInvoiceNumber, int originalSalesInvoiceId, 
                                    int customerId, String returnDate, double totalReturnAmount, 
                                    List<Object[]> items);
    
    List<Object[]> getAllSalesReturnInvoices();

    // --------------------------
    // Bank Management Operations
    // --------------------------
    List<Object[]> getAllBanks();
    
    boolean insertBank(String bankName, String accountNumber, String branchName);
    
    List<Object[]> getAllBankTransactions();
    
    List<Object[]> getAllCashTransactions();
    
    double getCurrentCashBalance();

    // --------------------------
    // Employee Management Operations
    // --------------------------
    List<Object[]> getAllEmployees();
    
    boolean insertEmployee(String name, String phone, String cnic, String address, String designation, String salaryType, double salaryAmount);
    
    boolean updateEmployee(int employeeId, String name, String phone, String cnic, String address, String designation, String salaryType, double salaryAmount);
    
    boolean deleteEmployee(int employeeId);
    
    // --------------------------
    // Employee Attendance Operations
    // --------------------------
    boolean insertEmployeeAttendance(int employeeId, String attendanceDate, String status, double workingHours);
    
    List<Object[]> getAllEmployeeAttendance();
    
    List<Object[]> getEmployeeAttendanceByDateRange(String startDate, String endDate);

    int getEmployeeIdByName(String employeeName);
    

    List<Object[]> getAllEmployeeLoans();

    // --------------------------
    // Designation Operations
    // --------------------------
    List<Object[]> getAllDesignations();
    
    boolean insertDesignation(String designationTitle);
    
    boolean updateDesignation(int designationId, String designationTitle);
    
    boolean deleteDesignation(int designationId);

    // --------------------------
    // Salesman Operations
    // --------------------------
    List<Object[]> getAllSalesmen();
    
    boolean insertSalesman(String name, String contact, String address, double commissionRate);
    
    boolean updateSalesman(int salesmanId, String name, String contact, String address, double commissionRate);

    boolean updateBank(Bank bank);

    boolean deleteBank(int bankId);

    boolean insertBankTransaction(BankTransaction transaction);

    boolean insertCashTransaction(BankTransaction transaction);

    boolean updateCashTransaction(BankTransaction transaction);

    boolean deleteCashTransaction(int transactionId);

    double getCashBalance();

    boolean updateBankBalance(double newBalance);  // TODO: remove

    List<Object[]> getViewData(String viewName, Map<String, String> filters);
    List<Object[]> getInvoiceItemsByID(Integer invoiceID);
    List<Object[]> getAllRawStock();
    List<Object[]> getAllProductionStock();
    ///////////////////////////////////////////////////////////////////////////////
    /// ///                   reports Methods
    ///////////////////////////////////////////////////////////////////////////////
    ResultSet getPurchaseReport(Date fromDate, Date toDate, String reportType); //checked by Umer Ghafoor
    ResultSet getSalesReport(Date fromDate, Date toDate, String reportType); //checked by Umer Ghafoor
    ResultSet getReturnPurchaseReport(Date fromDate, Date toDate, String reportType);//checked by Umer Ghafoor
    ResultSet getReturnSalesReport(Date fromDate, Date toDate, String reportType);//checked by Umer Ghafoor
    ResultSet getBankTransferReport(Date fromDate, Date toDate);
    ResultSet getProfitReport(Date fromDate, Date toDate);
    ResultSet getSummaryReport(Date fromDate, Date toDate);
    ResultSet getBalanceSheet(); // Checked by Sani
    Object[] getBalanceSheetData(); // Checked by Sani
    ResultSet getCustomersReport();
    ResultSet getSuppliersReport();
    ResultSet getAreaWiseReport();
    ResultSet getAreaWiseReport(String partyType, String areaType, String areaValue);
    ResultSet getBrandSalesReport(Date fromDate, Date toDate);

    String generateNextInvoiceNumber(String string);

    int getSupplierIdByName(String supplierName);

    /**
     * Get unit_id by unit name
     */
    int getUnitIdByName(String unitName);
}

