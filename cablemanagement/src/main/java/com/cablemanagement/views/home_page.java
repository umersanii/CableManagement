package com.cablemanagement.views;

import com.cablemanagement.views.pages.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;

public class home_page {

    private static boolean isCollapsed = false;

    public static Scene getHomeScene() {
        BorderPane mainLayout = new BorderPane();

        VBox sidebarContent = new VBox(10);
        sidebarContent.setPadding(new Insets(10));
        sidebarContent.getStyleClass().add("sidebar");

        ScrollPane scrollPane = new ScrollPane(sidebarContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefWidth(200);
        scrollPane.getStyleClass().add("custom-scroll");

        StackPane contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.getChildren().add(HomeContent.get());

        // Standard Emoji + Arial Font
        Button homeBtn = createSidebarButton("🏠 Home");
        Button settingsBtn = createSidebarButton("⚙️ Settings");
        Button profileBtn = createSidebarButton("☺ Profile");
        Button registerBtn = createSidebarButton("✎ Register");
        Button rawStockBtn = createSidebarButton("📦 Raw Stock");
        Button productionStockBtn = createSidebarButton("🏭 Production");

        Button booksBtn = createSidebarButton("📚 Books");
        Button bankMgmtBtn = createSidebarButton("Ⓑ Bank Mgmt");
        Button salesmanBtn = createSidebarButton("☺ Salesman");
        Button cashInHandBtn = createSidebarButton("💵 Cash In Hand");
        Button employeeMgmtBtn = createSidebarButton("☺ Employees");
        Button reportsBtn = createSidebarButton("📊 Reports");

        Button collapseBtn = new Button("⏪");
        collapseBtn.setFont(Font.font("Arial", 14));
        collapseBtn.setMaxWidth(Double.MAX_VALUE);
        collapseBtn.getStyleClass().add("collapse-button");

        sidebarContent.getChildren().addAll(
            homeBtn, settingsBtn, profileBtn, registerBtn,
            rawStockBtn, productionStockBtn,
            booksBtn, bankMgmtBtn, salesmanBtn, cashInHandBtn,
            employeeMgmtBtn, reportsBtn,
            collapseBtn
        );

        mainLayout.setLeft(scrollPane);
        mainLayout.setCenter(contentArea);

        // Button actions
        homeBtn.setOnAction(e -> contentArea.getChildren().setAll(HomeContent.get()));
        settingsBtn.setOnAction(e -> contentArea.getChildren().setAll(SettingsContent.get()));
        profileBtn.setOnAction(e -> contentArea.getChildren().setAll(ProfileContent.get()));
        registerBtn.setOnAction(e -> contentArea.getChildren().setAll(RegisterContent.get()));
        rawStockBtn.setOnAction(e -> contentArea.getChildren().setAll(RawStock.get()));
        productionStockBtn.setOnAction(e -> contentArea.getChildren().setAll(ProductionStock.get()));
        booksBtn.setOnAction(e -> contentArea.getChildren().setAll(BooksContent.get()));
        bankMgmtBtn.setOnAction(e -> contentArea.getChildren().setAll(BankManagementContent.get()));
        salesmanBtn.setOnAction(e -> contentArea.getChildren().setAll(SalesmanContent.get()));
        cashInHandBtn.setOnAction(e -> contentArea.getChildren().setAll(CashInHandContent.get()));
        employeeMgmtBtn.setOnAction(e -> contentArea.getChildren().setAll(EmployeeManagementContent.get()));
        reportsBtn.setOnAction(e -> contentArea.getChildren().setAll(ReportsContent.get()));

        collapseBtn.setOnAction(e -> {
            isCollapsed = !isCollapsed;
            sidebarContent.getChildren().clear();

            if (isCollapsed) {
                scrollPane.setPrefWidth(72);
                sidebarContent.getChildren().addAll(
                    createIconOnlyButton("🏠", contentArea, HomeContent.get()),
                    createIconOnlyButton("⚙️", contentArea, SettingsContent.get()),
                    createIconOnlyButton("☺", contentArea, ProfileContent.get()),
                    createIconOnlyButton("✎", contentArea, RegisterContent.get()),
                    createIconOnlyButton("📦", contentArea, RawStock.get()),
                    createIconOnlyButton("🏭", contentArea, ProductionStock.get()),
                    createIconOnlyButton("📚", contentArea, BooksContent.get()),
                    createIconOnlyButton("Ⓑ", contentArea, BankManagementContent.get()),
                    createIconOnlyButton("☺", contentArea, SalesmanContent.get()),
                    createIconOnlyButton("💵", contentArea, CashInHandContent.get()),
                    createIconOnlyButton("☺", contentArea, EmployeeManagementContent.get()),
                    createIconOnlyButton("📊", contentArea, ReportsContent.get())
                );
                collapseBtn.setText("⏩");
            } else {
                scrollPane.setPrefWidth(200);
                homeBtn.setText("🏠 Home");
                settingsBtn.setText("⚙️ Settings");
                profileBtn.setText("☺ Profile");
                registerBtn.setText("✎ Register");
                rawStockBtn.setText("📦 Raw Stock");
                productionStockBtn.setText("🏭 Production");
                booksBtn.setText("📚 Books");
                bankMgmtBtn.setText("Ⓑ Bank Mgmt");
                salesmanBtn.setText("☺ Salesman");
                cashInHandBtn.setText("💵 Cash In Hand");
                employeeMgmtBtn.setText("☺ Employees");
                reportsBtn.setText("📊 Reports");

                sidebarContent.getChildren().addAll(
                    homeBtn, settingsBtn, profileBtn, registerBtn,
                    rawStockBtn, productionStockBtn,
                    booksBtn, bankMgmtBtn, salesmanBtn, cashInHandBtn,
                    employeeMgmtBtn, reportsBtn
                );
                collapseBtn.setText("⏪");
            }

            sidebarContent.getChildren().add(collapseBtn);
        });

        Scene scene = new Scene(mainLayout, 800, 500);
        scene.getStylesheets().add(home_page.class.getResource("style.css").toExternalForm());
        return scene;
    }

    private static Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 14));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        return btn;
    }

    private static Button createIconOnlyButton(String icon, StackPane contentArea, Node targetPage) {
        Button btn = new Button(icon);
        btn.setFont(Font.font("Arial", 16));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        btn.setOnAction(e -> contentArea.getChildren().setAll(targetPage));
        btn.setAlignment(javafx.geometry.Pos.CENTER);
        btn.setStyle("-fx-text-alignment: center; -fx-alignment: center;");
        return btn;
    }
}
