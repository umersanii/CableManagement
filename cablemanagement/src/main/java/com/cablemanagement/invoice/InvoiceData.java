package com.cablemanagement.invoice;

import java.util.List;

public class InvoiceData {
    private int invoiceNumber;
    private String customerName;
    private String customerAddress;
    private String date;
    private List<Item> items;
    private double previousBalance;

    public InvoiceData(int invoiceNumber, String customerName, String customerAddress, String date, List<Item> items, double previousBalance) {
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.date = date;
        this.items = items;
        this.previousBalance = previousBalance;
    }

    public int getInvoiceNumber() {
        return invoiceNumber;
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
