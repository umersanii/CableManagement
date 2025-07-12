package com.cablemanagement.views.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

public class BooksContent {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane reportArea = new StackPane();
        reportArea.getChildren().add(viewPurchaseBook());

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

        // Buttons with separate views
        addButton(buttonBar, "View Purchase Book", () -> reportArea.getChildren().setAll(viewPurchaseBook()));
        addButton(buttonBar, "View Return Purchase Book", () -> reportArea.getChildren().setAll(viewReturnPurchaseBook()));
        addButton(buttonBar, "View Raw Stock Book", () -> reportArea.getChildren().setAll(viewRawStockBook()));
        addButton(buttonBar, "View Return Raw Stock Book", () -> reportArea.getChildren().setAll(viewReturnRawStockBook()));
        addButton(buttonBar, "View Production Book", () -> reportArea.getChildren().setAll(viewProductionBook()));
        addButton(buttonBar, "View Return Production Book", () -> reportArea.getChildren().setAll(viewReturnProductionBook()));
        addButton(buttonBar, "View Sales Book", () -> reportArea.getChildren().setAll(viewSalesBook()));
        addButton(buttonBar, "View Return Sales Book", () -> reportArea.getChildren().setAll(viewReturnSalesBook()));

        mainLayout.setTop(scrollPane);
        mainLayout.setCenter(reportArea);

        return mainLayout;
    }

    private static void addButton(HBox bar, String label, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("register-button");
        btn.setOnAction(e -> action.run());
        bar.getChildren().add(btn);
    }

    // ---------------------- Report Views ----------------------

    private static VBox viewPurchaseBook() {
        return createReportView("Purchase Book");
    }

    private static VBox viewReturnPurchaseBook() {
        return createReportView("Return Purchase Book");
    }

    private static VBox viewRawStockBook() {
        return createReportView("Raw Stock Book");
    }

    private static VBox viewReturnRawStockBook() {
        return createReportView("Return Raw Stock Book");
    }

    private static VBox viewProductionBook() {
        return createReportView("Production Book");
    }

    private static VBox viewReturnProductionBook() {
        return createReportView("Return Production Book");
    }

    private static VBox viewSalesBook() {
        return createReportView("Sales Book");
    }

    private static VBox viewReturnSalesBook() {
        return createReportView("Return Sales Book");
    }

    private static VBox createReportView(String title) {
        VBox box = new VBox(20);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("form-container");

        Label heading = new Label(title);
        heading.getStyleClass().add("form-heading");

        Label note = new Label("This is where the report for \"" + title + "\" will be displayed.");
        note.getStyleClass().add("form-subheading");

        // TODO: Add TableView or actual report content here

        box.getChildren().addAll(heading, note);
        return box;
    }
}
