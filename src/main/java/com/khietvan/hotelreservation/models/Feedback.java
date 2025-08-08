package com.khietvan.hotelreservation.models;

public class Feedback {
    private int feedbackId;
    private int guestId;
    private int reservationId;
    private String comments;
    private int rating; // 1 to 5

    public Feedback(int feedbackId, int guestId, int reservationId, String comments, int rating) {
        this.feedbackId = feedbackId;
        this.guestId = guestId;
        this.reservationId = reservationId;
        this.comments = comments;
        this.rating = rating;
    }

    public String getFeedbackDetails() {
        return "FeedbackID: " + feedbackId +
                ", GuestID: " + guestId +
                ", ReservationID: " + reservationId +
                ", Rating: " + rating +
                "/5, Comments: " + comments;
    }
}
