package com.khietvan.hotelreservation.models;

public class Guest {
    private int guestId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String feedBack;

    public Guest(int guestId, String name, String phone, String email, String address, String feedBack) {
        this.guestId = guestId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.feedBack = feedBack;
    }

    public Guest(int guestId, String name, String phone, String email, String address) {
        this.guestId = guestId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFeedBack() {
        return feedBack;
    }

    public void setFeedBack(String feedBack) {
        this.feedBack = feedBack;
    }

    public String getGuestDetails(){
        return "";
    }

    public Guest setGuestDetails(int id, String name, String phone, String email, String address, String feedBack){
        if(feedBack.isEmpty()){ // for none feedback
            return new Guest(id, name, phone, email, address);
        }
        return new Guest(id, name, phone, email, address, feedBack);
    }

    public boolean ValidateGuestDetails(){
        return true;
    }
}
