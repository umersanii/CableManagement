package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class RawStock {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createRawStockForm());

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
            "Register Raw Stock",
            "Create Raw Stock Purchase Invoice",
            "Create Raw Stock Return Purchase Invoice",
            "Create Raw Stock Use Invoice",
            "View Raw Stock Usage Report"
        };

        Runnable[] actions = {
            () -> formArea.getChildren().setAll(createRawStockForm()),
            () -> formArea.getChildren().setAll(createRawStockPurchaseInvoiceForm()),
            () -> formArea.getChildren().setAll(createRawStockReturnPurchaseInvoiceForm()),
            () -> formArea.getChildren().setAll(createRawStockUseInvoiceForm()),
            () -> formArea.getChildren().setAll(createRawStockUsageReportForm())
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

    private static VBox createRawStockForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Register Raw Stock");

        // Input fields matching Raw_Stock table
        TextField nameField = createTextField("Stock Name");
        TextField openingQtyField = createTextField("0", "Opening Quantity");
        TextField purchasePriceField = createTextField("Purchase Price/Unit");
        TextField reorderLevelField = createTextField("0", "Reorder Level");
        
        // These would be ComboBoxes in real implementation
        TextField categoryField = createTextField("Category");
        TextField brandField = createTextField("Brand");
        TextField unitField = createTextField("Unit");

        Button submitBtn = createSubmitButton("Submit Raw Stock");

        Label listHeading = createSubheading("Registered Raw Stock:");
        ListView<String> stockView = createListView();

        submitBtn.setOnAction(e -> handleRawStockSubmit(
            nameField, categoryField, brandField, unitField,
            openingQtyField, purchasePriceField, reorderLevelField,
            stockView
        ));

        form.getChildren().addAll(
            heading, 
            createFormRow("Stock Name:", nameField),
            createFormRow("Category:", categoryField),
            createFormRow("Brand:", brandField),
            createFormRow("Unit:", unitField),
            createFormRow("Opening Quantity:", openingQtyField),
            createFormRow("Purchase Price/Unit:", purchasePriceField),
            createFormRow("Reorder Level:", reorderLevelField),
            submitBtn, listHeading, stockView
        );
        
        return form;
    }

    private static VBox createRawStockPurchaseInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Raw Stock Purchase Invoice");

        // Fields matching Raw_Purchase_Invoice and Raw_Purchase_Invoice_Item
        TextField invoiceNumberField = createTextField("Invoice Number");
        TextField supplierField = createTextField("Supplier");
        DatePicker invoiceDatePicker = new DatePicker();
        invoiceDatePicker.setValue(LocalDate.now());
        TextField stockField = createTextField("Stock Item");
        TextField quantityField = createTextField("Quantity");
        TextField unitPriceField = createTextField("Unit Price");
        TextField discountField = createTextField("0", "Discount");
        TextField paidAmountField = createTextField("0", "Paid Amount");

        Button submitBtn = createSubmitButton("Submit Purchase Invoice");

        Label listHeading = createSubheading("Purchase Invoices:");
        ListView<String> invoiceView = createListView();

        submitBtn.setOnAction(e -> handlePurchaseInvoiceSubmit(
            invoiceNumberField, supplierField, invoiceDatePicker,
            stockField, quantityField, unitPriceField,
            discountField, paidAmountField, invoiceView
        ));

        form.getChildren().addAll(
            heading,
            createFormRow("Invoice Number:", invoiceNumberField),
            createFormRow("Supplier:", supplierField),
            createFormRow("Invoice Date:", invoiceDatePicker),
            createFormRow("Stock Item:", stockField),
            createFormRow("Quantity:", quantityField),
            createFormRow("Unit Price:", unitPriceField),
            createFormRow("Discount:", discountField),
            createFormRow("Paid Amount:", paidAmountField),
            submitBtn, listHeading, invoiceView
        );
        
        return form;
    }

    private static VBox createRawStockReturnPurchaseInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Raw Stock Return Purchase Invoice");

        // Fields matching Raw_Purchase_Return_Invoice and Raw_Purchase_Return_Invoice_Item
        TextField returnInvoiceNumberField = createTextField("Return Invoice Number");
        TextField originalInvoiceField = createTextField("Original Invoice");
        TextField supplierField = createTextField("Supplier");
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(LocalDate.now());
        TextField stockField = createTextField("Stock Item");
        TextField quantityField = createTextField("Quantity");
        TextField unitPriceField = createTextField("Unit Price");
        TextField totalReturnField = createTextField("Total Return Amount");

        Button submitBtn = createSubmitButton("Submit Return Invoice");

        Label listHeading = createSubheading("Return Purchase Invoices:");
        ListView<String> returnView = createListView();

        submitBtn.setOnAction(e -> handleReturnInvoiceSubmit(
            returnInvoiceNumberField, originalInvoiceField, supplierField,
            returnDatePicker, stockField, quantityField,
            unitPriceField, totalReturnField, returnView
        ));

        form.getChildren().addAll(
            heading,
            createFormRow("Return Invoice Number:", returnInvoiceNumberField),
            createFormRow("Original Invoice:", originalInvoiceField),
            createFormRow("Supplier:", supplierField),
            createFormRow("Return Date:", returnDatePicker),
            createFormRow("Stock Item:", stockField),
            createFormRow("Quantity:", quantityField),
            createFormRow("Unit Price:", unitPriceField),
            createFormRow("Total Return Amount:", totalReturnField),
            submitBtn, listHeading, returnView
        );
        
        return form;
    }

    private static VBox createRawStockUseInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Raw Stock Use Invoice");

        // Fields matching Raw_Stock_Usage table
        TextField stockField = createTextField("Stock Item");
        DatePicker usageDatePicker = new DatePicker();
        usageDatePicker.setValue(LocalDate.now());
        TextField quantityField = createTextField("Quantity Used");
        TextField referenceField = createTextField("Reference/Purpose");

        Button submitBtn = createSubmitButton("Submit Use Invoice");

        Label listHeading = createSubheading("Stock Usage Records:");
        ListView<String> usageView = createListView();

        submitBtn.setOnAction(e -> handleUsageInvoiceSubmit(
            stockField, usageDatePicker, quantityField,
            referenceField, usageView
        ));

        form.getChildren().addAll(
            heading,
            createFormRow("Stock Item:", stockField),
            createFormRow("Usage Date:", usageDatePicker),
            createFormRow("Quantity Used:", quantityField),
            createFormRow("Reference/Purpose:", referenceField),
            submitBtn, listHeading, usageView
        );
        
        return form;
    }

    private static VBox createRawStockUsageReportForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Raw Stock Usage Report");

        // Date range selection
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now());

        Button generateBtn = new Button("Generate Report");
        generateBtn.getStyleClass().add("form-submit");

        Label reportHeading = createSubheading("Usage Report:");
        ListView<String> reportView = createListView();
        reportView.setPrefHeight(400);

        generateBtn.setOnAction(e -> {
            String startDate = startDatePicker.getValue().format(DATE_FORMATTER);
            String endDate = endDatePicker.getValue().format(DATE_FORMATTER);
            
            // In real app, this would query the database
            reportView.getItems().clear();
            reportView.getItems().add("Usage Report from " + startDate + " to " + endDate);
            reportView.getItems().add("Copper Wire 8mm - 150m used");
            reportView.getItems().add("PVC Granules - 75kg used");
            reportView.getItems().add("Aluminum Conductor - 200m used");
        });

        form.getChildren().addAll(
            heading,
            createFormRow("Start Date:", startDatePicker),
            createFormRow("End Date:", endDatePicker),
            generateBtn,
            reportHeading,
            reportView
        );
        
        return form;
    }

    // Helper methods for UI components
    private static Label createHeading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-heading");
        label.setFont(Font.font(18));
        return label;
    }

    private static Label createSubheading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-subheading");
        return label;
    }

    private static TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("form-input");
        return field;
    }

    private static TextField createTextField(String text, String prompt) {
        TextField field = new TextField(text);
        field.setPromptText(prompt);
        field.getStyleClass().add("form-input");
        return field;
    }

    private static ListView<String> createListView() {
        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(200);
        listView.getStyleClass().add("category-list");
        return listView;
    }

    private static Button createSubmitButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("form-submit");
        return button;
    }

    private static HBox createFormRow(String labelText, Control field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        HBox row = new HBox(10, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");
        return row;
    }

    // Form submission handlers
    private static void handleRawStockSubmit(
        TextField nameField, TextField categoryField, TextField brandField,
        TextField unitField, TextField openingQtyField, TextField purchasePriceField,
        TextField reorderLevelField, ListView<String> stockView
    ) {
        if (nameField.getText().trim().isEmpty() || purchasePriceField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name and Purchase Price are required");
            return;
        }

        try {
            String stockEntry = String.format("%s | Cat: %s | Brand: %s | %s %s @ %s | Reorder: %s",
                nameField.getText().trim(),
                categoryField.getText().trim(),
                brandField.getText().trim(),
                openingQtyField.getText().trim(),
                unitField.getText().trim(),
                purchasePriceField.getText().trim(),
                reorderLevelField.getText().trim());
            
            stockView.getItems().add(stockEntry);
            
            // Clear fields
            nameField.clear();
            categoryField.clear();
            brandField.clear();
            unitField.clear();
            openingQtyField.setText("0");
            purchasePriceField.clear();
            reorderLevelField.setText("0");

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please check your entries");
        }
    }

    private static void handlePurchaseInvoiceSubmit(
        TextField invoiceNumberField, TextField supplierField, DatePicker invoiceDatePicker,
        TextField stockField, TextField quantityField, TextField unitPriceField,
        TextField discountField, TextField paidAmountField, ListView<String> invoiceView
    ) {
        if (invoiceNumberField.getText().trim().isEmpty() ||
            supplierField.getText().trim().isEmpty() ||
            stockField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Required fields are missing");
            return;
        }

        try {
            double quantity = Double.parseDouble(quantityField.getText().trim());
            double unitPrice = Double.parseDouble(unitPriceField.getText().trim());
            double discount = Double.parseDouble(discountField.getText().trim());
            double paid = Double.parseDouble(paidAmountField.getText().trim());
            double total = quantity * unitPrice - discount;

            String invoiceEntry = String.format("%s | %s | %s | %s x %s = %s | Paid: %s",
                invoiceNumberField.getText().trim(),
                invoiceDatePicker.getValue().format(DATE_FORMATTER),
                supplierField.getText().trim(),
                quantity,
                unitPrice,
                total,
                paid);
            
            invoiceView.getItems().add(invoiceEntry);
            
            // Clear fields
            invoiceNumberField.clear();
            supplierField.clear();
            stockField.clear();
            quantityField.clear();
            unitPriceField.clear();
            discountField.setText("0");
            paidAmountField.setText("0");
            invoiceDatePicker.setValue(LocalDate.now());

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers");
        }
    }

    private static void handleReturnInvoiceSubmit(
        TextField returnInvoiceNumberField, TextField originalInvoiceField, TextField supplierField,
        DatePicker returnDatePicker, TextField stockField, TextField quantityField,
        TextField unitPriceField, TextField totalReturnField, ListView<String> returnView
    ) {
        if (returnInvoiceNumberField.getText().trim().isEmpty() ||
            supplierField.getText().trim().isEmpty() ||
            stockField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Required fields are missing");
            return;
        }

        try {
            String returnEntry = String.format("%s | Orig: %s | %s | %s | %s x %s | Total: %s",
                returnInvoiceNumberField.getText().trim(),
                originalInvoiceField.getText().trim(),
                returnDatePicker.getValue().format(DATE_FORMATTER),
                supplierField.getText().trim(),
                quantityField.getText().trim(),
                unitPriceField.getText().trim(),
                totalReturnField.getText().trim());
            
            returnView.getItems().add(returnEntry);
            
            // Clear fields
            returnInvoiceNumberField.clear();
            originalInvoiceField.clear();
            supplierField.clear();
            stockField.clear();
            quantityField.clear();
            unitPriceField.clear();
            totalReturnField.clear();
            returnDatePicker.setValue(LocalDate.now());

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please check your entries");
        }
    }

    private static void handleUsageInvoiceSubmit(
        TextField stockField, DatePicker usageDatePicker, TextField quantityField,
        TextField referenceField, ListView<String> usageView
    ) {
        if (stockField.getText().trim().isEmpty() ||
            quantityField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Stock and Quantity are required");
            return;
        }

        try {
            String usageEntry = String.format("%s | %s | Qty: %s | Ref: %s",
                stockField.getText().trim(),
                usageDatePicker.getValue().format(DATE_FORMATTER),
                quantityField.getText().trim(),
                referenceField.getText().trim());
            
            usageView.getItems().add(usageEntry);
            
            // Clear fields
            stockField.clear();
            quantityField.clear();
            referenceField.clear();
            usageDatePicker.setValue(LocalDate.now());

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please check your entries");
        }
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}