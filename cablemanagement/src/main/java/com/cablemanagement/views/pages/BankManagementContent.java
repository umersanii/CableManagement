package com.cablemanagement.views.pages;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import com.cablemanagement.model.Bank;

public class BankManagementContent {

    private static final ObservableList<Bank> registeredBanks = FXCollections.observableArrayList();

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane contentArea = new StackPane();
        contentArea.getChildren().add(viewManageBanks());

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(72);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        addButton(buttonBar, "Manage Banks", () -> contentArea.getChildren().setAll(viewManageBanks()));
        addButton(buttonBar, "Bank Transactions", () -> contentArea.getChildren().setAll(viewTransactionSection()));
        addButton(buttonBar, "View Cash In Hand", () -> contentArea.getChildren().setAll(viewCashInHand()));
        addButton(buttonBar, "View Cash Ledger", () -> contentArea.getChildren().setAll(viewCashLedger()));

        mainLayout.setTop(scrollPane);
        mainLayout.setCenter(contentArea);

        return mainLayout;
    }

    private static void addButton(HBox bar, String label, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("register-button");
        btn.setOnAction(e -> action.run());
        bar.getChildren().add(btn);
    }

    private static VBox viewManageBanks() {
        VBox box = createSection("Manage Banks", "Register a new bank with full details and see the list.");

        TextField nameField = new TextField();
        nameField.getStyleClass().add("text-field");
        TextField branchField = new TextField();
        branchField.getStyleClass().add("text-field");
        TextField accountNumberField = new TextField();
        accountNumberField.getStyleClass().add("text-field");
        TextField accountHolderField = new TextField();
        accountHolderField.getStyleClass().add("text-field");

        Button registerBtn = new Button("Register Bank");
        registerBtn.getStyleClass().add("register-button");

        Button updateBtn = new Button("Update Bank");
        updateBtn.getStyleClass().add("update-button");
        updateBtn.setVisible(false);

        HBox row1 = new HBox(10,
                new VBox(new Label("Bank Name:"), nameField),
                new VBox(new Label("Branch:"), branchField)
        );

        HBox row2 = new HBox(10,
                new VBox(new Label("Account Number:"), accountNumberField),
                new VBox(new Label("Account Holder:"), accountHolderField)
        );

        HBox buttonRow = new HBox(10, registerBtn, updateBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        TableView<Bank> bankTable = new TableView<>(registeredBanks);
        bankTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bankTable.setPrefHeight(250);

        TableColumn<Bank, String> nameCol = new TableColumn<>("Bank Name");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));

        TableColumn<Bank, String> branchCol = new TableColumn<>("Branch");
        branchCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getBranch()));

        TableColumn<Bank, String> accNumCol = new TableColumn<>("Account Number");
        accNumCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAccountNumber()));

        TableColumn<Bank, String> holderCol = new TableColumn<>("Account Holder");
        holderCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAccountHolder()));

        TableColumn<Bank, Void> editCol = new TableColumn<>("Edit");
        editCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.getStyleClass().add("button");
                editButton.setOnAction(e -> {
                    Bank selected = getTableView().getItems().get(getIndex());
                    nameField.setText(selected.getName());
                    branchField.setText(selected.getBranch());
                    accountNumberField.setText(selected.getAccountNumber());
                    accountHolderField.setText(selected.getAccountHolder());

                    registerBtn.setVisible(false);
                    updateBtn.setVisible(true);

                    updateBtn.setOnAction(ev -> {
                        selected.setName(nameField.getText().trim());
                        selected.setBranch(branchField.getText().trim());
                        selected.setAccountNumber(accountNumberField.getText().trim());
                        selected.setAccountHolder(accountHolderField.getText().trim());
                        bankTable.refresh();

                        nameField.clear();
                        branchField.clear();
                        accountNumberField.clear();
                        accountHolderField.clear();

                        registerBtn.setVisible(true);
                        updateBtn.setVisible(false);
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton);
            }
        });

        bankTable.getColumns().addAll(nameCol, branchCol, accNumCol, holderCol, editCol);

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String branch = branchField.getText().trim();
            String accNum = accountNumberField.getText().trim();
            String holder = accountHolderField.getText().trim();

            if (!name.isEmpty() && !accNum.isEmpty()) {
                Bank bank = new Bank(name, branch, accNum, holder);
                registeredBanks.add(bank);

                nameField.clear();
                branchField.clear();
                accountNumberField.clear();
                accountHolderField.clear();
            }
        });

        VBox form = new VBox(15, row1, row2, buttonRow);
        form.setPadding(new Insets(10));

        box.getChildren().addAll(form, new Label("Registered Banks:"), bankTable);
        return box;
    }

    private static VBox viewTransactionSection() {
        VBox box = createSection("Bank Transactions", "Select a bank and perform a financial action.");

        ComboBox<Bank> bankSelector = new ComboBox<>(registeredBanks);
        bankSelector.setPromptText("Select Bank");
        bankSelector.setPrefWidth(250);
        bankSelector.getStyleClass().add("combo-box");

        ComboBox<String> actionSelector = new ComboBox<>();
        actionSelector.getItems().addAll("Deposit", "Withdraw", "Transfer Bank to Bank", "Transfer Bank to Cash");
        actionSelector.setPromptText("Select Action");
        actionSelector.setPrefWidth(250);
        actionSelector.getStyleClass().add("combo-box");

        HBox selectionRow = new HBox(20,
                new VBox(new Label("Bank:"), bankSelector),
                new VBox(new Label("Action:"), actionSelector)
        );
        selectionRow.setPadding(new Insets(10, 0, 10, 0));
        selectionRow.setAlignment(Pos.CENTER_LEFT);

        VBox dynamicFormArea = new VBox(10);
        dynamicFormArea.setPadding(new Insets(10, 0, 0, 0));

        actionSelector.setOnAction(e -> {
            String selectedAction = actionSelector.getValue();
            dynamicFormArea.getChildren().clear();
            if (selectedAction == null) return;

            switch (selectedAction) {
                case "Deposit":
                    dynamicFormArea.getChildren().add(depositForm(bankSelector));
                    break;
                case "Withdraw":
                    dynamicFormArea.getChildren().add(withdrawForm(bankSelector));
                    break;
                case "Transfer Bank to Bank":
                    dynamicFormArea.getChildren().add(transferBankToBankForm());
                    break;
                case "Transfer Bank to Cash":
                    dynamicFormArea.getChildren().add(transferBankToCashForm(bankSelector));
                    break;
            }
        });

        box.getChildren().addAll(selectionRow, dynamicFormArea);
        return box;
    }

    private static Node depositForm(ComboBox<Bank> bankSelector) {
        TextField amountField = new TextField();
        amountField.getStyleClass().add("text-field");

        Button submit = new Button("Deposit");
        submit.getStyleClass().add("button");

        VBox box = new VBox(10,
                new Label("Amount to Deposit:"), amountField,
                submit
        );
        return box;
    }

    private static Node withdrawForm(ComboBox<Bank> bankSelector) {
        TextField amountField = new TextField();
        amountField.getStyleClass().add("text-field");

        Button submit = new Button("Withdraw");
        submit.getStyleClass().add("button");

        VBox box = new VBox(10,
                new Label("Amount to Withdraw:"), amountField,
                submit
        );
        return box;
    }

    private static Node transferBankToBankForm() {
        ComboBox<Bank> fromBank = new ComboBox<>(registeredBanks);
        ComboBox<Bank> toBank = new ComboBox<>(registeredBanks);
        fromBank.getStyleClass().add("combo-box");
        toBank.getStyleClass().add("combo-box");

        TextField amount = new TextField();
        amount.getStyleClass().add("text-field");

        Button submit = new Button("Transfer");
        submit.getStyleClass().add("button");

        VBox box = new VBox(10,
                new Label("From Bank:"), fromBank,
                new Label("To Bank:"), toBank,
                new Label("Amount:"), amount,
                submit
        );
        return box;
    }

    private static Node transferBankToCashForm(ComboBox<Bank> bankSelector) {
        TextField amountField = new TextField();
        amountField.getStyleClass().add("text-field");

        Button submit = new Button("Transfer to Cash");
        submit.getStyleClass().add("button");

        VBox box = new VBox(10,
                new Label("Amount to Convert:"), amountField,
                submit
        );
        return box;
    }

    private static VBox viewCashInHand() {
        VBox box = createSection("Cash In Hand", "Shows the total available cash.");

        Label amount = new Label("Rs. 0.00");
        amount.getStyleClass().add("amount-label");

        box.getChildren().add(amount);
        return box;
    }

    private static VBox viewCashLedger() {
        VBox box = createSection("Cash Ledger", "Shows the list of all cash transactions.");

        TableView<String> ledgerTable = new TableView<>();
        ledgerTable.setPrefHeight(250);
        ledgerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper("Deposit"));

        TableColumn<String, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data -> new ReadOnlyStringWrapper("Rs. 500"));

        TableColumn<String, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new ReadOnlyStringWrapper("2025-07-12"));

        ledgerTable.getColumns().addAll(typeCol, amountCol, dateCol);
        box.getChildren().add(ledgerTable);
        return box;
    }

    private static VBox createSection(String title, String description) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.TOP_LEFT);
        box.getStyleClass().add("form-container");

        Label heading = new Label(title);
        heading.getStyleClass().add("form-heading");

        Label note = new Label(description);
        note.getStyleClass().add("form-subheading");

        box.getChildren().addAll(heading, note);
        return box;
    }
}
