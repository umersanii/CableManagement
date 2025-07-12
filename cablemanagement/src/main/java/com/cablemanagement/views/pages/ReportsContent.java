package com.cablemanagement.views.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ReportsContent {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(new Label("📊 Select a report to view"));

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(72);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Add report buttons
        addButton(buttonBar, "View Purchase Report", () -> formArea.getChildren().setAll(createPurchaseReport()));
        addButton(buttonBar, "View Sales Report", () -> formArea.getChildren().setAll(createSalesReport()));
        addButton(buttonBar, "View Return Purchase Report", () -> formArea.getChildren().setAll(createReturnPurchaseReport()));
        addButton(buttonBar, "View Return Sales Report", () -> formArea.getChildren().setAll(createReturnSalesReport()));
        addButton(buttonBar, "View Bank to Bank Transfer Report", () -> formArea.getChildren().setAll(createBankTransferReport()));
        addButton(buttonBar, "View Profit Report", () -> formArea.getChildren().setAll(createProfitReport()));
        addButton(buttonBar, "View Summary Report", () -> formArea.getChildren().setAll(createSummaryReport()));
        addButton(buttonBar, "View Balance Sheet", () -> formArea.getChildren().setAll(createBalanceSheet()));
        addButton(buttonBar, "View Customers General Report", () -> formArea.getChildren().setAll(createCustomerGeneralReport()));
        addButton(buttonBar, "View Suppliers General Report", () -> formArea.getChildren().setAll(createSupplierGeneralReport()));
        addButton(buttonBar, "View Area-Wise Customer/Supplier Report", () -> formArea.getChildren().setAll(createAreaWiseReport()));
        addButton(buttonBar, "View Brand-Wise Salesman Sales Report", () -> formArea.getChildren().setAll(createBrandWiseSalesReport()));
        addButton(buttonBar, "View Brand-Wise Salesman Return Report", () -> formArea.getChildren().setAll(createBrandWiseReturnReport()));
        addButton(buttonBar, "View Brand-Wise Profit Report", () -> formArea.getChildren().setAll(createBrandWiseProfitReport()));
        addButton(buttonBar, "View Customer-Wise Sales Report", () -> formArea.getChildren().setAll(createCustomerWiseSalesReport()));
        addButton(buttonBar, "View Supplier-Wise Sales Report", () -> formArea.getChildren().setAll(createSupplierWiseSalesReport()));
        addButton(buttonBar, "View Attendance Report", () -> formArea.getChildren().setAll(createAttendanceReport()));

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

    // ------------- Report Form Placeholders -------------

    private static VBox createPurchaseReport() {
        return baseReport("Purchase Report");
    }

    private static VBox createSalesReport() {
        return baseReport("Sales Report");
    }

    private static VBox createReturnPurchaseReport() {
        return baseReport("Return Purchase Report");
    }

    private static VBox createReturnSalesReport() {
        return baseReport("Return Sales Report");
    }

    private static VBox createBankTransferReport() {
        return baseReport("Bank to Bank Transfer Report");
    }

    private static VBox createProfitReport() {
        return baseReport("Profit Report");
    }

    private static VBox createSummaryReport() {
        return baseReport("Summary Report");
    }

    private static VBox createBalanceSheet() {
        return baseReport("Balance Sheet");
    }

    private static VBox createCustomerGeneralReport() {
        return baseReport("Customers General Report");
    }

    private static VBox createSupplierGeneralReport() {
        return baseReport("Suppliers General Report");
    }

    private static VBox createAreaWiseReport() {
        return baseReport("Area-Wise Customer/Supplier Report");
    }

    private static VBox createBrandWiseSalesReport() {
        return baseReport("Brand-Wise Salesman Sales Report");
    }

    private static VBox createBrandWiseReturnReport() {
        return baseReport("Brand-Wise Salesman Return Report");
    }

    private static VBox createBrandWiseProfitReport() {
        return baseReport("Brand-Wise Profit Report");
    }

    private static VBox createCustomerWiseSalesReport() {
        return baseReport("Customer-Wise Sales Report");
    }

    private static VBox createSupplierWiseSalesReport() {
        return baseReport("Supplier-Wise Sales Report");
    }

    private static VBox createAttendanceReport() {
        return baseReport("Employee Attendance Report");
    }

    private static VBox baseReport(String title) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label("📊 " + title);
        heading.setStyle("-fx-font-size: 20px; -fx-text-fill: #2c3e50;");

        Label message = new Label("Report content will be shown here...");
        message.setStyle("-fx-text-fill: #7f8c8d;");

        box.getChildren().addAll(heading, message);
        return box;
    }
}
