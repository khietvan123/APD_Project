package com.khietvan.hotelreservation.controllers.fastCheckIn;

import com.khietvan.hotelreservation.controllers.PromptGuestsDetailsController;
import com.khietvan.hotelreservation.dao.GuestDAO;
import com.khietvan.hotelreservation.dao.ReservationDAO;
import com.khietvan.hotelreservation.dao.RoomDAO;
import com.khietvan.hotelreservation.models.Guest;
import com.khietvan.hotelreservation.models.Reservation;
import com.khietvan.hotelreservation.models.Room;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class FastCheckInController {

    @FXML private TextField adultField, childField, seniorField;
    @FXML private DatePicker checkInDate, checkOutDate;
    @FXML private Button addRoomBtn, saveBtn, nextBtn;
    @FXML private ComboBox<String> roomBox1, roomBox2, roomBox3, roomBox4, roomBox5, roomBox6;
    @FXML private ComboBox<Integer> guestNo1, guestNo2, guestNo3, guestNo4, guestNo5, guestNo6;

    private final List<ComboBox<String>> roomBoxes   = new ArrayList<>();
    private final List<ComboBox<Integer>> guestNoBoxes = new ArrayList<>();
    private final Map<Room,Integer> selectedRooms     = new LinkedHashMap<>();
    private Guest currentGuest;

    @FXML
    public void initialize() {
        roomBoxes.addAll(List.of(roomBox1, roomBox2, roomBox3, roomBox4, roomBox5, roomBox6));
        guestNoBoxes.addAll(List.of(guestNo1, guestNo2, guestNo3, guestNo4, guestNo5, guestNo6));

        for (int i = 0; i < roomBoxes.size(); i++) {
            ComboBox<String> rb = roomBoxes.get(i);
            ComboBox<Integer> gb = guestNoBoxes.get(i);
            rb.getItems().addAll("SINGLE","DOUBLE","DELUX","PENTHOUSE");
            rb.setVisible(false);
            gb.setVisible(false);
            rb.setOnAction(e -> updateGuestNoOptions(rb.getValue(), gb));
        }

        addRoomBtn.setOnAction(e -> showNextRoomBox());
        saveBtn.setOnAction(e -> suggestRooms());
        nextBtn.setOnAction(this::handleNext);
    }

    private void showNextRoomBox() {
        for (int i = 0; i < roomBoxes.size(); i++) {
            if (!roomBoxes.get(i).isVisible()) {
                roomBoxes.get(i).setVisible(true);
                guestNoBoxes.get(i).setVisible(true);
                break;
            }
        }
    }

    private void updateGuestNoOptions(String type, ComboBox<Integer> box) {
        box.getItems().clear();
        if (type == null) return;
        switch(type) {
            case "SINGLE"   -> box.getItems().addAll(1,2);
            case "DOUBLE"   -> box.getItems().addAll(2,3,4);
            case "DELUX", "PENTHOUSE" -> box.getItems().addAll(1,2);
        }
        box.getSelectionModel().selectFirst();
    }

    private void suggestRooms() {
        int total = parseInt(adultField.getText())
                + parseInt(childField.getText())
                + parseInt(seniorField.getText());
        if (total==0) {
            showAlert("Please enter number of guests.");
            return;
        }

        var suggestions = getSuggestedRoomTypes(total);
        if (suggestions.isEmpty()) {
            showAlert("No suitable room combination found.");
            return;
        }

        // hide all
        roomBoxes.forEach(cb -> cb.setVisible(false));
        guestNoBoxes.forEach(cb -> cb.setVisible(false));

        // show + prefill
        for (int i = 0; i < suggestions.size(); i++) {
            var e = suggestions.get(i);
            var rb = roomBoxes.get(i);
            var gb = guestNoBoxes.get(i);
            rb.setVisible(true);
            gb.setVisible(true);
            rb.setValue(e.getKey());
            updateGuestNoOptions(e.getKey(), gb);
            if (gb.getItems().contains(e.getValue())) {
                gb.setValue(e.getValue());
            }
        }
    }

    /** Returns a list like [("DOUBLE",4)], [("DOUBLE",4),("SINGLE",1)], etc. */
    private List<Map.Entry<String,Integer>> getSuggestedRoomTypes(int totalGuests) {
        List<Map.Entry<String,Integer>> out = new ArrayList<>();
        if (totalGuests <= 0) return out;

        int fullDoubles = totalGuests / 4;
        int rem = totalGuests % 4;

        // add full doubles
        for (int i=0; i<fullDoubles; i++) {
            out.add(new AbstractMap.SimpleEntry<>("DOUBLE",4));
        }

        // remainder
        if (rem>0) {
            if (rem <=2) {
                out.add(new AbstractMap.SimpleEntry<>("SINGLE", rem));
            } else {
                // rem==3 or rem==3? either way, one DOUBLE is cheaper
                out.add(new AbstractMap.SimpleEntry<>("DOUBLE", rem));
            }
        }

        return out;
    }

    private void handleNext(ActionEvent ev) {
        if (!validateDates()) return;

        int totalGuests = parseInt(adultField.getText())
                + parseInt(childField.getText())
                + parseInt(seniorField.getText());
        if (totalGuests==0) {
            showAlert("Please enter total guests.");
            return;
        }

        selectedRooms.clear();
        for (int i=0; i<roomBoxes.size(); i++) {
            if (!roomBoxes.get(i).isVisible()) continue;
            String type = roomBoxes.get(i).getValue();
            Integer ppl = guestNoBoxes.get(i).getValue();
            if (type==null || ppl==null) {
                showAlert("Complete all room/guest selections.");
                return;
            }
            Room room = RoomDAO.findAvailableRoom(type,
                    checkInDate.getValue(), checkOutDate.getValue());
            if (room==null) {
                showAlert("No available "+type+" rooms.");
                return;
            }
            selectedRooms.put(room, ppl);
        }

        // build reservation
        Reservation reservation = new Reservation(
                0, 0,
                checkInDate.getValue(),
                checkOutDate.getValue(),
                false
        );
        selectedRooms.forEach((r, cnt) ->
                reservation.addRoom(r.getRoomId(), cnt)
        );

        // load guest details
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/khietvan/hotelreservation/prompt-guests-details.fxml"
            ));
            Parent root = loader.load();
            PromptGuestsDetailsController ctrl = loader.getController();
            ctrl.initGuestSubmission(guest -> {
                currentGuest = guest;
                // save guest
                Guest saved = GuestDAO.saveGuestAndReturn(currentGuest);
                if (saved==null) {
                    showAlert("Failed to save guest.");
                    return;
                }
                int guestId = saved.getGuestId();
                currentGuest.setGuestId(guestId);
                reservation.setGuestId(guestId);

                // mark rooms booked immediately
                selectedRooms.keySet().forEach(room -> {
                    room.setStatus(false);
                    RoomDAO.updateRoomStatus(room.getRoomId(), false);
                });

                // save reservation & link
                int resId = ReservationDAO.insertReservationWithRooms(reservation);
                reservation.setReservationId(resId);
                currentGuest.setReservation(reservation);

                showAlert("Reservation completed for "+guest.getName());
            });

            // pass data to prompt controller
            ctrl.setReservationData(
                    parseInt(adultField.getText()),
                    parseInt(childField.getText()),
                    parseInt(seniorField.getText()),
                    totalGuests,
                    checkInDate.getValue(),
                    checkOutDate.getValue(),
                    reservation
            );

            Stage st = new Stage();
            st.setScene(new Scene(root));
            st.setTitle("Enter Guest Info");
            st.show();

            // close this
            ((Stage)((Node)ev.getSource()).getScene().getWindow()).close();

        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Failed to load guest info screen.");
        }
    }

    private boolean validateDates() {
        LocalDate in = checkInDate.getValue(), out = checkOutDate.getValue();
        LocalDate today = LocalDate.now();
        if (in==null || out==null) {
            showAlert("Select both check-in and check-out.");
            return false;
        }
        if (in.isBefore(today)) {
            showAlert("Check-in cannot be before today.");
            return false;
        }
        if (!out.isAfter(in)) {
            showAlert("Check-out must be after check-in.");
            return false;
        }
        return true;
    }

    private int parseInt(String txt) {
        try { return Integer.parseInt(txt.trim()); }
        catch (Exception e) { return 0; }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
}
