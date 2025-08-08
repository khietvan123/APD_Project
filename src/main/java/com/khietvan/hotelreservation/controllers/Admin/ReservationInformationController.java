package com.khietvan.hotelreservation.controllers.Admin;

import com.khietvan.hotelreservation.dao.ReservationDAO;
import com.khietvan.hotelreservation.dao.RoomDAO;
import com.khietvan.hotelreservation.models.Guest;
import com.khietvan.hotelreservation.models.Reservation;
import com.khietvan.hotelreservation.models.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReservationInformationController {

    @FXML private TextField customerName;
    @FXML private TextField customerPhone;
    @FXML private TextField customerAddress;
    @FXML private TextField checkInDate;
    @FXML private TextField checkOutDate;

    @FXML private TableView<RoomInfo> roomInfo;
    @FXML private TableColumn<RoomInfo, Integer> roomId;
    @FXML private TableColumn<RoomInfo, String> roomType;
    @FXML private TableColumn<RoomInfo, Double> roomPrice;
    @FXML private TableColumn<RoomInfo, Integer> noOfGuestInRoom;

    @FXML private Button closeBtn;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        roomId.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        roomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        roomPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        noOfGuestInRoom.setCellValueFactory(new PropertyValueFactory<>("noOfGuests"));

        closeBtn.setOnAction(e ->
                ((Stage) closeBtn.getScene().getWindow()).close()
        );
    }

    /**
     * Populate the UI with this guest’s booking.
     */
    public void loadData(Guest guest) {
        customerName.setText(guest.getName());
        customerPhone.setText(guest.getPhone());
        customerAddress.setText(guest.getAddress());

        Reservation res = ReservationDAO.getReservationByGuestId(guest.getGuestId());
        if (res == null) return;

        checkInDate.setText(res.getCheckInDate().format(fmt));
        checkOutDate.setText(res.getCheckOutDate().format(fmt));

        ObservableList<RoomInfo> rows = FXCollections.observableArrayList();
        for (Map.Entry<Integer,Integer> e : res.getReservedRooms().entrySet()) {
            Room r = RoomDAO.getRoomById(e.getKey());
            rows.add(new RoomInfo(
                    r.getRoomId(),
                    r.getRoomType().name(),
                    r.getPrice(),
                    e.getValue()
            ));
        }
        roomInfo.setItems(rows);
    }

    /** Simple data-holder for the TableView. */
    public static class RoomInfo {
        private final Integer roomId;
        private final String roomType;
        private final Double price;
        private final Integer noOfGuests;

        public RoomInfo(int roomId, String roomType, double price, int noOfGuests) {
            this.roomId = roomId;
            this.roomType = roomType;
            this.price = price;
            this.noOfGuests = noOfGuests;
        }
        public Integer getRoomId()     { return roomId; }
        public String  getRoomType()   { return roomType; }
        public Double  getPrice()      { return price; }
        public Integer getNoOfGuests() { return noOfGuests; }
    }
}
