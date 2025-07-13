package com.cablemanagement.views.pages;

import com.cablemanagement.config;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public class SettingsContent {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(new Label("⚙️ Select a settings option"));

        VBox buttonColumn = new VBox(10);
        buttonColumn.setPadding(new Insets(10));
        buttonColumn.setAlignment(Pos.TOP_LEFT);

        // Responsive resizing
        buttonColumn.setPrefWidth(250);
        ScrollPane buttonScroll = new ScrollPane(buttonColumn);
        buttonScroll.setFitToWidth(true);
        buttonScroll.setPrefWidth(260);

        addButton(buttonColumn, "Change Password", () -> formArea.getChildren().setAll(createChangePasswordForm()));
        addButton(buttonColumn, "Logout", () -> formArea.getChildren().setAll(createLogoutPrompt()));
        addButton(buttonColumn, "Signup", () -> formArea.getChildren().setAll(createSignupForm()));

        mainLayout.setLeft(buttonScroll);
        mainLayout.setCenter(formArea);

        return mainLayout;
    }

    private static void addButton(VBox bar, String label, Runnable action) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("register-button");
        btn.setOnAction(e -> action.run());
        bar.getChildren().add(btn);
    }

    private static VBox createChangePasswordForm() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label("Change Password");
        heading.setStyle("-fx-font-size: 18px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField oldPass = new PasswordField();
        oldPass.setPromptText("Old Password");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");

        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm New Password");

        Button submit = new Button("Update Password");

        submit.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String oldPassword = oldPass.getText();
            String newPassword = newPass.getText();
            String confirmPassword = confirmPass.getText();

            if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
                showAlert("Error", "All fields are required!");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert("Error", "New passwords do not match!");
                return;
            }

            if (config.database != null && config.database.isConnected()) {
                if (config.database.changePassword(username, oldPassword, newPassword)) {
                    showAlert("Success", "Password changed successfully!");
                    usernameField.clear();
                    oldPass.clear();
                    newPass.clear();
                    confirmPass.clear();
                } else {
                    showAlert("Error", "Failed to change password. Please check your current password.");
                }
            } else {
                showAlert("Error", "Database not connected!");
            }
        });

        box.getChildren().addAll(heading, usernameField, oldPass, newPass, confirmPass, submit);
        return box;
    }

    private static VBox createLogoutPrompt() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Are you sure you want to logout?");
        Button logout = new Button("Confirm Logout");

        logout.setOnAction(e -> {
            // Close the current window and return to login
            Platform.exit();
            // In a real application, you might want to:
            // 1. Clear any session data
            // 2. Close database connections
            // 3. Return to login screen
            System.out.println("User logged out successfully");
        });

        box.getChildren().addAll(label, logout);
        return box;
    }

    private static VBox createSignupForm() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label("Signup New User");
        heading.setStyle("-fx-font-size: 18px;");

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.setPromptText("Select Role");
        roleCombo.getItems().addAll("admin", "cashier", "manager", "user");
        roleCombo.setValue("user"); // Default role

        Button signup = new Button("Create Account");

        signup.setOnAction(e -> {
            String user = username.getText().trim();
            String pass = password.getText();
            String confirmPass = confirmPassword.getText();
            String userRole = roleCombo.getValue();

            if (user.isEmpty() || pass.isEmpty() || userRole.isEmpty()) {
                showAlert("Error", "All fields are required!");
                return;
            }

            if (!pass.equals(confirmPass)) {
                showAlert("Error", "Passwords do not match!");
                return;
            }

            if (config.database != null && config.database.isConnected()) {
                if (config.database.userExists(user)) {
                    showAlert("Error", "Username already exists!");
                } else if (config.database.insertUser(user, pass, userRole)) {
                    showAlert("Success", "User created successfully!");
                    username.clear();
                    password.clear();
                    confirmPassword.clear();
                    roleCombo.setValue("user");
                } else {
                    showAlert("Error", "Failed to create user!");
                }
            } else {
                showAlert("Error", "Database not connected!");
            }
        });

        box.getChildren().addAll(heading, username, password, confirmPassword, roleCombo, signup);
        return box;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
