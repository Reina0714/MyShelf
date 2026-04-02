package com.example.myshelf;

public class InventoryItem {

    private int invId;
    private String itemName;
    private int quantity;
    private String expiryDate;
    private String category;
    private double price;
    private String status;

    // Empty constructor required by SQLite
    public InventoryItem() {
    }

    // Constructor without ID
    public InventoryItem(String itemName, int quantity, String expiryDate, String category, double price, String status) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.category = category;
        this.price = price;
        this.status = status;
    }

    // Constructor with ID
    public InventoryItem(int invId, String itemName, int quantity, String expiryDate, String category, double price, String status) {
        this.invId = invId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.category = category;
        this.price = price;
        this.status = status;
    }

    // Getters and setters
    public int getInvId() { return invId; }
    public void setInvId(int invId) { this.invId = invId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}