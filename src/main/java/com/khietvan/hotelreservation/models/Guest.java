package com.khietvan.hotelreservation.models;

public class Guest {
    private int guestId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String feedBack;
    private Discount discount;

    // Only one reservation per guest
    private Reservation reservation;

    public Guest(int guestId, String name, String phone, String email, String address, String feedBack) {
        this.guestId = guestId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.feedBack = feedBack;
    }

    public Guest(int guestId, String name, String phone, String email, String address) {
        this(guestId, name, phone, email, address, "");
    }

    public Guest() {}

    // ---------------------- Discount ----------------------
    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public String getDiscountText() {
        if (discount != null) {
            return discount.getTitle() + " (" + discount.getAmount() + "%)";
        }
        return "";
    }

    // ---------------------- Getters & Setters ----------------------
    public int getGuestId() { return guestId; }
    public void setGuestId(int guestId) { this.guestId = guestId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getFeedBack() { return feedBack; }
    public void setFeedBack(String feedBack) { this.feedBack = feedBack; }

    // ---------------------- Reservation ----------------------
    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    // ---------------------- Helper ----------------------
    public String getGuestDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Guest ID: ").append(guestId).append("\n")
                .append("Name: ").append(name).append("\n")
                .append("Phone: ").append(phone).append("\n")
                .append("Email: ").append(email).append("\n")
                .append("Address: ").append(address).append("\n");

        if (discount != null) {
            sb.append("Discount: ").append(getDiscountText()).append("\n");
        }

        if (reservation != null) {
            sb.append("Reservation:\n")
                    .append(" - ").append(reservation.getReservationDetails()).append("\n");
        }

        return sb.toString();
    }

    public Guest setGuestDetails(int id, String name, String phone, String email, String address, String feedBack) {
        if (feedBack.isEmpty()) {
            return new Guest(id, name, phone, email, address);
        }
        return new Guest(id, name, phone, email, address, feedBack);
    }

    public String getReservationStatusText() {
        if (reservation == null) return "—";
        return reservation.isStatus() ? "Checked-in" : "Pending";
    }

    public boolean ValidateGuestDetails() {
        return true;
    }
}
