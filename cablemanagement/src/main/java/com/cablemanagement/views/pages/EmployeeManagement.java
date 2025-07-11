package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EmployeeManagement {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createEmployeeList());

        HBox buttonBar = createButtonBar(formArea);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        configureScrollPane(scrollPane);

        mainLayout.setTop(scrollPane);
        mainLayout.setCenter(formArea);

        return mainLayout;
    }

    private static HBox createButtonBar(StackPane formArea) {
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        String[] buttonLabels = {
            "Employee List",
            "Register New Employee",
            "Mark Attendance",
            "Salary Payments",
            "Advance Salary",
            "Employee Loans",
            "Attendance Report",
            "Salary Report",
            "Loan Report"
        };

        Runnable[] actions = {
            () -> formArea.getChildren().setAll(createEmployeeList()),
            () -> formArea.getChildren().setAll(createRegisterEmployeeForm()),
            () -> formArea.getChildren().setAll(createMarkAttendanceForm()),
            () -> formArea.getChildren().setAll(createSalaryPaymentForm()),
            () -> formArea.getChildren().setAll(createAdvanceSalaryForm()),
            () -> formArea.getChildren().setAll(createEmployeeLoanForm()),
            () -> formArea.getChildren().setAll(createAttendanceReport()),
            () -> formArea.getChildren().setAll(createSalaryReport()),
            () -> formArea.getChildren().setAll(createLoanReport())
        };

        for (int i = 0; i < buttonLabels.length; i++) {
            addButton(buttonBar, buttonLabels[i], actions[i]);
        }

        return buttonBar;
    }

    private static void configureScrollPane(ScrollPane scrollPane) {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(72);
        scrollPane.setMinHeight(72);
        scrollPane.setMaxHeight(72);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    }

    private static void addButton(HBox bar, String label, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("register-button");
        btn.setOnAction(e -> action.run());
        bar.getChildren().add(btn);
    }

    private static VBox createEmployeeList() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Employee List");

        // Action buttons
        HBox buttons = new HBox(10);
        Button refreshBtn = createActionButton("Refresh");
        Button printBtn = createActionButton("Print");
        buttons.getChildren().addAll(refreshBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Employees table
        TableView<Employee> table = new TableView<>();
        
        TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        
        TableColumn<Employee, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        TableColumn<Employee, String> designationCol = new TableColumn<>("Designation");
        designationCol.setCellValueFactory(new PropertyValueFactory<>("designation"));
        
        TableColumn<Employee, String> salaryTypeCol = new TableColumn<>("Salary Type");
        salaryTypeCol.setCellValueFactory(new PropertyValueFactory<>("salaryType"));
        
        TableColumn<Employee, String> salaryCol = new TableColumn<>("Salary");
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salaryAmount"));
        
        TableColumn<Employee, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        table.getColumns().addAll(nameCol, phoneCol, designationCol, salaryTypeCol, salaryCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data - in real app, fetch from database
        ObservableList<Employee> data = FXCollections.observableArrayList(
            new Employee(1, "Zahid Khan", "03111222333", "Manager", "monthly", "35000.00", "Active"),
            new Employee(2, "Faisal Mehmood", "03211234567", "Technician", "daily", "1200.00", "Active"),
            new Employee(3, "Rashid Ali", "03331234567", "Sales Representative", "hourly", "250.00", "Active")
        );
        table.setItems(data);

        // Add context menu for actions
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem attendanceItem = new MenuItem("Mark Attendance");
        MenuItem salaryItem = new MenuItem("Pay Salary");
        MenuItem advanceItem = new MenuItem("Give Advance");
        MenuItem loanItem = new MenuItem("Give Loan");
        
        contextMenu.getItems().addAll(editItem, attendanceItem, salaryItem, advanceItem, loanItem);
        table.setContextMenu(contextMenu);

        form.getChildren().addAll(heading, buttons, table);

        refreshBtn.setOnAction(e -> {
            // In real app: Refresh from database
            System.out.println("Refreshing employee list...");
        });

        printBtn.setOnAction(e -> {
            System.out.println("Printing employee list...");
        });

        return form;
    }

    private static VBox createRegisterEmployeeForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Register New Employee");

        TextField nameField = createTextField("Employee Name");
        TextField phoneField = createTextField("Phone Number");
        TextField cnicField = createTextField("CNIC");
        TextField addressField = createTextField("Address");
        
        DatePicker hireDatePicker = new DatePicker();
        hireDatePicker.setValue(LocalDate.now());
        
        ComboBox<String> designationCombo = new ComboBox<>();
        designationCombo.setPromptText("Select Designation");
        // In real app: Populate from Designation table
        designationCombo.getItems().addAll("Manager", "Technician", "Sales Representative");
        
        ComboBox<String> salaryTypeCombo = new ComboBox<>();
        salaryTypeCombo.setPromptText("Salary Type");
        salaryTypeCombo.getItems().addAll("monthly", "daily", "hourly", "task");
        
        TextField salaryAmountField = createTextField("Salary Amount");
        
        CheckBox isActiveCheck = new CheckBox("Active Employee");
        isActiveCheck.setSelected(true);

        Button submitBtn = createSubmitButton("Register Employee");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        form.getChildren().addAll(
            heading,
            createFormRow("Name:", nameField),
            createFormRow("Phone:", phoneField),
            createFormRow("CNIC:", cnicField),
            createFormRow("Address:", addressField),
            createFormRow("Hire Date:", hireDatePicker),
            createFormRow("Designation:", designationCombo),
            createFormRow("Salary Type:", salaryTypeCombo),
            createFormRow("Salary Amount:", salaryAmountField),
            createFormRow("Status:", isActiveCheck),
            submitBtn
        );

        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String cnic = cnicField.getText().trim();
            String address = addressField.getText().trim();
            String hireDate = hireDatePicker.getValue().format(DATE_FORMATTER);
            String designation = designationCombo.getValue();
            String salaryType = salaryTypeCombo.getValue();
            String salaryAmount = salaryAmountField.getText().trim();
            boolean isActive = isActiveCheck.isSelected();

            if (name.isEmpty() || designation == null || salaryType == null || salaryAmount.isEmpty()) {
                showAlert("Error", "Name, Designation, Salary Type and Amount are required");
                return;
            }

            try {
                double salary = Double.parseDouble(salaryAmount);
                // In real app: Save to Employee table
                System.out.println("Registering new employee:");
                System.out.println("Name: " + name);
                System.out.println("Phone: " + phone);
                System.out.println("CNIC: " + cnic);
                System.out.println("Address: " + address);
                System.out.println("Hire Date: " + hireDate);
                System.out.println("Designation: " + designation);
                System.out.println("Salary Type: " + salaryType);
                System.out.println("Salary Amount: " + salary);
                System.out.println("Active: " + isActive);

                // Clear form
                nameField.clear();
                phoneField.clear();
                cnicField.clear();
                addressField.clear();
                salaryAmountField.clear();

                showAlert("Success", "Employee registered successfully");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid salary amount");
            }
        });

        return form;
    }

    private static VBox createMarkAttendanceForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Mark Employee Attendance");

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        // In real app: Populate from Employee table
        employeeCombo.getItems().addAll(
            "Zahid Khan (Manager)", 
            "Faisal Mehmood (Technician)", 
            "Rashid Ali (Sales Representative)"
        );

        DatePicker attendanceDatePicker = new DatePicker();
        attendanceDatePicker.setValue(LocalDate.now());

        ToggleGroup statusGroup = new ToggleGroup();
        RadioButton presentRadio = new RadioButton("Present");
        presentRadio.setToggleGroup(statusGroup);
        presentRadio.setSelected(true);
        RadioButton absentRadio = new RadioButton("Absent");
        absentRadio.setToggleGroup(statusGroup);
        RadioButton leaveRadio = new RadioButton("Leave");
        leaveRadio.setToggleGroup(statusGroup);
        
        HBox statusBox = new HBox(10, presentRadio, absentRadio, leaveRadio);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        TextField hoursField = createTextField("8", "Working Hours");
        hoursField.setDisable(true);

        presentRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            hoursField.setDisable(!newVal);
            if (newVal) hoursField.setText("8");
            else hoursField.setText("0");
        });

        Button submitBtn = createSubmitButton("Record Attendance");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        form.getChildren().addAll(
            heading,
            createFormRow("Employee:", employeeCombo),
            createFormRow("Date:", attendanceDatePicker),
            createFormRow("Status:", statusBox),
            createFormRow("Working Hours:", hoursField),
            submitBtn
        );

        submitBtn.setOnAction(e -> {
            String employee = employeeCombo.getValue();
            String date = attendanceDatePicker.getValue().format(DATE_FORMATTER);
            String status = ((RadioButton)statusGroup.getSelectedToggle()).getText();
            String hours = hoursField.getText().trim();

            if (employee == null) {
                showAlert("Error", "Employee is required");
                return;
            }

            try {
                double workingHours = Double.parseDouble(hours);
                // In real app: Save to Employee_Attendance table
                System.out.println("Recording attendance:");
                System.out.println("Employee: " + employee);
                System.out.println("Date: " + date);
                System.out.println("Status: " + status);
                System.out.println("Hours: " + workingHours);

                showAlert("Success", "Attendance recorded successfully");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid working hours");
            }
        });

        return form;
    }

    private static VBox createSalaryPaymentForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Employee Salary Payment");

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        employeeCombo.getItems().addAll(
            "Zahid Khan (Manager)", 
            "Faisal Mehmood (Technician)", 
            "Rashid Ali (Sales Representative)"
        );

        DatePicker paymentDatePicker = new DatePicker();
        paymentDatePicker.setValue(LocalDate.now());

        TextField amountField = createTextField("Amount");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        Button submitBtn = createSubmitButton("Record Payment");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        form.getChildren().addAll(
            heading,
            createFormRow("Employee:", employeeCombo),
            createFormRow("Payment Date:", paymentDatePicker),
            createFormRow("Amount:", amountField),
            createFormRow("Description:", descriptionArea),
            submitBtn
        );

        submitBtn.setOnAction(e -> {
            String employee = employeeCombo.getValue();
            String date = paymentDatePicker.getValue().format(DATE_FORMATTER);
            String amount = amountField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (employee == null || amount.isEmpty()) {
                showAlert("Error", "Employee and Amount are required");
                return;
            }

            try {
                double amt = Double.parseDouble(amount);
                // In real app: Save to Employee_Salary_Payment table
                System.out.println("Recording salary payment:");
                System.out.println("Employee: " + employee);
                System.out.println("Date: " + date);
                System.out.println("Amount: " + amt);
                System.out.println("Description: " + description);

                // Clear form
                amountField.clear();
                descriptionArea.clear();

                showAlert("Success", "Salary payment recorded successfully");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid amount");
            }
        });

        return form;
    }

    private static VBox createAdvanceSalaryForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Advance Salary Payment");

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        employeeCombo.getItems().addAll(
            "Zahid Khan (Manager)", 
            "Faisal Mehmood (Technician)", 
            "Rashid Ali (Sales Representative)"
        );

        DatePicker advanceDatePicker = new DatePicker();
        advanceDatePicker.setValue(LocalDate.now());

        TextField amountField = createTextField("Amount");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Reason for advance");
        descriptionArea.setPrefRowCount(3);

        Button submitBtn = createSubmitButton("Record Advance");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        form.getChildren().addAll(
            heading,
            createFormRow("Employee:", employeeCombo),
            createFormRow("Advance Date:", advanceDatePicker),
            createFormRow("Amount:", amountField),
            createFormRow("Description:", descriptionArea),
            submitBtn
        );

        submitBtn.setOnAction(e -> {
            String employee = employeeCombo.getValue();
            String date = advanceDatePicker.getValue().format(DATE_FORMATTER);
            String amount = amountField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (employee == null || amount.isEmpty()) {
                showAlert("Error", "Employee and Amount are required");
                return;
            }

            try {
                double amt = Double.parseDouble(amount);
                // In real app: Save to Advance_Salary table
                System.out.println("Recording advance salary:");
                System.out.println("Employee: " + employee);
                System.out.println("Date: " + date);
                System.out.println("Amount: " + amt);
                System.out.println("Description: " + description);

                // Clear form
                amountField.clear();
                descriptionArea.clear();

                showAlert("Success", "Advance salary recorded successfully");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid amount");
            }
        });

        return form;
    }

    private static VBox createEmployeeLoanForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Employee Loan");

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        employeeCombo.getItems().addAll(
            "Zahid Khan (Manager)", 
            "Faisal Mehmood (Technician)", 
            "Rashid Ali (Sales Representative)"
        );

        DatePicker loanDatePicker = new DatePicker();
        loanDatePicker.setValue(LocalDate.now());

        TextField amountField = createTextField("Loan Amount");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Loan purpose");
        descriptionArea.setPrefRowCount(3);

        Button submitBtn = createSubmitButton("Record Loan");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        form.getChildren().addAll(
            heading,
            createFormRow("Employee:", employeeCombo),
            createFormRow("Loan Date:", loanDatePicker),
            createFormRow("Amount:", amountField),
            createFormRow("Description:", descriptionArea),
            submitBtn
        );

        submitBtn.setOnAction(e -> {
            String employee = employeeCombo.getValue();
            String date = loanDatePicker.getValue().format(DATE_FORMATTER);
            String amount = amountField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (employee == null || amount.isEmpty()) {
                showAlert("Error", "Employee and Amount are required");
                return;
            }

            try {
                double amt = Double.parseDouble(amount);
                // In real app: Save to Employee_Loan table
                System.out.println("Recording employee loan:");
                System.out.println("Employee: " + employee);
                System.out.println("Date: " + date);
                System.out.println("Amount: " + amt);
                System.out.println("Description: " + description);

                // Clear form
                amountField.clear();
                descriptionArea.clear();

                showAlert("Success", "Employee loan recorded successfully");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid amount");
            }
        });

        return form;
    }

    private static VBox createAttendanceReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Attendance Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker();
        fromDatePicker.setValue(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker();
        toDatePicker.setValue(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        HBox buttons = new HBox(10);
        Button refreshBtn = createActionButton("Refresh");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(refreshBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Attendance table
        TableView<Attendance> table = new TableView<>();
        
        TableColumn<Attendance, String> nameCol = new TableColumn<>("Employee");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        
        TableColumn<Attendance, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("attendanceDate"));
        
        TableColumn<Attendance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<Attendance, String> hoursCol = new TableColumn<>("Hours");
        hoursCol.setCellValueFactory(new PropertyValueFactory<>("workingHours"));
        
        table.getColumns().addAll(nameCol, dateCol, statusCol, hoursCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data - in real app, fetch from Employee_Attendance table
        ObservableList<Attendance> data = FXCollections.observableArrayList(
            new Attendance("Zahid Khan", "2025-07-01", "present", "8"),
            new Attendance("Zahid Khan", "2025-07-02", "present", "8"),
            new Attendance("Faisal Mehmood", "2025-07-01", "absent", "0"),
            new Attendance("Faisal Mehmood", "2025-07-02", "present", "9"),
            new Attendance("Rashid Ali", "2025-07-01", "present", "7")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, table);

        filterBtn.setOnAction(e -> {
            String fromDate = fromDatePicker.getValue().format(DATE_FORMATTER);
            String toDate = toDatePicker.getValue().format(DATE_FORMATTER);
            System.out.println("Filtering attendance from " + fromDate + " to " + toDate);
        });

        refreshBtn.setOnAction(e -> {
            System.out.println("Refreshing attendance report...");
        });

        printBtn.setOnAction(e -> {
            System.out.println("Printing attendance report...");
        });

        exportBtn.setOnAction(e -> {
            System.out.println("Exporting attendance report...");
        });

        return form;
    }

    private static VBox createSalaryReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Salary Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker();
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker();
        toDatePicker.setValue(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        HBox buttons = new HBox(10);
        Button refreshBtn = createActionButton("Refresh");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(refreshBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Salary payments table
        TableView<SalaryPayment> table = new TableView<>();
        
        TableColumn<SalaryPayment, String> nameCol = new TableColumn<>("Employee");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        
        TableColumn<SalaryPayment, String> dateCol = new TableColumn<>("Payment Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        
        TableColumn<SalaryPayment, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        TableColumn<SalaryPayment, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        table.getColumns().addAll(nameCol, dateCol, amountCol, descriptionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data - in real app, fetch from Employee_Salary_Payment table
        ObservableList<SalaryPayment> data = FXCollections.observableArrayList(
            new SalaryPayment("Zahid Khan", "2025-07-01", "35000.00", "Monthly salary"),
            new SalaryPayment("Faisal Mehmood", "2025-07-02", "1200.00", "Daily wage"),
            new SalaryPayment("Rashid Ali", "2025-07-02", "1750.00", "Hourly wage - 7 hrs")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, table);

        filterBtn.setOnAction(e -> {
            String fromDate = fromDatePicker.getValue().format(DATE_FORMATTER);
            String toDate = toDatePicker.getValue().format(DATE_FORMATTER);
            System.out.println("Filtering salary payments from " + fromDate + " to " + toDate);
        });

        refreshBtn.setOnAction(e -> {
            System.out.println("Refreshing salary report...");
        });

        printBtn.setOnAction(e -> {
            System.out.println("Printing salary report...");
        });

        exportBtn.setOnAction(e -> {
            System.out.println("Exporting salary report...");
        });

        return form;
    }

    private static VBox createLoanReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Employee Loan Report");

        // Filters
        HBox filterBox = new HBox(10);
        CheckBox showActiveOnlyCheck = new CheckBox("Show Active Loans Only");
        showActiveOnlyCheck.setSelected(true);
        Button filterBtn = createActionButton("Filter");
        filterBox.getChildren().addAll(showActiveOnlyCheck, filterBtn);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        HBox buttons = new HBox(10);
        Button refreshBtn = createActionButton("Refresh");
        Button printBtn = createActionButton("Print");
        Button exportBtn = createActionButton("Export");
        buttons.getChildren().addAll(refreshBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Loans table
        TableView<EmployeeLoan> table = new TableView<>();
        
        TableColumn<EmployeeLoan, String> nameCol = new TableColumn<>("Employee");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        
        TableColumn<EmployeeLoan, String> dateCol = new TableColumn<>("Loan Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        
        TableColumn<EmployeeLoan, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        TableColumn<EmployeeLoan, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<EmployeeLoan, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        table.getColumns().addAll(nameCol, dateCol, amountCol, statusCol, descriptionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Sample data - in real app, fetch from Employee_Loan table
        ObservableList<EmployeeLoan> data = FXCollections.observableArrayList(
            new EmployeeLoan("Zahid Khan", "2025-03-10", "15000.00", "Pending", "Personal Loan"),
            new EmployeeLoan("Rashid Ali", "2025-04-01", "10000.00", "Paid", "Medical expense loan")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, filterBox, buttons, table);

        filterBtn.setOnAction(e -> {
            boolean activeOnly = showActiveOnlyCheck.isSelected();
            System.out.println("Filtering loans - Active Only: " + activeOnly);
        });

        refreshBtn.setOnAction(e -> {
            System.out.println("Refreshing loan report...");
        });

        printBtn.setOnAction(e -> {
            System.out.println("Printing loan report...");
        });

        exportBtn.setOnAction(e -> {
            System.out.println("Exporting loan report...");
        });

        return form;
    }

    // Model classes
    public static class Employee {
        private final int employeeId;
        private final String employeeName;
        private final String phoneNumber;
        private final String designation;
        private final String salaryType;
        private final String salaryAmount;
        private final String status;

        public Employee(int employeeId, String employeeName, String phoneNumber, 
                       String designation, String salaryType, String salaryAmount, String status) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.phoneNumber = phoneNumber;
            this.designation = designation;
            this.salaryType = salaryType;
            this.salaryAmount = salaryAmount;
            this.status = status;
        }

        public int getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getDesignation() { return designation; }
        public String getSalaryType() { return salaryType; }
        public String getSalaryAmount() { return salaryAmount; }
        public String getStatus() { return status; }
    }

    public static class Attendance {
        private final String employeeName;
        private final String attendanceDate;
        private final String status;
        private final String workingHours;

        public Attendance(String employeeName, String attendanceDate, String status, String workingHours) {
            this.employeeName = employeeName;
            this.attendanceDate = attendanceDate;
            this.status = status;
            this.workingHours = workingHours;
        }

        public String getEmployeeName() { return employeeName; }
        public String getAttendanceDate() { return attendanceDate; }
        public String getStatus() { return status; }
        public String getWorkingHours() { return workingHours; }
    }

    public static class SalaryPayment {
        private final String employeeName;
        private final String paymentDate;
        private final String amount;
        private final String description;

        public SalaryPayment(String employeeName, String paymentDate, String amount, String description) {
            this.employeeName = employeeName;
            this.paymentDate = paymentDate;
            this.amount = amount;
            this.description = description;
        }

        public String getEmployeeName() { return employeeName; }
        public String getPaymentDate() { return paymentDate; }
        public String getAmount() { return amount; }
        public String getDescription() { return description; }
    }

    public static class EmployeeLoan {
        private final String employeeName;
        private final String loanDate;
        private final String amount;
        private final String status;
        private final String description;

        public EmployeeLoan(String employeeName, String loanDate, String amount, String status, String description) {
            this.employeeName = employeeName;
            this.loanDate = loanDate;
            this.amount = amount;
            this.status = status;
            this.description = description;
        }

        public String getEmployeeName() { return employeeName; }
        public String getLoanDate() { return loanDate; }
        public String getAmount() { return amount; }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
    }

    // Helper methods
    private static Label createHeading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-heading");
        label.setFont(Font.font(18));
        return label;
    }

    private static TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("form-input");
        return field;
    }

    private static TextField createTextField(String text, String prompt) {
        TextField field = new TextField(text);
        field.setPromptText(prompt);
        field.getStyleClass().add("form-input");
        return field;
    }

    private static HBox createFormRow(String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        HBox row = new HBox(10, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");
        return row;
    }

    private static Button createSubmitButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("form-submit");
        return button;
    }

    private static Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        return button;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}