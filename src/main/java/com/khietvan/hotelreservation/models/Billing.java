package com.khietvan.hotelreservation.models;

public class Billing {
    private int billId;
    private int reservationId;
    private double amount;
    private double tax = 0.13;
    private double discount;
    private double totalAmount;

    public Billing(int billId, int reservationId, double amount, double tax, double discount) {
        this.billId = billId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.tax = tax;
        this.discount = discount;
        calculateTotal();
    }

    private void calculateTotal() {
        this.totalAmount = (amount + tax) - discount;
    }

    public double getTotalAmount() { return totalAmount; }

    public String printBill() {
        return "BillID: " + billId +
                ", ReservationID: " + reservationId +
                ", Amount: $" + amount +
                ", Tax: $" + tax +
                ", Discount: $" + discount +
                ", Total: $" + totalAmount;
    }
}
