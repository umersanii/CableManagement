package com.cablemanagement.views.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

        PasswordField oldPass = new PasswordField();
        oldPass.setPromptText("Old Password");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");

        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm New Password");

        Button submit = new Button("Update Password");

        box.getChildren().addAll(heading, oldPass, newPass, confirmPass, submit);
        return box;
    }

    private static VBox createLogoutPrompt() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Are you sure you want to logout?");
        Button logout = new Button("Confirm Logout");

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

        Button signup = new Button("Create Account");

        box.getChildren().addAll(heading, username, password, signup);
        return box;
    }
}
