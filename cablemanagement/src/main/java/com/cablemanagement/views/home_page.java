package com.cablemanagement.views;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.Node;

public class home_page {

    private static boolean isCollapsed = false;

    public static Scene getHomeScene() {
        BorderPane mainLayout = new BorderPane();

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(200);

        StackPane contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.getChildren().add(getHomeContent());

        // Create full and collapsed versions of buttons
        Button homeBtn = createSidebarButton("🏠", "Home");
        Button settingsBtn = createSidebarButton("⚙️", "Settings");
        Button profileBtn = createSidebarButton("👤", "Profile");

        Button collapseBtn = new Button("⏪");
        collapseBtn.setMaxWidth(Double.MAX_VALUE);
        collapseBtn.getStyleClass().add("collapse-button");

        sidebar.getChildren().addAll(homeBtn, settingsBtn, profileBtn, collapseBtn);
        mainLayout.setLeft(sidebar);

        // Button Actions
        homeBtn.setOnAction(e -> contentArea.getChildren().setAll(getHomeContent()));
        settingsBtn.setOnAction(e -> contentArea.getChildren().setAll(getSettingsContent()));
        profileBtn.setOnAction(e -> contentArea.getChildren().setAll(getProfileContent()));

        collapseBtn.setOnAction(e -> {
            isCollapsed = !isCollapsed;
            sidebar.getChildren().clear();

            if (isCollapsed) {
                sidebar.setPrefWidth(60);
                sidebar.getChildren().addAll(
                        createIconOnlyButton("🏠", contentArea, getHomeContent()),
                        createIconOnlyButton("⚙️", contentArea, getSettingsContent()),
                        createIconOnlyButton("👤", contentArea, getProfileContent())
                );
                collapseBtn.setText("⏩");
            } else {
                sidebar.setPrefWidth(200);
                homeBtn.setText("🏠 Home");
                settingsBtn.setText("⚙️ Settings");
                profileBtn.setText("👤 Profile");
                sidebar.getChildren().addAll(homeBtn, settingsBtn, profileBtn);
                collapseBtn.setText("⏪");
            }

            sidebar.getChildren().add(collapseBtn);
        });

        mainLayout.setCenter(contentArea);

        Scene scene = new Scene(mainLayout, 800, 500);
        // Load external CSS
        scene.getStylesheets().add(home_page.class.getResource("style.css").toExternalForm());
        return scene;
    }

    private static Button createSidebarButton(String icon, String text) {
        Button btn = new Button(icon + " " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        return btn;
    }

    private static Button createIconOnlyButton(String icon, StackPane contentArea, Node targetPage) {
        Button btn = new Button(icon);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        btn.setOnAction(e -> contentArea.getChildren().setAll(targetPage));
        return btn;
    }

    private static Node getHomeContent() {
        Label label = new Label("🏠 Home Page");
        label.setFont(new Font(24));
        return new StackPane(label);
    }

    private static Node getSettingsContent() {
        Label label = new Label("⚙️ Settings Page");
        label.setFont(new Font(24));
        return new StackPane(label);
    }

    private static Node getProfileContent() {
        Label label = new Label("👤 Profile Page");
        label.setFont(new Font(24));
        return new StackPane(label);
    }
}
