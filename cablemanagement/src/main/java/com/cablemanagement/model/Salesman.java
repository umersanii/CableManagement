package com.cablemanagement.model;

public class Salesman {
    private String name, contact, email, cnic, address;

    public Salesman(String name, String contact, String email, String cnic, String address) {
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.cnic = cnic;
        this.address = address;
    }

    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getEmail() { return email; }
    public String getCnic() { return cnic; }
    public String getAddress() { return address; }

    public void setName(String name) { this.name = name; }
    public void setContact(String contact) { this.contact = contact; }
    public void setEmail(String email) { this.email = email; }
    public void setCnic(String cnic) { this.cnic = cnic; }
    public void setAddress(String address) { this.address = address; }
}
