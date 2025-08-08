package com.khietvan.hotelreservation.controllers.Admin;

import com.khietvan.hotelreservation.dao.GuestDAO;
import com.khietvan.hotelreservation.dao.ReservationDAO;
import com.khietvan.hotelreservation.models.Discount;
import com.khietvan.hotelreservation.models.Guest;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AdminManagementController {

    @FXML private TextField searchNameField;
    @FXML private TextField searchPhoneField;

    @FXML private TableView<Guest> guestTable;
    @FXML private TableColumn<Guest, String> nameColumn;
    @FXML private TableColumn<Guest, String> phoneColumn;
    @FXML private TableColumn<Guest, String> emailColumn;
    @FXML private TableColumn<Guest, String> discountColumn;
    @FXML private TableColumn<Guest, String> reservationStatus;   // NEW

    @FXML private Button checkInBtn;   // wire this in FXML
    @FXML private Button backToMainBtn;
    @FXML private Button offerDiscountBtn;
    @FXML private Button modifyBtn;
    @FXML private Button cancelBtn;

    private final ObservableList<Guest> guestList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Basic guest columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        discountColumn.setCellValueFactory(cellData -> {
            Discount d = cellData.getValue().getDiscount();
            return new SimpleStringProperty(d != null ? d.toString() : "None");
        });

        reservationStatus.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getReservationStatusText())
        );

        guestTable.setItems(guestList);
        loadGuests();

        // Live filter
        searchNameField.textProperty().addListener((obs, o, n) -> loadGuests());
        searchPhoneField.textProperty().addListener((obs, o, n) -> loadGuests());

        // Double click opens reservation details window
        guestTable.setRowFactory(tv -> {
            TableRow<Guest> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    openReservationInfo(row.getItem());
                }
            });
            return row;
        });

        // ✅ Check-in button: set reservation.status = true for selected guest
        if (checkInBtn != null) {
            checkInBtn.setOnAction(e -> handleCheckIn());
        }

        guestTable.setItems(guestList);
        loadGuests();

        // live filtering stays the same
        searchNameField.textProperty().addListener((obs, o, n) -> loadGuests());
        searchPhoneField.textProperty().addListener((obs, o, n) -> loadGuests());

        // Disable Offer Discount until a row is selected
        offerDiscountBtn.setDisable(true);
        guestTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            offerDiscountBtn.setDisable(newSel == null);
        });

        // double-click row to open the reservation info dialog (keeps your behavior)
        guestTable.setRowFactory(tv -> {
            TableRow<Guest> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    openReservationInfo(row.getItem());
                }
            });
            return row;
        });

        // Offer Discount action — requires a selected row
        offerDiscountBtn.setOnAction(e -> {
            Guest selected = guestTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                // This is just a guard; the button should be disabled anyway.
                new Alert(Alert.AlertType.INFORMATION, "Please select a guest first.").showAndWait();
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/khietvan/hotelreservation/discount.fxml"
                ));
                Parent root = loader.load();

                DiscountController ctrl = loader.getController();
                ctrl.setGuest(selected); // pass the selected guest

                Stage popup = new Stage();
                popup.setTitle("Offer Discount");
                popup.setScene(new Scene(root));
                popup.initOwner(offerDiscountBtn.getScene().getWindow());
                popup.showAndWait();

                // refresh the row to reflect any applied discount
                guestTable.refresh();

            } catch (IOException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Cannot open discount menu").showAndWait();
            }
        });
        modifyBtn.setOnAction(e -> openModifyForSelected());
        cancelBtn.setOnAction(e -> handleCancelCheckIn());
    }

    private void handleCancelCheckIn() {
        Guest g = guestTable.getSelectionModel().getSelectedItem();
        if (g == null) {
            new Alert(Alert.AlertType.INFORMATION, "Please select a customer row first.").showAndWait();
            return;
        }

        // Confirm #1
        Alert confirm1 = new Alert(Alert.AlertType.CONFIRMATION);
        confirm1.setTitle("Cancel Check-In");
        confirm1.setHeaderText(null);
        confirm1.setContentText("Do you want to cancel check-in for " + g.getName() + "?");
        var r1 = confirm1.showAndWait();
        if (r1.isEmpty() || r1.get() != ButtonType.OK) return;

        // Load active reservation (if any)
        var res = ReservationDAO.getReservationWithRoomsByGuestId(g.getGuestId());
        if (res == null) {
            new Alert(Alert.AlertType.INFORMATION, "No active reservation found for this customer.").showAndWait();
            return;
        }

        // Confirm #2 (OK = keep guest; Cancel = delete guest too)
        Alert confirm2 = new Alert(Alert.AlertType.CONFIRMATION);
        confirm2.setTitle("Keep Customer Info?");
        confirm2.setHeaderText(null);
        confirm2.setContentText("Do you want to keep customer information?\nPress Cancel to delete the customer as well.");
        ButtonType keepBtn = ButtonType.OK;          // keep guest, delete reservation only
        ButtonType deleteGuestBtn = ButtonType.CANCEL; // delete guest + reservation
        confirm2.getButtonTypes().setAll(keepBtn, deleteGuestBtn);
        var r2 = confirm2.showAndWait();
        if (r2.isEmpty()) return;

        boolean ok;
        if (r2.get() == keepBtn) {
            // delete reservation only
            ok = ReservationDAO.deleteReservationCascade(res.getReservationId());
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Reservation canceled. Guest information kept.").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to cancel reservation.").showAndWait();
                return;
            }
        } else {
            // delete reservation then guest
            ok = ReservationDAO.deleteReservationCascade(res.getReservationId());
            if (!ok) {
                new Alert(Alert.AlertType.ERROR, "Failed to cancel reservation. Customer not deleted.").showAndWait();
                return;
            }
            boolean guestDeleted = GuestDAO.deleteGuest(g.getGuestId());
            if (!guestDeleted) {
                new Alert(Alert.AlertType.ERROR, "Reservation canceled but failed to delete customer.").showAndWait();
                return;
            }
            new Alert(Alert.AlertType.INFORMATION, "Reservation canceled and customer deleted.").showAndWait();
        }

        loadGuests(); // refresh table
    }

    private void openReservationInfo(Guest guest) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/khietvan/hotelreservation/Admin/Reservation-information.fxml"
            ));
            Parent root = loader.load();

            ReservationInformationController ctrl = loader.getController();
            ctrl.loadData(guest);

            Stage popup = new Stage();
            popup.setScene(new Scene(root));
            popup.setTitle("Booking for " + guest.getName());
            popup.show();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot load reservation details.").showAndWait();
        }
    }

    private void loadGuests() {
        String name = searchNameField.getText();
        String phone = searchPhoneField.getText();
        List<Guest> guests = GuestDAO.searchGuests(name, phone);
        System.out.println("Loaded guests: " + guests.size());
        guestList.setAll(guests);
        guestTable.refresh(); // also refresh status col
    }

    private String getReservationStatusLabel(Guest g) {
        // Returns "Checked-in", "Pending", or "No reservation"
        Boolean status = ReservationDAO.getLatestReservationStatusByGuestId(g.getGuestId());
        if (status == null) return "No reservation";
        return status ? "Checked-in" : "Pending";
    }

    private void handleCheckIn() {
        Guest selected = guestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Please select a customer first.").showAndWait();
            return;
        }

        Integer latestResId = ReservationDAO.getLatestReservationIdByGuestId(selected.getGuestId());
        if (latestResId == null) {
            new Alert(Alert.AlertType.WARNING, "No reservation found for this customer.").showAndWait();
            return;
        }

        Boolean current = ReservationDAO.getLatestReservationStatusByGuestId(selected.getGuestId());
        if (Boolean.TRUE.equals(current)) {
            new Alert(Alert.AlertType.INFORMATION,
                    "This customer is already checked in.").showAndWait();
            return;
        }

        boolean ok = ReservationDAO.updateReservationStatus(latestResId, true);
        if (ok) {
            guestTable.refresh();
            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Successfully checked in.\nThank you, " + selected.getName() + ". Enjoy your stay!"
            ).showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to update reservation status.").showAndWait();
        }
    }

    private void openModifyForSelected() {
        Guest g = guestTable.getSelectionModel().getSelectedItem();
        if (g == null) {
            new Alert(Alert.AlertType.INFORMATION, "Please select a customer first.").showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/khietvan/hotelreservation/Admin/Reservation-modify.fxml"
            ));
            Parent root = loader.load();

            ReservationModifyController ctrl = loader.getController();
            ctrl.loadData(g);

            Stage popup = new Stage();
            popup.setTitle("Modify: " + g.getName());
            popup.setScene(new Scene(root));
            popup.showAndWait();     // wait so we can refresh

            // refresh table after editing
            loadGuests();

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot open modify screen.").showAndWait();
        }
    }

    @FXML
    private void handleBackToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/khietvan/hotelreservation/main-screen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backToMainBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Main Screen");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
