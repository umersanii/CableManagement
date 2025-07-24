package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
            "Summary Report",
            "Balance Sheet",
            "Customers Report",
            "Suppliers Report",
            "Area-Wise Report",
            "Brand Sales Report",
            "Brand Profit Report",
            "Customer Sales Report",
            "Supplier Sales Report",
            "Attendance Report"
        };

        Runnable[] actions = {
            () -> reportArea.getChildren().setAll(createPurchaseReport()),
            () -> reportArea.getChildren().setAll(createSalesReport()),
            () -> reportArea.getChildren().setAll(createReturnPurchaseReport()),
            () -> reportArea.getChildren().setAll(createReturnSalesReport()),
            () -> reportArea.getChildren().setAll(createBankTransferReport()),
            () -> reportArea.getChildren().setAll(createProfitReport()),
            () -> reportArea.getChildren().setAll(createSummaryReport()),
            () -> reportArea.getChildren().setAll(createBalanceSheet()),
            () -> reportArea.getChildren().setAll(createCustomersReport()),
            () -> reportArea.getChildren().setAll(createSuppliersReport()),
            () -> reportArea.getChildren().setAll(createAreaWiseReport()),
            () -> reportArea.getChildren().setAll(createBrandSalesReport()),
            () -> reportArea.getChildren().setAll(createBrandProfitReport()),
            () -> reportArea.getChildren().setAll(createCustomerSalesReport()),
            () -> reportArea.getChildren().setAll(createSupplierSalesReport()),
            () -> reportArea.getChildren().setAll(createAttendanceReport())
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

        // Purchase report table - maps to View_Purchase_Report
        TableView<PurchaseReport> table = new TableView<>();

        TableColumn<PurchaseReport, String> invCol = new TableColumn<>("Invoice #");
        invCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));

        TableColumn<PurchaseReport, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));

        TableColumn<PurchaseReport, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));

        TableColumn<PurchaseReport, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        TableColumn<PurchaseReport, String> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));

        TableColumn<PurchaseReport, String> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));

        table.getColumns().addAll(invCol, dateCol, supplierCol, amountCol, discountCol, paidCol);
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
                    java.sql.ResultSet rs = config.database.getPurchaseReport(from, to);
                    int count = 0;
                    while (rs != null && rs.next()) {
                        table.getItems().add(new PurchaseReport(
                            rs.getString("invoiceNumber"),
                            rs.getString("invoiceDate"),
                            rs.getString("supplierName"),
                            rs.getString("totalAmount"),
                            rs.getString("discountAmount"),
                            rs.getString("paidAmount")
                        ));
                        count++;
                    }
                    System.out.println("PurchaseReport rows loaded: " + count);
                    if (count == 0) {
                        errorLabel.setText("No data found for selected date range.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading data: " + ex.getMessage());
            }
        });

        // Optionally, trigger filter on load
        filterBtn.fire();

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel, table);
        return form;
    }

    private static VBox createSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Sales Report");

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

        // Sales report table - maps to Sales_Invoice table
        TableView<SalesReport> table = new TableView<>();
        
        TableColumn<SalesReport, String> invCol = new TableColumn<>("Invoice #");
        invCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        
        TableColumn<SalesReport, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("salesDate"));
        
        TableColumn<SalesReport, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        
        TableColumn<SalesReport, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        TableColumn<SalesReport, String> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        
        TableColumn<SalesReport, String> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        
        table.getColumns().addAll(invCol, dateCol, customerCol, amountCol, discountCol, paidCol);
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
                    java.sql.ResultSet rs = config.database.getSalesReport(from, to);
                    int count = 0;
                    while (rs != null && rs.next()) {
                        // Format the amounts as strings with proper formatting
                        String totalAmount = String.format("%.2f", rs.getDouble("total_amount"));
                        String discountAmount = String.format("%.2f", rs.getDouble("discount_amount"));
                        String paidAmount = String.format("%.2f", rs.getDouble("paid_amount"));
                        
                        table.getItems().add(new SalesReport(
                            rs.getString("sales_invoice_number"),
                            rs.getString("sales_date"),
                            rs.getString("customer_name"),
                            totalAmount,
                            discountAmount,
                            paidAmount
                        ));
                        count++;
                    }
                    System.out.println("SalesReport rows loaded: " + count);
                    if (count == 0) {
                        errorLabel.setText("No sales data found for selected date range.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading sales data: " + ex.getMessage());
            }
        });

        // Optionally, trigger filter on load
        filterBtn.fire();

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel, table);
        return form;
    }

    private static VBox createReturnPurchaseReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Return Purchase Report");

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

        // Return purchase report table - maps to View_Return_Purchase_Report
        TableView<ReturnPurchaseReport> table = new TableView<>();
        
        TableColumn<ReturnPurchaseReport, String> invCol = new TableColumn<>("Invoice #");
        invCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        
        TableColumn<ReturnPurchaseReport, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        
        TableColumn<ReturnPurchaseReport, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        
        TableColumn<ReturnPurchaseReport, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        TableColumn<ReturnPurchaseReport, String> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        
        TableColumn<ReturnPurchaseReport, String> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        
        table.getColumns().addAll(invCol, dateCol, supplierCol, amountCol, discountCol, paidCol);
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
                    java.sql.ResultSet rs = config.database.getReturnPurchaseReport(from, to);
                    int count = 0;
                    while (rs != null && rs.next()) {
                        table.getItems().add(new ReturnPurchaseReport(
                            rs.getString("invoiceNumber"),
                            rs.getString("invoiceDate"),
                            rs.getString("supplierName"),
                            rs.getString("totalAmount"),
                            rs.getString("discountAmount"),
                            rs.getString("paidAmount")
                        ));
                        count++;
                    }
                    System.out.println("ReturnPurchaseReport rows loaded: " + count);
                    if (count == 0) {
                        errorLabel.setText("No data found for selected date range.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading data: " + ex.getMessage());
            }
        });

        // Optionally, trigger filter on load
        filterBtn.fire();

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel, table);
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
        HBox dateRangeBox = createDateRangeFilter();

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Profit report table - maps to View_Profit_Report
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
        
        // Sample data - in real app, fetch from View_Profit_Report
        ObservableList<ProfitReport> data = FXCollections.observableArrayList(
            new ProfitReport("INV-SL-001", "2025-07-05", "15000.00", "10000.00", "5000.00"),
            new ProfitReport("INV-SL-002", "2025-07-06", "25000.00", "18000.00", "7000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, table);
        return form;
    }

    private static VBox createSummaryReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Summary Report");

        // Date range filters
        HBox dateRangeBox = createDateRangeFilter();

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Summary report - maps to View_Summary_Report
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(20);
        summaryGrid.setVgap(10);
        summaryGrid.setPadding(new Insets(15));

        // Add summary items
        addSummaryItem(summaryGrid, 0, "Total Purchases:", "27500.00");
        addSummaryItem(summaryGrid, 1, "Total Sales:", "40000.00");
        addSummaryItem(summaryGrid, 2, "Total Purchase Returns:", "5000.00");
        addSummaryItem(summaryGrid, 3, "Total Sales Returns:", "3000.00");
        addSummaryItem(summaryGrid, 4, "Net Purchases:", "22500.00");
        addSummaryItem(summaryGrid, 5, "Net Sales:", "37000.00");
        addSummaryItem(summaryGrid, 6, "Gross Profit:", "14500.00");

        form.getChildren().addAll(heading, dateRangeBox, buttons, summaryGrid);
        return form;
    }

    private static VBox createBalanceSheet() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Balance Sheet");

        // Date picker for as-of date
        HBox dateBox = new HBox(10);
        Label asOfLabel = new Label("As of Date:");
        DatePicker asOfDatePicker = new DatePicker();
        asOfDatePicker.setValue(LocalDate.now());
        Button updateBtn = createActionButton("Update");
        dateBox.getChildren().addAll(asOfLabel, asOfDatePicker, updateBtn);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Balance sheet - maps to View_Balance_Sheet
        GridPane balanceGrid = new GridPane();
        balanceGrid.setHgap(20);
        balanceGrid.setVgap(10);
        balanceGrid.setPadding(new Insets(15));

        // Assets
        Label assetsLabel = new Label("Assets");
        assetsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        balanceGrid.add(assetsLabel, 0, 0);

        addBalanceSheetItem(balanceGrid, 1, "Cash Balance:", "15000.00");
        addBalanceSheetItem(balanceGrid, 2, "Bank Balance:", "600000.00");
        addBalanceSheetItem(balanceGrid, 3, "Inventory:", "250000.00");
        addBalanceSheetItem(balanceGrid, 4, "Total Assets:", "865000.00");

        // Liabilities
        Label liabilitiesLabel = new Label("Liabilities");
        liabilitiesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        balanceGrid.add(liabilitiesLabel, 0, 5);

        addBalanceSheetItem(balanceGrid, 6, "Accounts Payable:", "75000.00");
        addBalanceSheetItem(balanceGrid, 7, "Employee Loans:", "15000.00");
        addBalanceSheetItem(balanceGrid, 8, "Total Liabilities:", "90000.00");

        // Equity
        Label equityLabel = new Label("Equity");
        equityLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        balanceGrid.add(equityLabel, 0, 9);

        addBalanceSheetItem(balanceGrid, 10, "Owner's Equity:", "775000.00");
        addBalanceSheetItem(balanceGrid, 11, "Total Liabilities & Equity:", "865000.00");

        form.getChildren().addAll(heading, dateBox, buttons, balanceGrid);
        return form;
    }

    private static VBox createCustomersReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Customers General Report");

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Customers report table - maps to View_Customers_General_Report
        TableView<CustomerReport> table = new TableView<>();
        
        TableColumn<CustomerReport, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        
        TableColumn<CustomerReport, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        TableColumn<CustomerReport, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        table.getColumns().addAll(nameCol, phoneCol, addressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data - in real app, fetch from View_Customers_General_Report
        ObservableList<CustomerReport> data = FXCollections.observableArrayList(
            new CustomerReport("Ali Traders", "03001234567", "Model Town, Lahore"),
            new CustomerReport("Pak Electric House", "03111234567", "Gulshan-e-Iqbal, Karachi")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, buttons, table);
        return form;
    }

    private static VBox createSuppliersReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Suppliers General Report");

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Suppliers report table - maps to View_Suppliers_General_Report
        TableView<SupplierReport> table = new TableView<>();
        
        TableColumn<SupplierReport, String> nameCol = new TableColumn<>("Supplier Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        
        TableColumn<SupplierReport, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        TableColumn<SupplierReport, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        table.getColumns().addAll(nameCol, phoneCol, addressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data - in real app, fetch from View_Suppliers_General_Report
        ObservableList<SupplierReport> data = FXCollections.observableArrayList(
            new SupplierReport("RawMetals Pvt Ltd", "03221234567", "Model Town, Lahore"),
            new SupplierReport("Insulation Depot", "03331234567", "Gulshan-e-Iqbal, Karachi")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, buttons, table);
        return form;
    }

    private static VBox createAreaWiseReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Area-Wise Customer/Supplier Report");

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Area-wise report table - maps to View_Area_Wise_Customer_Supplier_Report
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
        
        // Sample data - in real app, fetch from View_Area_Wise_Customer_Supplier_Report
        ObservableList<AreaWiseReport> data = FXCollections.observableArrayList(
            new AreaWiseReport("Customer", "Ali Traders", "Model Town", "Lahore", "Punjab"),
            new AreaWiseReport("Supplier", "RawMetals Pvt Ltd", "Model Town", "Lahore", "Punjab"),
            new AreaWiseReport("Customer", "Pak Electric House", "Gulshan-e-Iqbal", "Karachi", "Sindh")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, buttons, table);
        return form;
    }

    private static VBox createBrandSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Brand-Wise Sales Report");

        // Date range filters
        HBox dateRangeBox = createDateRangeFilter();

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Brand sales report table - maps to View_Brand_Wise_Salesman_Sales_Report
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
        
        // Sample data - in real app, fetch from View_Brand_Wise_Salesman_Sales_Report
        ObservableList<BrandSalesReport> data = FXCollections.observableArrayList(
            new BrandSalesReport("Imran Khan", "PowerFlex", "50", "37500.00"),
            new BrandSalesReport("Ali Shah", "SafeWire", "30", "36000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, table);
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

    private static VBox createAttendanceReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Attendance Report");

        // Date range filters
        HBox dateRangeBox = createDateRangeFilter();

        // Action buttons
        HBox buttons = createReportActionButtons();

        // Attendance report table - maps to View_Attendance_Report
        TableView<AttendanceReport> table = new TableView<>();
        
        TableColumn<AttendanceReport, String> nameCol = new TableColumn<>("Employee");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        
        TableColumn<AttendanceReport, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("attendanceDate"));
        
        TableColumn<AttendanceReport, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<AttendanceReport, String> hoursCol = new TableColumn<>("Hours");
        hoursCol.setCellValueFactory(new PropertyValueFactory<>("workingHours"));
        
        table.getColumns().addAll(nameCol, dateCol, statusCol, hoursCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data - in real app, fetch from View_Attendance_Report
        ObservableList<AttendanceReport> data = FXCollections.observableArrayList(
            new AttendanceReport("Zahid Khan", "2025-07-01", "present", "8"),
            new AttendanceReport("Zahid Khan", "2025-07-02", "present", "8"),
            new AttendanceReport("Faisal Mehmood", "2025-07-01", "absent", "0")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, table);
        return form;
    }

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