package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.cablemanagement.database.SQLiteDatabase;

public class EmployeeManagementContent {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(new Label("Select an action above"));

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(72);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        addButton(buttonBar, "Manage Designations", () -> formArea.getChildren().setAll(createDesignationForm()));
        addButton(buttonBar, "Register New Employee", () -> formArea.getChildren().setAll(createRegisterEmployeeForm()));
        addButton(buttonBar, "Manage Contract-Based Employee", () -> formArea.getChildren().setAll(createContractEmployeeForm()));
        addButton(buttonBar, "Manage Salary-Based Employee", () -> formArea.getChildren().setAll(createSalaryEmployeeForm()));
        addButton(buttonBar, "View Salary Reports", () -> formArea.getChildren().setAll(createSalaryReportForm()));
        addButton(buttonBar, "Mark Employee Attendance", () -> formArea.getChildren().setAll(createAttendanceMarkForm()));
        addButton(buttonBar, "View Attendance Report", () -> formArea.getChildren().setAll(createAttendanceReportForm()));
        addButton(buttonBar, "Grant Advance Salary", () -> formArea.getChildren().setAll(createAdvanceSalaryForm()));
        addButton(buttonBar, "Register New Loan", () -> formArea.getChildren().setAll(createLoanRegisterForm()));
        addButton(buttonBar, "View Employee Loan Report", () -> formArea.getChildren().setAll(createLoanReportForm()));

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

    // ---------------- FORMS ----------------

    private static VBox createDesignationForm() {
        VBox box = baseForm("Designation Management");
        
        // Database instance
        SQLiteDatabase database = new SQLiteDatabase();
        
        // Form controls
        TextField nameField = new TextField();
        nameField.setPromptText("Designation Name");
        
        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");
        
        // Initially disable update and delete buttons
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        
        // Button styling
        addBtn.getStyleClass().add("register-button");
        updateBtn.getStyleClass().add("register-button");
        deleteBtn.getStyleClass().add("register-button");
        clearBtn.getStyleClass().add("register-button");
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);
        
        // Table for displaying designations
        TableView<DesignationTableData> table = new TableView<>();
        table.setPrefHeight(300);
        
        // Table columns
        TableColumn<DesignationTableData, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        idCol.setPrefWidth(50);
        
        TableColumn<DesignationTableData, String> nameCol = new TableColumn<>("Designation Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(200);
        
        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        
        // Observable list for table data
        ObservableList<DesignationTableData> designationData = FXCollections.observableArrayList();
        table.setItems(designationData);
        
        // Load initial data
        loadDesignationData(database, designationData);
        
        // Status label
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        
        // Event handlers
        addBtn.setOnAction(e -> {
            String designationName = nameField.getText().trim();
            if (designationName.isEmpty()) {
                statusLabel.setText("Please enter a designation name.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            if (database.insertDesignation(designationName)) {
                statusLabel.setText("Designation added successfully!");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                nameField.clear();
                loadDesignationData(database, designationData);
            } else {
                statusLabel.setText("Failed to add designation. It might already exist.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });
        
        updateBtn.setOnAction(e -> {
            DesignationTableData selected = table.getSelectionModel().getSelectedItem();
            String designationName = nameField.getText().trim();
            
            if (selected == null || designationName.isEmpty()) {
                statusLabel.setText("Please select a designation and enter a new name.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            if (database.updateDesignation(selected.getId(), designationName)) {
                statusLabel.setText("Designation updated successfully!");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                nameField.clear();
                table.getSelectionModel().clearSelection();
                updateBtn.setDisable(true);
                deleteBtn.setDisable(true);
                loadDesignationData(database, designationData);
            } else {
                statusLabel.setText("Failed to update designation.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });
        
        deleteBtn.setOnAction(e -> {
            DesignationTableData selected = table.getSelectionModel().getSelectedItem();
            
            if (selected == null) {
                statusLabel.setText("Please select a designation to delete.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            // Confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Designation");
            confirmAlert.setContentText("Are you sure you want to delete the designation '" + selected.getName() + "'?");
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (database.deleteDesignation(selected.getId())) {
                    statusLabel.setText("Designation deleted successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    nameField.clear();
                    table.getSelectionModel().clearSelection();
                    updateBtn.setDisable(true);
                    deleteBtn.setDisable(true);
                    loadDesignationData(database, designationData);
                } else {
                    statusLabel.setText("Failed to delete designation. It might be in use by employees.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });
        
        clearBtn.setOnAction(e -> {
            nameField.clear();
            table.getSelectionModel().clearSelection();
            updateBtn.setDisable(true);
            deleteBtn.setDisable(true);
            statusLabel.setText("");
        });
        
        // Table selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                updateBtn.setDisable(false);
                deleteBtn.setDisable(false);
            } else {
                updateBtn.setDisable(true);
                deleteBtn.setDisable(true);
            }
        });
        
        box.getChildren().addAll(nameField, buttonBox, statusLabel, table);
        
        return box;
    }
    
    // Helper method to load designation data into the table
    private static void loadDesignationData(SQLiteDatabase database, ObservableList<DesignationTableData> designationData) {
        designationData.clear();
        for (Object[] row : database.getAllDesignations()) {
            designationData.add(new DesignationTableData((Integer) row[0], (String) row[1]));
        }
    }
    
    // Inner class for table data with JavaFX properties
    public static class DesignationTableData {
        private final javafx.beans.property.SimpleIntegerProperty id;
        private final javafx.beans.property.SimpleStringProperty name;
        
        public DesignationTableData(int id, String name) {
            this.id = new javafx.beans.property.SimpleIntegerProperty(id);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
        }
        
        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }
        public javafx.beans.property.SimpleIntegerProperty idProperty() { return id; }
        
        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }
        public javafx.beans.property.SimpleStringProperty nameProperty() { return name; }
    }

    private static VBox createRegisterEmployeeForm() {
        VBox box = baseForm("Register New Employee");

        TextField name = new TextField();
        name.setPromptText("Full Name");

        ComboBox<String> designation = new ComboBox<>();
        designation.setPromptText("Select Designation");
        
        // Load designations from database
        SQLiteDatabase database = new SQLiteDatabase();
        for (Object[] row : database.getAllDesignations()) {
            designation.getItems().add((String) row[1]); // row[1] is the designation title
        }

        DatePicker joiningDate = new DatePicker();
        Button submit = new Button("Submit");

        box.getChildren().addAll(name, designation, joiningDate, submit);
        return box;
    }

    private static VBox createContractEmployeeForm() {
        VBox box = baseForm("Manage Contract-Based Employee");

        TextField name = new TextField();
        name.setPromptText("Employee Name");

        DatePicker contractEnd = new DatePicker();
        contractEnd.setPromptText("Contract End Date");

        Button save = new Button("Save");
        box.getChildren().addAll(name, contractEnd, save);
        return box;
    }

    private static VBox createSalaryEmployeeForm() {
        VBox box = baseForm("Manage Salary-Based Employee");

        TextField name = new TextField();
        name.setPromptText("Employee Name");

        TextField salary = new TextField();
        salary.setPromptText("Monthly Salary");

        Button save = new Button("Save");
        box.getChildren().addAll(name, salary, save);
        return box;
    }

    private static VBox createSalaryReportForm() {
        VBox box = baseForm("View Salary Reports");

        Label label = new Label("Salary report table coming soon...");
        box.getChildren().add(label);
        return box;
    }

    private static VBox createAttendanceMarkForm() {
        VBox box = baseForm("Mark Employee Attendance");

        ComboBox<String> employee = new ComboBox<>();
        employee.setPromptText("Select Employee");

        DatePicker date = new DatePicker();
        ToggleGroup status = new ToggleGroup();
        RadioButton present = new RadioButton("Present");
        RadioButton absent = new RadioButton("Absent");
        present.setToggleGroup(status);
        absent.setToggleGroup(status);

        Button mark = new Button("Mark Attendance");

        box.getChildren().addAll(employee, date, new HBox(10, present, absent), mark);
        return box;
    }

    private static VBox createAttendanceReportForm() {
        VBox box = baseForm("View Attendance Report");

        Label label = new Label("Attendance report table coming soon...");
        box.getChildren().add(label);
        return box;
    }

    private static VBox createAdvanceSalaryForm() {
        VBox box = baseForm("Grant Advance Salary");

        ComboBox<String> employee = new ComboBox<>();
        employee.setPromptText("Select Employee");

        TextField amount = new TextField();
        amount.setPromptText("Amount");

        Button grant = new Button("Grant");
        box.getChildren().addAll(employee, amount, grant);
        return box;
    }

    private static VBox createLoanRegisterForm() {
        VBox box = baseForm("Register New Loan");

        ComboBox<String> employee = new ComboBox<>();
        employee.setPromptText("Select Employee");

        TextField amount = new TextField();
        amount.setPromptText("Loan Amount");

        DatePicker dueDate = new DatePicker();
        Button save = new Button("Register Loan");

        box.getChildren().addAll(employee, amount, dueDate, save);
        return box;
    }

    private static VBox createLoanReportForm() {
        VBox box = baseForm("View Employee Loan Report");

        Label label = new Label("Loan report table coming soon...");
        box.getChildren().add(label);
        return box;
    }

    private static VBox baseForm(String title) {
        VBox form = new VBox(15);
        form.setPadding(new Insets(30));
        form.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 20px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        form.getChildren().add(heading);
        return form;
    }
}
