package com.cablemanagement.views;

import com.cablemanagement.config;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class signin_page extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sign In");

        // Create UI components
        Label userLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");

        // Action on login button
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // if ("admin".equals(username) && "1234".equals(password)) {
            //     // Switch to home page
            //     primaryStage.setScene(home_page.getHomeScene());
            // } else {
            //     showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials!");
            // }
            if(config.database.isConnected())
            {
                if(config.database.SignIn(username, password)){
                    primaryStage.setScene(home_page.getHomeScene());
                }
                else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials!");
                }
            }
            else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Unable to connect to database!");
            }
        });

        VBox layout = new VBox(10, userLabel, usernameField, passLabel, passwordField, loginButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Allow launching from another class
    public static void main(String[] args) {
        launch(args);
    }
}
