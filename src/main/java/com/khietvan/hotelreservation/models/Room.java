package com.khietvan.hotelreservation.models;

public class Room {
    private int roomId;
    private RoomType roomType;
    private int numberOfBeds;
    private double price;
    private boolean status; // true = Available, false = Booked
    private int minGuests;
    private int maxGuests;

    public Room(int roomId, RoomType roomType, int numberOfBeds, double price, boolean status) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.price = price;
        this.status = status;
        setGuestLimitsByRoomType(roomType);
    }

    public Room(int roomId, RoomType roomType, int numberOfBeds, double price, boolean status, int minGuests, int maxGuests) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.price = price;
        this.status = status;
        this.minGuests = minGuests;
        this.maxGuests = maxGuests;
    }


    private void setGuestLimitsByRoomType(RoomType roomType) {
        switch (roomType) {
            case SINGLE -> {
                this.minGuests = 1;
                this.maxGuests = 2;
            }
            case DOUBLE -> {
                this.minGuests = 2;
                this.maxGuests = 4;
            }
            case DELUX, PENTHOUSE -> {
                this.minGuests = 1;
                this.maxGuests = 2;
            }
        }
    }

    public void setGuestLimits(int minGuests, int maxGuests) {
        this.minGuests = minGuests;
        this.maxGuests = maxGuests;
    }

    public int getRoomId() {
        return roomId;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public int getNumberOfBeds() {
        return numberOfBeds;
    }

    public double getPrice() {
        return price;
    }

    // Return status as boolean
    public boolean isAvailable() {
        return status;
    }

    // Return status as String for UI or logging
    public String getStatus() {
        return status ? "Available" : "Booked";
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getMinGuests() {
        return minGuests;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    public String getRoomDetails() {
        return "RoomID: " + roomId +
                ", Type: " + roomType +
                ", Beds: " + numberOfBeds +
                ", Price: $" + price +
                ", Guests: Min " + minGuests + ", Max " + maxGuests +
                ", Status: " + getStatus();
    }
}
