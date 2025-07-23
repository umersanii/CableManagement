package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.beans.property.SimpleStringProperty;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import com.cablemanagement.database.SQLiteDatabase;
import com.cablemanagement.database.db;
import com.cablemanagement.model.Brand;
import com.cablemanagement.model.ProductionStockItem;

public class ProductionStock {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final db database = new SQLiteDatabase();
    private static final SQLiteDatabase sqliteDatabase = new SQLiteDatabase();

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createProductionStockForm());

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
            "Register Production Stock",
            "Create Production Invoice",
            "Create Return Production Invoice",
            "Create Sales Invoice", 
            "Create Return Sales Invoice",
            "View Production Stock Usage Report"
        };

        Runnable[] actions = {
            () -> formArea.getChildren().setAll(createProductionStockForm()),
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

    private static VBox createProductionStockForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Register Production Stock");

        // Input fields matching ProductionStock table structure
        TextField nameField = createTextField("Product Name");
        TextField quantityField = createTextField("Quantity");
        TextField unitCostField = createTextField("Unit Cost");
        
        // Brand ComboBox 
        ComboBox<String> brandCombo = new ComboBox<>();
        brandCombo.setPromptText("Select Brand");
        for (Brand b : database.getAllBrands()) {
            brandCombo.getItems().add(b.nameProperty().get());
        }
        brandCombo.setPrefWidth(200);

        Button submitBtn = createSubmitButton("Submit Production Stock");

        // Production Stock Table
        Label tableHeading = createSubheading("Registered Production Stock:");
        TableView<ProductionStockRecord> stockTable = createProductionStockTable();
        refreshProductionStockTable(stockTable);

        submitBtn.setOnAction(e -> handleProductionStockSubmit(
            nameField, brandCombo, quantityField, unitCostField, stockTable
        ));

        // Create form content in a compact layout
        VBox formContent = new VBox(15);
        formContent.getChildren().addAll(
            heading, 
            createFormRow("Product Name:", nameField),
            createFormRow("Brand:", brandCombo),
            createFormRow("Quantity:", quantityField),
            createFormRow("Unit Cost:", unitCostField),
            submitBtn, tableHeading, stockTable
        );

        // Wrap form in ScrollPane for responsiveness
        ScrollPane scrollPane = new ScrollPane(formContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefViewportHeight(600);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        form.getChildren().add(scrollPane);
        
        return form;
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
        
        // Auto-generated invoice number
        TextField invoiceNumberField = createTextField(sqliteDatabase.generateProductionInvoiceNumber());
        invoiceNumberField.setEditable(false);
        invoiceNumberField.getStyleClass().add("readonly-field");
        
        DatePicker productionDatePicker = new DatePicker();
        productionDatePicker.setValue(LocalDate.now());
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(3);
        notesArea.setPrefWidth(400);
        
        infoGrid.add(createFormRow("Invoice Number:", invoiceNumberField), 0, 0);
        infoGrid.add(createFormRow("Production Date:", productionDatePicker), 0, 1);
        infoGrid.add(createFormRow("Notes:", notesArea), 0, 2);

        // Items and Materials Sections
        HBox itemsMaterialsSection = new HBox(20);
        itemsMaterialsSection.setAlignment(Pos.TOP_LEFT);

        // Production Items Section
        VBox itemsSection = new VBox(10);
        itemsSection.setMinWidth(400);
        
        // Load production stocks for dropdown
        ComboBox<String> productComboBox = createProductionStockComboBox();
        TextField quantityField = createTextField("Quantity");
        
        HBox itemButtonBox = new HBox(10);
        Button addItemBtn = createActionButton("Add Item");
        Button clearItemsBtn = createActionButton("Clear All");
        
        itemButtonBox.getChildren().addAll(addItemBtn, clearItemsBtn);
        
        ListView<String> itemsList = createEnhancedListView();
        
        itemsSection.getChildren().addAll(
            createSubheading("Production Items:"),
            createFormRow("Product:", productComboBox),
            createFormRow("Quantity:", quantityField),
            itemButtonBox,
            itemsList
        );

        // Raw Materials Section
        VBox materialsSection = new VBox(10);
        materialsSection.setMinWidth(400);
        
        // Load raw stocks for dropdown
        ComboBox<String> rawMaterialComboBox = createRawStockComboBox();
        TextField rawQuantityField = createTextField("Quantity Used");
        
        HBox materialButtonBox = new HBox(10);
        Button addMaterialBtn = createActionButton("Add Material");
        Button clearMaterialsBtn = createActionButton("Clear All");
        
        materialButtonBox.getChildren().addAll(addMaterialBtn, clearMaterialsBtn);
        
        ListView<String> materialsList = createEnhancedListView();
        
        materialsSection.getChildren().addAll(
            createSubheading("Raw Materials Used:"),
            createFormRow("Raw Material:", rawMaterialComboBox),
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
        addItemBtn.setOnAction(e -> handleAddProductionItem(productComboBox, quantityField, itemsList));
        quantityField.setOnAction(e -> handleAddProductionItem(productComboBox, quantityField, itemsList));
        
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
        
        addMaterialBtn.setOnAction(e -> handleAddRawMaterial(rawMaterialComboBox, rawQuantityField, materialsList));
        rawQuantityField.setOnAction(e -> handleAddRawMaterial(rawMaterialComboBox, rawQuantityField, materialsList));
        
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
        
        submitBtn.setOnAction(e -> handleSubmitProductionInvoice(
            invoiceNumberField,
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

        // Auto-generate return invoice number
        TextField returnInvoiceNumberField = createTextField("Return Invoice Number");
        returnInvoiceNumberField.setEditable(false);
        
        try {
            String autoGeneratedNumber = sqliteDatabase.generateProductionReturnInvoiceNumber();
            returnInvoiceNumberField.setText(autoGeneratedNumber);
        } catch (Exception e) {
            returnInvoiceNumberField.setText("Error generating number");
            e.printStackTrace();
        }

        // Return date
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(LocalDate.now());

        // Production invoice dropdown
        ComboBox<String> productionInvoiceCombo = new ComboBox<>();
        productionInvoiceCombo.setPromptText("Select Production Invoice");
        productionInvoiceCombo.setPrefWidth(400);
        
        // Load production invoices
        try {
            List<Object[]> productionInvoiceData = sqliteDatabase.getAllProductionInvoicesForDropdown();
            List<String> productionInvoices = new ArrayList<>();
            for (Object[] invoice : productionInvoiceData) {
                int invoiceId = (Integer) invoice[0];
                String date = (String) invoice[1];
                String notes = (String) invoice[2];
                String displayText = "Invoice #" + invoiceId + " - " + date + (notes != null && !notes.isEmpty() ? " (" + notes + ")" : "");
                productionInvoices.add(displayText);
            }
            productionInvoiceCombo.getItems().addAll(productionInvoices);
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load production invoices: " + e.getMessage());
        }

        // Available items list (from selected production invoice)
        Label availableItemsLabel = createSubheading("Available Items from Selected Invoice:");
        ListView<String> availableItemsList = new ListView<>();
        availableItemsList.setPrefHeight(150);

        // Return items list with quantities
        Label returnItemsLabel = createSubheading("Items to Return:");
        ListView<String> returnItemsList = createEnhancedListView();
        
        // Add return item controls
        HBox addReturnItemBox = new HBox(10);
        ComboBox<String> itemCombo = new ComboBox<>();
        itemCombo.setPromptText("Select Item");
        itemCombo.setPrefWidth(200);
        TextField returnQuantityField = createTextField("Return Quantity");
        returnQuantityField.setPrefWidth(150);
        Button addReturnItemBtn = createActionButton("Add to Return");
        addReturnItemBox.getChildren().addAll(itemCombo, returnQuantityField, addReturnItemBtn);

        // Total return quantity
        TextField totalReturnQuantityField = createTextField("Total Return Quantity");
        totalReturnQuantityField.setEditable(false);
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        Button submitBtn = createSubmitButton("Submit Return Invoice");
        Button clearBtn = createActionButton("Clear All");
        actionButtons.getChildren().addAll(submitBtn, clearBtn);

        form.getChildren().addAll(
            heading,
            createFormRow("Return Invoice Number:", returnInvoiceNumberField),
            createFormRow("Return Date:", returnDatePicker),
            createFormRow("Select Production Invoice:", productionInvoiceCombo),
            availableItemsLabel,
            availableItemsList,
            returnItemsLabel,
            addReturnItemBox,
            returnItemsList,
            createFormRow("Total Return Quantity:", totalReturnQuantityField),
            actionButtons
        );

        // Event handlers
        productionInvoiceCombo.setOnAction(e -> {
            String selectedInvoice = productionInvoiceCombo.getValue();
            if (selectedInvoice != null && !selectedInvoice.isEmpty()) {
                try {
                    // Extract production invoice ID from the dropdown text
                    String productionInvoiceId = selectedInvoice.split(" - ")[0].replace("Invoice #", "");
                    
                    // Load items from selected production invoice
                    List<Object[]> itemsData = sqliteDatabase.getProductionItemsByInvoiceId(Integer.parseInt(productionInvoiceId));
                    List<String> items = new ArrayList<>();
                    for (Object[] item : itemsData) {
                        int productionId = (Integer) item[0];
                        String productName = (String) item[1];
                        String brandName = (String) item[2];
                        double quantity = (Double) item[3];
                        String displayText = productName + " - " + brandName + " - Quantity: " + quantity;
                        items.add(displayText);
                    }
                    availableItemsList.getItems().clear();
                    availableItemsList.getItems().addAll(items);
                    
                    // Update item combo box
                    itemCombo.getItems().clear();
                    itemCombo.getItems().addAll(items);
                    
                    // Clear previous return items
                    returnItemsList.getItems().clear();
                    totalReturnQuantityField.setText("0");
                    
                } catch (Exception ex) {
                    showAlert("Database Error", "Failed to load items: " + ex.getMessage());
                }
            }
        });

        addReturnItemBtn.setOnAction(e -> {
            String selectedItem = itemCombo.getValue();
            String returnQuantityText = returnQuantityField.getText().trim();
            
            if (selectedItem == null || selectedItem.isEmpty()) {
                showAlert("Missing Information", "Please select an item");
                return;
            }
            
            if (returnQuantityText.isEmpty()) {
                showAlert("Missing Information", "Please enter return quantity");
                return;
            }
            
            try {
                int returnQuantity = Integer.parseInt(returnQuantityText);
                if (returnQuantity <= 0) {
                    showAlert("Invalid Quantity", "Return quantity must be greater than 0");
                    return;
                }
                
                // Add to return items list
                String returnItemText = selectedItem + " (Return Qty: " + returnQuantity + ")";
                returnItemsList.getItems().add(returnItemText);
                
                // Update total return quantity
                int currentTotal = Integer.parseInt(totalReturnQuantityField.getText().isEmpty() ? "0" : totalReturnQuantityField.getText());
                totalReturnQuantityField.setText(String.valueOf(currentTotal + returnQuantity));
                
                // Clear fields
                itemCombo.setValue(null);
                returnQuantityField.clear();
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Return quantity must be a valid number");
            }
        });
        
        clearBtn.setOnAction(e -> {
            if (!returnItemsList.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all return items?");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    returnItemsList.getItems().clear();
                    totalReturnQuantityField.setText("0");
                    itemCombo.setValue(null);
                    returnQuantityField.clear();
                }
            }
        });
        
        submitBtn.setOnAction(e -> handleSubmitReturnProductionInvoice(
            returnInvoiceNumberField.getText(),
            returnDatePicker.getValue(),
            productionInvoiceCombo.getValue(),
            returnItemsList.getItems(),
            totalReturnQuantityField.getText(),
            returnInvoiceNumberField,
            returnDatePicker,
            productionInvoiceCombo,
            availableItemsList,
            returnItemsList,
            itemCombo,
            returnQuantityField,
            totalReturnQuantityField
        ));

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

    // Production Stock specific methods
    private static void handleProductionStockSubmit(
            TextField nameField, ComboBox<String> brandCombo, 
            TextField quantityField, TextField unitCostField, 
            TableView<ProductionStockRecord> stockTable) {
        
        String name = nameField.getText().trim();
        String brand = brandCombo.getSelectionModel().getSelectedItem();
        String quantityText = quantityField.getText().trim();
        String unitCostText = unitCostField.getText().trim();
        
        // Validation
        if (name.isEmpty() || brand == null || quantityText.isEmpty() || unitCostText.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }
        
        try {
            int quantity = Integer.parseInt(quantityText);
            double unitCost = Double.parseDouble(unitCostText);
            
            if (quantity < 0 || unitCost < 0) {
                showAlert("Error", "Quantity and Unit Cost must be non-negative.");
                return;
            }
            
            // Insert into database - using quantity as openingQty and unitCost as salePrice
            boolean success = database.insertProductionStock(name, "", brand, "", quantity, unitCost, 0.0);
            
            if (success) {
                showAlert("Success", "Production Stock registered successfully!");
                // Clear form
                nameField.clear();
                brandCombo.getSelectionModel().clearSelection();
                quantityField.clear();
                unitCostField.clear();
                // Refresh table
                refreshProductionStockTable(stockTable);
            } else {
                showAlert("Error", "Failed to register production stock.");
            }
            
        } catch (NumberFormatException ex) {
            showAlert("Error", "Please enter valid numbers for Quantity and Unit Cost.");
        }
    }

    private static TableView<ProductionStockRecord> createProductionStockTable() {
        TableView<ProductionStockRecord> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<ProductionStockRecord, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(150);
        
        TableColumn<ProductionStockRecord, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrand()));
        brandCol.setPrefWidth(100);
        
        TableColumn<ProductionStockRecord, String> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        quantityCol.setPrefWidth(80);
        
        TableColumn<ProductionStockRecord, String> unitCostCol = new TableColumn<>("Unit Cost");
        unitCostCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().getUnitCost())));
        unitCostCol.setPrefWidth(100);
        
        TableColumn<ProductionStockRecord, String> totalCostCol = new TableColumn<>("Total Cost");
        totalCostCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().getTotalCost())));
        totalCostCol.setPrefWidth(100);
        
        table.getColumns().addAll(nameCol, brandCol, quantityCol, unitCostCol, totalCostCol);
        return table;
    }

    private static void refreshProductionStockTable(TableView<ProductionStockRecord> table) {
        ObservableList<ProductionStockRecord> data = FXCollections.observableArrayList();
        
        // Get all production stocks from database
        for (Object[] stock : database.getAllProductionStocks()) {
            data.add(new ProductionStockRecord(
                (String) stock[1], // product_name
                (String) stock[2], // brand_name
                (Integer) stock[3], // quantity
                (Double) stock[4], // unit_cost
                (Double) stock[5]  // total_cost
            ));
        }
        
        table.setItems(data);
    }

    // Simple record class for table display
    private static class ProductionStockRecord {
        private final String name;
        private final String brand;
        private final int quantity;
        private final double unitCost;
        private final double totalCost;
        
        public ProductionStockRecord(String name, String brand, int quantity, double unitCost, double totalCost) {
            this.name = name;
            this.brand = brand;
            this.quantity = quantity;
            this.unitCost = unitCost;
            this.totalCost = totalCost;
        }
        
        public String getName() { return name; }
        public String getBrand() { return brand; }
        public int getQuantity() { return quantity; }
        public double getUnitCost() { return unitCost; }
        public double getTotalCost() { return totalCost; }
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

    // Production Stock ComboBox
    private static ComboBox<String> createProductionStockComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getStyleClass().add("form-input");
        comboBox.setPromptText("Select Production Stock");
        comboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Load production stocks from database
        try {
            List<Object[]> productionStocks = database.getAllProductionStocksForDropdown();
            ObservableList<String> items = FXCollections.observableArrayList();
            
            for (Object[] stock : productionStocks) {
                // Format: "Product Name - Brand - Available: X"
                String item = String.format("%s - %s - Available: %.2f", 
                    stock[1], // product_name
                    stock[3], // brand_name
                    stock[6]  // quantity
                );
                items.add(item);
            }
            
            comboBox.setItems(items);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load production stocks: " + e.getMessage());
        }
        
        return comboBox;
    }

    // Raw Stock ComboBox
    private static ComboBox<String> createRawStockComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getStyleClass().add("form-input");
        comboBox.setPromptText("Select Raw Material");
        comboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Load raw stocks from database
        try {
            List<Object[]> rawStocks = database.getAllRawStocksWithUnitsForDropdown();
            ObservableList<String> items = FXCollections.observableArrayList();
            
            for (Object[] stock : rawStocks) {
                // Format: "Raw Material Name - Brand - Available: X"
                String item = String.format("%s - %s - Available: %.2f", 
                    stock[1], // item_name
                    stock[3], // brand_name
                    stock[5]  // quantity
                );
                items.add(item);
            }
            
            comboBox.setItems(items);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load raw stocks: " + e.getMessage());
        }
        
        return comboBox;
    }

    // Handle adding production item with ComboBox
    private static void handleAddProductionItem(ComboBox<String> productComboBox, TextField quantityField, ListView<String> itemsList) {
        String selectedProduct = productComboBox.getValue();
        String quantityText = quantityField.getText().trim();
        
        if (selectedProduct != null && !quantityText.isEmpty()) {
            try {
                double quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    showAlert("Invalid Input", "Quantity must be greater than 0");
                    return;
                }
                
                // Extract product name from the ComboBox selection
                String productName = selectedProduct.split(" - ")[0];
                String displayText = String.format("%s - Quantity: %.2f", productName, quantity);
                
                itemsList.getItems().add(displayText);
                productComboBox.setValue(null);
                quantityField.clear();
                
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number for quantity");
            }
        } else {
            showAlert("Missing Input", "Please select a product and enter quantity");
        }
    }

    // Handle adding raw material with ComboBox
    private static void handleAddRawMaterial(ComboBox<String> rawMaterialComboBox, TextField quantityField, ListView<String> materialsList) {
        String selectedMaterial = rawMaterialComboBox.getValue();
        String quantityText = quantityField.getText().trim();
        
        if (selectedMaterial != null && !quantityText.isEmpty()) {
            try {
                double quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    showAlert("Invalid Input", "Quantity must be greater than 0");
                    return;
                }
                
                // Extract material name from the ComboBox selection
                String materialName = selectedMaterial.split(" - ")[0];
                String displayText = String.format("%s - Quantity Used: %.2f", materialName, quantity);
                
                materialsList.getItems().add(displayText);
                rawMaterialComboBox.setValue(null);
                quantityField.clear();
                
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number for quantity");
            }
        } else {
            showAlert("Missing Input", "Please select a raw material and enter quantity");
        }
    }

    // Handle production invoice submission
    private static void handleSubmitProductionInvoice(TextField invoiceNumberField, DatePicker productionDatePicker, TextArea notesArea, 
                                                    ListView<String> itemsList, ListView<String> materialsList) {
        try {
            String productionDate = productionDatePicker.getValue().format(DATE_FORMATTER);
            String notes = notesArea.getText().trim();
            
            if (itemsList.getItems().isEmpty()) {
                showAlert("Missing Items", "Please add at least one production item");
                return;
            }
            
            // Insert production invoice and get ID
            int invoiceId = database.insertProductionInvoiceAndGetId(productionDate, notes);
            if (invoiceId == -1) {
                showAlert("Error", "Failed to create production invoice");
                return;
            }
            
            // Prepare production items data
            List<Object[]> productionItems = new ArrayList<>();
            for (String item : itemsList.getItems()) {
                // Parse the display text to extract data
                String[] parts = item.split(" - Quantity: ");
                if (parts.length == 2) {
                    String productName = parts[0];
                    double quantity = Double.parseDouble(parts[1]);
                    
                    // Get production stock ID by name
                    int productionStockId = getProductionStockIdByName(productName);
                    if (productionStockId != -1) {
                        productionItems.add(new Object[]{productionStockId, quantity});
                    }
                }
            }
            
            // Insert production invoice items
            if (!database.insertProductionInvoiceItems(invoiceId, productionItems)) {
                showAlert("Error", "Failed to save production items");
                return;
            }
            
            // Prepare raw materials data (if any)
            if (!materialsList.getItems().isEmpty()) {
                List<Object[]> rawMaterialsUsed = new ArrayList<>();
                for (String material : materialsList.getItems()) {
                    // Parse the display text to extract data
                    String[] parts = material.split(" - Quantity Used: ");
                    if (parts.length == 2) {
                        String materialName = parts[0];
                        double quantity = Double.parseDouble(parts[1]);
                        
                        // Get raw stock ID by name
                        int rawStockId = database.getRawStockIdByName(materialName);
                        if (rawStockId != -1) {
                            rawMaterialsUsed.add(new Object[]{rawStockId, quantity});
                        }
                    }
                }
                
                // Insert raw material usage
                if (!database.insertProductionStockRawUsage(invoiceId, rawMaterialsUsed)) {
                    showAlert("Warning", "Production invoice created but failed to save raw material usage");
                }
            }
            
            showAlert("Success", "Production invoice created successfully!");
            
            // Clear form
            invoiceNumberField.setText(sqliteDatabase.generateProductionInvoiceNumber());
            productionDatePicker.setValue(LocalDate.now());
            notesArea.clear();
            itemsList.getItems().clear();
            materialsList.getItems().clear();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to submit production invoice: " + e.getMessage());
        }
    }

    // Helper method to get production stock ID by name
    private static int getProductionStockIdByName(String productName) {
        try {
            List<Object[]> productionStocks = database.getAllProductionStocksForDropdown();
            for (Object[] stock : productionStocks) {
                if (stock[1].toString().equals(productName)) {
                    return (Integer) stock[0]; // production_id
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Handle return production invoice submission
    private static void handleSubmitReturnProductionInvoice(String returnInvoiceNumber, LocalDate returnDate, 
                                                          String selectedProductionInvoice, ObservableList<String> returnItems,
                                                          String totalReturnQuantity, TextField returnInvoiceNumberField,
                                                          DatePicker returnDatePicker, ComboBox<String> productionInvoiceCombo,
                                                          ListView<String> availableItemsList, ListView<String> returnItemsList,
                                                          ComboBox<String> itemCombo, TextField returnQuantityField,
                                                          TextField totalReturnQuantityField) {
        try {
            // Validation
            if (selectedProductionInvoice == null || selectedProductionInvoice.isEmpty()) {
                showAlert("Missing Information", "Please select a production invoice");
                return;
            }
            
            if (returnItems.isEmpty()) {
                showAlert("Missing Information", "Please add at least one item to return");
                return;
            }
            
            if (totalReturnQuantity.isEmpty() || Integer.parseInt(totalReturnQuantity) <= 0) {
                showAlert("Missing Information", "Total return quantity must be greater than 0");
                return;
            }

            // Extract production invoice ID
            String productionInvoiceId = selectedProductionInvoice.split(" - ")[0].replace("Invoice #", "");
            
            // Create return invoice record
            String formattedDate = returnDate.format(DATE_FORMATTER);
            String reference = "Return for Invoice #" + productionInvoiceId;
            int totalQuantity = Integer.parseInt(totalReturnQuantity);
            
            // Insert return invoice and get ID
            int returnInvoiceId = sqliteDatabase.insertProductionReturnInvoiceAndGetId(formattedDate, reference, totalQuantity, Integer.parseInt(productionInvoiceId));
            
            if (returnInvoiceId > 0) {
                // Prepare return invoice items
                List<Object[]> returnInvoiceItems = new ArrayList<>();
                
                for (String returnItem : returnItems) {
                    try {
                        // Parse item format: "ProductName - BrandName - Quantity: X (Return Qty: Y)"
                        String[] parts = returnItem.split(" \\(Return Qty: ");
                        if (parts.length == 2) {
                            String productPart = parts[0]; // "ProductName - BrandName - Quantity: X"
                            String returnQtyPart = parts[1].replace(")", ""); // "Y"
                            
                            // Extract product name and brand name (everything before " - Quantity:")
                            String[] productParts = productPart.split(" - Quantity:");
                            if (productParts.length == 2) {
                                String fullProductInfo = productParts[0]; // "ProductName - BrandName"
                                String[] productInfoParts = fullProductInfo.split(" - ");
                                if (productInfoParts.length >= 2) {
                                    String productName = productInfoParts[0];
                                    int returnQuantity = Integer.parseInt(returnQtyPart);
                                    
                                    // Get production stock ID by name
                                    int productionStockId = getProductionStockIdByName(productName);
                                    if (productionStockId > 0) {
                                        returnInvoiceItems.add(new Object[]{
                                            returnInvoiceId,
                                            productionStockId,
                                            returnQuantity
                                        });
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing return item: " + returnItem + " - " + e.getMessage());
                    }
                }
                
                // Insert return invoice items
                if (!returnInvoiceItems.isEmpty()) {
                    sqliteDatabase.insertProductionReturnInvoiceItems(returnInvoiceId, Integer.parseInt(productionInvoiceId), returnInvoiceItems);
                    
                    showAlert("Success", "Return production invoice saved successfully!\nReturn Invoice Number: " + returnInvoiceNumber);
                    
                    // Clear form
                    try {
                        String newReturnInvoiceNumber = sqliteDatabase.generateProductionReturnInvoiceNumber();
                        returnInvoiceNumberField.setText(newReturnInvoiceNumber);
                    } catch (Exception e) {
                        returnInvoiceNumberField.setText("Error generating number");
                    }
                    
                    returnDatePicker.setValue(LocalDate.now());
                    productionInvoiceCombo.setValue(null);
                    availableItemsList.getItems().clear();
                    returnItemsList.getItems().clear();
                    itemCombo.setValue(null);
                    returnQuantityField.clear();
                    totalReturnQuantityField.setText("0");
                    
                } else {
                    showAlert("Error", "No valid return items found to save");
                }
            } else {
                showAlert("Error", "Failed to create return production invoice");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to submit return production invoice: " + e.getMessage());
        }
    }
}
