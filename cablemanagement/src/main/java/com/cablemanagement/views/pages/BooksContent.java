package com.cablemanagement.views.pages;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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

public class BooksContent {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
            "Raw Stock Book",
            "Return Raw Stock Book",
            "Production Book",
            "Return Production Book",
            "Sales Book",
            "Return Sales Book"
        };

        Runnable[] actions = {
            () -> formArea.getChildren().setAll(createPurchaseBookForm()),
            () -> formArea.getChildren().setAll(createReturnPurchaseBookForm()),
            () -> formArea.getChildren().setAll(createRawStockBookForm()),
            () -> formArea.getChildren().setAll(createReturnRawStockBookForm()),
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

    private static VBox createPurchaseBookForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Purchase Book");

        // Filter controls
        HBox filters = new HBox(20);
        filters.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker toDate = new DatePicker(LocalDate.now());
        
        ComboBox<String> supplierFilter = new ComboBox<>();
        supplierFilter.setPromptText("All Suppliers");
        supplierFilter.getItems().addAll("All Suppliers", "RawMetals Pvt Ltd", "Insulation Depot");
        
        filters.getChildren().addAll(
            createFormRow("From:", fromDate),
            createFormRow("To:", toDate),
            createFormRow("Supplier:", supplierFilter)
        );

        // Action buttons
        HBox buttons = new HBox(10);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Report table
        TableView<PurchaseRecord> table = new TableView<>();
        
        TableColumn<PurchaseRecord, String> invCol = new TableColumn<>("Invoice No");
        invCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        
        TableColumn<PurchaseRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<PurchaseRecord, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        
        TableColumn<PurchaseRecord, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        TableColumn<PurchaseRecord, String> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));
        
        TableColumn<PurchaseRecord, String> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("paid"));
        
        table.getColumns().addAll(invCol, dateCol, supplierCol, amountCol, discountCol, paidCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data
        ObservableList<PurchaseRecord> data = FXCollections.observableArrayList(
            new PurchaseRecord("INV-001", "2023-01-05", "RawMetals Pvt Ltd", "25000.00", "1000.00", "20000.00"),
            new PurchaseRecord("INV-002", "2023-01-10", "Insulation Depot", "18000.00", "0.00", "18000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, filters, buttons, table);

        generateBtn.setOnAction(e -> {
            // In real app: query database with filters
            System.out.println("Generating report from " + fromDate.getValue() + 
                             " to " + toDate.getValue() + 
                             ", Supplier: " + supplierFilter.getValue());
        });

        printBtn.setOnAction(e -> {
            // Print functionality
            System.out.println("Printing purchase book...");
        });

        exportBtn.setOnAction(e -> {
            // Export functionality
            System.out.println("Exporting to Excel...");
        });

        return form;
    }

    private static VBox createReturnPurchaseBookForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Return Purchase Book");

        // Filter controls
        HBox filters = new HBox(20);
        filters.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker toDate = new DatePicker(LocalDate.now());
        
        ComboBox<String> supplierFilter = new ComboBox<>();
        supplierFilter.setPromptText("All Suppliers");
        supplierFilter.getItems().addAll("All Suppliers", "RawMetals Pvt Ltd", "Insulation Depot");
        
        filters.getChildren().addAll(
            createFormRow("From:", fromDate),
            createFormRow("To:", toDate),
            createFormRow("Supplier:", supplierFilter)
        );

        // Action buttons
        HBox buttons = new HBox(10);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Report table
        TableView<ReturnPurchaseRecord> table = new TableView<>();
        
        TableColumn<ReturnPurchaseRecord, String> returnInvCol = new TableColumn<>("Return Invoice");
        returnInvCol.setCellValueFactory(new PropertyValueFactory<>("returnInvoice"));
        
        TableColumn<ReturnPurchaseRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<ReturnPurchaseRecord, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        
        TableColumn<ReturnPurchaseRecord, String> origInvCol = new TableColumn<>("Original Invoice");
        origInvCol.setCellValueFactory(new PropertyValueFactory<>("originalInvoice"));
        
        TableColumn<ReturnPurchaseRecord, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        table.getColumns().addAll(returnInvCol, dateCol, supplierCol, origInvCol, amountCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data
        ObservableList<ReturnPurchaseRecord> data = FXCollections.observableArrayList(
            new ReturnPurchaseRecord("RINV-001", "2023-01-08", "RawMetals Pvt Ltd", "INV-001", "5000.00"),
            new ReturnPurchaseRecord("RINV-002", "2023-01-12", "Insulation Depot", "INV-002", "2000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, filters, buttons, table);

        return form;
    }

    private static VBox createRawStockBookForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Raw Stock Book");

        // Filter controls
        HBox filters = new HBox(20);
        filters.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker toDate = new DatePicker(LocalDate.now());
        
        ComboBox<String> itemFilter = new ComboBox<>();
        itemFilter.setPromptText("All Items");
        itemFilter.getItems().addAll("All Items", "Copper Wire 8mm", "PVC Granules");
        
        filters.getChildren().addAll(
            createFormRow("From:", fromDate),
            createFormRow("To:", toDate),
            createFormRow("Item:", itemFilter)
        );

        // Action buttons
        HBox buttons = new HBox(10);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Report table
        TableView<RawStockRecord> table = new TableView<>();
        
        TableColumn<RawStockRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<RawStockRecord, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("item"));
        
        TableColumn<RawStockRecord, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        TableColumn<RawStockRecord, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(new PropertyValueFactory<>("reference"));
        
        table.getColumns().addAll(dateCol, itemCol, qtyCol, refCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data
        ObservableList<RawStockRecord> data = FXCollections.observableArrayList(
            new RawStockRecord("2023-01-05", "Copper Wire 8mm", "50.00", "Production Batch 1"),
            new RawStockRecord("2023-01-06", "PVC Granules", "30.00", "Production Batch 1")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, filters, buttons, table);

        return form;
    }

    private static VBox createReturnRawStockBookForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Return Raw Stock Book");

        // Filter controls
        HBox filters = new HBox(20);
        filters.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker toDate = new DatePicker(LocalDate.now());
        
        ComboBox<String> supplierFilter = new ComboBox<>();
        supplierFilter.setPromptText("All Suppliers");
        supplierFilter.getItems().addAll("All Suppliers", "RawMetals Pvt Ltd", "Insulation Depot");
        
        filters.getChildren().addAll(
            createFormRow("From:", fromDate),
            createFormRow("To:", toDate),
            createFormRow("Supplier:", supplierFilter)
        );

        // Action buttons
        HBox buttons = new HBox(10);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Report table
        TableView<ReturnRawStockRecord> table = new TableView<>();
        
        TableColumn<ReturnRawStockRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<ReturnRawStockRecord, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("item"));
        
        TableColumn<ReturnRawStockRecord, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        TableColumn<ReturnRawStockRecord, String> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        
        TableColumn<ReturnRawStockRecord, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        
        table.getColumns().addAll(dateCol, itemCol, qtyCol, priceCol, supplierCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data
        ObservableList<ReturnRawStockRecord> data = FXCollections.observableArrayList(
            new ReturnRawStockRecord("2023-01-08", "Copper Wire 8mm", "20.00", "250.00", "RawMetals Pvt Ltd"),
            new ReturnRawStockRecord("2023-01-10", "PVC Granules", "10.00", "100.00", "Insulation Depot")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, filters, buttons, table);

        return form;
    }

    private static VBox createProductionBookForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Production Book");

        // Filter controls
        HBox filters = new HBox(20);
        filters.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker toDate = new DatePicker(LocalDate.now());
        
        ComboBox<String> productFilter = new ComboBox<>();
        productFilter.setPromptText("All Products");
        productFilter.getItems().addAll("All Products", "Copper Cable Roll 25m", "PVC Sheathed Wire 50m");
        
        filters.getChildren().addAll(
            createFormRow("From:", fromDate),
            createFormRow("To:", toDate),
            createFormRow("Product:", productFilter)
        );

        // Action buttons
        HBox buttons = new HBox(10);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Report table
        TableView<ProductionRecord> table = new TableView<>();
        
        TableColumn<ProductionRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<ProductionRecord, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        
        TableColumn<ProductionRecord, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        TableColumn<ProductionRecord, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        
        table.getColumns().addAll(dateCol, productCol, qtyCol, notesCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data
        ObservableList<ProductionRecord> data = FXCollections.observableArrayList(
            new ProductionRecord("2023-01-05", "Copper Cable Roll 25m", "10", "Batch #1"),
            new ProductionRecord("2023-01-06", "PVC Sheathed Wire 50m", "5", "Batch #1")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, filters, buttons, table);

        return form;
    }

    private static VBox createReturnProductionBookForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Return Production Book");

        // Filter controls
        HBox filters = new HBox(20);
        filters.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker toDate = new DatePicker(LocalDate.now());
        
        filters.getChildren().addAll(
            createFormRow("From:", fromDate),
            createFormRow("To:", toDate)
        );

        // Action buttons
        HBox buttons = new HBox(10);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Report table
        TableView<ReturnProductionRecord> table = new TableView<>();
        
        TableColumn<ReturnProductionRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<ReturnProductionRecord, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(new PropertyValueFactory<>("reference"));
        
        TableColumn<ReturnProductionRecord, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        table.getColumns().addAll(dateCol, refCol, qtyCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data
        ObservableList<ReturnProductionRecord> data = FXCollections.observableArrayList(
            new ReturnProductionRecord("2023-01-07", "Damaged goods", "3"),
            new ReturnProductionRecord("2023-01-09", "Wrong specification", "2")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, filters, buttons, table);

        return form;
    }

    private static VBox createSalesBookForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Sales Book");

        // Filter controls
        HBox filters = new HBox(20);
        filters.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker toDate = new DatePicker(LocalDate.now());
        
        ComboBox<String> customerFilter = new ComboBox<>();
        customerFilter.setPromptText("All Customers");
        customerFilter.getItems().addAll("All Customers", "Ali Traders", "Pak Electric House");
        
        filters.getChildren().addAll(
            createFormRow("From:", fromDate),
            createFormRow("To:", toDate),
            createFormRow("Customer:", customerFilter)
        );

        // Action buttons
        HBox buttons = new HBox(10);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Report table
        TableView<SalesRecord> table = new TableView<>();
        
        TableColumn<SalesRecord, String> invCol = new TableColumn<>("Invoice No");
        invCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        
        TableColumn<SalesRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<SalesRecord, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customer"));
        
        TableColumn<SalesRecord, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        TableColumn<SalesRecord, String> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));
        
        TableColumn<SalesRecord, String> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("paid"));
        
        table.getColumns().addAll(invCol, dateCol, customerCol, amountCol, discountCol, paidCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data
        ObservableList<SalesRecord> data = FXCollections.observableArrayList(
            new SalesRecord("SINV-001", "2023-01-05", "Ali Traders", "15000.00", "500.00", "14500.00"),
            new SalesRecord("SINV-002", "2023-01-08", "Pak Electric House", "22000.00", "0.00", "22000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, filters, buttons, table);

        return form;
    }

    private static VBox createReturnSalesBookForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Return Sales Book");

        // Filter controls
        HBox filters = new HBox(20);
        filters.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker toDate = new DatePicker(LocalDate.now());
        
        ComboBox<String> customerFilter = new ComboBox<>();
        customerFilter.setPromptText("All Customers");
        customerFilter.getItems().addAll("All Customers", "Ali Traders", "Pak Electric House");
        
        filters.getChildren().addAll(
            createFormRow("From:", fromDate),
            createFormRow("To:", toDate),
            createFormRow("Customer:", customerFilter)
        );

        // Action buttons
        HBox buttons = new HBox(10);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Report table
        TableView<ReturnSalesRecord> table = new TableView<>();
        
        TableColumn<ReturnSalesRecord, String> returnInvCol = new TableColumn<>("Return Invoice");
        returnInvCol.setCellValueFactory(new PropertyValueFactory<>("returnInvoice"));
        
        TableColumn<ReturnSalesRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<ReturnSalesRecord, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customer"));
        
        TableColumn<ReturnSalesRecord, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        table.getColumns().addAll(returnInvCol, dateCol, customerCol, amountCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data
        ObservableList<ReturnSalesRecord> data = FXCollections.observableArrayList(
            new ReturnSalesRecord("RSINV-001", "2023-01-07", "Ali Traders", "3000.00"),
            new ReturnSalesRecord("RSINV-002", "2023-01-10", "Pak Electric House", "1500.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, filters, buttons, table);

        return form;
    }

    // Model classes for table data
    public static class PurchaseRecord {
        private final String invoiceNumber;
        private final String date;
        private final String supplier;
        private final String amount;
        private final String discount;
        private final String paid;

        public PurchaseRecord(String invoiceNumber, String date, String supplier, 
                            String amount, String discount, String paid) {
            this.invoiceNumber = invoiceNumber;
            this.date = date;
            this.supplier = supplier;
            this.amount = amount;
            this.discount = discount;
            this.paid = paid;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getDate() { return date; }
        public String getSupplier() { return supplier; }
        public String getAmount() { return amount; }
        public String getDiscount() { return discount; }
        public String getPaid() { return paid; }
    }

    public static class ReturnPurchaseRecord {
        private final String returnInvoice;
        private final String date;
        private final String supplier;
        private final String originalInvoice;
        private final String amount;

        public ReturnPurchaseRecord(String returnInvoice, String date, String supplier, 
                                  String originalInvoice, String amount) {
            this.returnInvoice = returnInvoice;
            this.date = date;
            this.supplier = supplier;
            this.originalInvoice = originalInvoice;
            this.amount = amount;
        }

        public String getReturnInvoice() { return returnInvoice; }
        public String getDate() { return date; }
        public String getSupplier() { return supplier; }
        public String getOriginalInvoice() { return originalInvoice; }
        public String getAmount() { return amount; }
    }

    public static class RawStockRecord {
        private final String date;
        private final String item;
        private final String quantity;
        private final String reference;

        public RawStockRecord(String date, String item, String quantity, String reference) {
            this.date = date;
            this.item = item;
            this.quantity = quantity;
            this.reference = reference;
        }

        public String getDate() { return date; }
        public String getItem() { return item; }
        public String getQuantity() { return quantity; }
        public String getReference() { return reference; }
    }

    public static class ReturnRawStockRecord {
        private final String date;
        private final String item;
        private final String quantity;
        private final String unitPrice;
        private final String supplier;

        public ReturnRawStockRecord(String date, String item, String quantity, 
                                  String unitPrice, String supplier) {
            this.date = date;
            this.item = item;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.supplier = supplier;
        }

        public String getDate() { return date; }
        public String getItem() { return item; }
        public String getQuantity() { return quantity; }
        public String getUnitPrice() { return unitPrice; }
        public String getSupplier() { return supplier; }
    }

    public static class ProductionRecord {
        private final String date;
        private final String product;
        private final String quantity;
        private final String notes;

        public ProductionRecord(String date, String product, String quantity, String notes) {
            this.date = date;
            this.product = product;
            this.quantity = quantity;
            this.notes = notes;
        }

        public String getDate() { return date; }
        public String getProduct() { return product; }
        public String getQuantity() { return quantity; }
        public String getNotes() { return notes; }
    }

    public static class ReturnProductionRecord {
        private final String date;
        private final String reference;
        private final String quantity;

        public ReturnProductionRecord(String date, String reference, String quantity) {
            this.date = date;
            this.reference = reference;
            this.quantity = quantity;
        }

        public String getDate() { return date; }
        public String getReference() { return reference; }
        public String getQuantity() { return quantity; }
    }

    public static class SalesRecord {
        private final String invoiceNumber;
        private final String date;
        private final String customer;
        private final String amount;
        private final String discount;
        private final String paid;

        public SalesRecord(String invoiceNumber, String date, String customer, 
                         String amount, String discount, String paid) {
            this.invoiceNumber = invoiceNumber;
            this.date = date;
            this.customer = customer;
            this.amount = amount;
            this.discount = discount;
            this.paid = paid;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getDate() { return date; }
        public String getCustomer() { return customer; }
        public String getAmount() { return amount; }
        public String getDiscount() { return discount; }
        public String getPaid() { return paid; }
    }

    public static class ReturnSalesRecord {
        private final String returnInvoice;
        private final String date;
        private final String customer;
        private final String amount;

        public ReturnSalesRecord(String returnInvoice, String date, 
                               String customer, String amount) {
            this.returnInvoice = returnInvoice;
            this.date = date;
            this.customer = customer;
            this.amount = amount;
        }

        public String getReturnInvoice() { return returnInvoice; }
        public String getDate() { return date; }
        public String getCustomer() { return customer; }
        public String getAmount() { return amount; }
    }

    // Helper methods
    private static Label createHeading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-heading");
        label.setFont(Font.font(18));
        return label;
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
}