package com.cablemanagement.database;

import java.util.List;
import java.util.function.Supplier;

import com.cablemanagement.model.Brand;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Manufacturer;

public interface db {

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

    boolean customerExists(String name);

    // --------------------------
    // Supplier Operations
    // --------------------------
    List<Supplier> getAllSuppliers();

    boolean insertSupplier(String name, String contact) ;

    boolean supplierExists(String name) ;

    // --------------------------
    // Unit Operations
    // --------------------------
    List<String> getAllUnits() ;

    boolean insertUnit(String unitName) ;

    boolean unitExists(String unitName) ;

}
