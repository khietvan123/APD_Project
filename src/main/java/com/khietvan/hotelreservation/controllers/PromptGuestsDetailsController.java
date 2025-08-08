package com.khietvan.hotelreservation.controllers;

import com.khietvan.hotelreservation.dao.GuestDAO;
import com.khietvan.hotelreservation.dao.ReservationDAO;
import com.khietvan.hotelreservation.models.Guest;
import com.khietvan.hotelreservation.models.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Consumer;

public class PromptGuestsDetailsController {

    @FXML private TextField name;
    @FXML private TextField phone;
    @FXML private TextField email;
    @FXML private TextField address;

    @FXML private Button confirmBtn;
    @FXML private Button cancelBtn;

    private int adults;
    private int children;
    private int seniors;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Reservation reservation;

    public void setReservationData(int adults, int children, int seniors, int totalChildren,
                                   LocalDate checkIn, LocalDate checkOut, Reservation reservation) {
        this.adults = adults;
        this.children = children;
        this.seniors = seniors;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.reservation = reservation;

        if (reservation == null) {
            System.out.println("Reservation is NULL!");
        } else {
            System.out.println("=== Received Reservation in PromptGuestsDetailsController ===");
            System.out.println("Check-In: " + reservation.getCheckInDate());
            System.out.println("Check-Out: " + reservation.getCheckOutDate());
            System.out.println("Status: " + reservation.getStatus());
            System.out.println("Reserved Rooms:");
            for (Map.Entry<Integer, Integer> entry : reservation.getReservedRooms().entrySet()) {
                System.out.println(" - Room ID: " + entry.getKey() + ", Guests in Room: " + entry.getValue());
            }
        }
    }

    @FXML
    public void initialize() {
        confirmBtn.setOnAction(this::handleConfirm);
        cancelBtn.setOnAction(this::handleCancel);
    }

    private void handleConfirm(ActionEvent event) {
        String guestName = name.getText().trim();
        String guestPhone = phone.getText().trim();
        String guestEmail = email.getText().trim();
        String guestAddress = address.getText().trim();

        System.out.println("=== Guest Input Form Data ===");
        System.out.println("Name: " + guestName);
        System.out.println("Phone: " + guestPhone);
        System.out.println("Email: " + guestEmail);
        System.out.println("Address: " + guestAddress);

        if (guestName.isEmpty()) {
            showAlert("Please enter your name before confirming.");
            return;
        }

        // 1. Create and save the guest
        Guest guest = new Guest();
        guest.setName(guestName);
        guest.setPhone(guestPhone);
        guest.setEmail(guestEmail);
        guest.setAddress(guestAddress);
        guest.setFeedBack("");
        guest.setDiscount(null);

        Guest savedGuest = GuestDAO.saveGuestAndReturn(guest);
        if (savedGuest == null) {
            showAlert("Failed to save guest to database.");
            return;
        }
        guest = savedGuest; // updated guest with guest_id
        System.out.println("[DEBUG] Saved guest ID: " + guest.getGuestId());


        // 3. Save reservation
        reservation.setGuestId(guest.getGuestId());
        int reservationId = ReservationDAO.insertReservationWithRooms(reservation);
        System.out.println("[DEBUG] Reservation insert result: " + reservationId);
        if (reservationId == -1) {
            showAlert("Failed to save reservation.");
            return;
        }
        reservation.setReservationId(reservationId);
        guest.setReservation(reservation);

        System.out.println("=== Final Reservation Summary ===");
        System.out.println("Guest ID: " + guest.getGuestId());
        System.out.println("Guest Name: " + guest.getName());
        System.out.println("Reservation ID: " + reservation.getReservationId());
        System.out.println("Check-In: " + reservation.getCheckInDate());
        System.out.println("Check-Out: " + reservation.getCheckOutDate());
        System.out.println("Status: " + reservation.getStatus());
        System.out.println("Rooms Reserved:");
        for (Map.Entry<Integer, Integer> entry : reservation.getReservedRooms().entrySet()) {
            System.out.println(" - Room ID: " + entry.getKey() + ", Guests: " + entry.getValue());
        }

        // 4. Alert success
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Confirmed");
        alert.setHeaderText("Reservation Complete!");
        alert.setContentText("Thank you Mr/Ms " + guestName +
                " for your booking. Please reach out to the Staff Kiosk to confirm your Check-In.");
        alert.showAndWait();

        // 5. Back to main screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/khietvan/hotelreservation/main-screen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error returning to main screen.");
        }
    }

    private void handleCancel(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Check-In");
        alert.setHeaderText("Do you want to cancel the check-in?");
        alert.setContentText("Click OK to cancel and return to the main screen, or Cancel to stay here.");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);

        alert.showAndWait().ifPresent(type -> {
            if (type == okButton) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/com/khietvan/hotelreservation/main-screen.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error returning to main screen.");
                }
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private Consumer<Guest> onSubmitCallback;
    public void initGuestSubmission(Consumer<Guest> callback) {
        this.onSubmitCallback = callback;
    }
}
