package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.List;

import com.cablemanagement.model.Salesman;
import com.cablemanagement.database.SQLiteDatabase;
import com.cablemanagement.database.db;

public class SalesmanContent {

    private static final ObservableList<Salesman> salesmanList = FXCollections.observableArrayList();
    private static Salesman selectedSalesman = null;
    private static final db database = new SQLiteDatabase();

    public static Node get() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("bordered-box");

        Label heading = new Label("SALESMAN");
        heading.setStyle("-fx-font-size: 24px; -fx-text-fill: #333333;");

        Label subHeading = new Label("Manage Salesman Accounts");
        subHeading.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555;");

        // Input Fields
        TextField nameField = new TextField(); nameField.setPromptText("Full Name");
        TextField contactField = new TextField(); contactField.setPromptText("Contact Number");
        TextField emailField = new TextField(); emailField.setPromptText("Email");
        TextField cnicField = new TextField(); cnicField.setPromptText("CNIC");
        TextField addressField = new TextField(); addressField.setPromptText("Address");

        Button actionButton = new Button("Add Salesman");
        actionButton.getStyleClass().add("form-submit");

        // Layout for fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));
        formGrid.addRow(0, nameField, contactField);
        formGrid.addRow(1, emailField, cnicField);
        formGrid.addRow(2, addressField, actionButton);

        // Make inputs responsive
        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(col, col);

        // Table - load data from database initially
        refreshSalesmanTable();
        TableView<Salesman> table = new TableView<>(salesmanList);
        table.setPrefHeight(300);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.getColumns().add(createCol("Name", "name"));
        table.getColumns().add(createCol("Contact", "contact"));
        table.getColumns().add(createCol("Email", "email"));
        table.getColumns().add(createCol("CNIC", "cnic"));
        table.getColumns().add(createCol("Address", "address"));
        table.getColumns().add(createActionCol(nameField, contactField, emailField, cnicField, addressField, actionButton, table));

        // Add / Update button action
        actionButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String email = emailField.getText().trim();
            String cnic = cnicField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty() || contact.isEmpty()) {
                showAlert("Error", "Name and Contact are required!");
                return;
            }

            if (selectedSalesman == null) {
                // Add new salesman to database
                boolean success = database.insertSalesman(name, contact, cnic, address);
                if (success) {
                    salesmanList.add(new Salesman(name, contact, email, cnic, address));
                    showAlert("Success", "Salesman added successfully!");
                } else {
                    showAlert("Error", "Failed to add salesman to database!");
                    return;
                }
            } else {
                // Update existing salesman (local update only for now)
                selectedSalesman.setName(name);
                selectedSalesman.setContact(contact);
                selectedSalesman.setEmail(email);
                selectedSalesman.setCnic(cnic);
                selectedSalesman.setAddress(address);
                table.refresh();
                selectedSalesman = null;
                actionButton.setText("Add Salesman");
            }

            nameField.clear(); contactField.clear(); emailField.clear();
            cnicField.clear(); addressField.clear();
        });

        mainLayout.getChildren().addAll(heading, subHeading, formGrid, table);

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        return scrollPane;
    }

    private static TableColumn<Salesman, String> createCol(String title, String property) {
        TableColumn<Salesman, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private static TableColumn<Salesman, Void> createActionCol(
        TextField nameField, TextField contactField, TextField emailField,
        TextField cnicField, TextField addressField, Button actionButton,
        TableView<Salesman> table
    ) {
        TableColumn<Salesman, Void> actionCol = new TableColumn<>("Action");

        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");

            {
                editBtn.getStyleClass().add("form-submit");
                editBtn.setOnAction(e -> {
                    selectedSalesman = getTableView().getItems().get(getIndex());
                    nameField.setText(selectedSalesman.getName());
                    contactField.setText(selectedSalesman.getContact());
                    emailField.setText(selectedSalesman.getEmail());
                    cnicField.setText(selectedSalesman.getCnic());
                    addressField.setText(selectedSalesman.getAddress());
                    actionButton.setText("Update Salesman");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        return actionCol;
    }

    private static void refreshSalesmanTable() {
        salesmanList.clear();
        List<Object[]> salesmen = database.getAllSalesmen();
        
        for (Object[] row : salesmen) {
            salesmanList.add(new Salesman(
                (String) row[1],  // salesman_name
                (String) row[2],  // phone_number
                "",               // email (not in database)
                (String) row[3],  // cnic
                (String) row[4]   // address
            ));
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
