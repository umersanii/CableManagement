package com.cablemanagement.views.pages;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.cablemanagement.config;

public class ReportsContent {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane reportArea = new StackPane();
        reportArea.getChildren().add(createReportsList());

        HBox buttonBar = createButtonBar(reportArea);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        configureScrollPane(scrollPane);

        mainLayout.setTop(scrollPane);
        mainLayout.setCenter(reportArea);

        return mainLayout;
    }

    private static HBox createButtonBar(StackPane reportArea) {
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        String[] buttonLabels = {
            "Purchase Report",
            "Sales Report",
            "Return Purchase Report",
            "Return Sales Report",
            "Bank Transfer Report",
            "Profit Report",
            // "Summary Report",
            "Balance Sheet",
            "Customers Report",
            "Suppliers Report",
            "Area-Wise Report",
            "Brand Sales Report",
            // "Brand Profit Report",
            // "Customer Sales Report",
            // "Supplier Sales Report",
            // "Attendance Report"
        };

        Runnable[] actions = {
            () -> reportArea.getChildren().setAll(createPurchaseReport()),
            () -> reportArea.getChildren().setAll(createSalesReport()),
            () -> reportArea.getChildren().setAll(createReturnPurchaseReport()),
            () -> reportArea.getChildren().setAll(createReturnSalesReport()),
            () -> reportArea.getChildren().setAll(createBankTransferReport()),
            () -> reportArea.getChildren().setAll(createProfitReport()),
            // () -> reportArea.getChildren().setAll(createSummaryReport()),
            () -> reportArea.getChildren().setAll(createBalanceSheet()),
            () -> reportArea.getChildren().setAll(createCustomersReport()),
            () -> reportArea.getChildren().setAll(createSuppliersReport()),
            () -> reportArea.getChildren().setAll(createAreaWiseReport()),
            () -> reportArea.getChildren().setAll(createBrandSalesReport()),
            // () -> reportArea.getChildren().setAll(createBrandProfitReport()),
            // () -> reportArea.getChildren().setAll(createCustomerSalesReport()),
            // () -> reportArea.getChildren().setAll(createSupplierSalesReport()),
            // () -> reportArea.getChildren().setAll(createAttendanceReport())
        };

        for (int i = 0; i < buttonLabels.length; i++) {
            addButton(buttonBar, buttonLabels[i], actions[i]);
        }

        return buttonBar;
    }

    private static void configureScrollPane(ScrollPane scrollPane) {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(72);
        scrollPane.setMinHeight(72);
        scrollPane.setMaxHeight(72);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    }

    private static void addButton(HBox bar, String label, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("register-button");
        btn.setOnAction(e -> action.run());
        bar.getChildren().add(btn);
    }

    private static VBox createReportsList() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Reports Dashboard");
        Label info = new Label("Select a report from the toolbar above to view detailed information");
        info.setStyle("-fx-font-size: 14; -fx-text-fill: #555;");

        form.getChildren().addAll(heading, info);
        return form;
    }

    private static VBox createPurchaseReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Purchase Report");

        // Report type dropdown
        HBox reportTypeBox = new HBox(10);
        Label reportLabel = new Label("Select Report:");
        ComboBox<String> reportComboBox = new ComboBox<>();
        reportComboBox.getItems().addAll(
            "All Reports",
            "Product-wise Report",
            "Category-wise Report",
            "Brand-wise Report",
            "Manufacturer-wise Report"
        );
        reportComboBox.setValue("All Reports");
        reportTypeBox.getChildren().addAll(reportLabel, reportComboBox);
        reportTypeBox.setAlignment(Pos.CENTER_LEFT);

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Purchase report table
        TableView<Map<String, String>> table = new TableView<>();

        TableColumn<Map<String, String>, String> invCol = new TableColumn<>("Invoice #");
        invCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Invoice #", "")));

        TableColumn<Map<String, String>, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Date", "")));

        TableColumn<Map<String, String>, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Supplier", "")));

        TableColumn<Map<String, String>, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Amount", "")));

        TableColumn<Map<String, String>, String> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Discount", "")));

        TableColumn<Map<String, String>, String> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Paid", "")));

        table.getColumns().add(invCol);
        table.getColumns().add(dateCol);
        table.getColumns().add(supplierCol);
        table.getColumns().add(amountCol);
        table.getColumns().add(discountCol);
        table.getColumns().add(paidCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load data on filter
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            table.getColumns().clear();
            errorLabel.setText("");

            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    String selectedReport = reportComboBox.getValue();

                    ResultSet rs = config.database.getPurchaseReport(from, to, selectedReport);
                    if (rs != null && rs.next()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        // Create columns dynamically
                        for (int i = 1; i <= columnCount; i++) {
                            final String colName = metaData.getColumnLabel(i);
                            TableColumn<Map<String, String>, String> col = new TableColumn<>(colName);
                            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(colName, "")));
                            table.getColumns().add(col);
                        }

                        // Remove rs.beforeFirst(); because SQLite ResultSet is TYPE_FORWARD_ONLY

                        // Fill table with data
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                        // The first rs.next() above already moved to the first row, so process that row first
                        do {
                            Map<String, String> row = new HashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.put(metaData.getColumnLabel(i), rs.getString(i));
                            }
                            data.add(row);
                        } while (rs.next());

                        table.setItems(data);
                    } else {
                        errorLabel.setText("No data found for selected filters.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading data: " + ex.getMessage());
            }
        });

        // Trigger filter once on load
        filterBtn.fire();

        form.getChildren().addAll(heading, reportTypeBox, dateRangeBox, buttons, errorLabel, table);
        return form;
    }

    private static VBox createSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Sales Report");

        HBox reportTypeBox = new HBox(10);
        Label reportLabel = new Label("Select Report:");
        ComboBox<String> reportComboBox = new ComboBox<>();
        reportComboBox.getItems().addAll(
            "All Reports",
            "Product-wise Report",
            "Category-wise Report",
            "Brand-wise Report",
            "Manufacturer-wise Report"
        );
        reportComboBox.setValue("All Reports"); // default
        reportTypeBox.getChildren().addAll(reportLabel, reportComboBox);
        reportTypeBox.setAlignment(Pos.CENTER_LEFT);

        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        HBox buttons = createReportActionButtons();

        TableView<ObservableList<String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            table.getColumns().clear();
            errorLabel.setText("");

            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    String selectedReport = reportComboBox.getValue();

                    ResultSet rs = config.database.getSalesReport(from, to, selectedReport);

                    if (rs != null) {
                        ResultSetMetaData meta = rs.getMetaData();
                        int columnCount = meta.getColumnCount();

                        // Auto-create columns
                        for (int i = 1; i <= columnCount; i++) {
                            final int colIndex = i;
                            TableColumn<ObservableList<String>, String> col =
                                new TableColumn<>(meta.getColumnLabel(i));
                            col.setCellValueFactory(data ->
                                new SimpleStringProperty(data.getValue().get(colIndex - 1))
                            );
                            table.getColumns().add(col);
                        }

                        // Add rows
                        while (rs.next()) {
                            ObservableList<String> row = FXCollections.observableArrayList();
                            for (int i = 1; i <= columnCount; i++) {
                                row.add(rs.getString(i));
                            }
                            table.getItems().add(row);
                        }

                        if (table.getItems().isEmpty()) {
                            errorLabel.setText("No data found for selected filters.");
                        }
                    } else {
                        errorLabel.setText("No data returned from query.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading sales data: " + ex.getMessage());
            }
        });

        filterBtn.fire();

        form.getChildren().addAll(heading, reportTypeBox, dateRangeBox, buttons, errorLabel, table);
        return form;
    }

private static VBox createReturnPurchaseReport() {
    VBox form = new VBox(15);
    form.setPadding(new Insets(20));
    form.getStyleClass().add("form-container");

    Label heading = createHeading("Return Purchase Report");

    // Report type dropdown
    HBox reportTypeBox = new HBox(10);
    Label reportLabel = new Label("Select Report:");
    ComboBox<String> reportComboBox = new ComboBox<>();
    reportComboBox.getItems().addAll(
        "All Reports",
        "Product-wise Report",
        "Category-wise Report",
        "Brand-wise Report",
        "Manufacturer-wise Report"
    );
    reportComboBox.setValue("All Reports");
    reportTypeBox.getChildren().addAll(reportLabel, reportComboBox);
    reportTypeBox.setAlignment(Pos.CENTER_LEFT);

    // Date range filters
    HBox dateRangeBox = new HBox(10);
    Label fromLabel = new Label("From:");
    DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
    Label toLabel = new Label("To:");
    DatePicker toDatePicker = new DatePicker(LocalDate.now());
    Button filterBtn = createActionButton("Filter");
    dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
    dateRangeBox.setAlignment(Pos.CENTER_LEFT);

    // Action buttons
    HBox buttons = createReportActionButtons();

    // Table (dynamic)
    TableView<Map<String, String>> table = new TableView<>();
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Error label
    Label errorLabel = new Label("");
    errorLabel.setStyle("-fx-text-fill: red;");

    // Load data
    filterBtn.setOnAction(e -> {
        table.getItems().clear();
        table.getColumns().clear();
        errorLabel.setText("");

        try {
            if (config.database != null && config.database.isConnected()) {
                java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                String selectedReport = reportComboBox.getValue();

                ResultSet rs = config.database.getReturnPurchaseReport(from, to, selectedReport); // Make sure backend handles report type

                if (rs != null && rs.next()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // Create columns dynamically
                    for (int i = 1; i <= columnCount; i++) {
                        final String colName = metaData.getColumnLabel(i);
                        TableColumn<Map<String, String>, String> col = new TableColumn<>(colName);
                        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(colName, "")));
                        table.getColumns().add(col);
                    }

                    // Fill data
                    ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                    
                    // The first rs.next() above already moved to the first row, so process that row first
                    do {
                        Map<String, String> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnLabel(i), rs.getString(i));
                        }
                        data.add(row);
                    } while (rs.next());

                    table.setItems(data);
                } else {
                    errorLabel.setText("No data found for selected filters.");
                }
            } else {
                errorLabel.setText("Database not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error loading data: " + ex.getMessage());
        }
    });

    // Trigger load on start
    filterBtn.fire();

    form.getChildren().addAll(heading, reportTypeBox, dateRangeBox, buttons, errorLabel, table);
    return form;
}

    private static VBox createReturnSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Return Sales Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Return sales report table - maps to Sales_Return_Invoice table
        TableView<ReturnSalesReport> table = new TableView<>();
        
        TableColumn<ReturnSalesReport, String> invCol = new TableColumn<>("Invoice #");
        invCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        
        TableColumn<ReturnSalesReport, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        
        TableColumn<ReturnSalesReport, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        
        TableColumn<ReturnSalesReport, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        table.getColumns().addAll(invCol, dateCol, customerCol, amountCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load data from backend
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    java.sql.ResultSet rs = config.database.getReturnSalesReport(from, to);
                    int count = 0;
                    while (rs != null && rs.next()) {
                        // Format the amount as string with proper formatting
                        String totalAmount = String.format("%.2f", rs.getDouble("total_return_amount"));
                        
                        table.getItems().add(new ReturnSalesReport(
                            rs.getString("return_invoice_number"),
                            rs.getString("return_date"),
                            rs.getString("customer_name"),
                            totalAmount
                        ));
                        count++;
                    }
                    System.out.println("ReturnSalesReport rows loaded: " + count);
                    if (count == 0) {
                        errorLabel.setText("No return sales data found for selected date range.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading return sales data: " + ex.getMessage());
            }
        });

        // Optionally, trigger filter on load
        filterBtn.fire();

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel, table);
        return form;
    }

    private static VBox createBankTransferReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Bank Transfer Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Bank transfer report table - maps to View_Bank_Transfer_Report
        TableView<BankTransferReport> table = new TableView<>();
        
        TableColumn<BankTransferReport, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        
        TableColumn<BankTransferReport, String> fromCol = new TableColumn<>("From Bank");
        fromCol.setCellValueFactory(new PropertyValueFactory<>("fromBank"));
        
        TableColumn<BankTransferReport, String> toCol = new TableColumn<>("To Bank");
        toCol.setCellValueFactory(new PropertyValueFactory<>("toBank"));
        
        TableColumn<BankTransferReport, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        table.getColumns().addAll(dateCol, fromCol, toCol, amountCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load data from backend
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    java.sql.ResultSet rs = config.database.getBankTransferReport(from, to);
                    int count = 0;
                    if (rs != null) {
                        try {
                            while (rs.next()) {
                                String fromBank = rs.getString("from_bank");
                                String toBank = rs.getString("to_bank");
                                String amount = String.format("%.2f", rs.getDouble("amount"));
                                String date = rs.getString("transaction_date");
                                
                                // Handle null values
                                if (fromBank == null) fromBank = "Unknown";
                                if (toBank == null) toBank = "Unknown";
                                if (date == null) date = "Unknown";
                                
                                table.getItems().add(new BankTransferReport(date, fromBank, toBank, amount));
                                count++;
                            }
                        } finally {
                            rs.close();
                        }
                    }
                    System.out.println("BankTransferReport rows loaded: " + count);
                    if (count == 0) {
                        errorLabel.setText("No bank transfer data found for selected date range.\n" +
                                         "Bank transfers are created when you use 'Transfer Bank to Bank' in Bank Management.\n" +
                                         "Only transactions with types 'transfer_in' and 'transfer_out' appear in this report.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading bank transfer data: " + ex.getMessage());
            }
        });

        // Optionally, trigger filter on load
        filterBtn.fire();

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel, table);
        return form;
    }

    private static VBox createProfitReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Profit Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Profit report table - maps to Sales_Invoice joined with Sales_Invoice_Item and ProductionStock
        TableView<ProfitReport> table = new TableView<>();
        
        TableColumn<ProfitReport, String> invCol = new TableColumn<>("Invoice #");
        invCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        
        TableColumn<ProfitReport, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        
        TableColumn<ProfitReport, String> saleCol = new TableColumn<>("Sale Amount");
        saleCol.setCellValueFactory(new PropertyValueFactory<>("saleAmount"));
        
        TableColumn<ProfitReport, String> costCol = new TableColumn<>("Cost Amount");
        costCol.setCellValueFactory(new PropertyValueFactory<>("costAmount"));
        
        TableColumn<ProfitReport, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));
        
        table.getColumns().addAll(invCol, dateCol, saleCol, costCol, profitCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load data from backend
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                System.out.println("DEBUG: Profit Report - Loading data...");
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    
                    System.out.println("DEBUG: Date range: " + from + " to " + to);
                    
                    java.sql.ResultSet rs = config.database.getProfitReport(from, to);
                    int count = 0;
                    while (rs != null && rs.next()) {
                        String invoiceNumber = rs.getString("sales_invoice_number");
                        String invoiceDate = rs.getString("sales_date");
                        double saleAmount = rs.getDouble("sale_amount");
                        double costAmount = rs.getDouble("cost_amount");
                        double profitAmount = rs.getDouble("profit");
                        
                        // Format amounts as currency strings
                        String formattedSaleAmount = String.format("%.2f", saleAmount);
                        String formattedCostAmount = String.format("%.2f", costAmount);
                        String formattedProfit = String.format("%.2f", profitAmount);
                        
                        System.out.println("DEBUG: Processing profit record - Invoice: " + invoiceNumber + 
                                         ", Date: " + invoiceDate + 
                                         ", Sale: " + formattedSaleAmount + 
                                         ", Cost: " + formattedCostAmount + 
                                         ", Profit: " + formattedProfit);
                        
                        table.getItems().add(new ProfitReport(
                            invoiceNumber,
                            invoiceDate,
                            formattedSaleAmount,
                            formattedCostAmount,
                            formattedProfit
                        ));
                        count++;
                    }
                    
                    System.out.println("ProfitReport rows loaded: " + count);
                    if (count == 0) {
                        errorLabel.setText("No profit data found for selected date range.\n" +
                                         "Profit is calculated as (Sale Amount - Cost Amount) per invoice.\n" +
                                         "Data comes from Sales_Invoice, Sales_Invoice_Item, and ProductionStock tables.");
                    }
                } else {
                    System.out.println("DEBUG: Database is null or not connected");
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading profit data: " + ex.getMessage());
            }
        });

        // Refresh button action
        ((Button) buttons.getChildren().get(0)).setOnAction(e -> filterBtn.fire());

        // Print button action
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            try {
                // Create a print-friendly representation
                StringBuilder printContent = new StringBuilder();
                printContent.append("Profit Report\n");
                printContent.append("Date Range: ").append(fromDatePicker.getValue()).append(" to ").append(toDatePicker.getValue()).append("\n");
                printContent.append("Generated on: ").append(LocalDate.now()).append("\n\n");
                printContent.append(String.format("%-15s %-12s %-15s %-15s %-15s\n", "Invoice #", "Date", "Sale Amount", "Cost Amount", "Profit"));
                printContent.append("=".repeat(75)).append("\n");
                
                double totalSales = 0, totalCosts = 0, totalProfit = 0;
                for (ProfitReport item : table.getItems()) {
                    printContent.append(String.format("%-15s %-12s %-15s %-15s %-15s\n",
                        item.getInvoiceNumber(),
                        item.getInvoiceDate(),
                        item.getSaleAmount(),
                        item.getCostAmount(),
                        item.getProfit()
                    ));
                    
                    // Calculate totals
                    try {
                        totalSales += Double.parseDouble(item.getSaleAmount());
                        totalCosts += Double.parseDouble(item.getCostAmount());
                        totalProfit += Double.parseDouble(item.getProfit());
                    } catch (NumberFormatException ignored) {}
                }
                
                printContent.append("=".repeat(75)).append("\n");
                printContent.append(String.format("%-28s %-15s %-15s %-15s\n", "TOTALS:", 
                    String.format("%.2f", totalSales), 
                    String.format("%.2f", totalCosts), 
                    String.format("%.2f", totalProfit)
                ));
                
                // For now, just show print dialog (actual printing implementation depends on requirements)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Print Report");
                alert.setHeaderText("Profit Report");
                alert.setContentText("Print functionality would be implemented here.\nReport contains " + table.getItems().size() + " records.");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for printing: " + ex.getMessage());
            }
        });

        // Export button action
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Invoice Number,Date,Sale Amount,Cost Amount,Profit\n");
                
                for (ProfitReport item : table.getItems()) {
                    csvContent.append(String.format("%s,%s,%s,%s,%s\n",
                        item.getInvoiceNumber(),
                        item.getInvoiceDate(),
                        item.getSaleAmount(),
                        item.getCostAmount(),
                        item.getProfit()
                    ));
                }
                
                // For now, just show export dialog (actual file saving implementation depends on requirements)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Profit Report Export");
                alert.setContentText("Export functionality would be implemented here.\n" +
                                   "Data would be saved as CSV with " + table.getItems().size() + " records.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        // Optionally, trigger filter on load
        filterBtn.fire();

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel, table);
        return form;
    }

    private static VBox createSummaryReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Summary Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(30));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Summary report - maps to getSummaryReport database method
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(20);
        summaryGrid.setVgap(10);
        summaryGrid.setPadding(new Insets(15));
        summaryGrid.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-padding: 15;");

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Initialize with default values
        updateSummaryGrid(summaryGrid, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0);

        // Load data from database
        filterBtn.setOnAction(e -> {
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date fromDate = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date toDate = java.sql.Date.valueOf(toDatePicker.getValue());
                    
                    System.out.println("DEBUG: Summary Report - Loading data from " + fromDate + " to " + toDate);
                    
                    java.sql.ResultSet rs = config.database.getSummaryReport(fromDate, toDate);
                    if (rs != null && rs.next()) {
                        double totalPurchases = rs.getDouble("total_purchases");
                        double totalSales = rs.getDouble("total_sales");
                        double totalPurchaseReturns = rs.getDouble("total_purchase_returns");
                        double totalSalesReturns = rs.getDouble("total_sales_returns");
                        double totalBankBalance = rs.getDouble("total_bank_balance");
                        int totalCustomers = rs.getInt("total_customers");
                        int totalSuppliers = rs.getInt("total_suppliers");
                        double totalInventoryValue = rs.getDouble("total_inventory_value");
                        
                        System.out.println("DEBUG: Summary data loaded - Purchases: " + totalPurchases + 
                                         ", Sales: " + totalSales + ", Bank Balance: " + totalBankBalance);
                        
                        // Update the grid with real data
                        updateSummaryGrid(summaryGrid, totalPurchases, totalSales, totalPurchaseReturns, 
                                        totalSalesReturns, totalBankBalance, totalCustomers, totalSuppliers, totalInventoryValue);
                        
                        try {
                            rs.close();
                        } catch (SQLException closeEx) {
                            System.out.println("DEBUG: Error closing ResultSet: " + closeEx.getMessage());
                        }
                    } else {
                        System.out.println("DEBUG: No summary data found");
                        errorLabel.setText("No data found for selected date range.");
                        updateSummaryGrid(summaryGrid, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0);
                        
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (SQLException closeEx) {
                                System.out.println("DEBUG: Error closing empty ResultSet: " + closeEx.getMessage());
                            }
                        }
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                    updateSummaryGrid(summaryGrid, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading summary data: " + ex.getMessage());
                updateSummaryGrid(summaryGrid, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0);
            }
        });

        // Refresh button action
        ((Button) buttons.getChildren().get(0)).setOnAction(e -> filterBtn.fire());

        // Print button action
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            try {
                // Create a print-friendly representation
                StringBuilder printContent = new StringBuilder();
                printContent.append("BUSINESS SUMMARY REPORT\n");
                printContent.append("Date Range: ").append(fromDatePicker.getValue()).append(" to ").append(toDatePicker.getValue()).append("\n");
                printContent.append("Generated on: ").append(LocalDate.now()).append("\n");
                printContent.append("=".repeat(50)).append("\n\n");
                
                // Extract current values from the grid
                printContent.append("FINANCIAL SUMMARY:\n");
                printContent.append("-".repeat(30)).append("\n");
                for (int i = 0; i < summaryGrid.getChildren().size(); i += 2) {
                    if (i + 1 < summaryGrid.getChildren().size()) {
                        Label labelNode = (Label) summaryGrid.getChildren().get(i);
                        Label valueNode = (Label) summaryGrid.getChildren().get(i + 1);
                        printContent.append(String.format("%-25s %s\n", labelNode.getText(), valueNode.getText()));
                    }
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Print Summary Report");
                alert.setHeaderText("Business Summary Report");
                alert.setContentText("Print functionality would be implemented here.\n\n" +
                                   "Sample Print Preview:\n" + 
                                   printContent.toString().substring(0, Math.min(300, printContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for printing: " + ex.getMessage());
            }
        });

        // Export button action
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Summary Item,Value\n");
                
                // Extract current values from the grid
                for (int i = 0; i < summaryGrid.getChildren().size(); i += 2) {
                    if (i + 1 < summaryGrid.getChildren().size()) {
                        Label labelNode = (Label) summaryGrid.getChildren().get(i);
                        Label valueNode = (Label) summaryGrid.getChildren().get(i + 1);
                        String label = labelNode.getText().replace(":", "").replace(",", ";");
                        String value = valueNode.getText().replace(",", "");
                        csvContent.append(String.format("%s,%s\n", label, value));
                    }
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Summary Report");
                alert.setHeaderText("Business Summary Report Export");
                alert.setContentText("Export functionality would be implemented here.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        // Load initial data
        filterBtn.fire();

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel, summaryGrid);
        return form;
    }

    // Helper method to update the summary grid with data
    private static void updateSummaryGrid(GridPane summaryGrid, double totalPurchases, double totalSales, 
                                        double totalPurchaseReturns, double totalSalesReturns, 
                                        double totalBankBalance, int totalCustomers, int totalSuppliers, 
                                        double totalInventoryValue) {
        summaryGrid.getChildren().clear();
        
        // Calculate derived values
        double netPurchases = totalPurchases - totalPurchaseReturns;
        double netSales = totalSales - totalSalesReturns;
        double grossProfit = netSales - netPurchases;
        
        // Add summary items with formatted values
        addSummaryItem(summaryGrid, 0, "Total Purchases:", String.format("%.2f", totalPurchases));
        addSummaryItem(summaryGrid, 1, "Total Sales:", String.format("%.2f", totalSales));
        addSummaryItem(summaryGrid, 2, "Total Purchase Returns:", String.format("%.2f", totalPurchaseReturns));
        addSummaryItem(summaryGrid, 3, "Total Sales Returns:", String.format("%.2f", totalSalesReturns));
        addSummaryItem(summaryGrid, 4, "Net Purchases:", String.format("%.2f", netPurchases));
        addSummaryItem(summaryGrid, 5, "Net Sales:", String.format("%.2f", netSales));
        addSummaryItem(summaryGrid, 6, "Gross Profit:", String.format("%.2f", grossProfit));
        addSummaryItem(summaryGrid, 7, "Total Bank Balance:", String.format("%.2f", totalBankBalance));
        addSummaryItem(summaryGrid, 8, "Total Customers:", String.valueOf(totalCustomers));
        addSummaryItem(summaryGrid, 9, "Total Suppliers:", String.valueOf(totalSuppliers));
        addSummaryItem(summaryGrid, 10, "Inventory Value:", String.format("%.2f", totalInventoryValue));
    }

    private static VBox createBalanceSheet() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Balance Sheet");

        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
        actionButtons.setPadding(new Insets(10));
        
        Button refreshBtn = createActionButton("Refresh");
        Button viewDetailedBtn = createActionButton("View Detailed Balance Sheet");
        Button printBtn = createActionButton("Print");
        actionButtons.getChildren().addAll(refreshBtn, viewDetailedBtn, printBtn);

        // Balance sheet - gets data from database
        GridPane balanceGrid = new GridPane();
        balanceGrid.setHgap(20);
        balanceGrid.setVgap(10);
        balanceGrid.setPadding(new Insets(15));
        balanceGrid.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load balance sheet data from database
        Runnable loadBalanceSheet = () -> {
            balanceGrid.getChildren().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    Object[] balanceData = config.database.getBalanceSheetData();
                    
                    // Defensive programming - check for null data
                    if (balanceData == null || balanceData.length < 8) {
                        errorLabel.setText("Error: Invalid balance sheet data returned from database");
                        return;
                    }
                    
                    // Safely extract data with null checks
                    double totalBankBalance = (balanceData[0] != null) ? (Double) balanceData[0] : 0.0;
                    double customersOweUs = (balanceData[1] != null) ? (Double) balanceData[1] : 0.0;
                    double weOweCustomers = (balanceData[2] != null) ? (Double) balanceData[2] : 0.0;
                    double suppliersOweUs = (balanceData[3] != null) ? (Double) balanceData[3] : 0.0;
                    double weOweSuppliers = (balanceData[4] != null) ? (Double) balanceData[4] : 0.0;
                    double totalReceivables = (balanceData[5] != null) ? (Double) balanceData[5] : 0.0;
                    double totalPayables = (balanceData[6] != null) ? (Double) balanceData[6] : 0.0;
                    double netWorth = (balanceData[7] != null) ? (Double) balanceData[7] : 0.0;
                    
                    int row = 0;
                    
                    // Assets Header
                    Label assetsLabel = new Label("ASSETS");
                    assetsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #27ae60;");
                    balanceGrid.add(assetsLabel, 0, row++);
                    
                    // Current Assets
                    addBalanceSheetItem(balanceGrid, row++, "Cash in Hand (All Banks):", String.format("Rs. %.2f", totalBankBalance));
                    addBalanceSheetItem(balanceGrid, row++, "Accounts Receivable (Customers):", String.format("Rs. %.2f", customersOweUs));
                    addBalanceSheetItem(balanceGrid, row++, "Accounts Receivable (Suppliers):", String.format("Rs. %.2f", suppliersOweUs));
                    
                    // Total Assets
                    Label totalAssetsLabel = new Label("TOTAL ASSETS:");
                    totalAssetsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    balanceGrid.add(totalAssetsLabel, 0, row);
                    
                    Label totalAssetsValue = new Label(String.format("Rs. %.2f", totalBankBalance + totalReceivables));
                    totalAssetsValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #27ae60;");
                    balanceGrid.add(totalAssetsValue, 1, row++);
                    
                    // Spacer
                    balanceGrid.add(new Label(" "), 0, row++);
                    
                    // Liabilities Header
                    Label liabilitiesLabel = new Label("LIABILITIES");
                    liabilitiesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");
                    balanceGrid.add(liabilitiesLabel, 0, row++);
                    
                    // Current Liabilities
                    addBalanceSheetItem(balanceGrid, row++, "Accounts Payable (Customers):", String.format("Rs. %.2f", weOweCustomers));
                    addBalanceSheetItem(balanceGrid, row++, "Accounts Payable (Suppliers):", String.format("Rs. %.2f", weOweSuppliers));
                    
                    // Total Liabilities
                    Label totalLiabilitiesLabel = new Label("TOTAL LIABILITIES:");
                    totalLiabilitiesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    balanceGrid.add(totalLiabilitiesLabel, 0, row);
                    
                    Label totalLiabilitiesValue = new Label(String.format("Rs. %.2f", totalPayables));
                    totalLiabilitiesValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                    balanceGrid.add(totalLiabilitiesValue, 1, row++);
                    
                    // Spacer
                    balanceGrid.add(new Label(" "), 0, row++);
                    
                    // Net Worth Header
                    Label netWorthLabel = new Label("NET WORTH");
                    netWorthLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3498db;");
                    balanceGrid.add(netWorthLabel, 0, row++);
                    
                    // Net Worth Value
                    Label netWorthDescLabel = new Label("NET WORTH (Assets - Liabilities):");
                    netWorthDescLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    balanceGrid.add(netWorthDescLabel, 0, row);
                    
                    String netWorthColor = netWorth >= 0 ? "#27ae60" : "#e74c3c";
                    Label netWorthValue = new Label(String.format("Rs. %.2f", netWorth));
                    netWorthValue.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + netWorthColor + ";");
                    balanceGrid.add(netWorthValue, 1, row++);
                    
                    // Footer
                    balanceGrid.add(new Label(" "), 0, row++);
                    Label footerLabel = new Label("As of: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    footerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                    balanceGrid.add(footerLabel, 0, row);
                    
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading balance sheet data: " + ex.getMessage());
            }
        };

        // Refresh button action
        refreshBtn.setOnAction(e -> loadBalanceSheet.run());

        // View detailed balance sheet button action
        viewDetailedBtn.setOnAction(e -> {
            try {
                com.cablemanagement.views.BalanceSheetView.showBalanceSheet();
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error opening detailed balance sheet: " + ex.getMessage());
            }
        });

        // Print button action
        printBtn.setOnAction(e -> {
            try {
                // Generate temporary filename for balance sheet PDF
                String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filename = System.getProperty("java.io.tmpdir") + java.io.File.separator + 
                                 "BalanceSheet_Summary_" + timestamp + ".pdf";
                
                // Use the BalanceSheetGenerator to create and open PDF for printing
                com.cablemanagement.invoice.BalanceSheetGenerator.generateAndPreviewBalanceSheet(filename);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing balance sheet for printing: " + ex.getMessage());
            }
        });

        // Load initial data
        loadBalanceSheet.run();

        form.getChildren().addAll(heading, actionButtons, errorLabel, balanceGrid);
        return form;
    }

    private static VBox createCustomersReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Customers General Report");

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Customers report table - maps to Customer table
        TableView<CustomerReport> table = new TableView<>();
        
        TableColumn<CustomerReport, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        
        TableColumn<CustomerReport, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        TableColumn<CustomerReport, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        table.getColumns().addAll(nameCol, phoneCol, addressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load data from backend
        try {
            System.out.println("DEBUG: Starting to load customer data...");
            if (config.database != null && config.database.isConnected()) {
                System.out.println("DEBUG: Database is connected, executing query...");
                java.sql.ResultSet rs = config.database.getCustomersReport();
                int count = 0;
                if (rs != null) {
                    System.out.println("DEBUG: ResultSet is not null, processing results...");
                    while (rs.next()) {
                        String customerName = rs.getString("customer_name");
                        String phoneNumber = rs.getString("contact_number");
                        String address = rs.getString("address");
                        
                        System.out.println("DEBUG: Processing customer: " + customerName + ", phone: " + phoneNumber);
                        
                        // Handle null values
                        if (customerName == null) customerName = "Unknown";
                        if (phoneNumber == null) phoneNumber = "N/A";
                        if (address == null) address = "N/A";
                        
                        table.getItems().add(new CustomerReport(customerName, phoneNumber, address));
                        count++;
                    }
                } else {
                    System.out.println("DEBUG: ResultSet is null!");
                }
                System.out.println("CustomersReport rows loaded: " + count);
                if (count == 0) {
                    errorLabel.setText("No customer data found.");
                }
            } else {
                errorLabel.setText("Database not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error loading customer data: " + ex.getMessage());
        }

        // Refresh button action
        ((Button) buttons.getChildren().get(0)).setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.ResultSet rs = config.database.getCustomersReport();
                    int count = 0;
                    while (rs != null && rs.next()) {
                        String customerName = rs.getString("customer_name");
                        String phoneNumber = rs.getString("contact_number");
                        String address = rs.getString("address");
                        
                        // Handle null values
                        if (customerName == null) customerName = "Unknown";
                        if (phoneNumber == null) phoneNumber = "N/A";
                        if (address == null) address = "N/A";
                        
                        table.getItems().add(new CustomerReport(customerName, phoneNumber, address));
                        count++;
                    }
                    System.out.println("CustomersReport rows refreshed: " + count);
                    if (count == 0) {
                        errorLabel.setText("No customer data found.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error refreshing customer data: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, buttons, errorLabel, table);
        return form;
    }

    private static VBox createSuppliersReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Suppliers General Report");

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Suppliers report table - maps to Supplier table
        TableView<SupplierReport> table = new TableView<>();
        
        TableColumn<SupplierReport, String> nameCol = new TableColumn<>("Supplier Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        
        TableColumn<SupplierReport, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        TableColumn<SupplierReport, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        table.getColumns().addAll(nameCol, phoneCol, addressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load data from backend
        try {
            if (config.database != null && config.database.isConnected()) {
                java.sql.ResultSet rs = config.database.getSuppliersReport();
                int count = 0;
                while (rs != null && rs.next()) {
                    String supplierName = rs.getString("supplier_name");
                    String phoneNumber = rs.getString("contact_number");
                    String address = rs.getString("address");
                    
                    // Handle null values
                    if (supplierName == null) supplierName = "Unknown";
                    if (phoneNumber == null) phoneNumber = "N/A";
                    if (address == null) address = "N/A";
                    
                    table.getItems().add(new SupplierReport(supplierName, phoneNumber, address));
                    count++;
                }
                System.out.println("SuppliersReport rows loaded: " + count);
                if (count == 0) {
                    errorLabel.setText("No supplier data found.");
                }
            } else {
                errorLabel.setText("Database not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error loading supplier data: " + ex.getMessage());
        }

        // Refresh button action
        ((Button) buttons.getChildren().get(0)).setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.ResultSet rs = config.database.getSuppliersReport();
                    int count = 0;
                    while (rs != null && rs.next()) {
                        String supplierName = rs.getString("supplier_name");
                        String phoneNumber = rs.getString("contact_number");
                        String address = rs.getString("address");
                        
                        // Handle null values
                        if (supplierName == null) supplierName = "Unknown";
                        if (phoneNumber == null) phoneNumber = "N/A";
                        if (address == null) address = "N/A";
                        
                        table.getItems().add(new SupplierReport(supplierName, phoneNumber, address));
                        count++;
                    }
                    System.out.println("SuppliersReport rows refreshed: " + count);
                    if (count == 0) {
                        errorLabel.setText("No supplier data found.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error refreshing supplier data: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, buttons, errorLabel, table);
        return form;
    }

    private static VBox createAreaWiseReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Area-Wise Customer/Supplier Report");

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Area-wise report table - maps to Customer and Supplier tables with area data
        TableView<AreaWiseReport> table = new TableView<>();
        
        TableColumn<AreaWiseReport, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("partyType"));
        
        TableColumn<AreaWiseReport, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<AreaWiseReport, String> tehsilCol = new TableColumn<>("Tehsil");
        tehsilCol.setCellValueFactory(new PropertyValueFactory<>("tehsilName"));
        
        TableColumn<AreaWiseReport, String> districtCol = new TableColumn<>("District");
        districtCol.setCellValueFactory(new PropertyValueFactory<>("districtName"));
        
        TableColumn<AreaWiseReport, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setCellValueFactory(new PropertyValueFactory<>("provinceName"));
        
        table.getColumns().addAll(typeCol, nameCol, tehsilCol, districtCol, provinceCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load data from backend
        try {
            if (config.database != null && config.database.isConnected()) {
                java.sql.ResultSet rs = config.database.getAreaWiseReport();
                int count = 0;
                while (rs != null && rs.next()) {
                    String partyType = rs.getString("party_type");
                    String name = rs.getString("name");
                    String tehsilName = rs.getString("tehsil_name");
                    String districtName = rs.getString("district_name");
                    String provinceName = rs.getString("province_name");
                    
                    // Handle null values
                    if (partyType == null) partyType = "Unknown";
                    if (name == null) name = "Unknown";
                    if (tehsilName == null) tehsilName = "Unknown";
                    if (districtName == null) districtName = "Unknown";
                    if (provinceName == null) provinceName = "Unknown";
                    
                    table.getItems().add(new AreaWiseReport(partyType, name, tehsilName, districtName, provinceName));
                    count++;
                }
                System.out.println("AreaWiseReport rows loaded: " + count);
                if (count == 0) {
                    errorLabel.setText("No area-wise data found. Please ensure customers and suppliers have proper area assignments.");
                }
            } else {
                errorLabel.setText("Database not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error loading area-wise data: " + ex.getMessage());
        }

        // Refresh button action
        ((Button) buttons.getChildren().get(0)).setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.ResultSet rs = config.database.getAreaWiseReport();
                    int count = 0;
                    while (rs != null && rs.next()) {
                        String partyType = rs.getString("party_type");
                        String name = rs.getString("name");
                        String tehsilName = rs.getString("tehsil_name");
                        String districtName = rs.getString("district_name");
                        String provinceName = rs.getString("province_name");
                        
                        // Handle null values
                        if (partyType == null) partyType = "Unknown";
                        if (name == null) name = "Unknown";
                        if (tehsilName == null) tehsilName = "Unknown";
                        if (districtName == null) districtName = "Unknown";
                        if (provinceName == null) provinceName = "Unknown";
                        
                        table.getItems().add(new AreaWiseReport(partyType, name, tehsilName, districtName, provinceName));
                        count++;
                    }
                    System.out.println("AreaWiseReport rows refreshed: " + count);
                    if (count == 0) {
                        errorLabel.setText("No area-wise data found. Please ensure customers and suppliers have proper area assignments.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error refreshing area-wise data: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, buttons, errorLabel, table);
        return form;
    }

    private static VBox createBrandSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Brand-Wise Sales Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Brand sales report table - maps to Sales_Invoice_Item joined with Brand and ProductionStock
        TableView<BrandSalesReport> table = new TableView<>();
        
        TableColumn<BrandSalesReport, String> salesmanCol = new TableColumn<>("Salesman");
        salesmanCol.setCellValueFactory(new PropertyValueFactory<>("salesmanName"));
        
        TableColumn<BrandSalesReport, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        
        TableColumn<BrandSalesReport, String> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("totalQuantity"));
        
        TableColumn<BrandSalesReport, String> salesCol = new TableColumn<>("Total Sales");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("totalSale"));
        
        table.getColumns().addAll(salesmanCol, brandCol, quantityCol, salesCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load data from backend
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                System.out.println("DEBUG: Brand Sales Report - Loading data...");
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    
                    System.out.println("DEBUG: Date range: " + from + " to " + to);
                    
                    java.sql.ResultSet rs = config.database.getBrandSalesReport(from, to);
                    int count = 0;
                    while (rs != null && rs.next()) {
                        String brandName = rs.getString("brand_name");
                        String totalQuantity = String.format("%.2f", rs.getDouble("total_quantity"));
                        String totalSales = String.format("%.2f", rs.getDouble("total_sales"));
                        String salesmanName = rs.getString("salesman_name");
                        
                        System.out.println("DEBUG: Processing record - Brand: " + brandName + ", Quantity: " + totalQuantity + ", Sales: " + totalSales);
                        
                        // Handle null values
                        if (brandName == null) brandName = "Unknown Brand";
                        if (salesmanName == null) salesmanName = "N/A";
                        
                        table.getItems().add(new BrandSalesReport(salesmanName, brandName, totalQuantity, totalSales));
                        count++;
                    }
                    
                    System.out.println("BrandSalesReport rows loaded: " + count);
                    if (count == 0) {
                        errorLabel.setText("No brand sales data found for selected date range.");
                    }
                } else {
                    System.out.println("DEBUG: Database is null or not connected");
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading brand sales data: " + ex.getMessage());
            }
        });

        // Refresh button action
        ((Button) buttons.getChildren().get(0)).setOnAction(e -> filterBtn.fire());

        // Print button action
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            try {
                // Create a print-friendly representation
                StringBuilder printContent = new StringBuilder();
                printContent.append("Brand-Wise Sales Report\n");
                printContent.append("Date Range: ").append(fromDatePicker.getValue()).append(" to ").append(toDatePicker.getValue()).append("\n");
                printContent.append("Generated on: ").append(LocalDate.now()).append("\n\n");
                printContent.append(String.format("%-20s %-20s %-15s %-15s\n", "Salesman", "Brand", "Quantity", "Total Sales"));
                printContent.append("=".repeat(70)).append("\n");
                
                for (BrandSalesReport item : table.getItems()) {
                    printContent.append(String.format("%-20s %-20s %-15s %-15s\n", 
                        item.getSalesmanName(), 
                        item.getBrandName(), 
                        item.getTotalQuantity(), 
                        item.getTotalSale()));
                }
                
                // For now, just show print dialog (actual printing implementation depends on requirements)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Print Report");
                alert.setHeaderText("Brand Sales Report");
                alert.setContentText("Print functionality would be implemented here.\nReport contains " + table.getItems().size() + " records.");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for printing: " + ex.getMessage());
            }
        });

        // Export button action
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Salesman,Brand,Quantity,Total Sales\n");
                
                for (BrandSalesReport item : table.getItems()) {
                    csvContent.append(String.format("%s,%s,%s,%s\n", 
                        item.getSalesmanName(), 
                        item.getBrandName(), 
                        item.getTotalQuantity(), 
                        item.getTotalSale()));
                }
                
                // For now, just show export dialog (actual file saving implementation depends on requirements)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Brand Sales Report Export");
                alert.setContentText("Export functionality would be implemented here.\nReport contains " + table.getItems().size() + " records ready for CSV export.");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        // Optionally, trigger filter on load
        filterBtn.fire();

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel, table);
        return form;
    }

    private static VBox createBrandProfitReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Brand-Wise Profit Report");

        // Date range filters
        HBox dateRangeBox = createDateRangeFilter();

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Brand profit report table - maps to View_Brand_Wise_Profit_Report
        TableView<BrandProfitReport> table = new TableView<>();
        
        TableColumn<BrandProfitReport, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        
        TableColumn<BrandProfitReport, String> salesCol = new TableColumn<>("Total Sales");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
        
        TableColumn<BrandProfitReport, String> costCol = new TableColumn<>("Total Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        TableColumn<BrandProfitReport, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));
        
        table.getColumns().addAll(brandCol, salesCol, costCol, profitCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data - in real app, fetch from View_Brand_Wise_Profit_Report
        ObservableList<BrandProfitReport> data = FXCollections.observableArrayList(
            new BrandProfitReport("PowerFlex", "37500.00", "25000.00", "12500.00"),
            new BrandProfitReport("SafeWire", "36000.00", "24000.00", "12000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, table);
        return form;
    }

    private static VBox createCustomerSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Customer-Wise Sales Report");

        // Date range filters
        HBox dateRangeBox = createDateRangeFilter();

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Customer sales report table - maps to View_Customer_Wise_Sales_Report
        TableView<CustomerSalesReport> table = new TableView<>();
        
        TableColumn<CustomerSalesReport, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        
        TableColumn<CustomerSalesReport, String> invoicesCol = new TableColumn<>("Invoices");
        invoicesCol.setCellValueFactory(new PropertyValueFactory<>("totalInvoices"));
        
        TableColumn<CustomerSalesReport, String> salesCol = new TableColumn<>("Total Sales");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
        
        table.getColumns().addAll(customerCol, invoicesCol, salesCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data - in real app, fetch from View_Customer_Wise_Sales_Report
        ObservableList<CustomerSalesReport> data = FXCollections.observableArrayList(
            new CustomerSalesReport("Ali Traders", "5", "75000.00"),
            new CustomerSalesReport("Pak Electric House", "3", "50000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, table);
        return form;
    }

    private static VBox createSupplierSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Supplier-Wise Sales Report");

        // Date range filters
        HBox dateRangeBox = createDateRangeFilter();

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Supplier sales report table - maps to View_Supplier_Wise_Sales_Report
        TableView<SupplierSalesReport> table = new TableView<>();
        
        TableColumn<SupplierSalesReport, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        
        TableColumn<SupplierSalesReport, String> invoicesCol = new TableColumn<>("Invoices");
        invoicesCol.setCellValueFactory(new PropertyValueFactory<>("totalInvoices"));
        
        TableColumn<SupplierSalesReport, String> suppliedCol = new TableColumn<>("Total Supplied");
        suppliedCol.setCellValueFactory(new PropertyValueFactory<>("totalSupplied"));
        
        table.getColumns().addAll(supplierCol, invoicesCol, suppliedCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data - in real app, fetch from View_Supplier_Wise_Sales_Report
        ObservableList<SupplierSalesReport> data = FXCollections.observableArrayList(
            new SupplierSalesReport("RawMetals Pvt Ltd", "10", "250000.00"),
            new SupplierSalesReport("Insulation Depot", "8", "180000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, table);
        return form;
    }

    // private static VBox createAttendanceReport() {
    //     VBox form = new VBox(15);
    //     form.setPadding(new Insets(20));
    //     form.getStyleClass().add("form-container");

    //     Label heading = createHeading("Attendance Report");

    //     // Date range filters
    //     HBox dateRangeBox = createDateRangeFilter();

    //     // Action buttons
    //     HBox buttons = createReportActionButtons();

    //     // Attendance report table - maps to View_Attendance_Report
    //     TableView<AttendanceReport> table = new TableView<>();
        
    //     TableColumn<AttendanceReport, String> nameCol = new TableColumn<>("Employee");
    //     nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        
    //     TableColumn<AttendanceReport, String> dateCol = new TableColumn<>("Date");
    //     dateCol.setCellValueFactory(new PropertyValueFactory<>("attendanceDate"));
        
    //     TableColumn<AttendanceReport, String> statusCol = new TableColumn<>("Status");
    //     statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
    //     TableColumn<AttendanceReport, String> hoursCol = new TableColumn<>("Hours");
    //     hoursCol.setCellValueFactory(new PropertyValueFactory<>("workingHours"));
        
    //     table.getColumns().addAll(nameCol, dateCol, statusCol, hoursCol);
    //     table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
    //     // Sample data - in real app, fetch from View_Attendance_Report
    //     ObservableList<AttendanceReport> data = FXCollections.observableArrayList(
    //         new AttendanceReport("Zahid Khan", "2025-07-01", "present", "8"),
    //         new AttendanceReport("Zahid Khan", "2025-07-02", "present", "8"),
    //         new AttendanceReport("Faisal Mehmood", "2025-07-01", "absent", "0")
    //     );
    //     table.setItems(data);

    //     form.getChildren().addAll(heading, dateRangeBox, buttons, table);
    //     return form;
    // }

    // Helper methods
    private static HBox createDateRangeFilter() {
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker();
        fromDatePicker.setValue(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker();
        toDatePicker.setValue(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);
        return dateRangeBox;
    }

    private static HBox createReportActionButtons() {
        HBox buttons = new HBox(10);
        Button refreshBtn = createActionButton("Refresh");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(refreshBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        return buttons;
    }

    private static void addSummaryItem(GridPane grid, int row, String label, String value) {
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-weight: bold;");
        grid.add(nameLabel, 0, row);
        
        Label valueLabel = new Label(value);
        grid.add(valueLabel, 1, row);
    }

    private static void addBalanceSheetItem(GridPane grid, int row, String label, String value) {
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-weight: bold;");
        grid.add(nameLabel, 0, row);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-weight: bold;");
        grid.add(valueLabel, 1, row);
    }

    private static Label createHeading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-heading");
        label.setFont(Font.font(18));
        return label;
    }

    private static Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        return button;
    }

    // Model classes for reports
    public static class PurchaseReport {
        private final String invoiceNumber;
        private final String invoiceDate;
        private final String supplierName;
        private final String totalAmount;
        private final String discountAmount;
        private final String paidAmount;

        public PurchaseReport(String invoiceNumber, String invoiceDate, String supplierName, 
                            String totalAmount, String discountAmount, String paidAmount) {
            this.invoiceNumber = invoiceNumber;
            this.invoiceDate = invoiceDate;
            this.supplierName = supplierName;
            this.totalAmount = totalAmount;
            this.discountAmount = discountAmount;
            this.paidAmount = paidAmount;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getInvoiceDate() { return invoiceDate; }
        public String getSupplierName() { return supplierName; }
        public String getTotalAmount() { return totalAmount; }
        public String getDiscountAmount() { return discountAmount; }
        public String getPaidAmount() { return paidAmount; }
    }

    public static class SalesReport {
        private final String invoiceNumber;
        private final String salesDate;
        private final String customerName;
        private final String totalAmount;
        private final String discountAmount;
        private final String paidAmount;

        public SalesReport(String invoiceNumber, String salesDate, String customerName, 
                         String totalAmount, String discountAmount, String paidAmount) {
            this.invoiceNumber = invoiceNumber;
            this.salesDate = salesDate;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
            this.discountAmount = discountAmount;
            this.paidAmount = paidAmount;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getSalesDate() { return salesDate; }
        public String getCustomerName() { return customerName; }
        public String getTotalAmount() { return totalAmount; }
        public String getDiscountAmount() { return discountAmount; }
        public String getPaidAmount() { return paidAmount; }
    }

    public static class ReturnPurchaseReport {
        private final String invoiceNumber;
        private final String invoiceDate;
        private final String supplierName;
        private final String totalAmount;
        private final String discountAmount;
        private final String paidAmount;

        public ReturnPurchaseReport(String invoiceNumber, String invoiceDate, String supplierName, 
                                  String totalAmount, String discountAmount, String paidAmount) {
            this.invoiceNumber = invoiceNumber;
            this.invoiceDate = invoiceDate;
            this.supplierName = supplierName;
            this.totalAmount = totalAmount;
            this.discountAmount = discountAmount;
            this.paidAmount = paidAmount;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getInvoiceDate() { return invoiceDate; }
        public String getSupplierName() { return supplierName; }
        public String getTotalAmount() { return totalAmount; }
        public String getDiscountAmount() { return discountAmount; }
        public String getPaidAmount() { return paidAmount; }
    }

    public static class ReturnSalesReport {
        private final String invoiceNumber;
        private final String invoiceDate;
        private final String customerName;
        private final String totalAmount;

        public ReturnSalesReport(String invoiceNumber, String invoiceDate, 
                               String customerName, String totalAmount) {
            this.invoiceNumber = invoiceNumber;
            this.invoiceDate = invoiceDate;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getInvoiceDate() { return invoiceDate; }
        public String getCustomerName() { return customerName; }
        public String getTotalAmount() { return totalAmount; }
    }

    public static class BankTransferReport {
        private final String transactionDate;
        private final String fromBank;
        private final String toBank;
        private final String amount;

        public BankTransferReport(String transactionDate, String fromBank, 
                                String toBank, String amount) {
            this.transactionDate = transactionDate;
            this.fromBank = fromBank;
            this.toBank = toBank;
            this.amount = amount;
        }

        public String getTransactionDate() { return transactionDate; }
        public String getFromBank() { return fromBank; }
        public String getToBank() { return toBank; }
        public String getAmount() { return amount; }
    }

    public static class ProfitReport {
        private final String invoiceNumber;
        private final String invoiceDate;
        private final String saleAmount;
        private final String costAmount;
        private final String profit;

        public ProfitReport(String invoiceNumber, String invoiceDate, 
                           String saleAmount, String costAmount, String profit) {
            this.invoiceNumber = invoiceNumber;
            this.invoiceDate = invoiceDate;
            this.saleAmount = saleAmount;
            this.costAmount = costAmount;
            this.profit = profit;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getInvoiceDate() { return invoiceDate; }
        public String getSaleAmount() { return saleAmount; }
        public String getCostAmount() { return costAmount; }
        public String getProfit() { return profit; }
    }

    public static class CustomerReport {
        private final String customerName;
        private final String phoneNumber;
        private final String address;

        public CustomerReport(String customerName, String phoneNumber, String address) {
            this.customerName = customerName;
            this.phoneNumber = phoneNumber;
            this.address = address;
        }

        public String getCustomerName() { return customerName; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getAddress() { return address; }
    }

    public static class SupplierReport {
        private final String supplierName;
        private final String phoneNumber;
        private final String address;

        public SupplierReport(String supplierName, String phoneNumber, String address) {
            this.supplierName = supplierName;
            this.phoneNumber = phoneNumber;
            this.address = address;
        }

        public String getSupplierName() { return supplierName; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getAddress() { return address; }
    }

    public static class AreaWiseReport {
        private final String partyType;
        private final String name;
        private final String tehsilName;
        private final String districtName;
        private final String provinceName;

        public AreaWiseReport(String partyType, String name, String tehsilName, 
                            String districtName, String provinceName) {
            this.partyType = partyType;
            this.name = name;
            this.tehsilName = tehsilName;
            this.districtName = districtName;
            this.provinceName = provinceName;
        }

        public String getPartyType() { return partyType; }
        public String getName() { return name; }
        public String getTehsilName() { return tehsilName; }
        public String getDistrictName() { return districtName; }
        public String getProvinceName() { return provinceName; }
    }

    public static class BrandSalesReport {
        private final String salesmanName;
        private final String brandName;
        private final String totalQuantity;
        private final String totalSale;

        public BrandSalesReport(String salesmanName, String brandName, 
                              String totalQuantity, String totalSale) {
            this.salesmanName = salesmanName;
            this.brandName = brandName;
            this.totalQuantity = totalQuantity;
            this.totalSale = totalSale;
        }

        public String getSalesmanName() { return salesmanName; }
        public String getBrandName() { return brandName; }
        public String getTotalQuantity() { return totalQuantity; }
        public String getTotalSale() { return totalSale; }
    }

    public static class BrandProfitReport {
        private final String brandName;
        private final String totalSales;
        private final String totalCost;
        private final String profit;

        public BrandProfitReport(String brandName, String totalSales, 
                               String totalCost, String profit) {
            this.brandName = brandName;
            this.totalSales = totalSales;
            this.totalCost = totalCost;
            this.profit = profit;
        }

        public String getBrandName() { return brandName; }
        public String getTotalSales() { return totalSales; }
        public String getTotalCost() { return totalCost; }
        public String getProfit() { return profit; }
    }

    public static class CustomerSalesReport {
        private final String customerName;
        private final String totalInvoices;
        private final String totalSales;

        public CustomerSalesReport(String customerName, String totalInvoices, 
                                 String totalSales) {
            this.customerName = customerName;
            this.totalInvoices = totalInvoices;
            this.totalSales = totalSales;
        }

        public String getCustomerName() { return customerName; }
        public String getTotalInvoices() { return totalInvoices; }
        public String getTotalSales() { return totalSales; }
    }

    public static class SupplierSalesReport {
        private final String supplierName;
        private final String totalInvoices;
        private final String totalSupplied;

        public SupplierSalesReport(String supplierName, String totalInvoices, 
                                 String totalSupplied) {
            this.supplierName = supplierName;
            this.totalInvoices = totalInvoices;
            this.totalSupplied = totalSupplied;
        }

        public String getSupplierName() { return supplierName; }
        public String getTotalInvoices() { return totalInvoices; }
        public String getTotalSupplied() { return totalSupplied; }
    }

    public static class AttendanceReport {
        private final String employeeName;
        private final String attendanceDate;
        private final String status;
        private final String workingHours;

        public AttendanceReport(String employeeName, String attendanceDate, 
                              String status, String workingHours) {
            this.employeeName = employeeName;
            this.attendanceDate = attendanceDate;
            this.status = status;
            this.workingHours = workingHours;
        }

        public String getEmployeeName() { return employeeName; }
        public String getAttendanceDate() { return attendanceDate; }
        public String getStatus() { return status; }
        public String getWorkingHours() { return workingHours; }
    }

}