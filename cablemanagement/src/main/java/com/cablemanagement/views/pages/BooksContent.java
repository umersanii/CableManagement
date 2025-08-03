package com.cablemanagement.views.pages;

import com.cablemanagement.config;
import com.cablemanagement.invoice.*;
import com.cablemanagement.model.*;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooksContent {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String EXPORT_PATH = "exports"; // Configure as needed

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createPurchaseBookForm());

        HBox buttonBar = createButtonBar(formArea);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        configureScrollPane(scrollPane);

        mainLayout.setTop(scrollPane);
        mainLayout.setCenter(formArea);

        return mainLayout;
    }

    private static HBox createButtonBar(StackPane formArea) {
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        String[] buttonLabels = {
            "Purchase Book",
            "Return Purchase Book",
            "Raw Stock Usage Book",
            "Production Book",
            "Return Production Book",
            "Sales Book",
            "Return Sales Book"
        };

        Runnable[] actions = {
            () -> formArea.getChildren().setAll(createPurchaseBookForm()),
            () -> formArea.getChildren().setAll(createReturnPurchaseBookForm()),
            () -> formArea.getChildren().setAll(createRawStockUsageBookForm()),
            () -> formArea.getChildren().setAll(createProductionBookForm()),
            () -> formArea.getChildren().setAll(createReturnProductionBookForm()),
            () -> formArea.getChildren().setAll(createSalesBookForm()),
            () -> formArea.getChildren().setAll(createReturnSalesBookForm())
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

    private static VBox createSection(String title, String description) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.TOP_LEFT);
        box.getStyleClass().add("form-container");

        Label heading = new Label(title);
        heading.getStyleClass().add("form-heading");

        Label note = new Label(description);
        note.getStyleClass().add("form-subheading");

        box.getChildren().addAll(heading, note);
        return box;
    }

    private static HBox createFormRow(String labelText, Control field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        HBox row = new HBox(10, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");
        return row;
    }

    private static Button createSubmitButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("form-submit");
        return button;
    }

    private static Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        return button;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static VBox createPurchaseBookForm() {
        VBox form = createSection("Purchase Book", "View and manage purchase records.");
        ObservableList<PurchaseRecord> data = FXCollections.observableArrayList();
        TableView<PurchaseRecord> table = createPurchaseTable(data);

        ComboBox<String> supplierFilter = createSupplierComboBox();
        HBox filters = createFilterControls(supplierFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter));
        printBtn.setOnAction(e -> printReport("PurchaseBook", table.getItems()));

        form.getChildren().addAll(filters, buttons, table);
        loadPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter);
        return form;
    }

    private static VBox createReturnPurchaseBookForm() {
        VBox form = createSection("Return Purchase Book", "View and manage return purchase records.");
        ObservableList<ReturnPurchaseRecord> data = FXCollections.observableArrayList();
        TableView<ReturnPurchaseRecord> table = createReturnPurchaseTable(data);

        ComboBox<String> supplierFilter = createSupplierComboBox();
        HBox filters = createFilterControls(supplierFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadReturnPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter));
        printBtn.setOnAction(e -> printReport("ReturnPurchaseBook", table.getItems()));

        form.getChildren().addAll(filters, buttons, table);
        loadReturnPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter);
        return form;
    }

    private static VBox createRawStockUsageBookForm() {
        VBox form = createSection("Raw Stock Usage Book", "View and manage raw stock usage records.");
        ObservableList<RawStockRecord> data = FXCollections.observableArrayList();
        TableView<RawStockRecord> table = createRawStockTable(data);

        ComboBox<String> itemFilter = createItemComboBox();
        HBox filters = createFilterControls(itemFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadRawStockData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), itemFilter));
        printBtn.setOnAction(e -> printReport("RawStockUsageBook", table.getItems()));

        form.getChildren().addAll(filters, buttons, table);
        loadRawStockData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), itemFilter);
        return form;
    }

    private static VBox createProductionBookForm() {
        VBox form = createSection("Production Book", "View and manage production records.");
        ObservableList<ProductionRecord> data = FXCollections.observableArrayList();
        TableView<ProductionRecord> table = createProductionTable(data);

        ComboBox<String> productFilter = createProductComboBox();
        HBox filters = createFilterControls(productFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadProductionData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), productFilter));
        printBtn.setOnAction(e -> printReport("ProductionBook", table.getItems()));

        form.getChildren().addAll(filters, buttons, table);
        loadProductionData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), productFilter);
        return form;
    }

    private static VBox createReturnProductionBookForm() {
        VBox form = createSection("Return Production Book", "View and manage return production records.");
        ObservableList<ReturnProductionRecord> data = FXCollections.observableArrayList();
        TableView<ReturnProductionRecord> table = createReturnProductionTable(data);

        HBox filters = createFilterControls(null);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadReturnProductionData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker")));
        printBtn.setOnAction(e -> printReport("ReturnProductionBook", table.getItems()));

        form.getChildren().addAll(filters, buttons, table);
        loadReturnProductionData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"));
        return form;
    }

    private static VBox createSalesBookForm() {
        VBox form = createSection("Sales Book", "View and manage sales records.");
        ObservableList<SalesRecord> data = FXCollections.observableArrayList();
        TableView<SalesRecord> table = createSalesTable(data);

        ComboBox<String> customerFilter = createCustomerComboBox();
        HBox filters = createFilterControls(customerFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadSalesData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), customerFilter));
        
        // Enhanced print functionality
        printBtn.setOnAction(e -> {
            SalesRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                showAlert("No Selection", "Please select an invoice to print");
                return;
            }

            try {
                // Get detailed invoice data from database
                String invoiceNumber = selectedRecord.getInvoiceNumber();
                // Get sales invoice ID first
                int salesInvoiceId = config.database.getSalesInvoiceIdByNumber(invoiceNumber);
                if (salesInvoiceId == -1) {
                    showAlert("Error", "Invoice " + invoiceNumber + " not found");
                    return;
                }
                
                List<Object[]> invoiceItems = config.database.getSalesInvoiceItemsByInvoiceId(salesInvoiceId);
                if (invoiceItems.isEmpty()) {
                    showAlert("Error", "No items found for invoice " + invoiceNumber);
                    return;
                }

                // Convert to Item objects for printing
                List<Item> printItems = new ArrayList<>();
                for (Object[] item : invoiceItems) {
                    String productName = item[1].toString();
                    double quantity = Double.parseDouble(item[2].toString());
                    double unitPrice = Double.parseDouble(item[3].toString());
                    printItems.add(new Item(productName, (int)quantity, unitPrice, 0.0));
                }

                // Create invoice data object
                InvoiceData invoiceData = new InvoiceData(
                    invoiceNumber,
                    selectedRecord.getDate(),
                    selectedRecord.getCustomer(),
                    "Customer Address - " + selectedRecord.getCustomer(), // You might want to get actual address
                    0.0, // Previous balance
                    printItems
                );

                // Open invoice for print preview
                boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Sales");
                
                if (!previewSuccess) {
                    // Fallback to printer selection if preview fails
                    boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Sales");
                    if (!printSuccess) {
                        showAlert("Error", "Failed to print invoice " + invoiceNumber);
                    }
                }
            } catch (Exception ex) {
                showAlert("Error", "Failed to prepare invoice for printing: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        form.getChildren().addAll(filters, buttons, table);
        loadSalesData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), customerFilter);
        return form;
    }

    private static VBox createReturnSalesBookForm() {
        VBox form = createSection("Return Sales Book", "View and manage return sales records.");
        ObservableList<ReturnSalesRecord> data = FXCollections.observableArrayList();
        TableView<ReturnSalesRecord> table = createReturnSalesTable(data);

        ComboBox<String> customerFilter = createCustomerComboBox();
        HBox filters = createFilterControls(customerFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadReturnSalesData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), customerFilter));
        
        // Enhanced print functionality for return sales
        printBtn.setOnAction(e -> {
            ReturnSalesRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                showAlert("No Selection", "Please select a return invoice to print");
                return;
            }

            try {
                String returnInvoiceNumber = selectedRecord.getReturnInvoice();
                int salesReturnInvoiceId = config.database.getSalesReturnInvoiceIdByNumber(returnInvoiceNumber);
                if (salesReturnInvoiceId == -1) {
                    showAlert("Error", "Return Invoice " + returnInvoiceNumber + " not found");
                    return;
                }

                List<Object[]> returnInvoiceItems = config.database.getSalesReturnInvoiceItemsByInvoiceId(salesReturnInvoiceId);
                if (returnInvoiceItems.isEmpty()) {
                    showAlert("Error", "No items found for return invoice " + returnInvoiceNumber);
                    return;
                }

                // Convert to Item objects for printing
                List<Item> printItems = new ArrayList<>();
                for (Object[] item : returnInvoiceItems) {
                    String productName = item[1].toString(); // product_name
                    double quantity = Double.parseDouble(item[2].toString()); // quantity
                    double unitPrice = Double.parseDouble(item[3].toString()); // unit_price
                    printItems.add(new Item(productName, (int)quantity, unitPrice, 0.0));
                }

                // Create invoice data object for return invoice
                InvoiceData invoiceData = new InvoiceData(
                    returnInvoiceNumber,
                    selectedRecord.getDate(),
                    selectedRecord.getCustomer(),
                    "Customer Address - " + selectedRecord.getCustomer(), // Could be enhanced to get actual address
                    0.0, // Previous balance
                    printItems
                );

                // Open return invoice for print preview
                boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Return Sales");
                
                if (!previewSuccess) {
                    // Fallback to printer selection if preview fails
                    boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Return Sales");
                    if (!printSuccess) {
                        showAlert("Error", "Failed to print return invoice " + returnInvoiceNumber);
                    }
                }
            } catch (Exception ex) {
                showAlert("Error", "Failed to prepare return invoice for printing: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        form.getChildren().addAll(filters, buttons, table);
        loadReturnSalesData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), customerFilter);
        return form;
    }

    // Table creation methods
@SuppressWarnings("unchecked")
private static TableView<PurchaseRecord> createPurchaseTable(ObservableList<PurchaseRecord> data) {
    TableView<PurchaseRecord> table = new TableView<>();
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Invoice Number Column
    TableColumn<PurchaseRecord, String> invCol = new TableColumn<>("Invoice No");
    invCol.setCellValueFactory(cellData -> cellData.getValue().invoiceNumberProperty());
    
    // Date Column
    TableColumn<PurchaseRecord, String> dateCol = new TableColumn<>("Date");
    dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
    
    // Supplier Column
    TableColumn<PurchaseRecord, String> supplierCol = new TableColumn<>("Supplier");
    supplierCol.setCellValueFactory(cellData -> cellData.getValue().supplierProperty());
    
    // Amount Column
    TableColumn<PurchaseRecord, Double> amountCol = new TableColumn<>("Amount");
    amountCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getAmount()));
    
    // Discount Column
    TableColumn<PurchaseRecord, Double> discountCol = new TableColumn<>("Discount");
    discountCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getDiscount()));
    
    // Paid Column
    TableColumn<PurchaseRecord, Double> paidCol = new TableColumn<>("Paid");
    paidCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getPaid()));

    table.getColumns().addAll(invCol, dateCol, supplierCol, amountCol, discountCol, paidCol);
    table.setItems(data);
    return table;
}
    @SuppressWarnings("unchecked")
    private static TableView<ReturnPurchaseRecord> createReturnPurchaseTable(ObservableList<ReturnPurchaseRecord> data) {
        TableView<ReturnPurchaseRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Return Invoice Number Column
        TableColumn<ReturnPurchaseRecord, String> returnInvCol = new TableColumn<>("Return Invoice");
        returnInvCol.setCellValueFactory(cellData -> cellData.getValue().returnInvoiceProperty());

        // Date Column
        TableColumn<ReturnPurchaseRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        // Supplier Column
        TableColumn<ReturnPurchaseRecord, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(cellData -> cellData.getValue().supplierProperty());

        // Item Name Column
        TableColumn<ReturnPurchaseRecord, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(cellData -> cellData.getValue().itemNameProperty());

        // Brand Column
        TableColumn<ReturnPurchaseRecord, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(cellData -> cellData.getValue().brandNameProperty());

        // Quantity Column
        TableColumn<ReturnPurchaseRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));

        // Unit Price Column
        TableColumn<ReturnPurchaseRecord, Double> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getUnitPrice()));

        // Total Amount Column
        TableColumn<ReturnPurchaseRecord, Double> amountCol = new TableColumn<>("Total Amount");
        amountCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getTotalAmount()));

        table.getColumns().addAll(returnInvCol, dateCol, supplierCol, itemCol, brandCol, qtyCol, unitPriceCol, amountCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<RawStockRecord> createRawStockTable(ObservableList<RawStockRecord> data) {
        TableView<RawStockRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RawStockRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<RawStockRecord, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(cellData -> cellData.getValue().itemProperty());

        TableColumn<RawStockRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));

        TableColumn<RawStockRecord, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(cellData -> cellData.getValue().referenceProperty());

        table.getColumns().addAll(dateCol, itemCol, qtyCol, refCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<ProductionRecord> createProductionTable(ObservableList<ProductionRecord> data) {
        TableView<ProductionRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductionRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<ProductionRecord, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(cellData -> cellData.getValue().productProperty());

        TableColumn<ProductionRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        TableColumn<ProductionRecord, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(cellData -> cellData.getValue().notesProperty());

        table.getColumns().addAll(dateCol, productCol, qtyCol, notesCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<ReturnProductionRecord> createReturnProductionTable(ObservableList<ReturnProductionRecord> data) {
        TableView<ReturnProductionRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReturnProductionRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<ReturnProductionRecord, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(cellData -> cellData.getValue().referenceProperty());

        TableColumn<ReturnProductionRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        table.getColumns().addAll(dateCol, refCol, qtyCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<SalesRecord> createSalesTable(ObservableList<SalesRecord> data) {
        TableView<SalesRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SalesRecord, String> invoiceCol = new TableColumn<>("Invoice No");
        invoiceCol.setCellValueFactory(cellData -> cellData.getValue().invoiceNumberProperty());

        TableColumn<SalesRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<SalesRecord, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cellData -> cellData.getValue().customerProperty());

        TableColumn<SalesRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());

        TableColumn<SalesRecord, Double> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(cellData -> cellData.getValue().discountProperty().asObject());

        TableColumn<SalesRecord, Double> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(cellData -> cellData.getValue().paidProperty().asObject());

        table.getColumns().addAll(invoiceCol, dateCol, customerCol, amountCol, discountCol, paidCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<ReturnSalesRecord> createReturnSalesTable(ObservableList<ReturnSalesRecord> data) {
        TableView<ReturnSalesRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReturnSalesRecord, String> returnInvoiceCol = new TableColumn<>("Return Invoice");
        returnInvoiceCol.setCellValueFactory(cellData -> cellData.getValue().returnInvoiceProperty());

        TableColumn<ReturnSalesRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<ReturnSalesRecord, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cellData -> cellData.getValue().customerProperty());

        TableColumn<ReturnSalesRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getAmount()));

        TableColumn<ReturnSalesRecord, String> originalInvoiceCol = new TableColumn<>("Original Invoice");
        originalInvoiceCol.setCellValueFactory(cellData -> cellData.getValue().originalInvoiceProperty());

        table.getColumns().addAll(returnInvoiceCol, dateCol, customerCol, amountCol, originalInvoiceCol);
        table.setItems(data);
        return table;
    }

    // Data loading methods
private static void loadPurchaseData(TableView<PurchaseRecord> table, DatePicker fromDatePicker, 
                                    DatePicker toDatePicker, ComboBox<String> supplierFilter) {
    ObservableList<PurchaseRecord> data = FXCollections.observableArrayList();
    try {
        Map<String, String> filters = new HashMap<>();
        if (fromDatePicker.getValue() != null) {
            filters.put("fromDate", fromDatePicker.getValue().format(DATE_FORMATTER));
        }
        if (toDatePicker.getValue() != null) {
            filters.put("toDate", toDatePicker.getValue().format(DATE_FORMATTER));
        }
        if (supplierFilter.getValue() != null && !supplierFilter.getValue().isEmpty()) {
            filters.put("supplier_name", supplierFilter.getValue()); // Use supplier_name for View_Purchase_Book
        }

        List<Object[]> result = config.database.getViewData("View_Purchase_Book", filters);
        System.out.println("View_Purchase_Book results: " + result.size() + " rows");

        // Use a set to track seen invoice numbers and skip duplicates
        java.util.HashSet<String> seenInvoiceIds = new java.util.HashSet<>();
        for (Object[] row : result) {
            String invoiceId = row[0] != null ? row[0].toString() : "";
            if (seenInvoiceIds.contains(invoiceId)) {
            continue; // Skip duplicate invoice ids
            }
            seenInvoiceIds.add(invoiceId);

            data.add(new PurchaseRecord(
            invoiceId, // raw_purchase_invoice_id
            row[1] != null ? row[1].toString() : "", // invoice_number
            row[2] != null ? row[2].toString() : "", // supplier_name
            row[3] != null ? row[3].toString() : "", // invoice_date (string)
            row[4] != null ? row[4].toString() : "", // item_name
            row[5] != null ? row[5].toString() : "", // brand_name
            row[6] != null ? row[6].toString() : "", // manufacturer_name
            row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0, // quantity
            row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0, // unit_price
            row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0, // item_total
            row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0, // total_amount
            row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0, // discount_amount
            row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0, // paid_amount
            row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0 // balance_due
            ));
        }
    } catch (Exception e) {
        showAlert("Database Error", "Failed to load purchase data: " + e.getMessage());
        e.printStackTrace();
    }

    table.setItems(data);

    // Debug output
    System.out.println("Table items count: " + table.getItems().size());
    if (!table.getItems().isEmpty()) {
        System.out.println("First record: " + table.getItems().get(0).getInvoiceNumber());
    }
}
private static void loadReturnPurchaseData(TableView<ReturnPurchaseRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> supplierFilter) {
    ObservableList<ReturnPurchaseRecord> data = FXCollections.observableArrayList();
    try {
        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) {
            filters.put("fromDate", fromDate.getValue().format(DATE_FORMATTER));
        }
        if (toDate.getValue() != null) {
            filters.put("toDate", toDate.getValue().format(DATE_FORMATTER));
        }
        if (supplierFilter.getValue() != null && !supplierFilter.getValue().isEmpty() && !supplierFilter.getValue().equals("All Suppliers")) {
            filters.put("supplier_name", supplierFilter.getValue());
        }

        List<Object[]> result = config.database.getViewData("View_Return_Purchase_Book", filters);
        System.out.println("View_Return_Purchase_Book results: " + result.size() + " rows");

        // Use a set to track seen invoice numbers and skip duplicates
        java.util.HashSet<String> seenInvoiceIds = new java.util.HashSet<>();
        for (Object[] row : result) {
            String invoiceId = row[0] != null ? row[0].toString() : "";
            if (seenInvoiceIds.contains(invoiceId)) {
                continue; // Skip duplicate invoice ids
            }
            seenInvoiceIds.add(invoiceId);

            data.add(new ReturnPurchaseRecord(
                invoiceId, // raw_purchase_invoice_id (return_invoice_id)
                row[1] != null ? row[1].toString() : "", // invoice_number (return_invoice_number)
                row[2] != null ? row[2].toString() : "", // supplier_name
                row[3] != null ? row[3].toString() : "", // invoice_date (return_date)
                row[4] != null ? row[4].toString() : "", // item_name
                row[5] != null ? row[5].toString() : "", // brand_name
                row[6] != null ? row[6].toString() : "", // manufacturer_name
                row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0, // quantity (return_quantity)
                row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0, // unit_price
                row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0, // item_total
                row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0, // total_amount (total_return_amount)
                row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0, // discount_amount
                row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0, // paid_amount
                row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0 // balance
            ));
        }
    } catch (Exception e) {
        showAlert("Database Error", "Failed to load return purchase data: " + e.getMessage());
        e.printStackTrace();
    }

    table.setItems(data);

    // Debug output
    System.out.println("Return Purchase Table items count: " + table.getItems().size());
    if (!table.getItems().isEmpty()) {
        System.out.println("First return record: " + table.getItems().get(0).getReturnInvoice());
    }
}

    private static void loadRawStockData(TableView<RawStockRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> itemFilter) {
        ObservableList<RawStockRecord> data = FXCollections.observableArrayList();
        try {
            Map<String, String> filters = new HashMap<>();
            if (fromDate.getValue() != null) {
                filters.put("fromDate", fromDate.getValue().format(DATE_FORMATTER));
            }
            if (toDate.getValue() != null) {
                filters.put("toDate", toDate.getValue().format(DATE_FORMATTER));
            }
            if (itemFilter.getValue() != null && !itemFilter.getValue().isEmpty() && !itemFilter.getValue().equals("All Items")) {
                filters.put("item_name", itemFilter.getValue());
            }

            List<Object[]> result = config.database.getViewData("View_Raw_Stock_Book", filters);
            System.out.println("View_Raw_Stock_Book results: " + result.size() + " rows");

            // Use a set to track seen invoice numbers and skip duplicates
            java.util.HashSet<String> seenInvoiceIds = new java.util.HashSet<>();
            for (Object[] row : result) {
                String invoiceId = row[0] != null ? row[0].toString() : "";
                if (seenInvoiceIds.contains(invoiceId)) {
                    continue; // Skip duplicate invoice ids
                }
                seenInvoiceIds.add(invoiceId);

                data.add(new RawStockRecord(
                    row[3] != null ? row[3].toString() : "", // invoice_date (usage_date)
                    row[4] != null ? row[4].toString() : "", // item_name
                    row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0, // quantity (quantity_used)
                    row[1] != null ? row[1].toString() : "" // invoice_number (use_invoice_number) as reference
                ));
            }
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load raw stock usage data: " + e.getMessage());
            e.printStackTrace();
        }

        table.setItems(data);

        // Debug output
        System.out.println("Raw Stock Usage Table items count: " + table.getItems().size());
        if (!table.getItems().isEmpty()) {
            System.out.println("First usage record: " + table.getItems().get(0).getReference());
        }
    }

    private static void loadProductionData(TableView<ProductionRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> productFilter) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) {
            filters.put("production_date", fromDate.getValue().format(DATE_FORMATTER));
            System.out.println("From date filter: " + fromDate.getValue().format(DATE_FORMATTER));
        }
        if (toDate.getValue() != null) {
            filters.put("production_date", toDate.getValue().format(DATE_FORMATTER));
            System.out.println("To date filter: " + toDate.getValue().format(DATE_FORMATTER));
        }
        if (productFilter.getValue() != null && !productFilter.getValue().equals("All Products")) {
            filters.put("product_name", productFilter.getValue());
            System.out.println("Product filter: " + productFilter.getValue());
        }

        System.out.println("Executing query for View_Production_Book");
        // Use production_date instead of date in the filter
        List<Object[]> rows = config.database.getViewData("View_Production_Book", filters);
        System.out.println("Retrieved " + (rows != null ? rows.size() : 0) + " rows from View_Production_Book");
        
        ObservableList<ProductionRecord> data = FXCollections.observableArrayList();
        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null) {
                    System.out.println("Processing row: " + java.util.Arrays.toString(row));
                    data.add(new ProductionRecord(
                        row[1] != null ? row[1].toString() : "", // production_date
                        row[2] != null ? row[2].toString() : "", // product_name
                        row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0, // quantity_produced
                        row[8] != null ? row[8].toString() : "" // notes
                    ));
                }
            }
        }
        table.setItems(data);
    }

    private static void loadReturnProductionData(TableView<ReturnProductionRecord> table, DatePicker fromDate, DatePicker toDate) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) {
            filters.put("return_date", fromDate.getValue().format(DATE_FORMATTER));
            System.out.println("From date filter: " + fromDate.getValue().format(DATE_FORMATTER));
        }
        if (toDate.getValue() != null) {
            filters.put("return_date", toDate.getValue().format(DATE_FORMATTER));
            System.out.println("To date filter: " + toDate.getValue().format(DATE_FORMATTER));
        }

        System.out.println("Loading return production data with filters: " + filters);
        System.out.println("Executing query for View_Return_Production_Book");
        List<Object[]> rows = config.database.getViewData("View_Return_Production_Book", filters);
        System.out.println("Retrieved " + (rows != null ? rows.size() : 0) + " rows from View_Return_Production_Book");

        ObservableList<ReturnProductionRecord> data = FXCollections.observableArrayList();
        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null) {
                    System.out.println("Processing row: " + java.util.Arrays.toString(row));
                    data.add(new ReturnProductionRecord(
                        row[2] != null ? row[2].toString() : "", // return_date
                        row[1] != null ? row[1].toString() : "", // return_invoice_number
                        row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0 // quantity_returned
                    ));
                }
            }
        }
        table.setItems(data);
    }

    private static void loadSalesData(TableView<SalesRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> customerFilter) {
        ObservableList<SalesRecord> data = FXCollections.observableArrayList();
        try {
            Map<String, String> filters = new HashMap<>();
            if (fromDate.getValue() != null) {
                filters.put("fromDate", fromDate.getValue().format(DATE_FORMATTER));
            }
            if (toDate.getValue() != null) {
                filters.put("toDate", toDate.getValue().format(DATE_FORMATTER));
            }
            if (customerFilter.getValue() != null && !customerFilter.getValue().isEmpty() && !customerFilter.getValue().equals("All Customers")) {
                filters.put("customer_name", customerFilter.getValue());
            }

            // Get all sales invoices directly since View_Sales_Book doesn't exist
            List<Object[]> result = null;
            try {
                result = config.database.getAllSalesInvoices();
                System.out.println("Direct sales invoice results: " + result.size() + " rows");
            } catch (Exception e) {
                System.err.println("Failed to load sales invoices: " + e.getMessage());
                result = new ArrayList<>();
            }

            // Filter the results manually based on date and customer filters
            List<Object[]> filteredResult = new ArrayList<>();
            for (Object[] row : result) {
                // getAllSalesInvoices() returns: sales_invoice_number, sales_date, customer_name, total_amount, discount_amount, paid_amount
                String salesDate = row[1] != null ? row[1].toString() : "";
                String customerName = row[2] != null ? row[2].toString() : "";
                
                // Apply date filtering
                boolean passesDateFilter = true;
                if (fromDate != null && toDate != null && !salesDate.isEmpty()) {
                    try {
                        LocalDate invoiceDate = LocalDate.parse(salesDate);
                        LocalDate fromLocalDate = fromDate.getValue();
                        LocalDate toLocalDate = toDate.getValue();
                        if (fromLocalDate != null && toLocalDate != null) {
                            passesDateFilter = !invoiceDate.isBefore(fromLocalDate) && !invoiceDate.isAfter(toLocalDate);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing date: " + salesDate);
                        passesDateFilter = false;
                    }
                }
                
                // Apply customer filtering
                boolean passesCustomerFilter = true;
                if (customerFilter.getValue() != null && !customerFilter.getValue().isEmpty() && !customerFilter.getValue().equals("All Customers")) {
                    String filterValue = customerFilter.getValue().toLowerCase();
                    passesCustomerFilter = customerName.toLowerCase().contains(filterValue);
                }
                
                if (passesDateFilter && passesCustomerFilter) {
                    filteredResult.add(row);
                }
            }
            
            System.out.println("Filtered sales invoice results: " + filteredResult.size() + " rows");

            // Use a set to track seen invoice numbers and skip duplicates
            java.util.HashSet<String> seenInvoiceNumbers = new java.util.HashSet<>();
            for (Object[] row : filteredResult) {
                // getAllSalesInvoices() format: sales_invoice_number, sales_date, customer_name, total_amount, discount_amount, paid_amount
                String invoiceNumber = row[0] != null ? row[0].toString() : "";
                if (seenInvoiceNumbers.contains(invoiceNumber)) {
                    continue; // Skip duplicate invoice numbers
                }
                seenInvoiceNumbers.add(invoiceNumber);

                String customerName = row[2] != null ? row[2].toString() : "";
                String salesDate = row[1] != null ? row[1].toString() : "";
                double totalAmount = row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0;
                double discountAmount = row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0;
                double paidAmount = row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0;

                // Data is already filtered above, so just add to the results
                data.add(new SalesRecord(
                    invoiceNumber,
                    salesDate,
                    customerName,
                    totalAmount,
                    discountAmount,
                    paidAmount
                ));
            }
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load sales data: " + e.getMessage());
            e.printStackTrace();
        }

        table.setItems(data);

        // Debug output
        System.out.println("Sales Table items count: " + table.getItems().size());
        if (!table.getItems().isEmpty()) {
            System.out.println("First sales record: " + table.getItems().get(0).getInvoiceNumber());
        }
    }

    private static void loadReturnSalesData(TableView<ReturnSalesRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> customerFilter) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        ObservableList<ReturnSalesRecord> data = FXCollections.observableArrayList();
        try {
            System.out.println("Loading return sales data...");

            // Get all return sales invoices directly since View_Return_Sales_Book doesn't exist
            List<Object[]> result = null;
            try {
                result = config.database.getAllSalesReturnInvoices();
                System.out.println("Direct return sales invoice results: " + result.size() + " rows");
            } catch (Exception e) {
                System.err.println("Failed to load return sales invoices: " + e.getMessage());
                result = new ArrayList<>();
            }

            // Filter the results manually based on date and customer filters
            List<Object[]> filteredResult = new ArrayList<>();
            for (Object[] row : result) {
                // getAllSalesReturnInvoices() returns: return_invoice_number, return_date, customer_name, total_return_amount, sales_invoice_number
                String returnDate = row[1] != null ? row[1].toString() : "";
                String customerName = row[2] != null ? row[2].toString() : "";
                
                // Apply date filtering
                boolean passesDateFilter = true;
                if (fromDate != null && toDate != null && !returnDate.isEmpty()) {
                    try {
                        LocalDate invoiceDate = LocalDate.parse(returnDate);
                        LocalDate fromLocalDate = fromDate.getValue();
                        LocalDate toLocalDate = toDate.getValue();
                        if (fromLocalDate != null && toLocalDate != null) {
                            passesDateFilter = !invoiceDate.isBefore(fromLocalDate) && !invoiceDate.isAfter(toLocalDate);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing date: " + returnDate);
                        passesDateFilter = false;
                    }
                }
                
                // Apply customer filtering
                boolean passesCustomerFilter = true;
                if (customerFilter.getValue() != null && !customerFilter.getValue().isEmpty() && !customerFilter.getValue().equals("All Customers")) {
                    String filterValue = customerFilter.getValue().toLowerCase();
                    passesCustomerFilter = customerName.toLowerCase().contains(filterValue);
                }
                
                if (passesDateFilter && passesCustomerFilter) {
                    filteredResult.add(row);
                }
            }
            
            System.out.println("Filtered return sales invoice results: " + filteredResult.size() + " rows");

            // Use a set to track seen return invoice numbers and skip duplicates
            java.util.HashSet<String> seenReturnInvoiceNumbers = new java.util.HashSet<>();
            for (Object[] row : filteredResult) {
                // getAllSalesReturnInvoices() format: return_invoice_number, return_date, customer_name, total_return_amount, sales_invoice_number
                String returnInvoiceNumber = row[0] != null ? row[0].toString() : "";
                if (seenReturnInvoiceNumbers.contains(returnInvoiceNumber)) {
                    continue; // Skip duplicate return invoice numbers
                }
                seenReturnInvoiceNumbers.add(returnInvoiceNumber);

                String customerName = row[2] != null ? row[2].toString() : "";
                String returnDate = row[1] != null ? row[1].toString() : "";
                double totalReturnAmount = row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0;
                String originalInvoiceNumber = row[4] != null ? row[4].toString() : "";

                // Data is already filtered above, so just add to the results
                data.add(new ReturnSalesRecord(
                    returnInvoiceNumber,
                    returnDate,
                    customerName,
                    totalReturnAmount,
                    originalInvoiceNumber
                ));
            }
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load return sales data: " + e.getMessage());
            e.printStackTrace();
        }

        table.setItems(data);
        System.out.println("Return Sales Table items count: " + data.size());
        if (!data.isEmpty()) {
            System.out.println("First return sales record: " + data.get(0).getReturnInvoice());
        }
    }

    private static ComboBox<String> createSupplierComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText("All Suppliers");
        if (config.database != null && config.database.isConnected()) {
            List<Supplier> suppliers = config.database.getAllSuppliers();
            ObservableList<String> items = FXCollections.observableArrayList("All Suppliers");
            for (Supplier supplier : suppliers) {
                items.add(supplier.nameProperty().get());
            }
            comboBox.setItems(items);
        }
        comboBox.getStyleClass().add("combo-box");
        return comboBox;
    }

    private static ComboBox<String> createCustomerComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText("All Customers");
        if (config.database != null && config.database.isConnected()) {
            List<Customer> customers = config.database.getAllCustomers();
            ObservableList<String> items = FXCollections.observableArrayList("All Customers");
            for (Customer customer : customers) {
                items.add(customer.nameProperty().get());
            }
            comboBox.setItems(items);
        }
        comboBox.getStyleClass().add("combo-box");
        return comboBox;
    }

    private static ComboBox<String> createItemComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText("All Items");
        if (config.database != null && config.database.isConnected()) {
            List<Object[]> items = config.database.getAllRawStock();
            ObservableList<String> names = FXCollections.observableArrayList("All Items");
            for (Object[] item : items) {
                names.add(item[1].toString());
            }
            comboBox.setItems(names);
        }
        comboBox.getStyleClass().add("combo-box");
        return comboBox;
    }

    private static ComboBox<String> createProductComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText("All Products");
        if (config.database != null && config.database.isConnected()) {
            List<Object[]> products = config.database.getAllProductionStock();
            ObservableList<String> names = FXCollections.observableArrayList("All Products");
            for (Object[] product : products) {
                names.add(product[1].toString());
            }
            comboBox.setItems(names);
        }
        comboBox.getStyleClass().add("combo-box");
        return comboBox;
    }

    private static HBox createFilterControls(ComboBox<String> filterCombo) {
        HBox filters = new HBox(20);
        filters.setAlignment(Pos.CENTER_LEFT);

        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        fromDate.getStyleClass().add("date-picker");
        DatePicker toDate = new DatePicker(LocalDate.now());
        toDate.getStyleClass().add("date-picker");

        List<Node> children = new ArrayList<>();
        children.add(createFormRow("From:", fromDate));
        children.add(createFormRow("To:", toDate));
        if (filterCombo != null) {
            children.add(createFormRow(filterCombo.getPromptText().replace("All ", "") + ":", filterCombo));
        }

        filters.getChildren().addAll(children);
        return filters;
    }

private static void exportReport(String reportName, ObservableList<?> data) {
    if (data == null || data.isEmpty()) {
        showAlert("Error", "No data to export! Please generate the report first.");
        return;
    }

    File exportDir = new File(EXPORT_PATH);
    if (!exportDir.exists()) {
        exportDir.mkdirs();
    }

    String filename = EXPORT_PATH + File.separator + reportName + "_" + 
                     LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
    
    try {
        InvoiceData invoiceData = createInvoiceData(reportName, data);
        if (invoiceData.getItems() == null || invoiceData.getItems().isEmpty()) {
            showAlert("Error", "No items to export in the report!");
            return;
        }
        System.out.println("invoice data size: " + invoiceData.getItems().size());
        for (Item item : invoiceData.getItems()) {
            System.out.println("Item: " + item.getName() + ", Qty: " + item.getQuantity() + 
                              ", Price: " + item.getUnitPrice() + ", Discount: " + item.getDiscountPercent());
        }
        
        // Use specialized invoice generators for Sales and Return Sales books
        if (reportName.equals("SalesBook")) {
            SalesInvoiceGenerator.generateSalesInvoicePDF(invoiceData, filename);
        } else if (reportName.equals("ReturnSalesBook")) {
            SalesInvoiceGenerator.generateReturnSalesInvoicePDF(invoiceData, filename);
        } else {
            // Use default generator for other reports
            InvoiceGenerator.generatePDF(invoiceData, filename);
        }
        
        showAlert("Success", "Report successfully exported to:\n" + new File(filename).getAbsolutePath());
    } catch (Exception e) {
        showAlert("Error", "Failed to export report: " + e.getMessage());
        e.printStackTrace();
    }
}
    private static void printReport(String reportName, ObservableList<?> data) {
        if (data.isEmpty()) {
            showAlert("Error", "No data to print!");
            return;
        }

        String filename = EXPORT_PATH + reportName + "_" + System.currentTimeMillis() + ".pdf";
        InvoiceData invoiceData = createInvoiceData(reportName, data);
        try {
            // Use specialized invoice generators for Sales and Return Sales books
            if (reportName.equals("SalesBook")) {
                SalesInvoiceGenerator.generateSalesInvoicePDF(invoiceData, filename);
            } else if (reportName.equals("ReturnSalesBook")) {
                SalesInvoiceGenerator.generateReturnSalesInvoicePDF(invoiceData, filename);
            } else {
                // Use default generator for other reports
                InvoiceGenerator.generatePDF(invoiceData, filename);
            }
            
            // Use the new print functionality
            boolean printSuccess = InvoiceGenerator.printPDF(filename);
            
            if (printSuccess) {
                showAlert("Success", "Report sent to printer successfully!");
            } else {
                showAlert("Warning", "Report was generated but printing failed. Check your printer connection.\n" +
                         "Report saved to: " + filename);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to print report: " + e.getMessage());
        }
    }

private static InvoiceData createInvoiceData(String reportName, ObservableList<?> data) {
    List<Item> items = new ArrayList<>();
    String customerName = "General Report";
    String customerAddress = "N/A";
    String invoiceNumber = reportName + "_Report"; // Generic identifier for the report
    String date = LocalDate.now().format(DATE_FORMATTER);
    double previousBalance = 0.0;

    switch (reportName) {
        case "PurchaseBook":
            for (Object record : data) {
                PurchaseRecord pr = (PurchaseRecord) record;
                double discountPercentage = pr.getAmount() != 0 ? pr.getDiscount() / pr.getAmount() * 100 : 0.0;
                items.add(new Item(
                    pr.getSupplierName() + " (Invoice: " + pr.getInvoiceNumber() + ")",
                    1, 
                    pr.getAmount(), 
                    discountPercentage
                ));
                System.out.println("Item: " + pr.getItemName() + ", Brand: " + pr.getBrandName() + 
                                  ", Manufacturer: " + pr.getManufacturerName() + 
                                  ", Qty: " + pr.getQuantity() + ", Unit Price: " + pr.getUnitPrice() + 
                                  ", Item Total: " + pr.getItemTotal());
                // Use first invoice number if needed
                if (items.size() == 1) {
                    invoiceNumber = pr.getInvoiceNumber();
                }
            }
            break;
        case "ReturnPurchaseBook":
            for (Object record : data) {
                ReturnPurchaseRecord rpr = (ReturnPurchaseRecord) record;
                double discountPercentage = rpr.getTotalAmount() != 0 ? rpr.getDiscountAmount() / rpr.getTotalAmount() * 100 : 0.0;
                items.add(new Item(
                    rpr.getItemName() + " - " + rpr.getBrandName() + " (Return Invoice: " + rpr.getReturnInvoice() + ")",
                    (int) rpr.getQuantity(), 
                    rpr.getUnitPrice(), 
                    discountPercentage
                ));
                System.out.println("Return Item: " + rpr.getItemName() + ", Brand: " + rpr.getBrandName() + 
                                  ", Supplier: " + rpr.getSupplier() + 
                                  ", Qty: " + rpr.getQuantity() + ", Unit Price: " + rpr.getUnitPrice() + 
                                  ", Total: " + rpr.getTotalAmount());
                if (items.size() == 1) {
                    invoiceNumber = rpr.getReturnInvoice();
                }
            }
            break;
        case "RawStockUsageBook":
            for (Object record : data) {
                RawStockRecord rsr = (RawStockRecord) record;
                items.add(new Item(
                    rsr.getItem() + " (Ref: " + rsr.getReference() + ")",
                    (int) rsr.getQuantity(), 
                    0.0, 
                    0.0
                ));
                if (items.size() == 1) {
                    invoiceNumber = rsr.getReference();
                }
            }
            break;
        case "ProductionBook":
            for (Object record : data) {
                ProductionRecord pr = (ProductionRecord) record;
                items.add(new Item(
                    pr.getProduct() + " (Notes: " + pr.getNotes() + ")",
                    (int) pr.getQuantity(), 
                    0.0, 
                    0.0
                ));
                if (items.size() == 1) {
                    invoiceNumber = reportName + "_Report"; // No specific invoice number
                }
            }
            break;
        case "ReturnProductionBook":
            for (Object record : data) {
                ReturnProductionRecord rpr = (ReturnProductionRecord) record;
                items.add(new Item(
                    "Return (Ref: " + rpr.getReference() + ")",
                    (int) rpr.getQuantity(), 
                    0.0, 
                    0.0
                ));
                if (items.size() == 1) {
                    invoiceNumber = rpr.getReference();
                }
            }
            break;
        case "SalesBook":
            // For Sales Book, extract customer info from the first record
            if (!data.isEmpty()) {
                SalesRecord firstRecord = (SalesRecord) data.get(0);
                customerName = firstRecord.getCustomer();
                customerAddress = "Customer Address"; // Could be enhanced to get actual address
            }
            
            for (Object record : data) {
                SalesRecord sr = (SalesRecord) record;
                double discountPercentage = sr.getAmount() != 0 ? sr.getDiscount() / sr.getAmount() * 100 : 0.0;
                items.add(new Item(
                    "Sales Transaction - Invoice: " + sr.getInvoiceNumber() + " (Customer: " + sr.getCustomer() + ")",
                    1, 
                    sr.getAmount(), 
                    discountPercentage
                ));
                if (items.size() == 1) {
                    invoiceNumber = sr.getInvoiceNumber();
                }
            }
            break;
        case "ReturnSalesBook":
            // For Return Sales Book, extract customer info from the first record
            if (!data.isEmpty()) {
                ReturnSalesRecord firstRecord = (ReturnSalesRecord) data.get(0);
                customerName = firstRecord.getCustomer();
                customerAddress = "Customer Address"; // Could be enhanced to get actual address
            }
            
            for (Object record : data) {
                ReturnSalesRecord rsr = (ReturnSalesRecord) record;
                items.add(new Item(
                    "Return Transaction - Return Invoice: " + rsr.getReturnInvoice() + " (Customer: " + rsr.getCustomer() + ")" + 
                    (rsr.getOriginalInvoice() != null && !rsr.getOriginalInvoice().isEmpty() ? 
                     " [Original Invoice: " + rsr.getOriginalInvoice() + "]" : ""),
                    1, 
                    rsr.getAmount(), 
                    0.0
                ));
                if (items.size() == 1) {
                    invoiceNumber = rsr.getReturnInvoice();
                }
            }
            break;
        default:
            break;
    }

    System.out.println("Invoice Data:");
    System.out.println("Invoice Number: " + invoiceNumber);
    System.out.println("Date: " + date);
    System.out.println("Customer Name: " + customerName);
    System.out.println("Customer Address: " + customerAddress);
    System.out.println("Previous Balance: " + previousBalance);
    System.out.println("Items:");
    for (Item item : items) {
        System.out.println("  Name: " + item.getName() +
                          ", Quantity: " + item.getQuantity() +
                          ", Unit Price: " + item.getUnitPrice() +
                          ", Discount %: " + item.getDiscountPercent());
    }
    return new InvoiceData(invoiceNumber, date, customerName, customerAddress, previousBalance, items);
}
       
    // Assumed PurchaseRecord class
static class PurchaseRecord {
    private final SimpleStringProperty rawPurchaseInvoiceId;
    private final SimpleStringProperty invoiceNumber;
    private final SimpleStringProperty supplierName;
    private final SimpleStringProperty invoiceDate;
    private final SimpleStringProperty itemName;
    private final SimpleStringProperty brandName;
    private final SimpleStringProperty manufacturerName;
    private final SimpleDoubleProperty quantity;
    private final SimpleDoubleProperty unitPrice;
    private final SimpleDoubleProperty itemTotal;
    private final SimpleDoubleProperty totalAmount;
    private final SimpleDoubleProperty discountAmount;
    private final SimpleDoubleProperty paidAmount;
    private final SimpleDoubleProperty balanceDue;

    public PurchaseRecord(String rawPurchaseInvoiceId, String invoiceNumber, String supplierName, 
                         String invoiceDate, String itemName, String brandName, String manufacturerName,
                         double quantity, double unitPrice, double itemTotal, double totalAmount,
                         double discountAmount, double paidAmount, double balanceDue) {
        this.rawPurchaseInvoiceId = new SimpleStringProperty(rawPurchaseInvoiceId);
        this.invoiceNumber = new SimpleStringProperty(invoiceNumber);
        this.supplierName = new SimpleStringProperty(supplierName);
        this.invoiceDate = new SimpleStringProperty(invoiceDate);
        this.itemName = new SimpleStringProperty(itemName);
        this.brandName = new SimpleStringProperty(brandName);
        this.manufacturerName = new SimpleStringProperty(manufacturerName);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.itemTotal = new SimpleDoubleProperty(itemTotal);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.discountAmount = new SimpleDoubleProperty(discountAmount);
        this.paidAmount = new SimpleDoubleProperty(paidAmount);
        this.balanceDue = new SimpleDoubleProperty(balanceDue);
    }

    // Property methods for TableView columns
    public SimpleStringProperty invoiceNumberProperty() { return invoiceNumber; }
    public SimpleStringProperty dateProperty() { return invoiceDate; }
    public SimpleStringProperty supplierProperty() { return supplierName; }

    // Getters (used by TableView columns)
    public String getRawPurchaseInvoiceId() { return rawPurchaseInvoiceId.get(); }
    public String getInvoiceNumber() { return invoiceNumber.get(); }
    public String getSupplierName() { return supplierName.get(); }
    public String getInvoiceDate() { return invoiceDate.get(); }
    public String getItemName() { return itemName.get(); }
    public String getBrandName() { return brandName.get(); }
    public String getManufacturerName() { return manufacturerName.get(); }
    public Double getQuantity() { return quantity.get(); }
    public Double getUnitPrice() { return unitPrice.get(); }
    public Double getItemTotal() { return itemTotal.get(); }
    public Double getTotalAmount() { return totalAmount.get(); }
    public Double getDiscountAmount() { return discountAmount.get(); }
    public Double getPaidAmount() { return paidAmount.get(); }
    public Double getBalanceDue() { return balanceDue.get(); }

    // Additional getters for TableView columns
    public Double getAmount() { return getTotalAmount(); }
    public Double getDiscount() { return getDiscountAmount(); }
    public Double getPaid() { return getPaidAmount(); }
}



    static class ReturnPurchaseRecord {
        private final StringProperty returnInvoiceId = new SimpleStringProperty();
        private final StringProperty returnInvoice = new SimpleStringProperty();
        private final StringProperty supplier = new SimpleStringProperty();
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty itemName = new SimpleStringProperty();
        private final StringProperty brandName = new SimpleStringProperty();
        private final StringProperty manufacturerName = new SimpleStringProperty();
        private final SimpleDoubleProperty quantity = new SimpleDoubleProperty();
        private final SimpleDoubleProperty unitPrice = new SimpleDoubleProperty();
        private final SimpleDoubleProperty itemTotal = new SimpleDoubleProperty();
        private final SimpleDoubleProperty totalAmount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty discountAmount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty paidAmount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty balance = new SimpleDoubleProperty();

        // Constructor for backwards compatibility (5 parameters)
        ReturnPurchaseRecord(String returnInvoice, String date, String supplier, String originalInvoice, double amount) {
            this.returnInvoiceId.set("");
            this.returnInvoice.set(returnInvoice);
            this.supplier.set(supplier);
            this.date.set(date);
            this.itemName.set(originalInvoice); // Using as original invoice for backward compatibility
            this.brandName.set("");
            this.manufacturerName.set("");
            this.quantity.set(1.0);
            this.unitPrice.set(amount);
            this.itemTotal.set(amount);
            this.totalAmount.set(amount);
            this.discountAmount.set(0.0);
            this.paidAmount.set(amount);
            this.balance.set(0.0);
        }

        // Full constructor (14 parameters)
        ReturnPurchaseRecord(String returnInvoiceId, String returnInvoice, String supplier, String date, 
                           String itemName, String brandName, String manufacturerName, 
                           double quantity, double unitPrice, double itemTotal, 
                           double totalAmount, double discountAmount, double paidAmount, double balance) {
            this.returnInvoiceId.set(returnInvoiceId);
            this.returnInvoice.set(returnInvoice);
            this.supplier.set(supplier);
            this.date.set(date);
            this.itemName.set(itemName);
            this.brandName.set(brandName);
            this.manufacturerName.set(manufacturerName);
            this.quantity.set(quantity);
            this.unitPrice.set(unitPrice);
            this.itemTotal.set(itemTotal);
            this.totalAmount.set(totalAmount);
            this.discountAmount.set(discountAmount);
            this.paidAmount.set(paidAmount);
            this.balance.set(balance);
        }

        // Property getters
        StringProperty returnInvoiceIdProperty() { return returnInvoiceId; }
        StringProperty returnInvoiceProperty() { return returnInvoice; }
        StringProperty supplierProperty() { return supplier; }
        StringProperty dateProperty() { return date; }
        StringProperty itemNameProperty() { return itemName; }
        StringProperty brandNameProperty() { return brandName; }
        StringProperty manufacturerNameProperty() { return manufacturerName; }
        SimpleDoubleProperty quantityProperty() { return quantity; }
        SimpleDoubleProperty unitPriceProperty() { return unitPrice; }
        SimpleDoubleProperty itemTotalProperty() { return itemTotal; }
        SimpleDoubleProperty totalAmountProperty() { return totalAmount; }
        SimpleDoubleProperty discountAmountProperty() { return discountAmount; }
        SimpleDoubleProperty paidAmountProperty() { return paidAmount; }
        SimpleDoubleProperty balanceProperty() { return balance; }

        // Backward compatibility properties
        StringProperty originalInvoiceProperty() { return itemName; } // For backward compatibility
        SimpleDoubleProperty amountProperty() { return totalAmount; } // For backward compatibility

        // Value getters
        String getReturnInvoiceId() { return returnInvoiceId.get(); }
        String getReturnInvoice() { return returnInvoice.get(); }
        String getSupplier() { return supplier.get(); }
        String getDate() { return date.get(); }
        String getItemName() { return itemName.get(); }
        String getBrandName() { return brandName.get(); }
        String getManufacturerName() { return manufacturerName.get(); }
        double getQuantity() { return quantity.get(); }
        double getUnitPrice() { return unitPrice.get(); }
        double getItemTotal() { return itemTotal.get(); }
        double getTotalAmount() { return totalAmount.get(); }
        double getDiscountAmount() { return discountAmount.get(); }
        double getPaidAmount() { return paidAmount.get(); }
        double getBalance() { return balance.get(); }

        // Backward compatibility getters
        String getOriginalInvoice() { return itemName.get(); } // For backward compatibility
        double getAmount() { return totalAmount.get(); } // For backward compatibility
    }

    static class RawStockRecord {
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty item = new SimpleStringProperty();
        private final SimpleDoubleProperty quantity = new SimpleDoubleProperty();
        private final StringProperty reference = new SimpleStringProperty();

        RawStockRecord(String date, String item, double quantity, String reference) {
            this.date.set(date);
            this.item.set(item);
            this.quantity.set(quantity);
            this.reference.set(reference);
        }

        StringProperty dateProperty() { return date; }
        StringProperty itemProperty() { return item; }
        SimpleDoubleProperty quantityProperty() { return quantity; }
        StringProperty referenceProperty() { return reference; }
        String getDate() { return date.get(); }
        String getItem() { return item.get(); }
        double getQuantity() { return quantity.get(); }
        String getReference() { return reference.get(); }
    }

    static class ProductionRecord {
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty product = new SimpleStringProperty();
        private final SimpleDoubleProperty quantity = new SimpleDoubleProperty();
        private final StringProperty notes = new SimpleStringProperty();

        ProductionRecord(String date, String product, double quantity, String notes) {
            this.date.set(date);
            this.product.set(product);
            this.quantity.set(quantity);
            this.notes.set(notes);
        }

        StringProperty dateProperty() { return date; }
        StringProperty productProperty() { return product; }
        SimpleDoubleProperty quantityProperty() { return quantity; }
        StringProperty notesProperty() { return notes; }
        String getDate() { return date.get(); }
        String getProduct() { return product.get(); }
        double getQuantity() { return quantity.get(); }
        String getNotes() { return notes.get(); }
    }

    static class ReturnProductionRecord {
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty reference = new SimpleStringProperty();
        private final SimpleDoubleProperty quantity = new SimpleDoubleProperty();

        ReturnProductionRecord(String date, String reference, double quantity) {
            this.date.set(date);
            this.reference.set(reference);
            this.quantity.set(quantity);
        }

        StringProperty dateProperty() { return date; }
        StringProperty referenceProperty() { return reference; }
        SimpleDoubleProperty quantityProperty() { return quantity; }
        String getDate() { return date.get(); }
        String getReference() { return reference.get(); }
        double getQuantity() { return quantity.get(); }
    }

    static class SalesRecord {
        private final StringProperty invoiceNumber = new SimpleStringProperty();
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty customer = new SimpleStringProperty();
        private final SimpleDoubleProperty amount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty discount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty paid = new SimpleDoubleProperty();

        SalesRecord(String invoiceNumber, String date, String customer, double amount, double discount, double paid) {
            this.invoiceNumber.set(invoiceNumber);
            this.date.set(date);
            this.customer.set(customer);
            this.amount.set(amount);
            this.discount.set(discount);
            this.paid.set(paid);
        }

        StringProperty invoiceNumberProperty() { return invoiceNumber; }
        StringProperty dateProperty() { return date; }
        StringProperty customerProperty() { return customer; }
        SimpleDoubleProperty amountProperty() { return amount; }
        SimpleDoubleProperty discountProperty() { return discount; }
        SimpleDoubleProperty paidProperty() { return paid; }
        String getInvoiceNumber() { return invoiceNumber.get(); }
        String getDate() { return date.get(); }
        String getCustomer() { return customer.get(); }
        double getAmount() { return amount.get(); }
        double getDiscount() { return discount.get(); }
        double getPaid() { return paid.get(); }
    }

    static class ReturnSalesRecord {
        private final StringProperty returnInvoice = new SimpleStringProperty();
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty customer = new SimpleStringProperty();
        private final SimpleDoubleProperty amount = new SimpleDoubleProperty();
        private final StringProperty originalInvoice = new SimpleStringProperty();

        ReturnSalesRecord(String returnInvoice, String date, String customer, double amount, String originalInvoice) {
            this.returnInvoice.set(returnInvoice);
            this.date.set(date);
            this.customer.set(customer);
            this.amount.set(amount);
            this.originalInvoice.set(originalInvoice);
        }

        StringProperty returnInvoiceProperty() { return returnInvoice; }
        StringProperty dateProperty() { return date; }
        StringProperty customerProperty() { return customer; }
        SimpleDoubleProperty amountProperty() { return amount; }
        StringProperty originalInvoiceProperty() { return originalInvoice; }
        String getReturnInvoice() { return returnInvoice.get(); }
        String getDate() { return date.get(); }
        String getCustomer() { return customer.get(); }
        double getAmount() { return amount.get(); }
        String getOriginalInvoice() { return originalInvoice.get(); }
    }
}