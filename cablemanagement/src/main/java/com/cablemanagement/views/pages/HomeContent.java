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
        // Create main container that fills all available space
        StackPane mainContainer = new StackPane();
        mainContainer.setStyle("-fx-background-color: #ecf0f1;");
        
        // Company name label at the top with increased font size
        Label companyName = new Label("HASEEB WIRE AND CABLES");
        companyName.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 600)); // Increased from 400 for much larger text
        companyName.setStyle("-fx-text-fill: #2c3e50; -fx-text-alignment: center; -fx-font-weight: 900;");
        
        // Position text at the top
        StackPane.setAlignment(companyName, Pos.TOP_CENTER);
        companyName.setTranslateY(30); // Add some top margin
        
        try {
            // Load and display the company logo to cover most of the available space
            Image logo = new Image(HomeContent.class.getResourceAsStream("/LOGO.jpg"));
            ImageView logoView = new ImageView(logo);
            
            // Make the logo much larger and fill most of the area
            logoView.setFitWidth(600);  // Increased from 200
            logoView.setFitHeight(500); // Increased from 200
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            
            // Center the logo but move it down to leave space for text at top
            StackPane.setAlignment(logoView, Pos.CENTER);
            logoView.setTranslateY(50); // Move down to account for text at top
            
            mainContainer.getChildren().addAll(logoView, companyName);
        } catch (Exception e) {
            // If logo can't be loaded, show a larger placeholder
            Label logoPlaceholder = new Label("📷");
            logoPlaceholder.setFont(Font.font("Arial", FontWeight.BOLD, 200)); // Much larger placeholder
            logoPlaceholder.setStyle("-fx-text-fill: #2c3e50;");
            
            // Center the placeholder but move it down to leave space for text at top
            StackPane.setAlignment(logoPlaceholder, Pos.CENTER);
            logoPlaceholder.setTranslateY(50); // Move down to account for text at top
            
            mainContainer.getChildren().addAll(logoPlaceholder, companyName);
        }
        
        return mainContainer;
    }
}
