package com.cablemanagement.views.pages;

import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.cablemanagement.config;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.cablemanagement.config;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Supplier;
import java.util.List;

public class AccountsContent {

    // Data model for table display
    public static class CustomerAccountData {
        private final String customerName;
        private final String contact;
        private final String balance;
        private final String tehsil;
        private final String district;
        private final String province;

        public CustomerAccountData(String customerName, String contact, String balance, 
                                 String tehsil, String district, String province) {
            this.customerName = customerName;
            this.contact = contact;
            this.balance = balance;
            this.tehsil = tehsil;
            this.district = district;
            this.province = province;
        }

        public String getCustomerName() { return customerName; }
        public String getContact() { return contact; }
        public String getBalance() { return balance; }
        public String getTehsil() { return tehsil; }
        public String getDistrict() { return district; }
        public String getProvince() { return province; }
    }

    public static class SupplierAccountData {
        private final String supplierName;
        private final String contact;
        private final String balance;
        private final String tehsil;
        private final String district;
        private final String province;

        public SupplierAccountData(String supplierName, String contact, String balance, 
                                 String tehsil, String district, String province) {
            this.supplierName = supplierName;
            this.contact = contact;
            this.balance = balance;
            this.tehsil = tehsil;
            this.district = district;
            this.province = province;
        }

        public String getSupplierName() { return supplierName; }
        public String getContact() { return contact; }
        public String getBalance() { return balance; }
        public String getTehsil() { return tehsil; }
        public String getDistrict() { return district; }
        public String getProvince() { return province; }
    }

    // Methods to get data with location details
    private static List<CustomerAccountData> getAllCustomersWithLocation() {
        List<CustomerAccountData> customers = new ArrayList<>();
        String query = "SELECT c.customer_name, c.contact_number, c.balance, " +
                      "COALESCE(t.tehsil_name, '') as tehsil_name, " +
                      "COALESCE(d.district_name, '') as district_name, " +
                      "COALESCE(p.province_name, '') as province_name " +
                      "FROM Customer c " +
                      "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                      "LEFT JOIN District d ON t.district_id = d.district_id " +
                      "LEFT JOIN Province p ON d.province_id = p.province_id " +
                      "ORDER BY c.customer_name";
        
        try (Connection conn = config.database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String name = rs.getString("customer_name");
                String contact = rs.getString("contact_number");
                double balance = config.database.getCustomerCurrentBalance(name);
                String tehsil = rs.getString("tehsil_name");
                String district = rs.getString("district_name");
                String province = rs.getString("province_name");
                
                customers.add(new CustomerAccountData(name, contact, 
                    String.format("%.2f", balance), tehsil, district, province));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    private static List<SupplierAccountData> getAllSuppliersWithLocation() {
        List<SupplierAccountData> suppliers = new ArrayList<>();
        String query = "SELECT s.supplier_name, s.contact_number, s.balance, " +
                      "COALESCE(t.tehsil_name, '') as tehsil_name, " +
                      "COALESCE(d.district_name, '') as district_name, " +
                      "COALESCE(p.province_name, '') as province_name " +
                      "FROM Supplier s " +
                      "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                      "LEFT JOIN District d ON t.district_id = d.district_id " +
                      "LEFT JOIN Province p ON d.province_id = p.province_id " +
                      "ORDER BY s.supplier_name";
        
        try (Connection conn = config.database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String name = rs.getString("supplier_name");
                String contact = rs.getString("contact_number");
                double balance = config.database.getSupplierCurrentBalance(name);
                String tehsil = rs.getString("tehsil_name");
                String district = rs.getString("district_name");
                String province = rs.getString("province_name");
                
                suppliers.add(new SupplierAccountData(name, contact, 
                    String.format("%.2f", balance), tehsil, district, province));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        // Title Section
        VBox titleSection = new VBox(10);
        titleSection.setPadding(new Insets(0, 0, 20, 0));
        titleSection.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("💰 Accounts Management");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label descriptionLabel = new Label("Manage customer and supplier account information");
        descriptionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        descriptionLabel.setStyle("-fx-text-fill: #7f8c8d;");

        titleSection.getChildren().addAll(titleLabel, descriptionLabel);

        // Create TabPane
        TabPane tabPane = new TabPane();
        tabPane.setPrefHeight(500);

        // Customer Account Tab
        Tab customerTab = new Tab("Customer Accounts");
        customerTab.setClosable(false);
        customerTab.setContent(createCustomerAccountsContent());

        // Supplier Account Tab
        Tab supplierTab = new Tab("Supplier Accounts");
        supplierTab.setClosable(false);
        supplierTab.setContent(createSupplierAccountsContent());

        tabPane.getTabs().addAll(customerTab, supplierTab);

        mainLayout.setTop(titleSection);
        mainLayout.setCenter(tabPane);

        return mainLayout;
    }

    private static VBox createCustomerAccountsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label heading = new Label("Customer Account Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        heading.setStyle("-fx-text-fill: #2c3e50;");

        // Search Row (removed filter and refresh)
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search customers...");
        searchField.setPrefWidth(300);

        searchRow.getChildren().addAll(new Label("Search:"), searchField);

        // Customer Table with real data
        TableView<CustomerAccountData> customerTable = new TableView<>();
        customerTable.setPrefHeight(350);
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        customerTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<CustomerAccountData, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setPrefWidth(200);
        nameCol.setMinWidth(150);
        nameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomerName()));
        
        TableColumn<CustomerAccountData, String> contactCol = new TableColumn<>("Contact");
        contactCol.setPrefWidth(150);
        contactCol.setMinWidth(120);
        contactCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContact()));
        
        TableColumn<CustomerAccountData, String> balanceCol = new TableColumn<>("Balance");
        balanceCol.setPrefWidth(130);
        balanceCol.setMinWidth(100);
        balanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBalance()));
        
        TableColumn<CustomerAccountData, String> tehsilCol = new TableColumn<>("Tehsil");
        tehsilCol.setPrefWidth(130);
        tehsilCol.setMinWidth(100);
        tehsilCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTehsil()));
        
        TableColumn<CustomerAccountData, String> districtCol = new TableColumn<>("District");
        districtCol.setPrefWidth(130);
        districtCol.setMinWidth(100);
        districtCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDistrict()));
        
        TableColumn<CustomerAccountData, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setPrefWidth(130);
        provinceCol.setMinWidth(100);
        provinceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProvince()));

        customerTable.getColumns().addAll(nameCol, contactCol, balanceCol, tehsilCol, districtCol, provinceCol);

        // Load real data from database
        ObservableList<CustomerAccountData> customerData = FXCollections.observableArrayList();
        try {
            customerData.addAll(getAllCustomersWithLocation());
        } catch (Exception e) {
            e.printStackTrace();
        }
        customerTable.setItems(customerData);

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                customerTable.setItems(customerData);
            } else {
                ObservableList<CustomerAccountData> filteredList = FXCollections.observableArrayList();
                for (CustomerAccountData customer : customerData) {
                    if (customer.getCustomerName().toLowerCase().contains(newValue.toLowerCase()) ||
                        customer.getContact().toLowerCase().contains(newValue.toLowerCase()) ||
                        customer.getTehsil().toLowerCase().contains(newValue.toLowerCase()) ||
                        customer.getDistrict().toLowerCase().contains(newValue.toLowerCase()) ||
                        customer.getProvince().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredList.add(customer);
                    }
                }
                customerTable.setItems(filteredList);
            }
        });

        // Action buttons below the table
        HBox actionButtonsRow = new HBox(15);
        actionButtonsRow.setAlignment(Pos.CENTER_LEFT);
        actionButtonsRow.setPadding(new Insets(15, 0, 0, 0));

        Button updateBtn = new Button("Update Customer");
        updateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16;");
        updateBtn.setPrefWidth(130);
        
        Button ledgerBtn = new Button("View Ledger");
        ledgerBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16;");
        ledgerBtn.setPrefWidth(130);
        
        Button paymentBtn = new Button("Add Payment");
        paymentBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16;");
        paymentBtn.setPrefWidth(130);

        // Initially disable buttons until a customer is selected
        updateBtn.setDisable(true);
        ledgerBtn.setDisable(true);
        paymentBtn.setDisable(true);

        // Enable/disable buttons based on selection
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            updateBtn.setDisable(!hasSelection);
            ledgerBtn.setDisable(!hasSelection);
            paymentBtn.setDisable(!hasSelection);
        });

        // Button actions
        updateBtn.setOnAction(e -> {
            CustomerAccountData selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                System.out.println("Update customer: " + selectedCustomer.getCustomerName());
                // Add your update logic here
            }
        });
        
        ledgerBtn.setOnAction(e -> {
            CustomerAccountData selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                System.out.println("View ledger for customer: " + selectedCustomer.getCustomerName());
                // Add your ledger logic here
            }
        });
        
        paymentBtn.setOnAction(e -> {
            CustomerAccountData selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                System.out.println("Add payment for customer: " + selectedCustomer.getCustomerName());
                // Add your payment logic here
            }
        });

        actionButtonsRow.getChildren().addAll(updateBtn, ledgerBtn, paymentBtn);

        // Placeholder data message when no customers exist
        if (customerData.isEmpty()) {
            Label noDataLabel = new Label("No customer data available. Add customers in the Register section.");
            noDataLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            customerTable.setPlaceholder(noDataLabel);
        }

        content.getChildren().addAll(heading, searchRow, customerTable, actionButtonsRow);
        return content;
    }

    private static VBox createSupplierAccountsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label heading = new Label("Supplier Account Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        heading.setStyle("-fx-text-fill: #2c3e50;");

        // Search Row (removed filter and refresh)
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search suppliers...");
        searchField.setPrefWidth(300);

        searchRow.getChildren().addAll(new Label("Search:"), searchField);

        // Supplier Table with real data
        TableView<SupplierAccountData> supplierTable = new TableView<>();
        supplierTable.setPrefHeight(350);
        supplierTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        supplierTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<SupplierAccountData, String> nameCol = new TableColumn<>("Supplier Name");
        nameCol.setPrefWidth(200);
        nameCol.setMinWidth(150);
        nameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSupplierName()));
        
        TableColumn<SupplierAccountData, String> contactCol = new TableColumn<>("Contact");
        contactCol.setPrefWidth(150);
        contactCol.setMinWidth(120);
        contactCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContact()));
        
        TableColumn<SupplierAccountData, String> balanceCol = new TableColumn<>("Balance");
        balanceCol.setPrefWidth(130);
        balanceCol.setMinWidth(100);
        balanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBalance()));
        
        TableColumn<SupplierAccountData, String> tehsilCol = new TableColumn<>("Tehsil");
        tehsilCol.setPrefWidth(130);
        tehsilCol.setMinWidth(100);
        tehsilCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTehsil()));
        
        TableColumn<SupplierAccountData, String> districtCol = new TableColumn<>("District");
        districtCol.setPrefWidth(130);
        districtCol.setMinWidth(100);
        districtCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDistrict()));
        
        TableColumn<SupplierAccountData, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setPrefWidth(130);
        provinceCol.setMinWidth(100);
        provinceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProvince()));

        supplierTable.getColumns().addAll(nameCol, contactCol, balanceCol, tehsilCol, districtCol, provinceCol);

        // Load real data from database
        ObservableList<SupplierAccountData> supplierData = FXCollections.observableArrayList();
        try {
            supplierData.addAll(getAllSuppliersWithLocation());
        } catch (Exception e) {
            e.printStackTrace();
        }
        supplierTable.setItems(supplierData);

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                supplierTable.setItems(supplierData);
            } else {
                ObservableList<SupplierAccountData> filteredList = FXCollections.observableArrayList();
                for (SupplierAccountData supplier : supplierData) {
                    if (supplier.getSupplierName().toLowerCase().contains(newValue.toLowerCase()) ||
                        supplier.getContact().toLowerCase().contains(newValue.toLowerCase()) ||
                        supplier.getTehsil().toLowerCase().contains(newValue.toLowerCase()) ||
                        supplier.getDistrict().toLowerCase().contains(newValue.toLowerCase()) ||
                        supplier.getProvince().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredList.add(supplier);
                    }
                }
                supplierTable.setItems(filteredList);
            }
        });

        // Action buttons below the table
        HBox actionButtonsRow = new HBox(15);
        actionButtonsRow.setAlignment(Pos.CENTER_LEFT);
        actionButtonsRow.setPadding(new Insets(15, 0, 0, 0));

        Button updateBtn = new Button("Update Supplier");
        updateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16;");
        updateBtn.setPrefWidth(130);
        
        Button ledgerBtn = new Button("View Ledger");
        ledgerBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16;");
        ledgerBtn.setPrefWidth(130);
        
        Button paymentBtn = new Button("Add Payment");
        paymentBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16;");
        paymentBtn.setPrefWidth(130);

        // Initially disable buttons until a supplier is selected
        updateBtn.setDisable(true);
        ledgerBtn.setDisable(true);
        paymentBtn.setDisable(true);

        // Enable/disable buttons based on selection
        supplierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            updateBtn.setDisable(!hasSelection);
            ledgerBtn.setDisable(!hasSelection);
            paymentBtn.setDisable(!hasSelection);
        });

        // Button actions
        updateBtn.setOnAction(e -> {
            SupplierAccountData selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier != null) {
                System.out.println("Update supplier: " + selectedSupplier.getSupplierName());
                // Add your update logic here
            }
        });
        
        ledgerBtn.setOnAction(e -> {
            SupplierAccountData selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier != null) {
                System.out.println("View ledger for supplier: " + selectedSupplier.getSupplierName());
                // Add your ledger logic here
            }
        });
        
        paymentBtn.setOnAction(e -> {
            SupplierAccountData selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier != null) {
                System.out.println("Add payment for supplier: " + selectedSupplier.getSupplierName());
                // Add your payment logic here
            }
        });

        actionButtonsRow.getChildren().addAll(updateBtn, ledgerBtn, paymentBtn);

        // Placeholder data message when no suppliers exist
        if (supplierData.isEmpty()) {
            Label noDataLabel = new Label("No supplier data available. Add suppliers in the Register section.");
            noDataLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            supplierTable.setPlaceholder(noDataLabel);
        }

        content.getChildren().addAll(heading, searchRow, supplierTable, actionButtonsRow);
        return content;
    }
}
