package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class ProductionStock {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createProductionInvoiceForm());

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
            "Create Production Invoice",
            "Create Return Production Invoice",
            "Create Sales Invoice", 
            "Create Return Sales Invoice",
            "View Production Stock Usage Report"
        };

        Runnable[] actions = {
            () -> formArea.getChildren().setAll(createProductionInvoiceForm()),
            () -> formArea.getChildren().setAll(createReturnProductionInvoiceForm()),
            () -> formArea.getChildren().setAll(createSalesInvoiceForm()),
            () -> formArea.getChildren().setAll(createReturnSalesInvoiceForm()),
            () -> formArea.getChildren().setAll(createProductionStockUsageReportForm())
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

    private static VBox createProductionInvoiceForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        // Header Section
        Label heading = createHeading("Create Production Invoice");
        
        // Production Info Section
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker productionDatePicker = new DatePicker();
        productionDatePicker.setValue(LocalDate.now());
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(3);
        notesArea.setPrefWidth(400);
        
        infoGrid.add(createFormRow("Production Date:", productionDatePicker), 0, 0);
        infoGrid.add(createFormRow("Notes:", notesArea), 0, 1);

        // Items and Materials Sections
        HBox itemsMaterialsSection = new HBox(20);
        itemsMaterialsSection.setAlignment(Pos.TOP_LEFT);

        // Production Items Section
        VBox itemsSection = new VBox(10);
        itemsSection.setMinWidth(400);
        
        TextField productField = createTextField("Product");
        TextField quantityField = createTextField("Quantity");
        
        HBox itemButtonBox = new HBox(10);
        Button addItemBtn = createActionButton("Add Item");
        Button clearItemsBtn = createActionButton("Clear All");
        
        itemButtonBox.getChildren().addAll(addItemBtn, clearItemsBtn);
        
        ListView<String> itemsList = createEnhancedListView();
        
        itemsSection.getChildren().addAll(
            createSubheading("Production Items:"),
            createFormRow("Product:", productField),
            createFormRow("Quantity:", quantityField),
            itemButtonBox,
            itemsList
        );

        // Raw Materials Section
        VBox materialsSection = new VBox(10);
        materialsSection.setMinWidth(400);
        
        TextField rawMaterialField = createTextField("Raw Material");
        TextField rawQuantityField = createTextField("Quantity Used");
        
        HBox materialButtonBox = new HBox(10);
        Button addMaterialBtn = createActionButton("Add Material");
        Button clearMaterialsBtn = createActionButton("Clear All");
        
        materialButtonBox.getChildren().addAll(addMaterialBtn, clearMaterialsBtn);
        
        ListView<String> materialsList = createEnhancedListView();
        
        materialsSection.getChildren().addAll(
            createSubheading("Raw Materials Used:"),
            createFormRow("Raw Material:", rawMaterialField),
            createFormRow("Quantity Used:", rawQuantityField),
            materialButtonBox,
            materialsList
        );

        // Add sections to layout
        itemsMaterialsSection.getChildren().addAll(itemsSection, materialsSection);
        
        // Submit Button
        Button submitBtn = createSubmitButton("Submit Production Invoice");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        // Add all components to main form
        form.getChildren().addAll(
            heading,
            infoGrid,
            itemsMaterialsSection,
            submitBtn
        );

        // Event Handlers
        addItemBtn.setOnAction(e -> handleAddItem(productField, quantityField, itemsList));
        productField.setOnAction(e -> handleAddItem(productField, quantityField, itemsList));
        quantityField.setOnAction(e -> handleAddItem(productField, quantityField, itemsList));
        
        clearItemsBtn.setOnAction(e -> {
            if (!itemsList.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all production items?");
                alert.setContentText("This will remove all items from the list.");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    itemsList.getItems().clear();
                }
            }
        });
        
        addMaterialBtn.setOnAction(e -> handleAddMaterial(rawMaterialField, rawQuantityField, materialsList));
        rawMaterialField.setOnAction(e -> handleAddMaterial(rawMaterialField, rawQuantityField, materialsList));
        rawQuantityField.setOnAction(e -> handleAddMaterial(rawMaterialField, rawQuantityField, materialsList));
        
        clearMaterialsBtn.setOnAction(e -> {
            if (!materialsList.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all raw materials?");
                alert.setContentText("This will remove all materials from the list.");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    materialsList.getItems().clear();
                }
            }
        });
        
        submitBtn.setOnAction(e -> handleSubmitProduction(
            productionDatePicker, 
            notesArea, 
            itemsList, 
            materialsList
        ));

        return form;
    }

    private static VBox createReturnProductionInvoiceForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Return Production Invoice");

        // Main form fields
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(LocalDate.now());
        TextField referenceField = createTextField("Reference");
        TextField quantityField = createTextField("Quantity");
        
        // Items list with delete functionality
        ListView<String> returnItemsList = createEnhancedListView();
        
        // Add item controls
        HBox addItemBox = new HBox(10);
        TextField itemField = createTextField("Item");
        Button addItemBtn = createActionButton("Add Item");
        addItemBox.getChildren().addAll(itemField, addItemBtn);
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        Button submitBtn = createSubmitButton("Submit Return");
        Button clearBtn = createActionButton("Clear All");
        actionButtons.getChildren().addAll(submitBtn, clearBtn);

        form.getChildren().addAll(
            heading,
            createFormRow("Return Date:", returnDatePicker),
            createFormRow("Reference:", referenceField),
            createFormRow("Quantity:", quantityField),
            createSubheading("Return Items:"),
            addItemBox,
            returnItemsList,
            actionButtons
        );

        // Event handlers
        addItemBtn.setOnAction(e -> {
            if (!itemField.getText().trim().isEmpty()) {
                returnItemsList.getItems().add(itemField.getText().trim());
                itemField.clear();
            }
        });
        
        clearBtn.setOnAction(e -> {
            if (!returnItemsList.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all return items?");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    returnItemsList.getItems().clear();
                }
            }
        });
        
        submitBtn.setOnAction(e -> {
            String date = returnDatePicker.getValue().format(DATE_FORMATTER);
            String reference = referenceField.getText().trim();
            String quantity = quantityField.getText().trim();
            
            if (reference.isEmpty() || quantity.isEmpty() || returnItemsList.getItems().isEmpty()) {
                showAlert("Missing Information", "Please fill all fields and add at least one item");
                return;
            }
            
            // In real app, save to database
            System.out.println("Return Production Invoice Submitted:");
            System.out.println("Date: " + date);
            System.out.println("Reference: " + reference);
            System.out.println("Quantity: " + quantity);
            System.out.println("Items:");
            returnItemsList.getItems().forEach(System.out::println);
            
            // Clear form
            returnDatePicker.setValue(LocalDate.now());
            referenceField.clear();
            quantityField.clear();
            returnItemsList.getItems().clear();
        });

        return form;
    }

    private static VBox createSalesInvoiceForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Sales Invoice");

        // Invoice header fields
        TextField invoiceNumberField = createTextField("Invoice Number");
        DatePicker salesDatePicker = new DatePicker();
        salesDatePicker.setValue(LocalDate.now());
        TextField customerField = createTextField("Customer");
        TextField discountField = createTextField("0", "Discount");
        TextField paidAmountField = createTextField("0", "Paid Amount");
        
        // Invoice items
        ListView<String> itemsList = createEnhancedListView();
        
        // Add item controls
        HBox addItemBox = new HBox(10);
        TextField productField = createTextField("Product");
        TextField quantityField = createTextField("Quantity");
        TextField priceField = createTextField("Price");
        Button addItemBtn = createActionButton("Add Item");
        addItemBox.getChildren().addAll(productField, quantityField, priceField, addItemBtn);
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        Button submitBtn = createSubmitButton("Submit Invoice");
        Button clearBtn = createActionButton("Clear All");
        actionButtons.getChildren().addAll(submitBtn, clearBtn);

        form.getChildren().addAll(
            heading,
            createFormRow("Invoice Number:", invoiceNumberField),
            createFormRow("Sales Date:", salesDatePicker),
            createFormRow("Customer:", customerField),
            createFormRow("Discount:", discountField),
            createFormRow("Paid Amount:", paidAmountField),
            createSubheading("Invoice Items:"),
            addItemBox,
            itemsList,
            actionButtons
        );

        // Event handlers
        addItemBtn.setOnAction(e -> {
            String product = productField.getText().trim();
            String quantity = quantityField.getText().trim();
            String price = priceField.getText().trim();
            
            if (!product.isEmpty() && !quantity.isEmpty() && !price.isEmpty()) {
                try {
                    double qty = Double.parseDouble(quantity);
                    double prc = Double.parseDouble(price);
                    double total = qty * prc;
                    itemsList.getItems().add(String.format("%s - %.2f x %.2f = %.2f", 
                        product, qty, prc, total));
                    productField.clear();
                    quantityField.clear();
                    priceField.clear();
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Input", "Please enter valid numbers for quantity and price");
                }
            }
        });
        
        clearBtn.setOnAction(e -> {
            if (!itemsList.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all invoice items?");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    itemsList.getItems().clear();
                }
            }
        });
        
        submitBtn.setOnAction(e -> {
            String invoiceNumber = invoiceNumberField.getText().trim();
            String customer = customerField.getText().trim();
            String date = salesDatePicker.getValue().format(DATE_FORMATTER);
            
            if (invoiceNumber.isEmpty() || customer.isEmpty() || itemsList.getItems().isEmpty()) {
                showAlert("Missing Information", "Please fill all fields and add at least one item");
                return;
            }
            
            // In real app, save to database
            System.out.println("Sales Invoice Submitted:");
            System.out.println("Invoice #: " + invoiceNumber);
            System.out.println("Date: " + date);
            System.out.println("Customer: " + customer);
            System.out.println("Items:");
            itemsList.getItems().forEach(System.out::println);
            
            // Clear form
            invoiceNumberField.clear();
            salesDatePicker.setValue(LocalDate.now());
            customerField.clear();
            discountField.setText("0");
            paidAmountField.setText("0");
            itemsList.getItems().clear();
        });

        return form;
    }

    private static VBox createReturnSalesInvoiceForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Return Sales Invoice");

        // Main form fields
        TextField returnInvoiceNumberField = createTextField("Return Invoice Number");
        TextField originalInvoiceField = createTextField("Original Invoice");
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(LocalDate.now());
        TextField customerField = createTextField("Customer");
        TextField returnAmountField = createTextField("Return Amount");
        
        // Return items list
        ListView<String> returnItemsList = createEnhancedListView();
        
        // Add item controls
        HBox addItemBox = new HBox(10);
        TextField productField = createTextField("Product");
        TextField quantityField = createTextField("Quantity");
        Button addItemBtn = createActionButton("Add Item");
        addItemBox.getChildren().addAll(productField, quantityField, addItemBtn);
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        Button submitBtn = createSubmitButton("Submit Return");
        Button clearBtn = createActionButton("Clear All");
        actionButtons.getChildren().addAll(submitBtn, clearBtn);

        form.getChildren().addAll(
            heading,
            createFormRow("Return Invoice Number:", returnInvoiceNumberField),
            createFormRow("Original Invoice:", originalInvoiceField),
            createFormRow("Return Date:", returnDatePicker),
            createFormRow("Customer:", customerField),
            createFormRow("Return Amount:", returnAmountField),
            createSubheading("Return Items:"),
            addItemBox,
            returnItemsList,
            actionButtons
        );

        // Event handlers
        addItemBtn.setOnAction(e -> {
            String product = productField.getText().trim();
            String quantity = quantityField.getText().trim();
            
            if (!product.isEmpty() && !quantity.isEmpty()) {
                returnItemsList.getItems().add(product + " - " + quantity);
                productField.clear();
                quantityField.clear();
            }
        });
        
        clearBtn.setOnAction(e -> {
            if (!returnItemsList.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all return items?");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    returnItemsList.getItems().clear();
                }
            }
        });
        
        submitBtn.setOnAction(e -> {
            String returnInvoice = returnInvoiceNumberField.getText().trim();
            String originalInvoice = originalInvoiceField.getText().trim();
            String customer = customerField.getText().trim();
            String date = returnDatePicker.getValue().format(DATE_FORMATTER);
            String amount = returnAmountField.getText().trim();
            
            if (returnInvoice.isEmpty() || customer.isEmpty() || amount.isEmpty() || 
                returnItemsList.getItems().isEmpty()) {
                showAlert("Missing Information", "Please fill all fields and add at least one item");
                return;
            }
            
            // In real app, save to database
            System.out.println("Return Sales Invoice Submitted:");
            System.out.println("Return #: " + returnInvoice);
            System.out.println("Original: " + originalInvoice);
            System.out.println("Date: " + date);
            System.out.println("Customer: " + customer);
            System.out.println("Amount: " + amount);
            System.out.println("Items:");
            returnItemsList.getItems().forEach(System.out::println);
            
            // Clear form
            returnInvoiceNumberField.clear();
            originalInvoiceField.clear();
            returnDatePicker.setValue(LocalDate.now());
            customerField.clear();
            returnAmountField.clear();
            returnItemsList.getItems().clear();
        });

        return form;
    }

    private static VBox createProductionStockUsageReportForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Production Stock Usage Report");

        // Date range selection
        HBox dateRangeBox = new HBox(20);
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now());
        dateRangeBox.getChildren().addAll(
            createFormRow("Start Date:", startDatePicker),
            createFormRow("End Date:", endDatePicker)
        );

        // Report type selection
        ComboBox<String> reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll(
            "Daily Production Summary",
            "Product-wise Usage",
            "Raw Material Consumption",
            "Sales vs Production"
        );
        reportTypeCombo.getSelectionModel().selectFirst();

        // Generate button
        Button generateBtn = createSubmitButton("Generate Report");

        // Report display area
        ListView<String> reportView = createEnhancedListView();
        reportView.setPrefHeight(400);

        form.getChildren().addAll(
            heading,
            dateRangeBox,
            createFormRow("Report Type:", reportTypeCombo),
            generateBtn,
            createSubheading("Report Results:"),
            reportView
        );

        // Event handler
        generateBtn.setOnAction(e -> {
            String startDate = startDatePicker.getValue().format(DATE_FORMATTER);
            String endDate = endDatePicker.getValue().format(DATE_FORMATTER);
            String reportType = reportTypeCombo.getSelectionModel().getSelectedItem();
            
            // In real app, fetch data from database
            reportView.getItems().clear();
            reportView.getItems().add("Production Stock Usage Report");
            reportView.getItems().add("Period: " + startDate + " to " + endDate);
            reportView.getItems().add("Report Type: " + reportType);
            reportView.getItems().add("");
            
            // Sample report data
            if (reportType.equals("Daily Production Summary")) {
                reportView.getItems().add("Date       | Product          | Quantity");
                reportView.getItems().add("2023-01-01 | Copper Cable 25m | 50");
                reportView.getItems().add("2023-01-01 | PVC Wire 50m     | 30");
                reportView.getItems().add("2023-01-02 | Copper Cable 25m | 45");
            } else if (reportType.equals("Raw Material Consumption")) {
                reportView.getItems().add("Material          | Quantity Used");
                reportView.getItems().add("Copper Wire 8mm   | 1250m");
                reportView.getItems().add("PVC Granules      | 450kg");
                reportView.getItems().add("Aluminum Conductor| 800m");
            }
            // Other report types would have different formats
        });

        return form;
    }

    private static void handleAddItem(TextField productField, TextField quantityField, ListView<String> itemsList) {
        String product = productField.getText().trim();
        String quantity = quantityField.getText().trim();
        
        if (!product.isEmpty() && !quantity.isEmpty()) {
            try {
                double qty = Double.parseDouble(quantity);
                itemsList.getItems().add(String.format("%s - %.2f", product, qty));
                productField.clear();
                quantityField.clear();
                productField.requestFocus();
            } catch (NumberFormatException e) {
                showAlert("Invalid Quantity", "Please enter a valid number for quantity");
            }
        }
    }

    private static void handleAddMaterial(TextField materialField, TextField quantityField, ListView<String> materialsList) {
        String material = materialField.getText().trim();
        String quantity = quantityField.getText().trim();
        
        if (!material.isEmpty() && !quantity.isEmpty()) {
            try {
                double qty = Double.parseDouble(quantity);
                materialsList.getItems().add(String.format("%s - %.2f", material, qty));
                materialField.clear();
                quantityField.clear();
                materialField.requestFocus();
            } catch (NumberFormatException e) {
                showAlert("Invalid Quantity", "Please enter a valid number for quantity");
            }
        }
    }

    private static void handleSubmitProduction(
        DatePicker datePicker, 
        TextArea notesArea, 
        ListView<String> itemsList, 
        ListView<String> materialsList
    ) {
        String date = datePicker.getValue().format(DATE_FORMATTER);
        String notes = notesArea.getText().trim();
        
        if (itemsList.getItems().isEmpty()) {
            showAlert("No Items", "Please add at least one production item");
            return;
        }
        
        // In real app, this would save to database
        StringBuilder summary = new StringBuilder();
        summary.append("Production Invoice Summary\n");
        summary.append("Date: ").append(date).append("\n");
        if (!notes.isEmpty()) summary.append("Notes: ").append(notes).append("\n");
        
        summary.append("\nProduction Items:\n");
        itemsList.getItems().forEach(item -> summary.append("- ").append(item).append("\n"));
        
        if (!materialsList.getItems().isEmpty()) {
            summary.append("\nRaw Materials Used:\n");
            materialsList.getItems().forEach(mat -> summary.append("- ").append(mat).append("\n"));
        }
        
        System.out.println(summary.toString());
        
        // Clear form
        datePicker.setValue(LocalDate.now());
        notesArea.clear();
        itemsList.getItems().clear();
        materialsList.getItems().clear();
    }

    private static ListView<String> createEnhancedListView() {
        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(300);
        listView.setPlaceholder(new Label("No items added"));
        
        // Context menu for delete
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete Selected");
        deleteItem.setOnAction(e -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                listView.getItems().remove(selectedIndex);
            }
        });
        contextMenu.getItems().add(deleteItem);
        listView.setContextMenu(contextMenu);
        
        // Keyboard delete support
        listView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    listView.getItems().remove(selectedIndex);
                }
            }
        });
        
        return listView;
    }

    private static Label createHeading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-heading");
        label.setFont(Font.font(18));
        return label;
    }

    private static Label createSubheading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-subheading");
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

    private static Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);
        return button;
    }

    private static Button createSubmitButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("form-submit");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private static HBox createFormRow(String labelText, Control field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        HBox row = new HBox(10, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");
        return row;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
