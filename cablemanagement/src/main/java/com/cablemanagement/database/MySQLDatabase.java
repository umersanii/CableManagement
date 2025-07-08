package com.cablemanagement.database;

import java.util.List;
import java.util.function.Supplier;

import com.cablemanagement.model.Brand;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Manufacturer;

public class MySQLDatabase implements db {

    @Override
    public String connect(String url, String user, String password) {
        // Implement here
        return "null";
    }

    @Override
    public void disconnect() {
        // Implement here
    }

    @Override
    public boolean isConnected() {
        // Implement here
        return true;
    }

    @Override
    public Object executeQuery(String query) {

        return null;
    }

    @Override
    public int executeUpdate(String query) {
        

        return -1;
    }

    @Override
    public boolean SignIn(String userId, String password) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'SignIn'");
        // return false; // This line is unreachable after the exception
        return true;
    }

    @Override
    public List<String> getAllTehsils() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllTehsils'");
    }

    @Override
    public List<String> getTehsilsByDistrict(String districtName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTehsilsByDistrict'");
    }

    @Override
    public boolean insertTehsil(String tehsilName, String districtName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertTehsil'");
    }

    @Override
    public boolean tehsilExists(String tehsilName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tehsilExists'");
    }

    @Override
    public List<String> getAllDistricts() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllDistricts'");
    }

    @Override
    public List<String> getDistrictsByProvince(String provinceName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDistrictsByProvince'");
    }

    @Override
    public boolean insertDistrict(String districtName, String provinceName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertDistrict'");
    }

    @Override
    public List<String> getAllProvinces() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllProvinces'");
    }

    @Override
    public boolean insertProvince(String provinceName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertProvince'");
    }

    @Override
    public List<String> getAllCategories() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllCategories'");
    }

    @Override
    public boolean insertCategory(String categoryName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertCategory'");
    }

    @Override
    public List<Manufacturer> getAllManufacturers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllManufacturers'");
    }

    @Override
    public boolean insertManufacturer(String name, String province, String district, String tehsil) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertManufacturer'");
    }

    @Override
    public boolean manufacturerExists(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'manufacturerExists'");
    }

    @Override
    public List<Brand> getAllBrands() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllBrands'");
    }

    @Override
    public boolean insertBrand(String name, String province, String district, String tehsil) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertBrand'");
    }

    @Override
    public boolean brandExists(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'brandExists'");
    }

    @Override
    public List<Customer> getAllCustomers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllCustomers'");
    }

    @Override
    public boolean insertCustomer(String name, String contact) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertCustomer'");
    }

    @Override
    public boolean customerExists(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'customerExists'");
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllSuppliers'");
    }

    @Override
    public boolean insertSupplier(String name, String contact) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertSupplier'");
    }

    @Override
    public boolean supplierExists(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'supplierExists'");
    }

    @Override
    public List<String> getAllUnits() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllUnits'");
    }

    @Override
    public boolean insertUnit(String unitName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertUnit'");
    }

    @Override
    public boolean unitExists(String unitName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unitExists'");
    }
}
