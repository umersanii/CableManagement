package com.cablemanagement.views.pages;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class HomeContent {
    public static Node get() {
        VBox homeContainer = new VBox(15);
        homeContainer.setAlignment(Pos.TOP_CENTER);
        homeContainer.setStyle("-fx-padding: 30 20 20 20;"); // Add top padding to move content up
        
        try {
            // Load and display the company logo
            Image logo = new Image(HomeContent.class.getResourceAsStream("/LOGO.jpg"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(200);
            logoView.setFitHeight(200);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            
            homeContainer.getChildren().add(logoView);
        } catch (Exception e) {
            // If logo can't be loaded, show a placeholder
            Label logoPlaceholder = new Label("📷");
            logoPlaceholder.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            logoPlaceholder.setStyle("-fx-text-fill: #2c3e50;");
            homeContainer.getChildren().add(logoPlaceholder);
        }
        
        // Add some spacing between logo and company name
        Label spacer = new Label("");
        spacer.setPrefHeight(20);
        homeContainer.getChildren().add(spacer);
        
        // Company name label - doubled font size and moved down
        Label companyName = new Label("HASEEB WIRE AND CABLES");
        companyName.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 72));
        companyName.setStyle("-fx-text-fill: #2c3e50; -fx-text-alignment: center; -fx-font-weight: 900;");
        
        homeContainer.getChildren().add(companyName);
        
        StackPane container = new StackPane(homeContainer);
        container.setStyle("-fx-background-color: #ecf0f1;");
        
        return container;
    }
}
