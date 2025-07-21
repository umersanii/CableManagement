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
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cablemanagement.database.SQLiteDatabase;
import com.cablemanagement.database.db;
import com.cablemanagement.model.Brand;
import com.cablemanagement.model.RawStockPurchaseItem;
import com.cablemanagement.model.RawStockUseItem;

public class RawStock {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final db database = new SQLiteDatabase();

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createRawStockForm());

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
            "Register Raw Stock",
            "Create Raw Stock Purchase Invoice",
            "Create Raw Stock Return Purchase Invoice",
            "Create Raw Stock Use Invoice",
            "View Raw Stock Usage Report"
        };

        Runnable[] actions = {
            () -> formArea.getChildren().setAll(createRawStockForm()),
            () -> formArea.getChildren().setAll(createRawStockPurchaseInvoiceForm()),
            () -> formArea.getChildren().setAll(createRawStockReturnPurchaseInvoiceForm()),
            () -> formArea.getChildren().setAll(createRawStockUseInvoiceForm()),
            () -> formArea.getChildren().setAll(createRawStockUsageReportForm())
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

    private static VBox createRawStockForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Register Raw Stock");

        // Input fields matching RawStock table structure
        TextField nameField = createTextField("Stock Name");
        TextField quantityField = createTextField("0", "Quantity");
        TextField unitPriceField = createTextField("Unit Price");
        
        // Brand ComboBox for better database integration
        ComboBox<String> brandCombo = new ComboBox<>();
        brandCombo.setPromptText("Select Brand");
        for (Brand b : database.getAllBrands()) {
            brandCombo.getItems().add(b.nameProperty().get());
        }
        brandCombo.setPrefWidth(200);
        
        // Supplier ComboBox (optional)
        ComboBox<String> supplierCombo = new ComboBox<>();
        supplierCombo.setPromptText("Select Supplier (Optional)");
        supplierCombo.getItems().addAll(database.getAllSupplierNames());
        supplierCombo.setPrefWidth(200);

        Button submitBtn = createSubmitButton("Submit Raw Stock");

        // Raw Stock Table
        Label tableHeading = createSubheading("Registered Raw Stock:");
        TableView<RawStockRecord> stockTable = createRawStockTable();
        refreshRawStockTable(stockTable);

        submitBtn.setOnAction(e -> handleRawStockSubmit(
            nameField, brandCombo, supplierCombo,
            quantityField, unitPriceField,
            stockTable
        ));

        // Create form content in a compact layout
        VBox formContent = new VBox(15);
        formContent.getChildren().addAll(
            heading, 
            createFormRow("Stock Name:", nameField),
            createFormRow("Brand:", brandCombo),
            createFormRow("Supplier:", supplierCombo),
            createFormRow("Quantity:", quantityField),
            createFormRow("Unit Price:", unitPriceField),
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

    private static TableView<RawStockRecord> createRawStockTable() {
        TableView<RawStockRecord> table = new TableView<>();
        table.setPrefHeight(200);
        table.setMaxHeight(200);
        
        TableColumn<RawStockRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setMaxWidth(50);
        
        TableColumn<RawStockRecord, String> nameCol = new TableColumn<>("Stock Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<RawStockRecord, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        brandCol.setPrefWidth(120);
        
        TableColumn<RawStockRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(80);
        qtyCol.setCellFactory(column -> new TableCell<RawStockRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.0f", item));
                }
            }
        });
        
        TableColumn<RawStockRecord, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setPrefWidth(100);
        priceCol.setCellFactory(column -> new TableCell<RawStockRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        TableColumn<RawStockRecord, Double> totalCol = new TableColumn<>("Total Cost");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        totalCol.setPrefWidth(100);
        totalCol.setCellFactory(column -> new TableCell<RawStockRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, brandCol, qtyCol, priceCol, totalCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }

    private static void refreshRawStockTable(TableView<RawStockRecord> table) {
        ObservableList<RawStockRecord> data = FXCollections.observableArrayList();
        List<Object[]> rawStocks = database.getAllRawStocks();
        
        for (Object[] row : rawStocks) {
            data.add(new RawStockRecord(
                (Integer) row[0],  // stock_id
                (String) row[1],   // item_name
                (String) row[2],   // brand_name
                (Double) row[3],   // quantity (converted to double)
                (Double) row[4],   // unit_price
                (Double) row[5]    // total_cost
            ));
        }
        
        table.setItems(data);
    }

    private static VBox createRawStockPurchaseInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Raw Stock Purchase Invoice");

        // Auto-generated invoice number (readonly)
        TextField invoiceNumberField = createTextField(database.generateNextInvoiceNumber("RPI"), "Auto-generated Invoice Number");
        invoiceNumberField.setEditable(false);
        invoiceNumberField.setStyle("-fx-background-color: #f0f0f0;");

        // Supplier dropdown
        ComboBox<String> supplierCombo = new ComboBox<>();
        supplierCombo.setPromptText("Select Supplier");
        supplierCombo.getItems().addAll(database.getAllSupplierNames());
        supplierCombo.setPrefWidth(300);

        DatePicker invoiceDatePicker = new DatePicker();
        invoiceDatePicker.setValue(LocalDate.now());

        // Invoice items section
        VBox itemsSection = new VBox(15);
        itemsSection.getStyleClass().add("section-container");

        Label itemsHeading = createSubheading("Invoice Items:");

        // Add item controls
        HBox addItemControls = new HBox(10);
        addItemControls.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> rawStockCombo = new ComboBox<>();
        rawStockCombo.setPromptText("Select Raw Stock");
        rawStockCombo.setPrefWidth(200);

        TextField quantityField = createTextField("Quantity");
        quantityField.setPrefWidth(100);

        TextField unitPriceField = createTextField("Unit Price");
        unitPriceField.setPrefWidth(100);

        Button addItemBtn = createActionButton("Add Item");

        // Populate raw stock dropdown
        List<Object[]> rawStocks = database.getAllRawStocksForDropdown();
        for (Object[] stock : rawStocks) {
            String displayName = String.format("%s (%s - %s)", stock[1], stock[2], stock[3]); // name (category - brand)
            rawStockCombo.getItems().add(displayName);
        }

        addItemControls.getChildren().addAll(
            new Label("Raw Stock:"), rawStockCombo,
            new Label("Qty:"), quantityField,
            new Label("Price:"), unitPriceField,
            addItemBtn
        );

        // Invoice items table
        TableView<RawStockPurchaseItem> itemsTable = createInvoiceItemsTable();

        // Total section
        HBox totalsSection = new HBox(20);
        totalsSection.setAlignment(Pos.CENTER_RIGHT);

        TextField discountField = createTextField("0", "Discount");
        discountField.setPrefWidth(100);

        TextField paidAmountField = createTextField("0", "Paid Amount");
        paidAmountField.setPrefWidth(100);

        Label totalLabel = new Label("Total: $0.00");
        totalLabel.getStyleClass().add("total-label");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        totalsSection.getChildren().addAll(
            new Label("Discount:"), discountField,
            new Label("Paid:"), paidAmountField,
            totalLabel
        );

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        Button submitBtn = createSubmitButton("Submit Purchase Invoice");
        Button clearAllBtn = createActionButton("Clear All");
        actionButtons.getChildren().addAll(submitBtn, clearAllBtn);

        itemsSection.getChildren().addAll(itemsHeading, addItemControls, itemsTable, totalsSection);

        form.getChildren().addAll(
            heading,
            createFormRow("Invoice Number:", invoiceNumberField),
            createFormRow("Supplier:", supplierCombo),
            createFormRow("Invoice Date:", invoiceDatePicker),
            itemsSection,
            actionButtons
        );

        // Event handlers
        addItemBtn.setOnAction(e -> handleAddInvoiceItem(
            rawStockCombo, quantityField, unitPriceField, 
            itemsTable, totalLabel, rawStocks
        ));

        // Auto-update total when discount or paid amount changes
        discountField.textProperty().addListener((obs, old, newVal) -> updateTotalLabel(itemsTable, discountField, totalLabel));
        paidAmountField.textProperty().addListener((obs, old, newVal) -> updateTotalLabel(itemsTable, discountField, totalLabel));

        clearAllBtn.setOnAction(e -> {
            if (!itemsTable.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all invoice items?");
                alert.setContentText("This will remove all items from the invoice.");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    itemsTable.getItems().clear();
                    updateTotalLabel(itemsTable, discountField, totalLabel);
                }
            }
        });

        submitBtn.setOnAction(e -> handleEnhancedPurchaseInvoiceSubmit(
            invoiceNumberField, supplierCombo, invoiceDatePicker,
            itemsTable, discountField, paidAmountField, totalLabel
        ));

        return form;
    }

    private static VBox createRawStockReturnPurchaseInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Raw Stock Return Purchase Invoice");

        // Auto-generated return invoice number (read-only)
        TextField returnInvoiceNumberField = createTextField("Return Invoice Number");
        returnInvoiceNumberField.setEditable(false);
        returnInvoiceNumberField.setText(database.generateReturnInvoiceNumber());

        // Dropdown for selecting original invoice
        ComboBox<String> originalInvoiceComboBox = new ComboBox<>();
        originalInvoiceComboBox.setPromptText("Select Original Invoice");
        originalInvoiceComboBox.setPrefWidth(250);
        
        // Dropdown for suppliers (automatically populated when original invoice is selected)
        ComboBox<String> supplierComboBox = new ComboBox<>();
        supplierComboBox.setPromptText("Supplier (Auto-selected)");
        supplierComboBox.setPrefWidth(250);
        supplierComboBox.setDisable(true);

        // Return date picker
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(LocalDate.now());

        // Table for selecting multiple stock items
        TableView<RawStockPurchaseItem> availableItemsTable = createAvailableItemsTable();
        TableView<RawStockPurchaseItem> selectedItemsTable = createSelectedReturnItemsTable();
        
        // Total amount field (auto-calculated)
        TextField totalReturnAmountField = createTextField("Total Return Amount");
        totalReturnAmountField.setEditable(false);
        totalReturnAmountField.setText("0.00");

        // Buttons
        Button addItemsBtn = createSubmitButton("Add Selected Items →");
        Button removeItemsBtn = createSubmitButton("← Remove Selected Items");
        Button submitReturnInvoiceBtn = createSubmitButton("Submit Return Invoice");
        
        // Load original invoices into dropdown
        loadOriginalInvoicesIntoDropdown(originalInvoiceComboBox);
        
        // Event handlers
        originalInvoiceComboBox.setOnAction(e -> {
            String selectedInvoice = originalInvoiceComboBox.getValue();
            if (selectedInvoice != null) {
                handleOriginalInvoiceSelection(selectedInvoice, supplierComboBox, availableItemsTable);
            }
        });

        addItemsBtn.setOnAction(e -> {
            ObservableList<RawStockPurchaseItem> selected = availableItemsTable.getSelectionModel().getSelectedItems();
            for (RawStockPurchaseItem item : new ArrayList<>(selected)) {
                // Create a dialog to get return quantity and price
                RawStockPurchaseItem returnItem = showReturnItemDialog(item);
                if (returnItem != null) {
                    selectedItemsTable.getItems().add(returnItem);
                    updateTotalAmount(selectedItemsTable, totalReturnAmountField);
                }
            }
        });

        removeItemsBtn.setOnAction(e -> {
            ObservableList<RawStockPurchaseItem> selected = selectedItemsTable.getSelectionModel().getSelectedItems();
            selectedItemsTable.getItems().removeAll(new ArrayList<>(selected));
            updateTotalAmount(selectedItemsTable, totalReturnAmountField);
        });

        submitReturnInvoiceBtn.setOnAction(e -> {
            handleReturnInvoiceSubmit(returnInvoiceNumberField, originalInvoiceComboBox, supplierComboBox,
                    returnDatePicker, selectedItemsTable, totalReturnAmountField);
        });

        // Layout
        HBox buttonBox = new HBox(10, addItemsBtn, removeItemsBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox tableContainer = new VBox(10);
        tableContainer.getChildren().addAll(
            createSubheading("Available Items from Original Invoice:"),
            availableItemsTable,
            buttonBox,
            createSubheading("Selected Return Items:"),
            selectedItemsTable
        );

        // Previous return invoices list
        Label listHeading = createSubheading("Previous Return Purchase Invoices:");
        TableView<Object[]> returnInvoicesTable = createReturnInvoicesTable();
        loadReturnInvoicesIntoTable(returnInvoicesTable);

        form.getChildren().addAll(
            heading,
            createFormRow("Return Invoice Number:", returnInvoiceNumberField),
            createFormRow("Original Invoice:", originalInvoiceComboBox),
            createFormRow("Supplier:", supplierComboBox),
            createFormRow("Return Date:", returnDatePicker),
            tableContainer,
            createFormRow("Total Return Amount:", totalReturnAmountField),
            submitReturnInvoiceBtn,
            listHeading,
            returnInvoicesTable
        );
        
        return form;
    }

    private static void loadOriginalInvoicesIntoDropdown(ComboBox<String> comboBox) {
        try {
            List<Object[]> invoices = database.getAllRawPurchaseInvoicesForDropdown();
            ObservableList<String> invoiceList = FXCollections.observableArrayList();
            
            for (Object[] invoice : invoices) {
                String displayText = String.format("%s - %s (%.2f)", 
                    invoice[1], invoice[2], (Double) invoice[4]); // invoice_number - supplier (total_amount)
                invoiceList.add(displayText);
            }
            
            comboBox.setItems(invoiceList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load original invoices");
        }
    }

    private static void handleOriginalInvoiceSelection(String selectedInvoice, 
                                                     ComboBox<String> supplierComboBox,
                                                     TableView<RawStockPurchaseItem> availableItemsTable) {
        try {
            // Extract invoice ID from the selected text
            List<Object[]> invoices = database.getAllRawPurchaseInvoicesForDropdown();
            int invoiceId = -1;
            String supplierName = "";
            
            for (Object[] invoice : invoices) {
                String displayText = String.format("%s - %s (%.2f)", 
                    invoice[1], invoice[2], (Double) invoice[4]);
                if (displayText.equals(selectedInvoice)) {
                    invoiceId = (Integer) invoice[0];
                    supplierName = (String) invoice[2];
                    break;
                }
            }
            
            if (invoiceId != -1) {
                // Set supplier
                supplierComboBox.setValue(supplierName);
                
                // Load items from the selected invoice
                List<Object[]> items = database.getRawStockItemsByInvoiceId(invoiceId);
                ObservableList<RawStockPurchaseItem> itemsList = FXCollections.observableArrayList();
                
                for (Object[] item : items) {
                    RawStockPurchaseItem purchaseItem = new RawStockPurchaseItem(
                        (Integer) item[0], // raw_stock_id
                        (String) item[1],  // raw_stock_name  
                        (String) item[2],  // category_name
                        (String) item[3],  // brand_name
                        (String) item[4],  // unit_name
                        (Double) item[5],  // quantity
                        (Double) item[6]   // unit_price
                    );
                    itemsList.add(purchaseItem);
                }
                
                availableItemsTable.setItems(itemsList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load invoice items");
        }
    }

    private static TableView<RawStockPurchaseItem> createAvailableItemsTable() {
        TableView<RawStockPurchaseItem> table = new TableView<>();
        table.setPrefHeight(200);
        
        TableColumn<RawStockPurchaseItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("rawStockName"));
        nameCol.setPrefWidth(150);
        
        TableColumn<RawStockPurchaseItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(100);
        
        TableColumn<RawStockPurchaseItem, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        brandCol.setPrefWidth(100);
        
        TableColumn<RawStockPurchaseItem, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitCol.setPrefWidth(80);
        
        TableColumn<RawStockPurchaseItem, Double> quantityCol = new TableColumn<>("Orig. Qty");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(80);
        
        TableColumn<RawStockPurchaseItem, Double> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        unitPriceCol.setPrefWidth(80);
        
        table.getColumns().addAll(nameCol, categoryCol, brandCol, unitCol, quantityCol, unitPriceCol);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        return table;
    }

    private static TableView<RawStockPurchaseItem> createSelectedReturnItemsTable() {
        TableView<RawStockPurchaseItem> table = new TableView<>();
        table.setPrefHeight(200);
        
        TableColumn<RawStockPurchaseItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("rawStockName"));
        nameCol.setPrefWidth(150);
        
        TableColumn<RawStockPurchaseItem, Double> quantityCol = new TableColumn<>("Return Qty");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(80);
        
        TableColumn<RawStockPurchaseItem, Double> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        unitPriceCol.setPrefWidth(80);
        
        TableColumn<RawStockPurchaseItem, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalCol.setPrefWidth(80);
        
        table.getColumns().addAll(nameCol, quantityCol, unitPriceCol, totalCol);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        return table;
    }

    private static RawStockPurchaseItem showReturnItemDialog(RawStockPurchaseItem originalItem) {
        Dialog<RawStockPurchaseItem> dialog = new Dialog<>();
        dialog.setTitle("Return Item Details");
        dialog.setHeaderText("Enter return details for: " + originalItem.getRawStockName());
        
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Return Quantity (Max: " + originalItem.getQuantity() + ")");
        TextField unitPriceField = new TextField();
        unitPriceField.setText(String.valueOf(originalItem.getUnitPrice()));
        
        grid.add(new Label("Return Quantity:"), 0, 0);
        grid.add(quantityField, 1, 0);
        grid.add(new Label("Unit Price:"), 0, 1);
        grid.add(unitPriceField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    double returnQty = Double.parseDouble(quantityField.getText());
                    double unitPrice = Double.parseDouble(unitPriceField.getText());
                    
                    if (returnQty <= 0 || returnQty > originalItem.getQuantity()) {
                        showAlert("Invalid Input", "Return quantity must be between 0 and " + originalItem.getQuantity());
                        return null;
                    }
                    
                    return new RawStockPurchaseItem(
                        originalItem.getRawStockId(),
                        originalItem.getRawStockName(),
                        originalItem.getCategory(),
                        originalItem.getBrand(),
                        originalItem.getUnit(),
                        returnQty,
                        unitPrice
                    );
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter valid numbers");
                    return null;
                }
            }
            return null;
        });
        
        return dialog.showAndWait().orElse(null);
    }

    private static void updateTotalAmount(TableView<RawStockPurchaseItem> table, TextField totalField) {
        double total = table.getItems().stream()
                .mapToDouble(RawStockPurchaseItem::getTotalPrice)
                .sum();
        totalField.setText(String.format("%.2f", total));
    }

    private static void handleReturnInvoiceSubmit(TextField returnInvoiceNumberField,
                                                ComboBox<String> originalInvoiceComboBox,
                                                ComboBox<String> supplierComboBox,
                                                DatePicker returnDatePicker,
                                                TableView<RawStockPurchaseItem> selectedItemsTable,
                                                TextField totalReturnAmountField) {
        try {
            // Validate inputs
            if (originalInvoiceComboBox.getValue() == null || originalInvoiceComboBox.getValue().isEmpty()) {
                showAlert("Validation Error", "Please select an original invoice");
                return;
            }
            
            if (selectedItemsTable.getItems().isEmpty()) {
                showAlert("Validation Error", "Please select at least one item to return");
                return;
            }
            
            // Get original invoice ID
            List<Object[]> invoices = database.getAllRawPurchaseInvoicesForDropdown();
            int originalInvoiceId = -1;
            int supplierId = -1;
            
            for (Object[] invoice : invoices) {
                String displayText = String.format("%s - %s (%.2f)", 
                    invoice[1], invoice[2], (Double) invoice[4]);
                if (displayText.equals(originalInvoiceComboBox.getValue())) {
                    originalInvoiceId = (Integer) invoice[0];
                    supplierId = database.getSupplierIdByName((String) invoice[2]);
                    break;
                }
            }
            
            if (originalInvoiceId == -1) {
                showAlert("Error", "Could not find original invoice");
                return;
            }
            
            // Insert return invoice
            String returnInvoiceNumber = returnInvoiceNumberField.getText();
            String returnDate = returnDatePicker.getValue().format(DATE_FORMATTER);
            double totalAmount = Double.parseDouble(totalReturnAmountField.getText());
            
            int returnInvoiceId = database.insertRawPurchaseReturnInvoiceAndGetId(
                returnInvoiceNumber, originalInvoiceId, supplierId, returnDate, totalAmount);
            
            if (returnInvoiceId > 0) {
                // Insert return invoice items
                List<RawStockPurchaseItem> items = new ArrayList<>(selectedItemsTable.getItems());
                boolean itemsInserted = database.insertRawPurchaseReturnInvoiceItems(returnInvoiceId, items);
                
                if (itemsInserted) {
                    showAlert("Success", "Return invoice created successfully!");
                    
                    // Clear form
                    returnInvoiceNumberField.setText(database.generateReturnInvoiceNumber());
                    originalInvoiceComboBox.setValue(null);
                    supplierComboBox.setValue(null);
                    returnDatePicker.setValue(LocalDate.now());
                    selectedItemsTable.getItems().clear();
                    totalReturnAmountField.setText("0.00");
                    
                    // Refresh return invoices table
                    // This would need to be passed as a parameter or accessed differently
                } else {
                    showAlert("Error", "Failed to save return invoice items");
                }
            } else {
                showAlert("Error", "Failed to create return invoice");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while creating the return invoice: " + e.getMessage());
        }
    }

    private static TableView<Object[]> createReturnInvoicesTable() {
        TableView<Object[]> table = new TableView<>();
        table.setPrefHeight(200);
        
        TableColumn<Object[], String> returnInvoiceCol = new TableColumn<>("Return Invoice");
        returnInvoiceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[0]));
        returnInvoiceCol.setPrefWidth(120);
        
        TableColumn<Object[], String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[1]));
        dateCol.setPrefWidth(100);
        
        TableColumn<Object[], String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[2]));
        supplierCol.setPrefWidth(120);
        
        TableColumn<Object[], String> originalCol = new TableColumn<>("Original Invoice");
        originalCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[3]));
        originalCol.setPrefWidth(120);
        
        TableColumn<Object[], Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>((Double) cellData.getValue()[4]));
        amountCol.setPrefWidth(80);
        
        table.getColumns().addAll(returnInvoiceCol, dateCol, supplierCol, originalCol, amountCol);
        
        return table;
    }

    private static void loadReturnInvoicesIntoTable(TableView<Object[]> table) {
        try {
            List<Object[]> returnInvoices = database.getAllRawPurchaseReturnInvoices();
            ObservableList<Object[]> data = FXCollections.observableArrayList(returnInvoices);
            table.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load return invoices");
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static ScrollPane createRawStockUseInvoiceForm() {
        VBox form = new VBox(15); // Reduced spacing to save vertical space
        form.setPadding(new Insets(20)); // Reduced padding
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Raw Stock Use Invoice");

        // Auto-generated Invoice Number (Read-only)
        TextField invoiceNumberField = createTextField("Use Invoice Number");
        invoiceNumberField.setEditable(false);
        invoiceNumberField.setStyle("-fx-background-color: #f0f0f0;");
        
        // Load auto-generated invoice number
        String autoInvoiceNumber = database.generateUseInvoiceNumber();
        invoiceNumberField.setText(autoInvoiceNumber);

        // Usage Date
        DatePicker usageDatePicker = new DatePicker();
        usageDatePicker.setValue(LocalDate.now());

        // Reference/Purpose field
        TextField referencePurposeField = createTextField("Reference/Purpose");

        // Compact form section for basic info
        VBox basicInfoSection = new VBox(10);
        basicInfoSection.getChildren().addAll(
            heading,
            createFormRow("Use Invoice Number:", invoiceNumberField),
            createFormRow("Usage Date:", usageDatePicker),
            createFormRow("Reference/Purpose:", referencePurposeField)
        );

        // Tables for Available and Selected Items (reduced heights)
        Label availableItemsLabel = createSubheading("Available Raw Stock Items:");
        TableView<RawStockUseItem> availableItemsTable = createRawStockItemsTable();
        availableItemsTable.setPrefHeight(150); // Reduced from 200
        availableItemsTable.setMaxHeight(150);
        
        Label selectedItemsLabel = createSubheading("Selected Items for Use:");
        TableView<RawStockUseItem> selectedItemsTable = createSelectedUsageItemsTable();
        selectedItemsTable.setPrefHeight(150); // Reduced from 200
        selectedItemsTable.setMaxHeight(150);

        // Buttons for item selection
        HBox itemButtonsBox = new HBox(10);
        itemButtonsBox.setAlignment(Pos.CENTER);
        itemButtonsBox.setPadding(new Insets(10, 0, 10, 0));
        Button addItemBtn = new Button("Add Selected Items");
        Button removeItemBtn = new Button("Remove Selected Items");
        addItemBtn.getStyleClass().add("form-submit");
        removeItemBtn.getStyleClass().add("form-submit");

        // Total amount display
        Label totalLabel = createSubheading("Total Usage Amount: $0.00");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Load available items initially
        loadAvailableRawStockItems(availableItemsTable);

        // Add item button action
        addItemBtn.setOnAction(e -> {
            RawStockUseItem selectedItem = availableItemsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                showUsageQuantityDialog(selectedItem, selectedItemsTable, totalLabel);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item from the available items table.");
            }
        });

        // Remove item button action
        removeItemBtn.setOnAction(e -> {
            RawStockUseItem selectedItem = selectedItemsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                selectedItemsTable.getItems().remove(selectedItem);
                updateTotalUsageAmount(selectedItemsTable, totalLabel);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item from the selected items table.");
            }
        });

        itemButtonsBox.getChildren().addAll(addItemBtn, removeItemBtn);

        // Submit button
        Button submitBtn = createSubmitButton("Submit Use Invoice");
        submitBtn.setOnAction(e -> handleRawStockUseInvoiceSubmit(
            invoiceNumberField, usageDatePicker, referencePurposeField,
            selectedItemsTable, totalLabel
        ));

        // Previous use invoices table (compact)
        Label previousInvoicesLabel = createSubheading("Previous Use Invoices:");
        TableView<Object[]> previousInvoicesTable = createPreviousUseInvoicesTable();
        previousInvoicesTable.setPrefHeight(120); // Reduced height
        previousInvoicesTable.setMaxHeight(120);
        loadPreviousUseInvoices(previousInvoicesTable);

        // Add all components to form
        form.getChildren().addAll(
            basicInfoSection,
            availableItemsLabel,
            availableItemsTable,
            itemButtonsBox,
            selectedItemsLabel,
            selectedItemsTable,
            totalLabel,
            submitBtn,
            previousInvoicesLabel,
            previousInvoicesTable
        );
        
        // Wrap in ScrollPane for responsiveness
        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefViewportHeight(600); // Set a reasonable height
        scrollPane.getStyleClass().add("scroll-pane");
        
        return scrollPane;
    }

    private static VBox createRawStockUsageReportForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Raw Stock Usage Report");

        // Date range selection
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now());

        Button generateBtn = new Button("Generate Report");
        generateBtn.getStyleClass().add("form-submit");

        Label reportHeading = createSubheading("Usage Report:");
        ListView<String> reportView = createListView();
        reportView.setPrefHeight(400);

        generateBtn.setOnAction(e -> {
            String startDate = startDatePicker.getValue().format(DATE_FORMATTER);
            String endDate = endDatePicker.getValue().format(DATE_FORMATTER);
            
            // In real app, this would query the database
            reportView.getItems().clear();
            reportView.getItems().add("Usage Report from " + startDate + " to " + endDate);
            reportView.getItems().add("Copper Wire 8mm - 150m used");
            reportView.getItems().add("PVC Granules - 75kg used");
            reportView.getItems().add("Aluminum Conductor - 200m used");
        });

        form.getChildren().addAll(
            heading,
            createFormRow("Start Date:", startDatePicker),
            createFormRow("End Date:", endDatePicker),
            generateBtn,
            reportHeading,
            reportView
        );
        
        return form;
    }

    // Helper methods for UI components
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

    private static ListView<String> createListView() {
        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(200);
        listView.getStyleClass().add("category-list");
        return listView;
    }

    private static Button createSubmitButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("form-submit");
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

    // Enhanced methods for new invoice functionality
    private static Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        return button;
    }

    private static TableView<RawStockPurchaseItem> createInvoiceItemsTable() {
        TableView<RawStockPurchaseItem> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("form-table");

        TableColumn<RawStockPurchaseItem, String> nameCol = new TableColumn<>("Raw Stock");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("rawStockName"));
        nameCol.setPrefWidth(150);

        TableColumn<RawStockPurchaseItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(100);

        TableColumn<RawStockPurchaseItem, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        brandCol.setPrefWidth(100);

        TableColumn<RawStockPurchaseItem, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitCol.setPrefWidth(60);

        TableColumn<RawStockPurchaseItem, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(80);

        TableColumn<RawStockPurchaseItem, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setPrefWidth(80);

        TableColumn<RawStockPurchaseItem, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalCol.setPrefWidth(80);

        // Add delete column
        TableColumn<RawStockPurchaseItem, Void> deleteCol = new TableColumn<>("Action");
        deleteCol.setPrefWidth(60);
        deleteCol.setCellFactory(param -> new TableCell<RawStockPurchaseItem, Void>() {
            private final Button deleteBtn = new Button("✖");

            {
                deleteBtn.getStyleClass().add("delete-button");
                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    RawStockPurchaseItem item = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        table.getColumns().addAll(nameCol, categoryCol, brandCol, unitCol, qtyCol, priceCol, totalCol, deleteCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    private static void handleAddInvoiceItem(ComboBox<String> rawStockCombo, TextField quantityField, 
                                           TextField unitPriceField, TableView<RawStockPurchaseItem> itemsTable, 
                                           Label totalLabel, List<Object[]> rawStocks) {
        String selectedRawStock = rawStockCombo.getValue();
        String quantityText = quantityField.getText().trim();
        String unitPriceText = unitPriceField.getText().trim();

        if (selectedRawStock == null || quantityText.isEmpty() || unitPriceText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select raw stock and enter quantity and unit price");
            return;
        }

        try {
            double quantity = Double.parseDouble(quantityText);
            double unitPrice = Double.parseDouble(unitPriceText);

            if (quantity <= 0 || unitPrice <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Quantity and unit price must be positive numbers");
                return;
            }

            // Find the selected raw stock details
            Object[] selectedStock = null;
            for (Object[] stock : rawStocks) {
                String displayName = String.format("%s (%s - %s)", stock[1], stock[2], stock[3]);
                if (displayName.equals(selectedRawStock)) {
                    selectedStock = stock;
                    break;
                }
            }

            if (selectedStock != null) {
                // Check if item already exists in table
                for (RawStockPurchaseItem existingItem : itemsTable.getItems()) {
                    if (existingItem.getRawStockId() == (Integer) selectedStock[0]) {
                        showAlert(Alert.AlertType.ERROR, "Error", "This raw stock is already in the invoice. Please edit the existing item or remove it first.");
                        return;
                    }
                }

                RawStockPurchaseItem item = new RawStockPurchaseItem(
                    (Integer) selectedStock[0],
                    (String) selectedStock[1],
                    (String) selectedStock[2],
                    (String) selectedStock[3],
                    (String) selectedStock[4],
                    quantity,
                    unitPrice
                );

                itemsTable.getItems().add(item);
                updateTotalLabel(itemsTable, null, totalLabel);

                // Clear fields
                rawStockCombo.setValue(null);
                quantityField.clear();
                unitPriceField.clear();
            }

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for quantity and unit price");
        }
    }

    private static void updateTotalLabel(TableView<RawStockPurchaseItem> itemsTable, TextField discountField, Label totalLabel) {
        double subtotal = itemsTable.getItems().stream()
            .mapToDouble(RawStockPurchaseItem::getTotalPrice)
            .sum();

        double discount = 0.0;
        if (discountField != null && !discountField.getText().trim().isEmpty()) {
            try {
                discount = Double.parseDouble(discountField.getText().trim());
            } catch (NumberFormatException e) {
                // Ignore invalid discount
            }
        }

        double total = subtotal - discount;
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private static void handleEnhancedPurchaseInvoiceSubmit(
        TextField invoiceNumberField, ComboBox<String> supplierCombo, DatePicker invoiceDatePicker,
        TableView<RawStockPurchaseItem> itemsTable, TextField discountField, 
        TextField paidAmountField, Label totalLabel) {

        String invoiceNumber = invoiceNumberField.getText().trim();
        String selectedSupplier = supplierCombo.getValue();
        String invoiceDate = invoiceDatePicker.getValue().format(DATE_FORMATTER);

        if (selectedSupplier == null || itemsTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a supplier and add at least one item");
            return;
        }

        try {
            double discount = discountField.getText().trim().isEmpty() ? 0.0 : 
                             Double.parseDouble(discountField.getText().trim());
            double paidAmount = paidAmountField.getText().trim().isEmpty() ? 0.0 : 
                               Double.parseDouble(paidAmountField.getText().trim());

            // Calculate total
            double subtotal = itemsTable.getItems().stream()
                .mapToDouble(RawStockPurchaseItem::getTotalPrice)
                .sum();
            double totalAmount = subtotal - discount;

            if (discount < 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Discount cannot be negative");
                return;
            }

            if (paidAmount < 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Paid amount cannot be negative");
                return;
            }

            // Convert table items to list
            List<RawStockPurchaseItem> items = new ArrayList<>(itemsTable.getItems());

            // Use simplified invoice insertion
            boolean success = database.insertSimpleRawPurchaseInvoice(
                invoiceNumber, selectedSupplier, invoiceDate, totalAmount, discount, paidAmount, items
            );

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    String.format("Purchase Invoice %s created successfully!\nTotal Amount: $%.2f", 
                    invoiceNumber, totalAmount));

                // Clear form
                clearPurchaseInvoiceForm(invoiceNumberField, supplierCombo, itemsTable, 
                                       discountField, paidAmountField, totalLabel);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create purchase invoice");
            }

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for discount and paid amount");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error saving to database: " + ex.getMessage());
        }
    }

    private static void clearPurchaseInvoiceForm(TextField invoiceNumberField, ComboBox<String> supplierCombo,
                                               TableView<RawStockPurchaseItem> itemsTable, TextField discountField,
                                               TextField paidAmountField, Label totalLabel) {
        // Generate new invoice number
        invoiceNumberField.setText(database.generateNextInvoiceNumber("RPI"));
        supplierCombo.setValue(null);
        itemsTable.getItems().clear();
        discountField.setText("0");
        paidAmountField.setText("0");
        updateTotalLabel(itemsTable, discountField, totalLabel);
    }

    // Form submission handlers
    private static void handleRawStockSubmit(
        TextField nameField, ComboBox<String> brandCombo, ComboBox<String> supplierCombo,
        TextField quantityField, TextField unitPriceField,
        TableView<RawStockRecord> stockTable
    ) {
        String name = nameField.getText().trim();
        String brand = brandCombo.getValue();
        String supplier = supplierCombo.getValue(); // Optional
        String quantityText = quantityField.getText().trim();
        String unitPriceText = unitPriceField.getText().trim();

        if (name.isEmpty() || brand == null || unitPriceText.isEmpty() || quantityText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name, Brand, Quantity, and Unit Price are required");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            double unitPrice = Double.parseDouble(unitPriceText);
            
            // Insert into RawStock table using the existing database method
            boolean success = database.insertRawStock(name, "", brand, "", quantity, unitPrice, 0.0);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Raw stock registered successfully!");
                
                // Clear fields
                nameField.clear();
                brandCombo.setValue(null);
                supplierCombo.setValue(null);
                quantityField.clear();
                unitPriceField.clear();
                
                // Refresh table
                refreshRawStockTable(stockTable);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register raw stock. Please check your entries.");
            }
            
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity must be a whole number and Unit Price must be a valid number");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + ex.getMessage());
        }
    }

    private static void handlePurchaseInvoiceSubmit(
        TextField invoiceNumberField, TextField supplierField, DatePicker invoiceDatePicker,
        TextField stockField, TextField quantityField, TextField unitPriceField,
        TextField discountField, TextField paidAmountField, ListView<String> invoiceView
    ) {
        if (invoiceNumberField.getText().trim().isEmpty() ||
            supplierField.getText().trim().isEmpty() ||
            stockField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Required fields are missing");
            return;
        }

        try {
            double quantity = Double.parseDouble(quantityField.getText().trim());
            double unitPrice = Double.parseDouble(unitPriceField.getText().trim());
            double discount = Double.parseDouble(discountField.getText().trim());
            double paid = Double.parseDouble(paidAmountField.getText().trim());
            double total = quantity * unitPrice - discount;

            String invoiceEntry = String.format("%s | %s | %s | %s x %s = %s | Paid: %s",
                invoiceNumberField.getText().trim(),
                invoiceDatePicker.getValue().format(DATE_FORMATTER),
                supplierField.getText().trim(),
                quantity,
                unitPrice,
                total,
                paid);
            
            invoiceView.getItems().add(invoiceEntry);
            
            // Clear fields
            invoiceNumberField.clear();
            supplierField.clear();
            stockField.clear();
            quantityField.clear();
            unitPriceField.clear();
            discountField.setText("0");
            paidAmountField.setText("0");
            invoiceDatePicker.setValue(LocalDate.now());

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers");
        }
    }

    // --------------------------
    // Raw Stock Use Invoice Helper Methods
    // --------------------------
    
    /**
     * Load raw stocks into the combo box for selection
     */
    private static void loadRawStocksIntoComboBox(ComboBox<RawStockUseItem> comboBox) {
        try {
            List<Object[]> rawStocks = database.getAllRawStocksWithUnitsForDropdown();
            ObservableList<RawStockUseItem> items = FXCollections.observableArrayList();
            
            for (Object[] row : rawStocks) {
                int rawStockId = (Integer) row[0];
                String rawStockName = (String) row[1];
                String categoryName = (String) row[2];
                String brandName = (String) row[3];
                String unitName = (String) row[4];
                double availableQuantity = (Double) row[5];
                double unitCost = (Double) row[6];
                
                RawStockUseItem item = new RawStockUseItem(
                    rawStockId, rawStockName, categoryName, brandName, 
                    unitName, 0.0, unitCost, availableQuantity
                );
                items.add(item);
            }
            
            comboBox.setItems(items);
            
            // Custom cell factory to display formatted text
            comboBox.setCellFactory(listView -> new ListCell<RawStockUseItem>() {
                @Override
                protected void updateItem(RawStockUseItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getDisplayString());
                    }
                }
            });
            
            comboBox.setButtonCell(new ListCell<RawStockUseItem>() {
                @Override
                protected void updateItem(RawStockUseItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select Raw Stock Item");
                    } else {
                        setText(item.getDisplayString());
                    }
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load raw stock items: " + e.getMessage());
        }
    }
    
    /**
     * Create table for displaying available raw stock items
     */
    private static TableView<RawStockUseItem> createRawStockItemsTable() {
        TableView<RawStockUseItem> table = new TableView<>();
        
        TableColumn<RawStockUseItem, String> nameCol = new TableColumn<>("Stock Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("rawStockName"));
        nameCol.setPrefWidth(180); // Reduced from 200
        
        TableColumn<RawStockUseItem, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        brandCol.setPrefWidth(120); // Reduced from 150
        
        TableColumn<RawStockUseItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryCol.setPrefWidth(100); // Reduced from 120
        
        TableColumn<RawStockUseItem, Double> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        availableCol.setPrefWidth(80); // Reduced from 100
        
        TableColumn<RawStockUseItem, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitName"));
        unitCol.setPrefWidth(60); // Reduced from 80
        
        TableColumn<RawStockUseItem, Double> costCol = new TableColumn<>("Unit Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        costCol.setPrefWidth(80); // Reduced from 100
        
        // Format currency columns
        availableCol.setCellFactory(col -> new TableCell<RawStockUseItem, Double>() {
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
        
        costCol.setCellFactory(col -> new TableCell<RawStockUseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        table.getColumns().addAll(nameCol, brandCol, categoryCol, availableCol, unitCol, costCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }
    
    /**
     * Create table for displaying selected usage items
     */
    private static TableView<RawStockUseItem> createSelectedUsageItemsTable() {
        TableView<RawStockUseItem> table = new TableView<>();
        
        TableColumn<RawStockUseItem, String> nameCol = new TableColumn<>("Stock Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("rawStockName"));
        nameCol.setPrefWidth(180); // Reduced from 200
        
        TableColumn<RawStockUseItem, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        brandCol.setPrefWidth(120); // Reduced from 150
        
        TableColumn<RawStockUseItem, Double> quantityCol = new TableColumn<>("Qty Used");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantityUsed"));
        quantityCol.setPrefWidth(80); // Reduced from 100
        
        TableColumn<RawStockUseItem, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitName"));
        unitCol.setPrefWidth(60); // Reduced from 80
        
        TableColumn<RawStockUseItem, Double> costCol = new TableColumn<>("Unit Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        costCol.setPrefWidth(80); // Reduced from 100
        
        TableColumn<RawStockUseItem, Double> totalCol = new TableColumn<>("Total Cost");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        totalCol.setPrefWidth(80); // Reduced from 100
        
        // Format numeric columns
        quantityCol.setCellFactory(col -> new TableCell<RawStockUseItem, Double>() {
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
        
        costCol.setCellFactory(col -> new TableCell<RawStockUseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        totalCol.setCellFactory(col -> new TableCell<RawStockUseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        table.getColumns().addAll(nameCol, brandCol, quantityCol, unitCol, costCol, totalCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }
    
    /**
     * Load available raw stock items into the table
     */
    private static void loadAvailableRawStockItems(TableView<RawStockUseItem> table) {
        try {
            List<Object[]> rawStocks = database.getAllRawStocksWithUnitsForDropdown();
            ObservableList<RawStockUseItem> items = FXCollections.observableArrayList();
            
            for (Object[] row : rawStocks) {
                int rawStockId = (Integer) row[0];
                String rawStockName = (String) row[1];
                String categoryName = (String) row[2];
                String brandName = (String) row[3];
                String unitName = (String) row[4];
                double availableQuantity = (Double) row[5];
                double unitCost = (Double) row[6];
                
                RawStockUseItem item = new RawStockUseItem(
                    rawStockId, rawStockName, categoryName, brandName, 
                    unitName, 0.0, unitCost, availableQuantity
                );
                items.add(item);
            }
            
            table.setItems(items);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load raw stock items: " + e.getMessage());
        }
    }
    
    /**
     * Show dialog to enter usage quantity and confirm item addition
     */
    private static void showUsageQuantityDialog(RawStockUseItem item, TableView<RawStockUseItem> selectedTable, Label totalLabel) {
        Dialog<RawStockUseItem> dialog = new Dialog<>();
        dialog.setTitle("Enter Usage Quantity");
        dialog.setHeaderText("Add item to usage invoice");
        
        // Create dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label itemLabel = new Label("Item: " + item.getRawStockName() + " (" + item.getBrandName() + ")");
        Label availableLabel = new Label("Available Quantity: " + item.getAvailableQuantity() + " " + item.getUnitName());
        Label costLabel = new Label("Unit Cost: $" + String.format("%.2f", item.getUnitCost()));
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter quantity to use");
        
        Label totalCostLabel = new Label("Total Cost: $0.00");
        totalCostLabel.setStyle("-fx-font-weight: bold;");
        
        // Update total cost as user types
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (!newVal.isEmpty()) {
                    double qty = Double.parseDouble(newVal);
                    double total = qty * item.getUnitCost();
                    totalCostLabel.setText("Total Cost: $" + String.format("%.2f", total));
                } else {
                    totalCostLabel.setText("Total Cost: $0.00");
                }
            } catch (NumberFormatException e) {
                totalCostLabel.setText("Total Cost: Invalid");
            }
        });
        
        content.getChildren().addAll(itemLabel, availableLabel, costLabel, 
            new Label("Quantity to Use:"), quantityField, totalCostLabel);
        
        dialog.getDialogPane().setContent(content);
        
        // Add buttons
        ButtonType addButtonType = new ButtonType("Add Item", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Enable/disable add button based on input
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (!newVal.trim().isEmpty()) {
                    double qty = Double.parseDouble(newVal.trim());
                    addButton.setDisable(qty <= 0 || qty > item.getAvailableQuantity());
                } else {
                    addButton.setDisable(true);
                }
            } catch (NumberFormatException e) {
                addButton.setDisable(true);
            }
        });
        
        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double quantity = Double.parseDouble(quantityField.getText().trim());
                    if (quantity > 0 && quantity <= item.getAvailableQuantity()) {
                        return item.createUseItem(quantity);
                    }
                } catch (NumberFormatException e) {
                    // Invalid input
                }
            }
            return null;
        });
        
        Optional<RawStockUseItem> result = dialog.showAndWait();
        result.ifPresent(useItem -> {
            // Check if item already exists in selected table
            boolean found = false;
            for (RawStockUseItem existingItem : selectedTable.getItems()) {
                if (existingItem.getRawStockId() == useItem.getRawStockId()) {
                    // Update quantity instead of adding duplicate
                    double newQuantity = existingItem.getQuantityUsed() + useItem.getQuantityUsed();
                    if (newQuantity <= item.getAvailableQuantity()) {
                        existingItem.setQuantityUsed(newQuantity);
                        found = true;
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Quantity Exceeded", 
                            "Total quantity would exceed available stock.");
                        return;
                    }
                    break;
                }
            }
            
            if (!found) {
                selectedTable.getItems().add(useItem);
            }
            
            selectedTable.refresh();
            updateTotalUsageAmount(selectedTable, totalLabel);
        });
    }
    
    /**
     * Update the total usage amount label
     */
    private static void updateTotalUsageAmount(TableView<RawStockUseItem> selectedTable, Label totalLabel) {
        double total = selectedTable.getItems().stream()
                .mapToDouble(item -> item.getQuantityUsed() * item.getUnitCost())
                .sum();
        totalLabel.setText("Total Usage Amount: $" + String.format("%.2f", total));
    }
    
    /**
     * Create table for displaying previous use invoices
     */
    private static TableView<Object[]> createPreviousUseInvoicesTable() {
        TableView<Object[]> table = new TableView<>();
        table.setPrefHeight(120);
        table.setMaxHeight(120);
        
        TableColumn<Object[], String> invoiceCol = new TableColumn<>("Invoice #");
        invoiceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue()[0]));
        invoiceCol.setPrefWidth(120); // Reduced from 150
        
        TableColumn<Object[], String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue()[1]));
        dateCol.setPrefWidth(100); // Reduced from 120
        
        TableColumn<Object[], String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            String.format("$%.2f", (Double) data.getValue()[2])));
        amountCol.setPrefWidth(100); // Reduced from 120
        
        TableColumn<Object[], String> purposeCol = new TableColumn<>("Purpose");
        purposeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue()[3]));
        purposeCol.setPrefWidth(200); // Same width
        
        table.getColumns().addAll(invoiceCol, dateCol, amountCol, purposeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }
    
    /**
     * Load previous use invoices into the table
     */
    private static void loadPreviousUseInvoices(TableView<Object[]> table) {
        try {
            List<Object[]> invoices = database.getAllRawStockUseInvoices();
            table.setItems(FXCollections.observableArrayList(invoices));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load previous invoices: " + e.getMessage());
        }
    }
    
    /**
     * Handle the submission of raw stock use invoice
     */
    private static void handleRawStockUseInvoiceSubmit(TextField invoiceNumberField, DatePicker usageDatePicker,
                                                      TextField referencePurposeField, TableView<RawStockUseItem> selectedItemsTable,
                                                      Label totalLabel) {
        // Validate inputs
        if (invoiceNumberField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invoice number is required.");
            return;
        }
        
        if (usageDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Usage date is required.");
            return;
        }
        
        if (selectedItemsTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "At least one item must be selected.");
            return;
        }
        
        try {
            String invoiceNumber = invoiceNumberField.getText().trim();
            String usageDate = usageDatePicker.getValue().format(DATE_FORMATTER);
            String referencePurpose = referencePurposeField.getText().trim();
            
            // Calculate total amount
            double totalAmount = selectedItemsTable.getItems().stream()
                    .mapToDouble(item -> item.getQuantityUsed() * item.getUnitCost())
                    .sum();
            
            // Insert invoice and get ID
            int invoiceId = database.insertRawStockUseInvoiceAndGetId(invoiceNumber, usageDate, totalAmount, referencePurpose);
            
            if (invoiceId > 0) {
                // Insert invoice items
                List<RawStockUseItem> items = new ArrayList<>(selectedItemsTable.getItems());
                boolean itemsInserted = database.insertRawStockUseInvoiceItems(invoiceId, items);
                
                if (itemsInserted) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Raw Stock Use Invoice created successfully!\nInvoice Number: " + invoiceNumber);
                    
                    // Clear the form
                    clearUseInvoiceForm(invoiceNumberField, usageDatePicker, referencePurposeField, 
                                      selectedItemsTable, totalLabel);
                    
                } else {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save invoice items.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create invoice.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Clear the use invoice form after successful submission
     */
    private static void clearUseInvoiceForm(TextField invoiceNumberField, DatePicker usageDatePicker,
                                          TextField referencePurposeField, TableView<RawStockUseItem> selectedItemsTable,
                                          Label totalLabel) {
        // Generate new invoice number
        String newInvoiceNumber = database.generateUseInvoiceNumber();
        invoiceNumberField.setText(newInvoiceNumber);
        
        // Reset date
        usageDatePicker.setValue(LocalDate.now());
        
        // Clear reference
        referencePurposeField.clear();
        
        // Clear selected items
        selectedItemsTable.getItems().clear();
        
        // Reset total
        totalLabel.setText("Total Usage Amount: $0.00");
    }

    private static void handleUsageInvoiceSubmit(
        TextField stockField, DatePicker usageDatePicker, TextField quantityField,
        TextField referenceField, ListView<String> usageView
    ) {
        if (stockField.getText().trim().isEmpty() ||
            quantityField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Stock and Quantity are required");
            return;
        }

        try {
            String usageEntry = String.format("%s | %s | Qty: %s | Ref: %s",
                stockField.getText().trim(),
                usageDatePicker.getValue().format(DATE_FORMATTER),
                quantityField.getText().trim(),
                referenceField.getText().trim());
            
            usageView.getItems().add(usageEntry);
            
            // Clear fields
            stockField.clear();
            quantityField.clear();
            referenceField.clear();
            usageDatePicker.setValue(LocalDate.now());

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please check your entries");
        }
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Model class for Raw Stock records
    public static class RawStockRecord {
        private final Integer id;
        private final String name;
        private final String brand;
        private final Double quantity;
        private final Double unitPrice;
        private final Double totalCost;

        public RawStockRecord(Integer id, String name, String brand,
                             Double quantity, Double unitPrice, Double totalCost) {
            this.id = id;
            this.name = name;
            this.brand = brand;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalCost = totalCost;
        }

        public Integer getId() { return id; }
        public String getName() { return name; }
        public String getBrand() { return brand; }
        public Double getQuantity() { return quantity; }
        public Double getUnitPrice() { return unitPrice; }
        public Double getTotalCost() { return totalCost; }
    }
}
