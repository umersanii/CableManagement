package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ProductionStock {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createProductionInvoiceForm());

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

        addButton(buttonBar, "Create Production Invoice", () -> formArea.getChildren().setAll(createProductionInvoiceForm()));
        addButton(buttonBar, "Create Return Production Invoice", () -> formArea.getChildren().setAll(createReturnProductionInvoiceForm()));
        addButton(buttonBar, "Create Sales Invoice", () -> formArea.getChildren().setAll(createSalesInvoiceForm()));
        addButton(buttonBar, "Create Return Sales Invoice", () -> formArea.getChildren().setAll(createReturnSalesInvoiceForm()));
        addButton(buttonBar, "View Production Stock Usage Report", () -> formArea.getChildren().setAll(createProductionStockUsageReportForm()));

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

    private static VBox createProductionInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Create Production Invoice");
        heading.getStyleClass().add("form-heading");

        TextField productNameField = new TextField();
        TextField quantityField = new TextField();
        productNameField.getStyleClass().add("form-input");
        quantityField.getStyleClass().add("form-input");

        Label productNameLabel = new Label("Product Name:");
        Label quantityLabel = new Label("Quantity:");
        productNameLabel.getStyleClass().add("form-label");
        quantityLabel.getStyleClass().add("form-label");

        HBox productRow = new HBox(10, productNameLabel, productNameField);
        productRow.setAlignment(Pos.CENTER_LEFT);
        productRow.getStyleClass().add("form-row");

        HBox quantityRow = new HBox(10, quantityLabel, quantityField);
        quantityRow.setAlignment(Pos.CENTER_LEFT);
        quantityRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Production Invoice");
        submitBtn.getStyleClass().add("form-submit");

        Label listHeading = new Label("Registered Production Invoices:");
        listHeading.getStyleClass().add("form-subheading");

        ObservableList<String> invoiceList = FXCollections.observableArrayList();
        ListView<String> invoiceView = new ListView<>(invoiceList);
        invoiceView.setPrefHeight(200);
        invoiceView.getStyleClass().add("category-list");

        submitBtn.setOnAction(e -> {
            String productName = productNameField.getText().trim();
            String quantity = quantityField.getText().trim();
            if (!productName.isEmpty() && !quantity.isEmpty()) {
                invoiceList.add(productName + " - " + quantity);
                productNameField.clear();
                quantityField.clear();
            }
        });

        // TODO: Integrate with database
        // TODO: Fetch data dynamically from database

        form.getChildren().addAll(heading, productRow, quantityRow, submitBtn, listHeading, invoiceView);
        return form;
    }

    private static VBox createReturnProductionInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Create Return Production Invoice");
        heading.getStyleClass().add("form-heading");

        TextField productNameField = new TextField();
        TextField quantityField = new TextField();
        productNameField.getStyleClass().add("form-input");
        quantityField.getStyleClass().add("form-input");

        Label productNameLabel = new Label("Product Name:");
        Label quantityLabel = new Label("Quantity:");
        productNameLabel.getStyleClass().add("form-label");
        quantityLabel.getStyleClass().add("form-label");

        HBox productRow = new HBox(10, productNameLabel, productNameField);
        productRow.setAlignment(Pos.CENTER_LEFT);
        productRow.getStyleClass().add("form-row");

        HBox quantityRow = new HBox(10, quantityLabel, quantityField);
        quantityRow.setAlignment(Pos.CENTER_LEFT);
        quantityRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Return Invoice");
        submitBtn.getStyleClass().add("form-submit");

        Label listHeading = new Label("Registered Return Production Invoices:");
        listHeading.getStyleClass().add("form-subheading");

        ObservableList<String> invoiceList = FXCollections.observableArrayList();
        ListView<String> invoiceView = new ListView<>(invoiceList);
        invoiceView.setPrefHeight(200);
        invoiceView.getStyleClass().add("category-list");

        submitBtn.setOnAction(e -> {
            String productName = productNameField.getText().trim();
            String quantity = quantityField.getText().trim();
            if (!productName.isEmpty() && !quantity.isEmpty()) {
                invoiceList.add(productName + " - " + quantity);
                productNameField.clear();
                quantityField.clear();
            }
        });

        // TODO: Integrate with database
        // TODO: Fetch data dynamically from database

        form.getChildren().addAll(heading, productRow, quantityRow, submitBtn, listHeading, invoiceView);
        return form;
    }

    private static VBox createSalesInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Create Sales Invoice");
        heading.getStyleClass().add("form-heading");

        TextField productNameField = new TextField();
        TextField quantityField = new TextField();
        TextField customerField = new TextField();
        productNameField.getStyleClass().add("form-input");
        quantityField.getStyleClass().add("form-input");
        customerField.getStyleClass().add("form-input");

        Label productNameLabel = new Label("Product Name:");
        Label quantityLabel = new Label("Quantity:");
        Label customerLabel = new Label("Customer:");
        productNameLabel.getStyleClass().add("form-label");
        quantityLabel.getStyleClass().add("form-label");
        customerLabel.getStyleClass().add("form-label");

        HBox productRow = new HBox(10, productNameLabel, productNameField);
        productRow.setAlignment(Pos.CENTER_LEFT);
        productRow.getStyleClass().add("form-row");

        HBox quantityRow = new HBox(10, quantityLabel, quantityField);
        quantityRow.setAlignment(Pos.CENTER_LEFT);
        quantityRow.getStyleClass().add("form-row");

        HBox customerRow = new HBox(10, customerLabel, customerField);
        customerRow.setAlignment(Pos.CENTER_LEFT);
        customerRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Sales Invoice");
        submitBtn.getStyleClass().add("form-submit");

        Label listHeading = new Label("Registered Sales Invoices:");
        listHeading.getStyleClass().add("form-subheading");

        ObservableList<String> invoiceList = FXCollections.observableArrayList();
        ListView<String> invoiceView = new ListView<>(invoiceList);
        invoiceView.setPrefHeight(200);
        invoiceView.getStyleClass().add("category-list");

        submitBtn.setOnAction(e -> {
            String productName = productNameField.getText().trim();
            String quantity = quantityField.getText().trim();
            String customer = customerField.getText().trim();
            if (!productName.isEmpty() && !quantity.isEmpty() && !customer.isEmpty()) {
                invoiceList.add(productName + " - " + quantity + " - " + customer);
                productNameField.clear();
                quantityField.clear();
                customerField.clear();
            }
        });

        // TODO: Integrate with database
        // TODO: Fetch data dynamically from database

        form.getChildren().addAll(heading, productRow, quantityRow, customerRow, submitBtn, listHeading, invoiceView);
        return form;
    }

    private static VBox createReturnSalesInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Create Return Sales Invoice");
        heading.getStyleClass().add("form-heading");

        TextField productNameField = new TextField();
        TextField quantityField = new TextField();
        TextField customerField = new TextField();
        productNameField.getStyleClass().add("form-input");
        quantityField.getStyleClass().add("form-input");
        customerField.getStyleClass().add("form-input");

        Label productNameLabel = new Label("Product Name:");
        Label quantityLabel = new Label("Quantity:");
        Label customerLabel = new Label("Customer:");
        productNameLabel.getStyleClass().add("form-label");
        quantityLabel.getStyleClass().add("form-label");
        customerLabel.getStyleClass().add("form-label");

        HBox productRow = new HBox(10, productNameLabel, productNameField);
        productRow.setAlignment(Pos.CENTER_LEFT);
        productRow.getStyleClass().add("form-row");

        HBox quantityRow = new HBox(10, quantityLabel, quantityField);
        quantityRow.setAlignment(Pos.CENTER_LEFT);
        quantityRow.getStyleClass().add("form-row");

        HBox customerRow = new HBox(10, customerLabel, customerField);
        customerRow.setAlignment(Pos.CENTER_LEFT);
        customerRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Return Invoice");
        submitBtn.getStyleClass().add("form-submit");

        Label listHeading = new Label("Registered Return Sales Invoices:");
        listHeading.getStyleClass().add("form-subheading");

        ObservableList<String> invoiceList = FXCollections.observableArrayList();
        ListView<String> invoiceView = new ListView<>(invoiceList);
        invoiceView.setPrefHeight(200);
        invoiceView.getStyleClass().add("category-list");

        submitBtn.setOnAction(e -> {
            String productName = productNameField.getText().trim();
            String quantity = quantityField.getText().trim();
            String customer = customerField.getText().trim();
            if (!productName.isEmpty() && !quantity.isEmpty() && !customer.isEmpty()) {
                invoiceList.add(productName + " - " + quantity + " - " + customer);
                productNameField.clear();
                quantityField.clear();
                customerField.clear();
            }
        });

        // TODO: Integrate with database
        // TODO: Fetch data dynamically from database

        form.getChildren().addAll(heading, productRow, quantityRow, customerRow, submitBtn, listHeading, invoiceView);
        return form;
    }

    private static VBox createProductionStockUsageReportForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("View Production Stock Usage Report");
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