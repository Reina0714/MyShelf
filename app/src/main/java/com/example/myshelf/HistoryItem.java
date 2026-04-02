package com.example.myshelf;

public class HistoryItem {
    private int logId;
    private String itemName;
    private String actionDate;
    private String finalStatus;
    private double priceLoss;

    public HistoryItem(int logId, String itemName, String actionDate, String finalStatus, double priceLoss) {
        this.logId = logId;
        this.itemName = itemName;
        this.actionDate = actionDate;
        this.finalStatus = finalStatus;
        this.priceLoss = priceLoss;
    }

    public int getLogId() { return logId; }
    public String getItemName() { return itemName; }
    public String getActionDate() { return actionDate; }
    public String getFinalStatus() { return finalStatus; }
    public double getPriceLoss() { return priceLoss; }
}