package com.cablemanagement.invoice;

import java.util.ArrayList;
import java.util.List;

public class InvoiceData {
    private String invoiceID;
    private String customerName;
    private String customerAddress;
    private String date;
    private List<Item> items;
    private double previousBalance;

    public InvoiceData(String invoiceID, String customerName, String customerAddress, String date, List<Item> items, double previousBalance) {
        this.invoiceID = invoiceID;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.date = date;
        this.items = items;
        this.previousBalance = previousBalance;
    }

   public InvoiceData(String invoiceNumber, String date, String customerName, String customerAddress, 
                       double previousBalance, List<Item> items) {
        this.invoiceID = invoiceNumber; // Parse String to int
        this.date = date;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.previousBalance = previousBalance;
        this.items = items != null ? items : new ArrayList<>(); // Defensive initialization
    }

    public String getInvoiceNumber() {
        return invoiceID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getDate() {
        return date;
    }

    public List<Item> getItems() {
        return items;
    }

    public double getPreviousBalance() {
        return previousBalance;
    }
}
