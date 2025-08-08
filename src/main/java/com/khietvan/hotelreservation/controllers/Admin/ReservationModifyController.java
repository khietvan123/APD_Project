package com.khietvan.hotelreservation.controllers.Admin;

import com.khietvan.hotelreservation.dao.GuestDAO;
import com.khietvan.hotelreservation.dao.ReservationDAO;
import com.khietvan.hotelreservation.dao.RoomDAO;
import com.khietvan.hotelreservation.models.Guest;
import com.khietvan.hotelreservation.models.Reservation;
import com.khietvan.hotelreservation.models.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReservationModifyController {

    // editable fields
    @FXML private TextField customerName;
    @FXML private TextField customerPhone;
    @FXML private TextField customerEmail;
    @FXML private TextField customerAddress;

    // read-only booking dates
    @FXML private TextField checkInDate;
    @FXML private TextField checkOutDate;
    // table
    @FXML private TableView<RoomRow> roomInfo;
    @FXML private TableColumn<RoomRow, Integer> roomId;
    @FXML private TableColumn<RoomRow, String>  roomType;
    @FXML private TableColumn<RoomRow, Double>  roomPrice;
    @FXML private TableColumn<RoomRow, Integer> noOfGuestInRoom;

    // buttons
    @FXML private Button saveBtn;
    @FXML private Button closeBtn;

    private Guest guest;                 // selected guest (mutated on save)
    private Reservation reservation;     // loaded reservation

    private final ObservableList<RoomRow> rows = FXCollections.observableArrayList();
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @FXML
    public void initialize() {
        // table mapping
        roomId.setCellValueFactory(cell -> cell.getValue().roomIdProperty().asObject());
        roomType.setCellValueFactory(cell -> cell.getValue().roomTypeProperty());
        roomPrice.setCellValueFactory(cell -> cell.getValue().roomPriceProperty().asObject());
        noOfGuestInRoom.setCellValueFactory(cell -> cell.getValue().guestsProperty().asObject());

        roomInfo.setItems(rows);

        // buttons
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());
        saveBtn.setOnAction(e -> saveEdits());
    }

    public void loadData(Guest g) {
        this.guest = g;
        // fill editable fields
        customerName.setText(g.getName());
        customerPhone.setText(g.getPhone());
        customerEmail.setText(g.getEmail());
        customerAddress.setText(g.getAddress());

        // load reservation + rooms
        reservation = ReservationDAO.getReservationWithRoomsByGuestId(g.getGuestId());
        if (reservation == null) {
            showInfo("No active reservation found for this guest.");
            // still allow editing personal info
            checkInDate.setText("");
            checkOutDate.setText("");
            rows.clear();
            return;
        }

        checkInDate.setText(reservation.getCheckInDate().format(DTF));
        checkOutDate.setText(reservation.getCheckOutDate().format(DTF));

        // Fill room rows from reservation.getRoomsAndGuestCounts()
        rows.clear();
        for (Map.Entry<Integer, Integer> e : reservation.getRoomsAndGuestCounts().entrySet()) {
            Room r = RoomDAO.getRoomById(e.getKey());
            if (r != null) {
                rows.add(new RoomRow(r.getRoomId(), r.getRoomType().name(), r.getPrice(), e.getValue()));
            } else {
                rows.add(new RoomRow(e.getKey(), "UNKNOWN", 0.0, e.getValue()));
            }
        }
    }

    private void saveEdits() {
        String name = customerName.getText().trim();
        String phone = customerPhone.getText().trim();
        String email = customerEmail.getText().trim();
        String addr = customerAddress.getText().trim();

        if (name.isEmpty()) { showWarn("Name is required."); return; }
        if (phone.isEmpty()) { showWarn("Phone is required."); return; }
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showWarn("Please enter a valid email address.");
            return;
        }

        boolean ok = GuestDAO.updateGuestBasic(guest.getGuestId(), name, phone, email, addr);
        if (!ok) { showErr("Failed to update guest."); return; }

        // mutate current object so parent table can optionally refresh later
        guest.setName(name);
        guest.setPhone(phone);
        guest.setEmail(email);
        guest.setAddress(addr);

        showInfo("Saved changes for " + name + ".");
        ((Stage) saveBtn.getScene().getWindow()).close();
    }

    private void showInfo(String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void showWarn(String m) { new Alert(Alert.AlertType.WARNING, m).showAndWait(); }
    private void showErr(String m)  { new Alert(Alert.AlertType.ERROR, m).showAndWait(); }

    // --- simple row model for the table ---
    public static class RoomRow {
        private final javafx.beans.property.IntegerProperty roomId = new javafx.beans.property.SimpleIntegerProperty();
        private final javafx.beans.property.StringProperty  roomType = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.DoubleProperty  roomPrice = new javafx.beans.property.SimpleDoubleProperty();
        private final javafx.beans.property.IntegerProperty guests = new javafx.beans.property.SimpleIntegerProperty();

        public RoomRow(int roomId, String roomType, double roomPrice, int guests) {
            this.roomId.set(roomId);
            this.roomType.set(roomType);
            this.roomPrice.set(roomPrice);
            this.guests.set(guests);
        }

        public int getRoomId() { return roomId.get(); }
        public javafx.beans.property.IntegerProperty roomIdProperty() { return roomId; }

        public String getRoomType() { return roomType.get(); }
        public javafx.beans.property.StringProperty roomTypeProperty() { return roomType; }

        public double getRoomPrice() { return roomPrice.get(); }
        public javafx.beans.property.DoubleProperty roomPriceProperty() { return roomPrice; }

        public int getGuests() { return guests.get(); }
        public javafx.beans.property.IntegerProperty guestsProperty() { return guests; }
    }
}
