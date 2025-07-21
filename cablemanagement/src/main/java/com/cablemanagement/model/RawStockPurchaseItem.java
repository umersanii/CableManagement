package com.cablemanagement.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;

public class RawStockPurchaseItem {
    private final IntegerProperty rawStockId = new SimpleIntegerProperty();
    private final StringProperty rawStockName = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final StringProperty brand = new SimpleStringProperty();
    private final StringProperty unit = new SimpleStringProperty();
    private final DoubleProperty quantity = new SimpleDoubleProperty();
    private final DoubleProperty unitPrice = new SimpleDoubleProperty();
    private final DoubleProperty totalPrice = new SimpleDoubleProperty();

    public RawStockPurchaseItem(int rawStockId, String rawStockName, String category, 
                               String brand, String unit, double quantity, double unitPrice) {
        this.rawStockId.set(rawStockId);
        this.rawStockName.set(rawStockName);
        this.category.set(category);
        this.brand.set(brand);
        this.unit.set(unit);
        this.quantity.set(quantity);
        this.unitPrice.set(unitPrice);
        this.totalPrice.set(quantity * unitPrice);
    }

    // Property getters
    public IntegerProperty rawStockIdProperty() { return rawStockId; }
    public StringProperty rawStockNameProperty() { return rawStockName; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty brandProperty() { return brand; }
    public StringProperty unitProperty() { return unit; }
    public DoubleProperty quantityProperty() { return quantity; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }

    // Value getters
    public int getRawStockId() { return rawStockId.get(); }
    public String getRawStockName() { return rawStockName.get(); }
    public String getCategory() { return category.get(); }
    public String getBrand() { return brand.get(); }
    public String getUnit() { return unit.get(); }
    public double getQuantity() { return quantity.get(); }
    public double getUnitPrice() { return unitPrice.get(); }
    public double getTotalPrice() { return totalPrice.get(); }

    // Value setters
    public void setQuantity(double quantity) { 
        this.quantity.set(quantity); 
        updateTotalPrice();
    }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice.set(unitPrice); 
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        this.totalPrice.set(this.quantity.get() * this.unitPrice.get());
    }

    @Override
    public String toString() {
        return String.format("%s - %.2f %s x %.2f = %.2f", 
                rawStockName.get(), quantity.get(), unit.get(), unitPrice.get(), totalPrice.get());
    }
}
