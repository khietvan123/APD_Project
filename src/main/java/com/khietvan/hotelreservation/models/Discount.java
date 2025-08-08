package com.khietvan.hotelreservation.models;

public class Discount {
    private int discountId;     // Optional: useful if storing in DB
    private double amount;      // e.g. 10.5 means 10.5%
    private String title;       // Reason for discount

    public Discount(int discountId, double amount, String title) {
        this.discountId = discountId;
        this.amount = amount;
        this.title = title;
    }

    public Discount(double amount, String title) {
        this.amount = amount;
        this.title = title;
    }

    public int getDiscountId() {
        return discountId;
    }

    public void setDiscountId(int discountId) {
        this.discountId = discountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title + " (" + amount + "%)";
    }
}
