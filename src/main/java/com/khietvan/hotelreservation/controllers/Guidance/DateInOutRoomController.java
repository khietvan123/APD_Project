package com.khietvan.hotelreservation.controllers.Guidance;

import com.khietvan.hotelreservation.controllers.PromptGuestsDetailsController;
import com.khietvan.hotelreservation.dao.RoomDAO;
import com.khietvan.hotelreservation.models.Reservation;
import com.khietvan.hotelreservation.models.Room;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateInOutRoomController {

    @FXML private DatePicker checkInDate;
    @FXML private DatePicker checkOutDate;

    @FXML private ComboBox<String> room1, room2, room3, room4, room5, room6;
    @FXML private ComboBox<Integer> guestNo1, guestNo2, guestNo3, guestNo4, guestNo5, guestNo6;

    @FXML private Button cheapestOptionBtn, nextBtn, addRoomBtn, backBtn;

    private int adults;
    private int children;
    private int seniors;

    public void setGuestCounts(int adults, int children, int seniors) {
        this.adults = adults;
        this.children = children;
        this.seniors = seniors;
    }


    private Reservation currentReservation;

    private final List<ComboBox<String>> roomBoxes = new ArrayList<>();
    private final List<ComboBox<Integer>> guestNoBoxes = new ArrayList<>();

    private static class RoomTypeInfo {
        String name;
        int minGuests;
        int maxGuests;
        double price;

        RoomTypeInfo(String name, int min, int max, double price) {
            this.name = name;
            this.minGuests = min;
            this.maxGuests = max;
            this.price = price;
        }
    }

    private final List<RoomTypeInfo> roomTypes = List.of(
            new RoomTypeInfo("SINGLE", 1, 2, 150),
            new RoomTypeInfo("DOUBLE", 2, 4, 250),
            new RoomTypeInfo("DELUX", 1, 2, 200),
            new RoomTypeInfo("PENTHOUSE", 1, 2, 500)
    );

    @FXML
    private void initialize() {
        roomBoxes.addAll(List.of(room1, room2, room3, room4, room5, room6));
        guestNoBoxes.addAll(List.of(guestNo1, guestNo2, guestNo3, guestNo4, guestNo5, guestNo6));

        for (int i = 0; i < roomBoxes.size(); i++) {
            ComboBox<String> roomBox = roomBoxes.get(i);
            ComboBox<Integer> guestBox = guestNoBoxes.get(i);

            roomBox.getItems().addAll("SINGLE", "DOUBLE", "DELUX", "PENTHOUSE");
            roomBox.setVisible(false);
            guestBox.setVisible(false);

            roomBox.setOnAction(e -> updateGuestNo(roomBox.getValue(), guestBox));
        }

        cheapestOptionBtn.setOnAction(e -> suggestCheapestRooms());
        nextBtn.setOnAction(e -> handleNext(e));
        backBtn.setOnAction(e -> goBack());
        addRoomBtn.setOnAction(e -> showNextRoomBox());
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

    private void suggestCheapestRooms() {
        int totalGuests = adults + children + seniors;
        if (totalGuests == 0) {
            showAlert("Guest counts missing.");
            return;
        }

        LocalDate today = LocalDate.now();
        if (checkInDate.getValue() == null || checkOutDate.getValue() == null) {
            showAlert("Please select check-in and check-out dates.");
            return;
        }
        if (checkInDate.getValue().isBefore(today)) {
            showAlert("Check-In cannot be in the past.");
            return;
        }
        if (!checkOutDate.getValue().isAfter(checkInDate.getValue())) {
            showAlert("Check-Out must be after Check-In.");
            return;
        }

        List<RoomTypeInfo> suggestedRooms = getCheapestRoomCombo(adults, children, seniors);
        if (suggestedRooms.isEmpty()) {
            showAlert("Sorry, we cannot accommodate that many guests.");
            return;
        }

        int remaining = totalGuests;

        // Hide everything first
        for (int i = 0; i < roomBoxes.size(); i++) {
            roomBoxes.get(i).setVisible(false);
            guestNoBoxes.get(i).setVisible(false);
        }

        // Show suggested rooms
        for (int i = 0; i < suggestedRooms.size(); i++) {
            RoomTypeInfo info = suggestedRooms.get(i);
            ComboBox<String> roomBox = roomBoxes.get(i);
            ComboBox<Integer> guestBox = guestNoBoxes.get(i);

            roomBox.setVisible(true);
            guestBox.setVisible(true);
            roomBox.setValue(info.name);

            guestBox.getItems().clear();
            for (int g = info.minGuests; g <= info.maxGuests; g++) {
                guestBox.getItems().add(g);
            }

            int assign = Math.min(info.maxGuests, remaining);
            guestBox.getSelectionModel().select(Integer.valueOf(assign));
            remaining -= assign;
        }
    }

    private List<RoomTypeInfo> getCheapestRoomCombo(int adults, int children, int seniors) {
        int total = adults + children + seniors;
        List<RoomTypeInfo> result = new ArrayList<>();

        if (total == 0) return result;

        if (total == 1) {
            result.add(findRoomType("SINGLE"));
            return result;
        }

        List<List<RoomTypeInfo>> options = new ArrayList<>();

        int doublesNeeded = (int)Math.ceil(total / 4.0);
        List<RoomTypeInfo> opt1 = new ArrayList<>();
        for (int i = 0; i < doublesNeeded; i++) opt1.add(findRoomType("DOUBLE"));
        options.add(opt1);

        int fullDoubles = total / 4;
        int remainder = total % 4;
        List<RoomTypeInfo> opt2 = new ArrayList<>();
        for (int i = 0; i < fullDoubles; i++) opt2.add(findRoomType("DOUBLE"));
        if (remainder > 0) {
            if (remainder <= 2) opt2.add(findRoomType("SINGLE"));
            else opt2.add(findRoomType("DOUBLE"));
        }
        options.add(opt2);

        if (total <= 6) {
            List<RoomTypeInfo> opt3 = new ArrayList<>();
            for (int i = 0; i < Math.ceil(total / 2.0); i++) opt3.add(findRoomType("SINGLE"));
            options.add(opt3);
        }

        result = options.stream()
                .min((a, b) -> Double.compare(totalCost(a), totalCost(b)))
                .orElse(new ArrayList<>());

        return result;
    }

    private double totalCost(List<RoomTypeInfo> rooms) {
        return rooms.stream().mapToDouble(r -> r.price).sum();
    }

    private RoomTypeInfo findRoomType(String name) {
        return roomTypes.stream()
                .filter(rt -> rt.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    private void finalizeReservation() {
        int totalGuests = adults + children + seniors;
        currentReservation = new Reservation(
                0,
                0,
                checkInDate.getValue(),
                checkOutDate.getValue(),
                false
        );


        showAlert("Reservation saved for " + totalGuests + " guests. Please proceed to staff kiosk.");
        ((Stage) nextBtn.getScene().getWindow()).close();
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/khietvan/hotelreservation/Guidance/Number-of-people.fxml"
            ));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Number of People");
            stage.show();

            // Close current window
            ((Stage) backBtn.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading Number of People screen.");
        }
    }

    @FXML
    private void handleNext(ActionEvent event) {
        try {
            // Validate dates
            LocalDate today = LocalDate.now();
            if (checkInDate.getValue() == null || checkOutDate.getValue() == null) {
                showAlert("Please select both Check-In and Check-Out dates.");
                return;
            }
            if (checkInDate.getValue().isBefore(today)) {
                showAlert("Check-In date cannot be before today.");
                return;
            }
            if (!checkOutDate.getValue().isAfter(checkInDate.getValue())) {
                showAlert("Check-Out date must be after Check-In date.");
                return;
            }

            int totalGuests = adults + children + seniors;
            if (totalGuests == 0) {
                showAlert("Please enter the number of guests.");
                return;
            }

            // Get selected rooms
            Map<Integer, Integer> selectedRooms = collectSelectedRooms();
            if (selectedRooms.isEmpty()) {
                showAlert("Please select at least one room.");
                return;
            }

            // Create Reservation
            currentReservation = new Reservation(
                    0, // Reservation ID
                    0, // Guest ID
                    checkInDate.getValue(),
                    checkOutDate.getValue(),
                    false
            );
            currentReservation.setRoomsReserved(selectedRooms);

            // Load guest details screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/khietvan/hotelreservation/prompt-guests-details.fxml"));
            Parent root = loader.load();

            PromptGuestsDetailsController controller = loader.getController();
            controller.setReservationData(
                    adults, children, seniors,
                    totalGuests,
                    checkInDate.getValue(),
                    checkOutDate.getValue(),
                    currentReservation
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Guest Details");
            stage.show();

            ((Stage)((Node)event.getSource()).getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading guest details screen.");
        }
    }

    private Map<Integer, Integer> collectSelectedRooms() {
        Map<Integer, Integer> roomMap = new HashMap<>();

        List<Room> availableRooms = RoomDAO.getAllAvailableRoomsSortedByPrice(checkInDate.getValue(), checkOutDate.getValue());

        for (int i = 0; i < roomBoxes.size(); i++) {
            ComboBox<String> roomTypeBox = roomBoxes.get(i);
            ComboBox<Integer> guestNoBox = guestNoBoxes.get(i);

            if (roomTypeBox.isVisible() && roomTypeBox.getValue() != null && guestNoBox.getValue() != null) {
                String type = roomTypeBox.getValue();
                int guestCount = guestNoBox.getValue();

                // Find first matching available room
                Room selectedRoom = availableRooms.stream()
                        .filter(r -> r.getRoomType().toString().equalsIgnoreCase(type))
                        .findFirst()
                        .orElse(null);

                if (selectedRoom != null) {
                    roomMap.put(selectedRoom.getRoomId(), guestCount);
                    availableRooms.remove(selectedRoom); // Avoid double use
                }
            }
        }
        return roomMap;
    }



    private void updateGuestNo(String roomType, ComboBox<Integer> guestBox) {
        guestBox.getItems().clear();
        if (roomType == null) {
            guestBox.setVisible(false);
            return;
        }
        switch (roomType) {
            case "SINGLE": guestBox.getItems().addAll(1, 2); break;
            case "DOUBLE": guestBox.getItems().addAll(2, 3, 4); break;
            case "DELUX":
            case "PENTHOUSE": guestBox.getItems().addAll(1, 2); break;
        }
        guestBox.setVisible(true);
        guestBox.getSelectionModel().selectFirst();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
