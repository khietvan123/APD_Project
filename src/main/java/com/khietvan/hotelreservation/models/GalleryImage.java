package com.khietvan.hotelreservation.models;

public class GalleryImage {
    private int id;
    private String address;
    private String description;

    public GalleryImage(int id, String address, String description) {
        this.id = id;
        this.address = address;
        this.description = description;
    }

    public int getId() { return id; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
}
