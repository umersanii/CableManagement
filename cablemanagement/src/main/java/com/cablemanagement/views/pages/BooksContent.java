package com.cablemanagement.views.pages;

import com.cablemanagement.config;
import com.cablemanagement.invoice.*;
import com.cablemanagement.model.*;
import com.cablemanagement.views.pages.BooksContent.ProductionRecord;
import com.cablemanagement.views.pages.BooksContent.ReturnProductionRecord;
import com.cablemanagement.views.pages.BooksContent.ReturnPurchaseRecord;
import com.cablemanagement.views.pages.BooksContent.ReturnRawStockRecord;
import com.cablemanagement.views.pages.BooksContent.ReturnSalesRecord;
import com.cablemanagement.views.pages.BooksContent.SalesRecord;

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
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        HBox buttons = new HBox(10, generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        generateBtn.setOnAction(e -> loadPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter));
        printBtn.setOnAction(e -> printReport("PurchaseBook", data));
        exportBtn.setOnAction(e -> exportReport("PurchaseBook", table.getItems()));

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
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        HBox buttons = new HBox(10, generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        generateBtn.setOnAction(e -> loadReturnPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter));
        printBtn.setOnAction(e -> printReport("ReturnPurchaseBook", data));
        exportBtn.setOnAction(e -> exportReport("ReturnPurchaseBook", data));

        form.getChildren().addAll(filters, buttons, table);
        loadReturnPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter);
        return form;
    }

    private static VBox createRawStockBookForm() {
        VBox form = createSection("Raw Stock Book", "View and manage raw stock usage records.");
        ObservableList<RawStockRecord> data = FXCollections.observableArrayList();
        TableView<RawStockRecord> table = createRawStockTable(data);

        ComboBox<String> itemFilter = createItemComboBox();
        HBox filters = createFilterControls(itemFilter);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        HBox buttons = new HBox(10, generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        generateBtn.setOnAction(e -> loadRawStockData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), itemFilter));
        printBtn.setOnAction(e -> printReport("RawStockBook", data));
        exportBtn.setOnAction(e -> exportReport("RawStockBook", data));

        form.getChildren().addAll(filters, buttons, table);
        loadRawStockData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), itemFilter);
        return form;
    }

    private static VBox createReturnRawStockBookForm() {
        VBox form = createSection("Return Raw Stock Book", "View and manage return raw stock records.");
        ObservableList<ReturnRawStockRecord> data = FXCollections.observableArrayList();
        TableView<ReturnRawStockRecord> table = createReturnRawStockTable(data);

        ComboBox<String> supplierFilter = createSupplierComboBox();
        HBox filters = createFilterControls(supplierFilter);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        HBox buttons = new HBox(10, generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        generateBtn.setOnAction(e -> loadReturnRawStockData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter));
        printBtn.setOnAction(e -> printReport("ReturnRawStockBook", data));
        exportBtn.setOnAction(e -> exportReport("ReturnRawStockBook", data));

        form.getChildren().addAll(filters, buttons, table);
        loadReturnRawStockData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter);
        return form;
    }

    private static VBox createProductionBookForm() {
        VBox form = createSection("Production Book", "View and manage production records.");
        ObservableList<ProductionRecord> data = FXCollections.observableArrayList();
        TableView<ProductionRecord> table = createProductionTable(data);

        ComboBox<String> productFilter = createProductComboBox();
        HBox filters = createFilterControls(productFilter);
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        HBox buttons = new HBox(10, generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        generateBtn.setOnAction(e -> loadProductionData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), productFilter));
        printBtn.setOnAction(e -> printReport("ProductionBook", data));
        exportBtn.setOnAction(e -> exportReport("ProductionBook", data));

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
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        HBox buttons = new HBox(10, generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        generateBtn.setOnAction(e -> loadReturnProductionData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker")));
        printBtn.setOnAction(e -> printReport("ReturnProductionBook", data));
        exportBtn.setOnAction(e -> exportReport("ReturnProductionBook", data));

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
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        HBox buttons = new HBox(10, generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        generateBtn.setOnAction(e -> loadSalesData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), customerFilter));
        printBtn.setOnAction(e -> printReport("SalesBook", data));
        exportBtn.setOnAction(e -> exportReport("SalesBook", data));

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
        Button generateBtn = createSubmitButton("Generate");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        HBox buttons = new HBox(10, generateBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        generateBtn.setOnAction(e -> loadReturnSalesData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), customerFilter));
        printBtn.setOnAction(e -> printReport("ReturnSalesBook", data));
        exportBtn.setOnAction(e -> exportReport("ReturnSalesBook", data));

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

        TableColumn<ReturnPurchaseRecord, String> returnInvCol = new TableColumn<>("Return Invoice");
        returnInvCol.setCellValueFactory(new PropertyValueFactory<>("returnInvoice"));

        TableColumn<ReturnPurchaseRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<ReturnPurchaseRecord, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));

        TableColumn<ReturnPurchaseRecord, String> origInvCol = new TableColumn<>("Original Invoice");
        origInvCol.setCellValueFactory(new PropertyValueFactory<>("originalInvoice"));

        TableColumn<ReturnPurchaseRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        table.getColumns().addAll(returnInvCol, dateCol, supplierCol, origInvCol, amountCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<RawStockRecord> createRawStockTable(ObservableList<RawStockRecord> data) {
        TableView<RawStockRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RawStockRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<RawStockRecord, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("item"));

        TableColumn<RawStockRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<RawStockRecord, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(new PropertyValueFactory<>("reference"));

        table.getColumns().addAll(dateCol, itemCol, qtyCol, refCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<ReturnRawStockRecord> createReturnRawStockTable(ObservableList<ReturnRawStockRecord> data) {
        TableView<ReturnRawStockRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReturnRawStockRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<ReturnRawStockRecord, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("item"));

        TableColumn<ReturnRawStockRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<ReturnRawStockRecord, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<ReturnRawStockRecord, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));

        table.getColumns().addAll(dateCol, itemCol, qtyCol, priceCol, supplierCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<ProductionRecord> createProductionTable(ObservableList<ProductionRecord> data) {
        TableView<ProductionRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductionRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<ProductionRecord, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));

        TableColumn<ProductionRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<ProductionRecord, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        table.getColumns().addAll(dateCol, productCol, qtyCol, notesCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<ReturnProductionRecord> createReturnProductionTable(ObservableList<ReturnProductionRecord> data) {
        TableView<ReturnProductionRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReturnProductionRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<ReturnProductionRecord, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(new PropertyValueFactory<>("reference"));

        TableColumn<ReturnProductionRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        table.getColumns().addAll(dateCol, refCol, qtyCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<SalesRecord> createSalesTable(ObservableList<SalesRecord> data) {
        TableView<SalesRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SalesRecord, String> invoiceCol = new TableColumn<>("Invoice No");
        invoiceCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));

        TableColumn<SalesRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<SalesRecord, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customer"));

        TableColumn<SalesRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<SalesRecord, Double> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));

        TableColumn<SalesRecord, Double> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("paid"));

        table.getColumns().addAll(invoiceCol, dateCol, customerCol, amountCol, discountCol, paidCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<ReturnSalesRecord> createReturnSalesTable(ObservableList<ReturnSalesRecord> data) {
        TableView<ReturnSalesRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReturnSalesRecord, String> returnInvoiceCol = new TableColumn<>("Return Invoice");
        returnInvoiceCol.setCellValueFactory(new PropertyValueFactory<>("returnInvoice"));

        TableColumn<ReturnSalesRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<ReturnSalesRecord, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customer"));

        TableColumn<ReturnSalesRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        table.getColumns().addAll(returnInvoiceCol, dateCol, customerCol, amountCol);
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
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) filters.put("return_date", ">= '" + fromDate.getValue().format(DATE_FORMATTER) + "'");
        if (toDate.getValue() != null) filters.put("return_date", "<= '" + toDate.getValue().format(DATE_FORMATTER) + "'");
        if (supplierFilter.getValue() != null && !supplierFilter.getValue().equals("All Suppliers")) {
            filters.put("supplier_name", "= '" + supplierFilter.getValue() + "'");
        }

        List<Object[]> rows = config.database.getViewData("View_Return_Purchase_Book", filters);
        ObservableList<ReturnPurchaseRecord> data = FXCollections.observableArrayList();
        for (Object[] row : rows) {
            data.add(new ReturnPurchaseRecord(
                row[0] != null ? row[0].toString() : "",
                row[1] != null ? row[1].toString() : "",
                row[2] != null ? row[2].toString() : "",
                row[4] != null ? row[4].toString() : "",
                row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0
            ));
        }
        table.setItems(data);
    }

    private static void loadRawStockData(TableView<RawStockRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> itemFilter) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) filters.put("usage_date", ">= '" + fromDate.getValue().format(DATE_FORMATTER) + "'");
        if (toDate.getValue() != null) filters.put("usage_date", "<= '" + toDate.getValue().format(DATE_FORMATTER) + "'");
        if (itemFilter.getValue() != null && !itemFilter.getValue().equals("All Items")) {
            filters.put("raw_stock_name", "= '" + itemFilter.getValue() + "'");
        }

        List<Object[]> rows = config.database.getViewData("View_Raw_Stock_Book", filters);
        ObservableList<RawStockRecord> data = FXCollections.observableArrayList();
        for (Object[] row : rows) {
            data.add(new RawStockRecord(
                row[0] != null ? row[0].toString() : "",
                row[1] != null ? row[1].toString() : "",
                row[2] != null ? Double.parseDouble(row[2].toString()) : 0.0,
                row[3] != null ? row[3].toString() : ""
            ));
        }
        table.setItems(data);
    }

    private static void loadReturnRawStockData(TableView<ReturnRawStockRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> supplierFilter) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) filters.put("return_date", ">= '" + fromDate.getValue().format(DATE_FORMATTER) + "'");
        if (toDate.getValue() != null) filters.put("return_date", "<= '" + toDate.getValue().format(DATE_FORMATTER) + "'");
        if (supplierFilter.getValue() != null && !supplierFilter.getValue().equals("All Suppliers")) {
            filters.put("supplier_name", "= '" + supplierFilter.getValue() + "'");
        }

        List<Object[]> rows = config.database.getViewData("View_Return_Raw_Stock_Book", filters);
        ObservableList<ReturnRawStockRecord> data = FXCollections.observableArrayList();
        for (Object[] row : rows) {
            data.add(new ReturnRawStockRecord(
                row[0] != null ? row[0].toString() : "",
                row[1] != null ? row[1].toString() : "",
                row[2] != null ? Double.parseDouble(row[2].toString()) : 0.0,
                row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0,
                row[4] != null ? row[4].toString() : ""
            ));
        }
        table.setItems(data);
    }

    private static void loadProductionData(TableView<ProductionRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> productFilter) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) filters.put("production_date", ">= '" + fromDate.getValue().format(DATE_FORMATTER) + "'");
        if (toDate.getValue() != null) filters.put("production_date", "<= '" + toDate.getValue().format(DATE_FORMATTER) + "'");
        if (productFilter.getValue() != null && !productFilter.getValue().equals("All Products")) {
            filters.put("production_stock_name", "= '" + productFilter.getValue() + "'");
        }

        List<Object[]> rows = config.database.getViewData("View_Production_Book", filters);
        ObservableList<ProductionRecord> data = FXCollections.observableArrayList();
        for (Object[] row : rows) {
            data.add(new ProductionRecord(
                row[0] != null ? row[0].toString() : "",
                row[1] != null ? row[1].toString() : "",
                row[2] != null ? Double.parseDouble(row[2].toString()) : 0.0,
                row[3] != null ? row[3].toString() : ""
            ));
        }
        table.setItems(data);
    }

    private static void loadReturnProductionData(TableView<ReturnProductionRecord> table, DatePicker fromDate, DatePicker toDate) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) filters.put("return_date", ">= '" + fromDate.getValue().format(DATE_FORMATTER) + "'");
        if (toDate.getValue() != null) filters.put("return_date", "<= '" + toDate.getValue().format(DATE_FORMATTER) + "'");

        List<Object[]> rows = config.database.getViewData("View_Return_Production_Book", filters);
        ObservableList<ReturnProductionRecord> data = FXCollections.observableArrayList();
        for (Object[] row : rows) {
            data.add(new ReturnProductionRecord(
                row[0] != null ? row[0].toString() : "",
                row[1] != null ? row[1].toString() : "",
                row[2] != null ? Double.parseDouble(row[2].toString()) : 0.0
            ));
        }
        table.setItems(data);
    }

    private static void loadSalesData(TableView<SalesRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> customerFilter) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) filters.put("sales_date", ">= '" + fromDate.getValue().format(DATE_FORMATTER) + "'");
        if (toDate.getValue() != null) filters.put("sales_date", "<= '" + toDate.getValue().format(DATE_FORMATTER) + "'");
        if (customerFilter.getValue() != null && !customerFilter.getValue().equals("All Customers")) {
            filters.put("customer_name", "= '" + customerFilter.getValue() + "'");
        }

        List<Object[]> rows = config.database.getViewData("View_Sales_Book", filters);
        ObservableList<SalesRecord> data = FXCollections.observableArrayList();
        for (Object[] row : rows) {
            data.add(new SalesRecord(
                row[0] != null ? row[0].toString() : "",
                row[1] != null ? row[1].toString() : "",
                row[2] != null ? row[2].toString() : "",
                row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0,
                row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0,
                row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0
            ));
        }
        table.setItems(data);
    }

    private static void loadReturnSalesData(TableView<ReturnSalesRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> customerFilter) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) filters.put("return_date", ">= '" + fromDate.getValue().format(DATE_FORMATTER) + "'");
        if (toDate.getValue() != null) filters.put("return_date", "<= '" + toDate.getValue().format(DATE_FORMATTER) + "'");
        if (customerFilter.getValue() != null && !customerFilter.getValue().equals("All Customers")) {
            filters.put("customer_name", "= '" + customerFilter.getValue() + "'");
        }

        List<Object[]> rows = config.database.getViewData("View_Return_Sales_Book", filters);
        ObservableList<ReturnSalesRecord> data = FXCollections.observableArrayList();
        for (Object[] row : rows) {
            data.add(new ReturnSalesRecord(
                row[0] != null ? row[0].toString() : "",
                row[1] != null ? row[1].toString() : "",
                row[2] != null ? row[2].toString() : "",
                row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0
            ));
        }
        table.setItems(data);
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

    // Create exports directory if it doesn't exist
    File exportDir = new File(EXPORT_PATH);
    if (!exportDir.exists()) {
        exportDir.mkdirs();
    }

    String filename = EXPORT_PATH + File.separator + reportName + "_" + 
                     LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
    
    try {
        InvoiceData invoiceData = createInvoiceData(reportName, data);
        InvoiceGenerator.generatePDF(invoiceData, filename);
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
            InvoiceGenerator.generatePDF(invoiceData, filename);
            InvoiceGenerator.printPDF(filename);
            showAlert("Success", "Report sent to printer!");
        } catch (Exception e) {
            showAlert("Error", "Failed to print report: " + e.getMessage());
        }
    }

    private static InvoiceData createInvoiceData(String reportName, ObservableList<?> data) {
    List<Item> items = new ArrayList<>();
    String customerName = "General Report";
    String customerAddress = "N/A";
    String invoiceNumber = reportName + "_" + System.currentTimeMillis();
    String date = LocalDate.now().format(DATE_FORMATTER);
    double previousBalance = 0.0;

    switch (reportName) {
        case "PurchaseBook":
            for (Object record : data) {
                PurchaseRecord pr = (PurchaseRecord) record;
                items.add(new Item(
                    pr.getSupplierName() + " (Invoice: " + pr.getInvoiceNumber() + ")",
                    1, 
                    pr.getAmount(), 
                    pr.getDiscount() / pr.getAmount() * 100
                ));
            }
            break;
        case "ReturnPurchaseBook":
            for (Object record : data) {
                ReturnPurchaseRecord rpr = (ReturnPurchaseRecord) record;
                items.add(new Item(
                    rpr.getSupplier() + " (Return Invoice: " + rpr.getReturnInvoice() + ")",
                    1, 
                    rpr.getAmount(), 
                    0.0
                ));
            }
            break;
        case "RawStockBook":
            for (Object record : data) {
                RawStockRecord rsr = (RawStockRecord) record;
                items.add(new Item(
                    rsr.getItem() + " (Ref: " + rsr.getReference() + ")",
                    (int) rsr.getQuantity(), 
                    0.0, 
                    0.0
                ));
            }
            break;
        case "ReturnRawStockBook":
            for (Object record : data) {
                ReturnRawStockRecord rrsr = (ReturnRawStockRecord) record;
                items.add(new Item(
                    rrsr.getItem() + " (Supplier: " + rrsr.getSupplier() + ")",
                    (int) rrsr.getQuantity(), 
                    rrsr.getUnitPrice(), 
                    0.0
                ));
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
            }
            break;
        case "SalesBook":
            for (Object record : data) {
                SalesRecord sr = (SalesRecord) record;
                items.add(new Item(
                    sr.getCustomer() + " (Invoice: " + sr.getInvoiceNumber() + ")",
                    1, 
                    sr.getAmount(), 
                    sr.getDiscount() / sr.getAmount() * 100
                ));
            }
            break;
        case "ReturnSalesBook":
            for (Object record : data) {
                ReturnSalesRecord rsr = (ReturnSalesRecord) record;
                items.add(new Item(
                    rsr.getCustomer() + " (Return Invoice: " + rsr.getReturnInvoice() + ")",
                    1, 
                    rsr.getAmount(), 
                    0.0
                ));
            }
            break;
        default:
            break;
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
        private final StringProperty returnInvoice = new SimpleStringProperty();
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty supplier = new SimpleStringProperty();
        private final StringProperty originalInvoice = new SimpleStringProperty();
        private final SimpleDoubleProperty amount = new SimpleDoubleProperty();

        ReturnPurchaseRecord(String returnInvoice, String date, String supplier, String originalInvoice, double amount) {
            this.returnInvoice.set(returnInvoice);
            this.date.set(date);
            this.supplier.set(supplier);
            this.originalInvoice.set(originalInvoice);
            this.amount.set(amount);
        }

        StringProperty returnInvoiceProperty() { return returnInvoice; }
        StringProperty dateProperty() { return date; }
        StringProperty supplierProperty() { return supplier; }
        StringProperty originalInvoiceProperty() { return originalInvoice; }
        SimpleDoubleProperty amountProperty() { return amount; }
        String getReturnInvoice() { return returnInvoice.get(); }
        String getDate() { return date.get(); }
        String getSupplier() { return supplier.get(); }
        String getOriginalInvoice() { return originalInvoice.get(); }
        double getAmount() { return amount.get(); }
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

    static class ReturnRawStockRecord {
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty item = new SimpleStringProperty();
        private final SimpleDoubleProperty quantity = new SimpleDoubleProperty();
        private final SimpleDoubleProperty unitPrice = new SimpleDoubleProperty();
        private final StringProperty supplier = new SimpleStringProperty();

        ReturnRawStockRecord(String date, String item, double quantity, double unitPrice, String supplier) {
            this.date.set(date);
            this.item.set(item);
            this.quantity.set(quantity);
            this.unitPrice.set(unitPrice);
            this.supplier.set(supplier);
        }

        StringProperty dateProperty() { return date; }
        StringProperty itemProperty() { return item; }
        SimpleDoubleProperty quantityProperty() { return quantity; }
        SimpleDoubleProperty unitPriceProperty() { return unitPrice; }
        StringProperty supplierProperty() { return supplier; }
        String getDate() { return date.get(); }
        String getItem() { return item.get(); }
        double getQuantity() { return quantity.get(); }
        double getUnitPrice() { return unitPrice.get(); }
        String getSupplier() { return supplier.get(); }
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

        ReturnSalesRecord(String returnInvoice, String date, String customer, double amount) {
            this.returnInvoice.set(returnInvoice);
            this.date.set(date);
            this.customer.set(customer);
            this.amount.set(amount);
        }

        StringProperty returnInvoiceProperty() { return returnInvoice; }
        StringProperty dateProperty() { return date; }
        StringProperty customerProperty() { return customer; }
        SimpleDoubleProperty amountProperty() { return amount; }
        String getReturnInvoice() { return returnInvoice.get(); }
        String getDate() { return date.get(); }
        String getCustomer() { return customer.get(); }
        double getAmount() { return amount.get(); }
    }
}