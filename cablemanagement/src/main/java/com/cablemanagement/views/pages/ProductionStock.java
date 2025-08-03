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
import javafx.beans.property.SimpleDoubleProperty;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import com.cablemanagement.database.SQLiteDatabase;
import com.cablemanagement.database.db;
import com.cablemanagement.model.Brand;
import com.cablemanagement.model.ProductionStockItem;
import com.cablemanagement.invoice.PrintManager;
import com.cablemanagement.invoice.InvoiceData;
import com.cablemanagement.invoice.Item;

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
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.getStyleClass().add("form-container");

        Label heading = createHeading("Register Production Stock");

        // === TWO-COLUMN LAYOUT CONTAINER ===
        HBox twoColumnLayout = new HBox(30);
        twoColumnLayout.setAlignment(Pos.TOP_LEFT);
        twoColumnLayout.setFillHeight(true);

        // === LEFT COLUMN - INPUT FORM ===
        VBox leftColumn = new VBox(20);
        leftColumn.setPrefWidth(400);
        leftColumn.setMinWidth(350);
        leftColumn.setMaxWidth(450);
        leftColumn.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20; -fx-background-color: #fafafa;");

        Label formTitle = createSubheading("Product Registration");
        formTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 16px;");

        // Input fields in a clean grid layout
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(15);
        inputGrid.setVgap(20);
        inputGrid.setAlignment(Pos.TOP_LEFT);

        // Product Name Field
        TextField nameField = createTextField("Enter Product Name");
        nameField.setPrefWidth(300);
        nameField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");

        // Brand ComboBox 
        ComboBox<String> brandCombo = new ComboBox<>();
        brandCombo.setPromptText("-- Select Brand --");
        brandCombo.setEditable(false);
        brandCombo.setPrefWidth(300);
        brandCombo.setStyle("-fx-padding: 8; -fx-font-size: 14px;");
        
        try {
            List<Brand> brands = database.getAllBrands();
            ObservableList<String> brandNames = FXCollections.observableArrayList();
            for (Brand brand : brands) {
                brandNames.add(brand.nameProperty().get());
            }
            brandCombo.setItems(brandNames);
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load brands: " + e.getMessage());
        }

        // Quantity Field
        TextField quantityField = createTextField("Enter Quantity");
        quantityField.setPrefWidth(300);
        quantityField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");

        // Unit Cost Field
        TextField unitCostField = createTextField("Enter Unit Cost");
        unitCostField.setPrefWidth(300);
        unitCostField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");

        // Sale Price Field (manual entry)
        TextField salePriceField = createTextField("Enter Sale Price");
        salePriceField.setPrefWidth(300);
        salePriceField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");

        // Add fields to grid with labels
        inputGrid.add(createFormRow("Product Name:", nameField), 0, 0);
        inputGrid.add(createFormRow("Brand:", brandCombo), 0, 1);
        inputGrid.add(createFormRow("Quantity:", quantityField), 0, 2);
        inputGrid.add(createFormRow("Unit Cost:", unitCostField), 0, 3);
        inputGrid.add(createFormRow("Sale Price:", salePriceField), 0, 4);

        // Action buttons for the form
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button submitBtn = createSubmitButton("Register Product");
        submitBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20;");
        submitBtn.setPrefWidth(150);

        Button clearBtn = createActionButton("Clear Form");
        clearBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        clearBtn.setPrefWidth(120);

        buttonBox.getChildren().addAll(submitBtn, clearBtn);

        // Add components to left column
        leftColumn.getChildren().addAll(formTitle, inputGrid, buttonBox);

        // === RIGHT COLUMN - PRODUCTION STOCK TABLE ===
        VBox rightColumn = new VBox(15);
        rightColumn.setMinWidth(600);
        rightColumn.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20; -fx-background-color: #ffffff;");

        Label tableTitle = createSubheading("Registered Production Stock");
        tableTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 16px;");

        // Search and filter controls
        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 0, 15, 0));

        TextField searchField = createTextField("Search products...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.setPromptText("Filter by Brand");
        filterCombo.setPrefWidth(150);
        filterCombo.getItems().add("All Brands");
        
        // Load brands for filter
        try {
            List<Brand> brands = database.getAllBrands();
            for (Brand brand : brands) {
                filterCombo.getItems().add(brand.nameProperty().get());
            }
            filterCombo.getSelectionModel().selectFirst(); // Select "All Brands"
        } catch (Exception e) {
            System.err.println("Failed to load brands for filter: " + e.getMessage());
        }

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 8 15;");

        searchBox.getChildren().addAll(searchField, filterCombo, refreshBtn);

        // Production Stock Table with enhanced styling
        TableView<ProductionStockRecord> stockTable = createProductionStockTable();
        stockTable.setPrefHeight(400);
        stockTable.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1;");

        // Stock summary labels
        HBox summaryBox = new HBox(30);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.setPadding(new Insets(15, 0, 0, 0));
        summaryBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-radius: 5;");

        Label totalItemsLabel = new Label("Total Items: 0");
        totalItemsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #495057;");

        Label totalValueLabel = new Label("Total Value: 0.00");
        totalValueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        Label lowStockLabel = new Label("Low Stock Items: 0");
        lowStockLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");

        summaryBox.getChildren().addAll(totalItemsLabel, totalValueLabel, lowStockLabel);

        // Add components to right column
        rightColumn.getChildren().addAll(tableTitle, searchBox, stockTable, summaryBox);

        // Add columns to the two-column layout
        twoColumnLayout.getChildren().addAll(leftColumn, rightColumn);

        // === MAIN CONTAINER WITH SCROLLING ===
        ScrollPane mainScrollPane = new ScrollPane(twoColumnLayout);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(false);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setPrefViewportHeight(600);
        mainScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Add heading and scrollable content to main container
        mainContainer.getChildren().addAll(heading, mainScrollPane);

        // === EVENT HANDLERS ===

        // Clear form button
        clearBtn.setOnAction(e -> {
            nameField.clear();
            brandCombo.setValue(null);
            quantityField.clear();
            unitCostField.clear();
            salePriceField.clear();
            nameField.requestFocus();
        });

        // Submit button
        submitBtn.setOnAction(e -> handleProductionStockSubmit(
            nameField, brandCombo, quantityField, unitCostField, salePriceField, stockTable,
            totalItemsLabel, totalValueLabel, lowStockLabel
        ));

        // Refresh button
        refreshBtn.setOnAction(e -> {
            refreshProductionStockTable(stockTable);
            updateStockSummary(stockTable, totalItemsLabel, totalValueLabel, lowStockLabel);
        });

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterProductionStockTable(stockTable, newVal, filterCombo.getValue());
        });

        // Filter functionality
        filterCombo.setOnAction(e -> {
            filterProductionStockTable(stockTable, searchField.getText(), filterCombo.getValue());
        });

        // Load initial data
        refreshProductionStockTable(stockTable);
        updateStockSummary(stockTable, totalItemsLabel, totalValueLabel, lowStockLabel);

        return mainContainer;
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
        TextField invoiceNumberField = createTextField("Production Invoice Number");
        invoiceNumberField.setEditable(false);
        invoiceNumberField.getStyleClass().add("readonly-field");
        
        try {
            String autoGeneratedNumber = sqliteDatabase.generateProductionInvoiceNumber();
            invoiceNumberField.setText(autoGeneratedNumber);
        } catch (Exception e) {
            invoiceNumberField.setText("Error generating number");
            e.printStackTrace();
        }
        
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
        
        // Submit & Print Button
        Button submitBtn = createSubmitButton("Submit & Print Production Invoice");
        submitBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
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
        Button submitBtn = createSubmitButton("Submit & Print Return Invoice");
        Button clearBtn = createActionButton("Clear All");
        
        submitBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        clearBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        
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
        
        submitBtn.setOnAction(e -> {
            // Call submit handler first
            handleSubmitReturnProductionInvoice(
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
            );
            
            // After successful submission, get the latest invoice and print it
            try {
                // Get the latest return production invoice and its items
                List<Object[]> lastInvoiceData = sqliteDatabase.getLastProductionReturnInvoice();
                if (!lastInvoiceData.isEmpty()) {
                    Object[] lastInvoice = lastInvoiceData.get(0);
                    String returnInvoiceNumber = (String) lastInvoice[0];
                    String returnDate = (String) lastInvoice[1];
                    String notes = (String) lastInvoice[2];
                    
                    // Get the items for the invoice
                    List<Object[]> returnItemsData = sqliteDatabase.getProductionReturnInvoiceItems(returnInvoiceNumber);
                    
                    // Prepare items for printing
                    List<Item> printItems = new ArrayList<>();
                    for (Object[] itemData : returnItemsData) {
                        String productName = (String) itemData[0];
                        double quantity = ((Number) itemData[1]).doubleValue();
                        double unitCost = ((Number) itemData[2]).doubleValue();
                        
                        printItems.add(new Item(
                            productName,
                            (int) quantity,
                            unitCost,
                            0.0 // no discount for returns
                        ));
                    }
                    
                    // Create invoice data for printing
                    String title = "Production Return";
                    String address = notes != null && !notes.isEmpty() ? notes : "Return Production Items";
                    InvoiceData invoiceData = new InvoiceData(
                        returnInvoiceNumber,
                        returnDate,
                        title,
                        address,
                        0.0, // no previous balance for returns
                        printItems
                    );
                    
                    // Try to open for preview first
                    boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Production Return");
                    if (!previewSuccess) {
                        // If preview fails, try printer selection
                        PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Production Return");
                    }
                }
            } catch (Exception ex) {
                showAlert("Print Error", "Failed to print return invoice: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        return form;
    }

    private static VBox createSalesInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Sales Invoice");
        
        // Create a VBox for scrollable content
        VBox scrollableContent = new VBox(20);
        scrollableContent.setPadding(new Insets(0, 20, 20, 20));

        // === INVOICE HEADER SECTION ===
        VBox headerSection = new VBox(15);
        headerSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label headerTitle = createSubheading("Invoice Information");
        headerTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Invoice header fields in a grid layout
        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(20);
        headerGrid.setVgap(15);
        headerGrid.setAlignment(Pos.TOP_LEFT);
        
        TextField invoiceNumberField = createTextField("Auto-generated");
        invoiceNumberField.setEditable(false);
        invoiceNumberField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
        
        // Auto-generate invoice number
        String autoInvoiceNumber = database.generateSalesInvoiceNumber();
        invoiceNumberField.setText(autoInvoiceNumber);
        
        DatePicker salesDatePicker = new DatePicker();
        salesDatePicker.setValue(LocalDate.now());
        salesDatePicker.setPrefWidth(200);
        
        // Customer dropdown with improved styling
        ComboBox<String> customerComboBox = new ComboBox<>();
        customerComboBox.setPromptText("-- Select Customer --");
        customerComboBox.setEditable(false);
        customerComboBox.setPrefWidth(250);
        
        // Load customers with error handling
        try {
            List<Object[]> customers = database.getAllCustomersForDropdown();
            ObservableList<String> customerNames = FXCollections.observableArrayList();
            for (Object[] customer : customers) {
                customerNames.add((String) customer[1]); // customer_name
            }
            customerComboBox.setItems(customerNames);
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load customers: " + e.getMessage());
        }
        
        // Add fields to grid
        headerGrid.add(createFormRow("Invoice Number:", invoiceNumberField), 0, 0);
        headerGrid.add(createFormRow("Sales Date:", salesDatePicker), 1, 0);  
        headerGrid.add(createFormRow("Customer:", customerComboBox), 0, 1, 2, 1);
        
        headerSection.getChildren().addAll(headerTitle, headerGrid);

        // === PRODUCT SELECTION SECTION ===
        VBox productSection = new VBox(15);
        productSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label productTitle = createSubheading("Add Products to Invoice");
        productTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Product selection grid
        GridPane productGrid = new GridPane();
        productGrid.setHgap(15);
        productGrid.setVgap(15);
        productGrid.setAlignment(Pos.TOP_LEFT);
        
        // Product dropdown with better styling
        ComboBox<String> productComboBox = new ComboBox<>();
        productComboBox.setPromptText("-- Select Product --");
        productComboBox.setEditable(false);
        productComboBox.setPrefWidth(250);
        
        // Load production stock items with error handling
        final List<Object[]> products = new ArrayList<>();
        try {
            products.addAll(database.getAllProductionStocksWithPriceForDropdown());
            ObservableList<String> productNames = FXCollections.observableArrayList();
            for (Object[] product : products) {
                String displayName = String.format("%s (Stock: %d)", 
                    product[1], // product_name
                    ((Number) product[3]).intValue() // quantity available
                );
                productNames.add(displayName);
            }
            productComboBox.setItems(productNames);
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load products: " + e.getMessage());
        }
        
        TextField quantityField = createTextField("1");
        quantityField.setPromptText("Enter Quantity");
        quantityField.setPrefWidth(120);
        
        TextField priceField = createTextField("0.00");
        priceField.setPromptText("Unit Price");
        priceField.setPrefWidth(120);
        priceField.setEditable(false);
        priceField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
        
        TextField stockAvailableField = createTextField("0");
        stockAvailableField.setPromptText("Available Stock");
        stockAvailableField.setPrefWidth(120);
        stockAvailableField.setEditable(false);
        stockAvailableField.setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #28a745;");
        
        // Auto-fill price and stock when product is selected
        productComboBox.setOnAction(e -> {
            String selectedDisplay = productComboBox.getValue();
            if (selectedDisplay != null) {
                // Extract product name from display (remove stock info)
                String productName = selectedDisplay.split(" \\(Stock:")[0];
                
                for (Object[] product : products) {
                    if (productName.equals(product[1])) {
                        double salePrice = ((Number) product[2]).doubleValue();
                        int availableStock = ((Number) product[3]).intValue();
                        
                        priceField.setText(String.format("%.2f", salePrice));
                        stockAvailableField.setText(String.valueOf(availableStock));
                        
                        // Update stock field color based on availability
                        if (availableStock > 10) {
                            stockAvailableField.setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #28a745;");
                        } else if (availableStock > 0) {
                            stockAvailableField.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffc107;");
                        } else {
                            stockAvailableField.setStyle("-fx-background-color: #f8d7da; -fx-border-color: #dc3545;");
                        }
                        break;
                    }
                }
            } else {
                priceField.clear();
                stockAvailableField.clear();
                stockAvailableField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
            }
        });
        
        // Action buttons for adding items
        Button addItemBtn = createActionButton("Add to Invoice");
        addItemBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button clearSelectionBtn = createActionButton("Clear Selection");
        clearSelectionBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        
        HBox itemActionButtons = new HBox(10);
        itemActionButtons.getChildren().addAll(addItemBtn, clearSelectionBtn);
        
        // Add to product grid
        productGrid.add(createFormRow("Product:", productComboBox), 0, 0);
        productGrid.add(createFormRow("Quantity:", quantityField), 1, 0);
        productGrid.add(createFormRow("Unit Price:", priceField), 0, 1);
        productGrid.add(createFormRow("Available Stock:", stockAvailableField), 1, 1);
        productGrid.add(itemActionButtons, 0, 2, 2, 1);
        
        productSection.getChildren().addAll(productTitle, productGrid);

        // === INVOICE ITEMS TABLE SECTION ===
        VBox tableSection = new VBox(15);
        tableSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label tableTitle = createSubheading("Invoice Items");
        tableTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Invoice items table with improved columns
        TableView<SalesInvoiceItemUI> itemsTable = new TableView<>();
        itemsTable.setPrefHeight(250);
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<SalesInvoiceItemUI, String> productCol = new TableColumn<>("Product Name");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productCol.setPrefWidth(250);
        
        TableColumn<SalesInvoiceItemUI, Double> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(100);
        quantityCol.setCellFactory(col -> new TableCell<SalesInvoiceItemUI, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f", item));
                }
            }
        });
        
        TableColumn<SalesInvoiceItemUI, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(col -> new TableCell<SalesInvoiceItemUI, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        TableColumn<SalesInvoiceItemUI, Double> totalCol = new TableColumn<>("Line Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalCol.setPrefWidth(120);
        totalCol.setCellFactory(col -> new TableCell<SalesInvoiceItemUI, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
                }
            }
        });
        
        itemsTable.getColumns().addAll(productCol, quantityCol, priceCol, totalCol);
        
        ObservableList<SalesInvoiceItemUI> invoiceItems = FXCollections.observableArrayList();
        itemsTable.setItems(invoiceItems);
        
        // Table action buttons
        Button removeItemBtn = createActionButton("Remove Selected Item");
        removeItemBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        
        Button clearAllItemsBtn = createActionButton("Clear All Items");
        clearAllItemsBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        
        HBox tableActionButtons = new HBox(10);
        tableActionButtons.getChildren().addAll(removeItemBtn, clearAllItemsBtn);
        
        tableSection.getChildren().addAll(tableTitle, itemsTable, tableActionButtons);

        // === PAYMENT SECTION ===
        VBox paymentSection = new VBox(15);
        paymentSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label paymentTitle = createSubheading("Payment Information");
        paymentTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        GridPane paymentGrid = new GridPane();
        paymentGrid.setHgap(20);
        paymentGrid.setVgap(15);
        paymentGrid.setAlignment(Pos.TOP_LEFT);
        
        TextField discountField = createTextField("0.00");
        discountField.setPromptText("Discount Amount");
        discountField.setPrefWidth(150);
        
        TextField paidAmountField = createTextField("0.00");
        paidAmountField.setPromptText("Amount Paid");
        paidAmountField.setPrefWidth(150);
        
        // Summary labels
        Label subtotalLabel = new Label("Subtotal: 0.00");
        subtotalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label discountLabel = new Label("Discount: 0.00");
        discountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc3545;");
        
        Label totalAmountLabel = new Label("Total Amount: 0.00");
        totalAmountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
        
        Label balanceLabel = new Label("Balance Due: 0.00");
        balanceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
        
        // Update balance when discount or paid amount changes
        discountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
        });
        
        paidAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
        });
        
        paymentGrid.add(createFormRow("Discount:", discountField), 0, 0);
        paymentGrid.add(createFormRow("Paid Amount:", paidAmountField), 1, 0);
        paymentGrid.add(subtotalLabel, 0, 1);
        paymentGrid.add(discountLabel, 1, 1);
        paymentGrid.add(totalAmountLabel, 0, 2, 2, 1);
        paymentGrid.add(balanceLabel, 0, 3, 2, 1);
        
        paymentSection.getChildren().addAll(paymentTitle, paymentGrid);

        // === ACTION BUTTONS ===
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(20, 0, 0, 0));
        
        Button submitBtn = createSubmitButton("Submit & Print Invoice");
        submitBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 24;");
        submitBtn.setPrefWidth(200);
        
        Button resetFormBtn = createActionButton("Reset Form");
        resetFormBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        resetFormBtn.setPrefWidth(150);
        
        actionButtons.getChildren().addAll(submitBtn, resetFormBtn);

        // Add all sections to scrollable content
        scrollableContent.getChildren().addAll(
            headerSection,
            productSection,  
            tableSection,
            paymentSection,
            actionButtons
        );
        
        // Create ScrollPane for the form content
        ScrollPane scrollPane = new ScrollPane(scrollableContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(600);
        scrollPane.setMaxHeight(600);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        // Add heading and scrollable content to main form
        form.getChildren().addAll(heading, scrollPane);

        // === EVENT HANDLERS ===
        
        // Clear selection button
        clearSelectionBtn.setOnAction(e -> {
            productComboBox.setValue(null);
            quantityField.setText("1");
            priceField.clear();
            stockAvailableField.clear();
            stockAvailableField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
        });
        
        // Add item to invoice button
        addItemBtn.setOnAction(e -> {
            String selectedDisplay = productComboBox.getValue();
            String quantityText = quantityField.getText().trim();
            String priceText = priceField.getText().trim();
            String stockText = stockAvailableField.getText().trim();
            
            // Validation
            if (selectedDisplay == null) {
                showAlert("Missing Information", "Please select a product");
                return;
            }
            
            if (quantityText.isEmpty()) {
                showAlert("Missing Information", "Please enter quantity");
                return;
            }
            
            if (priceText.isEmpty()) {
                showAlert("Missing Information", "Price not loaded. Please select product again");
                return;
            }
            
            try {
                double qty = Double.parseDouble(quantityText);
                double price = Double.parseDouble(priceText);
                int availableStock = stockText.isEmpty() ? 0 : Integer.parseInt(stockText);
                
                if (qty <= 0) {
                    showAlert("Invalid Input", "Quantity must be greater than 0");
                    return;
                }
                
                if (price <= 0) {
                    showAlert("Invalid Input", "Price must be greater than 0");
                    return;
                }
                
                if (qty > availableStock) {
                    showAlert("Insufficient Stock", 
                        String.format("Requested quantity (%.1f) exceeds available stock (%d)", qty, availableStock));
                    return;
                }
                
                // Extract product name from display
                String productName = selectedDisplay.split(" \\(Stock:")[0];
                
                // Check if product already exists in table
                boolean productExists = false;
                for (SalesInvoiceItemUI item : invoiceItems) {
                    if (item.getProductName().equals(productName)) {
                        double newQty = item.getQuantity() + qty;
                        if (newQty > availableStock) {
                            showAlert("Insufficient Stock", 
                                String.format("Total quantity (%.1f) would exceed available stock (%d)", newQty, availableStock));
                            return;
                        }
                        item.setQuantity(newQty);
                        productExists = true;
                        break;
                    }
                }
                
                if (!productExists) {
                    SalesInvoiceItemUI newItem = new SalesInvoiceItemUI(productName, qty, price);
                    invoiceItems.add(newItem);
                }
                
                itemsTable.refresh();
                updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                    subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
                
                // Clear selection after adding
                productComboBox.setValue(null);
                quantityField.setText("1");
                priceField.clear();
                stockAvailableField.clear();
                stockAvailableField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numbers for quantity and price");
            }
        });
        
        // Remove selected item button
        removeItemBtn.setOnAction(e -> {
            SalesInvoiceItemUI selectedItem = itemsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                invoiceItems.remove(selectedItem);
                updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                    subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
            } else {
                showAlert("No Selection", "Please select an item to remove from the table");
            }
        });
        
        // Clear all items button
        clearAllItemsBtn.setOnAction(e -> {
            if (!invoiceItems.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all invoice items?");
                alert.setContentText("This will remove all products from the invoice.");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    invoiceItems.clear();
                    updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                        subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
                }
            }
        });
        
        // Reset form button
        resetFormBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Reset");
            alert.setHeaderText("Reset entire form?");
            alert.setContentText("This will clear all data and generate a new invoice number.");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                // Generate new invoice number
                String newInvoiceNumber = database.generateSalesInvoiceNumber();
                invoiceNumberField.setText(newInvoiceNumber);
                
                // Reset all fields
                salesDatePicker.setValue(LocalDate.now());
                customerComboBox.setValue(null);
                productComboBox.setValue(null);
                quantityField.setText("1");
                priceField.clear();
                stockAvailableField.clear();
                stockAvailableField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
                discountField.setText("0.00");
                paidAmountField.setText("0.00");
                
                // Clear items
                invoiceItems.clear();
                updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                    subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
            }
        });
        
        // Submit invoice button
        submitBtn.setOnAction(e -> {
            String invoiceNumber = invoiceNumberField.getText().trim();
            String customer = customerComboBox.getValue();
            String date = salesDatePicker.getValue().format(DATE_FORMATTER);
            String discountText = discountField.getText().trim();
            String paidAmountText = paidAmountField.getText().trim();
            
            // Validation
            if (customer == null || customer.isEmpty()) {
                showAlert("Missing Information", "Please select a customer");
                return;
            }
            
            if (invoiceItems.isEmpty()) {
                showAlert("Missing Information", "Please add at least one item to the invoice");
                return;
            }
            
            try {
                double discount = discountText.isEmpty() ? 0.0 : Double.parseDouble(discountText);
                double paidAmount = paidAmountText.isEmpty() ? 0.0 : Double.parseDouble(paidAmountText);
                
                if (discount < 0 || paidAmount < 0) {
                    showAlert("Invalid Input", "Discount and paid amount cannot be negative");
                    return;
                }
                
                // Calculate total amount
                double subtotal = invoiceItems.stream()
                    .mapToDouble(SalesInvoiceItemUI::getTotalPrice)
                    .sum();
                double totalAmount = subtotal - discount;
                
                if (totalAmount < 0) {
                    showAlert("Invalid Input", "Discount cannot exceed subtotal");
                    return;
                }
                
                // Get customer ID
                int customerId = database.getCustomerIdByName(customer);
                if (customerId == -1) {
                    showAlert("Database Error", "Customer not found in database");
                    return;
                }
                
                // Prepare invoice items for database
                List<Object[]> items = new ArrayList<>();
                for (SalesInvoiceItemUI item : invoiceItems) {
                    int productId = database.getProductionStockIdByName(item.getProductName());
                    if (productId == -1) {
                        showAlert("Database Error", "Product '" + item.getProductName() + "' not found in database");
                        return;
                    }
                    items.add(new Object[]{productId, item.getQuantity(), item.getUnitPrice()});
                }
                
                // Show confirmation with invoice summary
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Invoice Creation");
                confirmAlert.setHeaderText("Create Sales Invoice?");
                confirmAlert.setContentText(String.format(
                    "Invoice: %s\nCustomer: %s\nSubtotal: %.2f\nDiscount: %.2f\nTotal: %.2f\nPaid: %.2f\nBalance: %.2f",
                    invoiceNumber, customer, subtotal, discount, totalAmount, paidAmount, totalAmount - paidAmount
                ));
                
                if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                    // Save to database
                    boolean success = database.insertSalesInvoice(invoiceNumber, customerId, date, 
                        totalAmount, discount, paidAmount, items);
                    
                    if (success) {
                        // Prepare invoice data for printing
                        List<Item> printItems = new ArrayList<>();
                        for (SalesInvoiceItemUI item : invoiceItems) {
                            printItems.add(new Item(
                                item.getProductName(),
                                (int) item.getQuantity(),
                                item.getUnitPrice(),
                                0.0 // discount percent (global discount handled separately)
                            ));
                        }
                        
                        // Get customer address (you may want to enhance this to get actual address from database)
                        String customerAddress = "Customer Address"; // Default placeholder
                        try {
                            // Try to get actual customer address from database if available
                            // Note: This method may not exist in the current db interface
                            // You can implement it in SQLiteDatabase class if needed
                            customerAddress = "Customer Address - " + customer;
                        } catch (Exception addressEx) {
                            // Use default if error occurs
                            System.out.println("Could not retrieve customer address, using default");
                        }
                        
                        InvoiceData invoiceData = new InvoiceData(
                            invoiceNumber,
                            date,
                            customer,
                            customerAddress,
                            0.0, // previous balance (you may want to implement this)
                            printItems
                        );
                        
                        // Open invoice for print preview (like Ctrl+P behavior)
                        boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Sales");
                        
                        if (previewSuccess) {
                            showAlert("Success", "Sales invoice created successfully!\n\nInvoice Number: " + invoiceNumber + "\n\nThe invoice has been opened for preview and printing.");
                        } else {
                            // Fallback to printer selection if preview fails
                            boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Sales");
                            if (printSuccess) {
                                showAlert("Success", "Sales invoice created and printed successfully!\n\nInvoice Number: " + invoiceNumber);
                            } else {
                                showAlert("Partial Success", "Sales invoice created successfully but printing failed.\n\nInvoice Number: " + invoiceNumber);
                            }
                        }
                        
                        // Reset form for next invoice
                        String newInvoiceNumber = database.generateSalesInvoiceNumber();
                        invoiceNumberField.setText(newInvoiceNumber);
                        salesDatePicker.setValue(LocalDate.now());
                        customerComboBox.setValue(null);
                        discountField.setText("0.00");
                        paidAmountField.setText("0.00");
                        invoiceItems.clear();
                        updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                            subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
                    } else {
                        showAlert("Database Error", "Failed to create sales invoice. Please check the database connection and try again.");
                    }
                }
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numbers for discount and paid amount");
            } catch (Exception ex) {
                showAlert("Unexpected Error", "An error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        // Print Last Invoice button
        // Print Last Invoice button removed

        return form;
    }

    private static ScrollPane createReturnSalesInvoiceForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Return Sales Invoice");

        // Return invoice header fields
        TextField returnInvoiceNumberField = createTextField("Auto-generated");
        returnInvoiceNumberField.setEditable(false);
        returnInvoiceNumberField.setStyle("-fx-background-color: #f0f0f0;");
        
        // Auto-generate return invoice number
        String autoReturnInvoiceNumber = database.generateSalesReturnInvoiceNumber();
        returnInvoiceNumberField.setText(autoReturnInvoiceNumber);
        
        // Original invoice dropdown
        ComboBox<String> originalInvoiceComboBox = new ComboBox<>();
        originalInvoiceComboBox.setPromptText("Select Original Invoice");
        originalInvoiceComboBox.setEditable(false);
        originalInvoiceComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Load sales invoices
        List<Object[]> salesInvoices = database.getAllSalesInvoicesForDropdown();
        ObservableList<String> invoiceNumbers = FXCollections.observableArrayList();
        for (Object[] invoice : salesInvoices) {
            String displayText = String.format("%s - %s (%s)", 
                invoice[1], // invoice_number
                invoice[2], // customer_name
                invoice[3]  // sales_date
            );
            invoiceNumbers.add(displayText);
        }
        originalInvoiceComboBox.setItems(invoiceNumbers);
        
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(LocalDate.now());
        
        TextField customerField = createTextField("Customer");
        customerField.setEditable(false);
        customerField.setStyle("-fx-background-color: #f0f0f0;");
        
        TextField returnAmountField = createTextField("Return Amount");
        returnAmountField.setEditable(false);
        returnAmountField.setStyle("-fx-background-color: #f0f0f0;");
        
        // Return items table
        TableView<SalesInvoiceItemUI> returnItemsTable = new TableView<>();
        returnItemsTable.setPrefHeight(250);
        returnItemsTable.getStyleClass().add("table-view");
        returnItemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<SalesInvoiceItemUI, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productCol.prefWidthProperty().bind(returnItemsTable.widthProperty().multiply(0.20));
        
        TableColumn<SalesInvoiceItemUI, Double> originalQtyCol = new TableColumn<>("Original Qty");
        originalQtyCol.setCellValueFactory(new PropertyValueFactory<>("originalQuantity"));
        originalQtyCol.prefWidthProperty().bind(returnItemsTable.widthProperty().multiply(0.20));
        
        TableColumn<SalesInvoiceItemUI, Double> returnQtyCol = new TableColumn<>("Return Qty");
        returnQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        returnQtyCol.prefWidthProperty().bind(returnItemsTable.widthProperty().multiply(0.20));
        
        TableColumn<SalesInvoiceItemUI, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.prefWidthProperty().bind(returnItemsTable.widthProperty().multiply(0.20));
        
        TableColumn<SalesInvoiceItemUI, Double> totalCol = new TableColumn<>("Total Amount");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalCol.prefWidthProperty().bind(returnItemsTable.widthProperty().multiply(0.20));
        
        returnItemsTable.getColumns().addAll(productCol, originalQtyCol, returnQtyCol, priceCol, totalCol);
        
        ObservableList<SalesInvoiceItemUI> returnItems = FXCollections.observableArrayList();
        returnItemsTable.setItems(returnItems);
        
        // Wrap return items table in ScrollPane
        ScrollPane returnItemsScrollPane = new ScrollPane(returnItemsTable);
        returnItemsScrollPane.setFitToWidth(true);
        returnItemsScrollPane.setFitToHeight(true);
        returnItemsScrollPane.setPrefHeight(250);
        returnItemsScrollPane.setMaxHeight(250);
        returnItemsScrollPane.getStyleClass().addAll("scroll-pane", "custom-scroll");
        
        // Available items table (from original invoice)
        TableView<SalesInvoiceItemUI> availableItemsTable = new TableView<>();
        availableItemsTable.setPrefHeight(200);
        availableItemsTable.getStyleClass().add("table-view");
        
        TableColumn<SalesInvoiceItemUI, String> availableProductCol = new TableColumn<>("Product Name");
        availableProductCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        availableProductCol.setPrefWidth(250);
        
        TableColumn<SalesInvoiceItemUI, Double> availableQtyCol = new TableColumn<>("Available Quantity");
        availableQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        availableQtyCol.setPrefWidth(140);
        
        TableColumn<SalesInvoiceItemUI, Double> availablePriceCol = new TableColumn<>("Unit Price");
        availablePriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        availablePriceCol.setPrefWidth(120);
        
        availableItemsTable.getColumns().addAll(availableProductCol, availableQtyCol, availablePriceCol);
        
        ObservableList<SalesInvoiceItemUI> availableItems = FXCollections.observableArrayList();
        availableItemsTable.setItems(availableItems);
        
        // Wrap available items table in ScrollPane
        ScrollPane availableItemsScrollPane = new ScrollPane(availableItemsTable);
        availableItemsScrollPane.setFitToWidth(true);
        availableItemsScrollPane.setFitToHeight(true);
        availableItemsScrollPane.setPrefHeight(200);
        availableItemsScrollPane.setMaxHeight(200);
        availableItemsScrollPane.getStyleClass().addAll("scroll-pane", "custom-scroll");
        
        // Add return item controls
        VBox addReturnSection = new VBox(10);
        addReturnSection.setPadding(new Insets(15, 0, 15, 0));
        addReturnSection.getStyleClass().add("form-container");
        
        HBox addReturnItemBox = new HBox(15);
        addReturnItemBox.setAlignment(Pos.CENTER_LEFT);
        addReturnItemBox.setPadding(new Insets(10));
        addReturnItemBox.getStyleClass().add("form-row");
        
        Label returnQtyLabel = new Label("Return Quantity:");
        returnQtyLabel.getStyleClass().add("form-label");
        
        TextField returnQuantityField = createTextField("Return Quantity");
        returnQuantityField.setPrefWidth(150);
        
        Button addReturnItemBtn = createActionButton("Add to Return");
        addReturnItemBtn.setPrefWidth(120);
        
        Button removeReturnItemBtn = createActionButton("Remove Item");
        removeReturnItemBtn.setPrefWidth(120);
        
        addReturnItemBox.getChildren().addAll(
            returnQtyLabel, returnQuantityField,
            addReturnItemBtn, removeReturnItemBtn
        );
        
        addReturnSection.getChildren().add(addReturnItemBox);
        
        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(20, 0, 10, 0));
        actionButtons.getStyleClass().add("form-row");
        
        Button submitBtn = createSubmitButton("Submit & Print Return");
        submitBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        
        Button clearBtn = createActionButton("Clear All");
        clearBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        
        actionButtons.getChildren().addAll(submitBtn, clearBtn);

        // Form layout with proper spacing
        VBox formContent = new VBox(20);
        formContent.getChildren().addAll(
            heading,
            new VBox(10, 
                createFormRow("Return Invoice Number:", returnInvoiceNumberField),
                createFormRow("Original Invoice:", originalInvoiceComboBox),
                createFormRow("Return Date:", returnDatePicker),
                createFormRow("Customer:", customerField),
                createFormRow("Return Amount:", returnAmountField)
            ),
            new VBox(15,
                createSubheading("Available Items from Original Invoice:"),
                availableItemsScrollPane,
                addReturnSection,
                createSubheading("Return Items:"),
                returnItemsScrollPane
            ),
            actionButtons
        );
        
        form.getChildren().add(formContent);
        
        // Create main ScrollPane for the entire form
        ScrollPane mainScrollPane = new ScrollPane(form);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(false);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.getStyleClass().addAll("scroll-pane", "custom-scroll");

        // Event handlers
        originalInvoiceComboBox.setOnAction(e -> {
            String selectedDisplay = originalInvoiceComboBox.getValue();
            if (selectedDisplay != null) {
                // Extract invoice number from display text
                String invoiceNumber = selectedDisplay.split(" - ")[0];
                
                // Find the selected invoice data
                for (Object[] invoice : salesInvoices) {
                    if (invoiceNumber.equals(invoice[1])) {
                        int salesInvoiceId = (Integer) invoice[0];
                        
                        // Get invoice details
                        Object[] invoiceData = database.getSalesInvoiceById(salesInvoiceId);
                        if (invoiceData != null) {
                            customerField.setText((String) invoiceData[3]); // customer_name
                        }
                        
                        // Load invoice items
                        List<Object[]> originalItems = database.getSalesInvoiceItemsByInvoiceId(salesInvoiceId);
                        availableItems.clear();
                        
                        for (Object[] item : originalItems) {
                            SalesInvoiceItemUI itemUI = new SalesInvoiceItemUI(
                                (String) item[1], // product_name
                                (Double) item[2], // quantity
                                (Double) item[3]  // unit_price
                            );
                            itemUI.setProductionStockId((Integer) item[0]);
                            availableItems.add(itemUI);
                        }
                        break;
                    }
                }
                
                // Clear return items when original invoice changes
                returnItems.clear();
                updateReturnAmount(returnItems, returnAmountField);
            }
        });
        
        addReturnItemBtn.setOnAction(e -> {
            SalesInvoiceItemUI selectedItem = availableItemsTable.getSelectionModel().getSelectedItem();
            String returnQtyText = returnQuantityField.getText().trim();
            
            if (selectedItem == null) {
                showAlert("No Selection", "Please select an item from the available items table");
                return;
            }
            
            if (returnQtyText.isEmpty()) {
                showAlert("Missing Information", "Please enter return quantity");
                return;
            }
            
            try {
                double returnQty = Double.parseDouble(returnQtyText);
                
                if (returnQty <= 0) {
                    showAlert("Invalid Input", "Return quantity must be positive");
                    return;
                }
                
                if (returnQty > selectedItem.getQuantity()) {
                    showAlert("Invalid Input", "Return quantity cannot exceed original quantity");
                    return;
                }
                
                // Check if item already exists in return items
                boolean itemExists = false;
                for (SalesInvoiceItemUI returnItem : returnItems) {
                    if (returnItem.getProductName().equals(selectedItem.getProductName())) {
                        double newReturnQty = returnItem.getQuantity() + returnQty;
                        if (newReturnQty > selectedItem.getQuantity()) {
                            showAlert("Invalid Input", 
                                String.format("Total return quantity (%.2f) cannot exceed original quantity (%.2f)", 
                                    newReturnQty, selectedItem.getQuantity()));
                            return;
                        }
                        returnItem.setQuantity(newReturnQty);
                        itemExists = true;
                        break;
                    }
                }
                
                if (!itemExists) {
                    SalesInvoiceItemUI returnItem = new SalesInvoiceItemUI(
                        selectedItem.getProductName(), returnQty, selectedItem.getUnitPrice());
                    returnItem.setProductionStockId(selectedItem.getProductionStockId());
                    returnItem.setOriginalQuantity(selectedItem.getQuantity());
                    returnItems.add(returnItem);
                }
                
                returnItemsTable.refresh();
                updateReturnAmount(returnItems, returnAmountField);
                returnQuantityField.clear();
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid number for return quantity");
            }
        });
        
        removeReturnItemBtn.setOnAction(e -> {
            SalesInvoiceItemUI selectedItem = returnItemsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                returnItems.remove(selectedItem);
                updateReturnAmount(returnItems, returnAmountField);
            } else {
                showAlert("No Selection", "Please select an item to remove from return items");
            }
        });
        
        clearBtn.setOnAction(e -> {
            if (!returnItems.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all return items?");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    returnItems.clear();
                    updateReturnAmount(returnItems, returnAmountField);
                }
            }
        });
        
        // Print Return button removed
        
        submitBtn.setOnAction(e -> {
            String returnInvoiceNumber = returnInvoiceNumberField.getText().trim();
            String selectedDisplay = originalInvoiceComboBox.getValue();
            String customer = customerField.getText().trim();
            String date = returnDatePicker.getValue().format(DATE_FORMATTER);
            String returnAmountText = returnAmountField.getText().trim();
            
            if (selectedDisplay == null || customer.isEmpty() || returnItems.isEmpty()) {
                showAlert("Missing Information", "Please select original invoice and add at least one return item");
                return;
            }
            
            try {
                double totalReturnAmount = Double.parseDouble(returnAmountText);
                
                // Get original invoice data
                String originalInvoiceNumber = selectedDisplay.split(" - ")[0];
                int originalSalesInvoiceId = -1;
                int customerId = -1;
                
                for (Object[] invoice : salesInvoices) {
                    if (originalInvoiceNumber.equals(invoice[1])) {
                        originalSalesInvoiceId = (Integer) invoice[0];
                        break;
                    }
                }
                
                customerId = database.getCustomerIdByName(customer);
                
                if (originalSalesInvoiceId == -1 || customerId == -1) {
                    showAlert("Error", "Original invoice or customer not found");
                    return;
                }
                
                // Prepare return items for database
                List<Object[]> items = new ArrayList<>();
                for (SalesInvoiceItemUI item : returnItems) {
                    items.add(new Object[]{item.getProductionStockId(), item.getQuantity(), item.getUnitPrice()});
                }
                
                // Save to database
                boolean success = database.insertSalesReturnInvoice(returnInvoiceNumber, originalSalesInvoiceId, 
                    customerId, date, totalReturnAmount, items);
                
                if (success) {
                    // Prepare invoice data for printing
                    List<Item> printItems = new ArrayList<>();
                    for (SalesInvoiceItemUI item : returnItems) {
                        printItems.add(new Item(
                            item.getProductName(),
                            (int) item.getQuantity(),
                            item.getUnitPrice(),
                            0.0 // discount percent for returns is 0
                        ));
                    }
                    
                    // Create invoice data for printing
                    String customerAddress = "Customer Address - " + customer; // You may enhance this to get actual address
                    InvoiceData invoiceData = new InvoiceData(
                        returnInvoiceNumber,
                        date,
                        customer,
                        customerAddress,
                        0.0, // previous balance
                        printItems
                    );
                    
                    // Open invoice for print preview
                    boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Sales Return");
                    
                    if (previewSuccess) {
                        showAlert("Success", "Sales return invoice created successfully!\n\nReturn Invoice Number: " + returnInvoiceNumber + 
                                "\n\nThe return invoice has been opened for preview and printing.");
                    } else {
                        // Fallback to printer selection if preview fails
                        boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Sales Return");
                        if (printSuccess) {
                            showAlert("Success", "Sales return invoice created and printed successfully!\n\nReturn Invoice Number: " + returnInvoiceNumber);
                        } else {
                            showAlert("Partial Success", "Sales return invoice created successfully but printing failed.\n\nReturn Invoice Number: " + returnInvoiceNumber);
                        }
                    }
                    
                    // Clear form and generate new return invoice number
                    String newReturnInvoiceNumber = database.generateSalesReturnInvoiceNumber();
                    returnInvoiceNumberField.setText(newReturnInvoiceNumber);
                    originalInvoiceComboBox.setValue(null);
                    returnDatePicker.setValue(LocalDate.now());
                    customerField.clear();
                    returnAmountField.clear();
                    availableItems.clear();
                    returnItems.clear();
                } else {
                    showAlert("Error", "Failed to create sales return invoice.\nThis could be due to database error or stock update failure. Please try again.");
                }
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Invalid return amount");
            }
        });

        return mainScrollPane;
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
            TextField quantityField, TextField unitCostField, TextField salePriceField,
            TableView<ProductionStockRecord> stockTable,
            Label totalItemsLabel, Label totalValueLabel, Label lowStockLabel) {
        
        String name = nameField.getText().trim();
        String brand = brandCombo.getSelectionModel().getSelectedItem();
        String quantityText = quantityField.getText().trim();
        String unitCostText = unitCostField.getText().trim();
        String salePriceText = salePriceField.getText().trim();
        
        // Validation
        if (name.isEmpty()) {
            showAlert("Missing Information", "Please enter a product name.");
            nameField.requestFocus();
            return;
        }
        
        if (brand == null || brand.isEmpty()) {
            showAlert("Missing Information", "Please select a brand.");
            brandCombo.requestFocus();
            return;
        }
        
        if (quantityText.isEmpty()) {
            showAlert("Missing Information", "Please enter quantity.");
            quantityField.requestFocus();
            return;
        }
        
        if (unitCostText.isEmpty()) {
            showAlert("Missing Information", "Please enter unit cost.");
            unitCostField.requestFocus();
            return;
        }
        
        if (salePriceText.isEmpty()) {
            showAlert("Missing Information", "Please enter sale price.");
            salePriceField.requestFocus();
            return;
        }
        
        try {
            int quantity = Integer.parseInt(quantityText);
            double unitCost = Double.parseDouble(unitCostText);
            double salePrice = Double.parseDouble(salePriceText);
            
            if (quantity <= 0) {
                showAlert("Invalid Input", "Quantity must be greater than 0.");
                quantityField.requestFocus();
                return;
            }
            
            if (unitCost <= 0) {
                showAlert("Invalid Input", "Unit cost must be greater than 0.");
                unitCostField.requestFocus();
                return;
            }
            
            if (salePrice <= 0) {
                showAlert("Invalid Input", "Sale price must be greater than 0.");
                salePriceField.requestFocus();
                return;
            }
            
            if (salePrice <= unitCost) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Price Warning");
                alert.setHeaderText("Sale price is less than or equal to unit cost");
                alert.setContentText(String.format("Sale Price: %.2f\nUnit Cost: %.2f\n\nThis will result in no profit or a loss. Do you want to continue?", salePrice, unitCost));
                
                if (alert.showAndWait().get() != ButtonType.OK) {
                    salePriceField.requestFocus();
                    return;
                }
            }
            
            // Check if product with same name and brand already exists
            if (database.productionStockExists(name, brand)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Product Exists");
                alert.setHeaderText("Product already exists");
                alert.setContentText("A product with the name '" + name + "' and brand '" + brand + "' already exists.\n\nDo you want to add this quantity to existing stock?");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    // Add to existing stock
                    boolean success = database.addToProductionStock(name, brand, quantity, unitCost, salePrice);
                    if (success) {
                        showAlert("Success", "Quantity added to existing production stock successfully!");
                    } else {
                        showAlert("Error", "Failed to add quantity to existing stock.");
                        return;
                    }
                } else {
                    return; // User cancelled
                }
            } else {
                // Insert new production stock - using the new overloaded method
                boolean success = database.insertProductionStock(name, "", brand, "", quantity, unitCost, salePrice, 0.0);
                
                if (!success) {
                    showAlert("Error", "Failed to register production stock. Please check database connection.");
                    return;
                }
                
                showAlert("Success", String.format("Production Stock registered successfully!\n\nProduct: %s\nBrand: %s\nQuantity: %d\nUnit Cost: %.2f\nSale Price: %.2f\nProfit Margin: %.1f%%", 
                    name, brand, quantity, unitCost, salePrice, ((salePrice - unitCost) / unitCost) * 100));
            }
            
            // Clear form after successful submission
            nameField.clear();
            brandCombo.getSelectionModel().clearSelection();
            quantityField.clear();
            unitCostField.clear();
            salePriceField.clear();
            nameField.requestFocus();
            
            // Refresh table and summary
            refreshProductionStockTable(stockTable);
            updateStockSummary(stockTable, totalItemsLabel, totalValueLabel, lowStockLabel);
            
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter valid numbers for Quantity, Unit Cost, and Sale Price.\n\nQuantity should be a whole number.\nUnit Cost and Sale Price should be decimal numbers.");
        } catch (Exception ex) {
            showAlert("Unexpected Error", "An error occurred while registering the product: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static TableView<ProductionStockRecord> createProductionStockTable() {
        TableView<ProductionStockRecord> table = new TableView<>();
        table.setPrefHeight(300);
        table.setMaxHeight(300);
        table.getStyleClass().add("table-view");
        
        TableColumn<ProductionStockRecord, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(150);
        
        TableColumn<ProductionStockRecord, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrand()));
        brandCol.setPrefWidth(120);
        
        TableColumn<ProductionStockRecord, String> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        quantityCol.setPrefWidth(100);
        
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
        
        try {
            // Get all production stocks from database
            List<Object[]> stockList = database.getAllProductionStocks();
            for (Object[] stock : stockList) {
                data.add(new ProductionStockRecord(
                    (Integer) stock[0],   // production_id
                    (String) stock[1],    // product_name
                    (String) stock[2],    // product_description
                    (String) stock[3],    // brand_name
                    (String) stock[4],    // brand_description
                    (Integer) stock[5],   // quantity
                    (Double) stock[6],    // unit_cost
                    (Double) stock[7]     // sale_price
                ));
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to refresh production stock table: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Failed to load production stock data: " + e.getMessage());
        }
        
        table.setItems(data);
    }

    // Simple record class for table display
    private static class ProductionStockRecord {
        private final int productionId;
        private final String name;
        private final String description;
        private final String brand;
        private final String brandDescription;
        private final int quantity;
        private final double unitCost;
        private final double salePrice;
        
        // Original constructor for backward compatibility
        public ProductionStockRecord(String name, String brand, int quantity, double unitCost, double totalCost) {
            this.productionId = 0;
            this.name = name;
            this.description = "";
            this.brand = brand;
            this.brandDescription = "";
            this.quantity = quantity;
            this.unitCost = unitCost;
            this.salePrice = totalCost; // Use totalCost as salePrice for backward compatibility
        }
        
        // New comprehensive constructor
        public ProductionStockRecord(int productionId, String name, String description, String brand, 
                                   String brandDescription, int quantity, double unitCost, double salePrice) {
            this.productionId = productionId;
            this.name = name;
            this.description = description;
            this.brand = brand;
            this.brandDescription = brandDescription;
            this.quantity = quantity;
            this.unitCost = unitCost;
            this.salePrice = salePrice;
        }
        
        // Getters
        public int getProductionId() { return productionId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getBrand() { return brand; }
        public String getBrandDescription() { return brandDescription; }
        public int getQuantity() { return quantity; }
        public double getUnitCost() { return unitCost; }
        public double getSalePrice() { return salePrice; }
        
        // Backward compatibility methods
        public double getTotalCost() { return quantity * unitCost; }
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
                    } else {
                        showAlert("Error", "Production stock not found for product: " + productName);
                        return;
                    }
                }
            }
            
            if (productionItems.isEmpty()) {
                showAlert("Error", "No valid production items found");
                return;
            }
            
            // Insert production invoice items and update stock quantities
            if (!database.insertProductionInvoiceItems(invoiceId, productionItems)) {
                showAlert("Error", "Failed to save production items and update stock quantities");
                return;
            }

            // Prepare items for printing
            List<Item> printItems = new ArrayList<>();
            for (String item : itemsList.getItems()) {
                String[] parts = item.split(" - Quantity: ");
                if (parts.length == 2) {
                    String productName = parts[0];
                    double quantity = Double.parseDouble(parts[1]);
                    printItems.add(new Item(
                        productName,
                        (int) quantity,
                        0.0, // unit price not applicable for production
                        0.0  // discount not applicable for production
                    ));
                }
            }

            // Create invoice data for printing with proper type and metadata
            InvoiceData invoiceData = new InvoiceData(
                InvoiceData.TYPE_PRODUCTION,
                invoiceNumberField.getText(),
                productionDate,
                "PRODUCTION INVOICE",
                "", // Empty address field
                printItems,
                0.0 // not applicable for production
            );
            
            // Add reference/notes as metadata
            invoiceData.setMetadata("tehsil", "");
            invoiceData.setMetadata("contact", "");
            invoiceData.setMetadata("notes", notes);
            
            // Open invoice for print preview
            boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Production");
            
            if (previewSuccess) {
                showAlert("Success", "Production invoice created and opened for preview!\n\nInvoice Number: " + 
                    invoiceNumberField.getText() + "\n\nProceeed with printing from the preview window.");
            } else {
                // Fallback to printer selection if preview fails
                boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Production");
                if (printSuccess) {
                    showAlert("Success", "Production invoice created and printed successfully!\n\nInvoice Number: " + 
                        invoiceNumberField.getText());
                } else {
                    showAlert("Partial Success", "Production invoice created but printing failed.\n\nInvoice Number: " + 
                        invoiceNumberField.getText() + "\n\nYou can print it later if needed.");
                }
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
            
            showAlert("Success", "Production invoice created successfully!\nProduction stock quantities have been updated.");
            
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

    // Helper method to get production stock unit cost by production ID
    private static double getProductionStockUnitCost(int productionId) {
        try {
            List<Object[]> productionStocks = database.getAllProductionStocksForDropdown();
            for (Object[] stock : productionStocks) {
                if (((Integer) stock[0]).equals(productionId)) {
                    // stock[5] contains unit_cost based on getAllProductionStocksForDropdown structure
                    return (Double) stock[5]; // unit_cost
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
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
            String notes = "Return for Invoice #" + productionInvoiceId;
            double totalQuantity = Double.parseDouble(totalReturnQuantity);
            int originalProductionInvoiceId = Integer.parseInt(productionInvoiceId);
            
            // First, prepare return invoice items and calculate totals
            List<Object[]> returnInvoiceItems = new ArrayList<>();
            double totalAmount = 0.0;
            
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
                                double returnQuantity = Double.parseDouble(returnQtyPart);
                                
                                // Get production stock ID and unit cost by name
                                int productionStockId = getProductionStockIdByName(productName);
                                if (productionStockId > 0) {
                                    double unitCost = getProductionStockUnitCost(productionStockId);
                                    double totalCost = returnQuantity * unitCost;
                                    totalAmount += totalCost;
                                    
                                    returnInvoiceItems.add(new Object[]{
                                        productionStockId,  // production_id
                                        returnQuantity,     // quantity_returned (Double)
                                        unitCost,          // unit_cost (Double)
                                        totalCost          // total_cost (Double)
                                    });
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing return item: " + returnItem + " - " + e.getMessage());
                }
            }
            
            // Insert return invoice and get ID
            int returnInvoiceId = sqliteDatabase.insertProductionReturnInvoiceAndGetId(
                returnInvoiceNumber, 
                originalProductionInvoiceId, 
                formattedDate, 
                totalQuantity, 
                totalAmount, 
                notes
            );
            
            if (returnInvoiceId > 0) {
                // Insert return invoice items
                if (!returnInvoiceItems.isEmpty()) {
                    sqliteDatabase.insertProductionReturnInvoiceItems(returnInvoiceId, returnInvoiceItems);
                    
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

    // Helper methods for sales invoice functionality
    private static void updatePaymentSummary(ObservableList<SalesInvoiceItemUI> items, 
                                           TextField discountField, TextField paidAmountField,
                                           Label subtotalLabel, Label discountLabel, 
                                           Label totalAmountLabel, Label balanceLabel) {
        try {
            // Calculate subtotal
            double subtotal = items.stream().mapToDouble(SalesInvoiceItemUI::getTotalPrice).sum();
            
            // Get discount and paid amounts
            double discount = 0.0;
            double paidAmount = 0.0;
            
            try {
                String discountText = discountField.getText().trim();
                discount = discountText.isEmpty() ? 0.0 : Double.parseDouble(discountText);
            } catch (NumberFormatException e) {
                discount = 0.0;
            }
            
            try {
                String paidText = paidAmountField.getText().trim();
                paidAmount = paidText.isEmpty() ? 0.0 : Double.parseDouble(paidText);
            } catch (NumberFormatException e) {
                paidAmount = 0.0;
            }
            
            // Calculate total and balance
            double totalAmount = subtotal - discount;
            double balance = totalAmount - paidAmount;
            
            // Update labels
            subtotalLabel.setText(String.format("Subtotal: %.2f", subtotal));
            discountLabel.setText(String.format("Discount: %.2f", discount));
            totalAmountLabel.setText(String.format("Total Amount: %.2f", totalAmount));
            
            // Set balance color based on amount
            if (balance <= 0) {
                balanceLabel.setText("Balance Due: 0.00 (PAID)");
                balanceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
            } else {
                balanceLabel.setText(String.format("Balance Due: %.2f", balance));
                balanceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
            }
            
        } catch (Exception e) {
            // Fallback in case of any errors
            subtotalLabel.setText("Subtotal: 0.00");
            discountLabel.setText("Discount: 0.00");
            totalAmountLabel.setText("Total Amount: 0.00");
            balanceLabel.setText("Balance Due: 0.00");
        }
    }
    
    private static void updateTotalAmount(ObservableList<SalesInvoiceItemUI> items, Label totalLabel) {
        double total = items.stream().mapToDouble(SalesInvoiceItemUI::getTotalPrice).sum();
        totalLabel.setText(String.format("Total Amount: %.2f", total));
    }

    private static void updateReturnAmount(ObservableList<SalesInvoiceItemUI> items, TextField returnAmountField) {
        double total = items.stream().mapToDouble(SalesInvoiceItemUI::getTotalPrice).sum();
        returnAmountField.setText(String.format("%.2f", total));
    }

    // UI Model class for Sales Invoice Items
    public static class SalesInvoiceItemUI {
        private final SimpleStringProperty productName;
        private final SimpleDoubleProperty quantity;
        private final SimpleDoubleProperty unitPrice;
        private final SimpleDoubleProperty totalPrice;
        private final SimpleDoubleProperty originalQuantity;
        private int productionStockId;

        public SalesInvoiceItemUI(String productName, double quantity, double unitPrice) {
            this.productName = new SimpleStringProperty(productName);
            this.quantity = new SimpleDoubleProperty(quantity);
            this.unitPrice = new SimpleDoubleProperty(unitPrice);
            this.totalPrice = new SimpleDoubleProperty(quantity * unitPrice);
            this.originalQuantity = new SimpleDoubleProperty(quantity);
        }

        // Property getters
        public SimpleStringProperty productNameProperty() { return productName; }
        public SimpleDoubleProperty quantityProperty() { return quantity; }
        public SimpleDoubleProperty unitPriceProperty() { return unitPrice; }
        public SimpleDoubleProperty totalPriceProperty() { return totalPrice; }
        public SimpleDoubleProperty originalQuantityProperty() { return originalQuantity; }

        // Value getters
        public String getProductName() { return productName.get(); }
        public double getQuantity() { return quantity.get(); }
        public double getUnitPrice() { return unitPrice.get(); }
        public double getTotalPrice() { return totalPrice.get(); }
        public double getOriginalQuantity() { return originalQuantity.get(); }
        public int getProductionStockId() { return productionStockId; }

        // Value setters
        public void setProductName(String productName) { this.productName.set(productName); }
        
        public void setQuantity(double quantity) { 
            this.quantity.set(quantity);
            updateTotalPrice();
        }
        
        public void setUnitPrice(double unitPrice) { 
            this.unitPrice.set(unitPrice);
            updateTotalPrice();
        }
        
        public void setOriginalQuantity(double originalQuantity) { 
            this.originalQuantity.set(originalQuantity); 
        }
        
        public void setProductionStockId(int productionStockId) { 
            this.productionStockId = productionStockId; 
        }

        private void updateTotalPrice() {
            this.totalPrice.set(this.quantity.get() * this.unitPrice.get());
        }
    }

    // Helper methods for the new two-column production stock form
    
    private static void updateStockSummary(TableView<ProductionStockRecord> stockTable, 
                                         Label totalItemsLabel, Label totalValueLabel, Label lowStockLabel) {
        try {
            ObservableList<ProductionStockRecord> items = stockTable.getItems();
            int totalItems = items.size();
            double totalValue = 0.0;
            int lowStockCount = 0;
            
            for (ProductionStockRecord record : items) {
                // Calculate total value (quantity * sale_price)
                totalValue += record.getQuantity() * record.getSalePrice();
                
                // Count low stock items (less than 10)
                if (record.getQuantity() < 10) {
                    lowStockCount++;
                }
            }
            
            totalItemsLabel.setText("Total Items: " + totalItems);
            totalValueLabel.setText(String.format("Total Value: %.2f", totalValue));
            
            if (lowStockCount > 0) {
                lowStockLabel.setText("Low Stock Items: " + lowStockCount);
                lowStockLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
            } else {
                lowStockLabel.setText("Low Stock Items: 0");
                lowStockLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
            }
            
        } catch (Exception e) {
            System.err.println("Error updating stock summary: " + e.getMessage());
            totalItemsLabel.setText("Total Items: 0");
            totalValueLabel.setText("Total Value: 0.00");
            lowStockLabel.setText("Low Stock Items: 0");
        }
    }
    
    private static void filterProductionStockTable(TableView<ProductionStockRecord> stockTable, 
                                                 String searchText, String brandFilter) {
        try {
            // Get all production stock from database
            List<Object[]> allStock = database.getAllProductionStocks();
            ObservableList<ProductionStockRecord> filteredRecords = FXCollections.observableArrayList();
            
            for (Object[] stock : allStock) {
                int productionId = (Integer) stock[0];
                String productName = (String) stock[1];
                String productDesc = (String) stock[2];
                String brandName = (String) stock[3];
                String brandDesc = (String) stock[4];
                int quantity = (Integer) stock[5];
                double unitCost = (Double) stock[6];
                double salePrice = (Double) stock[7];
                
                boolean matchesSearch = true;
                boolean matchesBrand = true;
                
                // Apply search filter
                if (searchText != null && !searchText.trim().isEmpty()) {
                    String search = searchText.toLowerCase();
                    matchesSearch = productName.toLowerCase().contains(search) || 
                                  brandName.toLowerCase().contains(search);
                }
                
                // Apply brand filter
                if (brandFilter != null && !brandFilter.equals("All Brands")) {
                    matchesBrand = brandName.equals(brandFilter);
                }
                
                if (matchesSearch && matchesBrand) {
                    filteredRecords.add(new ProductionStockRecord(
                        productionId, productName, productDesc, brandName, brandDesc, 
                        quantity, unitCost, salePrice
                    ));
                }
            }
            
            stockTable.setItems(filteredRecords);
            
        } catch (Exception e) {
            System.err.println("Error filtering production stock table: " + e.getMessage());
            showAlert("Error", "Failed to filter table: " + e.getMessage());
        }
    }
}
