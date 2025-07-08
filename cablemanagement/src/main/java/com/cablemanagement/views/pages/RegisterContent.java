package com.cablemanagement.views.pages;

import com.cablemanagement.model.Brand;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Manufacturer;
import com.cablemanagement.model.Supplier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RegisterContent {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        // Form area (center of the screen)
        StackPane formArea = new StackPane();
        formArea.getChildren().add(createCategoryForm());

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        // Important: force button bar to compute its own height
        buttonBar.setMinHeight(Region.USE_PREF_SIZE);
        buttonBar.setMaxHeight(Region.USE_PREF_SIZE);
        buttonBar.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // Wrap HBox in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(buttonBar);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(false); // This disables vertical expansion
        scrollPane.setPrefHeight(72);
        scrollPane.setMinHeight(72);
        scrollPane.setMaxHeight(72);

        // Styling (optional)
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Buttons and actions
        addButton(buttonBar, "Register Category", () -> formArea.getChildren().setAll(createCategoryForm()));
        addButton(buttonBar, "Register Manufacturer", () -> formArea.getChildren().setAll(createManufacturerForm()));
        addButton(buttonBar, "Register Brand", () -> formArea.getChildren().setAll(createBrandForm()));
        addButton(buttonBar, "Register Province", () -> formArea.getChildren().setAll(createProvinceForm()));
        addButton(buttonBar, "Register District", () -> formArea.getChildren().setAll(createDistrictForm()));
        addButton(buttonBar, "Register Tehsil", () -> formArea.getChildren().setAll(createTehsilForm()));
        addButton(buttonBar, "Register Units", () -> formArea.getChildren().setAll(createUnitForm()));
        addButton(buttonBar, "Register Customer", () -> formArea.getChildren().setAll(createCustomerForm()));
        addButton(buttonBar, "Register Supplier", () -> formArea.getChildren().setAll(createSupplierForm()));

        mainLayout.setTop(scrollPane);
        mainLayout.setCenter(formArea);

        return mainLayout;
    }

    private static void addButton(HBox bar, String label, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("register-button");
        btn.setOnAction(e -> action.run());
        bar.getChildren().add(btn);
    }

    // --------------------- FORMS ---------------------
    /// TODO: Integrate with databse
    /// - Fetch data dynamicly from data base

    private static VBox createCategoryForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Category");
        heading.getStyleClass().add("form-heading");

        Label categoryLabel = new Label("Category Name:");
        categoryLabel.getStyleClass().add("form-label");

        TextField categoryField = new TextField();
        categoryField.getStyleClass().add("form-input");
        
        Button submit = new Button("Submit Category");
        submit.getStyleClass().add("form-submit");

        HBox row = new HBox(10, categoryLabel, categoryField, submit);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");

        // Category list
        Label listHeading = new Label("Registered Categories:");
        listHeading.getStyleClass().add("form-subheading");

        ListView<String> categoryList = new ListView<>();
        categoryList.getStyleClass().add("category-list");

        VBox.setVgrow(categoryList, Priority.ALWAYS);

        // TODO: Fill category from databse

        // TODO: Implement Submit action
        submit.setOnAction(e -> {
            String name = categoryField.getText().trim();
            if (!name.isEmpty()) {
                categoryList.getItems().add(name);
                categoryField.clear();
            }
        });

        form.getChildren().addAll(heading, row, listHeading, categoryList);
        return form;
    }

    private static VBox createManufacturerForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Manufacturer");
        heading.getStyleClass().add("form-heading");

        // ----- Input Fields -----
        TextField nameField = new TextField();
        nameField.getStyleClass().add("form-input");

        ComboBox<String> provinceBox = new ComboBox<>();
        provinceBox.getStyleClass().add("form-input");

        ComboBox<String> districtBox = new ComboBox<>();
        districtBox.getStyleClass().add("form-input");

        ComboBox<String> tehsilBox = new ComboBox<>();
        tehsilBox.getStyleClass().add("form-input");

        // Labels
        Label nameLabel = new Label("Name:");
        nameLabel.getStyleClass().add("form-label");

        // Rows
        HBox nameRow = new HBox(10, nameLabel, nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.getStyleClass().add("form-row");

        // Group all 3 boxes in one row
        HBox locationRow = new HBox(20);
        locationRow.setAlignment(Pos.CENTER_LEFT);
        locationRow.getStyleClass().add("form-row");

        VBox provinceBoxWrap = new VBox(new Label("Province:"), provinceBox);
        provinceBoxWrap.getStyleClass().add("form-combo-wrap");

        VBox districtBoxWrap = new VBox(new Label("District:"), districtBox);
        districtBoxWrap.getStyleClass().add("form-combo-wrap");

        VBox tehsilBoxWrap = new VBox(new Label("Tehsil:"), tehsilBox);
        tehsilBoxWrap.getStyleClass().add("form-combo-wrap");

        locationRow.getChildren().addAll(provinceBoxWrap, districtBoxWrap, tehsilBoxWrap);

        // Submit Button
        Button submitBtn = new Button("Submit Manufacturer");
        submitBtn.getStyleClass().add("form-submit");

        // ----- Table of Existing Manufacturers -----
        Label tableHeading = new Label("Existing Manufacturers:");
        tableHeading.getStyleClass().add("form-subheading");

        TableView<Manufacturer> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("category-list");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Take max width

        TableColumn<Manufacturer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Manufacturer, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setCellValueFactory(data -> data.getValue().provinceProperty());

        TableColumn<Manufacturer, String> districtCol = new TableColumn<>("District");
        districtCol.setCellValueFactory(data -> data.getValue().districtProperty());

        TableColumn<Manufacturer, String> tehsilCol = new TableColumn<>("Tehsil");
        tehsilCol.setCellValueFactory(data -> data.getValue().tehsilProperty());

        table.getColumns().addAll(nameCol, provinceCol, districtCol, tehsilCol);
        
        // TODO: Dynamically add data to table 

        // TODO: Submit Action
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String province = provinceBox.getValue();
            String district = districtBox.getValue();
            String tehsil = tehsilBox.getValue();

            if (!name.isEmpty()) {
                Manufacturer m = new Manufacturer(name, province, district, tehsil);
                table.getItems().add(m);
                nameField.clear();
                provinceBox.getSelectionModel().clearSelection();
                districtBox.getSelectionModel().clearSelection();
                tehsilBox.getSelectionModel().clearSelection();
            }
        });

        form.getChildren().addAll(
            heading,
            nameRow,
            locationRow,
            submitBtn,
            tableHeading,
            table
        );

        return form;
    }

    private static VBox createBrandForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Brand");
        heading.getStyleClass().add("form-heading");

        // ----- Input Fields -----
        TextField brandField = new TextField();
        brandField.getStyleClass().add("form-input");

        ComboBox<String> provinceBox = new ComboBox<>();
        provinceBox.getStyleClass().add("form-input");

        ComboBox<String> districtBox = new ComboBox<>();
        districtBox.getStyleClass().add("form-input");

        ComboBox<String> tehsilBox = new ComboBox<>();
        tehsilBox.getStyleClass().add("form-input");

        // Labels
        Label brandLabel = new Label("Brand Name:");
        brandLabel.getStyleClass().add("form-label");

        // --- Rows ---
        HBox brandRow = new HBox(10, brandLabel, brandField);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        brandRow.getStyleClass().add("form-row");

        // Province, District, Tehsil in one row
        VBox provinceWrap = new VBox(new Label("Province:"), provinceBox);
        VBox districtWrap = new VBox(new Label("District:"), districtBox);
        VBox tehsilWrap = new VBox(new Label("Tehsil:"), tehsilBox);

        provinceWrap.getStyleClass().add("form-combo-wrap");
        districtWrap.getStyleClass().add("form-combo-wrap");
        tehsilWrap.getStyleClass().add("form-combo-wrap");

        HBox locationRow = new HBox(20, provinceWrap, districtWrap, tehsilWrap);
        locationRow.setAlignment(Pos.CENTER_LEFT);
        locationRow.getStyleClass().add("form-row");

        // Submit Button
        Button submitBtn = new Button("Submit Brand");
        submitBtn.getStyleClass().add("form-submit");

        // Table Heading
        Label tableHeading = new Label("Existing Brands:");
        tableHeading.getStyleClass().add("form-subheading");

        // Table View
        TableView<Brand> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("category-list");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Brand, String> brandCol = new TableColumn<>("Brand Name");
        brandCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Brand, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setCellValueFactory(data -> data.getValue().provinceProperty());

        TableColumn<Brand, String> districtCol = new TableColumn<>("District");
        districtCol.setCellValueFactory(data -> data.getValue().districtProperty());

        TableColumn<Brand, String> tehsilCol = new TableColumn<>("Tehsil");
        tehsilCol.setCellValueFactory(data -> data.getValue().tehsilProperty());

        table.getColumns().addAll(brandCol, provinceCol, districtCol, tehsilCol);

        // Submit Action
        submitBtn.setOnAction(e -> {
            String name = brandField.getText().trim();
            String province = provinceBox.getValue();
            String district = districtBox.getValue();
            String tehsil = tehsilBox.getValue();

            if (!name.isEmpty()) {
                Brand brand = new Brand(name, province, district, tehsil);
                table.getItems().add(brand);

                brandField.clear();
                provinceBox.getSelectionModel().clearSelection();
                districtBox.getSelectionModel().clearSelection();
                tehsilBox.getSelectionModel().clearSelection();
            }
        });

        // Add all elements to the form
        form.getChildren().addAll(
            heading,
            brandRow,
            locationRow,
            submitBtn,
            tableHeading,
            table
        );

        return form;
    }

    private static VBox createProvinceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Province");
        heading.getStyleClass().add("form-heading");

        Label provinceLabel = new Label("Province Name:");
        provinceLabel.getStyleClass().add("form-label");

        TextField provinceField = new TextField();
        provinceField.getStyleClass().add("form-input");

        Button submit = new Button("Submit Province");
        submit.getStyleClass().add("form-submit");

        HBox row = new HBox(10, provinceLabel, provinceField, submit);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");

        // Province list
        Label listHeading = new Label("Registered Provinces:");
        listHeading.getStyleClass().add("form-subheading");

        ListView<String> provinceList = new ListView<>();
        provinceList.getStyleClass().add("category-list");

        VBox.setVgrow(provinceList, Priority.ALWAYS);

        // TODO: Fetch provinces from database

        submit.setOnAction(e -> {
            String name = provinceField.getText().trim();
            if (!name.isEmpty()) {
                provinceList.getItems().add(name);
                provinceField.clear();
            }
        });

        form.getChildren().addAll(heading, row, listHeading, provinceList);
        return form;
    }

    private static VBox createDistrictForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register District");
        heading.getStyleClass().add("form-heading");

        // Labels
        Label nameLabel = new Label("District Name:");
        nameLabel.getStyleClass().add("form-label");

        Label provinceLabel = new Label("Province:");
        provinceLabel.getStyleClass().add("form-label");

        // Inputs
        TextField nameField = new TextField();
        nameField.getStyleClass().add("form-input");

        ComboBox<String> provinceBox = new ComboBox<>();
        provinceBox.getStyleClass().add("form-input");

        // Submit button
        Button submit = new Button("Submit District");
        submit.getStyleClass().add("form-submit");

        // Row for inputs
        HBox row1 = new HBox(10, nameLabel, nameField);
        HBox row2 = new HBox(10, provinceLabel, provinceBox, submit);

        for (HBox row : new HBox[]{row1, row2}) {
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("form-row");
        }

        // District list
        Label listHeading = new Label("Registered Districts:");
        listHeading.getStyleClass().add("form-subheading");

        ListView<String> districtList = new ListView<>();
        districtList.getStyleClass().add("category-list");
        VBox.setVgrow(districtList, Priority.ALWAYS);

        // Submit action
        submit.setOnAction(e -> {
            String name = nameField.getText().trim();
            String province = provinceBox.getValue();
            if (!name.isEmpty()) {
                String display = province != null ? name + " (" + province + ")" : name;
                districtList.getItems().add(display);
                nameField.clear();
                provinceBox.getSelectionModel().clearSelection();
            }
        });

        form.getChildren().addAll(heading, row1, row2, listHeading, districtList);
        return form;
    }

    private static VBox createTehsilForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Tehsil");
        heading.getStyleClass().add("form-heading");

        // Labels
        Label nameLabel = new Label("Tehsil Name:");
        nameLabel.getStyleClass().add("form-label");

        Label districtLabel = new Label("District:");
        districtLabel.getStyleClass().add("form-label");

        // Inputs
        TextField nameField = new TextField();
        nameField.getStyleClass().add("form-input");

        ComboBox<String> districtBox = new ComboBox<>();
        districtBox.getStyleClass().add("form-input");

        // Submit button
        Button submit = new Button("Submit Tehsil");
        submit.getStyleClass().add("form-submit");

        // Input rows
        HBox row1 = new HBox(10, nameLabel, nameField);
        HBox row2 = new HBox(10, districtLabel, districtBox, submit);

        for (HBox row : new HBox[]{row1, row2}) {
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("form-row");
        }

        // Tehsil list
        Label listHeading = new Label("Registered Tehsils:");
        listHeading.getStyleClass().add("form-subheading");

        ListView<String> tehsilList = new ListView<>();
        tehsilList.getStyleClass().add("category-list");
        VBox.setVgrow(tehsilList, Priority.ALWAYS);

        // Submit action
        submit.setOnAction(e -> {
            String name = nameField.getText().trim();
            String district = districtBox.getValue();
            if (!name.isEmpty()) {
                String display = district != null ? name + " (" + district + ")" : name;
                tehsilList.getItems().add(display);
                nameField.clear();
                districtBox.getSelectionModel().clearSelection();
            }
        });

        form.getChildren().addAll(heading, row1, row2, listHeading, tehsilList);
        return form;
    }

    private static VBox createUnitForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Units");
        heading.getStyleClass().add("form-heading");

        Label nameLabel = new Label("Unit Name:");
        nameLabel.getStyleClass().add("form-label");

        TextField nameField = new TextField();
        nameField.getStyleClass().add("form-input");

        Button submit = new Button("Submit Unit");
        submit.getStyleClass().add("form-submit");

        HBox row = new HBox(10, nameLabel, nameField, submit);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");

        Label listHeading = new Label("Registered Units:");
        listHeading.getStyleClass().add("form-subheading");

        ListView<String> unitList = new ListView<>();
        unitList.getStyleClass().add("category-list");
        VBox.setVgrow(unitList, Priority.ALWAYS);

        submit.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                unitList.getItems().add(name);
                nameField.clear();
            }
        });

        form.getChildren().addAll(heading, row, listHeading, unitList);
        return form;
    }

    private static VBox createCustomerForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Customer");
        heading.getStyleClass().add("form-heading");

        // Input fields
        TextField nameField = new TextField();
        TextField contactField = new TextField();
        nameField.getStyleClass().add("form-input");
        contactField.getStyleClass().add("form-input");

        Label nameLabel = new Label("Customer Name:");
        Label contactLabel = new Label("Contact:");
        nameLabel.getStyleClass().add("form-label");
        contactLabel.getStyleClass().add("form-label");

        HBox nameRow = new HBox(10, nameLabel, nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.getStyleClass().add("form-row");

        HBox contactRow = new HBox(10, contactLabel, contactField);
        contactRow.setAlignment(Pos.CENTER_LEFT);
        contactRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Customer");
        submitBtn.getStyleClass().add("form-submit");

        Label tableHeading = new Label("Registered Customers:");
        tableHeading.getStyleClass().add("form-subheading");

        // Table view
        TableView<Customer> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("category-list");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Customer, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(data -> data.getValue().contactProperty());

        table.getColumns().addAll(nameCol, contactCol);

        // Submit action
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            if (!name.isEmpty()) {
                Customer customer = new Customer(name, contact);
                table.getItems().add(customer);
                nameField.clear();
                contactField.clear();
            }
        });

        form.getChildren().addAll(
            heading,
            nameRow,
            contactRow,
            submitBtn,
            tableHeading,
            table
        );

        return form;
    }

    private static VBox createSupplierForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Supplier");
        heading.getStyleClass().add("form-heading");

        // Input fields
        TextField nameField = new TextField();
        TextField contactField = new TextField();
        nameField.getStyleClass().add("form-input");
        contactField.getStyleClass().add("form-input");

        Label nameLabel = new Label("Supplier Name:");
        Label contactLabel = new Label("Contact:");
        nameLabel.getStyleClass().add("form-label");
        contactLabel.getStyleClass().add("form-label");

        HBox nameRow = new HBox(10, nameLabel, nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.getStyleClass().add("form-row");

        HBox contactRow = new HBox(10, contactLabel, contactField);
        contactRow.setAlignment(Pos.CENTER_LEFT);
        contactRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Supplier");
        submitBtn.getStyleClass().add("form-submit");

        Label tableHeading = new Label("Registered Suppliers:");
        tableHeading.getStyleClass().add("form-subheading");

        // TableView for suppliers
        TableView<Supplier> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("category-list");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Supplier, String> nameCol = new TableColumn<>("Supplier Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Supplier, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(data -> data.getValue().contactProperty());

        table.getColumns().addAll(nameCol, contactCol);

        // Submit action
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            if (!name.isEmpty()) {
                Supplier supplier = new Supplier(name, contact);
                table.getItems().add(supplier);
                nameField.clear();
                contactField.clear();
            }
        });

        form.getChildren().addAll(
            heading,
            nameRow,
            contactRow,
            submitBtn,
            tableHeading,
            table
        );

        return form;
    }

}
