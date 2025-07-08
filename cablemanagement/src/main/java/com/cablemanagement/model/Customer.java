package com.cablemanagement.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Customer {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty contact = new SimpleStringProperty();

    public Customer(String name, String contact) {
        this.name.set(name);
        this.contact.set(contact);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty contactProperty() {
        return contact;
    }
}
