package com.khietvan.hotelreservation.models;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Reservation {
    private int reservationId;
    private int guestId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private boolean status = false; // e.g., "Booked", "CheckedIn", etc.

    // Map of Room ID -> Number of guests in that room
    private Map<Integer, Integer> reservedRooms = new HashMap<>();
    // roomId -> guestsInThatRoom
    private final Map<Integer, Integer> roomsAndGuestCounts = new LinkedHashMap<>();

    public Reservation(int reservationId, int guestId, LocalDate checkInDate, LocalDate checkOutDate, boolean status) {
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }
    public void addRoom(int roomId, int guestsInRoom) {
        reservedRooms.put(roomId, guestsInRoom);
    }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }
    public int getGuestId() { return guestId; }
    public void setGuestId(int guestId){this.guestId=guestId;}
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public String getStatus() { return status ? "Confirmed" : "Pending Staff Confirmation"; }
    public Map<Integer, Integer> getReservedRooms() { return reservedRooms; }

    public int getTotalGuests() {
        return reservedRooms.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String getReservationDetails() {
        return "ReservationID: " + reservationId +
                ", GuestID: " + guestId +
                ", CheckIn: " + checkInDate +
                ", CheckOut: " + checkOutDate +
                ", Total Guests: " + getTotalGuests() +
                ", Rooms: " + reservedRooms.toString();
    }

    public void setStatus(boolean status) { this.status = status; }

    public void setRoomsReserved(Map<Integer, Integer> rooms) {
        this.reservedRooms.clear();
        this.reservedRooms.putAll(rooms);
    }

    public Map<Integer,Integer> getRoomsAndGuestCounts() { return this.roomsAndGuestCounts; }

}
