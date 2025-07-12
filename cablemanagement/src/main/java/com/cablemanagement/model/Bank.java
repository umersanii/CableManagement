package com.cablemanagement.model;

public class Bank {
    private String name;
    private String branch;
    private String accountNumber;
    private String accountHolder;

    public Bank(String name, String branch, String accountNumber, String accountHolder) {
        this.name = name;
        this.branch = branch;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
    }

    public String getName() {
        return name;
    }

    public String getBranch() {
        return branch;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    @Override
    public String toString() {
        return name + " | " + branch + " | " + accountNumber + " | " + accountHolder;
    }

    public void setName(String name) { this.name = name; }
    public void setBranch(String branch) { this.branch = branch; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }

}
