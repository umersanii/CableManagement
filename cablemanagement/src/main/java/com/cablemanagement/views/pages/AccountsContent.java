package com.cablemanagement.views.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AccountsContent {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        // Simple placeholder content
        VBox contentArea = new VBox(20);
        contentArea.setPadding(new Insets(50));
        contentArea.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("💰 Accounts");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label descriptionLabel = new Label("Customer and Supplier Account Management");
        descriptionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        descriptionLabel.setStyle("-fx-text-fill: #7f8c8d;");

        Label comingSoonLabel = new Label("Coming Soon...");
        comingSoonLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        comingSoonLabel.setStyle("-fx-text-fill: #95a5a6;");

        contentArea.getChildren().addAll(titleLabel, descriptionLabel, comingSoonLabel);

        mainLayout.setCenter(contentArea);
        return mainLayout;
    }
}
