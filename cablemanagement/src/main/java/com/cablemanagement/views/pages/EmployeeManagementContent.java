package com.cablemanagement.views.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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

        addButton(buttonBar, "Register Designation", () -> formArea.getChildren().setAll(createDesignationForm()));
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
        VBox box = baseForm("Register Designation");

        TextField name = new TextField();
        name.setPromptText("Designation Name");
        Button submit = new Button("Submit");
        box.getChildren().addAll(name, submit);

        return box;
    }

    private static VBox createRegisterEmployeeForm() {
        VBox box = baseForm("Register New Employee");

        TextField name = new TextField();
        name.setPromptText("Full Name");

        ComboBox<String> designation = new ComboBox<>();
        designation.setPromptText("Select Designation");

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
