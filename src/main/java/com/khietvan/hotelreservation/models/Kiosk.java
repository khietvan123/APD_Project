package com.khietvan.hotelreservation.models;

public class Kiosk {
    private int kioskId;
    private String location;

    public Kiosk(int kioskId, String location) {
        this.kioskId = kioskId;
        this.location = location;
    }

    public void displayWelcomeMessage() {
        System.out.println("Welcome to Hotel ABC! Use this kiosk to book your stay safely and quickly.");
    }

    public void guideBookingProcess() {
        System.out.println("Step 1: Enter your details\nStep 2: Select dates\nStep 3: Choose your room\nStep 4: Confirm booking");
    }
}
