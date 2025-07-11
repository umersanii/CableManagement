package com.cablemanagement.views;

import com.cablemanagement.views.pages.HomeContent;
import com.cablemanagement.views.pages.SettingsContent;
import com.cablemanagement.views.pages.ProfileContent;
import com.cablemanagement.views.pages.RegisterContent;
import com.cablemanagement.views.pages.RawStock;
import com.cablemanagement.views.pages.ProductionStock;
import com.cablemanagement.views.pages.Books;
import com.cablemanagement.views.pages.BankManagement;
import com.cablemanagement.views.pages.EmployeeManagement;


import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
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
        contentArea.getChildren().add(HomeContent.get());

        // Create full and collapsed versions of buttons
        Button homeBtn = createSidebarButton("🏠", "Home");
        Button settingsBtn = createSidebarButton("⚙️", "Settings");
        Button profileBtn = createSidebarButton("👤", "Profile");
        Button registerBtn = createSidebarButton("📝", "Register");
        Button rawStockBtn = createSidebarButton("📦", "Raw Stock");
        Button productionStockBtn = createSidebarButton("🏭", "Production Stock");
        Button bookBtn = createSidebarButton("📚", "Book");
        Button bankManagementBtn = createSidebarButton("🏦", "Bank Management");
        Button employeeManagementBtn = createSidebarButton("👥", "Employee Management");

        Button collapseBtn = new Button("⏪");
        collapseBtn.setMaxWidth(Double.MAX_VALUE);
        collapseBtn.getStyleClass().add("collapse-button");

        sidebar.getChildren().addAll(homeBtn, settingsBtn, profileBtn, registerBtn, rawStockBtn, productionStockBtn, bookBtn, bankManagementBtn, employeeManagementBtn, collapseBtn);
        mainLayout.setLeft(sidebar);

        // Button Actions
        homeBtn.setOnAction(e -> contentArea.getChildren().setAll(HomeContent.get()));
        settingsBtn.setOnAction(e -> contentArea.getChildren().setAll(SettingsContent.get()));
        profileBtn.setOnAction(e -> contentArea.getChildren().setAll(ProfileContent.get()));
        registerBtn.setOnAction(e -> contentArea.getChildren().setAll(RegisterContent.get()));
        rawStockBtn.setOnAction(e -> contentArea.getChildren().setAll(RawStock.get()));
        productionStockBtn.setOnAction(e -> contentArea.getChildren().setAll(ProductionStock.get()));
        bookBtn.setOnAction(e -> contentArea.getChildren().setAll(Books.get()));
        bankManagementBtn.setOnAction(e -> contentArea.getChildren().setAll(BankManagement.get()));
        employeeManagementBtn.setOnAction(e -> contentArea.getChildren().setAll(EmployeeManagement.get()));

        collapseBtn.setOnAction(e -> {
            isCollapsed = !isCollapsed;
            sidebar.getChildren().clear();

            if (isCollapsed) {
                sidebar.setPrefWidth(60);
                sidebar.getChildren().addAll(
                        createIconOnlyButton("🏠", contentArea, HomeContent.get()),
                        createIconOnlyButton("⚙️", contentArea, SettingsContent.get()),
                        createIconOnlyButton("👤", contentArea, ProfileContent.get()),
                        createIconOnlyButton("📝", contentArea, RegisterContent.get()),
                        createIconOnlyButton("📦", contentArea, RawStock.get()),
                        createIconOnlyButton("🏭", contentArea, ProductionStock.get()),
                        createIconOnlyButton("📚", contentArea, Books.get())
                        , createIconOnlyButton("🏦", contentArea, BankManagement.get())
                        , createIconOnlyButton("👥", contentArea, EmployeeManagement.get())
                );
                collapseBtn.setText("⏩");
            } else {
                sidebar.setPrefWidth(200);
                homeBtn.setText("🏠 Home");
                settingsBtn.setText("⚙️ Settings");
                profileBtn.setText("👤 Profile");
                registerBtn.setText("📝 Register");
                rawStockBtn.setText("📦 Raw Stock");
                productionStockBtn.setText("🏭 Production Stock");
                bookBtn.setText("📚 Book");
                sidebar.getChildren().addAll(homeBtn, settingsBtn, profileBtn, registerBtn, rawStockBtn, productionStockBtn, bookBtn, bankManagementBtn, employeeManagementBtn);
                collapseBtn.setText("⏪");
            }

            sidebar.getChildren().add(collapseBtn);
        });

        mainLayout.setCenter(contentArea);

        Scene scene = new Scene(mainLayout, 800, 500);
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
}