package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RawStock {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createRawStockForm());

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        buttonBar.setMinHeight(Region.USE_PREF_SIZE);
        buttonBar.setMaxHeight(Region.USE_PREF_SIZE);
        buttonBar.setPrefHeight(Region.USE_COMPUTED_SIZE);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(72);
        scrollPane.setMinHeight(72);
        scrollPane.setMaxHeight(72);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        addButton(buttonBar, "Register Raw Stock", () -> formArea.getChildren().setAll(createRawStockForm()));
        addButton(buttonBar, "Create Raw Stock Purchase Invoice", () -> formArea.getChildren().setAll(createRawStockPurchaseInvoiceForm()));
        addButton(buttonBar, "Create Raw Stock Return Purchase Invoice", () -> formArea.getChildren().setAll(createRawStockReturnPurchaseInvoiceForm()));
        addButton(buttonBar, "Create Raw Stock Use Invoice", () -> formArea.getChildren().setAll(createRawStockUseInvoiceForm()));
        addButton(buttonBar, "View Raw Stock Usage Report", () -> formArea.getChildren().setAll(createRawStockUsageReportForm()));

        mainLayout.setTop(scrollPane);
        mainLayout.setCenter(formArea);

        return mainLayout;
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

        Label heading = new Label("Register Raw Stock");
        heading.getStyleClass().add("form-heading");

        TextField nameField = new TextField();
        TextField quantityField = new TextField();
        nameField.getStyleClass().add("form-input");
        quantityField.getStyleClass().add("form-input");

        Label nameLabel = new Label("Stock Name:");
        Label quantityLabel = new Label("Quantity:");
        nameLabel.getStyleClass().add("form-label");
        quantityLabel.getStyleClass().add("form-label");

        HBox nameRow = new HBox(10, nameLabel, nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.getStyleClass().add("form-row");

        HBox quantityRow = new HBox(10, quantityLabel, quantityField);
        quantityRow.setAlignment(Pos.CENTER_LEFT);
        quantityRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Raw Stock");
        submitBtn.getStyleClass().add("form-submit");

        Label listHeading = new Label("Registered Raw Stock:");
        listHeading.getStyleClass().add("form-subheading");

        ObservableList<String> stockList = FXCollections.observableArrayList();
        ListView<String> stockView = new ListView<>(stockList);
        stockView.setPrefHeight(200);
        stockView.getStyleClass().add("category-list");

        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String quantity = quantityField.getText().trim();
            if (!name.isEmpty() && !quantity.isEmpty()) {
                stockList.add(name + " - " + quantity);
                nameField.clear();
                quantityField.clear();
            }
        });

        // TODO: Integrate with database
        // TODO: Fetch data dynamically from database

        form.getChildren().addAll(heading, nameRow, quantityRow, submitBtn, listHeading, stockView);
        return form;
    }

    private static VBox createRawStockPurchaseInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Create Raw Stock Purchase Invoice");
        heading.getStyleClass().add("form-heading");

        TextField stockNameField = new TextField();
        TextField quantityField = new TextField();
        TextField supplierField = new TextField();
        stockNameField.getStyleClass().add("form-input");
        quantityField.getStyleClass().add("form-input");
        supplierField.getStyleClass().add("form-input");

        Label stockNameLabel = new Label("Stock Name:");
        Label quantityLabel = new Label("Quantity:");
        Label supplierLabel = new Label("Supplier:");
        stockNameLabel.getStyleClass().add("form-label");
        quantityLabel.getStyleClass().add("form-label");
        supplierLabel.getStyleClass().add("form-label");

        HBox stockRow = new HBox(10, stockNameLabel, stockNameField);
        stockRow.setAlignment(Pos.CENTER_LEFT);
        stockRow.getStyleClass().add("form-row");

        HBox quantityRow = new HBox(10, quantityLabel, quantityField);
        quantityRow.setAlignment(Pos.CENTER_LEFT);
        quantityRow.getStyleClass().add("form-row");

        HBox supplierRow = new HBox(10, supplierLabel, supplierField);
        supplierRow.setAlignment(Pos.CENTER_LEFT);
        supplierRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Purchase Invoice");
        submitBtn.getStyleClass().add("form-submit");

        Label listHeading = new Label("Registered Purchase Invoices:");
        listHeading.getStyleClass().add("form-subheading");

        ObservableList<String> invoiceList = FXCollections.observableArrayList();
        ListView<String> invoiceView = new ListView<>(invoiceList);
        invoiceView.setPrefHeight(200);
        invoiceView.getStyleClass().add("category-list");

        submitBtn.setOnAction(e -> {
            String stockName = stockNameField.getText().trim();
            String quantity = quantityField.getText().trim();
            String supplier = supplierField.getText().trim();
            if (!stockName.isEmpty() && !quantity.isEmpty() && !supplier.isEmpty()) {
                invoiceList.add(stockName + " - " + quantity + " - " + supplier);
                stockNameField.clear();
                quantityField.clear();
                supplierField.clear();
            }
        });

        // TODO: Integrate with database
        // TODO: Fetch data dynamically from database

        form.getChildren().addAll(heading, stockRow, quantityRow, supplierRow, submitBtn, listHeading, invoiceView);
        return form;
    }

    private static VBox createRawStockReturnPurchaseInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Create Raw Stock Return Purchase Invoice");
        heading.getStyleClass().add("form-heading");

        TextField stockNameField = new TextField();
        TextField quantityField = new TextField();
        TextField supplierField = new TextField();
        stockNameField.getStyleClass().add("form-input");
        quantityField.getStyleClass().add("form-input");
        supplierField.getStyleClass().add("form-input");

        Label stockNameLabel = new Label("Stock Name:");
        Label quantityLabel = new Label("Quantity:");
        Label supplierLabel = new Label("Supplier:");
        stockNameLabel.getStyleClass().add("form-label");
        quantityLabel.getStyleClass().add("form-label");
        supplierLabel.getStyleClass().add("form-label");

        HBox stockRow = new HBox(10, stockNameLabel, stockNameField);
        stockRow.setAlignment(Pos.CENTER_LEFT);
        stockRow.getStyleClass().add("form-row");

        HBox quantityRow = new HBox(10, quantityLabel, quantityField);
        quantityRow.setAlignment(Pos.CENTER_LEFT);
        quantityRow.getStyleClass().add("form-row");

        HBox supplierRow = new HBox(10, supplierLabel, supplierField);
        supplierRow.setAlignment(Pos.CENTER_LEFT);
        supplierRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Return Invoice");
        submitBtn.getStyleClass().add("form-submit");

        Label listHeading = new Label("Registered Return Purchase Invoices:");
        listHeading.getStyleClass().add("form-subheading");

        ObservableList<String> invoiceList = FXCollections.observableArrayList();
        ListView<String> invoiceView = new ListView<>(invoiceList);
        invoiceView.setPrefHeight(200);
        invoiceView.getStyleClass().add("category-list");

        submitBtn.setOnAction(e -> {
            String stockName = stockNameField.getText().trim();
            String quantity = quantityField.getText().trim();
            String supplier = supplierField.getText().trim();
            if (!stockName.isEmpty() && !quantity.isEmpty() && !supplier.isEmpty()) {
                invoiceList.add(stockName + " - " + quantity + " - " + supplier);
                stockNameField.clear();
                quantityField.clear();
                supplierField.clear();
            }
        });

        // TODO: Integrate with database
        // TODO: Fetch data dynamically from database

        form.getChildren().addAll(heading, stockRow, quantityRow, supplierRow, submitBtn, listHeading, invoiceView);
        return form;
    }

    private static VBox createRawStockUseInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Create Raw Stock Use Invoice");
        heading.getStyleClass().add("form-heading");

        TextField stockNameField = new TextField();
        TextField quantityField = new TextField();
        TextField purposeField = new TextField();
        stockNameField.getStyleClass().add("form-input");
        quantityField.getStyleClass().add("form-input");
        purposeField.getStyleClass().add("form-input");

        Label stockNameLabel = new Label("Stock Name:");
        Label quantityLabel = new Label("Quantity:");
        Label purposeLabel = new Label("Purpose:");
        stockNameLabel.getStyleClass().add("form-label");
        quantityLabel.getStyleClass().add("form-label");
        purposeLabel.getStyleClass().add("form-label");

        HBox stockRow = new HBox(10, stockNameLabel, stockNameField);
        stockRow.setAlignment(Pos.CENTER_LEFT);
        stockRow.getStyleClass().add("form-row");

        HBox quantityRow = new HBox(10, quantityLabel, quantityField);
        quantityRow.setAlignment(Pos.CENTER_LEFT);
        quantityRow.getStyleClass().add("form-row");

        HBox purposeRow = new HBox(10, purposeLabel, purposeField);
        purposeRow.setAlignment(Pos.CENTER_LEFT);
        purposeRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Use Invoice");
        submitBtn.getStyleClass().add("form-submit");

        Label listHeading = new Label("Registered Use Invoices:");
        listHeading.getStyleClass().add("form-subheading");

        ObservableList<String> invoiceList = FXCollections.observableArrayList();
        ListView<String> invoiceView = new ListView<>(invoiceList);
        invoiceView.setPrefHeight(200);
        invoiceView.getStyleClass().add("category-list");

        submitBtn.setOnAction(e -> {
            String stockName = stockNameField.getText().trim();
            String quantity = quantityField.getText().trim();
            String purpose = purposeField.getText().trim();
            if (!stockName.isEmpty() && !quantity.isEmpty() && !purpose.isEmpty()) {
                invoiceList.add(stockName + " - " + quantity + " - " + purpose);
                stockNameField.clear();
                quantityField.clear();
                purposeField.clear();
            }
        });

        // TODO: Integrate with database
        // TODO: Fetch data dynamically from database

        form.getChildren().addAll(heading, stockRow, quantityRow, purposeRow, submitBtn, listHeading, invoiceView);
        return form;
    }

    private static VBox createRawStockUsageReportForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("View Raw Stock Usage Report");
        heading.getStyleClass().add("form-heading");

        Label reportHeading = new Label("Usage Report:");
        reportHeading.getStyleClass().add("form-subheading");

        ObservableList<String> reportList = FXCollections.observableArrayList();
        ListView<String> reportView = new ListView<>(reportList);
        reportView.setPrefHeight(200);
        reportView.getStyleClass().add("category-list");

        // TODO: Fetch and populate report data from database

        form.getChildren().addAll(heading, reportHeading, reportView);
        return form;
    }
}