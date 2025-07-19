package com.cablemanagement.views.pages;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import com.cablemanagement.database.SQLiteDatabase;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        addButton(buttonBar, "Contract-Based Employee", () -> formArea.getChildren().setAll(createContractEmployeeForm()));
        addButton(buttonBar, "Manage All Employees", () -> formArea.getChildren().setAll(createSalaryEmployeeForm()));
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

    ////////////////////////////////////////////////////////////////////////
    ///                    Designation Management Form                  ////
    ////////////////////////////////////////////////////////////////////////

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

    //////////////////////////////////////////////////////////////////////////
    ///                   Register New Employee Form                     /////
    //////////////////////////////////////////////////////////////////////////

    private static VBox createRegisterEmployeeForm() {
        VBox box = baseForm("Register New Employee");
        
        // Database instance
        SQLiteDatabase database = new SQLiteDatabase();

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField cnicField = new TextField();
        cnicField.setPromptText("CNIC");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        ComboBox<String> designationCombo = new ComboBox<>();
        designationCombo.setPromptText("Select Designation");
        
        // Load designations from database
        for (Object[] row : database.getAllDesignations()) {
            designationCombo.getItems().add((String) row[1]); // row[1] is the designation title
        }

        ComboBox<String> salaryTypeCombo = new ComboBox<>();
        salaryTypeCombo.setPromptText("Select Salary Type");
        salaryTypeCombo.getItems().addAll("monthly", "daily", "hourly", "task");

        TextField salaryAmountField = new TextField();
        salaryAmountField.setPromptText("Salary Amount");

        Button submitBtn = new Button("Register Employee");
        submitBtn.getStyleClass().add("register-button");
        
        // Status label
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        // Event handler
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String cnic = cnicField.getText().trim();
            String address = addressField.getText().trim();
            String designation = designationCombo.getValue();
            String salaryType = salaryTypeCombo.getValue();
            String salaryAmountText = salaryAmountField.getText().trim();
            
            // Validation
            if (name.isEmpty() || designation == null || salaryType == null || salaryAmountText.isEmpty()) {
                statusLabel.setText("Please fill in all required fields.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            try {
                double salaryAmount = Double.parseDouble(salaryAmountText);
                if (salaryAmount <= 0) {
                    statusLabel.setText("Salary amount must be positive.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }
                
                if (database.insertEmployee(name, phone, cnic, address, designation, salaryType, salaryAmount)) {
                    statusLabel.setText("Employee registered successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    // Clear form
                    nameField.clear();
                    phoneField.clear();
                    cnicField.clear();
                    addressField.clear();
                    designationCombo.setValue(null);
                    salaryTypeCombo.setValue(null);
                    salaryAmountField.clear();
                } else {
                    statusLabel.setText("Failed to register employee.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid salary amount.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });

        box.getChildren().addAll(nameField, phoneField, cnicField, addressField, 
                                designationCombo, salaryTypeCombo, salaryAmountField, 
                                submitBtn, statusLabel);
        return box;
    }

    //////////////////////////////////////////////////////////////////////////
    ///                   Contact based Employee Form                    /////
    //////////////////////////////////////////////////////////////////////////

    private static VBox createContractEmployeeForm() {
        VBox box = baseForm("Contract-Based Employees");

        SQLiteDatabase database = new SQLiteDatabase();

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name, phone, or designation...");
        searchField.setMaxWidth(300);

        // Info label
        Label infoLabel = new Label("Contract-based employees are those with daily, hourly, or task-based payment types.");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        // Table setup
        TableView<EmployeeTableData> table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<EmployeeTableData, String> nameCol = new TableColumn<>("Employee Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<EmployeeTableData, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());

        TableColumn<EmployeeTableData, String> designationCol = new TableColumn<>("Designation");
        designationCol.setCellValueFactory(cellData -> cellData.getValue().designationProperty());

        TableColumn<EmployeeTableData, String> salaryTypeCol = new TableColumn<>("Payment Type");
        salaryTypeCol.setCellValueFactory(cellData -> cellData.getValue().salaryTypeProperty());

        TableColumn<EmployeeTableData, Double> rateCol = new TableColumn<>("Rate");
        rateCol.setCellValueFactory(cellData -> cellData.getValue().salaryAmountProperty().asObject());

        TableColumn<EmployeeTableData, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        table.getColumns().addAll(nameCol, phoneCol, designationCol, salaryTypeCol, rateCol, statusCol);

        // Data setup
        ObservableList<EmployeeTableData> contractEmployeeData = FXCollections.observableArrayList();
        table.setItems(contractEmployeeData);
        loadContractEmployeeData(database, contractEmployeeData);

        // Search logic
        FilteredList<EmployeeTableData> filteredData = new FilteredList<>(contractEmployeeData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase().trim();
            filteredData.setPredicate(emp -> {
                if (lower.isEmpty()) return true;
                return emp.getName().toLowerCase().contains(lower)
                    || emp.getPhone().toLowerCase().contains(lower)
                    || emp.getDesignation().toLowerCase().contains(lower);
            });
        });
        table.setItems(filteredData);

        VBox content = new VBox(10, searchField, infoLabel, table);
        content.setPadding(new Insets(15));
        content.setFillWidth(true);

        box.getChildren().add(content);
        return box;
    }

    //////////////////////////////////////////////////////////////////////////
    ///                    Manage All Employees                          /////
    //////////////////////////////////////////////////////////////////////////

    private static VBox createSalaryEmployeeForm() {
        VBox box = baseForm("Manage Employees");

        SQLiteDatabase database = new SQLiteDatabase();

        // Form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Employee Name");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField cnicField = new TextField();
        cnicField.setPromptText("CNIC");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        ComboBox<String> designationCombo = new ComboBox<>();
        designationCombo.setPromptText("Select Designation");
        for (Object[] row : database.getAllDesignations()) {
            designationCombo.getItems().add((String) row[1]);
        }

        ComboBox<String> salaryTypeCombo = new ComboBox<>();
        salaryTypeCombo.setPromptText("Select Salary Type");
        salaryTypeCombo.getItems().addAll("monthly", "daily", "hourly", "task");

        TextField salaryAmountField = new TextField();
        salaryAmountField.setPromptText("Salary Amount");

        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");

        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        updateBtn.getStyleClass().add("register-button");
        deleteBtn.getStyleClass().add("register-button");
        clearBtn.getStyleClass().add("register-button");

        // Form layout using GridPane
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

        formGrid.add(new Label("Name:"), 0, 0);
        formGrid.add(nameField, 1, 0);

        formGrid.add(new Label("Phone:"), 0, 1);
        formGrid.add(phoneField, 1, 1);

        formGrid.add(new Label("CNIC:"), 0, 2);
        formGrid.add(cnicField, 1, 2);

        formGrid.add(new Label("Address:"), 0, 3);
        formGrid.add(addressField, 1, 3);

        formGrid.add(new Label("Designation:"), 2, 0);
        formGrid.add(designationCombo, 3, 0);

        formGrid.add(new Label("Salary Type:"), 2, 1);
        formGrid.add(salaryTypeCombo, 3, 1);

        formGrid.add(new Label("Salary Amount:"), 2, 2);
        formGrid.add(salaryAmountField, 3, 2);

        HBox buttonBox = new HBox(10, updateBtn, deleteBtn, clearBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        formGrid.add(buttonBox, 3, 3);

        // Table
        TableView<EmployeeTableData> table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ObservableList<TableColumn<EmployeeTableData, ?>> columns = FXCollections.observableArrayList();
        columns.add(createColumn("ID", EmployeeTableData::idProperty, 50));
        columns.add(createColumn("Name", EmployeeTableData::nameProperty, 150));
        columns.add(createColumn("Phone", EmployeeTableData::phoneProperty, 120));
        columns.add(createColumn("Designation", EmployeeTableData::designationProperty, 120));
        columns.add(createColumn("Salary Type", EmployeeTableData::salaryTypeProperty, 100));
        columns.add(createColumn("Salary Amount", EmployeeTableData::salaryAmountProperty, 120));
        columns.add(createColumn("Status", EmployeeTableData::statusProperty, 80));
        table.getColumns().addAll(columns);

        ObservableList<EmployeeTableData> employeeData = FXCollections.observableArrayList();
        table.setItems(employeeData);
        loadEmployeeData(database, employeeData);

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        
        setupEventHandlers(table, database, nameField, phoneField, cnicField, addressField, 
            designationCombo, salaryTypeCombo, salaryAmountField, updateBtn, deleteBtn, statusLabel, employeeData);

        VBox content = new VBox(20, formGrid, statusLabel, table);
        content.setPadding(new Insets(15));
        content.setFillWidth(true);
        
        box.getChildren().add(content);
        return box;
    }

    private static <T> TableColumn<EmployeeTableData, T> createColumn(String title, Function<EmployeeTableData, ObservableValue<T>> property, int width) {
        TableColumn<EmployeeTableData, T> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> property.apply(cellData.getValue()));
        col.setPrefWidth(width);
        return col;
    }

    ////////////////////////////////////////////////////////////////////
    ///                   View Salary Reports Form                   ///
    ////////////////////////////////////////////////////////////////////

    private static VBox createSalaryReportForm() {
        VBox box = baseForm("View Salary Reports");

        Label label = new Label("Salary report table coming soon...");
        box.getChildren().add(label);
        return box;
    }

    ////////////////////////////////////////////////////////////////////
    ///                   Mark Employee Attendance Form              ///
    ////////////////////////////////////////////////////////////////////

private static VBox createAttendanceMarkForm() {
    VBox box = baseForm("Mark All Employees Attendance");

    SQLiteDatabase database = new SQLiteDatabase();

    // Top: Date Picker
    Label dateLabel = new Label("Select Date:");
    DatePicker datePicker = new DatePicker(LocalDate.now());

    // Table Columns: ID, Name, Role, Attendance, Hours
    TableView<EmployeeRow> table = new TableView<>();
    ObservableList<EmployeeRow> rows = FXCollections.observableArrayList();

    for (Object[] emp : database.getAllEmployees()) {
        if ("Active".equals(emp[8])) {
            rows.add(new EmployeeRow(
                (int) emp[0],
                emp[1].toString(),
                emp[5].toString()
            ));
        }
    }

    table.setItems(rows);
    table.setEditable(true);

    // Columns
    TableColumn<EmployeeRow, String> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().id)));

    TableColumn<EmployeeRow, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().name));

    TableColumn<EmployeeRow, String> roleCol = new TableColumn<>("Designation");
    roleCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().role));

    TableColumn<EmployeeRow, Boolean> statusCol = new TableColumn<>("Status (P/A)");
    statusCol.setCellFactory(col -> new TableCell<>() {
        final ToggleGroup group = new ToggleGroup();
        final RadioButton present = new RadioButton("P");
        final RadioButton absent = new RadioButton("A");
        {
            present.setToggleGroup(group);
            absent.setToggleGroup(group);
        }

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                EmployeeRow emp = getTableView().getItems().get(getIndex());
                present.setSelected(emp.present);
                absent.setSelected(!emp.present);

                group.selectedToggleProperty().addListener((obs, old, val) -> {
                    emp.present = (val == present);
                    emp.hours = emp.present ? emp.hours : "";
                    table.refresh();
                });

                HBox box = new HBox(5, present, absent);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        }
    });

    TableColumn<EmployeeRow, String> hoursCol = new TableColumn<>("Working Hours");
    hoursCol.setCellFactory(TextFieldTableCell.forTableColumn());
    hoursCol.setCellValueFactory(data -> data.getValue().hoursProperty());
    hoursCol.setOnEditCommit(e -> e.getRowValue().setHours(e.getNewValue()));

    table.getColumns().addAll(idCol, nameCol, roleCol, statusCol, hoursCol);
    table.setPrefHeight(400);

    // Submit Button & Status Label
    Button markBtn = new Button("Mark Attendance");
    Label statusLabel = new Label();

    markBtn.setOnAction(e -> {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            statusLabel.setText("Please select a date.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean allGood = true;
        for (EmployeeRow emp : rows) {
            double hours = 0.0;
            if (emp.present) {
                if (emp.hours.isEmpty()) {
                    statusLabel.setText("Missing hours for " + emp.name);
                    statusLabel.setStyle("-fx-text-fill: red;");
                    allGood = false;
                    break;
                }
                try {
                    hours = Double.parseDouble(emp.hours);
                    if (hours < 0 || hours > 24) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    statusLabel.setText("Invalid hours for " + emp.name);
                    statusLabel.setStyle("-fx-text-fill: red;");
                    allGood = false;
                    break;
                }
            }
            String status = emp.present ? "present" : "absent";
            database.insertEmployeeAttendance(emp.id, date.toString(), status, hours);
        }

        if (allGood) {
            statusLabel.setText("Attendance marked.");
            statusLabel.setStyle("-fx-text-fill: green;");
            table.refresh();
        }
    });

    box.getChildren().addAll(
        new VBox(5, dateLabel, datePicker),
        new Separator(),
        table,
        new Separator(),
        markBtn,
        statusLabel
    );

    return box;
}

public static class EmployeeRow {
    int id;
    String name;
    String role;
    boolean present = true;
    String hours = "";
    StringProperty hoursProperty = new SimpleStringProperty("");

    public EmployeeRow(int id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.hoursProperty.set(hours);
        this.hoursProperty.addListener((obs, old, val) -> hours = val);
    }

    public StringProperty hoursProperty() { return hoursProperty; }
    public void setHours(String val) { this.hours = val; this.hoursProperty.set(val); }
}



    private static VBox createAttendanceReportForm() {
        VBox box = baseForm("View Attendance Report");
        
        // Database instance
        SQLiteDatabase database = new SQLiteDatabase();
        
        // Date range filters
        HBox dateFilterBox = new HBox(10);
        dateFilterBox.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.setValue(java.time.LocalDate.now().minusDays(30)); // Default to last 30 days
        
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.setValue(java.time.LocalDate.now());
        
        Button filterBtn = new Button("Filter");
        filterBtn.getStyleClass().add("register-button");
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("register-button");
        
        dateFilterBox.getChildren().addAll(
            new Label("From:"), startDatePicker,
            new Label("To:"), endDatePicker,
            filterBtn, refreshBtn
        );
        
        // Attendance table
        TableView<AttendanceTableData> table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<AttendanceTableData, String> nameCol = new TableColumn<>("Employee Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().employeeNameProperty());
        nameCol.setPrefWidth(150);
        
        TableColumn<AttendanceTableData, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        dateCol.setPrefWidth(100);
        
        TableColumn<AttendanceTableData, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusCol.setPrefWidth(80);
        
        TableColumn<AttendanceTableData, Double> hoursCol = new TableColumn<>("Working Hours");
        hoursCol.setCellValueFactory(cellData -> cellData.getValue().workingHoursProperty().asObject());
        hoursCol.setPrefWidth(100);
        
        table.getColumns().addAll(nameCol, dateCol, statusCol, hoursCol);
        
        // Data for table
        ObservableList<AttendanceTableData> attendanceData = FXCollections.observableArrayList();
        table.setItems(attendanceData);
        
        // Load initial data
        loadAttendanceData(database, attendanceData, null, null);
        
        // Filter button action
        filterBtn.setOnAction(e -> {
            java.time.LocalDate startDate = startDatePicker.getValue();
            java.time.LocalDate endDate = endDatePicker.getValue();
            
            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Invalid Date Range");
                    alert.setHeaderText("Invalid Date Range");
                    alert.setContentText("Start date cannot be after end date.");
                    alert.showAndWait();
                    return;
                }
                loadAttendanceData(database, attendanceData, startDate.toString(), endDate.toString());
            } else {
                loadAttendanceData(database, attendanceData, null, null);
            }
        });
        
        // Refresh button action
        refreshBtn.setOnAction(e -> {
            java.time.LocalDate startDate = startDatePicker.getValue();
            java.time.LocalDate endDate = endDatePicker.getValue();
            
            if (startDate != null && endDate != null) {
                loadAttendanceData(database, attendanceData, startDate.toString(), endDate.toString());
            } else {
                loadAttendanceData(database, attendanceData, null, null);
            }
        });
        
        // Summary info
        Label summaryLabel = new Label("Attendance records will appear here");
        summaryLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        
        VBox content = new VBox(15, dateFilterBox, summaryLabel, table);
        content.setPadding(new Insets(15));
        content.setFillWidth(true);
        
        box.getChildren().add(content);
        return box;
    }

    private static VBox createAdvanceSalaryForm() {
        VBox box = baseForm("Grant Advance Salary");
        
        // Database instance
        SQLiteDatabase database = new SQLiteDatabase();

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        
        // Load active employees from database
        for (Object[] row : database.getAllEmployees()) {
            String status = (String) row[8];
            if ("Active".equals(status)) {
                employeeCombo.getItems().add((String) row[1]); // employee_name
            }
        }

        TextField amountField = new TextField();
        amountField.setPromptText("Advance Amount");

        DatePicker dateField = new DatePicker();
        dateField.setPromptText("Advance Date");
        dateField.setValue(java.time.LocalDate.now());

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description/Reason");

        Button grantBtn = new Button("Grant Advance");
        grantBtn.getStyleClass().add("register-button");
        
        // Status label
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        // Event handler (placeholder - would need salary advance table implementation)
        grantBtn.setOnAction(e -> {
            String employee = employeeCombo.getValue();
            String amountText = amountField.getText().trim();
            java.time.LocalDate date = dateField.getValue();
            String description = descriptionField.getText().trim();
            
            if (employee == null || amountText.isEmpty() || date == null) {
                statusLabel.setText("Please fill in all required fields.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    statusLabel.setText("Amount must be positive.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }
                
                // This would require implementing advance salary tracking in the database
                statusLabel.setText("Advance salary functionality needs to be implemented in the database.");
                statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid amount.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });

        box.getChildren().addAll(employeeCombo, amountField, dateField, descriptionField, grantBtn, statusLabel);
        return box;
    }

    private static VBox createLoanRegisterForm() {
        VBox box = baseForm("Register New Loan");
        
        // Database instance
        SQLiteDatabase database = new SQLiteDatabase();

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        
        // Load active employees from database
        for (Object[] row : database.getAllEmployees()) {
            String status = (String) row[8];
            if ("Active".equals(status)) {
                employeeCombo.getItems().add((String) row[1]); // employee_name
            }
        }

        TextField amountField = new TextField();
        amountField.setPromptText("Loan Amount");

        DatePicker loanDateField = new DatePicker();
        loanDateField.setPromptText("Loan Date");
        loanDateField.setValue(java.time.LocalDate.now());

        DatePicker dueDateField = new DatePicker();
        dueDateField.setPromptText("Due Date (Optional)");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description/Purpose");

        Button saveBtn = new Button("Register Loan");
        saveBtn.getStyleClass().add("register-button");
        
        // Status label
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        // Event handler (placeholder - would need loan table implementation)
        saveBtn.setOnAction(e -> {
            String employee = employeeCombo.getValue();
            String amountText = amountField.getText().trim();
            java.time.LocalDate loanDate = loanDateField.getValue();
            java.time.LocalDate dueDate = dueDateField.getValue();
            String description = descriptionField.getText().trim();
            
            if (employee == null || amountText.isEmpty() || loanDate == null) {
                statusLabel.setText("Please fill in all required fields.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    statusLabel.setText("Amount must be positive.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }
                
                // This would require implementing loan tracking in the database
                statusLabel.setText("Loan registration functionality needs to be implemented in the database.");
                statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid amount.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });

        box.getChildren().addAll(employeeCombo, amountField, loanDateField, dueDateField, descriptionField, saveBtn, statusLabel);
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
    
    // Helper method to load employee data into the table
    private static void loadEmployeeData(SQLiteDatabase database, ObservableList<EmployeeTableData> employeeData) {
        employeeData.clear();
        for (Object[] row : database.getAllEmployees()) {
            employeeData.add(new EmployeeTableData(
                (Integer) row[0],  // employee_id
                (String) row[1],   // employee_name
                (String) row[2],   // phone_number
                (String) row[3],   // cnic
                (String) row[4],   // address
                (String) row[5],   // designation_title
                (String) row[6],   // salary_type
                (Double) row[7],   // salary_amount
                (String) row[8]    // status
            ));
        }
    }
    
    // Helper method to load contract employee data (non-monthly employees)
    private static void loadContractEmployeeData(SQLiteDatabase database, ObservableList<EmployeeTableData> contractEmployeeData) {
        contractEmployeeData.clear();
        for (Object[] row : database.getAllEmployees()) {
            String salaryType = (String) row[6];
            // Only include non-monthly employees (contract-based)
            if (!"monthly".equals(salaryType)) {
                contractEmployeeData.add(new EmployeeTableData(
                    (Integer) row[0],  // employee_id
                    (String) row[1],   // employee_name
                    (String) row[2],   // phone_number
                    (String) row[3],   // cnic
                    (String) row[4],   // address
                    (String) row[5],   // designation_title
                    (String) row[6],   // salary_type
                    (Double) row[7],   // salary_amount
                    (String) row[8]    // status
                ));
            }
        }
    }
    
    // Helper method to clear form fields
    private static void clearForm(TextField nameField, TextField phoneField, TextField cnicField, 
                                 TextField addressField, ComboBox<String> designationCombo, 
                                 ComboBox<String> salaryTypeCombo, TextField salaryAmountField) {
        nameField.clear();
        phoneField.clear();
        cnicField.clear();
        addressField.clear();
        designationCombo.setValue(null);
        salaryTypeCombo.setValue(null);
        salaryAmountField.clear();
    }
    
    // Helper method to setup event handlers for employee management table and form
    private static void setupEventHandlers(
        TableView<EmployeeTableData> table,
        SQLiteDatabase database,
        TextField nameField,
        TextField phoneField,
        TextField cnicField,
        TextField addressField,
        ComboBox<String> designationCombo,
        ComboBox<String> salaryTypeCombo,
        TextField salaryAmountField,
        Button updateBtn,
        Button deleteBtn,
        Label statusLabel,
        ObservableList<EmployeeTableData> employeeData
    ) {
        // Table selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                phoneField.setText(newSelection.getPhone());
                cnicField.setText(newSelection.getCnic());
                addressField.setText(newSelection.getAddress());
                designationCombo.setValue(newSelection.getDesignation());
                salaryTypeCombo.setValue(newSelection.getSalaryType());
                salaryAmountField.setText(String.valueOf(newSelection.getSalaryAmount()));
                updateBtn.setDisable(false);
                deleteBtn.setDisable(false);
            } else {
                updateBtn.setDisable(true);
                deleteBtn.setDisable(true);
            }
        });

        // Update button handler
        updateBtn.setOnAction(e -> {
            EmployeeTableData selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLabel.setText("Please select an employee to update.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String cnic = cnicField.getText().trim();
            String address = addressField.getText().trim();
            String designation = designationCombo.getValue();
            String salaryType = salaryTypeCombo.getValue();
            String salaryAmountText = salaryAmountField.getText().trim();

            if (name.isEmpty() || designation == null || salaryType == null || salaryAmountText.isEmpty()) {
                statusLabel.setText("Please fill in all required fields.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }

            try {
                double salaryAmount = Double.parseDouble(salaryAmountText);
                if (salaryAmount <= 0) {
                    statusLabel.setText("Salary amount must be positive.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }
                boolean success = database.updateEmployee(
                    selected.getId(), name, phone, cnic, address, designation, salaryType, salaryAmount
                );
                if (success) {
                    statusLabel.setText("Employee updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    clearForm(nameField, phoneField, cnicField, addressField, designationCombo, salaryTypeCombo, salaryAmountField);
                    table.getSelectionModel().clearSelection();
                    updateBtn.setDisable(true);
                    deleteBtn.setDisable(true);
                    loadEmployeeData(database, employeeData);
                } else {
                    statusLabel.setText("Failed to update employee.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid salary amount.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });

        // Delete button handler
        deleteBtn.setOnAction(e -> {
            EmployeeTableData selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLabel.setText("Please select an employee to delete.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Employee");
            confirmAlert.setContentText("Are you sure you want to delete employee '" + selected.getName() + "'?");
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                boolean success = database.deleteEmployee(selected.getId());
                if (success) {
                    statusLabel.setText("Employee deleted successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    clearForm(nameField, phoneField, cnicField, addressField, designationCombo, salaryTypeCombo, salaryAmountField);
                    table.getSelectionModel().clearSelection();
                    updateBtn.setDisable(true);
                    deleteBtn.setDisable(true);
                    loadEmployeeData(database, employeeData);
                } else {
                    statusLabel.setText("Failed to delete employee.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });

        // Clear button handler
        // Find the clear button in the buttonBox if needed, or add a handler here if passed
        // For this implementation, clearBtn is not passed, so you may need to add it if you want to handle it here
    }

    // Inner class for employee table data with JavaFX properties
    public static class EmployeeTableData {
        private final javafx.beans.property.SimpleIntegerProperty id;
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty phone;
        private final javafx.beans.property.SimpleStringProperty cnic;
        private final javafx.beans.property.SimpleStringProperty address;
        private final javafx.beans.property.SimpleStringProperty designation;
        private final javafx.beans.property.SimpleStringProperty salaryType;
        private final javafx.beans.property.SimpleDoubleProperty salaryAmount;
        private final javafx.beans.property.SimpleStringProperty status;
        
        public EmployeeTableData(int id, String name, String phone, String cnic, String address,
                               String designation, String salaryType, double salaryAmount, String status) {
            this.id = new javafx.beans.property.SimpleIntegerProperty(id);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.phone = new javafx.beans.property.SimpleStringProperty(phone);
            this.cnic = new javafx.beans.property.SimpleStringProperty(cnic);
            this.address = new javafx.beans.property.SimpleStringProperty(address);
            this.designation = new javafx.beans.property.SimpleStringProperty(designation);
            this.salaryType = new javafx.beans.property.SimpleStringProperty(salaryType);
            this.salaryAmount = new javafx.beans.property.SimpleDoubleProperty(salaryAmount);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }
        
        // ID property
        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }
        public javafx.beans.property.SimpleIntegerProperty idProperty() { return id; }
        
        // Name property
        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }
        public javafx.beans.property.SimpleStringProperty nameProperty() { return name; }
        
        // Phone property
        public String getPhone() { return phone.get(); }
        public void setPhone(String phone) { this.phone.set(phone); }
        public javafx.beans.property.SimpleStringProperty phoneProperty() { return phone; }
        
        // CNIC property
        public String getCnic() { return cnic.get(); }
        public void setCnic(String cnic) { this.cnic.set(cnic); }
        public javafx.beans.property.SimpleStringProperty cnicProperty() { return cnic; }
        
        // Address property
        public String getAddress() { return address.get(); }
        public void setAddress(String address) { this.address.set(address); }
        public javafx.beans.property.SimpleStringProperty addressProperty() { return address; }
        
        // Designation property
        public String getDesignation() { return designation.get(); }
        public void setDesignation(String designation) { this.designation.set(designation); }
        public javafx.beans.property.SimpleStringProperty designationProperty() { return designation; }
        
        // Salary Type property
        public String getSalaryType() { return salaryType.get(); }
        public void setSalaryType(String salaryType) { this.salaryType.set(salaryType); }
        public javafx.beans.property.SimpleStringProperty salaryTypeProperty() { return salaryType; }
        
        // Salary Amount property
        public double getSalaryAmount() { return salaryAmount.get(); }
        public void setSalaryAmount(double salaryAmount) { this.salaryAmount.set(salaryAmount); }
        public javafx.beans.property.SimpleDoubleProperty salaryAmountProperty() { return salaryAmount; }
        
        // Status property
        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
        public javafx.beans.property.SimpleStringProperty statusProperty() { return status; }
    }

    // Helper method to load attendance data into the table
    private static void loadAttendanceData(SQLiteDatabase database, ObservableList<AttendanceTableData> attendanceData, String startDate, String endDate) {
        attendanceData.clear();
        List<Object[]> attendanceList;
        
        if (startDate != null && endDate != null) {
            attendanceList = database.getEmployeeAttendanceByDateRange(startDate, endDate);
        } else {
            attendanceList = database.getAllEmployeeAttendance();
        }
        
        for (Object[] row : attendanceList) {
            if (startDate != null && endDate != null) {
                // For date range query: employee_id, employee_name, attendance_date, status, working_hours
                attendanceData.add(new AttendanceTableData(
                    (String) row[1],   // employee_name
                    (String) row[2],   // attendance_date
                    (String) row[3],   // status
                    (Double) row[4]    // working_hours
                ));
            } else {
                // For all attendance query: employee_name, attendance_date, status, working_hours
                attendanceData.add(new AttendanceTableData(
                    (String) row[0],   // employee_name
                    (String) row[1],   // attendance_date
                    (String) row[2],   // status
                    (Double) row[3]    // working_hours
                ));
            }
        }
    }

    // Inner class for attendance table data with JavaFX properties
    public static class AttendanceTableData {
        private final javafx.beans.property.SimpleStringProperty employeeName;
        private final javafx.beans.property.SimpleStringProperty date;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleDoubleProperty workingHours;
        
        public AttendanceTableData(String employeeName, String date, String status, double workingHours) {
            this.employeeName = new javafx.beans.property.SimpleStringProperty(employeeName);
            this.date = new javafx.beans.property.SimpleStringProperty(date);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.workingHours = new javafx.beans.property.SimpleDoubleProperty(workingHours);
        }
        
        // Employee Name property
        public String getEmployeeName() { return employeeName.get(); }
        public void setEmployeeName(String employeeName) { this.employeeName.set(employeeName); }
        public javafx.beans.property.SimpleStringProperty employeeNameProperty() { return employeeName; }
        
        // Date property
        public String getDate() { return date.get(); }
        public void setDate(String date) { this.date.set(date); }
        public javafx.beans.property.SimpleStringProperty dateProperty() { return date; }
        
        // Status property
        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
        public javafx.beans.property.SimpleStringProperty statusProperty() { return status; }
        
        // Working Hours property
        public double getWorkingHours() { return workingHours.get(); }
        public void setWorkingHours(double workingHours) { this.workingHours.set(workingHours); }
        public javafx.beans.property.SimpleDoubleProperty workingHoursProperty() { return workingHours; }
    }
}
