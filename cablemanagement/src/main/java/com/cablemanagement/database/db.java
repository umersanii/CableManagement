package com.cablemanagement.database;

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


    ///////////////////////////////////////////////////
    /// Uncategorized, Errors fix krne k lye dale hain
    /// Fazal inko implement krna ha
    /// /////////////////////////////////////////////////
    /// 
    


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

    boolean manufacturerExists(String name);

    // --------------------------
    // Brand Operations
    // --------------------------
    List<Brand> getAllBrands();

    boolean insertBrand(String name, String province, String district, String tehsil);

    boolean brandExists(String name);

    // --------------------------
    // Customer Operations
    // --------------------------
    List<Customer> getAllCustomers();

    boolean insertCustomer(String name, String contact);
    
    boolean insertCustomer(String name, String contact, String tehsil);

    boolean customerExists(String name);

    // --------------------------
    // Supplier Operations
    // --------------------------
    List<Supplier> getAllSuppliers();

    boolean insertSupplier(String name, String contact) ;
    
    boolean insertSupplier(String name, String contact, String tehsil);

    boolean supplierExists(String name) ;

    // --------------------------
    // Unit Operations
    // --------------------------
    List<String> getAllUnits() ;

    boolean insertUnit(String unitName) ;

    boolean unitExists(String unitName) ;

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
    
    List<Object[]> getAllRawPurchaseInvoices();
    
    boolean insertRawPurchaseInvoice(String invoiceNumber, int supplierId, String invoiceDate, 
                                   double totalAmount, double discountAmount, double paidAmount);
    
    int insertRawPurchaseInvoiceAndGetId(String invoiceNumber, int supplierId, String invoiceDate, 
                                       double totalAmount, double discountAmount, double paidAmount);
    
    List<Object[]> getAllRawStockUsage();
    
    // New methods for enhanced invoice functionality
    String generateNextInvoiceNumber(String prefix);
    
    List<Object[]> getAllRawStocksForDropdown();
    
    List<String> getAllSupplierNames();
    
    int getSupplierIdByName(String supplierName);
    
    int getRawStockIdByName(String rawStockName);
    
    // Simplified invoice methods that work with existing tables
    boolean insertSimpleRawPurchaseInvoice(String invoiceNumber, String supplierName, String invoiceDate, 
                                          double totalAmount, double discountAmount, double paidAmount, 
                                          List<RawStockPurchaseItem> items);

    boolean ensureBrandExists(String brandName, int tehsilId);

    // --------------------------
    // Raw Purchase Return Invoice Operations
    // --------------------------
    String generateReturnInvoiceNumber();
    
    List<Object[]> getAllRawPurchaseInvoicesForDropdown();
    
    List<Object[]> getRawStockItemsByInvoiceId(int invoiceId);
    
    int insertRawPurchaseReturnInvoiceAndGetId(String returnInvoiceNumber, int originalInvoiceId, 
                                             int supplierId, String returnDate, double totalReturnAmount);
    
    boolean insertRawPurchaseReturnInvoiceItems(int returnInvoiceId, List<RawStockPurchaseItem> items);
    
    List<Object[]> getAllRawPurchaseReturnInvoices();

    // --------------------------
    // Raw Stock Use Invoice Operations
    // --------------------------
    String generateUseInvoiceNumber();
    
    List<Object[]> getAllRawStocksWithUnitsForDropdown();
    
    int insertRawStockUseInvoiceAndGetId(String useInvoiceNumber, String usageDate, 
                                        double totalUsageAmount, String referencePurpose);
    
    boolean insertRawStockUseInvoiceItems(int useInvoiceId, List<RawStockUseItem> items);
    
    List<Object[]> getAllRawStockUseInvoices();

    // --------------------------
    // Production Stock Operations  
    // --------------------------
    List<Object[]> getAllProductionStocks();
    
    List<Object[]> getAllProductionInvoices();
    
    List<Object[]> getAllSalesInvoices();

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
    
    List<Object[]> getEmployeeAttendanceByEmployee(int employeeId);
    
    int getEmployeeIdByName(String employeeName);
    
    // --------------------------
    // Employee Salary Payment Operations
    // --------------------------
    List<Object[]> getAllEmployeeSalaryPayments();
    
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

double getCashBalance();

boolean updateBankBalance(double newBalance);

List<Object[]> getViewData(String viewName, Map<String, String> filters);

 List<Object[]> getAllRawStock();
    List<Object[]> getAllProductionStock();
}