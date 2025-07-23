package com.cablemanagement.model;

import javafx.beans.property.*;

public class SalesInvoiceItem {
    private final IntegerProperty id;
    private final IntegerProperty salesInvoiceId;
    private final IntegerProperty productionStockId;
    private final StringProperty productName;
    private final DoubleProperty quantity;
    private final DoubleProperty unitPrice;
    private final DoubleProperty totalPrice;

    public SalesInvoiceItem(int id, int salesInvoiceId, int productionStockId, String productName,
                           double quantity, double unitPrice) {
        this.id = new SimpleIntegerProperty(id);
        this.salesInvoiceId = new SimpleIntegerProperty(salesInvoiceId);
        this.productionStockId = new SimpleIntegerProperty(productionStockId);
        this.productName = new SimpleStringProperty(productName);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.totalPrice = new SimpleDoubleProperty(quantity * unitPrice);
    }

    // Constructor without ID for new items
    public SalesInvoiceItem(int salesInvoiceId, int productionStockId, String productName,
                           double quantity, double unitPrice) {
        this(0, salesInvoiceId, productionStockId, productName, quantity, unitPrice);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty salesInvoiceIdProperty() { return salesInvoiceId; }
    public IntegerProperty productionStockIdProperty() { return productionStockId; }
    public StringProperty productNameProperty() { return productName; }
    public DoubleProperty quantityProperty() { return quantity; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }

    // Value getters
    public int getId() { return id.get(); }
    public int getSalesInvoiceId() { return salesInvoiceId.get(); }
    public int getProductionStockId() { return productionStockId.get(); }
    public String getProductName() { return productName.get(); }
    public double getQuantity() { return quantity.get(); }
    public double getUnitPrice() { return unitPrice.get(); }
    public double getTotalPrice() { return totalPrice.get(); }

    // Value setters
    public void setId(int id) { this.id.set(id); }
    public void setSalesInvoiceId(int salesInvoiceId) { this.salesInvoiceId.set(salesInvoiceId); }
    public void setProductionStockId(int productionStockId) { this.productionStockId.set(productionStockId); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public void setQuantity(double quantity) { 
        this.quantity.set(quantity);
        this.totalPrice.set(quantity * this.unitPrice.get());
    }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice.set(unitPrice);
        this.totalPrice.set(this.quantity.get() * unitPrice);
    }
}
